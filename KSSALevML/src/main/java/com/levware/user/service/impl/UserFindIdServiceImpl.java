package com.levware.user.service.impl;



import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.UserFindIdMapper;
import com.levware.user.service.UserFindIdService;



@Service("UserFindIdService")
public class UserFindIdServiceImpl implements UserFindIdService {


	@Resource(name = "userFindIdMapper")
	private UserFindIdMapper userFindIdMapper;

	
   /**
 
    * 사용자 아이디 찾기
    * @author 조형욱
    * @since 2019. 4. 9.
   */
	@Override
	public String getUserFindId(String insertSubId) throws Exception {

		String result = userFindIdMapper.getUserFindId(insertSubId);
		return result;
	}

}