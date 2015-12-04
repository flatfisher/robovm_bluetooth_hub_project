package com.liferay.bluetooth;


import org.robovm.apple.corebluetooth.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.objc.annotation.CustomClass;
import org.robovm.rt.bro.annotation.MachineSizedSInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@CustomClass("AddDeviceTableViewController")
public class AddDeviceTableViewController extends UITableViewController implements
        UIControl.OnValueChangedListener,
        CBCentralManagerDelegate {

    class ScanResult {
        public String deviceName;

        public String methodName;
    }

    private static String scanResultCellId = Constants.SCAN_RESULT_CELL;

    private ConfigManager configManager;

    private UIRefreshControl pullToRefreshManager;

    private CBCentralManager bluetoothManager;

    private List<ScanResult> scanResultArray;

    private int SCAN_TIME = 5000;

    private double ROW_HEIGHT;

    private List<String> configNameList;

    private List<String> checkedDeviceList;

    private UIActivityIndicatorView uiActivityIndicatorView;

    // first call method.
    @Override
    public void viewDidLoad() {
        super.viewDidLoad();

        System.out.println("add device viewDidLoad");

        checkedDeviceList = DataManager.getCheckedDeviceList();

        System.out.println("LIST"+checkedDeviceList);

        configNameList = getConfigData();

        ROW_HEIGHT = getView().getFrame().getHeight() / 8;

        scanResultArray = new ArrayList<ScanResult>();

        pullToRefreshManager = new UIRefreshControl();

        pullToRefreshManager.setAttributedTitle(new NSAttributedString(Constants.SCAN_DEVICE));

        pullToRefreshManager.addOnValueChangedListener(this);

        getTableView().setRowHeight(ROW_HEIGHT);

        getTableView().addSubview(pullToRefreshManager);

        getTableView().setAllowsMultipleSelection(true);

        uiActivityIndicatorView = new UIActivityIndicatorView(UIActivityIndicatorViewStyle.WhiteLarge);

        uiActivityIndicatorView.setCenter(getTableView().getCenter());

        uiActivityIndicatorView.setColor(UIColor.black());

        getNavigationController().getNavigationBar().addSubview(uiActivityIndicatorView);

        uiActivityIndicatorView.startAnimating();
    }

    @Override
    public void viewDidAppear(boolean b) {
        super.viewDidAppear(b);

        startScanBLEData();
    }

    private void startScanBLEData() {
        System.out.println("startScanBLEData");

        bluetoothManager = new CBCentralManager(this, null, null);
    }

    private void moveCheckedDeviceToScanResultArray() {
        scanResultArray.clear();

        for (String checkedDevice : checkedDeviceList) {

            ScanResult scanResult = new ScanResult();

            scanResult.deviceName = checkedDevice;

            scanResult.methodName = getMethodName(checkedDevice);

            scanResultArray.add(scanResult);
        }
    }

    @Override
    public void viewWillDisappear(boolean b) {
        super.viewWillDisappear(b);

        NSArray<UIViewController> array = getNavigationController().getViewControllers();

        if (array.getAssociatedObject(this) == null) {
            stopBLEScan();
        }
    }

    private void stopBLEScan() {
        if (bluetoothManager != null) {

            bluetoothManager.stopScan();

            bluetoothManager.release();

            bluetoothManager = null;
        }
    }

    private void saveConfig() {
        NSArray<UITableViewCell> cellNSArray = getTableView().getVisibleCells();

        List<String> checkedDeviceList = new ArrayList<String>();

        for (UITableViewCell cell : cellNSArray) {

            if (cell.getAccessoryType() == UITableViewCellAccessoryType.Checkmark) {
                String methodName = cell.getTextLabel().getText();

                if (!cell.getDetailTextLabel().getText().equals(Constants.NO_CONFIG_MESSAGE)) {

                    String deviceName = getDeviceName(methodName);

                    checkedDeviceList.add(deviceName);
                }
            }
        }

        DataManager.saveCheckedDeviceList(checkedDeviceList);
    }

    private String getDeviceName(String method){

        for(ScanResult scanResult:scanResultArray){

            if (method.contains(scanResult.methodName)){

                return scanResult.deviceName;
            }

        }
        return null;
    }

    private List<String> getConfigData() {
        NSDictionary nsDictionary = DataManager.getConfigData();

        configManager = new ConfigManager(nsDictionary);

        return configManager.getDeviceNameList();
    }

    //UIControl.OnValueChangedListener fot pull to refresh
    @Override
    public void onValueChanged(UIControl uiControl) {
        if (!bluetoothManager.isScanning()) {
            startScanBLEData();
        }
    }

    @Override
    public long getNumberOfRowsInSection(UITableView uiTableView, @MachineSizedSInt long l) {
        return scanResultArray.size();
    }

    @Override
    public long getNumberOfSections(UITableView uiTableView) {
        return 1;
    }

    @Override
    public UITableViewCell getCellForRow(UITableView uiTableView, NSIndexPath nsIndexPath) {
        int row = (int) nsIndexPath.getRow();

        UITableViewCell cell = uiTableView.dequeueReusableCell(scanResultCellId);

        if (cell == null) {
            cell = new UITableViewCell(UITableViewCellStyle.Subtitle, scanResultCellId);
        }

        String methodName = scanResultArray.get(row).methodName;

        String device = getDeviceName(methodName);

        if (isCheckSavedDevice(device )) {
            cell.setAccessoryType(UITableViewCellAccessoryType.Checkmark);
        }

        cell.getTextLabel().setText(methodName);

        setConfigFound(cell, device );

        cell.getDetailTextLabel().setNumberOfLines(0);

        cell.setSelectionStyle(UITableViewCellSelectionStyle.Blue);

        return cell;
    }

    private String getMethodName(String deviceName) {
        String typeLabel = getValueTypeFromGatt(deviceName);

        if (typeLabel.length() <= 0) {
            typeLabel = getValueTypeFromBroadcast(deviceName);
        }

        return typeLabel;
    }

    private String getValueTypeFromGatt(String deviceName) {
        String typeLabel = "";

        List<GattManager> gattList = configManager.getGattManagerList();

        for (GattManager gattManager : gattList) {

            if (gattManager.getDeviceName().equals(deviceName)) {

                List<String> list = gattManager.getValueTypeLabelList();

                int size = list.size();

                for (int i = 0; i < size; i++) {

                    if (i == 0) {
                        typeLabel = list.get(i);
                    } else {
                        typeLabel = typeLabel + " & " + list.get(i);
                    }
                }
                return typeLabel;
            }
        }

        return typeLabel;
    }

    private String getValueTypeFromBroadcast(String deviceName) {
        String typeLabel = "";

        List<BroadcastManager> broadcastList = configManager.getBroadcastManagerList();

        for (BroadcastManager broadcastManager : broadcastList) {

            if (broadcastManager.getDeviceName().equals(deviceName)) {

                List<String> list = broadcastManager.getValueTypeLabelList();

                int size = list.size();

                for (int i = 0; i < size; i++) {

                    if (i == 0) {
                        typeLabel = list.get(i);
                    } else {
                        typeLabel = typeLabel + " & " + list.get(i);
                    }
                }
                return typeLabel;
            }
        }

        return null;
    }

    private void setConfigFound(UITableViewCell cell, String device) {
        UILabel uiLabel = cell.getDetailTextLabel();

        uiLabel.setTextColor(UIColor.gray());

        if (checkConfigDataName(device) != null) {
            uiLabel.setText(Constants.CONFIG_MESSAGE);

        } else {

            uiLabel.setText(Constants.NO_CONFIG_MESSAGE);
        }
    }

    private boolean isCheckSavedDevice(String deviceName) {
        for (String device : checkedDeviceList) {

            if (device.equals(deviceName)) {

                return true;

            }

        }

        return false;
    }

    @Override
    public void didSelectRow(UITableView uiTableView, NSIndexPath nsIndexPath) {
        uiTableView.deselectRow(nsIndexPath, true);

        UITableViewCell cell = uiTableView.getCellForRow(nsIndexPath);

        if (!cell.getDetailTextLabel().getText().equals(Constants.NO_CONFIG_MESSAGE)) {

            if (cell.getAccessoryType() == UITableViewCellAccessoryType.Checkmark) {


                String method = cell.getTextLabel().getText();

                String deviceName = getDeviceName(method);

                disSelectDevice(deviceName);

                cell.setAccessoryType(UITableViewCellAccessoryType.None);

            } else {


                cell.setAccessoryType(UITableViewCellAccessoryType.Checkmark);
            }
        }
        saveConfig();
    }

    private void disSelectDevice(String deviceName) {
        DataManager.removeCheckedDevice(deviceName);
    }

    @Override
    public double getHeightForRow(UITableView uiTableView, NSIndexPath nsIndexPath) {
        return ROW_HEIGHT;
    }

    //CBCentralManagerDelegate
    @Override
    public void didUpdateState(CBCentralManager cbCentralManager) {
        // usable BLE
        if (cbCentralManager.getState() == CBCentralManagerState.PoweredOn) {

            bluetoothManager.scanForPeripherals(null, null);

            Timer scanTimer = new Timer();

            scanTimer.scheduleAtFixedRate(new TimerTask() {
                int count = 0;

                @Override
                public void run() {

                    System.out.println("add device scan...");

                    if (count > SCAN_TIME / 1000) {

                        System.out.println(bluetoothManager);

                        bluetoothManager.stopScan();

                        scanTimer.cancel();

                        getTableView().reloadData();

                        stopPullToRefresh();

                        stopUIActivityIndicator();

                    }
                    count++;
                }

            }, 0, 1000);
        }
    }

    private void stopPullToRefresh() {
        if (pullToRefreshManager.isRefreshing()) {
            pullToRefreshManager.endRefreshing();
        }
    }

    private void stopUIActivityIndicator() {
        if (uiActivityIndicatorView.isAnimating()) {
            uiActivityIndicatorView.stopAnimating();
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
        System.out.println("Add Device didDiscover" + cbPeripheral.getName());

        String deviceName = cbPeripheral.getName();

        if (isCheckCanAdd(deviceName)) {
            String name = checkConfigDataName(deviceName);

            if (name != null) {
                if (isCheckOverlap(name)) {

                    ScanResult scanResult = new ScanResult();

                    scanResult.deviceName = name;

                    scanResult.methodName = getMethodName(name);

                    scanResultArray.add(scanResult);
                }
            }
        }
    }


    private String checkConfigDataName(String device) {
        for (String configName : configNameList) {
            if (device.contains(configName)) {
                return configName;
            }
        }
        return null;
    }


    private boolean isCheckCanAdd(String deviceName) {
        if (deviceName == null) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isCheckOverlap(String deviceName) {
        for (ScanResult scanResult : scanResultArray) {
            if (scanResult.deviceName.equals(deviceName)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void didConnectPeripheral(CBCentralManager cbCentralManager,
                                     CBPeripheral cbPeripheral) {

    }

    @Override
    public void didFailToConnectPeripheral(CBCentralManager cbCentralManager,
                                           CBPeripheral cbPeripheral, NSError nsError) {

    }

    @Override
    public void didDisconnectPeripheral(CBCentralManager cbCentralManager,
                                        CBPeripheral cbPeripheral, NSError nsError) {

    }
}
