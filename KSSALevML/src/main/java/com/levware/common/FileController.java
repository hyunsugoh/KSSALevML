package com.levware.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class FileController {
	
	@Autowired
    private FileService fileService;
	
	@RequestMapping(value = "/fileDownload",method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String filePath = "D:\\";
		String fileName = "B3 API682_4th Seal Selection Justification.pdf";
		fileService.download(filePath,fileName,fileName,request,response);
	}

	/*
	 * 
	 * 
	@RequestMapping(value = "/fileDownload",method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestInfo reqInfo = new RequestInfo(request);
		Map<String,Object> map = commonService.selectMap("selectFile",reqInfo);
		String filePath = map.get("filepath").toString();
		String fileName = map.get("filename").toString();
		fileService.download(filePath,fileName,request,response);
	}
	
	
	@RequestMapping(value = "/common/pop/fileUpload.POP")
	public String fileUploadPopTest(HttpServletRequest request, Model model) throws Exception {
		return "/pop/common/Popup_fileupload";
	}
	
	@RequestMapping(value = "/common/pop/fileDownload.POP")
	public String fileDownloadPopTest(HttpServletRequest request, Model model) throws Exception {
		RequestInfo reqInfo = new RequestInfo(request);
		List<Map<String,Object>> fileList = commonService.selectList("selectFileList",reqInfo);
		model.addAttribute("FILE_LIST",fileList);
		return "/pop/common/Popup_filedownload";
	}
	
	@RequestMapping(value = "/common/pop/fileMng.POP")
	public String fileMngPop(HttpServletRequest request, Model model) throws Exception {
		RequestInfo reqInfo = new RequestInfo(request);
		List<Map<String,Object>> fileList = commonService.selectList("selectFileList",reqInfo);
		model.addAttribute("FILE_LIST",fileList);
		return "/pop/common/Popup_fileMng";
	}
	
	@ResponseBody
	@RequestMapping(value = "/common/fileMng/search",method = RequestMethod.POST)
	public List<Map<String,Object>> selectDealerOrderMng(@RequestBody Map<String,Object> param) throws Exception {
		List<Map<String,Object>> result = commonService.selectList("selectFileList",param); 
		return result;
	}
		
	@ResponseBody
	@RequestMapping(value = "/fileUpload",method = RequestMethod.POST)
	public String upload(@RequestParam("files") MultipartFile[] files,
						 @RequestParam("menuId") String menuId,
						 @RequestParam("fGroupId") String fGroupId) throws Exception {
		String fileGroupId = fileService.upload(files,menuId,fGroupId);
		return fileGroupId;
	}
	
	@RequestMapping(value = "/fileDownload",method = RequestMethod.GET)
	public void download(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestInfo reqInfo = new RequestInfo(request);
		Map<String,Object> map = commonService.selectMap("selectFile",reqInfo);
		String filePath = map.get("filepath").toString();
		String fileName = map.get("filename").toString();
		fileService.download(filePath,fileName,request,response);
	}
	
	@RequestMapping(value = "/groupFileDownloadAll",method = RequestMethod.GET)
	public void downloadAll(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestInfo reqInfo = new RequestInfo(request);
		List<Map<String,Object>> fileList = commonService.selectList("selectFileList", reqInfo);
		fileService.downloadZip(fileList,request,response);
	}
	
	@RequestMapping(value = "/multiFileDownloadAll",method = RequestMethod.GET)
	public void downloadAll2(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestInfo reqInfo = new RequestInfo(request);
		List<Map<String,Object>> fileList = commonService.selectList("selectTempFileList", reqInfo);
		fileService.downloadZip(fileList,request,response);
	}
	
	@ResponseBody
	@RequestMapping(value = "/fileDelete",method = RequestMethod.POST)
	public Map<String,Object> delete(@RequestBody Map<String,Object> param) throws Exception {
		fileService.delete(param.get("fileid").toString());
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("message","success");
		return result;
	}
	
	@RequestMapping(value = "/excelDownload",method = RequestMethod.GET)
	public void excelDownload(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestInfo reqInfo = new RequestInfo(request);
		excelDownService.downDealChannelMng(response,reqInfo);
	}
	
	@RequestMapping(value = "/excelDownload_QT",method = RequestMethod.GET)
	public void excelDownload_QueryTool2(HttpServletRequest request, HttpServletResponse response) throws Exception {
		excelDownService.setHeaderQueryTool(response);
	}
	*/
}
