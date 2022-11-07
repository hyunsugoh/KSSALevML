package com.levware.admin.service;

/**
 * 코드관리 VO
 * @author 박수연
 */
public class CodeManagementVO {
	
	
	/** 조회조건코드 */
	private String qryConCd;
	
	/** 조회조건코드명 */
	private String qryConCdNm;
	
	/** 계산식(연산자) */
	private String operSym;
	
	/** 예시 */
	private String qryExample;
	
	/** 생성자 */
	private String createId;
	
	
	public String getQryConCd(){
		return qryConCd;
	}
	public String getQryConCdNm(){
		return qryConCdNm;
	}
	public String getOperSym(){
		return operSym;
	}
	public String getQryExample(){
		return qryExample;
	}
	public String getCreateId(){
		return createId;
	}

	
	public void setQryConCd(String qryConCd){
		this.qryConCd = qryConCd;
	}
	public void setQryConCdNm(String qryConCdNm){
		this.qryConCdNm = qryConCdNm;
	}
	public void setOperSym(String operSym){
		this.operSym = operSym;
	}
	public void setQryExample(String qryExample){
		this.qryExample = qryExample;
	}
	public void setCreateId(String createId){
		this.createId = createId;
	}
	
	
	//ObjectList 
	@Override
	public String toString() {
		return "CodeManagementVO [qryConCd=" + qryConCd + ", qryConCdNm=" + qryConCdNm + ", operSym=" + operSym + ", qryExample=" + qryExample +"]";
	}
	


	
	
}
