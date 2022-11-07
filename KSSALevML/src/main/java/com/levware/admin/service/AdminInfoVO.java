package com.levware.admin.service;


/**
 * 관리자 회원정보 VO
 * @author 박수연
 * @since 2019.04.03
 */
public class AdminInfoVO {
	
	
	/** 관리자ID */
	private String managerId;
	/** 접속허용IP */
	private String managerIp;
	
	
	
	public String getManagerId(){
		return managerId;
	}
	public String getManagerIp(){
		return managerIp;
	}


	
	public void setManagerId(String managerId){
		this.managerId = managerId;
	}
	public void setManagerIp(String managerIp){
		this.managerIp = managerIp;
	}

	
	
	//ObjectList 
	@Override
	public String toString() {
		return "AdminInfoVO [managerId=" + managerId + ", managerIp=" + managerIp +"]";
	}
	


	
	
}
