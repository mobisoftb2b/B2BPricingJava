package com.promotions.manager;


import com.mobisale.constants.Tables;
import com.mobisale.singleton.ActiveSelectionData;
import com.mobisale.utils.SqlLiteUtil;
import com.promotions.data.*;
import com.promotions.database.PromotionsContract;
import com.promotions.database.PromotionsDatabase;
import com.promotions.interfaecs.IOrderObserver;
import com.mobisale.utils.DbUtil;
import com.mobisale.utils.LogUtil;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: israel
 * Date: 2/13/13
 * Time: 1:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class PromotionsDataManager {

    //private static final String TAG = "PromotionsDataManager";
    private static final String ITEM_ID = "ItemCode";
    //private static final String TABLE_ITEMS = "Items";

    private static HashMap<String, PromotionsDataManager> instances = new HashMap<>();
    private String m_custKey;

    private static ArrayList<String> allItemsCode = new ArrayList<>();
    private HashSet<String> dealKeys = new HashSet<>();
    private static TreeMap<String, PromotionHeader> dealsHeaderByDealKeyMap = new TreeMap<>();
    private static ArrayList<PromotionHeader> promotionHeaders = new ArrayList<>();
    private ArrayList<PromotionHeader> currentPromotionHeaders = new ArrayList<>();
    private IOrderObserver observer;
    private HashMap<String, ItemPromotionData> itemsDataMap = new HashMap<>();
    private HashMap<String, ArrayList<ItemBonusData>> itemBonusDataMap = new HashMap<>();
    //private HashMap<String, Boolean> dataExistMap = new HashMap<>();
    private static SqlLiteUtil sqlLiteUtil = new SqlLiteUtil();

    private static HashMap<String, ArrayList<String>> itemsSqlQueryMap = new HashMap<>();


    public static PromotionsDataManager getInstance(String customerKey) {
        PromotionsDataManager instance = instances.get(customerKey);
        if (instance == null){
            instance = new PromotionsDataManager();
            instances.put(customerKey, instance);
        }
        // Return the instance
        return instance;
    }

    private PromotionsDataManager() {

    }



   public void setObserver(IOrderObserver observer) {
        this.observer = observer;
    }

    // do this when starting singleton
    public static void clearResources() {
        if (dealsHeaderByDealKeyMap != null) {
            dealsHeaderByDealKeyMap.clear();
        }
        if (promotionHeaders != null) {
            promotionHeaders.clear();
        }

        if (allItemsCode != null)
            allItemsCode.clear();

        if (itemsSqlQueryMap != null)
            itemsSqlQueryMap.clear();
        //if (observer != null) {
        //    observer = null;
        //}
    }


    public static void initStaticResources() {
        // clean all at start
        clearResources();
        try {
            //observer = _observer;

            //setAllItems();
            queryForDealHeader();

            Set<String> keySet = ActiveSelectionData.getInstance().getAllItemCodes();
            for (String itemCode: keySet) {
                allItemsCode.add(itemCode);
            }
        /*allItemsCode = getColumnValues(ITEM_ID);

            /*queryForDeals();
            removeDealsWithNoItems();
            removeStepRecordsWithNoItems();
           */

        } catch (Exception e) {
            // TODO: 2019-07-29 add log
        }
    }


    public void startQueryForCustomer(String customerKey) {
        // clean all at start
        //clearResources();
        try {
            //observer = _observer;

            if (m_custKey == null) {
                m_custKey = customerKey;
                setAllItems();
            }

            /*queryForDeals();
            removeDealsWithNoItems();
            removeStepRecordsWithNoItems();
           */

        } catch (Exception e) {
            // TODO: 2019-07-29 add log
        }
    }




    public void queryForDealKeys(String customerKey) {

        dealKeys.clear();
        m_custKey = customerKey;
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;

        String rawQuery = "SELECT distinct " + PromotionsContract.PromotionCustomers.PROMOTION_CUSTOMERS_ESP_NUMBER +
                " FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_CUSTOMERS + " WHERE " + PromotionsContract.PromotionCustomers.PROMOTION_CUSTOMERS_CUST_KEY + " IN " + "(" + "'" + m_custKey + "'" + "," + "'0'" + ")";
        //SQLiteDatabase sqLiteDatabase = new PromotionsDatabase(MTNApplication.getContext()).getReadableDatabase();
        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.ConnectPromotions();
            else
                conn = DbUtil.connect(conn);

            st = conn.createStatement();
            LogUtil.LOG.error(rawQuery);
            rs = st.executeQuery(rawQuery);
            if (rs == null) {
                return;
            }
            int columnIndex;
            while (rs.next()) {
                columnIndex = rs.findColumn(PromotionsContract.PromotionCustomers.PROMOTION_CUSTOMERS_ESP_NUMBER);
                String dealCode = rs.getString(columnIndex);
                dealKeys.add(dealCode);
            }
        } catch (Exception e) {
            LogUtil.LOG.error("error query for promotion-keys :"+e.getMessage());
        } finally {
            if (sqlLiteUtil.IsSQlLite())
                sqlLiteUtil.Disconnect(conn);
            else
               DbUtil.CloseConnection(conn,rs,st);
        }
    }

    private static void queryForDealHeader() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String supplyDate = sdf.format(Calendar.getInstance().getTimeInMillis());

        String selection = " WHERE " + PromotionsContract.PromotionHeader.PROMOTION_HEADER_START_DATE + " <= " + supplyDate + " AND " + PromotionsContract.PromotionHeader.PROMOTION_HEADER_END_DATE + " >= " + supplyDate + " ORDER BY " + PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_PRIORITY + " ASC " + "," + PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_NUMBER + " DESC";
        //ESPNumber='20140027' AND "/

        String rawQuery = "SELECT * FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_HEADER + selection;
        //SQLiteDatabase db = new PromotionsDatabase(MTNApplication.getContext()).getReadableDatabase();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.ConnectPromotions();
            else
                conn = DbUtil.connect(conn);

            st = conn.createStatement();
            LogUtil.LOG.error(rawQuery);
            rs = st.executeQuery(rawQuery);
            if (rs == null) {
                return;
            }


            int columnIndex = 0;
            int newPriority = 0;
            while (rs.next()) {
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_NUMBER);
                String ESPNumber = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_TYPE);
                String ESPType = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_START_DATE);
                String StartDate = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_END_DATE);
                String EndDate = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_DEFINITION_METHOD);
                int DefinitionMethod = rs.getInt(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_EXCLUDE_DISCOUNT);
                String ExcludeDiscount = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_STEPS_BASED_UOM);
                String StepsBasedUOM = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_STEPS_BASED_CUR);
                String StepsBasedCur = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_NO_BLOCK_ESP);
                String NoBlockESP = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_PROMOTION_IN_FOCUS);
                String PromoInFocus = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_PRIORITY);
                int ESPPriority = rs.getInt(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_STATUS);
                int ESPStatus = rs.getInt(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_BASKET_ESP);
                String BasketESP = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_BONUS_ESP);
                String BonusESP = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_DESCRIPTION);
                String ESPDescription = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_RETURN_RELEVANT);
                String ESPReturnRelevant = rs.getString(columnIndex);
                //
                columnIndex = rs.findColumn(PromotionsContract.PromotionHeader.PROMOTION_HEADER_ESP_CLASSIFICATION);
                String IsClassification = rs.getString(columnIndex);
                //
                PromotionHeader promotionHeader = new PromotionHeader(ESPNumber, ESPType, StartDate, EndDate, DefinitionMethod, !(ExcludeDiscount == null || ExcludeDiscount.isEmpty() || ExcludeDiscount.equalsIgnoreCase("null")),
                        (StepsBasedUOM == null || StepsBasedUOM.isEmpty() ? ItemPromotionData.PC_UNIT : StepsBasedUOM), StepsBasedCur, !(NoBlockESP == null || NoBlockESP.isEmpty() || NoBlockESP.equalsIgnoreCase("null")), !(PromoInFocus == null || PromoInFocus.isEmpty() || PromoInFocus.equalsIgnoreCase("null")),
                        ESPPriority, ESPStatus, !(BasketESP == null || BasketESP.isEmpty() || BasketESP.equalsIgnoreCase("null")), !(BonusESP == null || BonusESP.isEmpty() || BonusESP.equalsIgnoreCase("null")), ESPDescription, !(ESPReturnRelevant == null || ESPReturnRelevant.isEmpty() || ESPReturnRelevant.equalsIgnoreCase("null")), newPriority++, IsClassification != null && IsClassification.equalsIgnoreCase("9"));
                promotionHeaders.add(promotionHeader);
                dealsHeaderByDealKeyMap.put(ESPNumber, promotionHeader);
            }

        } catch (Exception e) {
            LogUtil.LOG.error("error for query in promotion header array :"+e.getMessage());
        } finally {
            if (sqlLiteUtil.IsSQlLite())
                sqlLiteUtil.Disconnect(conn);
            else
                DbUtil.CloseConnection(conn,rs,st);
        }

    }
    //dealKeys.add(dealCode);

    public Boolean queryForDealsByCustomer(String Cust_Key, HashMap<String, ItemPromotionData> itemsDataMapArg) {
        //if (dataExistMap.get(Cust_Key) != null)
        //    return false;
        //SQLiteDatabase db = new PromotionsDatabase(MTNApplication.getContext()).getReadableDatabase();
        currentPromotionHeaders.clear();
        itemsDataMap = itemsDataMapArg;
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.ConnectPromotions();
            else
                conn = DbUtil.connect(conn);

            st = conn.createStatement();
            HashMap<String, String> itemCodesSql = new HashMap<>();
            if (itemsDataMap.size() == 0)
                return true;

            Set<Integer> itemsMapKeys = PromotionPopulationMapData.getInstance().GetPromotionMappingItems().keySet();
            for (Integer itemKey : itemsMapKeys)
            {
                ArrayList<String> mappingItems = PromotionPopulationMapData.getInstance().GetPromotionMappingItems().get(itemKey);
                for(String mappingItemField : mappingItems)
                {
                    itemCodesSql.put(mappingItemField, "SelectedCharacteristics=" + itemKey + " AND " + PromotionsContract.PromotionItems.PROMOTION_ITEMS_MATERIAL_CHAR_VALUE + " IN (");
                }

            }

            for (String itemCode : itemsDataMap.keySet())
            {
                ItemPromotionData item = itemsDataMap.get(itemCode);
                for (String itemCodeSql: itemCodesSql.keySet()){
                    String itemCodeStr = itemCodesSql.get(itemCodeSql);
                    if (itemCodeSql == "ItemID")
                        itemCodeStr += "'" + itemCode + "', ";
                    else {
                        String targetValue = ActiveSelectionData.getInstance().itemsAllDataMap.get(itemCode).get(itemCodeSql);
                        if (targetValue != null && targetValue.trim() != "")
                            itemCodeStr += "'" + targetValue + "', ";
                        else
                            itemCodeStr += "'', ";
                    }
                    itemCodesSql.put(itemCodeSql, itemCodeStr);
                }
            }
            String allWhereIn = "";
            for (String itemCodeSql: itemCodesSql.keySet()) {
                String subQuery = itemCodesSql.get(itemCodeSql);
                allWhereIn += " (" + subQuery.substring(0, subQuery.length()-2) + ")) OR";
            }
            allWhereIn = allWhereIn.substring(0, allWhereIn.length()-3);

            Iterator<String> i=dealKeys.iterator();
            while(i.hasNext())
            {
                String dealKey = i.next();
                //for (PromotionHeader dealHeader : promotionHeaders) {
                PromotionHeader dealHeader = dealsHeaderByDealKeyMap.get(dealKey);
                if (dealHeader == null)
                    return true;
                dealHeader.promotionStepManager.ClearSteps();
                int columnIndex;
                ArrayList<String> excludeItems = new ArrayList<String>();
                HashMap<Integer, PromotionPopulationItem> promotionItemHashMap = new HashMap<Integer, PromotionPopulationItem>();
                HashMap<Integer, StepRecordNumber> stepRecordNumberHashMap = new HashMap<Integer, StepRecordNumber>();
                //String dealKey = dealHeader.ESPNumber;
                //query for items
                String promotionsItemsColumns = PromotionsContract.PromotionItems.PROMOTION_ITEMS_ESP_NUMBER + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_NUMBER + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_SEQUENCE + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_INCLUDE_EXCLUDE_SIGN + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_SELECTED_CHARACTERISTICS + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_MATERIAL_CHAR_VALUE + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_VAR_PROD + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_TOTAL_QTY + ", "  +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_MANDATORY + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_UOM_FOR_MIN_QTY + ", " +
                                                PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_TOTAL_VAL;
                String rawQuery = "SELECT distinct " + promotionsItemsColumns
                        + " FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_ITEMS
                        + " WHERE " + PromotionsContract.PromotionItems.PROMOTION_ITEMS_ESP_NUMBER + "=" + "'" + dealKey + "'"
                        + " AND (" + allWhereIn + ") "
                        //+  " AND " + PromotionsContract.PromotionItems.PROMOTION_ITEMS_MATERIAL_CHAR_VALUE + " IN (" + itemCodesSql + ")
                        + " ORDER BY " + PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_NUMBER + " ASC";
                LogUtil.LOG.error(rawQuery);
                rs = st.executeQuery(rawQuery);
                if (rs == null) {
                    return true;
                }
                int rowcount = 0;
                while (rs.next()) {
                    rowcount ++;
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_ESP_NUMBER);
                    String ESPNumber = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_NUMBER);
                    int RecordNumber = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_SEQUENCE);
                    int RecordSequence = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_INCLUDE_EXCLUDE_SIGN);
                    String IncludeExcludeSign = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_SELECTED_CHARACTERISTICS);
                    int SelectedCharacteristics = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MATERIAL_CHAR_VALUE);
                    String MaterialCharValue = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_VAR_PROD);
                    int MinVarProd = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_TOTAL_QTY);
                    int MinTotQty = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MANDATORY);
                    String Mandatory = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_UOM_FOR_MIN_QTY);
                    String UOMForMinQty = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_TOTAL_VAL);
                    int MinTotVal = rs.getInt(columnIndex);
                    //
                    if (IncludeExcludeSign != null && IncludeExcludeSign.equalsIgnoreCase("E")) {
                        ArrayList<String> itemCodes = PromotionPopulationMapData.getInstance().getItems(SelectedCharacteristics, MaterialCharValue, itemsDataMapArg.keySet());
                        excludeItems.addAll(itemCodes);
                    } else {
                        PromotionPopulationItem promotionPopulationItem = new PromotionPopulationItem(Cust_Key, ESPNumber, RecordNumber, RecordSequence, SelectedCharacteristics, MaterialCharValue, MinVarProd, MinTotQty, !(Mandatory == null || Mandatory.isEmpty() || Mandatory.equalsIgnoreCase("null")), UOMForMinQty, MinTotVal, itemsDataMapArg.keySet());
                        promotionItemHashMap.put(RecordNumber, promotionPopulationItem);
                    }
                }
                if (rowcount == 0)
                    continue;

                for (PromotionPopulationItem promotionPopulationItem : promotionItemHashMap.values()) {
                    promotionPopulationItem.excludeIfNeeded(excludeItems);
                }

                String promotionStepsColumns = PromotionsContract.PromotionSteps.PROMOTION_STEPS_ESP_NUMBER + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_RECORD_NUMBER + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_STEP_ID + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_QTY_BASED_STEP + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_VAL_BASED_STEP + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_TYPE + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_DISCOUNT + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_PRICE_BASED_QTY + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_PRICE_BASED_QTY_UOM + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_PRICE + ", " +
                        PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_PRICE_CURRENCY;

                if (System.getenv("PROVIDER").equalsIgnoreCase("tambour")) {
                    promotionStepsColumns += ", " + PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_DISCOUNT + ", " +
                                             PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_QUANTITY + ", " +
                                             PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_QUANTITY_UOM + ", " +
                                             PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_MULTIPLE_QTY + ", " +
                                             PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_MULTIPLE_QTY_UOM;
                }
                rawQuery = "SELECT distinct " + promotionStepsColumns

                        + " FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_STEPS

                        + " WHERE " + PromotionsContract.PromotionSteps.PROMOTION_STEPS_ESP_NUMBER + "=" + "'" + dealKey + "'" + " ORDER BY " + PromotionsContract.PromotionSteps.PROMOTION_STEPS_RECORD_NUMBER + " ASC";
                LogUtil.LOG.error(rawQuery);
                rs = st.executeQuery(rawQuery);
                if (rs == null) {
                    return true;
                }
                int rowcountSteps = 0;
                while (rs.next()) {
                    rowcountSteps++;
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_ESP_NUMBER);
                    String ESPNumber = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_RECORD_NUMBER);
                    int RecordNumber = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_STEP_ID);
                    int Step = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_QTY_BASED_STEP);
                    int QtyBasedStep = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_VAL_BASED_STEP);
                    int ValBasedStep = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_TYPE);
                    int PromotionType = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_DISCOUNT);
                    double PromotionDiscount = rs.getDouble(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PRICE_BASED_QTY);
                    double PriceBasedQty = rs.getDouble(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PRICE_BASED_QTY_UOM);
                    String PriceBQtyUOM = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_PRICE);
                    double PromotionPrice = rs.getDouble(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_PRICE_CURRENCY);
                    String PromotionPriceCurrency = rs.getString(columnIndex);
                    //
                    APromotionStep promotionStep = null;
                    String stepDescription = "";//rs.getString(PromotionsContract.PromotionSteps.PROMOTION_STEPS_STEP_DESCRIPTION);
                    if (System.getenv("PROVIDER").equalsIgnoreCase("tambour")) {
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_DISCOUNT);
                        double BonusDiscount = rs.getDouble(columnIndex);
                        //

                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_QUANTITY);
                        int BonusQuantity = rs.getInt(columnIndex);
                        //
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_QUANTITY_UOM);
                        String BonusQuantityUOM = rs.getString(columnIndex);
                        //
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_MULTIPLE_QTY);
                        int BonusMultipleQty = rs.getInt(columnIndex);
                        //
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_MULTIPLE_QTY_UOM);
                        String BonusMultQtyUOM = rs.getString(columnIndex);
                        //
                        double BonusPrice = 0;//rs.getFloat(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_PRICE);
                        if (PromotionType == 4 || PromotionType == 5) {
                            promotionStep = new PromotionStepBonus(Cust_Key, ESPNumber, RecordNumber, Step, QtyBasedStep, ValBasedStep, PromotionType, PromotionDiscount, BonusPrice, BonusDiscount, PriceBasedQty, PriceBQtyUOM == null ? ItemPromotionData.PC_UNIT : PriceBQtyUOM, PromotionPrice, PromotionPriceCurrency, BonusQuantity, BonusQuantityUOM, BonusMultipleQty, BonusMultQtyUOM, stepDescription);
                        } else {
                            promotionStep = new PromotionStep(Cust_Key, ESPNumber, RecordNumber, Step, QtyBasedStep, ValBasedStep, PromotionType, PromotionDiscount, PriceBasedQty, PriceBQtyUOM == null ? ItemPromotionData.PC_UNIT : PriceBQtyUOM, PromotionPrice, PromotionPriceCurrency, stepDescription);
                        }
                    }


                    if (PromotionType != 4 &&  PromotionType != 5) {
                        promotionStep = new PromotionStep(Cust_Key, ESPNumber, RecordNumber, Step, QtyBasedStep, ValBasedStep, PromotionType, PromotionDiscount, PriceBasedQty, PriceBQtyUOM == null ? ItemPromotionData.PC_UNIT : PriceBQtyUOM, PromotionPrice, PromotionPriceCurrency, stepDescription);
                    }

                    //
                    StepRecordNumber stepRecordNumber = stepRecordNumberHashMap.get(RecordNumber);
                    if (stepRecordNumber == null) {
                        stepRecordNumber = new StepRecordNumber(Cust_Key, ESPNumber, RecordNumber, dealHeader.DefinitionMethod, dealHeader.StepsBasedUOM);
                        dealHeader.promotionStepManager.stepRecordNumbers.add(stepRecordNumber);
                    }
                    if (dealHeader.DefinitionMethod == 1) {
                        stepRecordNumber.promotionStepTreeMap.put(QtyBasedStep, promotionStep);
                    } else {
                        stepRecordNumber.promotionStepTreeMap.put(ValBasedStep, promotionStep);
                    }
                    if (promotionItemHashMap.get(RecordNumber) != null) {
                        stepRecordNumber.addPromotionPopulationItem(promotionItemHashMap.get(RecordNumber));
                        promotionItemHashMap.remove(RecordNumber);
                    }
                    stepRecordNumberHashMap.put(RecordNumber, stepRecordNumber);

                    HashMap<Integer, PromotionPopulationItem> bonusPromotionItemHashMap = new HashMap<Integer, PromotionPopulationItem>();
                    if (promotionStep.isPromotionStepBonus()) {

                        //query for bonus items
                        rawQuery = "SELECT * FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_ITEMS_BONUSES
                                + " WHERE " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_ESP_NUMBER + "=" + "'" + dealKey + "'" + " AND " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_RECORD_NUMBER + "=" + "'" + RecordNumber + "'" + " AND " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_STEP + "=" + "'" + Step + "'" + " ORDER BY " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_RECORD_NUMBER + " ASC";
                        //LogUtil.LOG.error(rawQuery);
                        rs = st.executeQuery(rawQuery);
                        if (rs == null) {
                            return true;
                        }
                        while (rs.next()) {
                            columnIndex = rs.findColumn(PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_INCLUDE_EXCLUDE_SIGN);
                            String IncludeExcludeSign = rs.getString(columnIndex);
                            //
                            columnIndex = rs.findColumn(PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_SELECTED_CHARACTERISTICS);
                            int SelectedCharacteristics = rs.getInt(columnIndex);
                            //
                            columnIndex = rs.findColumn(PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_MATERIAL_CHAR_VALUE);
                            String MaterialCharValue = rs.getString(columnIndex);
                            //
                            if (IncludeExcludeSign != null && IncludeExcludeSign.equalsIgnoreCase("E")) {
                                ArrayList<String> itemCodes = PromotionPopulationMapData.getInstance().getItems(SelectedCharacteristics, MaterialCharValue, itemsDataMapArg.keySet());
                                excludeItems.addAll(itemCodes);
                            } else {
                                ((PromotionStepBonus)promotionStep).setItems(SelectedCharacteristics, MaterialCharValue, itemsDataMapArg.keySet());
                            }
                        }
                        ((PromotionStepBonus)promotionStep).excludeIfNeeded(excludeItems);

                    }
                }

                if (rowcountSteps == 0)
                    continue;
                currentPromotionHeaders.add(dealHeader);
                for(String itemCode: itemsDataMapArg.keySet())
                {
                    ItemPromotionData item = itemsDataMapArg.get(itemCode);
                    item.setPromotionHeader(dealHeader);
                }

                StepRecordNumber stepRecordNumber = stepRecordNumberHashMap.get(0);
                if (stepRecordNumber != null) {
                    stepRecordNumber.addAllPromotionPopulationItem(new ArrayList<>(promotionItemHashMap.values()));
                }
                Collections.sort(dealHeader.promotionStepManager.stepRecordNumbers, new Comparator<StepRecordNumber>() {
                    @Override
                    public int compare(StepRecordNumber lhs, StepRecordNumber rhs) {
                        return rhs.RecordNumber - lhs.RecordNumber;
                    }
                });
            }
            //dataExistMap.put(Cust_Key, true);
        } catch (Exception e) {
            LogUtil.LOG.error("error in getting deals :"+e.getMessage());
            // TODO: 2019-07-29 add log "error in getting deals"
        } finally {
            if (sqlLiteUtil.IsSQlLite())
                sqlLiteUtil.Disconnect(conn);
            else
                DbUtil.CloseConnection(conn,rs,st);
        }
        return  true;
    }

    /*
    private void queryForDeals() {
        //SQLiteDatabase db = new PromotionsDatabase(MTNApplication.getContext()).getReadableDatabase();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            if (sqlLiteUtil.IsSQlLite() && sqlLiteUtil.IsSQLiteTablet())
                conn = sqlLiteUtil.ConnectPromotions();
            else
            conn = DbUtil.connect(conn);

            st = conn.createStatement();

            for (PromotionHeader dealHeader : promotionHeaders) {
                int columnIndex;
                ArrayList<String> excludeItems = new ArrayList<String>();
                HashMap<Integer, PromotionPopulationItem> promotionItemHashMap = new HashMap<Integer, PromotionPopulationItem>();
                HashMap<Integer, StepRecordNumber> stepRecordNumberHashMap = new HashMap<Integer, StepRecordNumber>();
                String dealKey = dealHeader.ESPNumber;
                //query for items
                String rawQuery = "SELECT * FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_ITEMS
                        + " WHERE " + PromotionsContract.PromotionItems.PROMOTION_ITEMS_ESP_NUMBER + "=" + "'" + dealKey + "'" + " ORDER BY " + PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_NUMBER + " ASC";
                //LogUtil.LOG.error(rawQuery);
                rs = st.executeQuery(rawQuery);
                if (rs == null) {
                    return;
                }
                while (rs.next()) {
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_ESP_NUMBER);
                    String ESPNumber = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_NUMBER);
                    int RecordNumber = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_RECORD_SEQUENCE);
                    int RecordSequence = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_INCLUDE_EXCLUDE_SIGN);
                    String IncludeExcludeSign = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_SELECTED_CHARACTERISTICS);
                    int SelectedCharacteristics = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MATERIAL_CHAR_VALUE);
                    String MaterialCharValue = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_VAR_PROD);
                    int MinVarProd = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_TOTAL_QTY);
                    int MinTotQty = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MANDATORY);
                    String Mandatory = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_UOM_FOR_MIN_QTY);
                    String UOMForMinQty = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionItems.PROMOTION_ITEMS_MIN_TOTAL_VAL);
                    int MinTotVal = rs.getInt(columnIndex);
                    //
                    if (IncludeExcludeSign != null && IncludeExcludeSign.equalsIgnoreCase("E")) {
                        ArrayList<String> itemCodes = PromotionPopulationMapData.getInstance().getItems(SelectedCharacteristics, MaterialCharValue, null);
                        excludeItems.addAll(itemCodes);
                    } else {
                        PromotionPopulationItem promotionPopulationItem = new PromotionPopulationItem(ESPNumber, RecordNumber, RecordSequence, SelectedCharacteristics, MaterialCharValue, MinVarProd, MinTotQty, !(Mandatory == null || Mandatory.isEmpty() || Mandatory.equalsIgnoreCase("null")), UOMForMinQty, MinTotVal, null);
                        promotionItemHashMap.put(RecordNumber, promotionPopulationItem);
                    }
                }

                for (PromotionPopulationItem promotionPopulationItem : promotionItemHashMap.values()) {
                    promotionPopulationItem.excludeIfNeeded(excludeItems);
                }

                //query for steps
                rawQuery = "SELECT * FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_STEPS
                        + " WHERE " + PromotionsContract.PromotionSteps.PROMOTION_STEPS_ESP_NUMBER + "=" + "'" + dealKey + "'" + " ORDER BY " + PromotionsContract.PromotionSteps.PROMOTION_STEPS_RECORD_NUMBER + " ASC";
                //LogUtil.LOG.error(rawQuery);
                rs = st.executeQuery(rawQuery);
                if (rs == null) {
                    return;
                }
                while (rs.next()) {
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_ESP_NUMBER);
                    String ESPNumber = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_RECORD_NUMBER);
                    int RecordNumber = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_STEP_ID);
                    int Step = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_QTY_BASED_STEP);
                    int QtyBasedStep = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_VAL_BASED_STEP);
                    int ValBasedStep = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_TYPE);
                    int PromotionType = rs.getInt(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_DISCOUNT);
                    double PromotionDiscount = rs.getDouble(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PRICE_BASED_QTY);
                    double PriceBasedQty = rs.getDouble(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PRICE_BASED_QTY_UOM);
                    String PriceBQtyUOM = rs.getString(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_PRICE);
                    double PromotionPrice = rs.getDouble(columnIndex);
                    //
                    columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_PROMOTION_PRICE_CURRENCY);
                    String PromotionPriceCurrency = rs.getString(columnIndex);
                    //
                    APromotionStep promotionStep = null;
                    String stepDescription = "";//rs.getString(PromotionsContract.PromotionSteps.PROMOTION_STEPS_STEP_DESCRIPTION);
                    if (System.getenv("PROVIDER").equalsIgnoreCase("tambour")) {
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_DISCOUNT);
                        double BonusDiscount = rs.getDouble(columnIndex);
                        //

                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_QUANTITY);
                        int BonusQuantity = rs.getInt(columnIndex);
                        //
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_QUANTITY_UOM);
                        String BonusQuantityUOM = rs.getString(columnIndex);
                        //
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_MULTIPLE_QTY);
                        int BonusMultipleQty = rs.getInt(columnIndex);
                        //
                        columnIndex = rs.findColumn(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_MULTIPLE_QTY_UOM);
                        String BonusMultQtyUOM = rs.getString(columnIndex);
                        //
                        double BonusPrice = 0;//rs.getFloat(PromotionsContract.PromotionSteps.PROMOTION_STEPS_BONUS_PRICE);
                        if (PromotionType == 4 || PromotionType == 5) {
                            promotionStep = new PromotionStepBonus(ESPNumber, RecordNumber, Step, QtyBasedStep, ValBasedStep, PromotionType, PromotionDiscount, BonusPrice, BonusDiscount, PriceBasedQty, PriceBQtyUOM == null ? ItemPromotionData.PC_UNIT : PriceBQtyUOM, PromotionPrice, PromotionPriceCurrency, BonusQuantity, BonusQuantityUOM, BonusMultipleQty, BonusMultQtyUOM, stepDescription);
                        } else {
                            promotionStep = new PromotionStep(ESPNumber, RecordNumber, Step, QtyBasedStep, ValBasedStep, PromotionType, PromotionDiscount, PriceBasedQty, PriceBQtyUOM == null ? ItemPromotionData.PC_UNIT : PriceBQtyUOM, PromotionPrice, PromotionPriceCurrency, stepDescription);
                        }
                    }

                    if (ESPNumber == "20140035" ||  ESPNumber == "20140028" || ESPNumber == "20140027" || ESPNumber == "20140142" || ESPNumber == "20140028" || ESPNumber == "20140021" || ESPNumber == "20136064")
                        ESPNumber = ESPNumber;

                    if (PromotionType != 4 &&  PromotionType != 5) {
                        promotionStep = new PromotionStep(ESPNumber, RecordNumber, Step, QtyBasedStep, ValBasedStep, PromotionType, PromotionDiscount, PriceBasedQty, PriceBQtyUOM == null ? ItemPromotionData.PC_UNIT : PriceBQtyUOM, PromotionPrice, PromotionPriceCurrency, stepDescription);
                    }

                    //
                    StepRecordNumber stepRecordNumber = stepRecordNumberHashMap.get(RecordNumber);
                    if (stepRecordNumber == null) {
                        stepRecordNumber = new StepRecordNumber(ESPNumber, RecordNumber, dealHeader.DefinitionMethod, dealHeader.StepsBasedUOM);
                        dealHeader.promotionStepManager.stepRecordNumbers.add(stepRecordNumber);
                    }
                    if (dealHeader.DefinitionMethod == 1) {
                        stepRecordNumber.promotionStepTreeMap.put(QtyBasedStep, promotionStep);
                    } else {
                        stepRecordNumber.promotionStepTreeMap.put(ValBasedStep, promotionStep);
                    }
                    if (promotionItemHashMap.get(RecordNumber) != null) {
                        stepRecordNumber.addPromotionPopulationItem(promotionItemHashMap.get(RecordNumber));
                        promotionItemHashMap.remove(RecordNumber);
                    }
                    stepRecordNumberHashMap.put(RecordNumber, stepRecordNumber);

                    HashMap<Integer, PromotionPopulationItem> bonusPromotionItemHashMap = new HashMap<Integer, PromotionPopulationItem>();
                    if (promotionStep.isPromotionStepBonus()) {

                        //query for bonus items
                        rawQuery = "SELECT * FROM " + PromotionsDatabase.Tables.TABLE_PROMOTION_ITEMS_BONUSES
                                + " WHERE " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_ESP_NUMBER + "=" + "'" + dealKey + "'" + " AND " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_RECORD_NUMBER + "=" + "'" + RecordNumber + "'" + " AND " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_STEP + "=" + "'" + Step + "'" + " ORDER BY " + PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_RECORD_NUMBER + " ASC";
                        //LogUtil.LOG.error(rawQuery);
                        rs = st.executeQuery(rawQuery);
                        if (rs == null) {
                            return;
                        }
                        while (rs.next()) {
                            columnIndex = rs.findColumn(PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_INCLUDE_EXCLUDE_SIGN);
                            String IncludeExcludeSign = rs.getString(columnIndex);
                            //
                            columnIndex = rs.findColumn(PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_SELECTED_CHARACTERISTICS);
                            int SelectedCharacteristics = rs.getInt(columnIndex);
                            //
                            columnIndex = rs.findColumn(PromotionsContract.PromotionItemsBonus.PROMOTION_BONUS_MATERIAL_CHAR_VALUE);
                            String MaterialCharValue = rs.getString(columnIndex);
                            //
                            if (IncludeExcludeSign != null && IncludeExcludeSign.equalsIgnoreCase("E")) {
                                ArrayList<String> itemCodes = PromotionPopulationMapData.getInstance().getItems(SelectedCharacteristics, MaterialCharValue, null);
                                excludeItems.addAll(itemCodes);
                            } else {
                                ((PromotionStepBonus)promotionStep).setItems(SelectedCharacteristics, MaterialCharValue, null);
                            }
                        }
                        ((PromotionStepBonus)promotionStep).excludeIfNeeded(excludeItems);

                    }
                }


                StepRecordNumber stepRecordNumber = stepRecordNumberHashMap.get(0);
                if (stepRecordNumber != null) {
                    stepRecordNumber.addAllPromotionPopulationItem(new ArrayList<>(promotionItemHashMap.values()));
                }
                Collections.sort(dealHeader.promotionStepManager.stepRecordNumbers, new Comparator<StepRecordNumber>() {
                    @Override
                    public int compare(StepRecordNumber lhs, StepRecordNumber rhs) {
                        return rhs.RecordNumber - lhs.RecordNumber;
                    }
                });
            }
        } catch (Exception e) {
            LogUtil.LOG.error("error in getting deals :"+e.getMessage());
            // TODO: 2019-07-29 add log "error in getting deals"
        } finally {
            if (sqlLiteUtil.IsSQlLite())
                sqlLiteUtil.Disconnect(conn);
            else
                DbUtil.CloseConnection(conn,rs,st);
        }
    }
    */

    public void setAllItems() {
        itemsDataMap.clear();
        Set<String> keySet = ActiveSelectionData.getInstance().getAllItemCodes();
        for (String itemCode: keySet) {
            ItemPromotionData itemPromotionData = new ItemPromotionData(itemCode);
            itemsDataMap.put(itemCode, itemPromotionData);
        }
        /*allItemsCode = getColumnValues(ITEM_ID);
        for (String itemCode : allItemsCode) {
            ItemPromotionData itemPromotionData = new ItemPromotionData(itemCode);
            itemsDataMap.put(itemCode, itemPromotionData);
        }
        */
    }

    public static  List<String> getAllItemsCode() {
        return allItemsCode;
    }

    private static ArrayList<String> getColumnValues(String columnName) {
        ArrayList<String> itemsCode = new ArrayList<>();
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        try {
            conn = DbUtil.connect(conn);
            st = conn.createStatement();
            String query;
            if (System.getenv("PROVIDER").equalsIgnoreCase("noa")) {
                query = "SELECT " + columnName + " FROM " +  Tables.TABLE_ITEMS;
            }
            else
            {
                query = "SELECT " + columnName + " FROM " +  Tables.TABLE_ITEMS_PRICING;
            }
            rs = st.executeQuery(query);
            if (rs == null) {
                return null;
            }

            while (rs.next()) {
                String columnValue = rs.getString(columnName);
                itemsCode.add(columnValue);
            }
        } catch (Exception e) {
            LogUtil.LOG.error("error 118 add log :"+e.getMessage());
            // TODO: 2019-07-31 add log
        } finally {
            DbUtil.CloseConnection(conn,rs,st);
        }
        return itemsCode;
    }

    public static ArrayList<String> getItemCodesFromField(String itemFieldCode, String whereCode, Set<String> possibleItemIDs) {
        return getItemsCodeWithBindArgs(ITEM_ID, itemFieldCode, whereCode, possibleItemIDs);
    }

    private static ArrayList<String> getItemsCodeWithBindArgs(String columnName, String itemFieldCode, String whereCode, Set<String> possibleItemIDs) {
        ArrayList<String> columnValues;
        ResultSet rs = null;
        Statement st = null;
        Connection conn = null;
        String query;
        String itemIDquery = "";
        if (possibleItemIDs != null && possibleItemIDs.size() > 0) {
                itemIDquery = " AND " + ITEM_ID + " IN (";
                for (String itemID : possibleItemIDs
                ) {
                    itemIDquery += "'" + itemID + "', ";

                }
                itemIDquery = itemIDquery.substring(0, itemIDquery.length()-2) + ")";
        }
        if (System.getenv("PROVIDER").equalsIgnoreCase("noa")) {
                query = "SELECT " + columnName + " FROM " + Tables.TABLE_ITEMS + " WHERE " + itemFieldCode + "=" + "'" + whereCode + "'" + itemIDquery;
        }
        else
        {
                query = "SELECT " + columnName + " FROM " + Tables.TABLE_ITEMS_PRICING + " WHERE " + itemFieldCode + "=" + "'" + whereCode + "'" + itemIDquery;

        }
        columnValues = itemsSqlQueryMap.get(query);
        if (columnValues == null) {
              try {
                    columnValues = new ArrayList<>();
                    conn = DbUtil.connect(conn);
                    st = conn.createStatement();

                    LogUtil.LOG.error(query);

                    rs = st.executeQuery(query);
                    if (rs == null) {
                        return null;
                    }
                    while (rs.next()) {
                        String columnValue = rs.getString(columnName);
                        columnValues.add(columnValue);
                    }
                    itemsSqlQueryMap.put(query, columnValues);
              }
              catch (Exception e) {
                    LogUtil.LOG.error("Error :"+e.getMessage());
                // TODO: 2019-07-31 add log
              }
              finally {
                    DbUtil.CloseConnection(conn,rs,st);
              }
        }

        return columnValues;
    }

    public void removeDealsWithNoItems() {
        ArrayList<PromotionHeader> needToRemove = new ArrayList<PromotionHeader>();
        for (PromotionHeader promotionHeader : promotionHeaders) {
            if (!promotionHeader.isHaveItems()) {
                needToRemove.add(promotionHeader);
            }
        }
        promotionHeaders.removeAll(needToRemove);
    }

    public void removeStepRecordsWithNoItems() {
        for (PromotionHeader promotionHeader : promotionHeaders) {
            ArrayList<StepRecordNumber> needToRemove = new ArrayList<StepRecordNumber>();
            for (StepRecordNumber stepRecordNumber : promotionHeader.promotionStepManager.stepRecordNumbers) {
                if (!stepRecordNumber.isHaveItems()) {
                    needToRemove.add(stepRecordNumber);
                }
            }
            promotionHeader.promotionStepManager.stepRecordNumbers.removeAll(needToRemove);
        }
    }


    public IOrderObserver getObserver() {
        return observer;
    }


    public PromotionHeader getPromotionHeader(String itemCode) {
        PromotionHeader activePromotionHeader = null;
        for (PromotionHeader dealHeader : getPromotionsHeader()) {
            boolean isItemExist = dealHeader.isItemExist(itemCode);
            if (isItemExist) {
                activePromotionHeader = dealHeader;
                break;
            }
        }
        return activePromotionHeader;
    }

    public static PromotionHeader getPromotionHeaderByESPNumber(String ESPNumber) {
        return dealsHeaderByDealKeyMap.get(ESPNumber);
    }

    public ArrayList<PromotionHeader> getPromotionsHeader() {
        ArrayList<PromotionHeader> promotionHeadersForCurrentCustomer = new ArrayList<>();
        for (PromotionHeader promotionHeader : currentPromotionHeaders) {
            if (dealKeys.contains(promotionHeader.ESPNumber))
                promotionHeadersForCurrentCustomer.add(promotionHeader);
        }
        return promotionHeadersForCurrentCustomer;
    }

    public void updateItemPricing(String itemCode, int uiIndex, double price, double discount, String unitType) {
        ArrayList<PromotionHeader> headers = getPromotionsHeader();
        for (PromotionHeader promotionHeader : headers) {
            promotionHeader.updateItemPricing(itemCode, uiIndex, price, discount, unitType);
        }
    }

    public void updateDealForItemCode(int position, String itemCode, float newQuantity) {
        PromotionHeader activePromotionHeader = null;
        ArrayList<PromotionHeader> promotionHeaders = getPromotionHeadersForItem(itemCode);
        while (promotionHeaders.size() > 0) {
            activePromotionHeader = promotionHeaders.get(0);
            activePromotionHeader.updatePromotionDiscount(itemCode, newQuantity);
            //here is the problem
            if (activePromotionHeader.getPromotionStatus() < 2) //1 - we found the the rule, 2 -blocked, 3 - not found the rule
                break;
            promotionHeaders.remove(0);
        }
        //if (observer != null) {
            if (activePromotionHeader != null) {
                activePromotionHeader.updateItemsPriceAndDiscount(getItemsDataMap());
                ItemPromotionData itemPromotionData = getOrderUIItem(itemCode);
                if (itemPromotionData != null) {
                    itemPromotionData.setStepDescriptions(activePromotionHeader.getStepDescriptions(itemCode));
                    itemPromotionData.setStepDetailDescription(activePromotionHeader.getSelectedStepDetailDescription(itemCode));
                    itemPromotionData.setNextStepQuantity(activePromotionHeader.getNextStepDiff(itemCode));
                }
            }
            //observer.onDealsUpdate(itemCode, this, activePromotionHeader);
        //}

    }

    public ArrayList<String> getDealStepsDescription(String itemCode) {
        ArrayList<String> stepsDescription = new ArrayList<String>();
        PromotionHeader activePromotionHeader = null;
        ArrayList<PromotionHeader> headers = getPromotionsHeader();
        for (PromotionHeader promotionHeader : headers) {
            boolean isItemExist = promotionHeader.isItemExist(itemCode);
            if (isItemExist) {
                activePromotionHeader = promotionHeader;
                break;
            }
        }
        if (activePromotionHeader != null) {
            stepsDescription = activePromotionHeader.getStepDescriptions(itemCode);
        }
        return stepsDescription;
    }

    /*
    public boolean blockPromotion(String itemCode) {
        boolean isBlocked = false;
        PromotionHeader activePromotionHeader = null;
        ArrayList<PromotionHeader> headers = getPromotionsHeader();
        for (PromotionHeader promotionHeader : headers) {
            boolean isItemExist = promotionHeader.isItemExist(itemCode);
            if (isItemExist) {
                activePromotionHeader = promotionHeader;
                break;
            }
        }
        if (activePromotionHeader != null) {
            isBlocked = activePromotionHeader.blockPromotion(itemCode);
        }
        if (isBlocked) {
            if (observer != null) {
                observer.onDealsUpdate(-100, null,activePromotionHeader);
            }
        }
        return isBlocked;
    }

    public boolean unBlockPromotion(String itemCode) {
        boolean reValue = true;
        PromotionHeader activePromotionHeader = null;
        for (PromotionHeader promotionHeader : getPromotionsHeader()) {
            boolean isItemExist = promotionHeader.isItemExist(itemCode);
            if (isItemExist) {
                activePromotionHeader = promotionHeader;
                break;
            }
        }
        if (activePromotionHeader != null) {
            activePromotionHeader.unBlockPromotion(itemCode);
            if (observer != null) {
                observer.onDealsUpdate(-100,null, activePromotionHeader);
            }
        }
        return reValue;
    }

    public boolean blockAllPromotion(PromotionHeader activePromotionHeader) {
        boolean isBlocked;
        isBlocked = activePromotionHeader.blockAllPromotion();
        if (isBlocked) {
            if (observer != null) {
                observer.onDealsUpdate(-100,null, activePromotionHeader);
            }
        }
        return isBlocked;
    }

    public boolean unBlockAllPromotion(PromotionHeader activePromotionHeader) {
        activePromotionHeader.unBlockAllPromotion();
        if (observer != null) {
            observer.onDealsUpdate(-100,null, activePromotionHeader);
        }
        return true;
    }
    */
    public static long getPromotionUpdateTime(String espNumber, String itemCode) {
        return System.currentTimeMillis();
    }

    public boolean isPromotionIsBlockedForItem(String itemCode) {
        PromotionHeader activePromotionHeader = null;
        for (PromotionHeader promotionHeader : getPromotionsHeader()) {
            boolean isItemExist = promotionHeader.isItemExist(itemCode);
            if (isItemExist) {
                activePromotionHeader = promotionHeader;
                break;
            }
        }
        if (activePromotionHeader != null) {
            return activePromotionHeader.isBlocked;
        }
        return false;
    }

    public boolean isHavePromotionForItem(String itemCode) {
        PromotionHeader activePromotionHeader = null;
        for (PromotionHeader promotionHeader : getPromotionsHeader()) {
            boolean isItemExist = promotionHeader.isItemExist(itemCode);
            if (isItemExist) {
                activePromotionHeader = promotionHeader;
                break;
            }
        }
        if (activePromotionHeader != null) {
            return true;
        }
        return false;
    }

    public int getPromotionHeadersSizeForItem(String itemCode) {
        int size = 0;
        for (PromotionHeader promotionHeader : getPromotionsHeader()) {
            boolean isItemExist = promotionHeader.isItemExist(itemCode);
            if (isItemExist) {
                size++;
            }
        }
        return size;
    }

    public ArrayList<PromotionHeader> getPromotionHeadersForItem(String itemCode) {
        ArrayList<PromotionHeader> promotionHeaders = new ArrayList<>();
        for (PromotionHeader promotionHeader : getPromotionsHeader()) {
            boolean isItemExist = promotionHeader.isItemExist(itemCode);
            if (isItemExist) {
                promotionHeaders.add(promotionHeader);
            }
        }
        return promotionHeaders;
    }

    public void updatePromotionPriorityForItem(ItemPromotionData selectedItemPromotionData, PromotionHeader firstPromotionHeader, PromotionHeader secondPromotionHeader, IOrderObserver promotionHeaderSelectedListener) {
        int priorityCounter = 0;
        for (PromotionHeader promotionHeader : getPromotionsHeader()) {
            priorityCounter++;
            if (promotionHeader.equals(secondPromotionHeader)) {
                priorityCounter--;
                continue;
            }
            if (promotionHeader.equals(firstPromotionHeader)) {
                secondPromotionHeader.newPriority = priorityCounter;
                priorityCounter++;
            }
            promotionHeader.newPriority = priorityCounter;
        }
        Collections.sort(getPromotionsHeader(), new Comparator<PromotionHeader>() {
            @Override
            public int compare(PromotionHeader lhs, PromotionHeader rhs) {
                return lhs.newPriority - rhs.newPriority;
            }
        });

        firstPromotionHeader.updatePromotionDiscount(selectedItemPromotionData.getItemCode(), 0);
        /*if (observer != null) {
            observer.onDealsUpdate(-1,null, firstPromotionHeader);
        }*/
        //if (promotionHeaderSelectedListener != null)
        //    promotionHeaderSelectedListener.onDealsUpdate(-1,null, firstPromotionHeader);

        for (ItemPromotionData itemPromotionData : getItemsDataMap().values()) {
            boolean isItemExist = secondPromotionHeader.isItemExist(itemPromotionData.getItemCode());
            if (isItemExist) {
                if (itemPromotionData.isItemPricingInitialized()) {
                    updateItemPricing(itemPromotionData.getItemCode(), itemPromotionData.getUiIndex(), itemPromotionData.getItemPricingData().getTotalStartValue(), itemPromotionData.getItemPricingData().getTotalDiscountValue(), itemPromotionData.getItemPricingData().getPriceUnitType());
                }
                if (itemPromotionData.isHaveAmount()) {
                    secondPromotionHeader.updatePromotionDiscount(itemPromotionData.getItemCode(), itemPromotionData.getTotalQuantityByUnitType());
                }
            }
        }
        /*if (observer != null) {
            observer.onPriorityUpdate(selectedItemPromotionData, secondPromotionHeader);
        }
        if (promotionHeaderSelectedListener != null)
            promotionHeaderSelectedListener.onPriorityUpdate(selectedItemPromotionData, secondPromotionHeader);

         */
    }


    public void duplicateItem(String itemCode, int quantity) {
        ArrayList<PromotionHeader> promotionHeaders = getPromotionHeadersForItem(itemCode);
        for (PromotionHeader promotionHeader : promotionHeaders) {
            promotionHeader.duplicateItem(itemCode, quantity);
        }
    }

    public void resetPromotions() {
        ArrayList<PromotionHeader> headers = getPromotionsHeader();
        for (PromotionHeader promotionHeader : headers) {
            promotionHeader.resetPromotions();
        }

    }


    public ItemPromotionData getOrderUIItem(String itemCode) {
        return itemsDataMap.get(itemCode);
    }

    public HashMap<String, ItemPromotionData> getItemsDataMap() {
        return itemsDataMap;
    }

    public void addItemPromotionData(String itemCode, ItemPromotionData itemPromotionData) {
        itemsDataMap.put(itemCode, itemPromotionData);
    }

    public void updateBonusData(String itemCode, String ESPNumber, String ESPDescription, int itemBonusPCQuantity, int itemBonusKARQuantity, String bonusQuantityUOM, double itemBonusPercent, long updateTime) {
        ItemBonusData itemBonusData = new ItemBonusData(itemCode, ESPNumber, ESPDescription, itemBonusPCQuantity, itemBonusKARQuantity, bonusQuantityUOM, itemBonusPercent, updateTime);
        ArrayList<ItemBonusData> bonusData = itemBonusDataMap.get(ESPNumber);
        if (bonusData == null) bonusData = new ArrayList<>();
        bonusData.add(itemBonusData);
        itemBonusDataMap.put(ESPNumber, bonusData);
    }

    public void resetBonusData(String ESPNumber) {
        itemBonusDataMap.remove(ESPNumber);
    }

    public void resetAllBonusData() {
        itemBonusDataMap.clear();
    }

    public  HashMap<String, ArrayList<ItemBonusData>> getAllItemBonusDataMap() {
        return itemBonusDataMap;
    }
}