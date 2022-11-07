package com.levware.user.service;

import java.io.Serializable;

public class PriceVO implements Serializable {
	
	/**
	 * serial
	 */
	private static final long serialVersionUID = 1289121972814876673L;

	/** 날짜 */
	private String investDt;
	
	/** 코드? */
	private String itmCd;
	
	/** 아이템 넘버 */
	private String itmNm;
	
	/** 무게? */
	private String unitQty;
	
	/** 단위 */
	private String specNmUnifi;
	
	/** 품질 */
	private String grdNmUnifi;
	
	/** 가격 (최댓값) */
	private int maxPrice;
	
	/** 가격 (평균값) */
	private int avgPrice;
	
	/** 가격 (최소값) */
	private int lowPrice;

	public String getInvestDt() {
		return investDt;
	}

	public void setInvestDt(String investDt) {
		this.investDt = investDt;
	}

	public String getItmCd() {
		return itmCd;
	}

	public void setItmCd(String itmCd) {
		this.itmCd = itmCd;
	}

	public String getItmNm() {
		return itmNm;
	}

	public void setItmNm(String itmNm) {
		this.itmNm = itmNm;
	}

	public String getUnitQty() {
		return unitQty;
	}

	public void setUnitQty(String unitQty) {
		this.unitQty = unitQty;
	}

	public String getSpecNmUnifi() {
		return specNmUnifi;
	}

	public void setSpecNmUnifi(String specNmUnifi) {
		this.specNmUnifi = specNmUnifi;
	}

	public String getGrdNmUnifi() {
		return grdNmUnifi;
	}

	public void setGrdNmUnifi(String grdNmUnifi) {
		this.grdNmUnifi = grdNmUnifi;
	}

	public int getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(int maxPrice) {
		this.maxPrice = maxPrice;
	}

	public int getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(int avgPrice) {
		this.avgPrice = avgPrice;
	}

	public int getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(int lowPrice) {
		this.lowPrice = lowPrice;
	}

	@Override
	public String toString() {
		return "PriceVO [investDt=" + investDt + ", itmCd=" + itmCd + ", itmNm=" + itmNm + ", unitQty=" + unitQty
				+ ", specNmUnifi=" + specNmUnifi + ", grdNmUnifi=" + grdNmUnifi + ", maxPrice=" + maxPrice
				+ ", avgPrice=" + avgPrice + ", lowPrice=" + lowPrice + "]";
	}
	
	
}
