package com.levware.user.service.impl;


import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.*;
import com.levware.user.service.UserInfoVO;
import com.levware.user.service.UserWithdrawalService;




@Service("UserWithdrawalService")
public class UserWithdrawalServiceImpl implements UserWithdrawalService {

	private static final Logger LOGGER = LogManager.getLogger(UserWithdrawalServiceImpl.class);

	@Resource(name = "userWithdrawalMapper")
	private UserWithdrawalMapper userWithdrawalMapper;

	
   	/**
	 * 회원 탈퇴 
	 * @since 2019.07.11
	 * @author 조형욱
	 */
	 
	@Override
	public String actionWithdrawal(Map<String, String> param) throws Exception {
		String result = "";
		
			
			//VO Setting
			UserInfoVO setParam = new UserInfoVO();
			setParam.setUserId(param.get("userId"));
			userWithdrawalMapper.actionWithdrawal(setParam);
			result ="true";
		
			
		return result;
	}


}