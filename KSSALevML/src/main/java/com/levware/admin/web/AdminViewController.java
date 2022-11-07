package com.levware.admin.web;

import javax.annotation.Resource;

/**
 * @Class Name : AdminSampleController.java
 * @Description : Admin Sample Controller Class
 * @Modification Information
 * @ @ 수정일 수정자 수정내용 @ --------- --------- ------------------------------- @
 *   2019.02.13 최초생성
 *
 * @author 개발프레임웍크 실행환경 개발팀
 * @since 2019. 02.13
 * @version 1.0
 * @author 최진
 * @see
 *
 * 		Copyright (C) by MOPAS All right reserved.
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

import com.levware.admin.service.BatchDataService;

@Controller
@RequestMapping(value="/admin", method = RequestMethod.GET)
public class AdminViewController {

	@Resource(name= "BatchDataService")
	private BatchDataService batchdataService;

	private static final Logger LOGGER = LogManager.getLogger(AdminViewController.class);

	/**
	 * 객체 관리
	 * @since 2019.04.01
	 * @author 조형욱
	 */
	@RequestMapping(value = "/adminobject", method = RequestMethod.GET)
	public String viewAdminObject(ModelMap model,Authentication authentication) throws Exception {
		LOGGER.debug("AdminObject page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState","Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","adminobject");
		return "admin/AdminObject";
	}

	/**
	 * 객체 관계  관리 화면
	 * @since 2019.03.28
	 * @author 조형욱
	 */

	@RequestMapping(value = "/adminobjectrel", method = RequestMethod.GET)
	public String viewAdminObjectRel(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("AdminObjectRel page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","adminobjectrel");
		return "admin/AdminObjectRel";
	}

	/**
	 * 코드관리 View Method
	 * @since 2019.03.05
	 * @version 1.0
	 * @author 박수연
	 * @param model
	 * @return CodeManagement.jsp
	 * @throws Exception
	 */
	@RequestMapping(value = "/codeManagement", method = RequestMethod.GET)
	public String viewCodeManagement(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("CodeManagement page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","codeManagement");
		return "admin/CodeManagement";
	}

	/**
	 * 객체정보별 조회 조건 관리
	 * @since 2019.04.01
	 * @author 강전일
	 */
	@RequestMapping(value = "/infoCriteria", method = RequestMethod.GET)
	public String viewInfoCriteria(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("infoCriteria page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","infoCriteria");
		return "admin/infoCriteria";
	}

	/**
	 * 회원관리 뷰
	 * @since 2019.03.27
	 * @author 강전일
	 */
	@RequestMapping(value = "/userList", method = RequestMethod.GET)
	public String viewUserList(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("userList page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","userList");
		return "admin/userList";
	}
	/**
	 * 관리자 가입 화면 조회
	 * @since 2019.04.11
	 * @author 조형욱
	 */

	@RequestMapping(value = "/adminsignup", method = RequestMethod.GET)
	public String viewAdminSignUp(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("adminsignup page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","adminsignup");
		return "admin/AdminSignUp";
	}



	/**
	 * 관리자 리스트  화면 조회
	 * @since 2019.04.15
	 * @author 조형욱
	 */

	@RequestMapping(value = "/Manager", method = RequestMethod.GET)
	public String viewManagerList(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("managerlist page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","manager");
		return "admin/Manager";
	}

	/**
	 * 관리자 비번변경 화면 조회
	 * @since 2019.04.11
	 * @author 조형욱
	 */

	@RequestMapping(value = "/adminpwdchange", method = RequestMethod.GET)
	public String viewAdminPwdChange(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("adminpwdchange page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","adminpwdchange");
		return "admin/AdminPwdChange";
	}

	/**
	 * Machine Learning 그룹 정보
	 * @since 2019.11.05
	 * @author 최진
	 */

	@RequestMapping(value = "/adminMLGroupInfo", method = RequestMethod.GET)
	public String viewAdminMLGroupInfo(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("adminMLGroupInfo page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","adminMLGroupInfo");
		return "admin/AdminMLGroupInfo";
	}

	/**
	 * Batch Data 화면
	 * @since 2019.11.12
	 * @author
	 */

	@RequestMapping(value = "/batchData", method = RequestMethod.GET)
	public String viewBatchData(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("batchData page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","adminMLGroupInfo");
		model.addAttribute("cnv1TotalCnt",batchdataService.cnv1DataCount());
		return "admin/batchData";
	}

	/**
	 * 공통코드 관리 view
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@RequestMapping(value = "/commonCode", method = RequestMethod.GET)
	public String viewCommonCode(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("commonCode page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","commonCode");
		return "admin/commonCode";
	}
	
	/**
	 * Rule 표준정보 view
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@RequestMapping(value = "/ruleStd", method = RequestMethod.GET)
	public String viewRuleStd(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("ruleStd page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","ruleStd");
		return "admin/ruleStd";
	}
	
	/**
	 * Rule Graph 
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@RequestMapping(value = "/ruleGraph", method = RequestMethod.GET)
	public String viewRuleGraph(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("rbGraph page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","commonCode");
		return "admin/ruleGraph";
	}

	@RequestMapping(value = "/manageDeploy", method = RequestMethod.GET)
	public String viewManageDeploy(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("managerlist page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","manageDeploy");
		return "admin/manageDeploy";
	}
	
	/**
	 * 가격정보 view
	 * @since 2021.02.24
	 * @author 강전일
	 */
	@RequestMapping(value = "/priceInfo", method = RequestMethod.GET)
	public String viewPriceInfo(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("priceInfo page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","priceInfo");
		return "admin/priceInfo";
	}
}
