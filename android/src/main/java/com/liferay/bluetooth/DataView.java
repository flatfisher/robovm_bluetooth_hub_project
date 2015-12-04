package com.liferay.bluetooth;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DataView extends LinearLayout {

    private Context context;

    private int rowHeight;

    private LinearLayout typeTextContainer;

    private LinearLayout valueTextContainer;

    private LinearLayout unitTextContainer;

    private List<TextView> valueTextList;

    private String deviceName;

    private List<String> valueTypeLabelList;

    private List<String> valueUnitTypeList;

//    private int defaultTextColor;

    public DataView(Context context, int rowHeight, GattManager gattManager) {
        super(context);

        this.context = context;

        this.rowHeight = rowHeight;

        setMinimumHeight(rowHeight);

        setOrientation(HORIZONTAL);

        deviceName = gattManager.getDeviceName();

        valueTypeLabelList = gattManager.getValueTypeLabelList();

        valueUnitTypeList = gattManager.getValueUnitTypeList();

//        defaultTextColor = Color.argb(0,75,75,75);

        initContainers();

        initTextViews();
    }

    private void initContainers() {
        LayoutParams typeParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        typeParams.weight = 6;
        typeTextContainer = new LinearLayout(context);
        typeTextContainer.setOrientation(VERTICAL);
        addView(typeTextContainer,typeParams);

        LayoutParams valueParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        valueParams.weight = 2;
        valueTextContainer = new LinearLayout(context);
        valueTextContainer.setOrientation(VERTICAL);
        addView(valueTextContainer,valueParams);

        LayoutParams unitParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        unitParams.weight = 2;
        unitTextContainer = new LinearLayout(context);
        unitTextContainer.setOrientation(VERTICAL);
        addView(unitTextContainer,unitParams);
    }

    private void initTextViews(){
        LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0);

        params.weight = 1;

        setTypeText(params);

        setValueTextList(params);

        setUnitText(params);
    }

    private void setTypeText(LayoutParams params) {
        for (String type:valueTypeLabelList){
            TextView typeText = new TextView(context);

            typeText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            typeText.setTextSize(rowHeight/20);

            typeText.setText(type);

            typeTextContainer.addView(typeText, params);
        }
    }

    private void setValueTextList(LayoutParams params){
        valueTextList = new ArrayList<TextView>();

        int size = valueUnitTypeList.size();

        for (int i = 0;i<size;i++){
            TextView valueText = new TextView(context);

            valueText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);

            valueText.setTextSize(rowHeight/20);

            valueText.setText("--");

            valueTextContainer.addView(valueText, params);

            valueTextList.add(valueText);
        }
    }

    private void setUnitText(LayoutParams params) {
        for (String unit:valueUnitTypeList){
            TextView unitText = new TextView(context);

            unitText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

            unitText.setTextSize(rowHeight/20);

            unitText.setText(unit);

            unitTextContainer.addView(unitText, params);
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void updateData(List<String> value) {
        int size = valueTextList.size();

        for (int i = 0;i<size;i++){

            valueTextList.get(i).setText(value.get(i));
        }
    }

    //for disconnect
    public void updateData() {
        int size = valueTextList.size();

        for (int i = 0;i<size;i++){
            valueTextList.get(i).setTextColor(Color.RED);

            valueTextList.get(i).setText("--");
        }
    }

    public DataView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DataView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DataView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
