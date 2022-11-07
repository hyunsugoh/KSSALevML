package com.levware.ml.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.MLiframeMapper;
import com.levware.ml.service.MLModelTestService;

@Service("mLModelTestService")
public class MLModelTestServiceImpl implements MLModelTestService {

	
	@Resource(name = "mLiframeMapper")
	private MLiframeMapper mLiFMapper;

	
	/**
	 * 테이블 전체 데이터 select
	 */
	@Override
	public Map<String, Object> getTableData(Map<String, Object> params) {
		// TODO Auto-generated method stub

		// return값 초기화
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			// 테이블에 설정할 헤더
			@SuppressWarnings("unchecked")
			List<String> headers = (List<String>) params.get("list");
			map.put("header", headers);
	
			/**
			 * 테이블에 설정할 데이터
			 * { 컬럼명 : 데이터 } => { 숫자 : 데이터 }
			 */
			List<Map<String, Object>> gridRows = new ArrayList<Map<String, Object>>();
			for(Map<String, Object> row : mLiFMapper.getTableData(params)) {
				
				Map<String, Object> gridRow = new HashMap<String, Object>();
				
				for(String header : headers) {
					gridRow.put(Integer.toString(headers.indexOf(header)), row.get(header));
				}
				
				gridRows.add(gridRow);
			}
			
			map.put("data", gridRows);
			map.put("msg", "success");
		} catch(Exception e) {
			map.put("msg", e.getMessage());
		}
		return map;
	}	
}
