package com.levware.rule.impl;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.levware.common.mappers.repo.RuleMapper;
import com.levware.rule.service.RuleService;

@Service("RuleService")
public class RuleServiceimpl implements RuleService {

	@Resource(name = "RuleMapper")
	private RuleMapper ruleMapper;
	
	/**
	 * 공통코드 조회
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getRuleList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> comCodeList = ruleMapper.getRuleList(param);
		return comCodeList;
	}

	@Override
	public int getRuleCount(Map<String, Object> params) throws Exception {
		return ruleMapper.getRuleCount(params);
	}

	/**
	 * rule base 규칙 저장
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@Override
	public void ruleListSave(List<Map<String, Object>> params)throws Exception{
		for(Map<String, Object> aMap : params) {
			if(aMap.containsKey("DEL_CHK") && aMap.get("DEL_CHK").equals("Y")) {
				ruleMapper.ruleListDelete(aMap);
			}else {
				System.out.println("saveMap");
				ruleMapper.ruleListSave(aMap);
			}
		}
	}
	
	/**
	 * rule base pop 조회
	 * @since 2020.11.06
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getPopRuleList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> comCodeList = ruleMapper.getPopRuleList(param);
		return comCodeList;
	}
	
	/**
	 * rule base pop 규칙 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */
	@Override
	public void ruleListPopSave(List<Map<String, Object>> params)throws Exception{
		for(Map<String, Object> aMap : params) {
			if(aMap.containsKey("DEL_CHK") && aMap.get("DEL_CHK").equals("Y")) {
				ruleMapper.ruleListPopDelete(aMap);
			}else {
				ruleMapper.ruleListPopSave(aMap);
			}
		}
	}
	
	/**
	 * Rule 표준정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> callMgList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> mgList = ruleMapper.callMgList(param);
		return mgList;
	}
	@Override
	public List<Map<String, Object>> callSowList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> sowList = ruleMapper.callSowList(param);
		return sowList;
	}
	@Override
	public List<Map<String, Object>> callStList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> stList = ruleMapper.callStList(param);
		return stList;
	}
}
