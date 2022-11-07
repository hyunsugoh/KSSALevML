package com.levware.rule.service;

import java.util.List;
import java.util.Map;

public interface RuleService {
	/**
	 * 공통코드 조회
	 * @since 2020.11.04
	 * @author 강전일
	 */
	public List<Map<String, Object>> getRuleList(Map<String, Object> param) throws Exception;
	public int getRuleCount(Map<String, Object> params) throws Exception;
	
	/**
	 * Rule Base 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */	
	public void ruleListSave(List<Map<String, Object>> params) throws Exception;
	
	/**
	 * Rule Base Pop 조회
	 * @since 2020.11.06
	 * @author 강전일
	 */	
	public List<Map<String, Object>> getPopRuleList(Map<String, Object> param) throws Exception;
	
	/**
	 * Rule Base Pop 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */	
	public void ruleListPopSave(List<Map<String, Object>> params) throws Exception;
	
	/**
	 * Rule 표준정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	public List<Map<String, Object>> callMgList(Map<String, Object> param) throws Exception;
	public List<Map<String, Object>> callSowList(Map<String, Object> param) throws Exception;
	public List<Map<String, Object>> callStList(Map<String, Object> param) throws Exception;
}
