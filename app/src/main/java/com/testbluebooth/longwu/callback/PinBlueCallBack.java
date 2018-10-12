package com.testbluebooth.longwu.callback;

import android.bluetooth.BluetoothDevice;

public abstract interface PinBlueCallBack {

    public void onScanStarted();

    public void onScanFinished();

    public void onScanning(BluetoothDevice device);

    public void onBondSuccess();

    public void onBonding();

    public void onBondFail();

    public void onBondRequest();
}
