package com.liferay.bluetooth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DataView extends LinearLayout {

    private Context context;

    private LinearLayout typeTextContainer;

    private LinearLayout valueTextContainer;

    private LinearLayout unitTextContainer;

    private List<TextView> valueTextList;

    private String deviceName;

    private List<String> valueTypeLabelList;

    private List<String> valueUnitTypeList;

    public DataView(Context context, int rowHeight, GattManager gattManager) {
        super(context);

        this.context = context;

        setMinimumHeight(rowHeight);

        setOrientation(HORIZONTAL);

        deviceName = gattManager.getDeviceName();

        valueTypeLabelList = gattManager.getValueTypeLabelList();

        valueUnitTypeList = gattManager.getValueUnitTypeList();

        initContainers();

        initTextViews();
    }

    private void initContainers() {
        LayoutParams containerParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);

        typeTextContainer = new LinearLayout(context);

        typeTextContainer.setOrientation(VERTICAL);

        containerParams.weight = 6;

        addView(typeTextContainer,containerParams);

        valueTextContainer = new LinearLayout(context);

        valueTextContainer.setOrientation(VERTICAL);

        containerParams.weight = 2;

        addView(valueTextContainer,containerParams);

        unitTextContainer = new LinearLayout(context);

        unitTextContainer.setOrientation(VERTICAL);

        containerParams.weight = 2;

        addView(unitTextContainer,containerParams);
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

            typeText.setText(type);

            typeTextContainer.addView(typeText, params);
        }
    }

    private void setValueTextList(LayoutParams params){
        valueTextList = new ArrayList<TextView>();

        int size = valueUnitTypeList.size();

        for (int i = 0;i<size;i++){
            TextView valueText = new TextView(context);

            valueText.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

            valueTextContainer.addView(valueText, params);

            valueTextList.add(valueText);
        }
    }

    private void setUnitText(LayoutParams params) {
        for (String unit:valueUnitTypeList){

            TextView unitText = new TextView(context);

            unitText.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);

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
