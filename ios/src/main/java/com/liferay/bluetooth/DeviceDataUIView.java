package com.liferay.bluetooth;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.uikit.NSTextAlignment;
import org.robovm.apple.uikit.UILabel;
import org.robovm.apple.uikit.UIView;

import java.util.ArrayList;
import java.util.List;

public class DeviceDataUIView extends UIView {

    private double displayHeight;

    private double displayWidth;

    private String deviceName;

    private List<String> valueTypeList;

    private List<UILabel> valueLabelList;

    private List<String> valueUnitList;

    public DeviceDataUIView(CGRect cgRect,
                       String deviceName,
                       List<String> valueTypeList,
                       List<String> valueUnitList) {

        super(cgRect);

        displayHeight = getFrame().getHeight();

        displayWidth = getFrame().getWidth();

        this.deviceName = deviceName;

        this.valueTypeList = valueTypeList;

        this.valueUnitList = valueUnitList;

        setValueTypeLabel();

        setValueLabel();

        setValueUnitLabel();

    }

    public void setDeviceName(String deviceName){

        this.deviceName = deviceName;

    }

    public String getDeviceName() {

        return deviceName;

    }

    private void setValueTypeLabel() {

        int count = valueTypeList.size();

        double width = displayWidth / 2;

        double height = displayHeight;

        double dividedHeight = displayHeight / count;

        for (int i = 0; i < count; i++) {

            CGRect position = new CGRect(0, dividedHeight * (i + 1) - dividedHeight / 2, width, height / 3);

            UILabel valueTypeLabel = new UILabel(position);

            valueTypeLabel.setTextAlignment(NSTextAlignment.Left);

            valueTypeLabel.setText(valueTypeList.get(i));

            addSubview(valueTypeLabel);
        }

    }

    private void setValueLabel() {

        int count = valueUnitList.size();

        valueLabelList = new ArrayList<UILabel>();

        double width = ((displayWidth / 2) / 3);

        double height = displayHeight;

        double dividedHeight = displayHeight / count;

        for (int i = 0; i < count; i++) {

            CGRect position = new CGRect(displayWidth / 2,
                    dividedHeight * (i + 1) - dividedHeight / 2, width, height / 3);

            UILabel valueLabel = new UILabel(position);

            valueLabel.setTextAlignment(NSTextAlignment.Right);

            valueLabel.setText("--");

            valueLabelList.add(valueLabel);

            addSubview(valueLabel);
        }

    }

    public void setValue(List<String> valueList) {

        int count = valueUnitList.size();

        for (int i = 0; i < count; i++) {

            String value = valueList.get(i);

            if (value == null || value.length() <= 0){

                valueLabelList.get(i).setText("--");

            }else{

                valueLabelList.get(i).setText(value);

            }

        }

    }

    private void setValueUnitLabel() {

        int count = valueUnitList.size();

        double width = displayWidth / 4;

        double height = displayHeight;

        double dividedHeight = displayHeight / count;

        for (int i = 0; i < count; i++) {

            CGRect position = new CGRect(width * 3, dividedHeight * (i + 1) - dividedHeight / 2, width, height / 3);

            UILabel valueTypeLabel = new UILabel(position);

            valueTypeLabel.setTextAlignment(NSTextAlignment.Left);

            valueTypeLabel.setText(valueUnitList.get(i));

            addSubview(valueTypeLabel);
        }

    }

}
