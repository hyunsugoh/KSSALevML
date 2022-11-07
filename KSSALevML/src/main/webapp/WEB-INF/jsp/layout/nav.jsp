<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<style type="text/css">
.custom-menu-form {padding:0px; margin:0px;}
.custom-menu-form-btn {font-size:12px !important;}
.custom-menu-font {font-size:12px !important;}
.custom-menu-form-btn:hover{color:#007bff;}
.font-size{font-size: 2vw;}

@media (max-width:1161px) {
  #nav_logo{display: none;}
}
@media (min-width:1160px) {
  #nav_small_logo{display: none;}
}
@media (max-width:700px) {
   #menu_1_lbl{display: none;}
   #menu_2_lbl{display: none;}
   #menu_3_lbl{display: none;}
   #menu_4_lbl{display: none;}
   #menu_5_lbl{display: none;}
   #menu_6_lbl{display: none;}	 
 }
 
 
</style>
<div class="fixed-top admin-brand-header px-md-4 mb-3 bg-white border-bottom shadow-sm">

	<c:if test="${authState == 'Logined' }">

	<div class="d-flex justify-content-center justify-content-md-end">
		<div class="my-0 mr-md-auto text-truncate" id="nav_small_logo" >
				<c:choose>
					  <c:when test="${authState == 'Logined' }">
					  <a class="navbar-brand" href="/user/main.do">
					  <img src="<c:url value='/images/common/ci/kssa_icon.png'/>" alt="ksm" class="my-0 mr-md-auto img-fluid head-ci-img" style="height: 20;width:80%;height:80%"></a>
					  </c:when>
					  <c:otherwise>
					  	<a class="navbar-brand" href="<c:url value='logon.do'/>">
					  	<img src="<c:url value='/images/common/ci/kssa_icon.png'/>" alt="ksm" class="my-0 mr-md-auto img-fluid head-ci-img"  style="height: 20;width:80%;height:80%"></a>
					  </c:otherwise>
				</c:choose>
				<strong class="font-size" style="font-size: 12pt;">KSM Seal Selection Assistance</strong>
		</div>
		<span class="mr-1 ml-3" style="font-size:0.6em;color: Tomato;padding-top: 4px;">
			<i class="far fa-user-circle fa-2x"></i>
		</span>
		<span class="align-self-center pr-3 custom-menu-font custom-menu-form text-truncate">${userName} 님</span>
		<c:if test="${role == '[ROLE_USER]'}">
			<form class="custom-menu-form" action="<%=request.getContextPath()%>/user/pwdChange.do">
				<button type="submit" class="btn stretched-link custom-menu-form-btn text-truncate">회원정보 수정</button>
			</form>
		</c:if>
		<c:if test="${role == '[ROLE_ADMIN]'|| role == '[ROLE_SUPER]'}">
		<form class="custom-menu-form" action="<c:url value='/admin/adminpwdchange.do'/>"  >
			<button type="submit" class="btn stretched-link custom-menu-form-btn text-truncate">비밀번호 변경</button>
		</form>
		</c:if>
		<form class="custom-menu-form" action="<%=request.getContextPath()%>/logout"
				method="POST">
				<button type="submit"
					class="btn stretched-link custom-menu-form-btn text-truncate">로그아웃</button>
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
		</form>
	</div>
	</c:if>	

	<div class="d-flex flex-column flex-lg-row align-items-center" >
		<div class="my-0 mr-md-auto" id="nav_logo" >
				<c:choose>
					  <c:when test="${authState == 'Logined' }">
					  <a class="navbar-brand" href="/user/main.do">
					  <img src="<c:url value='/images/common/ci/kssa_system_logo.png'/>" alt="ksm" class="my-0 mr-md-auto img-fluid head-ci-img" ></a>
					  </c:when>
					  <c:otherwise>
					  	<a class="navbar-brand" href="<c:url value='logon.do'/>">
					  	<img src="<c:url value='/images/common/ci/kssa_system_logo.png'/>" alt="ksm" class="my-0 mr-md-auto img-fluid head-ci-img" ></a>
					  </c:otherwise>
				</c:choose>
		</div>
		<c:if test="${authState == 'Logined'}">
		 <ul class="nav">
		 	<li class="nav-item">
	            <a class="nav-link people-nav-item" href="/user/main.do" title="메인화면"><i class="fas fa-home"></i> <span id="menu_1_lbl">메인화면</span></a>
	        </li>
	        <li class="nav-item">
	            <a class="nav-link people-nav-item"  href="/user/dashboard.do" title="데이터조회"><i class="fas fa-chart-line"></i><span id="menu_2_lbl"> 데이터 조회</span></a>
	        </li>
	        <ul class="nav navbar-nav">
		        <li class="nav-item dropdown">
		            <!-- <a id="mlCalcMenu" class="nav-link people-nav-item"  href="/ml/modelMngView.do"><i class="fas fa-brain"></i> 머신러닝 분석</a> -->
		            <a class="nav-link dropdown-toggle" href="#" id="mlCalcMenu" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" title="머신러닝분석">
		            	<i class="fas fa-brain"></i> <span id="menu_3_lbl">머신러닝 분석</span></a>
		            <div class="dropdown-menu" aria-labelledby="mlCalcMenu">
				        <a class="dropdown-item" href="<c:url value='/ml/predictWithMultiView.do'/>"><i class="far fa-object-group"></i> Seal 추천</a>
				        <c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">
				        <a class="dropdown-item" href="<c:url value='/project/ProjectMng.do'/>"><i class="far fa-object-group"></i> Model 관리</a>
	                	<!--  <a class="dropdown-item" href="<c:url value='/mlTest/viewModelTest.do'/>"><i class="far fa-object-group"></i> Model 테스트</a>  -->
	                	</c:if>
		            </div>
		        </li>
	        </ul>
	        <li class="nav-item">
	            <a class="nav-link people-nav-item" href="/library/FileData.do" title="자료실"><i class="fas fa-file-alt"></i> <span id="menu_4_lbl">자료실</span></a>
	        </li>
	        <li class="nav-item">
	            <a class="nav-link people-nav-item" href="/user/api/data/board.do" title="게시판"><i class="fas fa-flag"></i> <span id="menu_5_lbl">게시판</span></a>
	        </li>
	        <c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">
	        <ul class="nav navbar-nav">
		        <li class="nav-item dropdown">
		            <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" title="관리자기능"><i class="fas fa-user-cog"></i> <span id="menu_6_lbl">관리자 기능</span></a>
				 	<div class="dropdown-menu" aria-labelledby="navbarDropdown">
			          <a class="dropdown-item" href="<c:url value='/admin/adminobject.do'/>"><i class="far fa-object-ungroup"></i>객체관리</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/batchData.do'/>"><i class="fas fa-list-ul"></i>배치프로그램 관리</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/ml/modelMngView.do'/>"><i class="far fa-object-group"></i> ML Model관리</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/adminMLGroupInfo.do'/>"><i class="far fa-object-group"></i>ML 그룹 정보</a>
			          <div class="dropdown-divider"></div>
<%-- 			          <a class="dropdown-item" href="<c:url value='/admin/commonCode.do'/>"><i class="far fa-object-group"></i>Rule-Based 기준</a> --%>
<!-- 			          <div class="dropdown-divider"></div> -->
			          <a class="dropdown-item" href="<c:url value='/rule/commonCode.do'/>"><i class="far fa-object-group"></i>Rule-Based 기준</a>
			          <div class="dropdown-divider"></div>
<%-- 			          <a class="dropdown-item" href="<c:url value='/admin/ruleStd.do'/>"><i class="far fa-object-group"></i>Rule-Based 표준</a> --%>
<!-- 			          <div class="dropdown-divider"></div> -->
<!-- 			          <a class="dropdown-item" href="<c:url value='/rule/ruleStd.do'/>"><i class="far fa-object-group"></i>Rule-Based 표준</a>
			          <div class="dropdown-divider"></div> -->
			          <a class="dropdown-item" href="<c:url value='/admin/ruleGraph.do'/>"><i class="far fa-object-group"></i>Graph Data</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/priceInfo.do'/>"><i class="fas fa-dollar-sign"></i>가격 정보</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/infoCriteria.do'/>"><i class="fas fa-code-branch"></i>조회조건 관리</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/codeManagement.do'/>"><i class="fas fa-laptop-code"></i>조회조건코드관리</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/userList.do'/>"><i class="fas fa-users"></i>사용자관리</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/Manager.do'/>"><i class="fas fa-users-cog"></i>관리자관리</a>
			          <div class="dropdown-divider"></div>
			          <a class="dropdown-item" href="<c:url value='/admin/manageDeploy.do'/>"><i class="fas fa-cog"></i>모델 배포 관리</a>
			          </div>
		        </li>
		 	</ul>
	        </c:if>
	     </ul>
		 </c:if>
	</div>
</div>
