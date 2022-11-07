package com.levware.common;

import javax.servlet.http.HttpServletRequest;

public class IPTraceUtils {
/**
 * Proxy 환경의 클라이언트 IP 조회 Class 
 * @author 박수연
 * @since 2019.04.03
 * 참고출처
 * https://www.lesstif.com/pages/viewpage.action?pageId=20775886
 */
public static String getRemoteAddr(HttpServletRequest request) {
	 String ip = request.getHeader("X-Forwarded-For");
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	     ip = request.getHeader("Proxy-Client-IP"); 
	 } 
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	     ip = request.getHeader("WL-Proxy-Client-IP"); 
	 } 
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	     ip = request.getHeader("HTTP_CLIENT_IP"); 
	 } 
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	     ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
	 }
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		 ip = request.getHeader("X-Real-IP"); 
	 } 
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		 ip = request.getHeader("X-RealIP"); 
	 } 
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
		 ip = request.getHeader("REMOTE_ADDR"); 
	 } 
	 if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
	     ip = request.getRemoteAddr(); 
	 }

        return ip;
    }
}

