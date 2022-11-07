package com.levware.common;

/**
 * StringUtil
 */
public abstract class StringUtils {

	
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

}
