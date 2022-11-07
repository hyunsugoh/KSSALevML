package com.levware.user.service;

/**
 * 사용자 VO
 * @author 박수연
 * @since 2019.03.12
 */
public class UserInfoVO {
	
	
	/** 사용자 아이디 */
	private String userId;
	
	/** 패스워드 */
	private String userPassward;
	
	/** 사용자 명 */
	private String userNm;
	
	/** 보조 아이디 */
	private String subId;
	
	/** 개인확인힌트 */
	private String authHint;
	
	/** 개인확인힌트 정답 */
	private String authHintAns;
	
	/** 아이디 활성여부 */
	private int enabled;
	
	/** 유저권한 */
	private String userRole;

	/** 초기화비밀번호(인코딩안함) */
	private String initPassward;
	
	/** 테이블 명 */
	private String tableName;
	
	/** 속성 명 */
	private String columnName;
	
	
	/** 테이블 명 */
	private String qryConYn;
	
	/** 객체 명 */
	private String obj_name;	
	
	/** 객체정보 명 */
	private String objinfo_name;	
	
	public String getUserId(){
		return userId;
	}
	public String getUserPassward(){
		return userPassward;
	}
	public String getUserNm(){
		return  userNm;
	}
	public String getSubId(){
		return subId;
	}
	public String getAuthHint(){
		return authHint;
	}
	public String getAuthHintAns(){
		return authHintAns;
	}
	public int getEnabled(){
		return enabled;
	}
	public String getUserRole(){
		return userRole;
	}
	public String getInitPassward(){
		return initPassward;
	}
	public String getTableName(){
		return tableName;
	}
	public String getColumnName(){
		return columnName;
	}
	public String getQryConYn(){
		return qryConYn;
	}
	public String getObj_name() {
		return obj_name;
	}
	public String getObjinfo_name() {
		return objinfo_name;
	}
	
	

	public void setUserId(String userId){
		this.userId = userId;
	}
	public void setUserPassward(String userPassward){
		this.userPassward = userPassward;
	}
	public void setUserNm(String userNm){
		this.userNm = userNm;
	}
	public void setSubId(String subId){
		this.subId = subId;
	}
	public void setAuthHint(String authHint){
		this.authHint = authHint;
	}
	public void setAuthHintAns(String authHintAns){
		this.authHintAns = authHintAns;
	}
	public void setEnabled(int enabled){
		this.enabled = enabled;
	}
	public void setUserRole(String userRole){
		this.userRole = userRole;
	}
	public void setInitPassward(String initPassward){
		this.initPassward = initPassward;
	}
	public void getTableName(String tableName){
		this.tableName = tableName;
	}
	public void getColumnName(String columnName){
		this.columnName = columnName;
	}
	public void getQryConYn(String qryConYn){
		this.qryConYn = qryConYn;
	}
	
	public void setObj_name(String obj_name) {
		this.obj_name = obj_name;
	}

	public void setObjinfo_name(String objinfo_name) {
		this.objinfo_name = objinfo_name;
	}
	//ObjectList 
	@Override
	public String toString() {
		return "UserInfoVO [userId=" + userId + ", userPassward=" + userPassward + ", subId=" + subId + ", authHint=" + authHint          
				+", authHintAns=" + authHintAns + ", enabled=" + enabled +", userRole=" +userRole + ",initPassward=" +initPassward + ",tableName=" +tableName + ",columnName=" +columnName + ",qryConYn=" +qryConYn +",objinfo_name=" +objinfo_name+ ",obj_name=" +obj_name+ "]";
	}

	


	
	
}
