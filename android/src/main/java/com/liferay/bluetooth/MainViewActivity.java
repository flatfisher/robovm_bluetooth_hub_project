package com.liferay.bluetooth;

import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

public class MainViewActivity extends AppCompatActivity implements View.OnClickListener {

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

    private LinearLayout layoutSize;

    private LinearLayout mainDataView;

    private List<DataView> dataViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_main);

        layoutSize = (LinearLayout) findViewById(R.id.layoutSize);

        mainDataView = (LinearLayout) findViewById(R.id.get_data_view);

        bluetoothGattList = new ArrayList<BluetoothGatt>();

        bluetoothGattIndexList = new ArrayList<BluetoothGattIndex>();

        addDeviceButton = (Button) findViewById(R.id.add_view_button);

        addDeviceButton.setOnClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (DataManager.isCheckConfigData(this)) {
            //prepare gattManagerList.
            String jsonData = DataManager.getConfigData(this);

            gattManagerList = getGattManagerListFromCheckedDevice(jsonData);

            prepareDataView();

            initializeBluetoothAdapter();

            startScanBluetooth();

        } else {
            downloadConfigFromServer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopBluetooth();
    }

    private void stopBluetooth(){

        if (bluetoothLeScanner != null) {
            startScanDevice(false);

            int size = bluetoothGattList.size();

            for (int i = 0; i < size; i++) {
                bluetoothGattList.get(i).disconnect();

                bluetoothGattList.get(i).close();
            }
        }
    }

    private void prepareDataView() {
        dataViewList = new ArrayList<DataView>();

        mainDataView.removeAllViews();

        int rowHeight = layoutSize.getHeight() / 5;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);

        params.weight = 1;

        for (GattManager gattManager : gattManagerList) {
            DataView dataView = new DataView(this, rowHeight, gattManager);

            dataViewList.add(dataView);

            mainDataView.addView(dataView, params);
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
        Intent intent = new Intent(this, AddViewActivity.class);

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

            startScanDevice(true);
        } else {
            intentBluetoothSettingWindow();
        }
    }

    private void startScanDevice(final boolean enable) {
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
    };

    public void connectToDevice(BluetoothDevice device, int index) {
        BluetoothGattIndex bluetoothGattIndex = new BluetoothGattIndex();

        bluetoothGattIndex.deviceName = device.getName();

        bluetoothGattIndex.index = index;

        bluetoothGattIndexList.add(bluetoothGattIndex);

        bluetoothGattList.add(device.connectGatt(this, false, bluetoothGattCallback));
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int status, int newState) {
            Log.i("onConnectionStateChange", "passed" + bluetoothGatt.getDevice().getName());

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothGattList.remove(bluetoothGatt);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
            Log.i("onServicesDiscovered", "passed");

            BluetoothGattCharacteristic characteristic = getBluetoothGattCharacteristic(bluetoothGatt);

            doGattServer(bluetoothGatt, characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", "passed");

            BluetoothGattCharacteristic nextCharacteristic = getBluetoothGattCharacteristic(bluetoothGatt);

            doGattServer(bluetoothGatt, nextCharacteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt,
                                          BluetoothGattCharacteristic
                                                  characteristic, int status) {
            super.onCharacteristicWrite(bluetoothGatt, characteristic, status);

            Log.i("onCharacteristicWrite", "passed");

            BluetoothGattCharacteristic nextCharacteristic = getBluetoothGattCharacteristic(bluetoothGatt);

            doGattServer(bluetoothGatt, nextCharacteristic);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt,
                                            BluetoothGattCharacteristic
                                                    characteristic) {
            super.onCharacteristicChanged(bluetoothGatt, characteristic);

            Log.i("onCharacteristicRead", "passed");

            BluetoothGattCharacteristic nextCharacteristic = getBluetoothGattCharacteristic(bluetoothGatt);

            doGattServer(bluetoothGatt, nextCharacteristic);
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

        String deviceName = gattManager.getDeviceName();

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
            int index = getIndexOfBluetoothGatt(deviceName);

            gatt.setCharacteristicNotification(characteristic, true);

            for (BluetoothGattDescriptor bgDescriptor : characteristic.getDescriptors()) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString(bgDescriptor.getUuid().toString()));

                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                bluetoothGattList.get(index).writeDescriptor(descriptor);
            }
        } else if (method == GattProcess.FINISH) {
            DataView dataView = getDataView(deviceName);

            List<String> data = gattManager.getValue(characteristic.getValue());

            updateData(dataView,data);
        }
    }

    private DataView getDataView(String deviceName) {
        for (DataView dataView : dataViewList) {
            if (dataView.getDeviceName().equals(deviceName)) {
                return dataView;
            }
        }
        return null;
    }

    private static void updateData(final DataView dataView,List<String> data){
        new Thread(new Runnable() {
            public void run() {
                dataView.post(new Runnable() {
                    public void run() {

                        dataView.updateData(data);

                    }
                });
            }
        }).start();
    }

    class BluetoothGattIndex {
        int index;

        String deviceName;
    }

    private int getIndexOfBluetoothGatt(String deviceName) {
        for (BluetoothGattIndex bluetoothGattIndex : bluetoothGattIndexList) {
            if (bluetoothGattIndex.deviceName.equals(deviceName)) {

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
}