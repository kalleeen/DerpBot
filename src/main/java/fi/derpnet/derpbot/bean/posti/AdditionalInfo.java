
package fi.derpnet.derpbot.bean.posti;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdditionalInfo {

    @SerializedName("fi")
    @Expose
    private Object fi;
    @SerializedName("en")
    @Expose
    private Object en;
    @SerializedName("sv")
    @Expose
    private Object sv;
    @SerializedName("et")
    @Expose
    private Object et;
    @SerializedName("lt")
    @Expose
    private Object lt;
    @SerializedName("lv")
    @Expose
    private Object lv;
    @SerializedName("ru")
    @Expose
    private Object ru;

    public Object getFi() {
        return fi;
    }

    public void setFi(Object fi) {
        this.fi = fi;
    }

    public Object getEn() {
        return en;
    }

    public void setEn(Object en) {
        this.en = en;
    }

    public Object getSv() {
        return sv;
    }

    public void setSv(Object sv) {
        this.sv = sv;
    }

    public Object getEt() {
        return et;
    }

    public void setEt(Object et) {
        this.et = et;
    }

    public Object getLt() {
        return lt;
    }

    public void setLt(Object lt) {
        this.lt = lt;
    }

    public Object getLv() {
        return lv;
    }

    public void setLv(Object lv) {
        this.lv = lv;
    }

    public Object getRu() {
        return ru;
    }

    public void setRu(Object ru) {
        this.ru = ru;
    }

}
