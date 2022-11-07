package com.levware.ml.web;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.ml.service.MLModelTestService;

@Controller
@RequestMapping(value="/mlTest")
public class MLModelTestController {

	private static final Logger LOGGER = LogManager.getLogger(MLController.class);
	
	
	@Resource(name="mLModelTestService")
	MLModelTestService mlMTService;
	
	
	@RequestMapping(value="/viewModelTest", method = RequestMethod.GET)
	public String viewModelTest(HttpServletRequest request, Model model){
		return "ml/ModelTest";
	}
	
	
	@ResponseBody
	@RequestMapping(value="/getTableData", method=RequestMethod.POST)
	public Map<String, Object> getTableData(@RequestBody Map<String, Object> params) {
		return mlMTService.getTableData(params);
	}
	
}
