package com.liferay.bluetooth;


import org.robovm.apple.corebluetooth.*;
import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;
import org.robovm.objc.annotation.CustomClass;
import org.robovm.objc.annotation.IBOutlet;
import org.robovm.rt.bro.annotation.MachineSizedSInt;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@CustomClass("AddDeviceTableViewController")
public class AddDeviceTableViewController extends UITableViewController implements
        UIControl.OnValueChangedListener,
        CBCentralManagerDelegate {

    private static String scanResultCellId = Constants.SCAN_RESULT_CELL;

    private ConfigManager configManager;


    private UIRefreshControl pullToRefreshManager;

    private CBCentralManager bluetoothManager;

    private List<String> scanResultArray;

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

        configNameList = getConfigData();

        ROW_HEIGHT = getView().getFrame().getHeight() / 8;

        scanResultArray = new ArrayList<String>();

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
    public void viewWillAppear(boolean b) {
        super.viewWillAppear(b);

        System.out.println("add device viewWillAppear");

        startScanBLEData();
    }

    private void startScanBLEData() {
        System.out.println("startScanBLEData");

        bluetoothManager = new CBCentralManager(this, null, null);
    }

    private void moveCheckedDeviceToScanResultArray() {
        scanResultArray.clear();

        for (String checkedDevice : checkedDeviceList) {
            scanResultArray.add(checkedDevice);
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
                String deviceName = cell.getTextLabel().getText();

                if (!cell.getDetailTextLabel().getText().equals(Constants.NO_CONFIG_MESSAGE)) {

                    checkedDeviceList.add(deviceName);
                }

            }
        }

        DataManager.saveCheckedDeviceList(checkedDeviceList);
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

        String deviceName = scanResultArray.get(row);

        if (isCheckSavedDevice(deviceName)) {
            cell.setAccessoryType(UITableViewCellAccessoryType.Checkmark);
        }

        String valueType = getValueTypeName(deviceName);

        cell.getTextLabel().setText(valueType);

        setConfigFound(cell, deviceName);

        cell.getDetailTextLabel().setNumberOfLines(0);

        cell.setSelectionStyle(UITableViewCellSelectionStyle.Blue);

        return cell;
    }

    private String getValueTypeName(String deviceName) {
        String typeLabel = "";

        List<GattManager> gattList = configManager.getGattManagerList();

        for (GattManager gattManager:gattList){

            if (gattManager.getDeviceName().equals(deviceName)){

                List<String> list = gattManager.getValueTypeLabelList();

                int size = list.size();

                for (int i = 0; i < size;i++){

                    if (i==0){
                        typeLabel = list.get(i);
                    }else{
                        typeLabel = typeLabel + "&" + list.get(i);
                    }
                }
                return typeLabel;
            }
        }

        List<BroadcastManager> broadcastList =  configManager.getBroadcastManagerList();

        for (BroadcastManager broadcastManager:broadcastList){

            if (broadcastManager.getDeviceName().equals(deviceName)){

                List<String> list = broadcastManager.getValueTypeLabelList();

                int size = list.size();

                for (int i = 0; i < size;i++){

                    if (i==0){
                        typeLabel = list.get(i);
                    }else{
                        typeLabel = typeLabel + "&" + list.get(i);
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

                String deviceName = cell.getTextLabel().getText();

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
                    scanResultArray.add(name);
                }
            } else {
                if (isCheckOverlap(deviceName)) {
                    scanResultArray.add(deviceName);
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
        for (String device : scanResultArray) {
            if (device.equals(deviceName)) {
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
