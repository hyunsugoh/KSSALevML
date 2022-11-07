package com.levware.user.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.levware.common.mappers.repo.UserBoardMapper;
import com.levware.user.service.BoardVO;
import com.levware.user.service.UserBoardService;

@Service("UserBoardService")
public class UserBoardServiceImpl implements UserBoardService {
	
	public static Logger LOGGER = LogManager.getFormatterLogger(UserDataAPIServiceImpl.class);
	
	@Resource(name="UserBoardMapper")
	private UserBoardMapper boardMapper;
	
	@Autowired
	@Resource(name="garakDataSqlSession")
	private DefaultSqlSessionFactory sqlSession;
	
	/**
	 * boardlist
	 * 게시판 리스트 \ 불러오기
	 * @return ArrayList<OlapObjectVO> 
	 */
	@Override
	public List<BoardVO> boardList() throws Exception {
		
		return boardMapper.boardList();
	}

	@Override
	public void insertBoard(Map<String, Object> params) throws Exception {
		
		LOGGER.debug(params);
		
		String userName = (String) params.get("writer"); //String 형변환.
		String userId =(String)params.get("userid");
		
		Map<String, Object> setData = new HashMap<String, Object>(); //MAP 생성 후 데이터 담아주기

		SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd"); //년월일 형식 포맷
		SimpleDateFormat tmFormat = new SimpleDateFormat("HHmmss"); //시분초 포맷
		Date time = new Date(); //현재 시간
		String ctDt = dtFormat.format(time); // 년월일 포맷에 현재시간 넣어주기
		String ctTm = tmFormat.format(time); // 시분초  "
		
		setData.put("userId", userId);
		setData.put("boardTitle", params.get("title"));
		setData.put("boardContents", params.get("content"));
		setData.put("boardWriter", userName);
		LOGGER.debug(params.toString());
		if(params.containsKey("seqNum")){
			//update
			setData.put("seqNum", params.get("seqNum"));
			setData.put("updateDt", ctDt);
			setData.put("updateTm", ctTm);
			boardMapper.updateBoard(setData);
		}else{
			//insert
			int seqNum =  boardMapper.boardViewSeq(userId);

			seqNum++;
			setData.put("seqNum", seqNum);
			setData.put("createDt", ctDt);
			setData.put("createTm", ctTm);
			boardMapper.insertBoard(setData);
		}
		
	}

	@Override
	public List<BoardVO> selectBoard(int seqnum) throws Exception {
		return boardMapper.selectBoard(seqnum);
		
	}
	
	

	@Override
	public void updateViewCnt(int seqnum) throws Exception {
		boardMapper.updateViewCnt(seqnum);
		
	}

	@Override
	public void deleteBoard(int seqnum) throws Exception {
		 boardMapper.deleteBoard(seqnum);
	}

	
	
	
	


	

	
	
	
	
	
}
