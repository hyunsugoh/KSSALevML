<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script>
$(document).ready(function(){
	doSelect();
	HelpMsgAction.creathHelpBtn("userList","helpIcon",OLAPAdminHelpMsg);
	//엔터부르면 검색
  	$("#searchId").keydown(function(e) {    
		if(e.keyCode == 13) {    
			doSelect();
	   	}
  	});
	
  	$("#goTosignUp").on("click", function() { //회원가입
		window.location.replace(OlapUrlConfig.signup);
	});
  	
});

//조회
function doSelect(){
	var selectId = $("#searchId").val().trim();
	$.ajax({
		type:"GET"
		,url:OlapUrlConfig.getUserInfoList
		,headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}
		,beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     }
		,data: {"USER_ID": selectId}
	}).done(function(data){
		userGrid(data);
		var userCnt = 0;
		if(data != undefined && data !=null && data.length > 0){
			userCnt =data[0].USR_COUNT;
		}
		
		$("#userCount").text("회원 수: "+userCnt+"명");
	}).fail(function(jqXHR, textStatus, errorThrown){
		console.log("fail:" + textStatus);
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
              
         }else if (jqXHR.status == 500) {
        	 errAlert(jqXHR.status, jqXHR.responseText);
         }else{
			alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
		}
	}); // AJAX
}

//그리드 호출
function userGrid(data){
	$("#jsGrid").jsGrid('destroy');
	$("#jsGrid").jsGrid({
    	width: "100%",
        height: "380px",
        editing: true, //수정 기본처리
        sorting: true, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
        data: data, 
        fields: [
            { name: "delCheck", type: "checkbox", title: "<input type=\'checkbox\' id=\checkAll\ class=\chbox\><label for=\checkAll\><span class=\custom-checkbox\></span></label>", sorting: false, editing: true, width: 25,
            	itemTemplate:function(value, item){
        			var rtnTemplate=$("<div>"),$input = $("<input>");
        			var $label = $("<label>");
        			var $span = $("<span>");
        			$input.attr("type","checkbox");
        			$input.addClass("chbox");
        			if(item.hasOwnProperty("QRY_CON_CD")){
        				$input.attr("id","chbox_"+item["QRY_CON_CD"]);
        				$label.attr("for","chbox_"+item["QRY_CON_CD"]);
        			}
        			$span.addClass("custom-checkbox");
        			
        			$label.append($span);
       				$input.attr("checked", value || item.delCheck);
        			$input.on("change", function() {
        				item.delCheck = $(this).is(":checked");
                    });
        			rtnTemplate.append($input);
        			rtnTemplate.append($label);
        			return rtnTemplate.html();
        		}
            },
            
            { name: "USER_ID", type: "text", title: "아이디", width: 150,editing: false, align:"center"},
            { name: "CREATE_DT", type: "text", title: "최초 생성 일자", width: 100,editing: false , align:"center"},
            { name: "LOGIN_DATE", type: "text", title: "마지막 접속 일자", width: 100,editing: false , align:"center"}
        ],
        rowClick: function(args) {
            var selectData = args.item; //선택한 로우 데이터
			var selectDel = selectData.delCheck; //선택한 로우의 삭제체크값 
			//체크되지않은 행이면 체크함
			if(selectDel == "undefined" || selectDel == null || selectDel==false){
				$("#jsGrid").jsGrid("updateItem", selectData, {"delCheck":true});
		    }else if(selectDel==true){
		    	$("#jsGrid").jsGrid("updateItem", selectData, {"delCheck":false});
		    }
        },
    	onPageChanged: function() {
    		//페이지 변경되면 전체 체크 해제
    		$("#checkAll").prop("checked",false);
    		var gridData = $("#jsGrid").jsGrid("option", "data");
    		for(var i=0;i<gridData.length;i++){
     			$("#jsGrid").jsGrid("updateItem", gridData[i], {"delCheck":false});
     		}
    	}
    });
    
    //전체체크 (그리드 실행후 동작)
	$("#checkAll").click(function(){
		var gridData = $("#jsGrid").jsGrid("option", "data"); //현재 그리드 데이터
        //클릭되었으면
        if($("#checkAll").prop("checked")){
        	for(var i=0;i<gridData.length;i++){
     			$("#jsGrid").jsGrid("updateItem", gridData[i], {"delCheck":true});
     		}
        }else if(!$("#checkAll").prop("checked")){
        	for(var i=0;i<gridData.length;i++){
     			$("#jsGrid").jsGrid("updateItem", gridData[i], {"delCheck":false});
     		}
        }
    });
}

// 비밀번호 초기화 
function doInitialize(){
	//현재 그리드 데이터
	var gridData = $("#jsGrid").jsGrid("option", "data");
	var checkCdArry =[];
	for(var i=0;i<gridData.length;i++){
		if(gridData[i].delCheck==true){
			checkCdArry.push(gridData[i].USER_ID);
		}
	}
	//체크박스 확인
	if(checkCdArry.length == 0 ){
		alert("초기화할 계정을 선택해 주세요");
		return;
	}else if(checkCdArry.length > 1 ){
		alert("계정을 하나만 선택해 주세요");
		return;
	}else{
		//초기화 실행
		var insertId = checkCdArry[0]; 

		$.ajax({
			type:"POST",
			url:OlapUrlConfig.actionInitialize,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
			dataType : 'text',
			data: {
				"insertId":insertId
			}
		}).done(function(data){
			console.log("success: actionInitialize");
				
			if(data.length==9){
				alert("비밀번호가 <strong style='color:red;'>"+ data + "</strong>로 초기화 되었습니다.");
		 		doSelect();
			}else{
				console.log("fail: actionInitialize02");
				alert("관리자에게 문의해주세요.");
			}
				
		}).fail(function(jqXHR, textStatus, errorThrown){
			console.log("fail:  actionInitialize " + errorThrown);
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
	              
	         }else if (jqXHR.status == 500) {
	        	 errAlert(jqXHR.status, jqXHR.responseText);
	         }else{
				alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
			}
		});
	}
}

function confirmChk(){
	confirm({
		message:'<p class="text-center">초기화 하시겠습니까?</p>',
		title:'<h6>초기화</h6>',
		buttons :{
			confirm: {
				label: '확인',
				className: 'btn btn-sm btn-primary'
			},
			cancel: {
				label: '취소',
				className: 'btn btn-sm  btn-secondary'
			}
		},
	},function(result){
		if(result){
			doInitialize();
		}else{
			return;
		}
	});
}

//삭제
function doDelete(){
	var message = confirm("삭제 하시겠습니까?")
	if(message == false){
		return;
	}
	//체크한 코드 가져오기
	var gridData = $("#jsGrid").jsGrid("option", "data"); //현재 그리드 데이터
	var checkCdArry =[];
	for(var i=0;i<gridData.length;i++){
		if(gridData[i].delCheck==true){
			checkCdArry.push(gridData[i].USER_ID);
		}
	}
	if(checkCdArry.length != 0 ){
		//전송

		$.ajax({
			type:"GET",
			url:OlapUrlConfig.deleteUserList,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
			data: {
				checkCdArry : checkCdArry		
			}
		}).done(function(){
			alert("삭제가 완료되었습니다.");
			doSelect();
		}).fail(function(jqXHR, textStatus, errorThrown){
			console.log("fail:  deleteUserList " + errorThrown);
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
	              
	         }else if (jqXHR.status == 500) {
	        	 errAlert(jqXHR.status, jqXHR.responseText);
	         }else{
				alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
			}
		});
	}else if(checkCdArry.length == 0 ){
		alert("삭제할 데이터가 없습니다.");
		return;
	}
}

function doDeleteChk(){
	confirm({
		message:'<p class="text-center">삭제 하시겠습니까?</p>',
		title:'<h6></h6>',
		buttons :{
			confirm: {
				label: '확인',
				className: 'btn btn-sm btn-primary'
			},
			cancel: {
				label: '취소',
				className: 'btn btn-sm  btn-secondary'
			}
		},
	},function(result){
		if(result){
			doDelete();
		}else{
			return;
		}
	});
}

</script>
<style>
tr.highlight td.jsgrid-cell {
	background-color: #BBDEFB;
}

.jsgrid-header-row  { 
    text-align: center;
}
</style>

<!-- Main -->
<div class="col-md-9 col-lg-10 col-xl-10 col-10 ml-auto px-4">
	<div class="row">
		<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
		<div class="col-12">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="fas fa-users"></i>  <span class="ml-1">회원 관리</span></strong>
					</div>
				</div>
				<div class="col-6">
					<div class="d-flex justify-content-end">
						<div id="helpIcon" class="pt-0"></div>
					</div>
				</div>						
			</div>
		</div>	
		<div class="col-12 pt-5">
			<div class="row">
				<div class="col-md-6 pl-3 p-2 d-flex align-items-center">
					<div>
						<span id="userCount"></span>
					</div>
				</div>
				<div class="col-md-6">
					<div class="d-flex justify-content-end">
						<div class="input-group mb-3" >
							<input id="searchId" type="text"class="form-control" name="searchId" placeholder=" 아이디" />
							<div class="input-group-append">
								<button onclick="doSelect()" type="button" class="btn  btn-outline-primary">검색 <i class="fas fa-search"></i></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="col-12">
			<div id="jsGrid"></div>
		</div>
		<div class="col-12 d-flex justify-content-end">
			<div>
				<button id="goTosignUp" type="button" class="btn btn-outline-primary">회원가입</button>
				<button onclick="confirmChk()" type="button" class="btn btn-outline-primary">비밀번호 초기화</button>
				<button onclick="doDeleteChk()" type="button" class="btn btn-outline-info">삭제 <i class="fas fa-trash-alt"></i></button>
			</div>
			
		</div>
	</div>
</div>


