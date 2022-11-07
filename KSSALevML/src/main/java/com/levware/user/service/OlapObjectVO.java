package com.levware.user.service;

public class OlapObjectVO {

	/** 테이블 명*/
	private String tableName;
	
	/** 객체 명*/
	private String objectName;
	/** 객체 설명 */
	private String objectDescription;
	
	/** 아이콘 URL */
	private String iconURL;
	
	
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getObjectDescription() {
		return objectDescription;
	}
	public void setObjectDescription(String objectDescription) {
		this.objectDescription = objectDescription;
	}
	public String getIconURL() {
		return iconURL;
	}
	public void setIconURL(String iconURL) {
		this.iconURL = iconURL;
	}
	
	@Override
	public String toString() {
		return "OlapObjectVO [tableName=" + tableName + ", objectName=" + objectName + ", objectDescription="
				+ objectDescription + ", iconURL=" + iconURL + "]";
	}
	
	
	
}
