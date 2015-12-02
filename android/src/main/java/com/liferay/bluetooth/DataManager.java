package com.liferay.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.*;

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

    public static void saveCheckedDevice(Context context, String deviceName){

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE_NAME_CONFIG,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        List<String> checkedList = getCheckedList(context);

        if (!isOverlap(deviceName,checkedList)){

            checkedList.add(deviceName);

        }


        editor.putString(Constants.CHECKED_DEVICE_KEY, TextUtils.join(",", checkedList));

        editor.commit();

    }

    public static void removeCheckedDevice(Context context, String deviceName){

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.SHARED_PREFERENCE_NAME_CONFIG,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        List<String> checkedList = getCheckedList(context);

        checkedList.remove(deviceName);

        editor.putString(Constants.CHECKED_DEVICE_KEY, TextUtils.join(",", checkedList));

        editor.commit();

    }

    public static boolean isOverlap(String checkValue,List<String>list){

        for (String value:list){

            if (checkValue.equals(value)){

                return true;

            }

        }

        return false;
    }

    public static List<String> getCheckedList(Context context){

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCE_NAME_CONFIG,
                Context.MODE_PRIVATE);

        String serialized = sharedPreferences.getString(Constants.CHECKED_DEVICE_KEY, Constants.NO_CONFIG_MESSAGE);


        List<String> list = null;

        if (serialized.equals(Constants.NO_CONFIG_MESSAGE)){

            list = new ArrayList<String>();

        }else{

            list = new LinkedList<String>(Arrays.asList(TextUtils.split(serialized, ",")));

        }

        return list;

    }

}
