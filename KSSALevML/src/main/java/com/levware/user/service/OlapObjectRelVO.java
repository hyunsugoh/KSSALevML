package com.levware.user.service;

public class OlapObjectRelVO {

	private String stdTable;
	
	private String connTable;
	
	private String joinExpr;

	public String getStdTable() {
		return stdTable;
	}

	public void setStdTable(String stdTable) {
		this.stdTable = stdTable;
	}

	public String getConnTable() {
		return connTable;
	}

	public void setConnTable(String connTable) {
		this.connTable = connTable;
	}

	public String getJoinExpr() {
		return joinExpr;
	}

	public void setJoinExpr(String joinExpr) {
		this.joinExpr = joinExpr;
	}

	@Override
	public String toString() {
		return "OlapObjectRelVO [stdTable=" + stdTable + ", connTable=" + connTable + ", joinExpr=" + joinExpr + "]";
	}
	
	
}
