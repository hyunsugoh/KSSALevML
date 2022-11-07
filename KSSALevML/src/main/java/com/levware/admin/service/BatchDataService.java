package com.levware.admin.service;

import java.util.List;

public interface BatchDataService {
	
	public List<BatchDataVO> getBatchDataList() throws Exception;
	
	public List<BatchJobDataVO> getBatchJobList() throws Exception;
	
	public void batchReset() throws Exception; 
	
	public int cnv1DataCount() throws Exception;
}
