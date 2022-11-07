package com.levware.admin.web;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* OLAP Application의 Login View Controller
* <p><b>NOTE:</b> 
*  Admin의 Data 호출 관련 Controller
* @author 최진
* @since 2019.03.12
* @version 1.0
* @see
*
* <pre>
* == 개정이력(Modification Information) ==
*
* 수정일	수정자	수정내용
* -------	--------	---------------------------
* 2019.03.12	최 진	최초 생성
*
* </pre>
*/
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.levware.admin.service.AdminService;
import com.levware.common.FileService;
import com.levware.user.service.UserInfoVO;
import com.levware.user.service.UserPwdChgService;

@Controller
@RequestMapping(value="/admin/api/", method = RequestMethod.GET, produces="application/json")
public class AdminDataAPIController {
private static final Logger LOGGER = LogManager.getLogger(AdminDataAPIController.class);
	
	
	@Resource(name = "AdminService")
	private AdminService adminService;
	
	
	@Resource(name = "UserPwdChgService")
	UserPwdChgService userPwdChgService;
	
	@Autowired
    private FileService fileService;
	
	/**
	 * 객체정보별 조회 조건 관리 조회
	 * @since 2019.04.01
	 * @author 강전일
	 */
	@RequestMapping(value = "/getInfoCriteria", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<UserInfoVO> getInfoCriteria(@RequestParam(value="tableName") String param, HttpServletResponse response) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			LOGGER.debug("call getInfoCriteria param :: " + param);
			List<UserInfoVO> ObjectList =  adminService.getInfoCriteria(param);
			LOGGER.debug("getInfoCriteria ObjectList  result :: "+ObjectList.toString());
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
	 * 조회 조건 조회
	 * @since 2019.04.03
	 * @author 강전일
	 */
	@RequestMapping(value = "/getInfoCondition", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Map<String,Object>> getInfoCondition(HttpServletResponse response ) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			List<Map<String,Object>> ObjectList =  adminService.getInfoCondition();
			LOGGER.debug("/getInfoCondition ObjectList result :: " + ObjectList.toString());
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
	 * 조회조건 동적 체크
	 * @since 2019.04.04
	 * @author 강전일
	 */
	@RequestMapping(value = "/getSearchChk", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Map<String,Object>> getSearchChk(@RequestParam Map<String, Object> param , HttpServletResponse response ) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		
		LOGGER.debug("call getSearchChk param :: " + param);
		try{
			List<Map<String,Object>> ObjectList =  adminService.getSearchChk(param);
			LOGGER.debug("/getSearchChk ObjectList result :: " + ObjectList.toString());
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
	 * 조회 조건 저장
	 * @since 2019.04.04
	 * @author 강전일
	 */
	@RequestMapping(value="/updateCondifion", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void updateCondifion(
			  @RequestParam(value="checkCdArry") String chkArr
			, @RequestParam(value="unCheckCdArry") String unChkArr
			, @RequestParam(value="conditionJson") String conditionJson
			,  HttpServletResponse response
			, Authentication authentication) throws Exception {

		try{
			ObjectMapper mapper = new ObjectMapper();
			List<Map<String, Object>> chkArrParam = mapper.readValue(chkArr, new TypeReference<List<Map<String, Object>>>(){});
			List<Map<String, Object>> unChkArrParam = mapper.readValue(unChkArr, new TypeReference<List<Map<String, Object>>>(){});
			Map<String, Object> conditionJsonParam = mapper.readValue(conditionJson, new TypeReference<Map<String, Object>>(){});
			
			User user = (User) authentication.getPrincipal();
			LOGGER.debug("call updateCondifion Username :: " + user.getUsername());
			adminService.updateCondifion(chkArrParam, unChkArrParam, conditionJsonParam);
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
	 * 회원 목록 조회
	 * @since 2019.03.27
	 * @author 강전일
	 */
	@RequestMapping(value = "/getUserInfoList", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<UserInfoVO> getUserInfoList(@RequestParam(value="USER_ID") String param, HttpServletResponse response ) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		try{
			LOGGER.debug("call getUserInfoList");
			List<UserInfoVO> ObjectList =  adminService.getUserInfoList(param);
			LOGGER.debug("/getUserInfoList result :: " + ObjectList.toString());
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
	 * 회원 목록 삭제
	 * @since 2019.03.28
	 * @author 강전일
	 */
	@RequestMapping(value="/deleteUserList", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public void deleteUserList(@RequestParam(value="checkCdArry[]") List<String> arr, HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("call deleteUserList");
			LOGGER.debug("deleteUserList param :: " + arr.toString());
			adminService.deleteUserList(arr);
			adminService.deleteUserSaveList(arr);
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
	 * 비밀번호 초기화하고 변경비밀번호 반환
	 * @since 2019.03.14
	 * @author 강전일
	 */
	@RequestMapping(value="/actionInitialize", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public String actionInitialize(@RequestParam(value="insertId") String param, HttpServletResponse response) throws Exception {
		try{
			LOGGER.info("call actionInitialize");
			String result = userPwdChgService.actionInitialize(param);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
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
	 * 사용자 저장소 권한 정보 저장
	 * @since 
	 * @author 
	 */
	@RequestMapping(value = "/setUserData", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String,Object> setUserRefDataRole(@RequestBody Map<String, Object> param, HttpServletResponse response ) throws Exception {
		response.setContentType("application/json;charset=UTF-8");
		Map<String,Object> result = null;
		try{
			LOGGER.debug("call setUserData");
			adminService.setUserData(param);
			result = param;
			result.put("result","ok");
			LOGGER.debug("/setUserData result :: " + result.toString());
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
	 * 코드 정보 가져오기
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author 최진
	 */
	@RequestMapping(value="/getAdminTbCode", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Map<String, Object> getAdminTbCode(@RequestParam(value="flag") String flag, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info("call getAdminTbCode");
			 Map<String, Object> rtnData = adminService.getAdminTbCode(flag);
			return rtnData;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().print(runE.getMessage());
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print(e.getMessage());
			 return null;
		}
	 	
	}
	
	/**
	 * 코드 정보 insert
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author 최진
	 */
	@RequestMapping(value="/insertAdminTbCode",headers = "Accept=*/*", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void insertAdminTbCode(@RequestBody Map<String, Object> param, Authentication authentication, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info(param);
			User user = (User) authentication.getPrincipal();
			param.put("userName", user.getUsername());
			String flag = (String) param.get("flag");
			if(flag.equals("feature")){
				adminService.insertAdminTbFeaCode(param);
			}else if(flag.equals("detailValue")){
				adminService.insertAdminTbDetailValCode(param);
			}else{
				throw new Exception("Unknown Parameter");
			}
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print(e.getMessage());
		}
	 	
	}
	
	/**
	 * 코드 정보 update
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author 최진
	 */
	@RequestMapping(value="/updateAdminTbCode",headers = "Accept=*/*", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void updateAdminTbCode(@RequestBody Map<String, Object> param, Authentication authentication, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info(param);
			User user = (User) authentication.getPrincipal();
			param.put("userName", user.getUsername());
			String flag = (String) param.get("flag");
			if(flag.equals("feature")){
				adminService.updateAdminTbFeaCode(param);
			}else if(flag.equals("detailValue")){
				adminService.updateAdminTbDetailValCode(param);
			}else{
				throw new Exception("Unknown Parameter");
			}
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print(e.getMessage());
		}
		
	}
	/**
	 * 코드 정보 삭제
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author 최진
	 */
	@RequestMapping(value="/deleteAdminTbCode",headers = "Accept=*/*", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void deleteAdminTbCode(@RequestBody Map<String, Object> param, Authentication authentication, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info(param);
			User user = (User) authentication.getPrincipal();
			param.put("userName", user.getUsername());
			String flag = (String) param.get("flag");
			if(flag.equals("feature")){
				adminService.deleteAdminTbFeaCode(param);
			}else if(flag.equals("detailValue")){
				adminService.deleteAdminTbDetailValCode(param);
			}else{
				throw new Exception("Unknown Parameter");
			}
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print(e.getMessage());
		}
		
	}
	
	/**
	 * 코드 정보 다운로드
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author  조호철
	 */
	@RequestMapping(value="/downMLGroupInfo", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Map<String, Object> downMLGroupInfo(@RequestParam(value="flag") String flag, @RequestParam(value="CODE_NAME") String codeNm, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info("call getAdminTbCode");
			
			Map<String, Object> result = new HashMap<String, Object>();
			String serviceReslut = adminService.downMLGroupInfo(flag, codeNm); //시트 생성 및 시트명 설정
			result.put("result", serviceReslut);

			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().print(runE.getMessage());
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().print(e.getMessage());
			 return null;
		}
	 	
	}
	/**
	 * 코드 정보 Upload & Insert
	 * @param files
	 * @param menuId
	 * @param fGroupId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/excelUploadProcess",method = RequestMethod.POST)
	public Map<String,Object> excelUploadProcess(@RequestParam("files") MultipartFile files, @RequestParam("menuId") String menuId, @RequestParam("fGroupId") String fGroupId,
			@RequestParam("flag") String flag, Authentication authentication) throws Exception { 
		User user = (User) authentication.getPrincipal();
		String userId = user.getUsername();
//		param.put("userName", user.getUsername());
		Map<String,Object> result = new HashMap<String,Object>();
		// 엑셀파일 Upload
		Map<String,Object> fileUploadResult = fileService.upload(files,menuId,fGroupId);
		// 엑셀정보 Read 
		List<Map<String,Object>> excelDataList = adminService.excelUploadProcess(fileUploadResult.get("file_path") +File.separator+fileUploadResult.get("file_name"),userId,flag);
		result.put("fileUploadResult",fileUploadResult);
		result.put("excelDataList",excelDataList);
		return result;
	}
	
	/**
	 * 코드 정보 ML Group Excel File Upload Conf
	 * @param files
	 * @param selCode
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/confExcel",method = RequestMethod.POST)
	public Map<String,Object> confExcel(@RequestParam("files") MultipartFile files, @RequestParam("selCode") String selCode, @RequestParam("fileOriNm") String fileOriNm, Authentication authentication) throws Exception { 
		String menuId = "null";
		String fGroupId = "null";
		User user = (User) authentication.getPrincipal();
		String userId = user.getUsername();
		Map<String,Object> result = new HashMap<String,Object>();
		// temp file delect
		if(!fileOriNm.equals("N")){
			String path = "D:\\MSPS\\upload\\temp\\"+fileOriNm;
			File deleteFile = new File(path);
			deleteFile.delete();
		}
		// 엑셀파일 Upload
		Map<String,Object> fileUploadResult = fileService.upload(files,menuId,fGroupId);
		// 엑셀정보 Read
		List<Map<String,Object>> excelDataList =  adminService.excelUploadProcessConf(fileUploadResult.get("file_path") +File.separator+fileUploadResult.get("file_name"),selCode,userId);
		//10건 조회
		List<Map<String,Object>> excelDataTop10 = new ArrayList<Map<String,Object>>();
		for(int i=0; i<10; i++){
			excelDataTop10.add(excelDataList.get(i));
		}
		result.put("excelDataTop10",excelDataTop10);
		result.put("excelDataList",excelDataList);
		result.put("fileNm",fileUploadResult.get("file_name"));
		return result;
	}
	
	/**
	 * 코드 정보 Upload & Insert ml 그룹정보 DB저장
	 * @param files
	 * @param menuId
	 * @param fGroupId
	 * @param selCode
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/savInfoProcess",method = RequestMethod.POST)
	public void savInfoProcess(@RequestParam("excelGridData") String excelGridData, @RequestParam("selCode") String selCode, Authentication authentication) throws Exception { 
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> excelGridParam = mapper.readValue(excelGridData, new TypeReference<List<Map<String, Object>>>(){});
		Map<String,Object> result = new HashMap<String,Object>();
		// 엑셀정보 save 
		adminService.savInfoProcess(excelGridParam, selCode);
	}
	
	/**
	 * Hierarchy 정보 다운로드
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author  조호철
	 */

	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value = "/downHierarchyInfo",method = RequestMethod.POST)
	public Map<String,Object> downHierarchyInfo(@RequestBody Map<String, Object> param) throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		//List<Map<String,Object>> HierDataInfoList = (List<Map<String,Object>>)param.get("HierDataInfoList");
		List<String> HierDataInfo = (List<String>)param.get("HierDataInfo");		
		String serviceResult = adminService.downHierarchyInfo(param,HierDataInfo);
		result.put("result",serviceResult);
		return result;
	}
	
	/**
	 * ML hierarchical 정보 LEVEL Update
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author 
	 */
	@RequestMapping(value="/adminMLGroupInfoUpdate",headers = "Accept=*/*", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void adminMLGroupInfoUpdate(@RequestBody Map<String, Object> param, Authentication authentication, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info(param);
			User user = (User) authentication.getPrincipal();
			param.put("userName", user.getUsername());
			boolean flag = (boolean)param.get("updateConfirmed"); // updateConfirmed = true 때문에 String 이아닌 Object 사용.
			LOGGER.info(param);
			if(flag == true){
				adminService.adminMLGroupInfoUpdate(param);
				LOGGER.info("success");
			}else{
				throw new Exception("Unknown Parameter");
			}
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print(e.getMessage());
		}
		
	}
	
	
	/**
	 * ML hierarchical 정보 정보 삭제
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author 
	 */
	@RequestMapping(value="/adminMLGroupInfoDelete",headers = "Accept=*/*", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void adminMLGroupInfoDelete(@RequestBody Map<String, Object> param, Authentication authentication, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info(param);
			User user = (User) authentication.getPrincipal();
			param.put("userName", user.getUsername());
			boolean flag = (boolean) param.get("deleteConfirmed");
			if(flag == true){
				adminService.adminMLGroupInfoDelete(param);
			}else{
				throw new Exception("Unknown Parameter");
			}
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print(e.getMessage());
		}
		
	}
	
	/**
	 * ML hierarchical 정보 정보 생성
	 * @param flag
	 * @param response
	 * @return
	 * @throws Exception
	 * @author 
	 */
	@RequestMapping(value="/adminMLGroupInfoInsert",headers = "Accept=*/*", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public void adminMLGroupInfoInsert(@RequestBody Map<String, Object> param, Authentication authentication, HttpServletResponse response) throws Exception{
		try{
			LOGGER.info(param);
			User user = (User) authentication.getPrincipal();
			param.put("userName", user.getUsername());
			adminService.adminMLGroupInfoInsert(param);

		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().print(e.getMessage());
		}
		
	}
}
