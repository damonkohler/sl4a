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

/**
 * This facade exposes basic mediaplayer functioniality
 * 
 * @author Robbie Matthews (rjmatthews62@gmail.com)
 * 
 */

/**
 * Usage Notes:<br>
 * mediaPlayerFacade maintains a list of media streams, identified by a user supplied tag. If the
 * tag is null or blank, this tag defaults to "default"<br>
 * Basic operation is: mediaPlayOpen("file:///sdcard/MP3/sample.mp3","mytag",true)<br>
 * This will look for a media file at /sdcard/MP3/sample.mp3. Other urls should work. If the file
 * exists and is playable, this will return a true otherwise it will return a false.
 * 
 * <br>
 * If play=true, then the media file will play immediately, otherwise it will wait for a
 * mediaPlayStart() command.
 * 
 * <br>
 * When done with the resource, use mediaPlayClose
 * 
 * <br>
 * You can get information about the loaded media with mediaPlayInfo This returns a map with the
 * following elements:
 * <ul>
 * <li>"tag" - tag for this module.
 * <li>"loaded" - true if loaded, false if not. If false, no other elements are returned.
 * <li>"duration" - length of the media in milliseconds.
 * <li>"position" - current position of playback in milliseconds. Controlled by mediaPlaySeek
 * <li>"isplaying" - shows whether media is playing. Controlled by mediaPlayPause and mediaPlayStart
 * <li>"url" - the url used to open this media.
 * <li>"looping" - whether media will loop. Controlled by mediaSetLooping
 * </ul>
 * <br>
 * You can use mediaPlayList to get a list of the loaded tags. <br>
 * mediaIsPlaying will return true if the media is playing. (mediaPlayInfo)
 * 
 * NB: In remote mode, a media file will continue playing after the script has finished unless an
 * explicit "mediaPlayClose" event is called.
 */

public class MediaPlayerFacade extends RpcReceiver {

  private final Service mService;
  static private final Map<String, MediaPlayer> mPlayers = new Hashtable<String, MediaPlayer>();
  static private final Map<String, String> mUrls = new Hashtable<String, String>();

  public MediaPlayerFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
  }

  private String getDefault(String tag) {
    return (tag == null || tag.equals("")) ? "default" : tag;
  }

  private MediaPlayer getMp(String tag) {
    tag = getDefault(tag);
    return mPlayers.get(tag);
  }

  private String getUrl(String tag) {
    tag = getDefault(tag);
    return mUrls.get(tag);
  }

  private void putMp(String tag, MediaPlayer mp, String url) {
    tag = getDefault(tag);
    mPlayers.put(tag, mp);
    mUrls.put(tag, url);
  }

  private void removeMp(String tag) {
    tag = getDefault(tag);
    MediaPlayer mp = mPlayers.get(tag);
    if (mp != null) {
      mp.stop();
      mp.release();
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
    MediaPlayer mp = getMp(tag);
    mp = MediaPlayer.create(mService, Uri.parse(url));
    if (mp != null) {
      putMp(tag, mp, url);
      if (play) {
        mp.start();
      }
    }
    return mp != null;
  }

  @Rpc(description = "pause playing media file", returns = "true if successful")
  public synchronized boolean mediaPlayPause(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer mp = getMp(tag);
    if (mp == null) {
      return false;
    }
    mp.pause();
    return true;
  }

  @Rpc(description = "start playing media file", returns = "true if successful")
  public synchronized boolean mediaPlayStart(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer mp = getMp(tag);
    if (mp == null) {
      return false;
    }
    mp.start();
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
    MediaPlayer mp = getMp(tag);
    return (mp == null) ? false : mp.isPlaying();
  }

  @Rpc(description = "Information on current media", returns = "Media Information")
  public synchronized Map<String, Object> mediaPlayInfo(
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    Map<String, Object> result = new HashMap<String, Object>();
    MediaPlayer mp = getMp(tag);
    result.put("tag", getDefault(tag));
    if (mp == null) {
      result.put("loaded", false);
    } else {
      result.put("loaded", true);
      result.put("duration", mp.getDuration());
      result.put("position", mp.getCurrentPosition());
      result.put("isplaying", mp.isPlaying());
      result.put("url", getUrl(tag));
      result.put("looping", mp.isLooping());
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
    MediaPlayer mp = getMp(tag);
    if (mp == null) {
      return false;
    }
    mp.setLooping(enabled);
    return true;
  }

  @Rpc(description = "Seek To Position", returns = "New Position (in ms)")
  public synchronized int mediaPlaySeek(
      @RpcParameter(name = "msec", description = "Position in millseconds") Integer msec,
      @RpcParameter(name = "tag", description = "string identifying resource") @RpcDefault(value = "default") String tag) {
    MediaPlayer mp = getMp(tag);
    if (mp == null) {
      return 0;
    }
    mp.seekTo(msec);
    return mp.getCurrentPosition();
  }

  @Override
  public synchronized void shutdown() {
    for (String key : mPlayers.keySet()) {
      MediaPlayer mp = mPlayers.get(key);
      if (mp != null) {
        mp.stop();
        mp.release();
        mp = null;
      }
    }
    mPlayers.clear();
    mUrls.clear();
  }
}
