package com.levware.ml.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.ml.service.MLModelService;
import com.levware.ml.service.MLiframeService;

@Controller
@RequestMapping(value="/mlModel")
public class MLModelController {
	
	@Resource(name = "mLiframeService")
	private MLiframeService  mliFService;
	
	@Resource(name = "mLModelService")
	private MLModelService  mlModelService;
	
	@RequestMapping(value="/getModelCSV", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getModelCSV(@RequestBody Map<String, Object> params){
		String subPjtId = params.get("subpjtid").toString();
		String modelUid = params.get("modeluid").toString();
		List<String> header = mliFService.getModelCSVHeader(subPjtId,modelUid);
		List<Map<String,Object>> data = mliFService.getModelCSVData(subPjtId,modelUid);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("header",header);
		rtnDataObj.put("data",data);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/postTrainModel", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> postTrainModel(@RequestBody Map<String, Object> params){
		List<Map<String,Object>> result = mlModelService.postTrainModel(params);
		String subPjtId = params.get("subpjtid").toString();
		String modelUid = params.get("modeluid").toString();
		List<String> header = mliFService.getModelCSVHeader(subPjtId,modelUid);
		List<Map<String,Object>> data = mliFService.getModelCSVData(subPjtId,modelUid);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		if(header.size()>0) {
			rtnDataObj.put("header",header);
			rtnDataObj.put("data",data);
		}
		rtnDataObj.put("msg","success");
		rtnDataObj.put("list",result);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/postPredictModel", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> postPredictModel(@RequestBody Map<String, Object> params){
		String msg = mlModelService.postPredictModel(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/getPredictCSV", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getPredictCSV(@RequestBody Map<String, Object> params){
		String modelUid = params.get("modeluid").toString();
		String subPjtId = params.get("subpjtid").toString();

		List<String> header = mliFService.getPredictCSVHeader(subPjtId, modelUid);
		List<Map<String,Object>> result = mliFService.getPredictCSVData(subPjtId, modelUid);
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("result_header",header);
		rtnDataObj.put("result_data",result);
		return rtnDataObj;
    }
}