package com.levware.rb.service;

import java.util.List;
import java.util.Map;


public interface RBService {

	public Map<String,Object> predictSealByRuleBased(Map<String,Object> param) throws Exception;
	
	public String getGroupingStr(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getGroupingInfo(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getPumbTypeList() throws Exception;
	public List<Map<String,Object>> getRbGraphSelList(Map<String,Object> param) throws Exception;
	public Map<String,Object> getRbGraphResult(Map<String,Object> param) throws Exception;
	public Map<String,Object> getunknownApi(Map<String,Object> param) throws Exception;
	
	
	
	public void test(Map<String,Object> param) throws Exception;
	public void test1(Map<String,Object> param) throws Exception;
	public void test2(Map<String,Object> param) throws Exception;

	public List<Map<String, Object>> getASList(Map<String, Object> params) throws Exception;
	
}
