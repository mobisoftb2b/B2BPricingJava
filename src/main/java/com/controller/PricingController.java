package com.controller;

import com.mobisale.data.CustomerCache;
import com.mobisale.data.ItemMigvan;
import com.mobisale.manager.CacheBuilderManager;
import com.mobisale.singleton.*;
import com.mobisale.utils.*;
import com.promotions.data.PromotionPopulationMapData;
import com.promotions.models.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;

import com.promotions.PromotionsAPI;
/*
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
*/

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobisale.data.ItemPricingData;
import com.promotions.data.ItemBonusData;
import com.promotions.data.ItemPricingPromotionsData;
import com.promotions.data.ItemPromotionData;
import com.promotions.manager.PromotionsDataManager;
import org.springframework.web.bind.annotation.*;
//import redis.clients.jedis.Jedis;


import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.time.LocalDate;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.promotions.PromotionsAPI;

import static java.lang.Float.parseFloat;

/*
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
*/
@RestController
//@RequestMapping("docker-java-app2")
public class PricingController {

    private final Lock
            resetDataLock = new ReentrantLock();

    public PricingController() {
    }

    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public String test() {
        System.out.println("called test2");
        return "2";
    }




    @RequestMapping(value = "/testAPI", method = RequestMethod.GET)
    @ResponseBody
    public String GetTest() {

        try {
            int res = PromotionsAPI.test();
            return Integer.toString(res);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @RequestMapping(value = "/ResetAll", method = RequestMethod.GET)
    @ResponseBody
    public String ResetAll() {

        LogUtil.LOG.info("********************Reset all started*************");
        resetDataLock.lock();
        try {

            MtnMappingData.getInstance().clearResources();
            MtnMappingData.getInstance().executeQuery();

            ConditionTypesData.getInstance().clearResources();
            ConditionTypesData.getInstance().executeQuery();

            ConditionsAccessData.getInstance().clearResources();
            ConditionsAccessData.getInstance().executeQuery();

            PricingExistsTablesData.getInstance().clearResources();
            PricingExistsTablesData.getInstance().executeQuery();
            PricingExistsTablesData.getInstance().executeQuerySqlLite();

            PricingSequenceData.getInstance().clearResources();
            PricingSequenceData.getInstance().executeQuery();

            PricingProceduresData.getInstance().clearResources();
             PricingProceduresData.getInstance().executeQuery();

            if (System.getenv("HAS_PROMOTIONS").equalsIgnoreCase("true")) {
                PromotionPopulationMapData.getInstance().clearResources();
                PromotionPopulationMapData.getInstance().executeQuery();
                PromotionsDataManager.initStaticResources();
            }
            if (System.getenv("PRICING_CACHE").equalsIgnoreCase("true"))
            {
                PricingProcessData.getInstance().clearResources();
            }

            System.out.println("reset all init finished");
            LogUtil.LOG.info("********************Reset all finished*************");
        }
        catch (Exception e) {
            LogUtil.LOG.info("********************Reset all error*************");
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        finally {
            resetDataLock.unlock();
        }
        return "reset finished";
    }

    @RequestMapping(value = "/ResetPricing", method = RequestMethod.GET)
    @ResponseBody
    public String ResetPricing() {

        LogUtil.LOG.info("********************Reset pricing started*************");
        resetDataLock.lock();
        try {

            ConditionTypesData.getInstance().clearResources();
            ConditionTypesData.getInstance().executeQuery();

            ConditionsAccessData.getInstance().clearResources();
            ConditionsAccessData.getInstance().executeQuery();

            PricingExistsTablesData.getInstance().clearResources();
            PricingExistsTablesData.getInstance().executeQuery();
            PricingExistsTablesData.getInstance().executeQuerySqlLite();

            PricingSequenceData.getInstance().clearResources();
            PricingSequenceData.getInstance().executeQuery();

            PricingProceduresData.getInstance().clearResources();
            PricingProceduresData.getInstance().executeQuery();

            if (System.getenv("PRICING_CACHE").equalsIgnoreCase("true"))
            {
                PricingProcessData.getInstance().clearResources();
            }

            System.out.println("reset pricing finished");
            LogUtil.LOG.info("********************Reset pricing finished*************");
        }
        catch (Exception e) {
            LogUtil.LOG.info("********************Reset pricing error*************");
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        finally {
            resetDataLock.unlock();
        }
        return "reset finished";
    }

    @RequestMapping(value = "/ResetPromotions", method = RequestMethod.GET)
    @ResponseBody
    public String ResetPromotions() {

        LogUtil.LOG.info("********************Reset promotions started*************");
        resetDataLock.lock();
        try {

            if (System.getenv("HAS_PROMOTIONS").equalsIgnoreCase("true")) {
                PromotionPopulationMapData.getInstance().clearResources();
                PromotionPopulationMapData.getInstance().executeQuery();
                PromotionsDataManager.initStaticResources();
            }

            LogUtil.LOG.info("********************Reset promotions finished*************");
        }
        catch (Exception e) {
            LogUtil.LOG.info("********************Reset promotions error*************");
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        finally {
            resetDataLock.unlock();
        }
        return "reset promotions finished";
    }

    @RequestMapping(value = "/BuildPricingCacheAsync", method = RequestMethod.GET)
    @ResponseBody
    public String BuildPricingCacheAsync(){
        new Thread(() -> {
            BuildPricingCache();
        }).start();
        return "pricing started";
    }

    @RequestMapping(value = "/GetPromotionItems", method = RequestMethod.POST)
    @ResponseBody
    public String GetPromotionItems(@RequestBody String req){
        try{
            LogUtil.LOG.info("********************BuildPricingCacheForUserAsync started*************");
            ESPNumberItemsRequest jsonReq = new ObjectMapper().readValue(req, ESPNumberItemsRequest.class);
            System.out.println(jsonReq);

            ArrayList<String> items = PromotionsDataManager.GetPromotionItemsForESPNumber(jsonReq.ESPNumber);
            System.out.println(items);
            ObjectMapper mapper = new ObjectMapper();
            String jsonResult = mapper.writeValueAsString(items);
            return jsonResult;

            //return  "OK";
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
            return e.getMessage();
        }

    }


    @RequestMapping(value = "/BuildPricingCacheForUserAsync", method = RequestMethod.POST)
    @ResponseBody
    public String BuildPricingCacheForUserAsync(@RequestBody String req){
        try{
            LogUtil.LOG.info("********************BuildPricingCacheForUserAsync started*************");
            PricingCacheUserRequest jsonReq = new ObjectMapper().readValue(req, PricingCacheUserRequest.class);
            CustomerCache cust = new CustomerCache(jsonReq.CustID, jsonReq.CompanyID, jsonReq.Cust_Key);
            new Thread(() -> {
                BuildCacheForOneCustomer(cust);
            }).start();
            return "pricing started";
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
            return "pricing started error";
        }

    }

    @RequestMapping(value = "/ShowCacheForUser", method = RequestMethod.POST)
    @ResponseBody
    public String ShowCacheForUser(@RequestBody String req){
        try{
            ObjectMapper mapper = new ObjectMapper();
            PricingCacheUserRequest jsonReq = new ObjectMapper().readValue(req, PricingCacheUserRequest.class);
            List<ItemPricingShow> itemspricing = PricingProcessData.getInstance().GetAllItemsForCustomer(jsonReq.CustID);
            String jsonResult = mapper.writeValueAsString(itemspricing);
            return jsonResult;
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
            return "pricing started error";
        }
    }


    @RequestMapping(value = "/ShowCacheAll", method = RequestMethod.GET)
    @ResponseBody
    public String ShowCacheAll(){
        try{
            ObjectMapper mapper = new ObjectMapper();
            List<ItemPricingShow> itemspricing = PricingProcessData.getInstance().GetAllItemsForCustomer(null);
            String jsonResult = mapper.writeValueAsString(itemspricing);
            return jsonResult;
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
            return "pricing started error";
        }
    }


    private void BuildCacheForOneCustomer(CustomerCache cust){
        LogUtil.LOG.info("********************BuildCacheForOneCustomer " + cust.Cust_Key + " started*************");
        System.out.println("customer " + cust.Cust_Key);
        CacheBuilderManager cacheBuilderManager = new CacheBuilderManager();
        ArrayList<ItemMigvan> items = cacheBuilderManager.getItemsForCache(cust);
        //ArrayList<ItemMigvan> items = cacheBuilderManager.getItemsCodesMigvan(Cust_Key);
        ArrayList<Item> requestItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ItemMigvan item = items.get(i);
            Item reqItem = new Item();
            reqItem.ItemCode = item.ItemID;
            if (item.ShowUnit)
            {
                reqItem.UnitQuantity = "1";
                reqItem.CartQuantity = "0";
            }
            else
            {
                reqItem.CartQuantity = "1";
                reqItem.UnitQuantity = "0";
            }
            requestItems.add(reqItem);
        }
        List<Item> itemsPriced = getPricingOnly(cust.CustID, cust.Cust_Key, requestItems);
                /*
                String pricingProcedure = PricingProceduresData.getInstance().getPricingProcedureName(Cust_Key);
                ActiveSelectionData.getInstance().UpdateCustomerTablesSelection(customerCode, Cust_Key);

                 */
        //List<Item> itemsPriced = getPricingForPricingProcedure(customerCode, requestItems, pricingProcedure);
        for (int i = 0; i < itemsPriced.size(); i++) {
            Item item = itemsPriced.get(i);
            if (item.Pricing.PriceBruto > 0)
                System.out.println("item " + item.ItemCode + " price neto " + item.Pricing.PriceNeto);
            cust.items.add(item);
        }
        LogUtil.LOG.info("********************BuildCacheForOneCustomer " + cust.Cust_Key + " finished*************");
        //return cust;
    }

    @RequestMapping(value = "/BuildPricingCache", method = RequestMethod.GET)
    @ResponseBody
    public List<CustomerCache> BuildPricingCache(){
        try {
            LogUtil.LOG.info("********************BuildPricingCache started*************");
            //String customerCode = "10091257";
            //String Cust_Key = "1100100010091257";
            CacheBuilderManager cacheBuilderManager = new CacheBuilderManager();
            cacheBuilderManager.forceRepricingOrderSummary();
            ArrayList<CustomerCache> customersCache = cacheBuilderManager.getCustomersForCache();
            for(int j = 0; j < customersCache.size(); j++) {
                CustomerCache cust = customersCache.get(j);
                BuildCacheForOneCustomer(cust);
            }
            LogUtil.LOG.info("********************BuildPricingCache ended************");
            return  customersCache;
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/ReBuildPricingCache", method = RequestMethod.GET)
    @ResponseBody
    public List<CustomerCache> ReBuildPricingCache(){
        try {
            LogUtil.LOG.info("********************ReBuildPricingCache started*************");
            //String customerCode = "10091257";
            //String Cust_Key = "1100100010091257";
            CacheBuilderManager cacheBuilderManager = new CacheBuilderManager();
            cacheBuilderManager.forceRepricingOrderSummary();
            ArrayList<CustomerCache> customersCache = cacheBuilderManager.getCustomersForCache();
            for(int j = 0; j < customersCache.size(); j++) {
                CustomerCache cust = customersCache.get(j);
                BuildCacheForOneCustomer(cust);
            }
            LogUtil.LOG.info("********************ReBuildPricingCache ended*************");
            return  customersCache;
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/GetPricing", method = RequestMethod.POST)
    @ResponseBody
    public String GetPricing(@RequestBody String req) {

        try {

            LogUtil.LOG.error("Pricing request" + req);
            PricingRequest jsonReq = new ObjectMapper().readValue(req, PricingRequest.class);
            if (jsonReq.getpromotions_Cust_Key() == "")
                jsonReq.setpromotions_Cust_Key(jsonReq.getCust_Key());
            LogUtil.LOG.info("customerCode:" + jsonReq.getCustomerCode()+",items: "+ new ObjectMapper().writeValueAsString(jsonReq.getItems()));
            LogUtil.LOG.warn("GetPricing - before getPricingWithPromotions" + LocalDate.now());
            Boolean hasPromotions = false;
            if (System.getenv("HAS_PROMOTIONS") != null)
                hasPromotions = System.getenv("HAS_PROMOTIONS").equals("true");
            LogUtil.LOG.error("hasPromotions=" + hasPromotions.toString());
            if (hasPromotions)
                return getPricingWithPromotions(jsonReq.getCustomerCode(),jsonReq.getCust_Key(),jsonReq.getItems(), jsonReq.getShowPriceLines(), jsonReq.getpromotions_Cust_Key());
            else
                return getPricingNoPromotions(jsonReq.getCustomerCode(),jsonReq.getCust_Key(),jsonReq.getItems(), jsonReq.getShowPriceLines());
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        return "";
    }

    private String getPricingNoPromotions(String customerCode, String Cust_Key, List<com.promotions.models.Item> objectItems, Boolean showPriceLines) {
        String jsonResult = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            LogUtil.LOG.info("getPricingNoPromotions - before getPricing" + LocalDate.now());
            List<Item> itemsDataMap = getPricingOnly(customerCode, Cust_Key, objectItems);
            LogUtil.LOG.info("getPricingNoPromotions - after getPricing" + LocalDate.now());

            List<Item> itemsWithPrice = getPricingAfterNoPromotions(customerCode, Cust_Key, itemsDataMap,  showPriceLines);

            PricingResponse resp = new PricingResponse();
            resp.setItems(itemsWithPrice);
            resp.setCustomerCode(customerCode);
            jsonResult = mapper.writeValueAsString(resp);
        } catch (Exception e) {
            LogUtil.LOG.error(e);
        }

        return jsonResult;
    }

    private String getPricingWithPromotions(String customerCode, String Cust_Key, List<com.promotions.models.Item> objectItems, Boolean showPriceLines, String promotions_Cust_Key) {
        String jsonResult = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            LogUtil.LOG.info("getPricingWithPromotions - before getPricing" + LocalDate.now());
            List<Item> objectItemsWithPrice = getPricingOnly(customerCode, Cust_Key, objectItems);
            HashMap<String, ItemPromotionData> itemsDataMap = getItemsPromotionData(customerCode, Cust_Key, objectItemsWithPrice);
            LogUtil.LOG.info("getPricingWithPromotions - after getPricing" + LocalDate.now());
            itemsDataMap = getPromotions(promotions_Cust_Key, itemsDataMap);
            LogUtil.LOG.info("getPricingWithPromotions - after getPromotions" + LocalDate.now());
            List<Item> itemsWithPrice = getPricingAfterPromotions(customerCode, Cust_Key, objectItemsWithPrice, itemsDataMap, showPriceLines);
            LogUtil.LOG.info("getPricingWithPromotions - after getPricingAfterPromotions" + LocalDate.now());
            HashMap<String, ArrayList<ItemBonusData>> itemBonusDataMap = PromotionsDataManager.getInstance(Cust_Key).getAllItemBonusDataMap();
            LogUtil.LOG.info("getPricingWithPromotions - after getAllItemBonusDataMap" + LocalDate.now());


            PricingResponse resp = new PricingResponse();
            resp.setItems(itemsWithPrice);
            resp.setCustomerCode(customerCode);
            resp.setBonusDataMap(itemBonusDataMap);
            jsonResult = mapper.writeValueAsString(resp);
        } catch (Exception e) {
            LogUtil.LOG.error(e);
        }

        return jsonResult;
    }



     private ItemPromotionData getItemPromotionData(String customerCode, String Cust_Key, Item item)
     {
         ItemPricingPromotionsData itemPricingPromotionsData = new ItemPricingPromotionsData(item.Pricing.PriceBruto, item.Pricing.PriceNeto, item.Pricing.DiscountPercent, item.Pricing.UnitType, item.Pricing.SubTotalsPromotionDiscountValue);
         PromotionsDataManager promotionsDataManagerInstance = PromotionsDataManager.getInstance(Cust_Key != null ? Cust_Key : customerCode);
         ItemPromotionData itemPromotionData = promotionsDataManagerInstance.getOrderUIItem(item.ItemCode);
         //String description = "";
         //if (stepDescription.size() > 0)
            // description = stepDescription.get(0);
         if (itemPromotionData == null) {
             itemPromotionData = new ItemPromotionData(item.ItemCode);
             //itemPromotionData.setStepDetailDescription(description);
             promotionsDataManagerInstance.addItemPromotionData(item.ItemCode, itemPromotionData);
         }


         int unitInCar = 1;
         if (item.UnitInCart != null) {
             try {
                 unitInCar = Integer.parseInt(item.UnitInCart);
             } catch (Exception e) {

             }
         }
         else
         {
             String unitFromItem = ActiveSelectionData.getInstance().getItemValue(item.ItemCode, "UnitInCart");
             if (unitFromItem != null)
                 unitInCar = Integer.parseInt(unitFromItem);
         }

         int unitQuantity = 0;
         if (item.UnitQuantity != null) {
             try {
                 unitQuantity = Integer.parseInt(item.UnitQuantity);
             } catch (Exception e) {

             }
         }
         int cartQuantity = 0;
         if (item.CartQuantity != null) {
             try {
                 cartQuantity = Integer.parseInt(item.CartQuantity);
             } catch (Exception e) {

             }
         }
         if (unitQuantity > 0 && cartQuantity == 0 && unitInCar > 0) {
             cartQuantity = unitQuantity / unitInCar;
             unitQuantity = unitQuantity % unitInCar;
         }

         itemPromotionData.setUnitInKar(unitInCar);
         itemPromotionData.setUnitWeight(1);
         itemPromotionData.setPcQuantity(unitQuantity);
         itemPromotionData.setKarQuantity(cartQuantity);
         itemPromotionData.setItemPricingData(itemPricingPromotionsData);
         itemPromotionData.setItem(item);

         return itemPromotionData;
     }

     private HashMap<String, ItemPromotionData> getItemsPromotionData(String customerCode, String Cust_Key, List<Item> objectItems) {
        HashMap<String, ItemPromotionData> itemsDataMap = new HashMap<>();
        try {
            for (Item item : objectItems) {
                ItemPromotionData itemPromotionData = getItemPromotionData(customerCode, Cust_Key, item);
                itemsDataMap.put(item.ItemCode, itemPromotionData);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return itemsDataMap;
    }

    private void buildQuantity(com.promotions.models.Item item)
    {
        double quantity = 0;
        String unitFromInputData = ActiveSelectionData.getInstance().getItemValue(item.ItemCode, "UnitInCart");
        if (unitFromInputData != null)
            item.UnitInCart = unitFromInputData;
        if (item.Quantity == null || item.Quantity == "") {
            double unitQuantity = 0;
            double cartQuantity = 0;
            if (item.UnitQuantity != null && item.UnitQuantity != "")
                unitQuantity = Double.parseDouble(item.UnitQuantity);
            else
                item.UnitQuantity = "0";
            if (item.CartQuantity != null && item.CartQuantity != "")
                cartQuantity = Double.parseDouble(item.CartQuantity);
            else
                item.CartQuantity = "0";
            if (cartQuantity == 0)
                quantity = unitQuantity;
            else {
                int unitFromItem = 1;
                if (item.UnitInCart != null)
                   unitFromItem = Integer.parseInt(item.UnitInCart);
                quantity = unitQuantity + cartQuantity * unitFromItem;
            }
        }
        item.Quantity = Double.toString(quantity);
    }

    private List<Item> getPricingForPricingProcedure(String custID, List<Item> objectItems, String pricingProcedure) {
        List<Item> itemsWithPrice = new ArrayList<>();
        try{
            for (Item item : objectItems) {
                ActiveSelectionData.getInstance().UpdateItemTablesSelection(item.ItemCode);
                buildQuantity(item);
                com.promotions.models.ItemPricing cachedItemPricing = PricingProcessData.getInstance().GetItemWithPrice(custID, item);
                if (cachedItemPricing != null)
                {
                    item.Pricing = cachedItemPricing;
                }
                else {
                    LogUtil.LOG.info("itemCode" + item.ItemCode);
                    ItemPricingData itemPricingData = new ItemPricingData(pricingProcedure, item.ItemCode, parseFloat(item.Quantity));
                    LogUtil.LOG.info("getPricing - loop before initPricing ItemCode=" + item.ItemCode + " "
                            + LocalDate.now());
                    LogUtil.LOG.info("before initPricing");
                    itemPricingData.initPricing(true);
                    double PriceBruto = itemPricingData.getItemBasePrice();
                    double PriceNeto = itemPricingData.getItemNetoPrice();
                    double DiscountPercent = itemPricingData.getItemDiscountValue();
                    //double TotalLine = itemPricingData.getTotalItemPriceNeto();
                    String UnitType = itemPricingData.getPriceUnitType();
                    double subTotalsPromotionDiscountValue = itemPricingData.getSubTotalsPromotionDiscountValue();

                    LogUtil.LOG.info("PriceBruto: " + PriceBruto + ", DiscountPercent:" + DiscountPercent + ", PriceNeto:" + PriceNeto + ", UnitType:" + UnitType);
                    if (item.Pricing == null)
                        item.Pricing = new ItemPricing();
                    item.Pricing.PriceBruto = PriceBruto;
                    item.Pricing.PriceNeto = PriceNeto;
                    item.Pricing.DiscountPercent = DiscountPercent;
                    item.Pricing.UnitType = UnitType;
                    item.Pricing.SubTotalsPromotionDiscountValue = subTotalsPromotionDiscountValue;
                    item.Pricing.PricingProcedure = itemPricingData.PricingProcedure;
                    item.Pricing.PricingData = itemPricingData;

                    if (System.getenv("PRICING_CACHE").equalsIgnoreCase("true")) {
                        PricingProcessData.getInstance().AddItemWithPrice(custID, item);
                    }
                }
                itemsWithPrice.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemsWithPrice;
    }
    private List<Item> getPricingOnly(String customerCode, String Cust_Key, List<com.promotions.models.Item> objectItems) {
        List<Item> itemsWithPrice = new ArrayList<>();
        try {
            LogUtil.LOG.info("getPricing - before " + LocalDate.now());
            String supplyDate = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
            ActiveSelectionData.getInstance().updateSupplyDateSelection(supplyDate);
            LogUtil.LOG.info("getPricing - after updateSupplyDateSelection " + LocalDate.now());
            ActiveSelectionData.getInstance().UpdateCustomerTablesSelection(customerCode, Cust_Key);
            LogUtil.LOG.info("getPricing - after updateCustomerSelection " + LocalDate.now());
            LogUtil.LOG.info("getPricing - objectItems count=" + objectItems.size() + " "+ LocalDate.now());
            String pricingProcedure = PricingProceduresData.getInstance().getPricingProcedureName(Cust_Key);
            itemsWithPrice = getPricingForPricingProcedure(customerCode, objectItems, pricingProcedure);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemsWithPrice;
    }


    private HashMap<String, ItemPromotionData> getPromotions(String promotions_Cust_Key, HashMap<String, ItemPromotionData> itemsDataMap) throws Exception {
        PromotionsAPI promotionsAPI = new PromotionsAPI();
        promotionsAPI.runPromotionsForCustomer(promotions_Cust_Key, itemsDataMap);
        return itemsDataMap;
    }


    private ArrayList<Item> getPricingAfterPromotions(String customerCode, String Cust_Key, List<Item> objectItemsWithPrice, HashMap<String, ItemPromotionData> itemsDataMap,  Boolean showPriceLines) {
        ArrayList<Item> itemsWithPrice = new ArrayList<>();
        try {
            //String supplyDate = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
            //ActiveSelectionData.getInstance().updateSupplyDateSelection(supplyDate);
            //ActiveSelectionData.getInstance().UpdateCustomerTablesSelection(customerCode, Cust_Key);
            for (Item item : objectItemsWithPrice) {
                ActiveSelectionData.getInstance().UpdateItemTablesSelection(item.ItemCode);
                //LogUtil.LOG.info("getPricingAfterPromotions - before ItemPricingData" + LocalDate.now());
                //ItemPricingData itemPricingData = new ItemPricingData(Cust_Key, item.ItemCode, parseFloat(item.UnitQuantity));
                //itemPricingData.initPricing( true);
                //LogUtil.LOG.error("getPricingAfterPromotions - after initPricing" + LocalDate.now());
                //ItemPromotionData itemPromotionData = getItemPromotionData(item);
                //HashMap<String, ItemPromotionData> itemsDataMap = getItemPromitionData(objectItemsWithPrice);
                ItemPromotionData itemPromotionData = itemsDataMap.get(item.ItemCode);
                LogUtil.LOG.info("getPricingAfterPromotions - before  itemPromotionData" + LocalDate.now());

                if (itemPromotionData != null) {
                    ItemPricingPromotionsData itemPricingPromotionsData = itemPromotionData.getItemPricingData();
                    item.Pricing.PricingData.updatePromotionCondition(itemPricingPromotionsData.getItemDiscountValueWithoutExcludeDiscount(), itemPricingPromotionsData.getPromotionValue(), itemPricingPromotionsData.isExcludeDiscount(), itemPricingPromotionsData.isPromotionClassification(), itemPricingPromotionsData.isNeedToRound());

                    //System.out.println("PriceBruto: " + item.PriceBruto + ", DiscountPercent:" + item.DiscountPercent + ", PriceNeto:" + item.PriceNeto + ", UnitType:" + item.DiscountPercent);
                    if (item.Pricing == null)
                        item.Pricing = new ItemPricing();
                    LogUtil.LOG.info("pricing rounding");
                    Double itemQuantity = Double.parseDouble(item.Quantity);
                    item.Pricing.Quantity = itemQuantity;

                    Holder<Boolean> hasFixedPrice = new Holder<>(false);
                    ArrayList<ItemPricingData.ItemPricingLine> pricingLines = item.Pricing.PricingData.getItemPricingLinesForShow(hasFixedPrice);

                    for (ItemPricingData.ItemPricingLine line : pricingLines){
                        if (line.ConditionReturnCalculateValueBasket > 0)
                            item.Pricing.BasketDiscountPercent = line.ConditionReturnCalculateValueBasket;
                    }

                    if (showPriceLines)
                        item.Pricing.itemPricingLines = pricingLines;
                    item.Pricing.HasFixedPrice = hasFixedPrice.value;

                    if (System.getenv("PROVIDER").equalsIgnoreCase("noa")) {
                        item.Pricing.PriceBruto = item.Pricing.PricingData.getItemBasePrice();
                        //item.Pricing.PriceBruto = NumberUtil.roundDouble(item.Pricing.PricingData.getItemBasePrice(), 2);
                        item.Pricing.PriceBrutoUnit = item.Pricing.PriceBruto;
                        item.Pricing.PriceNeto = item.Pricing.PricingData.getItemNetoPrice();
                        //item.Pricing.PriceNeto = NumberUtil.roundDouble(item.Pricing.PricingData.getItemNetoPrice(), 2);
                        item.Pricing.PriceNetoUnit = item.Pricing.PriceNeto;
                    }
                    else{
                        item.Pricing.PriceBrutoUnit = item.Pricing.PricingData.getItemBasePrice();
                        //item.Pricing.PriceBrutoUnit = NumberUtil.roundDouble(item.Pricing.PricingData.getItemBasePrice(), 2);
                        item.Pricing.PriceBruto = item.Pricing.PricingData.getItemTotalBasePrice(itemQuantity);
                        //item.Pricing.PriceBruto = NumberUtil.roundDouble(item.Pricing.PricingData.getItemTotalBasePrice(itemQuantity), 2);
                        item.Pricing.PriceNetoUnit = item.Pricing.PricingData.getItemNetoPrice();
                        //item.Pricing.PriceNetoUnit = NumberUtil.roundDouble(item.Pricing.PricingData.getItemNetoPrice(), 2);
                        item.Pricing.PriceNeto = item.Pricing.PricingData.RecalculateTotalLine(itemQuantity);
                        //item.Pricing.PriceNeto = NumberUtil.roundDouble(item.Pricing.PricingData.RecalculateTotalLine(itemQuantity), 2);

                        if (item.Pricing.UnitType == "KAR" && item.UnitInCart != null) {
                            Integer unitFromItem = Integer.parseInt(item.UnitInCart);
                            if (unitFromItem > 0) {
                                item.Pricing.PriceBrutoUnit = item.Pricing.PriceBrutoUnit / unitFromItem;
                                item.Pricing.PriceNetoUnit = item.Pricing.PriceNetoUnit / unitFromItem;
                                item.Pricing.PriceBruto = item.Pricing.PriceBruto / unitFromItem;
                                item.Pricing.PriceNeto =  item.Pricing.PriceNeto / unitFromItem;
                            }
                        }

                    }


                    item.Pricing.DiscountPercent = item.Pricing.PricingData.getItemDiscountValue();
                    //item.Pricing.DiscountPercent = NumberUtil.roundDouble(item.Pricing.PricingData.getItemDiscountValue(), 2);
                    item.Pricing.UnitType = item.Pricing.PricingData.getPriceUnitType();
                    item.Pricing.SubTotalsPromotionDiscountValue = item.Pricing.PricingData.getSubTotalsPromotionDiscountValue();
                    //item.Pricing.SubTotalsPromotionDiscountValue = NumberUtil.roundDouble(item.Pricing.PricingData.getSubTotalsPromotionDiscountValue(), 2);
                    //we need to recalculate total because of cache. after cache it's for 1 item or 1 carton
                    item.Pricing.TotalLine = item.Pricing.PricingData.RecalculateTotalLine(itemQuantity);
                    //item.Pricing.TotalLine = NumberUtil.roundDouble(item.Pricing.PricingData.RecalculateTotalLine(itemQuantity), 2);

                    if (item.Promotion == null)
                        item.Promotion = new ItemPromotion();
                    item.Promotion.EspNumber =  itemPromotionData.getEspNumber();
                    //item.Promotion.PromotionDescription = itemPromotionData.getStepDetailDescription(); //itemPromotionData.getEspDescription();
                    item.Promotion.PromotionDescription = itemPromotionData.getStepDescriptionStr(); //itemPromotionData.getEspDescription();
                    item.Promotion.PromotionDetails =  itemPromotionData.getStepDescriptions();
                    item.Promotion.NextStepQuantity = itemPromotionData.getNextStepQuantity();
                    item.Promotion.PartFromDeal =  itemPromotionData.isPartFromDeal();
                    item.Promotion.PromotionValue = itemPricingPromotionsData.getPromotionValue();
                    //item.Promotion.PromotionValue = NumberUtil.roundDouble(itemPricingPromotionsData.getPromotionValue(), 2);

                }
                LogUtil.LOG.info("getPricingAfterPromotions - after  itemPromotionData" + LocalDate.now());
                itemsWithPrice.add(item);
            }

            LogUtil.LOG.error("getPricingAfterPromotions - exit" + LocalDate.now());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return itemsWithPrice;
    }


    private ArrayList<Item> getPricingAfterNoPromotions(String customerCode, String Cust_Key, List<Item> objectItemsWithPrice, Boolean showPriceLines) {
        ArrayList<Item> itemsWithPrice = new ArrayList<>();
        try {
            for (Item item : objectItemsWithPrice) {
                ActiveSelectionData.getInstance().UpdateItemTablesSelection(item.ItemCode);

                //System.out.println("PriceBruto: " + item.PriceBruto + ", DiscountPercent:" + item.DiscountPercent + ", PriceNeto:" + item.PriceNeto + ", UnitType:" + item.DiscountPercent);
                if (item.Pricing == null)
                    item.Pricing = new ItemPricing();
                LogUtil.LOG.info("pricing rounding");
                Double itemQuantity = Double.parseDouble(item.Quantity);
                item.Pricing.Quantity = itemQuantity;

                Holder<Boolean> hasFixedPrice = new Holder<>(false);
                ArrayList<ItemPricingData.ItemPricingLine> pricingLines = item.Pricing.PricingData.getItemPricingLinesForShow(hasFixedPrice);

                for (ItemPricingData.ItemPricingLine line : pricingLines) {
                    if (line.ConditionReturnCalculateValueBasket > 0)
                        item.Pricing.BasketDiscountPercent = line.ConditionReturnCalculateValueBasket;
                }

                if (showPriceLines)
                    item.Pricing.itemPricingLines = pricingLines;
                item.Pricing.HasFixedPrice = hasFixedPrice.value;

                if (System.getenv("PROVIDER").equalsIgnoreCase("noa")) {
                    item.Pricing.PriceBruto = item.Pricing.PricingData.getItemBasePrice();
                    //item.Pricing.PriceBruto = NumberUtil.roundDouble(item.Pricing.PricingData.getItemBasePrice(), 2);
                    item.Pricing.PriceBrutoUnit = item.Pricing.PriceBruto;
                    item.Pricing.PriceNeto = item.Pricing.PricingData.getItemNetoPrice();
                    //item.Pricing.PriceNeto = NumberUtil.roundDouble(item.Pricing.PricingData.getItemNetoPrice(), 2);
                    item.Pricing.PriceNetoUnit = item.Pricing.PriceNeto;
                } else {
                    item.Pricing.PriceBrutoUnit = item.Pricing.PricingData.getItemBasePrice();
                    //item.Pricing.PriceBrutoUnit = NumberUtil.roundDouble(item.Pricing.PricingData.getItemBasePrice(), 2);
                    item.Pricing.PriceBruto = item.Pricing.PricingData.getItemTotalBasePrice(itemQuantity);
                    //item.Pricing.PriceBruto = NumberUtil.roundDouble(item.Pricing.PricingData.getItemTotalBasePrice(itemQuantity), 2);
                    item.Pricing.PriceNetoUnit = item.Pricing.PricingData.getItemNetoPrice();
                    //item.Pricing.PriceNetoUnit = NumberUtil.roundDouble(item.Pricing.PricingData.getItemNetoPrice(), 2);
                    item.Pricing.PriceNeto = item.Pricing.PricingData.RecalculateTotalLine(itemQuantity);
                    //item.Pricing.PriceNeto = NumberUtil.roundDouble(item.Pricing.PricingData.RecalculateTotalLine(itemQuantity), 2);

                    if (item.Pricing.UnitType == "KAR" && item.UnitInCart != null) {
                        Integer unitFromItem = Integer.parseInt(item.UnitInCart);
                        if (unitFromItem > 0) {
                            item.Pricing.PriceBrutoUnit = item.Pricing.PriceBrutoUnit / unitFromItem;
                            item.Pricing.PriceNetoUnit = item.Pricing.PriceNetoUnit / unitFromItem;
                            item.Pricing.PriceBruto = item.Pricing.PriceBruto / unitFromItem;
                            item.Pricing.PriceNeto = item.Pricing.PriceNeto / unitFromItem;
                        }
                    }

                }


                item.Pricing.DiscountPercent = item.Pricing.DiscountPercent;//PricingData.getItemDiscountValue();
                //item.Pricing.DiscountPercent = NumberUtil.roundDouble(item.Pricing.PricingData.getItemDiscountValue(), 2);
                item.Pricing.UnitType = item.Pricing.PricingData.getPriceUnitType();
                item.Pricing.SubTotalsPromotionDiscountValue = item.Pricing.SubTotalsPromotionDiscountValue;//PricingData.getSubTotalsPromotionDiscountValue();
                //item.Pricing.SubTotalsPromotionDiscountValue = NumberUtil.roundDouble(item.Pricing.PricingData.getSubTotalsPromotionDiscountValue(), 2);
                //we need to recalculate total because of cache. after cache it's for 1 item or 1 carton
                item.Pricing.TotalLine = item.Pricing.PricingData.RecalculateTotalLine(itemQuantity);
                //item.Pricing.TotalLine = NumberUtil.roundDouble(item.Pricing.PricingData.RecalculateTotalLine(itemQuantity), 2);

                LogUtil.LOG.info("getPricingAfterNoPromotions - after  itemPromotionData" + LocalDate.now());
                 itemsWithPrice.add(item);
        }
            LogUtil.LOG.error("getPricingAfterNoPromotions - exit" + LocalDate.now());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return itemsWithPrice;
    }
}
