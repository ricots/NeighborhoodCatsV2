package com.roberterrera.neighborhoodcats.models;

/**
 * Created by Rob on 3/22/16.
 */
public class Cat {
    private int id;
    private String mName;
    private String mDesc;
    private String mPhoto; // file location of image
    private String mLatitude;
    private String mLongitude;

    public Cat(int id, String name, String desc, String latitude, String longitude, String photo) {
        this.id = id;
        this.mName = name;
        this.mDesc = desc;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mPhoto = photo;

    }

    public String getLatitude() {
        return mLatitude;
    }

    public void setLatitude(String latitude) {
        this.mLatitude = latitude;
    }

    public String getLongitude() {
        return mLongitude;
    }

    public void setLongitude(String longitude) {
        this.mLongitude = longitude;
    }

    public Cat() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        this.mDesc = desc;
    }

    public String getPhoto() {
        return mPhoto;
    }

    public void setPhoto(String photo) {
        this.mPhoto = photo;
    }
}
