package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.levware.admin.service.AdminObjectListVO;
import com.levware.admin.service.CodeManagementVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 *
 * Oracle Repository DataSource Mapper Class
 * @author 최진
 * @since 2019. 2. 13.
 */
@Mapper("adminMapper")
public interface AdminMapper {

	/**
	 *
	 * 오라클 접속 테스트용 테이블 리스트 count 값
	 * @return
	 * @throws Exception
	 */
	public List<AdminObjectListVO> getAdminObjectList() throws Exception;

	/**
	 * 코드관리 select
	 * @author 박수연
	 * @since 2019.3.06
	 */
	public List<CodeManagementVO> getCodeManagementList() throws Exception;
	/**
	 * 코드관리 insert
	 * @author 박수연
	 * @since 2019.3.07
	 */
	public void insertCodeManagement(CodeManagementVO param) throws Exception;
	/**
	 * 코드관리 delete
	 * @author 박수연
	 * @since 2019.3.08
	 */
	public void deleteCodeManagement(List<String> param) throws Exception;

	public int setAdminObjectDelete(@Param("table_name") String table_name	) throws Exception;

	public int setAdminObjectActive(@Param("table_name") String table_name,@Param("activ_yn") String activ_yn,@Param("ID") String ID) throws Exception;

	public int setAdminObjectInsert(@Param("object_name") String object_name,@Param("ID") String ID) throws Exception;

	public List<Map<String, Object>> getAdminObjectTableList() throws Exception;

	public int setAdminObjectUpdate(@Param("table_name") String table_name,@Param("obj_name") String obj_name,@Param("obj_desc") String obj_desc,@Param("ID") String ID) throws Exception;

	public List<Map<String, Object>> getAdminObjectInfoList(@Param("table_name") String table_name) throws Exception;


	/**
	 * 활성화 하기 전 체크  1
	 * @since 2019.03.20
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectActiveCheck1(@Param("table_name") String table_name) throws Exception;

	/**
	 * 활성화 하기 전 체크  2
	 * @since 2019.03.20
	 * @author 조형욱
	 */

	public List<Map<String, Object>> getAdminObjectActiveCheck2(@Param("table_name") String table_name) throws Exception;


	/**
	 * repo 객체정보관리 업데이트,인서트
	 * @since 2019.03.21
	 * @author 조형욱
	 */
	public int setAdminObjectInfoUpdate(@Param("OBJINFO_NAME") String OBJINFO_NAME
			,@Param("OBJINFO_DESC") String OBJINFO_DESC,@Param("DATA_TYPE") String DATA_TYPE,@Param("PK_GUBUN") String PK_GUBUN,@Param("STAND_DATE") String STAND_DATE
			,@Param("TABLE_NAME") String TABLE_NAME,@Param("COL_NAME") String COL_NAME,@Param("ID") String ID,@Param("VIEW_DURATION_UNIT") String VIEW_DURATION_UNIT ,@Param("VIEW_DURATION_NUM") String VIEW_DURATION_NUM ,@Param("SEQ") String SEQ ,@Param("DISPLAY_YN") String DISPLAY_YN
			,@Param("CALC_FUNC_YN") String CALC_FUNC_YN) throws Exception;

	public int setAdminObjectInfoInsert(@Param("TABLE_NAME") String TABLE_NAME,@Param("COLUMN_NAME") String COLUMN_NAME,@Param("OBJINFO_NAME") String OBJINFO_NAME
			,@Param("OBJINFO_DESC") String OBJINFO_DESC,@Param("DATA_TYPE") String DATA_TYPE,@Param("PK_GUBUN") String PK_GUBUN,@Param("STAND_DATE") String STAND_DATE,@Param("ID") String ID
			,@Param("VIEW_DURATION_UNIT") String VIEW_DURATION_UNIT ,@Param("VIEW_DURATION_NUM") String VIEW_DURATION_NUM ,@Param("SEQ") String SEQ ,@Param("DISPLAY_YN") String DISPLAY_YN ,@Param("CALC_FUNC_YN") String CALC_FUNC_YN
			) throws Exception;
	/**
	 *  객체정보관리 삭제
	 * @since 2019.03.22
	 * @author 조형욱
	 */


	public int setAdminObjectInfoDelete(@Param("TABLE_NAME") String TABLE_NAME,@Param("COL_NAME") String COL_NAME) throws Exception;

	/**
	 * repo 객체정보관리 기준일자 체크
	 * @since 2019.03.26
	 * @author 조형욱
	 */
	public List<Map<String, String>> getAdminObjectInfoCheckDate(@Param("TABLE_NAME") String TABLE_NAME) throws Exception;

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
	public List<Map<String, Object>> getAdminObjectRelConn(@Param("TABLE_NAME") String TABLE_NAME) throws Exception;

	/**
	 * 객체 관계  관리 리스트(콤보)
	 * @since 2019.03.28
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelCombo(@Param("TABLE_NAME") String TABLE_NAME) throws Exception;



	/**
	 * 객체 관계  관리 조인식 설정
	 * @since 2019.04.01
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelJoin(@Param("STD_TABLE") String STD_TABLE,@Param("CONN_TABLE") String CONN_TABLE) throws Exception;



	/**
	 * 객체 관계  관리 조인식 자동 작성
	 * @since 2019.04.01
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectRelJoinStandKeyList(@Param("STD_TABLE") String STD_TABLE) throws Exception;

	public List<Map<String, Object>> getAdminObjectRelJoinConnKeyList(@Param("CONN_TABLE") String CONN_TABLE) throws Exception;

	/**
	 * 객체 관계  관리 조인식 업데이트
	 * @since 2019.04.02
	 * @author 조형욱
	 */

	public int setAdminObjectRelJoinUpdate(@Param("STD_TABLE") String STD_TABLE,@Param("CONN_TABLE") String CONN_TABLE,@Param("JOIN_EXPR") String JOIN_EXPR,@Param("ID") String ID) throws Exception;

	/**
	 * 객체 관계  관리 조인식 인서트
	 * @since 2019.04.02
	 * @author 조형욱
	 */

	public int setAdminObjectRelJoinInsert(@Param("STD_TABLE") String STD_TABLE,@Param("CONN_TABLE") String CONN_TABLE,@Param("JOIN_EXPR") String JOIN_EXPR,@Param("ID") String ID) throws Exception;


	/**
	 * 객체 관계 관리 삭제
	 * @since 2019.04.02
	 * @author 조형욱
	 */

	public int setAdminObjectRelJoinDelete(@Param("STD_TABLE") String STD_TABLE,@Param("CONN_TABLE") String CONN_TABLE) throws Exception;

	/**
	 * repo 객체정보관리 인서트 업데이트 구분
	 * @since 2019.04.04
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectInfoGubunInsertUpdate(@Param("TABLE_NAME") String TABLE_NAME,@Param("COL_NAME") String COL_NAME) throws Exception;

	/**
	 * 활성화 하기 전 체크  3
	 * @since 2019.04.08
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getAdminObjectActiveCheck3(@Param("table_name") String table_name) throws Exception;
	/**
	 * 관리자 가입 아이디 중복 조회
	 * @since 2019.04.11
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getManagerIdCheck(@Param("insertId") String insertId) throws Exception;

	/**
	 * 관리자 가입 데이터 인서트
	 * @since 2019.04.11
	 * @author 조형욱
	 */
	public  int setSignUpManager(@Param("MANAGER_ID") String MANAGER_ID, @Param("MANAGER_PASSWORD") String MANAGER_PASSWORD, @Param("ID") String ID) throws Exception;

	/**
	 * 관리자 가입 IP 데이터 인서트
	 * @since 2019.04.11
	 * @param MANAGER_ID
	 * @param MANAGER_PASSWORD
	 * @param MANAGER_IP
	 * @param ID
	 * @return
	 * @throws Exception
	 */
	public  int setSignUpManagerIp(@Param("MANAGER_ID") String MANAGER_ID, @Param("MANAGER_PASSWORD") String MANAGER_PASSWORD, @Param("MANAGER_IP") String MANAGER_IP,@Param("ID") String ID) throws Exception;

	/**
	 * 관리자 리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */

	public List<Map<String, Object>> getManagerList() throws Exception;

	public Map<String, Object> getManagerInfo(String userId) throws Exception;
	
	
	/**
	 * 관리자 아이디 삭제
	 * @since 2019.04.15
	 * @author 조형욱
	 */
	public int setManagerDelete(@Param("MANAGER_ID") String MANAGER_ID) throws Exception;

	/**
	 * 관리자 ip리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */
	public List<Map<String, Object>> getManagerIpList(@Param("MANAGER_ID") String MANAGER_ID) throws Exception;

	/**
	 * ip insert Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	public int setManagerIpInsert(@Param("MANAGER_ID") String MANAGER_ID,@Param("MANAGER_IP") String MANAGER_IP,@Param("ID") String ID) throws Exception;

	/**
	 * ip Delete Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	public int setManagerIpDelete(@Param("MANAGER_ID") String MANAGER_ID,@Param("MANAGER_IP") String MANAGER_IP) throws Exception;

	/**
	 * ip update Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	public int setManagerIpUpdate(@Param("MANAGER_ID") String MANAGER_ID,@Param("MANAGER_IP") String MANAGER_IP,@Param("ID") String ID,@Param("STAND_MANAGER_IP") String STAND_MANAGER_IP) throws Exception;

	/**
	 * 관리자 비번 가져오기
	 * @since 2019.04.18
	 * @author 조형욱
	 */
	public Map<String, Object> getAdminPwd(@Param("managerId") String managerId) throws Exception;

	/**
	 * 관리자 비밀번호 변경
	 * @since 2019.04.18
	 * @author 조형욱
	 */
	public void ActionAdminPwdChange(@Param("MANAGER_ID") String MANAGER_ID,@Param("encodePwd") String encodePwd) throws Exception;

	/**
	 * 관리자 활성화 업데이트
	 * @since 2019.04.25
	 * @author 조형욱
	 */

	public int setManagerEnabledUpdate(@Param("MANAGER_ID") String MANAGER_ID,@Param("ENABLED") int ENABLED,@Param("UPDATE_ID") String UPDATE_ID) throws Exception;

	/**
	 * 회원저장목록(관리자) 삭제
	 * @since 2019.04.29
	 * @author 조형욱
	 */
	public void setManagerSaveDelete(@Param("MANAGER_ID") String MANAGER_ID) throws Exception;

	/**
	 * ML 코드 가져오기(그룹코드)
	 * @since 2019.11.05
	 * @author 최진
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getMLCode() throws Exception;

	/**
	 * ML 코드 관계 가져오기
	 * @since 2019.11.05
	 * @author 최진
	 * @return
	 */
	public List<Map<String, Object>> getMLCodeRelation() throws Exception;

	/**
	 * ML 코드 가져오기(feature코드)
	 * @since 2019.11.05
	 * @author 최진
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getMLFeaCode() throws Exception;

	/**
	 * 마지막 코드 가져오기(FEA_CODE)
	 * @since 2019.11.05
	 * @author 최진
	 * @return
	 * @throws Exception
	 */
	public String getFeaLastCodeName() throws Exception;

	/**
	 * Feature 값 insert
	 * @param nextCodeStr
	 * @param parentCodeName
	 * @param codeStrName
	 * @param codeValue
	 * @param userName
	 */
	public void insertFeaCodeData(@Param("feaCode") String nextCodeStr, @Param("parentCode") String parentCodeName,
			@Param("codeStr") String codeStrName, @Param("codeValue") String codeValue,
			@Param("userName") String userName) throws Exception;

	public void deleteAllFeaCodeData() throws Exception;

	public void deleteAllFeaGrpInfoData() throws Exception;
	
	public void delFeaCode(String selCode) throws Exception;
	public void savFeaCode(Map<String, Object> param) throws Exception;
	public void savGrpInfo(Map<String, Object> param) throws Exception;

	public void excelUploadinsertFeaCodeData(Map<String, Object> param) throws Exception; //TB_FEA CDOE
	
	public void excelUploadinsertFeaGrpInfoData(Map<String, Object> param) throws Exception;// TB_FEA_GRP_INFO

	public void excelUploadinsertHierarchyData(Map<String, Object> param) throws Exception;
	/**
	 * Feature값의 하위 detail 값 insert
	 * @param parentGrpCode
	 * @param parentFeaCode
	 * @param codeValue
	 * @param userName
	 * @throws Exception
	 */
	public void insertFeaGrpDetail(@Param("grpCode") String parentGrpCode, @Param("feaCode") String parentFeaCode,
			@Param("detailValue") String codeValue, @Param("userName") String userName) throws Exception;

	/**
	 * Feature 값 update
	 * @param parentGrpCode
	 * @param feaCode
	 * @param codeStrName
	 * @param codeValue
	 * @param userName
	 */
	public void updateFeaCodeData(@Param("grpCode") String parentGrpCode, @Param("feaCode") String feaCode, @Param("codeStr") String codeStrName,
			@Param("codeVal") String codeValue, @Param("userName") String userName, @Param("originCdStr") String originCodeStrName, @Param("originCdVal") String originCodeName) throws Exception;

	/**
	 * Feature값의 하위 detail 값 update
	 * @param parentGrpCode
	 * @param feaCode
	 * @param codeValue
	 * @param userName
	 */
	public void updateFeaDetailCodeData(@Param("grpCode") String parentGrpCode,  @Param("feaCode") String feaCode,
			@Param("codeVal") String codeValue, @Param("userName") String userName, @Param("originVal") String originValue) throws Exception;

	/**
	 * Feature 값 delete
	 * @param grpCode
	 * @param feaCode
	 * @param codeStrName
	 * @param codeValue
	 */
	public void deleteFeaCodeData(@Param("grpCode") String grpCode, @Param("feaCode") String feaCode,@Param("codeStrName") String codeStrName, @Param("codeValue") String codeValue);

	/**
	 *  Feature값의 하위 detail 값 delete
	 * @param grpCode
	 * @param feaCode
	 * @param value
	 */
	public void deleteFeaDetailCodeData(@Param("grpCode") String grpCode, @Param("feaCode") String feaCode, @Param("value") String value) throws Exception;

	/**
	 * Hierarchical Code(~5dept) 가져오기
	 * @return
	 */
	public List<Map<String, Object>> getHierarchyCodes() throws Exception;

	public void deleteHierarchyCodes() throws Exception;

	public void adminMLGroupInfoUpdate(@Param("pk_num") int pk_num,  @Param("level_1") String level_1,
			@Param("level_2") String level_2, @Param("level_3") String level_3, @Param("level_4") String level_4, @Param("level_5") String level_5) throws Exception;

	public void adminMLGroupInfoDelete(@Param("pk_num") int pk_num) throws Exception;

	public void adminMLGroupInfoInsert(@Param("level_1") String level_1,
			@Param("level_2") String level_2, @Param("level_3") String level_3, @Param("level_4") String level_4, @Param("level_5") String level_5,@Param("pk_num") int pk_num) throws Exception;

	/**
	 * ML 정보 엑셀 다운로드
	 * @since 2020.10.21
	 * @author 조호철
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> downMLGroupInfo(String codeNm) throws Exception;

	/**
	 * 공통코드 조회
	 * @since 2020.11.04
	 * @author 강전일
	 */
	public List<Map<String, Object>> getRuleList(Map<String, Object> param) throws Exception;

	public int getRuleCount(Map<String, Object> params) throws Exception;
	
	/**
	 * rule base 규칙 저장
	 * @since 2020.11.04
	 * @author 강전일
	 */
	public void ruleListSave(Map<String, Object> params) throws Exception;

	public void ruleListDelete(Map<String, Object> params) throws Exception;
	
	public List<Map<String, Object>> getPopRuleList(Map<String, Object> param) throws Exception;
	
	/**
	 * rule base pop 규칙 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */
	public void ruleListPopSave(Map<String, Object> params) throws Exception;

	public void ruleListPopDelete(Map<String, Object> params) throws Exception;
	
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
	public void savPriceInfo(Map<String, Object> param) throws Exception;
	public void editPriceInfo(Map<String, Object> param) throws Exception;
	public void delPriceInfo(Map<String, Object> param) throws Exception;
	
	public void savGrp(Map<String, Object> param) throws Exception;
	public void savSeal(Map<String, Object> param) throws Exception;
	public void savSheet(Map<String, Object> param) throws Exception;
	
	public void editGrp(Map<String, Object> param) throws Exception;
	public void editSheet(Map<String, Object> param) throws Exception;
	
	public void delGrp(Map<String, Object> param) throws Exception;
	public void delSealAll(Map<String, Object> param) throws Exception;
	public void delSheetAll(Map<String, Object> param) throws Exception;
	public List<Map<String, Object>> getDelSheetList(Map<String,Object> param) throws Exception;
	public void delSeal(Map<String, Object> param) throws Exception;
	public void delSheet(Map<String, Object> param) throws Exception;
	
	/**
	 * seal type list 조회
	 * @since 2021.03.11
	 * @author 강전일
	 */
	public List<Map<String, Object>> getSealTypeList(Map<String, Object> param) throws Exception;
}
