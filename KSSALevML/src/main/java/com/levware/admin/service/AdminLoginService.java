package com.levware.admin.service;

import java.util.List;





/**
 *  관리자 정보 처리
 * @author 박수연
 * @since 2019. 04.03
 */

public interface AdminLoginService {
	
	/**
	 *  아이디 검색으로 관리자 IP 조회
	 * @author 박수연
	 * @since 2019.3.13
	 */
	public List<AdminInfoVO> getManagerIp(String managerId) throws Exception;
	

}
