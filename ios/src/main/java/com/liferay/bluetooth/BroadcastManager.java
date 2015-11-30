package com.liferay.bluetooth;

import java.util.ArrayList;
import java.util.List;

public class BroadcastManager {

    private String deviceName;

    private byte[] values;

    private boolean isRunning = true;

    private List<String> valueDataList;

    private int dataLength;

    private List<BroadcastMethod> broadcastMethodList;

    private List<String> valueTypeLabelList;

    private List<String> valueUnitTypeList;

    private String inactiveType;

    private int[] inactiveValueIndexArray;

    private String unstableType;

    private int[] unstableValueIndexArray;

    private int count = 0;

    private int prevCount = 0;

    public BroadcastManager() {

    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setValues(byte[] values) {

        this.values = values;

        setConvertedData();

    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public void setBroadcastMethodList(List<BroadcastMethod> broadcastMethodList) {
        this.broadcastMethodList = broadcastMethodList;

        setValueTypeLabelList();

        setValueUnitTypeList();

    }

    public void setInactiveType(String inactiveType) {
        this.inactiveType = inactiveType;
    }

    public void setInactiveValueIndexArray(int[] inactiveValueIndexArray) {
        this.inactiveValueIndexArray = inactiveValueIndexArray;
    }

    public void setUnstableType(String unstableType) {
        this.unstableType = unstableType;
    }

    public void setUnstableValueIndexArray(int[] unstableValueIndexArray) {
        this.unstableValueIndexArray = unstableValueIndexArray;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public byte[] getValues() {
        return values;
    }

    public int getDataLength() {
        return dataLength;
    }

    public List<String> getValueData() {

        if (isRunning) {

            return valueDataList;

        } else {

            return valueDataList;

        }
    }

    private void setConvertedData() {
        valueDataList = new ArrayList<>();

        setRunning();

        if (isRunning) {

            parseAndSetValueData();

        } else {

            setNoDataValue();

        }
    }

    private void resetDefaultData() {

        for (BroadcastMethod broadcastMethod : broadcastMethodList) {

            broadcastMethod.secondValue = null;

            broadcastMethod.defaultValue = null;

            broadcastMethod.FIRST = true;

        }

    }

    private void setNoDataValue() {

        int size = broadcastMethodList.size();

        for (int i = 0; i < size; i++) {

            valueDataList.add("");

        }
    }

    private void setRunning() {

        if (!isInactive() && !isUnstable()) {

            isRunning = true;

        } else {

            isRunning = false;

        }
    }

    private boolean isInactive() {

        if (inactiveType.equals("same")) {

            int length = inactiveValueIndexArray.length;

            for (int i = 0; i < length; i++) {

                if (i + 1 < length) {

                    if (values[inactiveValueIndexArray[i]] !=

                            values[inactiveValueIndexArray[i + 1]]) {

                        return false;

                    } else {

                        return true;

                    }

                }
            }

        }
        return true;

    }

    private boolean isUnstable() {
        boolean unstable = true;

        if (unstableType.equals("count")) {

            int length = unstableValueIndexArray.length;

            for (int i = 0; i < length; i++) {

                count = values[unstableValueIndexArray[i]];

                if (count >= prevCount) {

                    unstable = false;

                }

                prevCount = count;

            }

        }

        return unstable;

    }

    private void parseAndSetValueData() {

        for (BroadcastMethod broadcastMethod : broadcastMethodList) {

            int indexArray = broadcastMethod.getIndex();

            String convert = broadcastMethod.getConvert();

            String convertedData = getConvertedData(values[indexArray], convert);

            if (isRunning) {

                if (broadcastMethod.defaultValue == null) {

                    broadcastMethod.defaultValue = convertedData;

                    valueDataList.add("");

                } else {


                    if (broadcastMethod.FIRST) {

                        broadcastMethod.secondValue = convertedData;

                        broadcastMethod.FIRST = false;

                        valueDataList.add("");

                    } else {

                        if (broadcastMethod.secondValue.equals(convertedData)) {

                            valueDataList.add("");

                        } else {

                            System.out.println(broadcastMethod.secondValue + "  " + convertedData);

                            valueDataList.add(convertedData);

                        }

                    }

                }

            } else {

                resetDefaultData();

                valueDataList.add("");

            }

        }

    }

    private String getConvertedData(byte value, String convert) {
        String converted = null;

        if (convert.equals("decimal")) {

            converted = Convert.byteToDecimalString(value);

            return converted;
        }

        return converted;

    }

    private void setValueTypeLabelList() {

        valueTypeLabelList = new ArrayList<String>();

        for (BroadcastMethod broadcastMethod : broadcastMethodList) {

            valueTypeLabelList.add(broadcastMethod.getName());

        }

    }

    private void setValueUnitTypeList() {

        valueUnitTypeList = new ArrayList<String>();

        for (BroadcastMethod broadcastMethod : broadcastMethodList) {

            valueUnitTypeList.add(broadcastMethod.getUnitType());

        }

    }

    public List<String> getValueTypeLabelList() {
        return valueTypeLabelList;
    }

    public List<String> getValueUnitTypeList() {
        return valueUnitTypeList;
    }

}
