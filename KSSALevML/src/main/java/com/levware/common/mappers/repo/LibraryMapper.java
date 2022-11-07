package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import egovframework.rte.psl.dataaccess.mapper.Mapper;

/**
 *
 * Oracle Repository DataSource Mapper Class
 * @author 강전일
 * @since 2020. 12. 23.
 */
@Mapper("libraryMapper")
public interface LibraryMapper {
	public void savLibrary(Map<String, Object> param) throws Exception;
	public void delLibrary(Map<String, Object> param) throws Exception;
	public void editLibrary(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getLibraryList(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getKeySchList(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> searchDocTree(Map<String, Object> param) throws Exception;
	public void savDocTree(Map<String, Object> param) throws Exception;
	public void editDocTree(Map<String, Object> param) throws Exception;
	public void delDocTree(Map<String, Object> param) throws Exception;
	public void dataPaste(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> delDocSch(Map<String, Object> param) throws Exception;
	public void delDocLibrary(Map<String, Object> param) throws Exception;
	public int getSubfolderYn(Map<String, Object> param) throws Exception;
	public int confFirst(Map<String, Object> param) throws Exception;
	public int confSecond(Map<String, Object> param) throws Exception;
	public int confThird(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getAllData(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getKeyAllData(Map<String, Object> param) throws Exception;
}
