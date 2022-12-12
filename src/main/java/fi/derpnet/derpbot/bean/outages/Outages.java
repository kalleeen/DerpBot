package fi.derpnet.derpbot.bean.outages;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Outages {

@SerializedName("timestamp")
@Expose
private Long timestamp;
@SerializedName("companies")
@Expose
private List<Company> companies = null;
@SerializedName("areas")
@Expose
private List<Area> areas = null;

public Long getTimestamp() {
return timestamp;
}

public void setTimestamp(Long timestamp) {
this.timestamp = timestamp;
}

public List<Company> getCompanies() {
return companies;
}

public void setCompanies(List<Company> companies) {
this.companies = companies;
}

public List<Area> getAreas() {
return areas;
}

public void setAreas(List<Area> areas) {
this.areas = areas;
}

}