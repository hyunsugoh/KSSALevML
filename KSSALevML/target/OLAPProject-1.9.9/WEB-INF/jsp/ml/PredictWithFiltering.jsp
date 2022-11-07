<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link rel="shortcut icon" href="<c:url value='/images/common/ci/ksm_favi.ico'/>">

<meta charset="utf-8">

<title></title>

<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">

<script>
var _vCurRow=''; <%--선택Row--%>
var _isRun=false;
var _fileUploadResult =null; <%-- Excel file Upload Info --%>
var _predictTotCnt, _predictDoneCnt=0; <%-- 예측처리건수 --%>
var _viewType = ""; // 화면타입 ( 1 : 모델예측. 2 : 데이터조회화면 예측(pop) )
var _searchList = null; // 조회된 데이터(Build를 위한)

// var _vFeatureInput = ["f_pump_type",
// 	"f_product",
// 	"f_temp_nor","f_temp_min","f_temp_max",
// 	"f_spec_gravity_nor","f_spec_gravity_min","f_spec_gravity_max",
// 	"f_visc_nor","f_visc_min","f_visc_max",
// 	"f_vap_pres_nor","f_vap_pres_min","f_vap_pres_max",
// 	"f_seal_cham_nor","f_seal_cham_min","f_seal_cham_max",
// 	"f_rpm_nor","f_rpm_min","f_rpm_max",
// 	"f_shaft_size"
// 	];
// var _vFeatureGrud = ["PUMP_TYPE",
// 	"PRODUCT",
// 	"TEMP_NOR","TEMP_MIN","TEMP_MAX",
// 	"SPEC_GRAVITY_NOR","SPEC_GRAVITY_MIN","SPEC_GRAVITY_MAX",
// 	"VISC_NOR","VISC_MIN","VISC_MAX",
// 	"VAP_PRES_NOR","VAP_PRES_MIN","VAP_PRES_MAX",
// 	"SEAL_CHAM_NOR","SEAL_CHAM_MIN","SEAL_CHAM_MAX",
// 	"RPM_NOR","RPM_MIN","RPM_MAX",
// 	"SHAFT_SIZE"
// 	];

$(document).ready(function(){
	
	<%-- 데이터조회화면 조회데이터 --%>
	var searchGridObj = parent.$("#objectViewGrid").jsGrid("option", "data");
	var searchList  = null;
	console.log(searchGridObj);
	if (typeof searchGridObj[0] != "undefined"){  <%-- 데이터 조회에서 호출 --%>
		_viewType = "2";
		_searchList = parent.$("#objectViewGrid").jsGrid("option", "data");	
		$("#search_cnt_title").html(  "( Retrieved Data : "+_searchList.length + "건 )"   );  <%-- 데이터 건수 표시 --%>
	}else{ <%-- Model예측에서 호출 --%>
		_viewType = "1";
	}
	
	featureGridInit(); <%-- 예측조건 입력 그리드 초기화 --%>
	insertItem({}); <%-- 예측조건 입력 그리드  첫항목 인서트 처리 --%>
		
	<%-- 추가버튼 Event --%>
	$("#btn_add_item").click(function(){
		if(	!chkActivityFunc()) return false;
		var insert_item = {};
		insertItem(insert_item);
	});
	
/*
var insert_item = {};
//데이터를 추가를 위해서 json object 생성
insert_item.Name = $("#Name").val();
insert_item.Age = parseInt($("#Age").val());
insert_item.Address = $("#Address").val();
insert_item.Country = $("#Country").val();
insert_item.Married = $('#Married').is(":checked") ? true : false;
//grid에 넣을 데이터를 object의 만들기
$("#jsGrid").jsGrid("insertItem", insert_item);
*/
	
	<%-- 예측버튼 Event --%>	
	$("#btn_predict").click(function(){
		
		//if(	!chkActivityFunc()) return false;
		
		<%--
			/데이터조회 후 예측/ 
			조회된 데이터가 없는 경우 - training data가 없는경우 
		--%>
		if(_viewType=="2" && parent.$("#objectViewGrid").jsGrid("option", "data").length  <= 0){
			alert("조회된 데이터가 없습니다.");
			return;
		}
		
	
		<%-- 결과정보 Reset --%>	
		$("#predict_result").empty();
		var predictList = $("#jsGrid_feature").jsGrid("option", "data");
		for (var i=0; i<predictList.length; i++){
			var predictItem = ""; 
			predictItem +="<input type='hidden' id='chkPredict_"+predictList[i].NO+"' value='n' />";
			predictItem +="<div class='card'>";
			predictItem +="<div class='card-header'>";
			predictItem +="<a class='card-link' data-toggle='collapse'  href='#predict"+predictList[i].NO+"' data-target='#predict"+predictList[i].NO+"'>";
			predictItem +="Item : "+predictList[i].NO + " " + (predictList[i].PUMP_TYPE==undefined?"None":predictList[i].PUMP_TYPE) + " " + (predictList[i].PRODUCT==undefined?"None":predictList[i].PRODUCT);
			predictItem +="</a><span id='predict_status"+predictList[i].NO+"' style='padding-left:20px;color:#17a2b8;'> Predicting...</span>";
			predictItem +="</div>";
			predictItem +="<div id='predict"+predictList[i].NO+"' class='collapse' data-parent='#predict_result'>";
			predictItem +="<div class='card-body'>";
			//predictItem +="<div id='predictGrid"+predictList[i].NO+"'>predict...</div>";
			predictItem +="<div class='d-flex'>";
			predictItem +="<div id='predictGrid1_"+predictList[i].NO+"'>predict...</div>";
			predictItem +="<div id='predictGrid2_"+predictList[i].NO+"'></div>";
			predictItem +="<div id='predictGrid3_"+predictList[i].NO+"'></div>";
			predictItem +="</div>";
			predictItem +="</div>";
			predictItem +="</div>";
			predictItem +="</div>";
			
			$("#predict_result").append(predictItem);
		}
		//predictMulti(predictList); <%-- 예측처리 --%>
		predictAll(predictList);  <%-- 예측처리(전체) --%>
	});
	
	<%-- 예측인자조건 정보 저장 버튼 Event --%>
	$("#btn_pop_apply_feature").click(function(){
		var gridRowObj = $('#jsGrid_feature').jsGrid('option', 'data')[_vCurRow];
		gridRowObj.PUMP_TYPE = $("#f_pump_type").val();
		gridRowObj.PRODUCT = $("#f_product").val();
		gridRowObj.TEMP_NOR = $("#f_temp_nor").val();
		gridRowObj.TEMP_MIN = $("#f_temp_min").val();
		gridRowObj.TEMP_MAX = $("#f_temp_max").val();
		gridRowObj.SPEC_GRAVITY_NOR = $("#f_spec_gravity_nor").val();
		gridRowObj.SPEC_GRAVITY_MIN = $("#f_spec_gravity_min").val();
		gridRowObj.SPEC_GRAVITY_MAX = $("#f_spec_gravity_max").val();
		gridRowObj.VISC_NOR = $("#f_visc_nor").val();
		gridRowObj.VISC_MIN = $("#f_visc_min").val();
		gridRowObj.VISC_MAX = $("#f_visc_max").val();
		gridRowObj.VAP_PRES_NOR = $("#f_vap_pres_nor").val();
		gridRowObj.VAP_PRES_MIN = $("#f_vap_pres_min").val();
		gridRowObj.VAP_PRES_MAX = $("#f_vap_pres_max").val();
		gridRowObj.SEAL_CHAM_NOR = $("#f_seal_cham_nor").val();
		gridRowObj.SEAL_CHAM_MIN = $("#f_seal_cham_min").val();
		gridRowObj.SEAL_CHAM_MAX = $("#f_seal_cham_max").val();
		gridRowObj.RPM_NOR = $("#f_rpm_nor").val();
		gridRowObj.RPM_MIN = $("#f_rpm_min").val();
		gridRowObj.RPM_MAX = $("#f_rpm_max").val();
		gridRowObj.SHAFT_SIZE = $("#f_shaft_size").val();
		$("#jsGrid_feature").jsGrid("refresh");
		$('#featureEdit').modal("hide");	
	});
	
	<%-- 예측인자조건 정보 삭제 버튼 Event --%>
	$("#btn_pop_delete_feature").click(function(){
		$('#jsGrid_feature').jsGrid('deleteItem',$('#jsGrid_feature').jsGrid('option', 'data')[_vCurRow]);
		$('#featureEdit').modal("hide");
	});
	
	<%-- 엑셀업로드 버튼 Event  - 팝업호출 --%>
	$("#btn_add_excel_item").click(function(){
		if(	!chkActivityFunc()) return false;
		$('#excelUpload').modal("show");
	});
	
	<%-- 엑셀업로드 실행 Event --%>
	$("#btn_excel_upload_ok").click(function(){
		excelUploadProcess();
	});
	
	<%-- 예측정보 엑셀저장 버튼 Event --%>
	$("#btn_sav_excel_item").click(function(){
		if(	!chkActivityFunc()) return false;
		excelSaveProcess();
	});
	
	<%-- 예측결과 Show All Event --%>
	$("#predictShowAll").click(function(){
		$('#predict_result .collapse').addClass('show');
	});
	<%-- 예측결과 Hide All Event --%>
	$("#predictHideAll").click(function(){
		$('#predict_result .collapse').removeClass('show');
	});
    
}); <%-- end  $(document).ready(function(){ --%>

function insertItem(insertObj){
	var vItemIdx = 0;
	var vGridList = $('#jsGrid_feature').jsGrid('option', 'data');
	for (var i=0;i<vGridList.length;i++) {
		if (Number(vGridList[i].NO) > vItemIdx){
			vItemIdx = Number(vGridList[i].NO);
		}
	}
	vItemIdx = vItemIdx + 1;
	insertObj.NO = vItemIdx; <%-- NO Set --%>
	$("#jsGrid_feature").jsGrid("insertItem",insertObj);
}

// feature grid init
function featureGridInit(){
	$("#jsGrid_feature").jsGrid('destroy');
	$("#jsGrid_feature").jsGrid({
    	width: "100%",
        height: "150px",
        editing: true, //수정 기본처리
        sorting: false, //정렬
        paging: false, //조회행넘어가면 페이지버튼 뜸
        loadMessage : "Now Loading...",
        fields: [
        	{name : "NO",title : "No.",type : "text",align : "center",width : 50, css:"font-size-down" },	
        	{name : "PUMP_TYPE",title : "Pump Type",type : "text",align : "left",width : 80, css:"font-size-down" },
        	{name : "PRODUCT",title : "Product",type : "text",align : "left",width : 250, css:"font-size-down"},
        	{name : "TEMP_NOR",title : "Temp.<br/>Rtd",type : "text",align : "center",width : 80, css:"font-size-down"},
        	{name : "TEMP_MIN",title : "Temp.<br/>Min",type : "text",align : "center",width : 80, css:"font-size-down"},
        	{name : "TEMP_MAX",title : "Temp.<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
        	{name : "SPEC_GRAVITY_NOR",title : "S.G.<br/>Rtd",type : "text",align : "center",width : 80, css:"font-size-down"},
        	{name : "SPEC_GRAVITY_MIN",title : "S.G.<br/>Min",type : "text",align : "center",width : 80, css:"font-size-down"},
        	{name : "SPEC_GRAVITY_MAX",title : "S.G.<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
        	{name : "VISC_NOR",title : "Visc<br/>Rtd",type : "text",width : 80,align : "center",css:"font-size-down"},
        	{name : "VISC_MIN",title : "Visc<br/>Min",type : "text",width : 80,align : "center",css:"font-size-down"},
        	{name : "VISC_MAX",title : "Visc<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
        	{name : "VAP_PRES_NOR",title : "Vapor<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "VAP_PRES_MIN",title : "Vapor<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "VAP_PRES_MAX",title : "Vapor<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "SEAL_CHAM_NOR",title : "Seal Cham.<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "SEAL_CHAM_MIN",title : "Seal Cham.<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "SEAL_CHAM_MAX",title : "Seal Cham.<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "RPM_NOR",title : "Shaft Speed<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "RPM_MIN",title : "Shaft Speed<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "RPM_MAX",title : "Shaft Speed<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{name : "SHAFT_SIZE",title : "Shaft dia.",align : "center",type : "text",width : 80, css:"font-size-down"},
        	{type: "control" ,   width: 80 ,
				itemTemplate : function(value, item) {
					var $result = $([]);
					$result = $result.add(this._createDeleteButton(item));
					return $result;
				},
				editTemplate : function(value, item) {
					// 업데이트 버튼과 캔슬 에디트 버튼 사이에 여백을 추가 
					return this._createCancelEditButton();
				},
				headerTemplate : function() {
					return '<b>삭제</b>';
				}
        	}]
	   ,
        controller:  {
            deleteItem: function(item) {
            	//console.log("delete");
            	//$("#jsGrid_feature").jsGrid('deleteItem', item); 
            }
        },
        onRefreshed: function(args){
        	//console.log('refresh');
        },
        confirmDeleting : false,
        onItemDeleting: function (args) {
        	//console.log("onItemDeleting");
            //$("#jsGrid_feature").jsGrid('deleteItem', args.item);
			//$("#jsGrid_feature").jsGrid('deleteItem', args.item); 
       },
       rowClick: function (args) {
    	  alert("click!");
    	   console.log(args);
    	   _vCurRow = args.itemIndex;      	   
	       	$("#f_pump_type").val(args.item.PUMP_TYPE);
	       	$("#f_product").val(args.item.PRODUCT);
	       	$("#f_temp_nor").val(args.item.TEMP_NOR);
	       	$("#f_temp_min").val(args.item.TEMP_MIN);
	       	$("#f_temp_max").val(args.item.TEMP_MAX);
	       	$("#f_spec_gravity_nor").val(args.item.SPEC_GRAVITY_NOR);
	       	$("#f_spec_gravity_min").val(args.item.SPEC_GRAVITY_MIN);
	       	$("#f_spec_gravity_max").val(args.item.SPEC_GRAVITY_MAX);
	       	$("#f_visc_nor").val(args.item.VISC_NOR);
	       	$("#f_visc_min").val(args.item.VISC_MIN);
	       	$("#f_visc_max").val(args.item.VISC_MAX);
	       	$("#f_vap_pres_nor").val(args.item.VAP_PRES_NOR);
	       	$("#f_vap_pres_min").val(args.item.VAP_PRES_MIN);
	       	$("#f_vap_pres_max").val(args.item.VAP_PRES_MAX);
	       	$("#f_seal_cham_nor").val(args.item.SEAL_CHAM_NOR);
	       	$("#f_seal_cham_min").val(args.item.SEAL_CHAM_MIN);
	       	$("#f_seal_cham_max").val(args.item.SEAL_CHAM_MAX);
	       	$("#f_rpm_nor").val(args.item.RPM_NOR);
	       	$("#f_rpm_min").val(args.item.RPM_MIN);
	       	$("#f_rpm_max").val(args.item.RPM_MAX);
	       	$("#f_shaft_size").val(args.item.SHAFT_SIZE);
	       		       	
	       	//$('#featureEdit .modal-content').css("height","300px");
	       	$('#featureEdit .modal-content').css("height",($(document).height()-80)+"px");
	       	
	       	$('#featureEdit').modal("show");	
        }
           
    });
}


function predictAll(predictList){
	var loadingMsg = new loading_bar({message:"Predicting..."});
	
	var cUrl = "";
	if (_viewType=="1"){ <%-- 모델예측 --%>
		cUrl="<c:url value='/ml/predictMulti1.do'/>";
	}else if (_viewType=="2"){ <%-- 데이터조회후예측 --%>
		cUrl="<c:url value='/ml/predictMulti2.do'/>";
	}else{
		alert("오류 발생");
		return false;
	}
	
	$.ajax({
		type:"POST",
		url:cUrl,
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({
			view_type : _viewType, 
			search_list : _searchList, 
			predict_list : predictList,
			predict_type : $("#f_predict_sel").val(),
			equip_type : $("#f_equip_type_sel").val(),
			target1_check : $("#target1checked").is(":checked"),
			target2_check : $("#target2checked").is(":checked"),
			target3_check : $("#target3checked").is(":checked")
		}),
		beforeSend: function(xhr) {
	       xhr.setRequestHeader("AJAX", true);
           loadingMsg.show();
	     },
	     complete: function () {
	     	loadingMsg.modal('hide');	
	     }
	}).done(function(data){
		loadingMsg.modal('hide');	
		// data set
		//setPredictData("predictGrid"+data.predict_idx, data.RESULT);
		var presult = data.RESULT;
		
		if( presult == null ){
			alert("처리중 오류가 발생하였습니다");
			return false;
		}
		
		console.log("presult : " + presult.length)
		for(var i=0;i<presult.length;i++){
			setPredictData("predictGrid1_"+presult[i].predict_idx, presult[i].RESULT.SEAL_TYPE, "Seal Type");
			setPredictData("predictGrid2_"+presult[i].predict_idx, presult[i].RESULT.API_PLAN, "API Plan");
			setPredictData("predictGrid3_"+presult[i].predict_idx, presult[i].RESULT.CONN_COL, "Seal Type|Material|API Plan");
			
			if($.trim(presult[i].predict_msg) == "complete"){
				$("#predict_status"+presult[i].predict_idx).css("color","#fd7e14");
			}else{
				$("#predict_status"+presult[i].predict_idx).css("color","#dc3545");			
			}
			$("#predict_status"+presult[i].predict_idx).html(presult[i].predict_msg);
		}
// 		setPredictData("predictGrid1_"+data.predict_idx, data.RESULT.SEAL_TYPE, "Seal Type");
// 		setPredictData("predictGrid2_"+data.predict_idx, data.RESULT.API_PLAN, "API Plan");
// 		setPredictData("predictGrid3_"+data.predict_idx, data.RESULT.CONN_COL, "Seal Type|Material|API Plan");
		
		//console.log(data.predict_msg);
// 		if($.trim(data.predict_msg) == "complete"){
// 			$("#predict_status"+idx).css("color","#fd7e14");
// 		}else{
// 			$("#predict_status"+idx).css("color","#dc3545");			
// 		}
// 		$("#predict_status"+idx).html(data.predict_msg);
//$("#chkPredict_"+idx).val("y"); //현재진행중인 예측완료처리 
		
		//setPredictProgress();
		
	}).fail(function(jqXHR, textStatus, errorThrown){
		loadingMsg.modal('hide');	
		//$("#chkPredict_"+idx).val("y");  현재진행중인 예측완료처리 
		var eMsg = "";
		if(jqXHR.status === 400){
			eMsg="요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.";
		}else if (jqXHR.status == 401) {
            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
            	location.href = OlapUrlConfig.loginPage;
            });
         } else if (jqXHR.status == 403) {
        	//eMsg="세션이 만료가 되었습니다.";
            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
            	location.href = OlapUrlConfig.loginPage;
            });
         }else if (jqXHR.status == 500) {
        	 //eMsg=jqXHR.responseText;
        	 eMsg="처리중 에러가 발생하였습니다.";
        	 eMsg = eMsg +"<br/>"+ (jqXHR.responseText).substring(0,200);
         }else{
        	 eMsg="서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.";
		}
		alert(eMsg);
		
		//$("#predict_status"+idx).html(eMsg);
		//$("#predictGrid1_"+idx).html("Error occurred");
		//setPredictProgress();
		
	}); // AJAX
}

function predictMulti(predictList){

	$("#predict_progress").css("width","0%");
	_predictTotCnt = predictList.length; // 전체예측건수
	_predictDoneCnt = 0; // 처리된예측건수
	
	for (var i=0; i<predictList.length; i++){
		console.log("predictMulti : " + i);
		predictOne(predictList[i], i);	
	}

/*
	var callPredictCnt = 0; // 
	var callCntPerAtOnce = 3;
	//호출 처리 조절
	if(callPredictCnt <= _predictDoneCnt){
        // run when condition is met
		for (var i=callPredictCnt; i<(callPredictCnt+callCntPerAtOnce); i++){
			console.log("predictMulti : " + i);
			predictOne(predictList[i], i);
		}
    }else {
        setTimeout(check, 1000); // check again in a second
    }
*/	
	
}

function predictMultiSub(){
}

function predictOne(predictFeature, idx){
	//Ajax로 예측호출
	var idx = idx+1;
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/predictWithFiltering.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		async: true,
		data: JSON.stringify({
			predict_item:predictFeature,
			predict_type:$("#f_predict_sel").val(),
			equip_type:$("#f_equip_type_sel").val(),
			predict_idx:idx,
			target1_check:$("#target1checked").is(":checked"),
			target2_check:$("#target2checked").is(":checked"),
			target3_check:$("#target3checked").is(":checked")
		}),
		beforeSend: function(xhr) {
	       xhr.setRequestHeader("AJAX", true);
	     }
	}).done(function(data){
		// data set
		//setPredictData("predictGrid"+data.predict_idx, data.RESULT);
		
		setPredictData("predictGrid1_"+data.predict_idx, data.RESULT.SEAL_TYPE, "Seal Type");
		setPredictData("predictGrid2_"+data.predict_idx, data.RESULT.API_PLAN, "API Plan");
		setPredictData("predictGrid3_"+data.predict_idx, data.RESULT.CONN_COL, "Seal Type|Material|API Plan");
		
		//console.log(data.predict_msg);
		if($.trim(data.predict_msg) == "complete"){
			$("#predict_status"+idx).css("color","#fd7e14");
		}else{
			$("#predict_status"+idx).css("color","#dc3545");			
		}
		$("#predict_status"+idx).html(data.predict_msg);
		$("#chkPredict_"+idx).val("y"); <%-- 현재진행중인 예측완료처리 --%>
		
		setPredictProgress();
		
	}).fail(function(jqXHR, textStatus, errorThrown){
		$("#chkPredict_"+idx).val("y"); <%-- 현재진행중인 예측완료처리 --%>
		var eMsg = "";
		if(jqXHR.status === 400){
			eMsg="요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.";
			//alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
		}else if (jqXHR.status == 401) {
            //alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
            //	location.href = OlapUrlConfig.loginPage;
            //});
         } else if (jqXHR.status == 403) {
        	 eMsg="세션이 만료가 되었습니다.";
            //alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
            //	location.href = OlapUrlConfig.loginPage;
            //});
         }else if (jqXHR.status == 500) {
        	 //eMsg=jqXHR.responseText;
        	 eMsg="처리중 에러가 발생하였습니다.";
        	 //errAlert(jqXHR.status, jqXHR.responseText)
         }else{
        	 eMsg="서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.";
			//alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
		}
		$("#predict_status"+idx).html(eMsg);
		$("#predictGrid1_"+idx).html("Error occurred");
		setPredictProgress();
		
	}); // AJAX
}


function setPredictData(gridId, data, className){
	console.log("result Set - gridId : " + gridId);	
	if(data ==null) data = "";
	
	$("#"+gridId).jsGrid('destroy');
	$("#"+gridId).jsGrid({
    	width: "100%",
        height: "200px",
        editing: true, //수정 기본처리
        sorting: false, //정렬
        paging: false, //조회행넘어가면 페이지버튼 뜸
        //loadMessage : "Now Loading...",
        data:data,
        fields: [
        	
        	{ name: "CHK", type: "checkbox", title: "선택", sorting: false, editing: true, width: 30, visible:(className=="Seal Type|Material|API Plan"?true:false),css:"font-size-down",
            	itemTemplate:function(value, item){
        			var rtnTemplate=$("<div>");
        			var $input = $("<input>");
        			var $label = $("<label>");
        			var $span = $("<span>");
        			$input.attr("type","checkbox");
        			$input.addClass("chbox");
        			$span.addClass("custom-checkbox");
        			$label.append($span);
       				$input.attr("checked", value || item.CHK);
        			$input.on("change", function() {
        				item.CHK = $(this).is(":checked");
                    });
        			rtnTemplate.append($input);
        			rtnTemplate.append($label);
        			return rtnTemplate.html();
        		}
            },
            
        	{name : "NO",title : "No.",type : "number",align : "center",width : 50, css:"font-size-down",editing: false },
        	{name : "CLASS",title : className, type : "text",align : "center",width : 150, css:"font-size-down",editing: false },	
        	{name : "PROB",title : "확률(%)",type : "number",width : 70,align : "center",css:"font-size-down",editing: false, format:"#,##0.000"}
        ],
        rowClick: function(args) {
	        // radio check //
	     	var selectData = args.item; //선택한 로우 데이터
	    	var selectDel = selectData.CHK; //선택한 로우의 삭제체크값 
	    	//체크되지않은 행이면 체크함
	    	if(selectDel == "undefined" || selectDel == null || selectDel==false){
	    		//전체 uncheck
	    		var gridData = $("#"+gridId).jsGrid("option", "data");
	    		console.log(gridData.length)
	    		for(var i=0;i<gridData.length;i++){
	    			$("#"+gridId).jsGrid("updateItem", gridData[i], {"CHK":false});	
	    		}
	    		$("#"+gridId).jsGrid("updateItem", selectData, {"CHK":true});
	    		
	    	}else if(selectDel==true){
	    		return false;
	    	}
        },
        onRefreshed: function(args){
        	//road 시 첫행 선택
        	var gridData = $("#"+gridId).jsGrid("option", "data");
        	if(gridData.length>0){
        		$("#"+gridId).jsGrid("updateItem", gridData[0], {"CHK":true});	
        	}
        }
	});
}

function setPredictProgress(){
	// _predictTotCnt : 총건수
	_predictDoneCnt=_predictDoneCnt+1;
	var pw = Math.floor((_predictDoneCnt/_predictTotCnt)*100);
	$("#predict_progress").css("width", (pw+"%"));
}

<%--
function setPredictData(gridId, data){
	console.log("gridId : " + gridId);	
	$("#"+gridId).jsGrid('destroy');
	$("#"+gridId).jsGrid({
    	width: "100%",
        height: "200px",
        editing: false, //수정 기본처리
        sorting: false, //정렬
        paging: false, //조회행넘어가면 페이지버튼 뜸
        //loadMessage : "Now Loading...",
        data:data,
        fields: [
        	{name : "SEAL_TYPE_NO",title : "No.",type : "number",align : "center",width : 50, css:"font-size-down" },
        	{name : "SEAL_TYPE_CLASS",title : "Seal Type.",type : "text",align : "center",width : 100, css:"font-size-down" },	
        	{name : "SEAL_TYPE_PROB",title : "Probability(%)",type : "number",width : 100,align : "center",css:"font-size-down", format:"#,##0.00000"},
        	{name : "API_PLAN_NO",title : "No.",type : "number",align : "center",width : 50, css:"font-size-down" },
        	{name : "API_PLAN_CLASS",title : "API Plan",type : "text",align : "center",width : 100, css:"font-size-down" },
        	{name : "API_PLAN_PROB",title : "Probability(%)",type : "number",width : 100,align : "center",css:"font-size-down", format:"#,##0.00000"},
        	{name : "CONN_COL_NO",title : "No.",type : "text",align : "center",width : 50, css:"font-size-down"},
        	{name : "CONN_COL_CLASS",title : "Seal Type+Material+API Plan",type : "text",align : "center",width : 200, css:"font-size-down"},
        	{name : "CONN_COL_PROB",title : "Probability(%)",type : "number",width : 100,align : "center",css:"font-size-down", format:"#,##0.00000"}
        ]
	});
}
--%>


function chkActivityFunc(){
// 	var vResult = true;
// 	$("*").filter("[id^=chkPredict_]").each(function(){
// 		if ($(this).val() =="n"){
// 			vResult= false;
// 			alert("이전작업이 진행중입니다.");
// 			return vResult;
// 		}
// 	});
//	return vResult;
return true;
}

<%--
	엑셀파일 업로드 및 인서트 처리
--%>
function excelUploadProcess(){
	var fGroupId = "";
	var menuId = "";
	var files = $('#input_files').prop('files')[0];
	var formData = new FormData();
	//for(var i=0;i<files.length;i++){
	//	formData.append('files', files[i].rawFile);
	//}
	formData.append('files', files);
	formData.append('menuId', menuId);
	formData.append('fGroupId', fGroupId);
	
	var loadingMsg = new loading_bar({message:"Uploading & Insert..."});
	_fileUploadResult = null;
    $.ajax({
        url: '/ml/excelFileUploadInsert.do',
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        beforeSend: function () {
        	loadingMsg.show();
        },
        complete: function () {
        	loadingMsg.modal('hide');	
        },
        success: function (result) {
	
        	$("#jsGrid_feature").jsGrid("option", "data", []); // 초기화	
        	<%-- Excel Data Insert  --%>
            var elist = result.excelDataList;
            for (var i=0;i<elist.length;i++){
            	var insert_item = {
            			PUMP_TYPE : elist[i].PUMP_TYPE,
            			PRODUCT : elist[i].PRODUCT,
            			SPEC_GRAVITY_NOR : elist[i].SPEC_GRAVITY,
            			SPEC_GRAVITY_MIN : elist[i].SPEC_GRAVITY,
            			SPEC_GRAVITY_MAX : elist[i].SPEC_GRAVITY,
            			VISC_NOR : elist[i].VISC,
            			VISC_MIN : elist[i].VISC,
            			VISC_MAX : elist[i].VISC,
            			TEMP_NOR : elist[i].TEMP_NOR,
            			TEMP_MIN : elist[i].TEMP_MIN,
            			TEMP_MAX : elist[i].TEMP_MAX,
            			VAP_PRES_NOR : elist[i].VAP_PRES,
            			VAP_PRES_MIN : elist[i].VAP_PRES,
            			VAP_PRES_MAX : elist[i].VAP_PRES,
            			SUCT_PRES_NOR : elist[i].SUCT_PRES_NOR,
            			SUCT_PRES_MIN : elist[i].SUCT_PRES_MIN,
            			SUCT_PRES_MAX : elist[i].SUCT_PRES_MAX,
            			DISCH_PRES_NOR : elist[i].DISCH_PRES_NOR,
            			DISCH_PRES_MIN : elist[i].DISCH_PRES_MIN,
            			DISCH_PRES_MAX : elist[i].DISCH_PRES_MAX,
            			SEAL_CHAM_NOR : elist[i].SEAL_CHAM_NOR,
            			SEAL_CHAM_MIN : elist[i].SEAL_CHAM_MIN,
            			SEAL_CHAM_MAX : elist[i].SEAL_CHAM_MAX,
            			RPM_NOR : elist[i].RPM,
            			RPM_MIN : elist[i].RPM,
            			RPM_MAX : elist[i].RPM,
            			SHAFT_SIZE : elist[i].SHAFT_SIZE
            	}
            	insertItem(insert_item);	
            } // end for
            
            <%-- upload file info Set --%>
            _fileUploadResult = result.fileUploadResult;
            /*
            result.fileUploadResult.file_group_id
            result.fileUploadResult.file_name
            result.fileUploadResult.file_name_org
            result.fileUploadResult.file_path
            result.fileUploadResult.result
            */
            
            // remove 예측결과
            $('#predict_result').empty();
            
            $('#excelUpload').modal("hide");
            //alert("처리되었습니다");
            
        },fail : function (result) {
			loadingMsg.modal('hide');	
			ajaxFailMsg(result);
		}
    });
}

<%--
	예측결과 -> 엑셀파일 : 저장
--%>
function excelSaveProcess(){
	
	if(_fileUploadResult==null){
		alert("업로드된 엑셀파일 정보가 없거나 정확하지 않습니다.");
		return false;
	}
	
	// 예측결과를 넘겨서 업로드한 파일에 Write
	var vItemIdx = 0;
	var vFeatureList = $('#jsGrid_feature').jsGrid('option', 'data'); // Feautre row 수
	var predictArr = new Array();
	for (var i=0;i<vFeatureList.length;i++) { // Feature 수만큼
		var classVal = "";
	
		// 체크된 항목을 찾는다.
		var gridId = "predictGrid3_"+(i+1);
		var gridData = $("#"+gridId).jsGrid("option", "data");
    	if (gridData.length>0){
    		for(var j=0;j<gridData.length;j++){
    			if (gridData[j].CHK){
    				classVal = gridData[j].CLASS;
    				break;
    			}
    		}
    	}else{
    		classVal = "";	
    	}
    	/*
		if (typeof ($("#predictGrid3_"+vFeatureList[i].NO).jsGrid("option", "data")[0]) == "undefined"){
			classVal = "";
		}else{
			classVal = $("#predictGrid3_"+vFeatureList[i].NO).jsGrid("option", "data")[0].CLASS;
		}
    	*/
		// 예측값
		predictArr.push(classVal);
	} // end for
	
	var loadingMsg = new loading_bar({message:"Result Save ..."});
	
	$.ajax({
        url: '/ml/predictInfoToExcelFile.do',
        type: 'POST',
        data: JSON.stringify({
        	fileInfo : _fileUploadResult,
        	predictInfo : predictArr
        }),
        async:true,
        processData: false,
        contentType: 'application/json',
        beforeSend: function () {
        	loadingMsg.show();
        },
        complete: function () {
        	loadingMsg.modal('hide');	
        },
        success: function (result) {
        	loadingMsg.modal('hide');
	        if(result.result == "ok"){
	        	// 파일다운로드 Call
				/*
				result.fileUploadResult.file_group_id
			    result.fileUploadResult.file_name
			    result.fileUploadResult.file_name_org
			    result.fileUploadResult.file_path
			    result.fileUploadResult.result
			    */
	        	var vFileParam = $.param({
	            		"file_name"        : _fileUploadResult.file_name,
	           	        "file_name_org": _fileUploadResult.file_name_org,
	           	        "file_path"          : _fileUploadResult.file_path
	           	});
	        	$("#fileDownFrame").attr("src", "<c:url value="/ml/predictApplyExcelFileDownload.do" />?"+vFileParam);
	        	
	        }else{
	        	alert("처리중 오류가 발생하였습니다.");
	        }
        },fail : function (result) {
			loadingMsg.modal('hide');
			ajaxFailMsg(result);
		}
	});
	
}

</script>

<style>
tr.highlight td.jsgrid-cell {background-color: #BBDEFB;}
.jsgrid-header-row {text-align: center;}
.red td {color: #f08080 !important;}
.jsgrid-delete-button-custom {background-position: 0 -80px;width: 16px;height: 16px;;opacity: .2;}
.jsgrid-edit-button-custom {background-position: 0 -120px;width: 16px;height: 16px;;opacity: .2;}
.font-size-down{font-size:12px;padding:0px;}
.jsgrid-cell {word-wrap: break-word;padding-bottom:0px;padding-top:5px;}

#jsGrid_SEAL_TYPE .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_SIZE .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_CONFIG .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_ALL .jsgrid-pager { font-size:12px;}



</style>
</head>

<body>

	<!-- ================  Contents ================  -->
	<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mt-2">
		<div class="row">
			<!-- style="display: none;" -->
			<div id="div1" style="display: none;"></div>
			<div id="div2" style="display: none;"></div>
			<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
			<div class="col-12">
				<div class="row">
					<div class="col-6" style="width:100%; flex:0 0 100%;max-width:100%;">
						<div class="h5" style="float:left;width:25%;">
							<strong><i class="far fa-object-ungroup"></i> <span class="ml-1"> Model 예측</span></strong>
						</div>
						<div class="h6 text-left mt-1" style="float:left;width:25%;">
							<span class="ml-2" id="search_cnt_title"></span> 
						</div>
						<!--  Button -->
						<div class="col-12 mt-3 text-right" style="float:left;margin-top:0px !important;width:50%">
							<button type="button" class="btn btn-outline-primary"  id="btn_predict">예측 <i class="fa fa-caret-square-right"></i></button>
							<button type="button" class="btn btn-outline-success"   id="btn_add_item">추가 <i class="fa fa-edit"></i></button>
							<button type="button" class="btn btn-outline-warning"   id="btn_add_excel_item">엑셀업로드 <i class="fa fa-file-excel"></i></button>
							<button type="button" class="btn btn-outline-warning"   id="btn_sav_excel_item">엑셀저장 <i class="fa fa-file-excel"></i></button>
						</div>
						<!--  Button -->
					</div>
					<div class="col-6">
						<div class="d-flex justify-content-end">
							<div id="helpIcon" class="pt-0"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="row">
			<div class="col-2">
				<p class="h5" style="width:150px;">
					<i class="fas fa-search"></i> <strong class="text-primary">예측조건</strong>
				</p>
			</div>
			<div class="col-10 pt-1"  > 
				<div class="d-flex">
					<div style="width:60px;text-align:right;padding-right:10px;"> 단계</div>
					<div>
						<select id="f_predict_sel" class="form-control input-small p-0 pl-1"  style="width:100px;height:25px;font-size:12px;">
							<option value='EPC'>EPC</option>
							<option value='OEM'>OEM</option>
						</select>
					</div>
					<div style="width:120px;text-align:right;padding-right:10px;"> Equip Type</div>
					<div>
						<select id="f_equip_type_sel" class="form-control input-small p-0 pl-1"  style="width:100px;height:25px;font-size:12px;">
							<option value='Pump'>Pump</option>
							<option value='Mixer'>Mixer</option>
							<option value='Other'>Other</option>
						</select>
					</div>
				</div>	
			</div>
		</div>
	
						
		<div class="row">	
			<div class="col-12 pt-1" >
				<!--  Feature Grid  -->
				<div id="jsGrid_feature"></div>
			</div>
		</div>
		
		
		<div class="row">	
			<div class="container-fluid">
				<div class="row">
					<div class="top-search-div collapse show mt-3" style="width:100%;margin-left:5px;">
						<div class="col-12">
						
							<div class="row">
														
								<!--  Predict -->
								<div class="col-lg-12  col-xl-12" >
								
									<div class="card-title">
										<div class="row">
										
											<div class="col-2">
												<p class="h5" style="width:150px;">
													<i class="fas fa-search"></i> <strong class="text-primary">예측결과</strong>
												</p>
											</div>
											
											<%-- predict checkbox --%>
											<div class="col-10 mt-1">
												<div class="d-flex flex-row pl-2">
													 
													 <div style="width:60%;">
														 <div class="custom-control custom-checkbox "  style="width:100px;background:url();" >
														    <input type="checkbox" class="custom-control-input" id="target1checked"  checked>
														    <label class="custom-control-label" for="target1checked" style="margin-top:-3px;"> Seal Type</label>
														 </div>   
														 <div class="custom-control custom-checkbox " style="width:100px;background:url();" >
														     <input type="checkbox" class="custom-control-input" id="target2checked" checked>
														    <label class="custom-control-label" for="target2checked" style="margin-top:-3px;"> API Plan</label>
														 </div>   
														 <div class="custom-control custom-checkbox " style="width:250px;background:url();">
														     <input type="checkbox" class="custom-control-input" id="target3checked" checked>
														    <label class="custom-control-label" for="target3checked" style="margin-top:-3px;"> Seal Type+Material+API Plan</label>
														</div>
													</div>
													
													<div class="progress" style="width:600px;display:none;">
													  <div id="predict_progress" class="progress-bar progress-bar-striped" role="progressbar" style="width: 0%" aria-valuenow="10" aria-valuemin="0" aria-valuemax="100"></div>
													</div>
													
													<div class="text-right"  style="margin-top:-15px;width:40%;">
														<p class="btn h5"    id="predictShowAll"><a href="#" class="text-info">Show All</a></p>/<p class="btn h5"    id="predictHideAll"><a href="#" class="text-info">Hide All</a></p>
													</div>
													
												</div>
											</div>
										</div>
										
													
<div id="predict_result">
</div>
													
													
					
										
									</div>
								</div>
												
							</div>
								
						</div>
					</div>
						
				</div>
			</div>
		</div>
		


	</div>
	
	
	
	
	
	
	
	
	
	
<%-- Feature Edit Popup --%>
<div class="modal" id="featureEdit">
	<div class="modal-dialog modal-lg" >
		<div class="modal-content"  style="height: 700px;width:800px;">

			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title" >Feature</div>
	              	<div style="font-size:24px;float:left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			<!-- Modal body -->
			<div class="modal-body" style="overflow-y:auto;">
				
				<div class="card custom-search-card-div pb-2">
					<!-- feature  -->
					<div class="row">
						<div class="col-sm-4">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<strong>Pump Type</strong>
							</div>
						</div>
						<div class="col-sm-8">
							<div class="pt-2 custom-responsive-p2 pr-1">
								<input id="f_pump_type" type="text" class="form-control form-control-sm"
									placeholder="Pump Type"  style="height:25px;width:100%;" >
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-sm-4">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<strong>Product</strong>
							</div>
						</div>
						<div class="col-sm-8">
							<div class="pt-2 custom-responsive-p2 pr-1">
								<input id="f_product" type="text" class="form-control form-control-sm"
									placeholder="Product"   style="height:25px;width:100%;" >
							</div>
						</div>
					</div>
					<!-- Temperature -->
					<div class="row">
						<div class="col-sm-4" >
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<strong>Temperature (℃)</strong>
							</div>
						</div>
						<div class="col-sm-8" >	
							<div class="pt-2 custom-responsive-p2" >
								<div class="d-flex">
									<input id="f_temp_nor" type="text" class="form-control form-control-sm"
										placeholder="Normal" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_temp_min" type="text" class="form-control form-control-sm"
										placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_temp_max" type="text" class="form-control form-control-sm"
										placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >		
								</div>
							</div>
						</div>
					</div>
					<!-- Specific Gravity -->
					<div class="row">
						<div class="col-sm-4">
							<div class="p-2 custom-responsive-p2"   style="min-width:120px;" >
								<strong>Specific Gravity</strong>
							</div>
						</div>	
						<div class="col-sm-8">
							<div class="pt-2 custom-responsive-p2 pr-1">
								<div class="d-flex">
									<input id="f_spec_gravity_nor" type="number" class="form-control form-control-sm"
										placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_spec_gravity_min" type="number" class="form-control form-control-sm"
										placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_spec_gravity_max" type="number" class="form-control form-control-sm"
										placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>		
							</div>
						</div>
					</div>			
					<!-- Viscosity -->
					<div class="row">
						<div class="col-sm-4">
							<div class="p-2 custom-responsive-p2 "    style="min-width:120px;" >
								<strong>Viscosity (cP)</strong>
							</div>
						</div>
						<div class="col-sm-8">
							<div class="pt-2 custom-responsive-p2">
								<div class="d-flex">
									<input id="f_visc_nor" type="number" class="form-control form-control-sm"
										placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_visc_min" type="number" class="form-control form-control-sm"
									placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_visc_max" type="number" class="form-control form-control-sm"
									placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >	
								</div>
							</div>
						</div>
					</div>		
					<!-- Vapor Pressure -->
					<div class="row">
						<div class="col-sm-4" style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "   style="min-width:150px;" >
								<strong>Vapor<br/> Pressure (BARA)</strong>
							</div>
						</div>
						<div class="col-sm-8"> 
							<div class="pt-2 custom-responsive-p2" >
								<div class="d-flex">
									<input id="f_vap_pres_nor" type="number" class="form-control form-control-sm"
										placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_vap_pres_min" type="number" class="form-control form-control-sm"
										placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_vap_pres_max" type="number" class="form-control form-control-sm"
										placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>	
					<!-- Seal Chamber Pressure 씰 챔버 압력-->
					<div class="row">
						<div class="col-sm-4" style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "   style="min-width:120px;" >
								<strong>Seal Chamber<br/> Pressure (BARG)</strong>
							</div>
						</div>
						<div class="col-sm-8"> 
							<div class="pt-2 custom-responsive-p2" >
								<div class="d-flex">
									<input id="f_seal_cham_nor" type="text" class="form-control form-control-sm"
										placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_seal_cham_min" type="text" class="form-control form-control-sm"
										placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_seal_cham_max" type="text" class="form-control form-control-sm"
										placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>		
					<!-- Shaft Speed -->
					<div class="row">
						<div class="col-sm-4" style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "   style="min-width:120px;" >
								<strong>Shaft Speed (rpm)</strong>
							</div>
						</div>
						<div class="col-sm-8"> 
							<div class="pt-2 custom-responsive-p2" >
								<div class="d-flex">
									<input id="f_rpm_nor" type="text" class="form-control form-control-sm"
										placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_rpm_min" type="text" class="form-control form-control-sm"
										placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_rpm_max" type="text" class="form-control form-control-sm"
										placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>		
					<!-- Shaft Dia -->
					<div class="row">
						<div class="col-sm-4" style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "   style="min-width:120px;" >
								<strong>Shaft Dia. (mm)</strong>
							</div>
						</div>
						<div class="col-sm-8"> 
							<div class="pt-2 custom-responsive-p2" >
								<div class="d-flex">
									<input id="f_shaft_size" type="number" class="form-control form-control-sm"
										placeholder="Shaft Size" maxlength="20"  style="height:25px;width:100%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>		
					
				</div>
					
					<div class="row">
						<div class="col-12" >
							<!--  Button -->
							<div class="col-12 mt-3 text-right" >
								<button type="button" class="btn btn-outline-success"  id="btn_pop_apply_feature">적용 <i class="fa fa-save"></i></button>
								<button type="button" class="btn btn-outline-success"  id="btn_pop_delete_feature">삭제 <i class="fa fa-eraser"></i></button>
							</div>
						</div>
					</div>
							
			</div>

		</div>
	</div>
</div>


	
<%-- file upload pop --%>
<div class="modal" id="excelUpload"  role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg" >
		<div class="modal-content" style="height: 300px;width:800px;">
		
			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title" >Excel File Upload</div>
	              	<div style="font-size:24px;float:left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			
			<!-- Modal body -->
			<div class="modal-body">
				<div class="popup_area">
					<div id="file_uploader" style="margin-top:20px;">
						<input name="input_files"  id="input_files"  type="file" aria-label="files" />
					</div>
				</div>	
				<div class="row">
					<div class="col-12" >
						<!--  Button -->
						<div class="col-12 mt-3 text-right" >
							<button type="button" class="btn btn-outline-success"  id="btn_excel_upload_ok">Upload <i class="fa fa-save"></i></button>
						</div>
					</div>
				</div>
			</div>
			
		</div>
	</div>
</div>

<iframe id="fileDownFrame" style="display:none;"></iframe>
</body>
</html>