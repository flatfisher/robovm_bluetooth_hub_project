package com.liferay.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

public class AddDeviceActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{

    class ScanResult {
        public String deviceName;

        public String configuration;
    }

    private ProgressBar progressBar;

    private SwipeRefreshLayout pullToRefresh;

    private RecyclerView scanResultView;

    private List<ScanResult> scanResultList;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner bluetoothLeScanner;

    private ScanSettings scanSettings;

    private List<ScanFilter> filterList;

    private android.os.Handler scanHandler;

    private static final long SCAN_PERIOD = 5000;

    private ConfigManager configManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        pullToRefresh = (SwipeRefreshLayout)findViewById(R.id.pull_to_refresh);

        pullToRefresh.setOnRefreshListener(this);

        progressBar = (ProgressBar)findViewById(R.id.ProgressBar);

        scanResultList = new ArrayList<ScanResult>();

        scanResultView = (RecyclerView) findViewById(R.id.scan_result_view);

        scanResultView.setLayoutManager(new LinearLayoutManager(this));

        scanResultView.setHasFixedSize(true);

        scanResultView.addItemDecoration(new DividerItemDecoration(this));

        startScanDevice();
    }

    @Override
    public void onRefresh() {
        startScanDevice();
    }

    private void startScanDevice(){
        if (DataManager.isCheckConfigData(this)) {
            String configData = DataManager.getConfigData(this);

            configManager = new ConfigManager(configData);

            initializeBluetoothAdapter();

            startScanBluetooth();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bluetoothLeScanner != null) {
            scanLeDevice(false);
        }
    }

    private void setScanResultOnRecyclerView() {
        pullToRefresh.setRefreshing(false);

        progressBar.setVisibility(View.INVISIBLE);

        scanResultView.setAdapter(new ScanResultViewAdapter(this, scanResultList));
    }

    // for bluetooth methods bellow the this line
    private void initializeBluetoothAdapter() {
        scanHandler = new Handler();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    private void initializeBluetoothScanner() {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        filterList = new ArrayList<ScanFilter>();
    }

    private void startScanBluetooth() {
        if (isEnableBluetoothSetting()) {
            initializeBluetoothScanner();

            scanLeDevice(true);
        } else {
            intentBluetoothSettingWindow();
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {

            scanHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    bluetoothLeScanner.stopScan(mScanCallback);

                    setScanResultOnRecyclerView();
                }
            }, SCAN_PERIOD);

            bluetoothLeScanner.startScan(filterList, scanSettings, mScanCallback);

        } else {
            bluetoothLeScanner.stopScan(mScanCallback);
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, android.bluetooth.le.ScanResult scanResult) {
            String name = scanResult.getDevice().getName();

            if (name != null) {
                if (!isOverlap(name)) {
                    ScanResult device = new ScanResult();

                    device.deviceName = name;

                    if (configManager.isCheckConfig(name)) {
                        device.configuration = Constants.CONFIG_MESSAGE;

                        scanResultList.add(device);
                    }
                }
            }
        }
    };

    private boolean isOverlap(String name) {
        for (ScanResult scanResult : scanResultList) {
            if (scanResult.deviceName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnableBluetoothSetting() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    private void intentBluetoothSettingWindow() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        startActivityForResult(intent, Constants.REQUEST_ENABLE_BLUETOOTH);
    }

    //add result for already setting configs.
//    private void addCheckedDeviceToScanResultList() {
//        List<String> list = DataManager.getCheckedList(this);
//
//        for (String checkedName : list) {
//            ScanResult scanResult = new ScanResult();
//
//            scanResult.deviceName = checkedName;
//
//            scanResult.configuration = Constants.CONFIG_MESSAGE;
//
//            scanResultList.add(scanResult);
//        }
//    }
}
