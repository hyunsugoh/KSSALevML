package com.levware.ml.algorithm.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class PreProcessData {

	/**
	 * 리스트형태의 데이터를 WEKA 라이브러리에서 사용하기위한 형태로 변환한다.
	 * @param dataList : List<Map<String,Object> - Instances로 생성할 리스트 데이터
	 * @param opt : Map<String,Object> - Option
	 *  - nominalAttrs : Map<String,List<String>> - 문자항목
	 *  - classIdx : int - 클래스 위치
	 *  - dataType : String - 데이터 구분 ( T : 트레이닝 , P:예측 )
	 *  
	 *  Instances 는
	 *   - attribute
	 *   - data (Instance)
	 *  로 구성 
	 *  
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Instances convListToArff(List<Map<String,Object>> dataList, Map<String,Object> opt) {
		
		if (dataList.size() ==0) return null;
		
		// ----------------------------------------------------
		// Attribute 설정
		// ----------------------------------------------------
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		// nominal 항목에 대한 리스트 정보
		List<String> nominalAttrDataList = null;
		// Attribute 설정을 위해 리스트의 첫항목을 가져온다.
		Map<String,Object> headerM = dataList.get(0);
		
		// Attribute 중 Nominal(text) 형식의 항목을 정의
		Map<String,Object> nominalAttrs = (Map<String,Object>)opt.get("nominalAttrs");
		List<String> nominalAttrKeys = new ArrayList<String>();
		for(String s : nominalAttrs.keySet()) {
			nominalAttrKeys.add(s);
		}
//		System.out.println("nominalAttrKeys : " + nominalAttrKeys.toString());
//		System.out.println("dataList toString : " + dataList.toString());
		
		// Attribute 정보 생성
		for (String head : headerM.keySet()) {
			//System.out.println("head : " + head);
			if (nominalAttrKeys.contains(head)) { //nominal attr인경우
				//System.out.println("  nominal : Y ");
				if (nominalAttrs.get(head) == null) { //Nominal Attribute의 데이터가 넘어오지 않은 경우
					//System.out.println("Nominal Attribute의 데이터가 넘어오지 않은 경우");
					nominalAttrDataList = new ArrayList<String>();
					for(Map<String,Object> m : dataList) {
						//System.out.println((String)m.get(sCol));
						if ( !nominalAttrDataList.contains((String)m.get(head))){ // 중복제거
							nominalAttrDataList.add((String)m.get(head));
						}
					}
				} else { //Nominal Attribute의 데이터가 넘어온경우
					//System.out.println("Nominal Attribute의 데이터가 넘어온 경우");
					nominalAttrDataList = (List<String>)nominalAttrs.get(head);
				}
				//System.out.println("head : " + head);
				//System.out.println("nominalAttrDataList : " + nominalAttrDataList.toString());
				
				atts.add(new Attribute(head, nominalAttrDataList)); // attribute 추가
				//System.out.println("Nominal Attr Info : "  + nominalAttrDataList.toString());
			}else {  // 일반 : numeric
				atts.add(new Attribute(head)); // attribute 추가
			}
		}
//		System.out.println("attribute 생성완료 : " + atts.toString());
		
		// Instances 객체 생성
		Instances Instances = new Instances("LevInstances",atts,0);
		
		// class Index Set.
		int classIdx = 0;
		if (opt.get("classIdx") != null) {
			classIdx = ((Integer)opt.get("classIdx")).intValue();
		}else { // 맨마지막 필드 적용
			classIdx = Instances.numAttributes() - 1;
		}
		Instances.setClassIndex(classIdx);
		System.out.println("class index 생성완료, index : " + atts.size());
		
		// ----------------------------------------------------
		// Data 설정
		// ----------------------------------------------------
		int i=0;
		for(Map<String,Object> m : dataList) {
			i=0;
			
			// Instance 생성
			DenseInstance instance = new DenseInstance(atts.size());
			
			// Instance <- Data Set
			for (String col : m.keySet()) {
				//System.out.println("attr col : " +col);
				
				// nominal 항목일 경우
				if(nominalAttrs.containsKey(col)) { 
					if (i==classIdx) { // class(label) 일 경우
						if (opt.get("dataType") != null &&  ("T".equals((String)opt.get("dataType")) || "TP".equals((String)opt.get("dataType")))) {
//							System.out.println("class attr value  col : " + col + " value : " + (String)m.get(col));
							instance.setValue(Instances.attribute(col), (String)m.get(col));
						}
					}else { //class가 아닐경우
//						System.out.println("attr value : col : " + col + " value : " + (String)m.get(col));
						if (m.get(col)==null || "NULL".equals(((String)m.get(col)).toUpperCase())) {
							instance.setValue(Instances.attribute(col), "");
						}else {
							instance.setValue(Instances.attribute(col), (String)m.get(col));
						}
					}
				// numeric 	
				}else { 
					//System.out.println("m.get(col) :  " + m.get(col));
					instance.setValue(Instances.attribute(col), (m.get(col)==null || "".equals(String.valueOf(m.get(col))))?0: Double.valueOf(m.get(col).toString()) );
				}
				i++;
			}
			Instances.add(instance); // 인스턴스 Add
		}
		//System.out.println("dataSet 생성완료 : " + Instances.toString());
		//System.out.println("dataSet 생성완료 : " + Instances.size());
		
		return Instances;
	}

	
}
