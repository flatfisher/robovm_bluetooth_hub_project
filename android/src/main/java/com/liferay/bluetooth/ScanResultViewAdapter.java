package com.liferay.bluetooth;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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

        viewHolder.container.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                CheckBox checkBox = viewHolder.saveCheck;

                if (checkBox.isChecked()){

                    checkBox.setChecked(false);

                }else{

                    checkBox.setChecked(true);

                }

            }

        });

        viewHolder.configuration.setText(configuration);

        if(DataManager.isCheckedDevice(deviceName,context)){

            viewHolder.saveCheck.setChecked(true);

        }

        if (configuration.equals(Constants.NO_CONFIG_MESSAGE)){

            viewHolder.saveCheck.setVisibility(View.INVISIBLE);

        }

        viewHolder.saveCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    if (!configuration.equals(Constants.NO_CONFIG_MESSAGE)){

                        DataManager.saveCheckedDevice(context,deviceName);

                        System.out.println(DataManager.getCheckedList(context));

                    }

                }else{

                    if (!configuration.equals(Constants.NO_CONFIG_MESSAGE)) {

                        DataManager.removeCheckedDevice(context, deviceName);

                        System.out.println(DataManager.getCheckedList(context));


                    }

                }

            }

        });

    }

    public static class ScanResultHolder extends RecyclerView.ViewHolder {

        public LinearLayout container;

        public TextView deviceName;

        public TextView configuration;

        public CheckBox saveCheck;

        public ScanResultHolder(View itemView) {

            super(itemView);

            container = (LinearLayout)itemView.findViewById(R.id.container);

            deviceName = (TextView)itemView.findViewById(R.id.device_name_text);

            configuration = (TextView)itemView.findViewById(R.id.config_info_text);

            saveCheck = (CheckBox)itemView.findViewById(R.id.setting_config_check);

        }

    }

}
