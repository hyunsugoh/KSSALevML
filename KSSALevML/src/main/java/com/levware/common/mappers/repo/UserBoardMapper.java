package com.levware.common.mappers.repo;

import java.util.List;
import java.util.Map;

import com.levware.user.service.BoardVO;

import egovframework.rte.psl.dataaccess.mapper.Mapper;
@Mapper("UserBoardMapper")
public interface UserBoardMapper {
	//게시판 리스트 출력
	public List<BoardVO> boardList() throws Exception;
	//게시글 번호
	public int boardViewSeq(String userName) throws Exception;
	//게시글 등록
	public void insertBoard(Map<String, Object> setData) throws Exception;
	//게시글 수정
	public void updateBoard(Map<String, Object> setData) throws Exception;
	
	public List<BoardVO> selectBoard(int seqNum) throws Exception ;
	
	public void updateViewCnt(int seqNum) throws Exception;
	
	public void deleteBoard(int seqNum) throws Exception;
	
	
	
}
