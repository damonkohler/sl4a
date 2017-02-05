package com.googlecode.android_scripting.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import com.googlecode.android_scripting.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by shimoda on 17/01/29.
 */

public class BluetoothNonpublicApi {
    // adapter
    public static final int STATE_UNKNOWN = -1;  // temporary
    public static final int STATE_BLE_ON = 15;
    public static final String ACTION_BLE_STATE_CHANGED =
            "android.bluetooth.adapter.action.BLE_STATE_CHANGED";

    // profile
    // BUG: numbers around 65536 = under searching...
    public static final int PRIORITY_AUTO_CONNECT = 1000;
    public static final int PRIORITY_ON = 100;
    public static final int PRIORITY_OFF = 0;
    public static final int PRIORITY_UNDEFINED = -1;

    public static final int MAP = 9;
    public static final int PAN = 5;
    public static final int PBAP = 6;
    public static final int MAP_CLIENT = -65536;
    public static final int PBAP_CLIENT = -65535;
    public static final int A2DP_SINK = 11;
    public static final int HEADSET_CLIENT = 16;
    public static final int INPUT_DEVICE = 4;
    public static final int AVRCP_CONTROLLER = 12;

    // Device
    public static final String ACTION_CONNECTION_ACCESS_REQUEST =
            "android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST";
    public static final String ACTION_CONNECTION_ACCESS_REPLY =
            "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY";
    public static final int PAIRING_VARIANT_CONSENT = 3;

    // MapClient
    public static final String ACTION_MESSAGE_RECEIVED =
            "android.bluetooth.mapmce.profile.action.MESSAGE_RECEIVED";
    public static final String ACTION_MESSAGE_SENT_SUCCESSFULLY =
            "android.bluetooth.mapmce.profile.action.MESSAGE_SENT_SUCCESSFULLY";
    public static final String ACTION_MESSAGE_DELIVERED_SUCCESSFULLY =
            "android.bluetooth.mapmce.profile.action.MESSAGE_DELIVERED_SUCCESSFULLY";

    public static boolean connectProfile(BluetoothProfile prf, BluetoothDevice sink) {
        Log.e("connect     function won't work with no-system app.");
        if (prf == null) {return false;}

        try {
            Method method = prf.getClass().getMethod("connect");
            if(method != null) {
                return (boolean)method.invoke(sink);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean disconnectProfile(BluetoothProfile prf, BluetoothDevice sink) {
        Log.e("disconnect  function won't work with no-system app.");
        if (prf == null) {return false;}

        try {
            Method method = prf.getClass().getMethod("disconnect");
            if(method != null) {
                return (boolean)method.invoke(sink);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean priorityOnProfile(BluetoothProfile prf, BluetoothDevice dev) {
        if (prf == null) {return false;}

        Integer prior = getPriorityProfile(prf, dev);
        if (prior < PRIORITY_ON) {
            return true;
        }
        return setPriorityProfile(prf, dev, prior);
    }

    public static Integer getPriorityProfile(BluetoothProfile prf,
                                             BluetoothDevice dev

    ) {
        Log.e("getPriority function won't work with no-system app.");
        if (prf == null) {return PRIORITY_UNDEFINED;}

        Integer prior = Integer.MIN_VALUE;
        try {
            Method method = prf.getClass().getMethod("getPriority");
            if(method != null) {
                prior = (Integer)method.invoke(dev);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return prior;
    }

    public static boolean setPriorityProfile(BluetoothProfile prf,
                                             BluetoothDevice dev,
                                             int prior

    ) {
        Log.e("setPriority function won't work with no-system app.");
        if (prf == null) {return false;}

        try {
            Method method = prf.getClass().getMethod("setPriority");
            if(method != null) {
                return (boolean)method.invoke(dev, PRIORITY_ON);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Integer getLeState(BluetoothAdapter adp
    ) {
        Log.e("getLeState function won't work with no-system app.");
        if (adp == null) {return STATE_UNKNOWN;}

        try {
            Method method = adp.getClass().getMethod("getLeState");
            if(method != null) {
                return (Integer)method.invoke(adp);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return STATE_UNKNOWN;
    }

    public static void setScanMode(BluetoothAdapter adp,
                                   Integer mode) {

        setScanMode(true, adp, mode, null);
    }

    public static void setScanMode(BluetoothAdapter adp,
                                   Integer mode,
                                   Integer duration) {
        setScanMode(false, adp, mode, duration);
    }

    public static void setScanMode(boolean f2opt,
                                   BluetoothAdapter adp,
                                   Integer mode,
                                   Integer duration) {
        Log.e("setScanMode function won't work with no-system app.");
        if (adp == null) {return;}

        try {
            Method method = adp.getClass().getMethod("setScanMode");
            if(method != null) {
                if (f2opt) {method.invoke(adp, mode);}
                else {method.invoke(adp, mode, duration);}
                return;
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
