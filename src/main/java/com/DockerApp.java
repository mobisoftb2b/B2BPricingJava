package com;

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
		newenv.put("DB_SERVER", "10.0.0.44\\MTNMSSQLSERVER");
		newenv.put("DB_PORT", "1433");
		newenv.put("DB_USER", "sa");
		newenv.put("DB_PASSWORD", "master2w");
		newenv.put("PRICING_DB_SQLITE", "strauss_pricing.db");
		newenv.put("PROMOTIONS_DB_SQLITE", "strauss_promotions.db");
		//newenv.put("PRICING_DB_SQLITE", "pricing.db");
		newenv.put("DB_DB", "Strauss_B2B"); //Noa_B2B
		//newenv.put("DB_DB", "Noa_B2B"); //Noa_B2B
		newenv.put("PROVIDER", "strauss");//noa
		//newenv.put("PROVIDER", "noa");
		newenv.put("VERSION", "DEV");
		newenv.put("HAS_PROMOTIONS", "true");//false
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
		PricingProceduresData.getInstance().executeQuery();

		if (System.getenv("HAS_PROMOTIONS").equalsIgnoreCase("true")) {
			PromotionPopulationMapData.getInstance().clearResources();
			PromotionPopulationMapData.getInstance().executeQuery();
			PromotionsDataManager.initStaticResources();
		}
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
		System.out.println("init finished");
		//new SqlLiteUtil().TestCanConnect();
		//
	}


}
