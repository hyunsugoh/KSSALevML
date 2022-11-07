<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<!DOCTYPE html>
<html  lang="ko">
	<head>
		<script>
			var _userid = "${userName}";
		</script>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
		<meta http-equiv="X-UA-Compatible" content="IE=edge" />
		
		<tiles:insertAttribute name="header" />
		<title>LEVML</title>
	</head>
	<body>
		<tiles:insertAttribute name="body" />	
	</body>
</html>