<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script>
	$(document).ready(
			function() {
				HelpMsgAction.creathHelpBtn("adminSignUp", "helpIcon",
						OLAPAdminHelpMsg);
			});

	//아이디 중복 체크
	function doSearch() {

		var flag = true;
		var insertId = $("#input_managerId").val().trim();
		var idRules = /^[a-zA-Z0-9]{4,12}$/;
		if (insertId == "") {
			$(".chkId-alert").css('display', 'block');
			return;
		} else if (!idRules.test(insertId)) {
			alert("아이디는 영문 또는 숫자로 4자리 이상을 사용하셔야 합니다.");
			return;
		} else if (insertId != "") {

			$.ajax({
				type : "GET",
				url : OlapUrlConfig.chkManagerId,
				beforeSend : function(xhr) {
					xhr.setRequestHeader("AJAX", true);
				},
				data : {
					"insertId" : insertId
				}
			}).done(function(data) {
				if (data.length == 0) {
					alert("사용 가능한 아이디 입니다.");
					document.signUpForm.idDuplication.value = "idCheck";
				} else {
					$(".chkId-alert3").css('display', 'block');
					$("#input_managerId").val("");
					var flag = false;
				}
			}).fail(function(jqXHR, textStatus, errorThrown) {
				console.log("fail: chkId " + errorThrown);
				if (jqxXHR.status === 400) {
					alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
				} else if (jqxXHR.status == 401) {
					alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.", function() {
						location.href = OlapUrlConfig.loginPage;
					});

				} else if (jqxXHR.status == 403) {
					alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.", function() {
						location.href = OlapUrlConfig.loginPage;
					});

				} else if (jqXHR.status == 500) {
					errAlert(jqXHR.status, jqXHR.responseText)
				} else {
					alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
				}
			});
		}
		return flag;
	}

	//필수값 체크
	function checkRequiedValue() {
		var flag = true;
		var requireds = $("#signUpForm").find(".form-control");
		for (var i = 0; i < requireds.length; i++) {
			var signId = requireds[i].id;
			var signTxt = $('#' + signId).val();
			if (signTxt == "" || signTxt == null) {
				if (i == 0) {
					$(".chkId-alert").css('display', 'block');
				} else if (i == 1) {
					$(".chkPw-alert").css('display', 'block');
				} else if (i == 2) {
					$(".chkPwDf-alert").css('display', 'block');
				} else if (i == 3) {
					$(".chkIp-alert").css('display', 'block');
				}
				flag = false;
			}
		}
		return flag;
	}

	//비밀번호 확인 
	function chkPwValue() {
		var flag = true;
		var pw = document.getElementById("input_Pw").value;
		var pwck = document.getElementById("input_chkPw").value;

		if (pw != pwck) {
			alert('비밀번호가 일치하지 않습니다.');
			flag = false;
		}

		return flag;
	}

	// 글자수 제한 & 비밀번호 정규식& ip 정규식
	function checkLen() {
		var flag = true;
		var userid = $("#input_managerId").val().trim();
		var pwd = $("#input_Pw").val().trim();
// 		var ip = $("#input_Ip").val().trim();
		var idRules = /^[a-zA-Z0-9]{4,12}$/;
		var passwordRules = /(?=.*\d{1,50})(?=.*[~`!@#$%\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{9,50}$/;
		var ipRules = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/;
		if (!idRules.test(userid)) {
			$(".chkId-alert2").css('display', 'block');
			var flag = false;
		} else if (!passwordRules.test(pwd)) {
			$(".chkPw-alert2").css('display', 'block');
			var flag = false;
// 		} else if (!ipRules.test(ip)) {
// 			$(".chkIp-alert2").css('display', 'block');
// 			var flag = false;
		} else {
			return flag;
		}
	}

	// 아이디 입력했을 때
	function inputIdChk() {
		$(".chkId-alert").css('display', 'none');
		$(".chkId-alert2").css('display', 'none');
		$(".chkId-alert3").css('display', 'none');
		document.signUpForm.idDuplication.value = "idUncheck";
	}

	//비밀번호 입력했을 때
	function inputPwChk() {
		$(".chkPw-alert").css('display', 'none');
		$(".chkPw-alert2").css('display', 'none');
	}

	//비밀번호 확인 입력했을 때
	function inputPwReChk() {
		$(".chkPwDf-alert").css('display', 'none');
	}

	//아이피 입력했을 때
	function inputIpChk() {
		$(".chkIp-alert").css('display', 'none');
		$(".chkIp-alert2").css('display', 'none');

	}

	//회원가입
	function signUp() {
		//필수값 체크
		if (checkRequiedValue()) {
			//아이디 비밀번호 정규식
			if (checkLen()) {
				//아이디 중복체크
				if (document.signUpForm.idDuplication.value == "idCheck") {

					//비밀번호 확인
					if (chkPwValue()) {
						//회원가입 데이터 인서트
						var param = {
							"MANAGER_ID" : $("#input_managerId").val().trim(),
							"MANAGER_PASSWORD" : $("#input_Pw").val().trim(),
// 							"IP" : $("#input_Ip").val().trim()

						};

						$
								.ajax(
										{
											type : "post",
											url : OlapUrlConfig.signUpManager,
											contentType : 'application/json',
											headers : {
												'X-CSRF-TOKEN' : $('#csrfvalue')
														.val()
											},
											beforeSend : function(xhr) {
												xhr.setRequestHeader("AJAX",
														true);
											},
											data : JSON.stringify(param),

											success : function(data) {

												if (data == 1) {

													alert(
															"관리자 가입이 완료되었습니다.",
															function() {
																location
																		.assign(OlapUrlConfig.Manager);
																//history.back();
															});

												} else {
													alert("관리자 가입이 실패하였습니다");
												}

											}
										})
								.fail(
										function(jqXHR, textStatus, errorThrown) {
											console.log("fail:  signUpInsert "
													+ errorThrown);
											if (jqxXHR.status === 400) {
												alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
											} else if (jqxXHR.status == 401) {
												alert(
														"인증에 실패 했습니다. 로그인 페이지로 이동합니다.",
														function() {
															location.href = OlapUrlConfig.loginPage;
														});

											} else if (jqxXHR.status == 403) {
												alert(
														"세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",
														function() {
															location.href = OlapUrlConfig.loginPage;
														});

											} else if (jqXHR.status == 500) {
												errAlert(jqXHR.status,
														jqXHR.responseText)
											} else {
												alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
											}
										});
					}
				} else {
					alert("아이디 중복체크를 해주세요");
				}
			}
		}
	}
</script>
</head>

<div class="col-md-9 col-lg-10 col-xl-10 col-10 ml-auto mr-auto px-4">
	<div class="row">
		<div class="col-12 pt-3">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="fas fa-user-plus"></i> <span
							class="ml-1">관리자 가입</span></strong>
					</div>
				</div>
				<div class="col-6">
					<div class="d-flex justify-content-end">
						<div id="helpIcon" class="pt-0"></div>
					</div>
				</div>
			</div>
		</div>
		<div class="container">
			<div class="col-12">
				<form class="form-horizontal mx-auto user-login-form-div"
					id="signUpForm" name="signUpForm">
					<div class="form-group row">
						<label class="col-sm-3 control-label text-right p-1">아이디</label>
						<div class="col-sm-6">
							<input type="text" class="form-control" id="input_managerId"
								onkeydown="inputIdChk()" maxlength="12"> <input
								type="hidden" name="idDuplication" value="idUncheck" />
							<p class="chkId-alert"
								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">아이디를
								입력해 주세요.</p>
							<p class="chkId-alert2"
								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">아이디는
								영문 또는 숫자로 4자리 이상을 사용하셔야 합니다.</p>
							<p class="chkId-alert3"
								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">이미
								존재하는 아이디 입니다.</p>
						</div>
						<div class="col-sm-3 p-1">
							<button onclick="doSearch()" type="button"
								class="btn btn-sm btn-outline-primary">중복 확인</button>
						</div>
					</div>
					<div class="form-group row">
						<label class="col-sm-3 control-label text-right p-1">비밀번호</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="input_Pw"
								onkeydown="inputPwChk()" maxlength="12">
							<p class="chkPw-alert"
								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">비밀번호를
								입력해 주세요.</p>
							<p class="chkPw-alert2"
								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">비밀번호는
								숫자와 영문자 특수문자 조합으로 9자리 이상을 사용하셔야 합니다.</p>
						</div>
					</div>
					<div class="form-group row">
						<label class="col-sm-3 control-label text-right p-1">비밀번호
							확인</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="input_chkPw"
								onkeydown="inputPwReChk()" maxlength="12">
							<p class="chkPwDf-alert"
								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">비밀번호를
								확인해 주세요.</p>
						</div>
					</div>
<!-- 					<div class="form-group row"> -->
<!-- 						<label class="col-sm-3 control-label text-right p-1">접속 허용 -->
<!-- 							IP</label> -->
<!-- 						<div class="col-sm-6"> -->
<!-- 							<input type="text" class="form-control" id="input_Ip" -->
<!-- 								onkeydown="inputIpChk()" maxlength="15"> -->
<!-- 							<p class="chkIp-alert" -->
<!-- 								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">ip를 -->
<!-- 								입력해 주세요.</p> -->
<!-- 							<p class="chkIp-alert2" -->
<!-- 								style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display: none;">ip -->
<!-- 								입력범위는 0.0.0.0 에서 255.255.255.255까지 입니다</p> -->
<!-- 						</div> -->

<!-- 					</div> -->

					<div class="form-group row" style="margin-top: 10%;">
						<div class="col-sm-3"></div>
						<div class="col-sm-6 control-label text-center p-1">
							<button class="btn btn-lg btn-outline-primary btn-block"
								onclick="signUp()" type="button">
								<i class="fa fa-sign-in"></i> 관리자 가입
							</button>
						</div>
						<div class="col-sm-3"></div>
					</div>
					<!--토큰 -->
					<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}'
						value='${_csrf.token}' />
				</form>
			</div>
		</div>

	</div>

</div>
