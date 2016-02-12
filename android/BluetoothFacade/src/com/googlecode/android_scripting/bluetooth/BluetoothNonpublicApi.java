package com.googlecode.android_scripting.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by shimoda on 17/01/29.
 */

public class BluetoothNonpublicApi {
    public static final int PRIORITY_AUTO_CONNECT = 1000;
    public static final int PRIORITY_ON = 100;
    public static final int PRIORITY_OFF = 0;

    public static final int MAP = 9;

    public static boolean connectProfile(BluetoothProfile prf, BluetoothDevice sink) {
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

        if (prior < PRIORITY_ON) {
            return true;
        }

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
