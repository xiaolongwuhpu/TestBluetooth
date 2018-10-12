package com.testbluebooth.longwu.adapter;

import android.bluetooth.BluetoothDevice;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.testbluebooth.longwu.R;

import java.util.List;

public class SimulateItemDataAdapter extends BaseQuickAdapter<BluetoothDevice,BaseViewHolder> {
    private String[] bondState = new String[]{"未绑定","绑定中","已绑定"};
    public SimulateItemDataAdapter(int layoutResId, List<BluetoothDevice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice item) {
        helper.setText(R.id.tv_blue_name, item.getName())
                .setText(R.id.tv_blue_address, item.getAddress())
                .setText(R.id.tv_blue_state, bondState[item.getBondState()-10]);
    }
}
