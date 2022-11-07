package com.levware.admin.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.levware.admin.service.BatchDataService;
import com.levware.admin.service.BatchDataVO;
import com.levware.admin.service.BatchJobDataVO;
import com.levware.common.mappers.repo.BatchDataMapper;


@Service("BatchDataService")
public class BatchDataServiceImpl implements BatchDataService{
	
	@Resource(name="batchDataMapper")
	private BatchDataMapper batchDataMapper;
	
	public List<BatchDataVO> getBatchDataList() throws Exception{
		
		List<BatchDataVO> getBatchDataList = batchDataMapper.getBatchDataList();
		
		return getBatchDataList;
		
	}
	
	public List<BatchJobDataVO> getBatchJobList() throws Exception{
			
		List<BatchJobDataVO> getBatchJobList = batchDataMapper.getBatchJobList();
			
		return getBatchJobList;
			
		}
	
	@Override
	public void batchReset() throws Exception{
		batchDataMapper.batchReset();
		
	}
	
	public int cnv1DataCount() throws Exception{
		
		int cnv1TotalCount = batchDataMapper.cnv1DataCount();
			
		return cnv1TotalCount;
			
		}
	

}