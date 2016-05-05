package com.roberterrera.neighborhoodcats.models;

import retrofit2.http.Path;

/**
 * Created by Rob on 5/4/16.
 */
public class Shelter {
    String mLocation;
    String mName;

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Shelter(String location, String name) {
        this.mLocation = location;
        this.mName = name;
    }
}
