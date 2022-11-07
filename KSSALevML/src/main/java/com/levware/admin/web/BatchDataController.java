package com.levware.admin.web;


import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hpsf.Date;
import org.codehaus.jackson.map.util.JSONWrappedObject;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.levware.admin.service.BatchDataService;
import com.levware.admin.service.BatchDataVO;
import com.levware.admin.service.BatchJobDataVO;

@Controller
@RequestMapping(value="/admin",method = RequestMethod.GET)
/*@CrossOrigin(origins = {"http://localhost:9090/batch/startjob","http://localhost:9090/batch/status","http://localhost:9090/ksm/scheduledtasks","http://localhost:9090/batch/stopjob"}) */
public class BatchDataController {
	
	private static final Logger LOGGER = LogManager.getLogger(BatchDataController.class);
	

	@Resource(name= "BatchDataService")
	private BatchDataService batchdataService;
	
	@RequestMapping(value= "/batchDataList" , headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<BatchDataVO> batchDataList(HttpServletResponse response) throws Exception{ response.setContentType("application/json;charset=UTF-8");		
		try {
			LOGGER.debug("call BatchDataList");
			List<BatchDataVO> BatchDataList =  batchdataService.getBatchDataList();
			return BatchDataList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}			
	}
	
	@RequestMapping(value= "/batchJobList" , headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<BatchJobDataVO> batchJobList(HttpServletResponse response) throws Exception{ response.setContentType("application/json;charset=UTF-8");		
		try {
			LOGGER.debug("call Batch_Job_List");
			List<BatchJobDataVO> BatchJobList =  batchdataService.getBatchJobList();
			return BatchJobList;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}			
	}
	
	
	@RequestMapping(value= "/batchStart" , headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String batchStart(HttpServletResponse response, @RequestParam("startDate") String startDate, 
			@RequestParam(value="endDate",required=false) String endDate,
			@RequestParam(value="flag",required=false) String flag)throws Exception{
		response.setContentType("application/json;charset=UTF-8");
		RestTemplate restTemplate = new RestTemplate();
		/* int startDay = Integer.parseInt(startDate);
		 int endDay = Integer.parseInt(endDate);*/
		try{
			//LOGGER.debug("date::::::"+startDate);//TODO startdate값 넘어오지는 부터 확인 - 성공
			LOGGER.debug("call batchStart");
			//String result = restTemplate.getForObject("http://localhost:9090/batch/startjob?startDate="+startDate,String.class);
			String result = restTemplate.getForObject("http://localhost:9090/batch/startjob?startDate="+startDate+"&endDate="+endDate+"&flag="+flag,String.class);
			//String result = restTemplate.getForObject("http://localhost:9090/batch/startjob",String.class);

			//System.out.println("result::::::::"+result);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}			
	}
	
	@RequestMapping(value= "/batchSchedule" , headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String batchSchedule(HttpServletResponse response)throws Exception{
		response.setContentType("application/json;charset=UTF-8");
		RestTemplate restTemplate = new RestTemplate();
		
		try{
			LOGGER.debug("call batchSchedule");
			String result = restTemplate.getForObject("http://localhost:9090/ksm/scheduledtasks",String.class);
			
			System.out.println("result::::::::"+result);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}			
	}
	 
	@RequestMapping(value= "/batchStatus" , headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String batchStatus(HttpServletResponse response, @RequestParam(value="startDate",required=false) String startDate, @RequestParam(value="endDate",required=false) String endDate)throws Exception{
		response.setContentType("application/json;charset=UTF-8");
		RestTemplate restTemplate = new RestTemplate();
		
		try{
			LOGGER.debug("call batchStatus");
			//String result = restTemplate.getForObject("http://localhost:9090/batch/status?startDate="+startDate,String.class);
			String result = restTemplate.getForObject("http://localhost:9090/batch/status?startDate="+startDate+"&endDate="+endDate,String.class);

			System.out.println("result::::::::"+result);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}			
	}
	@RequestMapping(value="/batchReset", headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
    public void batchReset(HttpServletResponse response, Authentication authentication) throws Exception{
    	LOGGER.debug("======================== batchReset ======================== ");
		try{
			batchdataService.batchReset();
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}    
    }
 
	@RequestMapping(value= "/batchEnd" , headers = "Accept=*/*", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public String batchEnd(HttpServletResponse response)throws Exception{
		response.setContentType("application/json;charset=UTF-8");
		RestTemplate restTemplate = new RestTemplate();
		
		try{
			LOGGER.debug("call batchEnd");
			String result = restTemplate.getForObject("http://localhost:9090/batch/stopjob",String.class);

			System.out.println("result::::::::"+result);
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}			
	}
	
}
