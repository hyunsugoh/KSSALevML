package com.levware.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Class Name : MLController.java
 * @Description : M/L Controller Class
 * @Modification Information
 * @ @ 수정일 수정자 수정내용 @ --------- --------- ------------------------------- @
 *   2019.10.08 최초생성
 *
 * @since 2019. 10.08
 * @version 1.0
 * @author LEVWARE
 * @see
 *
 */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.admin.service.AdminObjectListVO;
import com.levware.common.StringUtil;
import com.levware.ml.service.MLService;
import com.levware.ml.service.impl.ModelObjUtil;

@Controller
@RequestMapping(value="/ext")
public class ExtController {
	
	private static final Logger LOGGER = LogManager.getLogger(ExtController.class);
	
	@Resource(name = "mLService")
	private MLService  mLService;
	
	@Resource(name="modelObjUtil")
	protected ModelObjUtil modelObjUtil;

	@RequestMapping(value = "/orgToCnv")
	@ResponseBody
	public String logToCnv(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		try{
			Map<String,Object> param = new HashMap<String,Object>();
			param.put("START_DT", StringUtil.get(request.getParameter("start_dt")));
			param.put("END_DT", StringUtil.get(request.getParameter("end_dt")));
			mLService.orgToCnv(param);
			return "ok";
			}catch(RuntimeException runE){
				LOGGER.error(runE);
				runE.printStackTrace();
				 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				 return null;
			}catch(Exception e){
				LOGGER.error(e);
				e.printStackTrace();
				 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				 return null;
			}
	}
	
	@RequestMapping(value = "/modelCreate")
	@ResponseBody
	public String modelCreate() throws Exception{
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("USER_ID", "SYSTEM");
		mLService.modelCreate(param);
		return "ok";
	}
	
	@RequestMapping(value = "/mlLoadInit")
	@ResponseBody
	public String modelInit() throws Exception{
		modelObjUtil.savedModelRoadInit(new HashMap<String,Object>());
		//mLService.orgToCnv(new HashMap<String,Object>());
		return "ok";
	}
	
}
