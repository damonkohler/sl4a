/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.facade.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.content.ComponentName;
import android.content.Context;

import android.media.MediaMetadata;
import android.media.MediaMetadataRetriever;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
// import com.googlecode.android_scripting.facade.bluetooth.media.BluetoothSL4AAudioSrcMBS;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.googlecode.android_scripting.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SL4A Facade for running Bluetooth Media related test cases
 * The APIs provided here can be grouped into 3 categories:
 * 1. Those that can run on both an Audio Source and Sink
 * 2. Those that makes sense to run only on a Audio Source like a phone
 * 3. Those that makes sense to run only on a Audio Sink like a Car.
 *
 * This media test framework consists of 3 classes:
 * 1. BluetoothMediaFacade - this class that provides the APIs that a RPC client can interact with
 * 2. BluetoothSL4AMBS - This is a MediaBrowserService that is intended to run on the Audio Source
 * (phone).  This MediaBrowserService that runs as part of the SL4A app is used to intercept
 * Media key events coming in from a AVRCP Controller like Car.  Intercepting these events lets us
 * instrument the Bluetooth media related tests.
 * 3. BluetoothMediaPlayback - The class that the MediaBrowserService uses to play media files.
 * It is a UI-less MediaPlayer that serves the purpose of Bluetooth Media testing.
 *
 * The idea is for the BluetoothMediaFacade to create a BluetoothSL4AMBS MediaSession on the
 * Phone (Bluetooth Audio source/Avrcp Target) and use it intercept the Media commands coming
 * from the CarKitt (Bluetooth Audio Sink / Avrcp Controller).
 * On the Carkitt side, we just create and connect a MediaBrowser to the A2dpMediaBrowserService
 * that is part of the Carkitt's Bluetooth Audio App.  We use this browser to send media commands
 * to the Phone side and intercept the commands with the BluetoothSL4AMBS.
 * This set up helps to instrument tests that can test various Bluetooth Media usecases.
 */

public class BluetoothMediaFacade extends RpcReceiver {
    private static final String TAG = "BluetoothMediaFacade";
    private static final boolean VDBG = false;
    private final Service mService;
    private final Context mContext;
    private Handler mHandler;
    private MediaSessionManager mSessionManager;
    private MediaController mMediaController = null;
    private MediaController.Callback mMediaCtrlCallback = null;
    private MediaSessionManager.OnActiveSessionsChangedListener mSessionListener;
    private MediaBrowser mBrowser = null;

    private static EventFacade mEventFacade;
    // Events posted
    private static final String EVENT_PLAY_RECEIVED = "playReceived";
    private static final String EVENT_PAUSE_RECEIVED = "pauseReceived";
    private static final String EVENT_SKIP_PREV_RECEIVED = "skipPrevReceived";
    private static final String EVENT_SKIP_NEXT_RECEIVED = "skipNextReceived";

    // Commands received
    private static final String CMD_MEDIA_PLAY = "play";
    private static final String CMD_MEDIA_PAUSE = "pause";
    private static final String CMD_MEDIA_SKIP_NEXT = "skipNext";
    private static final String CMD_MEDIA_SKIP_PREV = "skipPrev";

    private static final String BLUETOOTH_PKG_NAME = "com.android.bluetooth";
    private static final String BROWSER_SERVICE_NAME =
            "com.android.bluetooth.a2dpsink.mbs.A2dpMediaBrowserService";
    private static final String A2DP_MBS_TAG = "A2dpMediaBrowserService";

    // MediaMetadata keys
    private static final String MEDIA_KEY_TITLE = "keyTitle";
    private static final String MEDIA_KEY_ALBUM = "keyAlbum";
    private static final String MEDIA_KEY_ARTIST = "keyArtist";
    private static final String MEDIA_KEY_DURATION = "keyDuration";
    private static final String MEDIA_KEY_NUM_TRACKS = "keyNumTracks";

    /**
     * Following things are initialized here:
     * 1. Setup Listeners to Active Media Session changes
     * 2. Create a new MediaController.callback instance
     */
    public BluetoothMediaFacade(FacadeManager manager) {
        super(manager);
        mService = manager.getService();
        mEventFacade = manager.getReceiver(EventFacade.class);
        mHandler = new Handler(Looper.getMainLooper());
        mContext = mService.getApplicationContext();
        mSessionManager =
                (MediaSessionManager) mContext.getSystemService(mContext.MEDIA_SESSION_SERVICE);
        mSessionListener = new SessionChangeListener();
        // Listen on Active MediaSession changes, so we can get the active session's MediaController
        if (mSessionManager != null) {
            ComponentName compName =
                    new ComponentName(mContext.getPackageName(), this.getClass().getName());
            mSessionManager.addOnActiveSessionsChangedListener(mSessionListener, null,
                    mHandler);
            if (VDBG) {
                List<MediaController> mcl = mSessionManager.getActiveSessions(null);
                Log.d(TAG + " Num Sessions " + mcl.size());
                for (int i = 0; i < mcl.size(); i++) {
                    Log.d(TAG + "Active session : " + i + ((MediaController) (mcl.get(
                            i))).getPackageName() + mcl.get(i));
                    // ((MediaController) (mcl.get(i))).getTag());
                }
            }
        }
        mMediaCtrlCallback = new MediaControllerCallback();
    }

    /**
     * The listener that was setup for listening to changes to Active Media Sessions.
     * This listener is useful in both Car and Phone sides.
     */
    private class SessionChangeListener
            implements MediaSessionManager.OnActiveSessionsChangedListener {
        /**
         * On the Phone side, it listens to the BluetoothSL4AAudioSrcMBS (that the SL4A app runs)
         * becoming active.
         * On the Car side, it listens to the A2dpMediaBrowserService (associated with the
         * Bluetooth Audio App) becoming active.
         * The idea is to get a handle to the MediaController appropriate for the device, so
         * that we can send and receive Media commands.
         */
        @Override
        public void onActiveSessionsChanged(List<MediaController> controllers) {
            if (VDBG) {
                Log.d(TAG + " onActiveSessionsChanged : " + controllers.size());
                for (int i = 0; i < controllers.size(); i++) {
                    Log.d(TAG + "Active session : " + i + ((MediaController) (controllers.get(
                            i))).getPackageName());  /* + ((MediaController) (controllers.get(
                            i))).getTag()); */
                }
            }
            // As explained above, looking for the BluetoothSL4AAudioSrcMBS (when running on Phone)
            // or A2dpMediaBrowserService (when running on Carkitt).
            for (int i = 0; i < controllers.size(); i++) {
                MediaController controller = (MediaController) controllers.get(i);
                Log.e("MediaController.getTag won't work in no-system app." + controller);
                /* TODO: try to implement.
                if ((controller.getTag().contains(BluetoothSL4AAudioSrcMBS.getTag()))
                        || (controller.getTag().contains(A2DP_MBS_TAG))) {
                    setCurrentMediaController(controller);
                    return;
                }
                 */
            }
        }
    }

    /**
     * When the MediaController for the required MediaSession is obtained, register for its
     * callbacks.
     * Not used yet, but this can be used to verify state changes in both ends.
     */
    private class MediaControllerCallback extends MediaController.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            Log.d(TAG + " onPlaybackStateChanged: " + state.getState());
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            Log.d(TAG + " onMetadataChanged ");
        }
    }

    /**
     * Callback on <code>MediaBrowser.connect()</code>
     * This is relevant only on the Carkitt side, since the intent is to connect a MediaBrowser
     * to the A2dpMediaBrowser Service that is run by the Car's Bluetooth Audio App.
     * On successful connection, we obtain the handle to the corresponding MediaController,
     * so we can imitate sending media commands via the Bluetooth Audio App.
     */
    MediaBrowser.ConnectionCallback mBrowserConnectionCallback =
            new MediaBrowser.ConnectionCallback() {
                private static final String classTag = TAG + " BrowserConnectionCallback";

                @Override
                public void onConnected() {
                    Log.d(classTag + " onConnected: session token " + mBrowser.getSessionToken());
                    MediaController mediaController = new MediaController(mContext,
                            mBrowser.getSessionToken());
                    // Update the MediaController
                    setCurrentMediaController(mediaController);
                }

                @Override
                public void onConnectionFailed() {
                    Log.d(classTag + " onConnectionFailed");
                }
            };

    /**
     * Update the Current MediaController.
     * As has been commented above, we need the MediaController handles to the
     * BluetoothSL4AAudioSrcMBS on Phone and A2dpMediaBrowserService on Car to send and receive
     * media commands.
     *
     * @param controller - Controller to update with
     */
    private void setCurrentMediaController(MediaController controller) {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        if (mMediaController == null && controller != null) {
            Log.d(TAG + " Setting MediaController ");  // + controller.getTag());
            mMediaController = controller;
            mMediaController.registerCallback(mMediaCtrlCallback);
        } else if (mMediaController != null && controller != null) {
            // We have a new MediaController that we have to update to.
            if (controller.getSessionToken().equals(mMediaController.getSessionToken())
                    == false) {
                Log.d(TAG + " Changing MediaController ");  //  + controller.getTag());
                mMediaController.unregisterCallback(mMediaCtrlCallback);
                mMediaController = controller;
                mMediaController.registerCallback(mMediaCtrlCallback, mainHandler);
            }
        } else if (mMediaController != null && controller == null) {
            // Clearing the current MediaController
            Log.d(TAG + " Clearing MediaController ");  // + mMediaController.getTag());
            mMediaController.unregisterCallback(mMediaCtrlCallback);
            mMediaController = controller;
        }
    }

    /**
     * Class method called from {@link BluetoothSL4AAudioSrcMBS} to post an Event through
     * EventFacade back to the RPC client.
     * This is dispatched from the Phone to the host (RPC Client) to acknowledge that it
     * received a playback command.
     *
     * @param playbackState PlaybackState change that is posted as an Event to the client.
     */
    public static void dispatchPlaybackStateChanged(int playbackState) {
        Bundle news = new Bundle();
        switch (playbackState) {
            case PlaybackState.STATE_PLAYING:
                mEventFacade.postEvent(EVENT_PLAY_RECEIVED, news);
                break;
            case PlaybackState.STATE_PAUSED:
                mEventFacade.postEvent(EVENT_PAUSE_RECEIVED, news);
                break;
            case PlaybackState.STATE_SKIPPING_TO_NEXT:
                mEventFacade.postEvent(EVENT_SKIP_NEXT_RECEIVED, news);
                break;
            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                mEventFacade.postEvent(EVENT_SKIP_PREV_RECEIVED, news);
                break;
            default:
                break;
        }
    }

    /******************************RPC APIS************************************************/

    /**
     * Relevance - Phone and Car.
     * Sends the passthrough command through the currently active MediaController.
     * If there isn't one, look for the currently active sessions and just pick the first one,
     * just a fallback.
     * This function is generic enough to be used in either a Phone or the Car side, since
     * all this does is to pick the currently active Media Controller and sends a passthrough
     * command.  In the test setup, this is used to mimic sending a passthrough command from
     * Car.
     */
    @Rpc(description = "Simulate a passthrough command")
    public void bluetoothMediaPassthrough(
            @RpcParameter(name = "passthruCmd", description = "play/pause/skipFwd/skipBack")
                    String passthruCmd) {
        Log.d(TAG + "Passthrough Cmd " + passthruCmd);
        if (mMediaController == null) {
            Log.i(TAG + " Media Controller not ready - Grabbing existing one");
            ComponentName name =
                    new ComponentName(mContext.getPackageName(),
                            mSessionListener.getClass().getName());
            List<MediaController> listMC = mSessionManager.getActiveSessions(null);
            if (listMC.size() > 0) {
                if (VDBG) {
                    Log.d(TAG + " Num Sessions " + listMC.size());
                    for (int i = 0; i < listMC.size(); i++) {
                        Log.d(TAG + "Active session : " + i + ((MediaController) (listMC.get(
                                i))).getPackageName());
                        // TODO: try to implement: + (MediaController) (listMC.get(i))).getTag());
                    }
                }
                mMediaController = (MediaController) listMC.get(0);
            } else {
                Log.d(TAG + " No Active Media Session to grab");
                return;
            }
        }

        switch (passthruCmd) {
            case CMD_MEDIA_PLAY:
                mMediaController.getTransportControls().play();
                break;
            case CMD_MEDIA_PAUSE:
                mMediaController.getTransportControls().pause();
                break;
            case CMD_MEDIA_SKIP_NEXT:
                mMediaController.getTransportControls().skipToNext();
                break;
            case CMD_MEDIA_SKIP_PREV:
                mMediaController.getTransportControls().skipToPrevious();
                break;
            default:
                Log.d(TAG + " Unsupported Passthrough Cmd");
                break;
        }
    }

    /**
     * Relevance - Phone and Car.
     * Returns the currently playing media's metadata.
     * Can be queried on the car and the phone in the middle of a streaming session to
     * verify they are in sync.
     *
     * @return Currently playing Media's metadata
     */
    @Rpc(description = "Gets the Metadata of currently playing Media")
    public Map<String, String> bluetoothMediaGetCurrentMediaMetaData() {
        Map<String, String> track = null;
        if (mMediaController == null) {
            Log.d(TAG + "MediaController Not set");
            return track;
        }
        MediaMetadata metadata = mMediaController.getMetadata();
        if (metadata == null) {
            Log.e("No Metadata available.");
            return track;
        }
        track = new HashMap<>();
        track.put(MEDIA_KEY_TITLE, metadata.getString(MediaMetadata.METADATA_KEY_TITLE));
        track.put(MEDIA_KEY_ALBUM, metadata.getString(MediaMetadata.METADATA_KEY_ALBUM));
        track.put(MEDIA_KEY_ARTIST, metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));
        track.put(MEDIA_KEY_DURATION,
                String.valueOf(metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)));
        track.put(MEDIA_KEY_NUM_TRACKS,
                String.valueOf(metadata.getLong(MediaMetadata.METADATA_KEY_NUM_TRACKS)));
        return track;
    }

    /**
     * Relevance - Phone and Car
     * Returns the current active media sessions for the device. This is useful to see if a
     * Media Session we are interested in is currently active.
     * In the Bluetooth Media tests, this is indirectly used to determine if audio is being
     * played via BT.  For ex., when the Car and Phone are connected via BT and audio is being
     * streamed, A2dpMediaBrowserService will be active on the Car side.  If the connection is
     * terminated in the middle, A2dpMediaBrowserService will no longer be active on the Carkitt,
     * whereas BluetoothSL4AAudioSrcMBS will still be active.
     *
     * @return A list of names of the active media sessions
     */
    @Rpc(description = "Get the current active Media Sessions")
    public List<String> bluetoothMediaGetActiveMediaSessions() {
        List<MediaController> controllers = mSessionManager.getActiveSessions(null);
        List<String> sessions = new ArrayList<String>();
        for (MediaController mc : controllers) {
            sessions.add(mc.toString());
            // sessions.add(mc.getTag());
        }
        return sessions;
    }

    /**
     * Relevance - Car Only
     * Called from the Carkitt to connect a MediaBrowser to the Bluetooth Audio App's
     * A2dpMediaBrowserService.  The callback on successful connection gives the handle to
     * the MediaController through which we can send media commands.
     */
    @Rpc(description = "Connect a MediaBrowser to the A2dpMediaBrowserservice in the Carkitt")
    public void bluetoothMediaConnectToCarMBS() {
        // Create a MediaBrowser to connect to the A2dpMBS
        if (mBrowser == null) {
            final ComponentName compName =
                    new ComponentName(BLUETOOTH_PKG_NAME, BROWSER_SERVICE_NAME);
            // Note - MediaBrowser connect needs to be done on the Main Thread's handler,
            // otherwise we never get the ServiceConnected callback.
            Runnable createAndConnectMediaBrowser = new Runnable() {
                @Override
                public void run() {
                    mBrowser = new MediaBrowser(mContext, compName, mBrowserConnectionCallback,
                            null);
                    if (mBrowser != null) {
                        Log.d(TAG + " Connecting to MBS");
                        mBrowser.connect();
                    } else {
                        Log.d(TAG + " Failed to create a MediaBrowser");
                    }
                }
            };

            Handler mainHandler = new Handler(mContext.getMainLooper());
            mainHandler.post(createAndConnectMediaBrowser);
        } //mBrowser
    }

    /**
     * Relevance - Phone Only
     * Start the BluetoothSL4AAudioSrcMBS on the Phone so the media commands coming in
     * via Bluetooth AVRCP can be intercepted by the SL4A test
     */
    @Rpc(description = "Start the BluetoothSL4AAudioSrcMBS on Phone.")
    public void bluetoothMediaPhoneSL4AMBSStart() {
        Log.d(TAG + "Starting BluetoothSL4AAudioSrcMBS");
        // Start the Avrcp Media Browser service.  Starting it sets it to active.
        /* TODO: try to implement.
        Intent startIntent = new Intent(mContext, BluetoothSL4AAudioSrcMBS.class);
        mContext.startService(startIntent);
         */
    }

    /**
     * Relevance - Phone Only
     * Stop the BluetoothSL4AAudioSrcMBS
     */
    @Rpc(description = "Stop the BluetoothSL4AAudioSrcMBS running on Phone.")
    public void bluetoothMediaPhoneSL4AMBSStop() {
        Log.d(TAG + "Stopping BluetoothSL4AAudioSrcMBS");
        // Stop the Avrcp Media Browser service.
        /* TODO: try to implement.
        Intent stopIntent = new Intent(mContext, BluetoothSL4AAudioSrcMBS.class);
        mContext.stopService(stopIntent);
         */
    }

    /**
     * Relevance - Phone only
     * This is used to simulate play/pause/skip media commands on the Phone directly, as against
     * receiving these commands via AVRCP from the Carkitt.
     * This function talks to the BluetoothSL4AAudioSrcMBS to simulate the media command.
     * An example test where this would be useful - Play music on Phone that is not connected
     * on bluetooth and connect in the middle to verify if music is steamed to the other end.
     *
     * @param command - Media command to simulate on the Phone
     */
    @Rpc(description = "Media Commands on the Phone's BluetoothAvrcpMBS.")
    public void bluetoothMediaHandleMediaCommandOnPhone(String command) {
        /* TODO: try to implement.
        BluetoothSL4AAudioSrcMBS mbs =
                BluetoothSL4AAudioSrcMBS.getAvrcpMediaBrowserService();
        if (mbs != null) {
            mbs.handleMediaCommand(command);
        } else {
            Log.e(TAG + " No BluetoothSL4AAudioSrcMBS running on the device");
        }
         */
    }


    @Override
    public void shutdown() {
        setCurrentMediaController(null);
    }
}
