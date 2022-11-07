package com.levware.common.session;

import java.io.Serializable;

public class SessionInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	//==================================================
	private String SITE_CD = ""; //SITE
	private String COMP_CD = ""; //company
	
	// 유저정보
	private String USER_ID = ""; //사용자ID
	private String USER_NM = ""; //사용자명
	private String ORG_CD = ""; //소속코드
	private String ORG_NM = ""; //소속
	private String POSITION_CD = ""; //직책코드
	private String POSITION_NM = ""; //직책
	
	// 로케일
	private String LOCALE   = "ko";	//로케일 default:한국어
	
	// -----------------------------------------------
	// 추가정보
	// -----------------------------------------------	
	// 권한
	private String REF_DATA_ROLE	= "";	//자료실권한
	private String SITE_NM = ""; //SITE Name
	private String ACCESSIP = ""; // 접속IP
	private String ACCESSTIME	= ""; // 접속시작시간
	private String ACCESSTIME_PREV	= "";	// 접속시작시간(최근)
	
	
	public String getSITE_CD() {
		return SITE_CD;
	}
	public void setSITE_CD(String sITE_CD) {
		SITE_CD = sITE_CD;
	}
	public String getCOMP_CD() {
		return COMP_CD;
	}
	public void setCOMP_CD(String cOMP_CD) {
		COMP_CD = cOMP_CD;
	}
	public String getUSER_ID() {
		return USER_ID;
	}
	public void setUSER_ID(String uSER_ID) {
		USER_ID = uSER_ID;
	}
	public String getUSER_NM() {
		return USER_NM;
	}
	public void setUSER_NM(String uSER_NM) {
		USER_NM = uSER_NM;
	}
	public String getORG_CD() {
		return ORG_CD;
	}
	public void setORG_CD(String oRG_CD) {
		ORG_CD = oRG_CD;
	}
	public String getORG_NM() {
		return ORG_NM;
	}
	public void setORG_NM(String oRG_NM) {
		ORG_NM = oRG_NM;
	}
	public String getPOSITION_CD() {
		return POSITION_CD;
	}
	public void setPOSITION_CD(String pOSITION_CD) {
		POSITION_CD = pOSITION_CD;
	}
	public String getPOSITION_NM() {
		return POSITION_NM;
	}
	public void setPOSITION_NM(String pOSITION_NM) {
		POSITION_NM = pOSITION_NM;
	}
	public String getLOCALE() {
		return LOCALE;
	}
	public void setLOCALE(String lOCALE) {
		LOCALE = lOCALE;
	}
	public String getREF_DATA_ROLE() {
		return REF_DATA_ROLE;
	}
	public void setREF_DATA_ROLE(String rEF_DATA_ROLE) {
		REF_DATA_ROLE = rEF_DATA_ROLE;
	}
	public String getSITE_NM() {
		return SITE_NM;
	}
	public void setSITE_NM(String sITE_NM) {
		SITE_NM = sITE_NM;
	}
	public String getACCESSIP() {
		return ACCESSIP;
	}
	public void setACCESSIP(String aCCESSIP) {
		ACCESSIP = aCCESSIP;
	}
	public String getACCESSTIME() {
		return ACCESSTIME;
	}
	public void setACCESSTIME(String aCCESSTIME) {
		ACCESSTIME = aCCESSTIME;
	}
	public String getACCESSTIME_PREV() {
		return ACCESSTIME_PREV;
	}
	public void setACCESSTIME_PREV(String aCCESSTIME_PREV) {
		ACCESSTIME_PREV = aCCESSTIME_PREV;
	}
	
	public SessionInfo(String _id) {
		this.USER_ID		= _id;
	}

	public SessionInfo(String _id, String _nm, String _org, String _org_nm, String _position, String _position_nm) {
		this.USER_ID		= _id;
		this.USER_NM		= _nm;
		this.ORG_CD			= _org;
		this.ORG_NM			= _org_nm;
		this.POSITION_CD	= _position;
		this.POSITION_NM	= _position_nm;
	}
	
}
