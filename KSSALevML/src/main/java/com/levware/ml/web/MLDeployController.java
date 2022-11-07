package com.levware.ml.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.levware.common.mappers.repo.MLDeployMapper;
import com.levware.ml.service.MLDeployService;

@Controller
public class MLDeployController {
	@Resource(name = "mlDeployMapper")
	private MLDeployMapper mlDpMapper;
	
	@Resource(name = "mlDeployService")
	private MLDeployService  mlDpService;
	
	@RequestMapping(value="/mlDeploy/deploy", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> deploy(@RequestBody Map<String, Object> param){
		String msg = mlDpService.deploy(param);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/mlDeploy/getDeployList", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getDeployList(@RequestBody Map<String, Object> param){
		List<Map<String,Object>> list = mlDpService.getDeployList(param);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("list",list);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/mlDeploy/getDeployListDetail", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getDeployListDetail(@RequestBody Map<String, Object> param){
		List<Map<String,Object>> list = mlDpService.getDeployListDetail(param);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("list",list);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/mlDeploy/getDeployLog", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getDeployLog(@RequestBody Map<String, Object> param){
		List<Map<String,Object>> list = mlDpService.getDeployLog(param);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("list",list);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/mlDeploy/updateDeployListDetail", method = RequestMethod.POST)
	@ResponseBody
	@SuppressWarnings("unchecked")
    public Map<String,Object> updateDeployListDetail(@RequestBody Map<String, Object> param){
		List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
		for(Map<String,Object> dMap : list) {
			dMap.put("userid", param.get("userid").toString());
			mlDpService.updateDeployItemDetail(dMap);
		}
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg","success");
		return rtnDataObj;
	}
	
	@RequestMapping(value="/csv/{subpjtid}/{deployid}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> deployModelCSV(HttpServletRequest request, @PathVariable("subpjtid") String subpjtid, @PathVariable("deployid") String deployid, 
										     @RequestParam("source_file") MultipartFile file, @RequestParam("api_key") String api_key, @RequestParam("userid") String userid) {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("subpjtid", subpjtid);
		param.put("deployid", deployid);
		String ip = request.getRemoteAddr();
		String apiKey = mlDpService.getAPIKey(param);
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		if(apiKey.equals(api_key)) {
			rtnDataObj = mlDpService.deployModelCSV(file,subpjtid,deployid, userid, ip);
		}else {
			rtnDataObj.put("status", "error");
			rtnDataObj.put("message", "API Key Error!");
			Map<String,Object> logParam = new HashMap<String,Object>();
	        logParam.put("DEPLOY_ID",deployid);
	        logParam.put("HOST_IP",ip);
	        logParam.put("STATUS","error");
	        mlDpMapper.insertDeployLog(logParam);
		}
		return rtnDataObj;
	}
	
	@RequestMapping(value="/data/{subpjtid}/{deployid}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> deployModelData(HttpServletRequest request, @RequestBody Map<String,Object> param, @PathVariable("subpjtid") String subpjtid, @PathVariable("deployid") String deployid) {
		param.put("subpjtid", subpjtid);
		param.put("deployid", deployid);
		String ip = request.getRemoteAddr();
		param.put("ip", ip);
		String api_key = param.get("api_key").toString();
		String apiKey = mlDpService.getAPIKey(param);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		if(apiKey.equals(api_key)) {
			rtnDataObj = mlDpService.deployModelData(param);
		}else {
			rtnDataObj.put("status", "error");
			rtnDataObj.put("message", "API Key Error!");
			Map<String,Object> logParam = new HashMap<String,Object>();
	        logParam.put("DEPLOY_ID",deployid);
	        logParam.put("HOST_IP",ip);
	        logParam.put("STATUS","error");
	        mlDpMapper.insertDeployLog(logParam);
		}
		return rtnDataObj;
	}
	
	/*
	@RequestMapping(value="/deployTest", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> deployTest(@RequestBody Map<String, Object> param){
		List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
		Class targetClass;
		try {
			MLDeployService mlDeploy = new MLDeployService();
			targetClass = Class.forName("com.levware.ml.service.MLDeployService");
			Method methods[] = targetClass.getDeclaredMethods();
			for(int i=0;i<methods.length;i++) {
				for(Map<String,Object> map : list) {
					if(methods[i].getName().equals(map.get("mid"))){
						methods[i].invoke(mlDeploy, methods[i].getName());
					}
				}
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		return rtnDataObj;
	}
	*/
}