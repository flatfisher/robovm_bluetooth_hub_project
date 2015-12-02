package com.liferay.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddDeviceActivity extends Activity {

    class ScanResult {

        public String deviceName;

        public String configuration;

    }

    private RecyclerView scanResultView;

    private RecyclerView.LayoutManager layoutManager;

    private List<ScanResult> scanResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_device);

        scanResultList = new ArrayList<ScanResult>();

        scanResultView = (RecyclerView)findViewById(R.id.scan_result_view);

        scanResultView.setLayoutManager(new LinearLayoutManager(this));

        scanResultView.setHasFixedSize(true);

        scanResultView.addItemDecoration(new DividerItemDecoration(this));
        
    }

    private void setScanResultOnRecyclerView(){

        scanResultView.setAdapter(new ScanResultViewAdapter(this, scanResultList));

    }

}
