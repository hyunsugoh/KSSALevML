package com.levware.user.web;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
* OLAP Application의 Login View Controller
* <p><b>NOTE:</b> 
*  Login View Page Controller
* @author 최진
* @since 2019.03.04
* @version 1.0
* @see
*
* <pre>
* == 개정이력(Modification Information) ==
*
* 수정일	수정자	수정내용
* -------	--------	---------------------------
* 2019.03.04	최 진	최초 생성
*
* </pre>
*/
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginViewController {
	public static Logger LOGGER = LogManager.getLogger(LoginViewController.class);
	
	

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String viewInit(ModelMap model, Authentication authentication, HttpServletResponse response, HttpSession session) throws Exception {
		LOGGER.debug("OLAP System page init");
		String redirectURL = "/logon.do";
		if(authentication != null){
			String role = authentication.getAuthorities().toString();
			LOGGER.debug("role : "+role);
			if(role.equals("[ROLE_ADMIN]") || role.equals("[ROLE_SUPER]")){
				//redirectURL = "/admin/adminobject.do";
				redirectURL = "/user/main.do";
			}else if(role.equals("[ROLE_USER]")){
				//redirectURL = "/user/dashboard.do";
				redirectURL = "/user/main.do";
			}else{
				LOGGER.debug("role is Null" + role);
			}
			
		}else{
			LOGGER.debug("==================== Authentication is Null =========================");
			
		}
		return "redirect:"+redirectURL;
	}
	
	/**
	 * Login View Mapping Method
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/logon", method = RequestMethod.GET)
	public String logon(Model model) throws Exception {
		LOGGER.info("LoginForm Page init");
		model.addAttribute("authState", "NotLogin");
		return "user/LoginForm";
	}
	
//	/**
//	 * SignUp View Mapping Method
//	 * @param model
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping(value = "/signup", method = RequestMethod.GET)
//	public String signup(Model model) throws Exception {
//		LOGGER.info("SignUp Page init");
//		
//		model.addAttribute("viewName", "signup");
//		return "user/SignUp";
//	}
	
	/**
	 * SignUp View Mapping Method
	 * 회원가입 tiles
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signup(Model model, Authentication authentication) throws Exception {
		LOGGER.info("SignUp Page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName", "signup");
		return "user/SignUp";
	}
	
}
