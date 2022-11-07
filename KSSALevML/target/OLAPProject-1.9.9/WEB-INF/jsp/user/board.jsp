<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<style>
.jsgrid-grid-body {overflow:hidden;}
.jsgrid-grid-header {overflow:hidden;}
</style>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<script src="<c:url value='/js/user/board.js'/>"></script>
<script src="<c:url value='/js/common/olap-baseConfig.js'/>"></script>
<script>
$(document).ready(function() {
	
	BoardDataList.loadAction("BoardList");
});
</script>

	<div class="container-fluid">
		<div class ="row">	
			
				<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">
					<div class="row">
					<div class="col-6">	    
				    	<div class="h5 pt-3">
							<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">게시판</span></strong>
						</div>
					</div>	
					</div>
						<div class="col-12 pt-5">
							<div id="jsGrid"></div>
						</div>
						
						<div class="row">
							<div class="col-12">
								<div id="externalPager" class="p-2" style="text-align: center;"></div>			
						        <div class="col-12 d-flex justify-content-end">
							        <c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">            
							        <a href="/user/api/data/boardwriteForm.do" class="btn btn-outline-info" id="writeForm"> 글쓰기 <i class="fas fa-edit"></i></a>
							        </c:if>
						        </div>
							</div> 
						</div>
					
				</div>
			
		</div>
	</div>

<script>

//글조회
function fn_view(code){
    
    var form = document.getElementById("boardForm");
    var url = "<c:url value='/board/viewContent.do'/>";
    
    url = url + "?code=" + code;
    
    form.action = url;    
    form.submit(); 
}
</script>