<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix="c" %>


<script>

//아이디 중복 체크
function doSearch(){
	var flag = true;
	var insertId = $("#input_userId").val().trim();
	var idRules = /^[a-zA-Z0-9]{4,12}$/;
	if(insertId==""){
	    $(".chkId-alert").css('display','block');
		return;
	}else if(!idRules.test(insertId)){
		alert("아이디는 영문 또는 숫자로 4자리 이상을 사용하셔야 합니다.");
		return;
	}else if(insertId!=""){
		var loadIndicator = new loading_bar2(); // 초기화
		loadIndicator.show(); // 로딩바 호출	
		$.ajax({
			type:"GET",
			url:OlapUrlConfig.chkUserId,
			data: {
				"insertId":insertId
			},complete : function () {
				loadIndicator.hide(); // 로딩바 종료
			}	
		}).done(function(data){
		 	if(data.length==0){
		 		alert("사용 가능한 아이디 입니다.");
		 		document.signUpForm.idDuplication.value ="idCheck";
		 	}else{
				$(".chkId-alert3").css('display','block');
		 		$("#input_userId").val("");
		 		var flag = false;
		 	}
		}).fail(function(jqXHR, textStatus, errorThrown){
			console.log("fail:  getUserHintList " + errorThrown);
			if(jqXHR.status === 400){
				alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
			}else if (jqXHR.status == 401) {
	            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
	            	location.href = OlapUrlConfig.loginPage;
	            });
	         } else if (jqXHR.status == 403) {
	            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
	            	location.href = OlapUrlConfig.loginPage;
	            });
	              
	         }else{
				alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
			}
		});
	}
	return flag;
}

//보조 아이디 중복 체크
function searchSubId(){
	var subId = $("#input_SubId").val().trim();
	if(subId==""){
	    $(".chkSubId-alert").css('display','block');
		return;
	}else if(subId!=""){
		var loadIndicator = new loading_bar2(); // 초기화
		loadIndicator.show(); // 로딩바 호출
		$.ajax({
			type:"GET",
			url:OlapUrlConfig.chkSubId,
			data: {
				"subId":subId
			},complete : function () {
				loadIndicator.hide(); // 로딩바 종료
			}
		}).done(function(data){
		 	if(data.length==0){
		 		alert("사용 가능한 아이디 입니다.");
		 		document.signUpForm.subIdDuplication.value ="idCheck";
		 	}else{
				$(".chkSubId-alert2").css('display','block');
		 		$("#input_SubId").val("");
		 	}
		}).fail(function(jqXHR, textStatus, errorThrown){
			console.log("fail:  getUserHintList " + errorThrown);
			if(jqXHR.status === 400){
				alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
			}else if (jqXHR.status == 401) {
	            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
	            	location.href = OlapUrlConfig.loginPage;
	            });
	             
	             
	         } else if (jqXHR.status == 403) {
	            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
	            	location.href = OlapUrlConfig.loginPage;
	            });
	              
	         }else{
				alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
			}
		});
	}
}

//필수값 체크
function checkRequiedValue() {
	var flag = true;
	var requireds = $("#signUpForm").find(".form-control");
	for(var i=0;i<requireds.length;i++){
		var signId = requireds[i].id;
		var signTxt = $('#'+signId).val();
		if(signTxt=="" || signTxt==null){
			if(i == 0){
				$(".chkId-alert").css('display','block');
			}else if(i == 1){
				$(".chkPw-alert").css('display','block');
			}else if(i == 2){
				$(".chkPwDf-alert").css('display','block');
			}else if(i == 3){
				$(".chkSubId-alert").css('display','block');
			}else if(i == 5){
				$(".chkHintAw-alert").css('display','block');
			}
			flag=false;
		}
	}
	return flag;
}

//비밀번호 확인 
function chkPwValue() {
	var flag = true;
	var pw = document.getElementById("input_userPw").value;
    var pwck = document.getElementById("input_chkPw").value;

    if (pw != pwck) {
    	alert('비밀번호가 일치하지 않습니다.');
    	flag=false;
    }
    
    return flag;
}

// 글자수 제한 & 비밀번호 정규식
function checkLen() {
	var flag = true;
	var userid = $("#input_userId").val().trim();
	var pwd = $("#input_userPw").val().trim();
	var idRules = /^[a-zA-Z0-9]{4,12}$/;
	var passwordRules = /(?=.*\d{1,50})(?=.*[~`!@#$%\^&*()-+=]{1,50})(?=.*[a-zA-Z]{1,50}).{9,50}$/;
	if(!idRules.test(userid)) {
		$(".chkId-alert2").css('display','block');
		var flag = false;
	}else if(!passwordRules.test(pwd)) {
		$(".chkPw-alert2").css('display','block');
		var flag = false;
	}else{
		return flag;
	}	
}

// 아이디 입력했을 때
function inputIdChk(){
	$(".chkId-alert").css('display','none');
	$(".chkId-alert2").css('display','none');
	$(".chkId-alert3").css('display','none');
    document.signUpForm.idDuplication.value ="idUncheck";
}

//비밀번호 입력했을 때
function inputPwChk(){
	$(".chkPw-alert").css('display','none');
	$(".chkPw-alert2").css('display','none');
}

//비밀번호 확인 입력했을 때
function inputPwReChk(){
	$(".chkPwDf-alert").css('display','none');
}

//보조아이디 입력했을 때
function inputSubIdChk(){
	$(".chkSubId-alert").css('display','none');
	$(".chkSubId-alert2").css('display','none');
    document.signUpForm.subIdDuplication.value ="idUncheck";
}

//개인확인 정답 입력했을 때
function inputAuthHintAnsChk(){
	$(".chkHintAw-alert").css('display','none');
}

//회원가입
function signUp(){
	//필수값 체크
	if(checkRequiedValue()){
		//아이디 비밀번호 정규식
		if(checkLen()){
			//아이디 중복체크
			if(document.signUpForm.idDuplication.value == "idCheck"){
				//보조 아이디 중복체크
				//if(document.signUpForm.subIdDuplication.value == "idCheck"){
					//비밀번호 확인
					if(chkPwValue()){
						//회원가입 데이터 인서트
						var param = { 
							"USER_ID"			:	$("#input_userId").val().trim()
							,"USER_PASSWORD"	:	$("#input_userPw").val().trim()
							//,"SUB_ID"			:	$("#input_SubId").val().trim()
							,"USER_NM"			:	$("#input_userNm").val().trim()
							//,"AUTH_HINT"		:	$("#input_hintSelect").val().trim()
							//,"AUTH_HINT_ANS"	:	$("#input_authHintAns").val().trim()		
						};
						var loadIndicator = new loading_bar2(); // 초기화
						loadIndicator.show(); // 로딩바 호출
						$.ajax({
							type:"post",
							url:OlapUrlConfig.signUpInsert,
							contentType: 'application/json',
	                        headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
							data: JSON.stringify(param),
							complete : function () {
								loadIndicator.hide(); // 로딩바 종료
							}	
						}).done(function(data){
						 	
				  			alert("사용자추가가 완료되었습니다.", function(){
                   				  //location.assign(OlapUrlConfig.loginPage); //관리자-회원관리 페이지로 
                   					location.assign(OlapUrlConfig.UserList); //관리자-회원관리 페이지로
                   			});
						}).fail(function(jqXHR, textStatus, errorThrown){
							console.log("fail:  signUpInsert " + errorThrown);
							if(jqXHR.status === 400){
								alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
							}else if (jqXHR.status == 401) {
					            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
					            	location.href = OlapUrlConfig.loginPage;
					            });
					             
					             
					         } else if (jqXHR.status == 403) {
					            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
					            	location.href = OlapUrlConfig.loginPage;
					            });
					              
					         }else{
								alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
							}
						});
					}
				//}else{
				//	alert("보조 아이디 중복체크를 해주세요");	
				//}	
			}else{
				alert("아이디 중복체크를 해주세요");	
			}	
		}
	}else{
		alert("필수값을 입력하세요");
	}
}

</script>

<div class="container">
	<div class="col-12">
		<form class="form-horizontal mx-auto user-login-form-div" id="signUpForm" name="signUpForm">
			<div class="form-group row">
		    	<label class="col-sm-3 control-label text-right p-1">아이디</label>
		    	<div class="col-sm-6">
		      		<input type="text" class="form-control" id="input_userId" onkeydown="inputIdChk()" maxlength="12">
					<input type="hidden" name="idDuplication" value ="idUncheck"/>
					<p class="chkId-alert" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">아이디를 입력해 주세요.</p>
					<p class="chkId-alert2" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">아이디는 영문 또는 숫자로 4자리 이상을 사용하셔야 합니다.</p>
					<p class="chkId-alert3" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">이미 존재하는 아이디 입니다.</p>
		    	</div>
		    	<div class="col-sm-3 p-1">
			    	<button onclick="doSearch()" type="button" class="btn btn-sm btn-outline-primary">
						중복 확인
					</button>
		    	</div>
		  	</div>
		  	
		  	<div class="form-group row">
		  		<label class="col-sm-3 control-label text-right p-1">이름</label>
		  		<div class="col-sm-6">
		      		<input type="text" class="form-control" id="input_userNm"  maxlength="200">
		    	</div>
		  	</div>
		  	
		  	<div class="form-group row">
		  		<label class="col-sm-3 control-label text-right p-1">비밀번호</label>
		  		<div class="col-sm-6">
		      		<input type="password" class="form-control" id="input_userPw" onkeydown="inputPwChk()" maxlength="12">
		      		<p class="chkPw-alert" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">비밀번호를 입력해 주세요.</p>
		      		<p class="chkPw-alert2" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">비밀번호는 숫자와 영문자 특수문자 조합으로 9자리 이상을 사용하셔야 합니다.</p>
		    	</div>
		  	</div>
		  	<div class="form-group row">
		  		<label class="col-sm-3 control-label text-right p-1">비밀번호 확인</label>
		    	<div class="col-sm-6">
		   			<input type="password" class="form-control" id="input_chkPw" onkeydown="inputPwReChk()" maxlength="12">
		   			<p class="chkPwDf-alert" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">비밀번호를 확인해 주세요.</p>
		    	</div>
		  	</div>
		  	<%--
		  	<div class="form-group row" style="visibility:hidden;">
		  		<label class="col-sm-3 control-label text-right p-1">보조 ID</label>
		    	<div class="col-sm-6">
		   			<input type="text" class="form-control" id="input_SubId" onkeydown="inputSubIdChk()" maxlength="12">
		    		<input type="hidden" name="subIdDuplication" value ="idUncheck"/>
		   			<p class="chkSubId-alert" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">보조 아이디를 입력해 주세요.</p>
		   			<p class="chkSubId-alert2" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">이미 존재하는 아이디 입니다.</p>
		    	</div>
		    	<div class="col-sm-3 p-1">
			    	<button onclick="searchSubId()" type="button" class="btn btn-sm btn-outline-primary">
						중복 확인
					</button>
		    	</div>
		  	</div>
		  	 --%>
		  	<!-- <div class="form-group row">
		  		<label class="col-sm-3 control-label text-right p-1">개인 확인 힌트</label>
		    	<div class="col-sm-6">
		   			<select class="form-control" id="input_hintSelect">
						<option>나의 보물 1호는?</option>
						<option>나의 좌우명은?</option>
						<option>가장 기억에 남는 장소는?</option>
						<option>좋아하는 스포츠팀 이름은?</option>
						<option>가장 감명깊게 본 영화는?</option>
						<option>내가 존경하는 인물은?</option>
					</select>
		    	</div>
		  	</div> -->
		  <!-- 	<div class="form-group row">
		  		<label class="col-sm-3 control-label text-right p-1">개인 확인  정답</label>
		    	<div class="col-sm-6">
		   			<input type="text" class="form-control" id="input_authHintAns" onkeydown="inputAuthHintAnsChk()" maxlength="12">
		   			<p class="chkHintAw-alert" style="font-size: 12px; line-height: 16px; margin: 3px 0 9px; color: #ff1616; text-align: left; width: 85%; display:none;">정답을 입력해 주세요.</p>
		    	</div>
		  	</div> -->
		  	<div class="form-group row" style="margin-top:10%;" >
		  		<div class="col-sm-3"></div>
		    	<div class="col-sm-6 control-label text-center p-1">
		   			<button class="btn btn-lg btn-outline-primary btn-block" onclick="signUp()" type="button"><i class="fa fa-sign-in"></i> 사용자추가</button>
		    	</div>
		    	<div class="col-sm-3"></div>
		  	</div>
			<!--토큰 -->
		  	<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
		</form>
	</div>
</div>
