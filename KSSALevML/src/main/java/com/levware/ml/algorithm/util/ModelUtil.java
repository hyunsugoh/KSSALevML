package com.levware.ml.algorithm.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.StringUtils;

import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Instances;


public class ModelUtil {
	
	//@Resource(name = "systemProperties")
	//private Properties systemProperties;
	
	public static Map<String,Object> modelSave(Object model, String supPath, String saveGb) {
		
		Map<String,Object> rtnMap = new HashMap<String,Object>();
		if (saveGb==null) saveGb = "";
		
		try {
			String modelBaseDir = getProperty().getProperty("model.base.dir");
			//String modelBaseDir = systemProperties.getProperty("model.base.dir");
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String dateString = format.format(new Date());
			
			String modelPath = modelBaseDir + "/" + supPath + "/"+dateString+("".equals(saveGb)?"":"_"+saveGb)+".model";
			
			System.out.println("modelPath : " + modelPath);
			
			//Folder 생성
			if(!(new File(modelBaseDir + "/" + supPath)).exists()) {
				(new File(modelBaseDir + "/" + supPath)).mkdirs();
			}
			
			//weka.core.SerializationHelper.write(modelPath, model);
			//compression
			File f = new File(modelPath);
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(gzipOutputStream);
            objectOutputStream.writeObject(model);
            objectOutputStream.flush();
            objectOutputStream.close();
            gzipOutputStream.close();
            fileOutputStream.close();
			
			rtnMap.put("result", "ok");
			rtnMap.put("model_path", modelPath);
		} catch(Exception e) {
			rtnMap.put("result", "error");
		}
		return rtnMap;
		
	}
	
	public static Object modelLoad(String modelPath) throws Exception{
		//Object model = weka.core.SerializationHelper.read(modelPath);
		//compression
		File f = new File(modelPath);
        FileInputStream fileInputStream = new FileInputStream(f);
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        ObjectInputStream objectOutputStream = new ObjectInputStream(gzipInputStream);
        //Classifier model = (Classifier) objectOutputStream.readObject();
        Object model = objectOutputStream.readObject();
        objectOutputStream.close();
        gzipInputStream.close();
        fileInputStream.close();
        
		return model;
	}
	

	public static Map<String, Double> modelEvaluation(Object model, Instances training, 	Instances test) {
		// 결과 값
		Map<String, Double> result = new HashMap<>();
		
		Evaluation eval = null;
		try {
			
			eval = new Evaluation(training);
			eval.evaluateModel((Classifier)model,test); 
			try {
				result.put("correlation_coefficient", eval.correlationCoefficient()); // 상관계수	
			}catch(Exception e) {
				result.put("correlation_coefficient", 0d); // 상관계수
			}
			result.put("mean_absolute_error", eval.meanAbsoluteError()); // 평균절대값오차 -> 절대오차의 평균 -> 절대오차 : ABS(예측값-실제값)
			result.put("root_mean_squared_error", eval.rootMeanSquaredError()); //평균 제곱근 오차 RMSE
			result.put("relative_absolute_error", eval.relativeAbsoluteError()); // 상대적절대값오차
			result.put("root_relative_squared_error", eval.rootRelativeSquaredError()); // 상대적평균오차
			result.put("correct_rate" , eval.pctCorrect()); // 테스트데이터의 예측정확도
			result.put("correct_cnt" , eval.correct()); // 테스트데이터의 정확히예측한 건수
			
			result.put("training_cnt", (double)training.size()); // 모델에 반영된 데이터 수
			result.put("test_cnt", (double)test.size()); // 테스트에 반영된 데이터 수
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return result;
	}
	
	
	public static Properties getProperty() {
		Properties properties = null;
		try {
			properties = new Properties();
			//properties.load(this.getClass().getClassLoader().getResourceAsStream("system.properties"));
			properties.load(ModelUtil.class.getClassLoader().getResourceAsStream("levware/property/system.properties"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return properties;
	}
	
	
	public static String getModelId() {
		//UUID uid = UUID.randomUUID();
		SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
		String dateString = format.format(new Date());
		return StringUtils.rightPad(dateString + (int)(Math.random() * 1000), 15, "0");
	}
	
	public static Map<String,Double> getSortMap(final Map<String,Double> result) {
		
		List<String> keySetList = new ArrayList<>(result.keySet());
        // 내림차순 //
        Collections.sort(keySetList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return result.get(o2).compareTo(result.get(o1));
            }
        });
        
        Map<String,Double> sortResultM = new LinkedHashMap<String,Double>();
        for(String key : keySetList) {
        	if ( result.get(key) == 0) continue;
        	//System.out.println(result.get(key));
        	sortResultM.put(key, result.get(key));
        }
        return sortResultM;
	}
	
}
