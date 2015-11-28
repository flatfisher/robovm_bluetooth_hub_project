package com.liferay.bluetooth;

import org.robovm.apple.foundation.NSArray;
import org.robovm.apple.foundation.NSDictionary;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private List<String> deviceNameList;

    private NSDictionary jsonConfigData;

    public ConfigManager(NSDictionary configData){

        deviceNameList = new ArrayList<String>();

        jsonConfigData = configData;

        setDeviceNameList();

    }

    public List<String> getDeviceNameList(){
        return deviceNameList;
    }

    private void setDeviceNameList(){
        NSArray deviceArray = (NSArray) jsonConfigData.get("device");

        int size = deviceArray.size();

        for(int i = 0;i<size;i++){
            NSDictionary device = (NSDictionary) deviceArray.get(i);

            String name = device.getString("name");

            deviceNameList.add(name);
        }

    }

}
