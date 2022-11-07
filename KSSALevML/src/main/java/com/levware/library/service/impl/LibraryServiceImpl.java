package com.levware.library.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.levware.common.FileService;
import com.levware.common.mappers.repo.LibraryMapper;
import com.levware.library.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;

@Service("libraryService")
public class LibraryServiceImpl implements LibraryService {
	
	@Resource(name = "libraryMapper")
	private LibraryMapper libraryMapper;
	
	@Autowired
    private FileService fileService;
	
	@Override
	public void savLibrary(Map<String, Object> param) throws Exception {
		MultipartFile files	= (MultipartFile) param.get("files");
		String menuId 		= param.get("menuId")==null ? "" : param.get("menuId").toString(); 
		String fGroupId 	= param.get("fGroupId")==null ? "" : param.get("fGroupId").toString(); 
		String folderId 	= param.get("FOLDER_ID")==null ? "" : param.get("FOLDER_ID").toString(); 
		String refId 		= param.get("REF_ID")==null ? "" : param.get("REF_ID").toString(); 
		String refNm		= param.get("REF_NM")==null ? "" : param.get("REF_NM").toString(); 
		String docNo		= param.get("DOC_NO")==null ? "" : param.get("DOC_NO").toString(); 
		String creDt		= param.get("CRE_DT")==null ? "" : param.get("CRE_DT").toString(); 
		String reviLev		= param.get("REVI_LEV")==null ? "" : param.get("REVI_LEV").toString(); 
		String chkUsf		= param.get("ACC_USF_YN")==null ? "" : param.get("ACC_USF_YN").toString(); 
		String chkSearch 	= param.get("ACC_SCH_YN")==null ? "" : param.get("ACC_SCH_YN").toString(); 
		String chkSav 		= param.get("ACC_SAV_YN")==null ? "" : param.get("ACC_SAV_YN").toString(); 
		String keyword1 	= param.get("KEYWORD_1")==null ? "" : param.get("KEYWORD_1").toString(); 
		String keyword2 	= param.get("KEYWORD_2")==null ? "" : param.get("KEYWORD_2").toString(); 
		String keyword3 	= param.get("KEYWORD_3")==null ? "" : param.get("KEYWORD_3").toString(); 
		String keyword4 	= param.get("KEYWORD_4")==null ? "" : param.get("KEYWORD_4").toString(); 
		String keyword5 	= param.get("KEYWORD_5")==null ? "" : param.get("KEYWORD_5").toString(); 
		String keyword6 	= param.get("KEYWORD_6")==null ? "" : param.get("KEYWORD_6").toString(); 
		String keyword7 	= param.get("KEYWORD_7")==null ? "" : param.get("KEYWORD_7").toString(); 
		String keyword8 	= param.get("KEYWORD_8")==null ? "" : param.get("KEYWORD_8").toString(); 
		String keyword9 	= param.get("KEYWORD_9")==null ? "" : param.get("KEYWORD_9").toString(); 
		String keyword10 	= param.get("KEYWORD_10")==null ? "" : param.get("KEYWORD_10").toString(); 
		String test 		= param.get("REF_TYPE")==null ? "" : param.get("REF_TYPE").toString(); 
		String user 		= param.get("USER_ID")==null ? "" : param.get("USER_ID").toString(); 
		
		Map<String, Object> result = fileService.upload(files, menuId, fGroupId);
		result.put("FOLDER_ID", folderId);
		result.put("REF_ID", refId);
		result.put("DOC_NO", docNo);
		result.put("REF_NM", refNm);
		result.put("CRE_DT", creDt);
		result.put("REVI_LEV", reviLev);
		result.put("ACC_USF_YN", chkUsf);
		result.put("ACC_SCH_YN", chkSearch);
		result.put("ACC_SAV_YN", chkSav);
		result.put("KEYWORD_1", keyword1);
		result.put("KEYWORD_2", keyword2);
		result.put("KEYWORD_3", keyword3);
		result.put("KEYWORD_4", keyword4);
		result.put("KEYWORD_5", keyword5);
		result.put("KEYWORD_6", keyword6);
		result.put("KEYWORD_7", keyword7);
		result.put("KEYWORD_8", keyword8);
		result.put("KEYWORD_9", keyword9);
		result.put("KEYWORD_10", keyword10);
		result.put("REF_TYPE", "test");
		result.put("USER_ID", user);
		
		libraryMapper.savLibrary(result);
	}
	
	@Override
	public void delLibrary(Map<String, Object> param) throws Exception {
		String filePath = param.get("filePath").toString(); 
		String fileName = param.get("fileName").toString();
		fileService.deleteFile(filePath, fileName);
		libraryMapper.delLibrary(param);
	}
	
	@Override
	public void editLibrary(Map<String, Object> param) throws Exception {
		MultipartFile files	= (MultipartFile) param.get("files");
		String menuId 		= param.get("menuId")==null ? "" : param.get("menuId").toString(); 
		String fGroupId 	= param.get("fGroupId")==null ? "" : param.get("fGroupId").toString(); 
		String folderId 	= param.get("FOLDER_ID")==null ? "" : param.get("FOLDER_ID").toString(); 
		String refId 		= param.get("REF_ID")==null ? "" : param.get("REF_ID").toString(); 
		String refNm		= param.get("REF_NM")==null ? "" : param.get("REF_NM").toString(); 
		String docNo		= param.get("DOC_NO")==null ? "" : param.get("DOC_NO").toString(); 
		String creDt		= param.get("CRE_DT")==null ? "" : param.get("CRE_DT").toString(); 
		String reviLev		= param.get("REVI_LEV")==null ? "" : param.get("REVI_LEV").toString(); 
		String chkUsf		= param.get("ACC_USF_YN")==null ? "" : param.get("ACC_USF_YN").toString(); 
		String chkSearch 	= param.get("ACC_SCH_YN")==null ? "" : param.get("ACC_SCH_YN").toString(); 
		String chkSav 		= param.get("ACC_SAV_YN")==null ? "" : param.get("ACC_SAV_YN").toString(); 
		String keyword1 	= param.get("KEYWORD_1")==null ? "" : param.get("KEYWORD_1").toString(); 
		String keyword2 	= param.get("KEYWORD_2")==null ? "" : param.get("KEYWORD_2").toString(); 
		String keyword3 	= param.get("KEYWORD_3")==null ? "" : param.get("KEYWORD_3").toString(); 
		String keyword4 	= param.get("KEYWORD_4")==null ? "" : param.get("KEYWORD_4").toString(); 
		String keyword5 	= param.get("KEYWORD_5")==null ? "" : param.get("KEYWORD_5").toString(); 
		String keyword6 	= param.get("KEYWORD_6")==null ? "" : param.get("KEYWORD_6").toString(); 
		String keyword7 	= param.get("KEYWORD_7")==null ? "" : param.get("KEYWORD_7").toString(); 
		String keyword8 	= param.get("KEYWORD_8")==null ? "" : param.get("KEYWORD_8").toString(); 
		String keyword9 	= param.get("KEYWORD_9")==null ? "" : param.get("KEYWORD_9").toString(); 
		String keyword10 	= param.get("KEYWORD_10")==null ? "" : param.get("KEYWORD_10").toString(); 
		String test 		= param.get("REF_TYPE")==null ? "" : param.get("REF_TYPE").toString(); 
		String user 		= param.get("USER_ID")==null ? "" : param.get("USER_ID").toString(); 
		String filePath 	= param.get("FILE_PATH")==null ? "" : param.get("FILE_PATH").toString(); 
		String fileName 	= param.get("FILE_NAME")==null ? "" : param.get("FILE_NAME").toString(); 
		Map<String, Object> result = new HashMap<>();
		if(files.equals("Y")){
		}else{
			//파일 업로드 후 경로 및 파일정보 가져옴
			fileService.deleteFile(filePath, fileName);
			result = fileService.upload(files, menuId, fGroupId);
		}
		result.put("FOLDER_ID", folderId);
		result.put("REF_ID", refId);
		result.put("DOC_NO", docNo);
		result.put("REF_NM", refNm);
		result.put("CRE_DT", creDt);
		result.put("REVI_LEV", reviLev);
		result.put("ACC_USF_YN", chkUsf);
		result.put("ACC_SCH_YN", chkSearch);
		result.put("ACC_SAV_YN", chkSav);
		result.put("KEYWORD_1", keyword1);
		result.put("KEYWORD_2", keyword2);
		result.put("KEYWORD_3", keyword3);
		result.put("KEYWORD_4", keyword4);
		result.put("KEYWORD_5", keyword5);
		result.put("KEYWORD_6", keyword6);
		result.put("KEYWORD_7", keyword7);
		result.put("KEYWORD_8", keyword8);
		result.put("KEYWORD_9", keyword9);
		result.put("KEYWORD_10", keyword10);
		result.put("REF_TYPE", "test");
		result.put("USER_ID", user);
		libraryMapper.editLibrary(result);
	}
	
	@Override
	public void noFile(Map<String, Object> param) throws Exception {
		String newYn = param.get("newYn").toString();
		if(newYn.equals("Y")){
			libraryMapper.savLibrary(param);
		}else{
			libraryMapper.editLibrary(param);
		}
	}
	
	public List<Map<String,Object>> getLibraryList(Map<String, Object> param) throws Exception{
		return libraryMapper.getLibraryList(param);
	}
	
	public List<Map<String,Object>> getKeySchList(Map<String, Object> param) throws Exception{
		return libraryMapper.getKeySchList(param);
	}
	
	public List<Map<String,Object>> searchDocTree(Map<String, Object> param) throws Exception{
		return libraryMapper.searchDocTree(param);
	}
	
	@Override
	public void savDocTree(Map<String, Object> param) throws Exception {
		libraryMapper.savDocTree(param);
	}
	
	@Override
	public void editDocTree(Map<String, Object> param) throws Exception {
		libraryMapper.editDocTree(param);
	}
	
	@Override
	public void delDocTree(Map<String, Object> param) throws Exception {
		libraryMapper.delDocTree(param);
		List<Map<String, Object>> tempList = libraryMapper.delDocSch(param);
		for(Map<String, Object> dMap : tempList){
			if(dMap != null){
				fileService.deleteFile(dMap.get("FILE_PATH").toString(), dMap.get("FILE_NM").toString());
			}
		}
		libraryMapper.delDocLibrary(param);
	}
	
	@Override
	public void dataPaste(Map<String, Object> param) throws Exception {
		libraryMapper.dataPaste(param);
	}
	
	@Override
	public int getSubfolderYn(Map<String, Object> param) throws Exception {
		return libraryMapper.getSubfolderYn(param);
	}

	@Override
	public Map<String, Object> confData(Map<String, Object> param) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		//문서번호가 동일한데 파일명 다른 경우 (등록진행불가)
		int confFirst = libraryMapper.confFirst(param);
		//파일명이 동일한데 문서번호가 다른 경우 (확인후 등록은 가능)
		int confSecond = libraryMapper.confSecond(param);
		//문서번호와 파일명이 동일한 경우 (작성일 & 레벨이 달라야 등록)
		int confThird = libraryMapper.confThird(param);
		result.put("confFirst", confFirst);
		result.put("confSecond", confSecond);
		result.put("confThird", confThird);
		return result;
	}
	
	public List<Map<String,Object>> getAllData(Map<String, Object> param) throws Exception{
		return libraryMapper.getAllData(param);
	}
	
	public List<Map<String,Object>> getKeyAllData(Map<String, Object> param) throws Exception{
		return libraryMapper.getKeyAllData(param);
	}
}
