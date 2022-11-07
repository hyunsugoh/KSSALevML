
package com.levware.user.web;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
* OLAP Application Data API Controller
* <p><b>NOTE:</b> 
*  User가 활용하는 Data를 API로 제어하는 컨트롤러
*  'content-type="application/json"'만 허용 
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.user.service.OlapObjectDetailInfoVO;
import com.levware.user.service.OlapObjectVO;
import com.levware.user.service.OlapSavedDataVO;
import com.levware.user.service.OlapSelectObjectVO;
import com.levware.user.service.UserDataAPIService;

import egovframework.rte.fdl.property.EgovPropertyService;

@Controller
@RequestMapping(value="/user/api/", method = RequestMethod.GET, produces="application/json")
public class UserDataAPIController {

	public static Logger LOGGER = LogManager.getFormatterLogger(UserDataAPIController.class);
	
	@Resource(name = "UserDataAPIService")
	UserDataAPIService userDataApiService;
	
	/**
	 * 객체 정보 get API 호출 Method
	 * @return JSON Object
	 * @throws Exception 
	 */
	@RequestMapping(value="/data/getObjectList", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Map<String, Object> getObjectList(HttpServletResponse response) throws Exception {
		try{
			LOGGER.debug("getObjectList call");
			response.setContentType("application/json;charset=UTF-8");
			Map<String, Object> rtnDataObj = new HashMap<String, Object>();
			// 객체 목록 불러오기
			List<OlapObjectVO> rtnData = userDataApiService.getObjectList();
			LOGGER.debug("getObjectList result :: ");
			LOGGER.debug(rtnData);
			rtnDataObj.put("record", rtnData);
			return rtnDataObj;
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
	 * 객체 관계 정보 get API 호출 Method
	 * @param stdTableName 기준 테이블명
	 * @param response
	 * @return rtnData 연결된 테이블 정보
	 * @throws Exception
	 */
	@RequestMapping(value="/data/getObjectRelInfo", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Map<String, Object> getObjectRelInfo(@RequestParam(value="stdName") String stdTableName,HttpServletResponse response) throws Exception {
		LOGGER.debug("getObjectRelInfo API Call");
		try{
			response.setContentType("application/json;charset=UTF-8");
			Map<String, Object> rtnData = userDataApiService.getObjectRelationInfo(stdTableName);
			LOGGER.debug(rtnData);
			return rtnData;
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
	 * 선택한 객체의 세부정보 호출 API Method
	 * @param tbName
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/data/getObjectDetailInfo", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<OlapObjectDetailInfoVO> getObjectDetailInfo(@RequestParam(value="tbName") String tbName, HttpServletResponse response) throws Exception {
		LOGGER.debug("getObjectDetailInfo API Call");
		
		try{
			List<OlapObjectDetailInfoVO> rtnData = userDataApiService.getObjectDetailInfo(tbName);
			return rtnData;
		
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
	 * 조건 조회에 필요한 조건 값 불러오기
	 * @param tbNames
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/data/getConditionData", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Map<String, Object> getConditionData(@RequestParam(value="tbNames[]") String[] tbNames, HttpServletResponse response) throws Exception{
		LOGGER.debug("======================== getConditionData ================================");
		try{
			Map<String, Object> rtnData = userDataApiService.getConditionData(tbNames);
			
			return rtnData;
			
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
	 * 동적 쿼리 생성 및 그리드 데이터 리턴
	 * @param response
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/data/selectGridData", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public Map<String, Object> selectGridData(HttpServletResponse response, @RequestBody OlapSelectObjectVO params, Authentication authentication) throws Exception{
		LOGGER.debug("======================== selectGridData ======================== ");
		try{
			User user = (User) authentication.getPrincipal();
			Map<String, Object> rtnGridData = userDataApiService.getSelectGridData(user, params);
			return rtnGridData;
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
	 * 사용자가 선택한 정보 저장하기
	 * @param response
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/data/saveUserDataset", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public void saveUserDataSet(HttpServletResponse response, @RequestBody Map<String, Object> params, Authentication authentication) throws Exception{
		LOGGER.debug("======================== saveUserDataset ======================== ");
		User user = (User) authentication.getPrincipal();
		LOGGER.debug(params);
		params.put("userInfo", user);
		try{
			userDataApiService.insertNUpdateUserDataset(params);			
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	/**
	 * 저장목록 불러오기
	 * @param response
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/data/selectSavedDataSetList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public List<OlapSavedDataVO> selectSavedDataSetList(HttpServletResponse response, Authentication authentication) throws Exception{
		LOGGER.debug("======================== selectUserSavedDataSetList ======================== ");
		
		try{
			User user = (User) authentication.getPrincipal();
			List<OlapSavedDataVO> rtnSavedData = userDataApiService.getSelectUserSavedDataSetList(user);
			return rtnSavedData;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}
	
	
	/**
	 * 저장목록 삭제하기
	 * @param response
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/data/deleteUserDataset", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public void deleteUserDataset(HttpServletResponse response, Authentication authentication, @RequestBody int seqNum){
		LOGGER.debug("======================== deleteUserDataset ======================== ");
		User user = (User) authentication.getPrincipal();
		try{
			userDataApiService.deleteUserDataset(user, seqNum);	
			
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		
	}
	
	/**
	 * 선택한 조건의 셀렉트 리스트 호출 API Method
	 * @param  dataset.conditionDataset[idx]
	
	 * @return  
	 * @throws Exception
	 */
	@RequestMapping(value="/data/getSelectList", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public  List<Map<String, Object>> getSelectList(@RequestBody Map<String, Object> param, HttpServletResponse response) throws Exception {
		LOGGER.debug("getSelectList API Call");

				
		try{
			response.setContentType("application/json;charset=UTF-8");
		    String tableName = (String) param.get("tableName");
			String columnName = (String) param.get("columnName");

			List<Map<String, Object>> rtnData = userDataApiService.getSelectList(tableName,columnName);
			return rtnData;
		
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
	
	// properties
	@Resource(name="propertyService")
	protected EgovPropertyService propertyService;
		
	/**
	 * 임시용 properties get
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/data/getTableName", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Map<String, Object> getTableName(HttpServletResponse response){
		LOGGER.debug("======================== getTableName ======================== ");
		try{
			Map<String, Object> rtnData = new HashMap<String, Object>();
			String defaultTableName = propertyService.getString("defaultTable");
			rtnData.put("tbName", defaultTableName);
			return rtnData;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	};
	
	
	@RequestMapping(value="/data/getUnitInfo", headers = "Accept=*/*", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<Map<String, Object>> getUnitInfo(@RequestParam String flag, HttpServletResponse response) throws Exception{
		
		LOGGER.debug("======================== getUnitInfo ======================== ");
		try{
			if(flag.equals("unitInfo")){
				List<Map<String, Object>> rtnData = userDataApiService.getUnitList();
				 return rtnData;
			}else{
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				 return null;
			}
			
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
	 * 동적 쿼리 생성 및 그리드 데이터 리턴
	 * @param response
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/data/selectASHistoryGridData", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Map<String, Object>> selectASHistoryGridData(HttpServletResponse response, @RequestParam String bomId) throws Exception{
		LOGGER.debug("======================== selectGridData ======================== ");
		try{	
			List<Map<String, Object>> rtnGridData = userDataApiService.getSelectASHistoryGridData(bomId);
			return rtnGridData;
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
