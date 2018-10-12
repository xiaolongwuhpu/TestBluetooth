package com.testbluebooth.longwu.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.testbluebooth.longwu.R;
import com.testbluebooth.longwu.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.send)
    Button send;
    private String TAG = this.getClass().getSimpleName();
    @BindView(R.id.tv_info)
    TextView tvInfo;
    @BindView(R.id.state)
    Button state;
    @BindView(R.id.discovery)
    Button discovery;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
//bluetoothDevice是dervices中选中的一项 bluetoothDevice=dervices.get(i);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initGPSPermission();



    }

    private int bluetoothState = 0;//0 关闭  1打开

    private void initGPSPermission() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean granted) throws Exception {
                        if (granted) {
                            // All requested permissions are granted
                            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter == null) {
                                ToastUtil.showLong("蓝牙找不到了");
                                return;
                            }

                            if (bluetoothAdapter.isEnabled()) {//是否已经打开蓝牙开关
                                bluetoothState = 1;
                                ToastUtil.showLong("open ok");
                                state.setText("已经打开,可以关闭");
                                discovery.setEnabled(true);


                            } else {
                                ToastUtil.showLong("closed");
                                state.setText("已经关闭,可以打开");
                                bluetoothState = 0;
                                discovery.setEnabled(false);
                            }


                        } else {
                            // At least one permission is denied
                        }
                    }
                });
    }

    @OnClick({R.id.state, R.id.discovery,R.id.send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.state:
                if (bluetoothState == 0) {
                    //获取buletobothAdapter并打开蓝牙
                    bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    bluetoothAdapter = bluetoothAdapter.getDefaultAdapter();
                    if (!bluetoothAdapter.isEnabled()) {
                        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);  // 弹对话框的形式提示用户开启蓝牙
                    }
                } else {


                }
                break;
            case R.id.discovery:
                //在onCreat()中添加
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                bluetoothLeScanner.startScan(scanCallback);//android5.0把扫描方法单独弄成一
                //然后在onclick（）中调用即可
                handler.postDelayed(runnable, 10000);

                break;
            case  R.id.send:
                byte[] senddatas =new byte[]{1,2,3,4,34,56,78,90};
                bluetoothGattCharacteristic.setValue(senddatas);
                bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);

                break;
        }
    }

    //定义对象
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();//存放扫描结果
    //startScan()回调函数
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult results) {
            super.onScanResult(callbackType, results);
             bluetoothDevice = results.getDevice();
            if (!devices.contains(bluetoothDevice)) {  //判断是否已经添加
                devices.add(bluetoothDevice);//也可以添加devices.getName()到列表，这里省略
            }

            // callbackType：回调类型
            // result：扫描的结果，不包括传统蓝牙

           if(bluetoothDevice!=null)
            //选择bluetoothDevice后配置回调函数
            bluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {//状态变为 已连接
                        Log.e(TAG, "成功建立连接");
                    }
                    gatt.discoverServices();//连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务
                    if (newState == BluetoothGatt.STATE_DISCONNECTED) { //状态变为 未连接
                        Toast.makeText(MainActivity.this, "连接断开", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                public void onServicesDiscovered(BluetoothGatt gatt, final int status) {
                    //用此函数接收数据
                    super.onServicesDiscovered(gatt, status);
                    String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";//已知服务
                    String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";//已知特征
                    try {
                        bluetoothGattService = bluetoothGatt.getService(UUID.fromString(service_UUID));//通过UUID找到服务
                        bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristic_UUID));//找到服务后在通过UUID找到特征
                        if (bluetoothGattCharacteristic != null) {
                            gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true);//启用onCharacteristicChanged(），用于接收数据
                            //Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "发现服务失败", Toast.LENGTH_LONG).show();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return;
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    //发现服务后的响应函数
                    byte[] bytesreceive = characteristic.getValue();
                    Log.e(TAG,bytesreceive[0]+""+bytesreceive[1]+""+bytesreceive[2]+""+bytesreceive[4]);

                    tvInfo.setText(bytesreceive[0]+""+bytesreceive[1]+""+bytesreceive[2]+""+bytesreceive[4]);
                }
            });



        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    };
    private Handler handler = new Handler();//import android.os.Handler;


    //服务  特性
    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    @OnClick(R.id.send)
    public void onViewClicked() {
    }
}
