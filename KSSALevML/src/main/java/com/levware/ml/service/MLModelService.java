package com.levware.ml.service;

import java.util.List;
import java.util.Map;

public interface MLModelService {
	public List<Map<String,Object>> getResultXML(Map<String,Object> param);
	public List<Map<String,Object>> postTrainModel(Map<String,Object> param);
	public String postPredictModel(Map<String,Object> param);
}
