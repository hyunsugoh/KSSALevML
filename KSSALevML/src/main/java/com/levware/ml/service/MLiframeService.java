package com.levware.ml.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;


public interface MLiframeService {
	public List<Map<String,Object>> getTableList(Map<String,Object> param);
	public List<Map<String,Object>> getTableColInfo(Map<String,Object> param);
	public List<Map<String,Object>> getCSVColInfo(Map<String,Object> param);
	public String saveTableData(Map<String,Object> param);
	public String saveSQLData(Map<String,Object> param);
	public String removeModelCSV(String subPjtId, String modelUid, String modelType);
	
	public List<Map<String,Object>> getParamList(Map<String,Object> param);
	public List<String> getParamColList(Map<String,Object> param);
	
	public List<String> getCSVHeader(String subPjtId, String modelUid);
	public List<String> getModelCSVHeader(String subPjtId, String modelUid);
	public List<Map<String,Object>> getCSVData(String subPjtId, String modelUid);
	public List<Map<String,Object>> getModelCSVData(String subPjtId, String modelUid);
	public List<String> getPredictCSVHeader(String subPjtId, String modelUid);
	public List<Map<String,Object>> getPredictCSVData(String subPjtId, String modelUid);
	
	public List<Map<String,Object>> getCSVInfo(MultipartFile file);
	public List<Map<String,Object>> getCSVMeta(File file);
	public String uploadCSV(MultipartFile file, String subPjtId, String modelUid, List<Integer> list, List<String> cols, String userid);
	
	public String replaceMissingNumber(Map<String,Object> param);
	public String replaceMissingString(Map<String,Object> param);
	public String deleteMissingData(Map<String,Object> param);
	public String splitData(Map<String,Object> param);
	
	public List<Map<String,Object>> joinData(Map<String,Object> param);
	public List<Map<String,Object>> distinct(Map<String,Object> param);
	public List<Map<String,Object>> sort(Map<String,Object> param);
	public Map<String,Object> stringToColumn(Map<String,Object> param);
	public Map<String,Object> normalization(Map<String,Object> param);
	public Map<String,Object> encoder(Map<String,Object> param);
	public Map<String,Object> decoder(Map<String,Object> param);
	
	public Map<String,Object> removeAbString(Map<String,Object> param);
	public Map<String,Object> trim(Map<String,Object> param);
	public Map<String,Object> substring(Map<String,Object> param);
	public Map<String,Object> correlation(Map<String,Object> param);
	public Map<String,Object> evaluation(Map<String,Object> param);
	public Map<String,Object> concatenate(Map<String,Object> param);
	
	public Map<String,Object> convertImageToPixel(List<Map<String, String>> infoList, String subPjtId, String modelUid, String userId);
	
	public String saveImage(MultipartFile[] files, String subPjtId, String modelUid, List<String> list, String userId);
	
	public Map<String, Object> pivot(Map<String,Object> param);
	public Map<String, Object> unpivot(Map<String,Object> param);
	public Map<String, Object> capitalize(Map<String, Object> param);
	public Map<String, Object> uncapitalize(Map<String, Object> param);
}
