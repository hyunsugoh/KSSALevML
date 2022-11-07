package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 * @author 
 * @since 2019. 10.  X.
 */
@Mapper("mLMapper")
public interface MLMapper {

	/**
	 *  
	 * Model List 조회
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getModelList(Map<String,Object> param) throws Exception;

	/**
	 * Model Training/Test Data 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getModelTrainingList(Map<String,Object> param) throws Exception;
	
	
	/**
	 * Nominal Info List - Product  : CNV 테이블에서 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getProductList(Map<String,Object> param) throws Exception;
	
	/**
	 * Nominal Info List - Pump Type : CNV 테이블에서 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getPumpTypeList(Map<String,Object> param) throws Exception;
	
	/**
	 * Nominal Info List - Equip Type : CNV 테이블에서 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getEquipTypeList(Map<String,Object> param) throws Exception;
	
	
	/**
	 * 데이터 조회(M_DATA_CNV1) 리스트를 반환
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getMdataCnv1(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성정보 저장(Insert)
	 * @param param
	 * @throws Exception
	 */
	public void setModelInfo(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성정보 저장(Update)
	 * @param param
	 * @throws Exception
	 */
	public void setModelInfoSave(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 저장 - 클래스정보
	 * @param param
	 * @throws Exception
	 */
	public void setModelAttrClassInfo(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 저장 - Product 
	 * @param param
	 * @throws Exception
	 */
	public void setModelAttrProductInfo(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 저장 - Pump Type
	 * @param param
	 * @throws Exception
	 */
	public void setModelAttrPumpTypeInfo(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 저장 - Equip Type
	 * @param param
	 * @throws Exception
	 */
	public void setModelAttrEquipTypeInfo(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 저장 - Group Code Info
	 * @param param
	 * @throws Exception
	 */
	public void setModelAttrGrpCodeInfo(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 저장 - Product Group Hierarchy
	 * @param param
	 * @throws Exception
	 */
	public void setModelAttrProductHierInfo(Map<String,Object> param) throws Exception;

	
	/**
	 * 모델생성정보 가져오기
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getModelInfo(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 조회 - Nominal 정보 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getPredictNominalList(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 가져오기 - Product
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getModelGroupCodeList(Map<String,Object> param) throws Exception;
	
	/**
	 * 모델생성 추가정보 가져오기 - Product Group Hierarchy
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getModelProductGroupCodeHierList(Map<String,Object> param) throws Exception;
	
	/**
	 * 가장 최근 Model ID를 가져오기
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public String getModelId(Map<String,Object> param) throws Exception;
	
	/**
	 * CNV 테이블 조회
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getDBbyPredictFeature(Map<String,Object> param) throws Exception;
	
	
	/**
	 * Text To Number 정보
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getTransTextVal(Map<String,Object> param) throws Exception;
	
	
	/**
	 * Org To Cnv : CNV Table을 Trunc.
	 * @throws Exception
	 */
	public void removeCNV1() throws Exception;
	public void removeCNV1_1() throws Exception;
	
	public void removeCNV2() throws Exception;
	public void removeCNV2_1() throws Exception;
	
	/**
	 * Org To Cnv : Org 정보를 가져오기
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getOrgList() throws Exception;
	public List<Map<String,Object>> getOrgList(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> getOrgList_1() throws Exception;	
	/**
	 * Org To Cnv : 단위변환정보 가져오기
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getTransUnitCodeInfo() throws Exception;
	/**
	 * Org To Cnv : 단위코드정보 가져오기
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getUnitCodeRelInfo() throws Exception;
	/**
	 * Org To Cnv : Text To Number 정보 가져오기
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getTransTxtValInfo() throws Exception;
	/**
	 * Org To Cnv : SSU 변환정보
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getSsuChglInfo() throws Exception;
	/**
	 * Org To Cnv : Group Code 정보 가져오기
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getGroupingInfo(String s) throws Exception;
	/**
	 * Org To Cnv : CNV 테이블에 저장
	 * @param param
	 * @throws Exception
	 */
	public void deleteDataCnv1(Map<String,Object> param) throws Exception;
	public void insertDataCnv1(Map<String,Object> param) throws Exception;
	public void insertDataCnv1GrpInfo(Map<String,Object> param) throws Exception;
	
	public void insertDataCnv2(Map<String,Object> param) throws Exception;
	public void deleteDataCnv2_1(Map<String,Object> param) throws Exception;
	public void insertDataCnv2_1(Map<String,Object> param) throws Exception;
	
	/**
	 * Org To Cnv : Product Group Hierarchy 정보 가져오기
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getGroupingHier(Map<String,Object> param) throws Exception;
	
	
	/**
	 * 단위목록 정보 반환
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getUnitInfoList() throws Exception;
	
	/**
	 * 단위목록 정보 반환 - 기준단위
	 * @return
	 * @throws Exception
	 */
	public List<Map<String,Object>> getUnitDefaultInfoList() throws Exception;
	
	public List<Map<String, Object>> getSealTypeInfo();
	public List<Map<String, Object>> getSealTypeInfo2();
	public List<Map<String, Object>> getSealTypeInfo_new();
	
	/**
	 * Feature Range Insert to TB_ML_MODEL_FEATURE_RANGE
	 * @param param
	 * @throws Exception
	 */
	public void insertFeatureRange(Map<String,Object> param) throws Exception;
	
	// 확인필요
	public List<Map<String,Object>> getPredictTrainingList(Map<String,Object> param) throws Exception;

	//0320 TB_ML_MODEL_FEATURE_RANGE값 가져오기
	public List<Map<String, Object>> getFeatureRangeList();

	/**
	 * 운전조건 콤보
	 * @param param
	 * @return
	 * @throws Exception
	 */
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
	
	public void savHistory(Map<String, Object> param) throws Exception;
	public void savHistoryDetail(Map<String,Object> param) throws Exception;
	public void editHistoryDetail(Map<String,Object> param) throws Exception;
	
	public void deleteHistory(Map<String, Object> param) throws Exception;
	public void deleteHistorySub(Map<String, Object> param) throws Exception;
	
	public List<Map<String,Object>> getFeatureData(Map<String, Object> param) throws Exception;
	
}
