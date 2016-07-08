/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.derpnet.derpbot.bean;

import java.time.LocalDateTime;
import java.util.Date;

/**
 *
 * @author kalle
 */
public class Leg {
    private String mode;
    private String line;
    private Date startTime;
    private Date finishTime;
    private Location startLocation;
    private Location finishLocation;
    private double distance;

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return the line
     */
    public String getLine() {
        return line;
    }

    /**
     * @param line the line to set
     */
    public void setLine(String line) {
        this.line = line;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the finishTime
     */
    public Date getFinishTime() {
        return finishTime;
    }

    /**
     * @param finishTime the finishTime to set
     */
    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    /**
     * @return the startLocation
     */
    public Location getStartLocation() {
        return startLocation;
    }

    /**
     * @param startLocation the startLocation to set
     */
    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    /**
     * @return the finishLocation
     */
    public Location getFinishLocation() {
        return finishLocation;
    }

    /**
     * @param finishLocation the finishLocation to set
     */
    public void setFinishLocation(Location finishLocation) {
        this.finishLocation = finishLocation;
    }

    /**
     * @return the distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @param distance the distance to set
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }
}
