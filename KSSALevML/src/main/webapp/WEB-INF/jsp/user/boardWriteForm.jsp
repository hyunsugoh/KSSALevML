<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<script src="<c:url value='/js/common/olap-baseConfig.js'/>"></script>
<script src="<c:url value='/js/common/olapHelpMessage/messages/dashboardMsg.js'/>"></script> <!--먼저 선언되어있는지확인하기.  -->
<script src="<c:url value='/js/common/olapHelpMessage/OLAPHelpMessage.js'/>"></script>
<script src="<c:url value='/js/user/board.js'/>"></script>

<script> 
$(document).ready(function() {
	
	$("#writer").val("${userName}");
	
}); 
</script>

	<div class ="container">
				<div class="row">
			     
			        <div class="col-12" style="height: 600px; margin-top: 100px;">
			        	<div class="h5">
						<strong><i class="far fa-object-ungroup"></i> <span
							class="ml-1">게시글 작성</span></strong>
						</div>
			            <!--  게시글 작성 -->
			            
			                <table class="table mt-3">
			                    <tr>
			                        <th>제목</th>
			                        <td><input type="text" id="title" name="title" placeholder="제목을 입력하세요." /></td>
			                    </tr>
			                    <tr>
			                        <th>내용</th>
			                        <td><textarea  rows="10" cols="10" id="content" name="content" placeholder="내용을 입력하세요.(최대 한글 2000자)"></textarea></td>
			                    </tr>
			                    <tr>
			                        <th>작성자</th>
			                        <td><p id="writer">${userName}</p></td>
			                    </tr>
			                    <tr>
			                    	<th></th>
			                    	<td></td>
			                    </tr>
			                </table>
			                <div class="d-flex justify-content-end">
			                    <a href='#' onClick='fn_addtoBoard()' class="btn btn-outline-info" style="margin-right: 1%;"> 등록 <i class="fas fa-save"></i></a>
			                    <a href='#' id ="returnBtn" onClick='fn_cancel()' class="btn btn-outline-success" style="margin-right: 1%;">목록 <i class="fas fa-list"></i></a>
			                </div>
			            
			        </div>
			    
			</div>
	</div>

<script>
	//글쓰기
	function fn_addtoBoard(){
		 userid = ("${userName}");
		 BoardDataList.saveAction(
				{
					title : $("#title").val(),
					content : $("#content").val(),
					writer : $("#writer").val(),
					userid : userid
				}); 
	
	}
	 
	function fn_cancel(){
		 location.href = "/user/api/data/board.do";
		
	    
	}  
</script>