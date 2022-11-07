package com.levware.user.service;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.User;

public interface UserDataAPIService {

	public List<OlapObjectVO> getObjectList() throws Exception;


	public Map<String, Object> getObjectRelationInfo(String stdTableName) throws Exception;


	public List<OlapObjectDetailInfoVO> getObjectDetailInfo(String tableName) throws Exception;

	public Map<String, Object> getConditionData(String[] tbNames) throws Exception;

	/**
	 * @author Jeonil Kang.
	 * @since 2019.03.06
	 *          수정일                             수정자                                  수정내용
	 *  ----------------    ------------    ---------------------------
	 *   2019.03.06         Jeonil Kang.          로그기록 인서트
	 */
	public void logHisInsert(String userId) throws Exception;


	/**
	 * 동적 쿼리 select >> grid
	 * @author Jin. Choi.
	 * @param params
	 * @return
	 */
	public Map<String, Object> getSelectGridData(User user, OlapSelectObjectVO params) throws Exception;


	/**
	 * 유저가 설정한 정보 insert/update
	 * @param params
	 * @throws Exception
	 */
	public void insertNUpdateUserDataset(Map<String, Object> params) throws Exception;


	public List<OlapSavedDataVO> getSelectUserSavedDataSetList(User user) throws Exception;


	public void deleteUserDataset(User user, int seqNum) throws Exception;

	/**
	 * 선택한 조건의 셀렉트 리스트 호출
	 * @param tableName 테이블 명 ,columnName 컬럼 명
	 * @return 
	 */

	public List<Map<String, Object>> getSelectList(String tableName,String columnName) throws Exception;


	public List<Map<String, Object>>  getUnitList() throws Exception;


	public List<Map<String, Object>> getSelectASHistoryGridData(String params) throws Exception;


}
