package com.levware.ml.service;

import java.util.Map;

import weka.classifiers.Classifier;


public interface ModelObjUtilService {

	public Classifier getModel(String sPredictType, String sModelType) throws Exception;;
	public void savedModelRoadInit(Map<String,Object> param) throws Exception ;
	public void savedModelRemove(Map<String,Object> param) throws Exception ;
	public void setModelCreateIngStatus(boolean stat); 
}
