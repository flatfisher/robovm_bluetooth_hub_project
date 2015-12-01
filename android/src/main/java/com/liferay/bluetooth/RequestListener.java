package com.liferay.bluetooth;

public interface RequestListener {

    public void onResponse(String jsonString, int code);

    public void onError(String errorMessage);

}
