package com.levware.user.service.impl;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
/**
 * OLAP Application Data API UserDataAPIServiceImpl
 * <p><b>NOTE:</b> 
 *  User가 활용하는 Data를 API로 제어하는 ServiceImpl
 *  비즈니스 핵심 로직을 구현
 * @author 최진
 * @since 2019.03.04
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 * 수정일	수정자	수정내용
 * -------	--------	---------------------------
 * 2019.03.04	최 진	최초 생성
 *
 * </pre>
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.levware.common.mappers.mart.MartUserMapper;
import com.levware.common.mappers.repo.RepoUserDataAPIMapper;
import com.levware.user.service.OlapConditionVO;
import com.levware.user.service.OlapObjectDetailInfoVO;
import com.levware.user.service.OlapObjectRelVO;
import com.levware.user.service.OlapObjectVO;
import com.levware.user.service.OlapSavedDataDelVO;
import com.levware.user.service.OlapSavedDataVO;
import com.levware.user.service.OlapSelectObjectVO;
import com.levware.user.service.UserDataAPIService;

import egovframework.rte.fdl.property.EgovPropertyService;
import weka.core.Copyright;

@Service("UserDataAPIService")
public class UserDataAPIServiceImpl implements UserDataAPIService {

	public static Logger LOGGER = LogManager.getFormatterLogger(UserDataAPIServiceImpl.class);


	// properties
	@Resource(name="propertyService")
	protected EgovPropertyService propertyService;

	@Resource(name = "repoUserDataApiMapper")
	private RepoUserDataAPIMapper repoMapper;


	@Resource(name = "martUserMapper")
	private MartUserMapper martMapper;

	@Autowired
	@Resource(name="garakDataSqlSession")
	private DefaultSqlSessionFactory sqlSession;

	/**
	 * getObjectList
	 * 객체 정보 불러오기
	 * @return ArrayList<OlapObjectVO> 
	 */
	public List<OlapObjectVO> getObjectList() throws Exception {

		return repoMapper.getObjectSelect();

	}

	/**
	 * getObjectRelationInfo
	 * 객체 관계 정보 불러오기
	 * @param stdTableName 테이블 명
	 * @return Map<테이블 명, Array>
	 */
	@Override
	public Map<String, Object> getObjectRelationInfo(String stdTableName) throws Exception {
		Map<String, Object> rtnData = new HashMap<String, Object>();
		List<OlapObjectRelVO> objectRelValues = repoMapper.getObjectRelSelect(stdTableName);
		rtnData.put(stdTableName, objectRelValues);
		return rtnData;
	}

	/**
	 * getObjectDetailInfo
	 * 객체 세부 정보 불러오기
	 * @param tableName 테이블 명
	 * @return ArrayList<OlapObjectVO>
	 */
	@Override
	public List<OlapObjectDetailInfoVO> getObjectDetailInfo(String tableName) throws Exception {
		return repoMapper.getObjectDetailInfo(tableName);
	}



	/**
	 * 조회 조건 정보 불러오기
	 * @param tbNames 테이블 명 String Array
	 * @return
	 */
	@Override
	public Map<String, Object> getConditionData(String[] tbNames) throws Exception {
		Map<String,Object> rtnData = new HashMap<String, Object>();
		String tableName = "";
		if(tbNames.length > 0){
			int i = 0;
			for(i = 0; i < tbNames.length; i++){
				tableName = tbNames[i];
				if(tableName.equals("") == false){
					List<OlapConditionVO> tableConditionInfo = repoMapper.getConditionData(tableName);

					// 단위 변환을 위한 unit 관계정보 불러오기
					List<Map<String, Object>> unitTbRelInfo = repoMapper.getUnitRelInfo(tableName);
					rtnData.put(tableName, tableConditionInfo);
					rtnData.put("unit", unitTbRelInfo);
				}
			}
		}else{
			LOGGER.error("Not exist Parameter :: " + tbNames.length);
		}
		return rtnData;
	}



	/**
	 * @Class Name : LoginSuccessHandler.java
	 * @Description : LoginSuccessHandler Class
	 * @Modification Information
	 * @ @ 수정일 수정자 수정내용 @ --------- --------- ------------------------------- @
	 *   2019.03.06 최초생성
	 *
	 * @author 개발프레임웍크 실행환경 개발팀
	 * @since 2019.03.06
	 * @version 1.0
	 * @author 강전일
	 * @see
	 *
	 * 		Copyright (C) by MOPAS All right reserved.
	 */
	public void  logHisInsert(String userId) throws Exception {
		repoMapper.logHisInsert(userId);
	}

	/**
	 * 
	 * @param _colName
	 * @param calcStr
	 * @param transCode
	 * @param uCode
	 * @param value
	 * @return
	 */
	private String convertConCalcStr(String _colName, String calcStr, String transCode, String uCode,String value){
		// 이중 변환
		String rtnValue = "";
		String[] calcSet = calcStr.split(" ");

		LOGGER.info(calcStr);



		for(int j=2;j<calcSet.length;j++){
			String calcSetVal = calcSet[j];
			if(calcSetVal.contains("{"+uCode+"}")){
				// 값

				calcSet[j] = calcSetVal.replace("{"+uCode+"}", value);
				LOGGER.debug("{"+uCode+"}"+ " Found the variable.  changed the value.");
			}else if(calcSetVal.contains("[") && calcSetVal.contains("]")){
				// 컬럼
				LOGGER.debug(calcSetVal + " Found the Table Column.");
				int start = calcSetVal.indexOf("[")+1;
				int end = calcSetVal.indexOf("]"); 
				calcSetVal = calcSetVal.substring(start, end);


				String[] convertUnitParamSet = calcSetVal.split("[.]");
				if(convertUnitParamSet.length > 0){
					String tbName =convertUnitParamSet[0];
					String targetColName = convertUnitParamSet[1];
					String dyVal = "0";
					List<Map<String , Object>> convertVal = new ArrayList<Map<String , Object>>(); 
					if(tbName.contains("TB_")){
						convertVal = repoMapper.getUnitDyValue(tbName, uCode, targetColName, value);
					}else{
						convertVal = martMapper.getUnitDyValue(tbName, uCode, targetColName, value);

					}
					if(convertVal.size()>0){
						for(int k = 0;k<convertVal.size();k++){
							dyVal = String.valueOf(convertVal.get(k).get(targetColName));	
						}

					}
					calcSet[j] = dyVal;

					LOGGER.debug(calcSetVal + "  Table changed the column to a value.");

				}
			}else if(calcSetVal.contains("'") && calcSetVal.contains("'")){
				// 컬럼 참조
				String convertColName = calcSetVal.substring(1, calcSetVal.length()-1);
				String[] colStrSet = _colName.split("_");

				calcSetVal = convertColName +"_" +colStrSet[colStrSet.length-1];
				//varchar로 된 경우로 인한 처리
				calcSetVal = "cast(" + calcSetVal + " as decimal(18,10))";
				LOGGER.debug(calcSetVal+ " :: Gets a column value as a condition.");
				calcSet[j] =  calcSetVal;
			}

		}	

		StringBuilder rtnVal = new StringBuilder();
		for(String elem:calcSet){
			if(elem.length() >0){
				rtnVal.append(" ");
			}
			rtnVal.append(elem);
		}
		rtnValue = rtnVal.toString().trim();
		LOGGER.info(rtnValue);
		return rtnValue;
	}


	private List<String> popFirstString(String baseStr){
		List<String> rtnVal = new ArrayList<String>();
		String[] convertedCalcStrAry = baseStr.split(" ");
		String calcBaseVal = convertedCalcStrAry[0];
		StringBuilder joinedStr = new StringBuilder();
		for(int i =2; i <convertedCalcStrAry.length;i++){
			String elem = convertedCalcStrAry[i];
			if(elem.length() > 0){
				joinedStr.append(" ");
			}
			joinedStr.append(elem);
		}
		baseStr = joinedStr.toString().trim();
		rtnVal.add(calcBaseVal.substring(1, calcBaseVal.length()-1));
		rtnVal.add(baseStr);
		return rtnVal;
	}

	/**
	 * 단위 변환 로직
	 * @param grpCode  그룹 코드
	 * @param uCode   유닛코드
	 * @param value    값 
	 * @param _colName  컬럼명
	 * @param _tableName 테이블명
	 * @return
	 * @throws Exception
	 */
	public String convertUnitCalc(String grpCode, String uCode, String value, String _colName, String _tableName) throws Exception{
		String rtnValue = "";
		LOGGER.debug("unitSet");
		// 단위 변환을 위한 unit 관계정보 불러오기
		List<Map<String, Object>> unitTbRelInfo = repoMapper.getUnitRelInfo(_tableName);
		String baseColUnit = "";
		for(int a = 0;a < unitTbRelInfo.size();a++){
			if(_colName.equals(unitTbRelInfo.get(a).get("COL_NAME"))){
				baseColUnit = (String) unitTbRelInfo.get(a).get("BASE_COL"); // 현 DB에 저장되어 있는 데이터 단위
			}
		}

		
		if(baseColUnit.equals(uCode)){
			// 유저가 선택한 unitCode와 동일하면 
			rtnValue = value;
		}else{
			String calcStr = "", transCode = ""; boolean isPlay = false;
			List<Map<String,Object>> unitSet = repoMapper.getUnitCalc(grpCode);
			for(int i =0;i<unitSet.size();i++){
				String getGrpCode = (String) unitSet.get(i).get("GRP_CODE");
				String getUnitCode = (String) unitSet.get(i).get("UNIT_CODE");
				transCode = (String) unitSet.get(i).get("TRANS_CODE");
				if(unitSet.get(i).get("CALC_STR") != null &&
						getUnitCode.equals(uCode) && 	// 유저가 선택한 코드와 unitCode가 같고
						transCode.equals(baseColUnit) && // targetCode와 관계 베이스 코드가 같으면 
						grpCode.equals(getGrpCode)){  // groupCode도 서로 같아야 함.
					calcStr = (String) unitSet.get(i).get("CALC_STR");
					isPlay = true;
				}
			}
			
			if(isPlay){
				String convertedCalcStr = convertConCalcStr(_colName, calcStr, transCode, uCode,value);
				List<String> resultStrSet = popFirstString(convertedCalcStr);
				rtnValue  = resultStrSet.get(1);
			}else{
				throw new RuntimeException("단위변환정보가 존재하지 않습니다.");
			}
		}
		return rtnValue;
	}

	/**
	 * @Class Name : UserDataAPIController.java
	 * @Description : 동적쿼리 생성 및 그리드 view용 데이터 리턴
	 * @Modification Information
	 * @ @ 수정일 수정자 수정내용 
	 * 
	 *   2019.03.18 최초생성
	 *
	 * @author Jin. Choi.
	 * @since 2019.03.18
	 * @version 1.0
	 * @author Jin. Choi.
	 * @see
	 *
	 * 		Copyright (C) by MOPAS All right reserved.
	 */
	@Override
	public Map<String, Object> getSelectGridData(User user, OlapSelectObjectVO params) throws Exception{
		// Valid Check
		LOGGER.debug(params.toString());

		// 다중 테이블 용
		//		String standardDateCol =  params.getTbName01();
		//		String getStandardDateCol = repoMapper.getStandDateCol(params.getTbName01());
		//		
		//		if(getStandardDateCol != null && !getStandardDateCol.equals("")){
		//			standardDateCol +=  "."+getStandardDateCol;
		//			params.setStandardDateCol(standardDateCol);
		//		}

		String defaultTableName = propertyService.getString("defaultTable");
		LOGGER.debug("default Talble Name : " +defaultTableName);
		params.setTbName01(defaultTableName);

		List<Map<String, Object>> conditionObj = params.getCondition();  		// Like1, Like2 like 가공처리.
		System.out.println("conditionObj : "  + conditionObj);
		
		// Group 필드 코드 변수
		String sGrpCode = null;
		// Group 필드 명
		String sGrpColumn = null;
		
		if(conditionObj != null && conditionObj.size() > 0){
			for(int i=0;i<conditionObj.size();i++){
				Map<String, Object> changedVal = conditionObj.get(i);
				String operVal = (String) changedVal.get("value");
				@SuppressWarnings("unchecked")
				Map<String, String> unitValue = (Map<String, String>) changedVal.get("unitOption"); // 단위정보 - 단위변환을 위한
				System.out.println("unitValue : " + unitValue);
				
				// Group 필드 조건에 대한 예외처리 - 2021.1.11
				if ( changedVal.get("columnName") != null && (
						changedVal.get("columnName").equals("PRODUCT") ||
						changedVal.get("columnName").equals("PUMP_TYPE") ||
						changedVal.get("columnName").equals("EQUIP_MFG") ||
						changedVal.get("columnName").equals("ULTIMATE_USER") 
						)) {
					
					/*
					GRML0001	Product
					GRML0002	Pump Type
					GRML0003	Equip Mfg
					GRML0004	Ulitmate User
					*/
					if (changedVal.get("columnName").equals("PRODUCT")) {
						sGrpCode = "GRML0001";
						sGrpColumn = "PRODUCT_G";
					}else if (changedVal.get("columnName").equals("PUMP_TYPE")) {
						sGrpCode = "GRML0002";
						sGrpColumn = "PUMP_TYPE_G";
					}else if (changedVal.get("columnName").equals("EQUIP_MFG")) {
						sGrpCode = "GRML0003";
						sGrpColumn = "EQUIP_MFG_G";
					}else if (changedVal.get("columnName").equals("ULTIMATE_USER")) {
						sGrpCode = "GRML0004";
						sGrpColumn = "ULTIMATE_USER_G";
					}
					
					System.out.println("changedVal : " + changedVal.get("value"));
					
					// --------------------------------
					// 같은 조건에서 추가 사용
					// --------------------------------
					
					
					String sGrpData = "";	
					if (conditionObj.get(i).get("operSym").equals("=")) { // 같은조건	
						String grpCode = sGrpCode;
						String detailValue = String.valueOf(changedVal.get("value"));
						
						List<Map<String, Object>> selectGroupData = martMapper.getGroupData(grpCode, detailValue);
						
						if (selectGroupData.size() > 0) {
							sGrpData = String.valueOf(((HashMap<String,Object>)selectGroupData.get(0)).get("CODE_VALUE"));
						}
					}
					
					String sProductExpQry = " or ( "
							+	"M_DATA_CNV1." + sGrpColumn +" = '" + sGrpData + "') ";
					
					/*
					String sProductExpQry = " or ( "
							+	"M_DATA_CNV1." + sGrpColumn +" = "
							+	" (select CODE_VALUE from TB_FEA_CODE"  
							+ 	"  where PARENT_CODE='"+sGrpCode+"' "
							+	"  and CODE_NAME = "  
							+ 	"   (select FEA_CODE from TB_FEA_GRP_INFO " 
							+ 	"    where DETAIL_VALUES = '"+changedVal.get("value")+"')"
							+ 	"  )"
							+ 	")";
					*/
					//M_DATA_CNV1_GRP_INFO에 그룹명이 있고 
					//M_DATA_CNV1_GRP_INFO에 한건의 정보만 있는 경우
					/*
					String sProductExpQry = " "
							+ "("
							+ 	"( select count(*) _cnt from M_DATA_CNV1_GRP_INFO" 
							+ 	"  where M_DATA_CNV1_GRP_INFO.DWG_NO=M_DATA_CNV1.DWG_NO"  
							+ 	"  and M_DATA_CNV1_GRP_INFO.DWG_REV=M_DATA_CNV1.DWG_REV"  
							+ 	"  and M_DATA_CNV1_GRP_INFO.SHEET_NO=M_DATA_CNV1.SHEET_NO"   
							+ 	"  and M_DATA_CNV1_GRP_INFO.JOB_NO=M_DATA_CNV1.JOB_NO"
							+ 	"  and M_DATA_CNV1_GRP_INFO.GRP_TYPE='"+changedVal.get("columnName")+"' "
							+ 	"  and M_DATA_CNV1_GRP_INFO.grp = (select CODE_VALUE from TB_FEA_CODE"  
							+ 	"                                  where PARENT_CODE='"+sGrpCode+"'  and CODE_NAME = "  
							+ 	"											  (select FEA_CODE from TB_FEA_GRP_INFO " 
							+ 	"											   where DETAIL_VALUES = '"+changedVal.get("value")+"')"
							+ 	"		                         ) ) = 1"
							+ 	"AND "
							+ 	"(	select count(*) _cnt from M_DATA_CNV1_GRP_INFO "
						    + 	"	where M_DATA_CNV1_GRP_INFO.DWG_NO=M_DATA_CNV1.DWG_NO"  
						  	+ 	"	and M_DATA_CNV1_GRP_INFO.DWG_REV=M_DATA_CNV1.DWG_REV "  
						  	+ 	"	and M_DATA_CNV1_GRP_INFO.SHEET_NO=M_DATA_CNV1.SHEET_NO "  
						  	+ 	"	and M_DATA_CNV1_GRP_INFO.JOB_NO=M_DATA_CNV1.JOB_NO"   
						  	+ 	"	and M_DATA_CNV1_GRP_INFO.GRP_TYPE='"+changedVal.get("columnName")+"') = 1"
							+ ")";
					*/
					
					// --------------------------------
					// 포함조건에 추가 사용
					// --------------------------------
					String sProductExpQry_in = " or exists  "
							  +" (select * from M_DATA_CNV1_GRP_INFO "
							  +"  where M_DATA_CNV1_GRP_INFO.DWG_NO=M_DATA_CNV1.DWG_NO " 
							  +"	and M_DATA_CNV1_GRP_INFO.DWG_REV=M_DATA_CNV1.DWG_REV "  
							  +"	and M_DATA_CNV1_GRP_INFO.SHEET_NO=M_DATA_CNV1.SHEET_NO "  
							  +"	and M_DATA_CNV1_GRP_INFO.JOB_NO=M_DATA_CNV1.JOB_NO "  
							  +"	and M_DATA_CNV1_GRP_INFO.GRP_TYPE='"+changedVal.get("columnName")+"' "  
							  +"	and M_DATA_CNV1_GRP_INFO.grp in ( select CODE_VALUE from TB_FEA_CODE  "  
							  +"							     	  							where PARENT_CODE='"+sGrpCode+"'  and CODE_NAME = "  
							  +"							     	  										( select FEA_CODE from TB_FEA_GRP_INFO "  
							  +"							     	  											where DETAIL_VALUES = '"+changedVal.get("value")+"') "  
							  +"							     	  						             ) "  
							  +"	) ";
					
					// --------------------------------
					// 포함하지않는 조건에 추가 사용
					// --------------------------------
					String sProductExpQry_not_in = " and not exists  "
							  +" (select * from M_DATA_CNV1_GRP_INFO "
							  +"  where M_DATA_CNV1_GRP_INFO.DWG_NO=M_DATA_CNV1.DWG_NO " 
							  +"	and M_DATA_CNV1_GRP_INFO.DWG_REV=M_DATA_CNV1.DWG_REV "  
							  +"	and M_DATA_CNV1_GRP_INFO.SHEET_NO=M_DATA_CNV1.SHEET_NO "  
							  +"	and M_DATA_CNV1_GRP_INFO.JOB_NO=M_DATA_CNV1.JOB_NO "  
							  +"	and M_DATA_CNV1_GRP_INFO.GRP_TYPE='"+changedVal.get("columnName")+"' "  
							  +"	and M_DATA_CNV1_GRP_INFO.grp in ( select CODE_VALUE from TB_FEA_CODE  "  
							  +"							     	  							where PARENT_CODE='"+sGrpCode+"'  and CODE_NAME = "  
							  +"							     	  										( select FEA_CODE from TB_FEA_GRP_INFO "  
							  +"							     	  											where DETAIL_VALUES = '"+changedVal.get("value")+"') "  
							  +"							     	  						             ) "  
							  +"	) ";
					
					//조건 추가정보
					//changedVal.put("expCond", sProductExpQry);
					if(conditionObj.get(i).get("operSym").equals("LIKE")) { // 포함조건
						changedVal.put("expCond", sProductExpQry_in);
					}else if (conditionObj.get(i).get("operSym").equals("NOT LIKE")) { // 포함하지않는 조건
						changedVal.put("expCond", sProductExpQry_not_in);
					}else if (conditionObj.get(i).get("operSym").equals("=")) { // 같은조건
						changedVal.put("expCond", sProductExpQry);
					}
				}
					
					
				// 단위정보를 가지고 있는 필드일 경우 처리
				if(unitValue != null){  
					LOGGER.debug(unitValue.get("UNIT_CODE")); 
					String grpCode = unitValue.get("GRP_CODE");
					String uCode = unitValue.get("UNIT_CODE");
					String _colName = (String) changedVal.get("columnName");
					operVal = convertUnitCalc(grpCode, uCode, operVal, _colName, defaultTableName); // 단위에 따른 수치 컨버전					
					LOGGER.debug("operVal", operVal);
					changedVal.put("value", operVal);
					
					// 2021.01.11
					// 단위와 같이 처리되는 필드는 기준데이터로 들어가 있는  필드와 쌍으로 구성되어 있음.
					// ex) 온도 Nor   => TEMP_NOR   TEMP_NOR_C
					// 그래서 조회 시에는 필드명에 "_C"를 붙여서 검색하고 결과는"_C"를 뺀 필드(변경전 원래 데이터)를 조회함
					changedVal.put("columnName", String.valueOf(changedVal.get("columnName"))+"_C");
				}

				// Like 검색에 대한 추가처리
				if(conditionObj.get(i).get("operSym").equals("LIKE1")){ // 앞글자 Like
					changedVal.put("value", operVal+"%");
					changedVal.put("operSym", "LIKE");
				}else if(conditionObj.get(i).get("operSym").equals("LIKE2")){ // 뒤글자 Like
					changedVal.put("value", "%"+operVal);
					changedVal.put("operSym", "LIKE");
				}else if(conditionObj.get(i).get("operSym").equals("LIKE") || conditionObj.get(i).get("operSym").equals("NOT LIKE")){ // 포함하는 또는 같지않은
					changedVal.put("value", "%"+operVal+"%");
				}else{
					LOGGER.debug("continue ::  operSym" + conditionObj.get(i).get("operSym"));
				} // 그 외 조건은 건너뛴다
				
				// 설정된 조건 Set
				conditionObj.set(i, changedVal);
					
				
			}
		}
		
		
//		params.setExpCondition(expConditionObj);
//		
//		System.out.println(expConditionObj);
//		System.out.println(conditionObj);

		// 그룹함수 존재여부 검사 & alias setting
		List<Map<String, Object>> detailInfoObj = params.getDetailInfo();
		List<Map<String, Object>> setNotGroupFuncColList = new ArrayList<Map<String,Object>>();
		String tbN01 = params.getTbName01();
		if(detailInfoObj != null && detailInfoObj.size() > 0){
			for(int j = 0; j < detailInfoObj.size();j++){
				Map<String, Object> detailInfoVal = detailInfoObj.get(j);
				if(detailInfoVal.get("calcFunc") != null && !detailInfoVal.get("calcFunc").equals("")){
					params.setExistGroupFunc(true);
				}else{
					setNotGroupFuncColList.add(detailInfoVal);
				}

				String aliasName = "";
				String tbName = (String)detailInfoVal.get("tableName");
				String colName = (String)detailInfoVal.get("colName");
		/*		if(tbN01.equals(tbName)){
					aliasName ="F";	
				}else{
					aliasName ="S";
				}

				aliasName += colName;
				detailInfoVal.put("alias", aliasName);*/
				detailInfoVal.put("alias", colName);
				
				System.out.println("detailInfoVal : " + detailInfoVal);
				detailInfoObj.set(j, detailInfoVal);
			}
			params.setNotGroupCol(setNotGroupFuncColList);
			params.setDetailInfo(detailInfoObj);
		}


		// Grid 형식에 맞게 가공. 
		Map<String, Object> rtnData = new HashMap<String, Object>();

		LOGGER.debug(params.toString());
		LOGGER.debug(params);
		// mapper 호출
		List<Map<String, Object>> selectData = martMapper.getSelectGridData(params);
		List<Map<String,Object>> columnInfo = params.getDetailInfo();

		List<Map<String,Object>> setColumnFields = new ArrayList<Map<String, Object>>(); 
		for(int k=0;k<columnInfo.size();k++){
			Map<String,Object> setVal = new HashMap<String,Object>();
			setVal.put("name", columnInfo.get(k).get("alias"));
			setVal.put("title", columnInfo.get(k).get("objInfoName"));

			if(columnInfo.get(k).get("dataType").equals("문자")){
				setVal.put("type", "text");	
			}else{
				// 그외의 경우의 수는 숫자
				// 날짜가 들어올 수도 있음.
				setVal.put("type", "number");	
			}
			setColumnFields.add(setVal);
		}
		//		LOGGER.debug(selectData);



		rtnData.put("fields", setColumnFields);
		rtnData.put("records", selectData);

		insertSelectHistory(user, params);


		return rtnData;
	}


	/**
	 * 쿼리 조회 이력 남기기
	 * @param user
	 * @param param
	 */
	private void insertSelectHistory(User user, OlapSelectObjectVO param) throws Exception{
		Map<String, Object> setData = new HashMap<String, Object>();
		String userName = user.getUsername();
		String qryStr = getSelectSqlStr("getSelectGridData", param);



		int qrySeq = repoMapper.getCountQryHistSeq(userName);
		qrySeq++;
		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat tmFormat = new SimpleDateFormat("HHmmss");
		Date time = new Date();
		String ctDt = dtFormat.format(time);
		String ctTm = tmFormat.format(time);
		qryStr = qryStr.replace("\n", "").replace("\r","");
		qryStr =qryStr.replaceAll(System.getProperty("line.separator"), " ");
		qryStr =qryStr.replaceAll(" 		 			  					", " ");
		qryStr =qryStr.replaceAll("				 					 			 , 					", ", ");
		qryStr =qryStr.replaceAll("		 			, ", " , ");
		qryStr =qryStr.replaceAll("				,					", "  , ");
		qryStr =qryStr.replaceAll("																  , 		", "  , ");
		qryStr =qryStr.replaceAll("		 		 ", " ");
		qryStr =qryStr.replaceAll("				 					 		FROM			", " FROM ");
		qryStr =qryStr.replaceAll("				AND					", " AND ");
		qryStr =qryStr.replaceAll("AND 									", "AND ");
		qryStr =qryStr.replaceAll("			 AND ", " AND ");
		qryStr =qryStr.replaceAll("																	GROUP BY					", " GROUP BY ");
		qryStr =qryStr.replaceAll("																	ORDER BY																	", " ORDER BY ");

		qryStr = qryStr.trim();
		LOGGER.debug(qryStr);			
		setData.put("seqNum", qrySeq);
		setData.put("userName", userName);
		setData.put("qryStr", qryStr);
		setData.put("createDt",ctDt);
		setData.put("createTm",ctTm);

		repoMapper.inserSelectHistory(setData);

	}

	// query를 String Type으로 변환하는 메서드 
	private String getSelectSqlStr(String queryId, OlapSelectObjectVO sqlParam){
		BoundSql boundSql = sqlSession.getConfiguration().getMappedStatement(queryId).getSqlSource().getBoundSql(sqlParam);
		String query1 = boundSql.getSql();

		Object paramObj = boundSql.getParameterObject();

		if(paramObj != null){              // 파라미터가 아무것도 없을 경우
			List<ParameterMapping> paramMapping = boundSql.getParameterMappings();
			for(ParameterMapping mapping : paramMapping){
				Object propValue = mapping.getProperty();

				query1=query1.replaceFirst("\\?", "#{"+propValue+"}");
				if(propValue.toString().equals("startYearStr")){
					query1 = query1.replace("#{"+propValue.toString()+"}", "'"+sqlParam.getStartYearStr()+"'");	
				}else if(propValue.toString().equals("startMonthNDateStr")){
					query1 = query1.replace("#{"+propValue.toString()+"}", "'"+sqlParam.getStartMonthNDateStr()+"'");	
				}else if(propValue.toString().equals("endYearStr")){
					query1 = query1.replace("#{"+propValue.toString()+"}", "'"+sqlParam.getEndYearStr()+"'");	
				}else if(propValue.toString().equals("endMonthNDateStr")){
					query1 = query1.replace("#{"+propValue.toString()+"}", "'"+sqlParam.getEndMonthNDateStr()+"'");	
				}else{
					query1 = query1.replace("#{"+propValue.toString()+"}", "'"+boundSql.getAdditionalParameter(mapping.getProperty())+"'");
				}

			}
			// Gets the SQL and parameters.

			query1 = query1.trim();			
		}
		return query1; 		


	}

	/**
	 * @Class Name : UserDataAPIController.java
	 * @Description : 유저가 설정한 정보 내역을 insert/update
	 * @Modification Information
	 * @ @ 수정일 수정자 수정내용 
	 * 
	 *   2019.04.03 최초생성
	 *
	 * @author Jin. Choi.
	 * @since 2019.04.03
	 * @version 1.0
	 * @author Jin. Choi.
	 * @see
	 *
	 * 		Copyright (C) by Levware All right reserved.
	 */
	@Override
	public void insertNUpdateUserDataset(Map<String, Object> params) throws Exception {
		User user = (User) params.get("userInfo");
		String userName = user.getUsername();

		// insert
		Map<String, Object> setData = new HashMap<String, Object>();

		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat tmFormat = new SimpleDateFormat("HHmmss");
		Date time = new Date();
		String ctDt = dtFormat.format(time);
		String ctTm = tmFormat.format(time);

		setData.put("userId", userName);
		setData.put("title", params.get("title"));
		setData.put("description", params.get("description"));
		setData.put("qryText", params.get("data"));
		LOGGER.debug(params.toString());
		if(params.containsKey("seqNum")){
			//update
			setData.put("seqCnt", params.get("seqNum"));
			setData.put("updateDt", ctDt);
			setData.put("updateTm", ctTm);
			repoMapper.updateUserDataset(setData);
		}else{
			//insert
			int seqCnt =  repoMapper.getCountUserSaveSeq(userName);

			seqCnt++;
			setData.put("seqCnt", seqCnt);
			setData.put("createDt", ctDt);
			setData.put("createTm", ctTm);
			repoMapper.insertUserDataset(setData);
		}
	}

	/**
	 * @Class Name : UserDataAPIController.java
	 * @Description : 유저가 저장한 저장목록 불러오기
	 * @Modification Information
	 * @ @ 수정일 수정자 수정내용 
	 * 
	 *   2019.04.03 최초생성
	 *
	 * @author Jin. Choi.
	 * @since 2019.04.03
	 * @version 1.0
	 * @author Jin. Choi.
	 * @throws Exception 
	 * @see
	 *
	 * 		Copyright (C) by Levware All right reserved.
	 */
	@Override
	public List<OlapSavedDataVO> getSelectUserSavedDataSetList(User user) throws Exception {
		String userName = user.getUsername();
		return repoMapper.getUserSavedDataList(userName);
	}

	/**
	 * @Class Name : UserDataAPIController.java
	 * @Description : 유저가 저장한 저장목록 삭제하기
	 * @Modification Information
	 * @ @ 수정일 수정자 수정내용 
	 * 
	 *   2019.04.03 최초생성
	 *
	 * @author Jin. Choi.
	 * @since 2019.04.05
	 * @version 1.0
	 * @author Jin. Choi.
	 * @throws Exception 
	 * @see
	 *
	 * 		Copyright (C) by Levware All right reserved.
	 */
	@Override
	public void deleteUserDataset(User user, int seqNum) throws Exception {
		String userName = user.getUsername();

		OlapSavedDataDelVO setData = new OlapSavedDataDelVO();
		setData.setUserName(userName);
		setData.setSeqNum(seqNum);
		repoMapper.deleteUserDataset(setData);
	}

	/**
	 * 선택한 조건의 셀렉트 리스트 호출
	 * @param tableName 테이블 명 ,columnName 컬럼 명
	 * @return 
	 */
	@Override
	public  List<Map<String, Object>> getSelectList(String tableName,String columnName) throws Exception {
		return repoMapper.getSelectList(tableName,columnName);
	}

	/**
	 * 데이터 단위 리스트 호출
	 */
	@Override 
	public List<Map<String, Object>> getUnitList() throws Exception{
		String unitParam = "GRU%";

		return repoMapper.getUnitList(unitParam);
	}


	@Override
	public List<Map<String, Object>> getSelectASHistoryGridData(String bomId) throws Exception{
		return repoMapper.getSelectASHistoryGridData(bomId);
	}
}
