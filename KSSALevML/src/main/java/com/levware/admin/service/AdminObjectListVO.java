package com.levware.admin.service;

import java.io.Serializable;

public class AdminObjectListVO implements Serializable {
	
	/**
	 * serial
	 */
	private static final long serialVersionUID = 1289121972814876673L;

	/** 날짜 */
	private String table_name;
	
	/** 코드? */
	private String obj_name;
	
	/** 아이템 넘버 */
	private String obj_desc;
	
	/** 무게? */
	private String activ_yn;

	public String getTable_name() {
		return table_name;
	}

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public String getObj_name() {
		return obj_name;
	}

	public void setObj_name(String obj_name) {
		this.obj_name = obj_name;
	}

	public String getObj_desc() {
		return obj_desc;
	}

	public void setObj_desc(String obj_desc) {
		this.obj_desc = obj_desc;
	}

	public String getActiv_yn() {
		return activ_yn;
	}

	public void setActiv_yn(String activ_yn) {
		this.activ_yn = activ_yn;
	}


	@Override
	public String toString() {
		return "AdminObjectListVO [table_name=" + table_name + ", obj_name=" + obj_name + ", obj_desc=" + obj_desc
				+ ", activ_yn=" + activ_yn + "]";
	}
	
	

	
	
}
