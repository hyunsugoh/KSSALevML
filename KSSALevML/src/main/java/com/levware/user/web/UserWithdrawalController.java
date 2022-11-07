package com.levware.user.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.user.service.UserInfoVO;
import com.levware.user.service.UserPwdChgService;
import com.levware.user.service.UserWithdrawalService;


@Controller
public class UserWithdrawalController {

	public static Logger LOGGER = LogManager.getFormatterLogger(UserWithdrawalController.class);
	
	@Resource(name = "UserPwdChgService")
	UserPwdChgService userPwdChgService;
	
	@Resource(name = "UserWithdrawalService")
	UserWithdrawalService userWithdrawalService;

	/**
	 * 회원 탈퇴
	 * @since 2019.07.11
	 * @author 조형욱
	 */
	@RequestMapping(value="/user/api/data/actionWithdrawal", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String actionWithdrawal(@RequestParam Map<String, String> map, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.info("call actionWithdrawal");
			String result = "";
		
			//로그온 유저 조회
			User user = (User) authentication.getPrincipal();
			String userId = user.getUsername();
			
			//DB비밀번호 조회
			List<UserInfoVO> ObjectList =  userPwdChgService.getUserHintList(userId);
			String dbPwd = ObjectList.get(0).getUserPassward();
			String insertcurrPwd = map.get("insertcurrPwdWithdrawal");
	
			//입력 비번 비교
			StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
			Boolean pwdCheck = passwordEncoder.matches(insertcurrPwd, dbPwd);
	
			if(pwdCheck){
				map.put("userId", userId);
				result = userWithdrawalService.actionWithdrawal(map);
			}else{
				result ="false"; //불일치
			}
			
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}

	
}