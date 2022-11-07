package com.levware.common.mappers.repo;

import org.apache.ibatis.annotations.Param;


import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 * 
 * 
 * 유저 아이디 찾기 
 * @author 조형욱
 * @since 2019. 04. 09.
 */
@Mapper("userFindIdMapper")
public interface UserFindIdMapper {
	

	/**
	 *  아이디 검색으로 비밀번호 힌트,번호 조회
	 * @author 박수연
	 * @since 2019.3.19
	 */
	public  String getUserFindId(@Param("insertSubId") String insertSubId) throws Exception;


}