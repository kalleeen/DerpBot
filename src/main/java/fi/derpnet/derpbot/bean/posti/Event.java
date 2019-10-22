
package fi.derpnet.derpbot.bean.posti;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Event {

    @SerializedName("eventCode")
    @Expose
    private String eventCode;
    @SerializedName("reasonCode")
    @Expose
    private Object reasonCode;
    @SerializedName("description")
    @Expose
    private Description description;
    @SerializedName("reasonDescription")
    @Expose
    private ReasonDescription reasonDescription;
    @SerializedName("additionalInfo")
    @Expose
    private AdditionalInfo_ additionalInfo;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("locationCode")
    @Expose
    private Object locationCode;
    @SerializedName("locationName")
    @Expose
    private String locationName;
    @SerializedName("role")
    @Expose
    private String role;

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public Object getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(Object reasonCode) {
        this.reasonCode = reasonCode;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public ReasonDescription getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(ReasonDescription reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public AdditionalInfo_ getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(AdditionalInfo_ additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Object getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(Object locationCode) {
        this.locationCode = locationCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
