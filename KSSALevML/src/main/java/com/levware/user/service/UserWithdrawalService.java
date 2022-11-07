package com.levware.user.service;


import java.util.Map;





public interface UserWithdrawalService {
	
	/**
	 * 회원 탈퇴 
	 * @since 2019.07.11
	 * @author 조형욱
	 */
	public String actionWithdrawal(Map<String, String> param) throws Exception;
		
}
