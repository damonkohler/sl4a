package com.google.ase;

import java.net.InetSocketAddress;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.google.ase.jsonrpc.JsonRpcServer;

public class AndroidProxyService extends Service {
  private AndroidFacade mAndroidFacade;
  private AndroidMediaFacade mAndroidMediaFacade;
  private NotificationManager mNotificationManager;
  private JsonRpcServer mRpcServer;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    boolean usePublicIp = intent.getBooleanExtra(Constants.EXTRA_USE_EXTERNAL_IP, false);
    mAndroidFacade = new AndroidFacade(this, new Handler(), intent);
    mAndroidMediaFacade = new AndroidMediaFacade();
    mRpcServer = JsonRpcServer.create(mAndroidFacade, mAndroidMediaFacade);

    final InetSocketAddress address = usePublicIp ?
        mRpcServer.startPublic() : mRpcServer.startLocal();

    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    String ticker = String.format("ASE running on %s:%d", address.getHostName(), address.getPort());
    Notification notification =
        new Notification(R.drawable.ase_logo_48, ticker, System.currentTimeMillis());
    Intent notificationIntent = new Intent(this, ScriptKiller.class);
    notificationIntent.setAction(Constants.ACTION_KILL_SERVICE);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    String message =
        String.format("%s:%d - Tap to stop.", address.getHostName(), address.getPort());
    notification.setLatestEventInfo(this, "ASE Network Service", message, contentIntent);
    notification.flags = Notification.FLAG_NO_CLEAR;
    mNotificationManager.notify(0, notification);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mRpcServer.shutdown();
    mNotificationManager.cancelAll();
    Toast.makeText(this, "ASE network service stopped.", Toast.LENGTH_SHORT).show();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
