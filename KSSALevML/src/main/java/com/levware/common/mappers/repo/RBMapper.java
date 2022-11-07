package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;


@Mapper("rBMapper")
public interface RBMapper {
	
	public List<Map<String,Object>> selectRuleComListType1(Map<String,Object> param) throws Exception; // Rule 기준조회 공통
	
	public void insertRuleRst(Map<String,Object> param) throws Exception; // 씰예측결과 
	public void insertRuleRstNote(Map<String,Object> param) throws Exception; // 씰예측결과 노트
	public void insertRuleRstProc(Map<String,Object> param) throws Exception; // 씰예측 주요 과정 정보
	
	
	

	public List<Map<String,Object>> selectMaterialList(Map<String,Object> param) throws Exception; //재질정보 리스트
	
	
	// ------------------------------------------------
	//Rule 기준 별도 조회
	// ------------------------------------------------
	public List<Map<String,Object>> selectRuleComListB11301(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB1901(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB1501(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB301(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB401(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB403(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB1801(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB1101(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB11101(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB11401(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListC7801(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListC7802(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListB801(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListA401(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListA501(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListA601(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListA701(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListA801(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListC101(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListC601(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListC602(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleComListE002(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> selectRuleC3(Map<String,Object> param) throws Exception; // Rule [C3] 기준 조회 
	public List<Map<String,Object>> selectRuleC3_bySealType(Map<String,Object> param) throws Exception; // Rule [C3] 기준 (Seal Type에 의한)조회
	
	public List<Map<String,Object>> selectRuleC9(Map<String,Object> param) throws Exception; // Rule [C9] 기준 조회
	public List<Map<String,Object>> selectRuleC9_1(Map<String,Object> param) throws Exception; // Rule [C9] 기준 조회
	
	public List<Map<String,Object>> selectRuleC1(Map<String,Object> param) throws Exception; // Rule [C1] 기준 조회
	public List<Map<String,Object>> selectRuleC1ChkContUse(Map<String,Object> param) throws Exception; // Rule [C1] 농도값 유무
	public List<Map<String,Object>> selectRuleC1ChkIsData(Map<String,Object> param) throws Exception; // Rule [C1] 데이터 유무
	public List<Map<String,Object>> selectRuleC1_temp_cond(Map<String,Object> param) throws Exception; // Rule [C1] 기준 조회 - 온도조건없이
	public List<Map<String,Object>> selectRuleC1_temp_cond_min(Map<String,Object> param) throws Exception; // Rule [C1] 기준 조회 - 온도조건없이
	public List<Map<String,Object>> selectRuleC1_temp_cond_max(Map<String,Object> param) throws Exception; // Rule [C1] 기준 조회 - 온도조건없이
	
	public List<Map<String,Object>> selectRuleGraph(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> selectRuleGraphBySimul(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> selectRuleGraphFunc(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> selectSealTypeTInfo(String sType) throws Exception;
	
	public List<Map<String,Object>> getRbGraphSelList(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getRbGraphCurvNoSelList(Map<String,Object> param) throws Exception;
	
	public List<Map<String,Object>> getRbSealSizeMain(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getRbSealSizeSub(Map<String,Object> param) throws Exception;
	
	
	// Test
	public void setTest1Save(Map<String,Object> param) throws Exception;
	public void setTest2Save(Map<String,Object> param) throws Exception;

	public List<Map<String, Object>> getASHistoryList(Map<String, Object> paramSet) throws Exception;
	
}
