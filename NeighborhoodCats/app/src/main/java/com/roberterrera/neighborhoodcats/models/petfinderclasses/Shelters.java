
package com.roberterrera.neighborhoodcats.models.petfinderclasses;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Shelters {

    @SerializedName("shelter")
    @Expose
    private List<Shelter_> shelter = new ArrayList<Shelter_>();

    /**
     * 
     * @return
     *     The shelter
     */
    public List<Shelter_> getShelter() {
        return shelter;
    }

    /**
     * 
     * @param shelter
     *     The shelter
     */
    public void setShelter(List<Shelter_> shelter) {
        this.shelter = shelter;
    }

}
