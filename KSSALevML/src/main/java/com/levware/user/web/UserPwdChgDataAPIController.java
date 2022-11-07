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

/**
 * @Class Name : UserPwdChgDataAPIController.java
 * @Description : 사용자 비밀번호 초기화/변경용 API 컨트롤러
 * @Modification Information
 * @since 2019.03.13
 * @version 1.0
 * @author 박수연
 */
@Controller
public class UserPwdChgDataAPIController {

	public static Logger LOGGER = LogManager.getFormatterLogger(UserPwdChgDataAPIController.class);
	
	@Resource(name = "UserPwdChgService")
	UserPwdChgService userPwdChgService;
	
	/**
	 * 아이디 검색으로 비밀번호 힌트 조회
	 * @since 2019.03.13
	 * @author 박수연
	 */
	@RequestMapping(value = "/getUserHintList", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<UserInfoVO> getCodeManagementList(@RequestParam(value="insertId") String param, HttpServletResponse response) throws Exception {
		try{
			response.setContentType("application/json;charset=UTF-8");
			LOGGER.info("call getUserHintList");
			List<UserInfoVO> ObjectList =  userPwdChgService.getUserHintList(param);
			return ObjectList;
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
	
	/**
	 * 비밀번호 초기화하고 변경비밀번호 반환
	 * @since 2019.03.14
	 * @author 박수연
	 */
	@RequestMapping(value="/actionInitialize", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String actionInitialize(@RequestParam(value="insertId") String param, HttpServletResponse response) throws Exception {
		try{
			LOGGER.info("call actionInitialize");
			String result = userPwdChgService.actionInitialize(param);
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
	
	/**
	 * 비밀번호 변경
	 * @since 2019.03.14
	 * @author 박수연
	 */
	@RequestMapping(value="/user/api/data/actionPwdChange", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String actionInitialize(@RequestParam Map<String, String> map, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.info("call actionPwdChange"); 
			String result = "";
		
			//로그온 유저 조회
			User user = (User) authentication.getPrincipal();
			String userId = user.getUsername();
			
			//DB비밀번호 조회
			List<UserInfoVO> ObjectList =  userPwdChgService.getUserHintList(userId);
			String dbPwd = ObjectList.get(0).getUserPassward();
			String insertcurrPwd = map.get("insertcurrPwd"); //변경전 현재 비밀번호
	
			//입력 비번 비교
			StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
			Boolean pwdCheck = passwordEncoder.matches(insertcurrPwd, dbPwd);
	
			if(pwdCheck){
				map.put("userId", userId);
				result = userPwdChgService.actionPwdChange(map);
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
