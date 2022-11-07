package com.levware.common.mappers.repo;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.levware.admin.service.AdminInfoVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 * Oracle Repository DataSource Mapper Class
 * 관리자 정보 처리  
 * @author 박수연
 * @since 2019. 04.03
 */
@Mapper("adminLoginMapper")
public interface AdminLoginMapper {


	/**
	 *  아이디 검색으로 관리자 IP 조회
	 * @author 박수연
	 * @since 2019.04.03
	 */
	public  List<AdminInfoVO> getManagerIp(@Param("managerId") String userId) throws Exception;

}
