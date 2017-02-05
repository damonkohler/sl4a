package com.googlecode.android_scripting.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import com.googlecode.android_scripting.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by shimoda on 17/01/29.
 */

public class BluetoothNonpublicApi {
    // BUG: negative number = under searching...
    public static final int PRIORITY_AUTO_CONNECT = 1000;
    public static final int PRIORITY_ON = 100;
    public static final int PRIORITY_OFF = 0;
    public static final int PRIORITY_UNDEFINED = -65536;

    public static final int MAP = 9;
    public static final int PAN = 5;
    public static final int PBAP = 6;
    public static final int MAP_CLIENT = -65536;
    public static final int PBAP_CLIENT = -65535;
    public static final int A2DP_SINK = 11;
    public static final int HEADSET_CLIENT = 16;
    public static final int INPUT_DEVICE = 4;
    public static final int AVRCP_CONTROLLER = 12;

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
}
