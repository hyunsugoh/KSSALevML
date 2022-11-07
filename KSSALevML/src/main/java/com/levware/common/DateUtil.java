package com.levware.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * DateUtil
 */
public abstract class DateUtil {

	/**
	 * Date 를 주어진 형식으로 포매팅해준다.
	 * 
	 * @param format
	 * @param date
	 * @return String
	 */
	public static String getFormatDate(String format, Date date) {
		return new SimpleDateFormat(format).format(date);
	}

	/**
	 * 오늘을 YYYY 형식으로 리턴한다.
	 * 
	 * @return 20100810
	 */
	public static String todayYYYY() {
		return getFormatDate("yyyy", Calendar.getInstance().getTime());
	}

	/**
	 * 오늘을 YYYYMM 형식으로 리턴한다.
	 * 
	 * @return 20100810
	 */
	public static String todayYYYYMM() {
		return getFormatDate("yyyyMM", Calendar.getInstance().getTime());
	}

	/**
	 * 오늘을 YYYYMMDD 형식으로 리턴한다.
	 * 
	 * @return 20100810
	 */
	public static String todayYYYYMMDD() {
		return getFormatDate("yyyyMMdd", Calendar.getInstance().getTime());
	}
	
	/**
	 * 오늘 2주전을 YYYYMMDD 형식으로 리턴한다.
	 * 
	 * @return 20100810
	 */
	public static String todayYYYYMMDD_two_week_before() {
		Calendar t = Calendar.getInstance();
		t.add(Calendar.WEEK_OF_MONTH, -2);
		t.add(Calendar.DATE, +1);
		return getFormatDate("yyyyMMdd", t.getTime());
	}
	
	/**
	 * 오늘 2후를 YYYY-MM-DD 형식으로 리턴한다.
	 * 
	 * @return 20100810
	 */
	public static String todayYYYYMMDD3_two_week_after() {
		Calendar t = Calendar.getInstance();
		t.add(Calendar.DATE, +14);
		return getFormatDate("yyyy-MM-dd", t.getTime());
	}
	
	/**
	 * 오늘 10년후를 YYYY-MM-DD 형식으로 리턴한다.
	 * 
	 * todayYYYYMMDD_period(1, 10)  : 10년 후 
	 * todayYYYYMMDD_period(2, -10) : 10달 전
	 * todayYYYYMMDD_period(4, -2)  : 2주 전
	 * todayYYYYMMDD_period(5, -10) : 10일 전
	 * 
	 * YEAR : 1
	 * MONTH : 2
	 * WEEK_OF_MONTH : 4
	 * DATE : 5
	 * 
	 * @return 20100810
	 */
	public static String todayYYYYMMDD_period(int ymd, int period) {
		Calendar t = Calendar.getInstance();
		
		t.add(ymd, period);
		if(period < 0)
			t.add(Calendar.DATE, +1);
		
		return getFormatDate("yyyyMMdd", t.getTime());
	}
	
	/**
	 * 오늘을 YYYY.MM 형식으로 리턴한다.
	 * 
	 * @return 2010.08
	 */
	public static String todayYYYYMM2() {
		return getFormatDate("yyyy.MM", Calendar.getInstance().getTime());
	}
	/**
	 * 오늘을 YYYY.MM.DD 형식으로 리턴한다.
	 * 
	 * @return 2010.08.10
	 */
	public static String todayYYYYMMDD2() {
		return getFormatDate("yyyy.MM.dd", Calendar.getInstance().getTime());
	}
	
	public static int getAge(String birth){
		String year = "";
		String month = "";
		String day = "";
		int age = 0;
		
		SimpleDateFormat formatY = new SimpleDateFormat("yyyy", Locale.KOREA);
		SimpleDateFormat formatM = new SimpleDateFormat("MM", Locale.KOREA);
		SimpleDateFormat formatD = new SimpleDateFormat("dd", Locale.KOREA);
		year = formatY.format(new Date());
		month = formatM.format(new Date());
		day = formatD.format(new Date());
		
		if(Integer.parseInt(month) > Integer.parseInt(birth.substring(4, 6))){
			age = Integer.parseInt(year) - Integer.parseInt(birth.substring(0, 4));
		}else if(Integer.parseInt(month) == Integer.parseInt(birth.substring(4, 6))){
			if(Integer.parseInt(day) > Integer.parseInt(birth.substring(6))){
				age = Integer.parseInt(year) - Integer.parseInt(birth.substring(0, 4));
			}else{
				age = Integer.parseInt(year) - (Integer.parseInt(birth.substring(0, 4)) + 1);
			}
		}else{
			age = Integer.parseInt(year) - (Integer.parseInt(birth.substring(0, 4)) + 1);
		}
		return age;
	}
	
	/**
	 * 현재 월을 반환
	 * @return
	 */
	public static String todayMM() {
		Calendar cal = Calendar.getInstance();
	    return ""+(cal.get(Calendar.MONTH) + 1);
	}
}
