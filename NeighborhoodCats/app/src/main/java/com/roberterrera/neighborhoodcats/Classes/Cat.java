package com.roberterrera.neighborhoodcats.Classes;

import android.widget.ImageView;

import io.realm.RealmObject;


/**
 * Created by Rob on 3/22/16.
 */
public class Cat extends RealmObject {
    private int id;
    private String mName;
    private String mDesc;
    private String mPhoto; // file location of image
    private String mLocation;

    public Cat(int id, String name, String desc, String photo, String location) {
        this.id = id;
        this.mName = name;
        this.mDesc = desc;
        this.mPhoto = photo;
        this.mLocation = location;
    }

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

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
    }
}
