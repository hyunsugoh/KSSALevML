package com.levware.user.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.levware.user.service.BoardVO;
import com.levware.user.service.UserBoardService;

/**
* 
* <p><b>NOTE:</b> 
*  
* @author hcjo
* @since 
* @version 1.0
* @see
*
* <pre>
* == 개정이력(Modification Information) ==
*
* 수정일	수정자	수정내용
* -------	--------	---------------------------
* 
*
* </pre>
*/
@Controller
@RequestMapping(value="/user/api/",method = RequestMethod.GET, produces="application/json") //value 를 선언하고
public class UserBoardController {
	
	public static Logger LOGGER = LogManager.getLogger(UserBoardController.class);
	
	@Resource(name="UserBoardService")
	private UserBoardService userboardservice;
	
	
	/**
	 * 게시판 화면 이동
	 * @return 
	 * @throws Exception 
	 */
	 @RequestMapping(value="/data/board")
	    public String board(HttpServletResponse response) throws Exception{
	        	
	    	
	    	return "user/board";
	    }
	
	
	/**
	 * 게시판 리스트 호출
	 * @return JSON Object
	 * @throws Exception 
	 */
	@RequestMapping(value="/data/boardList", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public List<BoardVO> boardList(HttpServletResponse response,@RequestParam(defaultValue="")String keyword,@RequestParam(defaultValue="1") int curPage,Model model) throws Exception {
		try{
		
			LOGGER.debug("boardList call");
			List<BoardVO> boardList = userboardservice.boardList();
			return boardList;
		}
		catch(RuntimeException runE){
			LOGGER.error(runE);
			 response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			 return null;
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			 return null;
		}
	}
	
	
	 /**
     * 글쓰기 폼
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/data/boardwriteForm")
    public String boardwriteForm(HttpServletResponse response) throws Exception{
        	
    	
    	return "user/boardWriteForm";
    }
	
    /**
     * 게시글 등록 
     * @return
     * @throws Exception
     */
    
    @RequestMapping(value="/data/insertBoard", headers = "Accept=*/*", method = RequestMethod.POST, produces = "application/json")
    public void insertBoard(HttpServletResponse response, @RequestBody Map<String, Object> params, Authentication authentication) throws Exception{
    	LOGGER.debug("======================== insertBoard ======================== ");
		LOGGER.debug(params);
		try{
			userboardservice.insertBoard(params);
		}catch(Exception e){
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}    
    }
    
    
    /**
     * 게시글 조회  호출
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/data/viewForm", method = RequestMethod.GET)
    public String viewForm(@RequestParam(value="seqNum") int seqNum, ModelMap model, HttpServletResponse response) throws Exception{
    	LOGGER.debug("======================== selectBoard ======================== ");
    	String goUrl = "user/viewForm"; 
    	try {
    		List<BoardVO> seqList = userboardservice.selectBoard(seqNum);
    		userboardservice.updateViewCnt(seqNum);
    		String createDt; 

    		for(int i =0 ;i < seqList.size();i++){
    			//model.addAttribute("createDt",seqList.get(i).getCreateDt());
    			createDt = seqList.get(i).getCreateDt().substring(0, 4)+"."+seqList.get(i).getCreateDt().substring(4, 6)+"."+seqList.get(i).getCreateDt().substring(6, 8);
    			model.addAttribute("seqNum",seqList.get(i).getSeqNum());
    			model.addAttribute("boardTitle",seqList.get(i).getBoardTitle());
    			model.addAttribute("boardContents",seqList.get(i).getBoardContents());
    			model.addAttribute("boardWriter",seqList.get(i).getBoardWriter());
    			model.addAttribute("createDt",createDt);
    			model.addAttribute("createTm",seqList.get(i).getCreateTm()); //model 에 담아서 값을 return (jsp) 넘겨준다.
    		}
    		return goUrl;

		} catch (Exception e) {
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return "user/viewForm";
		}
    	
    	
    }
    
    /**
     * 게시글 수정 
     * @return
     * @throws Exception
     */
    
    @RequestMapping(value="/data/updateForm", method = RequestMethod.GET)
    public String updateForm(@RequestParam(value="seqNum") int seqNum, ModelMap model, HttpServletResponse response) throws Exception{
    	LOGGER.debug("======================== updateBoard ======================== ");
    	String goUrl = "user/updateForm"; 
    	try {
    		List<BoardVO> seqList = userboardservice.selectBoard(seqNum);
    		String createDt; 
    		for(int i =0 ;i < seqList.size();i++){
    			//model.addAttribute("createDt",seqList.get(i).getCreateDt());
    			createDt = seqList.get(i).getCreateDt().substring(0, 4)+"."+seqList.get(i).getCreateDt().substring(4, 6)+"."+seqList.get(i).getCreateDt().substring(6, 8);
    			model.addAttribute("seqNum",seqList.get(i).getSeqNum());
    			model.addAttribute("boardTitle",seqList.get(i).getBoardTitle());
    			model.addAttribute("boardContents",seqList.get(i).getBoardContents());
    			model.addAttribute("boardWriter",seqList.get(i).getBoardWriter());
    			model.addAttribute("createDt",createDt);
    			model.addAttribute("createTm",seqList.get(i).getCreateTm()); //model 에 담아서 값을 return (jsp) 넘겨준다.
    		}
    		return goUrl;

		} catch (Exception e) {
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
    	
    
    }
    
    /**
     * 게시글 삭제
     * @return
     * @throws Exception
     */
    
    @RequestMapping(value="/data/deleteForm", method = RequestMethod.GET)
    public String deleteForm(@RequestParam(value="seqNum") int seqNum, ModelMap model, HttpServletResponse response) throws Exception{
    	LOGGER.debug("======================== deleteBoard ======================== ");
    	try {
    		userboardservice.deleteBoard(seqNum);
    		return "user/board";
		} catch (Exception e) {
			LOGGER.error(e);
			 response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return null;
		}
    	
    
    }
    
	
	
    
	


}
