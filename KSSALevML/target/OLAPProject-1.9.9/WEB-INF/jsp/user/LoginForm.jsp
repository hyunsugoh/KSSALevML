<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<style>
.form-control {
	width: 85%;
}

.btn-block {
	width: 85%;
}

.login-alert-idpw {
	font-size: 12px;
	line-height: 16px;
	margin: -2px 0 9px;
	color: #ff1616;
	text-align: left;
	width: 85%;
}

.login-etc-a {
	font-size: 15px;
	margin-right: 3%;
}

.login-btn {
	width: 179.3px;
}
</style>

<script>
	$(document).ready(function() {
		var fail2 = "${param.fail2}";
		var fail3 = "${param.fail3}";
		if (fail2) {
			alert("허용되지 않은 IP 입니다.", function() {
				document.logoutForm.submit();
			});

		}
		if (fail3) {
			alert("관리자에게 문의해주세요.", function() {
				document.logoutForm.submit();
			});

		}

		$("#goTosignUp").on("click", function() {
			window.location.replace(OlapUrlConfig.signup);
		});

		$("#goToidFind").on("click", function() {
			window.location.replace(OlapUrlConfig.idFind);
		});

		$("#goTopwdInitialize").on("click", function() {
			window.location.replace(OlapUrlConfig.pwdInitialize);
		});

	});
</script>

<div class="container">
	<div class="col-12">
		<form class="form-horizontal mx-auto user-login-form-div"
			action="login" method="post" style="display: block;">
			<input type="hidden" name="${_csrf.parameterName}"
				value="${_csrf.token}" />
			<div class="row">
				<div class="col-12">
					<div class="form-group">
						<input type="text" name="loginid" tabindex="1"
							class="form-control mx-auto" placeholder="Username" value=""
							required="required" maxlength="12" />
					</div>
					<div class="form-group">
						<input type="password" name="loginpw" tabindex="2"
							class="form-control mx-auto" placeholder="Password"
							required="required" maxlength="12" />
					</div>
					<c:if test="${not empty param.fail}">
						<p class="login-alert-idpw mx-auto">아이디 또는 비밀번호를 다시 확인하세요.</p>
						<p class="login-alert-idpw mx-auto">등록되지 않은 아이디이거나, 아이디 또는 비밀번호를 잘못
							입력하셨습니다.</p>
					</c:if>
					<div class="form-group text-center">
						<button class="btn btn-lg btn-outline-primary login-btn"
							type="submit" name="submit">
							<i class="fa fa-sign-in"></i> Login
						</button>
					</div>
				</div>
				<div class="col-12 mt-3">
					<div class="form-group">
						<div class="text-center mx-auto" style="margin-left: 4%;">
							<!-- <a id="goTosignUp" href="#" class="login-etc-a"> 회원가입 </a> -->
							<!-- <a id="goToidFind" href="#" class="login-etc-a"> 아이디 찾기 </a> -->
							<!-- <a	id="goTopwdInitialize" href="#" class="login-etc-a"> 비밀번호 초기화 </a> -->
							
						</div>
						
					</div>
					<div class="form-group" style="margin-left: 4%;">
							<p></p>
							<p class="login-etc-a text-center text-muted" style="text-align: center; font-size: 12px;">비밀번호 분실 시 관리자에게 문의바랍니다.</p>
							</div>	
					</div>

			</div>

		</form>
		<!-- 로그아웃 히든영역 -->
		<div style="display: none;">
			<form name="logoutForm" action="<%=request.getContextPath()%>/logout"
				method="POST">
				<input type="hidden" name="${_csrf.parameterName}"
					value="${_csrf.token}" />
			</form>
		</div>
	</div>
	<div class="col-12 mt-3 pt-3 text-center">
<!-- 		<small class="text-muted">본 메뉴는 개인정보를 일체 수집하지 않는 로그인이 필요한 메뉴로 -->
<!-- 			가입시 입력한 보조아이디 정보를 필히 숙지하시기 바랍니다.<br> 비밀번호, 보조아이디는 본인만 알 수 있습니다. -->
<!-- 		</small> -->
	</div>
</div>
