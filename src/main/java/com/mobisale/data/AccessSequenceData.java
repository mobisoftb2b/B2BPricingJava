package com.mobisale.data;

import com.mobisale.singleton.*;

import java.util.ArrayList;

/**
 * Created by israel on 3/16/14.
 */
public class AccessSequenceData {
    public static final int ACCESS_SEQUENCE_TYPE_PRICING = 0;
    public static final int ACCESS_SEQUENCE_TYPE_LISTING = 1;
    public static final String SEQUENCE_SAP_CONSTANT = "KNUMH";
    public static final String SEQUENCE_VALUE_TYPE = "KRECH";
    public static final String SEQUENCE_VALUE = "KBETR";
    public static final String SEQUENCE_DISCOUNT_FROM = "GKWRT";
    public static final String SEQUENCE_DISCOUNT_TO = "MXWRT";
    public static final String SEQUENCE_CREDIT_TERMS = "ZTERM";
    public static final String SEQUENCE_UNIT_TYPE = "KMEIN";
    public static final String SEQUENCE_PRICE_UNIT = "PriceUnit";
    public static final String SEQUENCE_DIVIDE_VALUE = "KPEIN";
    private final String SEQUENCE_DATE_FROM = "DATAB";
    private final String SEQUENCE_DATE_TO = "DATBI";
    public int ConditionType;

    private String SEQUENCE_QUERY_STRING = " ";
    public final String AccessSequence;
    public final int Access;
    public final String TableName;
    public final boolean ExclusiveAccess;
    public final int Requirement;
    private final int type;
    public String query = "";
    public String[] selectionArgs;
    private ArrayList<ConditionData> conditionDatas = new ArrayList<ConditionData>();

    public AccessSequenceData(int type, String accessSequence, int access, String tableName, boolean exclusiveAccess, int requirement) {
        AccessSequence = accessSequence;
        Access = access;
        TableName = tableName;
        ExclusiveAccess = exclusiveAccess;
        Requirement = requirement;
        this.type = type;
        switch (type){
            case ACCESS_SEQUENCE_TYPE_PRICING:
                ConditionType = -1;
                ConditionTypesData.ConditionTypeData conditionTypeData = ConditionTypesData.getInstance().getConditionType(accessSequence);
                if (conditionTypeData != null) {
                    ConditionType = conditionTypeData.ConditionType;
                }
                SEQUENCE_QUERY_STRING = "SELECT * FROM " + TableName + " WHERE ";
                break;
            case ACCESS_SEQUENCE_TYPE_LISTING:
                SEQUENCE_QUERY_STRING = "";
                break;
        }
    }

    public void addConditionData(ConditionData conditionData) {
        conditionDatas.add(conditionData);
    }

    public void buildSequenceData() {
        selectionArgs = new String[conditionDatas.size() + 2];
        int i = 0;
        for (ConditionData conditionData : conditionDatas) {
            selectionArgs[i + 1] = MtnMappingData.getInstance().getMtnField(conditionData.ActiveField);
            if (i == conditionDatas.size() - 1) {
                query = query + conditionData.ConditionField + "=?";
            } else {
                query = query + conditionData.ConditionField + "=?" + " AND ";
            }
            i++;
        }
        selectionArgs[i + 1] = ActiveSelectionData.ACTIVE_SELECTION_SUPPLY_DATE;
        selectionArgs[0] = AccessSequence;
        switch (type){
            case ACCESS_SEQUENCE_TYPE_PRICING:
                if(query.isEmpty()){
                    query = SEQUENCE_QUERY_STRING + "KSCHL=?"  + " AND " + "?" + " BETWEEN " + SEQUENCE_DATE_FROM + " AND " + SEQUENCE_DATE_TO + " ORDER BY " + SEQUENCE_DATE_FROM + " DESC";
                }else{
                    query = SEQUENCE_QUERY_STRING + "KSCHL=?"  + " AND " + query  + " AND " + "?" + " BETWEEN " + SEQUENCE_DATE_FROM + " AND " + SEQUENCE_DATE_TO + " ORDER BY " + SEQUENCE_DATE_FROM + " DESC";
                }
                break;
            case ACCESS_SEQUENCE_TYPE_LISTING:
                if(query.isEmpty()){
                    query = SEQUENCE_QUERY_STRING + "KSCHL=?"  + " AND " + "?" + " BETWEEN " + SEQUENCE_DATE_FROM + " AND " + SEQUENCE_DATE_TO;
                }else {
                    query = SEQUENCE_QUERY_STRING + "KSCHL=?"  + " AND " + query  + " AND " + "?" + " BETWEEN " + SEQUENCE_DATE_FROM + " AND " + SEQUENCE_DATE_TO;
                }
                break;
        }
    }
}
