<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<html  lang="ko">
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
		<meta http-equiv="X-UA-Compatible" content="IE=edge" />

		<!-- 추가된 소스 -->
		<script type="text/javascript">			
			var SBpath = "../../";
		</script>
		
		<script src="<c:url value='/SBGrid/SBGrid_Lib.js'/>"></script>
		<script src="<c:url value='/SBGrid/SBGrid_min.js'/>"></script>
		<link rel="stylesheet" type="text/css" href="<c:url value='/SBGrid/css/SBGrid_Default.css'/>" />
		<link rel="stylesheet" type="text/css" href="<c:url value='/SBGrid/css/SBGrid.css'/>" />
	
		<script src="<c:url value='/js/user/olapDataView.js'/>"></script>
		<script src="<c:url value='/js/user/savedDashboard.js'/>"></script>
		<script src="<c:url value='/js/user/board.js'/>"></script>
		
		<script src="<c:url value='/js/joint/lodash.js'/>"></script>
		<script src="<c:url value='/js/joint/backbone.js'/>"></script>
		<script src="<c:url value='/js/joint/joint.js'/>"></script>
		
		<script src="<c:url value='/js/common/jquery/jquery-3.3.1.min.js'/>"></script>
		<script src="<c:url value='/js/common/jquery/jquery-ui.min.js'/>"></script>
		<script src="<c:url value='/js/common/bootstrap/bootstrap.bundle.js'/>"></script>
		<script src="<c:url value='/js/common/jsgrid/jsgrid.js'/>"></script>
		<script src="<c:url value='/js/common/datepicker-master/datepicker.min.js'/>"></script>
		<script	src="<c:url value='/js/common/datepicker-master/i18n/datepicker.ko-KR.js'/>"></script>
		<!-- excel -->
		<script	src="<c:url value='/js/common/jsXlsx/xlsx.full.min.js'/>"></script>
		<script	src="<c:url value='/js/common/fileSaver/FileSaver.js'/>"></script>
		<script	src="<c:url value='/js/common/jsonDataExcel/jhxlsx.js'/>"></script>
		<!-- bootbox -->
		<script	src="<c:url value='/js/common/bootbox/bootbox.all.min.js'/>"></script>
		<script src="<c:url value='/js/common/olap-common.js'/>"></script>
		<script src="<c:url value='/js/common/ml-common.js'/>"></script>
		<script src="<c:url value='/js/common/olap-baseConfig.js'/>"></script>
		
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/joint/joint.css'/>" />
		
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/ml-common.css'/>" />
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/olap-common.css'/>" />
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/project-common.css'/>" />
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/bootstrap/bootstrap.min.css'/>">
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/awesome/css/all.css'/>">
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/datepicker-master/datepicker.min.css'/>">
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/jsgrid/jsgrid.min.css'/>">
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/jsgrid/jsgrid-theme.min.css'/>">
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/jquery-ui/jquery-ui.min.css'/>">
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/common/jquery-ui/theme.css'/>">
		
		<link rel="stylesheet" type="text/css" href="<c:url value='/css/user/dashboard.css'/>">
		<link rel="shortcut icon" href="<c:url value='/images/common/ci/le_favi.ico'/>">
		<!-- 추가된 소스 -->

		<tiles:insertAttribute name="header" />
		<title>KSM OLAP</title>
	</head>

	<body>
		<tiles:insertAttribute name="nav" />
		<div class="head-hide-area"></div>
		<tiles:insertAttribute name="body" />	
		<tiles:insertAttribute name="footer" />
	</body>

	<script>
		var _userid = "${userName}";
	</script>
</html>