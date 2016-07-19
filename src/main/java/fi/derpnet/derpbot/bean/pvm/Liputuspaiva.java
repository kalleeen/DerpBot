package fi.derpnet.derpbot.bean.pvm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Liputuspaiva {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("flag_day")
    @Expose
    private Integer flagDay;
    @SerializedName("age")
    @Expose
    private Integer age;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("alternate_names")
    @Expose
    private String alternateNames;

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

    public Integer getFlagDay() {
        return flagDay;
    }

    public void setFlagDay(Integer flagDay) {
        this.flagDay = flagDay;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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
}
