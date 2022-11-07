<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<script>
$(document).ready(function(){
	HelpMsgAction.creathHelpBtn("adminPwdChange", "helpIcon",OLAPAdminHelpMsg);
	init();
	$("#input_currentPwd").focus(function(){
		$('#alertArea').css("display","none");
	});
	
	$("#input_newPwd").focus(function(){
		$('#alertArea').css("display","none");
	});
	
	$("#input_newPwdRe").focus(function(){
		$('#alertArea').css("display","none");
	});
});

//인풋박스 초기화
function init(){
	$("#input_currentPwd").val("");
	$("#input_newPwd").val("");
	$("#input_newPwdRe").val("");
	$('#alertArea').css("display","none");
}

//글자수 제한 & 비밀번호 정규식
function checkLen(insertnewPwd) {
	var flag = true;
	var pwd = insertnewPwd;
	var idRules = /^[a-zA-Z0-9]{4,12}$/;
	var passwordRules = /(?=.*\d{1,50})(?=.*[~`!@#$%\^&*()-+=]{1,50})(?=.*[a-zA-Z]{2,50}).{9,50}$/;
	
	if(!passwordRules.test(pwd)) {
		$('#alert').text("비밀번호는 숫자와 영문자, 특수문자 조합으로");
		$('#alert2').text("9자리 이상을 사용하셔야 합니다.");
 		$('#alertArea').css("display","block");
		var flag = false;
	}else{
		return flag;
	}	
}

//비밀번호 변경
function doChange(){
	var insertcurrPwd = $("#input_currentPwd").val().trim(); 
	var insertnewPwd = $("#input_newPwd").val().trim(); 
	var insertnewPwdRe = $("#input_newPwdRe").val().trim(); 
	
	if(insertnewPwd!=insertnewPwdRe){
		$('#alert').text('새 비밀번호와 비밀번호 확인이 일치하지 않습니다.');
		$('#alert2').text('');
 		$('#alertArea').css("display","block");
	}else if(insertnewPwd==insertnewPwdRe){
		//정규식 체크
		if(checkLen(insertnewPwd)){
			
			//변경 실행
			$.ajax({
				type:"POST",
				url:OlapUrlConfig.ActionAdminPwdChange,
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 CSRF
				beforeSend: function(xhr) {
			        xhr.setRequestHeader("AJAX", true);
			     },
				data: {
					"insertcurrPwd":insertcurrPwd,
					"insertnewPwd":insertnewPwd
				}
			}).done(function(data){
				console.log("success: actionPwdChange");
				
				if(data==true){
					alert("비밀번호 변경이 완료되었습니다.",function(){
						location.href=OlapUrlConfig.adminobject;
					});
				}else if(data==false){
					alert("현재 비밀번호가 일치하지 않습니다.");
				}else{
					console.log("fail: actionPwdChange02" +data);
					alert("비밀번호 변경이 실패하였습니다.");
				}
				
			}).fail(function(jqXHR, textStatus, errorThrown){
				console.log("fail:  actionPwdChange " + errorThrown);
				if(jqXHR.status === 400){
					alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
				}else if (jqXHR.status == 401) {
		            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
		            	location.href = OlapUrlConfig.loginPage;
		            });
		             
		             
		         } else if (jqXHR.status == 403) {
		            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
		            	ocation.href = OlapUrlConfig.loginPage;
		            });
		              
		         }else if (jqXHR.status == 500) {
		        	 errAlert(jqXHR.status, jqXHR.responseText)
		         }else{
					alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
				}
			});
		}
		
	}else{
		console.log("fail: actionPwdChange003");
 		alert("비밀번호 변경이 실패하였습니다.");
 	}
}


</script>


<div class="col-md-9 col-lg-10 col-xl-10 col-10 ml-auto mr-auto px-4">
	<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}'
		value='${_csrf.token}' />
	<div class="row">
		<div class="col-12 pt-3">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="fas fa-key"></i> <span class="ml-1">비밀번호
								변경</span></strong>
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
				<form class="form-horizontal mx-auto user-login-form-div">
					<div class="form-group row">
						<label class="col-sm-3 control-label text-right p-1">현재
							비밀번호</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="input_currentPwd">
						</div>
					</div>
					<div class="form-group row">
						<label class="col-sm-3 control-label text-right p-1">새
							비밀번호</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="input_newPwd"
								maxlength="12">
						</div>
					</div>
					<div class="form-group row">
						<label class="col-sm-3 control-label text-right p-1">새
							비밀번호 확인</label>
						<div class="col-sm-6">
							<input type="password" class="form-control" id="input_newPwdRe"
								maxlength="12">
						</div>
					</div>
					<!-- 비번 변경 버튼 -->
					<div class="form-group row" style="margin-top: 5%;" id="changeArea">
						<div class="col-sm-4"></div>
						<div class="col-sm-4 control-label text-center p-1">
							<button onclick="doChange()" type="button"
								class="btn btn-lg btn-outline-primary btn-block">비밀번호
								변경</button>

						</div>
						<div class="col-sm-4"></div>
					</div>
				</form>

				<!-- 알림 메시지 영역 -->
				<div class="row p-2" id="alertArea" style="display: none;"
					align="center">
					<span style="color: red;" id="alert"></span><br> <span
						style="color: red;" id="alert2"></span>
				</div>
			</div>
		</div>

	</div>

</div>

