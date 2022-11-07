package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("mlDeployMapper")
public interface MLDeployMapper {
	public void deleteDeployM(Map<String,Object> param);
	public void deleteDeployD(Map<String,Object> param);
	public void insertDeployM(Map<String,Object> param);
	public void insertDeployD(Map<String,Object> param);
	public void insertDeployLog(Map<String,Object> param);
	
	public void updateDeployItemDetail(Map<String,Object> param);
	public List<Map<String,Object>> getDeployLog(Map<String,Object> param);
	
	public Map<String,Object> getDeployInfo(Map<String,Object> param);
	public String getModelId(Map<String,Object> param);
	public String getAPIUrl(Map<String,Object> param);
	public List<String> getFeatures(Map<String,Object> param);
	public String getLabel(Map<String,Object> param);
	
	public String getAPIKey(Map<String,Object> param);
	public List<Map<String,Object>> getDeployList(Map<String,Object> param);
	public List<Map<String,Object>> getDeployListDetail(Map<String,Object> param);
	/*
	public List<String> getEncodedValueList(Map<String,Object> param);
	public List<String> getDecodedValueList(Map<String,Object> param);
	*/
	
	/**
	 * 2021.01.08. 이소라 추가
	 * - Deplo Test 시 인코딩 해야 할 컬럼 select
	 *   (Trainig 시 인코딩 된 컬럼 리스트)
	 */
	public List<String> getEncodedColumns(Map<String, Object> param);
	
	/**
	 * 2021.01.08. 이소라 추가
	 * - 인코더의 model uid select
	 */
	public String getEncoderModelId(Map<String, Object> param);
}
