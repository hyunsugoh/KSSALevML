package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("mLiframeMapper")
public interface MLiframeMapper {
	public List<Map<String, Object>> getTableList(Map<String,Object> param);
	public List<Map<String, Object>> getTableColInfo(Map<String,Object> param);
	public List<Map<String, Object>> getTableData(Map<String,Object> param);
	public List<Map<String, Object>> getSQLData(Map<String,Object> param);
	public void deleteControlParam(Map<String,Object> param);
	public void insertControlParam(Map<String,Object> param);
	public void deleteControlColParam(Map<String,Object> param);
	public void insertControlColParam(Map<String,Object> param);
	public List<String> getParamColList(Map<String,Object> param);
	public List<Map<String, Object>> getParamList(Map<String,Object> param);
	
	public void insertLeftData(Map<String,Object> param);
	public void deleteLeftData(Map<String,Object> param);
	public void insertRightData(Map<String,Object> param);
	public void deleteRightData(Map<String,Object> param);
	public List<Map<String, Object>> getJoinData(Map<String,Object> param);
	
	public void insertData(Map<String,Object> param);
	public void deleteData(Map<String,Object> param);
	public List<Map<String, Object>> getDistinctData(Map<String,Object> param);
	public List<Map<String, Object>> getSortData(Map<String,Object> param);
	
	public Integer getEncodedLabel(Map<String,Object> param);
	public Integer getNextEncodeValue(Map<String,Object> param);
	public void insertEncodedLabel(Map<String,Object> param);
	
	public void insertDummyData(Map<String,Object> param);
	public void mergeDictionaryData(Map<String,Object> param);
	public void deleteDummyData();
	public List<String> getEncodedValueList(Map<String,Object> param);
	public List<String> getDecodedValueList(Map<String,Object> param);
	
	public void insertImageEncodingData(Map<String, Object> param);
	public String selectImageEncodingData(Map<String, Object> param);
	
	public void insertPivotData(List<Map<String, Object>> param);
	public List<Map<String, Object>> selectPivotData(Map<String, Object> param);
}
