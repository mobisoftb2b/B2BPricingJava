package com.mobisale.data;

public class PricingProcedureUpdateData {
    public int Id;
    public String Procedure;
    public  int StepNumber;
    public  String NewSubtotal;

    public PricingProcedureUpdateData(int id, String procedure, int stepNumber, String newSubtotal) {
        Id = id;
        Procedure = procedure;
        StepNumber = stepNumber;
        NewSubtotal = newSubtotal;
    }

}
