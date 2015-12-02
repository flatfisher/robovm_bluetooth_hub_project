package com.liferay.bluetooth;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class ScanResultViewAdapter extends RecyclerView.Adapter<ScanResultViewAdapter.ScanResultHolder> {

    private Context context;

    private LayoutInflater layoutInflater;

    private List<AddDeviceActivity.ScanResult> scanResultList;

    public ScanResultViewAdapter(Context context,List<AddDeviceActivity.ScanResult> scanResultList){

        this.context = context;

        layoutInflater = LayoutInflater.from(context);

        this.scanResultList = scanResultList;

    }

    @Override
    public ScanResultViewAdapter.ScanResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = layoutInflater.inflate(R.layout.scan_result_card, parent, false);

        return new ScanResultHolder(v);
    }

    @Override
    public int getItemCount() {

        return scanResultList.size();

    }

    @Override
    public void onBindViewHolder(ScanResultViewAdapter.ScanResultHolder viewHolder, int i) {

        String deviceName = scanResultList.get(i).deviceName;

        String configuration = scanResultList.get(i).configuration;

        viewHolder.deviceName.setText(deviceName);

        viewHolder.configuration.setText(configuration);

    }

    public static class ScanResultHolder extends RecyclerView.ViewHolder {

        public TextView deviceName;

        public TextView configuration;

        public CheckBox saveCheck;

        public ScanResultHolder(View itemView) {

            super(itemView);

            deviceName = (TextView)itemView.findViewById(R.id.device_name_text);

            configuration = (TextView)itemView.findViewById(R.id.config_info_text);

            saveCheck = (CheckBox)itemView.findViewById(R.id.setting_config_check);

        }

    }
}
