package com.liferay.bluetooth;

import org.robovm.apple.foundation.NSDictionary;
import org.robovm.apple.foundation.NSUserDefaults;

import java.util.ArrayList;
import java.util.List;

public class DataManager {

    public static void saveConfigData(NSDictionary jsonConfigData) {
        NSUserDefaults nsUserDefaults = new NSUserDefaults();

        nsUserDefaults.put(Constants.CONFIG_DATA_KEY, jsonConfigData);

        nsUserDefaults.synchronize();
    }

    public static NSDictionary getConfigData() {
        NSUserDefaults nsUserDefaults = new NSUserDefaults();

        NSDictionary configData = (NSDictionary) nsUserDefaults.get(Constants.CONFIG_DATA_KEY);

        return configData;
    }

    public static boolean isConfigData() {
        NSUserDefaults nsUserDefaults = new NSUserDefaults();

        NSDictionary nsDictionary = nsUserDefaults.getDictionary(Constants.CONFIG_DATA_KEY);

        if (nsDictionary == null) {

            return false;

        } else if (nsDictionary.size() <= 0) {

            return false;

        } else {

            return true;
        }
    }

    public static void saveCheckedDeviceList(List<String> checkedDeviceList){
        NSUserDefaults nsUserDefaults = NSUserDefaults.getStandardUserDefaults();

        nsUserDefaults.put(Constants.CHECKED_DEVICE_KEY,checkedDeviceList);

        nsUserDefaults.synchronize();
    }

    public static List<String> getCheckedDeviceList(){
        NSUserDefaults nsUserDefaults = NSUserDefaults.getStandardUserDefaults();

        List<String> deviceList = nsUserDefaults.getStringArray(Constants.CHECKED_DEVICE_KEY);

        if (deviceList == null){

            List<String> newDeviceList = new ArrayList<String>();

            nsUserDefaults.put(Constants.CHECKED_DEVICE_KEY,newDeviceList);

            return newDeviceList;
        }else{
            return deviceList;
        }
    }

    public static void removeCheckedDevice(String deviceName){
        NSUserDefaults nsUserDefaults = NSUserDefaults.getStandardUserDefaults();

        List<String> deviceList = nsUserDefaults.getStringArray(Constants.CHECKED_DEVICE_KEY);

        deviceList.remove(deviceName);

        saveCheckedDeviceList(deviceList);
    }

    public static boolean isCheckedData() {
        NSUserDefaults nsUserDefaults = new NSUserDefaults();

        NSDictionary nsDictionary = nsUserDefaults.getDictionary(Constants.CHECKED_DEVICE_KEY);

        if (nsDictionary == null) {
            return false;
        } else if (nsDictionary.size() <= 0) {
            return false;
        } else {
            return true;
        }
    }
}
