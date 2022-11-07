package com.levware.re;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
//import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.levware.admin.service.AdminService;
import com.levware.common.FileService;
//import com.levware.common.StringUtil;
import com.levware.ml.service.MLService;
import com.levware.rb.service.RBService;

/**
 *   2020.11.24 최초생성
 *
 * @since 2020. 11.03
 * @version 1.0
 * @author LEVWARE
 * @see
 *
 */

@Controller
@RequestMapping(value="/re")
public class ReController {
	
	private static final Logger LOGGER = LogManager.getLogger(ReController.class);
	
	@Resource(name = "mLService")
	private MLService  mLService;
	
	@Resource(name = "rBService")
	private RBService  rBService;
	
	@Resource(name = "fileService")
	private FileService  fileService;
	
	@Resource(name = "AdminService")
	private AdminService adminService;
	
	
	@RequestMapping(value = "/predictSeal", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> predictSealByRuleBased(
			@RequestBody Map<String, Object> param,
			HttpServletRequest request,
			HttpServletResponse response, 
			Authentication authentication) throws Exception {

		Map<String,Object> result = new HashMap<String,Object>();
				
		//System.out.println("param : " + param);
		
		//Rule 처리용 파라미터 복사
		HashMap<String, Object> hRuleParamMap = null;
		ByteArrayOutputStream byteArrOs;
		ObjectOutputStream objOs;
		ByteArrayInputStream byteArrIs;
		ObjectInputStream objIs;
		Object deepCopy;

		byteArrOs = new ByteArrayOutputStream(); 
		objOs = new ObjectOutputStream(byteArrOs);
		objOs.writeObject(param);
		byteArrIs = new ByteArrayInputStream(byteArrOs.toByteArray());
		objIs = new ObjectInputStream(byteArrIs);
		deepCopy = objIs.readObject();
		hRuleParamMap = (HashMap<String, Object>)deepCopy;
			
		// -------------------------------------------------------
		// 실적 기준 추천
		// 운영반영 시 또는 실적기준 추천 테스트 시 수정하여 진행
		// --------------------------------------------------------
		//result = dummy(param); // 개발테스트용(Dummy Data) <- 인자에 상관없이 고정값을 반환
		result = mLService.predictMultiWithSavedModel(param); // 실적베이스추천
			
		//Rule 기준 추천
		Map<String,Object> ruleResult = rBService.predictSealByRuleBased(hRuleParamMap);
		
		result.put("RULE_RESULT",ruleResult.get("RULE_RESULT"));
		//System.out.println("최종반환값 :" + result);
		
		// 2022-11-04 추가 : AS 이력 정보
		List<Map<String,Object>> ruleBaseNPredictASList = rBService.getASList(result);
		result.put("AS_HISTORY", ruleBaseNPredictASList);
		return result;
	}
	
	
	@RequestMapping(value = "/predictSeal2", method = RequestMethod.POST )
	@ResponseBody
	public Map<String,Object> predictSealByRuleBasedWithSearch(
			@RequestBody Map<String, Object> param,
			HttpServletRequest request,
			HttpServletResponse response, 
			Authentication authentication) throws Exception {
		
		Map<String,Object> result = null;
		
		//Rule 처리용 파라미터 복사
		HashMap<String, Object> hRuleParamMap = null;
		ByteArrayOutputStream byteArrOs;
		ObjectOutputStream objOs;
		ByteArrayInputStream byteArrIs;
		ObjectInputStream objIs;
		Object deepCopy;

		byteArrOs = new ByteArrayOutputStream(); 
		objOs = new ObjectOutputStream(byteArrOs);
		objOs.writeObject(param);
		byteArrIs = new ByteArrayInputStream(byteArrOs.toByteArray());
		objIs = new ObjectInputStream(byteArrIs);
		deepCopy = objIs.readObject();
		hRuleParamMap = (HashMap<String, Object>)deepCopy;
				
		//실적 기준 추천
		result =  mLService.predictMultiWithSearch(param); // 실적베이스추천
		
		//Rule 기준 추천
		Map<String,Object> ruleResult = rBService.predictSealByRuleBased(hRuleParamMap);
		
		result.put("RULE_RESULT",ruleResult.get("RULE_RESULT"));
		//System.out.println("최종반환값 :" + result);
		
		return result;
	}
	
	
	/**
	 * 가격정보 조회를 위한 
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
		
	
	@RequestMapping(value = "/pdfviewer")
	public String pdfviewer(ModelMap model,Authentication authentication) throws Exception {
		return "pdf/pdfviewer";
	}

	
	private Map<String,Object> dummy (Map<String,Object> param){
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String,Object>> predict_param_list = (List<Map<String,Object>>)param.get("predict_list"); 
		String s = "{\"RESULT\":[";
		for (int i=0; i< predict_param_list.size(); i++) {
			s+= "{\"predict_idx\":\""+(i+1)+"\",\"param_cnv\":{\"NO\":1,\"PUMP_TYPE\":\"OH2\",\"PRODUCT\":\"caustic 50% water 10%\",\"TEMP_NOR\":\"250\",\"TEMP_MIN\":\"250\",\"TEMP_MAX\":\"250\",\"SPEC_GRAVITY_NOR\":\"0.2\",\"SPEC_GRAVITY_MIN\":\"0.2\",\"SPEC_GRAVITY_MAX\":\"0.2\",\"VISC_NOR\":\"1.0\",\"VISC_MIN\":\"1.0\",\"VISC_MAX\":\"1.0\",\"VAP_PRES_NOR\":\"0.0\",\"VAP_PRES_MIN\":\"0.0\",\"VAP_PRES_MAX\":\"0.0\",\"SEAL_CHAM_NOR\":\"30\",\"SEAL_CHAM_MIN\":\"30\",\"SEAL_CHAM_MAX\":\"30\",\"RPM_NOR\":\"3600\",\"RPM_MIN\":\"3600\",\"RPM_MAX\":\"3600\",\"SHAFT_SIZE\":\"50\",\"TEMP_UNIT\":\"C\",\"TEMP_TEXT\":\"℃\",\"VISC_UNIT\":\"CP\",\"VISC_TEXT\":\"CP\",\"VAP_PRES_UNIT\":\"BARA\",\"VAP_PRES_TEXT\":\"BARA\",\"SEAL_CHAM_UNIT\":\"BARG\",\"SEAL_CHAM_TEXT\":\"BARG\",\"RPM_UNIT\":\"RPM\",\"RPM_TEXT\":\"RPM\",\"SHAFT_SIZE_UNIT\":\"MM\",\"SHAFT_SIZE_TEXT\":\"MM\",\"TEMP_NOR_C\":\"32.0\",\"TEMP_MIN_C\":\"32.0\",\"TEMP_MAX_C\":\"32.0\",\"SPEC_GRAVITY_NOR_C\":\"1.0\",\"SPEC_GRAVITY_MIN_C\":\"1.0\",\"SPEC_GRAVITY_MAX_C\":\"1.0\",\"VISC_NOR_C\":\"1.0\",\"VISC_MIN_C\":\"1.0\",\"VISC_MAX_C\":\"1.0\",\"VAP_PRES_NOR_C\":\"0.0\",\"VAP_PRES_MIN_C\":\"0.0\",\"VAP_PRES_MAX_C\":\"0.0\",\"SEAL_CHAM_NOR_C\":\"1.0\",\"SEAL_CHAM_MIN_C\":\"1.0\",\"SEAL_CHAM_MAX_C\":\"1.0\",\"RPM_NOR_C\":\"3600\",\"RPM_MIN_C\":\"3600\",\"RPM_MAX_C\":\"3600\",\"SHAFT_SIZE_C\":\"50\",\"EQUIPMENT\":\"Z060300\",\"SUCT_PRES_NOR\":\"1.0\",\"SUCT_PRES_MIN\":\"1.0\",\"SUCT_PRES_MAX\":\"1.0\",\"DISCH_PRES_NOR\":\"1.0\",\"DISCH_PRES_MIN\":\"1.0\",\"DISCH_PRES_MAX\":\"1.0\",\"L_SPD_MIN\":30.905512800000004,\"L_SPD_NOR\":30.905512800000004,\"L_SPD_MAX\":30.905512800000004,\"SLURRY_SEAL_YN\":\"N\",\"API682_TYPE\":[\"A\"],\"API682_RS\":[]},\"ProductGroupInfo\":\"CAUSTIC+WATER\",\"RESULT\":{\"CONN_COL\":[{\"PROB\":10.01,\"NO\":1,\"CLASS\":\"QBQLZ/QBQW | 5 U L X / 5 U L X | 21/52/61\"},{\"PROB\":10.0,\"NO\":2,\"CLASS\":\"QBQ/QBQW | 5 Z Y Z / 5 U Y Z | 11/52\"},{\"PROB\":10.0,\"NO\":3,\"CLASS\":\"QBQW | 5 A 7 Z | 02/61\"},{\"PROB\":10.0,\"NO\":4,\"CLASS\":\"BXW/BXW | K A K X / K A 4 X | 11/52\"},{\"PROB\":10.0,\"NO\":5,\"CLASS\":\"QBQW/QBQW | 5 A K Z / 5 A K Z | 11/52/61\"}],\"SEAL_TYPE\":[{\"PROB\":24.53,\"NO\":1,\"CLASS\":\"QBQW/QBQW\"},{\"PROB\":7.98,\"NO\":2,\"CLASS\":\"QBQW\"},{\"PROB\":6.51,\"NO\":3,\"CLASS\":\"BXRH\"},{\"PROB\":5.14,\"NO\":4,\"CLASS\":\"QBQ/QBQW\"},{\"PROB\":3.51,\"NO\":5,\"CLASS\":\"BXHHW/BXHW\"}],\"API_PLAN\":[{\"PROB\":20.78,\"NO\":1,\"CLASS\":\"11/52\"},{\"PROB\":16.58,\"NO\":2,\"CLASS\":\"23/61\"},{\"PROB\":15.04,\"NO\":3,\"CLASS\":\"21/52\"},{\"PROB\":10.44,\"NO\":4,\"CLASS\":\"11/52/61\"},{\"PROB\":10.0,\"NO\":5,\"CLASS\":\"11/52/61M\"}]},\"predict_msg\":\"complete\"},";
		}
		s = s.substring(0,s.length()-1);
		s+= "]}";
		Map<String, Object> map = null;
		try {
			map = mapper.readValue(s, Map.class);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	
//	@RequestMapping(value = "/predictSeal", method = RequestMethod.POST )
//	@ResponseBody
//	public Map<String,Object> predictSealByRuleBased(
//			@RequestBody Map<String, Object> param,
//			HttpServletRequest request,
//			HttpServletResponse response, 
//			Authentication authentication) throws Exception {
//
//		Map<String,Object> result = new HashMap<String,Object>();
//
//		/*
//		// Test ///////////////////////////////////////////////
//		try {		
////		int i = 1/0; 오류 테스트용 주석으로 막아놓기
//        JSONObject jObj = new JSONObject();
//
////        JSONArray jArr = new JSONArray();
////        for(String f : features) {
////           jArr.add(f);
////        }
//        
//        List<Map<String,Object>> predict_itemList = param.get("predict_list")==null?new ArrayList<Map<String,Object>>(): (List<Map<String,Object>>)param.get("predict_list");
//        for(Map<String,Object> m:predict_itemList) {
//        	String concatcol = "0";
//        	m.put("SEAL_TYPE_enc", "0");
//        	m.put("PRODUCT_G_enc", "0");
//        	m.put("PUMP_TYPE_G_enc", "0");
//        	m.put("API_PLAN_enc", "0");
//        	m.put("concat_col_enc", concatcol);
//        }
//        System.out.println("predict_itemList : " + predict_itemList);
//        jObj.put("data", predict_itemList);  //jArr
//        List<String> predict_hederList = new ArrayList<String>();
//        predict_hederList.add("TEMP_NOR");
//        predict_hederList.add("TEMP_MIN");
//        predict_hederList.add("TEMP_MAX");
//        predict_hederList.add("SPEC_GRAVITY_NOR");
//        predict_hederList.add("SPEC_GRAVITY_MIN");
//        predict_hederList.add("SPEC_GRAVITY_MAX");
//        predict_hederList.add("VISC_NOR");
//        predict_hederList.add("VISC_MIN");
//        predict_hederList.add("VISC_MAX");
//        predict_hederList.add("VAP_PRES_NOR");
//        predict_hederList.add("VAP_PRES_MIN");
//        predict_hederList.add("VAP_PRES_MAX");
//        predict_hederList.add("SEAL_CHAM_NOR");
//        predict_hederList.add("SEAL_CHAM_MIN");
//        predict_hederList.add("SEAL_CHAM_MAX");
//        predict_hederList.add("PUMP_TYPE_G");
//        predict_hederList.add("PUMP_TYPE_G_enc");
//        predict_hederList.add("PRODUCT_G");
//        predict_hederList.add("PRODUCT_G_enc");
//        predict_hederList.add("SEAL_TYPE");
//        predict_hederList.add("SEAL_TYPE_enc");
//        predict_hederList.add("API_PLAN");
//        predict_hederList.add("API_PLAN_enc");
//        predict_hederList.add("concat_col_enc");
//        predict_hederList.add("RPM_NOR");
//        predict_hederList.add("RPM_MIN");
//        predict_hederList.add("RPM_MAX");
//        predict_hederList.add("SHAFT_SIZE");
//        
//        jObj.put("header", predict_hederList);
//        
//        
////        TEMP_NOR
////        TEMP_MIN
////        TEMP_MAX
////        SPEC_GRAVITY_NOR
////        SPEC_GRAVITY_MIN
////        SPEC_GRAVITY_MAX
////        VISC_NOR
////        VISC_MIN
////        VISC_MAX
////        VAP_PRES_NOR
////        VAP_PRES_MIN
////        VAP_PRES_MAX
////        SEAL_CHAM_NOR
////        SEAL_CHAM_MIN
////        SEAL_CHAM_MAX
////        PRODUCT
////        PUMP_TYPE
//        
////        PRODUCT_G_enc
////        PUMP_TYPE_G_enc
////        SEAL_TYPE_enc
//        String stepGb = param.get("predict_type").toString();
//        if(stepGb.equals("EPC")){
//        	List<String> sapi_urlList = new ArrayList<String>();
//        	List<String> api_keyList = new ArrayList<String>();
//        	sapi_urlList.add("http://localhost/data/8c34a52e-79eb-41e8-a5b2-a88af4d59b50/c3d35daf-de03-4b9b-a544-26cb85ae194a.do");
//        	sapi_urlList.add("http://localhost/data/8c34a52e-79eb-41e8-a5b2-a88af4d59b50/9cf2723d-1621-4a85-8e88-483a8edce6e4.do");
//        	sapi_urlList.add("http://localhost/data/8c34a52e-79eb-41e8-a5b2-a88af4d59b50/070a14cb-6f11-42d7-b6dc-40f661782dc3.do");
//        	api_keyList.add("ksm_epc_m1");
//        	api_keyList.add("ksm_epc_m2");
//        	api_keyList.add("ksm_epc_m3");
//        	for(int i=0; i<sapi_urlList.size(); i++){
//        		String sApiUrl = sapi_urlList.get(i);	
//        		URL url = new URL(sApiUrl); // 호출할 url
//        		jObj.put("api_key", api_keyList.get(i)); // API Key
//        		String jParam = jObj.toString();
//        		Map<String,Object> rtObj = callApiServer(url, jParam);
//        		//success
//        	}
//        }else{
//        	List<String> sapi_urlList = new ArrayList<String>();
//        	List<String> api_keyList = new ArrayList<String>();
//        	sapi_urlList.add("http://localhost/data/8c34a52e-79eb-41e8-a5b2-a88af4d59b50/ca1d6c8d-1a8d-4efb-9424-3aad8854ce05.do");
//        	sapi_urlList.add("http://localhost/data/8c34a52e-79eb-41e8-a5b2-a88af4d59b50/ba66c5c8-ea93-4743-b101-c54a14196570.do");
//        	sapi_urlList.add("http://localhost/data/8c34a52e-79eb-41e8-a5b2-a88af4d59b50/312959c1-1601-474c-ad8d-5a7aba335795.do");
//        	api_keyList.add("ksm_oem_m1");
//        	api_keyList.add("ksm_oem_m2");
//        	api_keyList.add("ksm_oem_m3");
//        	for(int i=0; i<sapi_urlList.size(); i++){
//        		String sApiUrl = sapi_urlList.get(i);	
//        		URL url = new URL(sApiUrl); // 호출할 url
//        		jObj.put("api_key", api_keyList.get(i)); // API Key
//        		String jParam = jObj.toString();
//        		Map<String,Object> rtObj = callApiServer(url, jParam);
//        		//success
//        	}
//        }
//
//		// Test
//		}catch(Exception e) {
//			System.out.println("ERROR");
//			e.printStackTrace();
//			result.put("API_RESULT","FAIL");
//			return result;
//		}
//		*/
//				
//		System.out.println("param : " + param);
//		
//		//Rule 처리용 파라미터 복사
//		HashMap<String, Object> hRuleParamMap = null;
//		ByteArrayOutputStream byteArrOs;
//		ObjectOutputStream objOs;
//		ByteArrayInputStream byteArrIs;
//		ObjectInputStream objIs;
//		Object deepCopy;
//
//		byteArrOs = new ByteArrayOutputStream(); 
//		objOs = new ObjectOutputStream(byteArrOs);
//		objOs.writeObject(param);
//		byteArrIs = new ByteArrayInputStream(byteArrOs.toByteArray());
//		objIs = new ObjectInputStream(byteArrIs);
//		deepCopy = objIs.readObject();
//		hRuleParamMap = (HashMap<String, Object>)deepCopy;
//			
//		//if ((fileService.getRemoteAddr(request)).contains("0:0:0:0:0:0:0:1")) { // 임시처리 localhost
//		//	Map<String,Object> param1 = (Map<String,Object>)((HashMap<String,Object>)param).clone();
//			result = dummy(param); // 개발테스트용(Dummy Data)
//		//}else {
//			//result = mLService.predictMultiWithSavedModel(param); // 실적베이스추천
//		//}
//			
//		Map<String,Object> ruleResult = rBService.predictSealByRuleBased(hRuleParamMap);
//		
//		result.put("RULE_RESULT",ruleResult.get("RULE_RESULT"));
//		//System.out.println("최종반환값 :" + result);
//		return result;
//		
//	}
	
	//OEM여부에 따라서 API호출 메서드
	public Map<String, Object> callApiServer(URL url, String jParam) throws Exception {
        OutputStreamWriter osw = null;
        BufferedReader br = null;
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(3000000);
        conn.setReadTimeout(3000000);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        
        osw = new OutputStreamWriter(conn.getOutputStream(),"UTF-8");
        osw.write(jParam);
        osw.flush();
   
        br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));


        JSONParser jsonParse = new JSONParser();
		JSONObject rtObj = new JSONObject();
		String inputLine;
		while((inputLine = br.readLine()) != null) { // response 출력
			rtObj = (JSONObject)jsonParse.parse(inputLine);
		}
		return rtObj;
	}
		
	
}
		
