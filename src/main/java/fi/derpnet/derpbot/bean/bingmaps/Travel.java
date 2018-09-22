/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.derpnet.derpbot.bean.bingmaps;

/**
 *
 * @author kalle
 */
public class Travel {
    
    private double travelDistance;
    private double travelDuration;

    /**
     * @return the travelDistance
     */
    public double getTravelDistance() {
        return travelDistance;
    }

    /**
     * @param travelDistance the travelDistance to set
     */
    public void setTravelDistance(double travelDistance) {
        this.travelDistance = travelDistance;
    }

    /**
     * @return the travelDuration
     */
    public double getTravelDuration() {
        return travelDuration;
    }

    /**
     * @param travelDuration the travelDuration to set
     */
    public void setTravelDuration(double travelDuration) {
        this.travelDuration = travelDuration;
    }
    
}
