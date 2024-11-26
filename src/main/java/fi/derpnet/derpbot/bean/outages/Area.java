package fi.derpnet.derpbot.bean.outages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Area {

@SerializedName("name")
@Expose
private String name;
@SerializedName("alias")
@Expose
private String alias;
@SerializedName("total")
@Expose
private Integer total;
@SerializedName("fault")
@Expose
private Integer fault;
@SerializedName("maxday")
@Expose
private Integer maxday;

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public String getAlias() {
return alias;
}

public void setAlias(String alias) {
this.alias = alias;
}

public Integer getTotal() {
return total;
}

public void setTotal(Integer total) {
this.total = total;
}

public Integer getFault() {
return fault;
}

public void setFault(Integer fault) {
this.fault = fault;
}

public Integer getMaxday() {
return maxday;
}

public void setMaxday(Integer maxday) {
this.maxday = maxday;
}

}