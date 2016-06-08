
package com.roberterrera.neighborhoodcats.models.petfinderclasses;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("org.jsonschema2pojo")
public class Status {

    @SerializedName("message")
    @Expose
    private Message message;
    @SerializedName("code")
    @Expose
    private Code code;

    /**
     * 
     * @return
     *     The message
     */
    public Message getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     *     The message
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * 
     * @return
     *     The code
     */
    public Code getCode() {
        return code;
    }

    /**
     * 
     * @param code
     *     The code
     */
    public void setCode(Code code) {
        this.code = code;
    }

}
