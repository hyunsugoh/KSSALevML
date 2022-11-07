package com.levware.project.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.stereotype.Service;

import com.levware.common.mappers.mart.MartUserMapper;
import com.levware.common.mappers.repo.ProjectMapper;
import com.levware.project.service.ProjectService;
import com.levware.user.service.OlapSelectObjectVO;

@Service("ProjectService")
public class ProjectServiceimpl implements ProjectService {

	private static String profile=System.getProperty("server.os.profile");
	private static String rootFilePath=profile.equals("linux")?"/app/levml/csv/":"D:/lev_ml/";
	
	@Resource(name = "ProjectMapper")
	private ProjectMapper projectMapper;
	
	@Resource(name = "martUserMapper")
	private MartUserMapper martMapper;

	/**
	 * getProjectList 프로젝트 객체 정보 불러오기
	 */
	public List<Map<String,Object>> getProjectList(Map<String, Object> params){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		try {
			result = projectMapper.getProjectList(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * insertProject 프로젝트 생성
	 */
	@Override
	public String insertProject(Map<String, Object> params){
		String result = "success";
		String uuid = UUID.randomUUID().toString();
		params.put("projectId", uuid);
		try {
			projectMapper.insertProject(params);
		} catch (Exception e) {
			result = "error";
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 선택한 프로젝트 정보 가져오기
	 */
	public Map<String,Object> selectProject(Map<String, Object> params){
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			result = projectMapper.selectProject(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 선택한 프로젝트의 서브프로젝트 리스트
	 */
	public List<Map<String,Object>> subProjectList(Map<String, Object> params){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		try {
			result = projectMapper.subProjectList(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * insertSubProject 서브프로젝트 생성
	 */
	@Override
	public String insertSubProject(Map<String, Object> params){
		String result = "success";
		try {
			//update
			if (params.containsKey("subProjectId")) {
				projectMapper.updateSubProject(params);
			//insert
			} else {
				String uuid = UUID.randomUUID().toString();
				params.put("projectId", uuid);
				projectMapper.insertSubProject(params);
				File dir = new File(rootFilePath+"/"+uuid);
				dir.mkdir();
			}
		} catch (Exception e) {
			result="error";
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * copySubProject 서브프로젝트 생성
	 */
	@Override
	public String copySubProject(Map<String, Object> params){
		String result = "success";
		try {
			String uuid = UUID.randomUUID().toString();
			params.put("projectId", uuid);
			System.out.println(params);
			projectMapper.copySubProject(params);
			projectMapper.copyControlParam(params);
			projectMapper.copyControlColParam(params);
			File orgDir = new File(rootFilePath+"/"+params.get("copyProjectId").toString());
			File desDir = new File(rootFilePath+"/"+uuid);
			desDir.mkdir();
			copy(orgDir,desDir);
		} catch (Exception e) {
			result="error";
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String updateSubProject(Map<String, Object> params){
		String result = "success";
		if (params.containsKey("subProjectId")) {
			// update
			try {
				projectMapper.updateSubProject(params);
			} catch (Exception e) {
				result="error";
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public String deleteSubProject(Map<String, Object> params){
		String result = "success";
		// update
		if (params.containsKey("subProjectId")) {
			try {
				projectMapper.deleteSubProject(params);
				deleteFolder(rootFilePath+"/"+params.get("subProjectId").toString());
			} catch (Exception e) {
				result="error";
				e.printStackTrace();
			}
		}
		return result;
	}

	/*
	 * 선택한 서브프로젝트
	 */
	public Map<String,Object> getSubProjectInfo(Map<String, Object> params){
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			result = projectMapper.getSubProjectInfo(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * 공통함수 : 서브프로젝트의 contents값만 받아오기.
	 */
	public List<Map<String,Object>> getSubProjectContents(Map<String, Object> params){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		try {
			result= projectMapper.getSubProjectContents(params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	
	public String updateSubContents(Map<String, Object> params){
		String result = "success";
		try {
			String key = (String) params.get("key");
			Map<String,Object> getSubProjectInfo = getSubProjectInfo(params); // 기존 DB contents 컬럼 값
			JSONObject jsonObject = new JSONObject();
			String[] keyList = key.split("\\|");
			
			if(key.equals("linked")){
				String contents = getSubProjectInfo.get("CONTENTS").toString();
				jsonObject =  (JSONObject)JSONValue.parse(contents);
				String sourceId =(String)params.get("sourceId");
				String targetId =(String)params.get("targetId");
				System.out.println("sourceId :::"+sourceId+", targetId ::::"+targetId);	
				
				JSONObject sourceLv1Data = (JSONObject) jsonObject.get(sourceId);
				JSONObject targetLv1Data = (JSONObject) jsonObject.get(targetId);
				if(sourceLv1Data != null && targetLv1Data != null){
					sourceLv1Data.put("targetId", targetId);
					jsonObject.put(sourceId, sourceLv1Data);
					targetLv1Data.put("sourceId", sourceId);
					jsonObject.put(targetId, targetLv1Data);
				}else{
					jsonObject.put("sourceId", sourceId);
					jsonObject.put("targetId", targetId);
				}
				String updateContents = jsonObject.toString();
				params.put("contents", updateContents);
				projectMapper.updateSubContents(params);
			
			}else{
				if(getSubProjectInfo.get("CONTENTS") == null){ //처음 contetns col 값이 null 일때  => *처음생성을 '' 공백으로 줬지만 에러발생
					System.out.println("getSubProjectInfo is null");
					jsonObject.put((String) params.get("key"), params.get("value"));
					String updateContents = jsonObject.toString(); 
					params.put("contents", updateContents);
					projectMapper.updateSubContents(params);
				}else{
					System.out.println("getSubProjectInfo is not null");
					String contents = getSubProjectInfo.get("CONTENTS").toString();  //List 형식에서 contents 컬럼값을 String 으로 추출
					jsonObject =  (JSONObject)JSONValue.parse(contents);
					//System.out.println("DB 저장된 jsonObject값" + jsonObject);
					if(keyList.length == 1){ 
							jsonObject.put(keyList[0], params.get("value"));
							String updateContents = jsonObject.toString();
							params.put("contents", updateContents);
							projectMapper.updateSubContents(params);
			
					}else if(keyList.length == 2){
						if (jsonObject.containsKey(keyList[0])) { //objectId값.
							JSONObject Lv1Data = (JSONObject) jsonObject.get(keyList[0]); 
							Lv1Data.put(keyList[1], params.get("value"));
							jsonObject.put(keyList[0], Lv1Data);
							String updateContents = jsonObject.toString();
							params.put("contents", updateContents);
							projectMapper.updateSubContents(params); // 값이있을경우.
							
						}else{ //jsonobject에 해당 id값이 없을경우 (처음 넣을때)
							JSONObject Lv2Data = new JSONObject();
							Lv2Data.put(keyList[1], params.get("value"));
							jsonObject.put(keyList[0], Lv2Data); //기존 display 값을 유지하기위해 jsonObject에 바로 값을 넣어준다!! 06.08.
							String updateContents = jsonObject.toString();
							System.out.println("updateContents:::" + updateContents);
							params.put("contents", updateContents);
							projectMapper.updateSubContents(params); // 값이없을경우.
						}
					}
				}
			}
		} catch (Exception e) {
			result = "error";
			e.printStackTrace();
		}
		return result;
	}
	
	boolean checkString(String str){
		return str == null || str.isEmpty();
		
	}
	public void csvAction(Map<String, Object> params) throws Exception {

		String tableName =(String)params.get("tableName");
		String colValue =(String)params.get("value");
		System.out.println("colValue::::::::::"+colValue);
		String subPjtId =(String)params.get("subPjtId"); //csv 파일명으로 사용
		
		JSONArray jsonArray = (JSONArray)JSONValue.parse(colValue);
		List<Map<String, Object>> selectData = new ArrayList<>();
		
		JSONObject jsonObjectList = null;
		
		for(int i=0; i<jsonArray.size(); i++){
			jsonObjectList = (JSONObject)jsonArray.get(i);
			Iterator<?> iterator = jsonObjectList.keySet().iterator();
			Map<String,Object> mm = new HashMap<String,Object>();
			while(iterator.hasNext()){
				String key1 =(String)iterator.next();
				mm.put(key1, jsonObjectList.get(key1));
				if(key1.equals("colName")){
					mm.put("alias", jsonObjectList.get(key1)); // alias 추가해주기!
				}

			}
			selectData.add(mm);
		}

		OlapSelectObjectVO csvparamsVO = new OlapSelectObjectVO();
		csvparamsVO.setTbName01(tableName);
		csvparamsVO.setDetailInfo(selectData);
		System.out.println("csvparamsVO : "+csvparamsVO);
		List<Map<String, Object>> userSelectData = martMapper.getSelectGridData(csvparamsVO); //params:tableName, obj(Lv1Data value) =>     rowdata 정보 받아옴
		
		File file = new File("D:\\lev_ml");
		if(!file.exists()) {
			file.mkdir();
		}
		FileWriter csvWriter = new FileWriter("D:\\lev_ml\\"+subPjtId+".csv");
		//String filePath = "D:\\lev_ml\\"+subPjtId+".csv";
		//csvWriter.write("UTF-8");// 한글깨짐 방지.
		//csvWriter.write("\uFEFF");// 한글깨짐 방지.
		List<Map<String,Object>> columnInfo = csvparamsVO.getDetailInfo(); //getDetailInfo 정보를 불러와서  header정보만 추출해야함.
		
		LinkedHashMap<String, String> setVal = new LinkedHashMap<String, String>(); //순서대로 뽑기위해 LinkedHashMap 사용.
		for(int k=0;k<columnInfo.size();k++){
			String h = (String) columnInfo.get(k).get("objInfoName");
			setVal.put(h,h); 
		}
		
		//header START
		Collection<String> k = setVal.keySet();
		Iterator<String> keys = k.iterator();
		String header = "";
		List<String> headerList = new ArrayList<>();
		
		while( keys.hasNext()){ 
			String headerKeys = keys.next();
			headerList.add(headerKeys); //header값을 배열로 만든다.
			//System.out.println("headerKeys 헤더값: "+headerKeys);
			//System.out.println("headerKeys.size : "+headerList.size());
			if(keys.hasNext() == true){ //다음값이 있을경우 true 반환 아닐시 false.
				header += headerKeys +",";
			}else{
				header += headerKeys +"\r\n";
			}
		}
		csvWriter.append(header); 
		//header END
		
		//DecimalFormat form = new DecimalFormat("#.##");

		for (int i = 0; i < userSelectData.size(); i++) { //TODO 빈값 처리 필요 
			String rowdata = "";
			Map<String, Object> obj = userSelectData.get(i); // i번째 list
			//System.out.println("obj:::::::::::::::"+obj);
			for (int j = 0; j < headerList.size(); j++) {  
				String headerName = headerList.get(j); 
				if (j == headerList.size() - 1) { // 마지막일때 줄바꿈 처리
					//rowdata += (String) obj.get(headerName) + "\r\n"; // 마지막일때 줄바꿈
					rowdata += String.valueOf(obj.get(headerName)) + "\r\n"; // 마지막일때 줄바꿈
				} else { //중간일때 , 로 넣기
					//rowdata += (String) obj.get(headerName) + ",";
					rowdata += String.valueOf(obj.get(headerName)) + ",";
					
				}
			}
			//System.out.println("rowdata:::::::::::::::"+rowdata);
			csvWriter.append(rowdata);
		}
			
			
		csvWriter.close();
	}
	@Override
	public Map<String, Object> csvLoadAction(Map<String, Object> params) throws Exception {
		System.out.println("csvLoadAction start, Params:"+params);
		//String fileName = "";
		String filePath =(String)params.get("filePath");
		//String filePath = "";
		BufferedReader csvDataList = new BufferedReader(new FileReader(filePath)); //TODO 공통으로 사용하기위해 filepPath를 변수값으로 받아와서 사용.
		//filePath+"/"+fileName
		List<Map<String,Object>> content = new ArrayList<>();
		
		Map<String,Object> result = new HashMap<String,Object>();
		
		String line = "";
		String [] headerArr =null;
	    String [] lineArr=null;
	    int inum=0;
	    
	    while((line = csvDataList.readLine()) !=null){ //1. readLine()을 사용하여 한줄씩 읽는다
	    	inum++;
	    	if (inum <= 1){ //header 부분 추출
	    		headerArr = line.split(",");
	    		//System.out.println("headerArr.length :::"+headerArr.length);
	    		for(int j=0; j<headerArr.length; j++){
    				if(headerArr[j] != null){
    					//System.out.println("headerArr, result:"+headerArr[j]); //TODO  왜 null체크가 안되는것인가,,?
    					result.put("fileds", headerArr);
    				}
    				
	    		}
	    	};

	    	
	    	if (inum <= 1)  continue; // 헤더행 스킵
	    	//System.out.println("line::::"+line);
	    	Map<String,Object> getVal = new HashMap<String,Object>();
	    	lineArr = line.split(","); //2. ,를 기준으로 데이터   split
	    	//System.out.println("headerArr.length :::"+headerArr.length);
	    	//System.out.println("lineArr.length::::"+lineArr.length);
	    	for(int i=0; i<lineArr.length; i++){ //null 값이 있을 수 있기때문에  null체크는 하지않는다.
    			if(lineArr[i] != null){
    				getVal.put(headerArr[i], lineArr[i]);
    			}else if(lineArr[i] == null || lineArr[i] == ""){
    				getVal.put(headerArr[i], "");
    			}
    			//System.out.println("lineArr[i] ::::::::"+lineArr[i]);
	    	}
	    	
	    	content.add(getVal);
	    	result.put("record", content);
	    }
	    csvDataList.close();
		return result;
	    
	    
	}
	
	public void insertToModelInfo(Map<String, Object> params) throws Exception {
		
		
		
		Map<String, Object> setData = new HashMap<String, Object>(); //MAP 생성 후 데이터 담아주기
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("featureList", params.get("featureList"));
		String featureList = jsonObject.toString();
		String userId =(String)params.get("userId");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //년월일 형식 포맷
		Date time = new Date(); //현재 시간
		String ctDt = dateFormat.format(time); // 년월일 포맷에 현재시간 넣어주기
		
		setData.put("subProjectId", params.get("subProjectId"));
		setData.put("modelId", params.get("modelId"));
		setData.put("modelUrl", params.get("url"));
		setData.put("featureList", featureList);
		setData.put("createId", userId);
		setData.put("createDt", ctDt);	
		
		projectMapper.insertToModelInfo(setData);
		
	}
	
	public static void deleteFolder(String path) {
	    File folder = new File(path);
	    try {
			if(folder.exists()){
				File[] folder_list = folder.listFiles(); //파일리스트 얻어오기
				for (int i = 0; i < folder_list.length; i++) {
					if(folder_list[i].isFile()) {
						folder_list[i].delete();
					}else {
						deleteFolder(folder_list[i].getPath()); //재귀함수호출
					}
					folder_list[i].delete();
				}
				folder.delete(); //폴더 삭제
			}
	    } catch (Exception e) {
	    	e.getStackTrace();
	    }
	}
	
	public static void copy(File sourceF, File targetF){
		File[] ff = sourceF.listFiles();
		for (File file : ff) {
			File temp = new File(targetF.getAbsolutePath() + File.separator + file.getName());
			if(file.isDirectory()){
				temp.mkdir();
				copy(file, temp);
			} else {
				FileInputStream fis = null;
				FileOutputStream fos = null;
				try {
					fis = new FileInputStream(file);
					fos = new FileOutputStream(temp) ;
					byte[] b = new byte[4096];
					int cnt = 0;
					while((cnt=fis.read(b)) != -1){
						fos.write(b, 0, cnt);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally{
					try {
						fis.close();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
			}
		}
	}
}
