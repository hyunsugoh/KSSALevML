<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<style>
.jsgrid-grid-body {overflow:hidden;}
.jsgrid-grid-header {overflow:hidden;}
.scrollbar {overflow: hidden;}

@media (max-width:780px) {
  #main_logo{
  	display: none;
 }
}
 
</style>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<script src="<c:url value='/js/user/board.js'/>"></script>
<script src="<c:url value='/js/common/olap-baseConfig.js'/>"></script>
<script>
$(document).ready(function() {
	BoardDataList.loadAction("BoardList");
	$('body').css("overflow-y","hidden");
});
</script>
<style>
	.jsgrid-header-row,.jsgrid-alt-row {font-size:14px;}
</style>
<div class="container-fluid olap-background-img" style="height:830px; ">

	<div class="row">
		<div class="col-12">
			<div class="row justify-content-md-end">
				<div class="m-3" id="main_logo">
					<div class="d-flex justify-content-start">
						<img src="<c:url value='/images/common/ci/kssa_icon.png'/>" alt="ksm" class="my-0 img-fluid head-ci-img" >
					</div>
					<div class="col-12 h4">
						<strong>KSM Seal Selection Assistance</strong>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="row mt-lg-9 d-lg-block d-none" style="height:2%;" ></div>
	
	<div class="row justify-content-end">
		<div class="col-lg-8 col-xl-7 align-self-end">
			 <div class ="row p-2">
				<div class="col-12 d-flex justify-content-between align-items-center">
					<div class="h5">
						<strong><i class="far fa-object-ungroup h5"></i><span class="ml-1">게시판</span></strong>
				    </div>
				    <div><a href="/user/api/data/board.do" class="btn stretched-link" style="font-size: 15;" id="writeForm">더보기</a></div>
			    </div>     
				<div class="col-12" style="height: 350px;">
			   		<div id="mainBoard" class="jsgrid">
					</div>
			    </div>
			</div> 
		</div>
	
	</div>
	

</div>
	
