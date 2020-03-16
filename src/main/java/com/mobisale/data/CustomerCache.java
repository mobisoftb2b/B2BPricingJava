package com.mobisale.data;

import com.promotions.models.Item;

import java.util.ArrayList;

public class CustomerCache {
    public String CustID;
    public Integer CompanyID;
    public String Cust_Key;

    public ArrayList<Item> items = new ArrayList<>();

    public CustomerCache(){}

    public CustomerCache(String _CustID, Integer _CompanyID, String _Cust_Key){
        CustID = _CustID;
        CompanyID = _CompanyID;
        Cust_Key = _Cust_Key;
    }
}
