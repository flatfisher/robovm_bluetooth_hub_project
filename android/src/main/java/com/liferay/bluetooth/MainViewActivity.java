package com.liferay.bluetooth;

import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

public class MainViewActivity extends Activity implements View.OnClickListener {

    private Button addDeviceButton;

    private ConfigManager configManager;

    private List<GattManager> gattManagerList;

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothLeScanner bluetoothLeScanner;

    private ScanSettings scanSettings;

    private List<ScanFilter> filterList;

    private List<BluetoothGatt> bluetoothGattList;

    private List<BluetoothGattIndex> bluetoothGattIndexList;

    private android.os.Handler scanHandler;

    private static final long SCAN_PERIOD = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_main);

        bluetoothGattList = new ArrayList<BluetoothGatt>();

        bluetoothGattIndexList = new ArrayList<BluetoothGattIndex>();

        addDeviceButton = (Button) findViewById(R.id.add_device_button);

        addDeviceButton.setOnClickListener(this);

        if (DataManager.isCheckConfigData(this)) {
            //prepare gattManagerList.
            String jsonData = DataManager.getConfigData(this);

            gattManagerList = getGattManagerListFromCheckedDevice(jsonData);

            initializeBluetoothAdapter();

            startScanBluetooth();

        } else {

            downloadConfigFromServer();

        }

    }

    private List<GattManager> getGattManagerListFromCheckedDevice(String jsonData) {

        configManager = new ConfigManager(jsonData);

        List<GattManager> originalList = configManager.getGattManagerList();

        List<GattManager> newList = new ArrayList<GattManager>();

        for (GattManager gattManager : originalList) {

            if (DataManager.isCheckedDevice(gattManager.getDeviceName(), this)) {

                newList.add(gattManager);

            }

        }

        return newList;

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View v) {

        moveToAddDeviceActivity();

    }

    private void downloadConfigFromServer() {

        NetworkManager.getConfiguration(this, new RequestListener() {

            @Override
            public void onResponse(String jsonString, int code) {

                DataManager.saveConfigData(MainViewActivity.this, jsonString);

                moveToAddDeviceActivity();

            }

            @Override
            public void onError(String errorMessage) {

            }

        });

    }

    private void moveToAddDeviceActivity() {

//        scanLeDevice(false);

        Intent intent = new Intent(this, AddDeviceActivity.class);

        startActivity(intent);

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

            BluetoothDevice bluetoothDevice = scanResult.getDevice();

            int size = gattManagerList.size();

            for (int i = 0; i < size; i++) {

                if (gattManagerList.get(i).getDeviceName().equals(bluetoothDevice.getName())) {

                    Log.i("Wii be connected", scanResult.getDevice().getName());

                    connectToDevice(bluetoothDevice, i);

                }

            }

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

    public void connectToDevice(BluetoothDevice device,int index) {

        BluetoothGattIndex bluetoothGattIndex = new BluetoothGattIndex();

        bluetoothGattIndex.deviceName = device.getName();

        bluetoothGattIndex.index = index;

        bluetoothGattIndexList.add(bluetoothGattIndex);

        bluetoothGattList.add(device.connectGatt(this, false, bluetoothGattCallback));

    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "passed" + gatt.getDevice().getName());

            if (newState == BluetoothProfile.STATE_CONNECTED) {

                gatt.discoverServices();

            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("onServicesDiscovered", "passed");

            BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(gatt);

            doGattServer(gatt, characteristic);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", "passed");

            BluetoothGattCharacteristic nextCharacteristic = getBluetoothGattCharacteristic(gatt);

            doGattServer(gatt, nextCharacteristic);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic
                                                  characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            Log.i("onCharacteristicWrite", "passed");

            BluetoothGattCharacteristic nextCharacteristic = getBluetoothGattCharacteristic(gatt);

            doGattServer(gatt, nextCharacteristic);

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic
                                                    characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.i("onCharacteristicRead", "passed");

            BluetoothGattCharacteristic nextCharacteristic = getBluetoothGattCharacteristic(gatt);

            doGattServer(gatt, nextCharacteristic);

        }

    };

    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(BluetoothGatt gatt) {

        GattManager gattManager = getGattManager(gatt.getDevice().getName());

        GattProcess gattProcess = gattManager.getGattProcess();

        String gpService = gattProcess.getService().toLowerCase();

        String gpCharacteristic = gattProcess.getCharacteristic().toLowerCase();

        for (BluetoothGattService service : gatt.getServices()) {

            String uuid = service.getUuid().toString();

            if (uuid.toLowerCase().contains(gpService)) {

                for (BluetoothGattCharacteristic bgCharacteristic : service.getCharacteristics()) {

                    String characteristic = bgCharacteristic.getUuid().toString().toLowerCase();

                    if (characteristic.contains(gpCharacteristic)) {

                        return bgCharacteristic;

                    }

                }

            }

        }

        return null;
    }

    private void doGattServer(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i("doGattServer", "characteristic" + characteristic.getUuid().toString());

        GattManager gattManager = getGattManager(gatt.getDevice().getName());

        GattProcess gattProcess = gattManager.getGattProcess();

        int method = gattProcess.getMethod();

        String value = gattProcess.getValue();

        gattManager.upCurrentCount();

        if (method == GattProcess.READ) {

            gatt.readCharacteristic(characteristic);

        } else if (method == GattProcess.WRITE) {

            byte[] byteValue = Convert.hexStringToByteArray(value);

            characteristic.setValue(byteValue);

            gatt.writeCharacteristic(characteristic);

        } else if (method == GattProcess.NOTIFY) {

            String deviceName = gattManager.getDeviceName();

            int index = getIndexOfBluetoothGatt(deviceName);

            gatt.setCharacteristicNotification(characteristic, true);

            for (BluetoothGattDescriptor bgDescriptor : characteristic.getDescriptors()) {

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString(bgDescriptor.getUuid().toString()));

                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                bluetoothGattList.get(index).writeDescriptor(descriptor);

            }

        } else if (method == GattProcess.FINISH) {

            System.out.println("Value"+gattManager.getValue(characteristic.getValue()));

        }

    }

    class BluetoothGattIndex {

        int index;

        String deviceName;

    }

    private int getIndexOfBluetoothGatt(String deviceName) {

        for (BluetoothGattIndex bluetoothGattIndex : bluetoothGattIndexList){

            if (bluetoothGattIndex.deviceName.equals(deviceName)){

                int index = bluetoothGattIndexList.indexOf(bluetoothGattIndex);

                return index;

            }

        }

        return -1;

    }

    private GattManager getGattManager(String deviceName) {

        for (GattManager gattManager : gattManagerList) {

            if (gattManager.getDeviceName().equals(deviceName)) {

                return gattManager;

            }

        }

        return null;

    }

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