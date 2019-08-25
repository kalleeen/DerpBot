package fi.derpnet.derpbot.bean.passpredictor;

public class Pass {

    private String satellite;
    private Long begin;
    private Long end;
    private Integer maxElev;
    private Long frequency;
    private Long bandwidth;

    public String getSatellite() {
        return satellite;
    }

    public void setSatellite(String satellite) {
        this.satellite = satellite;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Integer getMaxElev() {
        return maxElev;
    }

    public void setMaxElev(Integer maxElev) {
        this.maxElev = maxElev;
    }

    public Long getFrequency() {
        return frequency;
    }

    public void setFrequency(Long frequency) {
        this.frequency = frequency;
    }

    public Long getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Long bandwidth) {
        this.bandwidth = bandwidth;
    }
}
