package com.liferay.bluetooth;

import org.robovm.apple.foundation.*;
import org.robovm.objc.block.VoidBlock3;

public class NetworkManager {

    private NetworkManager() {
    }

    public static void getConfiguration(VoidBlock3<NSData, NSURLResponse, NSError> newDataTaskChangeListener) {
        NSURL requestUrl = new NSURL("https://apiflatfisher.appspot.com/config");

        NSURLRequest request = new NSURLRequest(requestUrl);

        NSURLSession session = NSURLSession.getSharedSession();

        NSURLSessionDataTask dataTask = session.newDataTask(request, newDataTaskChangeListener);

        dataTask.resume();
    }

    public static void postToServer(String value) {

        NSURL requestUrl = new NSURL("https://apiflatfisher.appspot.com/");

        NSMutableURLRequest nsMutableURLRequest = new NSMutableURLRequest();

        nsMutableURLRequest.setURL(requestUrl);

        nsMutableURLRequest.setHTTPMethod("POST");

        NSString requestBody = new NSString("name=" + value);

        nsMutableURLRequest.setHTTPBody(requestBody.toData(NSStringEncoding.UTF8));

        NSURLSession nsurlSession = NSURLSession.getSharedSession();

        NSURLSessionDataTask nsurlSessionDataTask = nsurlSession.newDataTask(nsMutableURLRequest);

        nsurlSessionDataTask.resume();

    }

}
