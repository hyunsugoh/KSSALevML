package com.levware.common.mappers.mart;


import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("adminObjectSelectMapper")
public interface AdminObjectSelectMapper {
	/**
	 * 마트의 테이블 뷰 목록 가져오기
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAdminObjectSelect(List<String> param) throws Exception;

	
	public List<Map<String, Object>> getAdminObjectRepoColList(@Param("table_name") String table_name) throws Exception;
	              
	
}
