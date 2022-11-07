<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<script src="<c:url value='/js/common/olap-baseConfig.js'/>"></script>
<script src="<c:url value='/js/common/olapHelpMessage/messages/dashboardMsg.js'/>"></script> <!--먼저 선언되어있는지확인하기.  -->
<script src="<c:url value='/js/common/olapHelpMessage/OLAPHelpMessage.js'/>"></script>
<script src="<c:url value='/js/user/board.js'/>"></script>


<div class ="container">
			<div class="row">
			     
			        <div class="col-12" style="height: 600px; margin-top: 100px;">
			        	<div class="h5">
						<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">게시글 조회</span></strong>
						</div>
			            <!--  게시글 작성 -->
			            
			                <table class="table mt-3">
			                    <tr>
			                        <th>제목</th>
			                        <td style="display: none;"><input type="text" id="seqNum" name="seqNum" value="${seqNum }" readonly="readonly"/></td>
			                        <td><p>${boardTitle }</p></td>
			                    </tr>
			                    <tr>
			                        <th>날짜</th>
			                        <td><p>${createDt }</p></td>
			                    </tr>
			                    <tr>
			                        <th>내용</th>
			                        <td><textarea rows="10" cols="10" readonly="readonly" style="font: bold;"disabled="disabled">${boardContents }</textarea></td>
			                       
			                    </tr>
			                    <tr>
			                        <th>작성자</th>
			                        <td><p>${boardWriter }</p></td>
			                    </tr>
			                    <tr>
			                    	<th></th>
			                    	<td></td>
			                    </tr>
			                </table>
			                <div class="d-flex justify-content-end">
			                	<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">
			                    <a href='#' onClick='fn_update()' class="btn btn-outline-success" style="margin-right: 1%;">수정하기 <i class="fas fa-edit"></i></a>
			            		<a href='#' onClick='fn_delte()' class="btn btn-outline-primary" style="margin-right: 1%;">삭제하기 <i class="fas fa-trash-alt"></i></a>
			            		</c:if>
			                    <a href='#' id ="returnBtn" onClick='fn_cancel()' class="btn btn-outline-info" >목록 <i class="fas fa-list"></i></a>
			                </div>
			           
			        </div>
			
			</div>
</div>

<script>
	//수정하기
	function fn_update(){
		seqNum = $("#seqNum").val();
		
		 BoardDataList.updateFormAction(seqNum);
	
	}
	//삭제하기
	function fn_delte(){
		seqNum = $("#seqNum").val();
		
		 BoardDataList.deleteFormAction(seqNum);
	
	}
	 
	//목록
	 function fn_cancel(){
		 location.href = "/user/api/data/board.do";
		
	    
	}  
</script>