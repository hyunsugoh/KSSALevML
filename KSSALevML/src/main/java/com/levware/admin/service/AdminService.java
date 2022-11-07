package com.levware.admin.service;

import java.util.List;
import java.util.Map;
import com.levware.user.service.UserInfoVO;

/**
 * DB Connection Test용 Service
 *
 * @author Jin. Choi.
 * @since 2019.01.30
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *          수정일          수정자           수정내용
 *  ----------------    ------------    ---------------------------
 *   2014.01.30        Jin. Choi.          최초 생성
 *
 * </pre>
 */
public interface AdminService {

	public List<AdminObjectListVO> getAdminObjectList() throws Exception;

	/**
	 * 코드관리 select
	 * @since 2019.03.05
	 * @author 박수연
	 */
	public List<CodeManagementVO> getCodeManagementList() throws Exception;
	/**
	 * 코드관리 insert
	 * @since 2019.03.07
	 * @author 박수연
	 */
	public void insertCodeManagement(Map<String, String> param) throws Exception;
	/**
	 * 코드관리 delete
	 * @since 2019.03.07
	 * @author 박수연
	 */
	public void deleteCodeManagement(List<String> param) throws Exception;

	public int getAdminObjectDelete(String table_name) throws Exception;

	public int getAdminObjectActive(Map<String, String> param) throws Exception;
	/**
	 * 객체 관리 -> 마트에서 객체리스트  select
	 * @since 2019.03.11
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectSelect(List<String> param) throws Exception;
	/**
	 * 객체 관리 -> 마트에서 객체가져와서 repo에 저장
	 * @since 2019.03.11
	 * @author 조형욱
	 */
	public int setAdminObjectInsert (String object_name,String ID) throws Exception;
	/**
	 * repo 테이블 리스트 select
	 * @since 2019.03.14
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectTableList() throws Exception;
	/**
	 * repo 객체관리 업데이트
	 * @since 2019.03.15
	 * @author 조형욱
	 */

	public int getAdminObjectUpdate(Map<String, String> param) throws Exception;


	/**
	 * repo 객체정보관리 리스트
	 * @since 2019.03.18
	 * @author 조형욱
	 */

	public List<Map<String, Object>> getAdminObjectInfoList(String table_name) throws Exception;

	public List<Map<String, Object>> getAdminObjectRepoColList(String table_name) throws Exception;

	/**
	 * 활성화 하기 전 체크  1
	 * @since 2019.03.20
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectActiveCheck1(Map<String, String> param) throws Exception;

	/**
	 * 활성화 하기 전 체크  2
	 * @since 2019.03.20
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectActiveCheck2(Map<String, String> param) throws Exception;

	/**
	 *  객체정보관리 업데이트,인서트
	 * @since 2019.03.21
	 * @author 조형욱
	 */

	public int setAdminObjectInfoUpdate(Map<String, String> param) throws Exception;

	public int setAdminObjectInfoInsert(Map<String, String> param) throws Exception;



	/**
	 *  객체정보관리 삭제
	 * @since 2019.03.22
	 * @author 조형욱
	 */

	public int setAdminObjectInfoDelete(String TABLE_NAME,String COL_NAME) throws Exception;

	/**
	 * repo 객체정보관리 기준일자 체크
	 * @since 2019.03.26
	 * @author 조형욱
	 */

	public List<Map<String, String>> getAdminObjectInfoCheckDate(String TABLE_NAME) throws Exception;


	/**
	 * 객체 관계  관리 리스트(기준 테이블)
	 * @since 2019.03.28
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelStand() throws Exception;


	/**
	 * 객체 관계  관리 리스트(연결 테이블)
	 * @since 2019.03.28
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelConn(String TABLE_NAME) throws Exception;


	/**
	 * 객체 관계  관리 리스트(콤보)
	 * @since 2019.03.28
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelCombo(String TABLE_NAME) throws Exception;

	/**
	 * 객체 관계  관리 조인식 설정
	 * @since 2019.04.01
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelJoin(String STD_TABLE,String CONN_TABLE) throws Exception;

	/**
	 * 객체 관계  관리 조인식 자동 작성
	 * @since 2019.04.01
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelJoinStandKeyList(String STD_TABLE) throws Exception;

	public List<Map<String, Object>> getAdminObjectRelJoinConnKeyList(String CONN_TABLE) throws Exception;


	/**
	 * 객체 관계  관리 조인식 업데이트
	 * @since 2019.04.02
	 * @author 조형욱
	 */

	public int setAdminObjectRelJoinUpdate(String STD_TABLE,String CONN_TABLE,String JOIN_EXPR,String ID) throws Exception;

	/**
	 * 객체 관계  관리 조인식 인서트
	 * @since 2019.04.02
	 * @author 조형욱
	 */

	public int setAdminObjectRelJoinInsert(String STD_TABLE,String CONN_TABLE,String JOIN_EXPR,String ID) throws Exception;

	/**
	 * 객체 관계 관리 삭제
	 * @since 2019.04.02
	 * @author 조형욱
	 */
	public int setAdminObjectRelJoinDelete(String STD_TABLE,String CONN_TABLE) throws Exception;

	/**
	 * repo 객체정보관리 인서트 업데이트 구분
	 * @since 2019.04.04
	 * @author 조형욱
	 */

	public List<Map<String, Object>> getAdminObjectInfoGubunInsertUpdate(String TABLE_NAME,String COL_NAME) throws Exception;

	/**
	 * 활성화 하기 전 체크  3
	 * @since 2019.04.08
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectActiveCheck3(Map<String, String> param) throws Exception;

	/**
	 * 객체정보별 조회 조건 관리 조회
	 * @author 강전일
	 * @since 2019.4.01
	 */
	public List<UserInfoVO> getInfoCriteria(String tableName) throws Exception;

	/**
	 * 관리자 가입 아이디 중복 조회
	 * @since 2019.04.11
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getManagerIdCheck(String insertId) throws Exception;

	/**
	 * 관리자 가입 데이터 인서트
	 * @since 2019.04.11
	 * @author 조형욱
	 */
	public int setSignUpManager(Map<String, String> param) throws Exception;



	/**
	 * 조회 조건 조회
	 * @author 강전일
	 * @since 2019.4.03
	 */
	public List<Map<String,Object>> getInfoCondition() throws Exception;

	/**
	 * 조회조건 동적 체크
	 * @author 강전일
	 * @since 2019.04.04
	 */
	public List<Map<String,Object>> getSearchChk(Map<String, Object> param) throws Exception;

	/**
	 * 조회 조건 저장
	 * @since 2019.04.04
	 * @author 강전일
	 */
	public void updateCondifion(List<Map<String,Object>> chkArr, List<Map<String,Object>> unChkArr, Map<String, Object> conditionJson) throws Exception;


	/**
	 * 회원목록 조회
	 * @author 강전일
	 * @since 2019.3.27
	 */
	public List<UserInfoVO> getUserInfoList(String userId) throws Exception;
	

	/**
	 * 회원목록 삭제
	 * @since 2019.03.28
	 * @author 강전일
	 */
	public void deleteUserList(List<String> param) throws Exception;

	
	/**
	 * 사용자 정보 저장 (자료실 권한정보 등)
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String,Object> setUserData(Map<String,Object> param) throws Exception;
	
	/**
	 * 관리자 리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getManagerList() throws Exception;

	/**
	 * 관리자 아이디 삭제
	 * @since 2019.04.15
	 * @author 조형욱
	 */
	public int setManagerDelete(String MANAGER_ID) throws Exception;

	/**
	 * 관리자 ip리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getManagerIpList(String MANAGER_ID) throws Exception;

	/**
	 * ip insert Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */

	public int setManagerIpInsert(Map<String, Object> param) throws Exception;

	/**
	 * ip Delete Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	public int setManagerIpDelete(Map<String, Object> param) throws Exception;

	/**
	 * ip update Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	public int setManagerIpUpdate(Map<String, Object> param) throws Exception;

	/**
	 * 관리자 비번 가져오기
	 * @since 2019.04.18
	 * @author 조형욱
	 */
	public Map<String, Object> getAdminPwd(String managerId) throws Exception;

	/**
	 * 관리자 비밀번호 변경
	 * @since 2019.04.18
	 * @author 조형욱
	 */
	public String ActionAdminPwdChange(Map<String, String> param) throws Exception;


	/**
	 * 관리자 활성화 업데이트
	 * @since 2019.04.25
	 * @author 조형욱
	 */
	public int setManagerEnabledUpdate(Map<String, Object> param) throws Exception;
	/**
	 * 회원저장목록 삭제
	 * @since 2019.04.29
	 * @author 조형욱
	 */
	public void deleteUserSaveList(List<String> param) throws Exception;
	/**
	 * 회원저장목록(관리자) 삭제
	 * @since 2019.04.29
	 * @author 조형욱
	 */
	public void setManagerSaveDelete(String MANAGER_ID) throws Exception;

	/**
	 * 테이블 코드 정보 가져오기
	 * @param flag
	 * @return
	 * @since 2019.11.05
	 * @author 최진
	 */
	public Map<String, Object> getAdminTbCode(String flag) throws Exception;

	/**
	 * 테이블 코드 Feature insert
	 * @param param
	 * @return
	 * @since 2019.11.05
	 * @author 최진
	 * @throws Exception
	 */
	public void insertAdminTbFeaCode(Map<String, Object> param) throws Exception;

	/**
	 * 테이블 코드 FEA_GRUP_INFO insert
	 * @since 2019.11.05
	 * @author 최진
	 * @param param
	 * @throws Exception
	 */
	public void insertAdminTbDetailValCode(Map<String, Object> param) throws Exception;

	/**
	  * 테이블 코드 Feature update
	 * @since 2019.11.05
	 * @author 최진
	 * @param param
	 * @throws Exception
	 */
	public void updateAdminTbFeaCode(Map<String, Object> param) throws Exception;

	/**
	 * 테이블 코드 FEA_GRUP_INFO update
	 * @param param
	 * @throws Exception
	 */
	public void updateAdminTbDetailValCode(Map<String, Object> param) throws Exception;

	/**
	 *  테이블 코드 Feature delete
	 * @param param
	 * @throws Exception
	 */
	public void deleteAdminTbFeaCode(Map<String, Object> param) throws Exception;

	/**
	 *  테이블 코드 EA_GRUP_INFO delete
	 * @param param
	 * @throws Exception
	 */
	public void deleteAdminTbDetailValCode(Map<String, Object> param) throws Exception;


	/**
	  * 테이블 코드 TB_FEA_HIERA  update
	 * @since 2019.11.28
	 * @author
	 * @param param
	 * @throws Exception
	 */
	public void adminMLGroupInfoUpdate(Map<String, Object> param) throws Exception;


	/**
	  * 테이블 코드 TB_FEA_HIERA  delete
	 * @since 2019.11.28
	 * @author
	 * @param param
	 * @throws Exception
	 */
	public void adminMLGroupInfoDelete(Map<String, Object> param) throws Exception;

	/**
	  * 테이블 코드 TB_FEA_HIERA  insert
	 * @since 2019.11.28
	 * @author
	 * @param param
	 * @throws Exception
	 */
	public void adminMLGroupInfoInsert(Map<String, Object> param) throws Exception;


	public String downMLGroupInfo(String flag, String codeNm) throws Exception;

	//public void excelUploadProcess(String fileInfo) throws Exception;
	public List<Map<String,Object>> excelUploadProcess(String fileInfo,String userId, String flag) throws Exception;
	public List<Map<String,Object>> excelUploadProcessConf(String fileInfo, String selCode, String userId) throws Exception;
	public void savInfoProcess(List<Map<String, Object>> excelGridParam, String selCode) throws Exception;


	public String downHierarchyInfo(Map<String,Object> param,List<String> HierDataInfo) throws Exception;

	/**
	 * 공통코드 조회
	 * @since 2020.11.04
	 * @author 강전일
	 */
	public List<Map<String, Object>> getRuleList(Map<String, Object> param) throws Exception;
	public int getRuleCount(Map<String, Object> params) throws Exception;
	
	/**
	 * Rule Base 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */	
	public void ruleListSave(List<Map<String, Object>> params) throws Exception;
	
	/**
	 * Rule Base Pop 조회
	 * @since 2020.11.06
	 * @author 강전일
	 */	
	public List<Map<String, Object>> getPopRuleList(Map<String, Object> param) throws Exception;
	
	/**
	 * Rule Base Pop 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */	
	public void ruleListPopSave(List<Map<String, Object>> params) throws Exception;
	
	
	public Map<String,Object> getUserInfo(String userId) throws Exception;
	public Map<String,Object> getManagerInfo(String userId) throws Exception;
	
	/**
	 * Rule 표준정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	public List<Map<String, Object>> getMgList(Map<String, Object> param) throws Exception;
	public List<Map<String, Object>> getSowList(Map<String, Object> param) throws Exception;
	public List<Map<String, Object>> getStList(Map<String, Object> param) throws Exception;
	
	/**
	 * 가격정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	public List<Map<String, Object>> getPriceInfoList() throws Exception;
	public List<Map<String, Object>> getGrpList() throws Exception;
	public List<Map<String, Object>> getSealList(Map<String,Object> param) throws Exception;
	public List<Map<String, Object>> getSheetList(Map<String,Object> param) throws Exception;
	
	/**
	 * 가격정보 저장
	 * @since 2021.02.25
	 * @author 강전일
	 */
	public void savPriceInfo(Map<String,Object> param) throws Exception;
	public void editPriceInfo(Map<String,Object> param) throws Exception;
	public void noFile(Map<String,Object> param) throws Exception;
	public void delPriceInfo(Map<String,Object> param) throws Exception;
	
	public void savGrp(Map<String,Object> param) throws Exception;
	public void savSeal(Map<String,Object> param) throws Exception;
	public void savSheet(Map<String,Object> param) throws Exception;
	
	public void editGrp(Map<String,Object> param) throws Exception;
	public void editSheet(Map<String,Object> param) throws Exception;
	
	public void delGrp(Map<String,Object> param) throws Exception;
	public void delSeal(Map<String,Object> param) throws Exception;
	public void delSheet(Map<String,Object> param) throws Exception;
	
	/**
	 * seal type list 조회
	 * @since 2021.03.11
	 * @author 강전일
	 */
	public List<Map<String, Object>> getSealTypeList(Map<String,Object> param) throws Exception;
}

