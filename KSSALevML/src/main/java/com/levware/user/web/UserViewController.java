package com.levware.user.web;
/**
* OLAP Application의 User View Controller
* <p><b>NOTE:</b> 
*  User View Page Controller
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

/**
* OLAP Application의 User View Controller
* <p><b>NOTE:</b> 
*  User가 접근할 수 있는 View를 제어
* @author 최진
* @since 2019.02.28
* @version 1.0
* @see
*
* <pre>
* == 개정이력(Modification Information) ==
*
* 수정일	수정자	수정내용
* -------	--------	---------------------------
* 2019.02.28	최 진	최초 생성
*
* </pre>
*/
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/user", method = RequestMethod.GET)
public class UserViewController {
	
	
	public static Logger LOGGER = LogManager.getLogger(UserViewController.class);
	
	
	@RequestMapping(value="/main", method = RequestMethod.GET)
	public String viewMain(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("main page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState","Logined");
		return "user/main";
	}
	
	
	/**
	* userInfo 사용자ID에 해당하는 사용자명을 조회
	* @return 사용자명
	* @exception MyException
	* @see cmm.ROLE
	* @see SA_CHK_ITEMS
	*/
	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
	public String viewDashBoard(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("Dashboard page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		String goUrl = "user/Dashboard";
		
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState","Logined");		
		return goUrl;
	}
	
	
	

}
