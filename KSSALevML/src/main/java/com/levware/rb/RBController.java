package com.levware.rb;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.levware.common.FileService;
import com.levware.ml.service.MLService;
import com.levware.rb.service.RBService;

/**
 *   2020.11.03 최초생성
 *
 * @since 2020. 11.03
 * @version 1.0
 * @author LEVWARE
 * @see
 *
 */

@Controller
@RequestMapping(value="/rb")
public class RBController {
	
	private static final Logger LOGGER = LogManager.getLogger(RBController.class);
	
	@Resource(name="rBService")
	private RBService rBService;
	
	@Resource(name="mLService")
	private MLService mlService;
	
	
	/**
	 * Rule 기준 추천
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/predictSealByRuleBased", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> predictSealByRuleBased(
			@RequestBody Map<String, Object> param,
			HttpServletResponse response, 
			Authentication authentication) throws Exception {
		
		rBService.predictSealByRuleBased(param);
		
		return null;
	}
	
	
	/**
	 * Product Grouping 정보를 반환
	 * - 추가구분선택값 추가 : 2020.12.28
	 * - 반환타입 String -> Map
	 * @param param - PRODUCT
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getProductGrp", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String,Object>> getProductGrp(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		System.out.println("getProductGrp call");
		return rBService.getGroupingInfo(param);
	}
	
	/**
	 * Pmum Type 선택박스 데이터 조회
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getPumpType", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String,Object>> getPumpType(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		return rBService.getPumbTypeList();
	}
	
	
	@RequestMapping(value = "/getUnknownApi", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> getUnknownApi(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		return rBService.getunknownApi(param);
	}
	
	
	@RequestMapping(value = "/rbGraph", method = RequestMethod.GET)
	public String viewRuleGraph(ModelMap model,Authentication authentication) throws Exception {
		model.addAttribute("viewName","");
		return "";
	}
	
	@RequestMapping(value = "/getRbGraphSel", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String,Object>> getRbGraphSel(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		return rBService.getRbGraphSelList(param);
	}
	
	@RequestMapping(value = "/getRbGraphResult", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> getRbGraphResult(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		return rBService.getRbGraphResult(param);
	}
	
	
	
	
	
	
	
	// /////////////////////////////////////////////
	// 테스트 //////////////////////////////////////
	// /////////////////////////////////////////////
	
	@RequestMapping(value = "/rbView", method = RequestMethod.GET)
	public String viewModelMng(ModelMap model,Authentication authentication) throws Exception {
		//model.addAttribute("viewName","");
		return "";
	}

	@RequestMapping(value = "/rbTest")
	public String test(ModelMap model) throws Exception {
		rBService.test(null);
		return "";
	}
	
		
}