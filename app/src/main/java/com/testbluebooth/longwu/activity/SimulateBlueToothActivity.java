package com.testbluebooth.longwu.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.testbluebooth.longwu.R;
import com.testbluebooth.longwu.adapter.SimulateItemDataAdapter;
import com.testbluebooth.longwu.callback.PinBlueCallBack;
import com.testbluebooth.longwu.receiver.PinBluetoothReceiver;
import com.testbluebooth.longwu.util.ToastUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimulateBlueToothActivity extends AppCompatActivity {
    private static String TAG = "SimulateBlueToothActivity";
    @BindView(R.id.rb_close)
    RadioButton rbClose;
    @BindView(R.id.rb_open)
    RadioButton rbOpen;
    @BindView(R.id.btn_discovery)
    Button btnDiscovery;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Context mContext;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_simulate_blue_tooth);
        ButterKnife.bind(this);
        mContext = this;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        mHandler.postDelayed(mRefresh, 50);
        blueReceiver = new BluetoothReceiver();
        //需要过滤多个动作，则调用IntentFilter对象的addAction添加新动作
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        foundFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        foundFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(blueReceiver, foundFilter);
    }

    BluetoothReceiver blueReceiver;

    /**
     * 搜索蓝牙广播接收器
     */
    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "onReceive action=" + action);
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.PHONE_SMART) {
                    devices.add(device);
                    initAdapter();
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mHandler.removeCallbacks(mRefresh);
                btnDiscovery.setText("蓝牙设备搜索完成");
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    btnDiscovery.setText("正在配对" + device.getName());
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    btnDiscovery.setText("完成配对" + device.getName());
                    mHandler.postDelayed(mRefresh, 50);
                } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    btnDiscovery.setText("取消配对" + device.getName());
                }
            } else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {


            }
        }
    }

    private void checkStatus() {
        if (buttonStatus == 0 && bluetoothAdapter.isEnabled()) {
            ToastUtil.showLong("关闭...");
            bluetoothAdapter.disable();
        } else if (buttonStatus == 1 && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            ToastUtil.showLong("打开蓝牙中...");
        }
    }

    private int buttonStatus = 1;

    @OnClick({R.id.rb_close, R.id.rb_open, R.id.btn_discovery})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rb_close:
                cancelDiscovery();
                buttonStatus = 0;
                rbClose.setChecked(true);
                checkStatus();

                break;
            case R.id.rb_open:
                rbOpen.setChecked(true);
                buttonStatus = 1;
                checkStatus();

                break;
            case R.id.btn_discovery:
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                    ToastUtil.showLong("打开蓝牙中...");
                }
                beginDiscovery();
                break;
        }
    }

    private Runnable mRefresh = new Runnable() {
        @Override
        public void run() {
            beginDiscovery();
            mHandler.postDelayed(this, 2000);
        }
    };

    private void cancelDiscovery() {
        mHandler.removeCallbacks(mRefresh);
        btnDiscovery.setText("取消搜索...");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    private void beginDiscovery() {
        if (!bluetoothAdapter.isDiscovering()) {
            btnDiscovery.setText("正在搜索...");
            devices.clear();
            initAdapter();
            bluetoothAdapter.startDiscovery();
        }
    }

    //收到对方发来的消息
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                ToastUtil.showLong("收到消息了");
//                byte[] readBuf = (byte[]) msg.obj;
//                String readMessage = new String(readBuf, 0, msg.arg1);
//                Log.d(TAG, "handleMessage readMessage=" + readMessage);
//                AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothActivity.this);
//                builder.setTitle("我收到消息啦").setMessage(readMessage).setPositiveButton("确定", null);
//                builder.create().show();
            }
        }
    };


    /**
     * 蓝牙配对
     */
    private void pinPair(BluetoothDevice device) {

        if (device == null) {
            ToastUtil.showShort("设配为空");
            return;
        }

        if (!isBluetoothEnable()) {
            ToastUtil.showShort("蓝牙不可用");
            return;
        }

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {//没有配对
            try {
                Method method = device.getClass().getMethod("createBond");
                Boolean value = (Boolean) method.invoke(device);
                value.booleanValue();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "配对尝试失败!");
                ToastUtil.showShort("配对尝试失败!");
            }
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) { //配对中
            ToastUtil.showShort("配对中...");
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {  // 已经配对成功
            ToastUtil.showShort("已经配对成功");
        } else {
            ToastUtil.showShort("最终配对尝试失败!");
        }
    }

    /**
     * 取消配对
     */
    private void cancelPin(BluetoothDevice device) {
        if (device == null) {
            ToastUtil.showShort("设配为空");
            return;
        }
        if (!isBluetoothEnable()) {
            ToastUtil.showShort("蓝牙不可用");
            return;
        }
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {  // 已经配对成功
            try {
                Method method = device.getClass().getMethod("removeBond");
                Boolean value = (Boolean) method.invoke(device);
                value.booleanValue();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "配对取消失败!");
                ToastUtil.showShort("配对取消失败!");
            }
        } else {
            ToastUtil.showShort("最终取消配对尝试失败!");
        }
    }

    /**
     * 蓝牙是否可用
     *
     * @return
     */
    private boolean isBluetoothEnable() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(blueReceiver);
    }

    //-----------------------初始化---------------
    List<BluetoothDevice> devices = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    SimulateItemDataAdapter adapter;

    private void initAdapter() {
        //创建布局管理
        if (layoutManager == null || adapter == null) {
            layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
            dividerItemDecoration.setDrawable(getDrawable(R.drawable.item_devider));
            recyclerView.addItemDecoration(dividerItemDecoration);
            //创建适配器
            adapter = new SimulateItemDataAdapter(R.layout.item_bluetooth, devices);
            //给RecyclerView设置适配器
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//                devices.get(position).connectGatt(mContext, false, gattCallback);
                    ToastUtil.showShort("点击的是:" + devices.get(position).getName());

                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                    filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
                    pinPair(devices.get(position));
                    registerReceiver(pinBluetoothReceiver, filter);//广播接收器 共用一个;
                }
            });
        } else {
            adapter.setNewData(devices);
        }

    }

   private  PinBluetoothReceiver pinBluetoothReceiver = new PinBluetoothReceiver(new PinBlueCallBack() {
        @Override
        public void onScanStarted() {
            ToastUtil.showShort("扫描开始");
        }

        @Override
        public void onScanFinished() {
            ToastUtil.showShort("扫描结束");
        }

        @Override
        public void onScanning(BluetoothDevice device) {
            ToastUtil.showShort("扫描中...");
        }

        @Override
        public void onBondSuccess(BluetoothDevice device) {
            ToastUtil.showShort("配对成功");
        }

        @Override
        public void onBonding(BluetoothDevice device) {
            ToastUtil.showShort("配对中...");
        }

        @Override
        public void onBondFail(BluetoothDevice device) {
            ToastUtil.showShort("配对失败");
        }

        @Override
        public void onBondRequest() {
            ToastUtil.showShort("配对请求");
        }
    });
}
