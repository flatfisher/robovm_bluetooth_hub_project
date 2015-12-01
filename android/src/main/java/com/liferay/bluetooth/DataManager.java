package com.liferay.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {

    private DataManager() {

    }

    public static void saveConfigData(Context context, String jsonString) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE_NAME_CONFIG,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(Constants.CONFIG_DATA_KEY, jsonString);

        editor.commit();
    }

    public static String getConfigData(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME_CONFIG,
                Context.MODE_PRIVATE);

        return sharedPreferences.getString(Constants.CONFIG_DATA_KEY, Constants.NO_CONFIG_MESSAGE);

    }

    public static boolean isCheckConfigData(Context context) {

        if (!getConfigData(context).equals(Constants.NO_CONFIG_MESSAGE)) {

            return true;

        } else {

            return false;

        }

    }

}
