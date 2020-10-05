package com;

import com.controller.PricingController;
import com.mobisale.singleton.*;
import com.mobisale.utils.LogUtil;
import com.mobisale.utils.SqlLiteUtil;
import com.promotions.data.PromotionPopulationMapData;
import com.promotions.manager.PromotionsDataManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.logging.log4j.core.LoggerContext;
// import org.apache.logging.log4j.core.config.Configuration;
// import org.apache.logging.log4j.core.config.LoggerConfig;

@SpringBootApplication
public class DockerApp {
	protected static void setEnv(Map<String, String> newenv) throws Exception {
		try {
			Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
			Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
			theEnvironmentField.setAccessible(true);
			Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
			env.putAll(newenv);
			Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
			theCaseInsensitiveEnvironmentField.setAccessible(true);
			Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
			cienv.putAll(newenv);
		} catch (Exception e) {
			Class[] classes = Collections.class.getDeclaredClasses();
			Map<String, String> env = System.getenv();
			for(Class cl : classes) {
				if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
					Field field = cl.getDeclaredField("m");
					field.setAccessible(true);
					Object obj = field.get(env);
					Map<String, String> map = (Map<String, String>) obj;
					map.clear();
					map.putAll(newenv);
				}
			}
		}
	}

	protected static void SetDevEnv() throws Exception{
		HashMap<String, String> newenv = new HashMap<String, String>();

		//comet
		/*newenv.put("DB_SERVER", "10.0.0.5\\mobi2017");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "wiz");
		newenv.put("PRICING_DB_SQLITE", "comet_pricing.db");
		newenv.put("DB_DB", "Comet_B2B");
		newenv.put("PROVIDER", "comet");
		newenv.put("HAS_PROMOTIONS", "false");
		newenv.put("HAS_BONUSES", "false");
        */

		//monte
		/*newenv.put("DB_SERVER", "10.0.0.5\\mobi2017");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "wiz");
		newenv.put("PRICING_DB_SQLITE", "monte_pricing.db");
		newenv.put("DB_DB", "Montecchio_B2B");
		newenv.put("PROVIDER", "montecchio");
		newenv.put("HAS_PROMOTIONS", "false");
		newenv.put("HAS_BONUSES", "false");
        */
        /*
		//strauss
		newenv.put("DB_SERVER", "10.0.0.5\\b2b2016");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "Mobi1234");
		newenv.put("PRICING_DB_SQLITE", "strauss_pricing.db");
		newenv.put("PROMOTIONS_DB_SQLITE", "strauss_promotions.db");
		newenv.put("DB_DB", "Strauss_B2B");
		newenv.put("PROVIDER", "strauss");
		newenv.put("HAS_PROMOTIONS", "true");
		newenv.put("HAS_BONUSES", "false");
		newenv.put("UNIT_CARTON", "true");
		newenv.put("UNIT_CARTON", "false");
        */


        /*
        //noa
		newenv.put("DB_SERVER", "10.0.0.5\\b2b2016");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "Mobi1234");
		newenv.put("PRICING_DB_SQLITE", "noa_pricing.db");
		newenv.put("DB_DB", "Noa_B2B");
		newenv.put("PROVIDER", "noa");
		newenv.put("HAS_PROMOTIONS", "false");
		newenv.put("HAS_BONUSES", "false");
		newenv.put("UNIT_CARTON", "true");
        */


        /*
		//hcohen
		newenv.put("DB_SERVER", "10.0.0.5\\b2b2016");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "Mobi1234");
		newenv.put("PRICING_DB_SQLITE", "hcohen_pricing.db");
		newenv.put("PROMOTIONS_DB_SQLITE", "hcohen_promotions.db");
		newenv.put("DB_DB", "HCohen_B2B");
		newenv.put("PROVIDER", "hcohen");
		newenv.put("HAS_PROMOTIONS", "true");
		newenv.put("HAS_BONUSES", "true");
		newenv.put("UNIT_CARTON", "true");
         */

        /*
        //tambour
		newenv.put("DB_SERVER", "10.0.0.5\\mobi2019");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "wiz");
		newenv.put("PRICING_DB_SQLITE", "tambour_pricing.db");
		newenv.put("PROMOTIONS_DB_SQLITE", "tambour_promotions.db");
		newenv.put("DB_DB", "Tambour_B2B");
		newenv.put("PROVIDER", "tambour");//noa
		newenv.put("HAS_PROMOTIONS", "true");//false
		newenv.put("HAS_BONUSES", "true");//false
         */

		//taamei asia
        /*
		newenv.put("DB_SERVER", "10.0.0.5\\b2b2016");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "Mobi1234");
		newenv.put("PRICING_DB_SQLITE", "taameiAsia_pricing.db");
		newenv.put("DB_DB", "TaameiAsia_B2B");
		newenv.put("PROVIDER", "taameiAsia");
		newenv.put("HAS_PROMOTIONS", "false");
		newenv.put("HAS_BONUSES", "false");
		newenv.put("UNIT_CARTON", "true");
         */



		//laben

		newenv.put("DB_SERVER", "10.0.0.5\\b2b2016");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "Mobi1234");
		newenv.put("PRICING_DB_SQLITE", "laben_pricing.db");
		newenv.put("DB_DB", "Laben_B2B");
		newenv.put("PROVIDER", "laben");
		newenv.put("HAS_PROMOTIONS", "false");
		newenv.put("HAS_BONUSES", "false");
		newenv.put("UNIT_CARTON", "true");



		newenv.put("VERSION", "DEV");
		newenv.put("SQLITE", "true");
		newenv.put("SQLITE_TABLET", "true");
		newenv.put("PRICING_CACHE", "true");
		setEnv(newenv);
	}


	private static String readAllBytesJava7()
	{
		String content = "";
		try
		{
			content = new String ( Files.readAllBytes( Paths.get("logs/foo.txt") ) );
		}
		catch (IOException e)
		{
			LogUtil.LOG.error("Error in line: "+e.getStackTrace()[0].getLineNumber()+", Error Message:"+e.getMessage() + " 125");
			e.printStackTrace();
		}
		return content;
	}


	public static void main(String[] args) throws Exception {
		//SetDevEnv();
		LogUtil.LOG.info("Start app ***************");
		SpringApplication app = new SpringApplication(DockerApp.class);
		if (System.getenv("VERSION") == "DEV") {
			//app.setDefaultProperties(Collections.singletonMap("server.port", "8800"));
		}
		app.run(args);

		//String s = readAllBytesJava7();
		//LogUtil.LOG.error("Text file contennt"+ s);
		SqlLiteUtil.Init();

		MtnMappingData.getInstance().clearResources();
		MtnMappingData.getInstance().executeQuery();

		ConditionTypesData.getInstance().executeQuery();

		ConditionsAccessData.getInstance().clearResources();
		ConditionsAccessData.getInstance().executeQuery();

		PricingExistsTablesData.getInstance().executeQuery();
		PricingExistsTablesData.getInstance().executeQuerySqlLite();

		PricingSequenceData.getInstance().clearResources();
		PricingSequenceData.getInstance().executeQuery();
		//if (System.getenv("PROVIDER").equalsIgnoreCase("strauss")
		//		&& System.getenv("SQLITE").equalsIgnoreCase("true") && System.getenv("SQLITE_TABLET").equalsIgnoreCase("true")) {
		//	PricingProceduresData.getInstance().updateSubtotalPricingProcedure();
		//}

		if (System.getenv("PRICING_CACHE").equalsIgnoreCase("true"))
		{
			PricingProcessData.getInstance().clearResources();
		}

		PricingProceduresData.getInstance().clearResources();
		if (System.getenv("SQLITE").equalsIgnoreCase("true"))
			PricingProceduresData.getInstance().executeQuery();

		if (System.getenv("HAS_PROMOTIONS").equalsIgnoreCase("true")) {
			PromotionPopulationMapData.getInstance().clearResources();
			PromotionPopulationMapData.getInstance().executeQuery();
			PromotionsDataManager.initStaticResources();
		}
		System.out.println(System.getenv("PROVIDER"));
		System.out.println(System.getenv("DB_DB"));
		if (System.getenv("PROVIDER").equalsIgnoreCase("noa") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=noa");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadNoaPricingDB();
			else
				new SqlLiteUtil().ReadNoaTables();
		}
		if (System.getenv("PROVIDER").equalsIgnoreCase("strauss") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=strauss");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadStraussPricingDB();
			else
			    new SqlLiteUtil().ReadStraussTables();

		}


		if (System.getenv("PROVIDER").equalsIgnoreCase("hcohen") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=hcohen");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadHCohenPricingDB();
		}

		if (System.getenv("PROVIDER").equalsIgnoreCase("taameiAsia") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=taameiAsia");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadTaameiAsiaPricingDB();
		}

		if (System.getenv("PROVIDER").equalsIgnoreCase("laben") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=laben");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadLabenPricingDB();
		}

		if (System.getenv("PROVIDER").equalsIgnoreCase("tambour") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=tambour");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadTambourPricingDB();
		}

		if (System.getenv("PROVIDER").equalsIgnoreCase("comet") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=comet");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadCometPricingDB();
		}

		if (System.getenv("PROVIDER").equalsIgnoreCase("montecchio") && System.getenv("SQLITE").equalsIgnoreCase("true")) {
			LogUtil.LOG.info("PROVIDER=montecchio");
			if (System.getenv("SQLITE_TABLET").equalsIgnoreCase("true"))
				new SqlLiteUtil().ReadMontePricingDB();
		}

		if (System.getenv("SQLITE").equalsIgnoreCase("true") && System.getenv("PRICING_CACHE").equalsIgnoreCase("true")) {
			PricingController controller = new PricingController();
			LogUtil.LOG.info("Building cache ***********************");
			controller.BuildPricingCacheAsync();
		}

		System.out.println("new version 060420");
		LogUtil.LOG.info("Start end ***********************");
		//new SqlLiteUtil().TestCanConnect();
		//
	}


}
