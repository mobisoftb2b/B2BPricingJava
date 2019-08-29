package com.mtn.mobisale.data;

/**
 * Created by israel on 3/16/14.
 */
public class ConditionData {
    public final String ConditionField;
    public final String DocStruct;
    public final String ActiveField;

    public ConditionData(String conditionField, String docStruct, String activeField) {
        ConditionField = conditionField;
        DocStruct = docStruct;
        ActiveField = activeField;
    }
}
