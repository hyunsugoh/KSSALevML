<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix="c" %>


<script>
$(document).ready(function(){
	//시작시 초기화
	$("#input_authHint").val("");
	$("#input_userId").val("");
	$("#input_authHintAns").val("");
	$("#input_authHintAnsHidden").val("");
	$("#input_authHintAns").attr("readonly",true);
	$('#alertArea').css("display","none");
// 	$('#initializeArea').css("display","none");
	$('#pwdChgArea').css("display","none");
	
	//아이디 입력시 박스 초기화
	$("#input_userId").focus(function(){
		$("#input_authHint").val("");
		$("#input_userId").val("");
		
		$("#input_authHintAns").val("");
		$("#input_authHintAnsHidden").val("");
		
		$("#input_authHintAns").attr("readonly",true);
		$('#alertArea').css("display","none");
// 		$('#initializeArea').css("display","none");
		$('#pwdChgArea').css("display","none");
	});
	
	//정답 입력시 박스 초기화
	$("#input_authHintAns").focus(function(){
		$("#input_authHintAns").val("");
		$('#alertArea').css("display","none");
		$('#pwdChgArea').css("display","none");
	});
	
	$("#previousViewBtn").on("click",function(){
		window.location.replace(OlapUrlConfig.loginPage);
	});
});


//아이디 검색
function doSearch(){
	$("#input_authHint").val("");
	$("#input_authHintAns").val("");
	$("#input_authHintAnsHidden").val("");
	$('#alertArea').css("display","none");
	
	var insertId = $("#input_userId").val().trim(); 
	
	if(insertId=="" || insertId=="아이디를 입력해주세요."){
		$("#input_userId").val("아이디를 입력해주세요."); 
		return;
	}else if(insertId!=""){
		//전송
		$.ajax({
			type:"POST",
			url:OlapUrlConfig.getUserHintList,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 ajax POST호출 CSRF 
			data: {
				"insertId":insertId
			},complete : function () {
			}
		}).done(function(data){
			// console.log("done:  getUserHintList ");
		 	if(data.length==0){
			 	$('#alert').text('아이디가 존재하지 않습니다.');
			 	$('#alertArea').css("display","block");
		 	}else if(data.length==1){
				$("#input_authHint").val(data[0].authHint); 
				$("#input_authHintAnsHidden").val(data[0].authHintAns); //원본정답
				$("#input_authHintAns").val(""); 
				$("#input_authHintAns").attr("readonly",false); 
			 	$('#initializeArea').css("display","block");
		 	}else{
		 		// console.log("fail: getUserHintList02");
		 		alert("관리자에게 문의해주세요.");
		 	}
		}).fail(function(jqXHR, textStatus, errorThrown){
			// console.log("fail:  getUserHintList " + errorThrown);
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

//비밀번호 초기화
function doInitialize(){
	var insertId = $("#input_userId").val().trim(); 
	var insertAuthHintAns = $("#input_authHintAns").val().trim(); 
	var authHintAnsHidden = $("#input_authHintAnsHidden").val().trim(); 
	var authHint = $("#input_authHint").val().trim(); 
	
	if(insertId=="" || insertId=="아이디를 입력해주세요."){
		$("#input_userId").val("아이디를 입력해주세요."); 
		return;
	}
	if((insertAuthHintAns=="" || insertAuthHintAns=="아이디를 검색해주세요.") && authHint==""){
		$("#input_authHintAns").val("아이디를 검색해주세요."); 
		return;
	}
	
	if((insertAuthHintAns!=authHintAnsHidden) && insertAuthHintAns!="아이디를 검색해주세요."){
		$('#alert').text('정답이 일치하지 않습니다.');
 		$('#alertArea').css("display","block");
	}else if((insertAuthHintAns==authHintAnsHidden) && insertAuthHintAns!="아이디를 검색해주세요."){
		//초기화 실행
		$.ajax({
			type:"POST",
			url:OlapUrlConfig.userActionInitialize,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 CSRF
			dataType : 'text',
			data: {
				"insertId":insertId
			},complete : function () {
			}
		}).done(function(data){
			// console.log("success: actionInitialize");
			
			if(data.length==9){
				$('#initialPwd').text(data);
	 			$('#pwdChgArea').css("display","block");
	 			$("#submitBtn").attr("disabled",true);
			}else{
				// console.log("fail: actionInitialize02");
				alert("관리자에게 문의해주세요.");
			}
			
		}).fail(function(jqXHR, textStatus, errorThrown){
			// console.log("fail:  actionInitialize " + errorThrown);
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
		
	}else{
		// console.log("fail:  actionInitialize03 ");
 		alert("관리자에게 문의해주세요.");
 	}
}


</script>

<div class="container">
	<div class="col-md-6 offset-md-3 user-pwdlnitialize-form-div">
			<div class="form-group row">
		    	<label class="col-sm-3 control-label text-right p-1">아이디</label>
		    	<div class="col-sm-6">
		      		<input type="text" class="form-control" id="input_userId">
		    	</div>
		    	<div class="col-sm-3 p-1">
			    	<button onclick="doSearch()" type="button" class="btn btn-sm btn-outline-primary">
						검색 <i class="fas fa-search"></i>
					</button>
		    	</div>
		  	</div>
		  	<div class="form-group row">
		  		<label class="col-sm-3 control-label text-right p-1">개인확인힌트</label>
		  		<div class="col-sm-6">
		      		<input type="text" class="form-control" id="input_authHint" readonly="readonly">
		    	</div>
		  	</div>
		  	<div class="form-group row">
		  		<label class="col-sm-3 control-label text-right p-1">정답</label>
		    	<div class="col-sm-6">
		   			<input type="text" class="form-control" id="input_authHintAns" readonly="readonly">
		   			<input type="hidden" class="form-control" id="input_authHintAnsHidden">
		    	</div>
		  	</div>
		<!-- 초기화 버튼 -->
		<div class="row p-4 ml-2 text-center" id="initializeArea" style="display: block;">
			<button id="submitBtn" onclick="doInitialize()" type="button" class="col-5 btn btn-md btn-outline-primary">비밀번호 초기화</button>	
		</div>	
		<!-- 알림 메시지 영역 -->
		<div class="row p-2 text-center" id="alertArea" style="display: none;">
			<p style="color: red;" id="alert"></p>		
		</div>
		<!-- 초기화 완료 비밀번호 변경 영역 -->
		<div class="row p-4 text-center" id="pwdChgArea" style="display: none;">
			<div class="card py-3">
				<p>비밀번호가</p> 
				<p style="color: red;" id="initialPwd"></p>
				로 초기화 되었습니다.	 
				<p>로그인 후 비밀번호를 변경하시기 바랍니다.</p>
				<div class="alert alert-warning mx-3">
	  				<small>초기화된 비밀번호는 한 번만 노출됩니다. 필히 숙지하시기 바랍니다.</small>
				</div>	
			</div>
			<button id="previousViewBtn" type="button" class="btn btn-md btn-outline-primary mt-3">
				이전화면
			</button>	
		</div>					
	</div>
</div>
	
