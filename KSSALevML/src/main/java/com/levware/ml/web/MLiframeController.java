package com.levware.ml.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.levware.ml.service.MLModelService;
import com.levware.ml.service.MLiframeService;

@Controller
@RequestMapping(value="/mliframe")
public class MLiframeController {
	
	@Resource(name = "mLiframeService")
	private MLiframeService  mliFService;
	
	@Resource(name = "mLModelService")
	private MLModelService  mlModelService;
	
    private static String profile=System.getProperty("server.os.profile");
	private static String rootPath=profile.equals("linux")?"/app/levml/csv/":"D:/lev_ml/";
	
	@RequestMapping(value = "/controlView", method = RequestMethod.GET )
	public String controlView(HttpServletRequest request, Model model){
		String modelUid = request.getParameter("modelUid");
		String viewName = request.getParameter("viewName");
		String mid = request.getParameter("mid");
		String fd = mid.substring(0, 2);
		model.addAttribute("modelUid", modelUid);
		model.addAttribute("mid", mid);
		return "mliframe/"+fd+"/control"+viewName;
	}
	
	@RequestMapping(value="/getTableList", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getTableList(@RequestBody Map<String, Object> param){
		List<Map<String,Object>> result = mliFService.getTableList(param);
		List<Map<String,Object>> params = mliFService.getParamList(param);
		List<String> cols = mliFService.getParamColList(param);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("data",result);
		rtnDataObj.put("params",params);
		rtnDataObj.put("cols",cols);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/getTableColInfo", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getTableColInfo(@RequestBody Map<String, Object> params){
		List<Map<String,Object>> result = mliFService.getTableColInfo(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("data",result);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/getTableData", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getTableData(@RequestBody Map<String, Object> params){
		String msg = mliFService.saveTableData(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/getControlInfo", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getCSVColInfo(@RequestBody Map<String, Object> param){
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		if(param.containsKey("gubun")) {
			if(param.get("gubun").equals("ML")){
				List<Map<String,Object>> xml = mlModelService.getResultXML(param);
				rtnDataObj.put("xmldata",xml);
				String subPjtId = param.get("subpjtid").toString();
				String modelUid = param.get("modeluid").toString();
				if(param.containsKey("mid")
						&& (param.get("mid").equals("tr010") || param.get("mid").equals("tr011")
								|| param.get("mid").equals("tr013") || param.get("mid").equals("tr018"))){
					List<String> header = mliFService.getModelCSVHeader(subPjtId,modelUid);
					List<Map<String,Object>> data = mliFService.getModelCSVData(subPjtId,modelUid);
					rtnDataObj.put("header",header);
					rtnDataObj.put("data",data);
				}
			}
			param.put("type", "feature");
			List<Map<String,Object>> fcols = mliFService.getCSVColInfo(param);
			rtnDataObj.put("fcols",fcols);
			param.put("type", "label");
			List<Map<String,Object>> lcols = mliFService.getCSVColInfo(param);
			rtnDataObj.put("lcols",lcols);
		}else {
			List<Map<String,Object>> cols = mliFService.getCSVColInfo(param);
			if(cols.size()==0) {
				param.put("PRYN", "Y");
				cols = mliFService.getCSVColInfo(param);
			}
			rtnDataObj.put("cols",cols);
		}
		List<Map<String,Object>> params = mliFService.getParamList(param);
		rtnDataObj.put("params",params);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/getSQLData", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> getSQLData(@RequestBody Map<String, Object> params){
		String msg = mliFService.saveSQLData(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/removeModelCSV", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> removeModelCSV(@RequestBody Map<String, Object> params){
		String subPjtId = params.get("subpjtid").toString();
		String modelUid = params.get("modeluid").toString();
		String modelType = params.get("modeltype").toString();
		String msg = mliFService.removeModelCSV(subPjtId,modelUid,modelType);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
	}
	
	@ResponseBody
	@RequestMapping(value = "/getParamInfo",method = RequestMethod.POST)
	public Map<String,Object> getParamInfo(@RequestBody Map<String, Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		List<Map<String,Object>> params = mliFService.getParamList(param);
		result.put("params",params);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value = "/getCSVInfo",method = RequestMethod.POST)
	public Map<String,Object> getCSVInfo(@RequestParam("files") MultipartFile file){
		Map<String,Object> result = new HashMap<String,Object>();
		List<Map<String,Object>> data = mliFService.getCSVInfo(file);
		result.put("data",data);
		return result;
	}
	
	@ResponseBody
	@RequestMapping(value = "/getCSVMeta",method = RequestMethod.POST)
	public Map<String,Object> getCSVMeta(@RequestBody Map<String, Object> param){
		List<Map<String,Object>> params = mliFService.getParamList(param);
		List<String> cols = mliFService.getParamColList(param);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		//System.out.println("ROOT PATH : "+rootPath);
		if(params.size()>0) {
			rtnDataObj.put("params",params);
			rtnDataObj.put("cols",cols);
			String filePath = rootPath+param.get("subpjtid")+"/upload/"+params.get(0).get("PARAM_VALUE").toString();
			File file = new File(filePath);
			if(file.exists()) {
				List<Map<String,Object>> data = mliFService.getCSVMeta(file);
				rtnDataObj.put("data",data);
			}
		}
		return rtnDataObj;
	}
	
	@ResponseBody
	@RequestMapping(value = "/uploadCSV",method = RequestMethod.POST)
	public Map<String,Object> uploadCSV(@RequestParam("files") MultipartFile files,@RequestParam("subpjtid") String subPjtId, 
										@RequestParam("modeluid") String modelUid,@RequestParam("list") List<Integer> list,
										@RequestParam("cols") List<String> cols, @RequestParam("userid") String userid) throws Exception {
		String msg = mliFService.uploadCSV(files,subPjtId,modelUid,list,cols,userid);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/loadSplitCSVSrcTgt", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> loadSplitCSVSrcTgt(@RequestBody Map<String, Object> params){
		String soureIds = params.get("sourceids").toString();
		String subPjtId = params.get("subpjtid").toString();
		String modelUid = params.get("modeluid").toString();

		List<String> sHeader = mliFService.getCSVHeader(subPjtId, soureIds);
		List<Map<String,Object>> sResult = mliFService.getCSVData(subPjtId, soureIds);
		
		List<String> tHeader = mliFService.getCSVHeader(subPjtId, modelUid+"_tr");
		List<Map<String,Object>> trResult = mliFService.getCSVData(subPjtId, modelUid+"_tr");
		List<Map<String,Object>> teResult = mliFService.getCSVData(subPjtId, modelUid+"_te");
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("source_header",sHeader);
		rtnDataObj.put("source_data",sResult);
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("train_data",trResult);
		rtnDataObj.put("test_data",teResult);
		return rtnDataObj;
    }
	
	@RequestMapping(value="/loadCSVSrc", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> loadCSVSrc(@RequestBody Map<String, Object> params){
		String soureIds = params.get("sourceids").toString();
		String subPjtId = params.get("subpjtid").toString();

		List<String> sHeader = mliFService.getCSVHeader(subPjtId, soureIds);
		List<Map<String,Object>> sResult = mliFService.getCSVData(subPjtId, soureIds);
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("source_header",sHeader);
		rtnDataObj.put("source_data",sResult);
		return rtnDataObj;
    }
	
	@RequestMapping(value="/loadCSVSrcTgt", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> loadCSVSrcTgT(@RequestBody Map<String, Object> params){
		String soureIds = params.get("sourceids").toString();
		String subPjtId = params.get("subpjtid").toString();
		String modelUid = params.get("modeluid").toString();
		
		List<String> tHeader = new ArrayList<String>();
		List<Map<String,Object>> tResult = new ArrayList<Map<String,Object>>();
		
		List<String> sHeader = mliFService.getCSVHeader(subPjtId, soureIds);
		List<Map<String,Object>> sResult = mliFService.getCSVData(subPjtId, soureIds);
		
		if(sHeader.size()==0) {
			sHeader = mliFService.getPredictCSVHeader(subPjtId, soureIds);
			sResult = mliFService.getPredictCSVData(subPjtId, soureIds);
		}
		
		if(params.containsKey("gubun") && params.get("gubun").equals("PR")) {
			tHeader = mliFService.getPredictCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getPredictCSVData(subPjtId, modelUid);
		}else {
			tHeader = mliFService.getCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getCSVData(subPjtId, modelUid);
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("source_header",sHeader);
		rtnDataObj.put("source_data",sResult);
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("target_data",tResult);
		return rtnDataObj;
    }
	
	@RequestMapping(value="/loadCSVMultiSrcTgt", method = RequestMethod.POST)
	@ResponseBody
	@SuppressWarnings("unchecked")
    public Map<String,Object> loadCSVMultiSrcTgt(@RequestBody Map<String, Object> params){
		List<String> soureIds = (List<String>)params.get("sourceids");
		String subPjtId = params.get("subpjtid").toString();
		String modelUid = params.get("modeluid").toString();

		List<List<String>> sHeaderList = new ArrayList<List<String>>();
		List<List<Map<String,Object>>> sResultList = new ArrayList<List<Map<String,Object>>>(); 
		for(String sourceid : soureIds) {
			sHeaderList.add(mliFService.getCSVHeader(subPjtId, sourceid));
			sResultList.add(mliFService.getCSVData(subPjtId, sourceid));
		}
		
		List<String> tHeader = mliFService.getCSVHeader(subPjtId, modelUid);
		List<Map<String,Object>> tResult = mliFService.getCSVData(subPjtId, modelUid);
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("source_headers",sHeaderList);
		rtnDataObj.put("source_datas",sResultList);
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("target_data",tResult);
		return rtnDataObj;
    }
	
	@RequestMapping(value="/loadCSVTgt", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> loadCSVTgt(@RequestBody Map<String, Object> params){
		String subPjtId = params.get("subpjtid").toString();
		String modelUid = params.get("modeluid").toString();
		
		List<String> tHeader = new ArrayList<String>();
		List<Map<String,Object>> tResult = new ArrayList<Map<String,Object>>();
		if(params.containsKey("gubun") && params.get("gubun").equals("PR")) {
			tHeader = mliFService.getPredictCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getPredictCSVData(subPjtId, modelUid);
		}else {
			tHeader = mliFService.getCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getCSVData(subPjtId, modelUid);
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("target_data",tResult);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/replaceMissingNumber", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> replaceMissingNumber(@RequestBody Map<String, Object> params){
		String msg = mliFService.replaceMissingNumber(params);
		
		List<String> tHeader = new ArrayList<String>();
		List<Map<String,Object>> tResult = new ArrayList<Map<String,Object>>();  
		if(msg.equals("success")) {
			String subPjtId = params.get("subpjtid").toString();
			String modelUid = params.get("modeluid").toString();
			
			tHeader = mliFService.getCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getCSVData(subPjtId, modelUid);
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("target_data",tResult);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/replaceMissingString", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> replaceMissingString(@RequestBody Map<String, Object> params){
		String msg = mliFService.replaceMissingString(params);
		
		List<String> tHeader = new ArrayList<String>();
		List<Map<String,Object>> tResult = new ArrayList<Map<String,Object>>();  
		if(msg.equals("success")) {
			String subPjtId = params.get("subpjtid").toString();
			String modelUid = params.get("modeluid").toString();
			
			tHeader = mliFService.getCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getCSVData(subPjtId, modelUid);
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("target_data",tResult);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/deleteMissingData", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> deleteMissingData(@RequestBody Map<String, Object> params){
		String msg = mliFService.deleteMissingData(params);
		
		List<String> tHeader = new ArrayList<String>();
		List<Map<String,Object>> tResult = new ArrayList<Map<String,Object>>();  
		if(msg.equals("success")) {
			String subPjtId = params.get("subpjtid").toString();
			String modelUid = params.get("modeluid").toString();
			
			tHeader = mliFService.getCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getCSVData(subPjtId, modelUid);
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("target_data",tResult);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/splitData", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> splitData(@RequestBody Map<String, Object> params){
		String msg = mliFService.splitData(params);
		
		List<String> tHeader = new ArrayList<String>();
		List<Map<String,Object>> trResult = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> teResult = new ArrayList<Map<String,Object>>();
		if(msg.equals("success")) {
			String subPjtId = params.get("subpjtid").toString();
			String modelUid = params.get("modeluid").toString();
			
			tHeader = mliFService.getCSVHeader(subPjtId, modelUid+"_tr");
			trResult = mliFService.getCSVData(subPjtId, modelUid+"_tr");
			teResult = mliFService.getCSVData(subPjtId, modelUid+"_te");
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("train_data",trResult);
		rtnDataObj.put("test_data",teResult);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/joinData", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> insertLeftData(@RequestBody Map<String, Object> params){
		List<Map<String,Object>> list = mliFService.joinData(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("list",list);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/distinct", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> Distinct(@RequestBody Map<String, Object> params){
		List<Map<String,Object>> list = mliFService.distinct(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("list",list);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/sort", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> sort(@RequestBody Map<String, Object> params){
		List<Map<String,Object>> list = mliFService.sort(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("list",list);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/stringToColumn", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> stringToColumn(@RequestBody Map<String, Object> params){
		Map<String, Object> rtnDataObj = mliFService.stringToColumn(params);
		return rtnDataObj;
	}
	
	@RequestMapping(value="/normalization", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> normalization(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.normalization(params);
		return map;
	}
	
	@RequestMapping(value="/encoder", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> encoder(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.encoder(params);
		return map;
	}
	
	@RequestMapping(value="/decoder", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> decoder(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.decoder(params);
		return map;
	}
	
	@RequestMapping(value="/removeAbString", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> removeAbString(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.removeAbString(params);
		return map;
	}
	
	@RequestMapping(value="/trim", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> trim(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.trim(params);
		return map;
	}
	
	@RequestMapping(value="/substring", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> substring(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.substring(params);
		return map;
	}
	
	@RequestMapping(value="/correlation", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> correlation(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.correlation(params);
		return map;
	}

	/**
	 * 2020-01-18 이소라 추가
	 * 두 컬럼을 비교하여 동일 여부 컬럼 추가
	 * 
	 * @param params
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/evaluation", method=RequestMethod.POST)
	public Map<String, Object> evaluation(@RequestBody Map<String, Object> params) {
		return mliFService.evaluation(params);
	}
	
	@RequestMapping(value="/concatenate", method = RequestMethod.POST)
	@ResponseBody
    public Map<String,Object> concatenate(@RequestBody Map<String, Object> params){
		Map<String,Object> map = mliFService.concatenate(params);
		return map;
	}
	
	@ResponseBody
	@RequestMapping(value="/convertImageToPixel", method = RequestMethod.POST)
	public Map<String, Object> convertImageToPixel(@RequestBody Map<String, Object> params){
		String subPjtId = (String)params.get("subpjtid");
		String modelUid = (String)params.get("modeluid");
		String userId = (String)params.get("userId");
		List<Map<String, String>> data = (List<Map<String, String>>)params.get("data");
		
		return mliFService.convertImageToPixel(data, subPjtId, modelUid, userId);
	}
	

	@ResponseBody
	@RequestMapping(value = "/uploadImage", method = RequestMethod.POST)
	public Map<String, Object> uploadImage(@RequestParam("files") MultipartFile[] files,
			@RequestParam("subpjtid") String subPjtId, @RequestParam("modeluid") String modelUid,
			@RequestParam("userid") String userId, @RequestParam("list") List<String> list) {

		String msg = mliFService.saveImage(files, subPjtId, modelUid, list, userId);
		
		List<String> tHeader = new ArrayList<String>();
		List<Map<String,Object>> tResult = new ArrayList<Map<String,Object>>();  
		if(msg.equals("success")) {
			tHeader = mliFService.getCSVHeader(subPjtId, modelUid);
			tResult = mliFService.getCSVData(subPjtId, modelUid);
		}
		
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg", msg);
		rtnDataObj.put("target_header",tHeader);
		rtnDataObj.put("target_data",tResult);
		return rtnDataObj;
	}
	
	@ResponseBody
	@RequestMapping(value="/pivot", method=RequestMethod.POST)
	public Map<String, Object> pivot(@RequestBody Map<String, Object> params) {
		return mliFService.pivot(params);
	}
	
	
	@ResponseBody
	@RequestMapping(value="/unpivot", method=RequestMethod.POST)
	public Map<String, Object> unpivot(@RequestBody Map<String, Object> params) {
		return mliFService.unpivot(params);
	}
	
	
	@ResponseBody
	@RequestMapping(value="/capitalize", method=RequestMethod.POST)
	public Map<String, Object> capitalize(@RequestBody Map<String, Object> params) {
		return mliFService.capitalize(params);
	}
	
	
	@ResponseBody
	@RequestMapping(value="/uncapitalize", method=RequestMethod.POST)
	public Map<String, Object> uncapitalize(@RequestBody Map<String, Object> params) {
		return mliFService.uncapitalize(params);
	}
}