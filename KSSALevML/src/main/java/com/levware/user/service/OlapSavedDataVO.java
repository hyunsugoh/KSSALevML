package com.levware.user.service;

public class OlapSavedDataVO {

	private int seqNum;
	private String userId;
	private String titleText;
	private String descriptionText;
	private String qryStr;
	private String createDt;
	private String updateDt;
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getTitleText() {
		return titleText;
	}
	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}
	public String getDescriptionText() {
		return descriptionText;
	}
	public void setDescriptionText(String descriptionText) {
		this.descriptionText = descriptionText;
	}
	public String getQryStr() {
		return qryStr;
	}
	public void setQryStr(String qryStr) {
		this.qryStr = qryStr;
	}
	public String getCreateDt() {
		return createDt;
	}
	public void setCreateDt(String createDt) {
		this.createDt = createDt;
	}
	public String getUpdateDt() {
		return updateDt;
	}
	public void setUpdateDt(String updateDt) {
		this.updateDt = updateDt;
	}
	
	
	@Override
	public String toString() {
		return "OlapSavedDataVO [seqNum=" + seqNum + ", userId=" + userId + ", titleText=" + titleText
				+ ", descriptionText=" + descriptionText + ", qryStr=" + qryStr + ", createDt=" + createDt
				+ ", updateDt=" + updateDt + "]";
	}
	
	
	
	
}
