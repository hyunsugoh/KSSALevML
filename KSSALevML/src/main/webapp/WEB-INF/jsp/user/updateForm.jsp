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
						<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">게시글 수정</span></strong>
						</div>
			            <!--  게시글 작성 -->
			          
			                <table class="table mt-3">
			                    <tr>
			                        <th>제목</th>
			                        <td><input type="text" id="title" name="title" value="${boardTitle }"/></td>
			                        <td style="display: none;"><input type="text" id="seqNum" name="seqNum" value="${seqNum }" readonly="readonly"/></td>
			                    </tr>
			                    <tr>
			                        <th>날짜</th>
			                        <td><p>${createDt}</p></td>
			                    </tr>
			                    <tr>
			                        <th>내용</th>
			                        <%-- <td><input style="height: 300px;" type="text" id="content" name="content" value="${boardContents }"/></td> --%>
			                        <td><textarea rows="10" cols="10" id="content" name="content">${boardContents }</textarea></td>
			                    </tr>
			                    <tr>
			                        <th>작성자</th>
			                        <td><p>${boardWriter}</p></td>
			                    </tr>
			                    <tr>
			                    	<th></th>
			                    	<td></td>
			                    </tr>
			                </table>
			                <div class="d-flex justify-content-end">
			                	<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">
			                    <a href='#' onClick='fn_addtoBoard()' class="btn btn-outline-success" style="margin-right: 1%;"> 저장하기 <i class="fas fa-save"></i></a>
			                    </c:if>
			                    <a href='#' id ="returnBtn" onClick='fn_cancel()' class="btn btn-outline-info" > 취소 <i class="fas fa-times"></i></a>
			                </div>
			          
			        </div>
			   
			</div>
</div>

<script>
	//수정내용 저장
	function fn_addtoBoard(){
		 seqNum = $("#seqNum").val();
		 userid = ("${userName}");
		 BoardDataList.saveAction(
				{
					title : $("#title").val(),
					content : $("#content").val(),
					writer : $("#writer").val(),
					userid : userid,
					seqNum : seqNum
					
				}); 
		
	}
	
	 
	//취소
	 function fn_cancel(){
		 location.href = "/user/api/data/board.do";
		
	    
	}  
</script>