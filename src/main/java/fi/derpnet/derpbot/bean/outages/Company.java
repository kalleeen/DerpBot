package fi.derpnet.derpbot.bean.outages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Company {

@SerializedName("customers")
@Expose
private Integer customers;
@SerializedName("working")
@Expose
private Boolean working;
@SerializedName("message")
@Expose
private String message;
@SerializedName("alias")
@Expose
private String alias;
@SerializedName("name")
@Expose
private String name;
@SerializedName("outagemap")
@Expose
private String outagemap;
@SerializedName("servicelevel")
@Expose
private Double servicelevel;
@SerializedName("maxday")
@Expose
private Integer maxday;
@SerializedName("total")
@Expose
private Integer total;
@SerializedName("fault")
@Expose
private Integer fault;

public Integer getCustomers() {
return customers;
}

public void setCustomers(Integer customers) {
this.customers = customers;
}

public Boolean getWorking() {
return working;
}

public void setWorking(Boolean working) {
this.working = working;
}

public String getMessage() {
return message;
}

public void setMessage(String message) {
this.message = message;
}

public String getAlias() {
return alias;
}

public void setAlias(String alias) {
this.alias = alias;
}

public String getName() {
return name;
}

public void setName(String name) {
this.name = name;
}

public String getOutagemap() {
return outagemap;
}

public void setOutagemap(String outagemap) {
this.outagemap = outagemap;
}

public Double getServicelevel() {
return servicelevel;
}

public void setServicelevel(Double servicelevel) {
this.servicelevel = servicelevel;
}

public Integer getMaxday() {
return maxday;
}

public void setMaxday(Integer maxday) {
this.maxday = maxday;
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

}