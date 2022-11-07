package com.levware.ml.algorithm.tree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecisionTreeMain {

	public DecisionTreeMain() {}
	
	public Map<String, Object> process ( String processType, 
				List<Map<String,Object>> trainData,
				List<Map<String,Object>> predictData, 
				Map<String,Object> opt) throws Exception{
		
		// processType 에 따라 처리
		// traing,		forecast,		traingNforecast
		
		
		Map<String, Object> result = new HashMap<String, Object>();
		
		ProcessData processData = new ProcessData();
		
		if ("training".equals(processType)) {
			
			result = processData.trainig(trainData, opt);
			
		}else if ("predict".equals(processType)) {
			
			result = processData.predict(predictData, opt);
			
		}else if ("trainingNpredict".equals(processType)) {

			result = processData.traingNPredict(trainData, predictData, opt );
			
		}
		
		
		return  result;
	}
	
	
	
	
	
}
