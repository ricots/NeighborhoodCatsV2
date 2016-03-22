package com.roberterrera.neighborhoodcats.Classes;

import android.widget.ImageView;

/**
 * Created by Rob on 3/22/16.
 */
public class CatClass {
    String mName;
    String mDesc;
    ImageView mPhoto;
    String mLocation;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String mDesc) {
        this.mDesc = mDesc;
    }

    public ImageView getPhoto() {
        return mPhoto;
    }

    public void setPhoto(ImageView mPhoto) {
        this.mPhoto = mPhoto;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String mLocation) {
        this.mLocation = mLocation;
    }
}
