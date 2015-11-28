package com.liferay.bluetooth;

import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.UIActivityIndicatorView;
import org.robovm.apple.uikit.UIActivityIndicatorViewStyle;
import org.robovm.apple.uikit.UIColor;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.objc.annotation.CustomClass;
import org.robovm.objc.block.VoidBlock3;

@CustomClass("MainViewController")
public class MainViewController extends UIViewController {

    private ConfigManager configManager;

    private UIActivityIndicatorView uiActivityIndicatorView;

    @Override
    public void viewDidLoad() {
        super.viewDidLoad();

        getConfigData();

    }


    private void getConfigData() {
        //check saved configuration.
        if (DataManager.isConfigData()) {
            configManager = new ConfigManager(DataManager.getConfigData());

            System.out.println(configManager.getDeviceNameList());
        } else {
            getConfigurationFromServer();
        }
    }

    private void getConfigurationFromServer() {

        startActivityIndicator();

        NetworkManager.getConfiguration(new VoidBlock3<NSData, NSURLResponse, NSError>() {
            @Override
            public void invoke(NSData nsData, NSURLResponse nsurlResponse, NSError nsError) {
                try {

                    NSDictionary jsonConfigData = (NSDictionary) NSJSONSerialization.createJSONObject(nsData,
                            NSJSONReadingOptions.MutableLeaves);

                    DataManager.saveConfigData(jsonConfigData);

                    stopActivityIndicator();

                    moveToAddDeviceTableViewController();

                } catch (NSErrorException nserrorException) {

                }
            }
        });

    }

    private void moveToAddDeviceTableViewController() {

        performSegue("AddDevice", null);

    }

    private void startActivityIndicator() {
        uiActivityIndicatorView = new UIActivityIndicatorView(UIActivityIndicatorViewStyle.WhiteLarge);

        uiActivityIndicatorView.setCenter(getView().getCenter());

        uiActivityIndicatorView.setColor(UIColor.black());

        getView().addSubview(uiActivityIndicatorView);

        uiActivityIndicatorView.startAnimating();
    }

    private void stopActivityIndicator() {

        if (uiActivityIndicatorView.isAnimating()) {

            uiActivityIndicatorView.stopAnimating();

        }

    }

}
