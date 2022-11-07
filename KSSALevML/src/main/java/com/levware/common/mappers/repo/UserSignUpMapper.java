package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.levware.user.service.UserInfoVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 * 
 * Oracle Repository DataSource Mapper Class
 * 회원가입
 * @author 강전일
 * @since 2019. 3. 15.
 */
@Mapper("userSignUpMapper")
public interface UserSignUpMapper {


	/**
	 * 회원가입 아이디 조회
	 * @author 강전일
	 * @since 2019.3.15
	 */
	public  List<UserInfoVO> getUserIdList(@Param("userId") String userId) throws Exception;

	/**
	 * 회원가입 보조아이디 조회
	 * @author 강전일
	 * @since 2019.3.15
	 */
	public  List<UserInfoVO> getSubIdtList(@Param("subId") String subId) throws Exception;

	/**
	 * 회원가입 데이터 인서트
	 * @author 강전일
	 * @since 2019.3.19
	 */
	//public  void signUpInsert(@Param("encodePwd") String encodePwd, @Param("user_id") String user_id, @Param("sub_id") String sub_id, @Param("auth_hint") String auth_hint, @Param("auth_hint_ans") String auth_hint_ans) throws Exception; 
	public  void signUpInsert(@Param("encodePwd") String encodePwd, @Param("user_id") String user_id, @Param("user_nm") String user_nm, @Param("sub_id") String sub_id) throws Exception;
	/**
	 * 회목목록 조회 
	 * @author 강전일
	 * @since 2019.3.27
	 */
	public List<UserInfoVO> getUserInfoList(@Param("userId") String userId) throws Exception;

	public Map<String,Object> getUserInfo(@Param("userId") String userId) throws Exception;
	
	/**
	 * 회원 목록 삭제
	 * @since 2019.03.28
	 * @author 강전일
	 */
	public void deleteUserList(List<String> param) throws Exception;
	
	
	/**
	 * 회원저장목록 삭제
	 * @since 2019.04.29
	 * @
	 */
	public void deleteUserSaveList(List<String> param) throws Exception;
	
	/**
	 * 사용자 자료실권한정보 저장
	 * @param param
	 * @throws Exception
	 */
	public void setUserData(Map<String,Object> param) throws Exception;
	

}
