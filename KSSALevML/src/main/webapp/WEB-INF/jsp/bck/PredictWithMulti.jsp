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
var _limitSearchCnt = 10000;

var _getFeatureRangeList = ""; //예측할 값과 비교하기위해 전역변수화
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
	//console.log("searchGridObj",searchGridObj);
	if (typeof searchGridObj[0] != "undefined"){  <%-- 데이터 조회에서 호출 --%>
		_viewType = "2";
		_searchList = parent.$("#objectViewGrid").jsGrid("option", "data");
		
		inputNumber = numberFormat(_searchList.length) // 천단위 콤마 찍는 함수
		$("#search_cnt_title").html(  "( Retrieved Data : "+inputNumber + "건 )"   );  <%-- 데이터 건수 표시 --%>
		<%-- 데이터 조회 후 예측을 할 경우 조회데이터 건수 체크 --%>
		if ( _searchList.length > _limitSearchCnt){
			alert("조회된 데이터가 너무 많습니다<br/><br/>머신러닝분석/Model 예측 메뉴를 이용하세요.");
		}
		
	}else{ <%-- Model예측에서 호출 --%>
		_viewType = "1";
	}
	
	featureGridInit(); <%-- 예측조건 입력 그리드 초기화 --%>
	insertItem({}); <%-- 예측조건 입력 그리드  첫항목 인서트 처리 --%>
	
	getSealTypeInfo1();/* TB_SEAL_TYPE_T_INFO테이블 TYPE = P인 데이터 가져오기  */
	getSealTypeInfo2();/* TB_SEAL_TYPE_T_INFO테이블 TYPE = A인 데이터 가져오기  */
	<%-- 추가버튼 Event --%>
	$("#btn_add_item").click(function(){
		if(	!chkActivityFunc()) return false; //return  값이  false 
		var insert_item = {};
		insertItem(insert_item);
	});
	
	/*TB_SEAL_TYPE_T_INFO 테이블 데이터 가져오기  */

	
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
		
		<%-- 데이터 조회 후 예측을 할 경우 조회데이터 건수 체크 --%>
		if ( _viewType=="2" && _searchList.length > _limitSearchCnt){
			alert("조회된 데이터가 너무 많습니다<br/><br/>머신러닝분석/Model 예측 메뉴를 이용하세요.");
			return false;
		}
	
		<%-- 결과정보 Reset --%>	
		$("#predict_result").empty();
		var predictList = $("#jsGrid_feature").jsGrid("option", "data"); //TODO 입력한 예측 조건값 -  모델의 MIN MAX 값과 비교해야함.
		console.log("predictList 입력한예측값",predictList);
		//console.log("_getFeatureRangeList 모델 비교값",_getFeatureRangeList)
		for (var i=0; i<predictList.length; i++){
			var predictItem = ""; 
			predictItem +="<input type='hidden' id='chkPredict_"+predictList[i].NO+"' value='n' />";
			predictItem +="<div class='card'>";
			predictItem +="<div class='card-header'>";
			predictItem +="<a class='card-link' data-toggle='collapse' data-parent='#predict_result' id='predictbtn"+predictList[i].NO+"'  href='#predict"+predictList[i].NO+"' >";
			predictItem +="Item No : "+predictList[i].NO + " , Pump Type : " + (predictList[i].PUMP_TYPE==undefined?"None":predictList[i].PUMP_TYPE) + " , Product : " + (predictList[i].PRODUCT==undefined?"None":predictList[i].PRODUCT);
			predictItem +="</a><span id='predict_status"+predictList[i].NO+"' style='padding-left:20px;color:#17a2b8;'> Predicting...</span>";
			predictItem +="</div>";
			predictItem +="<div id='predict"+predictList[i].NO+"' class='collapse' >";
			predictItem +="<div class='card-body' style='padding-bottom:10px;padding-top:5px;'>";

			<%-- 예측결과 표시 --%>
			//predictItem +="<div id='predictGrid"+predictList[i].NO+"'>predict...</div>";
			predictItem +="<div class='d-flex' style='padding-top:5px;clear:both;'>";
			predictItem +="<div id='predictGrid1_"+predictList[i].NO+"' style='width:30%'>predict...</div>";
			predictItem +="<div id='predictGrid2_"+predictList[i].NO+"' style='width:30%'></div>";
			predictItem +="<div id='predictGrid3_"+predictList[i].NO+"' style='width:40%'></div>";
			predictItem +="</div>";
			<%-- 예측결과 표시 end --%>
			predictItem +="</div>";
			predictItem +="</div>";
			predictItem +="</div>";
			
			$("#predict_result").append(predictItem);
		}
		<%-- predictMulti(predictList); -- 예측처리 --%>
		predictAll(predictList);  <%-- 예측처리(전체) --%>
	});
	
	<%-- 예측결과 toggle --%>
	$('a[data-toggle="collapse"]').on('click',function(){
		var id=$(this).attr('href');
		if($(id).hasClass('in')){
			$(id).collapse('hide');
		}else{
			$(id).collapse('show');
		}
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
		/* 단위 */
		
		gridRowObj.TEMP_UNIT = $("#select_temperature_unit option:selected").val();
		gridRowObj.TEMP_TEXT= $("#select_temperature_unit option:selected").text();
		
		gridRowObj.VISC_UNIT = $("#select_viscosity_unit option:selected").val();
		gridRowObj.VISC_TEXT = $("#select_viscosity_unit option:selected").text();
		
		gridRowObj.VAP_PRES_UNIT = $("#select_vap_pres_unit option:selected").val();
		gridRowObj.VAP_PRES_TEXT = $("#select_vap_pres_unit option:selected").text();
		
		gridRowObj.SEAL_CHAM_UNIT = $("#select_seal_cham_unit option:selected").val();
		gridRowObj.SEAL_CHAM_TEXT = $("#select_seal_cham_unit option:selected").text();
		
		gridRowObj.RPM_UNIT = $("#select_rpm_unit").val();
		gridRowObj.RPM_TEXT = $("#select_rpm_unit option:selected").text();
		
		gridRowObj.SHAFT_SIZE_UNIT = $("#select_shaft_size_unit").val();
		gridRowObj.SHAFT_SIZE_TEXT = $("#select_shaft_size_unit option:selected").text(); 
		
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
		$('#excelUpload').modal("hide");
	});
	
	<%-- 예측정보 엑셀저장 버튼 Event --%>
	$("#btn_sav_excel_item").click(function(){
		if(	!chkActivityFunc()) return false;
		excelSaveProcess();
	});
	
	$("#featureRangeList").click(function(){
		$('#featureRangeInfo').modal("show");
		rangerListInfo();
	})
	<%-- 예측결과 Show All Event --%>
// 	$("#predictShowAll").click(function(){
// 		$('#predict_result .collapse').addClass('show');
// 	});
	<%-- 예측결과 Hide All Event --%>
// 	$("#predictHideAll").click(function(){
// 		$('#predict_result .collapse').removeClass('show');
// 	});

	<%-- 예측결과 Show/Hide All Event --%>
	$("#predictShowBtn").on('click', function(e) {
		if ($(this).find("i").hasClass("fa-angle-down")) {
			$('#predict_result .collapse').addClass('show');
			$(this).find("span").text("결과모두접기");
			$(this).find("i").removeClass("fa-angle-down");
			$(this).find("i").addClass("fa-angle-up");
		}else {
			$('#predict_result .collapse').removeClass('show');
			$(this).find("span").text("결과모두펼치기");
			$(this).find("i").removeClass("fa-angle-up");
			$(this).find("i").addClass("fa-angle-down");
		}
	});
	
	<%-- 예측인자 Hide/Show Event --%>
	$("#featureCollapseBtn").on('click', function(e) {
		if ($(this).find("i").hasClass("fa-angle-up")) {
			$(this).find("span").text("보이기");
			$(this).find("i").removeClass("fa-angle-up");
			$(this).find("i").addClass("fa-angle-down");
		} else {
			$(this).find("span").text("숨기기");
			$(this).find("i").removeClass("fa-angle-down");
			$(this).find("i").addClass("fa-angle-up");
		}
	});
	
	<%-- 단계 변경 Event --%>
	$("#f_predict_sel").change(function(){
		if ($(this).val() =="OEM"){
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_TEXT", "visible", true);
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_NOR", "visible", true);
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MIN", "visible", true);
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MAX", "visible", true);
			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE_TEXT", "visible", true);
			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE", "visible", true);
			$(".OEM_FEAUTRE").css("display","");
		}else{
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_TEXT", "visible", false);
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_NOR", "visible", false);
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MIN", "visible", false);
			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MAX", "visible", false);
			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE_TEXT", "visible", false);
			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE", "visible", false);
			$(".OEM_FEAUTRE").css("display","none");
		}
	});
	// page open 시 default 
	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_TEXT", "visible", false);
	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_NOR", "visible", false);
	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MIN", "visible", false);
	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MAX", "visible", false);
	$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE_TEXT", "visible", false);
	$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE", "visible", false);
	$(".OEM_FEAUTRE").css("display","none");
	
	
	var option1, option2, option3_1, option3_2, option4, option5;
	<%-- unit select box
		GRU0001	압력
		GRU0002	크기
		GRU0003	온도
		GRU0004	점도
		GRU0005	Speed
	--%>
	<c:forEach var="unit" items="${UNIT_INFO}" varStatus="status">
		<c:if test="${unit.GRP_CODE=='GRU0001'}">
			option3_1 = $("<option value='${unit.UNIT_CODE}'>"+"${unit.UNIT_NAME}"+"</option>");
			$('#select_vap_pres_unit').append(option3_1);
		    option3_2 = $("<option value='${unit.UNIT_CODE}'>"+"${unit.UNIT_NAME}"+"</option>");
		    $('#select_seal_cham_unit').append(option3_2);		    
		</c:if>
		<c:if test="${unit.GRP_CODE=='GRU0003'}">
			option1 = $("<option value='${unit.UNIT_CODE}'>"+"${unit.UNIT_NAME}"+"</option>");
		    $('#select_temperature_unit').append(option1);
		</c:if>
		<c:if test="${unit.GRP_CODE=='GRU0002'}">
			option2 = $("<option value='${unit.UNIT_CODE}'>"+"${unit.UNIT_NAME}"+"</option>");
		    $('#select_shaft_size_unit').append(option2);
		</c:if>
		<c:if test="${unit.GRP_CODE=='GRU0004'}">
			option4 = $("<option value='${unit.UNIT_CODE}'>"+"${unit.UNIT_NAME}"+"</option>");
		    $('#select_viscosity_unit').append(option4);
		</c:if>
		<c:if test="${unit.GRP_CODE=='GRU0005'}">
			option5 = $("<option value='${unit.UNIT_CODE}'>"+"${unit.UNIT_NAME}"+"</option>");
		    $('#select_rpm_unit').append(option5);
		</c:if>
	</c:forEach>
	
	getFeatureRangeList(); //0323 model의 getFeatureRangeList. TB_ML_MODEL_FEATURE_RANGE값 가져오기
	
	
    
}); <%-- end  $(document).ready(function(){ --%>
//url:"<c:url value='/ml/predictWithMulti.do'/>",
function setFeatureGrid(FEATURE_COL,predictItem,predictItem_C){ //0323 예측결과 예측값 인자 표시해주는 Grid를 그려주는 부분 MIN보다 작을시 파란색 MAX보다 클경우 빨간색.
 																//0408 predictItem_C 단위변환된 값. 이 값을 비교값으로 사용.
	for(j=0; j<_getFeatureRangeList.result.length; j++){ 
		if(_getFeatureRangeList.result[j].FEATURE_COL == FEATURE_COL){ //비교할 대상 값
			if(FEATURE_COL == "SHAFT_SIZE" && _getFeatureRangeList.result[j].FEATURE_COL == "SHAFT_SIZE"){ //SHAFT_SIZE만 min-width:100px; style 적용.
				if(predictItem_C < _getFeatureRangeList.result[j].MIN_VAL){ 
					predictItem =" <div class='predict-Feature' style='color:blue;min-width:100px;font-weight:bold;'>"+predictItem+"</div>";
				}else if(predictItem_C > _getFeatureRangeList.result[j].MAX_VAL){  
					predictItem =" <div class='predict-Feature' style='color:red;min-width:100px;font-weight:bold;'>"+predictItem+"</div>";
				}else{ 
					predictItem =" <div class='predict-Feature' style='min-width:100px'>"+predictItem+"</div>";
				}	
			}else{ //그외 나머지 
				if(predictItem_C < _getFeatureRangeList.result[j].MIN_VAL){ // 예측값이 모델에 저장된 MIN값보다 작을때
					predictItem =" <div class='predict-Feature' style='color:blue;font-weight:bold;'>"+predictItem+"</div>";
				}else if(predictItem_C > _getFeatureRangeList.result[j].MAX_VAL){ // 예측값이 모델에 저장된 MAX값보다 클때 
					predictItem =" <div class='predict-Feature' style='color:red;font-weight:bold;'>"+predictItem+"</div>";
				}else{ //예측값이 모델에 저장된 MIN ~ MAX 값 사이일때
					predictItem =" <div class='predict-Feature'>"+predictItem+"</div>";
				}	
			}
				
		}
	
	} 
	return predictItem ;
}

function getFeatureRangeList(){ //0323 생성된 model의 FeatureRangeList구하기 예측할 FeatureList값과 비교하기위해서.
	$.ajax({
		type:'GET',
		url: '/ml/getFeatureRangeList.do',
	}).done(function(result){
		FeatureRangeList = result;
		console.log("Model FeatureRangeList:",FeatureRangeList);
		_getFeatureRangeList = FeatureRangeList
	})
}
var sealTypeList_P = new Array();
function getSealTypeInfo1(){
	$.ajax({
		type:'GET',
		url: '/ml/getSealTypeInfo1.do',
	}).done(function(result){ 
		sealTypeList_P = result;
	})
}

var sealTypeList_A = new Array();
function getSealTypeInfo2(){
	$.ajax({
		type:'GET',
		url: '/ml/getSealTypeInfo2.do',
	}).done(function(result){ //
		sealTypeList_A = result;
	})
}

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
	//console.log("insertObj",insertObj);
	$("#jsGrid_feature").jsGrid("insertItem",insertObj);
}

function numberFormat(inputNumber) { //천단위 콤마 찍는 함수
	   return inputNumber.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
	}
function rangerListInfo(){
	
	console.log("FeatureRangeList",FeatureRangeList);
	for(i=0; i<FeatureRangeList.result.length; i++){
			var numberFormat_max_val = numberFormat(FeatureRangeList.result[i].MAX_VAL)
			var numberFormat_min_val = numberFormat(FeatureRangeList.result[i].MIN_VAL)
			//$("#feature_col"+i).val(FeatureRangeList.result[i].FEATURE_COL);
			if(FeatureRangeList.result[i].FEATURE_COL =="TEMP_NOR"){
				$("#feature_col"+i).val("Temperature Rtd.");
				$("#unit_val"+i).val("℃")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="TEMP_MIN"){
				$("#feature_col"+i).val("Temperature Min");
				$("#unit_val"+i).val("℃")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="TEMP_MAX"){
				$("#feature_col"+i).val("Temperature Max");
				$("#unit_val"+i).val("℃")
			}else if(FeatureRangeList.result[i].FEATURE_COL == "VISC_NOR"){
				$("#feature_col"+i).val("Viscosity Rtd.");
				$("#unit_val"+i).val("CP")
			}else if(FeatureRangeList.result[i].FEATURE_COL == "VISC_MIN"){
				$("#feature_col"+i).val("Viscosity Min");
				$("#unit_val"+i).val("CP")
			}else if(FeatureRangeList.result[i].FEATURE_COL == "VISC_MAX"){
				$("#feature_col"+i).val("Viscosity Max");
				$("#unit_val"+i).val("CP")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="VAP_PRES_NOR"){
				$("#feature_col"+i).val("Vapor Pressure Rtd.");
				$("#unit_val"+i).val("BARA")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="VAP_PRES_MIN"){
				$("#feature_col"+i).val("Vapor Pressure Min");
				$("#unit_val"+i).val("BARA")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="VAP_PRES_MAX"){
				$("#feature_col"+i).val("Vapor Pressure Max");
				$("#unit_val"+i).val("BARA")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="SPEC_GRAVITY_NOR"){
				$("#feature_col"+i).val("Specific Gravity Rtd")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="SPEC_GRAVITY_MIN"){
				$("#feature_col"+i).val("Specific Gravity Min")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="SPEC_GRAVITY_MAX"){
				$("#feature_col"+i).val("Specific Gravity Max")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="SEAL_CHAM_NOR"){
				$("#feature_col"+i).val("Seal Chamber Pressure Rtd.")
				$("#unit_val"+i).val("BARG")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="SEAL_CHAM_MIN"){
				$("#feature_col"+i).val("Seal Chamber Pressure Min")
				$("#unit_val"+i).val("BARG")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="SEAL_CHAM_MAX"){
				$("#feature_col"+i).val("Seal Chamber Pressure Max")
				$("#unit_val"+i).val("BARG")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="RPM_NOR"){
				$("#feature_col"+i).val("Shaft Speed Rtd.")
				$("#unit_val"+i).val("RPM")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="RPM_MIN"){
				$("#feature_col"+i).val("Shaft Speed Min")
				$("#unit_val"+i).val("RPM")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="RPM_MAX"){
				$("#feature_col"+i).val("Shaft Speed Max")
				$("#unit_val"+i).val("RPM")
			}else if(FeatureRangeList.result[i].FEATURE_COL =="SHAFT_SIZE"){
				$("#feature_col"+i).val("Shaft Dia.")
				$("#unit_val"+i).val("MM")
			}
			
			$("#max_val"+i).val(numberFormat_max_val);
			$("#min_val"+i).val(numberFormat_min_val);
			
			$("#feature_col"+i).attr("readonly",true);
			$("#max_val"+i).attr("readonly",true);
			$("#min_val"+i).attr("readonly",true);
			
	} 
	
	if (($(document).height()-80) >= 700 ){
   		$('#featureRangeInfo .modal-content').css("height","820px");
	}else{
		$('#featureRangeInfo .modal-content').css("height",($(document).height()-80)+"px");	
	} 
	
}

// feature grid init
function featureGridInit(){
	$("#jsGrid_feature").jsGrid('destroy');
	$("#jsGrid_feature").jsGrid({
		width: "100%",
	    height: "200px",
	    editing: true, //수정 기본처리
	    sorting: false, //정렬
	    paging: false, //조회행넘어가면 페이지버튼 뜸
	    loadMessage : "Now Loading...",
	    fields: [
	    	{name : "NO",title : "No.",type : "text",align : "center",width : 50, css:"font-size-down" },	
	    	{name : "PUMP_TYPE",title : "Pump Type",type : "text",align : "center",width : 80, css:"font-size-down" },
	    	{name : "PRODUCT",title : "Product",type : "text",align : "left",width : 250, css:"font-size-down"},
	    	{name : "TEMP_TEXT",title : "Temp.<br/>단위",type : "text", align : "center",width : 80, css:"font-size-down"}, // 
	    	{name : "TEMP_NOR",title : "Temp.<br/>Rtd",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "TEMP_MIN",title : "Temp.<br/>Min",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "TEMP_MAX",title : "Temp.<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "SPEC_GRAVITY_NOR",title : "S.G.<br/>Rtd",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SPEC_GRAVITY_MIN",title : "S.G.<br/>Min",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SPEC_GRAVITY_MAX",title : "S.G.<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VISC_TEXT",title : "Visc<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "VISC_NOR",title : "Visc<br/>Rtd",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VISC_MIN",title : "Visc<br/>Min",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VISC_MAX",title : "Visc<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VAP_PRES_TEXT",title : "Vapor<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "VAP_PRES_NOR",title : "Vapor<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "VAP_PRES_MIN",title : "Vapor<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "VAP_PRES_MAX",title : "Vapor<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_TEXT",title : "Seal Cham.<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_NOR",title : "Seal Cham.<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_MIN",title : "Seal Cham.<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_MAX",title : "Seal Cham.<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "RPM_TEXT",title : "Shaft Speed<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "RPM_NOR",title : "Shaft Speed<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "RPM_MIN",title : "Shaft Speed<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "RPM_MAX",title : "Shaft Speed<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SHAFT_SIZE_TEXT",title : "Shaft  dia.<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SHAFT_SIZE",title : "Shaft dia.",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	//UNIT (CODE= VALUE)
	    	{name : "TEMP_UNIT",title : "Temp.<br/>단위",type : "text", align : "center",width : 80, css:"font-size-down", visible : false}, 
	    	{name : "VISC_UNIT",title : "Visc<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "VAP_PRES_UNIT",title : "Vapor<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "SEAL_CHAM_UNIT",title : "Seal Cham.<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "RPM_UNIT",title : "Shaft Speed<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "SHAFT_SIZE_UNIT",title : "Shaft  dia.<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	
	    	{name : "TEMP_NOR_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "TEMP_MIN_C",title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "TEMP_MAX_C",title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "SPEC_GRAVITY_NOR_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "SPEC_GRAVITY_MIN_C",title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "SPEC_GRAVITY_MAX_C",title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "VISC_NOR_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "VISC_MIN_C",title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "VISC_MAX_C",title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "VAP_PRES_NOR_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "VAP_PRES_MIN_C",title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "VAP_PRES_MAX_C",title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "SEAL_CHAM_NOR_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "SEAL_CHAM_MIN_C",title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "SEAL_CHAM_MAX_C",title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "RPM_NOR_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "RPM_MIN_C",title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "RPM_MAX_C",title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "SHAFT_SIZE_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	
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
	    headerRowRenderer: function() {
	    	
	        var $result = $("<tr>").height(0)
	          	.append($("<th>").attr("rowspan", 2).width(50).text("No.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(80).text("Pump Type").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Product").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("colspan", 4).width(320).text("Temperature").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	        	.append($("<th>").attr("colspan", 3).width(240).text("Specific Gravity").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("colspan", 4).width(320).text("Viscosity").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("colspan", 4).width(320).text("Vapor Press").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("colspan", 4).width(320).text("Seal Chamber Press").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th class='OEM_FEAUTRE'>").attr("colspan", 4).width(320).text("Shaft Speed").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th class='OEM_FEAUTRE'>").attr("colspan", 2).width(160).text("Shaft Size").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(80).text("Delete").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            ;
	            
	        $result = $result.add($("<tr>")
	        		.append($("<th>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	        		.append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
					.append($("<th>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Size").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            );
	    	return $result;
	
	    }
	    ,
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
		  	//console.log(args);
		   
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
	     	
	       	if ($("#f_predict_sel").val() == "OEM"){
	       		$("#f_rpm_nor").val(args.item.RPM_NOR);
		       	$("#f_rpm_min").val(args.item.RPM_MIN);
		       	$("#f_rpm_max").val(args.item.RPM_MAX);
		       	$("#f_shaft_size").val(args.item.SHAFT_SIZE);
		       	 
	       	}else{
	       		$("#f_rpm_nor").val("");
		       	$("#f_rpm_min").val("");
		       	$("#f_rpm_max").val("");
		       	$("#f_shaft_size").val("");	     
		       	
	       	}
	       		       	
	       	//$('#featureEdit .modal-content').css("height","300px");
	       	if (($(document).height()-80) >= 700 ){
	       		$('#featureEdit .modal-content').css("height","900px");
			}else{
				$('#featureEdit .modal-content').css("height",($(document).height()-80)+"px");	
			}
	       	
	       	
	       	//단계에 따른 항목체크
	     	if ($("#f_predict_sel").val() == "OEM"){
	       		$("#f_rpm_nor, #f_rpm_min, #f_rpm_max, #f_shaft_size, #select_rpm_unit, #select_shaft_size_unit").attr("disabled",false);
	       	}else{
	       		$("#f_rpm_nor, #f_rpm_min, #f_rpm_max, #f_shaft_size, #select_rpm_unit, #select_shaft_size_unit").attr("disabled",true);
	       	}
	       	
	       	// 기준단위
	       	var vBaseUnitTemp="", vBaseUnitVisc="", vBaseUnitVapPres="",  vBaseUnitSealCham="", vBaseUnitRPM="", vBaseUnitShaftSize="";
	     	<c:forEach var="unit" items="${UNIT_DEFAULT_INFO}" varStatus="status">
			if("RPM_NOR" == "${unit.COL_NAME}"){
				vBaseUnitRPM="${unit.BASE_COL}";
			}else if ("TEMP_NOR" == "${unit.COL_NAME}"){
				vBaseUnitTemp="${unit.BASE_COL}";
			}else if ("SHAFT_SIZE" == "${unit.COL_NAME}"){
				vBaseUnitShaftSize="${unit.BASE_COL}";
			}else if ("VISC_NOR" == "${unit.COL_NAME}"){
				vBaseUnitVisc="${unit.BASE_COL}";
			}else if ("VAP_PRES_NOR" == "${unit.COL_NAME}"){
				vBaseUnitVapPres="${unit.BASE_COL}";
			}else if ("SEAL_CHAM_NOR" == "${unit.COL_NAME}"){
				vBaseUnitSealCham="${unit.BASE_COL}";
			}
		   </c:forEach>   
	       	
	     	if(args.item.TEMP_UNIT == null){
	     		$("#select_temperature_unit").val(vBaseUnitTemp);
	     	}else{
	     		$("#select_temperature_unit").val(args.item.TEMP_UNIT);
	     	}
	     	
	     	if(args.item.VISC_UNIT == null){
	     		$("#select_viscosity_unit").val(vBaseUnitVisc);
	     	}else{
	     		$("#select_viscosity_unit").val(args.item.VISC_UNIT);
	     	}
	     	
	     	if(args.item.VAP_PRES_UNIT == null){
	     		$("#select_vap_pres_unit").val(vBaseUnitVapPres);
	     	}else{
	     		$("#select_vap_pres_unit").val(args.item.VAP_PRES_UNIT);
	     	}
	     	
	     	if(args.item.SEAL_CHAM_UNIT == null){
	     		$("#select_seal_cham_unit").val(vBaseUnitSealCham);
	     	}else{
	     		$("#select_seal_cham_unit").val(args.item.SEAL_CHAM_UNIT);
	     	}
	     	
	     	if(args.item.RPM_UNIT == null){
	     		$("#select_rpm_unit").val(vBaseUnitRPM);
	     	}else{
	     		$("#select_rpm_unit").val(args.item.RPM_UNIT);
	     	}
	     	
	     	if(args.item.SHAFT_SIZE_UNIT == null){
	     		$("#select_shaft_size_unit").val(vBaseUnitShaftSize);
	     	}else{
	     		$("#select_shaft_size_unit").val(args.item.SHAFT_SIZE_UNIT);
	     	}
	   
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
	     	//loadingMsg.modal('hide');
	     	setTimeout(function() {
	     		loadingMsg.modal('hide');
	     	},500);
		 }
	}).done(function(data){
		//console.log("data set:",data);
		//var FeatureRangeList = data.FEATURERESULT; //기존 모델의 범위rangerList
		
		// data set
		//setPredictData("predictGrid"+data.predict_idx, data.RESULT);
		
		var presult = data.RESULT;
		
		if( presult == null ){
			alert("처리중 오류가 발생하였습니다");
			return false;
		}
		
		if (typeof presult.ERR_MSG != "undefined"){
			alert(presult.ERR_MSG);
			return false;
		}
		
		
		console.log("예측결과presult :",presult);
		for(var i=0;i<presult.length;i++){

			// 예측결과 그리드 Set
			setPredictData("predictGrid1_"+presult[i].predict_idx, presult[i].RESULT.SEAL_TYPE, "Seal Type");
			setPredictData("predictGrid2_"+presult[i].predict_idx, presult[i].RESULT.API_PLAN, "API Plan");
			setPredictData("predictGrid3_"+presult[i].predict_idx, presult[i].RESULT.CONN_COL, "Seal Type|Material|API Plan"); //3번째 그리드 데이터부분
			
			var featureGridDatas = $("#jsGrid_feature").jsGrid("option", "data");
			console.log("featureGridDatas",featureGridDatas);
			var featureGridData = null;
    		for(var k=0;k<featureGridDatas.length;k++){
    			if (presult[i].predict_idx == featureGridDatas[k].NO){
    				featureGridData = featureGridDatas[k]; 
    				featureGridData.TEMP_NOR_C = presult[i].param_cnv.TEMP_NOR;
    				featureGridData.TEMP_MIN_C = presult[i].param_cnv.TEMP_MIN;
    				featureGridData.TEMP_MAX_C = presult[i].param_cnv.TEMP_MAX;
    				featureGridData.SPEC_GRAVITY_NOR_C = presult[i].param_cnv.SPEC_GRAVITY_NOR;
    				featureGridData.SPEC_GRAVITY_MIN_C = presult[i].param_cnv.SPEC_GRAVITY_MIN;
    				featureGridData.SPEC_GRAVITY_MAX_C = presult[i].param_cnv.SPEC_GRAVITY_MAX
    				featureGridData.VISC_NOR_C = presult[i].param_cnv.VISC_NOR;
    				featureGridData.VISC_MIN_C = presult[i].param_cnv.VISC_MIN;
    				featureGridData.VISC_MAX_C = presult[i].param_cnv.VISC_MAX;
    				featureGridData.VAP_PRES_NOR_C = presult[i].param_cnv.VAP_PRES_NOR;
    				featureGridData.VAP_PRES_MIN_C = presult[i].param_cnv.VAP_PRES_MIN;
    				featureGridData.VAP_PRES_MAX_C = presult[i].param_cnv.VAP_PRES_MAX;
    				featureGridData.SEAL_CHAM_NOR_C = presult[i].param_cnv.SEAL_CHAM_NOR;
    				featureGridData.SEAL_CHAM_MIN_C = presult[i].param_cnv.SEAL_CHAM_MIN;
    				featureGridData.SEAL_CHAM_MAX_C = presult[i].param_cnv.SEAL_CHAM_MAX;
    				featureGridData.RPM_NOR_C = presult[i].param_cnv.RPM_NOR;
    				featureGridData.RPM_MIN_C = presult[i].param_cnv.RPM_MIN;
    				featureGridData.RPM_MAX_C = presult[i].param_cnv.RPM_MAX;
    				featureGridData.SHAFT_SIZE_C = presult[i].param_cnv.SHAFT_SIZE;
    				break;
    			}
    		}
    		$("#jsGrid_feature").jsGrid("refresh");
    		
    	
    		<%-- 예측인자 표시 추가 --%>
    		var predictItem = "";
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> Product Grouping. ";
			predictItem +=" <div class='d-flex'>";
			predictItem +=" <div class='predict-Feature'>"+"</div>";
			predictItem +=" <div class='predict-Feature' id='ProductGroup"+i+"'>"+"</div>";
			predictItem +=" <div class='predict-Feature'>"+""+"</div>";
			predictItem +=" </div></div>";
			
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> Temp. ("+featureGridData.TEMP_TEXT+") ";
			predictItem +=" <div class='d-flex'>";
			/*입력한 예측값과 모델의 MIN , MAX 값을 비교  */
			predictItem +=""+setFeatureGrid("TEMP_NOR",featureGridData.TEMP_NOR,featureGridData.TEMP_NOR_C)+""; //해당부분을 공통함수로 빼서 정리해보기.
			predictItem +=""+setFeatureGrid("TEMP_MIN",featureGridData.TEMP_MIN,featureGridData.TEMP_MIN_C)+""; 
			predictItem +=""+setFeatureGrid("TEMP_MAX",featureGridData.TEMP_MAX,featureGridData.TEMP_MAX_C)+"";  
			predictItem +=" </div></div>";
			
			//predictItem +=" <div class='predict-Feature'>"+predictList[i].TEMP_NOR+"</div>";
			//predictItem +=" <div class='predict-Feature'>"+predictList[i].TEMP_MIN+"</div>";
			//predictItem +=" <div class='predict-Feature'>"+predictList[i].TEMP_MAX+"</div>";
			//predictItem +=" </div></div>"; //비교하기전 원본은 이런식으로 뿌려주기만했었음. 
		
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> S.G. ";
			predictItem +=" <div class='d-flex'>";
			predictItem +=""+setFeatureGrid("SPEC_GRAVITY_NOR",featureGridData.SPEC_GRAVITY_NOR,featureGridData.SPEC_GRAVITY_NOR_C)+""; 
			predictItem +=""+setFeatureGrid("SPEC_GRAVITY_MIN",featureGridData.SPEC_GRAVITY_MIN,featureGridData.SPEC_GRAVITY_MIN_C)+""; 
			predictItem +=""+setFeatureGrid("SPEC_GRAVITY_MAX",featureGridData.SPEC_GRAVITY_MAX,featureGridData.SPEC_GRAVITY_MAX_C)+"";
			predictItem +=" </div></div>";
			
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> Viscosity ("+featureGridData.VISC_TEXT+") ";
			predictItem +=" <div class='d-flex'>";
			predictItem +=""+setFeatureGrid("VISC_NOR",featureGridData.VISC_NOR,featureGridData.VISC_NOR_C)+""; 
			predictItem +=""+setFeatureGrid("VISC_MIN",featureGridData.VISC_MIN,featureGridData.VISC_MIN_C)+""; 
			predictItem +=""+setFeatureGrid("VISC_MAX",featureGridData.VISC_MAX,featureGridData.VISC_MAX_C)+"";
			predictItem +=" </div></div>";
		
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> Vapor Pres. ("+featureGridData.VAP_PRES_TEXT+") ";
			predictItem +=" <div class='d-flex'>";
			predictItem +=""+setFeatureGrid("VAP_PRES_NOR",featureGridData.VAP_PRES_NOR,featureGridData.VAP_PRES_NOR_C)+""; 
			predictItem +=""+setFeatureGrid("VAP_PRES_MIN",featureGridData.VAP_PRES_MIN,featureGridData.VAP_PRES_MIN_C)+""; 
			predictItem +=""+setFeatureGrid("VAP_PRES_MAX",featureGridData.VAP_PRES_MAX,featureGridData.VAP_PRES_MAX_C)+"";
			predictItem +=" </div></div>";
			
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> Seal Cham. ("+featureGridData.SEAL_CHAM_TEXT+") ";
			predictItem +=" <div class='d-flex'>";
			predictItem +=""+setFeatureGrid("SEAL_CHAM_NOR",featureGridData.SEAL_CHAM_NOR,featureGridData.SEAL_CHAM_NOR_C)+""; 
			predictItem +=""+setFeatureGrid("SEAL_CHAM_MIN",featureGridData.SEAL_CHAM_MIN,featureGridData.SEAL_CHAM_MIN_C)+""; 
			predictItem +=""+setFeatureGrid("SEAL_CHAM_MAX",featureGridData.SEAL_CHAM_MAX,featureGridData.SEAL_CHAM_MAX_C)+"";
			predictItem +=" </div></div>"; 
		
			if ($("#f_predict_sel").val()=="OEM"	){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Shaft Speed ("+featureGridData.RPM_TEXT+") ";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGrid("RPM_NOR",featureGridData.RPM_NOR,featureGridData.RPM_NOR_C)+""; 
				predictItem +=""+setFeatureGrid("RPM_MIN",featureGridData.RPM_MIN,featureGridData.RPM_MIN_C)+""; 
				predictItem +=""+setFeatureGrid("RPM_MAX",featureGridData.RPM_MAX,featureGridData.RPM_MAX_C)+"";
				predictItem +=" </div></div>";
				
			
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Shaft Dia.("+featureGridData.SHAFT_SIZE_TEXT+") ";
				predictItem +=" <div class='d-flex'>"
				predictItem +=""+setFeatureGrid("SHAFT_SIZE",featureGridData.SHAFT_SIZE,featureGridData.SHAFT_SIZE_C)+"";
				predictItem +=" </div></div>";
			}
			<%-- 예측인자 표시 추가 end --%>
			$("#predict"+presult[i].predict_idx +" .card-body").prepend(predictItem); // 입력인자 결과 화면에 표시
			$("#ProductGroup"+i).text(presult[i].ProductGroupInfo); //예측조건에 입력한 Product Grouping 정보
    		
			var conn_Col = presult[i].RESULT.CONN_COL; //조회결과 예측값
			
			//console.log("conn_Col:",conn_Col);
			if(conn_Col != undefined ){ //conn_Col.length 로 체크하게되면 undefined인 값에는 length 속성 자체가 없기때문에 오류가 발생한다.  
				for(k=0; k < conn_Col.length; k++){ //seal type 분리용 
					var gridRowObj = $('#predictGrid3_'+presult[i].predict_idx).jsGrid('option', 'data')[k]; //k번째의 row를  gridRowObj선언.
					var	sealType_0 = conn_Col[k].CLASS.split("|") 
					var trimsealType_0 = $.trim(sealType_0[0]); //공백이 존재하여 제거필요
					 for(z = 0; z<sealTypeList_P.result.length; z++){  
						 if(trimsealType_0 == sealTypeList_P.result[z].SEAL_TYPE){
							//console.log(sealTypeList_P.result[z]);	
							if(sealTypeList_P.result[z].TYPE = "P"){
								gridRowObj.SEALTYPE_P ='<i class="fas fa-check-circle" style="color:blue;"></i>'; //k번째의 row에 값 셋팅.
								$('#predictGrid3_'+presult[i].predict_idx).jsGrid("refresh"); 	
							}
						}
					}
					for(j = 0; j<sealTypeList_A.result.length; j++){
						if(trimsealType_0 == sealTypeList_A.result[j].SEAL_TYPE){
							//console.log(sealTypeList_A.result[j]);	
							if(sealTypeList_A.result[j].TYPE = "A"){
								gridRowObj.SEALTYPE_A ='<i class="fas fa-check-circle" style="color:red;"></i>'; //k번째의 row에 값 셋팅.
								$('#predictGrid3_'+presult[i].predict_idx).jsGrid("refresh"); 	
							}
						}
					}
				} 
			}  
			
			
			
			if($.trim(presult[i].predict_msg) == "complete"){
				$("#predict_status"+presult[i].predict_idx).css("color","#fd7e14");//complete
				$("#predictbtn"+presult[i].predict_idx).trigger("click");
				//$("#predictbtn"+(i+1)).trigger("click");//btn
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
		//loadingMsg.modal("hide");
		
	}).fail(function(jqXHR, textStatus, errorThrown){
		//loadingMsg.modal('hide');	
		//$("#chkPredict_"+idx).val("y");  현재진행중인 예측완료처리 
		var eMsg = "";
		if(jqXHR.status === 400){
			eMsg="요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오";
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
		url:"<c:url value='/ml/predictWithMulti.do'/>",
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
		console.log("data",data);
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
	//console.log("result Set - gridId : " + gridId);		
	if(data ==null) data = "";
	
	$("#"+gridId).jsGrid('destroy');
	$("#"+gridId).jsGrid({
    	width: "100%",
        height: "180px",
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
            
        	{name : "NO",title : "No.",type : "number",align : "center",width : 20, css:"font-size-down",editing: false },
        	{name : "SEALTYPE_P",title : "Preferred",type : "text",align : "center",width : 40,visible:(className=="Seal Type|Material|API Plan"?true:false), css:"font-size-down",editing: false }, //,visible:(className=="sealTypeList"?true:false)
        	{name : "SEALTYPE_A",title : "API682",type : "text",align : "center",width : 40,visible:(className=="Seal Type|Material|API Plan"?true:false), css:"font-size-down",editing: false }, //,visible:(className=="sealTypeList"?true:false)
        	{name : "CLASS",title : className, type : "text",align : "center",width : 130, css:"font-size-down",editing: false },	
        	{name : "PROB",title : "확률(%)",type : "number",width : 40,align : "center",css:"font-size-down",editing: false, format:"#,##0.000"}
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
	     	//loadingMsg.modal('hide');
	     	setTimeout(function() {
	     		loadingMsg.modal('hide');
	     	},500);
	    },
        success: function (result) {
			
        	$("#jsGrid_feature").jsGrid("option", "data", []); // 초기화	
        	<%-- Excel Data Insert  --%>
            var elist = result.excelDataList;
            if(elist == null){
            	alert("처리할 데이터가 없거나 오류가 발생하였습니다");
            	loadingMsg.modal('hide');
            }
            var insert_items = new Array();
            for (var i=0;i<elist.length;i++){
            	var insert_item = {
            			NO : elist[i].NO,
            			PUMP_TYPE : elist[i].PUMP_TYPE,
            			PRODUCT : elist[i].PRODUCT,
            			SPEC_GRAVITY_NOR : elist[i].SPEC_GRAVITY,
            			SPEC_GRAVITY_MIN : elist[i].SPEC_GRAVITY,
            			SPEC_GRAVITY_MAX : elist[i].SPEC_GRAVITY,
            			VISC_UNIT : elist[i].VISC_UNIT,
            			VISC_TEXT : elist[i].VISC_TEXT,
            			VISC_NOR : elist[i].VISC,
            			VISC_MIN : elist[i].VISC,
            			VISC_MAX : elist[i].VISC,
            			TEMP_UNIT : elist[i].TEMP_UNIT,
            			TEMP_TEXT : elist[i].TEMP_TEXT,
            			TEMP_NOR : elist[i].TEMP_NOR,
            			TEMP_MIN : elist[i].TEMP_MIN,
            			TEMP_MAX : elist[i].TEMP_MAX,
            			VAP_PRES_UNIT : elist[i].VAP_PRES_UNIT,
            			VAP_PRES_TEXT : elist[i].VAP_PRES_TEXT,
            			VAP_PRES_NOR : elist[i].VAP_PRES,
            			VAP_PRES_MIN : elist[i].VAP_PRES,
            			VAP_PRES_MAX : elist[i].VAP_PRES,
            			SUCT_PRES_UNIT : elist[i].SUCT_PRES_UNIT,
            			SUCT_PRES_NOR : elist[i].SUCT_PRES_NOR,
            			SUCT_PRES_MIN : elist[i].SUCT_PRES_MIN,
            			SUCT_PRES_MAX : elist[i].SUCT_PRES_MAX,
            			DISCH_PRES_UNIT : elist[i].DISCH_PRES_UNIT,
            			DISCH_PRES_NOR : elist[i].DISCH_PRES_NOR,
            			DISCH_PRES_MIN : elist[i].DISCH_PRES_MIN,
            			DISCH_PRES_MAX : elist[i].DISCH_PRES_MAX,
            			SEAL_CHAM_UNIT : elist[i].SEAL_CHAM_UNIT,
            			SEAL_CHAM_TEXT : elist[i].SEAL_CHAM_TEXT,
            			SEAL_CHAM_NOR : elist[i].SEAL_CHAM_NOR,
            			SEAL_CHAM_MIN : elist[i].SEAL_CHAM_MIN,
            			SEAL_CHAM_MAX : elist[i].SEAL_CHAM_MAX,
            			RPM_UNIT : elist[i].RPM_UNIT,
            			RPM_TEXT : elist[i].RPM_TEXT,
            			RPM_NOR : elist[i].RPM,
            			RPM_MIN : elist[i].RPM,
            			RPM_MAX : elist[i].RPM,
            			SHAFT_SIZE_UNIT : elist[i].SHAFT_SIZE_UNIT,
            			SHAFT_SIZE_TEXT : elist[i].SHAFT_SIZE_TEXT,
            			SHAFT_SIZE : elist[i].SHAFT_SIZE
            	}
            	insert_items.push(insert_item);
            	//insertItem(insert_item);	
            } // end for
            //console.log("afadfsfs : " + insert_items);
            //insertItem(insert_items);
            $("#jsGrid_feature").jsGrid("option", "data", insert_items) //값을 가진 배열로(insert_items) 그리드를 그린다.
            //JSON.stringify(insert_items)
            
            
            <%-- upload file info Set --%>
            _fileUploadResult = result.fileUploadResult; //업로드한 엑셀파일을 전역변수에 넣어준다.
            /*
            result.fileUploadResult.file_group_id
            result.fileUploadResult.file_name
            result.fileUploadResult.file_name_org
            result.fileUploadResult.file_path
            result.fileUploadResult.result
            */
            
            // remove 예측결과
            $('#predict_result').empty();
            
            //loadingMsg.modal('hide');
            //$('#excelUpload').modal("hide");
            //alert("처리되었습니다");
            
        },fail : function (jqXHR, textStatus, errorThrown){
			//loadingMsg.modal('hide');
			//ajaxFailMsg(result);
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
		}
    });
}

<%--
	예측결과 -> 엑셀파일 : 저장
--%>
function excelSaveProcess(){
	//console.log("_fileUploadResult(업로드한엑셀명)",_fileUploadResult);
	 //console.log("_getFeatureRangeList",_getFeatureRangeList);
	//기본template 복사
	if(_fileUploadResult==null){ //직접입력할경우
		var vItemIdx = 0;
		var vFeatureList = $('#jsGrid_feature').jsGrid('option', 'data'); // Feautre List 예측조건 값
		console.log("vFeatureList",vFeatureList);
		var predictArr = new Array();
		for (var i=0;i<vFeatureList.length;i++) { 
			var classVal = "";
			//seal type +material+ api plan중 체크된 항목을 찾는다.
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
			predictArr.push(classVal); //확률이 가장 높은 checked 된 값.
		}
		//console.log("예측결과",predictArr);
		var loadingMsg = new loading_bar({message:"excel create & Result Save ..."});
		$.ajax({
	        url: '/ml/predictInfoToExcelFile2.do', //조회한 예측값을 엑셀로 다운받기.
	        type: 'POST',
	        data: JSON.stringify({
	        	predictInfo : predictArr, // 예측결과 grid 데이터 값.
	        	FeatureList : vFeatureList, //예측 조건 grid 값 인자값
	        	FeatureRangeList : _getFeatureRangeList.result // model의 Feature 최대 최소값.
	        }),
	        async:true,
	        processData: false,
	        contentType: 'application/json',
	        beforeSend: function () {
	        	loadingMsg.show();
	        },
	        complete: function () {
		     	//loadingMsg.modal('hide');
		     	setTimeout(function() {
		     		loadingMsg.modal('hide');
		     	},500);
		    },
		    success: function (result) { //result 생성된 엑셀파일명
	        	//loadingMsg.modal('hide');
	        	console.log("result",result);
		        if(result.result !== null){
		        	var vFileParam = $.param({
		        			"file_name" : result.result
		        		});
		        	$("#fileDownFrame").attr("src", "<c:url value="/ml/predictApplyExcelFileDownload2.do" />?"+vFileParam); //? 사용하여 파일명을 GET방식 parameter(vFileParam = 생성된 엑셀 파일명) 넘김.
		        }else{
		        	alert("처리중 오류가 발생하였습니다.");
		        }
	        }, 
	        fail : function (result) {
				//loadingMsg.modal('hide');
				//ajaxFailMsg(result);
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
			}
		});
		
	}else if(_fileUploadResult!==null){
		// 예측결과를 넘겨서 업로드한 파일에 Write
		var vItemIdx = 0;
		var vFeatureList = $('#jsGrid_feature').jsGrid('option', 'data'); // 예측조건인자값
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
	    
			// 예측값
			predictArr.push(classVal);
		} // end for
		
		var loadingMsg = new loading_bar({message:"Result Save ..."});
		
		$.ajax({
	        url: '/ml/predictInfoToExcelFile.do', //업로드한 엑셀파일에 예측값을 추가
	        type: 'POST',
	        data: JSON.stringify({
	        	fileInfo : _fileUploadResult, //upload excel
	        	predictInfo : predictArr, //예측결과
	        	FeatureList : vFeatureList,
	        	FeatureRangeList : _getFeatureRangeList.result // model의 Feature 최대 최소값.
	        }),
	        async:true,
	        processData: false,
	        contentType: 'application/json',
	        beforeSend: function () {
	        	loadingMsg.show();
	        },
	        complete: function () {
		     	//loadingMsg.modal('hide');
		     	setTimeout(function() {
		     		loadingMsg.modal('hide');
		     	},500);
		    },
	        success: function (result) {
	        	//loadingMsg.modal('hide');
	        	console.log("result",result);
		        if(result.result == "ok"){  
		        	var vFileParam = $.param({
		            		"file_name"        : _fileUploadResult.file_name,
		           	        "file_name_org": _fileUploadResult.file_name_org,
		           	        "file_path"          : _fileUploadResult.file_path
		           	});
		        	$("#fileDownFrame").attr("src", "<c:url value="/ml/predictApplyExcelFileDownload.do" />?"+vFileParam); //exceldown 실행? "src"에 "<c:url value="/ml/predictApplyExcelFileDownload.do" />?"+vFileParam 부여.	        	
		        }else{
		        	alert("처리중 오류가 발생하였습니다.");
		        }
	        },fail : function (result) {
				//loadingMsg.modal('hide');
				//ajaxFailMsg(result);
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
			}
		});
	}else{
		alert("엑셀 다운로드 실패.");
		return false;
	}
	
	
	
}

function numberFormat(inputNumber) {
	   return inputNumber.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
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

.predict-Feature_h { border:1px solid #ccc;background-color:#f7f7f7;min-width:60px;font-size:12px; text-align:center;}
.predict-Feature {border-top:1px solid #ccc;background-color:#fff;min-width:60px;font-size:12px; text-align:center;height:20px;}

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
			<div class="col-7 pt-1"  > 
				<div class="d-flex">
					<div style="width:60px;text-align:right;padding-right:10px;"> 단계</div>
					<div>
						<select id="f_predict_sel" class="form-control input-small p-0 pl-1"  style="width:120px;height:25px;font-size:12px;">
							<option value='EPC'>EPC</option>
							<option value='OEM'>OEM/End User</option>
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
			<div class="col-3 text-right"  style="height:30px;margin-top:3px;width: 40%;" >
				<a id="featureRangeList" class="btn" style="width:200px; min-width: 150px;"  data-toggle="collapse" data-target="">
					<i class="fas fa-info-circle"></i>
					 <span>Feautre Range info</span>
				</a>
				<a id="featureCollapseBtn" class="btn" style="width:100px; min-width: 50px;"  data-toggle="collapse" data-target="#fatureArea"> <span>숨기기</span>
					<i class="fa fa-angle-up" aria-hidden="true"></i>
				</a>
			</div>
			<!-- <div class="text-right"style="width: 40%; margin-top: -10px; height: 30px;">
				
				<a id="predictShowBtn" class="btn text-right"style="width: 170px; padding-right: 20px;"> <span>결과모두펼치기</span>
					<i class="fa fa-angle-down" aria-hidden="true"></i>
				</a>
			</div> -->
		</div>
						
		<div class="row collapse show" id="fatureArea">	
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
										
											<div class="col-2"  style="padding-left:10px !important ;">
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
													
<!-- 													<div class="progress" style="width:600px;display:none;"> -->
<!-- 													  <div id="predict_progress" class="progress-bar progress-bar-striped" role="progressbar" style="width: 0%" aria-valuenow="10" aria-valuemin="0" aria-valuemax="100"></div> -->
<!-- 													</div> -->
													
<!-- 													<div class="text-right"  style="margin-top:-15px;width:40%;"> -->
<!-- 														<p class="btn h5"    id="predictShowAll"><a href="#" class="text-info">Show All</a></p>/<p class="btn h5"    id="predictHideAll"><a href="#" class="text-info">Hide All</a></p> -->
<!-- 													</div> -->
													
													<div class="text-right"  style="width:40%;margin-top:-10px;height:30px;">
														<a id="predictShowBtn" class="btn text-right" style="width:170px;padding-right:20px;"> <span>결과모두펼치기</span>
															<i class="fa fa-angle-down" aria-hidden="true"></i>
														</a>
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
		<div class="modal-content">

			<!-- Modal Header -->
			<div class="modal-header">
	            <div style="font-size:24px;float:left;"></div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			
			<!--  Button -->
			<div class="row">
				<div class="col-12" >
					<div class="col-3 mt-3" >
						<div class=" h5 modal-title" >예측조건</div>
					</div>
					<div class="col-8 text-right" >
						<button type="button" class="btn btn-outline-success"  id="btn_pop_apply_feature">적용 <i class="fa fa-save"></i></button>
						<button type="button" class="btn btn-outline-success"  id="btn_pop_delete_feature">삭제 <i class="fa fa-eraser"></i></button>
					</div>
				</div>
			</div>
			
			<!-- Modal body -->
			<div class="modal-body" style="overflow-y:auto;">
				<div class="card custom-search-card-div pb-2">
					<!-- feature  -->
					<div class="row">
						<div class="col-5">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<strong>Pump Type</strong>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">
							<div class="pt-2 custom-responsive-p2 pr-1">
								<input id="f_pump_type" type="text" class="form-control form-control-sm" placeholder="Pump Type"  style="height:25px;width:100%;" >
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" ><strong>Product</strong></div>
						</div>
						<div class="col-7" style="padding-left:0px;">
							<div class="pt-2 custom-responsive-p2 pr-1">
								<input id="f_product" type="text" class="form-control form-control-sm" placeholder="Product"   style="height:25px;width:100%;" >
							</div>
						</div>
					</div>
					
					<!-- Temperature -->
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Temperature</strong></div>
									<div style="width:35%">
										<select id="select_temperature_unit" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">	
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="f_temp_nor" type="text" class="form-control form-control-sm" placeholder="Normal" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_temp_min" type="text" class="form-control form-control-sm" placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_temp_max" type="text" class="form-control form-control-sm" placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >		
								</div>
							</div>
						</div>
					</div>
					<!-- Specific Gravity -->
					<div class="row">
						<div class="col-5">
							<div class="p-2 custom-responsive-p2"   style="min-width:120px;" >
								<strong>Specific Gravity</strong>
							</div>
						</div>	
						<div class="col-7" style="padding-left:0px;">
							<div class="pt-2 custom-responsive-p2 pr-1">
								<div class="d-flex">
									<input id="f_spec_gravity_nor" type="number" class="form-control form-control-sm" placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_spec_gravity_min" type="number" class="form-control form-control-sm" placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_spec_gravity_max" type="number" class="form-control form-control-sm" placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>		
							</div>
						</div>
					</div>			
					<!-- Viscosity -->
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "    style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%">
										<strong>Viscosity</strong>
									</div>
									<div style="width:35%">
										<select id="select_viscosity_unit"  style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">
							<div class="pt-2 custom-responsive-p2 pr-1">
								<div class="d-flex">
									<input id="f_visc_nor" type="number" class="form-control form-control-sm" placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_visc_min" type="number" class="form-control form-control-sm" placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_visc_max" type="number" class="form-control form-control-sm" placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >	
								</div>
							</div>
						</div>
					</div>		
					<!-- Vapor Pressure -->
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "   style="min-width:150px;" >
								<div class="d-flex">
									<div style="width:65%">
										<strong>Vapor Pressure</strong>
									</div>
									<div style="width:35%">
										<select id="select_vap_pres_unit"  style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;"> 
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="f_vap_pres_nor" type="number" class="form-control form-control-sm" placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_vap_pres_min" type="number" class="form-control form-control-sm" placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_vap_pres_max" type="number" class="form-control form-control-sm" placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>	
					<!-- Seal Chamber Pressure 씰 챔버 압력-->
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%">
										<strong>Seal Chamber Pressure </strong>
									</div>
									<div style="width:35%">
										<select id="select_seal_cham_unit"  style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;"> 
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="f_seal_cham_nor" type="text" class="form-control form-control-sm" placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_seal_cham_min" type="text" class="form-control form-control-sm" placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_seal_cham_max" type="text" class="form-control form-control-sm" placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>		
					<!-- Shaft Speed -->
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 " style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%">
										<strong>Shaft Speed </strong>
									</div>
									<div style="width:35%">
										<select id="select_rpm_unit"  style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;"> 
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="f_rpm_nor" type="text" class="form-control form-control-sm" placeholder="Normal" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_rpm_min" type="text" class="form-control form-control-sm" placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_rpm_max" type="text" class="form-control form-control-sm" placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>		
					<!-- Shaft Dia -->
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2 "   style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%">
										<strong>Shaft Dia. </strong>
									</div>
									<div style="width:35%">
										<select id="select_shaft_size_unit"  style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;"> 
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="f_shaft_size" type="number" class="form-control form-control-sm" placeholder="Shaft Size" maxlength="20"  style="height:25px;width:100%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>		
				</div>
				
				<div class="card custom-search-card-div pb-2 mt-2">
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Application</strong></div>
									<div style="width:35%">
										<select id="select_application" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">	
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="service" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
									<select id="select_service" style="width:33%;height:25px;margin-left:2px;" class="selectpicker form-control"></select>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2" style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Equipment</strong></div>
									<div style="width:35%">
										<select id="select_equipment" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Equipment Type</strong></div>
									<div style="width:35%">
										<select id="select_equipment_type" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">	
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="quench_type" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
									<select id="select_quench_type" style="width:33%;height:25px;margin-left:2px;" class="selectpicker form-control"></select>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Caustic(%)</strong></div>
									<div style="width:35%">
										<select id="select_caustic" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Solid Size(m)</strong></div>
									<div style="width:35%">
										<input id="SOLID_SIZE_NOR" type="text" class="form-control form-control-sm" placeholder="Normal" maxlength="20"   style="height:25px;width:100%;" >
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">	
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="SOLID_SIZE_MIN" type="text" class="form-control form-control-sm" placeholder="Min" maxlength="20" style="height:25px;width:33%;margin-left:2px;" >
									<input id="SOLID_SIZE_MAX" type="text" class="form-control form-control-sm" placeholder="Max" maxlength="20" style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Solid (ppm)</strong></div>
									<div style="width:35%">
										<input id="SOLID_CONT" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:100%;" >
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>H2S (ppm)</strong></div>
									<div style="width:35%">
										<input id="H2S_CONT" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:100%;" >
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>H2SO4 (%)</strong></div>
									<div style="width:35%">
										<input id="H2SO4_CONT" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:100%;" >
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Sulfur (%)</strong></div>
									<div style="width:35%">
										<input id="SULFUR_CONT" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:100%;" >
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Chloride(%)</strong></div>
									<div style="width:35%">
										<input id="CHLORIDE_CONT" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:100%;" >
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">	
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="f_temp_PH" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_temp_PH2" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Vater(%)</strong></div>
									<div style="width:35%">
										<input id="SOLID_SIZE_NOR" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:100%;" >
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Brine</strong></div>
									<div style="width:35%">
										<select id="select_brine" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Oil</strong></div>
									<div style="width:35%">
										<select id="select_application" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-5"  style="padding-right:0px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<div class="d-flex">
									<div style="width:65%"><strong>Residue - Clean</strong></div>
									<div style="width:35%">
										<select id="select_application" style="width:100%;height:25px;padding:0px;" class="selectpicker form-control"></select>
									</div>
								</div>
							</div>
						</div>
						<div class="col-7" style="padding-left:0px;">	
							<div class="pt-2 custom-responsive-p2 pr-1" >
								<div class="d-flex">
									<input id="f_temp_nor" type="text" class="form-control form-control-sm" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
									<select id="select_application" style="width:33%;height:25px;margin-left:2px;" class="selectpicker form-control"></select>
								</div>
							</div>
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
		<div class="modal-content" style="height: 300px;">
		
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
<div class="modal" id="featureRangeInfo"  role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false"><!--0401  -->
	<div class="modal-dialog modal-lg" >
		<div class="modal-content" style="height: 820px;">
			<div class="modal-header">
				<div class=" h5 modal-title" >Model Feature Range</div>
	              	<div style="font-size:24px;float:left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			
			<div class="modal-body" style="overflow-y:auto;">
				<div class="card custom-search-card-div pb-2">
					<!-- feature  -->
					<div class="row">
						<div class="col-5" style="padding-left: 30px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
								<strong>Feature Name</strong>
							</div>
							<div class="p-2 custom-responsive-p2" id="feature_col_div">
							<input id="feature_col0" type="text"   style="height:25px;width:100%;border: none;" >
							<input id="feature_col1" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col2" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col3" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col4" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col5" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col6" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col7" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col8" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col9" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col10" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col11" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col12" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col13" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col14" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col15" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col16" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col17" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							<input id="feature_col18" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;" >
							</div>
							
						</div>
						
						<div class="col-2" style="padding-left: 15px; padding-right: 15px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;text-align: center;" >
								<strong>Unit</strong>
							</div>
							<div class="p-2 custom-responsive-p2"id="unit_val_div">
								<input id="unit_val0" type="text"   style="height:25px;width:100%;border: none;text-align: center;" >
								<input id="unit_val1" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val2" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val3" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val4" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val5" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val6" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val7" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val8" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val9" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val10" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val11" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val12" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val13" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val14" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val15" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val16" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val17" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="unit_val18" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >		
							</div>
						</div>
						
						
						<div class="col-2" style="padding-left: 15px; padding-right: 15px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;text-align: center;" >
								<strong>Min Value</strong>
							</div>
							<div class="p-2 custom-responsive-p2"id="mim_val_div">
								<input id="min_val0" type="text"   style="height:25px;width:100%;border: none;text-align: center;" >
								<input id="min_val1" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val2" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val3" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val4" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val5" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val6" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val7" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val8" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val9" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val10" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val11" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val12" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val13" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val14" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val15" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val16" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val17" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="min_val18" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >		
							</div>
						</div>
						
						<div class="col-3" style="padding-right: 30px;">
							<div class="p-2 custom-responsive-p2"  style="min-width:120px;text-align: center;" >
								<strong>Max Value</strong>
							</div>
							<div class="p-2 custom-responsive-p2" id="max_val_div">
								<input id="max_val0" type="text"   style="height:25px;width:100%;border: none;text-align: center;" >
								<input id="max_val1" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val2" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val3" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val4" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val5" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val6" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val7" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val8" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val9" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val10" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val11" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val12" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val13" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val14" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val15" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val16" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val17" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
								<input id="max_val18" type="text"   style="height:25px;width:100%;margin-top: 10px;border: none;text-align: center;" >
							</div>
						</div>
						
					</div>
					<div class="row">
						<!-- <div class="col-7" style="padding-left:0px;">	
							<div class="custom-responsive-p2" >
								<div class="d-flex">
									<input id="f_temp_nor" type="text" class="form-control form-control-sm"
										placeholder="Normal" maxlength="20"   style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_temp_min" type="text" class="form-control form-control-sm"
										placeholder="Min" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >
									<input id="f_temp_max" type="text" class="form-control form-control-sm"
										placeholder="Max" maxlength="20"  style="height:25px;width:33%;margin-left:2px;" >		
								</div>
							</div>
						</div> -->
						
					</div>
					
					<div class="row">
						
					</div>
				</div>										
			</div>
		</div>
	</div>
</div>

<iframe id="fileDownFrame" style="display:none;"></iframe>
</body>
</html>