package com.mtn.mobisale.data;

import java.util.ArrayList;

/**
 * Created by israel on 3/16/14.
 */
public class PricingProcedureListData {
    public final String Procedure;
    public ArrayList<PricingProcedureData> pricingProcedureDatas = new ArrayList<PricingProcedureData>();

    public PricingProcedureListData(String procedure) {
        Procedure = procedure;
    }

    public void addPricingProcedureData(PricingProcedureData pricingProcedureData) {
        pricingProcedureDatas.add(pricingProcedureData);
    }

}
