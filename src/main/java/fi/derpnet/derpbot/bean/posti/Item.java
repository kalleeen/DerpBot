
package fi.derpnet.derpbot.bean.posti;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {

    @SerializedName("trackingNumbers")
    @Expose
    private List<String> trackingNumbers = null;
    @SerializedName("packages")
    @Expose
    private Object packages;
    @SerializedName("packageQuantity")
    @Expose
    private Object packageQuantity;
    @SerializedName("grossWeight")
    @Expose
    private Object grossWeight;
    @SerializedName("netWeight")
    @Expose
    private Object netWeight;
    @SerializedName("volume")
    @Expose
    private Object volume;
    @SerializedName("loadingMeter")
    @Expose
    private Object loadingMeter;
    @SerializedName("length")
    @Expose
    private Object length;
    @SerializedName("width")
    @Expose
    private Object width;
    @SerializedName("height")
    @Expose
    private Object height;
    @SerializedName("product")
    @Expose
    private Object product;
    @SerializedName("productName")
    @Expose
    private Object productName;
    @SerializedName("services")
    @Expose
    private Object services;
    @SerializedName("events")
    @Expose
    private Object events;
    @SerializedName("phase")
    @Expose
    private Object phase;

    public List<String> getTrackingNumbers() {
        return trackingNumbers;
    }

    public void setTrackingNumbers(List<String> trackingNumbers) {
        this.trackingNumbers = trackingNumbers;
    }

    public Object getPackages() {
        return packages;
    }

    public void setPackages(Object packages) {
        this.packages = packages;
    }

    public Object getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(Object packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public Object getGrossWeight() {
        return grossWeight;
    }

    public void setGrossWeight(Object grossWeight) {
        this.grossWeight = grossWeight;
    }

    public Object getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(Object netWeight) {
        this.netWeight = netWeight;
    }

    public Object getVolume() {
        return volume;
    }

    public void setVolume(Object volume) {
        this.volume = volume;
    }

    public Object getLoadingMeter() {
        return loadingMeter;
    }

    public void setLoadingMeter(Object loadingMeter) {
        this.loadingMeter = loadingMeter;
    }

    public Object getLength() {
        return length;
    }

    public void setLength(Object length) {
        this.length = length;
    }

    public Object getWidth() {
        return width;
    }

    public void setWidth(Object width) {
        this.width = width;
    }

    public Object getHeight() {
        return height;
    }

    public void setHeight(Object height) {
        this.height = height;
    }

    public Object getProduct() {
        return product;
    }

    public void setProduct(Object product) {
        this.product = product;
    }

    public Object getProductName() {
        return productName;
    }

    public void setProductName(Object productName) {
        this.productName = productName;
    }

    public Object getServices() {
        return services;
    }

    public void setServices(Object services) {
        this.services = services;
    }

    public Object getEvents() {
        return events;
    }

    public void setEvents(Object events) {
        this.events = events;
    }

    public Object getPhase() {
        return phase;
    }

    public void setPhase(Object phase) {
        this.phase = phase;
    }

}
