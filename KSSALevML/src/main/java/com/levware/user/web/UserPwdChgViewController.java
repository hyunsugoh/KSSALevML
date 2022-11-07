package com.levware.user.web;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * @Class Name : UserPwdChgViewController.java
 * @Description : 사용자 비밀번호 초기화/변경용 컨트롤러
 * @Modification Information
 * @since 2019.03.13
 * @version 1.0
 * @author 박수연
 */
@Controller
public class UserPwdChgViewController {
	
	public static Logger LOGGER = LogManager.getFormatterLogger(UserPwdChgViewController.class);
	
	/**
	 *  비밀번호 초기화 View
	 *  http://localhost:8080/pwdInitialize.do
	 * @author 박수연
	 * @since 2019.3.13
	 */
	@RequestMapping(value = "/pwdInitialize", method = RequestMethod.GET)
	public String viewPwdInitialize(Model model) throws Exception {
		LOGGER.info("pwdInitialize page init");
		model.addAttribute("authState", "NotLogin");
		return "user/PwdInitialize";
	}
	
	/**
	 *  비밀번호 변경 View
	 *  http://localhost:8080/user/pwdChange.do 후에 대시보드view에 맞게 변경 /pwdChange 
	 * @author 박수연
	 * @since 2019.3.19
	 */
	@RequestMapping(value = "/user/pwdChange", method = RequestMethod.GET)
	public String viewPwdChange(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.info("pwdChange page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState","Logined");
		return "user/PwdChange";
	}
	

}
