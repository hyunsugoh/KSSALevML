package com.levware.user.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.*;
import com.levware.user.service.UserInfoVO;
import com.levware.user.service.UserPwdChgService;

/**
 * UserPwdChgServiceImpl
 * 유저 패스워드 초기화 
 * @author 박수연
 * @since 2019. 3. 12.
 */
@Service("UserPwdChgService")
public class UserPwdChgServiceImpl implements UserPwdChgService {

	private static final Logger LOGGER = LogManager.getLogger(UserPwdChgServiceImpl.class);

	@Resource(name = "userPwdChgMapper")
	private UserPwdChgMapper userPwdChgMapper;

	
	/**
	 *  아이디 검색으로 비밀번호 힌트 조회
	 * @author 박수연
	 * @since 2019.3.12
	 */
	@Override
	public List<UserInfoVO> getUserHintList(String userId) throws Exception {

		List<UserInfoVO> objectList = userPwdChgMapper.getUserHintList(userId);
		return objectList;
	}


	/**
	 * 비밀번호 4자리 초기화
	 * @author 박수연
	 * @since 2019.3.12
	 */
	public String actionInitialize(String userId) throws Exception {
		//4자리 초기화 번호 생성
		int p1 = (int)(Math.random() * 10); //0~9
		int p2 = (int)(Math.random() * 10);
		int p3 = (int)(Math.random() * 10);
		int p4 = (int)(Math.random() * 10);
		
		char p5 = (char)((int)(Math.random() * 26)+97);	//a~z
		char p6 = (char)((int)(Math.random() * 26)+97);
		char p7 = (char)((int)(Math.random() * 26)+65);	//A~Z
		char p8= (char)((int)(Math.random() * 26)+65);
		
		String pwd = "@" + p5 +Integer.toString(p1)+ p6 + p7 + Integer.toString(p2) +Integer.toString(p3) + p8 + Integer.toString(p4);
		
		//암호화
		StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		String encodePwd = passwordEncoder.encode(pwd);
		
		//인코딩 체크
		String rawPassword = pwd;	
		Boolean pwdCheck = passwordEncoder.matches(rawPassword, encodePwd);
		LOGGER.debug("check encodePwd: "+pwdCheck);
		
		if(pwdCheck){
			//VO Setting
			UserInfoVO setParam = new UserInfoVO();
			setParam.setUserId(userId);
			setParam.setUserPassward(encodePwd);
			userPwdChgMapper.actionInitialize(setParam);
		}else{
			pwd = "error";
		}
		
		return pwd;
	}
	
	/**
	 * 비밀번호 조회 하고 변경
	 * @author 박수연
	 * @since 2019.3.12
	 */
	@Override
	public String actionPwdChange(Map<String, String> param) throws Exception {
		String result = "";
		
		//비번 인코딩
		StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		String encodePwd = passwordEncoder.encode(param.get("insertnewPwd"));
		
		//인코딩 체크
		Boolean pwdCheck = passwordEncoder.matches(param.get("insertnewPwd"), encodePwd);
			
		if(pwdCheck){
			//VO Setting
			UserInfoVO setParam = new UserInfoVO();
			setParam.setUserId(param.get("userId"));
			setParam.setUserPassward(encodePwd);
			userPwdChgMapper.actionPwdChange(setParam);
			result ="true";
		}else{
			result = "error"; //인코딩에러
		}
			
		return result;
	}


}
