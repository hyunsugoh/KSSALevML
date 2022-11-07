package com.levware.user.service;

import java.util.List;
import java.util.Map;


public interface UserBoardService {
	
	public List<BoardVO> boardList() throws Exception;
	
	public void insertBoard(Map<String, Object> params) throws Exception;
	
	public List<BoardVO> selectBoard(int seqnum) throws Exception;
	
	public void updateViewCnt(int seqnum) throws Exception;
	
	public void deleteBoard(int seqnum) throws Exception;
	
}
