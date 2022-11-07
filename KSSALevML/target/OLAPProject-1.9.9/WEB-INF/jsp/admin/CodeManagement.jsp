<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
	<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">

<script>
$(document).ready(function(){
	doSelect();
	//HelpMsgAction.creathHelpBtn("codeManagement", "helpIcon",OLAPAdminHelpMsg);
});

//조회
function doSelect(){

	$.ajax({
		type:"GET",
		url:OlapUrlConfig.getCodeManagementList,
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
	}).done(function(data){

		drawGrid(data);
		//$('#codeForm').css("display","block");
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
        	 errAlert(jqXHR.status, jqXHR.responseText)
         }else{
			alert("서버와 통신에 실패하였습니다. 네트워크 상태나 서버 기동상태를 체크하십시오.");
		}
	}); // AJAX
}

//그리드 호출
function drawGrid(data){
    $("#jsGrid").jsGrid({
        width: "100%",
        height: "500px",
        editing: true, //수정 기본처리
        sorting: true, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
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
        noDataContent: "데이터가 없습니다.",
        fields: [
            { name: "delCheck", type: "checkbox", title: "<input type=\'checkbox\' id=\checkAll\ class=\chbox\><label for=\checkAll\><span class=\custom-checkbox\></span></label> 삭제", sorting: false, editing: true,
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
            
            { name: "qryConCd", type: "text", title: "코드", width: 150,editing: false, align:"center"},
            { name: "qryConCdNm", type: "text", title: "코드명", width: 200, editing: false},
            { name: "operSym", type: "text", title: "계산식 (오퍼레이터)", width: 200,editing: false , align:"center"},
            { name: "qryExample", type: "text", title: "예시", width: 200,editing: false , align:"center"}
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

//저장
function doSave(){
 	var insertCd = $('#input_cd').val().trim();
	var insertCdNm = $('#input_cdNm').val().trim();
	var insertOperSym= $('#input_operSym').val().trim();
	var insertQryExample= $('#input_qryExample').val().trim();
	
	//널 체크
	if(insertCd=='' || insertCdNm =='' || insertOperSym==''){
		alert("값의 입력이 필요합니다.");
		return;
	}
	
	//중복값 체크
	var gridData = $("#jsGrid").jsGrid("option", "data"); //현재 그리드 데이터
	for(var i=0;i<gridData.length;i++){
		if(gridData[i].qryConCd==insertCd){
			alert("동일한 코드 "+ insertCd +" 가 존재합니다.");
			return;
		}
	}
	
	//입력 전 유효성 체크
	var cdRules  = /^[a-zA-Z0-9]{1,10}$/;
	var nmRules  = /^[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{1,20}$/;
	var symRules = /^[A-Za-z0-9_\`\~\!\@\#\$\%\^\&\*\(\)\-\=\+\\\{\}\[\]\'\"\;\:\<\,\>\.\?\/\s]{1,10}$/; 
	
	if(!cdRules.test(insertCd)){
		alert("코드는 영문으로 10자 이하를 입력하십시오.");
		return;
		
	}
	
	if(!nmRules.test(insertCdNm)){
		alert("코드명은 한글로 20자 이하를 입력하십시오.");
		return;
		
	}
	
	if(!symRules.test(insertOperSym)){
		alert("계산식은 한글을 제외한 10자 이하로 입력하십시오.");
		return;
		
	}
	if(insertQryExample.length >50){
		alert("예시는 50자 이하로 입력하십시오.");
		return;
		
	}

	//전송
	$.ajax({
		type:"POST",
		url:OlapUrlConfig.insertCodeManagement,
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 ajax POST호출 CSRF 
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
		data: {
			"insertCd":insertCd,
			"insertCdNm":insertCdNm,
			"insertOperSym":insertOperSym,
			"insertQryExample":insertQryExample
		}
	}).done(function(){
		console.log("success: insertCodeManagement.");
		alert("저장이 완료되었습니다.");
	 	$('#input_cd').val("");
		$('#input_cdNm').val("");
		$('#input_operSym').val("");
		$('#input_qryExample').val("");
		doSelect();
		doClose();
	}).fail(function(jqXHR, textStatus, errorThrown){
		console.log("fail:  insertCodeManagement " + errorThrown);
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
         } else if (jqXHR.status == 500) {
        	 errAlert(jqXHR.status, jqXHR.responseText)
         }else{
			alert("서버와 통신에 실패하였습니다. 네트워크 상태나 서버 기동상태를 체크하십시오.");
		}
	});
}

//삭제
function doDelete(){
	//체크한 코드 가져오기
	var gridData = $("#jsGrid").jsGrid("option", "data"); //현재 그리드 데이터
	var checkCdArry =[];
	for(var i=0;i<gridData.length;i++){
		if(gridData[i].delCheck==true){
			checkCdArry.push(gridData[i].qryConCd);
		}
	}
	if(checkCdArry.length != 0 ){
		confirm({
			message:'<p class="text-center">조회 조건 코드를 삭제하시면 연관된 객체 정보별 조회 조건도 삭제됩니다.삭제하시겠습니까?</p>',
					title:'<h6>삭제하기</h6>',
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

				//전송
				$.ajax({
					type:"POST",
					url:OlapUrlConfig.deleteCodeManagement,
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 CSRF
					beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
					data: {
						checkCdArry : checkCdArry		
					}
				}).done(function(){
					console.log("success: deleteCodeManagement");
					alert("삭제가 완료되었습니다.");
					doSelect();
				}).fail(function(jqXHR, textStatus, errorThrown){
					console.log("fail:  deleteCodeManagement " + errorThrown);
					if(jqXHR.status === 400){
						alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
					}else if (jqXHR.status == 401) {
			            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
			            	location.href = OlapUrlConfig.loginPage;
			            });
			             
			             
			         }  else if (jqXHR.status == 500) {
			        	 errAlert(jqXHR.status, jqXHR.responseText);
			         } else if (jqXHR.status == 403) {
			            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
			            	location.href = OlapUrlConfig.loginPage;
			            });
			         }else{
						alert("서버와 통신에 실패하였습니다. 네트워크 상태나 서버 기동상태를 체크하십시오.");
					}
				});
				
			}
			
		});
		
	}else if(checkCdArry.length == 0 ){
		alert("삭제할 데이터가 없습니다.");
		return;
	}

}

function doInsert(){
	$('#codeForm').css("display","block");
}
function doClose(){
	$('#inserModal').modal("hide");
}

</script>

<style>
/* JS Grid customize */
.jsgrid-header-row>.jsgrid-header-cell {
	font-size: 12pt;
	font-weight: bold;
	text-align: center;
}
.jsgrid-cell { 
    word-wrap: break-word; 
}
</style>


<!-- Main -->
<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">
	<div class="row">
		<div class="col-12 pt-3">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="fas fa-laptop-code"></i> <span class="ml-1">조회 조건 코드 관리</span></strong>
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
			<div id="jsGrid"></div>
		</div>
		<div class="col-12 text-right">
			<button onclick="doInsert()" type="button" class="btn btn-outline-success" data-toggle="modal" data-target="#inserModal">등록 <i class="fas fa-plus"></i></button>
			<button onclick="doDelete()" type="button" class="btn btn-outline-primary">삭제 <i class="fas fa-trash-alt"></i></button>
		</div>

	</div>
	<div class="row">
			<div class="col-12">
				<div id="externalPager" class="p-2"></div>			
			</div>	
	
	</div>

	<div class="modal fade" id="inserModal">
			<div class="modal-dialog  modal-md">
				<div class="modal-content col-12">

					<!-- Modal Header -->
					<div class="modal-header">
						<h4 class="modal-title">등록</h4>
						<button type="button" class="close" data-dismiss="modal">&times;</button>
					</div>

					<!-- Modal body -->
					<div class="modal-body">
						<div class="col-12" id="codeForm" style="display: none">
						<div class="card" style="height:95%;">
							<div class="card-body object-select-card-body">
								 <div class= row>
									<p class="col-9 card-title h5">
										<i class="fas fa-plus"></i> <strong>조회 조건 코드 등록</strong>
									</p>
								</div>
								 <form class="form-horizontal p-3">
									<div class="form-group row">
								    	<label class="col-sm-3 control-label text-right p-1" style="height: 37px;">코드 *</label>
								    	<div class="col-sm-7">
								      		<input type="text" class="form-control" id="input_cd">
								    	</div>
								  	</div>
								  	<div class="form-group row">
								  		<label class="col-sm-3 control-label text-right p-1" style="height: 37px;">코드명 *</label>
								  		<div class="col-sm-7">
								      		<input type="text" class="form-control" id="input_cdNm">
								    	</div>
								  	</div>
								  	<div class="form-group row">
								  		<label class="col-sm-3 control-label text-right p-1" style="height: 37px;">계산식 *</label>
								    	<div class="col-sm-7">
								   			<input type="text" class="form-control" id="input_operSym">
								    	</div>
								  	</div>
								  	<div class="form-group row">
								  		<label class="col-sm-3 control-label text-right p-1" style="height: 37px;">예시 </label>
								    	<div class="col-sm-7">
								   			<input type="text" class="form-control" id="input_qryExample">
								    	</div>
								  	</div>
								  	
								</form>
								<div class="text-right">
									<button onclick="doSave()" type="button" class="btn btn-outline-success">저장 <i class="fas fa-save"></i></button>
								</div>
							</div>
						</div>
					</div>

					</div>

				</div>
			</div>
	</div>
</div>