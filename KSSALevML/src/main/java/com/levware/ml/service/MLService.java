package com.levware.ml.service;

import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;


public interface MLService {

	public List<Map<String,Object>> getModelList(Map<String,Object> param) throws Exception;
	
	public Map<String, Object> modelCreate(Map<String,Object> param) throws Exception;
	public Map<String, Object> modelInfoSave(Map<String,Object> param) throws Exception;
	
	public Map<String, Object> predictMultiWithSavedModel(Map<String,Object> param) throws Exception;
	public Map<String, Object> predictMultiWithSearch(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> getExcelUploadInfo(String fileInfo) throws Exception;
	public String setPredictInfoToExcelUploadFile(Map<String,Object> fileInfo, List<String> predictInfo, Map<String,Object> param) throws Exception;
	public String setPredictInfoToExcelUploadFile2(Map<String,Object> param, List<String> predictInfo, List<String> FeatureRangeList) throws Exception;
	public void orgToCnv(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> getUnitInfo() throws Exception;
	public List<Map<String,Object>> getUnitDefaultInfoList() throws Exception;
	
	public List<Map<String,Object>> getSealTypeInfo() throws Exception;
	public List<Map<String,Object>> getSealTypeInfo2() throws Exception;
	public List<Map<String,Object>> getSealTypeInfo_new() throws Exception;
	
	public List<Map<String,Object>> getFeatureRangeList() throws Exception;
	
	
	
	public String getGroupingStr(String sOrgVal, List<Map<String,Object>> listGrpInfo, List<Map<String,Object>> listGroupHierInfo) ;
	public String getProductStr(String sOrgVal, List<Map<String,Object>> listGrpInfo, int ord) ;
	
	public Object convWithUnit(String type, ScriptEngine engine, String sCol, 
			Object val, Object unit, String transUnit, 
			List<Map<String,Object>> listUnitChg,	List<Map<String,Object>> listTransTxtVal, List<Map<String,Object>> listSsuChg, 
			String sg) throws Exception;
		
	 public String unitConvBase(List<Map<String,Object>> listUnitCode ,String col) throws Exception;
	 
	 public Map<String,Object> setEmptyDataWithDefaultData(Map<String,Object> data);
	 
	
	//현재 미사용
	public List<Map<String,Object>> getDBbyPredictFeauture(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getTransTextVal(Map<String,Object> param) throws Exception;
	
	//public List<String> getPredictProduct(String modelId) throws Exception;
	//public void mlInit(Map<String,Object> param) throws Exception;
	//public Map<String, Object> modelPredict(Map<String,Object> param) throws Exception;
	//public Map<String,Object>  modelPredictWithSearch(Map<String,Object> param) throws Exception;
	//public Map<String, Object> predictMultiWithFiltering(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> getComboData() throws Exception;
	public List<Map<String,Object>> getServiceCombo(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getEquipmentCombo(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getEquipmentTypeCombo() throws Exception;
	public List<Map<String,Object>> getQuenchTypeCombo() throws Exception;
	public List<Map<String,Object>> getBrineGbCombo() throws Exception;
	public List<Map<String,Object>> getEndUserCombo() throws Exception;
	public List<Map<String,Object>> getGroupCombo(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getServiceGsCombo(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getCaseCombo(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getMngHistory(Map<String, Object> param) throws Exception;
	public String savHistory(Map<String,Object> param) throws Exception;
	public void editHistory(Map<String,Object> param) throws Exception;
	public void deleteHistory(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getFeatureData(Map<String, Object> param) throws Exception;
}
