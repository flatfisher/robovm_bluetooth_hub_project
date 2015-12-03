package com.liferay.bluetooth;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSDictionary;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    class Device {
        String deviceName;

        String serverType;
    }

    private List<Device> deviceList;

    private NSDictionary jsonConfigData;

    private List<GattManager> gattManagerList;

    private List<BroadcastManager> broadcastManagerList;

    public ConfigManager(NSDictionary configData) {
        deviceList = new ArrayList<Device>();

        gattManagerList = new ArrayList<GattManager>();

        broadcastManagerList = new ArrayList<BroadcastManager>();

        jsonConfigData = configData;

        parseConfigData();
    }

    public List<String> getDeviceNameList() {
        List<String> deviceNameList = new ArrayList<String>();

        for (Device device : deviceList) {
            deviceNameList.add(device.deviceName);
        }
        return deviceNameList;
    }

    public String getServerType(String deviceName) {
        String serverType;

        for (Device device : deviceList) {
            if (device.deviceName.equals(deviceName)) {

                serverType = device.serverType;

                return serverType;
            }
        }
        return null;
    }

    public List<GattManager> getGattManagerList() {
        return gattManagerList;
    }

    public List<BroadcastManager> getBroadcastManagerList() {
        return broadcastManagerList;
    }

    private void parseConfigData() {
        NSArray deviceArray = (NSArray) jsonConfigData.get("device");

        setDeviceList(deviceArray);

        createManagers(deviceArray);
    }

    private void setDeviceList(NSArray deviceArray) {
        int size = deviceArray.size();

        for (int i = 0; i < size; i++) {
            NSDictionary deviceDictionary = (NSDictionary) deviceArray.get(i);

            Device device = new Device();

            device.deviceName = deviceDictionary.getString("name");

            device.serverType = deviceDictionary.getString("server");

            deviceList.add(device);
        }
    }

    private void createManagers(NSArray deviceArray) {
        int size = deviceArray.size();

        for (int index = 0; index < size; index++) {

            NSDictionary device = (NSDictionary) deviceArray.get(index);

            String deviceName = device.getString("name");

            String server = device.getString("server");

            if (server.equals(Constants.GATT)) {

                createGattManager(deviceName, deviceArray, index);

            } else if (server.equals(Constants.BROADCAST)) {

                createBroadcastManager(deviceName,deviceArray,index);

            } else {
                // error no server type.
            }
        }
    }

    private void createGattManager(String deviceName, NSArray deviceArray, int index) {
        NSDictionary device = (NSDictionary) deviceArray.get(index);

        NSArray methodArray = (NSArray) device.get("method");

        List<GattManager> newGattManagerList = getGattManagerList(deviceName, methodArray);

        gattManagerList.addAll(newGattManagerList);
    }

    private List<GattManager> getGattManagerList(String deviceName, NSArray methodArray) {
        List<GattManager> gattManagerList = new ArrayList<GattManager>();

        String name;

        String convertType;

        List<String> valueTypeList = new ArrayList<String>();

        List<String> unitTypeList = new ArrayList<String>();

        List<GattProcess> gattProcessList = new ArrayList<GattProcess>();

        for (int i = 0; i < methodArray.size(); i++) {
            GattManager gattManager = new GattManager();

            gattManager.setDeviceName(deviceName);

            NSDictionary method = (NSDictionary) methodArray.get(i);

            name = String.valueOf(method.get("name"));

            gattManager.setMethodName(name);

            convertType = String.valueOf(method.get("convert"));

            gattManager.setConvertType(convertType);

            NSArray processArray = (NSArray) method.get("process");

            for (int j = 0; j < processArray.size(); j++) {
                NSDictionary process = (NSDictionary) processArray.get(j);

                GattProcess gattProcess = new GattProcess();

                gattProcess.setMethod(String.valueOf(process.get("method")));

                gattProcess.setService(String.valueOf(process.get("service")));

                gattProcess.setCharacteristic(String.valueOf(process.get("characteristic")));

                gattProcess.setValue(String.valueOf(process.get("value")));

                gattProcessList.add(gattProcess);
            }

            valueTypeList = getList((NSArray) method.get("valueType"));

            unitTypeList = getList((NSArray) method.get("unitType"));

            gattManager.setValueTypeLabelList(valueTypeList);

            gattManager.setValueUnitTypeList(unitTypeList);

            gattManager.setGattProcessList(gattProcessList);

            gattManagerList.add(gattManager);
        }
        return gattManagerList;
    }


    private List<String> getList(NSArray valueTypeArray) {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < valueTypeArray.size(); i++) {
            list.add(String.valueOf(valueTypeArray.get(i)));
        }

        return list;
    }

    private void createBroadcastManager(String deviceName, NSArray deviceArray, int index) {
        NSDictionary device = (NSDictionary) deviceArray.get(index);

        BroadcastManager broadcastManager = new BroadcastManager();

        NSArray methodArray = (NSArray) device.get("method");

        NSDictionary inactive = (NSDictionary) device.get("inactive");

        String inactiveType = inactive.getString("type");

        int[] inactiveIndex = getIntArray((NSArray) inactive.get("valueIndex"));

        NSDictionary unstable = (NSDictionary) device.get("unstable");

        String unstableType = unstable.getString("type");

        int[] unstableIndex = getIntArray((NSArray) unstable.get("valueIndex"));

        int length = device.getInt("dataLength");

        broadcastManager.setDeviceName(deviceName);

        broadcastManager.setInactiveType(inactiveType);

        broadcastManager.setInactiveValueIndexArray(inactiveIndex);

        broadcastManager.setUnstableType(unstableType);

        broadcastManager.setUnstableValueIndexArray(unstableIndex);

        broadcastManager.setDataLength(length);

        List<BroadcastManager> newBroadcastManagerList = getBroadCastManagerList(broadcastManager,
                                                                                    device, length,methodArray);
        broadcastManagerList.addAll(newBroadcastManagerList);
    }

    private static List<BroadcastManager> getBroadCastManagerList(BroadcastManager broadcastManager,
                                                                  NSDictionary device,
                                                                  int dataLength, NSArray methodArray) {

        List<BroadcastManager> broadcastManagerList = new ArrayList<BroadcastManager>();

        List<BroadcastMethod> methodList = new ArrayList<BroadcastMethod>();

        for (int i = 0; i < methodArray.size(); i++) {
            NSDictionary method = (NSDictionary) methodArray.get(i);

            int valueIndex = method.getInt("valueIndex");

            BroadcastMethod broadcastMethod = new BroadcastMethod();

            broadcastMethod.setName(method.getString("name"));

            broadcastMethod.setConvert(method.getString("convert"));

            broadcastMethod.setUnitType(method.getString("unitType"));

            broadcastMethod.setIndex(valueIndex);

            methodList.add(broadcastMethod);
        }

        broadcastManager.setBroadcastMethodList(methodList);

        broadcastManagerList.add(broadcastManager);

        return broadcastManagerList;
    }

    private static int[] getIntArray(NSArray valueIndex){
        int length = valueIndex.size();

        int[] valueIndexArray = new int[length];

        for (int j = 0; j < length; j++) {
            valueIndexArray[j] = Integer.parseInt(valueIndex.get(j).toString());
        }

        return valueIndexArray;
    }
}
