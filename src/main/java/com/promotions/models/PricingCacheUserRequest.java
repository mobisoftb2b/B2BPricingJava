package com.promotions.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PricingCacheUserRequest {
    public String CustID;
    @JsonIgnore
    public Integer CompanyID;
    @JsonIgnore
    public String Cust_Key;
}
