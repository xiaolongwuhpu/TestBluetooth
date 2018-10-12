package com.testbluebooth.longwu.receiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.testbluebooth.longwu.callback.PinBlueCallBack;

import java.lang.reflect.Method;

public class PinBluetoothReceiver extends BroadcastReceiver {
    private final String TAG = PinBluetoothReceiver.class.getSimpleName();
    private String pin = "0000";
    private PinBlueCallBack callBack;

    public PinBluetoothReceiver(PinBlueCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (action.equals(BluetoothDevice.ACTION_FOUND)) {

            try {
                callBack.onBondRequest();

                //1.确认配对
                //ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                Method setPairingConfirmation = device.getClass().getDeclaredMethod("setPairingConfirmation", boolean.class);
                setPairingConfirmation.invoke(device, true);
                //2.终止有序广播
                Log.d("order...", "isOrderedBroadcast:" + isOrderedBroadcast() + ",isInitialStickyBroadcast:" + isInitialStickyBroadcast());
                abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                //3.调用setPin方法进行配对...
//                boolean ret = ClsUtils.setPin(device.getClass(), device, pin);
                Method removeBondMethod = device.getClass().getDeclaredMethod("setPin", new Class[]{byte[].class});
                Boolean returnValue = (Boolean) removeBondMethod.invoke(device, new Object[]{pin.getBytes()});
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_NONE:
                    Log.d(TAG, "取消配对");
                    callBack.onBondFail(device);
                    break;
                case BluetoothDevice.BOND_BONDING:
                    Log.d(TAG, "配对中");
                    callBack.onBonding(device);
                    break;
                case BluetoothDevice.BOND_BONDED:
                    Log.d(TAG, "配对成功");
                    callBack.onBondSuccess(device);
                    break;
            }

        } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

        } else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {

        }

    }
}
