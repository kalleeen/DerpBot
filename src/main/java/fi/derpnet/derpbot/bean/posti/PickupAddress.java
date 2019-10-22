
package fi.derpnet.derpbot.bean.posti;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PickupAddress {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("street")
    @Expose
    private String street;
    @SerializedName("postcode")
    @Expose
    private String postcode;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("availability")
    @Expose
    private String availability;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("pupCode")
    @Expose
    private String pupCode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPupCode() {
        return pupCode;
    }

    public void setPupCode(String pupCode) {
        this.pupCode = pupCode;
    }

}
