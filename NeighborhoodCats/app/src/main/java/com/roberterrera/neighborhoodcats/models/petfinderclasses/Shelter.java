
package com.roberterrera.neighborhoodcats.models.petfinderclasses;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Shelter {

    @SerializedName("@encoding")
    @Expose
    private String Encoding;
    @SerializedName("@version")
    @Expose
    private String Version;
    @SerializedName("petfinder")
    @Expose
    private Petfinder petfinder;

    /**
     * 
     * @return
     *     The Encoding
     */
    public String getEncoding() {
        return Encoding;
    }

    /**
     * 
     * @param Encoding
     *     The @encoding
     */
    public void setEncoding(String Encoding) {
        this.Encoding = Encoding;
    }

    /**
     * 
     * @return
     *     The Version
     */
    public String getVersion() {
        return Version;
    }

    /**
     * 
     * @param Version
     *     The @version
     */
    public void setVersion(String Version) {
        this.Version = Version;
    }

    /**
     * 
     * @return
     *     The petfinder
     */
    public Petfinder getPetfinder() {
        return petfinder;
    }

    /**
     * 
     * @param petfinder
     *     The petfinder
     */
    public void setPetfinder(Petfinder petfinder) {
        this.petfinder = petfinder;
    }

}
