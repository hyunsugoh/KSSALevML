package com.levware.common.mappers.repo;



import com.levware.user.service.UserInfoVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;


@Mapper("userWithdrawalMapper")
public interface UserWithdrawalMapper {
	
	/**
	 * 회원 탈퇴 
	 * @since 2019.07.11
	 * @author 조형욱
	 */
	public void actionWithdrawal(UserInfoVO setParam) throws Exception;
	


}