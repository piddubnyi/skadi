/*******************************************************************************
 * Copyright (c) 2014 Jan Strauß
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package eu.over9000.skadi.channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import eu.over9000.skadi.gui.SkadiGUI;
import eu.over9000.skadi.logging.SkadiLogging;
import eu.over9000.skadi.util.ChannelDataRetriever;

/**
 * Singleton class responsible for scheduling the updating of channel metadata.
 * 
 * @author Jan Strauß
 * 
 */
public class ChannelUpdater implements ChannelEventListener {
	
	private static final int THREAD_COUNT = 5;
	private static final int UPDATE_PERIOD = 1;
	
	private static volatile ChannelUpdater instance; // volatile because of a findbugs warning
	
	private ChannelUpdater() {
		ChannelManager.getInstance().addListener(this);
	}
	
	public static ChannelUpdater getInstance() {
		if (ChannelUpdater.instance == null) {
			ChannelUpdater.instance = new ChannelUpdater();
		}
		return ChannelUpdater.instance;
	}
	
	private final ScheduledExecutorService executorService = Executors
	        .newScheduledThreadPool(ChannelUpdater.THREAD_COUNT);
	private final Map<Channel, ScheduledFuture<?>> tasks = new HashMap<>();
	
	private Runnable createUpdateTask(final Channel channel) {
		return new Runnable() {
			
			@Override
			public void run() {
				final ChannelMetadata newMetadata = ChannelDataRetriever.getChannelMetadata(channel.getURL());
				channel.updateMetadata(newMetadata);
			}
		};
	}
	
	public void stopUpdater() {
		this.executorService.shutdown();
	}
	
	@Override
	public void added(final Channel channel, final int count) {
		final Runnable updateJob = this.createUpdateTask(channel);
		final ScheduledFuture<?> future = this.executorService.scheduleAtFixedRate(updateJob, 0,
		        ChannelUpdater.UPDATE_PERIOD, TimeUnit.MINUTES);
		this.tasks.put(channel, future);
	}
	
	@Override
	public void removed(final Channel channel, final int count) {
		final ScheduledFuture<?> task = this.tasks.remove(channel);
		if (task != null) {
			task.cancel(false);
		}
	}
	
	@Override
	public void updatedMetadata(final Channel channel) {
	}
	
	@Override
	public void updatedStreamdata(final Channel channel) {
	}
	
	@Override
	public String getListenerName() {
		return this.getClass().getName();
	}
	
	public void forceUpdateAll() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				SkadiLogging.log("starting manual channel update");
				
				final Set<Callable<Void>> tasks = new HashSet<>();
				for (final Channel channel : ChannelManager.getInstance().getChannels()) {
					final Callable<Void> task = new Callable<Void>() {
						
						@Override
						public Void call() throws Exception {
							final ChannelMetadata newMetadata = ChannelDataRetriever.getChannelMetadata(channel
							        .getURL());
							channel.updateMetadata(newMetadata);
							return null;
						}
					};
					tasks.add(task);
				}
				
				try {
					ChannelUpdater.this.executorService.invokeAll(tasks);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				
				SkadiLogging.log("finished manual channel update.");
				SkadiGUI.getInstance().onForcedRefreshFinished();
			}
		}, "forced channel update performer").start();
		
	}
	
}
