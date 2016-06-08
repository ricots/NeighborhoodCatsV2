
package com.roberterrera.neighborhoodcats.models.petfinderclasses;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Petfinder {

    @SerializedName("@xmlns:xsi")
    @Expose
    private String XmlnsXsi;
    @SerializedName("lastOffset")
    @Expose
    private LastOffset lastOffset;
    @SerializedName("shelters")
    @Expose
    private Shelters shelters;
    @SerializedName("header")
    @Expose
    private Header header;
    @SerializedName("@xsi:noNamespaceSchemaLocation")
    @Expose
    private String XsiNoNamespaceSchemaLocation;

    /**
     * 
     * @return
     *     The XmlnsXsi
     */
    public String getXmlnsXsi() {
        return XmlnsXsi;
    }

    /**
     * 
     * @param XmlnsXsi
     *     The @xmlns:xsi
     */
    public void setXmlnsXsi(String XmlnsXsi) {
        this.XmlnsXsi = XmlnsXsi;
    }

    /**
     * 
     * @return
     *     The lastOffset
     */
    public LastOffset getLastOffset() {
        return lastOffset;
    }

    /**
     * 
     * @param lastOffset
     *     The lastOffset
     */
    public void setLastOffset(LastOffset lastOffset) {
        this.lastOffset = lastOffset;
    }

    /**
     * 
     * @return
     *     The shelters
     */
    public Shelters getShelters() {
        return shelters;
    }

    /**
     * 
     * @param shelters
     *     The shelters
     */
    public void setShelters(Shelters shelters) {
        this.shelters = shelters;
    }

    /**
     * 
     * @return
     *     The header
     */
    public Header getHeader() {
        return header;
    }

    /**
     * 
     * @param header
     *     The header
     */
    public void setHeader(Header header) {
        this.header = header;
    }

    /**
     * 
     * @return
     *     The XsiNoNamespaceSchemaLocation
     */
    public String getXsiNoNamespaceSchemaLocation() {
        return XsiNoNamespaceSchemaLocation;
    }

    /**
     * 
     * @param XsiNoNamespaceSchemaLocation
     *     The @xsi:noNamespaceSchemaLocation
     */
    public void setXsiNoNamespaceSchemaLocation(String XsiNoNamespaceSchemaLocation) {
        this.XsiNoNamespaceSchemaLocation = XsiNoNamespaceSchemaLocation;
    }

}
