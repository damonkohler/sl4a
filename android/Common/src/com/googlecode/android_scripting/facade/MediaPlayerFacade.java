package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.media.MediaPlayer;
import android.net.Uri;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * This facade exposes basic mediaplayer functioniality
 * 
 * @author Robbie Matthews (rjmatthews62@gmail.com)
 * 
 */

public class MediaPlayerFacade extends RpcReceiver {

  private final Service mService;
  MediaPlayer mp = null;
  String mUrl;

  public MediaPlayerFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
  }

  @Rpc(description = "Open a media file", returns = "true if play successful")
  public boolean mediaPlay(
      @RpcParameter(name = "url", description = "url of media resource") String url,
      @RpcParameter(name = "play", description = "start playing immediately") @RpcDefault(value = "true") Boolean play) {
    if (mp != null) {
      mp.stop();
      mp.release();
      mp = null;
      mUrl = null;
    }
    mp = MediaPlayer.create(mService, Uri.parse(url));
    if (mp != null && play) {
      mUrl = url;
      mp.start();
    }
    return mp != null;
  }

  @Rpc(description = "pause playing media file", returns = "true if successful")
  public boolean mediaPlayPause() {
    if (mp == null) {
      return false;
    }
    mp.pause();
    return true;
  }

  @Rpc(description = "start playing media file", returns = "true if successful")
  public boolean mediaPlayStart() {
    if (mp == null) {
      return false;
    }
    mp.start();
    return mediaIsPlaying();
  }

  @Rpc(description = "Close media file", returns = "true if successful")
  public boolean mediaPlayClose() {
    if (mp == null) {
      return false;
    }
    mp.release();
    mp = null;
    mUrl = null;
    return true;
  }

  @Rpc(description = "Checks if media file is playing.", returns = "true if playing")
  public boolean mediaIsPlaying() {
    return (mp == null) ? false : mp.isPlaying();
  }

  @Rpc(description = "Information on current media", returns = "Media Information")
  public Map<String, Object> mediaPlayInfo() {
    Map<String, Object> result = new HashMap<String, Object>();
    if (mp == null) {
      result.put("loaded", false);
    } else {
      result.put("loaded", true);
      result.put("duration", mp.getDuration());
      result.put("position", mp.getCurrentPosition());
      result.put("isplaying", mp.isPlaying());
      result.put("url", mUrl);
    }
    return result;
  }

  @Override
  public void shutdown() {
    if (mp != null) {
      mp.release();
      mp = null;
    }
  }
}
