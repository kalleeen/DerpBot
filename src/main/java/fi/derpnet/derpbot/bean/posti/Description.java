
package fi.derpnet.derpbot.bean.posti;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Description {

    @SerializedName("fi")
    @Expose
    private String fi;
    @SerializedName("en")
    @Expose
    private String en;
    @SerializedName("sv")
    @Expose
    private String sv;
    @SerializedName("et")
    @Expose
    private String et;
    @SerializedName("lt")
    @Expose
    private String lt;
    @SerializedName("lv")
    @Expose
    private String lv;
    @SerializedName("ru")
    @Expose
    private Object ru;

    public String getFi() {
        return fi;
    }

    public void setFi(String fi) {
        this.fi = fi;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getSv() {
        return sv;
    }

    public void setSv(String sv) {
        this.sv = sv;
    }

    public String getEt() {
        return et;
    }

    public void setEt(String et) {
        this.et = et;
    }

    public String getLt() {
        return lt;
    }

    public void setLt(String lt) {
        this.lt = lt;
    }

    public String getLv() {
        return lv;
    }

    public void setLv(String lv) {
        this.lv = lv;
    }

    public Object getRu() {
        return ru;
    }

    public void setRu(Object ru) {
        this.ru = ru;
    }

}
