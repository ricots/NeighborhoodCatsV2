
package com.roberterrera.neighborhoodcats.models.petfinderclasses;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Header {

    @SerializedName("timestamp")
    @Expose
    private Timestamp timestamp;
    @SerializedName("status")
    @Expose
    private Status status;
    @SerializedName("version")
    @Expose
    private Version version;

    /**
     * 
     * @return
     *     The timestamp
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * 
     * @param timestamp
     *     The timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * 
     * @return
     *     The status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *     The version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * 
     * @param version
     *     The version
     */
    public void setVersion(Version version) {
        this.version = version;
    }

}
