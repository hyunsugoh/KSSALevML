package com.levware.common;

/**
 * StringUtil
 */
public abstract class StringUtil {

	
	public static String get(Object o) {
		return o==null?"":String.valueOf(o);
	}
	
	public static String get(Object o, String def) {
		return o==null || "".equals(String.valueOf(o))?def:String.valueOf(o);
	}
	
	public static boolean isBlank(Object o) {
		if (o==null ) return true;
		else if ("".equals(o.toString())) return true;
		else return false;
	}
	
    public static String setNvl(Object obj) {

        if (obj == null) {
            return "";
        } else {
            if (obj.toString().trim().toUpperCase().equals("NULL")) {
                return "";
            } else {
                return obj.toString();
            }
        }
    }

    public static String setNvl(String str) {

        if (str == null || str.trim().toUpperCase().equals("NULL")) {
            return "";
        } else {
            return str;
        }
    }
    
    public static int setNvlN(Object obj) {

        if (obj == null) {
            return 0;
        } else {
            if (obj.toString().trim().toUpperCase().equals("NULL")) {
                return 0;
            } else {
                return Integer.parseInt(obj.toString());
            }
        }
    }

}
