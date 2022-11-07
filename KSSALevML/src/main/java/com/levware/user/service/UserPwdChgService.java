package com.levware.user.service;

import java.util.List;
import java.util.Map;





/**
 * UserPwdChgService
 * 유저 패스워드 초기화 
 * @author 박수연
 * @since 2019. 3. 12.
 */

public interface UserPwdChgService {
	
	/**
	 *  아이디 검색으로 비밀번호 힌트 조회
	 * @author 박수연
	 * @since 2019.3.13
	 */
	public List<UserInfoVO> getUserHintList(String userId) throws Exception;
	
	/**
	 * 비밀번호 초기화하고 변경비밀번호 반환
	 * @since 2019.03.14
	 * @author 박수연
	 */
	public String actionInitialize(String userId) throws Exception;
	
	/**
	 * 비밀번호 변경
	 * @since 2019.03.19
	 * @author 박수연
	 */
	public String actionPwdChange(Map<String, String> param) throws Exception;
	

	
}
