<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<html  lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<tiles:insertAttribute name="header" />
<title>KSM KSSA</title>
<link rel="stylesheet" href="<c:url value='/css/admin/admin-common.css'/>">
<script src="<c:url value='/js/admin/admin-menu.js'/>"></script>
<script src="<c:url value='/js/common/olapHelpMessage/messages/adminMsg.js'/>"></script>
<script src="<c:url value='/js/common/olapHelpMessage/OLAPHelpMessage.js'/>"></script>
</head>
<tiles:insertAttribute name="nav" />
<div class="head-hide-area"></div>
	<div class="container-fluid ">
		<div class="row">
			<tiles:insertAttribute name="body" />			
		</div>
</div>
<tiles:insertAttribute name="footer" />
</html>