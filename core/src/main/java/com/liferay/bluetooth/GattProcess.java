package com.liferay.bluetooth;

public class GattProcess {

    public static final int READ = 0;

    public static final int WRITE = 1;

    public static final int NOTIFY = 2;

    public static final int FINISH = 3;

    public static final int NO_METHOD_ERROR = -1;

    private String method;

    private String service;

    private String characteristic;

    private String value;

    public void setMethod(String method) {
        this.method = method;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getMethod() {

        if (method.equals("read")){
            return READ;

        }else if(method.equals("write")){
            return WRITE;

        }else if (method.equals("notify")){
            return NOTIFY;

        }else if(method.equals("finish")){
            return FINISH;

        }else{
            // no method error
            return NO_METHOD_ERROR;
        }
    }

    public String getService() {
        return service;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public String getValue() {
        return value;
    }

}
