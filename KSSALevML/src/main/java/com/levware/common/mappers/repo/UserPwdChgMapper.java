package com.levware.common.mappers.repo;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.levware.user.service.UserInfoVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 * 
 * Oracle Repository DataSource Mapper Class
 * 유저 패스워드 초기화 
 * @author 박수연
 * @since 2019. 3. 12.
 */
@Mapper("userPwdChgMapper")
public interface UserPwdChgMapper {
	

	/**
	 *  아이디 검색으로 비밀번호 힌트,번호 조회
	 * @author 박수연
	 * @since 2019.3.19
	 */
	public  List<UserInfoVO> getUserHintList(@Param("userId") String userId) throws Exception;

	/**
	 * 비밀번호 초기화 
	 * @author 박수연
	 * @since 2019.3.14
	 */
	public void actionInitialize(UserInfoVO setParam) throws Exception;
	
	/**
	 * 비밀번호 변경 
	 * @author 박수연
	 * @since 2019.3.20
	 */
	public void actionPwdChange(UserInfoVO setParam) throws Exception;

}
