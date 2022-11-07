package com.levware.ml.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.MLMapper;
import com.levware.ml.algorithm.util.ModelUtil;
import com.levware.ml.service.ModelObjUtilService;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;



@Service("modelObjUtil")
public class ModelObjUtil implements ModelObjUtilService{

	private static boolean initialized = false;//초기화 여부
	//EPC
	private static Classifier classifier_SEAL_TYPE_EPC = null;
	private static Classifier classifier_API_PLAN_EPC = null;
	private static Classifier classifier_CONN_COL_EPC = null;
	//OEM
	private static Classifier classifier_SEAL_TYPE_OEM = null;
	private static Classifier classifier_API_PLAN_OEM = null;
	private static Classifier classifier_CONN_COL_OEM = null;
	
	private static boolean modelCreateIng = false;
	
	@Resource(name = "mLMapper")
	private MLMapper mLMapper;
	
	public Classifier getModel(String sPredictType, String sModelType) throws Exception {
		
		if(!modelCreateIng && !initialized) modelInitialize();
		
		if("EPC".equals(sPredictType) && "SEAL_TYPE".equals(sModelType)) {
			return classifier_SEAL_TYPE_EPC;
		}else if("EPC".equals(sPredictType) && "API_PLAN".equals(sModelType)) {
			return classifier_API_PLAN_EPC;
		}else if("EPC".equals(sPredictType) && "CONN_COL".equals(sModelType)) {
			return classifier_CONN_COL_EPC;
		}else if("OEM".equals(sPredictType) && "SEAL_TYPE".equals(sModelType)) {
			return classifier_SEAL_TYPE_OEM;
		}else if("OEM".equals(sPredictType) && "API_PLAN".equals(sModelType)) {
			return classifier_API_PLAN_OEM;
		}else if("OEM".equals(sPredictType) && "CONN_COL".equals(sModelType)) {
			return classifier_CONN_COL_OEM;
		}else {
			return null;
		}
		
	}
	
	private synchronized void modelInitialize() throws Exception {
		
		Map<String,Object>  modelInfo = null;
		String sModelId = "";
		if(!initialized) {
			
			try {
				System.out.println("=====> initialized start");
				sModelId = getModelId("EPC"); //최근모델정보를 가져온다. ( 단계별 구분 )
				System.out.println("sModelId : " + sModelId);
				
				modelInfo = getModelInfo(sModelId, "SEAL_TYPE");
				//System.out.println("modelInfo");
				classifier_SEAL_TYPE_EPC = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); // Seal Type 모델
				System.out.println("classifier_SEAL_TYPE_EPC end");
				
				modelInfo = getModelInfo(sModelId, "API_PLAN");
				classifier_API_PLAN_EPC = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
				System.out.println("classifier_API_PLAN_EPC end");
				
				modelInfo = getModelInfo(sModelId, "CONN_COL");
				classifier_CONN_COL_EPC = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
				System.out.println("classifier_CONN_COL_EPC end");
				
				sModelId = getModelId("OEM"); //최근모델정보를 가져온다. ( 단계별 구분 )
				modelInfo = getModelInfo(sModelId, "SEAL_TYPE");
				classifier_SEAL_TYPE_OEM = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); // Seal Type 모델
				System.out.println("classifier_SEAL_TYPE_OEM end");
				
				modelInfo = getModelInfo(sModelId, "API_PLAN");
				classifier_API_PLAN_OEM = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
				System.out.println("classifier_API_PLAN_OEM end");
				
				modelInfo = getModelInfo(sModelId, "CONN_COL");
				classifier_CONN_COL_OEM = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
				System.out.println("classifier_CONN_COL_OEM end");
				
				System.out.println("=====> initialized end");
				
				initialized=true;
			}catch(Exception e) {
				classifier_SEAL_TYPE_EPC = null;
				classifier_API_PLAN_EPC = null;
				classifier_CONN_COL_EPC = null;
				classifier_SEAL_TYPE_OEM = null;
				classifier_API_PLAN_OEM = null;
				classifier_CONN_COL_OEM = null;
			}
		}
		
	}

	private String getModelId(String buildType) throws Exception {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("ATTR1", buildType);
		String s = mLMapper.getModelId(param);
		return s;
	}

	private Map<String,Object> getModelInfo(String modelId, String classType) throws Exception {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("MODEL_ID", modelId);
		param.put("CLASS_TYPE", classType);
		List<Map<String,Object>> list = mLMapper.getModelInfo(param);
		
		Map<String,Object> modelInfo= null;
		if (list.size() > 0) {
			modelInfo = list.get(0);
		}
		return modelInfo;
	}
	
	
	public void savedModelRoadInit(Map<String,Object> param) throws Exception {
		initialized = false;
		savedModelRemove(null);
		modelInitialize();
	}	
	
	public void savedModelRemove(Map<String,Object> param) throws Exception {
		classifier_SEAL_TYPE_EPC = null;
		classifier_API_PLAN_EPC = null;
		classifier_CONN_COL_EPC = null;
		classifier_SEAL_TYPE_OEM = null;
		classifier_API_PLAN_OEM = null;
		classifier_CONN_COL_OEM = null;
	}
	
	public void setModelCreateIngStatus(boolean stat) {
		modelCreateIng = stat;
	}
	
	
}
