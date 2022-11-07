package com.levware.user.web;



import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.user.service.UserFindIdService;

/**
 * @Class Name : UserFindIdController.java
 * @Description : 사용자 아이디 찾기
 * @since 2019.04.09
 * @version 1.0
 * @author 조형욱
 */
@Controller
public class UserFindIdController {

	public static Logger LOGGER = LogManager.getFormatterLogger(UserFindIdController.class);
	
	@Resource(name = "UserFindIdService")
	UserFindIdService userFindIdService;
	
	
		/**
	 *  아이디 찾기  View
	 *  http://localhost:8080/idFind.do
	 * @author 조형욱
	 * @since 2019.4.09
	 */
	@RequestMapping(value = "/idFind", method = RequestMethod.GET)
	public String viewIdFind(Model model) throws Exception {
		LOGGER.info("idFind page init");
		model.addAttribute("authState", "NotLogin");
		return "user/idFind";
	}
	

	@RequestMapping(value = "/actionFind", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public String getUserFindId(@RequestParam(value="insertSubId") String insertSubId, HttpServletResponse response) throws Exception {
		try{
			response.setContentType("application/json;charset=UTF-8");
			LOGGER.info("call actionFind");
			String result =  userFindIdService.getUserFindId(insertSubId);
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