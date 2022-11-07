package com.levware.admin.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.geometry.spherical.oned.ArcsSet.Split;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.netlib.util.StrictUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.levware.admin.service.AdminObjectListVO;
import com.levware.admin.service.AdminService;
import com.levware.admin.service.CodeManagementVO;
import com.levware.common.ExcelUtil;
import com.levware.common.FileService;
import com.levware.common.mappers.mart.AdminObjectSelectMapper;
import com.levware.common.mappers.repo.AdminInfoCriticaMapper;
import com.levware.common.mappers.repo.AdminMapper;
import com.levware.common.mappers.repo.UserSignUpMapper;
import com.levware.user.service.UserInfoVO;


import egovframework.rte.fdl.property.EgovPropertyService;
import freemarker.template.utility.StringUtil;


@Service("AdminService")
public class AdminServiceImpl implements AdminService {

	private static final Logger LOGGER = LogManager.getLogger(AdminServiceImpl.class);

	@Resource(name = "adminMapper")
	private AdminMapper adminMapper;

	@Resource(name = "adminObjectSelectMapper")
	private AdminObjectSelectMapper adminObjectSelectMapper;

	@Resource(name = "infoCritMapper")
	private AdminInfoCriticaMapper critMapper;


	@Resource(name = "userSignUpMapper")
	private UserSignUpMapper userSignUpMapper;

	@Resource(name="propertyService")
	protected EgovPropertyService propertyService;
	
	@Autowired
    private FileService fileService;



	public List<AdminObjectListVO>  getAdminObjectList() throws Exception {



		List<AdminObjectListVO> objectList = adminMapper.getAdminObjectList();



		return objectList;

	}

	/**
	 * 코드관리 select
	 * @since 2019.03.05
	 * @author 박수연
	 */
	public List<CodeManagementVO>  getCodeManagementList() throws Exception {
		List<CodeManagementVO> objectList = adminMapper.getCodeManagementList();
		return objectList;
	}
	/**
	 * 코드관리 insert
	 * @since 2019.03.07
	 * @author 박수연
	 */
	@Override
	public void insertCodeManagement(Map<String, String> param) throws Exception {
		//VO Setting
		CodeManagementVO setParam = new CodeManagementVO();
		setParam.setQryConCd(param.get("insertCd"));
		setParam.setQryConCdNm(param.get("insertCdNm"));
		setParam.setOperSym(param.get("insertOperSym"));
		setParam.setQryExample(param.get("insertQryExample"));
		setParam.setCreateId(param.get("createId"));
		adminMapper.insertCodeManagement(setParam);
	}
	/**
	 * 코드관리 delete
	 * @since 2019.03.08
	 * @author 박수연
	 */
	@Override
	public void deleteCodeManagement(List<String> param) throws Exception {
		adminMapper.deleteCodeManagement(param);
	}

	@Override
	public int getAdminObjectDelete(String table_name) throws Exception {

		int result =adminMapper.setAdminObjectDelete(table_name);

		return result;
	}



	@Override
	public int getAdminObjectActive(Map<String, String> param) throws Exception {

		String table_name = (String) param.get("table_name");

		String activ_yn = (String) param.get("activ_yn");

		String ID = (String) param.get("ID");

		int result =adminMapper.setAdminObjectActive(table_name,activ_yn,ID);

		return result;
	}


	/**
	 * 객체 관리 -> 마트에서 객체리스트  select
	 * @since 2019.03.11
	 * @author 조형욱
	 */
	@Override
	public List<Map<String, Object>> getAdminObjectSelect(List<String> param) throws Exception {
		List<Map<String, Object>> result= adminObjectSelectMapper.getAdminObjectSelect(param);
		return result;
	}
	/**
	 * 객체 관리 -> 마트에서 객체가져와서 repo에 저장
	 * @since 2019.03.11
	 * @author 조형욱
	 */
	@Override
	public int setAdminObjectInsert(String object_name,String ID) throws Exception {

		//String object_name = (String) param.get("object_name");



		int result =adminMapper.setAdminObjectInsert(object_name,ID);
		return result;
	}
	/**
	 * repo에 저장된 테이블 리스트 가져옴
	 * @since 2019.03.14
	 * @author 조형욱
	 */
	@Override
	public List<Map<String, Object>> getAdminObjectTableList() throws Exception {
		List<Map<String, Object>> result =adminMapper.getAdminObjectTableList();
		return result;
	}

	/**
	 * repo 객체관리 업데이트
	 * @since 2019.03.15
	 * @author 조형욱
	 */

	@Override
	public int getAdminObjectUpdate(Map<String, String> param) throws Exception {

		String table_name = (String) param.get("table_name");

		String obj_name = (String) param.get("obj_name");

		String obj_desc = (String) param.get("obj_desc");

		String ID = (String) param.get("ID");

		int result =adminMapper.setAdminObjectUpdate(table_name,obj_name,obj_desc,ID);

		return result;
	}



	/**
	 * repo 객체정보관리 리스트
	 * @since 2019.03.18
	 * @author 조형욱
	 */
	@Override
	public List<Map<String, Object>> getAdminObjectRepoColList(String table_name)  throws Exception {
		List<Map<String, Object>> result= adminObjectSelectMapper.getAdminObjectRepoColList(table_name);
		return result;
	}


	@Override
	public List<Map<String, Object>> getAdminObjectInfoList(String table_name)  throws Exception {
		List<Map<String, Object>> result= adminMapper.getAdminObjectInfoList(table_name);
		return result;
	}


	/**
	 * 활성화 하기 전 체크 1
	 * @since 2019.03.20
	 * @author 조형욱
	 */
	@Override
	public List<Map<String, Object>> getAdminObjectActiveCheck1(Map<String, String> param) throws Exception {

		String table_name = (String) param.get("table_name");

		List<Map<String, Object>> result= adminMapper.getAdminObjectActiveCheck1(table_name);
		return result;
	}


	/**
	 * 활성화 하기 전 체크 2
	 * @since 2019.03.20
	 * @author 조형욱
	 */
	@Override
	public List<Map<String, Object>> getAdminObjectActiveCheck2(Map<String, String> param) throws Exception {

		String table_name = (String) param.get("table_name");

		List<Map<String, Object>> result= adminMapper.getAdminObjectActiveCheck2(table_name);
		return result;
	}

	/**
	 * repo 객체정보관리 업데이트,인서트
	 * @since 2019.03.21
	 * @author 조형욱
	 */

	@Override
	public int setAdminObjectInfoUpdate(Map<String, String> param) throws Exception {



		String OBJINFO_NAME = (String) param.get("OBJINFO_NAME");

		String OBJINFO_DESC = (String) param.get("OBJINFO_DESC");

		String DATA_TYPE = (String) param.get("DATA_TYPE");


		String PK_GUBUN = (String) param.get("PK_GUBUN");

		String STAND_DATE = (String) param.get("STAND_DATE");


		String TABLE_NAME = (String) param.get("TABLE_NAME");

		String COL_NAME = (String) param.get("COL_NAME");

		String ID = (String) param.get("ID");

		String VIEW_DURATION_UNIT = (String) param.get("VIEW_DURATION_UNIT");

		String VIEW_DURATION_NUM = (String) param.get("VIEW_DURATION_NUM");
		String SEQ             = (String) param.get("SEQ");

		String DISPLAY_YN               = (String) param.get("DISPLAY_YN");
		String CALC_FUNC_YN             = (String) param.get("CALC_FUNC_YN");




		int result =adminMapper.setAdminObjectInfoUpdate(OBJINFO_NAME,OBJINFO_DESC,DATA_TYPE,PK_GUBUN,STAND_DATE,TABLE_NAME,COL_NAME,ID,VIEW_DURATION_UNIT,VIEW_DURATION_NUM,SEQ,DISPLAY_YN,CALC_FUNC_YN);


		return result;
	}
	@Override
	public int setAdminObjectInfoInsert(Map<String, String> param) throws Exception {

		String TABLE_NAME = (String) param.get("TABLE_NAME");

		String COLUMN_NAME = (String) param.get("COLUMN_NAME");

		String OBJINFO_NAME = (String) param.get("OBJINFO_NAME");

		String OBJINFO_DESC = (String) param.get("OBJINFO_DESC");

		String DATA_TYPE = (String) param.get("DATA_TYPE");

		String PK_GUBUN = (String) param.get("PK_GUBUN");

		String STAND_DATE = (String) param.get("STAND_DATE");

		String ID = (String) param.get("ID");

		String VIEW_DURATION_UNIT = (String) param.get("VIEW_DURATION_UNIT");
		String VIEW_DURATION_NUM = (String) param.get("VIEW_DURATION_NUM");
		String SEQ             = (String) param.get("SEQ");


		String DISPLAY_YN               = (String) param.get("DISPLAY_YN");
		String CALC_FUNC_YN             = (String) param.get("CALC_FUNC_YN");




		int result =adminMapper.setAdminObjectInfoInsert(TABLE_NAME,COLUMN_NAME,OBJINFO_NAME,OBJINFO_DESC,DATA_TYPE,PK_GUBUN,STAND_DATE,ID,VIEW_DURATION_UNIT,VIEW_DURATION_NUM,SEQ,DISPLAY_YN,CALC_FUNC_YN);


		return result;
	}


	/**
	 *  객체정보관리 삭제
	 * @since 2019.03.22
	 * @author 조형욱
	 */
	@Override
	public int setAdminObjectInfoDelete(String TABLE_NAME,String COL_NAME) throws Exception {

		int result =adminMapper.setAdminObjectInfoDelete(TABLE_NAME,COL_NAME);

		return result;
	}


	/**
	 * repo 객체정보관리 기준일자 체크
	 * @since 2019.03.26
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, String>> getAdminObjectInfoCheckDate(String TABLE_NAME)  throws Exception {
		List<Map<String, String>> result= adminMapper.getAdminObjectInfoCheckDate(TABLE_NAME);
		return result;


	}

	/**
	 * 객체 관계  관리 리스트(기준 테이블)
	 * @since 2019.03.28
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, Object>> getAdminObjectRelStand() throws Exception {
		List<Map<String, Object>> result =adminMapper.getAdminObjectRelStand();
		return result;
	}


	/**
	 * 객체 관계  관리 리스트(연결 테이블)
	 * @since 2019.03.28
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, Object>> getAdminObjectRelConn(String TABLE_NAME) throws Exception {
		List<Map<String, Object>> result =adminMapper.getAdminObjectRelConn(TABLE_NAME);
		return result;
	}

	/**
	 * 객체 관계  관리 리스트(콤보)
	 * @since 2019.03.28
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, Object>> getAdminObjectRelCombo(String TABLE_NAME) throws Exception {
		List<Map<String, Object>> result =adminMapper.getAdminObjectRelCombo(TABLE_NAME);
		return result;
	}

	/**
	 * 객체 관계  관리 조인식 설정
	 * @since 2019.04.01
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, Object>> getAdminObjectRelJoin(String STD_TABLE,String CONN_TABLE) throws Exception {
		List<Map<String, Object>> result =adminMapper.getAdminObjectRelJoin(STD_TABLE,CONN_TABLE);
		return result;
	}

	/**
	 * 객체 관계  관리 조인식 자동 작성
	 * @since 2019.04.01
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, Object>> getAdminObjectRelJoinStandKeyList(String STD_TABLE) throws Exception {
		List<Map<String, Object>> result =adminMapper.getAdminObjectRelJoinStandKeyList(STD_TABLE);
		return result;
	}

	@Override
	public List<Map<String, Object>> getAdminObjectRelJoinConnKeyList(String CONN_TABLE) throws Exception {
		List<Map<String, Object>> result =adminMapper.getAdminObjectRelJoinConnKeyList(CONN_TABLE);
		return result;
	}


	/**
	 * 객체 관계  관리 조인식 업데이트
	 * @since 2019.04.02
	 * @author 조형욱
	 */
	@Override
	public int setAdminObjectRelJoinUpdate(String STD_TABLE,String CONN_TABLE,String JOIN_EXPR,String ID) throws Exception {
		int result =adminMapper.setAdminObjectRelJoinUpdate(STD_TABLE,CONN_TABLE,JOIN_EXPR,ID);
		return result;
	}

	/**
	 * 객체 관계  관리 조인식 인서트
	 * @since 2019.04.02
	 * @author 조형욱
	 */
	@Override
	public int setAdminObjectRelJoinInsert(String STD_TABLE,String CONN_TABLE,String JOIN_EXPR,String ID) throws Exception {
		int result =adminMapper.setAdminObjectRelJoinInsert(STD_TABLE,CONN_TABLE,JOIN_EXPR,ID);
		return result;
	}


	/**
	 * 객체 관계 관리 삭제
	 * @since 2019.04.02
	 * @author 조형욱
	 */

	@Override
	public int setAdminObjectRelJoinDelete(String STD_TABLE,String CONN_TABLE) throws Exception {

		int result =adminMapper.setAdminObjectRelJoinDelete(STD_TABLE,CONN_TABLE);

		return result;
	}


	/**
	 * repo 객체정보관리 인서트 업데이트 구분
	 * @since 2019.04.04
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, Object>> getAdminObjectInfoGubunInsertUpdate(String TABLE_NAME,String COL_NAME)  throws Exception {
		List<Map<String, Object>> result= adminMapper.getAdminObjectInfoGubunInsertUpdate(TABLE_NAME,COL_NAME);
		return result;
	}

	/**
	 * 활성화 하기 전 체크  3
	 * @since 2019.04.08
	 * @author 조형욱
	 */
	@Override
	public List<Map<String, Object>> getAdminObjectActiveCheck3(Map<String, String> param) throws Exception {

		String table_name = (String) param.get("table_name");

		List<Map<String, Object>> result= adminMapper.getAdminObjectActiveCheck3(table_name);
		return result;
	}

	/**
	 * 관리자 가입 아이디 중복 조회
	 * @since 2019.04.11
	 * @author 조형욱
	 */

	@Override
	public List<Map<String, Object>> getManagerIdCheck(String insertId) throws Exception {
		List<Map<String, Object>> result =adminMapper.getManagerIdCheck(insertId);
		return result;
	}

	/**
	 * 관리자 가입 데이터 인서트
	 * @since 2019.04.11
	 * @author 조형욱
	 */
	@Override
	public int setSignUpManager(Map<String, String> param) throws Exception {


		//암호화
		String Pw = param.get("MANAGER_PASSWORD");
		StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();

		String MANAGER_PASSWORD = passwordEncoder.encode(Pw);

		String MANAGER_ID = (String) param.get("MANAGER_ID");


//		String MANAGER_IP = (String) param.get("IP");

		String ID = (String) param.get("ID");


		int result =adminMapper.setSignUpManager(MANAGER_ID,MANAGER_PASSWORD,ID);
//		int ipResult = adminMapper.setSignUpManagerIp(MANAGER_ID,MANAGER_PASSWORD,MANAGER_IP, ID);
//		return result + ipResult;
		return result;
	}

	/**
	 * 객체정보별 조회 조건 관리 조회
	 * @since 2019.4.01
	 * @author 강전일
	 */
	@Override
	public List<UserInfoVO> getInfoCriteria(String tableName) throws Exception {
		List<UserInfoVO> objectList = critMapper.getInfoCriteria(tableName);
		return objectList;
	}

	/**
	 * 조회 조건 조회
	 * @since 2019.4.03
	 * @author 강전일
	 */
	@Override
	public List<Map<String,Object>> getInfoCondition() throws Exception {
		List<Map<String,Object>> objectList = critMapper.getInfoCondition();
		LOGGER.debug(objectList + "objectList sign-up serviceimpl getInfoCondition");
		return objectList;
	}

	/**
	 * 조회조건 동적 체크
	 * @since 2019.04.04
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getSearchChk(Map<String, Object> param) throws Exception {
		List<Map<String,Object>> objectList = critMapper.getSearchChk(param);
		return objectList;
	}

	/**
	 * 조회 조건 저장
	 * @since 2019.04.04
	 * @author 강전일
	 */
	@Override
	public void updateCondifion(List<Map<String,Object>> chkArr, List<Map<String,Object>> unChkArr, Map<String,Object> conditionJson) throws Exception {
		LOGGER.info(chkArr);
		LOGGER.info(unChkArr);
		LOGGER.info(conditionJson);
		if(!chkArr.isEmpty()){
			critMapper.updateConChk(chkArr);
		}
		if(!unChkArr.isEmpty()){
			critMapper.deleteConUnChk(unChkArr);
		}
		critMapper.updateConYn(conditionJson);
	}

	/**
	 * 회원목록 select
	 * @since 2019.03.27
	 * @author 강전일
	 */
	@Override
	public List<UserInfoVO> getUserInfoList(String userId) throws Exception {
		List<UserInfoVO> objectList = userSignUpMapper.getUserInfoList(userId);
		return objectList;
	}
	
	/**
	 * 회원 목록 삭제
	 * @since 2019.03.28
	 * @author 강전일
	 */
	@Override
	public void deleteUserList(List<String> param) throws Exception {
		userSignUpMapper.deleteUserList(param);
	}
	
	
	/**
	 * 사용자 자료실 권한정보 저장
	 */
	public Map<String,Object> setUserData(Map<String, Object> param) throws Exception{
		userSignUpMapper.setUserData(param);
		return null;
	}

	/**
	 * 관리자 리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */
	public List<Map<String, Object>>  getManagerList() throws Exception {



		List<Map<String, Object>> ManagerList = adminMapper.getManagerList();

		return ManagerList;

	}

	/**
	 * 관리자 아이디 삭제
	 * @since 2019.04.15
	 * @author 조형욱
	 */

	@Override
	public int setManagerDelete(String MANAGER_ID) throws Exception {

		int result =adminMapper.setManagerDelete(MANAGER_ID);

		return result;
	}

	/**
	 * 관리자 ip리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */
	@Override
	public List<Map<String, Object>> getManagerIpList(String MANAGER_ID) throws Exception {
		List<Map<String, Object>> result =adminMapper.getManagerIpList(MANAGER_ID);
		return result;
	}

	/**
	 * ip insert Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */

	@Override
	public int setManagerIpInsert(Map<String, Object> param) throws Exception {

		String MANAGER_ID = (String) param.get("MANAGER_ID");
		String MANAGER_IP = (String) param.get("MANAGER_IP");
		String ID = (String) param.get("createId");

		int result= adminMapper.setManagerIpInsert(MANAGER_ID,MANAGER_IP,ID);
		return result;
	}

	/**
	 * ip Delete Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	@Override
	public int setManagerIpDelete(Map<String, Object> param) throws Exception {

		String MANAGER_ID = (String) param.get("MANAGER_ID");
		String MANAGER_IP = (String) param.get("MANAGER_IP");


		int result= adminMapper.setManagerIpDelete(MANAGER_ID,MANAGER_IP);
		return result;
	}

	/**
	 * ip update Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	@Override
	public int setManagerIpUpdate(Map<String, Object> param) throws Exception {

		String MANAGER_ID = (String) param.get("MANAGER_ID");
		String MANAGER_IP = (String) param.get("MANAGER_IP");
		String ID = (String) param.get("ID");
		String STAND_MANAGER_IP = (String) param.get("STAND_MANAGER_IP");



		int result= adminMapper.setManagerIpUpdate(MANAGER_ID,MANAGER_IP,ID,STAND_MANAGER_IP);
		return result;
	}

	/**
	 * 관리자 비번 가져오기
	 * @since 2019.04.18
	 * @author 조형욱
	 */
	public Map<String, Object>  getAdminPwd(String managerId) throws Exception {



		Map<String, Object> pw = adminMapper.getAdminPwd(managerId);

		return pw;

	}

	/**
	 * 관리자 비밀번호 변경
	 * @since 2019.04.18
	 * @author 조형욱
	 */
	@Override
	public String ActionAdminPwdChange(Map<String, String> param) throws Exception {
		String result = "";

		//비번 인코딩
		StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
		String encodePwd = passwordEncoder.encode(param.get("insertnewPwd"));

		//인코딩 체크
		Boolean pwdCheck = passwordEncoder.matches(param.get("insertnewPwd"), encodePwd);

		String MANAGER_ID = (String) param.get("managerId");


		if(pwdCheck){


			adminMapper.ActionAdminPwdChange(MANAGER_ID,encodePwd);
			result ="true";
		}else{
			result = "error"; //인코딩에러
		}

		return result;
	}

	/**
	 * 관리자 활성화 업데이트
	 * @since 2019.04.25
	 * @author 조형욱
	 */
	@Override
	public int setManagerEnabledUpdate(Map<String, Object> param) throws Exception {

		String MANAGER_ID = (String) param.get("MANAGER_ID");

		int ENABLED = (int) param.get("ENABLED");

		String UPDATE_ID = (String) param.get("UPDATE_ID");

		int result =adminMapper.setManagerEnabledUpdate(MANAGER_ID,ENABLED,UPDATE_ID);

		return result;
	}
	/**
	 * 회원저장목록 삭제
	 * @since 2019.04.29
	 * @author 조형욱
	 */
	@Override
	public void deleteUserSaveList(List<String> param) throws Exception {
		userSignUpMapper.deleteUserSaveList(param);
	}
	/**
	 * 회원저장목록(관리자) 삭제
	 * @since 2019.04.29
	 * @author 조형욱
	 */
	@Override
	public void setManagerSaveDelete(String MANAGER_ID) throws Exception {
		adminMapper.setManagerSaveDelete(MANAGER_ID);
	}

	/**
	 * 테이블 코드정보 가져오기
	 * @since 2019.11.05
	 * @author 최진
	 */
	@Override
	public Map<String, Object> getAdminTbCode(String flag) throws Exception {
		if(flag.equals("MLCode")){
			Map<String, Object> rtnDataset = new HashMap<String, Object>();
			List<Map<String, Object>> grpCodeData = adminMapper.getMLCode();
			List<Map<String, Object>> feaCodeData = adminMapper.getMLFeaCode();
			List<Map<String, Object>> relationData = adminMapper.getMLCodeRelation();

			rtnDataset.put("grpCode", grpCodeData);

			Map<String, Object> feaCodeMap = new HashMap<String, Object>();
			for(int i=0;i<feaCodeData.size();i++){
				String grpCode =(String) feaCodeData.get(i).get("PARENT_CODE");
				if(feaCodeMap.containsKey(grpCode)){
					@SuppressWarnings("unchecked") //@SuppressWarnings 정적분석 진행할때 오류가 아니라고 마킹해주는 역할. (uncheked : 미확인 오퍼레이션과 관련된 경고를 억제)
					List<Map<String, Object>> setElem = (List<Map<String, Object>>) feaCodeMap.get(grpCode);
					setElem.add(feaCodeData.get(i));
					feaCodeMap.put(grpCode, setElem);
				}else{
					List<Map<String, Object>> setElem = new ArrayList<Map<String, Object>>();
					setElem .add(feaCodeData.get(i));
					feaCodeMap.put(grpCode, setElem);
				}
			}
			rtnDataset.put("feaCode", feaCodeMap);

			Map<String, Object> valCodeMap = new HashMap<String, Object>();
			for(int j=0;j<relationData.size();j++){
				String feaCode =(String) relationData.get(j).get("FEA_CODE");
				if(valCodeMap.containsKey(feaCode)){
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> setElem = (List<Map<String, Object>>) valCodeMap.get(feaCode);
					setElem.add(relationData.get(j));
					valCodeMap.put(feaCode, setElem);
				}else{
					List<Map<String, Object>> setElem = new ArrayList<Map<String, Object>>();
					setElem .add(relationData.get(j));
					valCodeMap.put(feaCode, setElem);
				}
			}


			rtnDataset.put("relationList", valCodeMap);
			return rtnDataset;
		}else if(flag.equals("Hierarchy")){
			// Hierarchy 코드 정보 가져오기
			Map<String, Object> rtnDataset = new HashMap<String, Object>();
			List<Map<String, Object>> hierarchyCode = adminMapper.getHierarchyCodes();

			rtnDataset.put("hierarchy", hierarchyCode);
			return rtnDataset;
		}else{
			throw new Exception("Unknown Parameter");

		}


	}

	/**
	 * 코드 다음 번호 채번하는 메서드
	 * @param preStr
	 * @return
	 */
	private String getNextCodeStr (String preStr){

		String codeStr = preStr.replaceAll("[0-9]", "");
		String numStr = preStr.replaceAll("[^0-9]", "");
		int codeNum = Integer.parseInt(numStr);
		codeNum++;
		int numLength = (int)(Math.log10(codeNum)+1);
		int maxCodeLength = preStr.length() - codeStr.length();
		int addZeroCnt = maxCodeLength - numLength;
		for(int i=0;i<addZeroCnt;i++){
			codeStr +="0";
		}
		codeStr += codeNum;
		return codeStr;
	}
	/**
	 * 테이블 코드 Feature insert
	 * @since 2019.11.05
	 * @author 최진
	 */
	@Override
	public void insertAdminTbFeaCode(Map<String, Object> param) throws Exception {
		String lastCodeStr = adminMapper.getFeaLastCodeName();
		// 다음 채번
		String nextCodeStr = getNextCodeStr(lastCodeStr);
		LOGGER.info(nextCodeStr);
		@SuppressWarnings("unchecked")
		Map<String, Object> getParentParam = (Map<String, Object>) param.get("parentData");
		String parentCodeName = (String) getParentParam.get("CODE_NAME");
		String codeStrName = (String) param.get("codeStrName");
		String codeValue = (String) param.get("codeValue");
		String userName = (String) param.get("userName");
		adminMapper.insertFeaCodeData(nextCodeStr, parentCodeName, codeStrName, codeValue,userName);
	}

	/**
	 * 테이블 코드 fea_Grp_value detail insert
	 * @since 2019.11.05
	 * @author 최진
	 */
	@Override
	public void insertAdminTbDetailValCode(Map<String, Object> param) throws Exception {

		LOGGER.info(param);
		String codeValue = (String) param.get("codeValue");
		@SuppressWarnings("unchecked")
		Map<String, Object> getParentParams = (Map<String, Object>) param.get("parentData");
		String parentGrpCode = (String) getParentParams.get("PARENT_CODE");
		String parentFeaCode = (String) getParentParams.get("CODE_NAME");
		String userName = (String) param.get("userName");

		adminMapper.insertFeaGrpDetail(parentGrpCode, parentFeaCode, codeValue, userName);
	}

	/**
	 * 테이블 코드 Feature update
	 * @since 2019.11.05
	 * @author 최진
	 */
	@Override
	public void updateAdminTbFeaCode(Map<String, Object> param) throws Exception  {
		LOGGER.info(param);
		@SuppressWarnings("unchecked")
		Map<String, Object> originDataParams = (Map<String, Object>) param.get("originCodeData");
		String parentGrpCode = (String) originDataParams.get("PARENT_CODE");
		String feaCode = (String) originDataParams.get("CODE_NAME");
		String userName = (String) param.get("userName");
		String codeStrName = (String) param.get("codeStrName");
		String codeValue = (String) param.get("codeValue");

		String originCodeStrName = (String) originDataParams.get("CODE_STR_NAME");
		String originCodeName = (String) originDataParams.get("CODE_VALUE");

		adminMapper.updateFeaCodeData(parentGrpCode, feaCode, codeStrName, codeValue,userName,originCodeStrName,originCodeName);
	}

	/**
	 * 테이블 코드 Feature 하위 value update
	 * @since 2019.11.05
	 * @author 최진
	 */
	@Override
	public void updateAdminTbDetailValCode(Map<String, Object> param) throws Exception {
		LOGGER.info(param);
		@SuppressWarnings("unchecked")
		Map<String, Object> getParentParams = (Map<String, Object>) param.get("originCodeData");
		String parentGrpCode = (String) getParentParams.get("GRP_CODE");
		String feaCode = (String) getParentParams.get("FEA_CODE");
		String userName = (String) param.get("userName");
		String codeValue = (String) param.get("codeValue");
		String originValue =(String) getParentParams.get("DETAIL_VALUES");
		adminMapper.updateFeaDetailCodeData(parentGrpCode, feaCode, codeValue, userName, originValue);
	}

	/**
	 * @since 2019.11.05
	 * @author 최진
	 * @throws Exception
	 */
	@Override
	@Transactional
	public void deleteAdminTbFeaCode(Map<String, Object> param) throws Exception {
		LOGGER.info(param);
		@SuppressWarnings("unchecked")
		Map<String, Object> getDelParams = (Map<String, Object>) param.get("delData");

		String grpCode = (String) getDelParams.get("PARENT_CODE");
		String feaCode = (String) getDelParams.get("CODE_NAME");
		String codeStrName = (String) getDelParams.get("CODE_STR_NAME");
		String codeValue = (String) getDelParams.get("CODE_VALUE");
		String value = "";
		adminMapper.deleteFeaDetailCodeData(grpCode, feaCode, value);
		adminMapper.deleteFeaCodeData(grpCode, feaCode, codeStrName, codeValue);


	}

	/**
	 * @since 2019.11.05
	 * @author 최진
	 * @throws Exception
	 */
	@Override
	public void deleteAdminTbDetailValCode(Map<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.info(param);
		@SuppressWarnings("unchecked")
		Map<String, Object> getDelParams = (Map<String, Object>) param.get("delData");
		String grpCode = (String) getDelParams.get("GRP_CODE");
		String feaCode = (String) getDelParams.get("FEA_CODE");
		String value = (String) getDelParams.get("DETAIL_VALUES");
		adminMapper.deleteFeaDetailCodeData(grpCode, feaCode, value);


	}

	/**
	 * @since 2019.11.28
	 * @author
	 * @throws Exception
	 */
	@Override
	public void adminMLGroupInfoUpdate(Map<String, Object> param) throws Exception {

		LOGGER.info(param);
//		@SuppressWarnings("unchecked")
		//Map<String, Object> getParentParams = new HashMap<String, Object>();
		int pk_num = (int)param.get("PK_NUM");
		String level_1 = (String) param.get("LEVEL_1");
		String level_2 = (String) param.get("LEVEL_2");
		String level_3 = (String) param.get("LEVEL_3");
		String level_4 = (String) param.get("LEVEL_4");
		String level_5 =(String) param.get("LEVEL_5");

		adminMapper.adminMLGroupInfoUpdate(pk_num, level_1, level_2, level_3, level_4,level_5);
	}

	@Override
	public void adminMLGroupInfoDelete(Map<String, Object> param) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.info(param);
		//@SuppressWarnings("unchecked")
		int pk_num = (int)param.get("PK_NUM");
		/*String grpCode = (String) getDelParams.get("GRP_CODE");
		String feaCode = (String) getDelParams.get("FEA_CODE");
		String value = (String) getDelParams.get("DETAIL_VALUES");*/
		adminMapper.adminMLGroupInfoDelete(pk_num);


	}

	@Override
	public void adminMLGroupInfoInsert(Map<String, Object> param) throws Exception {


		LOGGER.info(param);

		/*String pk_numStr = (String) param.get("pkMax");
		LOGGER.info(pk_numStr);
		int pk_num = Integer.parseInt(pk_numStr);
		pk_num +=1;*/


		int pk_num = (int)param.get("pkMax");
		pk_num +=1;
		String level_1 = (String) param.get("level1");
		String level_2 = (String) param.get("level2");
		String level_3 = (String) param.get("level3");
		String level_4 = (String) param.get("level4");
		String level_5 =(String) param.get("level5");
		LOGGER.info(pk_num);

		adminMapper.adminMLGroupInfoInsert(level_1, level_2, level_3, level_4,level_5,pk_num);
	}

	@Override
	public String downMLGroupInfo(String flag, String codeNm) throws Exception {

		String excelTemplatefile = propertyService.getString("excelTemplatefile");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();

//		sheet.setColumnWidth(0, 10000); //(n번째,컬럼너비값)
		sheet.setColumnWidth(0, 10000);
		sheet.setColumnWidth(1, 10000);
//		sheet.setColumnWidth(3, 10000);

		XSSFRow row = sheet.createRow(0); //첫번째 row
		XSSFCell cell;
//		cell = row.createCell(0);
//		cell.setCellValue("CodeName");
//		cell = row.createCell(1);
//		cell.setCellValue("CodeStrName(TYPE)");
		cell = row.createCell(0);
		cell.setCellValue("Product Group명");
		cell = row.createCell(1);
		cell.setCellValue("Product Name");

		int startRow = 1;

		List<Map<String, Object>> downMLGroupInfoList = adminMapper.downMLGroupInfo(codeNm); //DATA 불러오기
		//System.out.println("downMLGroupInfoList::::"+downMLGroupInfoList);


		String codeName,codeStrName,productGroup,productName = "";
		for(Map<String,Object> downMLGroupInfoData : downMLGroupInfoList){

//			codeName = String.valueOf(downMLGroupInfoData.get("CODENAME"));
//			codeStrName = String.valueOf(downMLGroupInfoData.get("CODETYPE"));
			productGroup = String.valueOf(downMLGroupInfoData.get("PRODUCTGROUP"));
			productName = String.valueOf(downMLGroupInfoData.get("PRODUCTNAME"));

			//System.out.println(level1+"::"+level2+"::"+level3);
			row = sheet.createRow(startRow);
//			cell = row.createCell(0);
//			cell.setCellValue(codeName);
//			cell = row.createCell(1);
//			cell.setCellValue(codeStrName);
			cell = row.createCell(0);
			cell.setCellValue(productGroup);
			cell = row.createCell(1);
			cell.setCellValue(productName);

			startRow++;
		}


		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm"); // 년월일

		Date date = new Date();
		String time1 = dateFormat.format(date);

		String fileName = time1+"_MLGroupInfo.xlsx";
		File xlsFile = new File(excelTemplatefile + File.separator +fileName);
		FileOutputStream fos = null;

		try {
            fos = new FileOutputStream(xlsFile);
            workbook.write(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(workbook!=null) workbook.close();
                if(fos!=null) fos.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

		return ""+fileName+"";
	}
	/**
	 *  ML 그룹정보 엑셀 데이터 Insert
	 * 엑셀 템플릿파일에서 입력인자 정보를 추출한다.
	 *
	 */
	@Override
	public List<Map<String,Object>> excelUploadProcess(String fileInfo, String userId,String flag) throws Exception{
	//public void excelUploadProcess(String fileInfo) throws Exception{
		//System.out.println("fileInfo::::::::::"+fileInfo);
		//System.out.println("flag::::::::::"+flag);
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = null;

		FileInputStream file = new FileInputStream( fileInfo);
		XSSFWorkbook workbook  = new XSSFWorkbook(file);
		try {
			if(flag.equals("mLGroupInfo")){
				adminMapper.deleteAllFeaCodeData(); // 엑셀 데이터 Insert 전 기존 테이블 데이터 삭제
				adminMapper.deleteAllFeaGrpInfoData(); // 엑셀 데이터 Insert 전 기존 테이블 데이터 삭제
				XSSFSheet sheet = workbook.getSheetAt(0); // 첫번째 Sheet

				for(int i = 1; i<sheet.getLastRowNum() +1; i++){
					XSSFRow row = sheet.getRow(i);
					 map = new HashMap<String, Object>();

					if(null == row){ //행이 존재하지 않으면 패스
						continue;
					}

					if(row.getCell(0).toString() == null || row.getCell(0).toString().equals("")){//엑셀데이터가없을시 브레이크
						break;
					}
					map.put("parentCode", row.getCell(0).toString());
					map.put("codeStr", row.getCell(2).toString());
					map.put("detailValue", row.getCell(3).toString());
					map.put("userId", userId);
					mapList.add(map);

				}
				System.out.println("mapList size :::::::::::"+mapList.size());
				List<String> as = new ArrayList<String>();
				List<Map<String,Object>> insertList= new ArrayList<Map<String,Object>>();

				int codeIndex = 1;
				for(Map<String, Object> data : mapList){
					String a = (String)data.get("codeStr");
					if(!as.contains(a)){
						as.add(a);
						data.put("feaCode", "FEA" +StringUtils.leftPad(codeIndex++ +"", 4, "0"));
						insertList.add(data);
					}
				}

				/*
				 * inserList를 100개씩 나누어서 TB_FEA_CODE 테이블에 Insert 하는 작업
				 * */
				double size = insertList.size();
				double len = (double) Math.round(size/100);
				int start = 0 ,end = 100;
				Map<String, Object> param = new HashMap<String, Object>();

				for(int j=1; j<=len; j++){
					List<Map<String, Object>> splitInserData   = insertList.subList(start, end);
					param.put("param", splitInserData);
					adminMapper.excelUploadinsertFeaCodeData(param);
					splitInserData = null;
					start =start + 100;
					if(j == len-1){
						end = insertList.size();
					}else{
						end = end + 100;
					}
				}



				////////////////////// tb_FEA_GRP_INFO TABEL INSERT
				//System.out.println("11111111:::"+map);
				//System.out.println("insertList::::"+insertList);
				//System.out.println("mapList::::"+mapList);

				//case2 TEST 해보기
				/*for(Map<String, Object> insermap1 : mapList){
					for(int i = 0; i<mapList.size(); i++){
						insermap1.get("codeStr").equals(mapList.get(i).get("codeStr"));
						insermap1.put("feaCode", mapList.get(i).get("codeStr"));
					}

				}*/

				for(int i=0; i<mapList.size(); i++){ //661 //mapList
					for(int j =0; j<insertList.size(); j++){ //1560? mapList.size()
						if(mapList.get(i).get("codeStr").equals(insertList.get(j).get("codeStr"))){//
							mapList.get(i).put("feaCode", insertList.get(j).get("feaCode"));
						}
					}
				} //TODO 소스 코드 정리하기 case2로는 테스트 해보기!
				double size2 = mapList.size();
				double len2 = (double) Math.ceil(size2/100);
				int start2 = 0 ,end2 = 100;
				Map<String, Object> param2 = new HashMap<String, Object>();

				for(int j=1; j<=len2; j++){
					List<Map<String, Object>> splitInserData2   = mapList.subList(start2, end2);
					param2.put("param", splitInserData2);
					adminMapper.excelUploadinsertFeaGrpInfoData(param2);
					splitInserData2 = null;
					start2 =start2 + 100;
					if(j == len2-1){
						end2 = mapList.size();
					}else{
						end2= end2 + 100;
					}
				}
				/////////////////////





			}else if(flag.equals("hiearInfo")){
				adminMapper.deleteHierarchyCodes(); // 엑셀 데이터 Insert 전 기존 테이블 데이터 삭제
				XSSFSheet sheet = workbook.getSheetAt(0); // 첫번째 Sheet
				XSSFCell cell;
				for(int i = 1; i<sheet.getLastRowNum() +1; i++){
					XSSFRow row = sheet.getRow(i);
					 map = new HashMap<String, Object>();
					//행이 존재하지 않으면 패스
					if(null == row){
						continue;
					}
					int pk_num = i;
					//map.put("level_1", row.getCell(0).toString());
					//map.put("level_2", row.getCell(1).toString());
					//map.put("level_3", row.getCell(2).toString());
					map.put("level_1", row.getCell(0)==null?"":row.getCell(0).toString());
					map.put("level_2", row.getCell(1)==null?"":row.getCell(1).toString());
					map.put("level_3", row.getCell(2)==null?"":row.getCell(2).toString());
					map.put("pk_num", pk_num);

					mapList.add(map);
				}
				Map<String, Object> param = new HashMap<String, Object>();
				param.put("param", mapList);
				adminMapper.excelUploadinsertHierarchyData(param);
			}


			//TB_FEA_CODE 테이블 INSERT
			/*String lastCodeStr = adminMapper.getFeaLastCodeName();
			// 다음 채번


			//TB_FEA_GRP_INFO 테이블 INSERT //TODO 이것먼저 진행하기
			/*String codeValue = (String) param.get("codeValue");
			@SuppressWarnings("unchecked")
			Map<String, Object> getParentParams = (Map<String, Object>) param.get("parentData");
			String parentGrpCode = (String) getParentParams.get("PARENT_CODE");
			String parentFeaCode = (String) getParentParams.get("CODE_NAME");
			String userName = (String) param.get("userName");

			adminMapper.insertFeaGrpDetail(parentGrpCode, parentFeaCode, codeValue, userName);*/
		} catch (Exception e) {
			e.printStackTrace();
		}


		return mapList;

	}
	
	/**
	 *  ML 그룹정보 엑셀 데이터 확인용 upload and read
	 */
	@Override
	public List<Map<String,Object>> excelUploadProcessConf(String fileInfo, String selCode, String userId) throws Exception{
		List<Map<String,Object>> mapList = new ArrayList<Map<String,Object>>();
		Map<String, Object> map = null;
		
		FileInputStream file = new FileInputStream(fileInfo);
		XSSFWorkbook workbook  = new XSSFWorkbook(file);
		FormulaEvaluator formulaEval = workbook.getCreationHelper().createFormulaEvaluator();
		
		//Code Name 생성
		String codeNm = "";
		String codeNmTmp = "FEA"+selCode.substring(selCode.length()-2);
		try {
			XSSFSheet sheet = workbook.getSheetAt(0); // 첫번째 Sheet
			String groupStr = "";
			String bGroupStr = "";
			for(int i = 1; i<sheet.getLastRowNum() +1; i++){
				XSSFRow bfRow = sheet.getRow(i-1);
				XSSFRow row = sheet.getRow(i);
				map = new HashMap<String, Object>();
				
				if(null == row){ //행이 존재하지 않으면 패스
					continue;
				}
					
				if(row.getCell(0).toString() == null || row.getCell(0).toString().equals("")){//엑셀데이터가없을시 브레이크
					break;
				}
				CellValue evaluate = formulaEval.evaluate(row.getCell(0));
				CellValue bEvaluate = formulaEval.evaluate(bfRow.getCell(0));
				CellValue evaluate2 = formulaEval.evaluate(row.getCell(1));
				
				groupStr = evaluate.formatAsString().replaceAll("\"", "");
				bGroupStr = bEvaluate.formatAsString().replaceAll("\"", "");
						
				map.put("PRODUCT_GROUP", groupStr);
				map.put("PRODUCT_NAME", evaluate2.formatAsString().replaceAll("\"", ""));
				map.put("selCode", selCode);
				map.put("userId", userId);
				if(groupStr.equals(bGroupStr)){//엑셀데이터가없을시 브레이크
					map.put("codeNm", codeNm);
				}else{
					codeNm = codeNmTmp+String.format("%05d", i);
					map.put("codeNm", codeNm);
				}
				mapList.add(map);
			}
			List<String> as = new ArrayList<String>();
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mapList;
	}
	
	@Override
	public void savInfoProcess(List<Map<String, Object>> excelGridParam, String selCode) throws Exception {
		//데이터가 많아 100개씩 인서트
		double size = excelGridParam.size();
		double len = (double) Math.ceil(size/100);
		int cStart = 0 ,cEnd = 100;
		int start = 0 ,end = 100;
		
		//기존데이터 삭제 외래키 걸려서 code지우면 info도 같이 지워짐
		adminMapper.delFeaCode(selCode);
		
		//중복 코드 제거
		List<Map<String, Object>> codeList = new ArrayList<Map<String, Object>>();
		codeList.add(excelGridParam.get(0));
        for (int i = 1; i < excelGridParam.size(); i++) {
            if (!excelGridParam.get(i).get("codeNm").equals(excelGridParam.get(i-1).get("codeNm"))) {
            	codeList.add(excelGridParam.get(i));
            }
        }
        double cSize = codeList.size();
        double cLen = (double) Math.ceil(cSize/100);
        
		Map<String,Object> codeMap = new HashMap<String,Object>();
		for(int i=1; i<=cLen; i++){
			List<Map<String, Object>> splitInserData = codeList.subList(cStart, cEnd);
			codeMap.put("list", splitInserData);
			adminMapper.savFeaCode(codeMap);
			splitInserData = null;
			cStart = cStart + 100;
			if(i == cLen-1){
				cEnd = codeList.size();
			}else{
				cEnd= cEnd + 100;
			}
		}
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		for(int i=1; i<=len; i++){
			List<Map<String, Object>> splitInserData = excelGridParam.subList(start, end);
			paramMap.put("list", splitInserData);
			adminMapper.savGrpInfo(paramMap);
			splitInserData = null;
			start = start + 100;
			if(i == len-1){
				end = excelGridParam.size();
			}else{
				end= end + 100;
			}
		}
	}

	public static List<Map<String, Object>> removeDuplication(List<Map<String, Object>> mapList){
		List<Map<String,Object>> reList = mapList;
		String pk = "";
		int limit = 1;
		boolean isRemove = false;
		for(int j = reList.size()-1; j>=0; j--){ //1538
			isRemove = false;
			pk = (String) reList.get(j).get("codeStr"); //j 번째 codestr 값을 가져와 pk에 대입.
			for(int z=0; z<reList.size(); z++){
				if(reList.get(z).get("codeStr").equals(pk)){
					reList.remove(j);
					isRemove =true;
					break;
				}
			}
			if(!isRemove) limit ++;
		}

		System.out.println("중복제거후 mapList::::::::"+reList);
		return reList;
	}





	public static <T> Predicate<T> distinctByKey( Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
		}


	public String downHierarchyInfo(Map<String,Object> param,List<String> HierDataInfo) throws Exception {

		String excelTemplatefile = propertyService.getString("excelTemplatefile");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet();

		sheet.setColumnWidth(0, 10000); //(n번째,컬럼너비값)
		sheet.setColumnWidth(1, 10000);
		sheet.setColumnWidth(2, 10000);

		XSSFRow row = sheet.createRow(0); //첫번째 row
		XSSFCell cell;
		cell = row.createCell(0);
		cell.setCellValue("level_1");
		cell = row.createCell(1);
		cell.setCellValue("level_2");
		cell = row.createCell(2);
		cell.setCellValue("level_3");


		//row = sheet.createRow(1); //2번째 row. for문을 사용하여 데이터 넣기
		int startRow = 1;

		List<Map<String,Object>> HierDataInfoList = (List<Map<String,Object>>)param.get("HierDataInfo");
		//System.out.println("HierDataInfoList:::::"+HierDataInfoList);

		String level1,level2,level3 = "";
		for(Map<String,Object> HierDataList : HierDataInfoList){

			level1 = String.valueOf(HierDataList.get("LEVEL_1"));
			level2 = String.valueOf(HierDataList.get("LEVEL_2"));
			level3 = String.valueOf(HierDataList.get("LEVEL_3"));

			//System.out.println(level1+"::"+level2+"::"+level3);
			row = sheet.createRow(startRow);
			cell = row.createCell(0);
			cell.setCellValue(level1);
			cell = row.createCell(1);
			cell.setCellValue(level2);
			cell = row.createCell(2);
			cell.setCellValue(level3);

			startRow++;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm"); // 년월일

		Date date = new Date();
		String time1 = dateFormat.format(date);
		String fileName = time1+"_HierarchyInfo.xlsx";
		File xlsFile = new File(excelTemplatefile + File.separator +fileName);
		FileOutputStream fos = null;


		try {
            fos = new FileOutputStream(xlsFile);
            workbook.write(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(workbook!=null) workbook.close();
                if(fos!=null) fos.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
		//System.out.println("xlsFile::"+xlsFile);
		return ""+fileName+"";
	}

	/**
	 * 공통코드 조회
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getRuleList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> comCodeList = adminMapper.getRuleList(param);
		return comCodeList;
	}

	@Override
	public int getRuleCount(Map<String, Object> params) throws Exception {
		return adminMapper.getRuleCount(params);
	}

	/**
	 * rule base 규칙 저장
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@Override
	public void ruleListSave(List<Map<String, Object>> params)throws Exception{
		for(Map<String, Object> aMap : params) {
			if(aMap.containsKey("delChk") && aMap.get("delChk").equals("Y")) {
				adminMapper.ruleListDelete(aMap);
			}else {
				adminMapper.ruleListSave(aMap);
			}
		}
	}
	
	/**
	 * rule base pop 조회
	 * @since 2020.11.06
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getPopRuleList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> comCodeList = adminMapper.getPopRuleList(param);
		return comCodeList;
	}
	
	/**
	 * rule base pop 규칙 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */
	@Override
	public void ruleListPopSave(List<Map<String, Object>> params)throws Exception{
		for(Map<String, Object> aMap : params) {
			if(aMap.containsKey("delChk") && aMap.get("delChk").equals("Y")) {
				adminMapper.ruleListPopDelete(aMap);
			}else {
				System.out.println("saveMap");
				adminMapper.ruleListPopSave(aMap);
			}
		}
	}
	
	
	@Override
	public Map<String,Object> getUserInfo(String userId) throws Exception {
		return userSignUpMapper.getUserInfo(userId);
	}
	
	@Override
	public Map<String,Object> getManagerInfo(String userId) throws Exception {
		return adminMapper.getManagerInfo(userId);
	}
	
	/**
	 * Rule 표준정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getMgList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> mgList = adminMapper.getMgList(param);
		return mgList;
	}
	@Override
	public List<Map<String, Object>> getSowList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> sowList = adminMapper.getSowList(param);
		return sowList;
	}
	@Override
	public List<Map<String, Object>> getStList(Map<String, Object> param) throws Exception {
		List<Map<String, Object>> stList = adminMapper.getStList(param);
		return stList;
	}
	
	/**
	 * 가격정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getPriceInfoList() throws Exception {
		List<Map<String, Object>> result = adminMapper.getPriceInfoList();
		return result;
	}
	
	@Override
	public List<Map<String, Object>> getGrpList() throws Exception {
		List<Map<String, Object>> result = adminMapper.getGrpList();
		return result;
	}
	
	@Override
	public List<Map<String, Object>> getSealList(Map<String,Object> param) throws Exception {
		List<Map<String, Object>> result = adminMapper.getSealList(param);
		return result;
	}
	
	@Override
	public List<Map<String, Object>> getSheetList(Map<String,Object> param) throws Exception {
		List<Map<String, Object>> result = adminMapper.getSheetList(param);
		return result;
	}
	
	/**
	 * 가격정보 저장
	 * @since 2021.02.25
	 * @author 강전일
	 */
	@Override
	public void savPriceInfo(Map<String, Object> param) throws Exception {
		MultipartFile files	= (MultipartFile) param.get("files");
		String menuId 		= param.get("menuId")==null ? "" : param.get("menuId").toString(); 
		String fGroupId 	= param.get("fGroupId")==null ? "" : param.get("fGroupId").toString(); 
		String priceId 		= param.get("PRICE_ID")==null ? "" : param.get("PRICE_ID").toString(); 
		String sealType 	= param.get("SEAL_TYPE")==null ? "" : param.get("SEAL_TYPE").toString(); 
		String sheetNo 		= param.get("SHEET_NO")==null ? "" : param.get("SHEET_NO").toString(); 
		String user 		= param.get("USER_ID")==null ? "" : param.get("USER_ID").toString(); 
		
		Map<String, Object> result = fileService.upload(files, menuId, fGroupId);
		result.put("PRICE_ID", priceId);
		result.put("SEAL_TYPE", sealType);
		result.put("SHEET_NO", sheetNo);
		result.put("USER_ID", user);
		
		adminMapper.savPriceInfo(result);
	}
	
	@Override
	public void editPriceInfo(Map<String, Object> param) throws Exception {
		MultipartFile files	= (MultipartFile) param.get("files");
		String menuId 		= param.get("menuId")==null ? "" : param.get("menuId").toString(); 
		String fGroupId 	= param.get("fGroupId")==null ? "" : param.get("fGroupId").toString(); 
		String priceId 		= param.get("PRICE_ID")==null ? "" : param.get("PRICE_ID").toString(); 
		String sealType 	= param.get("SEAL_TYPE")==null ? "" : param.get("SEAL_TYPE").toString();
		String sheetNo 		= param.get("SHEET_NO")==null ? "" : param.get("SHEET_NO").toString(); 
		String user 		= param.get("USER_ID")==null ? "" : param.get("USER_ID").toString(); 
		String filePath 	= param.get("FILE_PATH")==null ? "" : param.get("FILE_PATH").toString(); 
		String fileName 	= param.get("FILE_NAME")==null ? "" : param.get("FILE_NAME").toString(); 
		Map<String, Object> result = new HashMap<>();
		if(files.equals("Y")){
		}else{
			//파일 업로드 후 경로 및 파일정보 가져옴
			fileService.deleteFile(filePath, fileName);
			result = fileService.upload(files, menuId, fGroupId);
		}
		result.put("PRICE_ID", priceId);
		result.put("SEAL_TYPE", sealType);
		result.put("SHEET_NO", sheetNo);
		result.put("USER_ID", user);
		adminMapper.editPriceInfo(result);
	}
	
	@Override
	public void noFile(Map<String, Object> param) throws Exception {
		String newYn = param.get("newYn").toString();
		if(newYn.equals("Y")){
			adminMapper.savSheet(param);
		}else{
			adminMapper.editSheet(param);
		}
	}
	
	@Override
	public void delPriceInfo(Map<String, Object> param) throws Exception {
		String filePath = param.get("filePath").toString(); 
		String fileName = param.get("fileName").toString();
		fileService.deleteFile(filePath, fileName);
		adminMapper.delPriceInfo(param);
	}
	
	@Override
	public void savGrp(Map<String, Object> param) throws Exception {
		adminMapper.savGrp(param);
	}
	
	@Override
	public void savSeal(Map<String, Object> param) throws Exception {
		adminMapper.savSeal(param);
	}
	
	@Override
	public void savSheet(Map<String, Object> param) throws Exception {
		MultipartFile files	= (MultipartFile) param.get("files");
		String menuId 		= param.get("menuId")==null ? "" : param.get("menuId").toString(); 
		String fGroupId 	= param.get("fGroupId")==null ? "" : param.get("fGroupId").toString(); 
		String grpId 		= param.get("GRP_ID")==null ? "" : param.get("GRP_ID").toString(); 
		String sheetNo 		= param.get("SHEET_NO")==null ? "" : param.get("SHEET_NO").toString(); 
		String user 		= param.get("USER_ID")==null ? "" : param.get("USER_ID").toString(); 
		
		Map<String, Object> result = fileService.upload(files, menuId, fGroupId);
		result.put("GRP_ID", grpId);
		result.put("SHEET_NO", sheetNo);
		result.put("USER_ID", user);
		
		adminMapper.savSheet(result);
	}
	
	@Override
	public void editGrp(Map<String, Object> param) throws Exception {
		adminMapper.editGrp(param);
	}
	
	@Override
	public void editSheet(Map<String, Object> param) throws Exception {
		MultipartFile files	= (MultipartFile) param.get("files");
		String menuId 		= param.get("menuId")==null ? "" : param.get("menuId").toString(); 
		String fGroupId 	= param.get("fGroupId")==null ? "" : param.get("fGroupId").toString(); 
		String grpId 		= param.get("GRP_ID")==null ? "" : param.get("GRP_ID").toString(); 
		String sheetNo 		= param.get("SHEET_NO")==null ? "" : param.get("SHEET_NO").toString(); 
		String user 		= param.get("USER_ID")==null ? "" : param.get("USER_ID").toString(); 
		String filePath 	= param.get("FILE_PATH")==null ? "" : param.get("FILE_PATH").toString(); 
		String fileName 	= param.get("FILE_NAME")==null ? "" : param.get("FILE_NAME").toString(); 
		Map<String, Object> result = new HashMap<>();
		if(files.equals("Y")){
		}else{
			//파일 업로드 후 경로 및 파일정보 가져옴
			fileService.deleteFile(filePath, fileName);
			result = fileService.upload(files, menuId, fGroupId);
		}
		result.put("GRP_ID", grpId);
		result.put("SHEET_NO", sheetNo);
		result.put("USER_ID", user);
		adminMapper.editSheet(result);
	}
	
	@Override
	public void delGrp(Map<String, Object> param) throws Exception {
		adminMapper.delGrp(param);
		adminMapper.delSealAll(param);
		List<Map<String, Object>> result = adminMapper.getDelSheetList(param);
		for(Map<String, Object> dmap : result){
			fileService.deleteFile(dmap.get("FILE_PATH").toString(), dmap.get("FILE_NM").toString());
		}
		adminMapper.delSheetAll(param);
	}
	
	@Override
	public void delSeal(Map<String, Object> param) throws Exception {
		adminMapper.delSeal(param);
	}
	
	@Override
	public void delSheet(Map<String, Object> param) throws Exception {
		String filePath = param.get("filePath").toString(); 
		String fileName = param.get("fileName").toString();
		fileService.deleteFile(filePath, fileName);
		adminMapper.delSheet(param);
	}
	
	/**
	 * seal type list 조회
	 * @since 2021.03.11
	 * @author 강전일
	 */
	@Override
	public List<Map<String, Object>> getSealTypeList(Map<String,Object> param) throws Exception {
		List<Map<String,Object>> paramList = new ArrayList<Map<String,Object>>();
		Map<String,Object> paramMap = new HashMap<String,Object>();
		String sealType = param.get("fSealType").toString();
		String[] sealTypes = sealType.split("/");
		for(int i=0; i<sealTypes.length; i++){
			Map<String,Object> dMap = new HashMap<String,Object>();
			dMap.put("sealType", sealTypes[i]);
			paramList.add(dMap);
		}
		paramMap.put("list", paramList);
		List<Map<String, Object>> result = adminMapper.getSealTypeList(paramMap);
		return result;
	}	
}

