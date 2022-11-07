package com.levware.user.service;

import java.util.List;
import java.util.Map;





/**
 * UserPwdChgService
 * 유저 패스워드 초기화 
 * @author 박수연
 * @since 2019. 3. 12.
 */

public interface UserSignUpService {
	
	/**
	 * 회원가입 아이디 중복 체크
	 * @author 강전일
	 * @since 2019.3.15
	 */
	public List<UserInfoVO> getUserIdList(String userId) throws Exception;
	
	/**
	 * 회원가입 보조아이디 중복 체크
	 * @author 강전일
	 * @since 2019.3.15
	 */
	public List<UserInfoVO> getSubIdtList(String userId) throws Exception;
	
	/**
	 * 회원가입 데이터 인서트
	 * @author 강전일
	 * @since 2019.3.19
	 */
	public void signUpInsert(Map<String, String> param) throws Exception;
	
}
