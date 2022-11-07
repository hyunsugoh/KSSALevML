package com.levware.ml.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.MLiframeMapper;
import com.levware.ml.service.MLModelService;

@Service("mLModelService")
public class MLModelServiceImpl implements MLModelService {
	
	@Resource(name = "mLiframeMapper")
	private MLiframeMapper mLiFMapper;
	
	private static String profile=System.getProperty("server.os.profile");
	private static String rootFilePath=profile.equals("linux")?"/app/levml/csv/":"D:/lev_ml/";
	
	public List<Map<String,Object>> getResultXML(Map<String,Object> param){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder documentBuilder;
		try {
			String xmlPath = rootFilePath+param.get("subpjtid").toString()+"/model/"+param.get("modeluid").toString()+".xml";
			documentBuilder = factory.newDocumentBuilder();
			Document doc = documentBuilder.parse(xmlPath);
			
			NodeList nList = doc.getElementsByTagName("test_metrics").item(0).getChildNodes();
			for(int temp = 0; temp < nList.getLength(); temp++){
				Map<String,Object> map = new HashMap<String,Object>();
				Node nNode = nList.item(temp);
				if(nNode.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element) nNode;
					String attrName = nNode.getNodeName();
					String value = eElement.getAttributeNode("value").getValue();
					map.put("attr", attrName);
					map.put("value", value);
					result.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public List<Map<String,Object>> postTrainModel(Map<String,Object> param){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder documentBuilder;
		try {
			String xmlPath = rootFilePath+param.get("subpjtid").toString()+"/model/"+param.get("modeluid").toString()+".xml";
			documentBuilder = factory.newDocumentBuilder();
			Document doc = documentBuilder.parse(xmlPath);
			
			NodeList nList = doc.getElementsByTagName("test_metrics").item(0).getChildNodes();
			for(int temp = 0; temp < nList.getLength(); temp++){
				Map<String,Object> map = new HashMap<String,Object>();
				Node nNode = nList.item(temp);
				if(nNode.getNodeType() == Node.ELEMENT_NODE){
					Element eElement = (Element) nNode;
					String attrName = nNode.getNodeName();
					String value = eElement.getAttributeNode("value").getValue();
					map.put("attr", attrName);
					map.put("value", value);
					result.add(map);
				}
			}
			
			if(param.containsKey("controlparam")) {
				saveControlParam(param);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String postPredictModel(Map<String,Object> param){
		String msg = "success";
		try {
			saveControlParam(param);
		} catch (Exception e) {
			msg = "error";
			e.printStackTrace();
		}
		return msg;
	}
	
	public List<Map<String,Object>> getResultCSV(Map<String,Object> param){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void saveControlParam(Map<String,Object> param){
		Map<String,Object> controlParam = (Map<String,Object>)param.get("controlparam");
		String subPjtId = param.get("subpjtid").toString();
		String modelUid = param.get("modeluid").toString();
		List<Map<String,Object>> params = (List<Map<String,Object>>)controlParam.get("params");
		
		mLiFMapper.deleteControlParam(param);
		for(Map<String,Object> map : params) {
			map.put("SUB_PJT_ID", subPjtId);
			map.put("MODEL_UID", modelUid);
			map.put("userid", param.get("userid"));
			mLiFMapper.insertControlParam(map);
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
}
