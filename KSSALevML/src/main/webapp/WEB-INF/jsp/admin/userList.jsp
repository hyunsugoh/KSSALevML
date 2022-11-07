<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">

<script>
$(document).ready(function(){
	doSelect();
	/* HelpMsgAction.creathHelpBtn("userList","helpIcon",OLAPAdminHelpMsg); */
	//엔터부르면 검색
  	$("#searchId").keydown(function(e) {    
		if(e.keyCode == 13) {    
			doSelect();
	   	}
  	});
	
  	$("#goTosignUp").on("click", function() { //회원가입
		window.location.replace(OlapUrlConfig.signup);
	});
  	
  	$("#savUserData").click(function(){
  		doSavUserData();
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
		
		$("#userCount").text("사용자 : "+userCnt+"명");
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

//사용자 정보 수정 (자료실 권한 정보 등)
function doSavUserData(){
	
	$.ajax({
		type:"POST"
		,url:"<c:url value='/admin/api/setUserData.do'/>"
		,headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}
		,beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", false);
	     }
		//,data: {"USER_ID": selectId}
		,contentType: "application/json"
		,data: JSON.stringify({
			"USER_ID": $("#mng_user_id").val(),
			"USER_NM": $("#mng_user_nm").val(),
			"REF_DATA_ROLE" : $("#mng_ref_data_role").val(),
			"USER_ROLE": $("#mng_user_role").val()
		})
	}).done(function(data){
		// 수정 정보  반영
		if(data.result =="ok"){
			var gridData = $("#jsGrid").jsGrid("option", "data"); //현재 그리드 데이터
			var refDataRoleNm = "권한없음";
        	for(var i=0;i<gridData.length;i++){
        		console.log(gridData[i].USER_ID +":"+data.USER_ID);
        		if (gridData[i].USER_ID == data.USER_ID){
        			console.log("반영 : " + gridData[i].USER_ID);
        			gridData[i].USER_NM = data.USER_NM;	
        			gridData[i].REF_DATA_ROLE = data.REF_DATA_ROLE;
        			gridData[i].USER_ROLE = data.USER_ROLE;
        			
        			if ( gridData[i].REF_DATA_ROLE == "R1"){
        				refDataRoleNm = "조회";
        			}else if ( gridData[i].REF_DATA_ROLE == "R5"){
        				refDataRoleNm = "저장";
        			}
        			gridData[i].REF_DATA_ROLE_TEXT = refDataRoleNm;
        			
        			break;
        		}
     		}
        	$("#jsGrid").jsGrid("refresh");//list 갱신
        	$('#refDataRoleMng').modal("hide");	
		}
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
        height: "600px",
        editing: true, //수정 기본처리
        sorting: true, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
        pageSize : 10,
        pageButtonCount: 5,
        pagerContainer: "#externalPager",
        pagerFormat: ' {first} {prev} {pages} {next} {last} &nbsp;&nbsp;  &nbsp;&nbsp; 전체 {pageCount} 페이지 중 현재 {pageIndex} 페이지',
        pagePrevText: "이전",
        pageNextText: "다음",
        pageFirstText: "처음",
        pageLastText: "마지막",
        pageNavigatorNextText: "&#8230;",
        pageNavigatorPrevText: "&#8230;",
		noDataContent: "데이터가 없습니다.",
		loadMessage: "데이터를 불러오는 중입니다...",
		pagerContainerClass: "custom-jsgrid-pager-container",
        pagerClass: "custom-jsgrid-pager",
        pagerNavButtonClass: "custom-jsgrid-pager-nav-button",
        pagerNavButtonInactiveClass: "custom-jsgrid-pager-nav-inactive-button",
        pageClass: "custom-jsgrid-pager-page",
        currentPageClass: "custom-jsgrid-pager-current-page",
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
            
            { name: "USER_ID", type: "text", title: "아이디", width: 120,editing: false, align:"center"},
            { name: "USER_NM", type: "text", title: "이름", width: 150,editing: false, align:"center"},
            { name: "CREATE_DT", type: "text", title: "최초 생성일", width: 80,editing: false , align:"center"},
            { name: "LOGIN_DATE", type: "text", title: "마지막 접속일", width: 80,editing: false , align:"center"},
            { name: "USER_ROLE", type: "text", title: "권한", width: 80,editing: false , align:"center"},
            { name: "REF_DATA_ROLE", type: "text", title: "자료실 권한", width: 80,editing: false , align:"center", visible : false},
            { name: "REF_DATA_ROLE_TEXT", type: "text", title: "자료실 권한", width: 50,editing: false , align:"center"},
            { name: "MNG", type: "text", title: "관리", width: 50,editing: false , align:"center",
            	itemTemplate: function(_, item) {
					var $rtnDiv = $("<div>");
					$rtnDiv.append("<button>");
					$rtnDiv.find("button").addClass("jsgrid-button jsgrid-edit-button");
					return $rtnDiv.find("button").on("click", function() {
						$('#mng_user_id').val(item.USER_ID);
						$('#mng_user_nm').val(item.USER_NM);
						//$('#mng_ref_data_role').val(item.REF_DATA_ROLE);
						$('#mng_ref_data_role option[value='+item.REF_DATA_ROLE+']').attr('selected','selected');
						$('#mng_user_role option[value='+item.USER_ROLE+']').attr('selected','selected');
						$('#refDataRoleMng').modal("show");						
						return false;
					});
				}	
            }
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
        <%--
    	,headerRowRenderer: function() {
	        var $result = $("<tr>").height(30)
                .append($("<th>").width(25).text("").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	          	.append($("<th>").width(150).text("아이디").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").width(200).text("이름").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").width(80).text("최초 생성 일자").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").width(80).text("마지막 접속 일자").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("colspan", 2).width(80).text("자료실").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	         ;
            return $result;    
    	}--%>
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
	var gridData = $("#jsGrid").jsGrid("option", "data");
	var checkCdArry =[];
	for(var i=0;i<gridData.length;i++){
		if(gridData[i].delCheck==true){
			checkCdArry.push(gridData[i].USER_ID);
		}
	}
	if(checkCdArry.length == 0 ){
		alert("초기화할 계정을 선택해 주세요");
		return;
	}else if(checkCdArry.length > 1 ){
		alert("계정을 하나만 선택해 주세요");
		return;
	}else{
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
<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">
	<div class="row">
		<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
		<div class="col-12 pt-3">
			<div class="row">
				<div class="col-12">
					<div class="row">
						<div class="col-lg-6 col-sm-12 ">
							<div class="h5">
								<strong><i class="fas fa-users"></i>  <span class="ml-1">사용자</span></strong>
							</div>
						</div>
						<div class="col-lg-6  col-sm-12 d-flex justify-content-end">
							<div>
								<button id="goTosignUp" type="button" class="btn btn-outline-primary">사용자 추가</button>
								<button onclick="confirmChk()" type="button" class="btn btn-outline-primary">비밀번호 초기화</button>
								<button onclick="doDeleteChk()" type="button" class="btn btn-outline-info">삭제 <i class="fas fa-trash-alt"></i></button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="card mt-1">
		<div class="row  p-2" >	
				<div class="col-md-4 pl-3 p-2 d-flex align-items-center">
					<div>
						<span id="userCount"></span>
					</div>
				</div>
				<div class="col-md-8 mb-1">
					<div class="d-flex justify-content-end">
						<div><input id="searchId" type="text"class="form-control" name="searchId" placeholder=" 아이디"  style="height:35px;"/> </div>
						<div><button onclick="doSelect()" type="button" class="btn  btn-outline-primary ml-1"><i class="fas fa-search"></i> 검색 </button></div>
					</div>
			</div>
		</div>
	</div>
	
	<div class="row">
		<div class="col-12 mt-2">
			<div id="jsGrid"></div>
		</div>
	</div>
		<div class="row">
			<div class="col-12">
				<div id="externalPager" class="p-1"></div>			
			</div> 
		</div>
</div>

<%-- 자료실 권한 설정 pop --%>
<div class="modal" id="refDataRoleMng" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg">
		<div class="modal-content" style="height: 300px;">
			<div class="modal-header">
				<!-- <div class=" h5 modal-title" >이력관리</div> -->
				<div style="font-size: 24px; float: left;"></div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>

			<div class="modal-body">
				<div class="row">
					<div class="col-2">
						<p class="h5" style="width: 150px;">
							<strong class="text-primary">사용자 정보 수정</strong>
						</p>
					</div>
				</div>
				<div class="row">
					<div class="col-6">
						<div class="pt-2 custom-responsive-p2">
							이름
						</div>
					</div>
					<div class="col-6">
						<input id="mng_user_nm" type="text"class="form-control" name="mng_user_nm"   style="height:25px;"/> 
					</div>
				</div>
				<div class="row">
					<div class="col-6">
						<div class="pt-2 custom-responsive-p2">
							권한
						</div>
					</div>
					<div class="col-6">
						<div class="pt-2" >
							<select id="mng_user_role"style="width: 100%; height: 30px; padding: 0px;"class="selectpicker form-control pt-1" >
								<option value='ROLE_USER'>ROLE_USER</option>
								<option value='ROLE_SUPER'>ROLE_SUPER</option>
							</select>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-6">
						<div class="pt-2 custom-responsive-p2">
							자료실 권한
						</div>
					</div>
					<div class="col-6">
						<div class="pt-2">
							<select id="mng_ref_data_role"style="width: 100%; height: 30px; "class="selectpicker form-control pt-1" >
								<option value='R0'>권한없음</option>
								<option value='R1'>조회</option>
								<option value='R5'>저장</option>
							</select>
						</div>
					</div>
				</div>
				<div class="row">	
					<div class="col-12 d-flex flex-row-reverse">
						<div class="p-2 custom-responsive-p2">
							<button id="savUserData" type="button"class="btn btn-outline-success"><i class="far fa-save"></i> 저장</button>
						</div>
					</div>
				</div>	
			</div>
		</div>
	</div>
	<input id='mng_user_id' type='text'  style="display:none;"/>
</div>


