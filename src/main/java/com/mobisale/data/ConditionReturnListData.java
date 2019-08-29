package com.mtn.mobisale.data;

import java.util.ArrayList;

/**
 * Created by israel on 3/16/14.
 */
public class ConditionReturnListData {
    public ArrayList<ConditionReturnData> conditionReturnDatas = new ArrayList<ConditionReturnData>();

    public ConditionReturnListData() {
    }

    public void addConditionReturnData(ConditionReturnData conditionReturnData) {
        conditionReturnDatas.add(conditionReturnData);
    }
}
