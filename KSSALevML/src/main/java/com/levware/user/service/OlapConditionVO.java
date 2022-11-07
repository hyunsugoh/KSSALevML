package com.levware.user.service;

public class OlapConditionVO {

	/** 테이블 */
	private String tableName;
	
	/** 컬럼 */
	private String columnName;
	
	/** 조회조건 코드 */
	private String qryConCode;
	
	/** 조회조건 코드 명*/
	private String qryConCodeNm;
	
	/** 연산자*/
	private String operSym;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getQryConCode() {
		return qryConCode;
	}

	public void setQryConCode(String qryConCode) {
		this.qryConCode = qryConCode;
	}

	public String getQryConCodeNm() {
		return qryConCodeNm;
	}

	public void setQryConCodeNm(String qryConCodeNm) {
		this.qryConCodeNm = qryConCodeNm;
	}

	public String getOperSym() {
		return operSym;
	}

	public void setOperSym(String operSym) {
		this.operSym = operSym;
	}

	@Override
	public String toString() {
		return "OlapConditionVO [tableName=" + tableName + ", columnName=" + columnName + ", qryConCode=" + qryConCode
				+ ", qryConCodeNm=" + qryConCodeNm + ", operSym=" + operSym + "]";
	}
	
	
}
