package com.levware.common.mappers.mart;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.levware.user.service.OlapSelectObjectVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("martUserMapper")
public interface MartUserMapper {
	
	/**
	 * 동적 쿼리 select 
	 * @param params
	 * @return
	 */
	List<Map<String, Object>> getSelectGridData(OlapSelectObjectVO params);

	List<Map<String, Object>> getUnitDyValue(@Param("tbName")  String tbName, @Param("uCode") String uCode, @Param("targetColName") String targetColName, @Param("value") String value);
	
	List<Map<String, Object>> getGroupData(@Param("grpCode")  String grpCode, @Param("detailValue") String detailValue);

}
