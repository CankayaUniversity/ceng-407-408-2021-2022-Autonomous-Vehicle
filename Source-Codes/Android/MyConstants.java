package com.p4f.esp32camai;

public class MyConstants {

    static final String INTENT_ACTION_DISCONNECT = BuildConfig.APPLICATION_ID + ".Disconnect";
    static final String NOTIFICATION_CHANNEL = BuildConfig.APPLICATION_ID + ".Channel";
    static final String INTENT_CLASS_MAIN_ACTIVITY = BuildConfig.APPLICATION_ID + ".MainActivity";

    static final int NOTIFY_MANAGER_START_FOREGROUND_SERVICE = 1001;

    public enum MODEL_TYPE {
        FLOAT32,
        UINT8
    };

    static final boolean DEBUG = false;

    private MyConstants() {}
}
