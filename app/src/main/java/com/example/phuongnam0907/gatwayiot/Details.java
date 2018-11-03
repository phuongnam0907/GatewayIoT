package com.example.phuongnam0907.gatwayiot;

import org.json.JSONException;
import org.json.JSONObject;

public class Details {
    private String mGateway;
    private String mTime;
    private SensorData mSensor;
    private SensorData[] mArray;
    private int length = -1;

    public Details(String idgateway, String time, SensorData sensor){
        mGateway = idgateway;
        mTime = time;
        mSensor = sensor;
    }

    public  Details(String idgateway, String time, SensorData[] sensorData){
        mGateway = idgateway;
        mTime = time;
        mArray = sensorData;
        length = sensorData.length;
    }
    public String getmGateway() {
        return mGateway;
    }

    public String getmTime() {
        return mTime;
    }

    public SensorData getmSensor() {
        return mSensor;
    }

    public SensorData[] getmArray() { return mArray; }

    public void setmGateway(String mGateway) {
        this.mGateway = mGateway;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public void setmSensor(SensorData mSensor) {
        this.mSensor = mSensor;
    }

    public void setmArray(SensorData[] mArray) {
        this.mArray = mArray;
    }

    @Override
    public String toString(){
        JSONObject jsonGateway = new JSONObject();
        JSONObject jsonData = new JSONObject();
        try{
            jsonGateway.put("gateway",mGateway);
            jsonGateway.put("time",mTime);

            if (length <= 0){
                jsonData.put("id",mSensor.getmId());
                jsonData.put("value",mSensor.getmValue());

                jsonGateway.put("sensor",mSensor);
            } else {
                for (int j = 0; j < length; j++){
                    jsonData.put("id+" + j,mSensor.getmId());
                    jsonData.put("value+" + j,mSensor.getmValue());

                    jsonGateway.put("sensor+" + j,mSensor);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonGateway.toString();
    }
}
