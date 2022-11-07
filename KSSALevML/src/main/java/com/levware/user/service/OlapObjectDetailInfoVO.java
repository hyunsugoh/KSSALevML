package com.levware.user.service;

public class OlapObjectDetailInfoVO {

	/** 테이블 명 */
	private String tableName;

	/** 컬럼 명 */
	private String colName;

	/** 객체정보 명 */
	private String objInfoName;

	/** 객체 정보 상세 설명 */
	private String objInfoNameDesc;

	/** 데이터 타입 */
	private String dataType;

	/** 계산 함수 */
	private String calcFunc = null;

	/** PK 여부 */
	private String pkYn;

	/** 조회조건 설정 여부 */
	private String qryConYn;

	/** 기준일자  여부 */
	private String standDate;


	/** 기간 설정-단위 */
	private String durationUnit;

	/** 기간 설정-숫자 */
	private String durationNum;
	
	/** 계산함수  yn */
	private String calcFuntionYn;
	

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColName() {
		return colName;
	}

	public void setColName(String colName) {
		this.colName = colName;
	}

	public String getObjInfoName() {
		return objInfoName;
	}

	public void setObjInfoName(String objInfoName) {
		this.objInfoName = objInfoName;
	}

	public String getObjInfoNameDesc() {
		return objInfoNameDesc;
	}

	public void setObjInfoNameDesc(String objInfoNameDesc) {
		this.objInfoNameDesc = objInfoNameDesc;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getCalcFunc() {
		return calcFunc;
	}

	public void setCalcFunc(String calcFunc) {
		this.calcFunc = calcFunc;
	}

	public String getPkYn() {
		return pkYn;
	}

	public void setPkYn(String pkYn) {
		this.pkYn = pkYn;
	}

	public String getQryConYn() {
		return qryConYn;
	}

	public void setQryConYn(String qryConYn) {
		this.qryConYn = qryConYn;
	}

	public String getStandDate() {
		return standDate;
	}

	public void setStandDate(String standDate) {
		this.standDate = standDate;
	}

	public String getDurationUnit() {
		return durationUnit;
	}

	public void setDurationUnit(String durationUnit) {
		this.durationUnit = durationUnit;
	}
	
	public String getDurationNum() {
		return durationNum;
	}

	public void setDurationNum(String durationNum) {
		this.durationNum = durationNum;
	}
	
	public String getCalcFuntionYn() {
		return calcFuntionYn;
	}

	public void setCalcFuntionYn(String calcFuntionYn) {
		this.calcFuntionYn = calcFuntionYn;
	}
	
	
	
	@Override
	public String toString() {
		return "OlapObjectDetailInfoVO [tableName=" + tableName + ", colName=" + colName + ", objInfoName="
				+ objInfoName + ", objInfoNameDesc=" + objInfoNameDesc + ", dataType=" + dataType + ", calcFunc="
				+ calcFunc + ", pkYn=" + pkYn + ", qryConYn=" + qryConYn + "]";
	}

}
