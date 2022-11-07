package com.levware.project.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.levware.project.service.ProjectService;

@Controller
@RequestMapping(value="/project")
public class ProjectController {
	@Resource(name = "ProjectService")
	private ProjectService  ProjectService;
	
	/**
     * 프로젝트 관리 화면 
     */
	@RequestMapping(value="/ProjectMng")
	public String ProjectMng(HttpServletResponse response) throws Exception{
	   	return "project/ProjectMng";
	}
	
	/**
     * 프로젝트 리스트 조회 
     */
	@RequestMapping(value="/getProjectList")
	@ResponseBody
	public List<Map<String, Object>> getProjectList(@RequestParam(value="projectId",required = false) String projectId, Authentication authentication) throws Exception{
		
		/**
		 * 2020.11.19. 이소라 수정
		 * - 현재 로그인 한 계정으로 생성한 프로젝트만 조회되도록 조회조건 수정.
		 */
		User user = (User) authentication.getPrincipal();
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userid", user.getUsername());
		
		List<Map<String, Object>> rtnData = ProjectService.getProjectList(params);
		return rtnData;
	}
	
	/**
     * 프로젝트 등록 
     */
	@RequestMapping(value="/insertProject")
	@ResponseBody
    public Map<String,Object> insertProject(@RequestBody Map<String, Object> params) throws Exception{
		String msg = ProjectService.insertProject(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
    }
	
	/**
     * 프로젝트 선택 - 서브 프로젝트 리스트 호출
     */
	@RequestMapping(value="/selectProject")
	@ResponseBody
	public Map<String, Object> selectProject(@RequestBody Map<String, Object> params) throws Exception{
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		// 프로젝트 목록 불러오기
		Map<String, Object> selectProjectinfo = ProjectService.selectProject(params);
		// 선택 프로젝트의 서브 프로젝트 불러오기
		List<Map<String, Object>> subProjectList = ProjectService.subProjectList(params);
		rtnDataObj.put("projectInfo", selectProjectinfo);
		rtnDataObj.put("record", subProjectList);
		return rtnDataObj;
	}
	
	/**
     * 서브프로젝트 등록 
     */
	@RequestMapping(value="/mngSubProject", headers = "Accept=*/*")
	@ResponseBody
    public Map<String,Object> insertSubProject(@RequestBody Map<String, Object> params) throws Exception{
		String msg = "";
		if(params.get("status").equals("I")) {
			msg = ProjectService.insertSubProject(params);
		}else if(params.get("status").equals("U")) {
			msg = ProjectService.updateSubProject(params);
		}else if(params.get("status").equals("D")) {
			msg = ProjectService.deleteSubProject(params);
		}else if(params.get("status").equals("C")) {
			msg = ProjectService.copySubProject(params);
		}
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
    }
	
	/**
     *서브 프로젝트 호출
     */
	@RequestMapping(value="/getSubProjectInfo")
	@ResponseBody
	public Map<String, Object> getSubProjectInfo(@RequestBody Map<String, Object> params) throws Exception{
		return ProjectService.getSubProjectInfo(params);
	}
	
	/**
     *서브 프로젝트 getContents
     */
	@RequestMapping(value="/getSubProjectContents")
	@ResponseBody
	public Map<String, Object> getSubProjectContents(@RequestBody Map<String, Object> params) throws Exception{
		String key = (String) params.get("key");
		String[] keyList = key.split("\\|");
				
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		List<Map<String, Object>> getSubProjectContents = ProjectService.getSubProjectContents(params);
		String contents = getSubProjectContents.get(0).get("contents").toString();
		JSONObject jsonObject = new JSONObject();
		jsonObject =  (JSONObject)JSONValue.parse(contents);
		JSONObject Lv1Data = (JSONObject) jsonObject.get(keyList[0]);
				
		rtnDataObj.put("record", Lv1Data);
		return rtnDataObj;
	}
	
	/**
     * 서브프로젝트 수정  공통함수
     */
	@RequestMapping(value="/updateSubContents")
	@ResponseBody
    public Map<String, Object> updateSubContents(@RequestBody Map<String, Object> params) throws Exception{
		String msg = ProjectService.updateSubContents(params);
		Map<String, Object> rtnDataObj = new HashMap<String, Object>();
		rtnDataObj.put("msg",msg);
		return rtnDataObj;
    }
	
	/**
     * csv 생성 
     */
	@RequestMapping(value="/csvAction")
    public void csvAction(@RequestBody Map<String, Object> params) throws Exception{
		ProjectService.csvAction(params);
    }
	
	/**
     * csv 읽기 
     */
	@RequestMapping(value="/csvLoadAction")
	@ResponseBody
    public Map<String, Object> csvLoadAction(@RequestBody Map<String, Object> params) throws Exception{
		Map<String, Object> rtnDataObj = ProjectService.csvLoadAction(params);
		return rtnDataObj;  
    }
	
	@RequestMapping(value= "/decisionTreeTrain")
	@ResponseBody
	public String decisionTreeTrain(@RequestBody Map<String, Object> params)throws Exception{
		RestTemplate restTemplate = new RestTemplate();
		//운영
		//String result = restTemplate.postForObject("http://localhost/decisionTreeTrain",params,String.class);
		//개발
		String result = restTemplate.postForObject("http://localhost:8080/decisionTreeTrain",params,String.class);
		return result;
	}
	
	@RequestMapping(value= "/decisionTreePredict")
	@ResponseBody
	public String decisionTreePredict(@RequestBody Map<String, Object> params)throws Exception{
		RestTemplate restTemplate = new RestTemplate();
		//운영
		//String result = restTemplate.postForObject("http://localhost/decisionTreePredict",params,String.class);
		//개발
		String result = restTemplate.postForObject("http://localhost:8080/decisionTreePredict",params,String.class);
		return result;
	}
	
	@RequestMapping(value= "/modelApiCreate")
	@ResponseBody
	public String modelApicall(@RequestBody Map<String, Object> params)throws Exception{
		RestTemplate restTemplate = new RestTemplate();
		//운영	
		//String result = restTemplate.postForObject("http://localhost/modelApiCreate",params,String.class);
		//개발
		String	result = restTemplate.postForObject("http://localhost:8080/modelApiCreate",params,String.class);
		return result;
	}
	
	@RequestMapping(value="/insertToModelInfo")
    public void insertToModelInfo(@RequestBody Map<String, Object> params) throws Exception{
		ProjectService.insertToModelInfo(params);
    }
	
	@RequestMapping(value= "/mlModelTest")
	@ResponseBody
	public String mlModelTest(@RequestBody Map<String, Object> params)throws Exception{
		RestTemplate restTemplate = new RestTemplate();
		//운영	
		//String result = restTemplate.postForObject("http://localhost/model",params,String.class);
		//개발
		String result = restTemplate.postForObject("http://localhost:8080/model",params,String.class);
		return result;
	}
	
	@RequestMapping(value= "/splitData")
	@ResponseBody
	public String splitData(@RequestBody Map<String, Object> params)throws Exception{
		RestTemplate restTemplate = new RestTemplate();
		//운영	
		//String result = restTemplate.postForObject("http://localhost/splitData",params,String.class);
		//개발
		String	result = restTemplate.postForObject("http://localhost:8080/splitData",params,String.class);
		return result;
	}
	
	/**
     * split csv(train, test) 읽기 - csvLoadAction 사용
     */
	@RequestMapping(value="/splitCsvLoad")
	@ResponseBody
    public Map<String, Object> splitCsvLoad(@RequestBody Map<String, Object> params) throws Exception{
		Map<String, Object> rtnDataObj = ProjectService.csvLoadAction(params);
		return rtnDataObj;  
    }
	
}
