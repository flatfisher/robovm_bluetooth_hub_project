package com.liferay.bluetooth;

import java.util.ArrayList;
import java.util.List;

public class GattManager {

    private String deviceName;

    private String methodName;

    private String convertType;

    private List<String> valueTypeLabelList;

    private List<String> valueUnitTypeList;

    private List<GattProcess> gattProcessList;

    private int currentCount;

    public GattManager() {
        currentCount = 0;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setConvertType(String convertType) {
        this.convertType = convertType;
    }

    public void setValueTypeLabelList(List<String> valueTypeLabelList) {
        this.valueTypeLabelList = valueTypeLabelList;
    }

    public void setValueUnitTypeList(List<String> valueUnitTypeList) {
        this.valueUnitTypeList = valueUnitTypeList;
    }

    public void setGattProcessList(List<GattProcess> gattProcessList) {
        this.gattProcessList = gattProcessList;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getConvertType() {
        return convertType;
    }

    public List<String> getValueTypeLabelList() {
        return valueTypeLabelList;
    }

    public List<String> getValueUnitTypeList() {
        return valueUnitTypeList;
    }

    public List<GattProcess> getGattProcessList() {
        return gattProcessList;
    }

    public GattProcess getGattProcess() {
        return gattProcessList.get(currentCount);
    }

    public GattProcess getLastGattProcess() {
        int size = gattProcessList.size();

        int index = 0;

        if (currentCount > size) {
            index = currentCount - 1;
        }
        return gattProcessList.get(index);
    }

    public void upCurrentCount() {
        int size = gattProcessList.size();

        if (currentCount + 1 >= size) {

            currentCount = size - 1;

        } else {
            currentCount = currentCount + 1;
        }
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public List<String> getValue(byte[] valueArray) {
        int dataCount = valueUnitTypeList.size();

        List<String> valueList = new ArrayList<String>();

        if (dataCount == 1) {
            String valueString = "";

            for (byte value : valueArray) {
                valueString = valueString + getConvertedData(value);
            }

            valueList.add(valueString);

        } else {

            for (int i = 0; i < dataCount; i++) {
                valueList.add(getConvertedData(valueArray[i]));
            }
        }
        return valueList;
    }

    private String getConvertedData(byte value) {
        if (convertType.equals("decimal")) {

            return Convert.byteToDecimalString(value);

        } else if (convertType.equals("ascii")) {

            return Convert.byteToAscii(value);

        } else {
            return null;
        }
    }
}
