package fi.derpnet.derpbot.bean.reittiopas;

public class Location {

    private double[] coordinates;
    private String description;
    private String code;

    public double[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[] coordinates) {
        this.coordinates = coordinates;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String startCode) {
        this.code = startCode;
    }
}
