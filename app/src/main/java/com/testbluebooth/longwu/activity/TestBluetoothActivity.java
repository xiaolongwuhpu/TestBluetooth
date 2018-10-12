package com.testbluebooth.longwu.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.testbluebooth.longwu.R;
import com.testbluebooth.longwu.adapter.BluthoothItemDataAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TestBluetoothActivity extends AppCompatActivity {
    @BindView(R.id.btn_discovery)
    Button btnDiscovery;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_bluetooth);
        ButterKnife.bind(this);
        // Initializes Bluetooth adapter.
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // 询问打开蓝牙
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

    }

    // 申请打开蓝牙请求的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "没有蓝牙权限", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.enable();
            }
        }
    }


    @OnClick(R.id.btn_discovery)
    public void onViewClicked() {
        devices.clear();
        mBluetoothAdapter.startLeScan(callback);

        //然后在onclick（）中调用即可
        myHandler.postDelayed(runnable, 5000);
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mBluetoothAdapter.stopLeScan(callback);
            count = 0;
        }
    };

    private int count = 0;
    List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (/*device.getBluetoothClass().getDeviceClass()==BluetoothClass.Device.Major.MISC &&*/ device.getName() != null) {
                if (!devices.contains(device)) {
                    devices.add(device);
                    Log.e("---", device.getName() + " = " + device.getAddress() + ";   type= " + device.getType());
                    if (count == 0) {
                        initAdapter();
                    } else {
                        adapter.setNewData(devices);
                    }
                    count++;
                }

            }
        }

    };

    private List<BluetoothGattService> servicesList;
    /**
     * BluetoothGattService 简称服务，是构成BLE设备协议栈的组成单位，一个蓝牙设备协议栈一般由一个或者多个BluetoothGattService组成。
     * BluetoothGattCharacteristic 简称特征，一个服务包含一个或者多个特征，特征作为数据的基本单元。
     * 一个BluetoothGattCharacteristic特征包含一个数据值和附加的关于特征的描述BluetoothGattDescriptor。
     * BluetoothGattDescriptor：用于描述特征的类，其同样包含一个value值。
     * <p>
     * 链接：https://www.jianshu.com/p/a27f3ca027e3
     */
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.e("===", "设备连接上 开始扫描服务");
                // 开始扫描服务，安卓蓝牙开发重要步骤之一
                gatt.discoverServices();
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e("===", "连接断开");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            //获取服务列表
            servicesList = gatt.getServices();

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    //--------------------------------------
    BluthoothItemDataAdapter adapter;

    private void initAdapter() {
        //创建布局管理
        if (layoutManager == null) {
            layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        }
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getDrawable(R.drawable.item_devider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        //创建适配器
        adapter = new BluthoothItemDataAdapter(R.layout.item_layout, devices);
        //给RecyclerView设置适配器
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                devices.get(position).connectGatt(TestBluetoothActivity.this, false, gattCallback);
            }
        });
    }
}
