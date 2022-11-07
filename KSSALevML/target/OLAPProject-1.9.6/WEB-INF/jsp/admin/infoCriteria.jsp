<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<sec:authentication var="principal" property="principal" />
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>	


<script>
$(document).ready(function(){
	doSelect();
	HelpMsgAction.creathHelpBtn("infoCriteria","helpIcon",OLAPAdminHelpMsg);
	//엔터부르면 검색
  	$("#tableName").keydown(function(e) {    
		if(e.keyCode == 13) {    
			doSelect();
	   	}
  	}); 
});

//조회
function doSelect(){
	var tableName = $("#tableName").val().trim();
	$.ajax({
		type:"GET"
		,url:OlapUrlConfig.getInfoCriteria
		,headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
		data: {"tableName": tableName}
	}).done(function(data){
		userGrid(data);
	}).fail(function(jqXHR, textStatus, errorThrown){
		if(jqXHR.status === 400){
			alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
		}else if (jqXHR.status == 401) {
            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
            	location.href = OlapUrlConfig.loginPage;
            });
s             
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
        height: "500px",
        editing: true, //수정 기본처리
        sorting: true, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
        data: data, 
        fields: [
            { name: "tableName",	type: "text", title: "테이블명", width: 100,editing: false, align:"center",css:"text-truncate"	},
            { name: "columnName",	type: "text", title: "컬럼", width: 70,editing: false , align:"center",css:"text-truncate"	},
            { name: "obj_name",	    type: "text", title: "객체명", width: 120,editing: false , align:"center",css:"text-truncate"	},
            { name: "objinfo_name",	type: "text", title: "객체정보명", width: 100,editing: false , align:"center",css:"text-truncate"	},
            
            { name: "qryConYn", 	type: "text", title: "조회 조건 설정 여부", width: 80,editing: false , align:"center"}
        ],
        rowClick: function(args) {
            var selectData = args.item; //선택한 로우 데이터
            $('#myModal').modal("show");
            $("#tableName2").text(selectData.tableName);
            $("#columnName2").text(selectData.columnName);
            doSearch();
        },
    	onPageChanged: function() {
    		//페이지 변경시
    		var gridData = $("#jsGrid").jsGrid("option", "data");
    	}
    });
}

//조회조건 select
function doSearch(){
	$.ajax({
		type:"GET"
		,url:OlapUrlConfig.getInfoCondition,
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
	}).done(function(data){
		searchGrid(data);
		doSearchChk();
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

//조회조건 동적 체크
function doSearchChk(){
	$.ajax({
		type:"GET"
		,url:OlapUrlConfig.getSearchChk
		,headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}
		,beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     }
		,data:{"TABLE_NAME": $("#tableName2").text(), "COLUMN_NAME": $("#columnName2").text()}
	}).done(function(data){
		var gridData = $("#jsPopGrid").jsGrid("option", "data");
		for(var i=0;i<gridData.length;i++){
			for(var j=0; j<data.length; j++){
				if(gridData[i].QRY_CON_CD == data[j].QRY_CON_CD){
 					$("#jsPopGrid").jsGrid("updateItem", gridData[i], {"delCheck":true});
				}
			}	
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
function searchGrid(data){

	$("#jsPopGrid").jsGrid({
    	width: "100%",

        editing: false, //수정 기본처리
        sorting: false, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
        data: data, 
        fields: [
        	{ name: "delCheck", type: "checkbox", title: "<input type=\'checkbox\' id=\checkAll\ class=\chbox\><label for=\checkAll\><span class=\custom-checkbox\></span></label>",  sorting: false, editing: true, width: 25,
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
        	
            { name: "QRY_CON_CD_NM", type: "text", title: "조회조건", width: 200,editing: false , align:"center"},
            { name: "QRY_CON_CD", type: "text", title: "", width: 200,editing: false , align:"center", visible: false}
        ],
        rowClick: function(args) {
  
            var selectData = args.item; //선택한 로우 데이터
			var selectDel = selectData.delCheck; //선택한 로우의 삭제체크값 
			//체크되지않은 행이면 체크함
			if(selectDel == "undefined" || selectDel == null || selectDel == false){
				$("#jsPopGrid").jsGrid("updateItem", selectData, {"delCheck":true});
		    }else if(selectDel==true){
		    	$("#jsPopGrid").jsGrid("updateItem", selectData, {"delCheck":false});
		    }
        }
    });
    //전체체크 (그리드 실행후 동작)
	$("#checkAll").click(function(){
		var gridData = $("#jsPopGrid").jsGrid("option", "data"); //현재 그리드 데이터
        //클릭되었으면
        if($("#checkAll").prop("checked")){
        	for(var i=0;i<gridData.length;i++){
     			$("#jsPopGrid").jsGrid("updateItem", gridData[i], {"delCheck":true});
     		}
        }else if(!$("#checkAll").prop("checked")){
        	for(var i=0;i<gridData.length;i++){
     			$("#jsPopGrid").jsGrid("updateItem", gridData[i], {"delCheck":false});
     		}
        }
    });
}

//조회조건 저장
function doSave(){
	//체크한 코드 가져오기
	var gridData = $("#jsPopGrid").jsGrid("option", "data"); //현재 그리드 데이터
	var checkCdArry =[];
	var unCheckCdArry =[];
	for(var i=0;i<gridData.length;i++){
		var tempJson = {};
		if(gridData[i].delCheck==true){
			tempJson["tbName"] = $("#tableName2").text();
			tempJson["columnNm"] = $("#columnName2").text();
			tempJson["value"] = gridData[i].QRY_CON_CD;
			tempJson["userId"] = "${principal.username}";
			
			checkCdArry.push(JSON.parse(JSON.stringify(tempJson)));
		}else{
			tempJson["tbName"] = $("#tableName2").text();
			tempJson["columnNm"] = $("#columnName2").text();
			tempJson["value"] = gridData[i].QRY_CON_CD;
			tempJson["userId"] = "${principal.username}";
			
			unCheckCdArry.push(JSON.parse(JSON.stringify(tempJson)));
		}
	}
	
	//체크 데이터 
	if(checkCdArry.length != 0 ){
		//전송
		var tempJson = {"userId": "${principal.username}", "tableNm" : $("#tableName2").text(), "columnNm" : $("#columnName2").text(), "qryConYn" : "Y"};
		var param = {
			checkCdArry : JSON.stringify(checkCdArry),
			unCheckCdArry : JSON.stringify(unCheckCdArry),
			conditionJson : JSON.stringify(tempJson)
		};	
		$.ajax({
			type:"POST",
			url:OlapUrlConfig.updateCondifion,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
            data: param
		}).done(function(){
			alert("저장 되었습니다.");
			$('#myModal').modal("hide");
			doSelect();
		}).fail(function(jqXHR, textStatus, errorThrown){
			console.log("fail:  updateCondifion " + errorThrown);
			
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
		//체크가 없으면 table name과 column name을 받아서 일괄 삭제 후, 조건 여부 update n 처리
		var tempJson = {"userId": "${principal.username}", "tableNm" : $("#tableName2").text(), "columnNm" : $("#columnName2").text(), "qryConYn" : "N"};
		var param = {
			checkCdArry : JSON.stringify(checkCdArry),
			unCheckCdArry : JSON.stringify(unCheckCdArry),
			conditionJson : JSON.stringify(tempJson)
		};
		$.ajax({
			type:"POST",
			url:OlapUrlConfig.updateCondifion,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
            data: param
		}).done(function(){
			alert("저장 되었습니다.");
			$('#myModal').modal("hide");
			doSelect();
		}).fail(function(jqXHR, textStatus, errorThrown){
			console.log("fail:  updateCondifion " + errorThrown);
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

</script>
<style>
tr.highlight td.jsgrid-cell {
	background-color: #BBDEFB;
}

.jsgrid-header-row  { 
    text-align: center;
}
.jsgrid>.jsgrid-pager-container{color:#007bff;}
</style>



<!-- Main -->
		<div class="col-md-9 col-lg-10 col-xl-10 col-10 ml-auto px-4">
			<div class="row">
				<div class="col-12">
					<!-- style="display: none;" -->
					<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
					<div class="row">
						<div class="col-6">
							<div class="h5">
								<strong><i class="fas fa-code-branch"></i><span class="ml-1">객체정보별 조회조건 관리</span></strong>
							</div>
						</div>
						<div class="col-6">
							<div class="d-flex justify-content-end">
								<div id="helpIcon" class="pt-0"></div>
							</div>
						</div>
					</div>
				</div>
				<div class="col-md-6"></div>
				<div class="col-md-6 pt-3">
					<div class="d-flex justify-content-end">
						<div class="input-group mb-3">
							<input id="tableName" class="form-control" type="text" name="tableName" placeholder=" 검색할 단어를 입력하세요"/>
							  <div class="input-group-append">
							  	<button onclick="doSelect()" type="button" class="btn btn-sm btn-outline-primary" style="margin-bottom: 0.8%;">검색 <i class="fas fa-search"></i></button>
							  </div>
						</div>
					</div>
					</div>
				<div class="col-12">
					<div id="jsGrid"></div>
				</div>
			</div>
		</div>
<!-- 객체정보별 조회조건 등록 팝업 -->
<div class="modal" id="myModal">
	<div class="modal-dialog modal-lg">
		<div class="modal-content">

		<!-- Modal Header -->
		<div class="modal-header">
			<div class=" h5 modal-title">객체정보별 조회조건 등록</div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
		</div>
		<!-- Modal body -->
		<div class="modal-body">
			<div class="card my-3 mx-3">
				<dl class="row pt-3">
					<dt class="col-sm-3">테이블</dt>
					<dd class="col-sm-9" id="tableName2"></dd>
					
					<dt class="col-sm-3">컬럼</dt>
					<dd class="col-sm-9" id="columnName2"></dd>
			  	</dl>
			</div>
				<p class="h5">
						<i class="fas fa-search"></i> <strong>조회조건</strong>
				</p>
				<div class="form-group">
					<div id="jsPopGrid"></div>
				</div>				
				
				
		</div>
		<div class="modal-footer">
			<div class="text-right">
				<button onclick="doSave()" type="button" class="btn btn-outline-success">저장 <i class="far fa-save"></i></button>
			</div>
		</div>		

		</div>
	</div>
</div>
