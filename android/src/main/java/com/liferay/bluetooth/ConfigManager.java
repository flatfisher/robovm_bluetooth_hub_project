package com.liferay.bluetooth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    class Device {
        String deviceName;

        String serverType;
    }

    private List<Device> deviceList;

    private List<GattManager> gattManagerList;

    public ConfigManager(String jsonConfigData) {
        deviceList = new ArrayList<Device>();

        gattManagerList = new ArrayList<GattManager>();

        convertJsonStringToJsonObject(jsonConfigData);
    }

    public boolean isCheckConfig(String deviceName){
        for (String device: getDeviceNameList()){
            if (device.equals(deviceName)){
                return true;
            }
        }
        return false;
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

    private void convertJsonStringToJsonObject(String jsonConfigData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonConfigData);

            setDeviceList(jsonObject);

            createManager(jsonObject);
        } catch (JSONException jsonException) {

        }
    }

    private void setDeviceList(JSONObject configData) throws JSONException {
        JSONArray deviceArray = configData.getJSONArray("device");

        int length = deviceArray.length();

        for (int i = 0; i < length; i++) {
            JSONObject deviceObject = deviceArray.getJSONObject(i);

            Device device = new Device();

            device.deviceName = deviceObject.getString("name");

            device.serverType = deviceObject.getString("server");

            deviceList.add(device);
        }
    }

    private void createManager(JSONObject configData) throws JSONException{
        JSONArray deviceArray = configData.getJSONArray("device");

        int length = deviceArray.length();

        for (int i = 0; i < length; i++) {
            JSONObject deviceObject = deviceArray.getJSONObject(i);

            String server = deviceObject.getString("server");

            if (server.equals(Constants.GATT)) {
                createGattManager(deviceArray, i);
            } else if (server.equals(Constants.BROADCAST)) {
                //for broadcast.
            } else {
                // error no server type.
            }
        }
    }

    private void createGattManager(JSONArray deviceArray, int index) throws JSONException {
        GattManager gattManager = new GattManager();

        JSONObject deviceObject = deviceArray.getJSONObject(index);

        gattManager.setDeviceName(deviceObject.getString("name"));

        JSONArray methodArray = deviceObject.getJSONArray("method");

        int length = methodArray.length();

        for (int i = 0; i < length; i++) {
            JSONObject methodObject = methodArray.getJSONObject(i);

            gattManager.setMethodName(methodObject.getString("name"));

            gattManager.setConvertType(methodObject.getString("convert"));

            JSONArray typeArray = methodObject.getJSONArray("valueType");

            gattManager.setValueTypeLabelList(getStringListFromJsonArray(typeArray));

            JSONArray unitArray = methodObject.getJSONArray("unitType");

            gattManager.setValueUnitTypeList(getStringListFromJsonArray(unitArray));

            JSONArray processArray = methodObject.getJSONArray("process");

            gattManager.setGattProcessList(getGattProcessList(processArray));

            gattManagerList.add(gattManager);
        }
    }

    private List<String> getStringListFromJsonArray(JSONArray jsonArray) throws JSONException {
        List<String> list = new ArrayList<String>();

        int length = jsonArray.length();

        for (int i = 0; i < length; i++) {
            list.add(jsonArray.getString(i));
        }

        return list;
    }

    private List<GattProcess> getGattProcessList(JSONArray processArray)throws JSONException{
        List<GattProcess> gattProcessList = new ArrayList<GattProcess>();

        int length = processArray.length();

        for (int i = 0;i<length;i++){
            JSONObject process = processArray.getJSONObject(i);

            GattProcess gattProcess = new GattProcess();

            gattProcess.setMethod(process.getString("method"));

            gattProcess.setCharacteristic(process.getString("characteristic"));

            gattProcess.setService(process.getString("service"));

            gattProcess.setValue(process.getString("value"));

            gattProcessList.add(gattProcess);
        }
        return gattProcessList;
    }
}
