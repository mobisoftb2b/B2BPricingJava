package com.mtn.mobisale.utils;


import java.math.BigDecimal;
import java.text.DecimalFormat;

public class NumberUtil {
    private static DecimalFormat df = new DecimalFormat("#,##0.00");
    private static DecimalFormat dfN = new DecimalFormat("#,##0.0#");
    private static DecimalFormat dfnn = new DecimalFormat("#,##0.#");
    private static DecimalFormat dfCommas = new DecimalFormat("#,###");
    private static DecimalFormat dfNoCommas = new DecimalFormat("###0.00");


    public static String formatNumber(Number number) {
        if (number == null) {
            return "";
        }
        return df.format(number);
    }

    public static String formatStr(String numStr) {
        if (numStr == null) {
            return "";
        }
        Float numFloat = 0F;
        try {
            numFloat = Float.parseFloat(numStr);
        } catch (Exception e) {
            LogUtil.LOG.error("This Will Be Printed On Error1");
        }
        return df.format(numFloat);
    }

    public static String formatCommas(String numStr) {
        if (numStr == null) {
            return "";
        }
        Float numFloat = 0F;
        try {
            numFloat = Float.parseFloat(numStr);
        } catch (Exception e) {
            LogUtil.LOG.error("This Will Be Printed On Error2");
        }
        String format = df.format(numFloat);
        return format.substring(0, format.indexOf("."));

    }

    public static String formatStrWithPercent(String numStr) {
        if (numStr == null) {
            return "";
        }
        Float numFloat = 0F;
        try {
            numFloat = Float.parseFloat(numStr);
        } catch (Exception e) {
            LogUtil.LOG.error("This Will Be Printed On Error3");

        }
        return df.format(numFloat) + "%";
    }

    public static double roundDoublePrecisionByParameter(double unRounded, boolean needToRound) {
        unRounded = (Double.isNaN(unRounded) || Double.isInfinite(unRounded)) ? 0 : unRounded;
        BigDecimal a = new BigDecimal(unRounded + "");
        int round = 3;
        a = a.setScale(round, BigDecimal.ROUND_HALF_UP);
        return a.doubleValue();
    }

    public static double roundDoublePrecision(double unRounded) {
        int round = 4;
        unRounded = Double.isNaN(unRounded) ? 0 : unRounded;
        BigDecimal a = new BigDecimal(unRounded + "");
        a = a.setScale(round, BigDecimal.ROUND_HALF_DOWN);
        return a.doubleValue();
    }

    public static double roundDoublePrecision2(double unRounded) {
        //BigDecimal a = new BigDecimal("12345.0789");
        BigDecimal a = new BigDecimal(unRounded);
        //a = a.divide(new BigDecimal("1"), 2, BigDecimal.ROUND_HALF_UP);
        a = a.setScale(2, BigDecimal.ROUND_HALF_UP);
        //System.out.println("a >> "+a.toPlainString()); //Returns 12345.08
        return a.doubleValue();
    }

    public static float roundFloatPrecision2(float unRounded) {
        unRounded = (Float.isNaN(unRounded) || Float.isInfinite(unRounded)) ? 0 : unRounded;
        //BigDecimal a = new BigDecimal("12345.0789");
        BigDecimal a = new BigDecimal(unRounded);
        //a = a.divide(new BigDecimal("1"), 2, BigDecimal.ROUND_HALF_UP);
        a = a.setScale(2, BigDecimal.ROUND_HALF_UP);
        //System.out.println("a >> "+a.toPlainString()); //Returns 12345.08
        return a.floatValue();
    }

    public static float roundPrecision2(String unRounded) {
        //BigDecimal a = new BigDecimal("12345.0789");
        BigDecimal a = new BigDecimal(unRounded);
        //a = a.divide(new BigDecimal("1"), 2, BigDecimal.ROUND_HALF_UP);
        a = a.setScale(2, BigDecimal.ROUND_HALF_UP);
        //System.out.println("a >> "+a.toPlainString()); //Returns 12345.08
        return a.floatValue();
    }

    public static String formatedRoundPrecision2(String unRounded) {
        float theNum = roundPrecision2(unRounded);
        String retVal = dfN.format(new Float(theNum));
//        MobisaleLog.d("MTN", "retVal = " + retVal);
//        MobisaleLog.d("MTN", "retVal length = " + retVal.length());
//        MobisaleLog.d("MTN", "DecimalPoint position = " + retVal.indexOf('.'));
        int pointPos = retVal.indexOf('.');
        if (retVal.length() - pointPos == 2) {
            retVal += "0";
        }
        return retVal;
    }

    public static String formatedRoundPrecision2WithPercent(String unRounded) {
        return formatedRoundPrecision2(unRounded) + "%";
    }

    public static float roundPrecision1(String unRounded) {
        BigDecimal a = new BigDecimal(unRounded);
        a = a.setScale(1, BigDecimal.ROUND_HALF_UP);
        return a.floatValue();
    }

    public static String formatedRoundPrecision1(String unRounded) {
        String retVal = " ";
        try {
            float theNum = roundPrecision1(unRounded);
            retVal = dfnn.format(new Float(theNum));
            int pointPos = retVal.indexOf('.');
            if (retVal.length() - pointPos == 1) {
                retVal += "0";
            }
            return retVal;
        } catch (NumberFormatException e) {
            LogUtil.LOG.error("This Will Be Printed On Error4");

        }
        return retVal;
    }

    public static String formatStrNoCommas(String numStr) {
        if (numStr == null) {
            return "";
        }
        Float numFloat = 0F;
        try {
            numFloat = Float.parseFloat(numStr);
        } catch (Exception e) {
            LogUtil.LOG.error("This Will Be Printed On Error5");
        }
        return dfNoCommas.format(numFloat);
    }

    public static String addTotal(String docTotal, String total) {

        float f1 = 0;
        try {
            f1 = Float.parseFloat(docTotal);
        }
        catch (Exception e) {
            LogUtil.LOG.error("This Will Be Printed On Error6");
        }
        float f2 = 0;
        try {
            f2 = Float.parseFloat(total);
        }
        catch (Exception e) {
            LogUtil.LOG.error("This Will Be Printed On Error7");
        }
        return (f1+f2) +"";

    }
}
