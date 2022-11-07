package com.levware.project.service;

import java.util.List;
import java.util.Map;

public interface ProjectService {

	
	public List<Map<String, Object>> getProjectList(Map<String, Object> params) throws Exception;
	
	public String insertProject(Map<String, Object> params) throws Exception;

	public Map<String, Object> selectProject(Map<String, Object> params) throws Exception;
	
	public List<Map<String, Object>> subProjectList(Map<String, Object> params) throws Exception;
	
	public String insertSubProject(Map<String, Object> params) throws Exception;

	public String updateSubProject(Map<String, Object> params) throws Exception;
	
	public String deleteSubProject(Map<String, Object> params) throws Exception;
	
	public String copySubProject(Map<String, Object> params) throws Exception;
	
	public Map<String, Object> getSubProjectInfo(Map<String, Object> params) throws Exception;
	
	public List<Map<String, Object>> getSubProjectContents(Map<String, Object> params) throws Exception;
	
	public String updateSubContents(Map<String, Object> params) throws Exception;
	
	public void csvAction(Map<String, Object> params) throws Exception;
	
	public Map<String, Object> csvLoadAction(Map<String, Object> params) throws Exception;
	
	public void insertToModelInfo(Map<String, Object> params) throws Exception;
	
}
