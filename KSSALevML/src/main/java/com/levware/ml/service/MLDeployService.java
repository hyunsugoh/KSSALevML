package com.levware.ml.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.hsqldb.lib.StringUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.levware.common.mappers.repo.MLDeployMapper;
import com.levware.common.mappers.repo.MLiframeMapper;
import com.levware.ml.service.impl.MLiframeServiceImpl;

@Service("mlDeployService")
public class MLDeployService {
	@Resource(name = "mlDeployMapper")
	private MLDeployMapper mlDpMapper;
	
	@Resource(name = "mLiframeMapper")
	private MLiframeMapper mLiFMapper;
	
	private static String profile=System.getProperty("server.os.profile");
	private static String rootFilePath=profile.equals("linux")?"/app/levml/csv/":"D:/lev_ml/";
	//로컬 PC
	//private static String host=profile.equals("linux")?"http://192.168.1.104:5100/":"http://localhost:5100/";
	//운영 PC
	private static String host="http://localhost:5100/";
	private static String fileEncode = "MS949";
	
	public String deploy(Map<String,Object> param){
		String msg = "success";
		try {
			mlDpMapper.deleteDeployM(param);
			mlDpMapper.deleteDeployD(param);
			mlDpMapper.insertDeployM(param);
			mlDpMapper.insertDeployD(param);
			saveControlParam(param);
			
			Map<String,Object> dpInfo = mlDpMapper.getDeployInfo(param);
			String subPjtNm = dpInfo.get("SUB_PJT_NM").toString();
			String dpNm = dpInfo.get("DEPLOY_NAME").toString();
			String dpDirPath = rootFilePath+"deploy/"+subPjtNm+"/"+dpNm+"/model";
			File dpDir = new File(dpDirPath);
			if(!dpDir.exists()) {
				dpDir.mkdirs();
			}
			
			String subPjtId = param.get("subpjtid").toString();
			String modelid = param.get("modelid").toString();
			String modelNm = param.get("modelnm").toString();
			File orgModelFile = new File(rootFilePath+subPjtId+"/model/"+modelid+".model");
			File modelFile = new File(rootFilePath+"deploy/"+subPjtNm+"/"+dpNm+"/model/"+modelNm+".model");
			File orgXMLFile = new File(rootFilePath+subPjtId+"/model/"+modelid+".xml");
			File xMLFile = new File(rootFilePath+"deploy/"+subPjtNm+"/"+dpNm+"/model/"+modelNm+".xml");
			
			if(modelFile.exists()) {
				modelFile.delete();
			}
			if(xMLFile.exists()) {
				xMLFile.delete();
			}
			copyFile(orgModelFile,modelFile);
			copyFile(orgXMLFile,xMLFile);
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		}
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> deployModelCSV(MultipartFile file,String subpjtid,String deployid, String userid, String ip){	
		Map<String,Object> result = new HashMap<String,Object>();
		
		// PREDICT 이후 사용하는 변수들
		FileInputStream prFis = null;
		InputStreamReader prIsr = null;
		BufferedReader prBr = null;
		
		// API 결과 받아올 때 사용하는 변수
		BufferedReader br = null;
		OutputStreamWriter osw = null;
		
		// ENCODER/DECODER 에서 사용
		BufferedWriter bw = null;
		
		String tempSubPjtId = UUID.randomUUID().toString()+"_temp";
		String tempSourceid = UUID.randomUUID().toString();
		String tempModelid = UUID.randomUUID().toString();
		String tempTargetid = UUID.randomUUID().toString();
		String tempDirPath = rootFilePath+tempSubPjtId;
		int dataSize = 0;
		try {
			File tempModelDir = new File(tempDirPath+"/model");
			tempModelDir.mkdirs();
			
			
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("subpjtid", subpjtid);
			param.put("deployid", deployid);
			param.put("userid", userid);
			
			
			Map<String,Object> dpInfo = mlDpMapper.getDeployInfo(param);
			String subPjtNm = dpInfo.get("SUB_PJT_NM").toString();
			String dpNm = dpInfo.get("DEPLOY_NAME").toString();
			String dpDirPath = rootFilePath+"deploy/"+subPjtNm+"/"+dpNm;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date dt = new Date();
			String dtStr = sdf.format(dt);
			
			String csvFilePath = dpDirPath+"/"+dtStr;
			File csvDir = new File(csvFilePath);
			if(!csvDir.exists()) {
				csvDir.mkdirs();
			}
			
			/*
			 * 페이지에서 추가한 파일을 temp 폴더로 복사
			 * 복사한 파일을 인코딩하여 인코딩된 파일을 temp 폴더에 생성.
			 */
			
			// 파일 이동 (업로드 파일 -> DEPLOY/yyyyMMdd)
			File csvFile = new File(csvFilePath+"/"+file.getOriginalFilename());
			file.transferTo(csvFile);
			
			// 파일 복사 (DEPLOY/yyyyMMdd -> temp)
			File orgnTempfile = new File(tempDirPath + "/" + file.getOriginalFilename());
			copyFile(csvFile, orgnTempfile);
			
			
			/* 
			 * ~인코딩~
			 * 
			 * 테스트에 사용될 parameter
			 * spid : b2346f82-3146-4d52-8955-4b8146d5acba
			 * dpid : f450e517-e645-401b-be93-1b783d13d278
			 * 
			 */
			
			// 인코더 블럭의 uid select -> param에 넣는다
			param.put("id", mlDpMapper.getEncoderModelId(param));
			
			// 파일의 헤더 정보
			List<String> encHeaders = getCSVHeader(orgnTempfile);
			
			// 인코딩 해야하는 컬럼 리스트
			List<String> encCols = mlDpMapper.getEncodedColumns(param);
			
			// 인코딩 해야할 컬럼의 인덱스
			List<Integer> encColIdx = new ArrayList<Integer>();
			encCols.forEach(x -> encColIdx.add(encHeaders.indexOf((String) x)));
			
			
			// 인코딩 해야할 컬럼이 없으면 인코딩 생략
			if(encColIdx.size() != 0) {
				
				// 파일의 데이터 가져오기
				List<Map<String, Object>> encData = getCSVData(orgnTempfile);
				
				int colCnt = encHeaders.size();
				// 인코딩한다
				for(Integer col : encColIdx) {
					String key = col.toString();
					
					List<Map<String, Object>> encList = new ArrayList<Map<String, Object>>();
					
					// 데이터를 더미테이블에 넣기 위해 map에 저장.
					for(int i=0; i < encData.size(); i++) {
						String val = encData.get(i).get(key).toString();
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("ORDNO", i);
						map.put("VAL", val);
						encList.add(map);
					}
					
					// 더미테이블에 저장된 데이터 삭제
					mLiFMapper.deleteDummyData();
					
					if(encData.size() < 1000) {
						Map<String,Object> dummyMap = new HashMap<String,Object>();
						dummyMap.put("dummy_list", encList);
						mLiFMapper.insertDummyData(dummyMap);
					} else {
						Map<String,Object> dummyMap = new HashMap<String,Object>();
						List<Map<String,Object>> dummyList = new ArrayList<Map<String,Object>>();
						for(int i=0; i<encList.size(); i++) {
							dummyList.add(encList.get(i));
							if(i%1000==0) {
								dummyMap.put("dummy_list", dummyList);
								mLiFMapper.insertDummyData(dummyMap);
								
								dummyMap = new HashMap<String,Object>();
								dummyList = new ArrayList<Map<String,Object>>();
							}
						}
						dummyMap.put("dummy_list", dummyList);
						mLiFMapper.insertDummyData(dummyMap);
					}
					mLiFMapper.mergeDictionaryData(param);
					List<String> rList = mLiFMapper.getEncodedValueList(param);
					int cnt = 0;
					for(Map<String,Object> map : encData) {
						map.put(Integer.toString(colCnt), rList.get(cnt++));
					}
					encHeaders.add(encHeaders.get(col) + "_enc");
					colCnt++;
				}
				
				
				// 이녀석이 있어야 pedict 가능 !@#!@#!@#@!#
				File tempCsvFile = new File(tempDirPath+"/"+tempSourceid+".csv");
				
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempCsvFile), fileEncode));
				
				
				// 파일에 헤더 추가
				String headerStr = StringUtils.arrayToDelimitedString(encHeaders.toArray(), ",");
				bw.write(headerStr);
				bw.newLine();
				
				for(int i=0;i<encData.size();i++) {
					List<String> dataList = new ArrayList<String>();
					for(int idx=0; idx<encHeaders.size(); idx++) {
						dataList.add(encData.get(i).get(Integer.toString(idx)).toString());
					}
					bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
					bw.newLine();
				}
				bw.flush();
				bw.close();
				
				/* 인코더 끝 */
				
			} else {
				File tempCsvFile = new File(tempDirPath+"/"+tempSourceid+".csv");
				copyFile(orgnTempfile, tempCsvFile);
			}
			
			
			
			String modelNm = "";
			String modelDirPath = dpDirPath+"/model";
			File modelDir = new File(modelDirPath);
			String[] files = modelDir.list();
			int fileCnt = files.length;
			for(int i=0;i<fileCnt;i++) {
				if(files[i].endsWith(".model")) {
					modelNm=files[i];
					break;
				}
			}
			
			// 모델파일 복사 (deploy folder -> temp folder)
			File tempModelFile = new File(tempDirPath+"/model/"+tempModelid+".model");
			File modelFile = new File(dpDirPath+"/model/"+modelNm);
			copyFile(modelFile,tempModelFile);
			
			
			// API 호출에 필요한 항목 SELECT
			String modelid = mlDpMapper.getModelId(param);
			param.put("modelid", modelid);
			String apiUrl = mlDpMapper.getAPIUrl(param);
			List<String> features = mlDpMapper.getFeatures(param);
			String label = mlDpMapper.getLabel(param);
			
			
			// API 호출
			URL url = new URL(host+apiUrl); // 호출할 url
			JSONObject jObj = new JSONObject();
			jObj.put("subpjtid", tempSubPjtId);
			jObj.put("sourceid", tempSourceid);
			jObj.put("modelid", tempModelid);
			jObj.put("targetid", tempTargetid);
			jObj.put("label", label);
			jObj.put("userid", "adminSuper");
			JSONArray jArr = new JSONArray();
			for(String f : features) {
				jArr.add(f);
			}
			jObj.put("features", jArr);
			String jParam = jObj.toString();
			
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
			if(rtObj.get("status").equals("success")) {
				String prCSVFilePath = dpDirPath+"/"+dtStr;
				File prCSVDir = new File(prCSVFilePath);
				if(!prCSVDir.exists()) {
					prCSVDir.mkdir();
				}
				// predict 된 파일 경로
				File tempTargetCSVFile = new File(tempDirPath+"/predict/"+tempTargetid+".csv");
				
				/*
				 * DECODER 추가
				 */
				
				// predict 완료된 파일에서 header 정보 get
				List<String> allPredHeaders = getCSVHeader(tempTargetCSVFile);
				
				// predict 완료된 데이터
				List<Map<String,Object>> allPredData = getCSVData(tempTargetCSVFile);
				
				// _pred로 끝나는 헤더 찾기
				List<String> tmpHeaders = allPredHeaders.stream()
															.filter(item -> { return item.matches(".*_pred"); })
															.collect(Collectors.toList());
				
				
				// 헤더가 한개이면 첫번째 헤더 사용 / 아니면 가장 마지막 헤더 사용
				String predHeader = tmpHeaders.size() == 1 ? tmpHeaders.get(0) : tmpHeaders.get(tmpHeaders.size()-1);
				String predIdx = Integer.toString(allPredHeaders.indexOf(predHeader));
				

				if(predHeader.matches(".*_enc_pred")) {
					// 인코딩 후 predict 된 컬럼이 있는 경우-> 디코딩 후 파일에 추가
					
					/* 디코딩 */
					List<Map<String, Object>> decList = new ArrayList<Map<String, Object>>();
					
					for(int i=0;i<allPredData.size();i++) {
						String value = allPredData.get(i).get(predIdx).toString();
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("ORDNO", i);
						map.put("VAL", value);
						decList.add(map);
					}
					
					mLiFMapper.deleteDummyData();
					
					if(allPredData.size()<1000) {
						Map<String,Object> dummyMap = new HashMap<String,Object>();
						dummyMap.put("dummy_list", decList);
						mLiFMapper.insertDummyData(dummyMap);
					}else {
						Map<String,Object> dummyMap = new HashMap<String,Object>();
						List<Map<String,Object>> dummyList = new ArrayList<Map<String,Object>>();
						for(int i=0;i<decList.size();i++) {
							dummyList.add(decList.get(i));
							if(i%1000==0) {
								dummyMap.put("dummy_list", dummyList);
								mLiFMapper.insertDummyData(dummyMap);
								dummyMap = new HashMap<String,Object>();
								dummyList = new ArrayList<Map<String,Object>>();
							}
						}
						dummyMap.put("dummy_list", dummyList);
						mLiFMapper.insertDummyData(dummyMap);
					}
					List<String> rList = mLiFMapper.getDecodedValueList(param);			// param - subpjid만 사용
					
					/* 디코딩 */
					
					// 원래 파일 데이터
					List<String> orgHeader = getCSVHeader(orgnTempfile);
					List<Map<String, Object>> orgData = getCSVData(orgnTempfile);
					dataSize = orgData.size();
					
					// 디코딩 한 컬럼 이름 변경 및 추가 (A_pred -> A_pred)
					orgHeader.add(predHeader.replace("_enc_pred", "_pred"));
					
					bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(orgnTempfile), fileEncode));
					
					// 파일에 헤더 추가
					String headerStr = StringUtils.arrayToDelimitedString(orgHeader.toArray(), ",");
					bw.write(headerStr);
					bw.newLine();
					
					for(int i=0;i<orgData.size();i++) {
						List<String> dataList = new ArrayList<String>();
						for(int idx=0; idx<orgData.get(i).size(); idx++) {
							dataList.add(orgData.get(i).get(Integer.toString(idx)).toString());							
						}
						
						
						dataList.add(rList.get(i));
						
						bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
						bw.newLine();
					}
					bw.flush();
					bw.close();
					
				} else {
					
					// 원래 파일 데이터
					List<String> orgHeader = getCSVHeader(orgnTempfile);
					List<Map<String, Object>> orgData = getCSVData(orgnTempfile);
					dataSize = orgData.size();
					
					orgHeader.add(predHeader);
					
					
					bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(orgnTempfile), fileEncode));
					
					// 파일에 헤더 추가
					String headerStr = StringUtils.arrayToDelimitedString(orgHeader.toArray(), ",");
					bw.write(headerStr);
					bw.newLine();
					
					for(int i=0;i<orgData.size();i++) {
						List<String> dataList = new ArrayList<String>();
						for(int idx=0; idx<orgData.get(i).size(); idx++) {
							dataList.add(orgData.get(i).get(Integer.toString(idx)).toString());
						}
						dataList.add(allPredData.get(i).get(predIdx).toString());
						
						bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
						bw.newLine();
					}
					bw.flush();
					bw.close();
					
				}
			
				/*
				 *  temp의 원본 파일 불러와서 덮어쓰기
				 *  1. temp 파일 불러오기
				 *  2. Header 추가 
				 *  3. row만큼 돌면서 마지막에 rList(i) 추가해서 쓰기 
				 */
				
				File prCSVFile = new File(prCSVFilePath+"/pred_"+file.getOriginalFilename());
				copyFile(orgnTempfile,prCSVFile);
				
				List<String> header = new ArrayList<String>();
				prFis = new FileInputStream(prCSVFile);
				prIsr = new InputStreamReader(prFis,fileEncode);
				prBr = new BufferedReader(prIsr);
				String line = prBr.readLine();
				if(line!=null) {
					String cols[] = StringUtil.split(line, ",");
					for(int i=0;i<cols.length;i++) {
						header.add(cols[i]);
					}
				}
				List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
				while((line = prBr.readLine()) != null){
					Map<String,Object> map = new HashMap<String,Object>();
					String cols[] = StringUtil.split(line, ",");
					for(int i=0;i<cols.length;i++) {
						String idx = Integer.toString(i);
						map.put(idx, cols[i]);
					}
					data.add(map);
				}
				result.put("status", rtObj.get("status"));
				result.put("message", rtObj.get("message"));
				result.put("header", header);
				result.put("data", data);
			}else {
				result.put("status", rtObj.get("status"));
				result.put("message", rtObj.get("message"));
			}
			
			Map<String,Object> logParam = new HashMap<String,Object>();
			logParam.put("DEPLOY_ID",deployid);
			logParam.put("HOST_IP",ip);
			logParam.put("STATUS","success");
			logParam.put("DATA_SIZE",dataSize);
			logParam.put("userid",userid);
			mlDpMapper.insertDeployLog(logParam);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String,Object> logParam = new HashMap<String,Object>();
			logParam.put("DEPLOY_ID",deployid);
			logParam.put("HOST_IP",ip);
			logParam.put("STATUS","error");
			logParam.put("DATA_SIZE",dataSize);
			logParam.put("userid",userid);
			mlDpMapper.insertDeployLog(logParam);
		} finally {
			try {
				deleteFolder(tempDirPath);
				br.close();
				osw.close();
				
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> deployModelData(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		FileInputStream prFis = null;
		InputStreamReader prIsr = null;
		BufferedReader prBr = null;
		
		BufferedReader br = null;
		OutputStreamWriter osw = null;
		
		FileOutputStream tFos = null;
		OutputStreamWriter tOsw = null;
		BufferedWriter tBw = null;
		
		String tempSubPjtId = UUID.randomUUID().toString()+"_temp";
		String tempSourceid = UUID.randomUUID().toString();
		String tempModelid = UUID.randomUUID().toString();
		String tempTargetid = UUID.randomUUID().toString();
		String tempDirPath = rootFilePath+tempSubPjtId;
		
		String subpjtid = param.get("subpjtid").toString();
		String deployid = param.get("deployid").toString();
		int dataSize = 0;
		try {
			File tempModelDir = new File(tempDirPath+"/model");
			tempModelDir.mkdirs();
			
			param.put("subpjtid", subpjtid);
			param.put("deployid", deployid);
			Map<String,Object> dpInfo = mlDpMapper.getDeployInfo(param);
			System.out.println("getDeployInfogetDeployInfogetDeployInfogetDeployInfo");
			System.out.println(dpInfo);
			String subPjtNm = dpInfo.get("SUB_PJT_NM").toString();
			String dpNm = dpInfo.get("DEPLOY_NAME").toString();
			String dpDirPath = rootFilePath+"deploy/"+subPjtNm+"/"+dpNm;
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date dt = new Date();
			String dtStr = sdf.format(dt);
			
			String csvFilePath = dpDirPath+"/"+dtStr;
			File csvDir = new File(csvFilePath);
			if(!csvDir.exists()) {
				csvDir.mkdir();
			}
			File tempCsvFile = new File(tempDirPath+"/"+tempSourceid+".csv");
			tFos = new FileOutputStream(tempCsvFile);
			tOsw = new OutputStreamWriter(tFos,fileEncode);
			tBw = new BufferedWriter(tOsw);
			
			List<String> hList = (List<String>)param.get("header");
			String headerStr = StringUtils.arrayToDelimitedString(hList.toArray(), ",");
			tBw.write(headerStr);
			tBw.newLine();
			
			List<Map<String,Object>> dList = (List<Map<String,Object>>)param.get("data");
			dataSize = dList.size();
			
			/* 임시 소스 */
			dList = new ArrayList<Map<String,Object>>();
			Map<String,Object> tmpMap = new HashMap<String,Object>();
			for(String key : hList) {
				tmpMap.put(key, 0);
			}
			dList.add(tmpMap);
			
			for(int i=0;i<dList.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : dList.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				tBw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				tBw.newLine();
			}
			tBw.flush();
			tBw.close();
			
			String modelNm = "";
			String modelDirPath = dpDirPath+"/model";
			File modelDir = new File(modelDirPath);
			String[] files = modelDir.list();
			int fileCnt = files.length;
			for(int i=0;i<fileCnt;i++) {
				if(files[i].endsWith(".model")) {
					modelNm=files[i];
					break;
	            }
			}
			File tempModelFile = new File(tempDirPath+"/model/"+tempModelid+".model");
			File modelFile = new File(dpDirPath+"/model/"+modelNm);
			copyFile(modelFile,tempModelFile);
			
			String modelid = mlDpMapper.getModelId(param);
			param.put("modelid", modelid);
			String apiUrl = mlDpMapper.getAPIUrl(param);
			List<String> features = mlDpMapper.getFeatures(param);
			String label = mlDpMapper.getLabel(param);
			
			URL url = new URL(host+apiUrl); // 호출할 url
			JSONObject jObj = new JSONObject();
			jObj.put("subpjtid", tempSubPjtId);
			jObj.put("sourceid", tempSourceid);
			jObj.put("modelid", tempModelid);
			jObj.put("targetid", tempTargetid);
	        jObj.put("label", label);
	        jObj.put("userid", "adminSuper");
	        JSONArray jArr = new JSONArray();
	        for(String f : features) {
	        	jArr.add(f);
	        }
	        jObj.put("features", jArr);
	        String jParam = jObj.toString();
	        
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
	        if(rtObj.get("status").equals("success")) {
	        	String prCSVFilePath = dpDirPath+"/"+dtStr;
				File prCSVDir = new File(prCSVFilePath);
				if(!prCSVDir.exists()) {
					prCSVDir.mkdir();
				}
	        	File tempTargetCSVFile = new File(tempDirPath+"/predict/"+tempTargetid+".csv");
				File prCSVFile = new File(prCSVFilePath+"/pred_"+tempSourceid+".csv");
				copyFile(tempTargetCSVFile,prCSVFile);
				
				List<String> header = new ArrayList<String>();
				prFis = new FileInputStream(prCSVFile);
				prIsr = new InputStreamReader(prFis,fileEncode);
				prBr = new BufferedReader(prIsr);
		        String line = prBr.readLine();
		        if(line!=null) {
		        	String cols[] = StringUtil.split(line, ",");
		            for(int i=0;i<cols.length;i++) {
		            	header.add(cols[i]);
		            }
		        }
		        List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		        while((line = prBr.readLine()) != null){
		        	Map<String,Object> map = new HashMap<String,Object>();
		            String cols[] = StringUtil.split(line, ",");
		            for(int i=0;i<cols.length;i++) {
		            	String idx = Integer.toString(i);
		            	map.put(idx, cols[i]);
		            }
		            data.add(map);
		        }
		        result.put("status", rtObj.get("status"));
		        result.put("message", rtObj.get("message"));
		        result.put("header", header);
		        result.put("data", data);
	        }else {
	        	result.put("status", rtObj.get("status"));
	        	result.put("message", rtObj.get("message"));
	        }
	        
	        Map<String,Object> logParam = new HashMap<String,Object>();
	        logParam.put("DEPLOY_ID",deployid);
	        logParam.put("HOST_IP",param.get("ip").toString());
	        logParam.put("STATUS","success");
	        logParam.put("DATA_SIZE",dataSize);
	        mlDpMapper.insertDeployLog(logParam);
		} catch (Exception e) {
			e.printStackTrace();
			Map<String,Object> logParam = new HashMap<String,Object>();
	        logParam.put("DEPLOY_ID",deployid);
	        logParam.put("HOST_IP",param.get("ip").toString());
	        logParam.put("STATUS","error");
	        logParam.put("DATA_SIZE",dataSize);
	        mlDpMapper.insertDeployLog(logParam);
		} finally {
			try {
				deleteFolder(tempDirPath);
				br.close();
				osw.close();
				tFos.close();
				tOsw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public void updateDeployItemDetail(Map<String,Object> param){
		mlDpMapper.updateDeployItemDetail(param);
	}
	
	public List<Map<String,Object>> getDeployLog(Map<String,Object> param){
		return mlDpMapper.getDeployLog(param);
	}
	
	public String getAPIKey(Map<String,Object> param){
		return mlDpMapper.getAPIKey(param);
	}
	
	public List<Map<String,Object>> getDeployList(Map<String,Object> param){
		return mlDpMapper.getDeployList(param);
	}
	
	public List<Map<String,Object>> getDeployListDetail(Map<String,Object> param){
		return mlDpMapper.getDeployListDetail(param);
	}
	
	public List<Map<String,Object>> dl002(String msg){
		System.out.println(msg);
		return new ArrayList<Map<String,Object>>();
	}
	
	public List<Map<String,Object>> dm002(String msg){
		System.out.println(msg);
		return new ArrayList<Map<String,Object>>();
	}
	
	@SuppressWarnings("unchecked")
	public void saveControlParam(Map<String,Object> param){
		Map<String,Object> controlParam = (Map<String,Object>)param.get("controlparam");
		String subPjtId = param.get("subpjtid").toString();
		String modelUid = param.get("modeluid").toString();
		String gridId = "controlGrid";
		if(param.containsKey("gridid") && !param.get("gridid").equals(null)) {
			gridId=param.get("gridid").toString();
		}else {
			param.put("gridid", gridId);
		}
		List<Map<String,Object>> params = (List<Map<String,Object>>)controlParam.get("params");
		
		mLiFMapper.deleteControlParam(param);
		for(Map<String,Object> map : params) {
			map.put("SUB_PJT_ID", subPjtId);
			map.put("MODEL_UID", modelUid);
			map.put("userid", param.get("userid"));
			mLiFMapper.insertControlParam(map);
		}
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
	
	public static void copyFile(File orgFile, File desFile) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(orgFile);
			fos = new FileOutputStream(desFile) ;
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

	List<String> getCSVHeader(File file) {
		List<String> header = new ArrayList<String>();
		
		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try {
			if(file.exists()) {
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis, fileEncode);
				br = new BufferedReader(isr);
				
		        String line = br.readLine();
		        if(line!=null) {
		        	String cols[] = StringUtil.split(line, ",");
		            for(int i=0;i<cols.length;i++) {
		            	header.add(cols[i]);
		            }
		        }
		        
		        br.close();
				isr.close();
				fis.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					fis.close();
					isr.close();
					br.close();
				}
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return header;
	}
	
	
	
	List<Map<String, Object>> getCSVData(File file) {
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		
		BufferedReader br = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		
		try {
			if(file.exists()) {
				fis = new FileInputStream(file);
				isr = new InputStreamReader(fis,fileEncode);
				br = new BufferedReader(isr);
				
		        String line = "";
		        int cnt = 0;
		        while((line = br.readLine()) != null){
		        	if(cnt>0) {
		        		Map<String,Object> map = new HashMap<String,Object>();
			            String cols[] = StringUtil.split(line, ",");
			            for(int i=0;i<cols.length;i++) {
			            	String idx = Integer.toString(i);
			            	if(MLiframeServiceImpl.isNumeric(cols[i])) {
			            		map.put(idx, MLiframeServiceImpl.fmt(Double.parseDouble(cols[i])));
			            	} else {
			            		map.put(idx, cols[i]);
			            	}
			            }
			            data.add(map);
		        	}else {
		        		cnt++;
		        	}
		        }
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(file.exists()) {
				try {
					br.close();
					fis.close();
					isr.close();					
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return data;
	}
	
}

