package com.testbluebooth.longwu;

import android.app.Application;

public class BaseApplication extends Application {

    public BaseApplication() {
    }

    private static BaseApplication baseApplication = null;
    public static  BaseApplication getInstance(){
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this;
    }
}
