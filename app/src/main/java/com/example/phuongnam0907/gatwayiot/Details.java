package com.example.phuongnam0907.gatwayiot;

import org.json.JSONException;
import org.json.JSONObject;

public class Details {
    private String mGateway;
    private String mTime;
    private SensorData mSensor;

    public Details(String idgateway, String time, SensorData sensor){
        mGateway = idgateway;
        mTime = time;
        mSensor = sensor;
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

    public void setmGateway(String mGateway) {
        this.mGateway = mGateway;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public void setmSensor(SensorData mSensor) {
        this.mSensor = mSensor;
    }

    @Override
    public String toString(){
        JSONObject jsonGateway = new JSONObject();
        JSONObject jsonData = new JSONObject();
        try{
            jsonGateway.put("gateway",mGateway);
            jsonGateway.put("time",mTime);

            jsonData.put("id",mSensor.getmId());
            jsonData.put("value",mSensor.getmValue());

            jsonGateway.put("sensor",mSensor);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonGateway.toString();
    }
}
