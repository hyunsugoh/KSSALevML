package com.levware.rule.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.rule.service.RuleService;

@Controller
@RequestMapping(value="/rule")
public class RuleController {
	@Resource(name = "RuleService")
	private RuleService  ruleService;
	
	/**
	 * 공통코드 관리 view
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@RequestMapping(value = "/commonCode", method = RequestMethod.GET)
	public String viewCommonCode(ModelMap model, Authentication authentication) throws Exception {
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","commonCode");
		return "rule/commonCode";
	}
	
	/**
	 * rule base 규칙 조회
	 * @since 2020.11.04
	 * @author 강전일ruleListSave
	 */
	@RequestMapping(value="/callRuleList")
	@ResponseBody
	public List<Map<String, Object>> getComCodeList(@RequestBody Map<String, Object> params, HttpServletResponse response) throws Exception{
		List<Map<String, Object>> rtnData = ruleService.getRuleList(params);
		return rtnData;
	}
	
	/**
	 * rule base 규칙 저장
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@RequestMapping(value="/ruleListSave", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void saveRuleList(@RequestBody List<Map<String, Object>> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			System.out.println(param);
			ruleService.ruleListSave(param);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	/**
	 * rule base 규칙 조회
	 * @since 2020.11.04
	 * @author 강전일ruleListSave
	 */
	@RequestMapping(value="/callPopRuleList")
	@ResponseBody
	public List<Map<String, Object>> callPopRuleList(@RequestBody Map<String, Object> params, HttpServletResponse response) throws Exception{
		List<Map<String, Object>> rtnData = ruleService.getPopRuleList(params);
		return rtnData;
	}
	
	/**
	 * rule base pop 규칙 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */
	@RequestMapping(value="/ruleListPopSave", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void ruleListPopSave(@RequestBody List<Map<String, Object>> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			System.out.println(param);
			ruleService.ruleListPopSave(param);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}	
	
	/**
	 * Rule 표준정보 view
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@RequestMapping(value = "/ruleStd", method = RequestMethod.GET)
	public String viewRuleStd(ModelMap model, Authentication authentication) throws Exception {
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","ruleStd");
		return "rule/ruleStd";
	}
	
	/**
	 * Rule 표준정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@RequestMapping(value="/callMgList")
	@ResponseBody
	public List<Map<String, Object>> callMgList(@RequestBody Map<String, Object> params, HttpServletResponse response) throws Exception{
		List<Map<String, Object>> rtnData = ruleService.callMgList(params);
		return rtnData;
	}
	
	@RequestMapping(value="/callSowList")
	@ResponseBody
	public List<Map<String, Object>> callSowList(@RequestBody Map<String, Object> params, HttpServletResponse response) throws Exception{
		List<Map<String, Object>> rtnData = ruleService.callSowList(params);
		return rtnData;
	}
	
	@RequestMapping(value="/callStList")
	@ResponseBody
	public List<Map<String, Object>> callStList(@RequestBody Map<String, Object> params, HttpServletResponse response) throws Exception{
		List<Map<String, Object>> rtnData = ruleService.callStList(params);
		return rtnData;
	}
}
