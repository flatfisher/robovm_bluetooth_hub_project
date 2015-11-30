package com.liferay.bluetooth;

public class BroadcastMethod {

    public boolean FIRST;

    public String defaultValue;

    public String secondValue;

    private String convert;

    private String unitType;

    private String name;

    private int index;

    public BroadcastMethod(){

        FIRST = true;

        defaultValue = null;

        secondValue = null;

    }

    public void setConvert(String convert) {
        this.convert = convert;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getConvert() {
        return convert;
    }

    public String getUnitType() {
        return unitType;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

}
