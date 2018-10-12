package com.testbluebooth.longwu.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.testbluebooth.longwu.callback.PinBlueCallBack;

public class PinBluetoothReceiver extends BroadcastReceiver {
    private final String TAG = PinBluetoothReceiver.class.getSimpleName();
    private String pin = "0000";
    private  PinBlueCallBack callBack;

    public PinBluetoothReceiver(PinBlueCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
