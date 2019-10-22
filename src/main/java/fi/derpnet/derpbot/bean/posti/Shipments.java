
package fi.derpnet.derpbot.bean.posti;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Shipments {

    @SerializedName("shipments")
    @Expose
    private List<Shipment> shipments = null;

    public List<Shipment> getShipments() {
        return shipments;
    }

    public void setShipments(List<Shipment> shipments) {
        this.shipments = shipments;
    }

}
