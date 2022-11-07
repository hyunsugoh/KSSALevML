package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.levware.user.service.UserInfoVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("infoCritMapper")
public interface AdminInfoCriticaMapper {

	/**
	 * 객체정보별 조회 조건 관리 조회 
	 * @author 강전일
	 * @since 2019.4.01
	 */
	public List<UserInfoVO> getInfoCriteria(@Param("tableName") String tableName) throws Exception;

	/**
	 * 조회 조건 조회 
	 * @author 강전일
	 * @since 2019.4.03
	 */
	public List<Map<String,Object>> getInfoCondition() throws Exception;
	
	
	/**
	 * 조회 조건 여부 업데이트
	 * @since 2019.04.04
	 * @author 강전일
	 */
	public List<Map<String,Object>> getSearchChk(Map<String, Object> param) throws Exception;
	
	/**
	 * 조회 조건 저장 체크된 데이터
	 * @since 2019.04.04
	 * @author 강전일
	 */
	public void updateConChk(List<Map<String,Object>> chkArr) throws Exception;
	
	/**
	 * 조회 조건 저장 체크안된 데이터
	 * @since 2019.04.04
	 * @author 강전일
	 */
	public void deleteConUnChk(List<Map<String,Object>> unChkArr) throws Exception;
	
	/**
	 * 조회 조건 여부 업데이트
	 * @since 2019.04.04
	 * @author 강전일
	 */
	public void updateConYn(Map<String,Object> conditionJson) throws Exception;
}
