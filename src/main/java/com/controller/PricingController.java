package com.controller;

import com.mobisale.singleton.*;
import com.mobisale.utils.SqlLiteUtil;
import com.promotions.data.PromotionPopulationMapData;
import com.promotions.models.*;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
import com.mobisale.utils.DbUtil;
import com.promotions.data.ItemBonusData;
import com.promotions.data.ItemPricingPromotionsData;
import com.promotions.data.ItemPromotionData;
import com.promotions.manager.PromotionsDataManager;
import org.springframework.web.bind.annotation.*;
//import redis.clients.jedis.Jedis;
import com.mobisale.utils.LogUtil;


import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.time.LocalDate;
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

    public PricingController() {
    }

    @RequestMapping(value = "/test2", method = RequestMethod.GET)
    public String test() {
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

        try {
            MtnMappingData.getInstance().clearResources();
            MtnMappingData.getInstance().executeQuery();

            ConditionTypesData.getInstance().clearResources();
            ConditionTypesData.getInstance().executeQuery();

            ConditionsAccessData.getInstance().clearResources();
            ConditionsAccessData.getInstance().executeQuery();

            PricingExistsTablesData.getInstance().clearResources();
            PricingExistsTablesData.getInstance().executeQuery();

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
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        return "reset finished";
    }

    @RequestMapping(value = "/GetPricing", method = RequestMethod.POST)
    @ResponseBody
    public String GetPricing(@RequestBody String req) {

        try {

            PricingRequest jsonReq = new ObjectMapper().readValue(req, PricingRequest.class);
            LogUtil.LOG.info("customerCode:" + jsonReq.getCustomerCode()+",items: "+ new ObjectMapper().writeValueAsString(jsonReq.getItems()));
            LogUtil.LOG.warn("GetPricing - before getPricingWithPromotions" + LocalDate.now());
            Boolean hasPromotions = false;
            if (System.getenv("HAS_PROMOTIONS") != null)
                hasPromotions = System.getenv("HAS_PROMOTIONS").equals("true");
            LogUtil.LOG.error("hasPromotions=" + hasPromotions.toString());
            if (hasPromotions)
                return getPricingWithPromotions(jsonReq.getCustomerCode(),jsonReq.getCust_Key(),jsonReq.getItems());
            else
                return getPricingNoPromotions(jsonReq.getCustomerCode(),jsonReq.getCust_Key(),jsonReq.getItems());
        }
        catch (Exception e) {
            LogUtil.LOG.error(e);
            e.printStackTrace();
        }
        return "";
    }

    private String getPricingNoPromotions(String customerCode, String Cust_Key, List<com.promotions.models.Item> objectItems) {
        String jsonResult = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            LogUtil.LOG.info("getPricingNoPromotions - before getPricing" + LocalDate.now());
            List<Item> itemsDataMap = getPricingOnly(customerCode, Cust_Key, objectItems);
            LogUtil.LOG.info("getPricingNoPromotions - after getPricing" + LocalDate.now());

            PricingResponse resp = new PricingResponse();
            resp.setItems(itemsDataMap);
            resp.setCustomerCode(customerCode);
            jsonResult = mapper.writeValueAsString(resp);
        } catch (Exception e) {
            LogUtil.LOG.error(e);
        }

        return jsonResult;
    }

    private String getPricingWithPromotions(String customerCode, String Cust_Key, List<com.promotions.models.Item> objectItems) {
        String jsonResult = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            LogUtil.LOG.info("getPricingWithPromotions - before getPricing" + LocalDate.now());
            List<Item> objectItemsWithPrice = getPricingOnly(customerCode, Cust_Key, objectItems);
            HashMap<String, ItemPromotionData> itemsDataMap = getItemsPromotionData(customerCode, Cust_Key, objectItemsWithPrice);
            LogUtil.LOG.info("getPricingWithPromotions - after getPricing" + LocalDate.now());
            itemsDataMap = getPromotions(Cust_Key, itemsDataMap);
            LogUtil.LOG.info("getPricingWithPromotions - after getPromotions" + LocalDate.now());
            List<Item> itemsWithPrice = getPricingAfterPromotions(customerCode, Cust_Key, objectItemsWithPrice, itemsDataMap);
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
        int quantity = 0;
        if (item.Quantity == null || item.Quantity == "") {
            int unitQuantity = 0;
            int cartQuantity = 0;
            if (item.UnitQuantity != null && item.UnitQuantity != "")
                unitQuantity = Integer.parseInt(item.UnitQuantity);
            if (item.CartQuantity != null && item.CartQuantity != "")
                cartQuantity = Integer.parseInt(item.CartQuantity);
            if (cartQuantity == 0)
                quantity = unitQuantity;
            else {
                int unitFromItem = 1;
                String unitFromInputData = ActiveSelectionData.getInstance().getItemValue(item.ItemCode, "UnitInCart");
                if (unitFromInputData != null)
                   unitFromItem = Integer.parseInt(unitFromInputData);
                quantity = unitQuantity + cartQuantity * unitFromItem;
            }
        }
        item.Quantity = Integer.toString(quantity);
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
            for (Item item : objectItems) {
                String pricingProcedure = PricingProceduresData.getInstance().getPricingProcedureName(Cust_Key);
                buildQuantity(item);
                com.promotions.models.ItemPricing cachedItemPricing = PricingProcessData.getInstance().GetItemWithPrice(pricingProcedure, item);
                if (cachedItemPricing != null)
                {
                    item.Pricing = cachedItemPricing;
                }
                else {
                    LogUtil.LOG.info("itemCode" + item.ItemCode);
                    ActiveSelectionData.getInstance().UpdateItemTablesSelection(item.ItemCode);
                    ItemPricingData itemPricingData = new ItemPricingData(pricingProcedure, item.ItemCode, parseFloat(item.Quantity));
                    LogUtil.LOG.info("getPricing - loop before initPricing ItemCode=" + item.ItemCode + " "
                            + LocalDate.now());
                    LogUtil.LOG.info("before initPricing");
                    itemPricingData.initPricing(true);
                    double PriceBruto = itemPricingData.getItemBasePrice();
                    double PriceNeto = itemPricingData.getItemNetoPrice();
                    double DiscountPercent = itemPricingData.getItemDiscountValue();
                    double TotalLine = itemPricingData.getTotalItemPriceNeto();
                    String UnitType = itemPricingData.getPriceUnitType();
                    double subTotalsPromotionDiscountValue = itemPricingData.getSubTotalsPromotionDiscountValue();

                    LogUtil.LOG.info("PriceBruto: " + PriceBruto + ", DiscountPercent:" + DiscountPercent + ", PriceNeto:" + PriceNeto + ", UnitType:" + DiscountPercent);
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
                        PricingProcessData.getInstance().AddItemWithPrice(item);
                    }
                }
                itemsWithPrice.add(item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemsWithPrice;
    }


    private HashMap<String, ItemPromotionData> getPromotions(String Cust_Key, HashMap<String, ItemPromotionData> itemsDataMap) throws Exception {
        PromotionsAPI promotionsAPI = new PromotionsAPI();
        promotionsAPI.runPromotionsForCustomer(Cust_Key, itemsDataMap);
        return itemsDataMap;
    }

    private ArrayList<Item> getPricingAfterPromotions(String customerCode, String Cust_Key, List<Item> objectItemsWithPrice, HashMap<String, ItemPromotionData> itemsDataMap) {
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
                    item.Pricing.PriceBruto = item.Pricing.PricingData.getItemBasePrice();
                    item.Pricing.PriceNeto = item.Pricing.PricingData.getItemNetoPrice();
                    item.Pricing.DiscountPercent = item.Pricing.PricingData.getItemDiscountValue();
                    item.Pricing.UnitType = item.Pricing.PricingData.getPriceUnitType();
                    item.Pricing.SubTotalsPromotionDiscountValue = item.Pricing.PricingData.getSubTotalsPromotionDiscountValue();
                    item.Pricing.TotalLine = item.Pricing.PricingData.getTotalItemPriceNeto();

                    if (item.Promotion == null)
                        item.Promotion = new ItemPromotion();
                    item.Promotion.EspNumber =  itemPromotionData.getEspNumber();
                    item.Promotion.PromotionDescription = itemPromotionData.getStepDetailDescription(); //itemPromotionData.getEspDescription();
                    item.Promotion.PartFromDeal =  itemPromotionData.isPartFromDeal();
                    item.Promotion.PromotionValue = itemPricingPromotionsData.getPromotionValue();

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
}
