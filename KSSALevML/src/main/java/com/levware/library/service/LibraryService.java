package com.levware.library.service;

import java.util.List;
import java.util.Map;

public interface LibraryService {
	public void savLibrary(Map<String,Object> param) throws Exception;
	public void delLibrary(Map<String,Object> param) throws Exception;
	public void editLibrary(Map<String,Object> param) throws Exception;
	public void noFile(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getLibraryList(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getKeySchList(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> searchDocTree(Map<String, Object> param) throws Exception;
	public void savDocTree(Map<String,Object> param) throws Exception;
	public void editDocTree(Map<String,Object> param) throws Exception;
	public void delDocTree(Map<String,Object> param) throws Exception;
	public void dataPaste(Map<String,Object> param) throws Exception;
	public int getSubfolderYn(Map<String,Object> param) throws Exception;
	public Map<String,Object> confData(Map<String,Object> param) throws Exception;
	public List<Map<String,Object>> getAllData(Map<String, Object> param) throws Exception;
	public List<Map<String,Object>> getKeyAllData(Map<String, Object> param) throws Exception;
}
