package com.levware.library.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ @ 수정일 수정자 수정내용 
 * @ --------- --------- ------------------------------- @
 */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.levware.common.FileService;
import com.levware.library.service.LibraryService;

@Controller
@RequestMapping(value="/library")
public class LibraryController {
	
	private static final Logger LOGGER = LogManager.getLogger(LibraryController.class);
	
	@Resource(name = "libraryService")
	private LibraryService  libraryService;
	
	
	@Autowired
    private FileService fileService;
	
	/**
	 * 자료실 view
	 * @since 2020.12.21
	 * @author 강전일
	 */
	@RequestMapping(value = "/FileData", method = RequestMethod.GET)
	public String viewLibraryFileData(ModelMap model, Authentication authentication) throws Exception {
		LOGGER.debug("FileData page init");
		User user = (User) authentication.getPrincipal();
		String role = authentication.getAuthorities().toString();
		model.addAttribute("role", role);
		model.addAttribute("userName", user.getUsername());
		model.addAttribute("authState", "Logined");
		model.addAttribute("viewTemplate","admin");
		model.addAttribute("viewName","commonCode");
		return "library/FileData";
	}
	
	/**
	 * 자료실 upload & insert
	 * @since 2020.12.22
	 * @author 강전일
	 */
	@RequestMapping(value = "/savLibrary",method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> savLibrary(@RequestParam("files") MultipartFile files
			, @RequestParam("FOLDER_ID") String folderId
			, @RequestParam("REF_ID") String refId
			, @RequestParam("DOC_NO") String docNo
			, @RequestParam("REF_NM") String refNm
			, @RequestParam("CRE_DT") String creDt
			, @RequestParam("REVI_LEV") String reviLev
			, @RequestParam("KEYWORD_1") String keyword1
			, @RequestParam("KEYWORD_2") String keyword2
			, @RequestParam("KEYWORD_3") String keyword3
			, @RequestParam("KEYWORD_4") String keyword4
			, @RequestParam("KEYWORD_5") String keyword5
			, @RequestParam("KEYWORD_6") String keyword6
			, @RequestParam("KEYWORD_7") String keyword7
			, @RequestParam("KEYWORD_8") String keyword8
			, @RequestParam("KEYWORD_9") String keyword9
			, @RequestParam("KEYWORD_10") String keyword10
			, @RequestParam("target1_check") String target1checked
			, @RequestParam("target2_check") String target2checked
			, @RequestParam("target3_check") String target3checked
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("fileUploadInsert page init");
			User user = (User) authentication.getPrincipal();
			String role = authentication.getAuthorities().toString();
			String menuId = "ref_data";
			String fGroupId = "";
			String chkUsf = "";
			String chkSearch = "";
			String chkSav = "";
			if(target1checked.equals("true")){
				chkUsf = "Y";
			}else{
				chkUsf = "N";
			}
			if(target2checked.equals("true")){
				chkSearch = "Y";
			}else{
				chkSearch = "N";
			}
			if(target3checked.equals("true")){
				chkSav = "Y";
			}else{
				chkSav = "N";
			}
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			param.put("files", files);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("FOLDER_ID", folderId);
			param.put("REF_ID", refId);
			param.put("DOC_NO", docNo);
			param.put("REF_NM", refNm);
			param.put("CRE_DT", creDt);
			param.put("REVI_LEV", reviLev);
			param.put("ACC_USF_YN", chkUsf);
			param.put("ACC_SCH_YN", chkSearch);
			param.put("ACC_SAV_YN", chkSav);
			param.put("KEYWORD_1", keyword1);
			param.put("KEYWORD_2", keyword2);
			param.put("KEYWORD_3", keyword3);
			param.put("KEYWORD_4", keyword4);
			param.put("KEYWORD_5", keyword5);
			param.put("KEYWORD_6", keyword6);
			param.put("KEYWORD_7", keyword7);
			param.put("KEYWORD_8", keyword8);
			param.put("KEYWORD_9", keyword9);
			param.put("KEYWORD_10", keyword10);
			param.put("REF_TYPE", "test");
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			libraryService.savLibrary(param);
			result.put("result",param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}
	
	/**
	 * 자료실 다운로드
	 * @since 2020.12.28
	 * @author 강전일
	 */
	@RequestMapping(value = "/downLibraryFile", method = RequestMethod.GET )
	public void downLibraryFile(@RequestParam("file_name") String fileName,
			@RequestParam("file_path") String filePath,
			@RequestParam("file_name_org") String fileNameOrg, HttpServletRequest request, HttpServletResponse response) throws Exception {
		LOGGER.info("downLibraryFile");
		fileService.download(filePath,fileName,fileNameOrg,request,response);
	}
	
	/**
	 * 자료실 조회
	 * @since 2020.12.28
	 * @author 강전일
	 */
	@RequestMapping(value = "/getLibraryList", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String, Object>> getLibraryList(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("call getLibraryList");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
	    	List<Map<String, Object>> list = libraryService.getLibraryList(param);
	    	return list;
			
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}
	
	/**
	 * 자료실 키워드 검색
	 * @since 2021.02.09
	 * @author 강전일
	 */
	@RequestMapping(value = "/getKeySchList", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String, Object>> getKeySchList(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("call getKeySchList");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			List<Map<String, Object>> list = libraryService.getKeySchList(param);
			return list;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	/**
	 * 자료실 delete
	 * @since 2020.12.28
	 * @author 강전일
	 */
	@RequestMapping(value = "/deleteLibrary", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> deleteLibrary(@RequestBody Map<String, Object> param, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("deleteLibrary page init");
			libraryService.delLibrary(param);
			result.put("result",param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			return null;
		}
	}	
	
	/**
	 * 자료실 update
	 * @since 2020.12.29
	 * @author 강전일
	 */
	@RequestMapping(value = "/editLibrary", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> editLibrary(@RequestParam("files") MultipartFile files
			, @RequestParam("FOLDER_ID") String folderId
			, @RequestParam("REF_ID") String refId
			, @RequestParam("DOC_NO") String docNo
			, @RequestParam("REF_NM") String refNm
			, @RequestParam("CRE_DT") String creDt
			, @RequestParam("REVI_LEV") String reviLev
			, @RequestParam("KEYWORD_1") String keyword1
			, @RequestParam("KEYWORD_2") String keyword2
			, @RequestParam("KEYWORD_3") String keyword3
			, @RequestParam("KEYWORD_4") String keyword4
			, @RequestParam("KEYWORD_5") String keyword5
			, @RequestParam("KEYWORD_6") String keyword6
			, @RequestParam("KEYWORD_7") String keyword7
			, @RequestParam("KEYWORD_8") String keyword8
			, @RequestParam("KEYWORD_9") String keyword9
			, @RequestParam("KEYWORD_10") String keyword10
			, @RequestParam("target1_check") String target1checked
			, @RequestParam("target2_check") String target2checked
			, @RequestParam("target3_check") String target3checked
			, @RequestParam("FILE_PATH") String filePath
			, @RequestParam("FILE_NAME") String fileName
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("fileUploadInsert page init");
			User user = (User) authentication.getPrincipal();
			String role = authentication.getAuthorities().toString();
			String menuId = "ref_data";
			String fGroupId = "";
			String chkUsf = "";
			String chkSearch = "";
			String chkSav = "";
			if(target1checked.equals("true")){
				chkUsf = "Y";
			}else{
				chkUsf = "N";
			}
			if(target2checked.equals("true")){
				chkSearch = "Y";
			}else{
				chkSearch = "N";
			}
			if(target3checked.equals("true")){
				chkSav = "Y";
			}else{
				chkSav = "N";
			}
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			//파일이 새로 들어왔는지 그대로인지 체크
			param.put("files", files);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("REF_ID", refId);
			param.put("DOC_NO", docNo);
			param.put("REF_NM", refNm);
			param.put("CRE_DT", creDt);
			param.put("REVI_LEV", reviLev);
			param.put("ACC_USF_YN", chkUsf);
			param.put("ACC_SCH_YN", chkSearch);
			param.put("ACC_SAV_YN", chkSav);
			param.put("KEYWORD_1", keyword1);
			param.put("KEYWORD_2", keyword2);
			param.put("KEYWORD_3", keyword3);
			param.put("KEYWORD_4", keyword4);
			param.put("KEYWORD_5", keyword5);
			param.put("KEYWORD_6", keyword6);
			param.put("KEYWORD_7", keyword7);
			param.put("KEYWORD_8", keyword8);
			param.put("KEYWORD_9", keyword9);
			param.put("KEYWORD_10", keyword10);
			param.put("FILE_PATH", filePath);
			param.put("FILE_NAME", fileName);
			param.put("REF_TYPE", "test");
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			libraryService.editLibrary(param);
			result.put("result", param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	/**
	 * 자료실 update 파일이 없는경우
	 * @since 2020.12.29
	 * @author 강전일
	 */
	@RequestMapping(value = "/noFile", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Object> noFile(@RequestParam("NEW_YN") String newYn
			, @RequestParam("FOLDER_ID") String folderId
			, @RequestParam("REF_ID") String refId
			, @RequestParam("DOC_NO") String docNo
			, @RequestParam("REF_NM") String refNm
			, @RequestParam("CRE_DT") String creDt
			, @RequestParam("REVI_LEV") String reviLev
			, @RequestParam("KEYWORD_1") String keyword1
			, @RequestParam("KEYWORD_2") String keyword2
			, @RequestParam("KEYWORD_3") String keyword3
			, @RequestParam("KEYWORD_4") String keyword4
			, @RequestParam("KEYWORD_5") String keyword5
			, @RequestParam("KEYWORD_6") String keyword6
			, @RequestParam("KEYWORD_7") String keyword7
			, @RequestParam("KEYWORD_8") String keyword8
			, @RequestParam("KEYWORD_9") String keyword9
			, @RequestParam("KEYWORD_10") String keyword10
			, @RequestParam("target1_check") String target1checked
			, @RequestParam("target2_check") String target2checked
			, @RequestParam("target3_check") String target3checked
			, @RequestParam("FILE_PATH") String filePath
			, @RequestParam("FILE_NAME") String fileName
			, Authentication authentication, HttpServletResponse response) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("fileUploadInsert page init");
			User user = (User) authentication.getPrincipal();
			String role = authentication.getAuthorities().toString();
			String menuId = "ref_data";
			String fGroupId = "";
			String chkUsf = "";
			String chkSearch = "";
			String chkSav = "";
			if(target1checked.equals("true")){
				chkUsf = "Y";
			}else{
				chkUsf = "N";
			}
			if(target2checked.equals("true")){
				chkSearch = "Y";
			}else{
				chkSearch = "N";
			}
			if(target3checked.equals("true")){
				chkSav = "Y";
			}else{
				chkSav = "N";
			}
			//파일 업로드 후 경로 및 파일정보 가져옴
			Map<String, Object> param = new HashMap<>();
			//파일이 새로 들어왔는지 그대로인지 체크
			param.put("newYn", newYn);
			param.put("menuId", menuId);
			param.put("fGroupId", fGroupId);
			param.put("FOLDER_ID", folderId);
			param.put("REF_ID", refId);
			param.put("DOC_NO", docNo);
			param.put("REF_NM", refNm);
			param.put("CRE_DT", creDt);
			param.put("REVI_LEV", reviLev);
			param.put("ACC_USF_YN", chkUsf);
			param.put("ACC_SCH_YN", chkSearch);
			param.put("ACC_SAV_YN", chkSav);
			param.put("KEYWORD_1", keyword1);
			param.put("KEYWORD_2", keyword2);
			param.put("KEYWORD_3", keyword3);
			param.put("KEYWORD_4", keyword4);
			param.put("KEYWORD_5", keyword5);
			param.put("KEYWORD_6", keyword6);
			param.put("KEYWORD_7", keyword7);
			param.put("KEYWORD_8", keyword8);
			param.put("KEYWORD_9", keyword9);
			param.put("KEYWORD_10", keyword10);
			param.put("FILE_PATH", filePath);
			param.put("FILE_NAME", fileName);
			param.put("REF_TYPE", "test");
			param.put("USER_ID", user.getUsername());
			//DB에 insert
			libraryService.noFile(param);
			result.put("result", param.get("result"));
			return result;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
	}	
	
	/**
	 * 문서검색 트리 조회
	 * @since 2021.02.03
	 * @author 강전일
	 */
	@RequestMapping(value = "/searchDocTree", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String,Object>> searchDocTree(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("call getLibraryList");
			List<Map<String, Object>> list = libraryService.searchDocTree(param);
			return list;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}	
	}	
	
	/**
	 * 문서검색 트리 저장
	 * @since 2021.02.03
	 * @author 강전일
	 */
	@RequestMapping(value = "/savDocTree",method = RequestMethod.POST)
	@ResponseBody
	public void savDocTree(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("savDocTree page init");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			libraryService.savDocTree(param);
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	
	/**
	 * 문서검색 트리 수정
	 * @since 2021.02.03
	 * @author 강전일
	 */
	@RequestMapping(value = "/editDocTree",method = RequestMethod.POST)
	@ResponseBody
	public void editDocTree(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			Map<String,Object> result = new HashMap<String,Object>();
			LOGGER.debug("editDocTree page init");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			libraryService.editDocTree(param);
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}	
	
	/**
	 * 문서검색 트리 삭제
	 * @since 2021.02.03
	 * @author 강전일
	 */
	@RequestMapping(value = "/delDocTree",method = RequestMethod.POST)
	@ResponseBody
	public void delDocTree(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("delDocTree page init");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			libraryService.delDocTree(param);
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}	
	
	/**
	 * 폴더 이동 붙여넣기
	 * @since 2021.03.24
	 * @author 강전일
	 */
	@RequestMapping(value = "/dataPaste",method = RequestMethod.POST)
	@ResponseBody
	public void dataPaste(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("dataPaste page init");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			libraryService.dataPaste(param);
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}	
	
	/**
	 * 문서검색 트리 조회
	 * @since 2021.02.03
	 * @author 강전일
	 */
	@RequestMapping(value = "/getSubfolderYn", method = RequestMethod.POST )
	@ResponseBody
	public int getSubfolderYn(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
			LOGGER.debug("call getSubfolderYn");
			int result = libraryService.getSubfolderYn(param);
			return result;
	}	
	
	/**
	 * 저장시 중복 데이터 확인
	 * @since 2021.02.03
	 * @author 강전일
	 */
	@RequestMapping(value = "/confData", method = RequestMethod.POST )
	@ResponseBody
	public Map<String, Object> confData(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		LOGGER.debug("call confData");
		Map<String, Object> result = libraryService.confData(param);
		return result;
	}	
	
	/**
	 * 자료실 전체 검색
	 * @since 2021.02.09
	 * @author 강전일
	 */
	@RequestMapping(value = "/getAllData", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String, Object>> getAllData(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("call getAllData");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			List<Map<String, Object>> list = libraryService.getAllData(param);
			return list;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
	
	/**
	 * 자료실 전체 키워드 검색
	 * @since 2021.02.09
	 * @author 강전일
	 */
	@RequestMapping(value = "/getKeyAllData", method = RequestMethod.POST )
	@ResponseBody
	public List<Map<String, Object>> getKeyAllData(@RequestBody Map<String, Object> param, HttpServletResponse response, Authentication authentication) throws Exception {
		try{
			LOGGER.debug("call getKeyAllData");
			User user = (User) authentication.getPrincipal();
			param.put("USER_ID", user.getUsername());
			List<Map<String, Object>> list = libraryService.getKeyAllData(param);
			return list;
		}catch(RuntimeException runE){
			LOGGER.error(runE);
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().print(runE.getMessage());
			return null;
		}catch(Exception e){
			LOGGER.error(e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}	
	}
}
