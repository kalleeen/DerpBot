
package fi.derpnet.derpbot.bean.posti;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Shipment {

    @SerializedName("shipmentType")
    @Expose
    private String shipmentType;
    @SerializedName("items")
    @Expose
    private List<Item> items = null;
    @SerializedName("trackingCode")
    @Expose
    private String trackingCode;
    @SerializedName("errandCode")
    @Expose
    private Object errandCode;
    @SerializedName("waybillnumber")
    @Expose
    private Object waybillnumber;
    @SerializedName("phase")
    @Expose
    private String phase;
    @SerializedName("estimatedDeliveryTime")
    @Expose
    private String estimatedDeliveryTime;
    @SerializedName("pickupAddress")
    @Expose
    private PickupAddress pickupAddress;
    @SerializedName("lastPickupDate")
    @Expose
    private Object lastPickupDate;
    @SerializedName("product")
    @Expose
    private Product product;
    @SerializedName("extraServices")
    @Expose
    private List<ExtraService> extraServices = null;
    @SerializedName("weight")
    @Expose
    private String weight;
    @SerializedName("height")
    @Expose
    private String height;
    @SerializedName("width")
    @Expose
    private String width;
    @SerializedName("depth")
    @Expose
    private String depth;
    @SerializedName("volume")
    @Expose
    private String volume;
    @SerializedName("packageQuantity")
    @Expose
    private Object packageQuantity;
    @SerializedName("loadingMeters")
    @Expose
    private Object loadingMeters;
    @SerializedName("destinationPostcode")
    @Expose
    private String destinationPostcode;
    @SerializedName("destinationCity")
    @Expose
    private String destinationCity;
    @SerializedName("destinationCountry")
    @Expose
    private String destinationCountry;
    @SerializedName("recipientSignature")
    @Expose
    private Object recipientSignature;
    @SerializedName("codAmount")
    @Expose
    private Object codAmount;
    @SerializedName("codCurrency")
    @Expose
    private Object codCurrency;
    @SerializedName("codBic")
    @Expose
    private Object codBic;
    @SerializedName("codIban")
    @Expose
    private Object codIban;
    @SerializedName("codReference")
    @Expose
    private Object codReference;
    @SerializedName("senderSapLocisticsContractNumber")
    @Expose
    private String senderSapLocisticsContractNumber;
    @SerializedName("events")
    @Expose
    private List<Event> events = null;
    @SerializedName("sender")
    @Expose
    private Object sender;
    @SerializedName("lockerCode")
    @Expose
    private Object lockerCode;
    @SerializedName("destinationPostal")
    @Expose
    private Object destinationPostal;
    @SerializedName("sourcePostal")
    @Expose
    private Object sourcePostal;
    @SerializedName("nameRecipient")
    @Expose
    private Object nameRecipient;
    @SerializedName("flexProduct")
    @Expose
    private Boolean flexProduct;
    @SerializedName("estimateInPast")
    @Expose
    private Boolean estimateInPast;
    @SerializedName("estimateInFarPast")
    @Expose
    private Boolean estimateInFarPast;
    @SerializedName("phoneSender")
    @Expose
    private Boolean phoneSender;
    @SerializedName("phoneRecipient")
    @Expose
    private Boolean phoneRecipient;
    @SerializedName("recipientInfoSet")
    @Expose
    private Boolean recipientInfoSet;
    @SerializedName("recipientPhoneSet")
    @Expose
    private Boolean recipientPhoneSet;
    @SerializedName("recipientPhoneRegistration")
    @Expose
    private Boolean recipientPhoneRegistration;
    @SerializedName("references")
    @Expose
    private References references;
    @SerializedName("adWidget")
    @Expose
    private Object adWidget;
    @SerializedName("shipmentSequence")
    @Expose
    private String shipmentSequence;

    public String getShipmentType() {
        return shipmentType;
    }

    public void setShipmentType(String shipmentType) {
        this.shipmentType = shipmentType;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public void setTrackingCode(String trackingCode) {
        this.trackingCode = trackingCode;
    }

    public Object getErrandCode() {
        return errandCode;
    }

    public void setErrandCode(Object errandCode) {
        this.errandCode = errandCode;
    }

    public Object getWaybillnumber() {
        return waybillnumber;
    }

    public void setWaybillnumber(Object waybillnumber) {
        this.waybillnumber = waybillnumber;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(String estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public PickupAddress getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(PickupAddress pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public Object getLastPickupDate() {
        return lastPickupDate;
    }

    public void setLastPickupDate(Object lastPickupDate) {
        this.lastPickupDate = lastPickupDate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<ExtraService> getExtraServices() {
        return extraServices;
    }

    public void setExtraServices(List<ExtraService> extraServices) {
        this.extraServices = extraServices;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public Object getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(Object packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public Object getLoadingMeters() {
        return loadingMeters;
    }

    public void setLoadingMeters(Object loadingMeters) {
        this.loadingMeters = loadingMeters;
    }

    public String getDestinationPostcode() {
        return destinationPostcode;
    }

    public void setDestinationPostcode(String destinationPostcode) {
        this.destinationPostcode = destinationPostcode;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public Object getRecipientSignature() {
        return recipientSignature;
    }

    public void setRecipientSignature(Object recipientSignature) {
        this.recipientSignature = recipientSignature;
    }

    public Object getCodAmount() {
        return codAmount;
    }

    public void setCodAmount(Object codAmount) {
        this.codAmount = codAmount;
    }

    public Object getCodCurrency() {
        return codCurrency;
    }

    public void setCodCurrency(Object codCurrency) {
        this.codCurrency = codCurrency;
    }

    public Object getCodBic() {
        return codBic;
    }

    public void setCodBic(Object codBic) {
        this.codBic = codBic;
    }

    public Object getCodIban() {
        return codIban;
    }

    public void setCodIban(Object codIban) {
        this.codIban = codIban;
    }

    public Object getCodReference() {
        return codReference;
    }

    public void setCodReference(Object codReference) {
        this.codReference = codReference;
    }

    public String getSenderSapLocisticsContractNumber() {
        return senderSapLocisticsContractNumber;
    }

    public void setSenderSapLocisticsContractNumber(String senderSapLocisticsContractNumber) {
        this.senderSapLocisticsContractNumber = senderSapLocisticsContractNumber;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public Object getSender() {
        return sender;
    }

    public void setSender(Object sender) {
        this.sender = sender;
    }

    public Object getLockerCode() {
        return lockerCode;
    }

    public void setLockerCode(Object lockerCode) {
        this.lockerCode = lockerCode;
    }

    public Object getDestinationPostal() {
        return destinationPostal;
    }

    public void setDestinationPostal(Object destinationPostal) {
        this.destinationPostal = destinationPostal;
    }

    public Object getSourcePostal() {
        return sourcePostal;
    }

    public void setSourcePostal(Object sourcePostal) {
        this.sourcePostal = sourcePostal;
    }

    public Object getNameRecipient() {
        return nameRecipient;
    }

    public void setNameRecipient(Object nameRecipient) {
        this.nameRecipient = nameRecipient;
    }

    public Boolean getFlexProduct() {
        return flexProduct;
    }

    public void setFlexProduct(Boolean flexProduct) {
        this.flexProduct = flexProduct;
    }

    public Boolean getEstimateInPast() {
        return estimateInPast;
    }

    public void setEstimateInPast(Boolean estimateInPast) {
        this.estimateInPast = estimateInPast;
    }

    public Boolean getEstimateInFarPast() {
        return estimateInFarPast;
    }

    public void setEstimateInFarPast(Boolean estimateInFarPast) {
        this.estimateInFarPast = estimateInFarPast;
    }

    public Boolean getPhoneSender() {
        return phoneSender;
    }

    public void setPhoneSender(Boolean phoneSender) {
        this.phoneSender = phoneSender;
    }

    public Boolean getPhoneRecipient() {
        return phoneRecipient;
    }

    public void setPhoneRecipient(Boolean phoneRecipient) {
        this.phoneRecipient = phoneRecipient;
    }

    public Boolean getRecipientInfoSet() {
        return recipientInfoSet;
    }

    public void setRecipientInfoSet(Boolean recipientInfoSet) {
        this.recipientInfoSet = recipientInfoSet;
    }

    public Boolean getRecipientPhoneSet() {
        return recipientPhoneSet;
    }

    public void setRecipientPhoneSet(Boolean recipientPhoneSet) {
        this.recipientPhoneSet = recipientPhoneSet;
    }

    public Boolean getRecipientPhoneRegistration() {
        return recipientPhoneRegistration;
    }

    public void setRecipientPhoneRegistration(Boolean recipientPhoneRegistration) {
        this.recipientPhoneRegistration = recipientPhoneRegistration;
    }

    public References getReferences() {
        return references;
    }

    public void setReferences(References references) {
        this.references = references;
    }

    public Object getAdWidget() {
        return adWidget;
    }

    public void setAdWidget(Object adWidget) {
        this.adWidget = adWidget;
    }

    public String getShipmentSequence() {
        return shipmentSequence;
    }

    public void setShipmentSequence(String shipmentSequence) {
        this.shipmentSequence = shipmentSequence;
    }

}
