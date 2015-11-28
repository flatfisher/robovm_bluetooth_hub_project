package com.liferay.bluetooth;

import org.robovm.apple.foundation.NSDictionary;
import org.robovm.apple.foundation.NSUserDefaults;

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

//        List<ConfigManager> configList = NetworkManager.getConfigManagerList(configData);

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

}
