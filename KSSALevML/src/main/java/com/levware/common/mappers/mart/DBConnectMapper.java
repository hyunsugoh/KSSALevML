package com.levware.common.mappers.mart;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.levware.user.service.PriceVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("dBConnectMapper")
public interface DBConnectMapper {
	/**
	 * 오라클 접속 테스트용 count 값
	 * @return
	 * @throws Exception
	 */
	public int getOracleTableRecordCnt() throws Exception;

	/**
	 * Price Data 가져오기
	 * @param endPage 
	 * @param startPage 
	 * @return PriceVO
	 * @throws Excpetion
	 */
	public List<PriceVO> getPriceData(@Param("startPage") int startPage, @Param("endPage") int endPage) throws Exception;
	


	
}
