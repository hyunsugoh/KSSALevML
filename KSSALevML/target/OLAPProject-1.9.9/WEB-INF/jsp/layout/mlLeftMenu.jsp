<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<nav class="col-md-3 col-lg-1 col-xl-1 col-1 bg-light sidebar" style="min-width:12%;">
	<div class="sidebar-sticky">
		<ul class="nav flex-column mt-5 admin-right-nav-text">
			<li class="nav-item">
				<a class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="Model 관리" href="<c:url value='/ml/modelMngView.do'/>">
					<i class="far fa-2x fa-object-ungroup admin-right-menu-md-icon"></i>
				</a>
				<a id="modelMng_a" class="nav-link d-none d-md-block"  href="<c:url value='/ml/modelMngView.do'/>">
					<i id="modelMng_i" class="far fa-object-ungroup"></i>  Model 관리
				</a>
			</li>
			<li class="nav-item" >
				<a class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="Model 예측"   href="<c:url value='/ml/modelPredictView.do'/>">
					<i class="far fa-2x fa-object-group admin-right-menu-md-icon"></i>
				</a>
				<a id="modelPredict_a" class="nav-link d-none d-md-block"  href="<c:url value='/ml/modelPredictView.do'/>">
					<i id="modelPredict_i" class="far fa-object-group"></i> Model 예측(X)
				</a>
			</li>
			<li class="nav-item" >
				<a class="nav-link d-block d-md-none" data-toggle="tooltip" data-placement="bottom" title="예측"   href="<c:url value='/ml/predictWithFiltering.do'/>">
					<i class="far fa-2x fa-object-group admin-right-menu-md-icon"></i>
				</a>
				<a id="predictWithFiltering_a" class="nav-link d-none d-md-block"  href="<c:url value='/ml/predictWithFilteringView.do'/>">
					<i id="modelPredict_i" class="far fa-object-group"></i>  Model 예측
				</a>
			</li>
		</ul>
	</div>
</nav>
<script>
	let viewName = "${viewName}";
	if(viewName !== undefined && viewName !== null){
		if($("#"+viewName+"_a").length >0){
			$("#"+viewName+"_a").addClass("active");
		}
		if($("#"+viewName+"_i").length >0){
			$("#"+viewName+"_i").addClass("active");
		}
	}
	
	
</script>