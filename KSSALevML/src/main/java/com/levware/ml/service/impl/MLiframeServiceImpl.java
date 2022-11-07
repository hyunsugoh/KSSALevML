package com.levware.ml.service.impl;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.hsqldb.lib.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.levware.common.mappers.repo.MLiframeMapper;
import com.levware.ml.service.MLiframeService;

@Service("mLiframeService")
public class MLiframeServiceImpl implements MLiframeService {

	@Resource(name = "mLiframeMapper")
	private MLiframeMapper mLiFMapper;
	
	private FileInputStream fis = null;
	private InputStream is = null;
	private InputStreamReader isr = null;
	private BufferedReader br = null;
	private FileOutputStream fos = null;
	private OutputStreamWriter osw = null;
	private BufferedWriter bw = null;
	
	private static String profile=System.getProperty("server.os.profile");
	private static String rootFilePath=profile.equals("linux")?"/app/levml/csv/":"D:/lev_ml/";
	private static String fileEncode = "MS949";
	
	public List<Map<String,Object>> getTableList(Map<String,Object> param){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		try {
			result = mLiFMapper.getTableList(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public List<Map<String,Object>> getTableColInfo(Map<String,Object> param){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		try {
			result = mLiFMapper.getTableColInfo(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public String saveTableData(Map<String,Object> param){
		String msg = "success";
		File file = null;
		try {
			List<Map<String,Object>> result = mLiFMapper.getTableData(param);
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			List<String> headerList = (List<String>)param.get("list");
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(Map<String,Object> map : result) {
				List<String> dataList = new ArrayList<String>();
				for(String key : headerList) {
					if(map.containsKey(key) && !map.get(key).equals(null)) {
						String value = map.get(key).toString(); 
						if(isNumeric(value)) {
							dataList.add(fmt(Double.parseDouble(value)));
						}else {
							dataList.add(value);
						}
					}else {
						dataList.add("");
					}
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		}catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				osw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	public List<Map<String,Object>> getCSVColInfo(Map<String,Object> param){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		File file = null;
		try {
			if(param.containsKey("type")) {
				param.put("gridid", param.get("type").toString()+"Grid");
			}
			List<String> colList = getParamColList(param);
			if(param.containsKey("PRYN")) {
				file = getPredictCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			}else {
				file = getCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			}
			if(file.exists()) {
				br = getBufferedReader(file);
		        String line = br.readLine();
		        if(line!=null) {
		        	String cols[] = StringUtil.split(line, ",");
		            for(int i=0;i<cols.length;i++) {
		            	Map<String,Object> map = new HashMap<String,Object>();
		            	map.put("COL_NAME",cols[i]);
		            	for(String colName : colList) {
		            		if(cols[i].equals(colName)) {
		            			map.put("SEL_CHK","Y");
		            			break;
		            		}
		            	}
		            	data.add(map);
		            }
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					br.close();
					isr.close();
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public List<Map<String,Object>> getParamList(Map<String,Object> param){
		return mLiFMapper.getParamList(param);
	}
	
	public List<String> getParamColList(Map<String,Object> param){
		if(!param.containsKey("gridid")) {
			param.put("gridid", "controlGrid");
		}
		return mLiFMapper.getParamColList(param);
	}
	
	@SuppressWarnings("unchecked")
	public String saveSQLData(Map<String,Object> param){
		String msg = "success";
		File file = null;
		try {
			List<Map<String,Object>> result = mLiFMapper.getSQLData(param);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			List<String> headerList = (List<String>)param.get("list");
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(Map<String,Object> map : result) {
				List<String> dataList = new ArrayList<String>();
				for(String key : headerList) {
					if(map.containsKey(key) && !map.get(key).equals(null)) {
						String value = map.get(key).toString(); 
						if(isNumeric(value)) {
							dataList.add(fmt(Double.parseDouble(value)));
						}else {
							dataList.add(value);
						}
					}else {
						dataList.add("");
					}
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {
				bw.close();
				osw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	public String removeModelCSV(String subPjtId, String modelUid, String modelType){
		String msg = "success";
		try {
			if(modelType.equals("dm005")) {
				File trFile = getCSVFile(subPjtId,modelUid+"_tr");
				if(trFile.exists()) {
					trFile.delete();
				}
				File teFile = getCSVFile(subPjtId,modelUid+"_te");
				if(teFile.exists()) {
					teFile.delete();
				}
			}else if(modelType.contains("tr")){
				File file = getModelCSVFile(subPjtId,modelUid);
				if(file.exists()) {
					file.delete();
				}
				File xmlFile = new File(rootFilePath+subPjtId+"/model/"+modelUid+".xml");
				if(xmlFile.exists()) {
					xmlFile.delete();
				}
				File modelFile = new File(rootFilePath+subPjtId+"/model/"+modelUid+".model");
				if(modelFile.exists()) {
					modelFile.delete();
				}
				File visualFile = new File(rootFilePath+subPjtId+"/visualization/"+modelUid+".png");
				if(visualFile.exists()) {
					visualFile.delete();
				}
			}else if(modelType.contains("pr")){
				File file = getPredictCSVFile(subPjtId,modelUid);
				if(file.exists()) {
					file.delete();
				}
			}else {
				File file = getCSVFile(subPjtId,modelUid);
				if(file.exists()) {
					file.delete();
				}
			}
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		}
		return msg;
	}
	
	public List<Map<String,Object>> getCSVInfo(MultipartFile file){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		try {
			is = file.getInputStream();
			isr = new InputStreamReader(is,fileEncode);
			br = new BufferedReader(isr);
			String line = br.readLine();
			if(line!=null) {
				String cols[] = StringUtil.split(line, ",");
	            for(int i=0;i<cols.length;i++) {
	            	Map<String,Object> map = new HashMap<String,Object>();
	            	map.put("COL_NAME",cols[i]);
	            	data.add(map);
	            }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
				isr.close();
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public List<Map<String,Object>> getCSVMeta(File file){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		try {
			br = getBufferedReader(file);
			String line = br.readLine();
			if(line!=null) {
				String cols[] = StringUtil.split(line, ",");
	            for(int i=0;i<cols.length;i++) {
	            	Map<String,Object> map = new HashMap<String,Object>();
	            	map.put("COL_NAME",cols[i]);
	            	data.add(map);
	            }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public String uploadCSV(MultipartFile file, String subPjtId, String modelUid, List<Integer> list, List<String> paramCols, String userid){
		String msg = "success";
		File sFile = null;
		try {
			String cFilePath = rootFilePath+subPjtId+"/upload";
			File cDir = new File(cFilePath);
			if(!cDir.exists()) {
				cDir.mkdir();
			}
			cFilePath=cFilePath+"/"+file.getOriginalFilename();
			File cFile = new File(cFilePath);
			if(cFile.exists()) {
				cFile.delete();
			}
			
			sFile = getCSVFile(subPjtId,modelUid);
			if(sFile.exists()) {
				sFile.delete();
			}
			bw = getBufferedWriter(sFile);
			is = file.getInputStream();
			isr = new InputStreamReader(is,fileEncode);
			br = new BufferedReader(isr);
			String line = "";
			while((line = br.readLine()) != null){
				List<String> data = new ArrayList<String>();
				String cols[] = StringUtil.split(line, ",");
				int cnt = 0;
	            for(int i=0;i<cols.length;i++) {
	            	if(list.size()>cnt && i==list.get(cnt)) {
	            		if(isNumeric(cols[i])) {
	            			data.add(fmt(Double.parseDouble(cols[i])));
	            		}else {
	            			data.add(cols[i]);
	            		}
		            	cnt++;
	            	}
	            }
	            String dataStr = StringUtils.arrayToDelimitedString(data.toArray(), ",");
				bw.write(dataStr);
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			file.transferTo(cFile);
			
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("subpjtid", subPjtId);
			param.put("modeluid", modelUid);
			param.put("SUB_PJT_ID", subPjtId);
			param.put("MODEL_UID", modelUid);
			param.put("userid", userid);
			param.put("gridid", "controlGrid");
			param.put("id", "input_files");
			param.put("value", file.getOriginalFilename());
			
			mLiFMapper.deleteControlParam(param);
			mLiFMapper.insertControlParam(param);
			
			mLiFMapper.deleteControlColParam(param);
			for(String col : paramCols) {
				param.put("INPUT_COL", col);
				param.put("GRID_ID", "controlGrid");
				mLiFMapper.insertControlColParam(param);
			}
		}catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {
				br.close();
				isr.close();
				is.close();				
				bw.close();
				osw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	public List<String> getCSVHeader(String subPjtId, String modelUid){
		List<String> header = new ArrayList<String>();
		File file = null;
		try {
			file = getCSVFile(subPjtId,modelUid);
			if(file.exists()) {
				br = getBufferedReader(file);
		        String line = br.readLine();
		        if(line!=null) {
		        	String cols[] = StringUtil.split(line, ",");
		            for(int i=0;i<cols.length;i++) {
		            	header.add(cols[i]);
		            }
		        }
			}else {
				file = getPredictCSVFile(subPjtId,modelUid);
				if(file.exists()) {
					br = getBufferedReader(file);
			        String line = br.readLine();
			        if(line!=null) {
			        	String cols[] = StringUtil.split(line, ",");
			            for(int i=0;i<cols.length;i++) {
			            	header.add(cols[i]);
			            }
			        }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					br.close();
					isr.close();
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return header;
	}
	
	public List<String> getModelCSVHeader(String subPjtId, String modelUid){
		List<String> header = new ArrayList<String>();
		File file = null;
		try {
			file = getModelCSVFile(subPjtId,modelUid);
			if(file.exists()) {
				br = getBufferedReader(file);
		        String line = br.readLine();
		        if(line!=null) {
		        	String cols[] = StringUtil.split(line, ",");
		            for(int i=0;i<cols.length;i++) {
		            	header.add(cols[i]);
		            }
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					br.close();
					isr.close();
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return header;
	}
	
	public List<Map<String,Object>> getCSVData(String subPjtId, String modelUid){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		File file = null;
		try {
			file = getCSVFile(subPjtId,modelUid);
			if(file.exists()) {
				br = getBufferedReader(file);
		        String line = "";
		        int cnt = 0;
		        while((line = br.readLine()) != null){
		        	if(cnt>0) {
		        		Map<String,Object> map = new HashMap<String,Object>();
			            String cols[] = StringUtil.split(line, ",");
			            for(int i=0;i<cols.length;i++) {
			            	String idx = Integer.toString(i);
			            	map.put(idx, cols[i]);
			            }
			            data.add(map);
		        	}else {
		        		cnt++;
		        	}
		        }
			}else {
				file = getPredictCSVFile(subPjtId,modelUid);
				if(file.exists()) {
					br = getBufferedReader(file);
			        String line = "";
			        int cnt = 0;
			        while((line = br.readLine()) != null){
			        	if(cnt>0) {
			        		Map<String,Object> map = new HashMap<String,Object>();
				            String cols[] = StringUtil.split(line, ",");
				            for(int i=0;i<cols.length;i++) {
				            	String idx = Integer.toString(i);
				            	map.put(idx, cols[i]);
				            }
				            data.add(map);
			        	}else {
			        		cnt++;
			        	}
			        }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					br.close();
					isr.close();
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public List<Map<String,Object>> getModelCSVData(String subPjtId, String modelUid){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		File file = null;
		try {
			file = getModelCSVFile(subPjtId,modelUid);
			if(file.exists()) {
				br = getBufferedReader(file);
		        String line = "";
		        int cnt = 0;
		        while((line = br.readLine()) != null){
		        	if(cnt>0) {
		        		Map<String,Object> map = new HashMap<String,Object>();
			            String cols[] = StringUtil.split(line, ",");
			            for(int i=0;i<cols.length;i++) {
			            	String idx = Integer.toString(i);
			            	map.put(idx, cols[i]);
			            }
			            data.add(map);
		        	}else {
		        		cnt++;
		        	}
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					br.close();
					isr.close();
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}
	
	public List<String> getPredictCSVHeader(String subPjtId, String modelUid){
		List<String> header = new ArrayList<String>();
		File file = null;
		try {
			file = getPredictCSVFile(subPjtId,modelUid);
			if(file.exists()) {
				br = getBufferedReader(file);
		        String line = br.readLine();
		        if(line!=null) {
		        	String cols[] = StringUtil.split(line, ",");
		            for(int i=0;i<cols.length;i++) {
		            	header.add(cols[i]);
		            }
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					br.close();
					isr.close();
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return header;
	}
	
	public List<Map<String,Object>> getPredictCSVData(String subPjtId, String modelUid){
		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
		File file = null;
		try {
			file = getPredictCSVFile(subPjtId,modelUid);
			if(file.exists()) {
				br = getBufferedReader(file);
		        String line = "";
		        int cnt = 0;
		        while((line = br.readLine()) != null){
		        	if(cnt>0) {
		        		Map<String,Object> map = new HashMap<String,Object>();
			            String cols[] = StringUtil.split(line, ",");
			            for(int i=0;i<cols.length;i++) {
			            	String idx = Integer.toString(i);
			            	if(isNumeric(cols[i])) {
			            		map.put(idx, fmt(Double.parseDouble(cols[i])));
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					br.close();
					isr.close();
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
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
		
		if(controlParam.containsKey("cols")){
			List<String> inputCols = (List<String>)controlParam.get("cols");
			mLiFMapper.deleteControlColParam(param);
			for(String col : inputCols) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("SUB_PJT_ID", subPjtId);
				map.put("MODEL_UID", modelUid);
				map.put("userid", param.get("userid"));
				map.put("INPUT_COL", col);
				map.put("GRID_ID", gridId);
				mLiFMapper.insertControlColParam(map);
			}
		}
		
		if(controlParam.containsKey("fcols")){
			List<String> inputCols = (List<String>)controlParam.get("fcols");
			param.put("gridid", "featureGrid");
			mLiFMapper.deleteControlColParam(param);
			for(String col : inputCols) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("SUB_PJT_ID", subPjtId);
				map.put("MODEL_UID", modelUid);
				map.put("userid", param.get("userid"));
				map.put("INPUT_COL", col);
				map.put("GRID_ID", "featureGrid");
				mLiFMapper.insertControlColParam(map);
			}
		}
		
		if(controlParam.containsKey("lcols")){
			List<String> inputCols = (List<String>)controlParam.get("lcols");
			param.put("gridid", "labelGrid");
			mLiFMapper.deleteControlColParam(param);
			for(String col : inputCols) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("SUB_PJT_ID", subPjtId);
				map.put("MODEL_UID", modelUid);
				map.put("userid", param.get("userid"));
				map.put("INPUT_COL", col);
				map.put("GRID_ID", "labelGrid");
				mLiFMapper.insertControlColParam(map);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public String replaceMissingNumber(Map<String,Object> param){
		String msg = "success";
		File sFile = null;
		File rFile = null;
		try {
			List<Integer> list = (List<Integer>)param.get("cols");
			Double abnum = Double.parseDouble(param.get("abnum").toString());
			Double renum = Double.parseDouble(param.get("renum").toString());
			
			sFile = getCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			}
			rFile = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			
			if(rFile.exists()) {
				rFile.delete();
			}
			bw = getBufferedWriter(rFile);
			
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				boolean hFlag = true;
		        String line = "";
		        while((line = br.readLine()) != null){
					List<String> data = new ArrayList<String>();
					String cols[] = StringUtil.split(line, ",");
					if(hFlag) {
						for(int i=0;i<cols.length;i++) {
							data.add(cols[i]);
						}
						hFlag=false;
					}else{
						int cnt = 0;
			            for(int i=0;i<cols.length;i++) {
			            	if(isNumeric(cols[i].trim())) {
			            		Double colnum = Double.parseDouble(cols[i].trim());
				            	if(list.size()>cnt && i==list.get(cnt)){
				            		if(colnum.compareTo(abnum)==0) {
				            			data.add(fmt(renum));
				            		}else {
				            			data.add(fmt(colnum));
				            		}
					            	cnt++;
				            	}else {
				            		data.add(fmt(colnum));
				            	}
			            	}else {
			            		if(list.size()>cnt && i==list.get(cnt)){
			            			cnt++;
			            		}
			            		data.add(cols[i]);
			            	}
			            }
					}
		            String dataStr = StringUtils.arrayToDelimitedString(data.toArray(), ",");
					bw.write(dataStr);
					bw.newLine();
				}
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
			
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {
				if(sFile.exists()) {
					br.close();
					isr.close();
					fis.close();
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public String replaceMissingString(Map<String,Object> param){
		String msg = "success";
		File sFile = null;
		File rFile = null;
		try {
			List<Integer> list = (List<Integer>)param.get("cols");
			String abstr = param.get("abstr").toString();
			String restr = param.get("restr").toString();
			
			sFile = getCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			}
			rFile = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			
			if(rFile.exists()) {
				rFile.delete();
			}
			bw = getBufferedWriter(rFile);
			
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				boolean hFlag = true;
		        String line = "";
		        while((line = br.readLine()) != null){
					List<String> data = new ArrayList<String>();
					String cols[] = StringUtil.split(line, ",");
					if(hFlag) {
						for(int i=0;i<cols.length;i++) {
							data.add(cols[i]);
						}
						hFlag=false;
					}else{
						int cnt = 0;
			            for(int i=0;i<cols.length;i++) {
			            	if(list.size()>cnt && i==list.get(cnt)){
			            		if(cols[i].trim().length()==0 && abstr.length()==0) {
			            			data.add(restr);
			            		}else if(cols[i].trim().equals(abstr)) {
				            		data.add(restr);
			            		}else {
			            			data.add(cols[i]);
			            		}
				            	cnt++;
			            	}else {
			            		data.add(cols[i]);
			            	}
			            }
					}
		            String dataStr = StringUtils.arrayToDelimitedString(data.toArray(), ",");
					bw.write(dataStr);
					bw.newLine();
				}
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
			
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {
				if(sFile.exists()) {
					br.close();
					isr.close();
					fis.close();
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public String deleteMissingData(Map<String,Object> param){
		String msg = "success";
		File sFile = null;
		File rFile = null;
		try {
			List<Integer> list = (List<Integer>)param.get("cols");
			
			List<Double> abnums = new ArrayList<Double>();
			if(param.get("abnum").toString().contains(",")) {
				String[] abnumStr = param.get("abnum").toString().split(",");
				for(int i=0;i<abnumStr.length;i++) {
					abnums.add(Double.parseDouble(abnumStr[i]));
				}
			}else if(isNumeric(param.get("abnum").toString())){
				abnums.add(Double.parseDouble(param.get("abnum").toString()));
			}
			
			List<String> abstrs = new ArrayList<String>();
			if(param.get("abstr").toString().contains(",")) {
				String[] abstrsArr = param.get("abstr").toString().split(",");
				for(int i=0;i<abstrsArr.length;i++) {
					abstrs.add(abstrsArr[i]);
				}
			}else {
				abstrs.add(param.get("abstr").toString());
			}
			boolean evchk = (boolean)param.get("evchk");
			boolean nanchk = (boolean)param.get("nanchk"); 
			
			sFile = getCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			}
			rFile = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			
			if(rFile.exists()) {
				rFile.delete();
			}
			bw = getBufferedWriter(rFile);
			
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				boolean hFlag = true;
		        String line = "";
		        
		        while((line = br.readLine()) != null){
		        	boolean flag = false;
					List<String> data = new ArrayList<String>();
					
					String cols[] = StringUtil.split(line, ",");
					if(hFlag) {
						for(int i=0;i<cols.length;i++) {
							data.add(cols[i]);
						}
						hFlag=false;
					}else{
						int cnt = 0;
			            for(int i=0;i<cols.length;i++) {
			            	if(list.size()>cnt && i==list.get(cnt)){
			            		String value = StringUtils.trimWhitespace(cols[i]);
			            		if(evchk && value.length()==0) {
			            			flag=true;
			            			break;
			            		}else if(nanchk && !isNumeric(value)) {
			            			flag=true;
			            			break;
			            		}else if(abnums.size()>0 && isNumeric(value)) {
			            			Double colnum = Double.parseDouble(value);
			            			for(Double abnum : abnums) {
			            				if(colnum.compareTo(abnum)==0) {
				            				flag=true;
					            			break;
				            			}
			            			}
			            		}else if(abstrs.size()>0){
			            			for(String abstr : abstrs) {
			            				if(value.equals(abstr)) {
				            				flag=true;
					            			break;
				            			}
			            			}
			            		}
			            		data.add(cols[i]);
				            	cnt++;
			            	}else {
			            		data.add(cols[i]);
			            	}
			            }
					}
					if(!flag) {
						String dataStr = StringUtils.arrayToDelimitedString(data.toArray(), ",");
						bw.write(dataStr);
						bw.newLine();
					}
				}
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
			
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {
				if(sFile.exists()) {
					br.close();
					isr.close();
					fis.close();
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public String splitData(Map<String,Object> param){
		String msg = "success";
		File sFile = null;
		File trFile = null;
		File teFile = null;
		try {
			Float trRatio = Float.parseFloat(param.get("trratio").toString());
			String ordVal = param.get("ordval").toString();
			
			sFile = getCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			}
			trFile = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString()+"_tr");
			teFile = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString()+"_te");
			
			List<Map<String,Object>> data = (List<Map<String,Object>>)param.get("data");
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				String line = br.readLine();
				
				int trCnt = Math.round(data.size()*trRatio);
				if(ordVal.equals("F")) {
					long seed = System.nanoTime();
					if(isNullValue(param,"seed")) {
						seed = Long.parseLong(param.get("seed").toString());
					}
					Collections.shuffle(data, new Random(seed));
				}
				if(trFile.exists()) {
					trFile.delete();
				}
				bw = getBufferedWriter(trFile);
				bw.write(line);
				for(int i=0;i<trCnt;i++) {
					bw.newLine();
					List<String> list = new ArrayList<String>();
					for(Entry<String, Object> entry : data.get(i).entrySet()) {
						list.add(entry.getValue().toString());
					}
					String dataStr = StringUtils.arrayToDelimitedString(list.toArray(), ",");
					bw.write(dataStr);
				}
				bw.flush();
				bw.close();
				
				if(teFile.exists()) {
					teFile.delete();
				}
				bw = getBufferedWriter(teFile);
				bw.write(line);
				for(int i=trCnt;i<data.size();i++) {
					bw.newLine();
					List<String> list = new ArrayList<String>();
					for(Entry<String, Object> entry : data.get(i).entrySet()) {
						list.add(entry.getValue().toString());
					}
					String dataStr = StringUtils.arrayToDelimitedString(list.toArray(), ",");
					bw.write(dataStr);
				}
				bw.flush();
				bw.close();
			}
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
			
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {
				if(sFile.exists()) {
					br.close();
					isr.close();
					fis.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> joinData(Map<String,Object> param){
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		File file = null;
		try {
			mLiFMapper.deleteLeftData(param);
			List<Map<String,Object>> leftList = (List<Map<String,Object>>)param.get("left_list");
			if(leftList.size()<100) {
				mLiFMapper.insertLeftData(param);
			}else {
				Map<String,Object> map = new HashMap<String,Object>();
				List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
				for(int i=0;i<leftList.size();i++) {
					dataList.add(leftList.get(i));
					if(i%100==0) {
						map.put("left_list", dataList);
						mLiFMapper.insertLeftData(map);
						map = new HashMap<String,Object>();
						dataList = new ArrayList<Map<String,Object>>();
					}
				}
				map.put("left_list", dataList);
				mLiFMapper.insertLeftData(map);
			}
			mLiFMapper.deleteRightData(param);
			List<Map<String,Object>> rightList = (List<Map<String,Object>>)param.get("right_list");
			if(rightList.size()<100) {
				mLiFMapper.insertRightData(param);
			}else {
				Map<String,Object> map = new HashMap<String,Object>();
				List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
				for(int i=0;i<rightList.size();i++) {
					dataList.add(rightList.get(i));
					if(i%100==0) {
						map.put("right_list", dataList);
						mLiFMapper.insertRightData(map);
						map = new HashMap<String,Object>();
						dataList = new ArrayList<Map<String,Object>>();
					}
				}
				map.put("right_list", dataList);
				mLiFMapper.insertRightData(map);
			}
			list = mLiFMapper.getJoinData(param);
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			List<String> headerList = new ArrayList<String>();
			List<Map<String,Object>> lCols = (List<Map<String,Object>>)param.get("left_cols");
			for(Map<String,Object> map : lCols) {
				headerList.add(map.get("COL_NAME").toString()+"_left");
			}
			List<Map<String,Object>> rCols = (List<Map<String,Object>>)param.get("right_cols");
			for(Map<String,Object> map : rCols) {
				headerList.add(map.get("COL_NAME").toString()+"_right");
			}
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(Map<String,Object> map : list) {
				List<String> dataList = new ArrayList<String>();
				for(String key : headerList) {
					if(map.containsKey(key) && !map.get(key).equals(null)) {
						dataList.add(map.get(key).toString());
					}else {
						dataList.add("");
					}
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> distinct(Map<String,Object> param){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		File file = null;
		try {
			mLiFMapper.deleteData(param);
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			if(list.size()<100) {
				mLiFMapper.insertData(param);
			}else {
				Map<String,Object> map = new HashMap<String,Object>();
				List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
				for(int i=0;i<list.size();i++) {
					dataList.add(list.get(i));
					if(i%100==0) {
						map.put("list", dataList);
						mLiFMapper.insertData(map);
						map = new HashMap<String,Object>();
						dataList = new ArrayList<Map<String,Object>>();
					}
				}
				map.put("list", dataList);
				mLiFMapper.insertData(map);
			}
			result = mLiFMapper.getDistinctData(param);
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			List<String> headerList = (List<String>)param.get("cols");
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<result.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : result.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String,Object>> sort(Map<String,Object> param){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		File file = null;
		try {
			mLiFMapper.deleteData(param);
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			if(list.size()<100) {
				mLiFMapper.insertData(param);
			}else {
				Map<String,Object> map = new HashMap<String,Object>();
				List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
				for(int i=0;i<list.size();i++) {
					dataList.add(list.get(i));
					if(i%100==0) {
						map.put("list", dataList);
						mLiFMapper.insertData(map);
						map = new HashMap<String,Object>();
						dataList = new ArrayList<Map<String,Object>>();
					}
				}
				map.put("list", dataList);
				mLiFMapper.insertData(map);
			}
			result = mLiFMapper.getSortData(param);
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			List<String> headerList = (List<String>)param.get("cols");
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<result.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : result.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> stringToColumn(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			Integer splitSize = Integer.parseInt(param.get("sptsize").toString());
			
			for(Integer col : colIdx) {
				String key = col.toString();
				for(Map<String,Object> map : list) {
					String value = StringUtils.trimWhitespace(map.get(key).toString());
					String[] vArr = value.split(" ");
					int colCnt = headerList.size();
					for(int i=0;i<splitSize;i++) {
						if(splitSize>=vArr.length) {
							map.put(Integer.toString(colCnt+i),"");
						}else {
							map.put(Integer.toString(colCnt+i),vArr[i]);
						}
					}
				}
				int hSize = headerList.size();
				int cnt = 1;
				for(int i=hSize;i<hSize+splitSize;i++) {
					headerList.add(headerList.get(col)+"_"+cnt++);
				}
			}
			
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> removeAbString(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			String abstr = param.get("abstr").toString();
			
			for(Integer col : colIdx) {
				String key = col.toString();
				for(Map<String,Object> map : list) {
					String value = map.get(key).toString();
					if(value.contains(abstr)) {
						map.put(key,value.replace(abstr,""));
					}
				}
			}
			
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> trim(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			
			for(Integer col : colIdx) {
				String key = col.toString();
				for(Map<String,Object> map : list) {
					String value = StringUtils.trimWhitespace(map.get(key).toString());
					map.put(key,value);
				}
			}
			
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> substring(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			int stIdx = Integer.parseInt(param.get("stidx").toString());
			int edIdx = stIdx+Integer.parseInt(param.get("sbtsize").toString());
			
			for(Integer col : colIdx) {
				String key = col.toString();
				for(Map<String,Object> map : list) {
					String value = map.get(key).toString();
					if(value.length()>edIdx) {
						value = value.substring(stIdx, edIdx);
					}
					map.put(key,value);
				}
			}
			
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> normalization(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<Integer> colidx = (List<Integer>)param.get("colidx");
			List<String> headerList = (List<String>)param.get("cols");

			if(param.get("normtype").equals("min_max")) {
				int colCnt = headerList.size();
				for(Integer col : colidx) {
					String key = col.toString();
					List<Double> dList = new ArrayList<Double>();
					for(Map<String,Object> map : list) {
						if(isNumeric(map.get(key).toString())) {
							dList.add(Double.parseDouble(map.get(key).toString()));
						}
					}
					Double min = Collections.min(dList);
					Double max = Collections.max(dList);
					for(Map<String,Object> map : list) {
						if(isNumeric(map.get(key).toString())) {
							Double x = Double.parseDouble(map.get(key).toString()); 
							Double y = (x-min)/(max-min);
							map.put(Integer.toString(colCnt), y);
						}else {
							map.put(Integer.toString(colCnt),map.get(key));
						}
					}
					colCnt++;
					headerList.add(headerList.get(col)+"_min_max");
				}
			}else if(param.get("normtype").equals("zscore")) {
				int colCnt = headerList.size();
				for(Integer col : colidx) {
					String key = col.toString();
					Double sum = 0.0;
					int cnt = 0;
					for(Map<String,Object> map : list) {
						if(isNumeric(map.get(key).toString())) {
							sum+=Double.parseDouble(map.get(key).toString());
							cnt++;
						}
					}
					Double mean = sum/cnt;
					Double tot = 0.0;
					for(Map<String,Object> map : list) {
						if(isNumeric(map.get(key).toString())) {
							Double val = Double.parseDouble(map.get(key).toString());
							tot+=(val-mean)*(val-mean);
						}
					}
					Double sdev = Math.sqrt(tot/cnt);
					for(Map<String,Object> map : list) {
						if(isNumeric(map.get(key).toString())) {
							Double x = Double.parseDouble(map.get(key).toString()); 
							Double y = (x-mean)/sdev;
							map.put(Integer.toString(colCnt), y);
						}else {
							map.put(Integer.toString(colCnt),map.get(key));
						}
					}
					colCnt++;
					headerList.add(headerList.get(col)+"_zscore");
				}
			}
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> concatenate(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			String delimiter = param.get("delimiter").toString();
			
			for(Map<String,Object> map : list) {
				List<String> conList = new ArrayList<String>();
				for(Integer col : colIdx) {
					String key = col.toString();
					String value = map.get(key).toString();
					conList.add(value);
				}
				map.put(Integer.toString(headerList.size()),StringUtils.arrayToDelimitedString(conList.toArray(), delimiter));
			}
			headerList.add("concat_col");
			
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> encoder(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			
			int colCnt = headerList.size();
			for(Integer col : colIdx) {
				String key = col.toString();
				List<Map<String,Object>> encList = new ArrayList<Map<String,Object>>();
				for(int i=0;i<list.size();i++) {
					String value = list.get(i).get(key).toString();
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("ORDNO", i);
					map.put("VAL", value);
					encList.add(map);
				}
				mLiFMapper.deleteDummyData();
				if(list.size()<1000) {
					Map<String,Object> pMap = new HashMap<String,Object>();
					pMap.put("dummy_list", encList);
					mLiFMapper.insertDummyData(pMap);
				}else {
					Map<String,Object> pMap = new HashMap<String,Object>();
					List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
					for(int i=0;i<encList.size();i++) {
						dataList.add(encList.get(i));
						if(i%1000==0) {
							pMap.put("dummy_list", dataList);
							mLiFMapper.insertDummyData(pMap);
							pMap = new HashMap<String,Object>();
							dataList = new ArrayList<Map<String,Object>>();
						}
					}
					pMap.put("dummy_list", dataList);
					mLiFMapper.insertDummyData(pMap);
				}
				mLiFMapper.mergeDictionaryData(param);
				List<String> rList = mLiFMapper.getEncodedValueList(param);
				int cnt = 0;
				for(Map<String,Object> map : list) {
					map.put(Integer.toString(colCnt), rList.get(cnt++));
				}
				headerList.add(headerList.get(col)+"_enc");
				colCnt++;
			}
			
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> decoder(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			
			int colCnt = headerList.size();
			for(Integer col : colIdx) {
				String key = col.toString();
				List<Map<String,Object>> decList = new ArrayList<Map<String,Object>>();
				for(int i=0;i<list.size();i++) {
					String value = list.get(i).get(key).toString();
					Map<String,Object> map = new HashMap<String,Object>();
					map.put("ORDNO", i);
					map.put("VAL", value);
					decList.add(map);
				}
				mLiFMapper.deleteDummyData();
				if(list.size()<1000) {
					Map<String,Object> pMap = new HashMap<String,Object>();
					pMap.put("dummy_list", decList);
					mLiFMapper.insertDummyData(pMap);
				}else {
					Map<String,Object> pMap = new HashMap<String,Object>();
					List<Map<String,Object>> dataList = new ArrayList<Map<String,Object>>();
					for(int i=0;i<decList.size();i++) {
						dataList.add(decList.get(i));
						if(i%1000==0) {
							pMap.put("dummy_list", dataList);
							mLiFMapper.insertDummyData(pMap);
							pMap = new HashMap<String,Object>();
							dataList = new ArrayList<Map<String,Object>>();
						}
					}
					pMap.put("dummy_list", dataList);
					mLiFMapper.insertDummyData(pMap);
				}
				List<String> rList = mLiFMapper.getDecodedValueList(param);
				int cnt = 0;
				for(Map<String,Object> map : list) {
					map.put(Integer.toString(colCnt), rList.get(cnt++));
				}
				headerList.add(headerList.get(col)+"_dec");
				colCnt++;
			}
			
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,Object> correlation(Map<String,Object> param){
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		try {
			List<String> headerList = (List<String>)param.get("cols");
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(int j=0;j<headerList.size();j++) {
					if(j==0) {
						dataList.add(list.get(i).get("gb").toString());
					}else {
						String colStr = "f"+(j-1);
						dataList.add(list.get(i).get(colStr).toString());
					}
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * 2020-01-18 이소라 추가
	 * 두 컬럼의 동일 여부 비교하여 결과 컬럼 추가
	 * A / B ==>  A / B / False문구
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> evaluation(Map<String, Object> param) {
		Map<String,Object> result = new HashMap<String,Object>();
		File file = null;
		
		try {
			List<Map<String,Object>> list = (List<Map<String,Object>>)param.get("list");
			List<String> headerList = getCSVHeader(param.get("subpjtid").toString(),param.get("sourceuid").toString());
			List<Integer> colIdx = (List<Integer>)param.get("cols");
			

			String predType = (String)param.get("predtype");				// 예측 유형
			String matchWord = (String)param.get("matchword");				// 일치할 경우
			String mismatchWord = (String)param.get("mismatchword");		// 불일치할 경우
			
			int matchCnt = 0;
			int mismatchCnt = 0;
			double matchPer = 0;
			double mismatchPer = 0;
			double entirePer = 0;
			
			
			int colCnt = headerList.size();
			if(predType.equals("classification")) {
				for(Map<String, Object> map : list) {
					String comp1 = fmt(Double.parseDouble((String)map.get(Integer.toString(colIdx.get(0)))));
					String comp2 = fmt(Double.parseDouble((String)map.get(Integer.toString(colIdx.get(1)))));
					
					if(comp1.compareTo(comp2)==0) {
						map.put(Integer.toString(colCnt), matchWord);
						matchCnt++;
					} else {
						map.put(Integer.toString(colCnt), mismatchWord);
						mismatchCnt++;
					}
				}
			} else {
				
				double moe = Double.parseDouble((String)param.get("moe"));
				for(Map<String, Object> map : list) {
					double comp1 = Double.parseDouble((String)map.get(Integer.toString(colIdx.get(0))));
					double comp2 = Double.parseDouble((String)map.get(Integer.toString(colIdx.get(1))));
					
					double gap = comp1 - comp2;
					
					map.put(Integer.toString(colCnt), Double.toString(gap));
					
					if(Math.abs(gap) - Math.abs(moe) <= 0) {
						map.put(Integer.toString(colCnt+1), matchWord);
						matchCnt++;
					} else {
						map.put(Integer.toString(colCnt+1), mismatchWord);
						mismatchCnt++;
					}
					
				}
				
				headerList.add(colCnt, headerList.get(colIdx.get(colIdx.size()-1)) + "_gap");
				colCnt++;
			}
			
			
			// 맨 마지막행 => 변경 고려
			headerList.add(colCnt, headerList.get(colIdx.get(colIdx.size()-1)) + "_eval");
			
			
			// 비율 계산 일치율/불일치율
			matchPer = (double)matchCnt / list.size() * 100;
			mismatchPer = (double)mismatchCnt / list.size() * 100;
			entirePer = list.size() / list.size() * 100;  // == 100
			 
			Map<String, Object> statistics = new HashMap<String, Object>();
			
			statistics.put("match_cnt", matchCnt);
			statistics.put("match_per", String.format("%.2f", matchPer));
			statistics.put("mismatch_cnt", mismatchCnt);
			statistics.put("mismatch_per", String.format("%.2f", mismatchPer));
			statistics.put("entire_cnt", list.size());
			statistics.put("entire_per", entirePer);
			
			result.put("statistics", statistics);
			result.put("list", list);
			result.put("header", headerList);
			
			file = getCSVFile(param.get("subpjtid").toString(),param.get("modeluid").toString());
			if(file.exists()) {
				file.delete();
			}
			bw = getBufferedWriter(file);
			
			String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
			bw.write(headerStr);
			bw.newLine();
			
			for(int i=0;i<list.size();i++) {
				List<String> dataList = new ArrayList<String>();
				for(Entry<String,Object> entry : list.get(i).entrySet()) {
					dataList.add(entry.getValue().toString());
				}
				bw.write(StringUtils.arrayToDelimitedString(dataList.toArray(), ","));
				bw.newLine();
			}
			bw.flush();
			bw.close();
			
			if(param.containsKey("controlparam")) {

				// span value 추가
				Map<String,Object> controlParam = (Map<String,Object>)param.get("controlparam");
				List<Map<String,Object>> params = (List<Map<String,Object>>)controlParam.get("params"); 

				params.forEach(map -> {
					String id = (String)map.get("id");
					
					if( id.indexOf("span_") == 0 ) {
						map.put("value", statistics.get(id.replace("span_", "")));
					}
				});
				
				saveControlParam(param);
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if(file.exists()) {
					bw.close();
					osw.close();
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		
		return result;
	}
	
public Map<String, Object> convertImageToPixel(List<Map<String, String>> infoList, String subPjtId, String modelUid, String userId) {
		
		/*
		 * 조건 : 이미지들의 가로/세로 사이즈가 동일해야함
		 *        => 이미지 1개 당 csv 파일 한 줄 
		 */
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {	
			// 이미지 업로드 폴더 경로 확인
			String imgDirPath = rootFilePath+subPjtId+"/upload";
			File imgDir = new File(imgDirPath);
			if(!imgDir.exists()) {
				// 업로드 폴더 경로가 없는 경우 -> error
				resultMap.put("status", "error");
				resultMap.put("msg", "업로드 폴더가 존재하지 않습니다.");
				
				return resultMap;
				
			}

			// csv 파일 확인
			File csvFile = getCSVFile(subPjtId,modelUid);
			if(csvFile.exists()) {
				csvFile.delete();
			}
			
			bw = getBufferedWriter(csvFile);
			
			
			// 이미지 파일당 csv 파일 1줄로 변환
			for(int idx=0; idx < infoList.size(); idx++) {
				
				Map<String, String> info = infoList.get(idx); 
				
				String imgName = info.get("file_nm");
				
				File imgFile = new File(imgDirPath + "/" + imgName);
				if(!imgFile.exists()) {
					// 이미지 파일 없는 경우 -> error
					
					resultMap.put("status", "error");
					resultMap.put("msg", (idx+1) + "번째 이미지가 존재하지 않습니다.");
					
					return resultMap;
				}
				
				BufferedImage img = ImageIO.read(imgFile);
				int width = img.getWidth();
				int height = img.getHeight();
				
				int[][] pixels = new int[width * height][3];
				int[] rgb;
				
	            for (int y = 0, counter = 0; y < height; y++) {
	                for (int x = 0; x < width; x++) {
	                	Color c = new Color(img.getRGB(x, y), true);
	                	rgb = new int[]{c.getRed(),c.getGreen(),c.getBlue()};
	                    System.arraycopy(rgb, 0, pixels[counter], 0, rgb.length);
	                    counter++;
	                }
	            }
				
	             
	            /*
	             * csv 파일의 y 값 encoding ( Box 별로 인코딩)
	             * 1. TB_ML_IMG_PARAM 테이블에 추가
	             * 2. TB_ML_IMG_PARAM 테이블에서 SELECT
	             * 3. RGB 변형된 값과 함께 csv 파일에 추가
	             */
	            Map<String,Object> map = new HashMap<String,Object>();
	            map.put("subPjtId", subPjtId);
	            map.put("modelUid", modelUid);
	            map.put("y", info.get("y"));
	            map.put("userid", userId);
	            
	            // 테이블에 없는 항목일 경우 insert
	            mLiFMapper.insertImageEncodingData(map);
	            
	            String yEnc = mLiFMapper.selectImageEncodingData(map);
	            
	            
	            List<Integer> dataList = new ArrayList<Integer>();
	            String[] arr = {"R","G","B"};
	            if(idx==0) {
	            	List<String> headerList = new ArrayList<String>();
	                for(int k=0;k<arr.length;k++) {
	                	for(int j=0;j<width*height;j++) {
	                    	String colStr = arr[k]+Integer.toString(j);
	                    	headerList.add(colStr);
	                    	dataList.add(pixels[j][k]);
	                    }
	                }

	                headerList.add("y");		// 사용자가 입력한 Y 값의 헤더
	                headerList.add("y_enc");	// 인코딩 된 Y 값
	                String headerStr = StringUtils.arrayToDelimitedString(headerList.toArray(), ",");
	    			bw.write(headerStr);
	    			bw.newLine();
	    			
	    			String dataStr = StringUtils.arrayToDelimitedString(dataList.toArray(), ",");
	    			dataStr += "," + info.get("y");			// Y 값
	    			dataStr += "," + yEnc;					// 인코딩 된 Y 값
	    			bw.write(dataStr);
	    			bw.newLine();
	            }else {
	            	for(int k=0;k<arr.length;k++) {
	                	for(int j=0;j<width*height;j++) {
	                    	dataList.add(pixels[j][k]);
	                    }
	                }
	            	
	            	String dataStr = StringUtils.arrayToDelimitedString(dataList.toArray(), ",");
	            	dataStr += "," + info.get("y");			// Y 값
	    			dataStr += "," + yEnc;					// 인코딩 된 Y 값
	    			bw.write(dataStr);
	    			bw.newLine();
	            }
			}
			
			resultMap.put("status", "success");
			
		} catch (Exception e) {
			resultMap.put("status", "error");
			resultMap.put("msg", e.getLocalizedMessage());
		} finally {
			try {
				bw.close();
				osw.close();
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				
				resultMap.put("status", "error");
				resultMap.put("msg", e.getLocalizedMessage());
			}
			
		}
		return resultMap;
	}
	
	
	@Override
	public String saveImage(MultipartFile[] files, String subPjtId, String modelUid, List<String> list, String userId) {
		/*
		 * 1. 이미지는 subpjid/upload 하위에 저장
		 * 2. csv 데이터는 이미지명 / y => subpjid 하위에 저장
		 * 3. 이미지명 중복 시 처리 필요
		 * 
		 */
		String msg = "success";
		
		try {
			List<List<String>> csvData = new ArrayList<List<String>>();
			
			String cFilePath = rootFilePath+subPjtId+"/upload";
			File cDir = new File(cFilePath);
			if(!cDir.exists()) {
				cDir.mkdir();
			}
			
			for(int i=0; i<files.length; i++) {
				File cFile = new File(cFilePath+"/"+files[i].getOriginalFilename());
				if(cFile.exists()) {
					// 파일이 있으면 파일명 수정 후 저장
					// 파일 + "(숫자)"
					String fileName = FilenameUtils.getBaseName(cFile.getName());
					String fileExt = FilenameUtils.getExtension(cFile.getName());
					
					for(int idx=1; true; idx++) {
						String fileNo = "(" + Integer.toString(idx) + ")";
						String newName = fileName + fileNo + "." + fileExt;
						File tmpFile = new File(cFilePath + "/" + newName);
						if(!tmpFile.exists()) {
							cFile = tmpFile;
							break;
						}
					}
				}
				files[i].transferTo(cFile);
				
				
				BufferedImage img = ImageIO.read(cFile);
				int imgWidth = img.getWidth();
				int imgHeight = img.getHeight();
				
				
				StringBuilder sb = new StringBuilder();
				sb.append(imgWidth);
				sb.append(" x ");
				sb.append(imgHeight);
				
				
				List<String> data = new ArrayList<String>();
				data.add(cFile.getName());			// 파일명
				data.add(sb.toString());			// 이미지 사이즈 width x height
				data.add(list.get(i));				// y 값
				
				csvData.add(data);
			}
			
			
			/*
			 * 파일에 데이터 저장
			 */
			File csvfile = getCSVFile(subPjtId, modelUid);
			
			if(csvfile.exists()) {
				// 파일이 있으면 추가
				osw = new FileWriter(csvfile, true);
				bw = new BufferedWriter(osw);
				
			} else {
				//파일이 없으면 생성
				bw = getBufferedWriter(csvfile);
				
				String headerStr = StringUtils.arrayToDelimitedString(new String[]{"file_nm", "size", "y"}, ",");
				bw.write(headerStr);
				bw.newLine();
			}
			
			for(int i=0; i<csvData.size(); i++) {
				String line = StringUtils.arrayToDelimitedString(csvData.get(i).toArray(), ",");
				
				bw.write(line);
				bw.newLine();
			}
			
			bw.flush();
			bw.close();
			
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		} finally {
			try {			
				bw.close();
				osw.close();
				if(fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	
	
	@Override
	public Map<String, Object> pivot(Map<String,Object> param) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		/*
		 * 1. csv 파일 load
		 * 2. dummy 테이블에 추가
		 * 
		 * 3. 열(column)으로 선택 된 항목 전체 ->  distinct -> 열로 사용가능
		 */
		
		File sFile = null;			// source file
		File rFile = null;			// result file
		
		// 선택된 헤더 정보
		Map<String, String> pivotParam = (HashMap<String, String>)param.get("params");

		try {
			
			// set source file
			sFile = getCSVFile(param.get("subpjtid").toString(), param.get("sourceid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile((String)param.get("subpjtid"),(String)param.get("sourceuid"));
			}
			
			// row, col, val로 설정 된 데이터 전체
			List<Map<String, Object>> orgData = new ArrayList<Map<String, Object>>();

			
			// col로 설정된 데이터를 Distinct 하기위한 변수
			List<String> columnData = new ArrayList<String>();
			
			Map<String, Map<String, Object>> header = new HashMap<String, Map<String, Object>>();
			
			// load source file
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				
				boolean shFlag = true;
				String line = new String();
				while((line = br.readLine()) != null) {
					
					 //List<String> cols = Arrays.asList(StringUtils.split(line, ",")); // line을 ,을 기준으로 나눈다.
					String cols[] = line.split(",");
					
					// 헤더인 경우
					if(shFlag) {
						
						// 각 헤더 index 저장
						for(int idx=0; idx < cols.length; idx++) {
							// row 헤더
							if(pivotParam.get("row").equals(cols[idx])) {
								Map<String, Object> map = new HashMap<String, Object>();
								
								map.put("index", idx);
								map.put("name", cols[idx]);
								
								header.put("row", map);
							} 
							// col 헤더
							else if(pivotParam.get("col").equals(cols[idx])) {
								Map<String, Object> map = new HashMap<String, Object>();
								
								map.put("index", idx);
								map.put("name", cols[idx]);
								
								header.put("col", map);
							}
							// val 헤더
							else if(pivotParam.get("val").equals(cols[idx])) {
								Map<String, Object> map = new HashMap<String, Object>();
								
								map.put("index", idx);
								map.put("name", cols[idx]);
								
								header.put("val", map);
							}
						}
						
						shFlag = false;
					} else {
					// 헤더가 아닌경우
						
						Map<String, Object> map = new HashMap<String, Object>();
						
						for(int idx=0; idx < cols.length; idx++) {
							// row
							if(idx == (Integer)header.get("row").get("index")) {
								map.put("row", cols[idx]);
							}
							// col
							else if(idx == (Integer)header.get("col").get("index")) {
								map.put("col", cols[idx]);
								
								// distinct 하기 위한 list
								columnData.add(cols[idx]);
							}
							// val
							else if(idx == (Integer)header.get("val").get("index")) {
								map.put("val", cols[idx]);
							}
						}
						
						orgData.add(map);
					}
				}
				
				// column distinct
				List<String> distinctedData = columnData.stream().distinct().sorted().collect(Collectors.toList());
				//columnData = columnData.stream().distinct().collect(Collectors.toList());
				
				
				// delete & insert DB
				mLiFMapper.deleteLeftData(param);
				
				if(orgData.size() < 1000) {
					mLiFMapper.insertPivotData(orgData);
				} else {
					for(int i=0; orgData.size()/((i+1)*1000) == 0;i++) {
						List<Map<String, Object>> tmpList = new ArrayList<>(orgData.subList(i*1000, ((i+1)*1000)-1));
						mLiFMapper.insertPivotData(tmpList);
					}
				}
				
				
				// select DB
				Map<String, Object> selectMap = new HashMap<String, Object>();
				selectMap.put("cols", distinctedData);
				selectMap.put("data", pivotParam);
				
				List<Map<String, Object>> resultData = mLiFMapper.selectPivotData(selectMap);
				
				
				// write File

				// set result file (overwrite)
				rFile = getCSVFile((String)param.get("subpjtid").toString(), (String)param.get("modeluid").toString());
				if(rFile.exists()) {
					rFile.delete();
				}
				
				bw = getBufferedWriter(rFile);
				
				// 헤더
				String headerStr = new String();
				
				headerStr += pivotParam.get("row").toString();
				for(int idx = 0; idx < distinctedData.size(); idx++) {
					headerStr += "," + pivotParam.get("col").toString() + "_" + distinctedData.get(idx) ;
				}
				
				bw.write(headerStr);
				bw.newLine();
				
				
				// 본문
				for(int idx = 0; idx < resultData.size(); idx++) {
					
					Map<String, Object> map = resultData.get(idx);
					String mainStr = new String();
					
					mainStr = map.get(pivotParam.get("row")).toString();
					map.remove(pivotParam.get("row"));
					
					Object[] mapKey = map.keySet().toArray();
					Arrays.sort(mapKey);
					
					
					/*
					 * Arrays.sort(mapKey, new Comparator<Object>() {
					 * 
					 * @Override public int compare(Object o1, Object o2) { String str1 =
					 * o1.toString(); String str2 = o2.toString();
					 * 
					 * 
					 * return str1.compareTo(str2); } });
					 */
					
					for(Object obj : mapKey) {
						String key = (String)obj;
						
						String value = map.get(key) == null ? "" : map.get(key).toString();
						
						mainStr += "," + value;
					}
					
					bw.write(mainStr);
					bw.newLine();
				}
				
				bw.flush();
			}

			saveControlParam(param);
			
			result.put("target_header", this.getCSVHeader(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			result.put("target_data", this.getCSVData(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			
			result.put("status", "success");
			
		} catch(Exception e) {
			result.put("status", "error");
			result.put("message", e.getMessage());
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	
	public Map<String, Object> unpivot(Map<String,Object> param) {
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		File sFile = null;			// source file
		File rFile = null;			// result file
		//File trFile = null;			// temp result file 
		
		try {
			
			// set source file
			sFile = getCSVFile(param.get("subpjtid").toString(), param.get("sourceid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile((String)param.get("subpjtid"),(String)param.get("sourceuid"));
			}
			
			// tmp 파일로 생성 후 실행 성공 시 원 파일 삭제 및 tmp 파일 복사
			rFile = getCSVFile((String)param.get("subpjtid").toString(), (String)param.get("modeluid").toString());
			//rFile = getCSVFile((String)param.get("subpjtid").toString(), (String)param.get("modeluid").toString() + "_tmp");
			
			if(rFile.exists()) { rFile.delete(); }
			
			
			
			// load source file
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				bw = getBufferedWriter(rFile);
				
				boolean hFlag = true;
				String line = new String();
				
				String[] hCols = null;
				
				String pattern = "^" + param.get("col").toString() + "_";
				
				Pattern bPtrn = Pattern.compile(pattern);
				
				while((line = br.readLine()) != null) {
				
					String cols[] = line.split(",");
					
					if(hFlag) {
						String hStr = new String();
						
						for(int i=1; i<cols.length; i++) {
							String bfVal =cols[i].replaceFirst(bPtrn.toString(), "");
							
							if(bfVal.length() < 1) {
								throw new Exception("Pivot 테이블에 이상이 있습니다.");
							}
						}
						
						hCols = cols;	// header Cols에 저장
						
						hStr += cols[0] + "," + param.get("col").toString() + "," + param.get("val");
						
						bw.write(hStr);
						bw.newLine();
						
						hFlag = false;
					} else {
						
						for(int i=1; i<cols.length; i++) {
						
							String bStr = new String();
							
							bStr += cols[0];			// row
							bStr += "," + hCols[i].replaceFirst(bPtrn.toString(), "");		// col
							bStr += "," + cols[i];		// val
							
							bw.write(bStr);
							bw.newLine();
							
						}
					}
				}
				
				bw.flush();
				
			}
			
			// 성공 후 원래 result 파일이 있으면 삭제 후 이름 변경 
			/*
			 * if(rFile.exists()) { rFile.delete(); } //trFile.renameTo(rFile);
			 * FileUtils.moveFile(trFile, rFile);
			 */
			
			saveControlParam(param);
			
			result.put("target_header", this.getCSVHeader(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			result.put("target_data", this.getCSVData(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			
			result.put("status", "success");
			
		} catch (Exception e) {
			// 실패 시 tmp 파일 삭제
			//trFile.delete();
			
			result.put("status", "error");
			result.put("message", e.getMessage());
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public Map<String, Object> capitalize(Map<String, Object> param){
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		File sFile = null;		// source file
		File rFile = null;		// result file
		
		try {
			// set source file
			sFile = getCSVFile(param.get("subpjtid").toString(), param.get("sourceid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile((String)param.get("subpjtid"),(String)param.get("sourceid"));
			}
			
			rFile = getCSVFile((String)param.get("subpjtid").toString(), (String)param.get("modeluid").toString());
			if(rFile.exists()) {
				rFile.delete();
			}
			
			
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				bw = getBufferedWriter(rFile);
				
				boolean hFlag = true;
				String line = new String();
				List<Integer> cvrtIdx = new ArrayList<Integer>();
				
				// param으로 들어온 체크된 헤더 리스트
				List<String> inputHeader = (ArrayList)param.get("colList");
				
				
				while((line = br.readLine()) != null) {
					
					String strs[] = line.split(",");
					
					if(hFlag) {			// 헤더인 경우
						
						for(int i=0; i<strs.length; i++) {
							if(inputHeader.indexOf(strs[i]) != -1 ) {
								cvrtIdx.add(i);
							}
						}
						
						bw.write(line);
						bw.newLine();
						
						hFlag = false; 
					} else {
						
						String convertLine = new String();
						
						for(int i=0; i<strs.length; i++) {
							String cvrtStr = new String(strs[i]).trim();
							
							// 변환 체크된 헤더의 인덱스인 경우
							if(cvrtIdx.indexOf(i) != -1) {
								cvrtStr = cvrtStr.toUpperCase();
							}
							convertLine += cvrtStr + ",";
							
						}
						
						convertLine = org.apache.commons.lang.StringUtils.removeEnd(convertLine, ",");
								
						bw.write(convertLine);
						bw.newLine();
					}
				}
				
				bw.flush();
				
				
			}
			
			result.put("target_header", this.getCSVHeader(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			result.put("target_data", this.getCSVData(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			
			result.put("status", "success");
			
		} catch(Exception e) {
			result.put("status", "error");
			result.put("message", e.getMessage());
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
public Map<String, Object> uncapitalize(Map<String, Object> param){
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		File sFile = null;		// source file
		File rFile = null;		// result file
		
		try {
			// set source file
			sFile = getCSVFile(param.get("subpjtid").toString(), param.get("sourceid").toString());
			if(!sFile.exists()) {
				sFile = getPredictCSVFile((String)param.get("subpjtid"),(String)param.get("sourceid"));
			}
			
			rFile = getCSVFile((String)param.get("subpjtid").toString(), (String)param.get("modeluid").toString());
			if(rFile.exists()) {
				rFile.delete();
			}
			
			
			if(sFile.exists()) {
				br = getBufferedReader(sFile);
				bw = getBufferedWriter(rFile);
				
				boolean hFlag = true;
				String line = new String();
				List<Integer> cvrtIdx = new ArrayList<Integer>();
				
				// param으로 들어온 체크된 헤더 리스트
				List<String> inputHeader = (ArrayList)param.get("colList");
				
				
				while((line = br.readLine()) != null) {
					
					String strs[] = line.split(",");
					
					if(hFlag) {			// 헤더인 경우
						
						for(int i=0; i<strs.length; i++) {
							if(inputHeader.indexOf(strs[i]) != -1 ) {
								cvrtIdx.add(i);
							}
						}
						
						bw.write(line);
						bw.newLine();
						
						hFlag = false; 
					} else {
						
						String convertLine = new String();
						
						for(int i=0; i<strs.length; i++) {
							String cvrtStr = new String(strs[i]).trim();
							
							// 변환 체크된 헤더의 인덱스인 경우
							if(cvrtIdx.indexOf(i) != -1) {
								cvrtStr = cvrtStr.toLowerCase();
							}
							convertLine += cvrtStr + ",";
							
						}
						
						convertLine = org.apache.commons.lang.StringUtils.removeEnd(convertLine, ",");
								
						bw.write(convertLine);
						bw.newLine();
					}
				}
				
				bw.flush();
				
			}
			
			result.put("target_header", this.getCSVHeader(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			result.put("target_data", this.getCSVData(param.get("subpjtid").toString(), param.get("modeluid").toString()));
			
			result.put("status", "success");
			
		} catch(Exception e) {
			result.put("status", "error");
			result.put("message", e.getMessage());
		} finally {
			try {
				br.close();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public File getCSVFile(String subPjtId,String uid){
		String filePath = rootFilePath+subPjtId+"/"+uid+".csv";
		return new File(filePath);
	}
	
	public File getPredictCSVFile(String subPjtId,String uid){
		String filePath = rootFilePath+subPjtId+"/predict/"+uid+".csv";
		return new File(filePath);
	}
	
	public File getModelCSVFile(String subPjtId,String uid){
		String filePath = rootFilePath+subPjtId+"/model/"+uid+".csv";
		return new File(filePath);
	}
	
	public BufferedWriter getBufferedWriter(File file){
		try {
			fos = new FileOutputStream(file);
			osw = new OutputStreamWriter(fos,fileEncode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new BufferedWriter(osw);
	}
	
	public BufferedReader getBufferedReader(File file){
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis,fileEncode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new BufferedReader(isr);
	}
	
	public static boolean isNumeric(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
	public static String fmt(double d){
	    if(d == (long) d)
	        return String.format("%d",(long)d);
	    else
	        return String.format("%s",d);
	}
	
	public static boolean isNullValue(Map<String,Object> map,String key){
	    if(map.containsKey(key) && !map.get(key).equals(null) && map.get(key).toString().length()>0) {
	    	return true;
	    }else {
	    	return false;
	    }
	}
}