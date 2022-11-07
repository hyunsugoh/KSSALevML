package com.levware.user.service;

import java.util.List;
import java.util.Map;

public class OlapSelectObjectVO {
	
	private String tbName01;
	
	private String tbName02;
	
	private String relationInfo;
	
	private List<Map<String,Object>> detailInfo;
	
	private List<Map<String, Object>> condition;
	
	private String startDate;
	
	private String startDateStr;
	
	private String endDate;
	
	private String endDateStr;
	
	private String startYearStr;
	
	private String startMonthNDateStr;
	
	private String endYearStr;
	
	private String endMonthNDateStr;
	
	private String standardDateCol;
	
	private boolean isExistGroupFunc = false;
	
	private List<Map<String,Object>> notGroupCol;
	
	private List<Map<String, Object>> orderby;
	
	private List<Map<String, Object>> expCondition;



	public String getTbName01() {
		return tbName01;
	}

	public void setTbName01(String tbName01) {
		this.tbName01 = tbName01;
	}

	public String getTbName02() {
		return tbName02;
	}

	public void setTbName02(String tbName02) {
		this.tbName02 = tbName02;
	}

	public String getRelationInfo() {
		return relationInfo;
	}

	public void setRelationInfo(String relationInfo) {
		this.relationInfo = relationInfo;
	}

	public List<Map<String, Object>> getDetailInfo() {
		return detailInfo;
	}

	public void setDetailInfo(List<Map<String, Object>> detailInfo) {
		this.detailInfo = detailInfo;
	}

	public List<Map<String, Object>> getCondition() {
		return condition;
	}

	public void setCondition(List<Map<String, Object>> condition) {
		this.condition = condition;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getStartDateStr() {
		return startDateStr;
	}

	public void setStartDateStr(String startDateStr) {
		this.startDateStr = startDateStr;
		
		this.startYearStr = startDateStr.substring(0,4);
		
		this.startMonthNDateStr = startDateStr.substring(4,startDateStr.length());
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getEndDateStr() {
		return endDateStr;
	}

	public void setEndDateStr(String endDateStr) {
		this.endDateStr = endDateStr;
		this.endYearStr = endDateStr.substring(0,4);
		
		this.endMonthNDateStr = endDateStr.substring(4, endDateStr.length());
	}

	public List<Map<String, Object>> getOrderby() {
		return orderby;
	}

	public void setOrderby(List<Map<String, Object>> orderby) {
		this.orderby = orderby;
	}

	public String getStartYearStr() {
		return startYearStr;
	}

	public String getStartMonthNDateStr() {
		return startMonthNDateStr;
	}

	public String getEndYearStr() {
		return endYearStr;
	}

	public String getEndMonthNDateStr() {
		return endMonthNDateStr;
	}

	public String getStandardDateCol() {
		return standardDateCol;
	}

	public void setStandardDateCol(String standardDateCol) {
		this.standardDateCol = standardDateCol;
	}

	public List<Map<String, Object>> getNotGroupCol() {
		return notGroupCol;
	}

	public void setNotGroupCol(List<Map<String, Object>> notGroupCol) {
		this.notGroupCol = notGroupCol;
	}

	
	
	public boolean isExistGroupFunc() {
		return isExistGroupFunc;
	}

	public void setExistGroupFunc(boolean isExistGroupFunc) {
		this.isExistGroupFunc = isExistGroupFunc;
	}

	
	public List<Map<String, Object>> getExpCondition() {
		return expCondition;
	}

	public void setExpCondition(List<Map<String, Object>> expCondition) {
		this.expCondition = expCondition;
	}
	
	@Override
	public String toString() {
		return "OlapSelectObjectVO [tbName01=" + tbName01 + ", tbName02=" + tbName02 + ", relationInfo=" + relationInfo
				+ ", detailInfo=" + detailInfo + ", condition=" + condition + ", startDate=" + startDate
				+ ", startDateStr=" + startDateStr + ", endDate=" + endDate + ", endDateStr=" + endDateStr
				+ ", startYearStr=" + startYearStr + ", startMonthNDateStr=" + startMonthNDateStr + ", endYearStr="
				+ endYearStr + ", endMonthNDateStr=" + endMonthNDateStr + ", standardDateCol=" + standardDateCol
				+ ", isExistGroupFunc=" + isExistGroupFunc + ", notGroupCol=" + notGroupCol + ", orderby=" + orderby
				+ "]";
	}

	
	



	
	
}
