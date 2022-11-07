package com.levware.user.service;

public class OlapSavedDataDelVO {

	private String userName;
	private int seqNum;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	@Override
	public String toString() {
		return "OlapSavedDataDelVO [userName=" + userName + ", seqNum=" + seqNum + "]";
	}
	
	
	
}
