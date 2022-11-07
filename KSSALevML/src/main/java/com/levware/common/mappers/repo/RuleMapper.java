package com.levware.common.mappers.repo;
import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("RuleMapper")
public interface RuleMapper {

	/**
	 * 공통코드 조회
	 * @since 2020.11.04
	 * @author 강전일
	 */
	public List<Map<String, Object>> getRuleList(Map<String, Object> param) throws Exception;

	public int getRuleCount(Map<String, Object> params) throws Exception;
	
	/**
	 * rule base 규칙 저장
	 * @since 2020.11.04
	 * @author 강전일
	 */
	public void ruleListSave(Map<String, Object> params) throws Exception;

	public void ruleListDelete(Map<String, Object> params) throws Exception;
	
	public List<Map<String, Object>> getPopRuleList(Map<String, Object> param) throws Exception;
	
	/**
	 * rule base pop 규칙 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */
	public void ruleListPopSave(Map<String, Object> params) throws Exception;

	public void ruleListPopDelete(Map<String, Object> params) throws Exception;
	
	/**
	 * Rule 표준정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	public List<Map<String, Object>> callMgList(Map<String, Object> param) throws Exception;
	public List<Map<String, Object>> callSowList(Map<String, Object> param) throws Exception;
	public List<Map<String, Object>> callStList(Map<String, Object> param) throws Exception;
}
