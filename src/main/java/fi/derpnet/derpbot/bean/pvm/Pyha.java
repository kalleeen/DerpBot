package fi.derpnet.derpbot.bean.pvm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pyha {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("alternate_names")
    @Expose
    private String alternateNames;
    @SerializedName("flag_day")
    @Expose
    private Integer flagDay;
    @SerializedName("age")
    @Expose
    private Integer age;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAlternateNames() {
        return alternateNames;
    }

    public void setAlternateNames(String alternateNames) {
        this.alternateNames = alternateNames;
    }

    public Integer getFlagDay() {
        return flagDay;
    }

    public void setFlagDay(Integer flagDay) {
        this.flagDay = flagDay;
    }

    public Integer getAge() {
        return age;
    }
}
