<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<!DOCTYPE html>
<html  lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<tiles:insertAttribute name="header" />
<title>KSM KSSA</title>
</head>
<body>
<tiles:insertAttribute name="nav" />
<div class="head-hide-area"></div>
<tiles:insertAttribute name="body" />	
<tiles:insertAttribute name="footer" />
</body>
</html>