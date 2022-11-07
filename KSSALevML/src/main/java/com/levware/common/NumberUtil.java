package com.levware.common;

import java.math.BigInteger;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * NumberUtil
 */
public abstract class NumberUtil {

	/**
	 * <p>
	 * Checks whether the String a valid Java number.
	 * </p>
	 * 
	 * <p>
	 * Valid numbers include hexadecimal marked with the <code>0x</code> qualifier, scientific notation and numbers marked with a type qualifier (e.g.
	 * 123L).
	 * </p>
	 * 
	 * <p>
	 * <code>Null</code> and empty String will return <code>false</code>.
	 * </p>
	 * 
	 * @param str
	 *            the <code>String</code> to check
	 * @return <code>true</code> if the string is a correctly formatted number
	 */
	public static boolean isNumber(String str) {

		return NumberUtils.isNumber(str);
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to a <code>byte</code>, returning <code>zero</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string is <code>null</code>, <code>zero</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toByte(null) = 0
	 *   NumberUtils.toByte("")   = 0
	 *   NumberUtils.toByte("1")  = 1
	 * </pre>
	 * 
	 * @param str
	 *            the string to convert, may be null
	 * @return the byte represented by the string, or <code>zero</code> if conversion fails
	 * @since 2.5
	 */
	public static byte toByte(String str) {

		return NumberUtils.toByte(str);
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to a <code>double</code>, returning <code>0.0d</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string <code>str</code> is <code>null</code>, <code>0.0d</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toDouble(null)   = 0.0d
	 *   NumberUtils.toDouble("")     = 0.0d
	 *   NumberUtils.toDouble("1.5")  = 1.5d
	 * </pre>
	 * 
	 * @param obj
	 *            the string to convert, may be <code>null</code>
	 * @return the double represented by the string, or <code>0.0d</code> if conversion fails
	 * @since 2.1
	 */
	public static double toDouble(Object obj) {

		if (null == obj) {
			return 0.0;
		}

		return NumberUtils.toDouble(obj.toString());
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to a <code>double</code>, returning <code>0.0d</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string <code>str</code> is <code>null</code>, <code>0.0d</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toDouble(null)   = 0.0d
	 *   NumberUtils.toDouble("")     = 0.0d
	 *   NumberUtils.toDouble("1.5")  = 1.5d
	 * </pre>
	 * 
	 * @param str
	 *            the string to convert, may be <code>null</code>
	 * @return the double represented by the string, or <code>0.0d</code> if conversion fails
	 * @since 2.1
	 */
	public static double toDouble(String str) {

		return NumberUtils.toDouble(str);
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to a <code>float</code>, returning <code>0.0f</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string <code>str</code> is <code>null</code>, <code>0.0f</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toFloat(null)   = 0.0f
	 *   NumberUtils.toFloat("")     = 0.0f
	 *   NumberUtils.toFloat("1.5")  = 1.5f
	 * </pre>
	 * 
	 * @param str
	 *            the string to convert, may be <code>null</code>
	 * @return the float represented by the string, or <code>0.0f</code> if conversion fails
	 * @since 2.1
	 */
	public static float toFloat(String str) {

		return NumberUtils.toFloat(str);
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to an <code>int</code>, returning <code>zero</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string is <code>null</code>, <code>zero</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toInt(null) = 0
	 *   NumberUtils.toInt("")   = 0
	 *   NumberUtils.toInt("1")  = 1
	 * </pre>
	 * 
	 * @param obj
	 *            the string to convert, may be null
	 * @return the int represented by the string, or <code>zero</code> if conversion fails
	 * @since 2.1
	 */
	public static int toInt(Object obj) {

		if (null == obj) {
			return 0;
		}

		return NumberUtils.toInt(obj.toString());
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to an <code>int</code>, returning <code>zero</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string is <code>null</code>, <code>zero</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toInt(null) = 0
	 *   NumberUtils.toInt("")   = 0
	 *   NumberUtils.toInt("1")  = 1
	 * </pre>
	 * 
	 * @param str
	 *            the string to convert, may be null
	 * @return the int represented by the string, or <code>zero</code> if conversion fails
	 * @since 2.1
	 */
	public static int toInt(String str) {

		return NumberUtils.toInt(str);
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to an <code>int</code>, returning a default value if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string is <code>null</code>, the default value is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toInt(null, 1) = 1
	 *   NumberUtils.toInt("", 1)   = 1
	 *   NumberUtils.toInt("1", 0)  = 1
	 * </pre>
	 * 
	 * @param str
	 *            the string to convert, may be null
	 * @param defaultValue
	 *            the default value
	 * @return the int represented by the string, or the default if conversion fails
	 * @since 2.1
	 */
	public static int toInt(String str, int defaultValue) {

		return NumberUtils.toInt(str, defaultValue);
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to a <code>long</code>, returning <code>zero</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string is <code>null</code>, <code>zero</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toLong(null) = 0L
	 *   NumberUtils.toLong("")   = 0L
	 *   NumberUtils.toLong("1")  = 1L
	 * </pre>
	 * 
	 * @param str
	 *            the string to convert, may be null
	 * @return the long represented by the string, or <code>0</code> if conversion fails
	 * @since 2.1
	 */
	public static long toLong(String str) {

		return NumberUtils.toLong(str);
	}

	/**
	 * <p>
	 * Convert a <code>String</code> to a <code>short</code>, returning <code>zero</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string is <code>null</code>, <code>zero</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.toShort(null) = 0
	 *   NumberUtils.toShort("")   = 0
	 *   NumberUtils.toShort("1")  = 1
	 * </pre>
	 * 
	 * @param str
	 *            the string to convert, may be null
	 * @return the short represented by the string, or <code>zero</code> if conversion fails
	 * @since 2.5
	 */
	public static short toShort(String str) {

		return NumberUtils.toShort(str);
	}
	
	/**
	 * 
	 * 단위 환산
	 * 
	 * NumberUtil.unit_convert(단위코드, 금액)
	 * 
	 */
	public static double unit_convert(String unitCd, double result) {
		
		// Kg
		if("U002".equals(unitCd))
			result = result / 1000;

		return result;
	}
	
	public static double round(double value, int in){
		
		String cipher = "1";
		
		cipher = rpad(cipher, in+1, "0");
		
		double temp = Math.round(value * Double.parseDouble(cipher));
		
		temp = temp / Double.parseDouble(cipher);
		
		return temp;
	}
	
	public static double round_up(double value, int in){
		
		String cipher = "1";
		
		cipher = rpad(cipher, in, "0");
		
		double temp = Math.ceil(value * Double.parseDouble(cipher));
		
		temp = temp / Double.parseDouble(cipher);
		
		return temp;
	}
	
	public static double round_down(double value, int in){
		
		String cipher = "1";
		
		cipher = rpad(cipher, in+1, "0");
		
		double temp = Math.floor(value * Double.parseDouble(cipher));
		
		temp = temp / Double.parseDouble(cipher);
		
		return temp;
	}
	
	public static String rpad(String source, int n, String pad){
		return pad(source, n, pad, 1);
	}
	
	/**
	 * ORACLE PAD함수 구현
	 * @param source
	 * @param n
	 * @param pad
	 * @return
	 */
	public static String pad(String source, int n, String pad, int lr) {
		StringBuffer sb = new StringBuffer();
		
		if(source == null)
		{
			return null;
		}
		if(source.length() >= n){
			return source;
		}
		
		if (n<=0) {
			return source; 
		} else {
			for (int i=0;i<n ; i++) {
				sb.append(pad);
			}
		}
		
		if (lr == 0) {		
			return (sb.toString()).substring(0, n - source.length())+source;
		} else {
			return source+(sb.toString()).substring(0, n - source.length());
		}
	}
	
	/**
	 * <p>
	 * Convert a <code>String</code> to an <code>int</code>, returning <code>zero</code> if the conversion fails.
	 * </p>
	 * 
	 * <p>
	 * If the string is <code>null</code>, <code>zero</code> is returned.
	 * </p>
	 * 
	 * <pre>
	 *   NumberUtils.createBigInteger(null) = 0
	 *   NumberUtils.createBigInteger("")   = 0
	 *   NumberUtils.createBigInteger("1")  = 1
	 *   NumberUtils.createBigInteger("11111111111111111111111111111111111111")  = 11111111111111111111111111111111111111
	 * </pre>
	 * 
	 * @param obj
	 *            the string to convert, may be null
	 * @return the int represented by the string, or <code>zero</code> if conversion fails
	 * @since 2.1
	 */
	public static BigInteger createBigInteger(Object obj) {

		if (null == obj) {
			return NumberUtils.createBigInteger("0");
		}

		return NumberUtils.createBigInteger(obj.toString());
	}
}
