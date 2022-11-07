package com.levware.common.mappers.repo;

import java.util.List;

import com.levware.admin.service.BatchDataVO;
import com.levware.admin.service.BatchJobDataVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

@Mapper("batchDataMapper")
public interface BatchDataMapper {
	
	public List<BatchDataVO> getBatchDataList() throws Exception;
	

	public List<BatchJobDataVO> getBatchJobList() throws Exception;


	public void batchReset();
	
	public int cnv1DataCount() throws Exception;
}
