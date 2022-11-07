package com.levware.user.service;

public class BoardVO {
	
	private int seqNum;
	private String userId;
	private String boardTitle;
	private String boardContents;
	private String boardWriter;
	private String createDt;
	private String createTm;
	private String updateDt;
	private String updateTm;
	private int boardViews;
	
	@Override
	public String toString() {
		return "BoardVO [seqNum=" + seqNum + ", userId=" + userId + ", boardTitle=" + boardTitle + ", boardContents="
				+ boardContents + ", boardWriter=" + boardWriter + ", createDt=" + createDt + ", createTm=" + createTm
				+ ", updateDt=" + updateDt + ", updateTm=" + updateTm + ", boardViews=" + boardViews + "]";
	}

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

	public String getBoardTitle() {
		return boardTitle;
	}

	public void setBoardTitle(String boardTitle) {
		this.boardTitle = boardTitle;
	}

	public String getBoardContents() {
		return boardContents;
	}

	public void setBoardContents(String boardContents) {
		this.boardContents = boardContents;
	}

	public String getBoardWriter() {
		return boardWriter;
	}

	public void setBoardWriter(String boardWriter) {
		this.boardWriter = boardWriter;
	}

	public String getCreateDt() {
		return createDt;
	}

	public void setCreateDt(String createDt) {
		this.createDt = createDt;
	}

	public String getCreateTm() {
		return createTm;
	}

	public void setCreateTm(String createTm) {
		this.createTm = createTm;
	}

	public String getUpdateDt() {
		return updateDt;
	}

	public void setUpdateDt(String updateDt) {
		this.updateDt = updateDt;
	}

	public String getUpdateTm() {
		return updateTm;
	}

	public void setUpdateTm(String updateTm) {
		this.updateTm = updateTm;
	}

	public int getBoardViews() {
		return boardViews;
	}

	public void setBoardViews(int boardViews) {
		this.boardViews = boardViews;
	}
	
	
	

	
	
	
}
