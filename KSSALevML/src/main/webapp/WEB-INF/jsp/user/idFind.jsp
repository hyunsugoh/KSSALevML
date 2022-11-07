<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix="c" %>

<script>
$(document).ready(function(){
	//시작시 초기화

	$("#input_userSubId").val("");
    $('#idFindArea').css("display","none");
	
    
  //보조 아이디 입력시 박스 초기화
	$("#input_userSubId").focus(function(){
       $("#input_userSubId").val("");
       $('#idFindArea').css("display","none");
	});	
  
	$("#goTologin").on("click", function(){
		window.location.replace(OlapUrlConfig.loginPage);
	});
	
});




//아이디 찾기 
function doFind(){
	var insertSubId = $("#input_userSubId").val().trim(); 
		
	if(insertSubId=="" || insertSubId=="보조 아이디를 입력해주세요."){
		$("#input_userSubId").val("보조 아이디를 입력해주세요."); 
		return;
	}else{
		var loadIndicator = new loading_bar2(); // 초기화
		loadIndicator.show(); // 로딩바 호출
		//아이디 찾기  실행
		$.ajax({
			type:"POST",
			url:OlapUrlConfig.actionFind,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 CSRF
			dataType : 'text',
			data: {
				"insertSubId":insertSubId
			},complete : function () {
				loadIndicator.hide(); // 로딩바 종료
			}
		}).done(function(data){
			// console.log("success: actionFind");
			
			if(data == ""){
				
				// console.log("fail: actionFind");
				alert("존재하는 ID가 없습니다.");
				
			}else{
				$('#findId').text(data);
	 			$('#idFindArea').css("display","block");
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
 	}
}


</script>

<div class="container">
	<div class="col-md-6 offset-md-3 text-center user-idFind-form-div">
		
		<form class="form-horizontal offset-md-1">
			<div class="form-group row">
		    	<label class="col-sm-3 control-label text-right p-1">보조 아이디</label>
		    	<div class="col-sm-6">
		      		<input type="text" class="form-control" id="input_userSubId">
		    	</div>

		  	</div>
		 
		</form>
		<!-- 아이디 찾기 버튼 -->
		<div class="row p-4 ml-2 mx-auto text-center" id="findArea" style="display: block;">
			<button onclick="doFind()" type="button" class="col-5 btn btn-md btn-outline-primary">아이디 찾기</button>	
		</div>	

		<!-- 아이디 알려주는 영역 -->
		<div class="row p-4 mx-auto text-center" id="idFindArea" style="display: none;">
			<p>아이디는</p> 
			<p style="color: red;" id="findId"></p>
			<p>입니다</p> 
	<!--	<p>비밀번호를 변경하시기 바랍니다.</p>	-->	
			<button id=goTologin type="button" class="btn btn-md btn-outline-primary">
				이전화면
			</button>	
		</div>					
	</div>
</div>
	