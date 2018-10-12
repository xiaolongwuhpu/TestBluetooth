package com.testbluebooth.longwu.callback;

import android.bluetooth.BluetoothDevice;

public abstract interface PinBlueCallBack {

    public void onScanStarted();

    public void onScanFinished();

    public void onScanning(BluetoothDevice device);

    public void onBondSuccess(BluetoothDevice device);

    public void onBonding(BluetoothDevice device);

    public void onBondFail(BluetoothDevice device);

    public void onBondRequest();
}
