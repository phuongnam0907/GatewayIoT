package com.example.phuongnam0907.gatwayiot;

public class SensorData {
    private String mId;
    private Float mValue;

    public SensorData(){

    }

    public SensorData (String id, Float value){
        mId = id;
        mValue = value;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public void setmValue(Float mValue) {
        this.mValue = mValue;
    }

    public String getmId() {
        return mId;
    }

    public Float getmValue() {
        return mValue;
    }
}
