<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<nav class="col-md-3 col-lg-2 col-xl-2 col-2 bg-light sidebar">
	<div class="sidebar-sticky">
		<ul class="nav flex-column mt-5 admin-right-nav-text">
			<li class="nav-item">
				<a id="adminobject_menu_btn_m"class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="객체 관리" href="<c:url value='/admin/adminobject.do'/>"> 
					<i class="far fa-2x fa-object-ungroup admin-right-menu-md-icon"></i>
				</a> 
				<a id="adminobject_menu_btn_pc" class="nav-link d-none d-md-block" href="<c:url value='/admin/adminobject.do'/>"> 
					<i class="far fa-object-ungroup"></i> 객체 관리
				</a>
			</li>
<!-- 			<li class="nav-item"> -->
<%-- 				<a id="adminobjectrel_menu_btn_m" class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="객체 관계 관리" href="<c:url value='/admin/adminobjectrel.do'/>">  --%>
<!-- 					<i class="far fa-2x fa-object-group admin-right-menu-md-icon"></i> -->
<!-- 				</a>  -->
<%-- 				<a id="adminobjectrel_menu_btn_pc" class="nav-link d-none d-md-block" href="<c:url value='/admin/adminobjectrel.do'/>">  --%>
<!-- 					<i class="far fa-object-group"></i> 객체관계 관리 -->
<!-- 				</a> -->
<!-- 			</li> -->
			<li class="nav-item">
				<a id="infoCriteria_menu_btn_m" class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="조회 조건 관리" href="<c:url value='/admin/infoCriteria.do'/>"> 
					<i class="fas fa-2x fa-code-branch admin-right-menu-md-icon"></i>
				</a> 
				<a id="infoCriteria_menu_btn_pc" class="nav-link d-none d-md-block" href="<c:url value='/admin/infoCriteria.do'/>"> 
					<i class="fas fa-code-branch"></i> 조회 조건 관리
				</a>
			</li>
			<li class="nav-item">
				<a id="codeManagement_menu_btn_m" class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="조회 조건 코드 관리" href="<c:url value='/admin/codeManagement.do'/>"> 
					<i class="fas fa-2x fa-laptop-code admin-right-menu-md-icon"></i>
				</a> 
				<a id="codeManagement_menu_btn_pc" class="nav-link d-none d-md-block" href="<c:url value='/admin/codeManagement.do'/>"> 
					<i class="fas fa-laptop-code"></i> 조회 조건 코드 관리
				</a>
			</li>
			<li class="nav-item">
				<a id="userList_menu_btn_m" class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="회원목록 조회" href="<c:url value='/admin/userList.do'/>"> 
					<i class="fas fa-2x fa-users admin-right-menu-md-icon"></i>
				</a> 
				<a id="userList_menu_btn_pc" class="nav-link d-none d-md-block" href="<c:url value='/admin/userList.do'/>"> 
					<i class="fas fa-users"></i> 회원관리
				</a>
			</li>
			<li class="nav-item">
				<a id="manager_menu_btn_m" class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="관리자 목록 조회" href="<c:url value='/admin/Manager.do'/>"> 
					<i class="fas fa-2x fa-users-cog admin-right-menu-md-icon"></i>
				</a> 
				<a id="manager_menu_btn_pc" class="nav-link d-none d-md-block" href="<c:url value='/admin/Manager.do'/>"> 
					<i class="fas fa-users-cog"></i> 관리자 관리
				</a>
			</li>
		</ul>
	</div>
</nav>
<script>
	let viewName = "${viewName}";
	if(viewName !== undefined && viewName !== null){
		if($("#"+viewName+"_menu_btn_pc").length >0){
			$("#"+viewName+"_menu_btn_pc").addClass("active");
		}
		if($("#"+viewName+"_menu_btn_m").length >0){
			$("#"+viewName+"_menu_btn_pc").addClass("active");
		}
		
	}
</script>