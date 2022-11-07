package com.levware.common.session;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


public  class SessionUtil {
	
	public static String LOGIN_SESSION = "LevSESS";     // 로그인 세션명
	public String sSessionID="";
	
	/**
	 * 세션 ID 를 반환한다.
	 * @param req
	 * @return
	 */
	public static String getSessionID( HttpServletRequest req ) {
		return req.getSession().getId();
	}
	
	/**
	 * 세션에서 로그인 객체를 반환한다.
	 * @param req
	 * @return
	 */
	public static SessionInfo getSessionInfo( HttpServletRequest req ) {
		return (SessionInfo) req.getSession().getAttribute("SessionInfo");
	}
	
	/**
	 * 세션에 로그인 객체를 저장한다.
	 * @param sess
	 * @param obj
	 */
	public static void setSessionInfo( HttpSession sess, SessionInfo obj, HttpServletRequest req ) {
		synchronized(sess) {
			sess.setAttribute( "SessionInfo", obj );
		}
	}
	
	/**
	 * 세션에 로그인 객체를 저장한다.
	 * @param req
	 * @param obj
	 */
	public static void setSessionInfo( HttpServletRequest req, SessionInfo obj ) {
		HttpSession sess = req.getSession();
		setSessionInfo( sess, obj , req);
	}
	
	/**
	 * 로그인 여부를 반환한다.
	 *   - 세션에 로그인 객체가 있으면 로그인 한것으로 간주한다.
	 * @param req
	 * @return
	 */
	public static boolean isLogin( HttpServletRequest req ) {
		return ( getSessionInfo(req) != null ) ? true : false ;
	}
	
	/**
	 * attribute 값을 가져오기 위한 메소드
	 * @param name
	 * @return
	 */
	public static Object getAttibute(String name) {
		return (Object)RequestContextHolder.getRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_SESSION);
	}
	/**
	 *  attribute 설정 메소드
	 * @param name
	 * @param object
	 */
	public static void setAttribute(String name, Object object){
		RequestContextHolder.getRequestAttributes().setAttribute(name, object, RequestAttributes.SCOPE_SESSION);
	}
	
	/**
	 * 설정한  attribute 삭제
	 * @param Name
	 */
	public static void removeAttribute(String Name){
		RequestContextHolder.getRequestAttributes().removeAttribute(Name, RequestAttributes.SCOPE_SESSION);
	}
	
	
	/**
	 * 현재 세션에 저장된 Locale 정보를 반환
	 * @param req
	 * @return
	 */
	public static Locale getLoc(HttpServletRequest req){
		if(isLogin(req)){
			if("ko".equals(getSessionInfo(req).getLOCALE())){
				return Locale.KOREA;
			}else if ("en".equals(getSessionInfo(req).getLOCALE())){
				return Locale.ENGLISH;
			}else if ("zh".equals(getSessionInfo(req).getLOCALE())){
				return Locale.CHINESE;
			}else{
				return Locale.KOREA;
			}
		}else{
			return Locale.KOREA;
		}
	}
		
}