package com.testbluebooth.longwu.adapter;

import android.bluetooth.BluetoothDevice;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.testbluebooth.longwu.R;

import java.util.List;

public class BluthoothItemDataAdapter extends BaseQuickAdapter<BluetoothDevice,BaseViewHolder> {
    public BluthoothItemDataAdapter(int layoutResId, List<BluetoothDevice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice item) {
        helper.setText(R.id.name, item.getName())
                .setText(R.id.address, item.getAddress() +";  type="+item.getType());
    }
}
