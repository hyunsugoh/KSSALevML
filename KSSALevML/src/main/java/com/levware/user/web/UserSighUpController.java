package com.levware.user.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.user.service.UserInfoVO;
import com.levware.user.service.UserSignUpService;

/**
 * @Class Name : UserSignUpController.java
 * @Description : 사용자 회원가입 컨트롤러
 * @Modification Information
 * @since 2019.03.15
 * @version 1.0
 * @author 강전일
 */
@Controller
public class UserSighUpController {

	public static Logger LOGGER = LogManager.getFormatterLogger(UserSighUpController.class);
	
	@Resource(name = "UserSignUpService")
	UserSignUpService userSignUpService;
	
	/**
	 * 회원가입 아이디 중복 조회
	 * @since 2019.03.15
	 * @author 강전일
	 */
	@RequestMapping(value = "/chkUserId", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<UserInfoVO> getUserIdList(@RequestParam(value="insertId") String userId, HttpServletResponse response) throws Exception {
		try{
			response.setContentType("application/json;charset=UTF-8");
			List<UserInfoVO> ObjectList =  userSignUpService.getUserIdList(userId);
			LOGGER.debug(ObjectList + "ObjectList sign-up controller");
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
	 * 회원가입 보조아이디 중복 조회
	 * @since 2019.03.15
	 * @author 강전일
	 */
	@RequestMapping(value = "/chkSubId", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<UserInfoVO> getSubIdtList(@RequestParam(value="subId") String param, HttpServletResponse response) throws Exception {
		try{
			response.setContentType("application/json;charset=UTF-8");
			List<UserInfoVO> ObjectList =  userSignUpService.getSubIdtList(param);
			LOGGER.debug(ObjectList + "ObjectList sign-up controller");
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
	 * 회원가입 데이터 인서트
	 * @since 2019.03.19
	 * @author 강전일
	 */
	@RequestMapping(value = "/signUpInsert", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public void signUpInsert(@RequestBody Map<String, String> param,  HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug(param + "param sign-up controller");
			userSignUpService.signUpInsert(param);
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	
}
