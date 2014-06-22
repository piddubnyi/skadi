package eu.over9000.skadi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import eu.over9000.skadi.channel.Channel;
import eu.over9000.skadi.gui.ImportDialog;
import eu.over9000.skadi.gui.SkadiGUI;
import eu.over9000.skadi.io.PersistenceManager;
import eu.over9000.skadi.logging.SkadiLogging;
import eu.over9000.skadi.updater.ChannelUpdater;
import eu.over9000.skadi.util.ChannelDataRetriever;
import eu.over9000.skadi.util.SkadiVersionChecker;

public class SkadiMain {
	
	public static final String CLIENT_ID = "i2uu9j43ure9x7n4ojpgg4hvcnw6y91";
	
	private static SkadiMain instance;
	
	private List<Channel> channels = new ArrayList<>();
	
	public String chrome_exec = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
	public String livestreamer_exec = "C:\\Program Files (x86)\\Livestreamer\\livestreamer.exe";
	public String vlc_exec = "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe";
	
	public boolean use_livestreamer = true;
	
	private ChannelUpdater updater;
	
	public static SkadiMain getInstance() {
		if (SkadiMain.instance == null) {
			SkadiMain.instance = new SkadiMain();
		}
		return SkadiMain.instance;
	}
	
	public static void main(final String[] args) {
		SkadiMain.getInstance().runInit(args);
	}
	
	private void runInit(final String[] args) {
		SkadiVersionChecker.checkVersion();
		
		this.addShutdownHook();
		PersistenceManager.getInstance().loadData();
		SkadiGUI.create();
		this.updater = new ChannelUpdater();
		
		for (final Channel channel : this.channels) {
			this.updater.scheduleChannel(channel);
		}
	}
	
	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				SkadiLogging.log("STOPPING UPDATER");
				SkadiMain.this.updater.stopUpdater();
				SkadiLogging.log("KILLING STREAMS/CHATS..");
				for (final Channel instance : SkadiMain.this.channels) {
					instance.closeStreamAndChat();
				}
				SkadiLogging.log("SAVING DATA..");
				PersistenceManager.getInstance().saveData();
				SkadiLogging.log("SHUTDOWN COMPLETE");
				
			}
		}));
	}
	
	public boolean addNewChannel(String url) {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		
		if (!SkadiMain.validateURL(url)) {
			SkadiLogging.log("invalid url given:" + url);
			return false;
		}
		
		if (!(url.startsWith("http://www.twitch.tv/") || url.startsWith("https://www.twitch.tv/"))) {
			if (url.startsWith("www.twitch.tv/")) {
				url = "http://" + url;
			} else if (url.startsWith("twitch.tv/")) {
				url = "http://www." + url;
			} else {
				url = "http://www.twitch.tv/" + url;
			}
		}
		
		if (this.checkIfStreamIsInList(url)) {
			SkadiLogging.log("Channel already in list");
			return false;
		}
		
		if (!ChannelDataRetriever.checkIfChannelExists(url)) {
			SkadiLogging.log("Channel does not exist");
			return false;
		}
		
		final Channel newChannel = new Channel(url);
		
		this.channels.add(newChannel);
		
		SkadiLogging.log("ADDED NEW CHANNEL:" + url);
		
		SkadiGUI.handleChannelTableAdd(newChannel);
		
		this.updater.scheduleChannel(newChannel);
		
		return true;
		
	}
	
	private boolean checkIfStreamIsInList(final String url) {
		for (final Channel channel : this.channels) {
			if (url.equals(channel.getURL())) {
				return true;
			}
		}
		return false;
	}
	
	public String importFollowedChannelsFromTwitch(final String username, final ImportDialog importDialog) {
		importDialog.setIndeterminate(true);
		final Set<String> newChannels = ChannelDataRetriever.getFollowedChannels(username, importDialog);
		
		int count = 0;
		
		final Iterator<String> iterator = newChannels.iterator();
		
		for (int index = 0; index < newChannels.size(); index++) {
			
			final String url = iterator.next();
			final boolean result = this.addNewChannel(url);
			importDialog.updateProgress(newChannels.size(), newChannels.size() + index, "Importing " + index + " of "
			        + newChannels.size() + " channels");
			if (result) {
				count++;
			}
		}
		return "Imported " + count + " of " + newChannels.size() + " followed channels.";
	}
	
	private static boolean validateURL(final String url) {
		return Pattern.matches("(http://)?(www\\.)?(twitch\\.tv/)?[A-Za-z0-9_]+/", url);
	}
	
	public List<Channel> getChannels() {
		return this.channels;
	}
	
	public void setChannels(final List<Channel> newChannels) {
		this.channels = newChannels;
	}
	
	public void deleteChannel(final Channel channel) {
		this.channels.remove(channel);
		this.updater.cancelChannel(channel);
		channel.closeStreamAndChat();
		SkadiGUI.handleChannelTableDelete(channel);
	}
}
