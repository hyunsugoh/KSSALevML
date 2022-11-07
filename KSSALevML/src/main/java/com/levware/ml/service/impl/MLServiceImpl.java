package com.levware.ml.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.levware.common.DateUtil;
import com.levware.common.ExcelUtil;
import com.levware.common.StringUtil;
import com.levware.common.mappers.repo.MLMapper;
import com.levware.ml.algorithm.tree.DecisionTreeMain;
import com.levware.ml.algorithm.tree.PreProcessData;
import com.levware.ml.algorithm.util.ModelUtil;
import com.levware.ml.service.MLService;

import egovframework.rte.fdl.property.EgovPropertyService;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

@Service("mLService")
public class MLServiceImpl implements MLService {

	private static final Logger LOGGER = LogManager.getLogger(MLServiceImpl.class);
	
//	private static boolean initialized = false;//초기화 여부
//	private static Classifier classifier_SEAL_TYPE_EPC = null;
//	private static Classifier classifier_API_PLAN_EPC = null;
//	private static Classifier classifier_CONN_COL_EPC = null;
//	private static Classifier classifier_SEAL_TYPE_OEM = null;
//	private static Classifier classifier_API_PLAN_OEM = null;
//	private static Classifier classifier_CONN_COL_OEM = null;

	@Resource(name = "mLMapper")
	private MLMapper mLMapper;
	
	@Resource(name="propertyService")
	protected EgovPropertyService propertyService;
	
	//private ModelObjUtil modelObjUtil
	
	@Resource(name="modelObjUtil")
	protected ModelObjUtil modelObjUtil;
	
	//private String defaultTextNominarValue = "-";
	
	private String[] sFeaturesEPC = new String[]{
			"PUMP_TYPE", "PRODUCT", 
			"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
			"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
			"VISC_NOR","VISC_MIN","VISC_MAX",
			"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
			"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX"
	};

	private String[] sFeaturesOEM =  new String[]{
			"PUMP_TYPE", "PRODUCT",
			"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
			"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
			"VISC_NOR","VISC_MIN","VISC_MAX",
			"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
			"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX",
			"RPM_NOR","RPM_MIN","RPM_MAX","SHAFT_SIZE"
	};
	
	private String[] sFeaturesEPC2 = new String[]{
			"EQUIP_TYPE", "PUMP_TYPE", "PRODUCT", 
			"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
			"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
			"VISC_NOR","VISC_MIN","VISC_MAX",
			"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
			"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX"
	};

	private String[] sFeaturesOEM2 =  new String[]{
			"EQUIP_TYPE", "PUMP_TYPE", "PRODUCT",
			"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
			"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
			"VISC_NOR","VISC_MIN","VISC_MAX",
			"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
			"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX",
			"RPM_NOR","RPM_MIN","RPM_MAX","SHAFT_SIZE"
	};
	
	public List<Map<String,Object>> getModelList(Map<String,Object> param) throws Exception {
		return mLMapper.getModelList(param);
	}
	
	/**
	 * Model Build And Save
	 */
	public Map<String,Object>  modelCreate(Map<String,Object> param) throws Exception {
		
		String modelId ="";
		//메모리에 올라와있는 Model을 제거
		modelObjUtil.savedModelRemove(null);
		//모델생성 상태 처리
		modelObjUtil.setModelCreateIngStatus(true);
		
		Map<String,Object> result = new HashMap<String,Object>();
		
		String[] modelTypes = {"EPC", "OEM"}; //Model Type
		//String[] targets = {"SEAL_TYPE", "SEAL_SIZE", "SEAL_CONFIG", "SEAL_ALL"}; // target 목록   
		String[] targets = {"SEAL_TYPE", "API_PLAN", "CONN_COL"}; // target 목록
		//String[] targets = {"SEAL_ALL"}; // target 목록
		Map<String,List<String>> nominalAttr = null;
		Map<String,Object> opt = null;
		List<Map<String,Object>>  list = null; 
		DecisionTreeMain dtm = new DecisionTreeMain();
		
		// Product Attr. info
		List<String> productAttrDataList = getNominalAttrList("PRODUCT",mLMapper.getProductList(param));
		//System.out.println("productAttrDataList set : " + productAttrDataList.size());	
			
		List<String> pumpTypeAttrDataList = getNominalAttrList("PUMP_TYPE",mLMapper.getPumpTypeList(param));
		//System.out.println("pumpTypeAttrDataList set : " + pumpTypeAttrDataList.size());
		
		List<String> equipTypeAttrDataList = getNominalAttrList("EQUIP_TYPE",mLMapper.getEquipTypeList(param));
		//System.out.println("pumpTypeAttrDataList set : " + pumpTypeAttrDataList.size());
		
		
		// Model Type 별 처리
		for (String modelType : modelTypes ) {
			
			//모델아이디
			String sModelId = ModelUtil.getModelId();
			boolean b_is_save_product = false; // product attr 저장유무
			//boolean isProductApply = (boolean)param.get("product_apply_check");
			
			HashMap<String,Object> modelCreateResult = new HashMap<String,Object>(); 
			modelCreateResult.put("MODEL_ID", sModelId);
			modelId = sModelId;
			modelCreateResult.put("USER_ID", param.get("USER_ID").toString());
		
			// Target 수만큼 ML 모델을 생성한다.
			for (String target : targets) {
				
				long start = System.currentTimeMillis();
	
				//리스트 조회 (trainig data)
				param.put("CLASS_TYPE",target);
				param.put("MODEL_TYPE",modelType);
				list = mLMapper.getModelTrainingList(param); //0324 M_DATA_CNV2 트레이닝 대상 조회.  여기 Feature의 Min Max값을  TB_ML_MODEL_FEATURE_RANGE 로 넣어야하나?
				
				/* Option
				 - nominalAttrs : Map<String,List<String>> - Text형 데이터 
				 - classIdx : int - 클래스 위치
				 - dataType : String - 데이터 구분 ( T : 트레이닝 , P:예측 )
				 - modelSubType : String - 클래스(타겟) 구분
				*/
				opt = new HashMap<String,Object>();
				opt.put("dataType", "T"); // training data
				opt.put("modelSubType", target); // model 하위 구분
				
				nominalAttr = new HashMap<String,List<String>>();
				nominalAttr.put(target, null);
				nominalAttr.put("PRODUCT", productAttrDataList);
				nominalAttr.put("PUMP_TYPE", pumpTypeAttrDataList);
				nominalAttr.put("EQUIP_TYPE", equipTypeAttrDataList);
	
				opt.put("nominalAttrs", nominalAttr); // nominal attribute
				opt.put("classIdx", null); // 클래스인덱스가 없을 경우 마지막 Attribute를 class로 설정
				
				//모델 생성
				Map<String,Object> result_part =  dtm.process("training", list, null, opt);
				result.put(target, result_part);
				
				//System.out.println(result_part.toString());
				
				@SuppressWarnings("unchecked")
				Map<String,Double> eval = (Map<String, Double>) result_part.get("eval");
				
				long end = System.currentTimeMillis(); //프로그램이 끝나는 시점 계산
				
				//모델 생성 결과 저장
				modelCreateResult.put("MODEL_TYPE", "TR"); // 모델타입 : Random Forest
				modelCreateResult.put("MODEL_SUB_TYPE", target);
				modelCreateResult.put("CORRECT_RATE", eval.get("correct_rate"));
				modelCreateResult.put("CORR_COEF", eval.get("correlation_coefficient"));
				modelCreateResult.put("MEAN_ABS_ERR", eval.get("mean_absolute_error"));
				modelCreateResult.put("RMSE", eval.get("root_mean_squared_error"));
				modelCreateResult.put("RAE", eval.get("relative_absolute_error"));
				modelCreateResult.put("RRSE", eval.get("root_relative_squared_error"));
				modelCreateResult.put("TRAINING_CNT", eval.get("training_cnt"));
				modelCreateResult.put("TEST_CNT", eval.get("test_cnt"));
				modelCreateResult.put("MODEL_LOC", result_part.get("model_path"));
				modelCreateResult.put("REMARKS", "");
				modelCreateResult.put("TITLE", "전체데이터 기준_"+ DateUtil.todayYYYYMMDD());
				modelCreateResult.put("PROCESS_TIME",( end - start )/1000.0);
				modelCreateResult.put("ATTR1", modelType); // Build Type A
				modelCreateResult.put("ATTR2","");// 추가옵션정보 2 - Product Apply
				modelCreateResult.put("ATTR3", "");
				mLMapper.setModelInfo(modelCreateResult); 
				
				//모델 생성 - 클래스 정보 저장 - 현재모델 기준의 클래스정보를 저장한다.
				modelCreateResult.put("ATTR_TYPE", target+"_CLASS"); //Attr Type을 추가
				modelCreateResult.put("CLASS_TYPE",target); //class label 추가
				mLMapper.setModelAttrClassInfo(modelCreateResult); //0324 TB_ML_MODEL_ATTR  - INSERT.
				
				//if(!b_is_save_product && isProductApply) {
				if(!b_is_save_product ) {
					
					//Product Attr Type을 추가 - Nominal
					modelCreateResult.put("ATTR_TYPE", "PRODUCT"); 
					mLMapper.setModelAttrProductInfo(modelCreateResult);
					
					//Pump Type Attr Type을 추가 - Nominal
					modelCreateResult.put("ATTR_TYPE", "PUMP_TYPE");
					mLMapper.setModelAttrPumpTypeInfo(modelCreateResult);
					
					//Equip Type Attr Type을 추가 - Nominal
					modelCreateResult.put("ATTR_TYPE", "EQUIP_TYPE");
					mLMapper.setModelAttrEquipTypeInfo(modelCreateResult);
					
					//product group code 저장
					modelCreateResult.put("ATTR_TYPE", "PRODUCT_GRP_CD");
					modelCreateResult.put("GRP_CODE", "product");
					mLMapper.setModelAttrGrpCodeInfo(modelCreateResult);
					
					//pump type group code 저장
					modelCreateResult.put("ATTR_TYPE", "PUMP_TYPE_GRP_CD");
					modelCreateResult.put("GRP_CODE", "pumpType");
					mLMapper.setModelAttrGrpCodeInfo(modelCreateResult);
					
					//group hierarchy 저장
					modelCreateResult.put("ATTR_TYPE", "PRODUCT_HIER");
					mLMapper.setModelAttrProductHierInfo(modelCreateResult);
					
					b_is_save_product = true;
				}
				
			}
		}
		//0324 TB_ML_MODEL_FEATURE_RANGE에 Feature MIN,MAX값 넣어주기.
		HashMap<String,Object> insertFeatureList = new HashMap<String,Object>(); 
		System.out.println("MODEL_ID:::"+modelId); //모델명
		String[] FeatureLists = {"TEMP_NOR","TEMP_MIN","TEMP_MAX","SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX","VISC_NOR","VISC_MIN","VISC_MAX","VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
								"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX","RPM_NOR","RPM_MIN","RPM_MAX","SHAFT_SIZE"}; //FeatureList 
		
		
		for(String FeatureList : FeatureLists){ //향상된 for문 :  TB_ML_MODEL_FEATURE_RANGE 테이블에 Model 생성시 FeatureList들의 MIN, MAX 값을 넣어준다.
			insertFeatureList.put("MODEL_ID", modelId);
			insertFeatureList.put("FEATURE_COL", FeatureList);
			insertFeatureList.put("USER_ID", param.get("USER_ID").toString());
			mLMapper.insertFeatureRange(insertFeatureList);
		}
		
		
		//모델 생성 후 Model을 메모리에 Load
		modelObjUtil.savedModelRoadInit(null);
		
		modelObjUtil.setModelCreateIngStatus(false);
		
		return result;
	}
	
	/**
	 * 저장된 모델을 기반으로 예측
	 * 엑셀 다중 예측 처리
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object>  predictMultiWithSavedModel(Map<String,Object> param) throws Exception {
		
		Map<String,Object> result = new HashMap<String,Object>(); // 전체결과
		Map<String,Object> result_predict_item = null;// 아이템별 처리결과
		List<Map<String,Object>> result_predict_items = new ArrayList<Map<String,Object>>();// 아이템별 처리결과스
		String sResultMsg = "";
		
		//List<Map<String,Object>> searchList = (ArrayList<Map<String,Object>>)param.get("search_list"); //데이터 조회화면 List
		//System.out.println("size :  " + searchList.size());
		
		List<Map<String,Object>> predictList = null;
		LinkedHashMap<String, Object> predict_param = null;
		//Map<String,Object> predict_param_full = (Map<String,Object>)param.get("predict_item");
		
		//예측 항목 (항목별로 모델 예측실행)
		//String[] targets = {"SEAL_TYPE", "SEAL_SIZE", "SEAL_CONFIG", "SEAL_ALL"}; // target 목록   
		List<String> targets = new ArrayList<String>();
		if((boolean)param.get("target1_check")) targets.add("SEAL_TYPE");
		if((boolean)param.get("target2_check")) targets.add("API_PLAN");
		if((boolean)param.get("target3_check")) targets.add("CONN_COL");
		System.out.println("target : " + targets.toString());
		
		Map<String,List<String>> nominalAttr = null;
		Map<String,Object> opt = null;

		//"PUMP_TYPE", "PRODUCT",
		String[] sFeatures = null;
		
		// Feature 목록
		if ("EPC".equals((String)param.get("predict_type"))) { // EPC
			sFeatures = sFeaturesEPC;
		}else { // OEM
			sFeatures = sFeaturesOEM;
		}
		
		String sModelId = getModelId(String.valueOf(param.get("predict_type"))); //최근모델정보를 가져온다. ( 단계별 구분 )
		String sPredictType = String.valueOf(param.get("predict_type")); // 예측타입(단계)
		
		List<String> predictClassSealTypeNominalList = getPredictTargetClass(sModelId, "SEAL_TYPE"); //모델생성당시 클래스 정보 Seal Type
		List<String> predictClassAPIPlanNominalList = getPredictTargetClass(sModelId, "API_PLAN"); //모델생성당시 클래스 정보 API Plan
		List<String> predictClassConnColNominalList = getPredictTargetClass(sModelId, "CONN_COL"); //모델생성당시 클래스 정보  연결정보
		
		List<String> predictProductNominalList = getPredictProduct(sModelId);  //모델생성당시 Product 정보
		List<String> predictEquipTypeNominalList =  getPredictEquipType(sModelId);  //모델생성당시 Equip Type 정보
		List<String> predictPumpTypeNominalList =  getPredictPumpType(sModelId);  //모델생성당시 Pump Type 정보
		
//		Map<String,Object> modelInfo_SEAL_TYPE = null;
//		Map<String,Object> modelInfo_API_PLAN = null;
//		Map<String,Object> modelInfo_CONN_COL = null;
		Classifier classifier_SEAL_TYPE = null;
		Classifier classifier_API_PLAN = null;
		Classifier classifier_CONN_COL = null;
		
//		if((boolean)param.get("target1_check")) {
//			modelInfo_SEAL_TYPE = getModelInfo(sModelId, "SEAL_TYPE");
//			classifier_SEAL_TYPE = (RandomForest)ModelUtil.modelLoad((String)modelInfo_SEAL_TYPE.get("MODEL_LOC")); // Seal Type 모델
//		}
//		if((boolean)param.get("target2_check")) {
//			modelInfo_API_PLAN = getModelInfo(sModelId, "API_PLAN");
//			classifier_API_PLAN = (RandomForest)ModelUtil.modelLoad((String)modelInfo_API_PLAN.get("MODEL_LOC")); //Api Plan 모델
//		}
//		if((boolean)param.get("target3_check")) {
//			modelInfo_CONN_COL = getModelInfo(sModelId, "CONN_COL");
//			classifier_CONN_COL = (RandomForest)ModelUtil.modelLoad((String)modelInfo_CONN_COL.get("MODEL_LOC")); // 복합 모델
//		}
		
		// Model 체크
//		modelInitialize();
		
//		if((boolean)param.get("target1_check")) {
//			if("EPC".equals(sPredictType)) {
//				classifier_SEAL_TYPE = classifier_SEAL_TYPE_EPC;
//			}else {
//				classifier_SEAL_TYPE = classifier_SEAL_TYPE_OEM;
//			}
//		}
//		if((boolean)param.get("target2_check")) {
//			if("EPC".equals(sPredictType)) {
//				classifier_API_PLAN = classifier_API_PLAN_EPC;
//			}else {
//				classifier_API_PLAN = classifier_API_PLAN_OEM;
//			}
//		}
//		if((boolean)param.get("target3_check")) {
//			if("EPC".equals(sPredictType)) {
//				classifier_CONN_COL = classifier_CONN_COL_EPC;
//			}else {
//				classifier_CONN_COL = classifier_CONN_COL_OEM;
//			}
//		}
		
		if((boolean)param.get("target1_check")) {
			classifier_SEAL_TYPE = modelObjUtil.getModel(sPredictType, "SEAL_TYPE") ;
		}
		if((boolean)param.get("target2_check")) {
			//classifier_SEAL_TYPE = mou.getModel(sPredictType, "API_PLAN") ;
			classifier_API_PLAN =modelObjUtil.getModel(sPredictType, "API_PLAN") ;
		}
		if((boolean)param.get("target3_check")) {
			//classifier_SEAL_TYPE = mou.getModel(sPredictType, "COLL_CON") ;
			classifier_CONN_COL = modelObjUtil.getModel(sPredictType, "CONN_COL") ;
		}
		
		if (  ((boolean)param.get("target1_check") && classifier_SEAL_TYPE == null) ||
			((boolean)param.get("target2_check") &&	classifier_API_PLAN == null) ||
			((boolean)param.get("target3_check") && classifier_CONN_COL == null)
				) {
			
			Map<String,Object> eMsg = new HashMap<String,Object>();
			eMsg.put("ERR_MSG","Model 정보가 변경중이거나 사용할 수 없는 상태입니다.");
			result.put("RESULT",eMsg);
			return result;
		}
		
		// ProductGroup 정보를 가져온다
		Map<String,Object> modelAttrParam = new HashMap<String,Object>();
		modelAttrParam.put("MODEL_ID",sModelId);
		modelAttrParam.put("ATTR_TYPE","PRODUCT_GRP_CD");
		List<Map<String,Object>> modelProductGroupCodeList = mLMapper.getModelGroupCodeList(modelAttrParam);		
		
		// Pump Type Group 정보를 가져온다
		//modelAttrParam.put("MODEL_ID",sModelId);
		modelAttrParam.put("ATTR_TYPE","PUMP_TYPE_GRP_CD");
		//List<Map<String,Object>> pumpTypeGroupList = mLMapper.getModelGroupCodeList(modelAttrParam);
		//List<Map<String,Object>> pumpTypeGroupList = mLMapper.getPumpTypeGroupList(new HashMap<String,Object>());
		List<Map<String,Object>> modelPumpTypeGroupCodeList = mLMapper.getModelGroupCodeList(modelAttrParam);
		
		// Grouping Hierarchy 정보를 가져온다.
		modelAttrParam.put("ATTR_TYPE","PRODUCT_HIER");
		List<Map<String,Object>> modelProductGroupCodeHierList = mLMapper.getModelProductGroupCodeHierList(modelAttrParam);
		
		
		// -----------------------------
		// 단위변환처리 정보
		// -----------------------------
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		// 단위코드정보
		List<Map<String,Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();
		// 단위변환정보
		List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
		// Text 입력값 변환정보
		List<Map<String,Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();
		// SSU 변환 정보 (Visc 용)
		List<Map<String,Object>> listSsuChg = mLMapper.getSsuChglInfo();
		String aBaseUnit = ""; // 항목별 기준단위
		String sSpecGravityNor =""; // 비중
		String sSpecGravityMin ="";
		String sSpecGravityMax ="";
		
		// ==================================
		// 아이템 별 예측 처리 ~~~ , 전체 예측 아이템 목록
		// ==================================
		List<Map<String,Object>> predict_param_list = (List<Map<String,Object>>)param.get("predict_list"); 
		
		String sNo = "";
		for (Map<String,Object> predict_param_full : predict_param_list) {
			
			result_predict_item = new HashMap<String,Object>(); 
			
			// 아이템 No
			sNo = String.valueOf(predict_param_full.get("NO"));
			System.out.println("NO : " + sNo);
			
			// 빈값 처리
			predict_param_full = setEmptyDataWithDefaultData(predict_param_full);
			
			// ------------------
			// 단위 환산
			// ------------------
			sSpecGravityNor =  String.valueOf(predict_param_full.get("SPEC_GRAVITY_NOR"));
			sSpecGravityMin =  String.valueOf(predict_param_full.get("SPEC_GRAVITY_MIN"));
			sSpecGravityMax = String.valueOf(predict_param_full.get("SPEC_GRAVITY_MAX"));		
			
			// SHAFT_SIZE
			/*aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE");
			predict_param_full.put("SHAFT_SIZE", convWithUnit("3",engine, "SHAFT_SIZE", predict_param_full.get("SHAFT_SIZE"), predict_param_full.get("SHAFT_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));*/
						
			// 온도
			aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR");
			predict_param_full.put("TEMP_NOR",  convWithUnit("3",engine, "TEMP_NOR", predict_param_full.get("TEMP_NOR"), predict_param_full.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("TEMP_MIN",  convWithUnit("3",engine, "TEMP_MIN", predict_param_full.get("TEMP_MIN"), predict_param_full.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("TEMP_MAX", convWithUnit("3",engine, "TEMP_MAX", predict_param_full.get("TEMP_MAX"), predict_param_full.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
						
			// 점도 : Viscosity
			aBaseUnit = unitConvBase(listUnitCode, "VISC_NOR");
			predict_param_full.put("VISC_NOR", convWithUnit("3",engine,"VISC_NOR", predict_param_full.get("VISC_NOR"), predict_param_full.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityNor));
			predict_param_full.put("VISC_MIN", convWithUnit("3",engine,"VISC_MIN", predict_param_full.get("VISC_MIN"), predict_param_full.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMin));
			predict_param_full.put("VISC_MAX", convWithUnit("3",engine,"VISC_MAX", predict_param_full.get("VISC_MAX"), predict_param_full.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMax));
						
			// 증기압력 : Vapor Pressure
			aBaseUnit = unitConvBase(listUnitCode, "VAP_PRES_NOR");
			predict_param_full.put("VAP_PRES_NOR", convWithUnit("3",engine,"VAP_PRES_NOR", predict_param_full.get("VAP_PRES_NOR"), predict_param_full.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("VAP_PRES_MIN", convWithUnit("3",engine,"VAP_PRES_MIN", predict_param_full.get("VAP_PRES_MIN"), predict_param_full.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("VAP_PRES_MAX", convWithUnit("3",engine,"VAP_PRES_MAX", predict_param_full.get("VAP_PRES_MAX"), predict_param_full.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
						
			// 씰챔버압력 : Seal Chamber Pressure
			aBaseUnit = unitConvBase(listUnitCode, "SEAL_CHAM_NOR");
			predict_param_full.put("SEAL_CHAM_NOR", convWithUnit("3",engine,"SEAL_CHAM_NOR", predict_param_full.get("SEAL_CHAM_NOR"), predict_param_full.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("SEAL_CHAM_MIN", convWithUnit("3",engine,"SEAL_CHAM_MIN", predict_param_full.get("SEAL_CHAM_MIN"), predict_param_full.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("SEAL_CHAM_MAX", convWithUnit("3",engine,"SEAL_CHAM_MAX", predict_param_full.get("SEAL_CHAM_MAX"), predict_param_full.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
						
			// RPM - Shaft Speed
			aBaseUnit = unitConvBase(listUnitCode, "RPM_NOR");
			predict_param_full.put("RPM_NOR", convWithUnit("3",engine,"RPM_NOR", predict_param_full.get("RPM_NOR"), predict_param_full.get("RPM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("RPM_MIN", convWithUnit("3",engine,"RPM_MIN", predict_param_full.get("RPM_MIN"), predict_param_full.get("RPM_UNIT"),  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("RPM_MAX", convWithUnit("3",engine,"RPM_MAX", predict_param_full.get("RPM_MAX"), predict_param_full.get("RPM_UNIT"),  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			
			// Shaft Size
			aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE");
			predict_param_full.put("SHAFT_SIZE", convWithUnit("3",engine, "SHAFT_SIZE", predict_param_full.get("SHAFT_SIZE"), predict_param_full.get("SHAFT_SIZE_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			
			
			// Text 입력값을 숫자로 변환 처리
			//predict_param_full = transTxtVal(listTransTxtVal, predict_param_full);
			
			System.out.println("predict_param_full : " + predict_param_full.toString());
			
			// Product 조건
			//param.put("PRODUCT", predict_param_full.get("PRODUCT") );
			String applyProductGrpString = "";
			String sThisProduct = predict_param_full.get("PRODUCT")==null?"":predict_param_full.get("PRODUCT").toString();
			
			applyProductGrpString = getGroupingStr(sThisProduct,modelProductGroupCodeList, modelProductGroupCodeHierList);
			System.out.println("적용 PRODUCT GRP : "+ applyProductGrpString);
			
			String applyPumpTypeGrpString = "";
			String sThisPumpType = predict_param_full.get("PUMP_TYPE")==null?"":predict_param_full.get("PUMP_TYPE").toString();
			
			applyPumpTypeGrpString = getGroupingStr(sThisPumpType,modelPumpTypeGroupCodeList, null);
			System.out.println("적용 PUMP TYPE GRP : "+ applyPumpTypeGrpString);
			
			boolean isProductGrpInfo = true;
			if (applyProductGrpString.length() > 0 
					&& applyPumpTypeGrpString.length() > 0
					&& !"-".equals(applyProductGrpString)
					&& !"-".equals(applyPumpTypeGrpString)
					) { // 그룹핑 정보가 있을 경우 예측처리
				isProductGrpInfo = true;
			}else { // 처리하지 않음.
				isProductGrpInfo = false;
			}
			
			//예측인자의 Nominal value가 Nominal 리스트에 없는 경우
			if(!predictProductNominalList.contains(applyProductGrpString)) {
				isProductGrpInfo = false;
			}
			if(!predictPumpTypeNominalList.contains(applyPumpTypeGrpString)) {
				isProductGrpInfo = false;
			}
			
			
			Map<String,Object> predicts = null;
			
			if(isProductGrpInfo) { //product 정보가 있을 경우
				
				predicts = new HashMap<String,Object>();
				
				for (String target : targets.toArray(new String[targets.size()])) { // Target Class 별 예측
					
//					Map<String,Object> modelInfo = null;
//					
//					if("SEAL_TYPE".equals(target)) {
//						modelInfo = modelInfo_SEAL_TYPE;
//					}else if ("API_PLAN".equals(target)) {
//						modelInfo = modelInfo_API_PLAN;
//					}else {
//						modelInfo = modelInfo_CONN_COL;
//					}
//					
//					if (modelInfo == null) continue;
					
					//예측 Feature base
					predict_param = new LinkedHashMap<String,Object>();
					predict_param.put("EQUIP_TYPE", String.valueOf(param.get("equip_type"))); // EQUIP TYPE은 개별로 입력되지 않으므로 여기서 일괄 적용되게 함.
					for(String sf : sFeatures) {
						if("PRODUCT".equals(sf)) { // 펌프타입조건 추가
							predict_param.put(sf,applyProductGrpString);
						}else if ("PUMP_TYPE".equals(sf)) {
							predict_param.put("PUMP_TYPE", applyPumpTypeGrpString ); // 펌프타입조건 추가
						}else {
							predict_param.put(sf,predict_param_full.get(sf));
						}
					}
					
					//Map<String,Object> data = (LinkedHashMap<String,Object>)predict_param.clone();
					predict_param.put(target, "");
					predictList = new ArrayList<Map<String,Object>>();
					predictList.add(predict_param);
					
					opt = new HashMap<String,Object>();
					opt.put("modelSubType", target);
					opt.put("dataType", "P"); // Predict data
					//opt.put("modelPath", modelInfo.get("MODEL_LOC")); //모델파일경로
					
					nominalAttr = new HashMap<String,List<String>>();
					//모델생성당시 클래스 정보
					if(target.equals("SEAL_TYPE")) {
						nominalAttr.put(target, predictClassSealTypeNominalList); 
					}else if(target.equals("API_PLAN")) {
						nominalAttr.put(target, predictClassAPIPlanNominalList); 
					}else if(target.equals("CONN_COL")) {
						nominalAttr.put(target, predictClassConnColNominalList); 
					}
					
					//nominalAttr.put(target, getPredictTargetClass(sModelId, target)); //모델생성당시 클래스 정보
					nominalAttr.put("PRODUCT", predictProductNominalList);  //모델생성당시 Product 정보
					nominalAttr.put("EQUIP_TYPE", predictEquipTypeNominalList);  //모델생성당시 Equip Type 정보
					nominalAttr.put("PUMP_TYPE", predictPumpTypeNominalList);  //모델생성당시 Pump Type 정보
					
					opt.put("nominalAttrs", nominalAttr); // nominal attribute
					opt.put("classIdx", null); // 클래스인덱스가 없을 경우 마지막 Attribute를 class로 설정
					
					//System.out.println("Option : " + opt.toString());
					
					
					//모델 예측
					Classifier classifier = null;
					if(target.equals("SEAL_TYPE")) {
						classifier = classifier_SEAL_TYPE;
					}else if(target.equals("API_PLAN")) {
						classifier = classifier_API_PLAN; 
					}else if(target.equals("CONN_COL")) {
						classifier = classifier_CONN_COL; 
					}
					
					Map<String,Object> predict = new HashMap<String,Object>();;
					if (classifier != null) {
						//predict = dtm.process("predict", null, predictList, opt);
						
						// 예측데이터 Set
						Instances predictDataSet = PreProcessData.convListToArff(predictList, opt);

						//System.out.println("predictDataSet.size() : " + predictDataSet.size());
						//System.out.println(sNo + ":"+ target + " : " + "predict : data set end "  );
						
						// 예측
						double[] prediction = classifier.distributionForInstance(predictDataSet.get(0));

						//System.out.println(sNo + ":"+ target + " : " + "predict : predict : " + prediction.length);
						
						Map<String,Double> _predict = new HashMap<String,Double>();
				        for(int i=0; i<prediction.length; i=i+1){
				        	_predict.put(predictDataSet.classAttribute().value(i).toString(), prediction[i]);
				        	//System.out.println(predictDataSet.classAttribute().value(i).toString() +"|"+ prediction[i]);
				        }
				        Map<String,Double> p_m = ModelUtil.getSortMap(_predict); // 정렬
				        List<Map<String,Object>> p_l = new ArrayList<Map<String,Object>>();
				        Map<String,Object> p_l_m = null;
				        int i=1;
				        for( String key : p_m.keySet()) {
				        	p_l_m = new HashMap<String,Object>();
				        	p_l_m.put("NO", i++);
				        	p_l_m.put("CLASS", key);
				        	p_l_m.put("PROB", Math.round(p_m.get(key)*10000)/100.0);
				        	p_l.add(p_l_m);
				        	if(i==6) break; // 상위 5건만 유지
				        }
				        predict.put("predict_result", p_l);
						
					}else {
						predict = new HashMap<String,Object>();
					}
					
					predicts.put(target,predict.get("predict_result"));
					
				} // end for (String target : targets.toArray(new String[targets.size()]))
				
				if(!"".equals(sResultMsg)) {
					sResultMsg = " / " + sResultMsg;
				}
				result_predict_item.put("predict_idx",sNo);
				result_predict_item.put("predict_msg","complete" + sResultMsg); 
				result_predict_item.put("RESULT",predicts);
				result_predict_item.put("ProductGroupInfo",applyProductGrpString); //product group info
			}else { // Product Grouping 정보가 없을 경우
				
				result_predict_item.put("predict_idx", sNo);
				result_predict_item.put("predict_msg","complete" + sResultMsg); 
				//result_predict_item.put("predict_msg","조회된 데이터가 없습니다."); 
				result_predict_item.put("RESULT","");
				
			}

			//컨버전된 피처값 활용을 위해 다시 넘김
			result_predict_item.put("param_cnv", predict_param_full );
			
			// 아이템별 결과 저장
			result_predict_items.add(result_predict_item);
		
		} // end for (Map<String,Object> predict_param_full : predict_param_list)

		result.put("RESULT",result_predict_items);
		
		
		return result;
	}
	
	/****
	 * 데이터 조회 - 머신러닝예측
	 */
	@SuppressWarnings("unchecked")
	public Map<String,Object>  predictMultiWithSearch(Map<String,Object> param) throws Exception {
		System.out.println("데이터 조회 param::::"+param);
		
		Map<String,Object> result = new HashMap<String,Object>(); // 전체결과
		Map<String,Object> result_predict_item = null;// 아이템별 처리결과
		List<Map<String,Object>> result_predict_items = new ArrayList<Map<String,Object>>();// 아이템별 처리결과스
		String sResultMsg = "";
		
		List<Map<String,Object>> searchList = (ArrayList<Map<String,Object>>)param.get("search_list"); //데이터 조회화면 List
		System.out.println("searchList size :  " + searchList.size());
		
		List<Map<String,Object>> predictList = null;
		LinkedHashMap<String, Object> predict_param = null;
		
		//예측 항목 (항목별로 모델 예측실행)
		List<String> targets = new ArrayList<String>();
		if((boolean)param.get("target1_check")) targets.add("SEAL_TYPE");
		if((boolean)param.get("target2_check")) targets.add("API_PLAN");
		if((boolean)param.get("target3_check")) targets.add("CONN_COL");
		System.out.println("target : " + targets.toString());
		
		Map<String,List<String>> nominalAttr = null;
		Map<String,Object> opt = null;

		//"PUMP_TYPE", "PRODUCT",
		String[] sFeatures = null;
		
		// Feature 목록
		if ("EPC".equals((String)param.get("predict_type"))) { // EPC
			sFeatures = sFeaturesEPC2;
		}else { // OEM
			sFeatures = sFeaturesOEM2;
		}
		
		//Map<String,Object> classifierMap = new HashMap<String,Object>();
		
		Map<String,Object> training_param = null;
		List<Map<String,Object>> trainingList = null;
		Map<String,List<Map<String,Object>>> trainingListMap = new HashMap<String,List<Map<String,Object>>>();
		List<String> productAttrDataList = new ArrayList<String>(); // product attribute list 
		List<String> pumpTypeAttrDataList = new ArrayList<String>(); // pump type attribute list
		List<String> equipTypeAttrDataList = new ArrayList<String>(); // equip type attribute list
		List<String> classNominalAttrDataList = null; // class attribute list
		Map<String,List<String>> classNominalAttrDataListMap = new HashMap<String,List<String>>();
		
		List<Map<String,Object>> groupHierList = mLMapper.getGroupingHier(null);
		
		//인자조건에 대하여 기준단위로 컨버전된 값을 사용하기 위해 전체 목록을 가져온다.
		List<Map<String,Object>> mDataCnv1List = mLMapper.getMdataCnv1(param);
		
		System.out.println("training data create start... ");
		
		// Target 수만큼 트레이닝 데이터를 생성한다.
		for (String target : targets) {
			
			trainingList = new ArrayList<Map<String,Object>>();
			classNominalAttrDataList = new ArrayList<String>();
			
			// 트레이닝 데이터 List 생성
			for(Map<String,Object> m : searchList) {
				
				Map<String,Object> cnv = null; // 전처리된 데이터
				
				// 기준단위로 컨버전된 필드값으로 가져오게 처리한다.
				for(Map<String,Object> cnv_tmp : mDataCnv1List) {
					//DWG_NO  SHEET_NO  JOB_NO   기준으로 단위환산된 값을 가져온다.
					if((StringUtil.get(m.get("DWG_NO")).equals(StringUtil.get(cnv_tmp.get("DWG_NO")))) 
							&& (StringUtil.get(m.get("SHEET_NO")).equals(StringUtil.get(cnv_tmp.get("SHEET_NO"))))
							&& (StringUtil.get(m.get("JOB_NO")).equals(StringUtil.get(cnv_tmp.get("JOB_NO"))))
							) {
						cnv = cnv_tmp;
						break;
					}
				}
				
				if (cnv == null) {
					continue;
				}
				
				
				String sClassVal = "";
				if("CONN_COL".equals(target)) { // 복합정보 Seal Type + Material + API Plan 형태로 데이트를 구성필요
					sClassVal = "" + cnv.get("SEAL_TYPE") + " | " + cnv.get("MATERIAL") + " | " + cnv.get("API_PLAN"); //클래스 항목 적용
					
					if(cnv.get("SEAL_TYPE") ==null || "".equals(String.valueOf(cnv.get("SEAL_TYPE"))) || "-".equals(String.valueOf(cnv.get("SEAL_TYPE"))) ||
							cnv.get("MATERIAL") ==null || "".equals(String.valueOf(cnv.get("MATERIAL"))) || "-".equals(String.valueOf(cnv.get("MATERIAL"))) ||
									cnv.get("API_PLAN") ==null || "".equals(String.valueOf(cnv.get("API_PLAN"))) || "-".equals(String.valueOf(cnv.get("API_PLAN")))
							){
						continue;
					}
				}else {
					sClassVal = "" + cnv.get(target); //클래스 항목 적용
					
					if(sClassVal ==null || "".equals(sClassVal) || "-".equals(sClassVal) || "null".equals(sClassVal)) continue;
				}
				
				
				//Equip Type이 Comp. 인경우 제외
//				if(m.get("EQUIP_TYPE") != null && "Comp.".equals(String.valueOf(m.get("EQUIP_TYPE")))){
//					continue;
//				}
//				
//				String sClassVal = "";
//				if("CONN_COL".equals(target)) { // 복합정보 Seal Type + Material + API Plan 형태로 데이트를 구성필요
//					sClassVal = "" + m.get("SEAL_TYPE") + " | " + m.get("MATERIAL") + " | " + m.get("API_PLAN"); //클래스 항목 적용
//					
//					if(m.get("SEAL_TYPE") ==null || "".equals(String.valueOf(m.get("SEAL_TYPE"))) || "-".equals(String.valueOf(m.get("SEAL_TYPE"))) ||
//							m.get("MATERIAL") ==null || "".equals(String.valueOf(m.get("MATERIAL"))) || "-".equals(String.valueOf(m.get("MATERIAL"))) ||
//									m.get("API_PLAN") ==null || "".equals(String.valueOf(m.get("API_PLAN"))) || "-".equals(String.valueOf(m.get("API_PLAN")))
//							){
//						continue;
//					}
//				}else {
//					sClassVal = "" + m.get(target); //클래스 항목 적용
//					
//					if(sClassVal ==null || "".equals(sClassVal) || "-".equals(sClassVal) || "null".equals(sClassVal)) continue;
//				}
				
				// 빈값 처리
				//m = setEmptyDataWithDefaultData(m);
				
				//트레이닝 데이터 Map
				training_param = new LinkedHashMap<String,Object>();
//				for(String sf : sFeatures) {
//					if ("PRODUCT".equals(sf) || "PUMP_TYPE".equals(sf)) {
//						training_param.put(sf,String.valueOf(m.get(sf+"_G")).toUpperCase()); // 대문자 처리
//					}else {
//						training_param.put(sf,m.get(sf));
//					}
//				}
				
				for(String sf : sFeatures) {
					if ("PRODUCT".equals(sf) || "PUMP_TYPE".equals(sf)) {
						training_param.put(sf,String.valueOf(cnv.get(sf+"_G")).toUpperCase()); // 대문자 처리
					}else if ("EQUIP_TYPE".equals(sf)) {
						training_param.put(sf,cnv.get(sf)); // 변환값 없이 처리
					}else {
						training_param.put(sf,cnv.get(sf+"_C"));
					}
				}
				
				training_param.put(target, sClassVal); //클래스 항목 적용	(마지막)
				trainingList.add(training_param); //트레이닝 리스트에 추가
				
				//class attribute list 를 생성
				if ( !classNominalAttrDataList.contains(sClassVal)){ // 중복제거
					classNominalAttrDataList.add(sClassVal);
				}
				
				if ( !productAttrDataList.contains(cnv.get("PRODUCT_G"))){ 
					productAttrDataList.add(String.valueOf(cnv.get("PRODUCT_G")));
				}
				
				if ( !pumpTypeAttrDataList.contains(cnv.get("PUMP_TYPE_G"))){ 
					pumpTypeAttrDataList.add(String.valueOf(cnv.get("PUMP_TYPE_G")));
				}
				
				if ( !equipTypeAttrDataList.contains(cnv.get("EQUIP_TYPE"))){ 
					equipTypeAttrDataList.add(String.valueOf(cnv.get("EQUIP_TYPE")));
				}
//				if ( !classNominalAttrDataList.contains(sClassVal)){ // 중복제거
//					classNominalAttrDataList.add(sClassVal);
//				}
//				
//				if ( !productAttrDataList.contains(m.get("PRODUCT_G"))){ 
//					productAttrDataList.add(String.valueOf(m.get("PRODUCT_G")));
//				}
//				
//				if ( !pumpTypeAttrDataList.contains(m.get("PUMP_TYPE_G"))){ 
//					pumpTypeAttrDataList.add(String.valueOf(m.get("PUMP_TYPE_G")));
//				}
//				
//				if ( !equipTypeAttrDataList.contains(m.get("EQUIP_TYPE"))){ 
//					equipTypeAttrDataList.add(String.valueOf(m.get("EQUIP_TYPE")));
//				}
				
			}// end 트레이닝 데이터 생성
			classNominalAttrDataListMap.put(target, classNominalAttrDataList);
			trainingListMap.put(target, trainingList);
		} // end for
		
		//System.out.println("productAttrDataList : " + productAttrDataList.toString());
		//System.out.println("pumpTypeAttrDataList : " + pumpTypeAttrDataList.toString());
		//System.out.println("equipTypeAttrDataList : " + equipTypeAttrDataList.toString());
			
		
		System.out.println("classNominalAttrDataList :  " +classNominalAttrDataList);
		System.out.println("productAttrDataList :  " +productAttrDataList);
		System.out.println("pumpTypeAttrDataList :  " +pumpTypeAttrDataList);
		System.out.println("equipTypeAttrDataList :  " +equipTypeAttrDataList);
		
		
		System.out.println("model create start... ");
		
		RandomForest classifier_SealType =  null;
		RandomForest classifier_ApiPlan =  null;
		J48 classifier_ConnCol =  null;
		
		// Target 수만큼 모델 생성
		for (String target : targets) {

			opt = new HashMap<String,Object>();
			opt.put("dataType", "TP"); // 
			opt.put("modelSubType", target); // model 하위 구분
			
			nominalAttr = new HashMap<String,List<String>>();
			nominalAttr.put(target, classNominalAttrDataListMap.get(target));
			nominalAttr.put("PRODUCT", productAttrDataList);
			nominalAttr.put("PUMP_TYPE", pumpTypeAttrDataList);
			nominalAttr.put("EQUIP_TYPE", equipTypeAttrDataList);
			opt.put("nominalAttrs", nominalAttr); // nominal attribute
			
			opt.put("classIdx", null); // 클래스인덱스가 없을 경우 마지막 Attribute를 class로 설정
			
			//System.out.println("opt : " + opt.toString());
			
			//트레이닝 인스턴스 생성
			Instances trainingDataSet = PreProcessData.convListToArff(trainingListMap.get(target), opt);
			
			//모델 생성
			//Classifier classifier =  null;
			if ("CONN_COL".equals(target)){
				classifier_ConnCol = new J48();
				classifier_ConnCol.buildClassifier(trainingDataSet);
			}else if ("API_PLAN".equals(target)){
				classifier_ApiPlan = new RandomForest();
				classifier_ApiPlan.setNumIterations(10);
				classifier_ApiPlan.buildClassifier(trainingDataSet);
			}else if ("SEAL_TYPE".equals(target)){
				classifier_SealType = new RandomForest();
				classifier_SealType.setNumIterations(10);
				classifier_SealType.buildClassifier(trainingDataSet);
			}
		} // end for
		
		System.out.println("predict start... ");
		
		// 예측인자 리스트
		List<Map<String,Object>> predict_param_list = (List<Map<String,Object>>)param.get("predict_list");
		
		List<Map<String,Object>> productGroupList = mLMapper.getGroupingInfo("product");
		List<Map<String,Object>> pumpTypeGroupList = mLMapper.getGroupingInfo("pumpType"); //pumptype DB 데이터 x
		//List<Map<String,Object>> equipTypeGroupList = mLMapper.getEquipTypeList(param);
		
		// -----------------------------
		// 단위변환처리 정보
		// -----------------------------
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		// 단위코드정보
		List<Map<String,Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();
		// 단위변환정보
		List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
		// Text 입력값 변환정보
		List<Map<String,Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();
		// SSU 변환 정보 (Visc 용)
		List<Map<String,Object>> listSsuChg = mLMapper.getSsuChglInfo();
		String aBaseUnit = ""; // 항목별 기준단위
		String sSpecGravityNor =""; // 비중
		String sSpecGravityMin ="";
		String sSpecGravityMax ="";
		
		Classifier classifier =  null;
		String sNo = "";
		// --------------------------------------------
		// 예측 목록별로 처리
		// --------------------------------------------
		for (Map<String,Object> predict_param_full : predict_param_list) {
			
			result_predict_item = new HashMap<String,Object>(); 
			
			// 빈값 처리
			predict_param_full = setEmptyDataWithDefaultData(predict_param_full);
						
			// ------------------
			// 단위 환산 및 Txt Value 처리
			// ------------------
			sSpecGravityNor =  String.valueOf(predict_param_full.get("SPEC_GRAVITY_NOR"));
			sSpecGravityMin =  String.valueOf(predict_param_full.get("SPEC_GRAVITY_MIN"));
			sSpecGravityMax = String.valueOf(predict_param_full.get("SPEC_GRAVITY_MAX"));		
			
		
			// 온도
			aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR");
			predict_param_full.put("TEMP_NOR",  convWithUnit("3",engine, "TEMP_NOR", predict_param_full.get("TEMP_NOR"), predict_param_full.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("TEMP_MIN",  convWithUnit("3",engine, "TEMP_MIN", predict_param_full.get("TEMP_MIN"), predict_param_full.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("TEMP_MAX", convWithUnit("3",engine, "TEMP_MAX", predict_param_full.get("TEMP_MAX"), predict_param_full.get("TEMP_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
						
			// 점도 : Viscosity
			aBaseUnit = unitConvBase(listUnitCode, "VISC_NOR");
			predict_param_full.put("VISC_NOR", convWithUnit("3",engine,"VISC_NOR", predict_param_full.get("VISC_NOR"), predict_param_full.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityNor));
			predict_param_full.put("VISC_MIN", convWithUnit("3",engine,"VISC_MIN", predict_param_full.get("VISC_MIN"), predict_param_full.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMin));
			predict_param_full.put("VISC_MAX", convWithUnit("3",engine,"VISC_MAX", predict_param_full.get("VISC_MAX"), predict_param_full.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMax));
						
			// 증기압력 : Vapor Pressure
			aBaseUnit = unitConvBase(listUnitCode, "VAP_PRES_NOR");
			predict_param_full.put("VAP_PRES_NOR", convWithUnit("3",engine,"VAP_PRES_NOR", predict_param_full.get("VAP_PRES_NOR"), predict_param_full.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("VAP_PRES_MIN", convWithUnit("3",engine,"VAP_PRES_MIN", predict_param_full.get("VAP_PRES_MIN"), predict_param_full.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("VAP_PRES_MAX", convWithUnit("3",engine,"VAP_PRES_MAX", predict_param_full.get("VAP_PRES_MAX"), predict_param_full.get("VAP_PRES_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
						
			// 씰챔버압력 : Seal Chamber Pressure
			aBaseUnit = unitConvBase(listUnitCode, "SEAL_CHAM_NOR");
			predict_param_full.put("SEAL_CHAM_NOR", convWithUnit("3",engine,"SEAL_CHAM_NOR", predict_param_full.get("SEAL_CHAM_NOR"), predict_param_full.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("SEAL_CHAM_MIN", convWithUnit("3",engine,"SEAL_CHAM_MIN", predict_param_full.get("SEAL_CHAM_MIN"), predict_param_full.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("SEAL_CHAM_MAX", convWithUnit("3",engine,"SEAL_CHAM_MAX", predict_param_full.get("SEAL_CHAM_MAX"), predict_param_full.get("SEAL_CHAM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
						
			// RPM - Shaft Speed
			aBaseUnit = unitConvBase(listUnitCode, "RPM_NOR");
			predict_param_full.put("RPM_NOR", convWithUnit("3",engine,"RPM_NOR", predict_param_full.get("RPM_NOR"), predict_param_full.get("RPM_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			predict_param_full.put("RPM_MIN", convWithUnit("3",engine,"RPM_MIN", predict_param_full.get("RPM_MIN"), predict_param_full.get("RPM_UNIT"),  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			predict_param_full.put("RPM_MAX", convWithUnit("3",engine,"RPM_MAX", predict_param_full.get("RPM_MAX"), predict_param_full.get("RPM_UNIT"),  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			
			// Shaft Size
			aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE");
			predict_param_full.put("SHAFT_SIZE", convWithUnit("3",engine, "SHAFT_SIZE", predict_param_full.get("SHAFT_SIZE"), predict_param_full.get("SHAFT_SIZE_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));
						
			
			// 아이템 No
			sNo = String.valueOf(predict_param_full.get("NO"));
			System.out.println("NO : " + sNo);
									
			// Product 조건
			//param.put("PRODUCT", predict_param_full.get("PRODUCT") );
			String sThisProduct = predict_param_full.get("PRODUCT")==null?"":predict_param_full.get("PRODUCT").toString();
			// ProductGroup 정보를 가져온다
			String applyProductGrpString = getGroupingStr(sThisProduct,productGroupList, groupHierList);
			//System.out.println("search적용 PRODUCT GRP : "+ applyProductGrpString); 
			
			// Pump Type Group 정보를 가져온다
			String sThisPumpType = predict_param_full.get("PUMP_TYPE")==null?"":predict_param_full.get("PUMP_TYPE").toString();
			if("".equals(sThisPumpType)) sThisPumpType= String.valueOf(getBlankDataDefault("PUMP_TYPE")); // 빈값처리
			String applyPumpTypeGrpString = getGroupingStr(sThisPumpType,pumpTypeGroupList,null); //여기서 applyPumpTypeGrpString값이: - 로 출력됨.
			
			boolean isNominalInfo = true; // nominal 정보 정상 적용여부
			//System.out.println("param:::"+param);
			
			if (applyProductGrpString.length() <=0 
					|| applyPumpTypeGrpString.length() <= 0
					|| "-".equals(applyProductGrpString)
					|| "-".equals(applyPumpTypeGrpString)
					) { // 그룹핑 정보가 없을 경우 예측처리
				isNominalInfo = false;
			}
			
			
			if(!productAttrDataList.contains(applyProductGrpString) ||
					!pumpTypeAttrDataList.contains(applyPumpTypeGrpString) ||
					!equipTypeAttrDataList.contains(String.valueOf(param.get("equip_type")))
					) {
				isNominalInfo = false;
			}
			
			Map<String,Object> predicts = null;
			if(isNominalInfo) {//nominal 정보가 있을 경우
				
				predicts = new HashMap<String,Object>();
				
				for (String target : targets.toArray(new String[targets.size()])) { // Target Class 별 예측
					
					//예측 Feature base set
					predict_param = new LinkedHashMap<String,Object>();
					predict_param.put("EQUIP_TYPE", String.valueOf(param.get("equip_type"))); // EQUIP TYPE은 개별로 입력되지 않으므로 여기서 일괄 적용되게 함.
					for(String sf : sFeatures) {
						if("PRODUCT".equals(sf)) { // 펌프타입조건 추가
							predict_param.put(sf,applyProductGrpString);
						}else if ("PUMP_TYPE".equals(sf)) {
							predict_param.put("PUMP_TYPE", applyPumpTypeGrpString ); // 펌프타입조건 추가
						}else if("EQUIP_TYPE".equals(sf)) { // Equip Type
							continue; // Equip Type은 고정값으로 입력됨.
						}else {
							predict_param.put(sf,predict_param_full.get(sf));
						}
					}
					
					//Map<String,Object> data = (LinkedHashMap<String,Object>)predict_param.clone();
					predict_param.put(target, "");
					predictList = new ArrayList<Map<String,Object>>();
					predictList.add(predict_param);
					
					opt = new HashMap<String,Object>();
					opt.put("modelSubType", target);
					opt.put("dataType", "P"); // Predict data
					
					nominalAttr = new HashMap<String,List<String>>();
					nominalAttr.put(target, classNominalAttrDataListMap.get(target));//클래스 정보 
					nominalAttr.put("PRODUCT", productAttrDataList);  //Product 정보
					nominalAttr.put("EQUIP_TYPE", equipTypeAttrDataList);  //Equip Type 정보
					nominalAttr.put("PUMP_TYPE", pumpTypeAttrDataList);  //Pump Type 정보
					
					opt.put("nominalAttrs", nominalAttr); // nominal attribute
					opt.put("classIdx", null); // 클래스인덱스가 없을 경우 마지막 Attribute를 class로 설정
					
					//System.out.println("Option : " + opt.toString());
					System.out.println("예측Feature : " + predictList.toString());
					
					//모델 예측
					classifier = null;
					//Classifier classifier =  null;
					if("CONN_COL".equals(target)) {
						//classifier = (J48)classifierMap.get(target);
						classifier = classifier_ConnCol;
					}else if("API_PLAN".equals(target)) {
						
						classifier = classifier_ApiPlan;
						//classifier = (RandomForest)classifierMap.get(target);
					}else if("SEAL_TYPE".equals(target)) {
						classifier = classifier_SealType;
					}
					
					Map<String,Object> predict = new HashMap<String,Object>();;
					if (classifier != null) {
						//predict = dtm.process("predict", null, predictList, opt);
						
						// 예측데이터 Set
						Instances predictDataSet = PreProcessData.convListToArff(predictList, opt);

						//System.out.println("predictDataSet.size() : " + predictDataSet.size());
						System.out.println(sNo + ":"+ target + " : " + "predict : data set end "  );
						
						// 예측
						double[] prediction = null;
						try {
							prediction = classifier.distributionForInstance(predictDataSet.get(0));
						}catch(Exception e) {
							System.out.println("predictDataSet :"+ predictDataSet);
							e.printStackTrace();
						}
						System.out.println(sNo + ":"+ target + " : " + "predict : predict 건수 : " + prediction.length);
						
						Map<String,Double> _predict = new HashMap<String,Double>();
				        for(int i=0; i<prediction.length; i=i+1){
				        	_predict.put(predictDataSet.classAttribute().value(i).toString(), prediction[i]);
				        	//System.out.println(predictDataSet.classAttribute().value(i).toString() +"|"+ prediction[i]);
				        }
				        Map<String,Double> p_m = ModelUtil.getSortMap(_predict); // 정렬
				        List<Map<String,Object>> p_l = new ArrayList<Map<String,Object>>();
				        Map<String,Object> p_l_m = null;
				        int i=1;
				        for( String key : p_m.keySet()) {
				        	p_l_m = new HashMap<String,Object>();
				        	p_l_m.put("NO", i++);
				        	p_l_m.put("CLASS", key);
				        	p_l_m.put("PROB", Math.round(p_m.get(key)*10000)/100.0);
				        	p_l.add(p_l_m);
				        	if(i==6) break; // 상위 5건만 유지
				        }
				        predict.put("predict_result", p_l);
						
					}else {
						predict = new HashMap<String,Object>();
					}
					
					predicts.put(target,predict.get("predict_result"));
					
				}
				
				if(!"".equals(sResultMsg)) {
					sResultMsg = " / " + sResultMsg;
				}
				result_predict_item.put("predict_idx",sNo);
				result_predict_item.put("predict_msg","complete" + sResultMsg); 
				result_predict_item.put("RESULT",predicts);
				result_predict_item.put("ProductGroupInfo",applyProductGrpString); //product group info
			
			} else { // Product Grouping 정보가 없을 경우
				result_predict_item.put("predict_idx", sNo);
				result_predict_item.put("predict_msg","complete" + sResultMsg); 
				//result_predict_item.put("predict_msg","조회된 데이터가 없습니다."); 
				result_predict_item.put("RESULT","");
				result_predict_item.put("ProductGroupInfo",""); //product group info
			}
			
			//컨버전된 피처값 활용을 위해 다시 넘김
			result_predict_item.put("param_cnv", predict_param_full );
			
			// 아이템별 결과 저장
			result_predict_items.add(result_predict_item);
		}
		
		result.put("RESULT",result_predict_items);
		return result;
	}
	
	
	public List<Map<String,Object>> getDBbyPredictFeauture(Map<String,Object> param) throws Exception {
		return mLMapper.getDBbyPredictFeature(param);
	}
	
	
	public Map<String,Object>  modelInfoSave(Map<String,Object> param) throws Exception {
		mLMapper.setModelInfoSave(param);
		return new HashMap<String,Object>();
	}
	
	
	public List<String> getPredictTargetClass(String modelId, String classType) throws Exception {
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("MODEL_ID", modelId);
		param.put("ATTR_TYPE", classType+"_CLASS"); //class 를 붙여서 db에 저장되어있음.
		List<Map<String,Object>> list = mLMapper.getPredictNominalList(param);
		
		List<String> classList = new ArrayList<String>();
		for(Map<String,Object> m:list) {
			classList.add((String)m.get("V_VAL1"));
		}
		return classList;
	}
	
	public List<String> getPredictProduct(String modelId) throws Exception {
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("MODEL_ID", modelId);
		param.put("ATTR_TYPE", "PRODUCT"); 
		List<Map<String,Object>> list = mLMapper.getPredictNominalList(param);
		
		List<String> classList = new ArrayList<String>();
		for(Map<String,Object> m:list) {
			classList.add((String)m.get("V_VAL1"));
		}
		return classList;
	}
	
	
	public List<String> getPredictEquipType(String modelId) throws Exception {
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("MODEL_ID", modelId);
		param.put("ATTR_TYPE", "EQUIP_TYPE"); 
		List<Map<String,Object>> list = mLMapper.getPredictNominalList(param);
		
		List<String> classList = new ArrayList<String>();
		for(Map<String,Object> m:list) {
			classList.add((String)m.get("V_VAL1"));
		}
		return classList;
	}
	
	public List<String> getPredictPumpType(String modelId) throws Exception {
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("MODEL_ID", modelId);
		param.put("ATTR_TYPE", "PUMP_TYPE"); 
		List<Map<String,Object>> list = mLMapper.getPredictNominalList(param);
		
		List<String> classList = new ArrayList<String>();
		for(Map<String,Object> m:list) {
			classList.add((String)m.get("V_VAL1"));
		}
		return classList;
	}
	
	/**
	 * 최근 모델 ID를 반환한다. 
	 * @param buildType
	 * @return
	 * @throws Exception
	 */
	public String getModelId(String buildType) throws Exception {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("ATTR1", buildType);
		String s = mLMapper.getModelId(param);
		return s;
	}
	
	public Map<String,Object> getModelInfo(String modelId, String classType) throws Exception {
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("MODEL_ID", modelId);
		param.put("CLASS_TYPE", classType);
		List<Map<String,Object>> list = mLMapper.getModelInfo(param);
		
		Map<String,Object> modelInfo= null;
		if (list.size() > 0) {
			//sFilePath =(String)list.get(0).get("MODEL_LOC");
			modelInfo = list.get(0);
		}
		return modelInfo;
	}
	
	public List<Map<String,Object>> getTransTextVal(Map<String,Object> param) throws Exception {
		return  mLMapper.getTransTextVal(param);
	}
	
	
	
	public void orgToCnv(Map<String,Object> param) throws Exception{
		orgToCnv1(param);
		//orgToCnv2(param);
		orgToCnv2_1(param);
	}
	
	/**
	 * org.-data to conv1
	 */
	public void orgToCnv1(Map<String,Object> param) throws Exception{
	
//		try {
		System.out.println("orgToConv1 start");
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		// ------------------------------------------------------
		// 범위 조정 필요 : 현재 전체 데이터 대상으로 처리 중
		
		// 변환 Change 데이터 초기화
		//mLMapper.removeCNV1();
		//System.out.println("truncate ok");
		
		// 변환 Orignal 데이터 조회
		List<Map<String,Object>> list = mLMapper.getOrgList(param);
		// ------------------------------------------------------
		
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
				
		// 단위코드정보
		List<Map<String,Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();
		// 단위변환정보
		List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
		// Text 입력값 변환정보
		List<Map<String,Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();
		// SSU 변환 정보 (Visc 용)
		List<Map<String,Object>> listSsuChg = mLMapper.getSsuChglInfo();
		// Grouping 정보
		List<Map<String,Object>> listGrpPumpTypeInfo = mLMapper.getGroupingInfo("pumpType");
		List<Map<String,Object>> listGrpProductInfo = mLMapper.getGroupingInfo("product");
		List<Map<String,Object>> listGrpEquipMfgInfo = mLMapper.getGroupingInfo("equipMfg");
		List<Map<String,Object>> listGrpUltimateUserInfo = mLMapper.getGroupingInfo("ultimateUser");
		
		// ProductGroup Hierarchy
		List<Map<String,Object>> productGroupHierList = mLMapper.getGroupingHier(null);
		
		System.out.println("list : " + list.size());
		System.out.println("listUnitCode : " + listUnitCode.size());
		System.out.println("listUnitChg : " + listUnitChg.size());
		System.out.println("listTransTxtVal : " + listTransTxtVal.size());
		
		// 컬럼별 기준단위
		String aBaseUnit = null;
		String sTempUnit = null;
		
		String sPumpTypeG = "", sProductTypeG = "", sEquipMfgG = "", sUltimateUserG = "";
		String sProduct = "", sPumpType = "", sEquipMfg = "", sUltimateUser = "" ;
		String sealTypeCombined = "";
		String materialCombined = "", materialCombined_I =  "", materialCombined_M = "", materialCombined_O = "";
		
		//Map<String,Object> m_dummy = null;
		//int idx=1;
		for (Map<String,Object> m : list ){
			
			//System.out.println("No : " + m.get("IDX_NO") + " : " + (idx++));
			//m_dummy =(Map<String,Object>)m.clone(); // 현재 데이터셋을 복제한다.
			//m_dummy = new HashMap<String,Object>();
			//m_dummy.putAll(m); // 현재 데이터셋을 복제한다.
			
			String sSpecGravityNor =  m.get("SPEC_GRAVITY_NOR")==null?"":String.valueOf(m.get("SPEC_GRAVITY_NOR"));
			String sSpecGravityMin =  m.get("SPEC_GRAVITY_MIN")==null?"":String.valueOf(m.get("SPEC_GRAVITY_MIN"));
			String sSpecGravityMax = m.get("SPEC_GRAVITY_MAX")==null?"":String.valueOf(m.get("SPEC_GRAVITY_MAX"));
			
			if(!"".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityMin = sSpecGravityNor;
				sSpecGravityMax = sSpecGravityNor;
			}else if("".equals(sSpecGravityNor) && !"".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityNor = sSpecGravityMin;
				sSpecGravityMax = sSpecGravityMin;
			}else if("".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && !"".equals(sSpecGravityMax) ) {
				sSpecGravityNor = sSpecGravityMax;
				sSpecGravityMin = sSpecGravityMax;
			}else if(!"".equals(sSpecGravityNor) && !"".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityMax = sSpecGravityNor;
			}else if(!"".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && !"".equals(sSpecGravityMax) ) {
				sSpecGravityMin = sSpecGravityNor;
			}else if("".equals(sSpecGravityNor) && !"".equals(sSpecGravityMin)  && !"".equals(sSpecGravityMax) ) {
				sSpecGravityNor = sSpecGravityMax;
			}else if("".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityNor = "1";
				sSpecGravityMin = "1";
				sSpecGravityMax = "1";
			}
			
			
			// ----------------
			// 온도
			// ℃ 기준 / TEMP_UNIT : NULL일경우 ℃ 으로 처리
			// AMB 처리
			// ----------------
			sTempUnit = m.get("TEMP_UNIT")==null?"":(String)m.get("TEMP_UNIT");
			if (isTransUnitData("1", listUnitChg, sTempUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR");
				m.put("TEMP_NOR_C",  convWithUnit("1",engine, "TEMP_NOR", m.get("TEMP_NOR"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("TEMP_MIN_C",  convWithUnit("1",engine, "TEMP_MIN", m.get("TEMP_MIN"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("TEMP_MAX_C", convWithUnit("1",engine, "TEMP_MAX", m.get("TEMP_MAX"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("TEMP_UNIT", (m.get("TEMP_UNIT")!=null&&!"".equals((String)m.get("TEMP_UNIT")))?aBaseUnit:"" );
			}else {
				m.put("TEMP_NOR_C",  m.get("TEMP_NOR") == null || "".equals(String.valueOf(m.get("TEMP_NOR")))? null:asDouble2(m.get("TEMP_NOR")));
				m.put("TEMP_MIN_C",  m.get("TEMP_MIN") == null || "".equals(String.valueOf(m.get("TEMP_MIN")))? null:asDouble2(m.get("TEMP_MIN")));
				m.put("TEMP_MAX_C",  m.get("TEMP_MAX") == null || "".equals(String.valueOf(m.get("TEMP_MAX")))? null:asDouble2(m.get("TEMP_MAX")));
			}
			
			// 비중 - 단위환산보다는 Null 처리를 위해 공통 처리
			m.put("SPEC_GRAVITY_NOR",  convWithUnit("1",engine, "SPEC_GRAVITY_NOR", m.get("SPEC_GRAVITY_NOR"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("SPEC_GRAVITY_MIN",  convWithUnit("1",engine,"SPEC_GRAVITY_MIN", m.get("SPEC_GRAVITY_MIN"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMin ));
			m.put("SPEC_GRAVITY_MAX", convWithUnit("1",engine, "SPEC_GRAVITY_MAX", m.get("SPEC_GRAVITY_MAX"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			
			// ----------------
			// 점도 : Viscosity
			// cP 기준
			// ----------------
			if (isTransUnitData("1", listUnitChg, m.get("VISC_UNIT"))){
				aBaseUnit = unitConvBase(listUnitCode, "VISC_NOR");
				m.put("VISC_NOR_C", convWithUnit("1",engine,"VISC_NOR", m.get("VISC_NOR"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityNor));
				m.put("VISC_MIN_C", convWithUnit("1",engine,"VISC_MIN", m.get("VISC_MIN"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMin));
				m.put("VISC_MAX_C", convWithUnit("1",engine,"VISC_MAX", m.get("VISC_MAX"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMax));
				//m.put("VISC_UNIT", (m.get("VISC_UNIT")!=null&&!"".equals((String)m.get("VISC_UNIT")))?aBaseUnit:"" );
			}else {
				m.put("VISC_NOR_C",  m.get("VISC_NOR") == null || "".equals(String.valueOf(m.get("VISC_NOR")))? null:asDouble2(m.get("VISC_NOR")));
				m.put("VISC_MIN_C",  m.get("VISC_MIN") == null || "".equals(String.valueOf(m.get("VISC_MIN")))? null:asDouble2(m.get("VISC_MIN")));
				m.put("VISC_MAX_C",  m.get("VISC_MAX") == null || "".equals(String.valueOf(m.get("VISC_MAX")))? null:asDouble2(m.get("VISC_MAX")));
			}
			
			
			// ----------------
			// 증기압력 : Vapor Pressure
			//- BARA 기준
			// ----------------
			String vapPresUnit = m.get("VAP_PRES_UNIT")==null?"":String.valueOf(m.get("VAP_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, vapPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "VAP_PRES_NOR");
				m.put("VAP_PRES_NOR_C", convWithUnit("1",engine,"VAP_PRES_NOR", m.get("VAP_PRES_NOR"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("VAP_PRES_MIN_C", convWithUnit("1",engine,"VAP_PRES_MIN", m.get("VAP_PRES_MIN"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("VAP_PRES_MAX_C", convWithUnit("1",engine,"VAP_PRES_MAX", m.get("VAP_PRES_MAX"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("VAP_PRES_UNIT", aBaseUnit );
			}else {
				m.put("VAP_PRES_NOR_C",  m.get("VAP_PRES_NOR") == null || "".equals(String.valueOf(m.get("VAP_PRES_NOR")))? null:asDouble2(m.get("VAP_PRES_NOR")));
				m.put("VAP_PRES_MIN_C",  m.get("VAP_PRES_MIN") == null || "".equals(String.valueOf(m.get("VAP_PRES_MIN")))? null:asDouble2(m.get("VAP_PRES_MIN")));
				m.put("VAP_PRES_MAX_C",  m.get("VAP_PRES_MAX") == null || "".equals(String.valueOf(m.get("VAP_PRES_MAX")))? null:asDouble2(m.get("VAP_PRES_MAX")));
			}
			// ----------------
			// 씰챔버압력 : Seal Chamber Pressure
			//- BARG 기준
			// ----------------
			String scPresUnit = m.get("SEAL_CHAM_UNIT")==null?"":String.valueOf(m.get("SEAL_CHAM_UNIT"));
			if (isTransUnitData("1", listUnitChg, scPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_CHAM_NOR");
				m.put("SEAL_CHAM_NOR_C", convWithUnit("1",engine,"SEAL_CHAM_NOR", m.get("SEAL_CHAM_NOR"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SEAL_CHAM_MIN_C", convWithUnit("1",engine,"SEAL_CHAM_MIN", m.get("SEAL_CHAM_MIN"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SEAL_CHAM_MAX_C", convWithUnit("1",engine,"SEAL_CHAM_MAX", m.get("SEAL_CHAM_MAX"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("SEAL_CHAM_UNIT", aBaseUnit );
			}else {
				m.put("SEAL_CHAM_NOR_C",  m.get("SEAL_CHAM_NOR") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_NOR")))? null:asDouble2(m.get("SEAL_CHAM_NOR")));
				m.put("SEAL_CHAM_MIN_C",  m.get("SEAL_CHAM_MIN") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MIN")))? null:asDouble2(m.get("SEAL_CHAM_MIN")));
				m.put("SEAL_CHAM_MAX_C",  m.get("SEAL_CHAM_MAX") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MAX")))? null:asDouble2(m.get("SEAL_CHAM_MAX")));
			}
			
			// ----------------
			// 흡입압력 : SUCT_PRES_NOR
			//- BARG 기준
			// ----------------
			String suctPresUnit = m.get("SUCT_PRES_UNIT")==null?"":String.valueOf(m.get("SUCT_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, suctPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SUCT_PRES_NOR");
				m.put("SUCT_PRES_NOR_C", convWithUnit("1",engine,"SUCT_PRES_NOR", m.get("SUCT_PRES_NOR"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SUCT_PRES_MIN_C", convWithUnit("1",engine,"SUCT_PRES_MIN", m.get("SUCT_PRES_MIN"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SUCT_PRES_MAX_C", convWithUnit("1",engine,"SUCT_PRES_MAX", m.get("SUCT_PRES_MAX"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("SUCT_PRES_UNIT", aBaseUnit );
			}else {
				m.put("SUCT_PRES_NOR_C",  m.get("SUCT_PRES_NOR") == null || "".equals(String.valueOf(m.get("SUCT_PRES_NOR")))? null:asDouble2(m.get("SUCT_PRES_NOR")));
				m.put("SUCT_PRES_MIN_C",  m.get("SUCT_PRES_MIN") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MIN")))? null:asDouble2(m.get("SUCT_PRES_MIN")));
				m.put("SUCT_PRES_MAX_C",  m.get("SUCT_PRES_MAX") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MAX")))? null:asDouble2(m.get("SUCT_PRES_MAX")));
			}
			
			// ----------------
			// 배출압력 : DISCH_PRES_NOR
			//- BARG 기준
			// ----------------
			String dischPresUnit = m.get("DISCH_PRES_UNIT")==null?"":String.valueOf(m.get("DISCH_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, dischPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "DISCH_PRES_NOR");
				m.put("DISCH_PRES_NOR_C", convWithUnit("1",engine,"DISCH_PRES_NOR", m.get("DISCH_PRES_NOR"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("DISCH_PRES_MIN_C", convWithUnit("1",engine,"DISCH_PRES_MIN", m.get("DISCH_PRES_MIN"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("DISCH_PRES_MAX_C", convWithUnit("1",engine,"DISCH_PRES_MAX", m.get("DISCH_PRES_MAX"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("DISCH_PRES_UNIT", aBaseUnit );
			}else {
				m.put("DISCH_PRES_NOR_C",  m.get("DISCH_PRES_NOR") == null || "".equals(String.valueOf(m.get("DISCH_PRES_NOR")))? null:asDouble2(m.get("DISCH_PRES_NOR")));
				m.put("DISCH_PRES_MIN_C",  m.get("DISCH_PRES_MIN") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MIN")))? null:asDouble2(m.get("DISCH_PRES_MIN")));
				m.put("DISCH_PRES_MAX_C",  m.get("DISCH_PRES_MAX") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MAX")))? null:asDouble2(m.get("DISCH_PRES_MAX")));
			}
			
			// ----------------
			// 배리어 Fluid 압력 : BARR_PRES_PRES
			//- BARG 기준
			// ----------------
			String barrPresUnit = m.get("BARR_PRES_UNIT")==null?"":String.valueOf(m.get("BARR_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, dischPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "BARR_PRES_PRES");
				m.put("BARR_PRES_PRES_C", convWithUnit("1",engine,"BARR_PRES_PRES", m.get("BARR_PRES_PRES"), barrPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			}else {
				m.put("BARR_PRES_PRES_C",  m.get("BARR_PRES_PRES") == null || "".equals(String.valueOf(m.get("BARR_PRES_PRES")))? null:asDouble2(m.get("BARR_PRES_PRES")));
			}
			
			// RPM - Shaft Speed ==> 현재 RPM UNIT 컬럼 없음 20.11.09
			aBaseUnit = unitConvBase(listUnitCode, "RPM_NOR");
			m.put("RPM_NOR_C", convWithUnit("1",engine,"RPM_NOR", m.get("RPM_NOR"), "RPM", aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("RPM_MIN_C", convWithUnit("1",engine,"RPM_MIN", m.get("RPM_MIN"), "RPM",  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			m.put("RPM_MAX_C", convWithUnit("1",engine,"RPM_MAX", m.get("RPM_MAX"), "RPM",  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
					
			// Seal Size
			String sealSizeInnerUnit = m.get("SEAL_SIZE_I_UNIT")==null?"":String.valueOf(m.get("SEAL_SIZE_I_UNIT"));
			if (isTransUnitData("1", listUnitChg, sealSizeInnerUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE_I");
				m.put("SEAL_SIZE_I_C", convWithUnit("1",engine,"SEAL_SIZE_I", m.get("SEAL_SIZE_I"), sealSizeInnerUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			}else {
				m.put("SEAL_SIZE_I_C",  m.get("SEAL_SIZE_I") == null || "".equals(String.valueOf(m.get("SEAL_SIZE_I")))? null:asDouble2(m.get("SEAL_SIZE_I")));
			}
			
			String sealSizeMiddleUnit = m.get("SEAL_SIZE_M_UNIT")==null?"":String.valueOf(m.get("SEAL_SIZE_M_UNIT"));
			if (isTransUnitData("1", listUnitChg, sealSizeMiddleUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE_M");
				m.put("SEAL_SIZE_M_C", convWithUnit("1",engine,"SEAL_SIZE_M", m.get("SEAL_SIZE_M"), sealSizeMiddleUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			}else {
				m.put("SEAL_SIZE_M_C",  m.get("SEAL_SIZE_M") == null || "".equals(String.valueOf(m.get("SEAL_SIZE_M")))? null:asDouble2(m.get("SEAL_SIZE_M")));
			}
			
			String sealSizeOuterUnit = m.get("SEAL_SIZE_O_UNIT")==null?"":String.valueOf(m.get("SEAL_SIZE_O_UNIT"));
			if (isTransUnitData("1", listUnitChg, sealSizeOuterUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE_O");
				m.put("SEAL_SIZE_O_C", convWithUnit("1",engine,"SEAL_SIZE_O", m.get("SEAL_SIZE_O"), sealSizeMiddleUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			}else {
				m.put("SEAL_SIZE_O_C",  m.get("SEAL_SIZE_O") == null || "".equals(String.valueOf(m.get("SEAL_SIZE_O")))? null:asDouble2(m.get("SEAL_SIZE_O")));
			}
			
			// ----------------
			// 크기
			//  MM 기준  / SHAFT_UNIT : NULL일경우 MM 으로 처리 
			// ----------------
			// SHAFT_SIZE
			aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE"); //기본 unit을 MM으로 변경
			m.put("SHAFT_SIZE_C", convWithUnit("1",engine, "SHAFT_SIZE", m.get("SHAFT_SIZE"), m.get("SHAFT_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			
			// ----------------
			// Grouping 정보
			// COL_VALUE,GRP_CODE,GRP,GRP_SUB
			// ----------------
			
			// Product Group
			//sProductTypeG = "";
			if (m.get("PRODUCT")!=null) {
				sProduct = String.valueOf(m.get("PRODUCT"));
				sProductTypeG = getGroupingStr(sProduct,listGrpProductInfo,productGroupHierList);
				m.put("PRODUCT_G", sProductTypeG );
			}else {
				m.put("PRODUCT_G", "-" );
			}
						
			// Pump Type Group
			sPumpType = (m.get("PUMP_TYPE")==null||"".equals(String.valueOf(m.get("PUMP_TYPE"))))?""+getBlankDataDefault("PUMP_TYPE"):String.valueOf(m.get("PUMP_TYPE"));
			sPumpTypeG = getGroupingStr(sPumpType,listGrpPumpTypeInfo,null);
			m.put("PUMP_TYPE_G", sPumpTypeG );
			
			// Equip Mfg Group
			sEquipMfg = (m.get("EQUIP_MFG")==null||"".equals(String.valueOf(m.get("EQUIP_MFG"))))?""+getBlankDataDefault("EQUIP_MFG"):String.valueOf(m.get("EQUIP_MFG"));
			sEquipMfgG = getGroupingStr(sEquipMfg,listGrpEquipMfgInfo,null);
			m.put("EQUIP_MFG_G", sEquipMfgG );
			
			// Ultimate User Group
			sUltimateUser = (m.get("ULTIMATE_USER")==null||"".equals(String.valueOf(m.get("ULTIMATE_USER"))))?""+getBlankDataDefault("ULTIMATE_USER"):String.valueOf(m.get("ULTIMATE_USER"));
			sUltimateUserG = getGroupingStr(sUltimateUser,listGrpUltimateUserInfo,null);
			m.put("ULTIMATE_USER_G", sUltimateUserG );
			
			
			// 기타 처리 -------------------------------	
			// Equip Type
			// seal text 제거 -> ERP Data에서 Seal 문자가 붙어서 넘어옴.
			m.put("EQUIP_TYPE", StringUtil.get(m.get("EQUIP_TYPE")).replace("Seal", "").trim() );
			
			// Seal Type (결합)  -> CNV2에서만?
			sealTypeCombined = StringUtil.get(m.get("SEAL_TYPE_I"));
			if(!"".equals(StringUtil.get(m.get("SEAL_TYPE_M") ))){
				sealTypeCombined += "/"+ StringUtil.get(m.get("SEAL_TYPE_M") );
			}
			if(!"".equals(StringUtil.get(m.get("SEAL_TYPE_O") ))){
				sealTypeCombined += "/"+ StringUtil.get(m.get("SEAL_TYPE_O") );
			}
			m.put("SEAL_TYPE",sealTypeCombined);
			
			// Material (결합)  -> CNV2에서만?
			materialCombined_I = StringUtil.get(m.get("MATERIAL_I1"));
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I2") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I2") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I3") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I3") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I4") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I4") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I5") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I5") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I6") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I6") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I7") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I7") );
					
			materialCombined_M = StringUtil.get(m.get("MATERIAL_M1"));
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M2") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M2") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M3") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M3") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M4") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M4") );		
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M5") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M5") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M6") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M6") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M7") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M7") );
					
			materialCombined_O = StringUtil.get(m.get("MATERIAL_O1"));
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O2") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O2") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O3") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O3") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O4") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O4") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O5") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O5") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O6") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O6") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O7") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O7") );
			
			// 각각의 재질정보를 공백을 좌우 적용하여 / 로 연결
			// ex : 5 U K X / 5 U K X
			materialCombined = materialCombined_I;
			if (!"".equals(materialCombined_M)) materialCombined += " / " + materialCombined_M;
			if (!"".equals(materialCombined_O)) materialCombined += " / " + materialCombined_O;
					
			m.put("MATERIAL",materialCombined);		
							
			// ----------------
			// Insert row
			// ----------------
			//mLMapper.insertDataCnv1(m);	
			resultList.add(m);
						
		} // end for (Map<String,Object> m : list )
		
		System.out.println("Insert 시작 resultList.size()  :  " + resultList.size());
		
		if(resultList.size() > 0) {
			for(Map<String,Object> paramMap : resultList) {
				
				// delete
				mLMapper.deleteDataCnv1(paramMap);
				// insert
				mLMapper.insertDataCnv1(paramMap);
		
				//group info insert
				int i=0;
				for(String s : StringUtil.get(paramMap.get("PRODUCT_G")).split("[+]")) {
					paramMap.put("GRP_TYPE","PRODUCT");
					paramMap.put("GRP_SEQ",i++);
					paramMap.put("GRP",s);
					mLMapper.insertDataCnv1GrpInfo(paramMap);
				}
				i = 0;
				for(String s : StringUtil.get(paramMap.get("PUMP_TYPE_G")).split("[+]")) {
					paramMap.put("GRP_TYPE","PUMP_TYPE");
					paramMap.put("GRP_SEQ",i++);
					paramMap.put("GRP",s);
					mLMapper.insertDataCnv1GrpInfo(paramMap);
				}
				i = 0;
				for(String s : StringUtil.get(paramMap.get("EQUIP_MFG_G")).split("[+]")) {
					paramMap.put("GRP_TYPE","EQUIP_MFG");
					paramMap.put("GRP_SEQ",i++);
					paramMap.put("GRP",s);
					mLMapper.insertDataCnv1GrpInfo(paramMap);
				}
				i = 0;
				for(String s : StringUtil.get(paramMap.get("ULTIMATE_USER_G")).split("[+]")) {
					paramMap.put("GRP_TYPE","ULTIMATE_USER");
					paramMap.put("GRP_SEQ",i++);
					paramMap.put("GRP",s);
					mLMapper.insertDataCnv1GrpInfo(paramMap);
				}
							
			}
		}
		
		//Bulk Insert
		/*
		if(resultList.size() > 0) {
			int iCommit=0;
	    	int iBulkSize=29; //인자 2100개 제한(Sql Server)
	    	double iStepCnt =  Math.ceil(resultList.size()/iBulkSize);
	    	List<Map<String,Object>> subList = null;
	    	Map<String,Object> paramMap = new HashMap<String,Object>();
	    	for(int i=0;i<iStepCnt;i++) {
	    		subList = resultList.subList(i*iBulkSize, (i+1)*iBulkSize);
	    		paramMap.put("list", subList);
	    		mLMapper.insertDataCnv1(paramMap);	
	    		System.out.println(iCommit + " : ok");
	    		iCommit++;
			}
			subList = resultList.subList(iCommit*iBulkSize, list.size());
			System.out.println("last count :  " + subList.size());
			if (subList.size()>0) {
				paramMap.put("list", subList);
				mLMapper.insertDataCnv1(paramMap);	
			}
		}
		*/
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
	/**
	 * org.-data to conv2
	 */
	public void orgToCnv2_1(Map<String,Object> param) throws Exception{
		
		System.out.println("orgToConv2 start");
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		// 데이터 초기화
		//mLMapper.removeCNV2_1();
		
		//System.out.println("truncate ok");
		
		// 전체 데이터 (Org)
		List<Map<String,Object>> list = mLMapper.getOrgList(param);
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
				
		// 단위코드정보
		List<Map<String,Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();
		// 단위변환정보
		List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
		// Text 입력값 변환정보
		List<Map<String,Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();
		// SSU 변환 정보 (Visc 용)
		List<Map<String,Object>> listSsuChg = mLMapper.getSsuChglInfo();
		// Grouping 정보
		List<Map<String,Object>> listGrpPumpTypeInfo = mLMapper.getGroupingInfo("pumpType");
		List<Map<String,Object>> listGrpProductInfo = mLMapper.getGroupingInfo("product");
		List<Map<String,Object>> listGrpEquipMfgInfo = mLMapper.getGroupingInfo("equipMfg");
		List<Map<String,Object>> listGrpUltimateUserInfo = mLMapper.getGroupingInfo("ultimateUser");
		
		// ProductGroup Hierarchy
		List<Map<String,Object>> productGroupHierList = mLMapper.getGroupingHier(null);
		
		
		System.out.println("list : " + list.size());
		System.out.println("listUnitCode : " + listUnitCode.size());
		System.out.println("listUnitChg : " + listUnitChg.size());
		System.out.println("listTransTxtVal : " + listTransTxtVal.size());
		
		// 컬럼별 기준단위
		String aBaseUnit = null;
		String sTempUnit = null;
		
		String sPumpTypeG = "", sProductTypeG = "", sEquipMfgG = "", sUltimateUserG = "";
		String sProduct = "", sPumpType = "", sEquipMfg = "", sUltimateUser = "" ;
		String sealTypeCombined = "";
		String materialCombined = "", materialCombined_I =  "", materialCombined_M = "", materialCombined_O = "";
		
		// 빈값 먼저 처리
		List<Map<String,Object>> list_2nd = new ArrayList<Map<String,Object>>();
		
		for( Map<String,Object> data : list ){
			//빈값 필드 처리
			data = setEmptyDataWithDefaultData(data);
			list_2nd.add(data);
		}
		
		for (Map<String,Object> m : list_2nd ){
			String sSpecGravityNor =  String.valueOf(m.get("SPEC_GRAVITY_NOR"));
			String sSpecGravityMin =  String.valueOf(m.get("SPEC_GRAVITY_MIN"));
			String sSpecGravityMax =  String.valueOf(m.get("SPEC_GRAVITY_MAX"));
			
			// ----------------
			// 온도
			// ℃ 기준 / TEMP_UNIT : NULL일경우 ℃ 으로 처리
			// AMB 처리
			// ----------------
			
			sTempUnit = m.get("TEMP_UNIT")==null?"":(String)m.get("TEMP_UNIT");
			if (isTransUnitData("1", listUnitChg, sTempUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR");
				m.put("TEMP_NOR",  convWithUnit("1",engine, "TEMP_NOR", m.get("TEMP_NOR"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("TEMP_MIN",  convWithUnit("1",engine, "TEMP_MIN", m.get("TEMP_MIN"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("TEMP_MAX", convWithUnit("1",engine, "TEMP_MAX", m.get("TEMP_MAX"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			}else {
				m.put("TEMP_NOR",  m.get("TEMP_NOR") == null || "".equals(String.valueOf(m.get("TEMP_NOR")))? null:asDouble2(m.get("TEMP_NOR")));
				m.put("TEMP_MIN",  m.get("TEMP_MIN") == null || "".equals(String.valueOf(m.get("TEMP_MIN")))? null:asDouble2(m.get("TEMP_MIN")));
				m.put("TEMP_MAX",  m.get("TEMP_MAX") == null || "".equals(String.valueOf(m.get("TEMP_MAX")))? null:asDouble2(m.get("TEMP_MAX")));
			}
			
			// 비중 - 단위환산보다는 Null 처리를 위한 공통 처리를 위함
			m.put("SPEC_GRAVITY_NOR",  convWithUnit("1",engine, "SPEC_GRAVITY_NOR", m.get("SPEC_GRAVITY_NOR"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("SPEC_GRAVITY_MIN",  convWithUnit("1",engine,"SPEC_GRAVITY_MIN", m.get("SPEC_GRAVITY_MIN"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMin ));
			m.put("SPEC_GRAVITY_MAX", convWithUnit("1",engine, "SPEC_GRAVITY_MAX", m.get("SPEC_GRAVITY_MAX"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			
			// ----------------
			// 점도 : Viscosity
			// cP 기준
			// ----------------
			if (isTransUnitData("1", listUnitChg, m.get("VISC_UNIT"))){
				aBaseUnit = unitConvBase(listUnitCode, "VISC_NOR");
				m.put("VISC_NOR", convWithUnit("1",engine,"VISC_NOR", m.get("VISC_NOR"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityNor));
				m.put("VISC_MIN", convWithUnit("1",engine,"VISC_MIN", m.get("VISC_MIN"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMin));
				m.put("VISC_MAX", convWithUnit("1",engine,"VISC_MAX", m.get("VISC_MAX"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMax));
			}else {
				m.put("VISC_NOR",  m.get("VISC_NOR") == null || "".equals(String.valueOf(m.get("VISC_NOR")))? null:asDouble2(m.get("VISC_NOR")));
				m.put("VISC_MIN",  m.get("VISC_MIN") == null || "".equals(String.valueOf(m.get("VISC_MIN")))? null:asDouble2(m.get("VISC_MIN")));
				m.put("VISC_MAX",  m.get("VISC_MAX") == null || "".equals(String.valueOf(m.get("VISC_MAX")))? null:asDouble2(m.get("VISC_MAX")));
			}
			
			// ----------------
			// 증기압력 : Vapor Pressure
			//- BARA 기준
			// ----------------
			String vapPresUnit = m.get("VAP_PRES_UNIT")==null?"":String.valueOf(m.get("VAP_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, vapPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "VAP_PRES_NOR");
				m.put("VAP_PRES_NOR", convWithUnit("1",engine,"VAP_PRES_NOR", m.get("VAP_PRES_NOR"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("VAP_PRES_MIN", convWithUnit("1",engine,"VAP_PRES_MIN", m.get("VAP_PRES_MIN"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("VAP_PRES_MAX", convWithUnit("1",engine,"VAP_PRES_MAX", m.get("VAP_PRES_MAX"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			}else {
				m.put("VAP_PRES_NOR",  m.get("VAP_PRES_NOR") == null || "".equals(String.valueOf(m.get("VAP_PRES_NOR")))? null:asDouble2(m.get("VAP_PRES_NOR")));
				m.put("VAP_PRES_MIN",  m.get("VAP_PRES_MIN") == null || "".equals(String.valueOf(m.get("VAP_PRES_MIN")))? null:asDouble2(m.get("VAP_PRES_MIN")));
				m.put("VAP_PRES_MAX",  m.get("VAP_PRES_MAX") == null || "".equals(String.valueOf(m.get("VAP_PRES_MAX")))? null:asDouble2(m.get("VAP_PRES_MAX")));
			}
			
			// ----------------
			// 씰챔버압력 : Seal Chamber Pressure
			//- BARG 기준
			// ----------------
			String scPresUnit = m.get("SEAL_CHAM_UNIT")==null?"":String.valueOf(m.get("SEAL_CHAM_UNIT"));
			if (isTransUnitData("1", listUnitChg, scPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_CHAM_NOR");
				m.put("SEAL_CHAM_NOR", convWithUnit("1",engine,"SEAL_CHAM_NOR", m.get("SEAL_CHAM_NOR"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SEAL_CHAM_MIN", convWithUnit("1",engine,"SEAL_CHAM_MIN", m.get("SEAL_CHAM_MIN"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SEAL_CHAM_MAX", convWithUnit("1",engine,"SEAL_CHAM_MAX", m.get("SEAL_CHAM_MAX"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			}else {
				m.put("SEAL_CHAM_NOR",  m.get("SEAL_CHAM_NOR") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_NOR")))? null:asDouble2(m.get("SEAL_CHAM_NOR")));
				m.put("SEAL_CHAM_MIN",  m.get("SEAL_CHAM_MIN") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MIN")))? null:asDouble2(m.get("SEAL_CHAM_MIN")));
				m.put("SEAL_CHAM_MAX",  m.get("SEAL_CHAM_MAX") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MAX")))? null:asDouble2(m.get("SEAL_CHAM_MAX")));
			}
			
			// ----------------
			// 흡입압력 : SUCT_PRES_NOR
			//- BARG 기준
			// ----------------
			String suctPresUnit = m.get("SUCT_PRES_UNIT")==null?"":String.valueOf(m.get("SUCT_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, suctPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SUCT_PRES_NOR");
				m.put("SUCT_PRES_NOR", convWithUnit("1",engine,"SUCT_PRES_NOR", m.get("SUCT_PRES_NOR"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SUCT_PRES_MIN", convWithUnit("1",engine,"SUCT_PRES_MIN", m.get("SUCT_PRES_MIN"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SUCT_PRES_MAX", convWithUnit("1",engine,"SUCT_PRES_MAX", m.get("SUCT_PRES_MAX"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			}else {
				m.put("SUCT_PRES_NOR",  m.get("SUCT_PRES_NOR") == null || "".equals(String.valueOf(m.get("SUCT_PRES_NOR")))? null:asDouble2(m.get("SUCT_PRES_NOR")));
				m.put("SUCT_PRES_MIN",  m.get("SUCT_PRES_MIN") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MIN")))? null:asDouble2(m.get("SUCT_PRES_MIN")));
				m.put("SUCT_PRES_MAX",  m.get("SUCT_PRES_MAX") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MAX")))? null:asDouble2(m.get("SUCT_PRES_MAX")));
			}
			
			// ----------------
			// 배출압력 : DISCH_PRES_NOR
			//- BARG 기준
			// ----------------
			String dischPresUnit = m.get("DISCH_PRES_UNIT")==null?"":String.valueOf(m.get("DISCH_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, dischPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "DISCH_PRES_NOR");
				m.put("DISCH_PRES_NOR", convWithUnit("1",engine,"DISCH_PRES_NOR", m.get("DISCH_PRES_NOR"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("DISCH_PRES_MIN", convWithUnit("1",engine,"DISCH_PRES_MIN", m.get("DISCH_PRES_MIN"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("DISCH_PRES_MAX", convWithUnit("1",engine,"DISCH_PRES_MAX", m.get("DISCH_PRES_MAX"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			}else {
				m.put("DISCH_PRES_NOR",  m.get("DISCH_PRES_NOR") == null || "".equals(String.valueOf(m.get("DISCH_PRES_NOR")))? null:asDouble2(m.get("DISCH_PRES_NOR")));
				m.put("DISCH_PRES_MIN",  m.get("DISCH_PRES_MIN") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MIN")))? null:asDouble2(m.get("DISCH_PRES_MIN")));
				m.put("DISCH_PRES_MAX",  m.get("DISCH_PRES_MAX") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MAX")))? null:asDouble2(m.get("DISCH_PRES_MAX")));
			}
			
			// ----------------
			// 배리어 Fluid 압력 : BARR_PRES_PRES
			//- BARG 기준
			// ----------------
			String barrPresUnit = m.get("BARR_PRES_UNIT")==null?"":String.valueOf(m.get("BARR_PRES_UNIT"));
			if (isTransUnitData("1", listUnitChg, dischPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "BARR_PRES_PRES");
				m.put("BARR_PRES_PRES", convWithUnit("1",engine,"BARR_PRES_PRES", m.get("BARR_PRES_PRES"), barrPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			}else {
				m.put("BARR_PRES_PRES",  m.get("BARR_PRES_PRES") == null || "".equals(String.valueOf(m.get("BARR_PRES_PRES")))? null:asDouble2(m.get("BARR_PRES_PRES")));
			}
			
			// RPM - Shaft Speed  ==> 현재 RPM_UNIT 단위정보 없음. RPM 자체가 단위
			aBaseUnit = unitConvBase(listUnitCode, "RPM_NOR");
			m.put("RPM_NOR", convWithUnit("1",engine,"RPM_NOR", m.get("RPM_NOR"), "RPM", aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("RPM_MIN", convWithUnit("1",engine,"RPM_MIN", m.get("RPM_MIN"), "RPM",  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			m.put("RPM_MAX", convWithUnit("1",engine,"RPM_MAX", m.get("RPM_MAX"), "RPM",  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			
			
			// Seal Size
			String sealSizeInnerUnit = m.get("SEAL_SIZE_I_UNIT")==null?"":String.valueOf(m.get("SEAL_SIZE_I_UNIT"));
			if (isTransUnitData("1", listUnitChg, sealSizeInnerUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE_I");
				m.put("SEAL_SIZE_I", convWithUnit("1",engine,"SEAL_SIZE_I", m.get("SEAL_SIZE_I"), sealSizeInnerUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			}else {
				m.put("SEAL_SIZE_I",  m.get("SEAL_SIZE_I") == null || "".equals(String.valueOf(m.get("SEAL_SIZE_I")))? null:asDouble2(m.get("SEAL_SIZE_I")));
			}
			
			String sealSizeMiddleUnit = m.get("SEAL_SIZE_M_UNIT")==null?"":String.valueOf(m.get("SEAL_SIZE_M_UNIT"));
			if (isTransUnitData("1", listUnitChg, sealSizeMiddleUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE_M");
				m.put("SEAL_SIZE_M", convWithUnit("1",engine,"SEAL_SIZE_M", m.get("SEAL_SIZE_M"), sealSizeMiddleUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			}else {
				m.put("SEAL_SIZE_M",  m.get("SEAL_SIZE_M") == null || "".equals(String.valueOf(m.get("SEAL_SIZE_M")))? null:asDouble2(m.get("SEAL_SIZE_M")));
			}
			
			String sealSizeOuterUnit = m.get("SEAL_SIZE_O_UNIT")==null?"":String.valueOf(m.get("SEAL_SIZE_O_UNIT"));
			if (isTransUnitData("1", listUnitChg, sealSizeOuterUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE_O");
				m.put("SEAL_SIZE_O", convWithUnit("1",engine,"SEAL_SIZE_O", m.get("SEAL_SIZE_O"), sealSizeMiddleUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			}else {
				m.put("SEAL_SIZE_O",  m.get("SEAL_SIZE_O") == null || "".equals(String.valueOf(m.get("SEAL_SIZE_O")))? null:asDouble2(m.get("SEAL_SIZE_O")));
			}
			
			// ----------------
			// 크기
			//  MM 기준  / SHAFT_UNIT : NULL일경우 MM 으로 처리 
			// ----------------
			// SHAFT_SIZE
			aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE"); //기본 unit을 MM으로 변경
			m.put("SHAFT_SIZE", convWithUnit("1",engine, "SHAFT_SIZE", m.get("SHAFT_SIZE"), m.get("SHAFT_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			
			// ----------------
			// Grouping 정보
			// COL_VALUE,GRP_CODE,GRP,GRP_SUB
			// ----------------
			
			// Product Group
			//sProductTypeG = "";
			if (m.get("PRODUCT")!=null) {
				sProduct = String.valueOf(m.get("PRODUCT"));
				sProductTypeG = getGroupingStr(sProduct,listGrpProductInfo,productGroupHierList);
				m.put("PRODUCT_G", sProductTypeG );
			}else {
				m.put("PRODUCT_G", "-" );
			}
						
			// Pump Type Group
			sPumpType = (m.get("PUMP_TYPE")==null||"".equals(String.valueOf(m.get("PUMP_TYPE"))))?""+getBlankDataDefault("PUMP_TYPE"):String.valueOf(m.get("PUMP_TYPE"));
			sPumpTypeG = getGroupingStr(sPumpType,listGrpPumpTypeInfo,null);
			m.put("PUMP_TYPE_G", sPumpTypeG );
			
			// Equip Mfg Group
			sEquipMfg = (m.get("EQUIP_MFG")==null||"".equals(String.valueOf(m.get("EQUIP_MFG"))))?""+getBlankDataDefault("EQUIP_MFG"):String.valueOf(m.get("EQUIP_MFG"));
			sEquipMfgG = getGroupingStr(sEquipMfg,listGrpEquipMfgInfo,null);
			m.put("EQUIP_MFG_G", sEquipMfgG );
			
			// Ultimate User Group
			sUltimateUser = (m.get("ULTIMATE_USER")==null||"".equals(String.valueOf(m.get("ULTIMATE_USER"))))?""+getBlankDataDefault("ULTIMATE_USER"):String.valueOf(m.get("ULTIMATE_USER"));
			sUltimateUserG = getGroupingStr(sUltimateUser,listGrpUltimateUserInfo,null);
			m.put("ULTIMATE_USER_G", sUltimateUserG );
			
			
			// 기타 처리 -------------------------------	
			// Equip Type
			// seal text 제거 -> ERP Data에서 Seal 문자가 붙어서 넘어옴.
			m.put("EQUIP_TYPE", String.valueOf(m.get("EQUIP_TYPE")).replace("Seal", "").trim() );
			
			// Seal Type (결합)
			sealTypeCombined = StringUtil.get(m.get("SEAL_TYPE_I"));
			if(!"".equals(StringUtil.get(m.get("SEAL_TYPE_M") ))){
				sealTypeCombined += "/"+ StringUtil.get(m.get("SEAL_TYPE_M") );
			}
			if(!"".equals(StringUtil.get(m.get("SEAL_TYPE_O") ))){
				sealTypeCombined += "/"+ StringUtil.get(m.get("SEAL_TYPE_O") );
			}
			m.put("SEAL_TYPE",sealTypeCombined);
			
			// Material (결합) 
			materialCombined_I = StringUtil.get(m.get("MATERIAL_I1"));
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I2") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I2") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I3") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I3") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I4") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I4") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I5") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I5") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I6") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I6") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_I7") ))) materialCombined_I += " " + StringUtil.get(m.get("MATERIAL_I7") );
					
			materialCombined_M = StringUtil.get(m.get("MATERIAL_M1"));
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M2") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M2") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M3") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M3") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M4") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M4") );		
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M5") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M5") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M6") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M6") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_M7") ))) materialCombined_M += " " + StringUtil.get(m.get("MATERIAL_M7") );
					
			materialCombined_O = StringUtil.get(m.get("MATERIAL_O1"));
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O2") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O2") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O3") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O3") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O4") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O4") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O5") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O5") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O6") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O6") );
			if (!"".equals(StringUtil.get(m.get("MATERIAL_O7") ))) materialCombined_O += " " + StringUtil.get(m.get("MATERIAL_O7") );
			
			// 각각의 재질정보를 공백을 좌우 적용하여 / 로 연결
			// ex : 5 U K X / 5 U K X
			materialCombined = materialCombined_I;
			if (!"".equals(materialCombined_M)) materialCombined += " / " + materialCombined_M;
			if (!"".equals(materialCombined_O)) materialCombined += " / " + materialCombined_O;
					
			m.put("MATERIAL",materialCombined);		
			
			// --------------------------------			
			// 추가 필드 blank 처리
			// --------------------------------
			//Seal type
			if (m.get("SEAL_TYPE") ==null || "".equals(String.valueOf(m.get("SEAL_TYPE")))) {
				m.put("SEAL_TYPE", "-" );
			}
			//API Plan
			if (m.get("API_PLAN") ==null || "".equals(String.valueOf(m.get("API_PLAN")))) {
				m.put("API_PLAN", "-" );
			}
			//Material
			if (m.get("MATERIAL") ==null || "".equals(String.valueOf(m.get("MATERIAL")))) {
				m.put("MATERIAL", "-" );
			}
			//Equip Type
			if (m.get("EQUIP_TYPE") ==null || "".equals(String.valueOf(m.get("EQUIP_TYPE")))) {
				m.put("EQUIP_TYPE", "-" );
			}
			
			// ----------------
			// Insert row
			// ----------------
			//mLMapper.insertDataCnv1(m);	
			resultList.add(m);
						
		} // end for (Map<String,Object> m : list )
		
		System.out.println("Insert 시작 resultList.size()  :  " + resultList.size());
		
		
		if(resultList.size() > 0) {
			for(Map<String,Object> paramMap : resultList) {
				// delete
				mLMapper.deleteDataCnv2_1(paramMap);
				// insert
				mLMapper.insertDataCnv2_1(paramMap);
			}
		}
				
		//Bulk Insert
//		if(resultList.size() > 0) {
//			int iCommit=0;
//	    	int iBulkSize=29; //인자 2100개 제한(Sql Server)
//	    	double iStepCnt =  Math.ceil(resultList.size()/iBulkSize);
//	    	List<Map<String,Object>> subList = null;
//	    	Map<String,Object> paramMap = new HashMap<String,Object>();
//	    	for(int i=0;i<iStepCnt;i++) {
//	    		subList = resultList.subList(i*iBulkSize, (i+1)*iBulkSize);
//	    		paramMap.put("list", subList);
//	    		mLMapper.insertDataCnv2_1(paramMap);	
//	    		System.out.println(iCommit + " : ok");
//	    		iCommit++;
//			}
//			subList = resultList.subList(iCommit*iBulkSize, list.size());
//			System.out.println("last count :  " + subList.size());
//			if (subList.size()>0) {
//				paramMap.put("list", subList);
//				mLMapper.insertDataCnv2_1(paramMap);	
//			}
//		}
		
	}
	
	/**
	 * org.-data to conv1
	 */
	@Deprecated
	public void __old_orgToCnv1(Map<String,Object> param) throws Exception{
		
		System.out.println("orgToConv1 start");
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		
		
		// ------------------------------------------------------
		// 범위 조정 필요 : 현재 전체 데이터 대상으로 처리 중
		
		// 변환 Change 데이터 초기화
		mLMapper.removeCNV1();
		//System.out.println("truncate ok");
		
		// 변환 Orignal 데이터 조회
		List<Map<String,Object>> list = mLMapper.getOrgList();
		// ------------------------------------------------------
		
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
				
		// 단위코드정보
		List<Map<String,Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();
		// 단위변환정보
		List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
		// Text 입력값 변환정보
		List<Map<String,Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();
		// SSU 변환 정보 (Visc 용)
		List<Map<String,Object>> listSsuChg = mLMapper.getSsuChglInfo();
		// Grouping 정보
		List<Map<String,Object>> listGrpPumpTypeInfo = mLMapper.getGroupingInfo("pumpType");
		List<Map<String,Object>> listGrpProductInfo = mLMapper.getGroupingInfo("product");
		
		// ProductGroup Hierarchy
		List<Map<String,Object>> productGroupHierList = mLMapper.getGroupingHier(null);
		
		System.out.println("list : " + list.size());
		System.out.println("listUnitCode : " + listUnitCode.size());
		System.out.println("listUnitChg : " + listUnitChg.size());
		System.out.println("listTransTxtVal : " + listTransTxtVal.size());
		
		// 컬럼별 기준단위
		String aBaseUnit = null;
		String sTempUnit = null;
		
		String sPumpTypeG = "";
		String sProductTypeG = "";
		String sProduct = "";
		String sPumpType = "";
		
				
		//Map<String,Object> m_dummy = null;
		//int idx=1;
		for (Map<String,Object> m : list ){
			
			//System.out.println("No : " + m.get("IDX_NO") + " : " + (idx++));
			//m_dummy =(Map<String,Object>)m.clone(); // 현재 데이터셋을 복제한다.
			//m_dummy = new HashMap<String,Object>();
			//m_dummy.putAll(m); // 현재 데이터셋을 복제한다.
			
			String sSpecGravityNor =  m.get("SPEC_GRAVITY_NOR")==null?"":String.valueOf(m.get("SPEC_GRAVITY_NOR"));
			String sSpecGravityMin =  m.get("SPEC_GRAVITY_MIN")==null?"":String.valueOf(m.get("SPEC_GRAVITY_MIN"));
			String sSpecGravityMax = m.get("SPEC_GRAVITY_MAX")==null?"":String.valueOf(m.get("SPEC_GRAVITY_MAX"));
			if(!"".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityMin = sSpecGravityNor;
				sSpecGravityMax = sSpecGravityNor;
			}else if("".equals(sSpecGravityNor) && !"".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityNor = sSpecGravityMin;
				sSpecGravityMax = sSpecGravityMin;
			}else if("".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && !"".equals(sSpecGravityMax) ) {
				sSpecGravityNor = sSpecGravityMax;
				sSpecGravityMin = sSpecGravityMax;
			}else if(!"".equals(sSpecGravityNor) && !"".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityMax = sSpecGravityNor;
			}else if(!"".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && !"".equals(sSpecGravityMax) ) {
				sSpecGravityMin = sSpecGravityNor;
			}else if("".equals(sSpecGravityNor) && !"".equals(sSpecGravityMin)  && !"".equals(sSpecGravityMax) ) {
				sSpecGravityNor = sSpecGravityMax;
			}else if("".equals(sSpecGravityNor) && "".equals(sSpecGravityMin)  && "".equals(sSpecGravityMax) ) {
				sSpecGravityNor = "1";
				sSpecGravityMin = "1";
				sSpecGravityMax = "1";
			}
			
			// ----------------
			// 크기
			//  MM 기준  / SHAFT_UNIT : NULL일경우 MM 으로 처리 
			// ----------------
			// SHAFT_SIZE
			/*aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE"); //기본 unit을 MM으로 변경
			m.put("SHAFT_SIZE", convWithUnit("1",engine, "SHAFT_SIZE", m.get("SHAFT_SIZE"), m.get("SHAFT_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			m.put("SHAFT_UNIT", aBaseUnit );*/ //원본 CNV1 - 조회용
			if(m.get("SHAFT_SIZE") != null && m.get("SHAFT_SIZE").toString().equals("")){
				m.put("SHAFT_SIZE",null); //SHAFT_SIZEr 값 체크 후 null 또는 "" 일경우 NULL로 변환 => 공백으로 변환시 에러발생.
			}
			
			// ----------------
			// 온도
			// ℃ 기준 / TEMP_UNIT : NULL일경우 ℃ 으로 처리
			// AMB 처리
			// ----------------
			
// 2020.11.09 오류수정				
//			sTempUnit = m.get("TEMP_UNIT")==null?"":(String)m.get("TEMP_UNIT");
//			if ("℃".equals(sTempUnit)){
//				sTempUnit="C";
//			}else if ("℉".equals(sTempUnit)){
//				sTempUnit="F";
//			}else {
//				sTempUnit="C";
//			}
			//ERP DATA과 상이한 형태의 값으로 들어와서 임시처리 20.11.13
			sTempUnit = m.get("TEMP_UNIT")==null?"":(String)m.get("TEMP_UNIT");
			if ("°C".equals(sTempUnit)){
				sTempUnit="℃";
			}else if ("°F".equals(sTempUnit)){
				sTempUnit="℉";
			}
			if (isTransUnitData("1", listUnitChg, sTempUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR");
				m.put("TEMP_NOR",  convWithUnit("1",engine, "TEMP_NOR", m.get("TEMP_NOR"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("TEMP_MIN",  convWithUnit("1",engine, "TEMP_MIN", m.get("TEMP_MIN"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("TEMP_MAX", convWithUnit("1",engine, "TEMP_MAX", m.get("TEMP_MAX"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("TEMP_UNIT", aBaseUnit );
				m.put("TEMP_UNIT", (m.get("TEMP_UNIT")!=null&&!"".equals((String)m.get("TEMP_UNIT")))?aBaseUnit:"" );
			}else {
				m.put("TEMP_NOR",  m.get("TEMP_NOR") == null || "".equals(String.valueOf(m.get("TEMP_NOR")))? null:asDouble2(m.get("TEMP_NOR")));
				m.put("TEMP_MIN",  m.get("TEMP_MIN") == null || "".equals(String.valueOf(m.get("TEMP_MIN")))? null:asDouble2(m.get("TEMP_MIN")));
				m.put("TEMP_MAX",  m.get("TEMP_MAX") == null || "".equals(String.valueOf(m.get("TEMP_MAX")))? null:asDouble2(m.get("TEMP_MAX")));
			}
			
			// 비중 - 단위환산보다는 Null 처리를 위한 공통 처리를 위함
			m.put("SPEC_GRAVITY_NOR",  convWithUnit("1",engine, "SPEC_GRAVITY_NOR", m.get("SPEC_GRAVITY_NOR"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("SPEC_GRAVITY_MIN",  convWithUnit("1",engine,"SPEC_GRAVITY_MIN", m.get("SPEC_GRAVITY_MIN"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMin ));
			m.put("SPEC_GRAVITY_MAX", convWithUnit("1",engine, "SPEC_GRAVITY_MAX", m.get("SPEC_GRAVITY_MAX"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			
			// ----------------
			// 점도 : Viscosity
			// cP 기준
			// ----------------
			if (isTransUnitData("1", listUnitChg, m.get("VISC_UNIT"))){
				aBaseUnit = unitConvBase(listUnitCode, "VISC_NOR");
				m.put("VISC_NOR", convWithUnit("1",engine,"VISC_NOR", m.get("VISC_NOR"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityNor));
				m.put("VISC_MIN", convWithUnit("1",engine,"VISC_MIN", m.get("VISC_MIN"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMin));
				m.put("VISC_MAX", convWithUnit("1",engine,"VISC_MAX", m.get("VISC_MAX"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMax));
				//m.put("VISC_UNIT", aBaseUnit );
				m.put("VISC_UNIT", (m.get("VISC_UNIT")!=null&&!"".equals((String)m.get("VISC_UNIT")))?aBaseUnit:"" );
			}else {
				m.put("VISC_NOR",  m.get("VISC_NOR") == null || "".equals(String.valueOf(m.get("VISC_NOR")))? null:asDouble2(m.get("VISC_NOR")));
				m.put("VISC_MIN",  m.get("VISC_MIN") == null || "".equals(String.valueOf(m.get("VISC_MIN")))? null:asDouble2(m.get("VISC_MIN")));
				m.put("VISC_MAX",  m.get("VISC_MAX") == null || "".equals(String.valueOf(m.get("VISC_MAX")))? null:asDouble2(m.get("VISC_MAX")));
			}
			
			
			// ----------------
			// 증기압력 : Vapor Pressure
			//- BARA 기준
			// ----------------
			String vapPresUnit = m.get("VAP_PRES_UNIT")==null?"":String.valueOf(m.get("VAP_PRES_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(vapPresUnit)) {
				vapPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(vapPresUnit)) {
				vapPresUnit = "KG/CM2G";
			}
			if (isTransUnitData("1", listUnitChg, vapPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "VAP_PRES_NOR");
				m.put("VAP_PRES_NOR", convWithUnit("1",engine,"VAP_PRES_NOR", m.get("VAP_PRES_NOR"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("VAP_PRES_MIN", convWithUnit("1",engine,"VAP_PRES_MIN", m.get("VAP_PRES_MIN"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("VAP_PRES_MAX", convWithUnit("1",engine,"VAP_PRES_MAX", m.get("VAP_PRES_MAX"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("VAP_PRES_UNIT", aBaseUnit );
				m.put("VAP_PRES_UNIT", aBaseUnit );
			}else {
				m.put("VAP_PRES_NOR",  m.get("VAP_PRES_NOR") == null || "".equals(String.valueOf(m.get("VAP_PRES_NOR")))? null:asDouble2(m.get("VAP_PRES_NOR")));
				m.put("VAP_PRES_MIN",  m.get("VAP_PRES_MIN") == null || "".equals(String.valueOf(m.get("VAP_PRES_MIN")))? null:asDouble2(m.get("VAP_PRES_MIN")));
				m.put("VAP_PRES_MAX",  m.get("VAP_PRES_MAX") == null || "".equals(String.valueOf(m.get("VAP_PRES_MAX")))? null:asDouble2(m.get("VAP_PRES_MAX")));
			}
			// ----------------
			// 씰챔버압력 : Seal Chamber Pressure
			//- BARG 기준
			// ----------------
			String scPresUnit = m.get("SEAL_CHAM_UNIT")==null?"":String.valueOf(m.get("SEAL_CHAM_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(scPresUnit)) {
				scPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(scPresUnit)) {
				scPresUnit = "KG/CM2G";
			}
			if (isTransUnitData("1", listUnitChg, scPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_CHAM_NOR");
				m.put("SEAL_CHAM_NOR", convWithUnit("1",engine,"SEAL_CHAM_NOR", m.get("SEAL_CHAM_NOR"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SEAL_CHAM_MIN", convWithUnit("1",engine,"SEAL_CHAM_MIN", m.get("SEAL_CHAM_MIN"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SEAL_CHAM_MAX", convWithUnit("1",engine,"SEAL_CHAM_MAX", m.get("SEAL_CHAM_MAX"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("SEAL_CHAM_UNIT", aBaseUnit );
				m.put("SEAL_CHAM_UNIT", aBaseUnit);
			}else {
				m.put("SEAL_CHAM_NOR",  m.get("SEAL_CHAM_NOR") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_NOR")))? null:asDouble2(m.get("SEAL_CHAM_NOR")));
				m.put("SEAL_CHAM_MIN",  m.get("SEAL_CHAM_MIN") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MIN")))? null:asDouble2(m.get("SEAL_CHAM_MIN")));
				m.put("SEAL_CHAM_MAX",  m.get("SEAL_CHAM_MAX") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MAX")))? null:asDouble2(m.get("SEAL_CHAM_MAX")));
			}
			
			// ----------------
			// 흡입압력 : SUCT_PRES_NOR
			//- BARG 기준
			// ----------------
			String suctPresUnit = m.get("SUCT_PRES_UNIT")==null?"":String.valueOf(m.get("SUCT_PRES_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(suctPresUnit)) {
				suctPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(suctPresUnit)) {
				suctPresUnit = "KG/CM2G";
			}
			if (isTransUnitData("1", listUnitChg, suctPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SUCT_PRES_NOR");
				m.put("SUCT_PRES_NOR", convWithUnit("1",engine,"SUCT_PRES_NOR", m.get("SUCT_PRES_NOR"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SUCT_PRES_MIN", convWithUnit("1",engine,"SUCT_PRES_MIN", m.get("SUCT_PRES_MIN"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SUCT_PRES_MAX", convWithUnit("1",engine,"SUCT_PRES_MAX", m.get("SUCT_PRES_MAX"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("SUCT_PRES_UNIT", aBaseUnit );
				m.put("SUCT_PRES_UNIT", aBaseUnit);
			}else {
				m.put("SUCT_PRES_NOR",  m.get("SUCT_PRES_NOR") == null || "".equals(String.valueOf(m.get("SUCT_PRES_NOR")))? null:asDouble2(m.get("SUCT_PRES_NOR")));
				m.put("SUCT_PRES_MIN",  m.get("SUCT_PRES_MIN") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MIN")))? null:asDouble2(m.get("SUCT_PRES_MIN")));
				m.put("SUCT_PRES_MAX",  m.get("SUCT_PRES_MAX") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MAX")))? null:asDouble2(m.get("SUCT_PRES_MAX")));
			}
			
			// ----------------
			// 배출압력 : DISCH_PRES_NOR
			//- BARG 기준
			// ----------------
			String dischPresUnit = m.get("DISCH_PRES_UNIT")==null?"":String.valueOf(m.get("DISCH_PRES_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(dischPresUnit)) {
				dischPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(dischPresUnit)) {
				dischPresUnit = "KG/CM2G";
			}
			if (isTransUnitData("1", listUnitChg, dischPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "DISCH_PRES_NOR");
				m.put("DISCH_PRES_NOR", convWithUnit("1",engine,"DISCH_PRES_NOR", m.get("DISCH_PRES_NOR"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("DISCH_PRES_MIN", convWithUnit("1",engine,"DISCH_PRES_MIN", m.get("DISCH_PRES_MIN"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("DISCH_PRES_MAX", convWithUnit("1",engine,"DISCH_PRES_MAX", m.get("DISCH_PRES_MAX"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				//m.put("DISCH_PRES_UNIT", aBaseUnit );
				m.put("DISCH_PRES_UNIT",aBaseUnit);
			}else {
				m.put("DISCH_PRES_NOR",  m.get("DISCH_PRES_NOR") == null || "".equals(String.valueOf(m.get("DISCH_PRES_NOR")))? null:asDouble2(m.get("DISCH_PRES_NOR")));
				m.put("DISCH_PRES_MIN",  m.get("DISCH_PRES_MIN") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MIN")))? null:asDouble2(m.get("DISCH_PRES_MIN")));
				m.put("DISCH_PRES_MAX",  m.get("DISCH_PRES_MAX") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MAX")))? null:asDouble2(m.get("DISCH_PRES_MAX")));
			}
			
			// RPM - Shaft Speed ==> 현재 RPM UNIT 컬럼 없음 20.11.09
			aBaseUnit = unitConvBase(listUnitCode, "RPM_NOR");
			m.put("RPM_NOR", convWithUnit("1",engine,"RPM_NOR", m.get("RPM_NOR"), null, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("RPM_MIN", convWithUnit("1",engine,"RPM_MIN", m.get("RPM_MIN"), null,  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			m.put("RPM_MAX", convWithUnit("1",engine,"RPM_MAX", m.get("RPM_MAX"), null,  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			//m.put("RPM_UNIT", aBaseUnit );
			//m.put("RPM_UNIT", (m.get("RPM_UNIT")!=null&&!"".equals((String)m.get("RPM_UNIT")))?aBaseUnit:"" );
			
			// 기타
			//m.put("PUMP_TYPE", convWithUnit("1",engine,"PUMP_TYPE", m.get("PUMP_TYPE"), null, null, null, listTransTxtVal, null, null));
			//m.put("PUMP_TYPE",   (m.get("PUMP_TYPE")==null||"".equals(String.valueOf(m.get("PUMP_TYPE"))))?getBlankDataDefault("PUMP_TYPE"):String.valueOf(m.get("PUMP_TYPE")));
			
			// ----------------
			// 크기 Seal Size
			//  MM 기준  / SHAFT_UNIT : NULL일경우 MM 으로 처리 
			// ----------------
			// seal size ->  MM가 붙은 데이터가 있어서 IN로 통일함
			/*String sealSize = m.get("SEAL_SIZE")==null?"":(String)m.get("SEAL_SIZE");
			String[] sealSizes = sealSize.split("[/]");
			// MM : IN * 25.4
			sealSize = "";  
			for(String s : sealSizes) {
				if(s.contains("MM")) {
					s = s.replace("MM", "");
				}else {
					try {
						s = String.valueOf(Math.round(Double.valueOf(s) *25.4*10)/10);
					}catch(Exception e) {
						LOGGER.info("seal size 변환 오류 : " + m.get("IDX_NO") + " : " + s);
					} 
				}
				sealSize = sealSize + s + "/";
			}
			if (sealSize.endsWith("/")) {
				sealSize = sealSize.substring(0, sealSize.length()-1);
			}
			aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE");
			m.put("SEAL_SIZE", sealSize );*/
			//0826 Seal Size 데이터변환 처리 부분 제거 ==> SEAL_SIZE 주석처리
			
			// ----------------
			// Grouping 정보
			// COL_VALUE,GRP_CODE,GRP,GRP_SUB
			// ----------------
			
			// Product
			//sProductTypeG = "";
			if (m.get("PRODUCT")!=null) {
				sProduct = String.valueOf(m.get("PRODUCT"));
				sProductTypeG = getGroupingStr(sProduct,listGrpProductInfo,productGroupHierList);
				m.put("PRODUCT_G", sProductTypeG );
			}else {
				m.put("PRODUCT_G", "-" );
			}
						
			// Pump Type
            //sPumpTypeG = "";
			//if (m.get("PUMP_TYPE")!=null) {
				//sPumpType = String.valueOf(m.get("PUMP_TYPE"));
				sPumpType = (m.get("PUMP_TYPE")==null||"".equals(String.valueOf(m.get("PUMP_TYPE"))))?""+getBlankDataDefault("PUMP_TYPE"):String.valueOf(m.get("PUMP_TYPE"));
				sPumpTypeG = getGroupingStr(sPumpType,listGrpPumpTypeInfo,null);
				m.put("PUMP_TYPE_G", sPumpTypeG );
			//}else {
			//	m.put("PUMP_TYPE_G", "-" );
			//}
				
			// Equip Type
			// seal text 제거
			m.put("EQUIP_TYPE", String.valueOf(m.get("EQUIP_TYPE")).replace("Seal", "").trim() );	
				
			
			// ----------------
			// Insert row
			// ----------------
			//mLMapper.insertDataCnv1(m);	
			resultList.add(m);
						
		} // end for (Map<String,Object> m : list )
		
		System.out.println("Insert 시작 resultList.size()  :  " + resultList.size());
		
		if(resultList.size() > 0) {
			for(Map<String,Object> paramMap : resultList) {
				mLMapper.insertDataCnv1(paramMap);
			}
		}
		
					
		//Bulk Insert
		/*
		if(resultList.size() > 0) {
			int iCommit=0;
	    	int iBulkSize=29; //인자 2100개 제한(Sql Server)
	    	double iStepCnt =  Math.ceil(resultList.size()/iBulkSize);
	    	List<Map<String,Object>> subList = null;
	    	Map<String,Object> paramMap = new HashMap<String,Object>();
	    	for(int i=0;i<iStepCnt;i++) {
	    		subList = resultList.subList(i*iBulkSize, (i+1)*iBulkSize);
	    		paramMap.put("list", subList);
	    		mLMapper.insertDataCnv1(paramMap);	
	    		System.out.println(iCommit + " : ok");
	    		iCommit++;
			}
			subList = resultList.subList(iCommit*iBulkSize, list.size());
			System.out.println("last count :  " + subList.size());
			if (subList.size()>0) {
				paramMap.put("list", subList);
				mLMapper.insertDataCnv1(paramMap);	
			}
		}
		*/
	}
	
	/**
	 * org.-data to conv2
	 */
	@Deprecated
	public void orgToCnv2(Map<String,Object> param) throws Exception{
		
		System.out.println("orgToConv2 start");
		
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		// 데이터 초기화
		mLMapper.removeCNV2();
		
		//System.out.println("truncate ok");
		
		// 전체 데이터
		List<Map<String,Object>> list = mLMapper.getOrgList();
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();
				
		// 단위코드정보
		List<Map<String,Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();
		// 단위변환정보
		List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
		// Text 입력값 변환정보
		List<Map<String,Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();
		// SSU 변환 정보 (Visc 용)
		List<Map<String,Object>> listSsuChg = mLMapper.getSsuChglInfo();
		// Grouping 정보
		List<Map<String,Object>> listGrpPumpTypeInfo = mLMapper.getGroupingInfo("pumpType");
		List<Map<String,Object>> listGrpProductInfo = mLMapper.getGroupingInfo("product");
		
		// ProductGroup Hierarchy
		List<Map<String,Object>> productGroupHierList = mLMapper.getGroupingHier(null);
		
		
		System.out.println("list : " + list.size());
		System.out.println("listUnitCode : " + listUnitCode.size());
		System.out.println("listUnitChg : " + listUnitChg.size());
		System.out.println("listTransTxtVal : " + listTransTxtVal.size());
		
		// 컬럼별 기준단위
		String aBaseUnit = null;
		String sTempUnit = null;
		
		String sPumpTypeG = "";
		String sProductTypeG = "";
		String sProduct = "";
		String sPumpType = "";
		
		// 빈값 먼저 처리
		List<Map<String,Object>> list_2nd = new ArrayList<Map<String,Object>>();
//		String sAttr1 = "";
//		String sAttr2 = "";
//		Object oVal = "";
//		String [] blankChkCol = new String[]{
//				"PUMP_TYPE",
//				"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
//				"VISC_NOR","VISC_MIN","VISC_MAX",
//				"TEMP_NOR","TEMP_MIN","TEMP_MAX",
//				"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
//				"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX",
//				"SUCT_PRES_NOR","SUCT_PRES_MIN","SUCT_PRES_MAX",
//				"DISCH_PRES_NOR","DISCH_PRES_MIN","DISCH_PRES_MAX",
//				"RPM_NOR","RPM_MIN","RPM_MAX",
//				"SHAFT_SIZE"
//		};
		
		for( Map<String,Object> data : list ){
			
			//빈값 필드 처리
			data = setEmptyDataWithDefaultData(data);
			
//			for (String sCol : blankChkCol) {
//				try {
//				sAttr1 = sCol.substring(0, sCol.lastIndexOf("_"));
//				sAttr2 = sCol.substring(sCol.lastIndexOf("_")+1);
//				}catch(Exception e) {
//					sAttr1="";
//					sAttr2 = "";
//				}
//				if ("NOR".equals(sAttr2)){ // Normal일 경우
//					if ( data.get(sAttr1 + "_MIN") != null  &&  data.get(sAttr1 + "_MAX") != null) {
//						oVal = data.get(sAttr1 + "_MAX");
//					}else if ( data.get(sAttr1 + "_MIN") != null  &&  data.get(sAttr1 + "_MAX") == null) {
//						oVal = data.get(sAttr1 + "_MIN");
//					}else if ( data.get(sAttr1 + "_MIN") == null  &&  data.get(sAttr1 + "_MAX") != null) {
//						oVal = data.get(sAttr1 + "_MAX");
//					}else if ( data.get(sAttr1 + "_MIN") == null  &&  data.get(sAttr1 + "_MAX") == null) {
//						oVal = getBlankDataDefault(sAttr1);
//					}
//				}else if ("MIN".equals(sAttr2)){ // Min일 경우
//					if ( data.get(sAttr1 + "_NOR") != null  &&  data.get(sAttr1 + "_MAX") == null) {
//						oVal = data.get(sAttr1 + "_NOR");
//					}else if ( data.get(sAttr1 + "_NOR") == null  &&  data.get(sAttr1 + "_MAX") != null) {
//						oVal = data.get(sAttr1 + "_MAX");
//					}else if ( data.get(sAttr1 + "_NOR") != null  &&  data.get(sAttr1 + "_MAX") != null) {
//						oVal = data.get(sAttr1 + "_NOR");
//					}else if ( data.get(sAttr1 + "_NOR") == null  &&  data.get(sAttr1 + "_MAX") == null) {
//						oVal = getBlankDataDefault(sAttr1);
//					}
//				}else if ("MAX".equals(sAttr2)){ // Max일 경우
//					if ( data.get(sAttr1 + "_NOR") != null  &&  data.get(sAttr1 + "_MIN") == null) {
//						oVal = data.get(sAttr1 + "_NOR");
//					}else if ( data.get(sAttr1 + "_NOR") == null  &&  data.get(sAttr1 + "_MIN") != null) {
//						oVal = data.get(sAttr1 + "_MIN");
//					}else if ( data.get(sAttr1 + "_NOR") != null  &&  data.get(sAttr1 + "_MIN") != null) {
//						oVal = data.get(sAttr1 + "_NOR");
//					}else if ( data.get(sAttr1 + "_NOR") == null  &&  data.get(sAttr1 + "_MIN") == null) {
//						oVal = getBlankDataDefault(sAttr1);
//					}
//				}else { // NOR, MIN, MAX로 구분되는 필드가 아닌경우
//					if (data.get(sCol)==null || "".equals(String.valueOf(data.get(sCol)))) {
//						oVal = getBlankDataDefault(sCol);
//					}else {
//						oVal = data.get(sCol);
//					}
//				}	
//				data.put(sCol,oVal);
//			}
			list_2nd.add(data);
		}
		
		//Map<String,Object> m_dummy = null;
		int idx=1;
		for (Map<String,Object> m : list_2nd ){
			
			//System.out.println("No : " + m.get("IDX_NO") + " : " + (idx++));
			//m_dummy =(Map<String,Object>)m.clone(); // 현재 데이터셋을 복제한다.
			//m_dummy = new HashMap<String,Object>();
			//m_dummy.putAll(m); // 현재 데이터셋을 복제한다.
			
			String sSpecGravityNor =  String.valueOf(m.get("SPEC_GRAVITY_NOR"));
			String sSpecGravityMin =  String.valueOf(m.get("SPEC_GRAVITY_MIN"));
			String sSpecGravityMax =  String.valueOf(m.get("SPEC_GRAVITY_MAX"));
			
			// ----------------
			// 크기
			//  MM 기준  / SHAFT_UNIT : NULL일경우 MM 으로 처리 
			// ----------------
			// SHAFT_SIZE
			aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE");
			m.put("SHAFT_SIZE", convWithUnit("1",engine, "SHAFT_SIZE", m.get("SHAFT_SIZE"), m.get("SHAFT_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, null, null));
			m.put("SHAFT_UNIT", aBaseUnit ); //0901 CNV1 값만  주석처리 (머신러닝 모델 예측용) 
			
			// ----------------
			// 온도
			// ℃ 기준 / TEMP_UNIT : NULL일경우 ℃ 으로 처리
			// AMB 처리
			// ----------------
			
// 2020.11.09 오류수정					
//			sTempUnit = m.get("TEMP_UNIT")==null?"":(String)m.get("TEMP_UNIT");
//			if ("℃".equals(sTempUnit)){
//				sTempUnit="C";
//			}else if ("℉".equals(sTempUnit)){
//				sTempUnit="F";
//			}else {
//				sTempUnit="C";
//			}
			//ERP DATA과 상이한 형태의 값으로 들어와서 임시처리 20.11.13
			sTempUnit = m.get("TEMP_UNIT")==null?"":(String)m.get("TEMP_UNIT");
			if ("°C".equals(sTempUnit)){
				sTempUnit="℃";
			}else if ("°F".equals(sTempUnit)){
				sTempUnit="℉";
			}
			if (isTransUnitData("1", listUnitChg, sTempUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR");
				m.put("TEMP_NOR",  convWithUnit("1",engine, "TEMP_NOR", m.get("TEMP_NOR"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("TEMP_MIN",  convWithUnit("1",engine, "TEMP_MIN", m.get("TEMP_MIN"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("TEMP_MAX", convWithUnit("1",engine, "TEMP_MAX", m.get("TEMP_MAX"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				m.put("TEMP_UNIT", aBaseUnit );
			}else {
				m.put("TEMP_NOR",  m.get("TEMP_NOR") == null || "".equals(String.valueOf(m.get("TEMP_NOR")))? null:asDouble2(m.get("TEMP_NOR")));
				m.put("TEMP_MIN",  m.get("TEMP_MIN") == null || "".equals(String.valueOf(m.get("TEMP_MIN")))? null:asDouble2(m.get("TEMP_MIN")));
				m.put("TEMP_MAX",  m.get("TEMP_MAX") == null || "".equals(String.valueOf(m.get("TEMP_MAX")))? null:asDouble2(m.get("TEMP_MAX")));
			}
			
			// 비중 - 단위환산보다는 Null 처리를 위한 공통 처리를 위함
			m.put("SPEC_GRAVITY_NOR",  convWithUnit("1",engine, "SPEC_GRAVITY_NOR", m.get("SPEC_GRAVITY_NOR"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("SPEC_GRAVITY_MIN",  convWithUnit("1",engine,"SPEC_GRAVITY_MIN", m.get("SPEC_GRAVITY_MIN"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMin ));
			m.put("SPEC_GRAVITY_MAX", convWithUnit("1",engine, "SPEC_GRAVITY_MAX", m.get("SPEC_GRAVITY_MAX"), null, null, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			
			// ----------------
			// 점도 : Viscosity
			// cP 기준
			// ----------------
			if (isTransUnitData("1", listUnitChg, m.get("VISC_UNIT"))){
				aBaseUnit = unitConvBase(listUnitCode, "VISC_NOR");
				m.put("VISC_NOR", convWithUnit("1",engine,"VISC_NOR", m.get("VISC_NOR"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityNor));
				m.put("VISC_MIN", convWithUnit("1",engine,"VISC_MIN", m.get("VISC_MIN"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMin));
				m.put("VISC_MAX", convWithUnit("1",engine,"VISC_MAX", m.get("VISC_MAX"), m.get("VISC_UNIT"), aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravityMax));
				m.put("VISC_UNIT", aBaseUnit );
			}else {
				m.put("VISC_NOR",  m.get("VISC_NOR") == null || "".equals(String.valueOf(m.get("VISC_NOR")))? null:asDouble2(m.get("VISC_NOR")));
				m.put("VISC_MIN",  m.get("VISC_MIN") == null || "".equals(String.valueOf(m.get("VISC_MIN")))? null:asDouble2(m.get("VISC_MIN")));
				m.put("VISC_MAX",  m.get("VISC_MAX") == null || "".equals(String.valueOf(m.get("VISC_MAX")))? null:asDouble2(m.get("VISC_MAX")));
			}
			
			// ----------------
			// 증기압력 : Vapor Pressure
			//- BARA 기준
			// ----------------
			String vapPresUnit = m.get("VAP_PRES_UNIT")==null?"":String.valueOf(m.get("VAP_PRES_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(vapPresUnit)) {
				vapPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(vapPresUnit)) {
				vapPresUnit = "KG/CM2G";
			}			
			if (isTransUnitData("1", listUnitChg, vapPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "VAP_PRES_NOR");
				m.put("VAP_PRES_NOR", convWithUnit("1",engine,"VAP_PRES_NOR", m.get("VAP_PRES_NOR"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("VAP_PRES_MIN", convWithUnit("1",engine,"VAP_PRES_MIN", m.get("VAP_PRES_MIN"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("VAP_PRES_MAX", convWithUnit("1",engine,"VAP_PRES_MAX", m.get("VAP_PRES_MAX"), vapPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				m.put("VAP_PRES_UNIT", aBaseUnit );
			}else {
				m.put("VAP_PRES_NOR",  m.get("VAP_PRES_NOR") == null || "".equals(String.valueOf(m.get("VAP_PRES_NOR")))? null:asDouble2(m.get("VAP_PRES_NOR")));
				m.put("VAP_PRES_MIN",  m.get("VAP_PRES_MIN") == null || "".equals(String.valueOf(m.get("VAP_PRES_MIN")))? null:asDouble2(m.get("VAP_PRES_MIN")));
				m.put("VAP_PRES_MAX",  m.get("VAP_PRES_MAX") == null || "".equals(String.valueOf(m.get("VAP_PRES_MAX")))? null:asDouble2(m.get("VAP_PRES_MAX")));
			}
			
			// ----------------
			// 씰챔버압력 : Seal Chamber Pressure
			//- BARG 기준
			// ----------------
			String scPresUnit = m.get("SEAL_CHAM_UNIT")==null?"":String.valueOf(m.get("SEAL_CHAM_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(scPresUnit)) {
				scPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(scPresUnit)) {
				scPresUnit = "KG/CM2G";
			}			
			if (isTransUnitData("1", listUnitChg, scPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_CHAM_NOR");
				m.put("SEAL_CHAM_NOR", convWithUnit("1",engine,"SEAL_CHAM_NOR", m.get("SEAL_CHAM_NOR"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SEAL_CHAM_MIN", convWithUnit("1",engine,"SEAL_CHAM_MIN", m.get("SEAL_CHAM_MIN"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SEAL_CHAM_MAX", convWithUnit("1",engine,"SEAL_CHAM_MAX", m.get("SEAL_CHAM_MAX"), scPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				m.put("SEAL_CHAM_UNIT", aBaseUnit );
			}else {
				m.put("SEAL_CHAM_NOR",  m.get("SEAL_CHAM_NOR") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_NOR")))? null:asDouble2(m.get("SEAL_CHAM_NOR")));
				m.put("SEAL_CHAM_MIN",  m.get("SEAL_CHAM_MIN") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MIN")))? null:asDouble2(m.get("SEAL_CHAM_MIN")));
				m.put("SEAL_CHAM_MAX",  m.get("SEAL_CHAM_MAX") == null || "".equals(String.valueOf(m.get("SEAL_CHAM_MAX")))? null:asDouble2(m.get("SEAL_CHAM_MAX")));
			}
			
			// ----------------
			// 흡입압력 : SUCT_PRES_NOR
			//- BARG 기준
			// ----------------
			String suctPresUnit = m.get("SUCT_PRES_UNIT")==null?"":String.valueOf(m.get("SUCT_PRES_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(suctPresUnit)) {
				suctPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(suctPresUnit)) {
				suctPresUnit = "KG/CM2G";
			}
			if (isTransUnitData("1", listUnitChg, suctPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "SUCT_PRES_NOR");
				m.put("SUCT_PRES_NOR", convWithUnit("1",engine,"SUCT_PRES_NOR", m.get("SUCT_PRES_NOR"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("SUCT_PRES_MIN", convWithUnit("1",engine,"SUCT_PRES_MIN", m.get("SUCT_PRES_MIN"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("SUCT_PRES_MAX", convWithUnit("1",engine,"SUCT_PRES_MAX", m.get("SUCT_PRES_MAX"), suctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				m.put("SUCT_PRES_UNIT", aBaseUnit );
			}else {
				m.put("SUCT_PRES_NOR",  m.get("SUCT_PRES_NOR") == null || "".equals(String.valueOf(m.get("SUCT_PRES_NOR")))? null:asDouble2(m.get("SUCT_PRES_NOR")));
				m.put("SUCT_PRES_MIN",  m.get("SUCT_PRES_MIN") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MIN")))? null:asDouble2(m.get("SUCT_PRES_MIN")));
				m.put("SUCT_PRES_MAX",  m.get("SUCT_PRES_MAX") == null || "".equals(String.valueOf(m.get("SUCT_PRES_MAX")))? null:asDouble2(m.get("SUCT_PRES_MAX")));
			}
			
			// ----------------
			// 배출압력 : DISCH_PRES_NOR
			//- BARG 기준
			// ----------------
			String dischPresUnit = m.get("DISCH_PRES_UNIT")==null?"":String.valueOf(m.get("DISCH_PRES_UNIT"));
			// 임시처리
			if ("KG/CM²A".equals(dischPresUnit)) {
				dischPresUnit = "KG/CM2A";
			}else if ("KG/CM²G".equals(dischPresUnit)) {
				dischPresUnit = "KG/CM2G";
			}
			if (isTransUnitData("1", listUnitChg, dischPresUnit)){
				aBaseUnit = unitConvBase(listUnitCode, "DISCH_PRES_NOR");
				m.put("DISCH_PRES_NOR", convWithUnit("1",engine,"DISCH_PRES_NOR", m.get("DISCH_PRES_NOR"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				m.put("DISCH_PRES_MIN", convWithUnit("1",engine,"DISCH_PRES_MIN", m.get("DISCH_PRES_MIN"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
				m.put("DISCH_PRES_MAX", convWithUnit("1",engine,"DISCH_PRES_MAX", m.get("DISCH_PRES_MAX"), dischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
				m.put("DISCH_PRES_UNIT", aBaseUnit );
			}else {
				m.put("DISCH_PRES_NOR",  m.get("DISCH_PRES_NOR") == null || "".equals(String.valueOf(m.get("DISCH_PRES_NOR")))? null:asDouble2(m.get("DISCH_PRES_NOR")));
				m.put("DISCH_PRES_MIN",  m.get("DISCH_PRES_MIN") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MIN")))? null:asDouble2(m.get("DISCH_PRES_MIN")));
				m.put("DISCH_PRES_MAX",  m.get("DISCH_PRES_MAX") == null || "".equals(String.valueOf(m.get("DISCH_PRES_MAX")))? null:asDouble2(m.get("DISCH_PRES_MAX")));
			}
			
			// RPM - Shaft Speed  ==> 현재 RPM_UNIT 단위정보 없음. RPM 자체가 단위
			aBaseUnit = unitConvBase(listUnitCode, "RPM_NOR");
			m.put("RPM_NOR", convWithUnit("1",engine,"RPM_NOR", m.get("RPM_NOR"), null, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
			m.put("RPM_MIN", convWithUnit("1",engine,"RPM_MIN", m.get("RPM_MIN"), null,  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMin));
			m.put("RPM_MAX", convWithUnit("1",engine,"RPM_MAX", m.get("RPM_MAX"), null,  aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityMax));
			//m.put("RPM_UNIT", aBaseUnit );
			
			// 기타
			//m.put("PUMP_TYPE",  (m.get("PUMP_TYPE")==null||"".equals(String.valueOf(m.get("PUMP_TYPE"))))?getBlankDataDefault("PUMP_TYPE"):String.valueOf(m.get("PUMP_TYPE")));
			
			// ----------------
			// 크기 Seal Size
			//  MM 기준  / SHAFT_UNIT : NULL일경우 MM 으로 처리 
			// ----------------
			// seal size ->  MM가 붙은 데이터가 있어서 IN로 통일함
			/*String sealSize = m.get("SEAL_SIZE")==null?"":(String)m.get("SEAL_SIZE");
			String[] sealSizes = sealSize.split("[/]");
			// MM : IN * 25.4
			sealSize = "";  
			for(String s : sealSizes) {
				if(s.contains("MM")) {
					s = s.replace("MM", "");
				}else {
					try {
						s = String.valueOf(Math.round(Double.valueOf(s) *25.4*10)/10);
					}catch(Exception e) {
						LOGGER.info("seal size 변환 오류 : " + m.get("IDX_NO") + " : " + s);
					} 
					
				}
				sealSize = sealSize + s + "/";
			}
			if (sealSize.endsWith("/")) {
				sealSize = sealSize.substring(0, sealSize.length()-1);
			}
			aBaseUnit = unitConvBase(listUnitCode, "SEAL_SIZE");
			m.put("SEAL_SIZE", sealSize );*/
			//0826 Seal Size 데이터변환 처리 부분 제거 ==> SEAL_SIZE 주석처리
			
			// ----------------
			// Grouping 정보
			// COL_VALUE,GRP_CODE,GRP,GRP_SUB
			// ----------------
			
			// Product
			//sProductTypeG = "";
			if (m.get("PRODUCT")!=null) {
				sProduct = String.valueOf(m.get("PRODUCT")); // 대문자로 통일
				sProductTypeG = getGroupingStr(sProduct,listGrpProductInfo,productGroupHierList);
				m.put("PRODUCT_G", sProductTypeG );
			}else {
				m.put("PRODUCT_G", "-" );
			}
						
			// Pump Type
            //sPumpTypeG = "";
			if (m.get("PUMP_TYPE")!=null) {
				sPumpType = String.valueOf(m.get("PUMP_TYPE")); // 대문자로 통일
				sPumpTypeG = getGroupingStr(sPumpType,listGrpPumpTypeInfo,null);
				m.put("PUMP_TYPE_G", sPumpTypeG );
			}else {
				m.put("PUMP_TYPE_G", "-" );
			}
			
			// Equip Type
			// seal text 제거
			m.put("EQUIP_TYPE", String.valueOf(m.get("EQUIP_TYPE")).replace("Seal", "").trim() );	

			
			// 추가 필드 blank 처리
			//Seal type
			if (m.get("SEAL_TYPE") ==null || "".equals(String.valueOf(m.get("SEAL_TYPE")))) {
				m.put("SEAL_TYPE", "-" );
			}
			//API Plan
			if (m.get("API_PLAN") ==null || "".equals(String.valueOf(m.get("API_PLAN")))) {
				m.put("API_PLAN", "-" );
			}
			//Material
			if (m.get("MATERIAL") ==null || "".equals(String.valueOf(m.get("MATERIAL")))) {
				m.put("MATERIAL", "-" );
			}
			//Equip Type
			if (m.get("EQUIP_TYPE") ==null || "".equals(String.valueOf(m.get("EQUIP_TYPE")))) {
				m.put("EQUIP_TYPE", "-" );
			}
			
			// ----------------
			// Insert row
			// ----------------
			//mLMapper.insertDataCnv1(m);	
			resultList.add(m);
						
		} // end for (Map<String,Object> m : list )
		
		System.out.println("Insert 시작 resultList.size()  :  " + resultList.size());
		
		//Bulk Insert
		if(resultList.size() > 0) {
			int iCommit=0;
	    	int iBulkSize=29; //인자 2100개 제한(Sql Server)
	    	double iStepCnt =  Math.ceil(resultList.size()/iBulkSize);
	    	List<Map<String,Object>> subList = null;
	    	Map<String,Object> paramMap = new HashMap<String,Object>();
	    	for(int i=0;i<iStepCnt;i++) {
	    		subList = resultList.subList(i*iBulkSize, (i+1)*iBulkSize);
	    		paramMap.put("list", subList);
	    		mLMapper.insertDataCnv2(paramMap);	
	    		System.out.println(iCommit + " : ok");
	    		iCommit++;
			}
			subList = resultList.subList(iCommit*iBulkSize, list.size());
			System.out.println("last count :  " + subList.size());
			if (subList.size()>0) {
				paramMap.put("list", subList);
				mLMapper.insertDataCnv2(paramMap);	
			}
		}
		
	}
	
	
	/**
	 * 단위수식 변환 처리
	 * @param listUnitChg
	 * @param val
	 * @param unitCode
	 * @return
	 * @throws Exception
	 */
	
	public Object convWithUnit(
			String type,
			ScriptEngine engine,
			String sCol,
			Object val, 
			Object unit, 
			String transUnit,
			List<Map<String,Object>> listUnitChg , 
			List<Map<String,Object>> listTransTxtVal ,
			List<Map<String,Object>> listSsuChg,
			String sg
			) throws Exception{
		
//		System.out.println("====================== ");
//		System.out.println("type: "+type +",sCol:"+sCol+",val:"+val+",unit:"+unit+",transUnit:"+transUnit);
		
		
		String sConvType = (type==null?"1":type);
		
		// specific gravity
		String sSpecGravity = sg;
		
		// 빈수치가 아닐경우 Text형 데이터유무 체크
		//System.out.println("빈수치 데이터 처리 skip");
		//System.out.println("현재 val : " + val);
		// 텍스트형 수치 처리
		Object oTransVal = transTxtVal(listTransTxtVal, val);
		if ( val != oTransVal ) {
//			System.out.println("text 변환데이터 : " + oTransVal);
			return asBlanktoNull(String.valueOf(oTransVal));
		}
		
		// 이 단계에서 빈값일 경우는 0을 Return
		if(val==null || "".equals(val)) {
			return null;
		}
				
//		System.out.println("텍스트형 수치 처리 skip");
		
		// 첫번째 반환된는 숫자형태의 값을 취한다.
		// 숫자와 문자의 조합으로 데이터가 들어오는 현상은로 인함.. 추 후 로직 확인하여 보완 필요
//		String sVal = String.valueOf(val);
//		String patternStr = "([-]?[0-9]*[.]?[0-9]+)";
//	    Pattern pattern = Pattern.compile(patternStr);
//	    Matcher matcher = pattern.matcher(sVal);
//	    if(matcher.find()){ // 첫번째 찾는 값으로 처리
//	    	val = matcher.group(0);
//	    }else {
//	    	val = null;
//	    }
//	    System.out.println("val : " + val);
		
//	    String patternStr = "[^0-9.]";
//	    Pattern pattern = Pattern.compile(patternStr);
//	    Matcher matcher = pattern.matcher(sVal);
//	    if(matcher.find()){
//	    	System.out.println("matcher.start() : " + matcher.start() + " / val :" + sVal);//시작인덱스
//	    	if(!sVal.startsWith("-") && sVal.length() > 1) {
//	    		val = sVal.substring(0, matcher.start());
//	    	}
//	    	System.out.println("val) : " + val);
//	    }
		// 숫자이외의 값을 제거한다.
		//val = (String.valueOf(val)).replaceAll("[^0-9.]","");
		
		// 이 단계에서 단위가 없는 경우는 Value 를 그냥 Return
		if (unit==null  || "".equals(unit)) {
			//return asBlanktoNull(val);
			return asDouble2(val);
		}
		
		// 이 단계에서 빈값일 경우는 null을 Return
		if(val==null || "".equals(val)) {
			return null;
		}
		
		//System.out.println("단위가 없는 경우 skip");
		
		// ---------------------------------------
		// 단위 수식에 따른 변환처리
		// ---------------------------------------
		
		Map<String,Object> unitm = null;
		String sCompareUnitVal = "";
		for (Map<String,Object> m : listUnitChg ){
			
			if("1".equals(sConvType)) { //  org to cnv
				sCompareUnitVal = String.valueOf(m.get("UNIT_NAME"));
			}else if("2".equals(sConvType)) {  // 엑셀에서 데이터 가져오는 경우
				sCompareUnitVal = String.valueOf(m.get("EX_UNIT_NAME"));
			}else if("3".equals(sConvType)) {  // 입력값을 단위변환할경우
				sCompareUnitVal = String.valueOf(m.get("UNIT_CODE"));
			}else { // default
				sCompareUnitVal = String.valueOf(m.get("UNIT_NAME"));
			}
			
//			System.out.println("sCompareUnitVal : " + sCompareUnitVal);
				
			if( unit.equals(sCompareUnitVal) && transUnit.equals(String.valueOf(m.get("TRANS_CODE"))) ){
				unitm = m;
				break;
			}
		}
		
//		System.out.println("단위변환 수식 정보 unitm : " + unitm);
		
		//단위변환 수식 정보
		if (unitm==null) {
			if ("".equals(val)) return null;
			else {
				//return String.valueOf(val);
				//return asBlanktoNull(val);
				return asDouble2(val);
			}
		}
		
		//수식
		String calc = String.valueOf(unitm.get("CALC_STR"));
		//System.out.println("초기 calc  : " + calc);
		
		if ( "".equals(calc)) { // 수식이 없을경우
			//System.out.println("calc : DEFAULT" );
			//return String.valueOf(val);
			//return asBlanktoNull(val);
			return asDouble2(val);
			
		}else {
			// -----------------------------
			// 공통 변환처리
			// -----------------------------
			// 비중
			if(sSpecGravity != null) calc = calc.replace("'SPEC_GRAVITY'", sSpecGravity); 
			// SSU -> CST 변환
			if(calc.contains(" [M_VISC_SSU_CHG.CST]")) {
				String cst = "0";
				double dSsuVal = Double.valueOf(val.toString());
				for (Map<String,Object> m : listSsuChg ){
					if ( dSsuVal <  Double.valueOf(m.get("SSU").toString())) {
						cst = m.get("CST").toString();
						break;
					}
				}
				calc = calc.replace(" [M_VISC_SSU_CHG.CST]", cst); // CST
			}
			
			// 수식을 설정한다.
			calc = (calc.replace("{"+(String)unitm.get("UNIT_CODE")+"}", val.toString())).substring(calc.indexOf("=")+1);
			calc =  "1.0*" + calc; // double 형태로 만든다
			//System.out.println("최종 calc  : " + calc);
			
			Object rtnVal = "";
			try {
				//rtnVal = "" + (1.0 * Math.round(Double.valueOf((engine.eval(calc)).toString()) * 1000)/1000);  // 소수 3자리로 반올림
				rtnVal = "" + (1.0 * Math.round(asDouble(engine.eval(calc)) * 1000)/1000);  // 소수 3자리로 반올림
				//System.out.println("rtnVal  : " + rtnVal);
			}catch(Exception e) {
				e.printStackTrace();
				rtnVal = null;
				//throw new Exception("수식 계산 오류 :" + calc);
			}
			
//			System.out.println("최종 calc 연산결과 반올림 후  : " + rtnVal);
			return asBlanktoNull(rtnVal);
		}
	}
	
	private Double asDouble(Object o) {
	    Double val = null;
	    
	    if (o instanceof Number) {
	        val = ((Number) o).doubleValue();
	    }
	    return val;
	}
	    
	private Object asDouble2(Object o) {
		Object val = null;
	    if (o != null) {
	    	try {
	    		val = Double.parseDouble(String.valueOf(o));
		    }catch(NumberFormatException e) {
		    	val = null;
			}
	    }
	    return val;
	}
	
	private Object asBlanktoNull(Object o) {
		if(o== null || "".equals(String.valueOf(o))){
			return null;
		}else {
			Object val;
			if("3,580".equals(String.valueOf(o))) {
				System.out.println("----------------------------------------" + o);
			}
			try {
	    		val = Double.parseDouble(String.valueOf(o));
		    }catch(NumberFormatException e) {
		    	val = null;
			}
			
			if("3,580".equals(String.valueOf(o))) {
				System.out.println("-------------val---------------------------" + val);
			}
			return val;
		}
	}
		
	public String unitConvBase(List<Map<String,Object>> listUnitCode ,String col) throws Exception{
		String sBaseCol=null;
		for (Map<String,Object> m : listUnitCode ){
			if( col.equals(m.get("COL_NAME").toString())){
				sBaseCol = m.get("BASE_COL").toString();
				break;
			}
		}
		return sBaseCol;
	}

	public Object transTxtVal(List<Map<String,Object>> listTransTxtVal ,Object val) throws Exception{
		if (val==null) {
			return null;
		}else {
			Object oTransVal = val;
			for (Map<String,Object> m : listTransTxtVal ){
				//if( (String.valueOf(oTransVal)).equals(m.get("VAL_CODE").toString())){
				if( (String.valueOf(oTransVal)).contains(m.get("VAL_CODE").toString())){ // 포함하는으로 변경
					oTransVal = asDouble(m.get("N_VAL"));
					break;
				}
			}
			
			//반환
			return oTransVal;
		}
	}
	
	public Map<String,Object> transTxtVal(List<Map<String,Object>> listTransTxtVal ,Map<String,Object> map) throws Exception{
		if (map==null) {
			return null;
		}else {
			
			// 체크 필드
			String[] chkCol = new String[]{
					"VISC_NOR","VISC_MIN","VISC_MAX",
					"TEMP_NOR","TEMP_MIN","TEMP_MAX",
					"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
					"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX",
					"SUCT_PRES_NOR","SUCT_PRES_MIN","SUCT_PRES_MAX",
					"DISCH_PRES_NOR","DISCH_MIN","DISCH_PRES_MAX"
			};
			
			for(String s : chkCol) {
				for (Map<String,Object> m : listTransTxtVal ){
					//if( (String.valueOf(oTransVal)).equals(m.get("VAL_CODE").toString())){
					if( (String.valueOf(map.get(s))).contains(m.get("VAL_CODE").toString())){ // 포함하는으로 변경
						map.put(s,asDouble(m.get("N_VAL")));
						break;
					}
				}
			}
			
			//반환
			return map;
		}
	}
	
	public boolean isTransUnitData(String sConvType, List<Map<String,Object>> listUnitChg, Object unit) {
		boolean isUnit = false;
		String sCompareUnitVal = "";
		
		if(unit==null || "".equals(String.valueOf(unit))) return true;
		
		for(Map<String,Object> m:listUnitChg) {
			
			if("1".equals(sConvType)) { //  org to cnv
				sCompareUnitVal = String.valueOf(m.get("UNIT_NAME"));
			}else if("2".equals(sConvType)) {  // 엑셀에서 데이터 가져오는 경우
				sCompareUnitVal = String.valueOf(m.get("EX_UNIT_NAME"));
			}else if("3".equals(sConvType)) {  // 입력값을 단위변환할경우
				sCompareUnitVal = String.valueOf(m.get("UNIT_CODE"));
			}else { // default
				sCompareUnitVal = String.valueOf(m.get("UNIT_NAME"));
			}
			
			if (String.valueOf(unit).equals(sCompareUnitVal)) {
				isUnit = true;
				break;
			}
		}

		return isUnit;
	}
	
	
	public Object getBlankDataDefault(String col_prefix) {
		Object val = null;
		if ("SPEC_GRAVITY".equals(col_prefix)){
			val = "1.0"; 
		}else if ("VISC".equals(col_prefix)){
			val = "1.0";
		}else if ("TEMP".equals(col_prefix)){
			val = "32.0"; 
		}else if ("VAP_PRES".equals(col_prefix)){
			val = "0.0"; 
		}else if ("SEAL_CHAM".equals(col_prefix)){
			val = "1.0";
		}else if ("SUCT_PRES".equals(col_prefix)){
			val = "1.0"; 
		}else if ("DISCH_PRES".equals(col_prefix)){
			val = "1.0";	
		}else if ("PUMP_TYPE".equals(col_prefix)){
			val = "OH2"; 
		}else if ("SHAFT_SIZE".equals(col_prefix)){
			val = "50"; 
		}else if ("RPM".equals(col_prefix)){
			val = "3600"; 
		}
		return val;
	}
	
	public Object getBlankDataDefaultUnit(String col_prefix) {
		Object val = null;
		if ("VISC".equals(col_prefix)){
			val = "CP";
		}else if ("TEMP".equals(col_prefix)){
			val = "C"; 
		}else if ("VAP_PRES".equals(col_prefix)){
			val = "BARA"; 
		}else if ("SEAL_CHAM".equals(col_prefix)){
			val = "BARG";
		}else if ("SUCT_PRES".equals(col_prefix)){
			val = "BARG"; 
		}else if ("DISCH_PRES".equals(col_prefix)){
			val = "BARG";	
		}else if ("SHAFT_SIZE".equals(col_prefix)){
			val = "MM"; 
		}
		return val;
	}
	
	
	public Map<String,Object> setEmptyDataWithDefaultData(Map<String,Object> data) {
			
		String [] blankChkCol1 = new String[]{
				"SPEC_GRAVITY",
				"VISC",
				"TEMP",
				"VAP_PRES",
				"SEAL_CHAM",
				"SUCT_PRES",
				"DISCH_PRES",
				"RPM"
		};
		
		String [] blankChkCol2 = new String[]{
				"PUMP_TYPE",
				"SHAFT_SIZE"
		};
			
		for (String sCol : blankChkCol1) {
			
			//System.out.println("sCol : " + sCol);
			
			if ( !(data.get(sCol + "_NOR") ==null || "".equals(String.valueOf(data.get(sCol + "_NOR"))) ) &&
					!(data.get(sCol + "_MIN") ==null || "".equals(String.valueOf(data.get(sCol + "_MIN"))) ) &&
					!(data.get(sCol + "_MAX") ==null || "".equals(String.valueOf(data.get(sCol + "_MAX"))) )
					) {
				//처리없음
				//System.out.println("case 1 ");
				continue;
			}else if ( !(data.get(sCol + "_NOR") ==null || "".equals(String.valueOf(data.get(sCol + "_NOR"))) ) &&
					(data.get(sCol + "_MIN") ==null || "".equals(String.valueOf(data.get(sCol + "_MIN"))) ) &&
					(data.get(sCol + "_MAX") ==null || "".equals(String.valueOf(data.get(sCol + "_MAX"))) )
					) { 
				//System.out.println("case 2 ");
				data.put(sCol + "_MIN",data.get(sCol + "_NOR"));
				data.put(sCol + "_MAX",data.get(sCol + "_NOR"));
				
			}else if ( (data.get(sCol + "_NOR") ==null || "".equals(String.valueOf(data.get(sCol + "_NOR"))) ) &&
					!(data.get(sCol + "_MIN") ==null || "".equals(String.valueOf(data.get(sCol + "_MIN"))) ) &&
					(data.get(sCol + "_MAX") ==null || "".equals(String.valueOf(data.get(sCol + "_MAX"))) )
					) { 
				//System.out.println("case 3 ");
				data.put(sCol + "_NOR",data.get(sCol + "_MIN"));
				data.put(sCol + "_MAX",data.get(sCol + "_MIN"));
				
			}else if ( (data.get(sCol + "_NOR") ==null || "".equals(String.valueOf(data.get(sCol + "_NOR"))) ) &&
					(data.get(sCol + "_MIN") ==null || "".equals(String.valueOf(data.get(sCol + "_MIN"))) ) &&
					!(data.get(sCol + "_MAX") ==null || "".equals(String.valueOf(data.get(sCol + "_MAX"))) )
					) { 
				//System.out.println("case 4 ");
				data.put(sCol + "_NOR",data.get(sCol + "_MAX"));
				data.put(sCol + "_MIN",data.get(sCol + "_MAX"));	
				
			}else if ( !(data.get(sCol + "_NOR") ==null || "".equals(String.valueOf(data.get(sCol + "_NOR"))) ) &&
					!(data.get(sCol + "_MIN") ==null || "".equals(String.valueOf(data.get(sCol + "_MIN"))) ) &&
					(data.get(sCol + "_MAX") ==null || "".equals(String.valueOf(data.get(sCol + "_MAX"))) )
					) {  
				//System.out.println("case 5 ");
				data.put(sCol + "_MAX",data.get(sCol + "_NOR"));
				
			}else if ( !(data.get(sCol + "_NOR") ==null || "".equals(String.valueOf(data.get(sCol + "_NOR"))) ) &&
					(data.get(sCol + "_MIN") ==null || "".equals(String.valueOf(data.get(sCol + "_MIN"))) ) &&
					!(data.get(sCol + "_MAX") ==null || "".equals(String.valueOf(data.get(sCol + "_MAX"))) )
					) {  
				//System.out.println("case 6 ");
				data.put(sCol + "_MIN",data.get(sCol + "_NOR"));
				
			}else if  ( (data.get(sCol + "_NOR") ==null || "".equals(String.valueOf(data.get(sCol + "_NOR"))) ) &&
					!(data.get(sCol + "_MIN") ==null || "".equals(String.valueOf(data.get(sCol + "_MIN"))) ) &&
					!(data.get(sCol + "_MAX") ==null || "".equals(String.valueOf(data.get(sCol + "_MAX"))) )
					) {   
				//System.out.println("case 7 ");
				data.put(sCol + "_NOR",data.get(sCol + "_MAX"));
				
			}else {
				//System.out.println("case 8 ");
				data.put(sCol + "_NOR",getBlankDataDefault(sCol));
				data.put(sCol + "_MIN",getBlankDataDefault(sCol));
				data.put(sCol + "_MAX",getBlankDataDefault(sCol));
				//기준단위로 설정한다.
				if (getBlankDataDefaultUnit(sCol) != null) {
					data.put(sCol + "_UNIT",getBlankDataDefaultUnit(sCol));
				}
			}
			
		}
				
		
		for (String sCol : blankChkCol2) {
			if(data.get(sCol) ==null || "".equals(String.valueOf(data.get(sCol)))){
				data.put(sCol ,getBlankDataDefault(sCol));
				//기준단위로 설정한다.
				if (getBlankDataDefaultUnit(sCol) != null) {
					data.put(sCol + "_UNIT",getBlankDataDefaultUnit(sCol));
				}
			}
		}
		
		return data;
	}
	
	
	/**
	 * 엑셀 템플릿파일에서 입력인자 정보를 추출한다.
	 * 단위변환처리 19.11
	 * 단위변환처리 없이 화면에 Loading 20.01
	 */
	public List<Map<String,Object>> getExcelUploadInfo(String fileInfo) throws Exception{
		List<Map<String,Object>> dataL = new ArrayList<Map<String,Object>>();
		
		System.out.println(fileInfo);
		
		FileInputStream file = new FileInputStream( fileInfo);
		XSSFWorkbook workbook  = new XSSFWorkbook(file);
		try {
		
			//XSSFSheet sheet = workbook.getSheet("sheet name");
			XSSFSheet sheet = workbook.getSheetAt(0); // 첫번째 Sheet
			
			// 시트 한개이상
			//XSSFSheet sheet = null;
			//for(int i=0 ; i<workbook.getNumberOfSheets() ; i++){
			//	sheet = workbook.getSheetAt(i);
			//}
			
			FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
	
			// Row
			int rows = sheet.getPhysicalNumberOfRows();
			int skipRow = 16;
			XSSFRow row = null;
			
			//단위정보 Row
			row = sheet.getRow(12);
			String sViscUnit = ExcelUtil.getExcelValue(row.getCell(8),formulaEval); // Viscosity Unit
			String sTempNorUnit = ExcelUtil.getExcelValue(row.getCell(9),formulaEval); // Temp. nor Unit
			String sTempMinUnit = ExcelUtil.getExcelValue(row.getCell(10),formulaEval); // Temp. min Unit
			String sTempMaxUnit = ExcelUtil.getExcelValue(row.getCell(11),formulaEval); // Temp. max Unit
			String sVaporPresUnit = ExcelUtil.getExcelValue(row.getCell(12),formulaEval); // vapor press Unit
			String sSuctPresUnit = ExcelUtil.getExcelValue(row.getCell(13),formulaEval); // 
			String sDischPresUnit = ExcelUtil.getExcelValue(row.getCell(16),formulaEval); // 
			String sSealChamUnit = ExcelUtil.getExcelValue(row.getCell(19),formulaEval); // 
			String sRpmUnit = ExcelUtil.getExcelValue(row.getCell(22),formulaEval); // 
			String sShaftSizeUnit = ExcelUtil.getExcelValue(row.getCell(23),formulaEval); // 
			
			String sViscUnitText = "", sTempUnitText = "", sVaporPresUnitText = "", sSealChamUnitText = "", sRpmUnitText = "", sShaftSizeUnitText = "";
			
			//Unit 추가처리 
			sSealChamUnit = sSealChamUnit.replace("\"", "");
			sDischPresUnit  = sDischPresUnit.replace("\"", "");
			
			// 단위변환정보
			List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
			
			System.out.println("sTempNorUnit : " + sTempNorUnit);
			
			//엑셀 단위정보를 화면 단위정보로 변환한다.
			for(Map<String,Object> unit : listUnitChg) {
				System.out.println(String.valueOf(unit.get("EX_UNIT_NAME")) );
				if(String.valueOf(unit.get("EX_UNIT_NAME")).equals(sViscUnit)) {
					sViscUnit = String.valueOf(unit.get("UNIT_CODE"));
					sViscUnitText = String.valueOf(unit.get("UNIT_NAME"));
				}
				if (String.valueOf(unit.get("EX_UNIT_NAME")).equals(sTempNorUnit)) {
					sTempNorUnit = String.valueOf(unit.get("UNIT_CODE"));
					sTempUnitText = String.valueOf(unit.get("UNIT_NAME"));
				}
				if (String.valueOf(unit.get("EX_UNIT_NAME")).equals(sVaporPresUnit)) {
					sVaporPresUnit = String.valueOf(unit.get("UNIT_CODE"));
					sVaporPresUnitText = String.valueOf(unit.get("UNIT_NAME"));
				}
				
				// 현재 미사용
				if (String.valueOf(unit.get("EX_UNIT_NAME")).equals(sSuctPresUnit)) {
					sSuctPresUnit = String.valueOf(unit.get("UNIT_CODE"));
				}
				if (String.valueOf(unit.get("EX_UNIT_NAME")).equals(sDischPresUnit)) {
					sDischPresUnit = String.valueOf(unit.get("UNIT_CODE"));
				}
				
				if (String.valueOf(unit.get("EX_UNIT_NAME")).equals(sSealChamUnit)) {
					sSealChamUnit = String.valueOf(unit.get("UNIT_CODE"));
					sSealChamUnitText = String.valueOf(unit.get("UNIT_NAME"));
				}
				if (String.valueOf(unit.get("EX_UNIT_NAME")).equals(sRpmUnit)) {
					sRpmUnit = String.valueOf(unit.get("UNIT_CODE"));
					sRpmUnitText = String.valueOf(unit.get("UNIT_NAME"));
				}
				if (String.valueOf(unit.get("EX_UNIT_NAME")).equals(sShaftSizeUnit)) {
					sShaftSizeUnit = String.valueOf(unit.get("UNIT_CODE"));
					sShaftSizeUnitText = String.valueOf(unit.get("UNIT_NAME"));
				}
			}
			
			// Blank 처리 대상 필드
			String sTempNor, sTempMin, sTempMax = "";
			String sSuctPresNor, sSuctPresMin, sSuctPresMax = "";
			String sDischPresNor, sDischPresMin, sDischPresMax = "";
			String sSealChamNor, sSealChamMin, sSealChamMax = "";
			String sRpm = "";
			String sShaftSize = "";
			String sPumpType="";
			String sVisc = "";
			String sVaporPres = "";
			String sSpecGravity = "";
			
			Map<String,Object> dataM = null;
			int iNo=1;
			
			for(int i=skipRow ; i<rows ;i++){
				row = sheet.getRow(i);
				
				// Item No 가 빈값이 나올때 까지
				if (row.getCell(0) ==null || "".equals(ExcelUtil.getExcelValue(row.getCell(0)))){
					break;
				}
				
				System.out.println("열 : "+ i + " | " + ExcelUtil.getExcelValue(row.getCell(0)) );
				
				sPumpType = ExcelUtil.getExcelValue(row.getCell(2));
				sSpecGravity = ExcelUtil.getExcelValue(row.getCell(7)); //비중
				
				sTempNor = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(9)));
				sTempMin = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(10)));
				sTempMax = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(11)));
				
				sVisc = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(8))); //비중
				sVaporPres = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(12)));
				
				// 현재 미사용
				sSuctPresNor = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(13)));
				sSuctPresMin = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(14)));
				sSuctPresMax = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(15)));
				sDischPresNor = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(16)));
				sDischPresMin = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(17)));
				sDischPresMax = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(18)));
				
				sSealChamNor = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(19),formulaEval));
				sSealChamMin = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(20),formulaEval));
				sSealChamMax = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(21),formulaEval));
				
				sRpm = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(22)));
				sShaftSize = getExcelCalibrationData(ExcelUtil.getExcelValue(row.getCell(23)));
				
				System.out.println("Data get end!");
				
				// data Map
				dataM = new HashMap<String,Object>();
				dataM.put("NO",iNo++);
				
				dataM.put("PUMP_TYPE",sPumpType);
				dataM.put("PRODUCT",ExcelUtil.getExcelValue(row.getCell(5)));
				dataM.put("SPEC_GRAVITY",sSpecGravity); // 비중
				
				dataM.put("VISC_UNIT",sViscUnit); // 비중단위
				dataM.put("VISC_TEXT",sViscUnitText);
				dataM.put("VISC",sVisc); // 비중
				
				dataM.put("TEMP_UNIT",sTempNorUnit);
				dataM.put("TEMP_TEXT",sTempUnitText);
				dataM.put("TEMP_NOR",sTempNor);
				dataM.put("TEMP_MIN",sTempMin);
				dataM.put("TEMP_MAX",sTempMax);
				
				dataM.put("VAP_PRES_UNIT",sVaporPresUnit);
				dataM.put("VAP_PRES_TEXT",sVaporPresUnitText);
				dataM.put("VAP_PRES",sVaporPres);
				
				// 현재미사용
				dataM.put("SUCT_PRES_UNIT",sSuctPresUnit);
				dataM.put("SUCT_PRES_NOR",sSuctPresNor);
				dataM.put("SUCT_PRES_MIN",sSuctPresMin);
				dataM.put("SUCT_PRES_MAX",sSuctPresMax);
				dataM.put("DISCH_PRES_UNIT",sDischPresUnit);
				dataM.put("DISCH_PRES_NOR",sDischPresNor);
				dataM.put("DISCH_PRES_MIN",sDischPresMin);
				dataM.put("DISCH_PRES_MAX",sDischPresMax);
				
				dataM.put("SEAL_CHAM_UNIT",sSealChamUnit);
				dataM.put("SEAL_CHAM_TEXT",sSealChamUnitText);
				dataM.put("SEAL_CHAM_NOR",sSealChamNor);
				dataM.put("SEAL_CHAM_MIN",sSealChamMin);
				dataM.put("SEAL_CHAM_MAX",sSealChamMax);
				
				dataM.put("RPM_UNIT",sRpmUnit);
				dataM.put("RPM_TEXT",sRpmUnitText);
				dataM.put("RPM",sRpm);
				
				dataM.put("SHAFT_SIZE_UNIT",sShaftSizeUnit);
				dataM.put("SHAFT_SIZE_TEXT",sShaftSizeUnitText);
				dataM.put("SHAFT_SIZE",sShaftSize);
				
				// 리스트에 추가
				dataL.add(dataM);
				
			}// end for
			
			
		}finally {
			if(workbook!=null) workbook.close();
		}
		
		return dataL;
		
	}
	/*
	public List<Map<String,Object>> getExcelUploadInfo(String fileInfo) throws Exception{
		
		List<Map<String,Object>> dataL = new ArrayList<Map<String,Object>>();
		
		System.out.println(fileInfo);
		
		FileInputStream file = new FileInputStream( fileInfo);
		XSSFWorkbook workbook  = new XSSFWorkbook(file);
		try {
		
			//XSSFSheet sheet = workbook.getSheet("sheet name");
			XSSFSheet sheet = workbook.getSheetAt(0); // 첫번째 Sheet
			
			// 시트 한개이상
			//XSSFSheet sheet = null;
			//for(int i=0 ; i<workbook.getNumberOfSheets() ; i++){
			//	sheet = workbook.getSheetAt(i);
			//}
			
			FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
	
			// Row
			int rows = sheet.getPhysicalNumberOfRows();
			int skipRow = 16;
			XSSFRow row = null;
			
			//단위정보 Row
			row = sheet.getRow(12);
			String sViscUnit = ExcelUtil.getExcelValue(row.getCell(8),formulaEval); // Viscosity Unit
			String sTempNorUnit = ExcelUtil.getExcelValue(row.getCell(9),formulaEval); // Temp. nor Unit
			String sTempMinUnit = ExcelUtil.getExcelValue(row.getCell(10),formulaEval); // Temp. min Unit
			String sTempMaxUnit = ExcelUtil.getExcelValue(row.getCell(11),formulaEval); // Temp. max Unit
			String sVaporPresUnit = ExcelUtil.getExcelValue(row.getCell(12),formulaEval); // vapor press Unit
			String sSuctPresUnit = ExcelUtil.getExcelValue(row.getCell(13),formulaEval); // 
			String sDischPresUnit = ExcelUtil.getExcelValue(row.getCell(16),formulaEval); // 
			String sSealChamUnit = ExcelUtil.getExcelValue(row.getCell(19),formulaEval); // 
			String sRpmUnit = ExcelUtil.getExcelValue(row.getCell(22),formulaEval); // 
			String sShaftSizeUnit = ExcelUtil.getExcelValue(row.getCell(23),formulaEval); //
			
			//Unit 추가처리 
			sSealChamUnit = sSealChamUnit.replace("\"", "");
			sDischPresUnit  = sDischPresUnit.replace("\"", "");
			
			//단위변환 수식 처리 엔진
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			
			// 단위코드정보
			List<Map<String,Object>> listUnitCode = mLMapper.getUnitCodeRelInfo();
			// 단위변환정보
			List<Map<String,Object>> listUnitChg = mLMapper.getTransUnitCodeInfo();
			// Text 입력값 변환정보
			List<Map<String,Object>> listTransTxtVal = mLMapper.getTransTxtValInfo();
			// SSU 변환 정보 (Visc 용)
			List<Map<String,Object>> listSsuChg = mLMapper.getSsuChglInfo();
			
			// 기준변환단위변수
			String aBaseUnit = "";
			
			// Blank 처리 대상 필드
			String sTempNor, sTempMin, sTempMax = "";
			String sSuctPresNor, sSuctPresMin, sSuctPresMax = "";
			String sDischPresNor, sDischPresMin, sDischPresMax = "";
			String sSealChamNor, sSealChamMin, sSealChamMax = "";
			String sRpm = "";
			String sShaftSize = "";
			String sPumpType="";
			String sVisc = "";
			String sVaporPres = "";
			String sSpecGravity = "";
			
			Map<String,Object> dataM = null;
			int iNo=1;
			for(int i=skipRow ; i<rows ;i++){
				row = sheet.getRow(i);
				
				// Item No 가 빈값이 나올때 까지
				if (row.getCell(0) ==null || "".equals(ExcelUtil.getExcelValue(row.getCell(0)))){
					break;
				}
				
				System.out.println("A열체크 : "+ i + " | " + ExcelUtil.getExcelValue(row.getCell(0)) );
				
				sPumpType = ExcelUtil.getExcelValue(row.getCell(2));
				sSpecGravity = ExcelUtil.getExcelValue(row.getCell(7)); //비중
				
				// Blank Value 처리
				sTempNor = ExcelUtil.getExcelValue(row.getCell(9));
				sTempMin = ExcelUtil.getExcelValue(row.getCell(10));
				sTempMax = ExcelUtil.getExcelValue(row.getCell(11));
				
				sVisc = ExcelUtil.getExcelValue(row.getCell(8));
				sVaporPres = ExcelUtil.getExcelValue(row.getCell(12));
				
				sSuctPresNor = ExcelUtil.getExcelValue(row.getCell(13));
				sSuctPresMin = ExcelUtil.getExcelValue(row.getCell(14));
				sSuctPresMax = ExcelUtil.getExcelValue(row.getCell(15));
				
				sDischPresNor = ExcelUtil.getExcelValue(row.getCell(16));
				sDischPresMin = ExcelUtil.getExcelValue(row.getCell(17));
				sDischPresMax = ExcelUtil.getExcelValue(row.getCell(18));
				
				sSealChamNor = ExcelUtil.getExcelValue(row.getCell(19),formulaEval);
				sSealChamMin = ExcelUtil.getExcelValue(row.getCell(20),formulaEval);
				sSealChamMax = ExcelUtil.getExcelValue(row.getCell(21),formulaEval);
				
				System.out.println("Data get end!");
				
				// 비중은 단위계산을 위해 빈값 일 경우 처리를 한다.
				if("".equals(sSpecGravity)) sSpecGravity = String.valueOf(getBlankDataDefault("SPEC_GRAVITY"));
				
				// Temp
//				if (!"".equals(sTempNor) && "".equals(sTempMin) && "".equals(sTempMax)) {
//					sTempMin = sTempNor;
//					sTempMax = sTempNor;
//				}else if ("".equals(sTempNor) && !"".equals(sTempMin) && "".equals(sTempMax)) {
//					sTempNor = sTempMin;
//					sTempMax = sTempMin;
//				}else if ("".equals(sTempNor) && "".equals(sTempMin) && !"".equals(sTempMax)) {
//					sTempNor = sTempMax;
//					sTempMin = sTempMax;
//				}else if (!"".equals(sTempNor) && !"".equals(sTempMin) && "".equals(sTempMax)) {
//					sTempMax = sTempNor;
//				}else if (!"".equals(sTempNor) && "".equals(sTempMin) && !"".equals(sTempMax)) {
//					sTempMin = sTempNor;
//				}else if ("".equals(sTempNor) && !"".equals(sTempMin) && !"".equals(sTempMax)) {
//					sTempNor = sTempMax;
//				}else {
//					sTempNor = String.valueOf(getBlankDataDefault("TEMP"));
//					sTempMin = String.valueOf(getBlankDataDefault("TEMP"));
//					sTempMax = String.valueOf(getBlankDataDefault("TEMP"));
//				}
//					
//				if (!"".equals(sSuctPresNor) && "".equals(sSuctPresMin) && "".equals(sSuctPresMax)) {
//					sSuctPresMin = sSuctPresNor;
//					sSuctPresMax = sSuctPresNor;
//				}else if ("".equals(sSuctPresNor) && !"".equals(sSuctPresMin) && "".equals(sSuctPresMax)) {
//					sSuctPresNor = sSuctPresMin;
//					sSuctPresMax = sSuctPresMin;
//				}else if ("".equals(sSuctPresNor) && "".equals(sSuctPresMin) && !"".equals(sSuctPresMax)) {
//					sSuctPresNor = sSuctPresMax;
//					sSuctPresMin = sSuctPresMax;
//				}else if (!"".equals(sSuctPresNor) && !"".equals(sSuctPresMin) && "".equals(sSuctPresMax)) {
//					sSuctPresMax = sSuctPresNor;
//				}else if (!"".equals(sSuctPresNor) && "".equals(sSuctPresMin) && !"".equals(sSuctPresMax)) {
//					sSuctPresMin = sSuctPresNor;
//				}else if ("".equals(sSuctPresNor) && !"".equals(sSuctPresMin) && !"".equals(sSuctPresMax)) {
//					sSuctPresNor = sSuctPresMax;
//				}else {
//					sSuctPresNor = String.valueOf(getBlankDataDefault("SUCT_PRES"));
//					sSuctPresMin = String.valueOf(getBlankDataDefault("SUCT_PRES"));
//					sSuctPresMax = String.valueOf(getBlankDataDefault("SUCT_PRES"));
//				}
//					
//				if (!"".equals(sDischPresNor) && "".equals(sDischPresMin) && "".equals(sDischPresMax)) {
//					sDischPresMin = sDischPresNor;
//					sDischPresMax = sDischPresNor;
//				}else if ("".equals(sDischPresNor) && !"".equals(sDischPresMin) && "".equals(sDischPresMax)) {
//					sDischPresNor = sDischPresMin;
//					sDischPresMax = sDischPresMin;
//				}else if ("".equals(sDischPresNor) && "".equals(sDischPresMin) && !"".equals(sDischPresMax)) {
//					sDischPresNor = sDischPresMax;
//					sDischPresMin = sDischPresMax;
//				}else if (!"".equals(sDischPresNor) && !"".equals(sDischPresMin) && "".equals(sDischPresMax)) {
//					sDischPresMax = sDischPresNor;
//				}else if (!"".equals(sDischPresNor) && "".equals(sDischPresMin) && !"".equals(sDischPresMax)) {
//					sDischPresMin = sDischPresNor;
//				}else if ("".equals(sDischPresNor) && !"".equals(sDischPresMin) && !"".equals(sDischPresMax)) {
//					sDischPresNor = sDischPresMax;
//				}else {
//					sDischPresNor = String.valueOf(getBlankDataDefault("DISCH_PRES"));
//					sDischPresMin = String.valueOf(getBlankDataDefault("DISCH_PRES"));
//					sDischPresMax = String.valueOf(getBlankDataDefault("DISCH_PRES"));
//				}	
//				
//				if (!"".equals(sSealChamNor) && "".equals(sSealChamMin) && "".equals(sSealChamMax)) {
//					sSealChamMin = sSealChamNor;
//					sSealChamMax = sSealChamNor;
//				}else if ("".equals(sSealChamNor) && !"".equals(sSealChamMin) && "".equals(sSealChamMax)) {
//					sSealChamNor = sSealChamMin;
//					sSealChamMax = sSealChamMin;
//				}else if ("".equals(sSealChamNor) && "".equals(sSealChamMin) && !"".equals(sSealChamMax)) {
//					sSealChamNor = sSealChamMax;
//					sSealChamMin = sSealChamMax;
//				}else if (!"".equals(sSealChamNor) && !"".equals(sSealChamMin) && "".equals(sSealChamMax)) {
//					sSealChamMax = sSealChamNor;
//				}else if (!"".equals(sSealChamNor) && "".equals(sSealChamMin) && !"".equals(sSealChamMax)) {
//					sSealChamMin = sSealChamNor;
//				}else if ("".equals(sSealChamNor) && !"".equals(sSealChamMin) && !"".equals(sSealChamMax)) {
//					sSealChamNor = sSealChamMax;
//				}else {
//					sSealChamNor = String.valueOf(getBlankDataDefault("SEAL_CHAM"));
//					sSealChamMin = String.valueOf(getBlankDataDefault("SEAL_CHAM"));
//					sSealChamMax = String.valueOf(getBlankDataDefault("SEAL_CHAM"));
//				}
//				
//				sRpm = ExcelUtil.getExcelValue(row.getCell(22));
//				sShaftSize = ExcelUtil.getExcelValue(row.getCell(23));
//				
//				if ("".equals(sPumpType)) sPumpType = String.valueOf(getBlankDataDefault("PUMP_TYPE"));
//				if ("".equals(sVisc)) sVisc = String.valueOf(getBlankDataDefault("VISC"));
//				if ("".equals(sVaporPres)) sVaporPres = String.valueOf(getBlankDataDefault("VAP_PRES"));
//				if ("".equals(sSpecGravity)) sSpecGravity = String.valueOf(getBlankDataDefault("SPEC_GRAVITY"));
//				if ("".equals(sRpm)) sRpm = String.valueOf(getBlankDataDefault("RPM"));
//				if ("".equals(sShaftSize)) sShaftSize = String.valueOf(getBlankDataDefault("SHAFT_SIZE"));
//				
//				System.out.println("Blank 처리 End!");
				
				//aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR");
				//m.put("TEMP_NOR",  convWithUnit("1",engine, "TEMP_NOR", m.get("TEMP_NOR"), sTempUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravityNor));
				
				dataM = new HashMap<String,Object>();
				dataM.put("NO",iNo++);
				
				//dataM.put("PUMP_TYPE",ExcelUtil.getExcelValue(row.getCell(2)));
				dataM.put("PUMP_TYPE",sPumpType);
				dataM.put("PRODUCT",ExcelUtil.getExcelValue(row.getCell(5)));
				dataM.put("SPEC_GRAVITY",sSpecGravity); // 비중 : 단위변환 없음
				
				aBaseUnit = unitConvBase(listUnitCode, "VISC_NOR"); //기준단위
				dataM.put("VISC",
						convWithUnit("2",engine,"VISC", sVisc, sViscUnit, aBaseUnit, listUnitChg, listTransTxtVal, listSsuChg, sSpecGravity)
				);
				
				aBaseUnit = unitConvBase(listUnitCode, "TEMP_NOR"); //기준단위
				dataM.put("TEMP_NOR",
						convWithUnit("2",engine,"TEMP_NOR", sTempNor, sTempNorUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null)
				);
				dataM.put("TEMP_MIN",
						convWithUnit("2",engine,"TEMP_MIN", sTempMin, sTempMinUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null)
				);
				dataM.put("TEMP_MAX",
						convWithUnit("2",engine,"TEMP_MAX", sTempMax, sTempMaxUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null)
				);
				
				aBaseUnit = unitConvBase(listUnitCode, "VAP_PRES_NOR"); //기준단위
				dataM.put("VAP_PRES",
						convWithUnit("2",engine,"VAP_PRES", sVaporPres, sVaporPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null)
				);
				
				aBaseUnit = unitConvBase(listUnitCode, "SUCT_PRES_NOR"); //기준단위
				dataM.put("SUCT_PRES_NOR",
						convWithUnit("2",engine,"SUCT_PRES_NOR", sSuctPresNor, sSuctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				dataM.put("SUCT_PRES_MIN",
						convWithUnit("2",engine,"SUCT_PRES_MIN", sSuctPresMin, sSuctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				dataM.put("SUCT_PRES_MAX",
						convWithUnit("2",engine,"SUCT_PRES_MAX", sSuctPresMax, sSuctPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				
				aBaseUnit = unitConvBase(listUnitCode, "DISCH_PRES_NOR"); //기준단위
				dataM.put("DISCH_PRES_NOR",
						convWithUnit("2",engine,"DISCH_PRES_NOR", sDischPresNor, sDischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				dataM.put("DISCH_PRES_MIN",
						convWithUnit("2",engine,"DISCH_PRES_MIN", sDischPresMin, sDischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				dataM.put("DISCH_PRES_MAX",
						convWithUnit("2",engine,"DISCH_PRES_MAX", sDischPresMax, sDischPresUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				
				aBaseUnit = unitConvBase(listUnitCode, "SEAL_CHAM_NOR"); //기준단위
				dataM.put("SEAL_CHAM_NOR",
						convWithUnit("2",engine,"SEAL_CHAM_NOR", sSealChamNor, sSealChamUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				dataM.put("SEAL_CHAM_MIN",
						convWithUnit("2",engine,"SEAL_CHAM_MIN", sSealChamMin, sSealChamUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				dataM.put("SEAL_CHAM_MAX",
						convWithUnit("2",engine,"SEAL_CHAM_MAX", sSealChamMax, sSealChamUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, sSpecGravity)
				);
				
				aBaseUnit = unitConvBase(listUnitCode, "RPM_NOR"); //기준단위
				dataM.put("RPM",
						convWithUnit("2",engine,"RPM", sRpm, sRpmUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null)
				);
				
				aBaseUnit = unitConvBase(listUnitCode, "SHAFT_SIZE"); //기준단위
				dataM.put("SHAFT_SIZE",
						convWithUnit("2",engine,"SHAFT_SIZE", sShaftSize, sShaftSizeUnit, aBaseUnit, listUnitChg, listTransTxtVal, null, null)
				);
				
//				dataM.put("VISC",ExcelUtil.getExcelValue(row.getCell(8)));
//				dataM.put("TEMP_NOR",ExcelUtil.getExcelValue(row.getCell(9)));
//				dataM.put("TEMP_MIN",ExcelUtil.getExcelValue(row.getCell(10)));
//				dataM.put("TEMP_MAX",ExcelUtil.getExcelValue(row.getCell(11)));
//				dataM.put("VAP_PRES",ExcelUtil.getExcelValue(row.getCell(12)));
//				dataM.put("SUCT_PRES_NOR",ExcelUtil.getExcelValue(row.getCell(13)));
//				dataM.put("SUCT_PRES_MIN",ExcelUtil.getExcelValue(row.getCell(14)));
//				dataM.put("SUCT_PRES_MAX",ExcelUtil.getExcelValue(row.getCell(15)));
//				dataM.put("DISCH_PRES_NOR",ExcelUtil.getExcelValue(row.getCell(16)));
//				dataM.put("DISCH_PRES_MIN",ExcelUtil.getExcelValue(row.getCell(17)));
//				dataM.put("DISCH_PRES_MAX",ExcelUtil.getExcelValue(row.getCell(18)));
//				dataM.put("SEAL_CHAM_NOR",ExcelUtil.getExcelValue(row.getCell(19),formulaEval));
//				dataM.put("SEAL_CHAM_MIN",ExcelUtil.getExcelValue(row.getCell(20),formulaEval));
//				dataM.put("SEAL_CHAM_MAX",ExcelUtil.getExcelValue(row.getCell(21),formulaEval));
//				dataM.put("RPM",ExcelUtil.getExcelValue(row.getCell(22)));
//				dataM.put("SHAFT_SIZE",ExcelUtil.getExcelValue(row.getCell(23)));
				
				//for(int j=0 ; j<row.getPhysicalNumberOfCells() ;j++){
				//	XSSFCell cell = row.getCell(j);
				//}
				
				// 리스트에 추가
				dataL.add(dataM);
			}
			 
		}finally {
			if(workbook!=null) workbook.close();
		}
		return dataL;
	}
	*/
	
	
	public String setPredictInfoToExcelUploadFile(Map<String,Object> fileInfo, List<String> predictInfo, Map<String,Object> param) throws Exception {
		
		FileInputStream file = new FileInputStream( fileInfo.get("file_path")   + File.separator  +  fileInfo.get("file_name") ); //하드 디스크상에 존재하는 파일로부터 바이트단위의 입력을 받는 클래스.
		System.out.println("FileInputStream end");
		// 엑셀 
		XSSFWorkbook workbook = new XSSFWorkbook(file);
		//System.out.println("workbook end");
		// 시트 
		//XSSFSheet sheet = workbook.getSheet("sheet name");
		XSSFSheet sheet = workbook.getSheetAt(0); // 첫번째 Sheet
		
		int startRow = 16;
		XSSFRow row;
		String [] predicts, sealType, material = null;
		for (String predict : predictInfo) {
			row = sheet.getRow(startRow);
			//System.out.println("predict : " + predict);
			
			predicts = predict.split("[|]");
			//System.out.println("predicts length : " + predicts.length);
			//Seal Type | Material | API Plan 구조
			// 25 / 26      27 / 28        33   <- cell index
			// 내    외        내    외
			sealType = predicts.length>0?predicts[0].split("[/]"):new String[] {}; //예측결과값 작성.
			material = predicts.length>1?predicts[1].split("[/]"):new String[] {};
			
			ExcelUtil.setExcelValue(row.getCell(25),sealType.length>0?sealType[0]:"");
			//System.out.println("sealType[0] : " + (sealType.length>0?sealType[0]:""));
			ExcelUtil.setExcelValue(row.getCell(26), sealType.length>1?sealType[1]:"");
			//System.out.println("sealType[1] : " + (sealType.length>1?sealType[1]:""));
			ExcelUtil.setExcelValue(row.getCell(29), material.length>0?material[0]:"");
			//System.out.println("material[0] : " + (material.length>0?material[0]:""));
			ExcelUtil.setExcelValue(row.getCell(30), material.length>1?material[1]:"");
			//System.out.println("material[1] : " + (material.length>1?material[1]:""));
			ExcelUtil.setExcelValue(row.getCell(33), predicts.length>2?predicts[2]:"");
			//System.out.println("api" + (predicts.length>2?predicts[2]:""));
			startRow++;
		}
		//0327 기존 저장되어있는 cell의값과 model의 min max값을 비교하여 해당하는 셀에 폰트색 변경 소스 넣어주기.
		String pump_type,product ="";
		List<Map<String,Object>> FeatureList_param_list = (List<Map<String,Object>>)param.get("FeatureList");
		System.out.println("FeatureList_param_list:::::::"+FeatureList_param_list);
		List<Map<String,Object>> ModelFeatureRangeList = (List<Map<String,Object>>)param.get("FeatureRangeList"); //feature model range list
		String []FeatureList_condition_list_array= {"SPEC_GRAVITY_NOR","VISC_NOR","TEMP_NOR","TEMP_MIN","TEMP_MAX","VAP_PRES_NOR","SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX","RPM_NOR","SHAFT_SIZE"};
		int []FeatureList_condition_list_index= {7,8,9,10,11,12,19,20,21,22,23}; //엑셀파일에 대한 위에 셀의 위치
		int conditionRow = 16;
		Cell cell = null; 
		//XSSFCell cell = null;
		Font fontRed = workbook.createFont();
		fontRed.setColor(IndexedColors.RED.getIndex());
		fontRed.setFontName("맑은 고딕");
		Font fontBlue = workbook.createFont();
		fontBlue.setColor(IndexedColors.BLUE.getIndex()); //red와blue를 분기처리해서 사용
		fontBlue.setFontName("맑은 고딕");
		
		
		for(Map<String,Object> FeatureList_condition_list : FeatureList_param_list){ //인자값
		
			row = sheet.getRow(conditionRow);
		
			int idx  =0;
			
			for ( String FeatureList_condition : FeatureList_condition_list_array){ //모델값
				System.out.println("FeatureList_condition:::"+FeatureList_condition);
				double featureMax=0d; //최대값
				double featureMin=0d; //최소값
				String feature_val=""; //
				String feature_val_C=""; //
				for(Map<String, Object> ModelFeatureRangeLists : ModelFeatureRangeList){
					if (String.valueOf(ModelFeatureRangeLists.get("FEATURE_COL")).equals(FeatureList_condition)){ // FEATURE_COL 이름이 같을때
						featureMax = Double.parseDouble(String.valueOf(ModelFeatureRangeLists.get("MAX_VAL"))); //해당 Feature MAX ,MIN 값 추출
						featureMin = Double.parseDouble(String.valueOf(ModelFeatureRangeLists.get("MIN_VAL")));
						break;
					}
				}
				feature_val = String.valueOf(FeatureList_condition_list.get(FeatureList_condition));// 인자값 String 으로 사용시 숫자 단위를 인식할수 없다.
				feature_val_C = String.valueOf(FeatureList_condition_list.get(FeatureList_condition +"_C"));// 단위 변환된 인자값. min, max 비교용
				System.out.println("feature_val 엑셀 삽입 값:::::"+feature_val);
				System.out.println("feature_val_C 단위변환된 비교 값:::::"+feature_val_C);
				
				XSSFCellStyle style = workbook.createCellStyle();
				XSSFDataFormat df = workbook.createDataFormat();
				style.setDataFormat(df.getFormat("General")); // General:일반 . #,##0 :통상, 0.00:사용자지정 => 모든값이 String으로 들어가서 자릿수 작용X.
				cell = row.getCell(FeatureList_condition_list_index[idx]);
				
				if(!feature_val.equals("")){  //인자값이 공백이지 않을 때
					/*if(FeatureList_condition_list_index[idx] == 19){ // SEAL_CHAM_NOR 컬럼에 수식이 존재.
						cell.setCellFormula(null); //업로드된 엑셀에 걸려있는 function 수식 초기화.
					}*/
					cell.setCellFormula(null); //업로드된 엑셀에 걸려있는 function 수식 초기화.
					ExcelUtil.setExcelValue(row.getCell(FeatureList_condition_list_index[idx]),feature_val); //cell에 setvalue(값을 넣어줌)
					
					
					if (Double.parseDouble(feature_val_C) > featureMax){ //비교할때 Double사용 ,단위변환된 인자값이 모델의 MAX값 초과.
						style.setFont(fontRed); //빨간색
					}else if (Double.parseDouble(feature_val_C) < featureMin){//단위변환된 인자값이 모델의 MIN값 미만.
						style.setFont(fontBlue); //파란색
					}
					
					style.setVerticalAlignment(VerticalAlignment.CENTER);  //세로-가운데정렬
					style.setAlignment(HorizontalAlignment.CENTER); //가로-가운데정렬
					style.setWrapText(true); //자동줄바꿈
					style.setBorderRight(BorderStyle.THIN); //style border 
					style.setBorderLeft(BorderStyle.THIN);
					style.setBorderTop(BorderStyle.THIN);
					style.setBorderBottom(BorderStyle.THIN);
					cell.setCellStyle(style); //해당 cell에 style 적용
				}	
				idx++;
			}
			pump_type = String.valueOf(FeatureList_condition_list.get("PUMP_TYPE"));
			product = String.valueOf(FeatureList_condition_list.get("PRODUCT"));
			
			ExcelUtil.setExcelValue(row.getCell(2),pump_type);
			ExcelUtil.setExcelValue(row.getCell(5),product);
			
			conditionRow++;
		}
		
		//0327
		
		File saveFile = new File(fileInfo.get("file_path")   + File.separator  +  fileInfo.get("file_name") );
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream(saveFile);
            workbook.write(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(workbook!=null) workbook.close();
                if(fos!=null) fos.close();
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		return "ok";
	}
	//파일업로드가아닌 직접 예측조건 입력시 엑셀 다운로드
	public String setPredictInfoToExcelUploadFile2(Map<String,Object> param, List<String> predictInfo, List<String> FeatureRangeList) throws Exception {
		
		String excelTemplatefile = propertyService.getString("excelTemplatefile"); //key값으로 context-properties.xml 에 선언했던 경로를 가지고 온다.
		String excelTemplateFileName = "predict_sheet.xlsm"; //TODO (엑셀파일명) 엑셀저장시 생성된 엑셀파일(템플릿)의 파일이름. 
		FileInputStream file = new FileInputStream(excelTemplatefile  + File.separator  + excelTemplateFileName); //하드 디스크상에 존재하는 파일로부터 바이트단위의 입력을 받는 클래스=> 해당경로의 템플릿 파일을 입력받는다.
		
		//엑셀template파일을 복사하는 코드
		FileInputStream originFile = null; //복사할 대상 파일 (엑셀기본템플릿)
		FileOutputStream copyFile= null; //복사된 파일
		
		String[] SplitedFileName = excelTemplateFileName.split("\\."); // . 하나의 도트는 예약어이기때문에 사용하기위해서 \\를 붙여준다 ->엑셀파일을 . 기준으로 splite
		String copyFileName = ""; //복사된파일의 이름을 변경하기위해 선언
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		String date = sdf.format(today); //파일명+날짜 를위한 날짜 소스코드
		if(SplitedFileName.length > 1){
			int i=0;
			while(i < SplitedFileName.length-1){
				copyFileName += SplitedFileName[i]; //copyFileName 에 samplesheet(0번째 배열=파일명) 삽입.
				i++;
			}
			copyFileName += "_"+ date + "." + SplitedFileName[SplitedFileName.length-1]; //copyFileName = samplesheet + _날짜. + xlsm => 최종 파일명
		}
		
		if(copyFileName.equals("")){ //최종 파일명 유효성체크
			throw new Exception(); 
		}
		
		//origin excel 파일을 복사하는 과정
		try {
			originFile = file;
			copyFile = new FileOutputStream(excelTemplatefile + File.separator  + copyFileName); //복사된 파일의 위치를 지정해줘야함. => 같은위치에 파일명이 다르게 복사되어 생성됨.
			
			byte[] buffer = new byte[1024];
			int readcount = 0;
			
			while((readcount=originFile.read(buffer))!= -1){
				copyFile.write(buffer, 0, readcount);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			originFile.close();
			copyFile.close();
		}
		//System.out.println("엑셀파일복사 끝");
		
		System.out.println("FileInputStream end2::::data insert ");
		System.out.println(param);
		System.out.println("FeatureRangeList:::::::::::"+FeatureRangeList); //비교로직 생성 - color 넣어주기
		FileInputStream file2 =  new FileInputStream(excelTemplatefile + File.separator  + copyFileName); //복사된 파일을 다시열어줘서 excel data 입력
		
		// 엑셀 데이터 입력 
		XSSFWorkbook workbook = new XSSFWorkbook(file2);
		//System.out.println("workbook end");
		// 시트 
		//XSSFSheet sheet = workbook.getSheet("sheet name");
		XSSFSheet sheet = workbook.getSheetAt(0); // 첫번째 Sheet
		int unitRow =12; //예측조건단위 row
		int conditionRow =16;//예측조건값 row
		int startRow = 16; //예측결과 행
		XSSFRow row;
		
		//예측조건-단위 삽입
		String temp_unit,visc_unit,var_pres_unit,seal_cham_unit,rpm_unit,shaft_size_unit = "";
		List<Map<String,Object>> FeatureList_param_list = (List<Map<String,Object>>)param.get("FeatureList");
		System.out.println("FeatureList_param_list:::::"+FeatureList_param_list);
		
		for (Map<String,Object> FeatureList_unit_list : FeatureList_param_list) {
			//System.out.println(FeatureList_unit_list); 0325 완료후 주석해제.
			row = sheet.getRow(unitRow);
			temp_unit = String.valueOf(FeatureList_unit_list.get("TEMP_TEXT"));
			visc_unit = String.valueOf(FeatureList_unit_list.get("VISC_TEXT"));
			var_pres_unit = String.valueOf(FeatureList_unit_list.get("VAP_PRES_TEXT"));
			seal_cham_unit = String.valueOf(FeatureList_unit_list.get("SEAL_CHAM_TEXT"));
			rpm_unit = String.valueOf(FeatureList_unit_list.get("RPM_TEXT"));
			shaft_size_unit = String.valueOf(FeatureList_unit_list.get("SHAFT_SIZE_TEXT"));
			
			ExcelUtil.setExcelValue(row.getCell(8),visc_unit);
			ExcelUtil.setExcelValue(row.getCell(9),temp_unit);
			ExcelUtil.setExcelValue(row.getCell(10),temp_unit);
			ExcelUtil.setExcelValue(row.getCell(11),temp_unit);
			ExcelUtil.setExcelValue(row.getCell(12),var_pres_unit);
			ExcelUtil.setExcelValue(row.getCell(19),seal_cham_unit);
			ExcelUtil.setExcelValue(row.getCell(22),rpm_unit);
			ExcelUtil.setExcelValue(row.getCell(23),shaft_size_unit); //12번째 row에 그려줌
			
		}
		//예측조건-값 삽입
		String pump_type,product,temp_nor,temp_min,temp_max,spec_gravity_nor,spec_gravity_min,spec_gravity_max,visc_nor,visc_min,visc_max = "";
		String vap_pres_nor,vap_pres_min,vap_pres_max,seal_cham_nor,seal_cham_min,seal_cham_max,rpm_nor,rpm_min,rpm_max,shaft_size = "";
		
		//model min max 값 판단.
		List<Map<String,Object>> ModelFeatureRangeList = (List<Map<String,Object>>)param.get("FeatureRangeList"); //feature model range list
		String []FeatureList_condition_list_array= {"SPEC_GRAVITY_NOR","VISC_NOR","TEMP_NOR","TEMP_MIN","TEMP_MAX","VAP_PRES_NOR","SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX","RPM_NOR","SHAFT_SIZE"};
		int []FeatureList_condition_list_index= {7,8,9,10,11,12,19,20,21,22,23}; //엑셀파일에 대한 위에 셀의 위치
		
		Cell cell = null;
		Font fontRed = workbook.createFont();
		fontRed.setColor(IndexedColors.RED.getIndex());
		fontRed.setFontName("맑은 고딕");
		Font fontBlue = workbook.createFont();
		fontBlue.setColor(IndexedColors.BLUE.getIndex()); //red와blue를 분기처리해서 사용
		fontBlue.setFontName("맑은 고딕");


		for(Map<String,Object> FeatureList_condition_list : FeatureList_param_list){ //인자값
		
			row = sheet.getRow(conditionRow);
		
			int idx  =0;
			
			for ( String FeatureList_condition : FeatureList_condition_list_array){ //모델값
				System.out.println("FeatureList_condition:::"+FeatureList_condition);
				double featureMax=0d; //최대값
				double featureMin=0d; //최소값
				String feature_val=""; //
				String feature_val_C=""; //
				
				for(Map<String, Object> ModelFeatureRangeLists : ModelFeatureRangeList){
					if (String.valueOf(ModelFeatureRangeLists.get("FEATURE_COL")).equals(FeatureList_condition)){ // FEATURE_COL 이름이 같을때
						featureMax = Double.parseDouble(String.valueOf(ModelFeatureRangeLists.get("MAX_VAL")));
						featureMin = Double.parseDouble(String.valueOf(ModelFeatureRangeLists.get("MIN_VAL")));
						break;
					}
					
				}
				feature_val = String.valueOf(FeatureList_condition_list.get(FeatureList_condition));// 인자값
				feature_val_C = String.valueOf(FeatureList_condition_list.get(FeatureList_condition +"_C"));// 단위 변환된 인자값. min, max 비교용
				System.out.println("feature_val 엑셀삽입값:::::"+feature_val);
				System.out.println("feature_val_C 비교할값:::::"+feature_val_C);
				XSSFCellStyle style = workbook.createCellStyle();
				style.setVerticalAlignment(VerticalAlignment.CENTER);  //세로-가운데정렬
				style.setAlignment(HorizontalAlignment.CENTER); //가로-가운데정렬
				style.setWrapText(true); //자동줄바꿈
				
				ExcelUtil.setExcelValue(row.getCell(FeatureList_condition_list_index[idx]),feature_val); //해당 row에 idx번째 cell에 setvalue. =>feature_val 인자값
				
				if(!feature_val.equals("")){  //인자값이 공백이지 않을 때
					if (Double.parseDouble(feature_val_C) > featureMax){ //비교할때 Double사용 , 인자값이 모델의 MAX값 초과.
						style.setFont(fontRed); //빨간색
					}else if (Double.parseDouble(feature_val_C) < featureMin){//인자값이 모델의 MIN값 미만.
						style.setFont(fontBlue); //파란색
					}
					style.setBorderRight(BorderStyle.THIN); //style border 
					style.setBorderLeft(BorderStyle.THIN);
					style.setBorderTop(BorderStyle.THIN);
					style.setBorderBottom(BorderStyle.THIN);
					cell = row.getCell(FeatureList_condition_list_index[idx]); //row의 cell위치 추출
					cell.setCellStyle(style); //해당 cell에 style 적용
				}	
				idx++;
			}
			
			pump_type = String.valueOf(FeatureList_condition_list.get("PUMP_TYPE"));
			product = String.valueOf(FeatureList_condition_list.get("PRODUCT"));
			/*
			temp_nor = String.valueOf(FeatureList_condition_list.get("TEMP_NOR"));//   MIN . MAX 판단 전 소스코드
			temp_min = String.valueOf(FeatureList_condition_list.get("TEMP_MIN"));//
			temp_max = String.valueOf(FeatureList_condition_list.get("TEMP_MAX"));//
			spec_gravity_nor = String.valueOf(FeatureList_condition_list.get("SPEC_GRAVITY_NOR"));//
			spec_gravity_min = String.valueOf(FeatureList_condition_list.get("SPEC_GRAVITY_MIN"));
			spec_gravity_max = String.valueOf(FeatureList_condition_list.get("SPEC_GRAVITY_MAX"));
			visc_nor = String.valueOf(FeatureList_condition_list.get("VISC_NOR"));//
			visc_min = String.valueOf(FeatureList_condition_list.get("VISC_MIN"));
			visc_max = String.valueOf(FeatureList_condition_list.get("VISC_MAX"));
			vap_pres_nor = String.valueOf(FeatureList_condition_list.get("VAP_PRES_NOR"));//
			vap_pres_min = String.valueOf(FeatureList_condition_list.get("VAP_PRES_MIN"));
			vap_pres_max = String.valueOf(FeatureList_condition_list.get("VAP_PRES_MAX"));
			seal_cham_nor = String.valueOf(FeatureList_condition_list.get("SEAL_CHAM_NOR"));//
			seal_cham_min = String.valueOf(FeatureList_condition_list.get("SEAL_CHAM_MIN"));//
			seal_cham_max = String.valueOf(FeatureList_condition_list.get("SEAL_CHAM_MAX"));//
			rpm_nor = String.valueOf(FeatureList_condition_list.get("RPM_NOR"));//
			rpm_min = String.valueOf(FeatureList_condition_list.get("RPM_MIN"));
			rpm_max = String.valueOf(FeatureList_condition_list.get("RPM_MAX"));
			shaft_size = String.valueOf(FeatureList_condition_list.get("SHAFT_SIZE"));// 
			*/ 
			ExcelUtil.setExcelValue(row.getCell(2),pump_type);
			ExcelUtil.setExcelValue(row.getCell(5),product);
			/*
			ExcelUtil.setExcelValue(row.getCell(7),spec_gravity_nor); //MIN . MAX 판단 전 소스코드
			ExcelUtil.setExcelValue(row.getCell(8),visc_nor);
			ExcelUtil.setExcelValue(row.getCell(9),temp_nor);
			ExcelUtil.setExcelValue(row.getCell(10),temp_min);
			ExcelUtil.setExcelValue(row.getCell(11),temp_max);
			ExcelUtil.setExcelValue(row.getCell(12),vap_pres_nor);
			ExcelUtil.setExcelValue(row.getCell(19),seal_cham_nor);
			ExcelUtil.setExcelValue(row.getCell(20),seal_cham_min);
			ExcelUtil.setExcelValue(row.getCell(21),seal_cham_max);
			ExcelUtil.setExcelValue(row.getCell(22),rpm_nor);
			ExcelUtil.setExcelValue(row.getCell(23),shaft_size); //엑셀에서는 전체를 뿌려주는게 아닌 이 값들만 출력함.
			*/
			conditionRow++;
		}
		
		
		//예측결과 엑셀 삽입
		String [] predicts, sealType, material = null;
		for (String predict : predictInfo) {
			row = sheet.getRow(startRow);
			//System.out.println("predict : " + predict);
			
			predicts = predict.split("[|]");
			//System.out.println("predicts length : " + predicts.length);
			//Seal Type | Material | API Plan 구조
			// 25 / 26      27 / 28        33   <- cell index
			// 내    외        내    외
			sealType = predicts.length>0?predicts[0].split("[/]"):new String[] {};
			material = predicts.length>1?predicts[1].split("[/]"):new String[] {};
			
			ExcelUtil.setExcelValue(row.getCell(25),sealType.length>0?sealType[0]:"");
			//System.out.println("sealType[0] : " + (sealType.length>0?sealType[0]:""));
			ExcelUtil.setExcelValue(row.getCell(26), sealType.length>1?sealType[1]:"");
			//System.out.println("sealType[1] : " + (sealType.length>1?sealType[1]:""));
			ExcelUtil.setExcelValue(row.getCell(29), material.length>0?material[0]:"");
			//System.out.println("material[0] : " + (material.length>0?material[0]:""));
			ExcelUtil.setExcelValue(row.getCell(30), material.length>1?material[1]:"");
			//System.out.println("material[1] : " + (material.length>1?material[1]:""));
			ExcelUtil.setExcelValue(row.getCell(33), predicts.length>2?predicts[2]:"");
			//System.out.println("api" + (predicts.length>2?predicts[2]:""));
			startRow++;
		}
		
		//File saveFile = new File(fileInfo.get("file_path")   + File.separator  +  fileInfo.get("file_name") );
		File saveFile = new File(excelTemplatefile + File.separator  + copyFileName);
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream(saveFile);
            workbook.write(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(workbook!=null) workbook.close();
                if(fos!=null) fos.close();
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		return ""+copyFileName+""; //return  값으로 파일명을 넘김=> 파라미터로 해당 파일을 찾기위해서
	}
	

	/**
	 * Grouping 정보를 바탕으로 데이터 변환
	 * - Grouping 정보를 반환
	 * Product, Pump Type
	 * @param sProduct
	 * @param listGrpInfo
	 * @return
	 */
	public String getGroupingStr(String sOrgVal, List<Map<String,Object>> listGrpInfo, List<Map<String,Object>> listGroupHierInfo) {
		String sGroupResult = "";
		sOrgVal = sOrgVal.toUpperCase(); // 대문자 처리
		
		//System.out.println("---------------------------------------------");
		//System.out.println("getGroupingStr :::::::: sOrgVal : " + sOrgVal);
		for (Map<String,Object> m : listGrpInfo ){
			
			if (sOrgVal.contains(String.valueOf(m.get("GRP_SUB")))  && !sGroupResult.contains( String.valueOf(m.get("GRP"))) ) { // 기존에 포함되지 않은 경우
				
				//비교대상 단어가 3자리 이하인경우
				if(  String.valueOf(m.get("GRP_SUB")).length() <=3 ) {
					
					//좌우에 문자이외의 값이 있는 경우를 제외
					//[^a-zA-Z]*\b단어\b[^a-zA-Z]*
					Pattern p = Pattern.compile("[^a-zA-Z]*\\b"+ String.valueOf(m.get("GRP_SUB"))+"\\b[^a-zA-Z]*");
					Matcher rm = p.matcher(sOrgVal);
					if (!rm.find()) {
						continue;
					}
				}
				
				sGroupResult = sGroupResult + String.valueOf(m.get("GRP")) +"+";
				sOrgVal = sOrgVal.replace(String.valueOf(m.get("GRP_SUB")), "_____"); //선택된 항목 Remove : 공백을 만듦으로서 다음 문자 체크때 의도치 않게 단어가 조합될 수 있으므로 별도 문자로 치환한다.
			}
		}
		//System.out.println("getGroupingStr :::::::: sGroupResult : " + sGroupResult);
		
		if (sGroupResult.length()>0) {
			sGroupResult = sGroupResult.substring(0,sGroupResult.length()-1);
		}else {
			sGroupResult = "-";
		}
		
		String[] tmp = sGroupResult.split("\\+");
		List<String> tmpList = new ArrayList<>(Arrays.asList(tmp)); // 리스트로 변환
		
		//그룹 Hierarchy 처리
		if( listGroupHierInfo != null) {
			for ( Map<String,Object> m : listGroupHierInfo) {
				if(tmpList.contains(String.valueOf(m.get("LV3")))){ // 3레벨이 있는 경우
					tmpList.remove(String.valueOf(m.get("LV2"))); //2레벨 항목 제거
					tmpList.remove(String.valueOf(m.get("LV1"))); //1레벨 항목 제거
				}
			}
			for ( Map<String,Object> m : listGroupHierInfo) {
				if(tmpList.contains(String.valueOf(m.get("LV2")))){ // 2레벨이 있는 경우
					tmpList.remove(String.valueOf(m.get("LV1")));// 1레벨 항목 제거
				}
			}
		}
		
		tmpList.sort(null);
		
		//결과 정렬
		//String[] tmp = sGroupResult.split("\\+");
		//Arrays.sort(tmp); // sort
		sGroupResult = String.join("+", tmpList);
		
		//System.out.println("getGroupingStr :::::::: sGroupResult 최종 : " + sGroupResult);
		
		return sGroupResult;
	}
	
	
	/**
	 * Product Group 정보를 바탕으로 유효한 Product 정보를 추출한다.
	 * - Grouping 하위 Product  정보를 반환
	 */
	public String getProductStr(String sOrgVal, List<Map<String,Object>> listGrpInfo, int ord) {
		String sProductResult = "";
		List<String> productList = new ArrayList<String>();
		sOrgVal = sOrgVal.toUpperCase(); // 대문자 처리
		
		for (Map<String,Object> m : listGrpInfo ){
			//if (sOrgVal.contains(String.valueOf(m.get("GRP_SUB")))  && !sProductResult.contains( String.valueOf(m.get("GRP_SUB"))) ) { // 기존에 포함되지 않은 경우
			if (sOrgVal.contains(String.valueOf(m.get("GRP_SUB"))) && !productList.contains(String.valueOf(m.get("GRP_SUB")))) { 
				//비교대상 단어가 3자리 이하인경우
				if(  String.valueOf(m.get("GRP_SUB")).length() <=3 ) {
					//좌우에 문자(a~z)값이 있는 경우를 제외
					//[^a-zA-Z]*\b단어\b[^a-zA-Z]*
					Pattern p = Pattern.compile("[^a-zA-Z]*\\b"+ String.valueOf(m.get("GRP_SUB"))+"\\b[^a-zA-Z]*");
					Matcher rm = p.matcher(sOrgVal);
					if (!rm.find()) {
						continue;
					}
				}
				
				productList.add( String.valueOf(m.get("GRP_SUB")) );
				sOrgVal = sOrgVal.replace(String.valueOf(m.get("GRP_SUB")), "_____"); //선택된 항목 Remove : 공백을 만듦으로서 다음 문자 체크때 의도치 않게 단어가 조합될 수 있으므로 별도 문자로 치환한다.
			}
		}
		//System.out.println("getGroupingStr :::::::: sGroupResult : " + sGroupResult);
		if (ord==1) { // 정렬조건일때
			productList.sort(null);// 정렬
		}
		
		//결과 정렬
		sProductResult = String.join("+", productList);
		return sProductResult;
	}
	
	private List<String> getNominalAttrList(String key, List<Map<String,Object>> list ){
		List<String> nominalAttrList = new ArrayList<String>();
		for(Map<String,Object> m : list) {
			if ( !nominalAttrList.contains(String.valueOf(m.get(key)))){ 
				nominalAttrList.add(String.valueOf(m.get(key)));
			}
		}
		return nominalAttrList;
	}
	
	
	public List<Map<String,Object>> getUnitInfo() throws Exception{
		return mLMapper.getUnitInfoList();
	}
	
	public List<Map<String,Object>> getUnitDefaultInfoList() throws Exception{
		return mLMapper.getUnitDefaultInfoList();
	}
	
	
	public List<Map<String,Object>> getSealTypeInfo() throws Exception{
		return mLMapper.getSealTypeInfo();
	}
	
	public List<Map<String,Object>> getSealTypeInfo2() throws Exception{
		return mLMapper.getSealTypeInfo2();
	}
	
	public List<Map<String,Object>> getSealTypeInfo_new() throws Exception{
		return mLMapper.getSealTypeInfo_new();
	}
	
	
	/**
	 * 엑셀 데이터에 대한 예외보정처리를 한다.
	 * @param val
	 * @return
	 */
	private String getExcelCalibrationData(String val) {
		if("-".equals(val) || "\"\"".equals(val)) {
			return "";
		}else {
			return val;
		}
	}
	
	public List<Map<String,Object>> getFeatureRangeList() throws Exception{
		return mLMapper.getFeatureRangeList();
	}
	
	
//	public synchronized void modelInitialize() throws Exception {
//		
//		Map<String,Object>  modelInfo = null;
//		String sModelId = "";
//		if(!initialized) {
//			System.out.println("initialized start");
//			sModelId = getModelId("EPC"); //최근모델정보를 가져온다. ( 단계별 구분 )
//			modelInfo = getModelInfo(sModelId, "SEAL_TYPE");
//			classifier_SEAL_TYPE_EPC = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); // Seal Type 모델
//			
//			System.out.println("classifier_SEAL_TYPE_EPC end");
//			
//			
//			modelInfo = getModelInfo(sModelId, "API_PLAN");
//			classifier_API_PLAN_EPC = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
//			
//			System.out.println("classifier_API_PLAN_EPC end");
//			
//			modelInfo = getModelInfo(sModelId, "CONN_COL");
//			classifier_CONN_COL_EPC = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
//			
//			System.out.println("classifier_CONN_COL_EPC end");
//			
//			
//			sModelId = getModelId("OEM"); //최근모델정보를 가져온다. ( 단계별 구분 )
//			modelInfo = getModelInfo(sModelId, "SEAL_TYPE");
//			classifier_SEAL_TYPE_OEM = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); // Seal Type 모델
//			modelInfo = getModelInfo(sModelId, "API_PLAN");
//			classifier_API_PLAN_OEM = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
//			modelInfo = getModelInfo(sModelId, "CONN_COL");
//			classifier_CONN_COL_OEM = (RandomForest)ModelUtil.modelLoad((String)modelInfo.get("MODEL_LOC")); //Api Plan 모델
//				
//			
//			System.out.println("classifier_CONN_COL_OEM end");
//			
//			initialized=true;
//		}
//		
//	}
//	
//	
//	public void mlInit(Map<String,Object> param) throws Exception {
//		initialized = false;
//		modelInitialize();
//	}	
	
	
	/** old func *********************************************************************************************************************/
	/**
	 * Model Build And Predict
	 */
	
//	/**
//	 * Model Load And Predict
//	 */
//	public Map<String,Object>  modelPredict(Map<String,Object> param) throws Exception {
//		
//		Map<String,Object> result = new HashMap<String,Object>();
//		
//		System.out.println(param.get("seal_all_check"));
//		//String[] targets = {"SEAL_TYPE", "SEAL_SIZE", "SEAL_CONFIG", "SEAL_ALL"}; // target 목록   
//		List<String> targets = new ArrayList<String>();
//		if((boolean)param.get("seal_all_check")) targets.add("SEAL_ALL");
//		if((boolean)param.get("seal_type_check")) targets.add("SEAL_TYPE");
//		if((boolean)param.get("seal_size_check")) targets.add("SEAL_SIZE");
//		if((boolean)param.get("seal_config_check")) targets.add("SEAL_CONFIG");
//		System.out.println("target : " + targets.toString());
//		
//		Map<String,List<String>> nominalAttr = null;
//		Map<String,Object> opt = null;
//		List<Map<String,Object>> dataList = null;
//		LinkedHashMap<String, Object> predict_param = null;
//		//m.put("PRODUCT", );
//		
//		String[] sFeatures = null;
//		
//		DecisionTreeMain dtm = new DecisionTreeMain();
//		for (String target : targets.toArray(new String[targets.size()])) {
//			
//			Map<String,Object> modelInfo = getModelInfo((String)param.get("MODEL_ID"), target);
//			if (modelInfo == null) continue;
//			
//			String sProductApply = modelInfo.get("ATTR2")==null?"N":(String)modelInfo.get("ATTR2");
//	
//			System.out.println("sProductApply :::: " + sProductApply);
//			
//			// product를 feature로 적용하지 않는 경우경우
//			if("Y".equals(sProductApply)) {
//				sFeatures = new String[]{"PRODUCT", "TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
//					"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
//					"VISC_NOR","VISC_MIN","VISC_MAX",
//					"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX"
//					};
//			}else {
//				sFeatures = new String[]{"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
//						"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
//						"VISC_NOR","VISC_MIN","VISC_MAX",
//						"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX"
//						};
//			}
//			System.out.println("sFeatures : " + sFeatures.length);
//			
//			//예측 Feature base
//			predict_param = new LinkedHashMap<String,Object>();
//			for(String sf : sFeatures) {
//				predict_param.put(sf,param.get(sf)); 
//			}
//			
//			//Map<String,Object> data = (LinkedHashMap<String,Object>)predict_param.clone();
//			predict_param.put(target, "");
//			dataList = new ArrayList<Map<String,Object>>();
//			dataList.add(predict_param);
//			
//			opt = new HashMap<String,Object>();
//			opt.put("modelSubType", target);
//			opt.put("dataType", "P"); // Predict data
//			
//			opt.put("modelPath", modelInfo.get("MODEL_LOC")); //모델파일경로
//			opt.put("productApply", modelInfo.get("ATTR2")); //product apply 유무
//			
//			nominalAttr = new HashMap<String,List<String>>();
//			nominalAttr.put(target, getPredictTargetClass((String)param.get("MODEL_ID"), target)); //모델생성당시 클래스 정보
//			
//			if("Y".equals(sProductApply)) {
//				nominalAttr.put("PRODUCT", getPredictProduct((String)param.get("MODEL_ID")));  //모델생성당시 Product 정보
//			}
//			
//			opt.put("nominalAttrs", nominalAttr); // nominal attribute
//			opt.put("classIdx", null); // 클래스인덱스가 없을 경우 마지막 Attribute를 class로 설정
//			
//			//Seal All 전체 예측과 Product가 적용된 경우는 Decisiontree로 처리한다.
//			if ("Y".equals(sProductApply) &&  "SEAL_ALL".equals(target) ) {
//				opt.put("classifier", "weka.classifiers.trees.J48");
//			}
//			
//			System.out.println("Option : " + opt.toString());
//			//System.out.println("dataList : " + dataList.toString());
//			
//			//모델 예측
//			Map<String,Object> predict = null;
//			if (modelInfo.get("MODEL_LOC") != null && !"".equals(modelInfo.get("MODEL_LOC"))) {
//				predict = dtm.process("predict", null, dataList, opt);
//			}else {
//				predict = new HashMap<String,Object>();
//			}
//			
//			result.put(target,predict.get("predict_result"));
//		}// end for 
//		return result;
//	}
	
	
/*
	@SuppressWarnings("unchecked")
	public Map<String,Object>  modelPredictWithSearch(Map<String,Object> param) throws Exception {
		
		Map<String,Object> result = new HashMap<String,Object>();
		boolean isProductApply = (boolean)param.get("product_apply_check");
		
		List<Map<String,Object>> searchList = (ArrayList<Map<String,Object>>)param.get("search_list"); //데이터 조회화면 List
		System.out.println("size :  " + searchList.size());
		
		List<Map<String,Object>> trainingList = null;
		LinkedHashMap<String, Object> training_param = null;
		
		List<Map<String,Object>> predictList = null;
		LinkedHashMap<String, Object> predict_param = new LinkedHashMap<String,Object>();
		
		//예측 항목 (항목별로 모델 예측실행)
		//String[] targets = {"SEAL_TYPE", "SEAL_SIZE", "SEAL_CONFIG", "SEAL_ALL"}; // target 목록   
		List<String> targets = new ArrayList<String>();
		if((boolean)param.get("seal_type_check")) targets.add("SEAL_TYPE");
		if((boolean)param.get("api_plan_check")) targets.add("API_PLAN");
		if((boolean)param.get("conn_col_check")) targets.add("CONN_COL");
		System.out.println("target : " + targets.toString());
		
		Map<String,List<String>> nominalAttr = null;
		Map<String,Object> opt = null;
		
		String[] sFeatures = {
				"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
				"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
				"VISC_NOR","VISC_MIN","VISC_MAX",
				"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
				"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX",
				"RPM_NOR","RPM_MIN","RPM_MAX","SHAFT_SIZE"
		};
		
		// product를 feature로 적용하지 않는 경우경우
		//if(!isProductApply) {
		//	sFeatures = (String[])ArrayUtils.removeElement(sFeatures, "PRODUCT");
		//}
		System.out.println("sFeatures : " + sFeatures.length);
		
		//예측 Feature base
		for(String sf : sFeatures) {
			//predict_param.put(sf,param.get(sf));
			if("".equals(param.get(sf)) && "PRODUCT".equals(sf)) {
				predict_param.put(sf,"-");
			}else {
				predict_param.put(sf,param.get(sf));
//				if("".equals(param.get(sf)) && !"PRODUCT".equals(sf)) {
//					predict_param.put(sf,"0.00001");
//				}else {
//					predict_param.put(sf,param.get(sf));
//				}
			}
		}
		
		DecisionTreeMain dtm = new DecisionTreeMain();
		for (String target : targets.toArray(new String[targets.size()])) {
			long t1 = System.currentTimeMillis();
			// training list
			// Target별로 Training List를 생성한다.
			trainingList = new ArrayList<Map<String,Object>>();
			List<String> productAttrDataList = new ArrayList<String>(); // product attribute list 
			List<String> classNominalAttrDataList = new ArrayList<String>(); // class attribute list 
			for(Map<String,Object> m : searchList) {
				training_param = new LinkedHashMap<String,Object>();
				String sClassVal = "";
				if("CONN_COL".equals(target)) { // 복합정보 Seal Type + Material + API Plan 형태로 데이트를 구성필요
					sClassVal = m.get("FSEAL_TYPE") + " / " + m.get("FMATERIAL") + " / " + m.get("FAPI_PLAN"); //클래스 항목 적용	
				}else {
					sClassVal = ""+m.get("F"+target); //클래스 항목 적용
				}
				if(sClassVal ==null || "".equals(sClassVal)) continue;
				
				for(String sf : sFeatures) {
					//training_param.put(sf,m.get("F"+sf)); // data 조회에서 컬럼명앞에 F 문자가 붙어서 넘어옴.
					
					if( (m.get("F"+sf) == null ||  "".equals(m.get("F"+sf))) && "PRODUCT".equals(sf)) {
						training_param.put(sf,"-");
					}else {
						training_param.put(sf,m.get("F"+sf));
//						if("".equals(m.get("F"+sf)) && !"PRODUCT".equals(sf)) {
//							training_param.put(sf,"0.00001");
//						}else {
//							training_param.put(sf,m.get("F"+sf));
//						}
					}
					
				}
				training_param.put(target, sClassVal); //클래스 항목 적용	(마지막)
				trainingList.add(training_param); //트레이닝 리스트에 추가
				
				//class attribute list 를 생성
				if ( !classNominalAttrDataList.contains(sClassVal)){ // 중복제거
					classNominalAttrDataList.add(sClassVal);
				}
				
				if(isProductApply) {
					String sProductVal = m.get("FPRODUCT")==null?"-":(String)m.get("FPRODUCT");
					if ( !productAttrDataList.contains(sProductVal)){ 
						productAttrDataList.add(sProductVal);
					}
				}
				
			}
			
			long t2 = System.currentTimeMillis();
			System.out.println("트레이닝 데이터 생성 : " + (t2-t1)/1000.0);
			
			// 예측데이터
			Map<String,Object> predictData = (LinkedHashMap<String,Object>)predict_param.clone();
			predictData.put(target, "");
			predictList = new ArrayList<Map<String,Object>>();
			predictList.add(predictData);
			
			// Option 생성
			opt = new HashMap<String,Object>();
			opt.put("modelSubType", target);
			opt.put("dataType", "TP"); // Training & Predict data
			
			//Seal All 전체 예측과 Product가 적용된 경우는 Decisiontree로 처리한다.
			//if (isProductApply && "SEAL_ALL".equals(target) ) {
			//	opt.put("classifier", "weka.classifiers.trees.J48");
			//}
			
			nominalAttr = new HashMap<String,List<String>>();
			nominalAttr.put(target,classNominalAttrDataList); //모델생성당시 클래스 정보
			//if(isProductApply) {
			//	nominalAttr.put("PRODUCT", productAttrDataList); // product attr
			//}
			
			opt.put("nominalAttrs", nominalAttr); // nominal attribute
			opt.put("classIdx", null); // 클래스인덱스가 없을 경우 마지막 Attribute를 class로 설정
			
			System.out.println("Option : " + opt.toString());
			System.out.println("Predict Data List : " + predictList.toString());
			
			//모델 예측
			Map<String,Object> predict = dtm.process("trainingNpredict", trainingList, predictList, opt);
			
			result.put(target,predict.get("predict_result"));
		} // end for
		
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map<String,Object>  predictMultiWithFiltering(Map<String,Object> param) throws Exception {
		
		Map<String,Object> result = new HashMap<String,Object>();
		Map<String,Object> result_predict = new HashMap<String,Object>();
		
		//List<Map<String,Object>> searchList = (ArrayList<Map<String,Object>>)param.get("search_list"); //데이터 조회화면 List
		//System.out.println("size :  " + searchList.size());
		
		List<Map<String,Object>> trainingList = null;
		LinkedHashMap<String, Object> training_param = null;
		
		List<Map<String,Object>> predictList = null;
		LinkedHashMap<String, Object> predict_param = new LinkedHashMap<String,Object>();
		
		//예측 항목 (항목별로 모델 예측실행)
		//String[] targets = {"SEAL_TYPE", "SEAL_SIZE", "SEAL_CONFIG", "SEAL_ALL"}; // target 목록   
		List<String> targets = new ArrayList<String>();
		if((boolean)param.get("target1_check")) targets.add("SEAL_TYPE");
		if((boolean)param.get("target2_check")) targets.add("API_PLAN");
		if((boolean)param.get("target3_check")) targets.add("CONN_COL");
		System.out.println("target : " + targets.toString());
		
		Map<String,List<String>> nominalAttr = null;
		Map<String,Object> opt = null;

		
		Map<String,Object> predict_param_full = (Map<String,Object>)param.get("predict_item");
		
		//"PUMP_TYPE", "PRODUCT",
		String[] sFeatures = null;
		
		if ("EPC".equals((String)param.get("predict_type"))) { // EPC
			//sFeatures = sFeaturesEPC;
			sFeatures = new String[]{
					"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
					"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
					"VISC_NOR","VISC_MIN","VISC_MAX",
					"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
					"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX"
			};
		}else { // OEM
			//sFeatures = sFeaturesOEM;
			sFeatures =  new String[]{
					"TEMP_NOR", "TEMP_MIN", "TEMP_MAX",
					"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
					"VISC_NOR","VISC_MIN","VISC_MAX",
					"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
					"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX",
					"RPM_NOR","RPM_MIN","RPM_MAX",
					"SHAFT_SIZE"
			};
		}
		
		//예측 Feature base
		for(String sf : sFeatures) {
			predict_param.put(sf,predict_param_full.get(sf));
		}
		
		// ProductGroup 정보를 가져온다
		List<Map<String,Object>> productGroupList = mLMapper.getProductGroupList(new HashMap<String,Object>());
		param.put("PUMP_TYPE", predict_param_full.get("PUMP_TYPE") ); // 펌프타입조건 추가
		// Product 조건
		//param.put("PRODUCT", predict_param_full.get("PRODUCT") );
		List<String> applyProductList = new ArrayList<String>();
		String sThisProduct = predict_param_full.get("PRODUCT")==null?"":predict_param_full.get("PRODUCT").toString();
		String sGrp="";
		for(Map<String,Object> m: productGroupList) {
			if(sThisProduct.indexOf(String.valueOf(m.get("GRP_SUB"))) >= 0) { // 그룹정보가 포함된 경우
				sGrp = String.valueOf(m.get("GRP"));
				if(!applyProductList.contains(sGrp)){
					applyProductList.add(sGrp);
				}
			}
		}
		System.out.println("PRODUCT_LIST : "+ applyProductList.toString());
		param.put("PRODUCT_LIST", applyProductList);
		// 트레이닝 대상 데이터
		//List<Map<String,Object>> searchList = mLMapper.getPredictTrainingList(param);
		List<Map<String,Object>> searchList = null;
		// 그룹핑정보가 없을 경우 처리하지 않음 -> 확인필요
		if (applyProductList.size() > 0 ) {
			searchList = mLMapper.getPredictTrainingList(param);
		}else {
			searchList = new ArrayList<Map<String,Object>>();
		}
		
		DecisionTreeMain dtm = new DecisionTreeMain();
		String sResultMsg = "";
		if(searchList.size() > 0) {
			
			for (String target : targets.toArray(new String[targets.size()])) { // Target Class 별 예측
				long t1 = System.currentTimeMillis();
				
				// training list
				// Target별로 Training List를 생성한다.
				// Pump Type과 Product를 필터링 하여 조회한다.
				trainingList = new ArrayList<Map<String,Object>>();
				
				List<String> classNominalAttrDataList = new ArrayList<String>(); // class attribute list
				
				for(Map<String,Object> m : searchList) {
					training_param = new LinkedHashMap<String,Object>();
					String sClassVal = m.get(target)==null?"":(String)m.get(target); //클래스 항목 적용
					
					//System.out.println("sClassVal : " + sClassVal);
					
					//if(sClassVal ==null || "".equals(sClassVal)) continue;
					
					for(String sf : sFeatures) {
						//training_param.put(sf,m.get("F"+sf)); // data 조회에서 컬럼명앞에 F 문자가 붙어서 넘어옴.
						training_param.put(sf,m.get(sf));
					}
					training_param.put(target, sClassVal); //클래스 항목 적용	(마지막)
					trainingList.add(training_param); //트레이닝 리스트에 추가
					
					//class attribute list 를 생성
					if ( !classNominalAttrDataList.contains(sClassVal)){ // 중복제거
						classNominalAttrDataList.add(sClassVal);
					}
				}
				
				long t2 = System.currentTimeMillis();
				System.out.println("트레이닝 데이터 생성 : " + (t2-t1)/1000.0);
				
				// 예측데이터
				Map<String,Object> predictData = (LinkedHashMap<String,Object>)predict_param.clone();
				predictData.put(target, "");
				predictList = new ArrayList<Map<String,Object>>();
				predictList.add(predictData);
				
				// Option 생성
				opt = new HashMap<String,Object>();
				opt.put("modelSubType", target);
				opt.put("dataType", "TP"); // Training & Predict data
				
				nominalAttr = new HashMap<String,List<String>>();
				nominalAttr.put(target,classNominalAttrDataList); //모델생성당시 클래스 정보
				opt.put("nominalAttrs", nominalAttr); // nominal attribute
				opt.put("classIdx", null); // 클래스인덱스가 없을 경우 마지막 Attribute를 class로 설정
				opt.put("listCnt",3);
				
				System.out.println("classNominalAttrDataList : " + classNominalAttrDataList.toString());
				System.out.println("Option : " + opt.toString());
				System.out.println("Predict Data List : " + predictList.toString());
				
				
				//모델 예측
				Map<String,Object> predict = null;
				
				// Class(Target) 가 한건인 경우 예외처리
				if(classNominalAttrDataList.size() == 1 ) {
					
					sResultMsg = sResultMsg + target + ":Case 1건 / ";
					
					predict = new HashMap<String,Object>();
					
					List<Map<String,Object>> classOneTmpPredictList = new ArrayList<Map<String,Object>>();
					Map<String,Object> classOneTmpPredict = new HashMap<String,Object>();
					classOneTmpPredict.put("NO", "1");
					classOneTmpPredict.put("CLASS", classNominalAttrDataList.get(0));
					classOneTmpPredict.put("PROB", "100");
					classOneTmpPredictList.add(classOneTmpPredict);
					
					predict.put("predict_result",classOneTmpPredictList);
					
				}else {
//					if("CONN_COL".equals(target)) {
//						opt.put("classifier", "weka.classifiers.trees.J48");
//					}
					predict = dtm.process("trainingNpredict", trainingList, predictList, opt);
				} 
				//모델 예측
				//Map<String,Object> predict = dtm.process("trainingNpredict", trainingList, predictList, opt);
				
				result_predict.put(target,predict.get("predict_result"));
			} // end for
		
			result.put("predict_idx",param.get("predict_idx"));
			if(!"".equals(sResultMsg)) {
				sResultMsg = " / " + sResultMsg;
			}
			result.put("predict_msg","complete " + sResultMsg); // 예측결과
			result.put("RESULT",result_predict);
			
			
		}else {
			
			result.put("predict_idx",param.get("predict_idx"));
			result.put("predict_msg","조회된 데이터가 없습니다."); // 예측결과
			result.put("RESULT","");
			
		}
		return result;
	}
*/
	
	public List<Map<String,Object>> getComboData() throws Exception{
		return mLMapper.getComboData();
	}
	
	public List<Map<String,Object>> getServiceCombo(Map<String, Object> param) throws Exception{
		return mLMapper.getServiceCombo(param);
	}
	
	public List<Map<String,Object>> getEquipmentCombo(Map<String, Object> param) throws Exception{
		return mLMapper.getEquipmentCombo(param);
	}
	
	public List<Map<String,Object>> getEquipmentTypeCombo() throws Exception{
		return mLMapper.getEquipmentTypeCombo();
	}
	
	public List<Map<String,Object>> getQuenchTypeCombo() throws Exception{
		return mLMapper.getQuenchTypeCombo();
	}
	
	public List<Map<String,Object>> getBrineGbCombo() throws Exception{
		return mLMapper.getBrineGbCombo();
	}
	
	public List<Map<String,Object>> getEndUserCombo() throws Exception{
		return mLMapper.getEndUserCombo();
	}
	
	public List<Map<String,Object>> getGroupCombo(Map<String, Object> param) throws Exception{
		return mLMapper.getGroupCombo(param);
	}
	
	public List<Map<String,Object>> getServiceGsCombo(Map<String, Object> param) throws Exception{
		return mLMapper.getServiceGsCombo(param);
	}
	
	public List<Map<String,Object>> getCaseCombo(Map<String, Object> param) throws Exception{
		return mLMapper.getCaseCombo(param);
	}
	
	public List<Map<String,Object>> getMngHistory(Map<String, Object> param) throws Exception{
		return mLMapper.getMngHistory(param);
	}
	
	public String savHistory(Map<String, Object> param) throws Exception{
		String userId = (String) param.get("USER_ID");
		Map<String,Object> hMap = (Map<String,Object>) param.get("header");
		hMap.put("USER_ID", userId);
		
		//title 중복 체크
		System.out.println("hMap : " + hMap);
		List<Map<String,Object>> check = mLMapper.getMngHistory(hMap);
		if(check.size() > 0){
			return "E01";
		}
		
		List<Map<String,Object>> dList =  new ArrayList<Map<String, Object>>();
		List<Map<String,Object>> tempList = (List<Map<String,Object>>) param.get("detail");
		
		for(Map<String,Object> dMap : tempList) {
			dMap.put("USER_ID", userId);
			dMap.put("historyId", hMap.get("historyId"));
			dList.add(dMap);
		}
    	Map<String,Object> paramMap = new HashMap<String,Object>();

		paramMap.put("list", dList);
		mLMapper.savHistory(hMap); // main
		mLMapper.savHistoryDetail(paramMap); // sub
		return "ok";
	}
	
	public void editHistory(Map<String, Object> param) throws Exception{
		String userId = (String) param.get("USER_ID");
		Map<String,Object> hMap = (Map<String,Object>) param.get("header");
		List<Map<String,Object>> dList =  new ArrayList<Map<String, Object>>();
		List<Map<String,Object>> tempList = (List<Map<String,Object>>) param.get("detail");
		for(Map<String,Object> dMap : tempList) {
			dMap.put("USER_ID", userId);
			dList.add(dMap);
		}
		Map<String,Object> paramMap = new HashMap<String,Object>();
		
		paramMap.put("list", dList);
		mLMapper.deleteHistorySub(hMap);
		mLMapper.savHistoryDetail(paramMap);
	}
	
	public void deleteHistory(Map<String, Object> param) throws Exception{
		mLMapper.deleteHistory(param);
		mLMapper.deleteHistorySub(param);
	}
	
	public List<Map<String,Object>> getFeatureData(Map<String, Object> param) throws Exception{
		return mLMapper.getFeatureData(param);
	}
}
