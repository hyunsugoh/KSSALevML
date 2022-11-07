package com.levware.admin.web;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * OLAP Application의 Admin View Controller
 * <p><b>NOTE:</b>
 *  Admin View Page Controller
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
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.levware.admin.service.AdminObjectListVO;
import com.levware.admin.service.AdminService;
import com.levware.admin.service.CodeManagementVO;



/**
 * @Class Name : AdminSampleController.java
 * @Description : Admin Sample Controller Class
 * @Modification Information
 * @ @ 수정일 수정자 수정내용 @ --------- --------- ------------------------------- @
 *   2019.02.13 최초생성
 *
 * @author 개발프레임웍크 실행환경 개발팀
 * @since 2019. 02.13
 * @version 1.0
 * @author 최진
 * @see
 *
 * 		Copyright (C) by MOPAS All right reserved.
 */
@Controller
@RequestMapping(value="/admin", method = RequestMethod.GET)
public class AdminController {
	private static final Logger LOGGER = LogManager.getLogger(AdminController.class);
	private static String profile=System.getProperty("server.os.profile");

	@Resource(name = "AdminService")
	private AdminService adminService;



	@RequestMapping(value = "/AdminObjectList", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<AdminObjectListVO> adminObjectList(HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
		LOGGER.debug("call adminObjectList");
        List<AdminObjectListVO> ObjectList =  adminService.getAdminObjectList();
		return ObjectList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}



	/**
	 * 코드관리 select Method
	 * @since 2019.03.06
	 * @author 박수연
	 */
	@RequestMapping(value = "/getCodeManagementList", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<CodeManagementVO> getCodeManagementList(HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
		LOGGER.debug("call CodeManagementList");
		List<CodeManagementVO> ObjectList =  adminService.getCodeManagementList();
		LOGGER.debug("CodeManagementList result ::" + ObjectList.toString());
		return ObjectList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}
	/**
	 * 코드관리 insert Method
	 * @since 2019.03.07
	 * @author 박수연
	 */
	@RequestMapping(value="/insertCodeManagement", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void insertCodeManagement(@RequestParam Map<String, String> map, Authentication authentication,HttpServletResponse response) throws Exception {

		try{
		LOGGER.debug("call insertCodeManagement");
		//로그온 유저 조회
		User user = (User) authentication.getPrincipal();
		String userId = user.getUsername();
		map.put("createId", userId);
		LOGGER.debug("call insertCodeManagement username :: " + userId);
		adminService.insertCodeManagement(map);
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		}
	}
	/**
	 * 코드관리 delete Method
	 * @since 2019.03.08
	 * @author 박수연
	 */
	@RequestMapping(value="/deleteCodeManagement", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void deleteCodeManagement(@RequestParam(value="checkCdArry[]") List<String> arr,HttpServletResponse response) throws Exception {

		try{
			LOGGER.debug("call deleteCodeManagement");
			LOGGER.debug("deleteCodeManagement deleteList :: " +arr.toString());
			adminService.deleteCodeManagement(arr);
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

		}
	}



	@RequestMapping(value = "/AdminObjectDelete", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectDelete(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectDelete");

			String table_name = (String) param.get("table_name");
			String activ_yn = (String) param.get("activ_yn");
			LOGGER.debug("adminObjectDelete param ","table_name :: "+table_name, "activ_yn"+activ_yn);
			int result = 0;

			switch(activ_yn){
			case "Y":
				result = -1;
				break;
			case "N":
				result= adminService.getAdminObjectDelete(table_name);
				break;
			}
			LOGGER.debug("adminObjectDelete result");
			LOGGER.debug(result);

			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}

	}




	/**
	 * adminObjectActive
	 * @since 2019.03.08
	 * @author 조형욱
	 */
	@RequestMapping(value = "/AdminObjectActive", headers = "Accept=*/*", method =   RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectActive(@RequestBody Map<String, String> param,Authentication authentication,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectActive");
			User user = (User) authentication.getPrincipal();
			String ID = user.getUsername();
			param.put("ID", ID);
			LOGGER.debug(param);

			int result = -1;

			String table_name = (String) param.get("table_name");

			LOGGER.debug("adminObjectActive param :: table_name : "+table_name);

			//선택한 행 없이 활성화 버튼 누를 경우
			if(table_name == null || table_name.length() == 0){

				result = -10;
				return result;

			}
			// 활성화 하기 전  객체명,객체정보명 등이 입력 됬는지 체크 하는 로직
			List<Map<String, Object>> check1= adminService.getAdminObjectActiveCheck1(param);

			List<Map<String, Object>> check2= adminService.getAdminObjectActiveCheck2(param);

			List<Map<String, Object>> check3= adminService.getAdminObjectActiveCheck3(param);

			LOGGER.debug("활성화 하기 전  객체명,객체정보명 등이 입력 됬는지 체크 check1 :: "+check1.toString());
			LOGGER.debug("활성화 하기 전  객체명,객체정보명 등이 입력 됬는지 체크 check2 :: "+check2.toString());
			LOGGER.debug("활성화 하기 전  객체명,객체정보명 등이 입력 됬는지 체크 check3 :: "+check3.toString());

			String activ_yn = (String) param.get("activ_yn");

			if(activ_yn.equals("N")){

				if(check1.isEmpty()||check1.get(0)==null){
					result = -2;
					return result;
				}else if(check2.isEmpty()||check2.get(0)==null){
					result = -3;
					return result;
				}else if(check3.isEmpty()||check3.get(0)==null){

					result = -4;

					return result;

				}else{
					result= adminService.getAdminObjectActive(param);
					return result;
				}
			}else{
				result= adminService.getAdminObjectActive(param);
				return result;
			}
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}
	}

	/**
	 * 객체 관리 -> 마트에서 객체리스트  select
	 * @since 2019.03.11
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectSelect", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> adminObjectSelect(HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectSelect");

			//repo에 추가된 테이블 리스트
			List<Map<String, Object>> tableList = adminService.getAdminObjectTableList();

			LOGGER.debug("adminObjectSelect repository added table list :: result : "+tableList.toString());

			// OBJECT_NAME:테이블 명 에서 테이블만 가져와서 리스트에 넣어준다
			List<String> tl = new ArrayList<String>();

			for(int i=0; i < tableList.size();i++){
				String List = tableList.get(i).get("table_name").toString();



				tl.add(List);
			}

			LOGGER.debug("Table OBJECT_NAME >> "+tl.toString());

			//마트의 테이블 리스트에서 repo에 추가된 테이블 리스트를 제외하고 display
			List<Map<String, Object>> object_select =  adminService.getAdminObjectSelect(tl);

			LOGGER.debug("adminObjectSelect response result :: "+object_select.toString());
			return object_select;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}


	/**
	 * 객체 관리 -> 마트에서 객체가져와서 repo에 인서트
	 * @since 2019.03.11
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectInsert", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectInsert(@RequestBody String object_name, Authentication authentication,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectInsert");

			User user = (User) authentication.getPrincipal();
			String ID = user.getUsername();

			LOGGER.debug("adminObjectInsert Param ",object_name,ID);


			int result =  adminService.setAdminObjectInsert(object_name,ID);
			LOGGER.debug(result);
			return result;

		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}
	}

	/**
	 * 객체 관리 업데이트
	 * @since 2019.03.15
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectUpdate", headers = "Accept=*/*", method =   RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectUpdate(@RequestBody Map<String, String> param, Authentication authentication,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectUpdate");
			User user = (User) authentication.getPrincipal();
			String ID = user.getUsername();
			param.put("ID", ID);
			LOGGER.debug("adminObjectUpdate param ",param);

			String activ_yn = (String) param.get("activ_yn");
			LOGGER.debug(activ_yn);
			int result = 0;

			switch(activ_yn){
			case "Y":
				result = -1;
				break;
			case "N":
				result= adminService.getAdminObjectUpdate(param);
				break;
			}

			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}

	}



	/**
	 * repo 객체정보관리 리스트
	 * @since 2019.03.18
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectInfoList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> adminObjectInfoList(@RequestBody Map<String, Object> args,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectInfoList");

			LOGGER.debug("adminObjectInfoList param ",args);

			String table_name = (String) args.get("table_name");
			String activ_yn = (String) args.get("activ_yn");

			LOGGER.debug("활성화 파라미터 ::"+activ_yn);

			//repo의 객체정보관리에서 정보를 가져오고 마트에서 컬럼 정보를 가져와서 합친다
			List<Map<String, Object>> InfoList = adminService.getAdminObjectInfoList(table_name);
			List<Map<String, Object>> RepoColList = adminService.getAdminObjectRepoColList(table_name);


			for(int i=0; i < InfoList.size();i++){

				for(int j=0; j < RepoColList.size();j++){

					if(InfoList.get(i).get("COL_NAME").equals(RepoColList.get(j).get("COLUMN_NAME"))){


						InfoList.get(i).putAll(RepoColList.get(j));
						RepoColList.remove(j);

					}

				}

			}


			LOGGER.debug(InfoList.toString());
			LOGGER.debug(RepoColList.toString());

			InfoList.addAll(RepoColList);

			//앞 화면에서 받아온 활성화 값을 모든 행에 넣어준다
			for(int i=0; i < InfoList.size();i++){

				InfoList.get(i).put("ACTIV_YN",activ_yn);


			}
			LOGGER.debug("객체정보 리스트 최종 "+InfoList);

			return InfoList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}

	}



	/**
	 * repo 객체정보관리 업데이트,인서트
	 * @since 2019.03.21
	 * @author 조형욱
	 */
	@RequestMapping(value = "/AdminObjectInfoUpdate", headers = "Accept=*/*", method =   RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectInfoUpdate(@RequestBody Map<String, String> param, Authentication authentication,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		LOGGER.debug(param);
		try{
			LOGGER.debug("call adminObjectInfoUpdate");

			User user = (User) authentication.getPrincipal();
			String ID = user.getUsername();
			param.put("ID", ID);

			int result = 0;
			//기준일자는 하나만 등록 해야된다 . 등록되 있는지 확인하고 등록되 있으면 리턴

			LOGGER.debug("기준일자 테스트 "+param);

			String TABLE_NAME = (String) param.get("TABLE_NAME");
			String COL_NAME = (String) param.get("COL_NAME");

			List<Map<String, String>>  dateCheck  = adminService.getAdminObjectInfoCheckDate(TABLE_NAME);

//			LOGGER.debug("기준일자 검증 "+dateCheck);

//			LOGGER.debug((String) param.get("STAND_DATE"));
//			String STAND_DATE = (String) param.get("STAND_DATE") != null ? (String) param.get("STAND_DATE")  : "";



			//기준일자가 Y인 로우를 편집하려고 할때  해당 테이블 의 기존 repo db에 저장된 정보를 가져와서 (기준일자,컬럼) 비교를 한다
//			if("Y".equals(STAND_DATE)){
//				for(int i=0;i<dateCheck.size();i++){
//					//편집하려는  테이블의 기존 repo db에 stand_date 가 Y인 값이 있고 그 값에 해당하는 컬럼 이름이 현재 수정하려하는 칼럼이음과 일치하지 않으면(새로 추가하는 것이라면) 리턴 시킨다
//					if(dateCheck.get(i).get("STAND_DATE").equals("Y")&&!dateCheck.get(i).get("COL_NAME").equals(COL_NAME)) {
//
//						result =-5;
//
//						return result;
//					}
//
//
//				}
//			}

			//LOGGER.info(param);


			//인서트 대상인지 업데이트 대상인지 구분
			List<Map<String, Object>> insertUpdate_Gubun = adminService.getAdminObjectInfoGubunInsertUpdate(TABLE_NAME,COL_NAME);
			if(insertUpdate_Gubun.isEmpty()){

				result= adminService.setAdminObjectInfoInsert(param);

				return result;

			}else{

				result= adminService.setAdminObjectInfoUpdate(param);

				return result;

			}

		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}
	}

	/**
	 * 객체 정보 관리 삭제
	 * @since 2019.03.22
	 * @author 조형욱
	 */
	@RequestMapping(value = "/AdminObjectInfoDelete", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectInfoDelete(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectInfoDelete");

			String TABLE_NAME = (String) param.get("TABLE_NAME");
			String COL_NAME = (String) param.get("COL_NAME");

			int result = 0;


			result= adminService.setAdminObjectInfoDelete(TABLE_NAME,COL_NAME);


			LOGGER.debug(result);

			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}

	}


	/**
	 * 객체 관계  관리 리스트(기준 테이블)
	 * @since 2019.03.28
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectRelStand", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> adminObjectRelStand(HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call AdminObjectRelStand");


			List<Map<String, Object>> List = adminService.getAdminObjectRelStand();


			return List;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}


	/**
	 * 객체 관계  관리 리스트(연결 테이블)
	 * @since 2019.03.28
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectRelConn", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> adminObjectRelConn(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call AdminObjectRelConn");

			String TABLE_NAME = (String) param.get("TABLE_NAME");


			LOGGER.debug(TABLE_NAME);


			List<Map<String, Object>> ListConn = adminService.getAdminObjectRelConn(TABLE_NAME);



			return ListConn;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}


	}
	/**
	 * 객체 관계  관리 리스트(콤보)
	 * @since 2019.03.28
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectRelCombo", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> adminObjectRelCombo(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call AdminObjectRelCombo");
			String TABLE_NAME = (String) param.get("TABLE_NAME");
			LOGGER.debug(TABLE_NAME);

			List<Map<String, Object>> List = adminService.getAdminObjectRelCombo(TABLE_NAME);

			LOGGER.debug("콤보 리스트"+List);
			return List;

		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}

	/**
	 * 객체 관계  관리 조인식 설정
	 * @since 2019.04.01
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectRelJoin", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> adminObjectRelJoin(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call AdminObjectRelJoin");

			String STD_TABLE = (String) param.get("STAND");
			String CONN_TABLE = (String) param.get("TABLE_NAME");

			LOGGER.debug("기준"+STD_TABLE);
			LOGGER.debug("연결"+CONN_TABLE);

			List<Map<String, Object>> ListJoin = adminService.getAdminObjectRelJoin(STD_TABLE,CONN_TABLE);

			String joinExpress = "";

			//저장된 조인 관게와 조인식이 없을 경우 두 테이블 키 칼럼을 가져와서 조인식 작성
			if(ListJoin.isEmpty()){
				List<Map<String, Object>> standKeyList = adminService.getAdminObjectRelJoinStandKeyList(STD_TABLE);
				List<Map<String, Object>> connKeyList = adminService.getAdminObjectRelJoinConnKeyList(CONN_TABLE);
				LOGGER.debug(standKeyList);
				LOGGER.debug(connKeyList);
				LOGGER.debug("if문 타는건지");

				if(!standKeyList.isEmpty()&&!connKeyList.isEmpty()){
					for(int i=0; i < standKeyList.size();i++){

						for(int j=0; j < connKeyList.size();j++){

							if(standKeyList.get(i).get("COL_NAME").equals(connKeyList.get(j).get("COL_NAME"))){

								joinExpress  = joinExpress + standKeyList.get(i).get("TABLE_NAME").toString() + "." +standKeyList.get(i).get("COL_NAME".toString())+"="+connKeyList.get(j).get("TABLE_NAME").toString() +
										"." +connKeyList.get(j).get("COL_NAME").toString()
										+" AND ";
								LOGGER.debug(joinExpress);
							}

						}

					}


				}
				//앤드 짤라 내는 로직 추가
				if(!joinExpress.equals("")){
					joinExpress=joinExpress.substring(0,joinExpress.length()-5);
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("JOIN_EXPR",joinExpress);
				map.put("STD_TABLE",STD_TABLE);
				map.put("CONN_TABLE",CONN_TABLE);

				ListJoin.add(map);
			}

			LOGGER.debug(ListJoin);
			return ListJoin;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}

	}

	/**
	 * 객체 관계  관리 조인식 업데이트
	 * @since 2019.04.02
	 * @author 조형욱
	 */

	@RequestMapping(value = "/AdminObjectRelJoinUpdate", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectRelJoinUpdate(@RequestBody Map<String, Object> param,Authentication authentication,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call AdminObjectRelJoinUpdate");
			User user = (User) authentication.getPrincipal();
			String user_ID = user.getUsername();
			param.put("ID", user_ID);


			int result = 0;

			String STD_TABLE = (String) param.get("STD_TABLE");
			String CONN_TABLE = (String) param.get("CONN_TABLE");
			String JOIN_EXPR = (String) param.get("JOIN_EXPR");
			String ID = (String) param.get("ID");

			List<Map<String, Object>> ListJoin = adminService.getAdminObjectRelJoin(STD_TABLE,CONN_TABLE);
			if(ListJoin.size() == 0){
				result= adminService.setAdminObjectRelJoinInsert(STD_TABLE,CONN_TABLE,JOIN_EXPR,ID);

				return result;

			}else{

				result= adminService.setAdminObjectRelJoinUpdate(STD_TABLE,CONN_TABLE,JOIN_EXPR,ID);

				return result;

			}

		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}


	}

	/**
	 * 객체 관계 관리 삭제
	 * @since 2019.04.02
	 * @author 조형욱
	 */
	@RequestMapping(value = "/AdminObjectRelJoinDelete", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int adminObjectRelJoinDelete(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call adminObjectRelJoinDelete");

			String STD_TABLE = (String) param.get("STAND");
			String CONN_TABLE = (String) param.get("TABLE_NAME");

			int result = 0;


			result= adminService.setAdminObjectRelJoinDelete(STD_TABLE,CONN_TABLE);


			LOGGER.debug(result);

			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}

	}

	/**
	 * 관리자 가입 아이디 중복 조회
	 * @since 2019.04.11
	 * @author 조형욱
	 */
	@RequestMapping(value = "/chkManagerId", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> getManagerIdCheck(@RequestParam(value="insertId") String insertId, HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
		List<Map<String, Object>> ObjectList =  adminService.getManagerIdCheck(insertId);
		LOGGER.debug(ObjectList + "관리자 가입 아이디 중복 조회 controller");
		return ObjectList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}

	/**
	 * 관리자 가입 데이터 인서트
	 * @since 2019.04.11
	 * @author 조형욱
	 */
	@RequestMapping(value = "/signUpManager", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int signUpManager(@RequestBody Map<String, String> param, Authentication authentication,HttpServletResponse response) throws Exception {
		LOGGER.debug(param + "관리자 sign-up controller");

		try{
		User user = (User) authentication.getPrincipal();
		String ID = user.getUsername();
		param.put("ID", ID);

		int result =  adminService.setSignUpManager(param);
		LOGGER.info(result);
		return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}
	}

	/**
	 * 관리자 리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */


	@RequestMapping(value = "/ManagerList", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> ManagerList(HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
		LOGGER.debug("call ManagerList");

		List<Map<String, Object>> ManagerList =  adminService.getManagerList();
		return ManagerList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}

	/**
	 * 관리자 아이디 삭제
	 * @since 2019.04.15
	 * @author 조형욱
	 */

	@RequestMapping(value = "/ManagerDelete", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int managerDelete(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
		LOGGER.debug("call managerDelete!");

		String MANAGER_ID = (String) param.get("MANAGER_ID");


		int result = 0;


		result= adminService.setManagerDelete(MANAGER_ID);
		        adminService.setManagerSaveDelete(MANAGER_ID);

		LOGGER.debug(result);

		return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}


	}

	/**
	 * 관리자 ip리스트
	 * @since 2019.04.15
	 * @author 조형욱
	 */

	@RequestMapping(value = "/ManagerIpList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> ManagerIpList(@RequestBody Map<String, Object> param,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			LOGGER.debug("call ManagerIpList");

			String MANAGER_ID = (String) param.get("MANAGER_ID");



	    	List<Map<String, Object>> List = adminService.getManagerIpList(MANAGER_ID);

			return List;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}

	/**
	 * ip insert Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	@RequestMapping(value="/ManagerIpInsert", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public int ManagerIpInsert(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {

		try{
			LOGGER.debug("call ManagerIpInsert");
			//로그온 유저 조회
			User user = (User) authentication.getPrincipal();
			String userId = user.getUsername();
			param.put("createId", userId);
			LOGGER.debug(param);
			int result =adminService.setManagerIpInsert(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}
	}

	/**
	 * ip Delete Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	@RequestMapping(value="/ManagerIpDelete", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public int ManagerIpDelete(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {

		try{
			LOGGER.debug("call ManagerIpDelete!");

			int result =adminService.setManagerIpDelete(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}
	}

	/**
	 * ip update Method
	 * @since 2019.04.16
	 * @author 조형욱
	 */
	@RequestMapping(value="/ManagerIpUpdate", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public int ManagerIpUpdate(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {

		try{
			LOGGER.debug("call ManagerIpUpdate");
			//로그온 유저 조회
			User user = (User) authentication.getPrincipal();
			String userId = user.getUsername();
			param.put("ID", userId);
			LOGGER.debug(param);
			int result =adminService.setManagerIpUpdate(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return -200;
		}
	}

	/**
	 * 관리자 비밀번호 변경
	 * @since 2019.04.18
	 * @author 조형욱
	 */
	@RequestMapping(value="/ActionAdminPwdChange", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String actionAdminPwdChange(@RequestParam Map<String, String> map, HttpServletResponse response, Authentication authentication) throws Exception {

		try{
			LOGGER.debug("call actionAdminPwdChange");
			String result = "";

			//로그온 유저 조회
			User user = (User) authentication.getPrincipal();
			String managerId = user.getUsername();

			//DB비밀번호 조회
			Map<String, Object> adminPwd = adminService.getAdminPwd(managerId);
			String dbPwd = (String) adminPwd.get("MANAGER_PW");


			String insertcurrPwd = map.get("insertcurrPwd");

			//입력 비번 비교
			StandardPasswordEncoder passwordEncoder = new StandardPasswordEncoder();
			Boolean pwdCheck = passwordEncoder.matches(insertcurrPwd, dbPwd);

			if(pwdCheck){
				map.put("managerId", managerId);
				result = adminService.ActionAdminPwdChange(map);
			}else{
				result ="false"; //불일치
			}

			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}

	/**
	 * 관리자 활성화 업데이트
	 * @since 2019.04.25
	 * @author 조형욱
	 */


	@RequestMapping(value = "/ManagerEnabledUpdate", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public int ManagerEnabledUpdate(@RequestBody Map<String, Object> param,Authentication authentication,HttpServletResponse response)
			throws Exception {
		response.setContentType("application/json;charset=UTF-8");

		try{
			int result = 0;
			LOGGER.debug("call ManagerEnabledUpdate");
			User user = (User) authentication.getPrincipal();
			String UPDATE_ID = user.getUsername();
			param.put("UPDATE_ID", UPDATE_ID);
			LOGGER.debug(param);
			result =  adminService.setManagerEnabledUpdate(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return -100;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return -300;
		}
	}

	/**
	 * rule base 규칙 조회
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@RequestMapping(value = "/callRuleList", headers = "Accept=*/*",method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String, Object> getComCodeList(@RequestBody Map<String, Object> param, HttpServletResponse response)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			Map<String, Object> rtnDataObj = new HashMap<String, Object>();
			LOGGER.debug("call callRuleList");
			param.put("limit", 500);
			if (param.get("page") != null && !"".equals(param.get("page"))) {
				param.put("offset", Integer.parseInt(param.get("page").toString())*500);
			} else {
				param.put("offset", 0);
			}
			List<Map<String, Object>> callRuleList =  adminService.getRuleList(param);
//			int getRuleCount = adminService.getRuleCount(param);
			rtnDataObj.put("callRuleList", callRuleList);
//			rtnDataObj.put("getRuleCount", getRuleCount);
			rtnDataObj.put("page", param.get("page"));
			return rtnDataObj;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}

	/**
	 * rule base 규칙 저장
	 * @since 2020.11.04
	 * @author 강전일
	 */
	@RequestMapping(value="/ruleListSave", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void saveRuleList(@RequestBody List<Map<String, Object>> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call ruleListSave");
			adminService.ruleListSave(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	/**
	 * rule base pop 규칙 조회
	 * @since 2020.11.06
	 * @author 강전일
	 */
	@RequestMapping(value = "/callPopRuleList", headers = "Accept=*/*",method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String, Object> callPopRuleList(@RequestBody Map<String, Object> param, HttpServletResponse response)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			Map<String, Object> rtnDataObj = new HashMap<String, Object>();
			LOGGER.debug("call callRuleList");
			List<Map<String, Object>> callPopRuleList =  adminService.getPopRuleList(param);
			rtnDataObj.put("callPopRuleList", callPopRuleList);
			return rtnDataObj;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}
	
	/**
	 * rule base pop 규칙 저장
	 * @since 2020.11.06
	 * @author 강전일
	 */
	@RequestMapping(value="/ruleListPopSave", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void ruleListPopSave(@RequestBody List<Map<String, Object>> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call ruleListPopSave");
			adminService.ruleListPopSave(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	/**
	 * Rule 표준정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@RequestMapping(value = "/callMgList", headers = "Accept=*/*",method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String, Object> getMgList(@RequestBody Map<String, Object> param, HttpServletResponse response)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			Map<String, Object> rtnDataObj = new HashMap<String, Object>();
			LOGGER.debug("call callMgList");
			param.put("limit", 500);
			if (param.get("page") != null && !"".equals(param.get("page"))) {
				param.put("offset", Integer.parseInt(param.get("page").toString())*500);
			} else {
				param.put("offset", 0);
			}
			List<Map<String, Object>> getMgList =  adminService.getMgList(param);
			rtnDataObj.put("getMgList", getMgList);
			rtnDataObj.put("page", param.get("page"));
			return rtnDataObj;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	@RequestMapping(value = "/callSowList", headers = "Accept=*/*",method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String, Object> getSowList(@RequestBody Map<String, Object> param, HttpServletResponse response)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			Map<String, Object> rtnDataObj = new HashMap<String, Object>();
			LOGGER.debug("call getSowList");
			param.put("limit", 500);
			if (param.get("page") != null && !"".equals(param.get("page"))) {
				param.put("offset", Integer.parseInt(param.get("page").toString())*500);
			} else {
				param.put("offset", 0);
			}
			List<Map<String, Object>> getSowList =  adminService.getSowList(param);
			rtnDataObj.put("getSowList", getSowList);
			rtnDataObj.put("page", param.get("page"));
			return rtnDataObj;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	@RequestMapping(value = "/callStList", headers = "Accept=*/*",method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String, Object> getStList(@RequestBody Map<String, Object> param, HttpServletResponse response)throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			Map<String, Object> rtnDataObj = new HashMap<String, Object>();
			LOGGER.debug("call callMgList");
			param.put("limit", 500);
			if (param.get("page") != null && !"".equals(param.get("page"))) {
				param.put("offset", Integer.parseInt(param.get("page").toString())*500);
			} else {
				param.put("offset", 0);
			}
			List<Map<String, Object>> getStList =  adminService.getStList(param);
			rtnDataObj.put("getStList", getStList);
			rtnDataObj.put("page", param.get("page"));
			return rtnDataObj;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	/**
	 * 가격정보 조회
	 * @since 2021.01.26
	 * @author 강전일
	 */
	@RequestMapping(value = "/getPriceInfoList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> getPriceInfoList(HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
		LOGGER.debug("call getPriceInfoList");
        List<Map<String, Object>> dataList =  adminService.getPriceInfoList();
		return dataList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}
	
	/**
	 * 가격정보  insert
	 * @since 2021.02.25
	 * @author 강전일
	 */
	@RequestMapping(value = "/savPriceInfo",method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> savPriceInfo(@RequestParam("files") MultipartFile files
			, @RequestParam("PRICE_ID") String priceId
			, @RequestParam("SEAL_TYPE") String sealType
			, @RequestParam("SHEET_NO") String sheetNo
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("savPriceInfo page init");
			User user = (User) authentication.getPrincipal();
			String menuId = "ref_data";
			String fGroupId = "";
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			param.put("files", files);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("PRICE_ID", priceId);
			param.put("SEAL_TYPE", sealType);
			param.put("SHEET_NO", sheetNo);
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			adminService.savPriceInfo(param);
			result.put("result",param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}
	
	/**
	 * 가격정보 update
	 * @since 2021.02.25
	 * @author 강전일
	 */
	@RequestMapping(value = "/editPriceInfo", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> editPriceInfo(@RequestParam("files") MultipartFile files
			, @RequestParam("PRICE_ID") String priceId
			, @RequestParam("SEAL_TYPE") String sealType
			, @RequestParam("SHEET_NO") String sheetNo
			, @RequestParam("FILE_PATH") String filePath
			, @RequestParam("FILE_NAME") String fileName
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("editPriceInfo page init");
			User user = (User) authentication.getPrincipal();
			String menuId = "ref_data";
			String fGroupId = "";
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			//파일이 새로 들어왔는지 그대로인지 체크
			param.put("files", files);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("PRICE_ID", priceId);
			param.put("SEAL_TYPE", sealType);
			param.put("SHEET_NO", sheetNo);
			param.put("FILE_PATH", filePath);
			param.put("FILE_NAME", fileName);
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			adminService.editPriceInfo(param);
			result.put("result", param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}
	
	/**
	 * 가격정보 update 파일이 없는경우
	 * @since 2021.02.25
	 * @author 강전일
	 */
//	@RequestMapping(value = "/noFile", method = RequestMethod.POST)
//	@ResponseBody
//	public Map<String,Object> noFile(@RequestParam("NEW_YN") String newYn
//			, @RequestParam("PRICE_ID") String priceId
//			, @RequestParam("SEAL_TYPE") String sealType
//			, @RequestParam("SHEET_NO") String sheetNo
//			, @RequestParam("FILE_PATH") String filePath
//			, @RequestParam("FILE_NAME") String fileName
//			, Authentication authentication, HttpServletResponse response) throws Exception {
//		try{
//			Map<String,Object> result = new HashMap<String,Object>();
//			LOGGER.debug("noFile page init");
//			User user = (User) authentication.getPrincipal();
//			String menuId = "ref_data";
//			String fGroupId = "";
//			//파일 업로드 후 경로 및 파일정보 가져옴
//			Map<String, Object> param = new HashMap<>();
//			//파일이 새로 들어왔는지 그대로인지 체크
//			param.put("newYn", newYn);
//			param.put("menuId", menuId);
//			param.put("fGroupId", fGroupId);
//			param.put("PRICE_ID", priceId);
//			param.put("SEAL_TYPE", sealType);
//			param.put("SHEET_NO", sheetNo);
//			param.put("FILE_PATH", filePath);
//			param.put("FILE_NAME", fileName);
//			param.put("USER_ID", user.getUsername());
//			//DB에 insert
//			adminService.noFile(param);
//			result.put("result", param.get("result"));
//			return result;
//		}catch(RuntimeException runE){
//			LOGGER.error(runE);
//			response.setCharacterEncoding("UTF-8");
//			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//			response.getWriter().print(runE.getMessage());
//			return null;
//		}catch(Exception e){
//			LOGGER.error(e);
//			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//			return null;
//		}
//	}	
	
	/**
	 * 가격정보 delete
	 * @since 2021.02.25
	 * @author 강전일
	 */
	@RequestMapping(value = "/delPriceInfo", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> delPriceInfo(@RequestBody Map<String, Object> param, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("deletePriceInfo page init");
			adminService.delPriceInfo(param);
			result.put("result",param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			return null;
		}
	}		
	
	/**
	 * 그룹정보 조회
	 * @since 2021.03.10
	 * @author 강전일
	 */
	@RequestMapping(value = "/getGrpList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> getGrpList(HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
		LOGGER.debug("call getGrpList");
        List<Map<String, Object>> dataList =  adminService.getGrpList();
		return dataList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}
	
	@RequestMapping(value = "/getSealList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> getSealList(@RequestBody Map<String, Object> param, HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			LOGGER.debug("call getSealList");
			List<Map<String, Object>> dataList =  adminService.getSealList(param);
			return dataList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	@RequestMapping(value = "/getSheetList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> getSheetList(@RequestBody Map<String, Object> param, HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			LOGGER.debug("call getSheetList");
			List<Map<String, Object>> dataList =  adminService.getSheetList(param);
			return dataList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	/**
	 * 가격정보 저장
	 * @since 2021.03.10
	 * @author 강전일
	 */
	@RequestMapping(value="/savGrp", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void savGrp(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call savGrp");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			adminService.savGrp(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	@RequestMapping(value="/savSeal", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void savSeal(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call savSeal");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			adminService.savSeal(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/savSheet",method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> savSheet(@RequestParam("files") MultipartFile files
			, @RequestParam("GRP_ID") String grpId
			, @RequestParam("SHEET_NO") String sheetNo
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("savSheet page init");
			User user = (User) authentication.getPrincipal();
			String menuId = "ref_data";
			String fGroupId = "";
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			param.put("files", files);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("GRP_ID", grpId);
			param.put("SHEET_NO", sheetNo);
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			adminService.savSheet(param);
			result.put("result",param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	@RequestMapping(value = "/noFile", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> noFile(@RequestParam("GRP_ID") String grpId
			, @RequestParam("SHEET_NO") String sheetNo
			, @RequestParam("NEW_YN") String newYn
			, @RequestParam("FILE_PATH") String filePath
			, @RequestParam("FILE_NAME") String fileName
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("noFile page init");
			User user = (User) authentication.getPrincipal();
			String menuId = "ref_data";
			String fGroupId = "";
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			//파일이 새로 들어왔는지 그대로인지 체크
			param.put("newYn", newYn);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("GRP_ID", grpId);
			param.put("SHEET_NO", sheetNo);
			param.put("FILE_PATH", filePath);
			param.put("FILE_NAME", fileName);
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			adminService.noFile(param);
			result.put("result", param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	/**
	 * 가격정보 update
	 * @since 2021.02.25
	 * @author 강전일
	 */
	@RequestMapping(value="/editGrp", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void editGrp(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call editGrp");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			adminService.editGrp(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/editSheet", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> editSheet(@RequestParam("files") MultipartFile files
			, @RequestParam("GRP_ID") String grpId
			, @RequestParam("SHEET_NO") String sheetNo
			, @RequestParam("FILE_PATH") String filePath
			, @RequestParam("FILE_NAME") String fileName
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("editSheet page init");
			User user = (User) authentication.getPrincipal();
			String menuId = "ref_data";
			String fGroupId = "";
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			//파일이 새로 들어왔는지 그대로인지 체크
			param.put("files", files);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("GRP_ID", grpId);
			param.put("SHEET_NO", sheetNo);
			param.put("FILE_PATH", filePath);
			param.put("FILE_NAME", fileName);
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			adminService.editSheet(param);
			result.put("result", param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	/**
	 * 가격정보 delete
	 * @since 2021.02.25
	 * @author 강전일
	 */
	@RequestMapping(value="/delGrp", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void delGrp(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call delGrp");
			adminService.delGrp(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}	
	
	@RequestMapping(value="/delSeal", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void delSeal(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call delSeal");
			adminService.delSeal(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}	
	
	@RequestMapping(value="/delSheet", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void delSheet(@RequestBody Map<String, Object> param, Authentication authentication,HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call delSheet");
			adminService.delSheet(param);
		} catch (Exception e) {
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}	
	
	/**
	 * seal type list 조회
	 * @since 2021.03.11
	 * @author 강전일
	 */
	@RequestMapping(value = "/getSealTypeList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> getSealTypeList(@RequestBody Map<String, Object> param, HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
		LOGGER.debug("call getSealTypeList");
        List<Map<String, Object>> dataList =  adminService.getSealTypeList(param);
		return dataList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}	
}
