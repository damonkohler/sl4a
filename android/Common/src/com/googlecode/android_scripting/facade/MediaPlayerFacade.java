package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.media.MediaPlayer;
import android.net.Uri;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * This facade exposes basic mediaPlayer functionality.
 * 
 * <br>
 * <br>
 * <b>Usage Notes:</b><br>
 * mediaPlayerFacade maintains a list of media streams, identified by a user supplied tag. If the
 * tag is null or blank, this tag defaults to "default"<br>
 * Basic operation is: mediaPlayOpen("file:///sdcard/MP3/sample.mp3","mytag",true)<br>
 * This will look for a media file at /sdcard/MP3/sample.mp3. Other urls should work. If the file
 * exists and is playable, this will return a true otherwise it will return a false.
 * 
 * <br>
 * If play=true, then the media file will play immediately, otherwise it will wait for a
 * {@link #mediaPlayStart mediaPlayerStart} command.
 * 
 * <br>
 * When done with the resource, use {@link #mediaPlayClose mediaPlayClose}
 * 
 * <br>
 * You can get information about the loaded media with {@link #mediaPlayInfo mediaPlayInfo} This
 * returns a map with the following elements:
 * <ul>
 * <li>"tag" - user supplied tag identifying this mediaPlayer.
 * <li>"loaded" - true if loaded, false if not. If false, no other elements are returned.
 * <li>"duration" - length of the media in milliseconds.
 * <li>"position" - current position of playback in milliseconds. Controlled by
 * {@link #mediaPlaySeek mediaPlaySeek}
 * <li>"isplaying" - shows whether media is playing. Controlled by {@link #mediaPlayPause
 * mediaPlayPause} and {@link #mediaPlayStart mediaPlayStart}
 * <li>"url" - the url used to open this media.
 * <li>"looping" - whether media will loop. Controlled by {@link #mediaPlaySetLooping
 * mediaPlaySetLooping}
 * </ul>
 * <br>
 * You can use {@link #mediaPlayList mediaPlayList} to get a list of the loaded tags. <br>
 * {@link #mediaIsPlaying mediaIsPlaying} will return true if the media is playing.<br>
 * 
 * <b>Events:</b><br>
 * A playing media will throw a <b>"media"</b> event on completion.
 * 
 * NB: In remote mode, a media file will continue playing after the script has finished unless an
 * explicit {@link #mediaPlayClose mediaPlayClose} event is called.
 * 
 * @author Robbie Matthews (rjmatthews62@gmail.com)
 */

public class MediaPlayerFacade extends RpcReceiver implements MediaPlayer.OnCompletionListener {

  private final Service mService;
  static private final Map<String, MediaPlayer> mPlayers = new Hashtable<String, MediaPlayer>();
  static private final Map<String, String> mUrls = new Hashtable<String, String>();

  private final EventFacade mEventFacade;

  public MediaPlayerFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mEventFacade = manager.getReceiver(EventFacade.class);
  }

  private String getDefault(String tag) {
    return (tag == null || tag.equals("")) ? "default" : tag;
  }

  private MediaPlayer getPlayer(String tag) {
    tag = getDefault(tag);
    return mPlayers.get(tag);
  }

  private String getUrl(String tag) {
    tag = getDefault(tag);
    return mUrls.get(tag);
  }

  private void putMp(String tag, MediaPlayer player, String url) {
    tag = getDefault(tag);
    mPlayers.put(tag, player);
    mUrls.put(tag, url);
  }

  private void removeMp(String tag) {
    tag = getDefault(tag);
    MediaPlayer player = mPlayers.get(tag);
    if (player != null) {
      player.stop();
      player.release();
    }
    mPlayers.remove(tag);
    mUrls.remove(tag);
  }

  @Rpc(description = "Open a media file", returns = "true if play successful")
  public synchronized boolean mediaPlay(
      @RpcParameter(name = "url", description = "url of media resource") String url,
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag,
      @RpcParameter(name = "play", description = "start playing immediately") @RpcDefault(value = "true") Boolean play) {
    removeMp(tag);
    MediaPlayer player = getPlayer(tag);
    player = MediaPlayer.create(mService, Uri.parse(url));
    if (player != null) {
      putMp(tag, player, url);
      player.setOnCompletionListener(this);
      if (play) {
        player.start();
      }
    }
    return player != null;
  }

  @Rpc(description = "pause playing media file", returns = "true if successful")
  public synchronized boolean mediaPlayPause(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer player = getPlayer(tag);
    if (player == null) {
      return false;
    }
    player.pause();
    return true;
  }

  @Rpc(description = "start playing media file", returns = "true if successful")
  public synchronized boolean mediaPlayStart(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer player = getPlayer(tag);
    if (player == null) {
      return false;
    }
    player.start();
    return mediaIsPlaying(tag);
  }

  @Rpc(description = "Close media file", returns = "true if successful")
  public synchronized boolean mediaPlayClose(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    removeMp(tag);
    return true;
  }

  @Rpc(description = "Checks if media file is playing.", returns = "true if playing")
  public synchronized boolean mediaIsPlaying(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer player = getPlayer(tag);
    return (player == null) ? false : player.isPlaying();
  }

  @Rpc(description = "Information on current media", returns = "Media Information")
  public synchronized Map<String, Object> mediaPlayInfo(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    Map<String, Object> result = new HashMap<String, Object>();
    MediaPlayer player = getPlayer(tag);
    result.put("tag", getDefault(tag));
    if (player == null) {
      result.put("loaded", false);
    } else {
      result.put("loaded", true);
      result.put("duration", player.getDuration());
      result.put("position", player.getCurrentPosition());
      result.put("isplaying", player.isPlaying());
      result.put("url", getUrl(tag));
      result.put("looping", player.isLooping());
    }
    return result;
  }

  @Rpc(description = "Lists currently loaded media", returns = "List of Media Tags")
  public Set<String> mediaPlayList() {
    return mPlayers.keySet();
  }

  @Rpc(description = "Set Looping", returns = "True if successful")
  public synchronized boolean mediaPlaySetLooping(
      @RpcParameter(name = "enabled") @RpcDefault(value = "true") Boolean enabled,
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer player = getPlayer(tag);
    if (player == null) {
      return false;
    }
    player.setLooping(enabled);
    return true;
  }

  @Rpc(description = "Seek To Position", returns = "New Position (in ms)")
  public synchronized int mediaPlaySeek(
      @RpcParameter(name = "msec", description = "Position in millseconds") Integer msec,
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer player = getPlayer(tag);
    if (player == null) {
      return 0;
    }
    player.seekTo(msec);
    return player.getCurrentPosition();
  }

  @Override
  public synchronized void shutdown() {
    for (String key : mPlayers.keySet()) {
      MediaPlayer player = mPlayers.get(key);
      if (player != null) {
        player.stop();
        player.release();
        player = null;
      }
    }
    mPlayers.clear();
    mUrls.clear();
  }

  @Override
  public void onCompletion(MediaPlayer player) {
    String tag = getTag(player);
    if (tag != null) {
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("action", "complete");
      data.put("tag", tag);
      mEventFacade.postEvent("media", data);
    }
  }

  private String getTag(MediaPlayer player) {
    for (Entry<String, MediaPlayer> m : mPlayers.entrySet()) {
      if (m.getValue() == player) {
        return m.getKey();
      }
    }
    return null;
  }
}
