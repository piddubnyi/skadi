#Skadi-simple v2
Fork of original Skadi with some functions reworked:
Allows you to comfortably watch Twitch channels via streamlink / VLC (or any videoplayer compatible with streamlink).
No browser needed - you only watch plain stream in video player.

## Download
see https://github.com/piddubnyi/skadi-simple/releases

## Required software
* ~~[Java 1.8u60+]~~[Any Java version](https://www.java.com/download/)
* [streamlink](https://github.com/streamlink/streamlink/releases)
* ~~[Chrome]~~(https://www.google.com/chrome/)
* [VLC](https://www.videolan.org/vlc/) (or any videoplayer compatible with streamlink, see  [streamlink documentation](http://docs.streamlink.io/players.html))

Make sure to keep streamlink and Java up to date.

## Features
* ~~version check / update download~~
* import followed channels
* channel filtering
* streams can be opened in all available stream qualities
* ~~chats are opened in chrome/chromium~~ no chat, Just watch your stream.
* channel detail pane (double click on a channel or click the 'i' button) showing the channel panels, preview, stats, sub-emotes and a viewer graph
* light and dark theme
* table and grid view
* logging
* ~~notifications if a channel goes live~~ Suitable for work, nothing bothers you
* minimize to tray (https://javafx-jira.kenai.com/browse/RT-17503 uses old AWT API)
* drag and drop channel names/urls do add
* channel auto updated every 60s / force refresh
* twitch-auth and followed sync between Skadi and Twitch

### TODO
If you have a feature request or found a bug, create a new issue (and include the log file if appropriate).

## Setup
If Skadi fails to open streams or chats with the default configuration values (see log file or the status bar), you might need to change the paths to streamlink in the settings dialog. Often the problem isn't related to Skadi but to streamlink, so you might want to check if streamlink is setup correctly first.
The log and config are in ./.skadi
There is a button to open the log in the settings dialog.

## Usage
launch Skadi via `java -jar skadi.jar` if a double click on the jar does not work.

## Building
Skadi uses maven as build tool.
use `mvn package` to build Skadi, result can be found at `target/Skadi-xyz.jar`
