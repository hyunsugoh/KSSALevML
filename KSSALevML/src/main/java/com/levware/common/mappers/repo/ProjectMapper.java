package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("ProjectMapper")
public interface ProjectMapper {

	public List<Map<String,Object>> getProjectList(Map<String, Object> setData) throws Exception ;
	
	public void insertProject(Map<String, Object> setData) throws Exception;
	
	public Map<String,Object> selectProject(Map<String, Object> setData) throws Exception ;
	
	public List<Map<String,Object>> subProjectList(Map<String, Object> setData) throws Exception ;
	
	public void insertSubProject(Map<String, Object> setData) throws Exception;
	public void updateSubProject(Map<String, Object> setData) throws Exception;
	public void deleteSubProject(Map<String, Object> setData) throws Exception;
	public void copySubProject(Map<String, Object> setData) throws Exception;
	public void copyControlParam(Map<String, Object> setData) throws Exception;
	public void copyControlColParam(Map<String, Object> setData) throws Exception;
	
	public Map<String,Object> getSubProjectInfo(Map<String, Object> setData) throws Exception ;
	public List<Map<String,Object>> getSubProjectContents(Map<String, Object> setData) throws Exception ;
	public void updateSubContents(Map<String, Object> setData) throws Exception;
	public void insertToModelInfo(Map<String, Object> setData) throws Exception;
}
