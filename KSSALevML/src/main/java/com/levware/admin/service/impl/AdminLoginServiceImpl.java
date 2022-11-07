package com.levware.admin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.levware.admin.service.AdminInfoVO;
import com.levware.admin.service.AdminLoginService;
import com.levware.common.mappers.repo.AdminLoginMapper;
/**
 *  관리자 정보 처리
 * @author 박수연
 * @since 2019. 04.03
 */
@Service("AdminLoginService")
public class AdminLoginServiceImpl implements AdminLoginService {

	private static final Logger LOGGER = LogManager.getLogger(AdminLoginServiceImpl.class);

	@Resource(name = "adminLoginMapper")
	private AdminLoginMapper adminLoginMapper;

	/**
	 *  아이디 검색으로 관리자 IP 조회
	 * @author 박수연
	 * @since 2019. 04.03
	 */
	@Override
	public List<AdminInfoVO> getManagerIp(String managerId) throws Exception {
		List<AdminInfoVO> objectList = adminLoginMapper.getManagerIp(managerId);
		LOGGER.debug(objectList.toString());
		return objectList;
	}

}
