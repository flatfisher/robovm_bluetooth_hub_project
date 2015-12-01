package com.liferay.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainViewActivity extends Activity {

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner bluetoothLeScanner;

    private ScanSettings scanSettings;

    private List<ScanFilter> filterList;

    private BluetoothGatt bluetoothGatt;

    private android.os.Handler scanHandler;

    private static final long SCAN_PERIOD = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_main);

        initializeBluetoothAdapter();

    }

    @Override
    protected void onResume() {

        super.onResume();

        if (isEnableBluetoothSetting()) {

            initializeBluetoothScanner();

            scanLeDevice(true);


        } else {

            intentBluetoothSettingWindow();

        }

    }


    // for bluetooth codes bellow the this line

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

    private void scanLeDevice(final boolean enable) {

        if (enable) {

            scanHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    bluetoothLeScanner.stopScan(mScanCallback);


                }

            }, SCAN_PERIOD);

            bluetoothLeScanner.startScan(filterList, scanSettings, mScanCallback);

        } else {

            bluetoothLeScanner.stopScan(mScanCallback);

        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult scanResult) {

            Log.i("Result","Name"+scanResult.getDevice().getName()+"Address" +scanResult.getDevice().getAddress());

            BluetoothDevice bluetoothDevice = scanResult.getDevice();

//            connectToDevice(bluetoothDevice);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

            for (ScanResult sr : results) {

                Log.i("ScanResult - Results", sr.toString());

            }

        }

        @Override
        public void onScanFailed(int errorCode) {

            Log.e("Scan Failed", "Error Code: " + errorCode);

        }

    };

    public void connectToDevice(BluetoothDevice device) {

        if (bluetoothGatt == null) {

            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);

            scanLeDevice(false);

        }

    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {

        }

    };

    private boolean isEnableBluetoothSetting() {

        if (bluetoothAdapter != null || bluetoothAdapter.isEnabled()) {

            return true;

        } else {

            return false;

        }

    }

    private void intentBluetoothSettingWindow() {

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        startActivityForResult(intent, Constants.REQUEST_ENABLE_BLUETOOTH);

    }

}