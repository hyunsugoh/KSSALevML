package com.levware.ml.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Class Name : MLController.java
 * @Description : M/L Controller Class
 * @Modification Information
 * @ @ 수정일 수정자 수정내용 
 * @ --------- --------- ------------------------------- @
 *   2019.10.08 최초생성
 *
 * @since 2019. 10.08
 * @version 1.0
 * @author LEVWARE
 * @see
 *
 */
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

@Controller
@RequestMapping(value="/ml")
public class MLController {
	
	private static final Logger LOGGER = LogManager.getLogger(MLController.class);
	
	@Resource(name = "mLService")
	private MLService  mLService;
	
	
	@Autowired
    private FileService fileService;
	
	/**
	 * 모델 관리 View
	 * @since 2019.10.08
	 * @author LEVWARE
	 */
	@RequestMapping(value = "/modelMngView", method = RequestMethod.GET)
	public String viewModelMng(ModelMap model,Authentication authentication) throws Exception {
		LOGGER.info("ModelMng page init");
		model.addAttribute("viewName","modelMng");
		return "ml/ModelMng";
	}
	
	/**
	 * 모델 조회
	 * @param param
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/modelList", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String, Object>> modelMngList(@RequestBody Map<String, Object> param,HttpServletResponse response) throws Exception {
		
		try{
			LOGGER.debug("call modelMngList");
	    	List<Map<String, Object>> list = mLService.getModelList(param);
	    	return list;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	
	/**
	 * 모델 생성
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/modelCreate", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> modelMngCreate(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call mode Create");
			//유저정보
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			//모델생성
	    	Map<String,Object> modelCreateResult = mLService.modelCreate(param);
	    	return modelCreateResult;

		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}

	
	
	/**
	 * 모델생성정보 Update
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/modelInfoSave", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> modelInfoSave(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call modelInfoSave");
			Map<String,Object> result = new HashMap<String,Object>();
			//모델예측
			mLService.modelInfoSave(param);
			result.put("result","ok");
			return result;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	
	/**
	 * 데이터조회 -> 모델예측 (Pop) View
	 * @param model
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/modelPredictWithSearchView", method = RequestMethod.GET)
	public String viewModelPredictWithSearchView(ModelMap model,Authentication authentication) throws Exception {
		LOGGER.info("modelPredictWithySearchView page init");
		
		model.addAttribute("UNIT_INFO", mLService.getUnitInfo());
		model.addAttribute("UNIT_DEFAULT_INFO", mLService.getUnitDefaultInfoList());
		
		
		
		//model.addAttribute("viewName","modelPredictWithySearch");
		//model.addAttribute("TRAN_TXT_VAL", mLService.getTransTextVal(new HashMap<String,Object>()));
		//return "ml/pop/ModelPredictWithSearch";
		return "ml/pop/PredictWithMulti";
	}
	
	
	/**
	 * 모델 예측 View
	 * @param model
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/predictWithMultiView", method = RequestMethod.GET )
	public String predictWithFilteringView(ModelMap model,Authentication authentication) throws Exception {
		LOGGER.info("predictWithFiltering page init");
		model.addAttribute("viewName","PredictWithFiltering");
		
		model.addAttribute("UNIT_INFO", mLService.getUnitInfo());
		model.addAttribute("UNIT_DEFAULT_INFO", mLService.getUnitDefaultInfoList());
		List<Map<String,Object>> comboStr = mLService.getComboData();
		model.addAttribute("UNIT_COMBO", comboStr);
		
//		model.addAttribute("GRU0001", mLService.getUnitInfo("GRU0001"));
//		model.addAttribute("GRU0002", mLService.getUnitInfo("GRU0002"));
//		model.addAttribute("GRU0003", mLService.getUnitInfo("GRU0003"));
//		model.addAttribute("GRU0004", mLService.getUnitInfo("GRU0004"));
//		model.addAttribute("GRU0005", mLService.getUnitInfo("GRU0005"));
		
		return "ml/PredictWithMulti";
	}
	
	/**
	 * 모델 test View
	 * @param model
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/predictWithModelView", method = RequestMethod.GET )
	public String predictWithModelView(@RequestParam(value="subprtid") String subprtid, ModelMap model,Authentication authentication) throws Exception {
		LOGGER.info("predictWithModel page init");
		LOGGER.info("서브프로젝트::::: OPEN::",subprtid);

		return "mliframe/PredictWithModel";
	}
	
	/**
	 * Multi 예측 : 저장된 모델을 기반
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/predictWithMulti", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> predictWithFiltering(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call predictWithFiltering");
			Map<String,Object> result = null;
			//모델예측
			//result =  mLService.predictWithFiltering(param);
			result = mLService.predictMultiWithSavedModel(param);
			
			return result;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	/**
	 * Multi 예측 : 저장된 모델을 기반
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/predictMulti1", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> predictMulti1(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call predictMulti1");
			Map<String,Object> result = null;
			//모델예측
			//result =  mLService.predictWithFiltering(param);
			result = mLService.predictMultiWithSavedModel(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	/**
	 * Multi 예측 : 조회된 데이터를 기반
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/predictMulti2", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> predictMulti2(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call predictMulti2");
			Map<String,Object> result = null;
			//모델 트레이닝/예측
			result =  mLService.predictMultiWithSearch(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	/**
	 * 템플릿 엑셀정보 Upload & Insert
	 * @param files
	 * @param menuId
	 * @param fGroupId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/excelFileUploadInsert",method = RequestMethod.POST)
	public Map<String,Object> excelUploadInsert(@RequestParam("files") MultipartFile files,
						 @RequestParam("menuId") String menuId,
						 @RequestParam("fGroupId") String fGroupId) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		// 엑셀파일 Upload
		Map<String,Object> fileUploadResult = fileService.upload(files,menuId,fGroupId);
		// 엑셀정보 Read
		List<Map<String,Object>> excelDataList =  mLService.getExcelUploadInfo(fileUploadResult.get("file_path") +File.separator+fileUploadResult.get("file_name"));
		result.put("fileUploadResult",fileUploadResult);
		result.put("excelDataList",excelDataList);
		return result;
	}
	
	/**
	 * 예측 결과를 업로드된 엑셀에 저장
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/predictInfoToExcelFile",method = RequestMethod.POST)
	public Map<String,Object> predictInfoToExcel(@RequestBody Map<String, Object> param) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		Map<String,Object> fileInfo = (Map<String,Object>)param.get("fileInfo");
		List<String> predictInfo = (List<String>)param.get("predictInfo");
		//System.out.println("fileInfo : " + fileInfo.toString());
		//System.out.println("predictInfo : " + predictInfo.toString());
		String serviceResult = mLService.setPredictInfoToExcelUploadFile(fileInfo, predictInfo,param);
		result.put("result",serviceResult);
		return result;
	}
	/**
	 * 예측 결과를 지정된 엑셀에 저장2 =>엑셀업로드 하지 않았을 경우
	 * 파일업로드가아닌 직접 예측조건 입력시 엑셀 다운로드
	 * @param param
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/predictInfoToExcelFile2",method = RequestMethod.POST)
	public Map<String,Object> predictInfoToExcel2(@RequestBody Map<String, Object> param) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		List<String> predictInfo = (List<String>)param.get("predictInfo");
		List<String> FeatureRangeList = (List<String>)param.get("FeatureRangeList");
		System.out.println("param:::::::"+param);
		System.out.println("FeatureRangeList:::::::"+FeatureRangeList);
		String serviceResult = mLService.setPredictInfoToExcelUploadFile2(param, predictInfo,FeatureRangeList);
		result.put("result",serviceResult);
		return result;
	}
	
	
	/**
	 * 저장된 엑셀파일을 Download
	 * @param fileName
	 * @param filePath
	 * @param fileNameOrg
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping(value = "/predictApplyExcelFileDownload", method = RequestMethod.GET )
	public void excelFileDownload(@RequestParam("file_name") String fileName,
			@RequestParam("file_path") String filePath,
			@RequestParam("file_name_org") String fileNameOrg, HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOGGER.info("predictApplyExcelFileDownload");
		fileService.download(filePath,fileName, fileNameOrg, request,  response);
	}
	//저장된 엑셀파일을 Download - 엑셀업로드가아닌 직접 입력용.
	@RequestMapping(value = "/predictApplyExcelFileDownload2", method = RequestMethod.GET )
	public void excelFileDownload2(@RequestParam("file_name") String fileName,
			 HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOGGER.info("predictApplyExcelFileDownload2");
		fileService.download2(fileName,request,  response);
	}
	
	/**
	 * TB_SEAL_TYPE_T_INFO 테이블 데이터 가져오기
	 */
	@SuppressWarnings("unchecked") //TODO 의미파악하기1 
	@ResponseBody //TODO 의미파악하기2
	@RequestMapping(value = "/getSealTypeInfo1", method = RequestMethod.GET )
	public Map<String, Object> getSealTypeInfo() throws Exception{
		LOGGER.info("get TB_SEAL_TYPE_T_INFO Data");
		Map<String,Object> result = new HashMap<String,Object>();
		List<Map<String, Object>> serviceResult = mLService.getSealTypeInfo();
		System.out.println("getSealTypeInfo:::" + serviceResult);
		result.put("result", serviceResult);
		return result; 
	}
	
	/**
	 * TB_SEAL_TYPE_T_INFO 테이블 데이터 가져오기
	 */
	@SuppressWarnings("unchecked") //TODO 의미파악하기1 
	@ResponseBody //TODO 의미파악하기2
	@RequestMapping(value = "/getSealTypeInfo2", method = RequestMethod.GET )
	public Map<String, Object> getSealTypeInfo2() throws Exception{
		LOGGER.info("get TB_SEAL_TYPE_T_INFO Data");
		Map<String,Object> result = new HashMap<String,Object>();
		List<Map<String, Object>> serviceResult = mLService.getSealTypeInfo2();
		System.out.println("getSealTypeInfo2:::" + serviceResult);
		result.put("result", serviceResult);
		return result; 
	}
	
	
	/**
	 * TB_SEAL_TYPE_T_INFO 테이블 데이터 가져오기
	 */
	@ResponseBody 
	@RequestMapping(value = "/getSealTypeInfo_new", method = RequestMethod.GET )
	public Map<String, Object> getSealTypeInfo_new() throws Exception{
		LOGGER.info("get TB_SEAL_TYPE_P_INFO Data");
		Map<String,Object> result = new HashMap<String,Object>();
		List<Map<String, Object>> serviceResult = mLService.getSealTypeInfo_new();
		System.out.println("getSealTypeInfo2:::" + serviceResult);
		result.put("result", serviceResult);
		return result; 
	}
	
	
	
	/**
	 * TB_ML_MODEL_FEATURE_RANGE 테이블 데이터 가져오기
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody 
	@RequestMapping(value = "/getFeatureRangeList", method = RequestMethod.GET )
	public Map<String, Object> getFeatureRangeList() throws Exception{
		LOGGER.info("get TB_ML_MODEL_FEATURE_RANGE Data");
		Map<String,Object> result = new HashMap<String,Object>();
		List<Map<String, Object>> FeatureRangeList = mLService.getFeatureRangeList();
		System.out.println("getFeatureRangeList:::" + FeatureRangeList);
		result.put("result", FeatureRangeList);
		return result; 
	}
	
	
//	
//	/**
//	 * 조회한 결과를 기준으로 Traingin 및 Predict 수행
//	 * Model을 따로 저장하지 않음.
//	 * @param param
//	 * @param response
//	 * @param authentication
//	 * @return
//	 * @throws Exception
//	 */
//	@RequestMapping(value = "/modelPredictWithSearch", method = RequestMethod.POST )
//	@ResponseBody
//	public Map<String,Object> modelPredictWithSearch(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
//		
//		try{
//			LOGGER.debug("call modelPredictWithySearch");
//			Map<String,Object> result = null;
//			//모델 트레이닝/예측
//			result =  mLService.modelPredictWithSearch(param);
//			return result;
//			
//		}catch(RuntimeException runE){
//			LOGGER.error(runE);
//			response.setCharacterEncoding("UTF-8");
//			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//			response.getWriter().print(runE.getMessage());
//			return null;
//		}catch(Exception e){
//			LOGGER.error(e);
//			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//			 return null;
//		}	
//	}
	
//	@RequestMapping(value = "/modelPredict", method = RequestMethod.POST )
//	@ResponseBody
//	public Map<String,Object> modelPredict(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
//		
//		try{
//			LOGGER.debug("call modelPredict");
//			Map<String,Object> result = null;
//			//모델예측
//			result =  mLService.modelPredict(param);
//			//DB조회
//			result.put("DB_BY_PREDICT", mLService.getDBbyPredictFeauture(param));
//			
//			return result;
//			
//		}catch(RuntimeException runE){
//			LOGGER.error(runE);
//			response.setCharacterEncoding("UTF-8");
//			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//			response.getWriter().print(runE.getMessage());
//			return null;
//		}catch(Exception e){
//			LOGGER.error(e);
//			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//			 return null;
//		}	
//	}
//	
//	/**
//	 * 모델 예측
//	 * @since 2019.10.08
//	 * @author LEVWARE
//	 */
//	@RequestMapping(value = "/modelPredictView", method = RequestMethod.GET)
//	public String viewModelPredict(ModelMap model,Authentication authentication) throws Exception {
//		LOGGER.info("ModelPredict page init");
//		model.addAttribute("viewName","modelPredict");
//		model.addAttribute("TRAN_TXT_VAL", mLService.getTransTextVal(new HashMap<String,Object>()));
//		return "ml/ModelPredict";
//	}
	
//	@RequestMapping(value = "/modelProductList", method = RequestMethod.POST )
//	@ResponseBody
//	public List<String> modelProductList(@RequestBody Map<String, Object> param,HttpServletResponse response) throws Exception {
//		try{
//			LOGGER.debug("call modelProductList");
//    		List<String> list = mLService.getPredictProduct((String)param.get("MODEL_ID")); 
//	    	
//	    	return list;
//		}catch(RuntimeException runE){
//			LOGGER.error(runE);
//			response.setCharacterEncoding("UTF-8");
//			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//			response.getWriter().print(runE.getMessage());
//			return null;
//		}catch(Exception e){
//			LOGGER.error(e);
//			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//			 return null;
//		}	
//	}
	
//	@RequestMapping(value = "/orgToCnv" )
//	@ResponseBody
//	public String main(String[] args) throws Exception{
//		mLService.orgToCnv(new HashMap<String,Object>());
//		return "conversion Ok";
//	}
	
	/**
	 * combo data
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getServiceCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getServiceCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getServiceCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getServiceCombo(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	@RequestMapping(value = "/getEquipmentCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getEquipmentCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getServiceCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getEquipmentCombo(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	@RequestMapping(value = "/getEquipmentTypeCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getEquipmentTypeCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getEquipmentTypeCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getEquipmentTypeCombo();
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	@RequestMapping(value = "/getQuenchTypeCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getQuenchTypeCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getQuenchTypeCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getQuenchTypeCombo();
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	@RequestMapping(value = "/getBrineGbCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getBrineGbCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getBrineGbCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getBrineGbCombo();
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}

	/**
	 * end user, group, service, case combo data
	 * @param param
	 * @param response
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getEndUserCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getEndUserCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getEndUserCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getEndUserCombo();
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	@RequestMapping(value = "/getGroupCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getGroupCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getGroupCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getGroupCombo(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	@RequestMapping(value = "/getServiceGsCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getServiceGsCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getServiceGsCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getServiceGsCombo(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	@RequestMapping(value = "/getCaseCombo", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> getCaseCombo(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call getCaseCombo");
			List<Map<String,Object>> result = null;
			result = mLService.getCaseCombo(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	/**
	 * 이력 조회
	 * @param param
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getMngHistory", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String, Object>> getMngHistory(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("call getMngHistory");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
	    	List<Map<String, Object>> list = mLService.getMngHistory(param);
	    	return list;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	/**
	 * 이력 저장
	 * @param param
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/savHistory", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> savHistory(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {

		try{
			LOGGER.debug("call savHistory");
			Map<String,Object> result = new HashMap<String,Object>();
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			System.out.println(param);
			result.put("result",mLService.savHistory(param));
			return result;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	/**
	 * 이력 수정
	 * @param param
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/editHistory", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> editHistory(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		
		try{
			LOGGER.debug("call editHistory");
			Map<String,Object> result = new HashMap<String,Object>();
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			System.out.println("paramparamparamparamparamparamparam");
			System.out.println(param);
			mLService.editHistory(param);
			result.put("result","ok");
			return result;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	/**
	 * 이력 삭제
	 * @param param
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getDeleteHistory", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> getDeleteHistory(@RequestBody Map<String, Object> param,HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("call getDeleteHistory");
			Map<String,Object> result = new HashMap<String,Object>();
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			System.out.println(param);
			mLService.deleteHistory(param);
			result.put("result","ok");
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	/**
	 * 이력팝업 추전조건 조회
	 * @param param
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getFeatureData", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String, Object>> getFeatureData(@RequestBody Map<String, Object> param,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call getFeatureData");
	    	List<Map<String, Object>> list = mLService.getFeatureData(param);
	    	return list;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
}
