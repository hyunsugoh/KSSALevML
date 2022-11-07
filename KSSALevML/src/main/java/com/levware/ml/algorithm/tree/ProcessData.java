package com.levware.ml.algorithm.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.levware.ml.algorithm.util.ModelUtil;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class ProcessData {

	private int I_TREE_NUM = 10;
	
	private static final Logger LOGGER = LogManager.getLogger(ProcessData.class);

	/**
	 * 모델 생성
	 * @param receiveData
	 * @param opt
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> trainig(List<Map<String,Object>> receiveData, Map<String, Object> opt ) throws Exception{
		
		Map<String, Object> result	= new HashMap<String, Object>(); // 결과
				
		//list to arff
		Instances DataSet = PreProcessData.convListToArff(receiveData, opt);
		LOGGER.debug("trainig : data set end");
		
		//data set
		int trainSize = (int) Math.round(DataSet.numInstances() * 70/ 100);
		int testSize = DataSet.numInstances() - trainSize;
		//Instances trainingDataSet = new Instances(DataSet, 0, trainSize);
		Instances trainingDataSet = DataSet; // 전체데이터
		Instances testingDataSet = new Instances(DataSet, trainSize, testSize);
		LOGGER.debug("trainig : train / test data set");
		
		//모델 Build
		Classifier classifier = null;
		if (opt.get("classifier") == null || "".equals(opt.get("classifier"))){
			System.out.println("-----> random forest");
			LOGGER.debug("trainig : random forest classifier");
			classifier = new RandomForest();
			((RandomForest) classifier).setNumIterations(I_TREE_NUM);
		}else {
			System.out.println("-----> random forest");
			Class<?> classifierClass = Class.forName((String)opt.get("classifier"));
			classifier = (Classifier)classifierClass.newInstance();
		}
		//RandomForest classifier=new RandomForest();
		//classifier.setNumIterations(I_TREE_NUM);
		//J48 classifier=new J48();
		//NaiveBayes classifier = new NaiveBayes();
		classifier.buildClassifier(trainingDataSet);
		LOGGER.debug("trainig : train end");
		
		//모델 저장
		Map<String,Object> rtnSave = ModelUtil.modelSave(classifier, "RF",(String)opt.get("modelSubType"));
		
		//result.put("eval", ModelUtil.modelEvaluation(classifier, trainingDataSet, testingDataSet));//Evaluation - 평가
		result.put("eval", ModelUtil.modelEvaluation(classifier, trainingDataSet, trainingDataSet));//Evaluation - 평가
		result.put("model_path", rtnSave.get("model_path"));
		LOGGER.debug("result set end");
		return result;
	}
	
	
	/**
	 * 모델예측
	 * @param processType
	 * @param receiveData
	 * @param modelFilePath
	 * @return
	 */
	public Map<String, Object> predict(List<Map<String,Object>> receiveData, Map<String, Object> opt) throws Exception{
		
		Map<String,Object> result = new HashMap<String,Object>();
		
		String modelPath = (String)opt.get("modelPath");
		System.out.println("predict : modelPath : "+modelPath);
		
		//Model Load
		//RandomForest classifier = (RandomForest)ModelUtil.modelLoad(modelPath);
		//J48 classifier = (J48)ModelUtil.modelLoad(modelPath);
		Classifier classifier = null;
		if (opt.get("classifier") == null || "".equals(opt.get("classifier"))){
			System.out.println("predict : random forest classifier");
			classifier = (RandomForest)ModelUtil.modelLoad(modelPath);
		}else {
			classifier = (Classifier) ModelUtil.modelLoad(modelPath);
		}
		
		// 예측데이터 Set
		Instances predictDataSet = PreProcessData.convListToArff(receiveData, opt);

		//System.out.println("predictDataSet.size() : " + predictDataSet.size());
		System.out.println("predict : data set end");
		
		// 예측
		double[] prediction = classifier.distributionForInstance(predictDataSet.get(0));

		System.out.println("predict : predict : " + prediction.length);
		
		Map<String,Double> predict = new HashMap<String,Double>();
        for(int i=0; i<prediction.length; i=i+1){
        	predict.put(predictDataSet.classAttribute().value(i).toString(), prediction[i]);
        	//System.out.println(predictDataSet.classAttribute().value(i).toString() +"|"+ prediction[i]);
        }
        Map<String,Double> p_m = ModelUtil.getSortMap(predict); // 정렬
        List<Map<String,Object>> p_l = new ArrayList<Map<String,Object>>();
        Map<String,Object> p_l_m = null;
        int i=1;
        for( String key : p_m.keySet()) {
        	p_l_m = new HashMap<String,Object>();
        	p_l_m.put("NO", i++);
        	p_l_m.put("CLASS", key);
        	p_l_m.put("PROB", Math.round(p_m.get(key)*10000000)/100000.0);
        	p_l.add(p_l_m);
        }
        result.put("predict_result", p_l);
        System.out.println("Predict : end");
        return result;
	}
	
	
	/**
	 * 모델 빌드 및 예측을 동시에 진행.
	 * 모델 정보를 별도로 저장하지는 않는다.
	 * @param searchData
	 * @param predictData
	 * @param opt
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> traingNPredict(List<Map<String,Object>> searchData, List<Map<String,Object>> predictData, Map<String, Object> opt) throws Exception{
		
		Map<String, Object> result	= new HashMap<String, Object>(); // 결과
		
		long t1 = System.currentTimeMillis();
		
		//list to arff
		opt.put("dataType","T");
		Instances DataSet = PreProcessData.convListToArff(searchData, opt);
		LOGGER.debug("training n predict : data set end");
		
		long t2 = System.currentTimeMillis();
		LOGGER.debug("training n predict : 트레이닝 데이터 생성 : " + (t2-t1)/1000.0);
		
		//모델 Training - data set
		Instances trainingDataSet = DataSet; // 전체데이터
		LOGGER.debug("training n predict : train / test data set end ");
		
		//모델 Build
		Classifier classifier = null;
		if (opt.get("classifier") == null || "".equals(opt.get("classifier"))){
			LOGGER.debug("training n predict : random forest classifier");
			classifier = new RandomForest();
			((RandomForest) classifier).setNumIterations(I_TREE_NUM);
		}else {
			Class<?> classifierClass = Class.forName((String)opt.get("classifier"));
			classifier = (Classifier)classifierClass.newInstance();
		}
		//RandomForest classifier=new RandomForest();
		//classifier.setNumIterations(I_TREE_NUM);
		//NaiveBayes classifier = new NaiveBayes();
		//J48 classifier = new J48();
		classifier.buildClassifier(trainingDataSet);
		
		long t3 = System.currentTimeMillis();
		LOGGER.debug("training n predict : 모델 생성 : " + (t3-t2)/1000.0);
		
		//모델 Predict -  data set
		opt.put("dataType","P");
		Instances predictDataSet = PreProcessData.convListToArff(predictData, opt);
		
		long t4 = System.currentTimeMillis();
		LOGGER.debug("training n predict : 예측 데이터 생성 : " + (t4-t3)/1000.0);
		LOGGER.debug("training n predict : 예측 데이터  Size : " + predictDataSet.size());
		
		//모델 Predict
		double[] prediction = classifier.distributionForInstance(predictDataSet.get(0));
		
		long t5 = System.currentTimeMillis();
		LOGGER.debug("training n predict : 모델 예측 : " + (t5-t4)/1000.0);
		
		Map<String,Double> predict = new HashMap<String,Double>();
        for(int i=0; i<prediction.length; i=i+1){
        	predict.put(predictDataSet.classAttribute().value(i).toString(), prediction[i]);
        }
        Map<String,Double> p_m = ModelUtil.getSortMap(predict); // 정렬
        List<Map<String,Object>> p_l = new ArrayList<Map<String,Object>>();
        Map<String,Object> p_l_m = null;
        int i=1;
        int iRow = opt.get("listCnt")==null?0:(Integer)opt.get("listCnt"); // row 제한
        for( String key : p_m.keySet()) {
        	p_l_m = new HashMap<String,Object>();
        	p_l_m.put("NO", i++);
        	p_l_m.put("CLASS", key);
        	p_l_m.put("PROB", Math.round(p_m.get(key)*10000000)/100000.0);
        	p_l.add(p_l_m);
        	if(iRow >0 && iRow<i) break;
        }
        result.put("predict_result", p_l);
        
        long t6 = System.currentTimeMillis();
        LOGGER.debug("training n predict : 모델 결과 Set : " + (t6-t5)/1000.0);
        LOGGER.debug("training n predict : training and predict end");
        
        return result;
	}
	
	
	
}
