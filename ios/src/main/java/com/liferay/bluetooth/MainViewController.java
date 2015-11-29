package com.liferay.bluetooth;

import org.robovm.apple.corebluetooth.*;
import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.UIActivityIndicatorView;
import org.robovm.apple.uikit.UIActivityIndicatorViewStyle;
import org.robovm.apple.uikit.UIColor;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.annotation.CustomClass;
import org.robovm.objc.block.VoidBlock3;

import java.util.ArrayList;
import java.util.List;

@CustomClass("MainViewController")
public class MainViewController extends UIViewController implements
        CBCentralManagerDelegate,
        CBPeripheralDelegate {

    private ConfigManager configManager;

    private List<GattManager> gattManagerList;

    private List<BroadcastManager> broadcastManagerList;

    private List<DeviceDataUIView> deviceDataUIViewList;

    private List<String> checkedDeviceList;

    private CBCentralManager bluetoothManager;

    private UIActivityIndicatorView uiActivityIndicatorView;

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();

    }

    @Override
    public void viewWillAppear(boolean b) {
        super.viewWillAppear(b);

        getConfigData();

    }

    private void getConfigData() {
        //check saved configuration.
        if (DataManager.isConfigData()) {
            configManager = new ConfigManager(DataManager.getConfigData());

            gattManagerList = configManager.getGattManagerList();

            startBluetoothIFCheckedDevice();

        } else {
            getConfigurationFromServer();
        }
    }

    private void getConfigurationFromServer() {

        startActivityIndicator();

        NetworkManager.getConfiguration(new VoidBlock3<NSData, NSURLResponse, NSError>() {
            @Override
            public void invoke(NSData nsData, NSURLResponse nsurlResponse, NSError nsError) {
                try {

                    NSDictionary jsonConfigData = (NSDictionary) NSJSONSerialization.createJSONObject(nsData,
                            NSJSONReadingOptions.MutableLeaves);

                    DataManager.saveConfigData(jsonConfigData);

                    stopActivityIndicator();

                    moveToAddDeviceTableViewController();

                } catch (NSErrorException nserrorException) {

                }
            }
        });

    }

    private void moveToAddDeviceTableViewController() {

        if (!DataManager.isCheckedData()) {

            performSegue("AddDevice", null);

        }

    }

    private void startActivityIndicator() {
        uiActivityIndicatorView = new UIActivityIndicatorView(UIActivityIndicatorViewStyle.WhiteLarge);

        uiActivityIndicatorView.setCenter(getView().getCenter());

        uiActivityIndicatorView.setColor(UIColor.black());

        getView().addSubview(uiActivityIndicatorView);

        uiActivityIndicatorView.startAnimating();
    }

    private void stopActivityIndicator() {

        if (uiActivityIndicatorView.isAnimating()) {

            uiActivityIndicatorView.stopAnimating();

        }

    }

    private void startBluetoothIFCheckedDevice() {
        checkedDeviceList = getCheckedList();

        int size = checkedDeviceList.size();

        if (size >= 1) {

            deviceDataUIViewList = new ArrayList<DeviceDataUIView>();

            setUpDeviceDataView();

            startScanBluetooth();

        } else {

            removeMainView();

            moveToAddDeviceTableViewController();

        }

    }

    private void startScanBluetooth() {
        bluetoothManager = new CBCentralManager(this, null, null);
    }

    private List<String> getCheckedList() {
        return DataManager.getCheckedDeviceList();
    }

    @Override
    public void didUpdateState(CBCentralManager cbCentralManager) {
        // usable BLE
        if (cbCentralManager.getState() == CBCentralManagerState.PoweredOn) {
            bluetoothManager.scanForPeripherals(null, null);
        } else {
            System.out.println("unusable Bluetooth.");
        }
    }

    @Override
    public void willRestoreState(CBCentralManager cbCentralManager,
                                 CBCentralManagerRestoredState cbCentralManagerRestoredState) {

    }

    @Override
    public void didDiscoverPeripheral(CBCentralManager cbCentralManager,
                                      CBPeripheral cbPeripheral,
                                      CBAdvertisementData cbAdvertisementData, NSNumber nsNumber) {

        String deviceName = cbPeripheral.getName();

        String serverType = getServerType(deviceName);

        if (serverType != null) {

            if (serverType.equals(Constants.GATT)) {

                cbCentralManager.connectPeripheral(cbPeripheral, null);

            } else if (serverType.equals(Constants.BROADCAST)) {

            } else {
                // no server type error.
            }

        }

    }

    private String getServerType(String deviceName) {

        if (deviceName != null) {

            for (String checkedDevice : checkedDeviceList) {

                if (deviceName.contains(checkedDevice)) {

                    return configManager.getServerType(checkedDevice);

                }

            }
        }

        return null;

    }

    @Override
    public void didConnectPeripheral(CBCentralManager cbCentralManager, CBPeripheral cbPeripheral) {

        System.out.println("will connect" + cbPeripheral.getName());

        cbPeripheral.setDelegate(this);

        cbPeripheral.discoverServices(null);

    }

    @Override
    public void didFailToConnectPeripheral(CBCentralManager cbCentralManager,
                                           CBPeripheral cbPeripheral, NSError nsError) {

    }

    @Override
    public void didDisconnectPeripheral(CBCentralManager cbCentralManager,
                                        CBPeripheral cbPeripheral, NSError nsError) {

    }

    @Override
    public void didUpdateName(CBPeripheral cbPeripheral) {

    }

    @Override
    public void didModifyServices(CBPeripheral cbPeripheral, NSArray<CBService> nsArray) {

    }

    @Override
    public void didUpdateRSSI(CBPeripheral cbPeripheral, NSError nsError) {

    }

    @Override
    public void didReadRSSI(CBPeripheral cbPeripheral, NSNumber nsNumber, NSError nsError) {

    }

    @Override
    public void didDiscoverServices(CBPeripheral cbPeripheral, NSError nsError) {
        System.out.println("didDiscoverServices");

        if (nsError == null) {

            discoverCharacteristics(cbPeripheral);

        } else {
            System.out.print("Discover Services Error");
        }

    }

    private void discoverCharacteristics(CBPeripheral cbPeripheral) {

        NSArray<CBService> services = cbPeripheral.getServices();

        for (GattManager gattManager : gattManagerList) {

            for (CBService cbService : services) {

                for (GattProcess gattProcess : gattManager.getGattProcessList()) {

                    if (cbService.getUUID().getUUIDString().equals(gattProcess.getService())) {

                        cbPeripheral.discoverCharacteristics(null, cbService);

                        break;
                    }
                }

            }

        }

    }

    @Override
    public void didDiscoverIncludedServices(CBPeripheral cbPeripheral, CBService cbService, NSError nsError) {

    }

    @Override
    public void didDiscoverCharacteristics(CBPeripheral cbPeripheral, CBService cbService, NSError nsError) {
        System.out.println("didDiscoverCharacteristics");

        if (nsError == null) {

            setCharacteristics(cbService, cbPeripheral);

        } else {

            System.out.print("Discover Characteristics Error");

        }

    }

    private void setCharacteristics(CBService cbService, CBPeripheral cbPeripheral) {
        NSArray<CBCharacteristic> characteristics = cbService.getCharacteristics();

        for (GattManager gattManager : gattManagerList) {

            if (cbPeripheral.getName().contains(gattManager.getDeviceName())) {

                GattProcess gattProcess = gattManager.getLastGattProcess();

                for (CBCharacteristic characteristic : characteristics) {

                    String characteristicsUUID = gattProcess.getCharacteristic();

                    if (characteristic.getUUID().getUUIDString().equals(characteristicsUUID)) {

                        doGattServer(gattManager, cbPeripheral, characteristic);

                        break;

                    }
                }
            }
        }

    }

    private void doGattServer(GattManager gattManager, CBPeripheral cbPeripheral, CBCharacteristic characteristic) {
        GattProcess gattProcess = gattManager.getGattProcess();

        int method = gattProcess.getMethod();

        String value = gattProcess.getValue();

        gattManager.upCurrentCount();

        if (method == GattProcess.READ) {

            cbPeripheral.readValue(characteristic);

        } else if (method == GattProcess.WRITE) {

            setWriteValue(cbPeripheral, characteristic, value);

        } else if (method == GattProcess.NOTIFY) {

            cbPeripheral.setNotifyValue(true, characteristic);

        } else if (method == GattProcess.FINISH) {

            byte[] values = characteristic.getValue().getBytes();

            updateValueOnDataLayout(gattManager,values);
        }

    }

    private void updateValueOnDataLayout(GattManager gattManager,byte[] values){

        DeviceDataUIView deviceDataUIView = getDeviceDataUIView(gattManager.getDeviceName());

        List<String> valueList = gattManager.getValue(values);

        deviceDataUIView.setValue(valueList);

    }

    private DeviceDataUIView getDeviceDataUIView(String deviceName){

        for (DeviceDataUIView deviceDataUIView:deviceDataUIViewList){

            if (deviceDataUIView.getDeviceName().equals(deviceName)){

                return deviceDataUIView;

            }
        }

        return null;

    }


    //for BLE Gatt Server
    private void setWriteValue(CBPeripheral cbPeripheral, CBCharacteristic characteristic, String value) {

        byte[] hex = Convert.stringToByteArray(value);

        NSData data = new NSData(hex);

        cbPeripheral.writeValue(data, characteristic, CBCharacteristicWriteType.WithResponse);

    }

    @Override
    public void didUpdateValue(CBPeripheral cbPeripheral, CBCharacteristic cbCharacteristic, NSError nsError) {

        if (nsError == null) {

            GattManager gattManager = getGattManager(cbPeripheral.getName());

            GattProcess gattProcess = gattManager.getGattProcess();
            ;
            if (gattProcess.getCharacteristic().equals(cbCharacteristic.getUUID().getUUIDString())) {

                doGattServer(gattManager, cbPeripheral, cbCharacteristic);

            }
        }

    }

    @Override
    public void didWriteValue(CBPeripheral cbPeripheral, CBCharacteristic cbCharacteristic, NSError nsError) {
        System.out.println("didWriteValue");

        if (nsError == null) {

            GattManager gattManager = getGattManager(cbPeripheral.getName());

            if (gattManager != null) {

                GattProcess gattProcess = gattManager.getLastGattProcess();

                String uuid = gattProcess.getCharacteristic();

                if (cbCharacteristic.getUUID().getUUIDString().equals(uuid)) {

                    GattProcess newGattProcess = gattManager.getGattProcess();

                    for (CBCharacteristic characteristic : cbCharacteristic.getService().getCharacteristics()) {

                        if (newGattProcess.getCharacteristic().equals(characteristic.getUUID().getUUIDString())) {

                            doGattServer(gattManager, cbPeripheral, characteristic);

                        }

                    }

                }

            }

        }

    }

    private GattManager getGattManager(String deviceName) {

        for (GattManager gattManager : gattManagerList) {

            if (deviceName.contains(gattManager.getDeviceName())) {

                return gattManager;
            }

        }
        return null;
    }

    @Override
    public void didUpdateNotificationState(CBPeripheral cbPeripheral,
                                           CBCharacteristic cbCharacteristic, NSError nsError) {
        System.out.print("didUpdateNotificationState");
    }

    @Override
    public void didDiscoverDescriptors(CBPeripheral cbPeripheral,
                                       CBCharacteristic cbCharacteristic, NSError nsError) {
        System.out.print("didDiscoverDescriptors");
    }

    @Override
    public void didUpdateValue(CBPeripheral cbPeripheral, CBDescriptor cbDescriptor, NSError nsError) {
        System.out.print("didUpdateValue");
    }

    @Override
    public void didWriteValue(CBPeripheral cbPeripheral, CBDescriptor cbDescriptor, NSError nsError) {
        System.out.print("didWriteValue");
    }

    private void setUpDeviceDataView() {

        int size = checkedDeviceList.size();

        double width = getView().getFrame().getWidth();

        double height = getView().getFrame().getHeight();

            for (int index = 0; index < size; index++) {

                String checkedDevice = checkedDeviceList.get(index);

                if (configManager.getServerType(checkedDevice).equals(Constants.GATT)) {

                    createDeviceLayout(checkedDevice,index,height,width);

                }

            }

        addDeviceDataView();

    }

    private void createDeviceLayout(String checkedDevice,int index,double height,double width){

        GattManager gattManager = getGattManager(checkedDevice);

        CGRect position = new CGRect(0, height / 5 * index, width, height / 5);

        String deviceName = gattManager.getDeviceName();

        List<String> valueTypeList = gattManager.getValueTypeLabelList();

        List<String> valueUnitList = gattManager.getValueUnitTypeList();

        DeviceDataUIView deviceDataUIView = new DeviceDataUIView(position, deviceName,
                valueTypeList, valueUnitList);

        deviceDataUIViewList.add(deviceDataUIView);

    }

    private void addDeviceDataView(){

        for (DeviceDataUIView deviceDataUIView : deviceDataUIViewList) {

            getView().addSubview(deviceDataUIView);

        }
    }

    private void removeMainView() {
        stopBluetoothScan();

        removeDataView();
    }


    private void stopBluetoothScan() {

        if (bluetoothManager != null) {

            bluetoothManager.stopScan();

            bluetoothManager.release();

            bluetoothManager = null;
        }

    }

    private void removeDataView() {

        if (deviceDataUIViewList != null) {

            for (DeviceDataUIView deviceDataView : deviceDataUIViewList) {

                deviceDataView.removeFromSuperview();

                deviceDataView.release();

                deviceDataUIViewList = null;

            }
        }
    }

}
