package com.levware.user.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.UserSignUpMapper;
import com.levware.user.service.UserInfoVO;
import com.levware.user.service.UserSignUpService;

/**
 * UserSignUpServiceImpl
 * 회원가입
 * @author 강전일
 * @since 2019. 3. 15.
 */
@Service("UserSignUpService")
public class UserSignUpServiceImpl implements UserSignUpService {

	private static final Logger LOGGER = LogManager.getLogger(UserSignUpServiceImpl.class);

	@Resource(name = "userSignUpMapper")
	private UserSignUpMapper userSignUpMapper;


	/**
	* 회원가입 아이디 중복 체크
	* @author 강전일
	* @since 2019.3.15
	*/
	@Override
	public List<UserInfoVO> getUserIdList(String userId) throws Exception {
		List<UserInfoVO> objectList = userSignUpMapper.getUserIdList(userId);
		LOGGER.debug(objectList + "objectList sign-up serviceimpl");
		return objectList;
	}
	
	/**
	 * 회원가입 보조아이디 중복 체크
	 * @author 강전일
	 * @since 2019.3.15
	 */
	@Override
	public List<UserInfoVO> getSubIdtList(String subId) throws Exception {
		List<UserInfoVO> objectList = userSignUpMapper.getSubIdtList(subId);
		LOGGER.debug(objectList + "objectList sign-up serviceimpl");
		return objectList;
	}
	
	/**
	 * 회원가입 데이터 인서트 && 비밀번호 암호화
	 * @author 강전일
	 * @since 2019.3.19
	 */
	@Override
	public void signUpInsert(Map<String, String> param) throws Exception {
		//암호화
		String userPw = param.get("USER_PASSWORD");
		StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		
		String encodePwd = passwordEncoder.encode(userPw);
		
		String user_id = (String) param.get("USER_ID");
		
		String sub_id = (String) param.get("SUB_ID");
		
		String user_nm = (String) param.get("USER_NM");

		//String auth_hint = (String) param.get("AUTH_HINT");
		
		//String auth_hint_ans = (String) param.get("AUTH_HINT_ANS");
		
		
		
		LOGGER.debug(param + "param sign-up serviceimpl");
		
		//userSignUpMapper.signUpInsert(encodePwd, user_id, sub_id, auth_hint, auth_hint_ans);
		userSignUpMapper.signUpInsert(encodePwd, user_id, user_nm, sub_id);
	}
	
}
