<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<style>
.custom-btn-color {
	color: #fff;
	background-color: #25949b;
	border-color: #fff
}

.custom-btn-color:hover {
	color: #fff;
	background-color: #004d4d;
	border-color: #fff
}

.custom-btn-color:focus {
	box-shadow: 0 0 0 .2rem rgba(0, 90, 90, 0.5)
}

.tbl_model {
	position: relative;
	width: 100%;
	table-layout: fixed;
	border-spacing: 0;
	border-collapse: collapse;
	word-wrap: break-word;
	word-break: keep-all;
	border: 0;
	border-bottom: 1px solid #e5e5e5;
	border-right: 1px solid #e5e5e5;
}

.tbl_model th {
	color: #333;
	background: #f9f9f9;
}

.tbl_model td, .tbl_model th {
	line-height: 14px;
	text-align: left;
	letter-spacing: -1px;
	border: 0;
	border-top: 1px solid #e5e5e5;
}

.thcell {
	padding: 32px;
}
</style>
<script>
	$(document).ready(function() {
		init();
		$("#input_currentPwd").focus(function() {
			$('#alertArea').css("display", "none");
		});

		$("#input_newPwd").focus(function() {
			$('#alertArea').css("display", "none");
		});

		$("#input_newPwdRe").focus(function() {
			$('#alertArea').css("display", "none");
		});

		$("#goTodashboard").on("click", function() {
			//window.location.replace(OlapUrlConfig.userDashboard);
			location.href = "/user/main.do";
		});

	});

	//인풋박스 초기화
	function init() {
		$("#input_currentPwd").val("");
		$("#input_newPwd").val("");
		$("#input_newPwdRe").val("");
		$('#alertArea').css("display", "none");
		$("#input_currentPwd_withdrawal").val("");
	}

	//글자수 제한 & 비밀번호 정규식
	function checkLen(insertnewPwd) {
		var flag = true;
		var pwd = insertnewPwd;
		var idRules = /^[a-zA-Z0-9]{4,12}$/;
		var passwordRules = /(?=.*\d{1,50})(?=.*[~`!@#$%\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{9,50}$/;

		if (!passwordRules.test(pwd)) {
			$('#alert').text("비밀번호는 숫자와 영문자, 특수문자 조합으로");
			$('#alert2').text("9자리 이상을 사용하셔야 합니다.");
			$('#alertArea').css("display", "block");
			var flag = false;
		} else {
			return flag;
		}
	}

	//비밀번호 변경
	function doChange() {
		var insertcurrPwd = $("#input_currentPwd").val().trim();
		var insertnewPwd = $("#input_newPwd").val().trim();
		var insertnewPwdRe = $("#input_newPwdRe").val().trim();

		if (insertnewPwd != insertnewPwdRe) {
			$('#alert').text('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
			$('#alert2').text('');
			$('#alertArea').css("display", "block");
		} else if (insertnewPwd == insertnewPwdRe) {
			//정규식 체크
			if (checkLen(insertnewPwd)) {
				var loadIndicator = new loading_bar2(); // 초기화
				loadIndicator.show(); // 로딩바 호출
				//변경 실행
				$.ajax({
					type : "POST",
					url : OlapUrlConfig.actionPwdChange,
					headers : {
						'X-CSRF-TOKEN' : $('#csrfvalue').val()
					}, //스프링시큐리티 CSRF
					beforeSend : function(xhr) {
						xhr.setRequestHeader("AJAX", true);
					},
					data : {
						"insertcurrPwd" : insertcurrPwd,
						"insertnewPwd" : insertnewPwd
					},
					complete : function() {
						loadIndicator.hide(); // 로딩바 종료
					}
				}).done(function(data) {
					console.log("success: actionPwdChange");

					if (data == true) {
						alert("비밀번호 변경이 완료되었습니다.", function() {
							//location.href = OlapUrlConfig.userDashboard;
							location.href = "/user/main.do";
						});
					} else if (data == false) {
						alert("현재 비밀번호가 일치하지 않습니다.");
					} else {
						console.log("fail: actionPwdChange02" + data);
						alert("관리자에게 문의해주세요.");
					}

				}).fail(function(jqXHR, textStatus, errorThrown) {
					console.log("fail:  actionPwdChange " + errorThrown);
					if (jqXHR.status === 400) {
						alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
					} else if (jqXHR.status == 401) {
						alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.", function() {
							location.href = OlapUrlConfig.loginPage;
						});

					} else if (jqXHR.status == 403) {
						alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.", function() {
							location.href = OlapUrlConfig.loginPage;
						});

					} else {
						alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
					}
				});
			}

		} else {
			console.log("fail: actionPwdChange003");
			alert("관리자에게 문의해주세요.");
		}
	}

	//회원 탈퇴
	function doWithdrawal() {

		confirm("회원 탈퇴가 진행됩니다.<br>탈퇴 하시겠습니까?", function(result) {

			if (result !== true) {
				init();
			} else if (result == true) {

				var insertcurrPwdWithdrawal = $("#input_currentPwd_withdrawal")
						.val().trim();
				var loadIndicator = new loading_bar2(); // 초기화
				loadIndicator.show(); // 로딩바 호출

				$.ajax({
					type : "POST",
					url : OlapUrlConfig.actionWithdrawal,
					headers : {
						'X-CSRF-TOKEN' : $('#csrfvalue').val()
					}, //스프링시큐리티 CSRF
					beforeSend : function(xhr) {
						xhr.setRequestHeader("AJAX", true);
					},
					data : {
						"insertcurrPwdWithdrawal" : insertcurrPwdWithdrawal
					},
					complete : function() {
						loadIndicator.hide(); // 로딩바 종료
					}
				}).done(function(data) {
					console.log("success: actionWithdrawal");

					if (data == true) {
						alert("회원 탈퇴 되었습니다.", function() {
							location.href = OlapUrlConfig.loginPage;
						});
					} else if (data == false) {
						alert("비밀번호가 일치하지 않습니다.");
					} else {
						console.log("fail: actionWithdrawal02" + data);
						alert("관리자에게 문의해주세요.");
					}

				}).fail(function(jqXHR, textStatus, errorThrown) {
					console.log("fail:  actionWithdrawal " + errorThrown);
					if (jqXHR.status === 400) {
						alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
					} else if (jqXHR.status == 401) {
						alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.", function() {
							location.href = OlapUrlConfig.loginPage;
						});

					} else if (jqXHR.status == 403) {
						alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.", function() {
							location.href = OlapUrlConfig.loginPage;
						});

					} else {
						alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
					}
				});

			} else {
				console.log("fail: actionWithdrawal03");
				alert("관리자에게 문의해주세요.");
			}
		});
	}
</script>
<div class="container">
	<table class="tbl_model mt-5 border">
		<colgroup>
			<col style="width: 8%">
			<col>
		</colgroup>
		<tbody>
			<tr>
				<th scope="row">
					<div class="thcell">비밀번호 변경</div>
				</th>
				<td class="thcell" style="width: 50%">

					<div class="col-md-8">
						<form class="form-horizontal">
							<div class="form-group row">
								<label class="col-sm-5 col-md-4 control-label text-right p-1">현재
									비밀번호</label>
								<div class="col-sm-7 col-md-8">
									<input type="password" class="form-control"
										id="input_currentPwd">
								</div>
							</div>
							<div class="form-group row">
								<label class="col-sm-5 col-md-4 control-label text-right p-1">새
									비밀번호</label>
								<div class="col-sm-7 col-md-8">
									<input type="password" class="form-control" id="input_newPwd"
										maxlength="12">
								</div>
							</div>
							<div class="form-group row">
								<label class="col-sm-5 col-md-4  control-label text-right p-1">새
									비밀번호 확인</label>
								<div class="col-sm-7 col-md-8">
									<input type="password" class="form-control" id="input_newPwdRe"
										maxlength="12">
								</div>
							</div>
						</form>
						<!-- 초기화 버튼 -->
						<div class="row justify-content-sm-center" id="changeArea"
							align="center">
							<div class="col-4">
								<button onclick="doChange()" type="button"
									class="btn btn-md btn-outline-primary btn-block">비밀번호
									변경</button>
								<button id="goTodashboard" type="button"
									class="btn btn-md btn-outline-primary btn-block">이전화면</button>
							</div>
						</div>
						<!-- 알림 메시지 영역 -->
						<div class="row p-2" id="alertArea" style="display: none;"
							align="center">
							<span style="color: red;" id="alert"></span><br> <span
								style="color: red;" id="alert2"></span>
						</div>
					</div>

				</td>
			</tr>
			<tr>
				<th scope="row">
					<div class="thcell">
						<label>회원 탈퇴</label>
					</div>
				</th>
				<td class="thcell">
					<div class="col-md-8">
						<form class="form-horizontal">
							<div class="form-group row">
								<label class="col-sm-5 col-md-4 control-label text-right p-1">비밀번호
								</label>
								<div class="col-sm-7 col-md-8">
									<input type="password" class="form-control"
										id="input_currentPwd_withdrawal">
								</div>
							</div>


						</form>
						<!-- 탈퇴 버튼 -->
						<div class="row justify-content-sm-center" id="" align="center">
							<div class="col-4">
								<button onclick="doWithdrawal()" type="button"
									class="btn btn-md btn-outline-primary btn-block">회원 탈퇴</button>

							</div>
						</div>

					</div>
			</tr>
		</tbody>
	</table>
</div>
