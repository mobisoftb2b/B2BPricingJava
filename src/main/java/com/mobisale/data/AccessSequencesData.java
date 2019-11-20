package com.mobisale.data;

import java.util.TreeMap;

/**
 * Created by israel on 3/16/14.
 */
public class AccessSequencesData {
    public static final String SEQUENCE_KSCHL = "KSCHL";
    public static final String LISTING_PREFIX_QUERY_STRING = "SELECT " + SEQUENCE_KSCHL  + " FROM " + "KOTG_ALL" + " WHERE ";
    public final String AccessSequence;
    public TreeMap<Integer, AccessSequenceData> accessSequenceDataTreeMap = new TreeMap<Integer, AccessSequenceData>();

    public AccessSequencesData(String accessSequence) {
        AccessSequence = accessSequence;
    }

    public void addAccessSequencesData(AccessSequenceData accessSequenceData) {
        accessSequenceDataTreeMap.put(accessSequenceData.Access, accessSequenceData);
    }
}
