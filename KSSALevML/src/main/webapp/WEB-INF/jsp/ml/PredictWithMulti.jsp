<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
<link rel="stylesheet" href="<c:url value='/css/common/olap-collapse-grid.css'/>">

<!-- 뷰어 -->
<link rel="stylesheet" href="<c:url value='/js/common/imgViewer/viewer.css'/>">
<script src="<c:url value='/js/common/imgViewer/viewer.min.js'/>"></script> 
<script src="<c:url value='/js/common/pdfobject/pdfobject.min.js'/>"></script>
<script src="<c:url value='/js/common/olap-collapse-grid.js'/>"></script>
<script>
var _vCurRow=''; <%--선택Row--%>
var _isRun=false;
var _fileUploadResult =null; <%-- Excel file Upload Info --%>
var _predictTotCnt, _predictDoneCnt=0; <%-- 추천처리건수 --%>
var _viewType = ""; // 화면타입 ( 1 : 모델추천. 2 : 데이터조회화면 추천(pop) )
var _searchList = null; // 조회된 데이터(Build를 위한)
var _limitSearchCnt = 10000;
var _historyId =""; // 이력관리 ID
var _historyObj;
var _getFeatureRangeList = ""; //추천할 값과 비교하기위해 전역변수화-
var _isChgProductApply = true; // 인자 입력화면에서 Product 가변경된 후 적용버튼이 실행되었는지 여부
let _asHistoryData = [];
let _ASHistoryDataObj = {};
$(document).ready(function(){
	<%-- 데이터조회화면 조회데이터 --%>
	var searchGridObj = parent.$("#objectViewGrid").jsGrid("option", "data");
	var searchList  = null;
	if (typeof searchGridObj[0] != "undefined"){  <%-- 데이터 조회에서 호출 --%>
		_viewType = "2";
		_searchList = parent.$("#objectViewGrid").jsGrid("option", "data");
		
		inputNumber = numberFormat(_searchList.length) // 천단위 콤마 찍는 함수
		$("#search_cnt_title").html(  "( Data : "+inputNumber + "건 )"   );  <%-- 데이터 건수 표시 --%>
		<%-- 데이터 조회 후 추천을 할 경우 조회데이터 건수 체크 --%>
		if ( _searchList.length > _limitSearchCnt){
			alert("조회된 데이터가 너무 많습니다<br/><br/>머신러닝분석/Model 추천 메뉴를 이용하세요.");
		}
		
	}else{ <%-- Model추천에서 호출 --%>
		_viewType = "1";
	}
	
	featureGridInit(); <%-- 추천조건 입력 그리드 초기화 --%>
	insertItem({}); <%-- 추천조건 입력 그리드  첫항목 인서트 처리 --%>
	
	getSealTypeInfo_new(); // TB_SEAL_TYPE_P_INFO 테이블 정보
	//getSealTypeInfo1();/* TB_SEAL_TYPE_T_INFO테이블 TYPE = P인 데이터 가져오기  */
	//getSealTypeInfo2();/* TB_SEAL_TYPE_T_INFO테이블 TYPE = A인 데이터 가져오기  */
	
	<%-- 추가버튼 Event --%>
	$("#btn_add_item").click(function(){
		if(	!chkActivityFunc()) return false; //return  값이  false 
		var insert_item = {};
		insertItem(insert_item);
	});
	
	<%-- 이력관리버튼 Event --%>
	$("#btn_history_mng").click(function(){
		//이력관리 초기화
		$("#saveTitle").val("");
		$("#saveRemark").val("");
		
		$('#historyMng').modal("show");
		historyGridInit();
		//높이조절
		if (($(document).height()-80) >= 700 ){
       		$('#historyMng .modal-content').css("height","800px");
		}else{
			$('#historyMng .modal-content').css("height",($(document).height()-150)+"px");	
			$('#jsGrid_history').jsGrid("option", "height",($(document).height()-450)+"px");	
		}
		$("#jsGrid_history").jsGrid("loadData");
	});
	
	<%-- 추천버튼 Event --%>	
	$("#btn_predict").click(function(){
		//if(	!chkActivityFunc()) return false;
		<%--
			/데이터조회 후 추천/ 
			조회된 데이터가 없는 경우 - training data가 없는경우 
		--%>
		if(_viewType=="2" && parent.$("#objectViewGrid").jsGrid("option", "data").length  <= 0){
			alert("조회된 데이터가 없습니다.");
			return;
		}
		
		<%-- 데이터 조회 후 추천을 할 경우 조회데이터 건수 체크 --%>
		if ( _viewType=="2" && _searchList.length > _limitSearchCnt){
			alert("조회된 데이터가 너무 많습니다<br/><br/>머신러닝분석/Model 추천 메뉴를 이용하세요.");
			return false;
		}
	
		<%-- 결과정보 Reset --%>	
		$("#predict_result").empty();
		var predictList = $("#jsGrid_feature").jsGrid("option", "data"); //TODO 입력한 추천 조건값 -  모델의 MIN MAX 값과 비교해야함.
		for (var i=0; i<predictList.length; i++){
			
			var predictItem = ""; 
			predictItem +="<input type='hidden' id='chkPredict_"+predictList[i].NO+"' value='n' />";
			predictItem +="<div class='card'>";
			predictItem +="<div class='card-header'>";
			predictItem +="<a class='card-link' data-toggle='collapse' data-parent='#predict_result' id='predictbtn"+predictList[i].NO+"'  href='#predict"+predictList[i].NO+"' >";
			predictItem +="Item No : "+predictList[i].NO + " , Pump Type : " + (predictList[i].PUMP_TYPE==undefined?"None":predictList[i].PUMP_TYPE) + " , Product : " + (predictList[i].PRODUCT==undefined?"None":predictList[i].PRODUCT);
			predictItem +="</a><span id='predict_status"+predictList[i].NO+"' style='padding-left:20px;color:#17a2b8;'> Predicting...</span>";
			predictItem +="</div>";
			predictItem +="<div id='predict"+predictList[i].NO+"' class='collapse'>";
			<%-- 조건 표시 --%>
			predictItem +="<div class='row' style='padding-top:8px;clear:both;'>";
			predictItem +="<div class='col-12'>";
			predictItem +="<div id='predict_condition_"+predictList[i].NO+"' style='padding:5px;clear:both;font-size:0.8em;'></div>";
			predictItem +="</div>";
			predictItem +="</div>";
			<%-- 조건 표시 --%>
			predictItem +="<div class='card-body' style='padding:5px;overflow-x:auto;'>";
			<%-- tab영역 --%>
			predictItem +="<div id='wrapper' style='padding-top:5px;clear:both;'>";
			predictItem +="<ul class='tabs'>";
			predictItem +="<li id='tabLi2_"+predictList[i].NO + "' class='btn text-right'><a onclick='tabEvent2(\""+predictList[i].NO+"\");' style='font-size:0.8em;'>Rule Based 추천</a></li>";
			predictItem +="<li id='tabLi_"+predictList[i].NO + "' class='btn text-right'><a onclick='tabEvent(\""+predictList[i].NO+"\");' style='font-size:0.8em;'>실적 Based 추천</a></li>";
			predictItem +="</ul>";
			predictItem +="<div class='tab_container'>";
			predictItem +="<div id='tabDiv_"+predictList[i].NO + "' class='tab_content'>";  // 실적기준 추천결과 
			<%-- 추천결과 표시 --%>
			//predictItem +="<div id='predictGrid"+predictList[i].NO+"'>predict...</div>";
			//predictItem +="<div class='d-flex' style='padding-top:5px;clear:both;'>";
			predictItem +="<div class='row'>";
			predictItem +="<div id='predictGrid1_"+predictList[i].NO+"' class='col-xl-4 col-lg-4 col-md-6 col-sm-6 col-xs-12' style='margin-bottom:3px;'>predict...</div>";
			predictItem +="<div id='predictGrid2_"+predictList[i].NO+"' class='col-xl-4 col-lg-4 col-md-6 col-sm-6 col-xs-12' style='padding-left:0px;margin-bottom:3px;'></div>";
			predictItem +="<div id='predictGrid3_"+predictList[i].NO+"' class='col-xl-4 col-lg-4 col-md-12 col-sm-12 col-xs-12' style='padding-left:0px;margin-bottom:3px;'></div>";
			predictItem +="</div>";
			<%-- 추천결과 표시 end --%>
			predictItem +="</div>";
			<%-- tab임시 end --%>
			
			predictItem +="<div id='tabDiv2_"+predictList[i].NO + "' class='tab_content'>";  //룰기준 추천결과
			<%-- rule based 결과 표시 --%>
			//predictItem +="<div id='predictGrid"+predictList[i].NO+"'>predict...</div>";
			//predictItem +="<div class='d-flex' style='padding-top:5px;clear:both;'>";
			predictItem +="<div class='row'>";
			
			predictItem +="<div id='ruleBasedGrid_"+predictList[i].NO+"' class='col-xl-12 col-lg-12 col-md-12 col-sm-12 col-xs-12' style=''>predict...</div>";
			predictItem +="</div>";
			
			<%-- note 결과 표시 --%>
			predictItem +="<div class='row' style='padding-top:5px;clear:both;'>";
			predictItem +="<div class='col-xl-6 col-lg-6 col-md-12 col-sm-12'>";
			predictItem +="<div><b id='noteBtn_"+predictList[i].NO+"' class='btn text-right' onclick='noteBtn(\""+predictList[i].NO+"\")'> * Note <i class='fa fa-angle-down' aria-hidden='true'></i></b><span id='noteMsg_"+predictList[i].NO+"' style='font-size:1em;color:red'></span></div>";
			predictItem +="<div id='noteSpace_"+predictList[i].NO+"' data-toggle='collapse' class='collapse'>";
			predictItem +="<div id='noteData_"+predictList[i].NO+"'style='padding:5px;clear:both;border:1px solid #ccc; font-size:0.8em; height: 250px; overflow-y:auto'></div>";
			predictItem +="</div>";
			predictItem +="</div>";
			predictItem +="<div class='col-xl-6 col-lg-6 col-md-12 col-sm-12'>";
			predictItem +="<div><b id='prdtBtn_"+predictList[i].NO+"' class='btn text-right' onclick='prdtBtn(\""+predictList[i].NO+"\")'>* 추천 과정 <i class='fa fa-angle-down' aria-hidden='true'></i></b></div>";
			predictItem +="<div id='prdtSpace_"+predictList[i].NO+"' data-toggle='collapse' class='collapse'>";
			predictItem +="<div id='prdtProc_"+predictList[i].NO+"'style='padding:5px;clear:both;border:1px solid #ccc; font-size:0.8em; height: 250px; overflow-y:auto'></div>";
			predictItem +="</div>";
			predictItem +="</div>";
			predictItem +="</div>";
			predictItem +="</div>";
			<%-- note 결과 표시 end --%>
			
			predictItem +="</div>";
			<%-- rulebased 결과 표시 --%>
			predictItem +="</div>";
			predictItem +="</div>";
			predictItem +="</div>";
			<%-- tab임시 end --%>
			predictItem +="</div>";
			predictItem +="</div>";
			
			$("#predict_result").append(predictItem);
			tabEvent2(predictList[i].NO);
		}
		<%-- predictMulti(predictList); -- 추천처리 --%>
		predictAll(predictList);  <%-- 추천처리(전체) --%>
		//display
		$("#openResult").css("display","");	
	});
	
	<%-- 추천결과 toggle --%>
	$('a[data-toggle="collapse"]').on('click',function(){
		var id=$(this).attr('href');
		if($(id).hasClass('in')){
			$(id).collapse('hide');
		}else{
			$(id).collapse('show');
		}
	});
	
	<%-- 추천인자조건 정보 저장 버튼 Event --%>
	$("#btn_pop_apply_feature").click(function(){
		//필수값 체크
		if(valChk()){
			return;
		};
		
		var gridRowObj = $('#jsGrid_feature').jsGrid('option', 'data')[_vCurRow];
		
		gridRowObj.PUMP_TYPE = $("#select_pump_type option:selected").val();
		gridRowObj.PUMP_TYPE_TEXT = $("#select_pump_type option:selected").text();
		
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
// 		gridRowObj.SEAL_TYPE_DIR = $("#f_seal_type_dir").val();
		gridRowObj.SEAL_INNER_DIR = $("#f_seal_inner_dir").val().toUpperCase();
		gridRowObj.SEAL_OUTER_DIR = $("#f_seal_outer_dir").val().toUpperCase();
// 		gridRowObj.MATERIAL_DIR = $("#f_material_dir").val();
		gridRowObj.M_IN_1 = $("#f_m_in_1").val();
		gridRowObj.M_IN_2 = $("#f_m_in_2").val();
		gridRowObj.M_IN_3 = $("#f_m_in_3").val();
		gridRowObj.M_IN_4 = $("#f_m_in_4").val();
		gridRowObj.M_OUT_1 = $("#f_m_out_1").val();
		gridRowObj.M_OUT_2 = $("#f_m_out_2").val();
		gridRowObj.M_OUT_3 = $("#f_m_out_3").val();
		gridRowObj.M_OUT_4 = $("#f_m_out_4").val();
		gridRowObj.API_PLAN_DIR = $("#f_api_plan_dir").val().toUpperCase();
		gridRowObj.PC_TOXIC_CHK = $("#pc_toxic_chk").is(":checked")==true?"Y":"N";
		gridRowObj.PC_HAZARD_CHK = $("#pc_hazard_chk").is(":checked")==true?"Y":"N";
		gridRowObj.PC_FLAM_CHK = $("#pc_flam_chk").is(":checked")==true?"Y":"N";
		gridRowObj.PC_LEAKAGE_CHK = $("#pc_leakage_chk").is(":checked")==true?"Y":"N";
		gridRowObj.PC_HIGH_CORR_CHK = $("#pc_high_corr_chk").is(":checked")==true?"Y":"N";
		gridRowObj.PC_COOL_TROUBLE_CHK = $("#pc_cool_trouble_chk").is(":checked")==true?"Y":"N";
		
		/* 단위 */
		gridRowObj.TEMP_UNIT = $("#select_temperature_unit option:selected").val();
		gridRowObj.TEMP_TEXT = $("#select_temperature_unit option:selected").text();
		
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
		
		//추가된 조건들
		gridRowObj.FTA_YN = $("#select_fta_yn option:selected").val();
		gridRowObj.FTA_YN_TEXT = $("#select_fta_yn option:selected").text();
		
		gridRowObj.APPLICATION = $("#select_application option:selected").val();
		gridRowObj.APPLICATION_TEXT = $("#select_application option:selected").text();
		
		gridRowObj.SERVICE	= $("#select_service option:selected").val();
		gridRowObj.SERVICE_TEXT	= $("#select_service option:selected").text();
		
		gridRowObj.EQUIPMENT = $("#select_equipment option:selected").val();
		gridRowObj.EQUIPMENT_TEXT = $("#select_equipment option:selected").text();

		gridRowObj.EQUIPMENT_TYPE = $("#select_equipment_type option:selected").val();
		gridRowObj.EQUIPMENT_TYPE_TEXT = $("#select_equipment_type option:selected").text();
		
		gridRowObj.QUENCH_TYPE = $("#select_quench_type option:selected").val();
		gridRowObj.QUENCH_TYPE_TEXT = $("#select_quench_type option:selected").text();
		
		gridRowObj.SOLID_GB			= $("#select_solid_gb_yn option:selected").val();
		gridRowObj.SOLID_GB_TEXT	= $("#select_solid_gb_yn option:selected").text();
		
		gridRowObj.SOLID_SIZE_NOR 	= $("#solid_size_nor").val();
		gridRowObj.SOLID_SIZE_MIN 	= $("#solid_size_min").val();
		gridRowObj.SOLID_SIZE_MAX 	= $("#solid_size_max").val();
		gridRowObj.SOLID_SIZE_MAX_CHK 	= $("#solid_size_check").is(":checked")?"Y":"N";
		gridRowObj.SOLID_CONT 		= $("#solid_cont").val();
		
		gridRowObj.PH 				= $("#PH").val();
		gridRowObj.SEAL_SIZE		= $("#SEAL_SIZE").val();
		
		gridRowObj.END_USER			= $("#select_endUser option:selected").val();
		gridRowObj.END_USER_TEXT	= $("#select_endUser option:selected").text();
		
		gridRowObj.GS_GROUP			= $("#select_group option:selected").val();
		gridRowObj.GS_GROUP_TEXT	= $("#select_group option:selected").text();

		gridRowObj.GS_SERVICE		= $("#select_service_gs option:selected").val();
		gridRowObj.GS_SERVICE_TEXT	= $("#select_service_gs option:selected").text();
		
		gridRowObj.GS_CASE			= $("#select_case option:selected").val();
		gridRowObj.GS_CASE_TEXT		= $("#select_case option:selected").text();
		
		gridRowObj.S_D_GB			= $("#select_s_d_gb option:selected").val();
		gridRowObj.S_D_GB_TEXT		= $("#select_s_d_gb option:selected").text();

		gridRowObj.API682_YN		= $("#select_api682_yn option:selected").val();
		gridRowObj.API682_YN_TEXT	= $("#select_api682_yn option:selected").text();
		
		gridRowObj.BELLOWS_YN		= $("#select_bellows_yn option:selected").val();
		gridRowObj.BELLOWS_YN_TEXT	= $("#select_bellows_yn option:selected").text();
		
		gridRowObj.CARTRIDGE_TYPE		= $("#select_cartridge_type option:selected").val();
		gridRowObj.CARTRIDGE_TYPE_TEXT	= $("#select_cartridge_type option:selected").text();
		
		gridRowObj.SEAL_CONFIG	= $("#select_seal_config option:selected").val();
		gridRowObj.SEAL_CONFIG_TEXT	= $("#select_seal_config option:selected").text();
		
		gridRowObj.SPLIT_YN			= $("#select_split_yn option:selected").val();
		gridRowObj.SPLIT_YN_TEXT	= $("#select_split_yn option:selected").text();
		
		gridRowObj.BRINE_GB = $("#select_brine_gb option:selected").val();
		gridRowObj.BRINE_GB_TEXT = $("#select_brine_gb option:selected").text();
		
		gridRowObj.BRINE_SUB_GB = $("#select_brine_sub_gb option:selected").val();
		gridRowObj.BRINE_SUB_GB_TEXT = $("#select_brine_sub_gb option:selected").text();
		
// 		gridRowObj.OIL_HRDN_YN = $("#select_oil_hrdn_yn option:selected").val();
// 		gridRowObj.OIL_HRDN_YN_TEXT = $("#select_oil_hrdn_yn option:selected").text();
		
// 		gridRowObj.RESI_CLEAN_GB = $("#select_resi_clean_gb option:selected").val();
// 		gridRowObj.RESI_CLEAN_GB_TEXT = $("#select_resi_clean_gb option:selected").text();
		
// 		gridRowObj.RESI_HRDN_GB = $("#select_resi_hrdn_gb option:selected").val();
// 		gridRowObj.RESI_HRDN_GB_TEXT = $("#select_resi_hrdn_gb option:selected").text();
		
////// //////////////////////////////////////////////////////////////////// /  수정필요		

		//product 입력필드 초기화
		for(var i=0;i<12;i++){
			eval("gridRowObj.PRODUCTNM_"+(i+1) +" = ''" );
			eval("gridRowObj.PRODUCTGB_"+(i+1) +" = ''" );
			eval("gridRowObj.PRODUCTUT_"+(i+1) +" = ''" );
			eval("gridRowObj.PRODUCTND_"+(i+1) +" = ''" );
		}
	
		//Product 입력필드 항목정보를 가져온다.
		var productContInputs = [];
		$("#divProducts").find('._productContInput').each(function(){
			productContInputs.push($(this).attr("id")); 
		});
		
		//Product 입력필드의 값을 Grid에 설정한다.
		for(var i=0; i<productContInputs.length;i++){
			eval("gridRowObj.PRODUCTNM_"+(i+1) +" = $('#"+productContInputs[i]+"').text()" );
			eval("gridRowObj.PRODUCTGB_"+(i+1) +" = $('#"+productContInputs[i]+"_GB option:selected').val()");
			eval("gridRowObj.PRODUCTUT_"+(i+1) +" = $('#"+productContInputs[i]+"_UT option:selected').val()");
			eval("gridRowObj.PRODUCTND_"+(i+1) +" = $('#"+productContInputs[i]+"_ND').val()");
		}

		//구분값 적용
		$("#jsGrid_feature").jsGrid("refresh");
		$('#featureEdit').modal("hide");	
	});
	
	<%-- 추천인자조건 정보 삭제 버튼 Event --%>
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
	
	<%-- 추천정보 엑셀저장 버튼 Event --%>
	$("#btn_sav_excel_item").click(function(){
		if(	!chkActivityFunc()) return false;
		excelSaveProcess();
	});
	
	<%-- 관련자료 버튼 Event --%>
	$("#btn_fileData").click(function(){
		libraryGridInit();
		$("#jsGrid_library").jsGrid("loadData");
		$('#fileDataPop').modal("show");
	});
	
	$("#featureRangeList").click(function(){
		$('#featureRangeInfo').modal("show");
		rangerListInfo();
	});
	
	$("#featureReset").click(function(){
		$("#editHistory").css('display','none');
		$("#historyTitle").text("");
		$("#f_predict_sel").val("EPC");
		$("#f_equip_type_sel").val("Pump");
		featureGridInit();
		insertItem({});
	});

	<%-- 추천결과 Show/Hide All Event --%>
	$("#predictShowBtn").on('click', function(e) {
		if ($(this).find("i").hasClass("fa-angle-down")) {
			$('#predict_result .collapse').addClass('show');
			$(this).find("span").text("결과접기");
			$(this).find("i").removeClass("fa-angle-down");
			$(this).find("i").addClass("fa-angle-up");
		}else {
			$('#predict_result .collapse').removeClass('show');
			$(this).find("span").text("결과펼치기");
			$(this).find("i").removeClass("fa-angle-up");
			$(this).find("i").addClass("fa-angle-down");
		}
	});
	
	<%-- 추천인자 Hide/Show Event --%>
	$("#featureCollapseBtn").on('click', function(e) {
		if ($(this).find("i").hasClass("fa-angle-up")) {
			$(this).find("span").text("조건 보이기");
			$(this).find("i").removeClass("fa-angle-up");
			$(this).find("i").addClass("fa-angle-down");
		} else {
			$(this).find("span").text("조건 숨기기");
			$(this).find("i").removeClass("fa-angle-down");
			$(this).find("i").addClass("fa-angle-up");
		}
	});
	
	<%-- 추천옵션 Show/Hide All Event --%>
	$("#rcmdOptBtn").on('click', function(e) {
		if ($(this).find("i").hasClass("fa-angle-down")) {
			$('#rcmdResult').collapse('show');
			$(this).find("i").removeClass("fa-angle-down");
			$(this).find("i").addClass("fa-angle-up");
		}else {
			$('#rcmdResult').collapse('hide');
			$(this).find("i").removeClass("fa-angle-up");
			$(this).find("i").addClass("fa-angle-down");
		}
	});
	
	<%-- 단계 변경 Event --%>
//	2021.02.05 SHAFT SPEED와 SHAFT DIA가 기존에 단계에 따라 그리드에 보였다 안보였다 하였다. 그리드 그래픽이 깨지는 문제가 있어서 항상 보이게 수정함
// 	$("#f_predict_sel").change(function(){
// 		if ($(this).val() =="OEM"){
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_TEXT", "visible", true);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_NOR", "visible", true);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MIN", "visible", true);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MAX", "visible", true);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE_TEXT", "visible", true);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE", "visible", true);
// 			$(".OEM_FEAUTRE").css("display","");
// 		}else{
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_TEXT", "visible", false);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_NOR", "visible", false);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MIN", "visible", false);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MAX", "visible", false);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE_TEXT", "visible", false);
// 			$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE", "visible", false);
// 			$(".OEM_FEAUTRE").css("display","none");
// 		}
// 	});
	// page open 시 default 
// 	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_TEXT", "visible", false);
// 	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_NOR", "visible", false);
// 	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MIN", "visible", false);
// 	$("#jsGrid_feature").jsGrid("fieldOption", "RPM_MAX", "visible", false);
// 	$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE_TEXT", "visible", false);
// 	$("#jsGrid_feature").jsGrid("fieldOption", "SHAFT_SIZE", "visible", false);
// 	$(".OEM_FEAUTRE").css("display","none");
	
	var option1, option2, option3_1, option3_2, option4, option5, option6, option16, option17;
	<%-- unit select box
		GRU0001	압력
		GRU0002	크기
		GRU0003	온도
		GRU0004	점도
		GRU0005	Speed
	--%>
	//빈칸 set
	option6 = $("<option selected value></option>");
	$('#select_application').append(option6);
	option16 = $("<option selected value></option>");
	$('#select_seal_config').append(option16);
	option17 = $("<option selected value></option>");
	$('#select_cartridge_type').append(option17);
	
	//pump type combo set
	$.ajax({
		type:"POST",
		url:"<c:url value='/rb/getPumpType.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({})
	}).done(function(result){
		$('#select_pump_type').empty();
		var option15 = $("<option selected value></option>");
		$('#select_pump_type').append(option15);
		for(var i = 0; i<result.length; i++){
			option15 = $("<option value="+result[i].PUMP_TYPE+">"+result[i].PUMP_TYPE_NM+"</option>");
			$('#select_pump_type').append(option15);
		}
	})
	
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
	<c:forEach var="unit" items="${UNIT_COMBO}" varStatus="status">
		<c:if test="${unit.MCD=='Z040'}">
			option6 = $("<option value='${unit.SCD}'>"+"${unit.CD_NM}"+"</option>");
			$('#select_application').append(option6);
		</c:if>
		<c:if test="${unit.MCD=='Z150'}">
			option16 = $("<option value='${unit.SCD}'>"+"${unit.CD_NM}"+"</option>");
			$('#select_seal_config').append(option16);
		</c:if>
		<c:if test="${unit.MCD=='Z160'}">
			option17 = $("<option value='${unit.SCD}'>"+"${unit.CD_NM}"+"</option>");
			$('#select_cartridge_type').append(option17);
		</c:if>
	</c:forEach>
	
	getFeatureRangeList(); //0323 model의 getFeatureRangeList. TB_ML_MODEL_FEATURE_RANGE값 가져오기
	
	<%-- application combo Change Event --%>
	$("#select_application").change(function(){
		var scdId = $("#select_application option:selected").val();
		if(isNotEmpty(scdId)){
			$.ajax({
				type:"POST",
				async:false,
				url:"<c:url value='/ml/getServiceCombo.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					SCD : scdId
				})
			}).done(function(result){
				$('#select_service').empty();
				var option7;
				for(var i = 0; i<result.length; i++){
					option7 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
					$('#select_service').append(option7);
				}
				$("#select_service").val(result[0].SCD).change();
			})
		}else{
			$("#select_service").val("").change();
		}
	});
	
	<%-- application combo Change Event --%>
	$("#select_service").change(function(){
		$.ajax({
			type:"POST",
			async:false,
			url:"<c:url value='/ml/getEquipmentCombo.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			data: JSON.stringify({
				SCD1 : $("#select_application option:selected").val()
				,SCD2 : $("#select_service option:selected").val()
			})
		}).done(function(result){
			$('#select_equipment').empty();
			var option8;
			for(var i = 0; i<result.length; i++){
				option8 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
				$('#select_equipment').append(option8);
			}
		})
	});
	
	<%-- Equipment Type, Quench Type, Brine 구분 combo Change Event --%>
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/getEquipmentTypeCombo.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({})
	}).done(function(result){
		$('#select_equipment_type').empty();
		var option8 = $("<option selected value></option>");
		$('#select_equipment_type').append(option8);
		for(var i = 0; i<result.length; i++){
			option8 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
			$('#select_equipment_type').append(option8);
		}
	})
	
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/getQuenchTypeCombo.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({})
	}).done(function(result){
		$('#select_quench_type').empty();
		var option9 = $("<option selected value></option>");
		$('#select_quench_type').append(option9);
		for(var i = 0; i<result.length; i++){
			option9 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
			$('#select_quench_type').append(option9);
		}
	})
	
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/getBrineGbCombo.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({})
	}).done(function(result){
		$('#select_brine_gb').empty();
		var option10 = $("<option selected value></option>");
		$('#select_brine_gb').append(option10);
		for(var i = 0; i<result.length; i++){
			option10 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
			$('#select_brine_gb').append(option10);
		}
	})
	
	<%-- end user, group, service, case combo Change Event --%>
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/getEndUserCombo.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({})
	}).done(function(result){
		$('#select_endUser').empty();
		var option11 = $("<option selected value></option>");
		$('#select_endUser').append(option11);
		for(var i = 0; i<result.length; i++){
			option11 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
			$('#select_endUser').append(option11);
		}
	})
	
	$("#select_endUser").change(function(){
		$.ajax({
			type:"POST",
			async:false,
			url:"<c:url value='/ml/getGroupCombo.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			data: JSON.stringify({
				SCD1 : $("#select_endUser").val()
			})
		}).done(function(result){
			$('#select_group').empty();
			var option12;
			if(result.length > 0){
				for(var i = 0; i<result.length; i++){
					option12 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
					$('#select_group').append(option12);
				}
		 		$("#divGroup").css('display','block');
				$("#select_group").val(result[0].SCD).change();
			}else{
		 		$("#divGroup").css('display','none');
				$("#select_group").val("").change();
			}
		})
	});
	
	$("#select_group").change(function(){
		$.ajax({
			type:"POST",
			async:false,
			url:"<c:url value='/ml/getServiceGsCombo.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			data: JSON.stringify({
				SCD1 : $("#select_group option:selected").val()
			})
		}).done(function(result){
			$('#select_service_gs').empty();
			if(result.length > 0){
				for(var i = 0; i<result.length; i++){
					option13 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
					$('#select_service_gs').append(option13);
				};
				$("#divServiceGs").css('display','block');
				$("#select_service_gs").val(result[0].SCD).change();
			}else{
				$("#divServiceGs").css('display','none');
				$("#select_service_gs").val("").change();
			}
		})
	});
	
	$("#select_service_gs").change(function(){
		$.ajax({
			type:"POST",
			async:false,
			url:"<c:url value='/ml/getCaseCombo.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			data: JSON.stringify({
				SCD1 : $("#select_service_gs option:selected").val()
			})
		}).done(function(result){
			$('#select_case').empty();
			var option14;
			if(result.length > 0){
				for(var i = 0; i<result.length; i++){
					option14 = $("<option value="+result[i].SCD+">"+result[i].CD_NM+"</option>");
					$('#select_case').append(option14);
				}
				$("#divCase").css('display','block');
				$("#select_case").val(result[0].SCD).change();
			}else{
				$("#divCase").css('display','none');
				$("#select_case").val("").change();
			}
		})
	});
	
	
	$("#select_fta_yn").change(function(){
		var syn = $("#select_fta_yn option:selected").val();
		if(isNotEmpty(syn) && syn == "Y"){
			$("#select_application").val("");
	     	$("#select_service").val("");
	     	$("#select_equipment").val("");
	     	
	     	$("#divFtaApplication").css('display','block');
	     	$("#divFtaService").css('display','block');
	     	$("#divFtaEquipment").css('display','block');
		}else{
			$("#select_application").val("");
	     	$("#select_service").val("");
	     	$("#select_equipment").val("");
	     	
	     	$("#divFtaApplication").css('display','none');
	     	$("#divFtaService").css('display','none');
	     	$("#divFtaEquipment").css('display','none');
		}
	});
	
	$("#select_solid_gb_yn").change(function(){
		var solidGb = $("#select_solid_gb_yn option:selected").val();
		if(isNotEmpty(solidGb) && solidGb == "Y1"){
			$("#solid_size_nor").val("");
			$("#solid_size_min").val("");
			$("#solid_size_max").val("");
			$('#solid_size_check').prop('checked', false);
			$("#solid_cont").val("");
			$("#divSolidSize").css('display','block');
			$("#divSolidCont").css('display','block');
		}else{
			$("#solid_size_nor").val("");
			$("#solid_size_min").val("");
			$("#solid_size_max").val("");
			$('#solid_size_check').prop('checked', false);
			$("#solid_cont").val("");
			$("#divSolidSize").css('display','none');
			$("#divSolidCont").css('display','none');
		}
	});
	
	
	$("#select_brine_gb").change(function(){
		var brineGb = $("#select_brine_gb option:selected").val();
		if(isNotEmpty(brineGb) && brineGb == "Z110010"){
			$("#select_brine_sub_gb").val("");
			$("#select_brine_sub_gb").css('display','block');
		}else{
			$("#select_brine_sub_gb").val("");
			$("#select_brine_sub_gb").css('display','none');
		}
	});
	
	<%-- save history Event --%>
	$("#savHistory").click(function(){
		var historyId = uuidv4();
		var detailObj = parent.$("#jsGrid_feature").jsGrid("option", "data");
		for(var i=0; i<detailObj.length; i++){
			detailObj[i].subNo = historyId+"_"+i;
		}
		var headerObj = {
			historyId : historyId
			,title 	: $("#saveTitle").val()
			,remark : $("#saveRemark").val()
			,step   : $("#f_predict_sel option:selected").val()
			,equipType : $("#f_equip_type_sel option:selected").val()
		};
	    var param = {
			header : headerObj
		    ,detail : detailObj
		}
		if(isNotEmpty($("#saveTitle").val())){
			$.ajax({
				type:"POST",
				url:"<c:url value='/ml/savHistory.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify(param)
			}).done(function(data){
				
				if(data.result == "E01"){
					alert("중복된 Title 입니다");
				}else{
					
					_historyId = historyId;
					$("#jsGrid_feature").jsGrid("loadData");
					
					$('#historyMng').modal("hide");
					//$("#editHistory").css('display','none');
					$("#editHistory").css('display','inline');
					$("#historyTitle").text("( "+$("#saveTitle").val()+" )");
					
					//이력수정을 위해 HistoryId를 설정한다.
					_historyObj = new Object();
					_historyObj.HISTORY_ID = historyId
					
					alert("저장 되었습니다.");	
				}
			}).fail (function (result) {
				var eMsg = "";
				if(jqXHR.status === 400){
					eMsg="요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.";
				}else if (jqXHR.status == 401) {
		            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
		            	location.href = OlapUrlConfig.loginPage;
		            });
		         } else if (jqXHR.status == 403) {
		            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
		            	location.href = OlapUrlConfig.loginPage;
		            });
		         }else if (jqXHR.status == 500) {
		        	 eMsg="처리중 에러가 발생하였습니다.";
		        	 eMsg = eMsg +"<br/>"+ (jqXHR.responseText).substring(0,200);
		         }else{
		        	 eMsg="서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.";
				}
				alert(eMsg);
			})
		}else{
			alert("제목은 필수 입력값 입니다.");
			return;
		}
	})
	
	$("#loadHistory").click(function(){
		if(isNotEmpty(_historyObj)){  
			_historyId = _historyObj.HISTORY_ID;
			$("#historyTitle").text("( "+_historyObj.TITLE+" )");
			confirm("선택하신 정보를 불러오시겠습니까?<br/>"+_historyObj.TITLE, function(result) {  // bootbox js plugin for confirmation dialog
            	if(result == true){
		        	$("#f_predict_sel").val(_historyObj.STEP);
		        	$("#f_equip_type_sel").val(_historyObj.EQUIP_TYPE);
		        	$("#jsGrid_feature").jsGrid("loadData");
		        	$("#editHistory").css('display','inline');
		        	_historyObj = null;
            	}
			});
		}else{
			alert("적용하실 이력정보를 선택해 주세요.");
		}
	})
	
	<%-- edit history Event --%>
	$("#editHistory").click(function(){
		var detailObj = parent.$("#jsGrid_feature").jsGrid("option", "data");
		var headerObj = {
			HISTORY_ID : detailObj[0].HISTORY_ID
		};
		//항목이 추가되면 sub no를 생성해 준다.
		for(var i=0; i<detailObj.length; i++){
			detailObj[i].historyId = detailObj[0].HISTORY_ID;
			detailObj[i].subNo = detailObj[0].HISTORY_ID+"_"+i;
		};
		$.ajax({
			type:"POST",
			url:"<c:url value='/ml/editHistory.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
		    data: JSON.stringify({
		    	header : headerObj
		    	,detail : detailObj
		    })
		}).done(function(result){
			alert("저장 되었습니다.");
			$("#jsGrid_feature").jsGrid("loadData");
		})
	})
	
	<%-- 
		입력된 product에 따라 Product별 추가정보 입력 필드 처리
		- Product focus out Event 
		-> 적용버튼 클릭으로 대체
	--%>
	//$("#f_product").focusout(function(){
	$("#btn_pop_apply_product").click(function(){		
		
		var product = $("#f_product").val();
		
		if (product==""){
			//초기화
			hideCondition_product_apply();
			$("#divProducts").empty();
			return;	
		}
		
		var productItem ="";
		var paramData = {
			PRODUCT : product
		};
		$.ajax({ 
			type : "POST",
			url: "<c:url value='/rb/getProductGrp.do'/>",
			data : JSON.stringify(paramData),
			contentType: "application/json;charset=UTF-8",
			success : function(data){
				//초기화
				hideCondition_product_apply();
				$("#divProducts").empty();
				
				var dataStr = data;
				
				//Product별 추가 필드 처리
				for(var i=0; i<dataStr.length; i++){
// 					if(dataStr[i].product == "SLURRY"){	
// 						$("#select_solid_gb_yn").val("");
						
					if(dataStr[i].product_grp == "BRINE"){
						$("#select_brine_gb").val("");
						$("#divBrine").css('display','block');
						
//					}else if(dataStr[i].product == "OIL"){
// 						$("#select_oil_hrdn_yn").val("");
// 						$("#divOil").css('display','block');
						
//					}else if(dataStr[i].product == "RESIDUE"){
// 						$("#select_resi_clean_gb").val("");
// 						$("#select_resi_hrdn_gb").val("");
// 						$("#divResidue").css('display','block');
						
					}else if(dataStr[i].product_grp == "CHLORIDE"){
					//}else if(dataStr[i].product_grp == "CHLORINE"){	
						$('#PH').val("");
						$("#divChloride").css('display','block');
						
					}
				}
				
				//display가 none인 입력창은 공란으로 초기화
				if($('#select_solid_gb_yn').css('display') == 'none'){
					$("#select_solid_gb_yn").val("");
				}
				if($('#divBrine').css('display') == 'none'){
					$("#select_brine_gb").val("");
					$("#select_brine_sub_gb").val("");
					$("#select_brine_sub_gb").css('display','none');
				}
// 				if($('#divOil').css('display') == 'none'){
// 					$("#select_oil_hrdn_yn").val("");
// 				}
// 				if($('#divResidue').css('display') == 'none'){
// 					$("#select_resi_clean_gb").val("");
// 					$("#select_resi_hrdn_gb").val("");
// 				}
				if($('#divChloride').css('display') == 'none'){
					$('#PH').val("");
				}
				
				//----------------------------------------------------------------------
				// product 별 농도단위, 구분, 농도 입력 필드 활성화 처리
				//----------------------------------------------------------------------
				//CRUID OIL 유무
				var isCruidOil = false;
				for(var i=0; i<dataStr.length; i++){
					if(dataStr[i].product == "CRUDE OIL") {
						isCruidOil = true;
						break;
					}
				}
				
				for(var i=0; i<dataStr.length; i++){
					productItem += getRuleProductInputItem(i+1, dataStr[i], isCruidOil);
				}
				$("#divProducts").append(productItem);
				
				// 변경된 Product정보 반영여부를 true로 설정
				_isChgProductApply = true;
			},
			error : function(){
				alert("데이터 처리 중 오류가 발생하였습니다.");
			}
		});
	});	
	
	
	<%--
		입력인자에서 Product 입력값이 변경될 경우
		Product 정보 적용여부를 False로 상태를 설정한다.
	--%>
	$("#f_product").change(function(){
		_isChgProductApply = false;	
	});
	
	
	
}); <%-- end  $(document).ready(function(){ --%>

//url:"<c:url value='/ml/predictWithMulti.do'/>",
function setFeatureGrid(FEATURE_COL,predictItem,predictItem_C){ //0323 추천결과 추천값 인자 표시해주는 Grid를 그려주는 부분 MIN보다 작을시 파란색 MAX보다 클경우 빨간색.
	for(j=0; j<_getFeatureRangeList.result.length; j++){
		if(_getFeatureRangeList.result[j].FEATURE_COL == FEATURE_COL){ //비교할 대상 값
			if(FEATURE_COL == "SHAFT_SIZE" && _getFeatureRangeList.result[j].FEATURE_COL == "SHAFT_SIZE"){ //SHAFT_SIZE만 min-width:100px; style 적용.
				if(predictItem_C < _getFeatureRangeList.result[j].MIN_VAL){ 
					predictItem =" <div class='predict-Feature' style='color:blue;min-width:100px;font-weight:bold;'>"+setUTB(predictItem)+"</div>";
				}else if(predictItem_C > _getFeatureRangeList.result[j].MAX_VAL){  
					predictItem =" <div class='predict-Feature' style='color:red;min-width:100px;font-weight:bold;'>"+setUTB(predictItem)+"</div>";
				}else{ 
					predictItem =" <div class='predict-Feature' style='min-width:100px'>"+setUTB(predictItem)+"</div>";
				}	
			}else{ //그외 나머지 
				if(predictItem_C < _getFeatureRangeList.result[j].MIN_VAL){ // 추천값이 모델에 저장된 MIN값보다 작을때
					predictItem =" <div class='predict-Feature' style='color:blue;font-weight:bold;'>"+setUTB(predictItem)+"</div>";
				}else if(predictItem_C > _getFeatureRangeList.result[j].MAX_VAL){ // 추천값이 모델에 저장된 MAX값보다 클때 
					predictItem =" <div class='predict-Feature' style='color:red;font-weight:bold;'>"+setUTB(predictItem)+"</div>";
				}else{ //추천값이 모델에 저장된 MIN ~ MAX 값 사이일때
					predictItem =" <div class='predict-Feature'>"+setUTB(predictItem)+"</div>";
				}	
			}
		}
	} 
	return predictItem ;
}

function setUTB(val){
	if(typeof val == "undefined"){
		return "";	
	}else{
		return val;
	}
}
	

//추가된 운전 조건
function setFeatureGridAdd(FEATURE_COL,predictItem,predictItem_C){
	predictItem =" <div class='predict-Feature-Add' style='width:100%;'>"+'&nbsp;'+ predictItem +'&nbsp;'+"</div>";
	return predictItem ;
}

//추가된 운전 조건 - Style parameter
function setFeatureGridAdd_style(FEATURE_COL,predictItem,predictItem_C, style){
	predictItem =" <div class='predict-Feature-Add' style='"+style+"'>"+'&nbsp;'+ predictItem +'&nbsp;'+"</div>";
	return predictItem ;
}

function getFeatureRangeList(){ //0323 생성된 model의 FeatureRangeList구하기 추천할 FeatureList값과 비교하기위해서.
	$.ajax({
		type:'GET',
		url: '/ml/getFeatureRangeList.do',
	}).done(function(result){
		FeatureRangeList = result;
		_getFeatureRangeList = FeatureRangeList
	})
}

var sealTypeList_PSA = new Array();
function getSealTypeInfo_new(){
	$.ajax({
		type:'GET',
		url: '/ml/getSealTypeInfo_new.do',
	}).done(function(result){ 
		sealTypeList_PSA = result;
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
	$("#jsGrid_feature").jsGrid("insertItem",insertObj);
}

function numberFormat(inputNumber) { //천단위 콤마 찍는 함수
	return inputNumber.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function rangerListInfo(){
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
	    	{name : "HISTORY_ID",title : "History ID",type : "text",align : "center",width : 80, visible:false},
	    	{name : "SUB_NO",title : "Sub No",type : "text",align : "center",width : 80, visible:false},
	    	{name : "PUMP_TYPE_TEXT",title : "Pump Type",type : "text",align : "center",width : 80, css:"font-size-down" },
	    	{name : "PRODUCT",title : "Product",type : "text",align : "left",width : 250, css:"font-size-down"},
	    	{name : "TEMP_TEXT",title : "Temp.<br/>Unit",type : "text", align : "center",width : 80, css:"font-size-down"},  
	    	{name : "TEMP_NOR",title : "Temp.<br/>Rtd",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "TEMP_MIN",title : "Temp.<br/>Min",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "TEMP_MAX",title : "Temp.<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "SPEC_GRAVITY_NOR",title : "S.G.<br/>Rtd",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SPEC_GRAVITY_MIN",title : "S.G.<br/>Min",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SPEC_GRAVITY_MAX",title : "S.G.<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VISC_TEXT",title : "Visc<br/>Unit",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "VISC_NOR",title : "Visc<br/>Rtd",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VISC_MIN",title : "Visc<br/>Min",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VISC_MAX",title : "Visc<br/>Max",type : "text",width : 80,align : "center",css:"font-size-down"},
	    	{name : "VAP_PRES_TEXT",title : "Vapor<br/>Unit",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "VAP_PRES_NOR",title : "Vapor<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "VAP_PRES_MIN",title : "Vapor<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "VAP_PRES_MAX",title : "Vapor<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_TEXT",title : "Seal Cham.<br/>Unit",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_NOR",title : "Seal Cham.<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_MIN",title : "Seal Cham.<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SEAL_CHAM_MAX",title : "Seal Cham.<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "RPM_TEXT",title : "Shaft Speed<br/>Unit",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "RPM_NOR",title : "Shaft Speed<br/>Rtd",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "RPM_MIN",title : "Shaft Speed<br/>Min",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "RPM_MAX",title : "Shaft Speed<br/>Max",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SHAFT_SIZE_TEXT",title : "Shaft  dia.<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "SHAFT_SIZE",title : "Shaft dia.",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	//UNIT (CODE= VALUE)
	    	{name : "PUMP_TYPE",title : "PumpType.<br/>단위",type : "text", align : "center",width : 80, css:"font-size-down", visible : false}, 
	    	{name : "TEMP_UNIT",title : "Temp.<br/>단위",type : "text", align : "center",width : 80, css:"font-size-down", visible : false}, 
	    	{name : "VISC_UNIT",title : "Visc<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "VAP_PRES_UNIT",title : "Vapor<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "SEAL_CHAM_UNIT",title : "Seal Cham.<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "RPM_UNIT",title : "Shaft Speed<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "SHAFT_SIZE_UNIT",title : "Shaft  dia.<br/>단위",type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	
	    	{name : "TEMP_NOR_C",		title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "TEMP_MIN_C",		title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "TEMP_MAX_C",		title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "SPEC_GRAVITY_NOR_C",title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "SPEC_GRAVITY_MIN_C",title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "SPEC_GRAVITY_MAX_C",title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "VISC_NOR_C",		title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "VISC_MIN_C",		title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "VISC_MAX_C",		title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "VAP_PRES_NOR_C",	title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "VAP_PRES_MIN_C",	title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "VAP_PRES_MAX_C",	title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "SEAL_CHAM_NOR_C",	title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "SEAL_CHAM_MIN_C",	title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "SEAL_CHAM_MAX_C",	title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "RPM_NOR_C",		title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	{name : "RPM_MIN_C",		title : "Temp.<br/>Min",type : "text", visible : false},
	    	{name : "RPM_MAX_C",		title : "Temp.<br/>Max",type : "text", visible : false},
	    	{name : "SHAFT_SIZE_C",		title : "Temp.<br/>Rtd",type : "text", visible : false},
	    	
	    	{name : "FTA_YN_TEXT",	title : "FTA",		align : "center",type : "text",width : 100, css:"font-size-down"},
	    	{name : "APPLICATION_TEXT",	title : "Application",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "SERVICE_TEXT",		title : "Service",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "EQUIPMENT_TEXT",	title : "Equipment",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "EQUIPMENT_TYPE_TEXT",title : "Equipment Type",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "QUENCH_TYPE_TEXT",	title : "Quench Type",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "SOLID_GB_TEXT",	title : "Solid/Slurry",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "SOLID_SIZE_NOR",	title : "SOLID_SIZE_NOR",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SOLID_SIZE_MIN",	title : "SOLID_SIZE_MIN",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SOLID_SIZE_MAX",	title : "SOLID_SIZE_MAX",align : "center",type : "text",width : 80, css:"font-size-down"},
	    	{name : "SOLID_SIZE_MAX_CHK",title : "SOLID_SIZE_MAX_CHK",align : "center",type : "text",width : 50, css:"font-size-down"},
	    	{name : "SOLID_CONT",		title : "Solid 농도(ppm)",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "PH",title : "PH",	align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "BRINE_GB_TEXT",	title : "Brine 구분",align : "center",type : "text",width : 160, css:"font-size-down"},
	    	{name : "BRINE_SUB_GB_TEXT",title : "Brine Sub",align : "center",type : "text",width : 160, css:"font-size-down"},
// 	    	{name : "OIL_HRDN_YN_TEXT",	title : "Oil - 굳는성질",align : "center",type : "text",width : 250, css:"font-size-down"},
// 	    	{name : "RESI_CLEAN_GB_TEXT",title : "Residue-Clean",align : "center",type : "text",width : 250, css:"font-size-down"},
// 	    	{name : "RESI_HRDN_GB_TEXT",title : "Residue-굳는성질",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "SEAL_SIZE",		title : "Seal size (In)",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "END_USER_TEXT",	title : "End User",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "GS_GROUP_TEXT",	title : "[GS]Group",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "GS_SERVICE_TEXT",	title : "[GS]Service",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "GS_CASE_TEXT",		title : "[GS]Case",align : "center",type : "text",width : 250, css:"font-size-down"},
// 	    	{name : "SEAL_TYPE_DIR",	title : "Seal Type",		align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "SEAL_INNER_DIR",	title : "Seal Inner",		align : "center",type : "text",width : 100, css:"font-size-down"},
	    	{name : "SEAL_OUTER_DIR",	title : "Seal Outer",		align : "center",type : "text",width : 100, css:"font-size-down"},
// 	    	{name : "MATERIAL_DIR",		title : "Material",			align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "M_IN_1",			title : "M_IN_1",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "M_IN_2",			title : "M_IN_2",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "M_IN_3",			title : "M_IN_3",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "M_IN_4",			title : "M_IN_4",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "M_OUT_1",			title : "M_OUT_1",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "M_OUT_2",			title : "M_OUT_2",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "M_OUT_3",			title : "M_OUT_3",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "M_OUT_4",			title : "M_OUT_4",			align : "center",type : "text",width : 30, css:"font-size-down"},
	    	{name : "API_PLAN_DIR",		title : "API Plan",			align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "S_D_GB_TEXT",		title : "Single/Dual",		align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "API682_YN_TEXT",	title : "API 682 적용",		align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "BELLOWS_YN_TEXT",	title : "Bellows 적용",		align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "CARTRIDGE_TYPE_TEXT",title : "Cartridge",		align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "SEAL_CONFIG_TEXT",	title : "Seal Configuration",align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "SPLIT_YN_TEXT",	title : "Split",			align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "PC_TOXIC_CHK",		title : "Toxic",			align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "PC_HAZARD_CHK",	title : "Hazardous",		align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "PC_FLAM_CHK",		title : "Flammable",		align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "PC_LEAKAGE_CHK",	title : "Crystallizaion/Polymerizaion",align : "center",type : "text",width : 250, css:"font-size-down"},
	    	{name : "PC_HIGH_CORR_CHK",	title : "High Corrosive",	align : "center",type : "text",width : 200, css:"font-size-down"},
	    	{name : "PC_COOL_TROUBLE_CHK",	title : "Cooling으로 인한 Trouble 우려",	align : "center",type : "text",width : 200, css:"font-size-down"},
	    	
	    	//UNIT (CODE= VALUE)
	    	
	    	{name : "FTA_YN",		title : "FTA.",	type : "text", align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "APPLICATION",		title : "Application.",	type : "text", align : "center",width : 80, css:"font-size-down", visible : false}, 
	    	{name : "SERVICE",			title : "Service.",		type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "EQUIPMENT",		title : "Equipment.",	type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "EQUIPMENT_TYPE",	title : "Equipment Type.",	type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "QUENCH_TYPE",		title : "Quench Type.",	type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "SOLID_GB",			title : "Solid/Slurry.",	type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "BRINE_GB",			title : "Brine 구분.",	type : "text",align : "center",width : 160, css:"font-size-down", visible : false},
	    	{name : "BRINE_SUB_GB",		title : "Brine 구분.",	type : "text",align : "center",width : 160, css:"font-size-down", visible : false},
// 	    	{name : "OIL_HRDN_YN",		title : "Oil Hrdn YN.<br/>단위",	type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
// 	    	{name : "RESI_CLEAN_GB",	title : "Resi Clean GB.<br/>단위",	type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
// 	    	{name : "RESI_HRDN_GB",		title : "Resi Hrdn GB.<br/>단위",	type : "text",align : "center",width : 80, css:"font-size-down", visible : false},
	    	{name : "END_USER",			title : "End User.",	align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "GS_GROUP",			title : "Group.",	align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "GS_SERVICE",		title : "Service.",	align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "GS_CASE",			title : "Case.",		align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "S_D_GB",			title : "Single/Dual.",	align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "API682_YN",		title : "API 682 적용.",	align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "BELLOWS_YN",		title : "Bellows 적용.",	align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "CARTRIDGE_TYPE",	title : "Cartridge.",	align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "SEAL_CONFIG",		title : "Seal Configuration.",align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	{name : "SPLIT_YN",			title : "Split.",align : "center",type : "text",width : 80, css:"font-size-down", visible : false},
	    	
	    	//농도 필드
	    	{name : "PRODUCTNM_1",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_1",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_1",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_1",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_2",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_2",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_2",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_2",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_3",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_3",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_3",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_3",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_4",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_4",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_4",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_4",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_5",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_5",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_5",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_5",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_6",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_6",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_6",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_6",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_7",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_7",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_7",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_7",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_8",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_8",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_8",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_8",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_9",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_9",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_9",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_9",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_10",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_10",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_10",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_10",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_11",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_11",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_11",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_11",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	
	    	{name : "PRODUCTNM_12",	title : "productNm",	type : "text",align : "center",width : 160, css:"font-size-down"},
	    	{name : "PRODUCTGB_12",	title : "productGb",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTUT_12",	title : "productUt",	type : "text",align : "center",width : 80, css:"font-size-down"},
	    	{name : "PRODUCTND_12",	title : "productNd",	type : "text",align : "center",width : 80, css:"font-size-down"},
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
		,controller:  {
			loadData : function(filter) {
				return $.ajax({
					type:"POST",
					url:"<c:url value='/ml/getFeatureData.do'/>",
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					contentType: "application/json",
					data: JSON.stringify({
						historyId : _historyId
					})
				}).done(function(result){
					$('#historyMng').modal("hide");
				})
			},
			deleteItem: function(item) {
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
	            .append($("<th>").attr("colspan", 4).width(320).text("Shaft Speed").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("colspan", 2).width(160).text("Shaft Dia.").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(100).text("FTA").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Application").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Service").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Equipment").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Equipment Type").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Quench Type").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Solid/Slurry").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 4).width(290).text("Solid Size(μm)").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("SOLID_CONT").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("PH").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 4).width(320).text("Brine 구분").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	            .append($("<th>").attr("rowspan", 2).width(250).text("Oil - 굳는성질").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	            .append($("<th>").attr("rowspan", 2).width(250).text("Residue - Clean").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	            .append($("<th>").attr("rowspan", 2).width(250).text("Residue - 굳는성질").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Seal size (In)").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("End User").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Group").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Service").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Case").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Seal Type").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 8).width(240).text("Material").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("API Plan").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Single/Dual").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("API 682 적용").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Bellows 적용").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Cartridge").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Seal Configuration").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Split").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Toxic").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Hazardous").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("Flammable").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(250).text("Crystallizaion/Polymerizaion").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("High Corrosive").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(200).text("굳는성질").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product1").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product2").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product3").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product4").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product5").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product6").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product7").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product8").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product9").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product10").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product11").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).attr("colspan", 4).width(400).text("Product12").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            //.append($("<th class='OEM_FEAUTRE'>").attr("colspan", 4).width(320).text("Shaft Speed").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            //.append($("<th class='OEM_FEAUTRE'>").attr("colspan", 2).width(160).text("Shaft Size").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-bottom":"1px solid #e9e9e9"}))
	            .append($("<th>").attr("rowspan", 2).width(80).text("Delete").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            ;
	            
	        $result = $result.add($("<tr>")
	        		.append($("<th>").width(80).text("Unit").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	        		.append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Unit").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
					.append($("<th>").width(80).text("Unit").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Unit").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Unit").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Unit").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	                .append($("<th>").width(80).text("Size").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Rtd.").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Min").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Max").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("단위").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
// 	                .append($("<th class='OEM_FEAUTRE'>").width(80).text("Size").css({"text-align":"center","border-right":"1px solid #e9e9e9"}))
	            );
	    	return $result;
	
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
		   _vCurRow = args.itemIndex;      	   
	       	$("#select_pump_type").val(args.item.PUMP_TYPE);
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
	       	//if (($(document).height()-80) >= 700 ){
	       	//	$('#featureEdit .modal-content').css("height","700px");
			//}else{
				$('#featureEdit .modal-content').css("height",($(document).height()-90)+"px");	
			//}
	       	
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
	     	
	     	//Rule 추가 운전조건 set
	     	hideCondition();
	     	
	     	if(args.item.FTA_YN == null){
	     		$("#select_fta_yn").val("N");
	     	}else{
	     		$("#select_fta_yn").val(args.item.FTA_YN).change();
	     	}
	     	$("#select_application").val(args.item.APPLICATION).change();
	     	$("#select_service").val(args.item.SERVICE).change();
	     	$("#select_equipment").val(args.item.EQUIPMENT);
	     	
	     	$("#select_equipment_type").val(args.item.EQUIPMENT_TYPE);
	     	$("#select_quench_type").val(args.item.QUENCH_TYPE);
	     	
	     	//Seal Size
	     	$("#SEAL_SIZE").val(args.item.SEAL_SIZE);
	     	
	     	//end User조건
	     	$("#select_endUser").val(args.item.END_USER).change();
	     	$("#select_group").val( args.item.GS_GROUP).change();
	     	$("#select_service_gs").val( args.item.GS_SERVICE).change();
	     	$("#select_case").val( args.item.GS_CASE);
	     	
	     	//지정
// 	     	$("#f_seal_type_dir").val(args.item.SEAL_TYPE_DIR);
	     	$("#f_seal_inner_dir").val(args.item.SEAL_INNER_DIR);
	     	$("#f_seal_outer_dir").val(args.item.SEAL_OUTER_DIR);
// 	     	$("#f_material_dir").val(args.item.MATERIAL_DIR);
	     	$("#f_m_in_1").val(args.item.M_IN_1);
	     	$("#f_m_in_2").val(args.item.M_IN_2);
	     	$("#f_m_in_3").val(args.item.M_IN_3);
	     	$("#f_m_in_4").val(args.item.M_IN_4);
	     	$("#f_m_out_1").val(args.item.M_OUT_1);
	     	$("#f_m_out_2").val(args.item.M_OUT_2);
	     	$("#f_m_out_3").val(args.item.M_OUT_3);
	     	$("#f_m_out_4").val(args.item.M_OUT_4);
	     	$("#f_api_plan_dir").val(args.item.API_PLAN_DIR);
	     	$("#select_s_d_gb").val(args.item.S_D_GB); //single/dual
	     	
	     	//Requirement
	     	if(args.item.API682_YN == null){
	     		$("#select_api682_yn").val("Y");
	     	}else{
	     		$("#select_api682_yn").val(args.item.API682_YN);
	     	}
	
	     	$("#select_bellows_yn").val(args.item.BELLOWS_YN);
	     	$("#select_cartridge_type").val(args.item.CARTRIDGE_TYPE);
	     	$("#select_seal_config").val(args.item.SEAL_CONFIG);
	     	$("#select_split_yn").val(args.item.SPLIT_YN);
	     	
	     	if(args.item.PC_TOXIC_CHK=="Y"){
	     		$("#pc_toxic_chk").prop("checked", true);
	     	}else{
	     		$("#pc_toxic_chk").prop("checked", false);
	     	};
	     	if(args.item.PC_HAZARD_CHK=="Y"){
	     		$("#pc_hazard_chk").prop("checked", true);
	     	}else{
	     		$("#pc_hazard_chk").prop("checked", false);
	     	};
	     	if(args.item.PC_FLAM_CHK=="Y"){
	     		$("#pc_flam_chk").prop("checked", true);
	     	}else{
	     		$("#pc_flam_chk").prop("checked", false);
	     	};
	     	if(args.item.PC_LEAKAGE_CHK=="Y"){
	     		$("#pc_leakage_chk").prop("checked", true);
	     	}else{
	     		$("#pc_leakage_chk").prop("checked", false);
	     	};
	     	if(args.item.PC_HIGH_CORR_CHK=="Y"){
	     		$("#pc_high_corr_chk").prop("checked", true);
	     	}else{
	     		$("#pc_high_corr_chk").prop("checked", false);
	     	};
	     	if(args.item.PC_COOL_TROUBLE_CHK=="Y"){
	     		$("#pc_cool_trouble_chk").prop("checked", true);
	     	}else{
	     		$("#pc_cool_trouble_chk").prop("checked", false);
	     	};
	     	
	     	//solid
	     	$("#select_solid_gb_yn").val(args.item.SOLID_GB);
	     	if(args.item.SOLID_GB == "Y1"){ // solid 상세입력
				$("#divSolidSize").css('display','block');
				$("#solid_size_nor").val(args.item.SOLID_SIZE_NOR);
		     	$("#solid_size_min").val(args.item.SOLID_SIZE_MIN);
		     	$("#solid_size_max").val(args.item.SOLID_SIZE_MAX);
		     	$("#solid_size_check").prop('checked', args.item.SOLID_SIZE_MAX_CHK=="Y"?true:false);
				$("#divSolidCont").css('display','block');
		     	$("#solid_cont").val(args.item.SOLID_CONT);
			}
	     	
	     	//Product 조건
	     	$("#divProducts").empty();
			var product = args.item.PRODUCT;
			var productItem ="";
			var paramData = {
				PRODUCT : product
			};
			$.ajax({
				type : "POST",
				url: "<c:url value='/rb/getProductGrp.do'/>",
				data : JSON.stringify(paramData),
				contentType: "application/json;charset=UTF-8",
				success : function(data){
					var dataStr = data;
					//product 별 농도단위, 구분, 농도 입력 필드 활성화 처리
					
					//CRUID OIL 유무
					var isCruidOil = false;
					for(var i=0; i<dataStr.length; i++){
						if(dataStr[i].product == "CRUDE OIL") {
							isCruidOil = true;
							break;
						}
					}
					
					for(var str in args.item){
						if(str.startsWith("PRODUCTNM") ){
							var productNameVal = eval("args.item."+str);
							if (productNameVal !="") {
								for(var i=0; i<dataStr.length; i++){
									if(dataStr[i].product == productNameVal){
										//Product별 추가 필드 처리
// 										if(dataStr[i].product == "SLURRY"){	
// 											$("#select_solid_gb_yn").val(args.item.SOLID_GB);
										if(dataStr[i].product_grp == "BRINE"){
											$("#divBrine").css('display','block');
											$("#select_brine_gb").val(args.item.BRINE_GB);
											if(args.item.BRINE_GB == "Z110010"){
												$("#select_brine_sub_gb").css('display','block');
										     	$("#select_brine_sub_gb").val(args.item.BRINE_SUB_GB);
											}
										}else if(dataStr[i].product == "OIL"){
// 											$("#divOil").css('display','block');
// 											$("#select_oil_hrdn_yn").val(args.item.OIL_HRDN_YN);
										}else if(dataStr[i].product == "RESIDUE"){
// 											$("#divResidue").css('display','block');
// 											$("#select_resi_clean_gb").val(args.item.RESI_CLEAN_GB);
// 								     		$("#select_resi_hrdn_gb").val(args.item.RESI_HRDN_GB);
										}else if(dataStr[i].product_grp == "CHLORIDE"){
										//}else if(dataStr[i].product_grp == "CHLORINE"){	
											$("#divChloride").css('display','block');
								     		$("#chloride_cont").val(args.item.CHLORIDE_CONT);
								     		$("#PH").val(args.item.PH);
										}
									}
								}
							}
						}
					}
						
					//Grid의 Product Info 정보에 따라 항목을 구성
					for(var str in args.item){
						if(str.startsWith("PRODUCTNM") ){
							var productNameVal = eval("args.item."+str);
							if (isNotEmpty(productNameVal)) {
								for(var i=0; i<dataStr.length; i++){
									if(dataStr[i].product == productNameVal){
										var productItem = getRuleProductInputItem(i+1,dataStr[i],isCruidOil);
										$("#divProducts").append(productItem);
										var productNameTrim = dataStr[i].product.replace(/ /gi, "_");//모든 공백제거
										for(var j=0; j<12; j++){
											if(productNameVal == args.item["PRODUCTNM_"+j]){
												//value set
												$("#"+productNameTrim+"_GB").val(args.item["PRODUCTGB_"+j]);
						 						$("#"+productNameTrim+"_UT").val(args.item["PRODUCTUT_"+j]);
						 						$("#"+productNameTrim+"_ND").val(args.item["PRODUCTND_"+j]);
											}
										}
									}
								}
							}
						}
					}
				}
			});
			
			$('#alert').removeClass("show"); // Warning Message는 Hide 처리
			$('#alert').css("display","none");
			
			$('#featureEdit').modal("show");

	    }
	});
}

function getRuleProductInputItem(i, item, isCruidOil){
	
	var productItem = "";

	if(item.product == "-" || item.product == "" || 
			(item.product == "WATER" && !isCruidOil) ){
		productItem="";
	}else{
		
		var productNameTrim = item.product.replace(/ /gi, "_");//모든 공백제거 (공백을 "_" 로 대체한다)
		
		var productGrpNameTrim = item.product_grp.replace(/ /gi, "_");//모든 공백제거 (공백을 "_" 로 대체한다)
		
 		productItem +="<div class='row'>";
 		productItem +="<div class='col-7' style='padding-right: 0px;'>";
		productItem +="<div class='p-2 custom-responsive-p2' style='min-width: 120px;'>";
		productItem +="<div class='d-flex'>";
		productItem +="<div style='width: 35%'>";
		productItem +="<strong id='"+productNameTrim+"' class='_productContInput'>"+item.product+"</strong>";
		productItem +="</div>";
		
		if(typeof item.product_gb != "undefined"){
			
			//구분 선택박스 비활성화 유무
			var disabled = "";
			if(item.product_gb.length <2){
				disabled = "disabled";
			}
			
			productItem +="<div style='width: 40%'>";
			productItem +="<select id='"+productNameTrim+"_GB' style='width: 100%; height: 25px; padding: 0px;'class='selectpicker form-control' "+disabled+">";
			for(var j=0; j< item.product_gb.length; j++){
				productItem +="<option value='"+item.product_gb[j].PRODUCT_GB+"'>"+item.product_gb[j].PRODUCT_GB+"</option>";
			}
			productItem +="</select>";
			productItem +="</div>";
		}else{
			productItem +="<div style='width: 40%'><select id='"+productNameTrim+"_GB'style='width: 100%; height: 25px; padding: 0px;'class='selectpicker form-control' disabled><option value=''>-</option></select></div>";
		}
		
		//농도필드 활성화 유무
		var contEnable = "";
		
		// 상위그룹명으로 체크함.
		// SOLID = SLURRY
		// CAUSTIC = SODIUM HYDROXIDE
		// H2SO4 = SULFURIC ACID
		// SULFUR
		// CHLORIDE  -> CHLORINE으로 변경됨. -> CHLORIDE로 다시 변경
		// WATER
		// H2S -> HYDROGEN SULFIDE
		//alert(productGrpNameTrim);
		if(item.cont_yn == "Y" ||
				(//productGrpNameTrim == "SLURRY" ||
						productGrpNameTrim == "CAUSTIC" ||
						//productNameTrim == "SODIUM HYDROXIDE" ||
						productGrpNameTrim == "SULFURIC_ACID" ||
						productGrpNameTrim == "SULFUR" ||
						productGrpNameTrim == "CHLORIDE" ||
						//productGrpNameTrim == "CHLORINE" ||
						productGrpNameTrim == "HYDROGEN_SULFIDE" ||
						(productGrpNameTrim == "WATER" && isCruidOil)
				)
			){
			contEnable = "";
		}else{
			contEnable = "disabled";
		}
		
		productItem +="<div style='width: 25%' class='pl-1 pr-0'>";
		productItem +="<select id='"+productNameTrim+"_UT' style='width: 100%; height: 25px; padding: 0px;'class='selectpicker form-control' "+contEnable+">";
		productItem +="<option value='%'>%</option>";
		productItem +="<option value='PPM'>PPM</option>";
		productItem +="</select>";
		productItem +="</div></div></div></div>";
		
		productItem +="<div class='col-5' style='padding-left: 0px;'>";
		productItem +="<div class='pt-2 custom-responsive-p2 pl-1 pr-1'>";
		productItem +="<div class='d-flex'>";
		productItem +="<input id='"+productNameTrim+"_ND' type='number' class='form-control form-control-sm' maxlength='20' style='height: 25px; width: 100%;' placeholder='농도' "+contEnable+">";
		productItem +="</div></div></div>";
		
		productItem +="</div>";
	}
	
	return productItem;
	
}

/**
 * 추천 처리
 */
function predictAll(predictList){
	
	if(predictList.length == 0){
		alert("추천조건을 등록하세요");	
		return;
	}

	var loadingMsg = new loading_bar({message:"Predicting..."});
	
	var cUrl = "";
	if (_viewType=="1"){ <%-- 모델추천 --%>
// 		cUrl="<c:url value='/ml/predictMulti1.do'/>";
		cUrl="<c:url value='/re/predictSeal.do'/>";
	}else if (_viewType=="2"){ <%-- 데이터조회후추천 --%>
//		cUrl="<c:url value='/ml/predictMulti2.do'/>";
		cUrl="<c:url value='/re/predictSeal2.do'/>";
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
			target3_check : $("#target3checked").is(":checked"),
			target4_check : $("#target4checked").is(":checked")    // Rule Based 
		}),
		beforeSend: function(xhr) {
	       xhr.setRequestHeader("AJAX", true);
           loadingMsg.show();
	     },
	     complete: function () {
	     	setTimeout(function() {
	     		loadingMsg.modal('hide');
	     	},500);
		 }
	}).done(function(data){
		var presult = data.RESULT;
		var rpresult = data.RULE_RESULT;
		
		console.log(data);
		
		if (data.API_RESULT != "undefined"){
			if(data.API_RESULT == "FAIL"){
				alert("API 서버 통신과 실패하였습니다.");
				return false;
			}
		}
		
		if( presult == null ){
			alert("처리중 오류가 발생하였습니다");
			return false;
		}
		
		if (typeof presult.ERR_MSG != "undefined"){
			alert(presult.ERR_MSG);
			return false;
		}
		if(data.hasOwnProperty("AS_HISTORY")){
			_asHistoryData = data.AS_HISTORY;
		}
		var noteIdSeq = -1;//Note 구분석 체크 index
		for(var i=0;i<presult.length;i++){
			// 추천결과 그리드 Set
			setPredictData("predictGrid1_"+presult[i].predict_idx, presult[i].RESULT.SEAL_TYPE, "Seal Type");
			setPredictData("predictGrid2_"+presult[i].predict_idx, presult[i].RESULT.API_PLAN, "API Plan");
			setASHistoryData("predict","predictGrid3_"+presult[i].predict_idx, presult[i].RESULT.CONN_COL, "Seal Type|Material|API Plan",setPredictData);
// 			setPredictData("predictGrid3_"+presult[i].predict_idx, presult[i].RESULT.CONN_COL, "Seal Type|Material|API Plan"); //3번째 그리드 데이터부분
			
			// rule based 그리드 Set
			
			
//  			setRuleBasedGrid("ruleBasedGrid_"+rpresult[i].predict_idx, rpresult[i].RESULT.RST, "");
			
			for(var j=0; j<rpresult[i].RESULT.NOTE.length; j++){
				/* $("#noteData_"+rpresult[i].predict_idx).append(rpresult[i].RESULT.NOTE[j].P_IDX+". "+rpresult[i].RESULT.NOTE[j].NOTE+"<br>"); */
				if(noteIdSeq != parseInt(rpresult[i].RESULT.NOTE[j].P_ID_SEQ)){
					if(noteIdSeq >= 0){
						//index 구분선
						$("#noteData_"+rpresult[i].predict_idx).append("-------------------------------------------------------------------<br>");	
					}
					noteIdSeq = parseInt(rpresult[i].RESULT.NOTE[j].P_ID_SEQ);
				}
				$("#noteData_"+rpresult[i].predict_idx).append(rpresult[i].RESULT.NOTE[j].P_ID_SEQ+". "+rpresult[i].RESULT.NOTE[j].NOTE+"<br>");	
			}
			
			if(rpresult[i].RESULT.NOTE.length > 0){
				$("#noteMsg_"+rpresult[i].predict_idx).html("Note 수 : " + rpresult[i].RESULT.NOTE.length + "건");
			}
			
			for(var j=0; j<rpresult[i].RESULT.PROC.length; j++){
				$("#prdtProc_"+rpresult[i].predict_idx).append(rpresult[i].RESULT.PROC[j].P_IDX+". "+rpresult[i].RESULT.PROC[j].PROC_CONT+"<br>");
			}
			
			var featureGridDatas = $("#jsGrid_feature").jsGrid("option", "data");
			var featureGridData = null;
    		for(var k=0;k<featureGridDatas.length;k++){
    			if (presult[i].predict_idx == featureGridDatas[k].NO){
    				featureGridData = featureGridDatas[k]; 
    				featureGridData.TEMP_NOR_C = presult[i].param_cnv.TEMP_NOR;
    				featureGridData.TEMP_MIN_C = presult[i].param_cnv.TEMP_MIN;
    				featureGridData.TEMP_MAX_C = presult[i].param_cnv.TEMP_MAX;
    				featureGridData.SPEC_GRAVITY_NOR_C = presult[i].param_cnv.SPEC_GRAVITY_NOR;
    				featureGridData.SPEC_GRAVITY_MIN_C = presult[i].param_cnv.SPEC_GRAVITY_MIN;
    				featureGridData.SPEC_GRAVITY_MAX_C = presult[i].param_cnv.SPEC_GRAVITY_MAX;
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
    				//추가된 운전 조건
//     				featureGridData.APPLICATION_TEXT = rpresult[i].param.APPLICATION_TEXT;
//     				featureGridData.BRINE_GB_TEXT = rpresult[i].param.BRINE_GB_TEXT;
//     				featureGridData.EQUIPMENT_TEXT = rpresult[i].param.EQUIPMENT_TEXT;
//     				featureGridData.EQUIPMENT_TYPE_TEXT = rpresult[i].param.EQUIPMENT_TYPE_TEXT;
//     				featureGridData.OIL_HRDN_YN_TEXT = rpresult[i].param.OIL_HRDN_YN_TEXT;
//     				featureGridData.PH = rpresult[i].param.PH;
//     				featureGridData.QUENCH_TYPE_TEXT = rpresult[i].param.QUENCH_TYPE_TEXT;
//     				featureGridData.RESI_CLEAN_GB_TEXT = rpresult[i].param.RESI_CLEAN_GB_TEXT;
//     				featureGridData.RESI_HRDN_GB_TEXT = rpresult[i].param.RESI_HRDN_GB_TEXT;
//     				featureGridData.SERVICE_TEXT = rpresult[i].param.SERVICE_TEXT;
//     				featureGridData.SOLID_SIZE_MAX = rpresult[i].param.SOLID_SIZE_MAX;
//     				featureGridData.SOLID_SIZE_MIN = rpresult[i].param.SOLID_SIZE_MIN;
//     				featureGridData.SOLID_SIZE_NOR = rpresult[i].param.SOLID_SIZE_NOR;
//     				featureGridData.SULFUR_CONT = rpresult[i].param.SULFUR_CONT;
    				break;
    			}
    		}
    		$("#jsGrid_feature").jsGrid("refresh");
    		
    		console.log(featureGridData);
    		
    		<%-- 추천인자 표시 추가 --%>
    		var predictItem = "";
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> Product Grouping. ";
			predictItem +=" <div class='d-flex'>";
			predictItem +=" <div class='predict-Feature'>"+"</div>";
			predictItem +=" <div class='predict-Feature' id='ProductGroup"+i+"'>"+"</div>";
			predictItem +=" <div class='predict-Feature'>"+""+"</div>";
			predictItem +=" </div></div>";
			
			predictItem +=" <div class='predict-Feature_h' style='float:left;'> Temp. ("+featureGridData.TEMP_TEXT+") ";
			predictItem +=" <div class='d-flex'>";
			/*입력한 추천값과 모델의 MIN , MAX 값을 비교  */
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
		
			if ($("#f_predict_sel").val()=="OEM"){
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
			
			//추가된 운전 조건
			if (isNotEmpty(featureGridData.FTA_YN_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> FTA";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("FTA_YN_TEXT",featureGridData.FTA_YN_TEXT,featureGridData.FTA_YN_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.APPLICATION_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Application";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("APPLICATION_TEXT",featureGridData.APPLICATION_TEXT,featureGridData.APPLICATION_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.SERVICE_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Service";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("SERVICE_TEXT",featureGridData.SERVICE_TEXT,featureGridData.SERVICE_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.EQUIPMENT_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Equipment";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("EQUIPMENT_TEXT",featureGridData.EQUIPMENT_TEXT,featureGridData.EQUIPMENT_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.EQUIPMENT_TYPE_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Equipment Type";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("EQUIPMENT_TYPE_TEXT",featureGridData.EQUIPMENT_TYPE_TEXT,featureGridData.EQUIPMENT_TYPE_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.QUENCH_TYPE_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Quench Type";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("QUENCH_TYPE_TEXT",featureGridData.QUENCH_TYPE_TEXT,featureGridData.QUENCH_TYPE_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.SOLID_SIZE_NOR)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Solid Size(μm)";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd_style("SOLID_SIZE_NOR",featureGridData.SOLID_SIZE_NOR,featureGridData.SOLID_SIZE_NOR,"")+""; 
				predictItem +=""+setFeatureGridAdd_style("SOLID_SIZE_MIN",featureGridData.SOLID_SIZE_MIN,featureGridData.SOLID_SIZE_MIN,"")+""; 
				predictItem +=""+setFeatureGridAdd_style("SOLID_SIZE_MAX",featureGridData.SOLID_SIZE_MAX,featureGridData.SOLID_SIZE_MAX,"")+"";
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.SOLID_CONT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Solid 농도(ppm)";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("SOLID_CONT",featureGridData.SOLID_CONT,featureGridData.SOLID_CONT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.PH)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> PH";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("PH",featureGridData.PH,featureGridData.PH)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.BRINE_GB_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Brine 구분";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd_style("BRINE_GB_TEXT",featureGridData.BRINE_GB_TEXT,featureGridData.BRINE_GB_TEXT, "")+""; 
				predictItem +=""+setFeatureGridAdd_style("BRINE_SUB_GB_TEXT",featureGridData.BRINE_SUB_GB_TEXT,featureGridData.BRINE_SUB_GB_TEXT, "")+""; 
				predictItem +=" </div></div>";
			}
// 			if (isNotEmpty(featureGridData.OIL_HRDN_YN_TEXT)){
// 				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Oil - 굳는성질";
// 				predictItem +=" <div class='d-flex'>";
// 				predictItem +=""+setFeatureGridAdd("OIL_HRDN_YN_TEXT",featureGridData.OIL_HRDN_YN_TEXT,featureGridData.OIL_HRDN_YN_TEXT)+""; 
// 				predictItem +=" </div></div>";
// 			}
// 			if (isNotEmpty(featureGridData.RESI_CLEAN_GB_TEXT)){
// 				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Residue - Clean";
// 				predictItem +=" <div class='d-flex'>";
// 				predictItem +=""+setFeatureGridAdd("RESI_CLEAN_GB_TEXT",featureGridData.RESI_CLEAN_GB_TEXT,featureGridData.RESI_CLEAN_GB_TEXT)+""; 
// 				predictItem +=" </div></div>";
// 			}
// 			if (isNotEmpty(featureGridData.RESI_HRDN_GB_TEXT)){
// 				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Residue - 굳는성질";
// 				predictItem +=" <div class='d-flex'>";
// 				predictItem +=""+setFeatureGridAdd("RESI_HRDN_GB_TEXT",featureGridData.RESI_HRDN_GB_TEXT,featureGridData.RESI_HRDN_GB_TEXT)+""; 
// 				predictItem +=" </div></div>";
// 			}
			if (isNotEmpty(featureGridData.END_USER_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> End User";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("END_USER_TEXT",featureGridData.END_USER_TEXT,featureGridData.END_USER_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.GS_GROUP_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> [GS]Service";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("GS_GROUP_TEXT",featureGridData.GS_GROUP_TEXT,featureGridData.GS_GROUP_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.GS_SERVICE_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> [GS]Group";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("GS_SERVICE_TEXT",featureGridData.GS_SERVICE_TEXT,featureGridData.GS_SERVICE_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.GS_CASE_TEXT)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> [GS]Case";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("GS_CASE_TEXT",featureGridData.GS_CASE_TEXT,featureGridData.GS_CASE_TEXT)+""; 
				predictItem +=" </div></div>";
			}
// 			if (isNotEmpty(featureGridData.SEAL_TYPE_DIR)){
// 				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Seal Type";
// 				predictItem +=" <div class='d-flex'>";
// 				predictItem +=""+setFeatureGridAdd("SEAL_TYPE_DIR",featureGridData.SEAL_TYPE_DIR,featureGridData.SEAL_TYPE_DIR)+""; 
// 				predictItem +=" </div></div>";
// 			}
			if (isNotEmpty(featureGridData.SEAL_INNER_DIR)||isNotEmpty(featureGridData.SEAL_OUTER_DIR)){
				predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Seal Type";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd_style("SEAL_INNER_DIR",featureGridData.SEAL_INNER_DIR,featureGridData.SEAL_INNER_DIR,"")+""; 
				predictItem +=""+setFeatureGridAdd_style("SEAL_OUTER_DIR",featureGridData.SEAL_OUTER_DIR,featureGridData.SEAL_OUTER_DIR,"")+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.M_IN_1)||isNotEmpty(featureGridData.M_IN_2)||isNotEmpty(featureGridData.M_IN_3)||isNotEmpty(featureGridData.M_IN_4)||isNotEmpty(featureGridData.M_OUT_1)||isNotEmpty(featureGridData.M_OUT_2)||isNotEmpty(featureGridData.M_OUT_3)||isNotEmpty(featureGridData.M_OUT_4)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Material";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("M_IN_1",featureGridData.M_IN_1,featureGridData.M_IN_1)+""; 
				predictItem +=""+setFeatureGridAdd("M_IN_2",featureGridData.M_IN_2,featureGridData.M_IN_2)+""; 
				predictItem +=""+setFeatureGridAdd("M_IN_3",featureGridData.M_IN_3,featureGridData.M_IN_3)+""; 
				predictItem +=""+setFeatureGridAdd("M_IN_4",featureGridData.M_IN_4,featureGridData.M_IN_4)+""; 
				predictItem +=""+setFeatureGridAdd("M_OUT_1",featureGridData.M_OUT_1,featureGridData.M_OUT_1)+""; 
				predictItem +=""+setFeatureGridAdd("M_OUT_2",featureGridData.M_OUT_2,featureGridData.M_OUT_2)+""; 
				predictItem +=""+setFeatureGridAdd("M_OUT_3",featureGridData.M_OUT_3,featureGridData.M_OUT_3)+""; 
				predictItem +=""+setFeatureGridAdd("M_OUT_4",featureGridData.M_OUT_4,featureGridData.M_OUT_4)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.API_PLAN_DIR)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> API Plan";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("API_PLAN_DIR",featureGridData.API_PLAN_DIR,featureGridData.API_PLAN_DIR)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.S_D_GB_TEXT)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Single/Dual";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("S_D_GB_TEXT",featureGridData.S_D_GB_TEXT,featureGridData.S_D_GB_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.API682_YN_TEXT)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;wdth:80px;'> API 682 적용";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd_style("API682_YN_TEXT",featureGridData.API682_YN_TEXT,featureGridData.API682_YN_TEXT, "width:80px;")+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.BELLOWS_YN_TEXT)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Bellows 적용";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("BELLOWS_YN_TEXT",featureGridData.BELLOWS_YN_TEXT,featureGridData.BELLOWS_YN_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.CARTRIDGE_TYPE_TEXT)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Cartridge";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("CARTRIDGE_TYPE_TEXT",featureGridData.CARTRIDGE_TYPE_TEXT,featureGridData.CARTRIDGE_TYPE_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.SEAL_CONFIG_TEXT)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Seal Configuration";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("SEAL_CONFIG_TEXT",featureGridData.SEAL_CONFIG_TEXT,featureGridData.SEAL_CONFIG_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.SPLIT_YN_TEXT)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Split";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("SPLIT_YN_TEXT",featureGridData.SPLIT_YN_TEXT,featureGridData.SPLIT_YN_TEXT)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.PC_TOXIC_CHK)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Toxic";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("PC_TOXIC_CHK",featureGridData.PC_TOXIC_CHK,featureGridData.PC_TOXIC_CHK)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.PC_HAZARD_CHK)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Hazardous";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("PC_HAZARD_CHK",featureGridData.PC_HAZARD_CHK,featureGridData.PC_HAZARD_CHK)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.PC_FLAM_CHK)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Flammable";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("PC_FLAM_CHK",featureGridData.PC_FLAM_CHK,featureGridData.PC_FLAM_CHK)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.PC_LEAKAGE_CHK)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Crystallizaion/Polymerizaion";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("PC_LEAKAGE_CHK",featureGridData.PC_LEAKAGE_CHK,featureGridData.PC_LEAKAGE_CHK)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.PC_HIGH_CORR_CHK)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> High Corrosive";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("PC_HIGH_CORR_CHK",featureGridData.PC_HIGH_CORR_CHK,featureGridData.PC_HIGH_CORR_CHK)+""; 
				predictItem +=" </div></div>";
			}
			if (isNotEmpty(featureGridData.PC_COOL_TROUBLE_CHK)){
				predictItem +=" <div class='predict-Feature_h' style='float:left;'> Cooling으로 인한 Trouble 우려";
				predictItem +=" <div class='d-flex'>";
				predictItem +=""+setFeatureGridAdd("PC_COOL_TROUBLE_CHK",featureGridData.PC_COOL_TROUBLE_CHK,featureGridData.PC_COOL_TROUBLE_CHK)+""; 
				predictItem +=" </div></div>";
			}
			
			//Product 별 농도 정보1~ 12
			for(var pi=1;pi<=12;pi++){
				if (isNotEmpty(eval("featureGridData.PRODUCTNM_"+pi))){
					predictItem +=" <div class='predict-Feature-Add_h' style='float:left;'> Product"+pi;
					predictItem +=" <div class='d-flex'>";
					//predictItem +=""+setFeatureGridAdd_style("PRODUCTNM_"+pi, eval("featureGridData.PRODUCTNM_"+pi), eval("featureGridData.PRODUCTNM_"+pi), "min-width:150px;width:150px")+"";
					//predictItem +=""+setFeatureGridAdd_style("PRODUCTGB_"+pi, eval("featureGridData.PRODUCTGB_"+pi), eval("featureGridData.PRODUCTGB_"+pi), "min-width:100px;width:100px")+"";
					//predictItem +=""+setFeatureGridAdd_style("PRODUCTUT_"+pi, eval("featureGridData.PRODUCTUT_"+pi), eval("featureGridData.PRODUCTUT_"+pi), "min-width:50px;width:50px")+""; 
					//predictItem +=""+setFeatureGridAdd_style("PRODUCTND_"+pi, eval("featureGridData.PRODUCTND_"+pi), eval("featureGridData.PRODUCTND_"+pi), "min-width:50px;width:50px")+"";
					predictItem +=""+setFeatureGridAdd_style("PRODUCTNM_"+pi, eval("featureGridData.PRODUCTNM_"+pi), eval("featureGridData.PRODUCTNM_"+pi), "")+"";
					predictItem +=""+setFeatureGridAdd_style("PRODUCTGB_"+pi, eval("featureGridData.PRODUCTGB_"+pi), eval("featureGridData.PRODUCTGB_"+pi), "")+"";
					predictItem +=""+setFeatureGridAdd_style("PRODUCTUT_"+pi, eval("featureGridData.PRODUCTUT_"+pi), eval("featureGridData.PRODUCTUT_"+pi), "")+""; 
					predictItem +=""+setFeatureGridAdd_style("PRODUCTND_"+pi, eval("featureGridData.PRODUCTND_"+pi), eval("featureGridData.PRODUCTND_"+pi), "")+"";
					predictItem +=" </div></div>";
				}
			}
			
			<%-- 추천인자 표시 추가 end --%>
			$("#predict_condition_"+presult[i].predict_idx +"").prepend(predictItem); // 입력인자 결과 화면에 표시
			
			//$("#ProductGroup"+i).text(presult[i].ProductGroupInfo); //추천조건에 입력한 Product Grouping 정보
    		$("#ProductGroup"+i).text(rpresult[i].ProductGroupInfo); //추천조건에 입력한 Product Grouping 정보 - - fnfrlwns
    		
			// ------------------------------------------
			// Preferred , Support, API682 아이콘 표시
			// ------------------------------------------
			var conn_Col = presult[i].RESULT.CONN_COL; //조회결과 추천값
			var vPumpType = $("#f_equip_type_sel").val(); // Equip Type
			if(conn_Col != undefined ){ //conn_Col.length 로 체크하게되면 undefined인 값에는 length 속성 자체가 없기때문에 오류가 발생한다.  
				for(k=0; k < conn_Col.length; k++){ //seal type 분리용 
					var gridRowObj = $('#predictGrid3_'+presult[i].predict_idx).jsGrid('option', 'data')[k]; //k번째의 row를  gridRowObj선언.
					var	classFull = conn_Col[k].CLASS.split("|");
					var sealTypeFull = $.trim(classFull[0]); //공백이 존재하여 제거필요
					
					//sealTypeList_PSA
					
					// Preferred 와 Support는 각 Seal Type 별로 부여하여 결합하여 표시한다.
					var sealTypes = sealTypeFull.split("/");
					var vPreferredAndSupportDisplay="";
					for(var si=0;si<sealTypes.length;si++){
						for(var z = 0; z<sealTypeList_PSA.result.length; z++){
							 if(sealTypes[si] == sealTypeList_PSA.result[z].SEAL_TYPE && vPumpType == sealTypeList_PSA.result[z].EQUIP_TYPE){
								//console.log(sealTypeList_P.result[z]);	
								if(sealTypeList_PSA.result[z].R_TYPE == "P"){
									//vPreferredAndSupportDisplay +='<i class="fas fa-check-circle" style="color:blue;"></i>/'; //k번째의 row에 값 셋팅.
									vPreferredAndSupportDisplay +='<span class="badge badge-primary">P</span>/';
								}
								if(sealTypeList_PSA.result[z].R_TYPE == "S"){
									//vPreferredAndSupportDisplay +='<i class="fas fa-check-circle" style="color:blue;"></i>/'; //k번째의 row에 값 셋팅.
									vPreferredAndSupportDisplay +='<span class="badge badge-danger">S</span>/';
								}
								break;
							}
						}
					}
					if (vPreferredAndSupportDisplay !=""){
						if (vPreferredAndSupportDisplay.length>0) vPreferredAndSupportDisplay = vPreferredAndSupportDisplay.substring(0,vPreferredAndSupportDisplay.length-1);
						//console.log(vPreferredAndSupportDisplay)
						gridRowObj.SEALTYPE_P =vPreferredAndSupportDisplay
						$('#predictGrid3_'+presult[i].predict_idx).jsGrid("refresh");
					}
					
					//API682는 결합된 전체 Seal Type 정보와 비교하여 부여
					for(j = 0; j<sealTypeList_PSA.result.length; j++){
						if(sealTypeFull == sealTypeList_PSA.result[j].SEAL_TYPE && sealTypeList_PSA.result[j].R_TYPE == 'A'){
							//console.log(sealTypeList_A.result[j]);	
							//gridRowObj.SEALTYPE_A ='<i class="fas fa-check-circle" style="color:red;"></i>'; //k번째의 row에 값 셋팅.
							gridRowObj.SEALTYPE_A = '<span class="badge badge-success">A</span>';
							$('#predictGrid3_'+presult[i].predict_idx).jsGrid("refresh");
							break;
						}
					}
				} 
			
				$('#predictGrid3_'+presult[i].predict_idx + ' .jsgrid-grid-body').css('height',"140px");
				//console.log(gridId + " : " + $("#"+gridId + ' .jsgrid-grid-body').css('height'));
			}  
			
			// --------------------------------------------
			// Preferred , Support, API682 아이콘 표시 End
			// --------------------------------------------
			
			
			if($.trim(presult[i].predict_msg) == "complete"){
				$("#predict_status"+presult[i].predict_idx).css("color","#fd7e14");//complete
				$("#predictbtn"+presult[i].predict_idx).trigger("click");
				//$("#predictbtn"+(i+1)).trigger("click");//btn
			}else{
				$("#predict_status"+presult[i].predict_idx).css("color","#dc3545");			
			}
			$("#predict_status"+presult[i].predict_idx).html(presult[i].predict_msg);
		}
		
		//Rule 결과 Set
		for(var i=0;i<rpresult.length;i++){
			// 추천결과 그리드 Set
			// rule based 그리드 Set
			
 			setASHistoryData("rule","ruleBasedGrid_"+rpresult[i].predict_idx, rpresult[i].RESULT.RST, "",setRuleBasedGrid);
//  			setRuleBasedGrid("ruleBasedGrid_"+rpresult[i].predict_idx, rpresult[i].RESULT.RST, "");
					
 			// ------------------------------------------
			// Preferred , Support, API682 아이콘 표시
			// ------------------------------------------
			var rule_seal_type = rpresult[i].RESULT.RST; //결과
			var vPumpType = $("#f_equip_type_sel").val(); // Equip Type
			
			console.log("=rule_seal_type=> : " + rule_seal_type);
			
			if(rule_seal_type != undefined ){ 
				for(k=0; k < rule_seal_type.length; k++){ //seal type 분리용 
					var gridRowObj = $('#ruleBasedGrid_'+rpresult[i].predict_idx).jsGrid('option', 'data')[k]; //k번째의 row를  gridRowObj선언.
					var	sealTypeFull = rule_seal_type[k].SEAL;
					console.log("==> " + k + " : " + sealTypes);
					// Preferred 와 Support는 각 Seal Type 별로 부여하여 결합하여 표시한다.
					var sealTypes = sealTypeFull.split("/");
					var vPreferredAndSupportDisplay="";
					for(var si=0;si<sealTypes.length;si++){
						for(var z = 0; z<sealTypeList_PSA.result.length; z++){
							 if(sealTypes[si] == sealTypeList_PSA.result[z].SEAL_TYPE && vPumpType == sealTypeList_PSA.result[z].EQUIP_TYPE){
								if(sealTypeList_PSA.result[z].R_TYPE == "P"){
									vPreferredAndSupportDisplay +='<span class="badge badge-primary">P</span>/';
								}
								if(sealTypeList_PSA.result[z].R_TYPE == "S"){
									vPreferredAndSupportDisplay +='<span class="badge badge-danger">S</span>/';
								}
								break;
							}
						}
					}
					if (vPreferredAndSupportDisplay !=""){
						if (vPreferredAndSupportDisplay.length>0) vPreferredAndSupportDisplay = vPreferredAndSupportDisplay.substring(0,vPreferredAndSupportDisplay.length-1);
						gridRowObj.SEALTYPE_P = vPreferredAndSupportDisplay
						$('#ruleBasedGrid_'+rpresult[i].predict_idx).jsGrid("refresh");
					}
					
					//API682는 결합된 전체 Seal Type 정보와 비교하여 부여
					for(j = 0; j<sealTypeList_PSA.result.length; j++){
						if(sealTypeFull == sealTypeList_PSA.result[j].SEAL_TYPE && sealTypeList_PSA.result[j].R_TYPE == 'A'){
							gridRowObj.SEALTYPE_A = '<span class="badge badge-success">A</span>';
							$('#ruleBasedGrid_'+rpresult[i].predict_idx).jsGrid("refresh");
							break;
						}
					}
				} 
			
				//$('#predictGrid3_'+presult[i].predict_idx + ' .jsgrid-grid-body').css('height',"140px");
			} 
			
		}
		
		
	}).fail(function(jqXHR, textStatus, errorThrown){
		//loadingMsg.modal('hide');	
		//$("#chkPredict_"+idx).val("y");  현재진행중인 추천완료처리 
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

function setPredictData(gridId, data, className){
	//console.log("result Set - gridId : " + gridId);		
	if(data ==null) data = "";
	
	let fields = [
    	
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
    	{name : "SEALTYPE_P",title : "Status",type : "text",align : "center",width : 40,visible:(className=="Seal Type|Material|API Plan"?true:false), css:"font-size-down",editing: false }, //,visible:(className=="sealTypeList"?true:false)
    	{name : "SEALTYPE_A",title : "API682",type : "text",align : "center",width : 40,visible:(className=="Seal Type|Material|API Plan"?true:false), css:"font-size-down",editing: false }, //,visible:(className=="sealTypeList"?true:false)
    	{name : "CLASS",title : className, type : "text",align : "center",width : 130, css:"font-size-down",editing: false },	
    	{name : "PROB",title : "확률(%)",type : "number",width : 40,align : "center",css:"font-size-down",editing: false, format:"#,##0.000"}
    	
    ];
	
	
	// 3번 그리드에만 AS 이력을 추가하기 위해 분기 처리함. 2022-11-03
	if(gridId.indexOf("predictGrid3_") > -1){
		
		fields.push({name : "AS_CNT", title: "AS 이력", type : "text", align:"center", width:30, css:"font-size-down click-evt-as_cnt",editing:false,
    		itemTemplate: function(value, item){ return value + " 건";}});
	}
	
	
	$("#"+gridId).jsGrid('destroy');
	$("#"+gridId).jsGrid({
    	width: "100%",
        height: "180px",
        editing: false, //수정 기본처리
        sorting: false, //정렬
        paging: false, //조회행넘어가면 페이지버튼 뜸
        //loadMessage : "Now Loading...",
        data:data,
        fields: fields,
        rowClick: function(args) {
	        //radio check
	     	var selectData = args.item; //선택한 로우 데이터
	     	if(selectData.hasOwnProperty("AS_CNT") &&
	     			$(args.event.target).hasClass("click-evt-as_cnt")){
	     		
	     	
	     		const selectedKeyName = selectData["CLASS"];
	     		if(_ASHistoryDataObj.hasOwnProperty(selectedKeyName)){
	     			popupASHistoryDetail(_ASHistoryDataObj[selectedKeyName]);
	     		}
				
	     	}else{
	     		var selectDel = selectData.CHK; //선택한 로우의 삭제체크값 
		    	//체크되지않은 행이면 체크함
		    	if(selectDel == "undefined" || selectDel == null || selectDel==false){
		    		//전체 uncheck
		    		var gridData = $("#"+gridId).jsGrid("option", "data");
		    		for(var i=0;i<gridData.length;i++){
		    			$("#"+gridId).jsGrid("updateItem", gridData[i], {"CHK":false});	
		    		}
		    		$("#"+gridId).jsGrid("updateItem", selectData, {"CHK":true});
		    		
		    	}else if(selectDel==true){
		    		return false;
		    	}
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
	$("#"+gridId + ' .jsgrid-grid-body').css('height', '140px');  
}


function setPredictProgress(){
	// _predictTotCnt : 총건수
	_predictDoneCnt=_predictDoneCnt+1;
	var pw = Math.floor((_predictDoneCnt/_predictTotCnt)*100);
	$("#predict_progress").css("width", (pw+"%"));
}



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
            
            // remove 추천결과
            $('#predict_result').empty();
            
            //loadingMsg.modal('hide');
            //$('#excelUpload').modal("hide");
            //alert("처리되었습니다");
            
        },fail : function (jqXHR, textStatus, errorThrown){
			//loadingMsg.modal('hide');
			//ajaxFailMsg(result);
			//$("#chkPredict_"+idx).val("y");  현재진행중인 추천완료처리 
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
	추천결과 -> 엑셀파일 : 저장
--%>
function excelSaveProcess(){
	//console.log("_fileUploadResult(업로드한엑셀명)",_fileUploadResult);
	//console.log("_getFeatureRangeList",_getFeatureRangeList);
	//기본template 복사
	if(_fileUploadResult==null){ //직접입력할경우
		var vItemIdx = 0;
		var vFeatureList = $('#jsGrid_feature').jsGrid('option', 'data'); // Feautre List 추천조건 값
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
		//console.log("추천결과",predictArr);
		var loadingMsg = new loading_bar({message:"excel create & Result Save ..."});
		$.ajax({
	        url: '/ml/predictInfoToExcelFile2.do', //조회한 추천값을 엑셀로 다운받기.
	        type: 'POST',
	        data: JSON.stringify({
	        	predictInfo : predictArr, // 추천결과 grid 데이터 값.
	        	FeatureList : vFeatureList, //추천 조건 grid 값 인자값
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
		// 추천결과를 넘겨서 업로드한 파일에 Write
		var vItemIdx = 0;
		var vFeatureList = $('#jsGrid_feature').jsGrid('option', 'data'); // 추천조건인자값
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
	    
			// 추천값
			predictArr.push(classVal);
		} // end for
		
		var loadingMsg = new loading_bar({message:"Result Save ..."});
		
		$.ajax({
	        url: '/ml/predictInfoToExcelFile.do', //업로드한 엑셀파일에 추천값을 추가
	        type: 'POST',
	        data: JSON.stringify({
	        	fileInfo : _fileUploadResult, //upload excel
	        	predictInfo : predictArr, //추천결과
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

//history pop grid init
function historyGridInit(){
	
	
	$("#jsGrid_history").jsGrid('destroy');
	$("#jsGrid_history").jsGrid({
		width: "100%",
	    height: "500px",
	    editing: false, //수정 기본처리
	    sorting: true, //정렬
	    //paging: false, //조회행넘어가면 페이지버튼 뜸
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
        pagerContainerClass: "custom-jsgrid-pager-container",
        pagerClass: "custom-jsgrid-pager",
        pagerNavButtonClass: "custom-jsgrid-pager-nav-button",
        pagerNavButtonInactiveClass: "custom-jsgrid-pager-nav-inactive-button",
        pageClass: "custom-jsgrid-pager-page",
        currentPageClass: "custom-jsgrid-pager-current-page",
	    loadMessage : "Now Loading...",
        confirmDeleting: false,
        onItemDeleting: function(args) {
			if(!args.item.deleteConfirmed){  // custom property for confirmation
            	args.cancel = true; // cancel deleting
				confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
                	if(result == true){
                    	args.item.deleteConfirmed = true;
                    	$("#jsGrid_history").jsGrid('deleteItem', args.item); //call deleting once more in callback
                	}
				});
			}
        },
	    fields: [
	    	{name : "HISTROTY_ID",	title : "HISTROTY_ID",	type : "text",	align : "center",	width : 80, css:"font-size-down", visible : false},
	    	{name : "STEP",			title : "STEP",			type : "text",	align : "center",	width : 80, css:"font-size-down", visible : false},
	    	{name : "EQUIP_TYPE",	title : "EQUIP_TYPE",	type : "text",	align : "center",	width : 80, css:"font-size-down", visible : false},
	    	{name : "TITLE",		title : "제목",			type : "text",	align : "left",		width : 200,css:"font-size-down",
	    		itemTemplate : function(value, item) {
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate pl-2');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.REMARK)){
						$rtnDiv.attr('title', item.REMARK);
					}
					$rtnDiv.text(item.TITLE)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						// 만약 툴팁이 존재할 경우 모든 툴팁 닫기
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
	    		}
	    	},
	    	{name : "REG_DT",	title : "일자",	type : "text",	align : "center",	width : 80, css:"font-size-down"},
	    	{type: "control" ,  width: 50 , css:"font-size-down",
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
					return '삭제';
				}
	    	}
	    ]
        ,rowClick : function(args) {
			//그리드의 행 데이터를 클릭 했을 때 하이라이트 처리 
			var $row = this.rowByItem(args.item), selectedRow = $("#jsGrid_history").find('table tr.highlight');
			if (selectedRow.length) {
				selectedRow.toggleClass('highlight');
			};
			$row.toggleClass("highlight");
			_historyObj = args.item;
        }
		,controller:  {
			loadData : function(filter) {
				return $.ajax({
					type:"POST",
					url:"<c:url value='/ml/getMngHistory.do'/>",
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					contentType: "application/json",
					data: JSON.stringify({
						SCD : $("#select_application option:selected").val()
					})
				})
			},
	        deleteItem: function(item) {
				$.ajax({
					type:"POST",
					url:"<c:url value='/ml/getDeleteHistory.do'/>",
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					contentType: "application/json",
					data: JSON.stringify(item)
				}).done(function(result){
					alert("삭제하였습니다.");
				})
	        }
	    },
	    onPageChanged: function() {
    		//페이지 변경시
    		var gridData = $("#jsGrid_history").jsGrid("option", "data");
    	}
	});
}

//HISTORY ID 생성
function uuidv4() {
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
		var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
		return v.toString(16);
	});
}

//운전 조건 HIDE
function hideCondition() {
 	$("#divSolidSize").css('display','none');
 	$("#divSolidCont").css('display','none');
 	$("#divChloride").css('display','none');
 	$("#divBrine").css('display','none');
//  	$("#divOil").css('display','none');
//  	$("#divResidue").css('display','none');
 	$("#divGroup").css('display','none');
 	$("#divServiceGs").css('display','none');
 	$("#divCase").css('display','none');
 	$("#select_brine_sub_gb").css('display','none');
 	
 	$("#divFtaApplication").css('display','none');
 	$("#divFtaService").css('display','none');
 	$("#divFtaEquipment").css('display','none');
}

//운전 조건 HIDE_product 적용 시
function hideCondition_product_apply() {
 	$("#divChloride").css('display','none');
 	$("#divBrine").css('display','none');
 	$("#select_brine_sub_gb").css('display','none');
}

//tab event
function tabEvent(tabNo) {
    $("#tabDiv2_"+tabNo).hide();
    $("#tabLi2_"+tabNo).removeClass("active");
    $("#tabLi_"+tabNo).addClass("active");
    $("#tabDiv_"+tabNo).show();
    $("#btn_viewImg").hide();
    return false;
}

function tabEvent2(tabNo) {
    $("#tabDiv_"+tabNo).hide();
	$("#tabLi_"+tabNo).removeClass("active"); 
	$("#tabLi2_"+tabNo).addClass("active");
    $("#tabDiv2_"+tabNo).show();
    $("#btn_viewImg").show();
    _fSealType="";
    var predictList = $("#jsGrid_feature").jsGrid("option", "data"); 
//     for(var i=0; i<predictList.length; i++){
//     	$("#ruleBasedGrid_"+predictList[i].NO + ' .jsgrid-grid-body').css('height', "100px"); 
//     }
    		
    return false;
}

//rule based grid init
function setRuleBasedGrid(gridId, data, className){
	if(data == null) {
		data = "";
	} 
	
	$("#"+gridId).jsGrid('destroy');
	$("#"+gridId).jsGrid({
    	width: "100%",
        //height: data.length > 3? "140px" : "110px",
        //height: "140px",
        editing: false, //수정 기본처리
        sorting: false, //정렬
        paging: false, //조회행넘어가면 페이지버튼 뜸
        //loadMessage : "Now Loading...",
        data:data,
        fields: [
        	{name : "P_ID_SEQ",	title : "No",	type : "text",align : "center",width : 20, css:"font-size-down",editing: false},
        	/* {name : "P_IDX",	title : "No",	type : "text",align : "center",width : 20, css:"font-size-down",editing: false}, */
        	//{name : "P_SEQ",	title : "Seq",	type : "text",align : "center",width : 20, css:"font-size-down",editing: false, visible : false},
        	{name : "SEALTYPE_P",title : "Status",type : "text",align : "center",width : 30, css:"font-size-down",editing: false },
        	{name : "SEALTYPE_A",title : "API682",type : "text",align : "center",width : 30, css:"font-size-down",editing: false },
        	{name : "SEAL",	title : "Seal",	type : "text",align : "center",width : 60, css:"font-size-down",editing: false},
        	{name : "MTRL1",	title : "1",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL1_ADD_INFO) && isNotEmpty(item.MTRL1_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL1_ADD_INFO.MTRL_CD+" : "+item.MTRL1_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL1)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						// 만약 툴팁이 존재할 경우 모든 툴팁 닫기
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "MTRL2",	title : "2",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL2_ADD_INFO) && isNotEmpty(item.MTRL2_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL2_ADD_INFO.MTRL_CD+" : "+item.MTRL2_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL2)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						// 만약 툴팁이 존재할 경우 모든 툴팁 닫기
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "MTRL3",	title : "3",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL3_ADD_INFO) && isNotEmpty(item.MTRL3_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL3_ADD_INFO.MTRL_CD+" : "+item.MTRL3_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL3)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						// 만약 툴팁이 존재할 경우 모든 툴팁 닫기
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "MTRL4",	title : "4",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL4_ADD_INFO) && isNotEmpty(item.MTRL4_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL4_ADD_INFO.MTRL_CD+" : "+item.MTRL4_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL4)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						// 만약 툴팁이 존재할 경우 모든 툴팁 닫기
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "MTRL_OUT1",	title : "1",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL_OUT1_ADD_INFO) && isNotEmpty(item.MTRL_OUT1_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL_OUT1_ADD_INFO.MTRL_CD+" : "+item.MTRL_OUT1_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL_OUT1)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "MTRL_OUT2",	title : "2",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL_OUT2_ADD_INFO) && isNotEmpty(item.MTRL_OUT2_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL_OUT2_ADD_INFO.MTRL_CD+" : "+item.MTRL_OUT2_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL_OUT2)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "MTRL_OUT3",	title : "3",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL_OUT3_ADD_INFO) && isNotEmpty(item.MTRL_OUT3_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL_OUT3_ADD_INFO.MTRL_CD+" : "+item.MTRL_OUT3_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL_OUT3)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "MTRL_OUT4",	title : "4",	type : "text",align : "center",width : 10, css:"font-size-down",editing: false,
        		itemTemplate : function(_,item){
					var $rtnDiv = $("<div>");
					$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
					$rtnDiv.attr('id','savedData-sequence-'+item.P_IDX);
					$rtnDiv.attr('data-toggle','tooltip');
					$rtnDiv.attr('data-placement','auto');
					if(isNotEmpty(item.MTRL_OUT4_ADD_INFO) && isNotEmpty(item.MTRL_OUT4_ADD_INFO.MTRL_NM)){
						$rtnDiv.attr('title',item.MTRL_OUT4_ADD_INFO.MTRL_CD+" : "+item.MTRL_OUT4_ADD_INFO.MTRL_NM);
					}
					$rtnDiv.text(item.MTRL_OUT4)
					return $rtnDiv.hover(function() {
						$(this).tooltip({ boundary: 'window' });
						$(this).tooltip('show');
					}, function(){
						$(this).tooltip('hide');
						if($(".tooltip-inner").length > 0){
							$(".tooltip").tooltip("hide");
						}
					});
				} 
        	},
        	{name : "PLAN",	title : "Plan",	type : "text",align : "center",width : 70, css:"font-size-down",editing: false},
	    	{name : "VIEW_PRICE",	title : "가격정보",	type : "text",	align : "center",	width : 30, css:"text-truncate",
	    		itemTemplate : function (value, item) {
                	return $("<span>").attr("class", "fa fa-search");
            	}
            },
            {name: "AS_CNT", title : "AS 이력", type:"text", align: "center", width: 30,css:"font-size-down click-evt-as_cnt",
            	itemTemplate : function(value, item){
            		return value + " 건";
           		}
           	}
        ],
	    headerRowRenderer: function() {
	        var $result = $("<tr>").height(0)
	          	.append($("<th>").attr("rowspan", 2).width(20).text("No.").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            //.append($("<th>").attr("rowspan", 2).width(20).text("Seq.").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("rowspan", 2).width(30).text("Status").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("rowspan", 2).width(30).text("API682").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("rowspan", 2).width(60).text("Seal Type").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("colspan", 4).width(40).text("Material Inner").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("colspan", 4).width(40).text("Material Outer").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("rowspan", 2).width(70).text("API Plan").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("rowspan", 2).width(30).text("가격정보").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            .append($("<th>").attr("rowspan", 2).width(30).text("AS 이력").css({"text-align":"center","border-right":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            ;
	        $result = $result.add($("<tr>")
	        		.append($("<th>").width(10).text("1").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	        		.append($("<th>").width(10).text("2").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	                .append($("<th>").width(10).text("3").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	                .append($("<th>").width(10).text("4").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	                .append($("<th>").width(10).text("1").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	                .append($("<th>").width(10).text("2").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	                .append($("<th>").width(10).text("3").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	                .append($("<th>").width(10).text("4").css({"text-align":"center","border-right":"1px solid #e9e9e9","border-top":"1px solid #e9e9e9","font-size":"12px","padding":"0px"}))
	            );
	        return $result;
	    },
	    onRefreshed: function(args){
	    },
	    rowClick: function(args) {
	    	let selectData = args.item;
	        var $target = $(args.event.target);
        	if(selectData.hasOwnProperty("AS_CNT") &&
	     			$(args.event.target).hasClass("click-evt-as_cnt")){
	     		
	     		const selectedKeyName = selectData["keyName"];
	     		if(_ASHistoryDataObj.hasOwnProperty(selectedKeyName)){
	     			popupASHistoryDetail(_ASHistoryDataObj[selectedKeyName]);
	     		}
				
        	}else if($target.closest(".fa.fa-search").length) {
        		//파일이 없는 경우 체크
	        	if(isEmpty(args.item.SEAL)){
	        		alert("가격정보를 조회하고자 하는 항목을 선택하세요.");
	        		return;
	        	};
	        	$.ajax({
	        		type:"POST",
	        		//url:"<c:url value='/admin/getSealTypeList.do'/>",
	        		url:"<c:url value='/re/getSealTypeList.do'/>",
	        		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
	        		contentType: "application/json",
	        		data: JSON.stringify({
	        			fSealType : args.item.SEAL
	        		})
	        	}).done(function(result){
	        		$('#viewDiv').empty();
		        	if(isEmpty(result)){
		        		alert("가격정보가 존재하지 않습니다.");
		        		return;
		        	};
	        		var viewItem = ""; 
	        		viewItem = "<div class='view'>"
	        		for(var i=0; i<result.length; i++){
// 		        		var vFileParam = $.param({
// 		        			"file_name"		: result[i].FILE_NM
// 		        		    ,"file_name_org": result[i].FILE_ORG_NM
// 		        		    ,"file_path"	: result[i].FILE_PATH
// 		        		});
// 	        			var srcPath = "<c:url value="/library/downLibraryFile.do" />?"+vFileParam;
// 	        			viewItem +="<img class='viewItem' src='"+srcPath+"' alt='"+result[i].FILE_ORG_NM+"' style='display:none;'/>";
	        			
						var strNo = result[i].FILE_PATH.indexOf("/");
						var vPath = result[i].FILE_PATH.substr(strNo);
						var srcPath = "/uploadpath/"+vPath+"/"+result[i].FILE_NM;
						viewItem +="<img class='viewItem' src='"+srcPath+"' alt='"+result[i].FILE_ORG_NM+"' style='display:none;'/>";
	        		};
	        		viewItem += "</div>"
	        		$("#viewDiv").append(viewItem);
	        		var	viewer = new Viewer(document.querySelector('.view'), {
	        			navbar : true,
	        			toolbar : true
	        		});
	        		$('.viewItem').trigger("click");
	        	});	
        	}
        }	    
//	    ,
//      rowClass: function(item, itemIndex) {
//      return item.P_IDX%2==0 ? 'js-bg-1' : 'js-bg-2';
//  },
	        
	});
	//$("#"+gridId + ' .jsgrid-grid-body').css('height', data.length > 3? "100px !important" : "60px !important");  // grid height -40?
	$("#"+gridId + ' .jsgrid-grid-body').css("overflow","");
}


//빈값 체크
function isNotEmpty(str){
	if(typeof str == "undefined" || str == null || str == "")
    	return false;
    else
        return true ;
}

<%-- Note 펼치기접기 --%>
function noteBtn(idx){
	if ($('#noteBtn_'+idx).find("i").hasClass("fa-angle-down")) {
		$('#noteSpace_'+idx).collapse('show');
		$('#noteBtn_'+idx).find("i").removeClass("fa-angle-down");
		$('#noteBtn_'+idx).find("i").addClass("fa-angle-up");
	}else {
		$('#noteSpace_'+idx).collapse('hide');
		$('#noteBtn_'+idx).find("i").removeClass("fa-angle-up");
		$('#noteBtn_'+idx).find("i").addClass("fa-angle-down");
	}
};

<%-- 추천 과정 펼치기접기 --%>
function prdtBtn(idx){
	if ($('#prdtBtn_'+idx).find("i").hasClass("fa-angle-down")) {
		$('#prdtSpace_'+idx).collapse('show');
		$('#prdtBtn_'+idx).find("i").removeClass("fa-angle-down");
		$('#prdtBtn_'+idx).find("i").addClass("fa-angle-up");
	}else {
		$('#prdtSpace_'+idx).collapse('hide');
		$('#prdtBtn_'+idx).find("i").removeClass("fa-angle-up");
		$('#prdtBtn_'+idx).find("i").addClass("fa-angle-down");
	}
};

//init fileData grid
function libraryGridInit(){
	$("#jsGrid_library").jsGrid('destroy');
	$("#jsGrid_library").jsGrid({
		width: "100%",
	    height: $(document).height()-200,
	    editing: false, //수정 기본처리
	    sorting: false, //정렬
	    paging: false, //조회행넘어가면 페이지버튼 뜸
	    loadMessage : "Now Loading...",
        confirmDeleting: false,
        onItemDeleting: function(args) {
			if(!args.item.deleteConfirmed){  // custom property for confirmation
            	args.cancel = true; // cancel deleting
				confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
                	if(result == true){
                    	args.item.deleteConfirmed = true;
                    	$("#jsGrid_library").jsGrid('deleteItem', args.item); //call deleting once more in callback
                	}
				});
			}
        },
	    fields: [
	    	{name : "REF_ID",		title : "REF_ID",		type : "text",	align : "center",	width : 80, visible:false}
	    	,{name : "REF_NM",		title : "자료명",			type : "text",	align : "left",		width : 250,css:"text-truncate"}
	    	,{name : "ACC_SCH_YN",	title : "조회",			type : "text",	align : "center",	width : 80, css:"text-truncate", 
	    		itemTemplate : function (value, item) {
                	var iconClass = "";
                	//권한 체크
                	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
                    	iconClass = "fa fa-search"; 
                	}else{
                    	if (value == "Y" && ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5")) {
                        	iconClass = "fa fa-search"; 
                    	}
                	}
                	return $("<span>").attr("class", iconClass);
            	}
            }
	    	,{name : "ACC_SAV_YN",	title : "저장",			type : "text",	align : "center",	width : 80, css:"text-truncate", 
	    		itemTemplate : function (value, item) {
                	var iconClass = "";
                	//권한 체크
                	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
                		iconClass = "fa fa-download"; 
                	}else{
                    	if (value == "Y" && "${ref_data_role}"== "R5") {
                    		iconClass = "fa fa-download"; 
                    	}
                	}
                	return $("<span>").attr("class", iconClass);
            	}
            }
	    	,{name : "REG_DT",		title : "등록일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
	    ]
        ,rowClick: function(args) {
        	var $target = $(args.event.target);
        	if($target.closest(".fa.fa-download").length) {
        		//파일이 없는 경우 체크
        		if(isEmpty(args.item.FILE_NM)){
        			alert("파일이 존재하지 않습니다.");
	        		return;
        		}
        		var vFileParam = $.param({
        			"file_name"		: args.item.FILE_NM
        		    ,"file_name_org": args.item.FILE_ORG_NM
        		    ,"file_path"	: args.item.FILE_PATH
        		});
        		$("#fileDownFrame").attr("src", "<c:url value="/library/downLibraryFile.do" />?"+vFileParam); //exceldown 실행? "src"에 "<c:url value="/ml/predictApplyExcelFileDownload.do" />?"+vFileParam 부여.	        	
        	}
        	if($target.closest(".fa.fa-search").length) {
        		//파일이 없는 경우 체크
        		if(isEmpty(args.item.FILE_NM)){
        			alert("파일이 존재하지 않습니다.");
	        		return;
        		}
        		//size set
		       	$('#pdfModal .modal-content').css("height",($(document).height()-80)+"px");	
				$('#pdf').css("height",($(document).height()-170)+"px");	
				$('#pdf').css("max-width","100%");		
				$('#pdf').css("margin","0px");	
				
				$('#pdfModal').modal("show");
        		var vFileParam = $.param({
        			"file_name"		: args.item.FILE_NM
        		    ,"file_name_org": args.item.FILE_ORG_NM
        		    ,"file_path"	: args.item.FILE_PATH
        		});
				var options = {
        			pdfOpenParams: {
        				navpanes: 0
						,toolbar: 0
        				,statusbar: 0
        			    ,view:"FitV"
        			    ,pagemode:"none"
        				,page: 1
        			}
					,forcePDFJS: true
        			,PDFJS_URL:"<c:url value='/js/common/pdfjs/web/viewer.html'/>"
        		};
        		var myPDF = PDFObject.embed("<c:url value="/library/downLibraryFile.do" />?"+vFileParam,"#pdf", options);
        	}
        }
		,controller:  {
			loadData : function(filter) {
				return $.ajax({
					type:"POST",
					url:"<c:url value='/library/getLibraryList.do'/>",
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					contentType: "application/json",
					data: JSON.stringify({})
				})
			}
	    }
	});
}

//빈값 체크
function isEmpty(value) { 
	if( value == "" || value == null || value == undefined || ( value != null && typeof value == "object" && !Object.keys(value).length ) ){ return true }else{ return false }
};

//필수값 체크
function valChk() { 
	var chkResult = true;
	var chkMessage = "";
	
	if(isEmpty($("#select_pump_type").val())){
		chkMessage = chkMessage+"Pump Type 선택값이 없습니다.<br>";
	}
	if(isEmpty($("#f_product").val())){
		chkMessage = chkMessage+"Product 입력값이 없습니다.<br>";
	}
	if(isEmpty($("#f_temp_nor").val()) && isEmpty($("#f_temp_min").val()) && isEmpty($("#f_temp_max").val())){
		chkMessage = chkMessage+"Temperature 입력값이 없습니다.<br>";
	}
	if(isEmpty($("#f_spec_gravity_nor").val()) && isEmpty($("#f_spec_gravity_min").val()) && isEmpty($("#f_spec_gravity_max").val())){
		chkMessage = chkMessage+"Specific Gravity 입력값이 없습니다.<br>";
	}
	
	if(isEmpty($("#f_visc_nor").val()) && isEmpty($("#f_visc_min").val()) && isEmpty($("#f_visc_max").val())){
		chkMessage = chkMessage+"Viscosity 입력값이 없습니다.<br>";
	}
	
	if(isEmpty($("#f_vap_pres_nor").val()) && isEmpty($("#f_vap_pres_min").val()) && isEmpty($("#f_vap_pres_max").val())){
		chkMessage = chkMessage+"Vapor Pressure 입력값이 없습니다.<br>";
	}
	if(isEmpty($("#f_seal_cham_nor").val()) && isEmpty($("#f_seal_cham_min").val()) && isEmpty($("#f_seal_cham_max").val())){
		chkMessage = chkMessage+"Seal Chamber Pressure 입력값이 없습니다.<br/>";
	}
	
	// OEM 단계에서 추가체크
	// Shaft Speed, Shaft Dia.
	if ($("#f_predict_sel").val()=="OEM"){
		if(isEmpty($("#f_rpm_nor").val()) && isEmpty($("#f_rpm_min").val()) && isEmpty($("#f_rpm_max").val())){
			chkMessage = chkMessage+"Shaft Speed 입력값이 없습니다.<br/>";
		}
		
		if(isEmpty($("#f_shaft_size").val())){
			chkMessage = chkMessage+"Shaft Dia. 입력값이 없습니다.<br/>";
		}
	}
	
	if(_isChgProductApply == false ){
		chkMessage = chkMessage+"Product 적용 버튼을 클릭하여 Product 변경정보를 반영하세요.<br>";
	}

	//Solid = 'Y1'
	if($("#select_solid_gb_yn").val() == "Y1"){
		if($("#solid_size_nor").val() == "" &&
				$("#solid_size_min").val() == "" &&
				$("#solid_size_max").val() == "" &&
				!$("#solid_size_check").is(":checked")){
		
			chkMessage = chkMessage+"Solid Size 입력값이 없습니다.<br/>";	
		}

		if ($("#solid_cont").val() == ""){
			chkMessage = chkMessage+"Solid 농도 입력값이 없습니다.<br/>";	
		}
	}
	
	// 입력 API Plan 체크
	var vApiPlanDir = $("#f_api_plan_dir").val().toUpperCase(); // 대문자로 변경
	var vApiPlanDirMsg = "";
	if(vApiPlanDir != ""){
		$.ajax({
			type:"POST",
			async:false,
			url:"<c:url value='/rb/getUnknownApi.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			data: JSON.stringify({
				API_PLAN_DIR : vApiPlanDir
			})
		}).done(function(result){

			// 잘못된 API 입력여부 결과 표시
			var vUnknownApi = result.UNKNOWN_API;
			for(var i = 0; i<vUnknownApi.length; i++){
				if(i==0){
					vApiPlanDirMsg = vApiPlanDirMsg + vUnknownApi[i];
				}else{
					vApiPlanDirMsg = vApiPlanDirMsg + ","+ vUnknownApi[i];	
				}
			}
			
			if (vApiPlanDirMsg != ""){
				vApiPlanDirMsg = "알수없는 Plan정보 입니다 : " + vApiPlanDirMsg + "</br/>";
			}
			
			
			//다른 입력값과 Arrangement 비교
			var vArrangementDirAPI = Number(result.ARRANGEMENT);
			
			// single/dual 구분값
			var vSingleDualGb = 0;
			if($("#select_s_d_gb").val() == ""){
				vSingleDualGb = 0;
			}else if ($("#select_s_d_gb").val() == "S"){
				vSingleDualGb = 1;
			}else{
				vSingleDualGb = 2;	
			}
				
			if( vArrangementDirAPI !=0 && vSingleDualGb !=0 &&
					(vArrangementDirAPI >= 2 &&  vSingleDualGb ==1) ||
					(vArrangementDirAPI == 1 &&  vSingleDualGb ==2)
					){
				vApiPlanDirMsg = "지정된 API Plan과 Single/Dual 설정 결과가 맞지 않습니다. <br/> " ;
			}
			
			// configuration 입력값
			var vSealConfig = 0;
			if ($("#select_seal_config").val() != ""){
				vSealConfig = $("#select_seal_config").val().substr(0,1);
			}
			
			
			if( vArrangementDirAPI !=0 && vSealConfig !=0 &&
					vArrangementDirAPI != vSealConfig){
				vApiPlanDirMsg = "지정된 API Plan과 Seal Configuration 설정 결과가 맞지 않습니다. <br/> " ;
			}
			
			// --------------------
			// 체크 메세지에 추가
			// --------------------
			chkMessage = chkMessage + vApiPlanDirMsg;
		})
	}
	
	if(isEmpty(chkMessage)){
		chkResult = false
	}else{
		$("#valChkMsg").html(chkMessage);
		$('#alert').css("display","block");
		$('#alert').addClass("show");
		//alert(chkMasage);
	}
	return chkResult;
};

/**
 * @Author Jin. Choi
   2022-11-04
   AS 이력 팝업 
 */
function popupASHistoryDetail (_data){
	console.log("popupASHistoryDetail", _data);

	 const windowInHeight = $(window).innerHeight();
	 $("#ASHistoryPopup .modal-body .overflow-auto").height(windowInHeight - 400);
	 $("#ASHistoryPopup").modal('show');
	 
	// src/main/webapp/js/common/olap-collapse-grid.js
	const titleIcon = '<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#CF4042" class="bi bi-card-list" viewBox="0 0 16 16">'+
'<path d="M14.5 3a.5.5 0 0 1 .5.5v9a.5.5 0 0 1-.5.5h-13a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h13zm-13-1A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h13a1.5 1.5 0 0 0 1.5-1.5v-9A1.5 1.5 0 0 0 14.5 2h-13z"/>'+
'<path d="M5 8a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7A.5.5 0 0 1 5 8zm0-2.5a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7a.5.5 0 0 1-.5-.5zm0 5a.5.5 0 0 1 .5-.5h7a.5.5 0 0 1 0 1h-7a.5.5 0 0 1-.5-.5zm-1-5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0zM4 8a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0zm0 2.5a.5.5 0 1 1-1 0 .5.5 0 0 1 1 0z"/>'+
'</svg>';
	const collapseConfig = {
		isNum:true,
		field:[{
			title:"요청일",name:"REQ_DT", font_size:10, type:"date", width:'10%'
		},{
			title:"거래처",name:"CUST_NM", font_size:10
		},{
			title:"사용거래처",name:"USE_CUST_NM", font_size:10
		},{
			title:"불만내용구분",name:"CLAIM_TYPE_NM", font_size:10
		},{
			title:"하자유형",name:"FAIL_REASON1_NM", align:"center", font_size:10
		},],
		collapseField:[{
			title:"불만내용",name:"CLAIM_CONTENTS", font_size:10,
			title_icon_str: titleIcon
		},{
			title:"추정원인",name:"FAIL_REASON_NOTES", font_size:10,
			title_icon_str: titleIcon
		},{
			title:"하자 Comments",name:"FAIL_COMMENTS",font_size:10,
			title_icon_str: titleIcon
		}]
	};
	
	CustomCollapseGrid("asHistoryGrid",collapseConfig,_data);
	$('#ASHistoryPopup').modal('handleUpdate');
	
}

/**
 * @Author Jin. Choi.
 2022-11-03
 AS 이력 데이터 호출
 */
 function setASHistoryData (flag, gridId, data, className, callback){
	
	if(flag !== "rule" && flag !== "predict"){console.error("Unknown Arguments flag: '"+ flag +"'"); return;}
	
		for(let idx in data){
			let _row = data[idx];
			let _className = "";
			if(flag === "rule") {
				_className = _row["SEAL"] + " | " + _row["MTRL1"] + " "+ _row["MTRL2"] + " "+ _row["MTRL3"] + " "
				+ _row["MTRL4"];
				if(_row["MTRL_OUT1"] !== "") {
					_className += " / " +_row["MTRL_OUT1"]+ " "+_row["MTRL_OUT2"]+" "+_row["MTRL_OUT3"]+" "+_row["MTRL_OUT4"];
				}
				
				_className += " | " + _row["PLAN"];
				data[idx]["keyName"] = _className;
			} else if(flag === "predict") {
				_className = _row["CLASS"];
			}
			
			let asCnt = 0;
			for(let i = 0;i<_asHistoryData.length;i++){
				let asHistoryClass = _asHistoryData[i]["KEYNAME"];
				if(asHistoryClass === _className){
					
					if(_ASHistoryDataObj.hasOwnProperty(asHistoryClass) === false){
						_ASHistoryDataObj[asHistoryClass] = [_asHistoryData[i]];
					}else{
						_ASHistoryDataObj[asHistoryClass].push(_asHistoryData[i]);
					}
					asCnt++;
				}
			}
			_row["AS_CNT"] = asCnt;
		}
	
		
	callback(gridId, data, className);

	
}


</script>
<style>
tr.highlight td.jsgrid-cell {
	background-color: #BBDEFB;
}
.jsgrid-header-row {
	text-align: center;
}
.red td {
	color: #f08080 !important;
}
.jsgrid-delete-button-custom {
	background-position: 0 -80px;
	width: 16px;
	height: 16px;;
	opacity: .2;
}
.jsgrid-edit-button-custom {
	background-position: 0 -120px;
	width: 16px;
	height: 16px;;
	opacity: .2;
}
.font-size-down {
	font-size: 12px;
	padding: 0px;
	padding-top:2px;
}
.jsgrid-cell {
	word-wrap: break-word;
	padding-bottom: 0px;
	padding-top: 5px;
}
#jsGrid_SEAL_TYPE .jsgrid-pager {
	font-size: 12px;
}
#jsGrid_SEAL_SIZE .jsgrid-pager {
	font-size: 12px;
}
#jsGrid_SEAL_CONFIG .jsgrid-pager {
	font-size: 12px;
}
#jsGrid_SEAL_ALL .jsgrid-pager {
	font-size: 12px;
}
.predict-Feature_h {
	border: 1px solid #ccc;
	background-color: #f7f7f7;
	/*min-width: 60px;*/
	font-size: 12px;
	text-align: center;
	padding-left:5px;
	padding-right:5px;
}
.predict-Feature {
	border-top: 1px solid #ccc;
	background-color: #fff;
	min-width: 40px;
	font-size: 12px;
	text-align: center;
	height: 20px;
}
.predict-Feature-Add_h {
	border: 1px solid #ccc;
	background-color: #f7f7f7;
	/*min-width: 60px;*/
	font-size: 12px;
	text-align: center;
	padding-left:5px;
	padding-right:5px;
}
.predict-Feature-Add {
	border-top: 1px solid #ccc;
	background-color: #fff;
	min-width: 40px;
	font-size: 12px;
	text-align: center;
	height: 20px;
}
.js-bg-1  td{
	background-color: #fff7dd !important;
	border : 1px solid #e8e8e8;
}
.js-bg-2  td{
	background-color: #ffffff !important;
	border : 1px solid #e8e8e8;
}
.jsgrid-selected-row td{
	background: #c4e2ff !important;
}
/*TAB CSS*/
ul.tabs {
    margin: 0;
    padding: 0;
    float: left;
    list-style: none;
    height: 32px; /*--Set height of tabs--*/
    /*border-bottom: 1px solid #999;*/
    /*border-left: 1px solid #999;*/
    width: 100%;
}
ul.tabs li {
    float: left;
    margin: 0;
    padding: 0;
    height: 31px; /*--Subtract 1px from the height of the unordered list--*/
    line-height: 31px; /*--Vertically aligns the text within the tab--*/
    /*border: 1px solid #999;*/
    border: 1px solid #ccc;
    /*border-left: none;*/
    margin-bottom: -1px; /*--Pull the list item down 1px--*/
    overflow: hidden;
    position: relative;
    /*background: #e0e0e0;*/
    background: #fff;
}
ul.tabs li a {
    text-decoration: none;
    color: #000;
    display: block;
    font-size: 1.2em;
    padding: 0 20px;
    /*--Gives the bevel look with a 1px white border inside the list item--*/
    border: 1px solid #fff; 
    outline: none;
}
ul.tabs li a:hover {
    /*background: #ccc;*/
    background: #EDF3FD;
}
html ul.tabs li.active, html ul.tabs li.active a:hover  {
    /*--Makes sure that the active tab does not listen to the hover properties--*/
    /*background: #fff;*/
    background: #DFE9FA;
    /*--Makes the active tab look like it's connected with its content--*/
    /*border-bottom: 1px solid #fff;*/
    /*border-bottom: 1px solid #999;*/ 
}
/*Tab Conent CSS*/
.tab_container {
    /*border: 1px solid #999;*/
    border-top: none;
    overflow: hidden;
    clear: both;
    float: left; 
    width: 100%;
    background: #fff;
}
.tab_content {
	/*padding:5px;*/
    padding: 3px 3px 3px 0px;
    font-size: 1.2em;
}

.pdfobject-container {
	width: 100%;
	max-width: 600px;
	height: 600px;
	margin: 2em 0;
	overflow:hidden !important;
}
	
.pdfobject { border: solid 1px #666; }


.modal-header {
    padding: 0.5rem;
}



/* pager */
.custom-jsgrid-pager{
	margin-top:28px;
	text-align: center !important;
}

.custom-jsgrid-pager-nav-inactive-button a,
.custom-jsgrid-pager-nav-button a,
.custom-jsgrid-pager-page a{
	text-decoration:none;
	display: inline-block;
    font-weight: 400;
    color: #212529;
    text-align: center;
    vertical-align: middle;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    background-color: transparent;
    border: 1px solid transparent;
    padding: .375rem .75rem;
    font-size: 1rem;
    line-height: 1.5;
    border-radius: .25rem;
    transition: color .15s ease-in-out,background-color .15s ease-in-out,border-color .15s ease-in-out,box-shadow .15s ease-in-out;
	padding: .25rem .5rem;
    font-size: .875rem;
    line-height: 1.5;
    border-radius: .2rem;
	color: #1d6a96;
    border-color: #1d6a96;
    margin-right:4px;
}
.custom-jsgrid-pager-page.custom-jsgrid-pager-current-page{
  margin-right:4px;
	text-decoration:none;
	  display: inline-block;
  font-weight: 400;
  text-align: center;
  vertical-align: middle;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  background-color: transparent;
  border: 1px solid transparent;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
      padding-right: 0.375rem;
  padding-left: 0.375rem;
   padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
  line-height: 1.5;
  border-radius: 0.2rem;
    color: #fff;
  background-color: #1d6a96;
  border-color: #1d6a96;
}
.custom-jsgrid-pager-nav-inactive-button a:hover,
.custom-jsgrid-pager-nav-button a:hover,
.custom-jsgrid-pager-page a:hover,
.custom-jsgrid-pager-page.custom-jsgrid-pager-current-page:hover {
  color: #fff;
  background-color: #689c97;
  border-color: #689c97;
}

.custom-jsgrid-pager-page.custom-jsgrid-pager-current-page:focus{box-shadow: 0 0 0 0.2rem rgba(38, 143, 255, 0.5);}


</style>
</head>
<body style="overflow-x:hidden;">
<!-- style="display: none;" -->
<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
	<!-- ================  Contents ================  -->
	<div class="container-fluid">
	
		<div class="row mt-2">
			<div class="col-12">
				<div class="row">
					<div class="col-xl-3  col-lg-4 col-md-3  col-sm-12">
						<div class="h5" style="float: left;">
							<strong><i class="far fa-object-ungroup"></i><span class="ml-1"> Seal 추천</span></strong>
						</div>
						<div class="h6 text-left mt-1" style="">
							<span class="ml-2" id="search_cnt_title"></span>
						</div>
					</div>
					<div class="col-xl-2  col-lg-4 col-md-3  col-sm-12">
						<div class="h5" style="float: left;">
							<span class="ml-1" id="historyTitle"> </span>
						</div>
					</div>
					<div class="col-xl-7  col-lg-8 col-md-9 col-sm-12 text-right "> 
						<!--  Button -->
								<button type="button" class="btn btn-outline-primary mb-1"
									id="btn_predict">
									추천 <i class="fa fa-caret-square-right"></i>
								</button>
								<button type="button" class="btn btn-outline-info mb-1"
									id="btn_add_item">
									항목추가 <i class="fa fa-edit"></i>
								</button>
								<button type="button" class="btn btn-outline-success mb-1"
									id="btn_history_mng">
									이력관리 <i class="fas fa-history"></i>
								</button>
								<button type="button" class="btn btn-outline-success mb-1" 
									id="editHistory" style="display:none;">
									이력수정 <i class="far fa-save"></i>
								</button>
								<button type="button" class="btn btn-outline-warning mb-1"
									id="btn_add_excel_item">
									엑셀업로드 <i class="fa fa-file-excel"></i>
								</button>
								<button type="button" class="btn btn-outline-warning mb-1"
									id="btn_sav_excel_item">
									엑셀저장 <i class="fa fa-file-excel"></i>
								</button>
								<%--
								<button type="button" class="btn btn-outline-secondary mb-1" id="btn_fileData">
									관련자료 <i class="fas fa-file-alt"></i>
								</button>
								 --%>
						<!--  Button -->
					</div>
				</div>
			</div>
		</div>
		
		<div class="row">
			<div class="col-xl-2 col-lg-2  col-md-2 col-sm-12 ">
				<p class="h5" style="width: 150px;">
					<i class="fas fa-server"></i> <strong class="text-primary">추천조건</strong>
				</p>
			</div>
			<div class="col-xl-6 col-lg-6  col-md-8 col-sm-12  pt-1">
				<div class="d-flex">
					<div style="width: 60px; text-align: right; padding-right: 10px;">
						단계</div>
					<div>
						<select id="f_predict_sel"
							class="form-control input-small p-0 pl-1"
							style="width: 120px; height: 25px; font-size: 12px;">
							<option value='EPC'>EPC</option>
							<option value='OEM' selected>OEM/End User</option>
						</select>
					</div>
					<div style="width: 120px; text-align: right; padding-right: 10px;">
						Equip Type</div>
					<div>
						<select id="f_equip_type_sel"
							class="form-control input-small p-0 pl-1"
							style="width: 100px; height: 25px; font-size: 12px;">
							<option value='Pump'>Pump</option>
							<option value='Mixer' disabled>Mixer</option>
							<option value='Other' disabled>Other</option>
						</select>
					</div>
				</div>
			</div>
			
			<div class="col-xl-4 col-lg-4  col-md-12 col-sm-12  text-right">
				<a id="featureReset" class="btn" data-toggle="collapse" data-target=""> <i class="fas fa-sync-alt"></i> <span>조건 초기화</span></a>
				<a id="featureRangeList" class="btn" data-toggle="collapse" data-target=""> <i class="fas fa-info-circle"></i> <span>Feautre Range info</span></a>
				<a id="featureCollapseBtn" class="btn" data-toggle="collapse" data-target="#fatureArea" style="padding:0px;"> <span>조건 숨기기</span> <i class="fa fa-angle-up" aria-hidden="true"></i>
				</a>
			</div>
		</div>

		<div class="row collapse show" id="fatureArea">
			<div class="col-12 pt-1">
				<!--  Feature Grid  -->
				<div id="jsGrid_feature"></div>
			</div>
		</div>
	</div>
	
    <!-- 추천결과 -->
	<div class="container-fluid">
		<div class="row">
			<div class="top-search-div collapse show mt-1"
				style="width: 100%; margin-left: 5px;">
				<div class="col-12">
					<div class="row">
						<!--  Predict -->
						<div class="col-lg-12  col-xl-12">
							<div class="card-title">
								<div class="row">
									<div class="col-4" style="padding-left: 10px !important;">
										<p class="h5" >
											<i class="fas fa-search"></i> <strong class="text-primary">추천결과</strong>
										</p>
									</div>
									<%-- predict checkbox --%>
									<div class="col-8  text-right">
										<p class="h5" >
											<a id="rcmdOptBtn" class="text-default  btn"  style="padding:0px;"><span>추천옵션</span> <i class="fa fa-angle-down" aria-hidden="true"></i> </a>
											<a id="predictShowBtn" 	class='text-default btn'  style="padding:0px;"><span>결과접기</span> <i class="fa fa-angle-up" aria-hidden="true"></i> </a>
										</p>
									</div>
								</div>
								
								<div id="rcmdResult" data-toggle='collapse' class="collapse" style="padding-top:5px;clear:both;border:1px solid #ccc; font-size:0.8em;margin-bottom:5px;">
									<div class="row p-1">
										<div class="col-3 text-center">
											<strong>실적 Based 추천</strong>
										</div>
										<div class="col-9">
											<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
												<input type="checkbox" class="custom-control-input" id="target1checked" checked> 
												<label class="custom-control-label" for="target1checked" style="margin-top: -3px;"> Seal Type</label>
											</div>
											<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
												<input type="checkbox" class="custom-control-input" id="target2checked" checked> 
												<label class="custom-control-label" for="target2checked" style="margin-top: -3px;"> API Plan</label>
											</div>
											<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
												<input type="checkbox" class="custom-control-input" id="target3checked" checked> 
												<label class="custom-control-label" for="target3checked" style="margin-top: -3px;"> Seal Type + Material + API Plan</label>
											</div>
										</div>
									</div>
									<div class="row p-1">
										<div class="col-3  text-center">
											<strong>Rule Based 추천</strong>
										</div>
										<div class="col-9">
											<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
												<input type="checkbox" class="custom-control-input" id="target4checked" checked> 
												<label class="custom-control-label" for="target4checked"style="margin-top: -3px;"> Rule Based 추천</label>
											</div>
										</div>
									</div>
								</div>
								
								<div id="predict_result"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%-- 페이지 본문 End --%>
	<%-- ----------------------------------------------------------------------------- --%>
	<%-- ----------------------------------------------------------------------------- --%>
	<%-- ----------------------------------------------------------------------------- --%>
	
	<%-- Feature Edit Popup --%>
	<div class="modal" id="featureEdit" data-backdrop="static">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">

				<!-- Modal Header -->
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>

				<!--  Button -->
				<div class="row" style="margin-bottom:5px;">
					<div class="col-4 mt-3" style="margin-left: 20px;">
						<div class=" h5 modal-title">추천조건</div>
					</div>
					<div class="col-7 mt-3 text-right">
						<button type="button" class="btn btn-outline-success"
							id="btn_pop_apply_feature" >
							적용 <i class="fa fa-save"></i>
						</button>
						<button type="button" class="btn btn-outline-success"
							id="btn_pop_delete_feature">
							삭제 <i class="fa fa-eraser"></i>
						</button>
					</div>
				</div>
				
				<!-- alert -->
				<div id="alert" class="alert alert-warning alert-dismissible fade" role="alert" style="display:none;margin:5px;" >
				  <h4 class="alert-heading">■ 입력항목을 확인하세요.</h4>
				  <button type="button" class="close" onClick="$('#alert').hide();">
				    <span aria-hidden="true">&times;</span>
				  </button>
				  <hr>
				  <p class="mb-0" id="valChkMsg"></p>
				</div>

				<!-- Modal body -->
				<div class="modal-body" style="overflow-y: auto;">
					<div class="card custom-search-card-div pb-2">
						<!-- feature  -->
						<div class="row">
							<div class="col-5">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<strong>Pump Type</strong>
								</div>
							</div>
							<div class="col-2" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<select id="select_pump_type" style="width: 100%; height: 25px; padding: 0px;" class="selectpicker form-control"></select>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-5">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<strong>Product</strong>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
									<input id="f_product" type="text"class="form-control form-control-sm" placeholder="Product"style="height: 25px; width: 100%;">
									<button type="button" class="btn btn-outline-info" id="btn_pop_apply_product"
										style="height:25px;width:30px;padding-top:0px;padding-left:7px;margin-left:5px;" title="Product 적용">
							 			<i class="fa fa-caret-square-right"></i>
									</button>
									</div>
								</div>
							</div>
						</div>

						<!-- Temperature -->
						<div class="row">
							<div class="col-5" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 65%">
											<strong>Temperature</strong>
										</div>
										<div style="width: 35%">
											<select id="select_temperature_unit"
												style="width: 100%; height: 25px; padding: 0px;"
												class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
										<input id="f_temp_nor" type="text"
											class="form-control form-control-sm" placeholder="Normal"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_temp_min" type="text"
											class="form-control form-control-sm" placeholder="Min"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_temp_max" type="text"
											class="form-control form-control-sm" placeholder="Max"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
									</div>
								</div>
							</div>
						</div>
						<!-- Specific Gravity -->
						<div class="row">
							<div class="col-5">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<strong>Specific Gravity</strong>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
										<input id="f_spec_gravity_nor" type="number"
											class="form-control form-control-sm" placeholder="Normal"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_spec_gravity_min" type="number"
											class="form-control form-control-sm" placeholder="Min"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_spec_gravity_max" type="number"
											class="form-control form-control-sm" placeholder="Max"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
									</div>
								</div>
							</div>
						</div>
						<!-- Viscosity -->
						<div class="row">
							<div class="col-5" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2 " style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 65%">
											<strong>Viscosity</strong>
										</div>
										<div style="width: 35%">
											<select id="select_viscosity_unit"
												style="width: 100%; height: 25px; padding: 0px;"
												class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
										<input id="f_visc_nor" type="number"
											class="form-control form-control-sm" placeholder="Normal"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_visc_min" type="number"
											class="form-control form-control-sm" placeholder="Min"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_visc_max" type="number"
											class="form-control form-control-sm" placeholder="Max"
											maxlength="20"
											style="height: 25px; width: 33%; margin-left: 2px;">
									</div>
								</div>
							</div>
						</div>
						<!-- Vapor Pressure -->
						<div class="row">
							<div class="col-5" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2 " style="min-width: 150px;">
									<div class="d-flex">
										<div style="width: 65%">
											<strong>Vapor Pressure</strong>
										</div>
										<div style="width: 35%">
											<select id="select_vap_pres_unit"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
										<input id="f_vap_pres_nor" type="number"class="form-control form-control-sm" placeholder="Normal"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_vap_pres_min" type="number"class="form-control form-control-sm" placeholder="Min"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_vap_pres_max" type="number"class="form-control form-control-sm" placeholder="Max"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
									</div>
								</div>
							</div>
						</div>
						<!-- Seal Chamber Pressure 씰 챔버 압력-->
						<div class="row">
							<div class="col-5" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2 " style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 65%">
											<strong>Seal Chamber Pressure </strong>
										</div>
										<div style="width: 35%">
											<select id="select_seal_cham_unit"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
										<input id="f_seal_cham_nor" type="text"class="form-control form-control-sm" placeholder="Normal"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_seal_cham_min" type="text"class="form-control form-control-sm" placeholder="Min"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_seal_cham_max" type="text"class="form-control form-control-sm" placeholder="Max"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
									</div>
								</div>
							</div>
						</div>
						<!-- Shaft Speed -->
						<div class="row">
							<div class="col-5" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2 " style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 65%">
											<strong>Shaft Speed </strong>
										</div>
										<div style="width: 35%">
											<select id="select_rpm_unit"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
										<input id="f_rpm_nor" type="text"class="form-control form-control-sm" placeholder="Normal"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_rpm_min" type="text"class="form-control form-control-sm" placeholder="Min"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
										<input id="f_rpm_max" type="text"class="form-control form-control-sm" placeholder="Max"maxlength="20"style="height: 25px; width: 33%; margin-left: 2px;">
									</div>
								</div>
							</div>
						</div>
						<!-- Shaft Dia -->
						<div class="row">
							<div class="col-5" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2 " style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 65%">
											<strong>Shaft Dia. </strong>
										</div>
										<div style="width: 35%">
											<select id="select_shaft_size_unit"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-7" style="padding-left: 0px;">
								<div class="pt-2 custom-responsive-p2 pr-1">
									<div class="d-flex">
										<input id="f_shaft_size" type="number"class="form-control form-control-sm" placeholder="Shaft Size"maxlength="20"style="height: 25px; width: 100%; margin-left: 2px;">
									</div>
								</div>
							</div>
						</div>
					</div>

					<div class="card custom-search-card-div pb-2 mt-2">
						<div class="row">
							<div class="col-lg-6 col-sm-12" >
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>FTA</strong>
										</div>
										<div style="width: 60%">
												<select id="select_fta_yn"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control">
												<option value='Y'>Y</option>
												<option value='N'>N</option>
											</select>
										</div>
									</div>
								</div>
							</div>
							<%-- 미사용 --%>
							<div class="col-lg-6 col-sm-12" style="height:1px;visibility:hidden;">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex" >
										<div style="width: 40%">
											<strong>Equipment Type</strong>
										</div>
										<div style="width: 60%">
											<select id="select_equipment_type"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						
						
						<div class="row" id="divFtaApplication">
							<div class="col-12 pr-3" >
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20%">
											<strong>Application</strong>
										</div>
										<div style="width: 80%">
											<select id="select_application"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divFtaService">
							<div class="col-12 pr-3" >
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20%">
											<strong>Service</strong>
										</div>
										<div style="width: 80%">
											<select id="select_service"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divFtaEquipment">
							<div class="col-12 pr-3" >
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20%">
											<strong>Equipment</strong>
										</div>
										<div style="width: 80%">
											<select id="select_equipment"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						
						
						<%--
						<div class="row" id="fta_service_equipment">
							<div class="col-lg-6 col-sm-12" id="fta_service">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Service</strong>
										</div>
										<div style="width: 60%">
											<select id="select_service"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-lg-6 col-sm-12" id="fta_equipment">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Equipment</strong>
										</div>
										<div style="width: 60%">
											<select id="select_equipment"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							
						</div>
						--%>
						
						
						
						<div class="row" id="divEndUser">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>End User</strong>
										</div>
										<div style="width: 60%">
											<select id="select_endUser"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divGroup">
							<div class="col-12 pr-3">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20%">
											<strong>[GS] Group</strong>
										</div>
										<div style="width: 80%">
											<select id="select_group"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divServiceGs">
							<div class="col-12 pr-3" >
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20%">
											<strong>[GS] Service</strong>
										</div>
										<div style="width: 80%">
											<select id="select_service_gs"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divCase">
							<div class="col-12 pr-3" >
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20%">
											<strong>[GS] Case</strong>
										</div>
										<div style="width: 80%">
											<select id="select_case"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						
						<div class="row">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Seal Size (In)</strong>
										</div>
										<div style="width: 60%">
											<input id="SEAL_SIZE" type="number" class="form-control form-control-sm" maxlength="20" style="height: 25px; width: 100%;">
										</div>
									</div>
								</div>
							</div>
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Quench Type <br/><font style="font-size:12px">(Plan 62적용 시)</font></strong>
										</div>
										<div style="width: 60%">
											<select id="select_quench_type"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<!-- 지정 -->
						<div class="row">
						    <div class="col"><hr></div>
						    <div class="col-auto">지정</div>
						    <div class="col"><hr></div>
						</div>
						<div class="row">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Seal Type</strong>
										</div>
<!-- 											<input id="f_seal_type_dir" type="text" class="form-control form-control-sm" maxlength="20" style="height: 25px; width: 98%;"> -->
										<div style="width: 30%">
											<input id="f_seal_inner_dir" type="text" class="form-control form-control-sm" maxlength="20" style="height: 25px; width: 98%;">
										</div>
										/
										<div style="width: 30%">
											<input id="f_seal_outer_dir" type="text" class="form-control form-control-sm" maxlength="20" style="height: 25px; width: 98%;">
										</div>
									</div>
								</div>
							</div>
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 39%">
											<strong>Material</strong>
										</div>
<!-- 											<input id="f_material_dir" type="text" class="form-control form-control-sm" maxlength="20" style="height: 25px; width: 100%;"> -->
										<div style="width: 8%">
											<input id="f_m_in_1" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
										<div style="width: 8%">
											<input id="f_m_in_2" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
										<div style="width: 8%">
											<input id="f_m_in_3" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
										<div style="width: 8%">
											<input id="f_m_in_4" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
											/
										<div style="width: 8%">
											<input id="f_m_out_1" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
										<div style="width: 8%">
											<input id="f_m_out_2" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
										<div style="width: 8%">
											<input id="f_m_out_3" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
										<div style="width: 8%">
											<input id="f_m_out_4" type="text" maxlength="1" style="height: 25px; width: 100%;padding-left:1px;padding-right:1px;text-align:center;text-transform: uppercase;">
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>API Plan</strong>
										</div>
										<div style="width: 60%">
											<input id="f_api_plan_dir" type="text" class="form-control form-control-sm" maxlength="20" style="height: 25px; width: 100%;">
										</div>
									</div>
								</div>
							</div>
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Single/Dual</strong>
										</div>
										<div style="width: 60%">
												<select id="select_s_d_gb"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control">
												<option value=''></option>
												<option value='S'>Single</option>
												<option value='D'>Dual</option>											
											</select>
										</div>
									</div>
								</div>
							</div>							
						</div>
						<!-- Requirement -->
						<div class="row">
						    <div class="col"><hr></div>
						    <div class="col-auto">Requirement</div>
						    <div class="col"><hr></div>
						</div>
						<div class="row">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>API 682 적용</strong>
										</div>
										<div style="width: 60%">
											<select id="select_api682_yn"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control">
												<option value='Y'>Y</option>
												<option value='N'>N</option>											
											</select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Bellows 적용</strong>
										</div>
										<div style="width: 60%">
											<select id="select_bellows_yn"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control">
												<option value=''></option>
												<option value='Y'>Y</option>
												<option value='N'>N</option>											
											</select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Cartridge</strong>
										</div>
										<div style="width: 60%">
											<select id="select_cartridge_type"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong style="font-size: smaller;">Seal Configuration</strong>
										</div>
										<div style="width: 60%">
											<select id="select_seal_config"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control"></select>
										</div>
									</div>
								</div>
							</div>
						</div>		
						<div class="row">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Split</strong>
										</div>
										<div style="width: 60%">
											<select id="select_split_yn"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control">
												<option value=''></option>
												<option value='Y'>Y</option>
												<option value='N'>N</option>											
											</select>
										</div>
									</div>
								</div>
							</div>
						</div>			
						<!-- Product 특성 -->
						<div class="row">
						    <div class="col"><hr></div>
						    <div class="col-auto">Product 특성</div>
						    <div class="col"><hr></div>
						</div>	
						<div id="divProCrt">
							<div class="row">
								<div class="col-lg-6 col-sm-12">
									<div class="p-2 custom-responsive-p2 " style="min-width: 120px;">
										<div class="d-flex">
											<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
												<input type="checkbox" class="custom-control-input" id="pc_toxic_chk"> 
												<label class="custom-control-label" for="pc_toxic_chk" style="margin-top: -3px;">Toxic</label>
											</div>
										</div>
									</div>
								</div>
								<div class="col-lg-6 col-sm-12">
									<div class="p-2 custom-responsive-p2">
										<div class="d-flex">
											<div class="custom-control custom-checkbox " style="width: 50%; background: url();">
												<input type="checkbox" class="custom-control-input" id="pc_hazard_chk"> 
												<label class="custom-control-label" for="pc_hazard_chk" style="margin-top: -3px;">Hazardous</label>
											</div>
											<div class="custom-control custom-checkbox " style="width: 50%; background: url();">
												<input type="checkbox" class="custom-control-input" id="pc_flam_chk"> 
												<label class="custom-control-label" for="pc_flam_chk" style="margin-top: -3px;">Flammable</label>
											</div>
										</div>
									</div>
								</div>
							</div>		
							<div class="row">
								<div class="col-lg-6 col-sm-12">
									<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
										<div class="d-flex">
											<div class="custom-control custom-checkbox " style="width: 100%; background: url();">
												<input type="checkbox" class="custom-control-input" id="pc_leakage_chk"> 
												<label class="custom-control-label" for="pc_leakage_chk" style="margin-top: -3px;">Crystallizaion/Polymerizaion<font style="font-size:12px"> (Leakage)</font></label>
											</div>
										</div>
									</div>
								</div>
								<div class="col-lg-6 col-sm-12">
									<div class="p-2 custom-responsive-p2">
										<div class="d-flex">
											<div class="custom-control custom-checkbox " style="width: 100%; background: url();">
												<input type="checkbox" class="custom-control-input" id="pc_high_corr_chk"> 
												<label class="custom-control-label" for="pc_high_corr_chk" style="margin-top: -3px;">High Corrosive<font style="font-size:12px"> (장비 재질 고려)</font></label>
											</div>
										</div>
									</div>
								</div>
							</div>	
							<div class="row pb-3">
								<div class="col-lg-6 col-sm-12">
									<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
										<div class="d-flex">
											<div class="custom-control custom-checkbox " style="width: 100%; background: url();">
												<input type="checkbox" class="custom-control-input" id="pc_cool_trouble_chk"> 
												<label class="custom-control-label" for="pc_cool_trouble_chk" style="margin-top: -3px;">굳는성질 (Pour Point,냉각,상온 등)<font style="font-size:12px"></font></label>
											</div>
										</div>
									</div>
								</div>
							</div>	
						</div>
						<!-- 가변 개수 처리 -->
						<div class="row" id="divSlurry">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>Solid/Slurry</strong>
										</div>
										<div style="width: 60%">
											<select id="select_solid_gb_yn"style="width: 100%; height: 25px; padding: 0px;margin-left:-3px" class="selectpicker form-control">
												<option value=''></option>
												<option value='N'>N</option>
												<option value='Y'>Y</option>											
												<option value='Y1'>Y (상세입력)</option>											
											</select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divSolidSize">
							<div class="col-12 pr-3" >
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20%">
											<strong>Solid Size(μm)</strong>
										</div>
										<div style="width: 20%;margin-left:-3px;margin-right:5px;">
											<input id="solid_size_nor" type="number"class="form-control form-control-sm" placeholder="Normal"maxlength="20" style="height: 25px; width: 100%;">
										</div>
										<div style="width: 20%;margin-right:5px;">
											<input id="solid_size_min" type="number"class="form-control form-control-sm" placeholder="Min"maxlength="20"style="height: 25px; width: 100%;">
										</div>
										<div style="width: 20%;margin-right:5px;">
											<input id="solid_size_max" type="number"class="form-control form-control-sm" placeholder="Max"maxlength="20"style="height: 25px; width: 100%;">
										</div>
										<div style="width: 20%">
											<div class="custom-control custom-checkbox" style="background: url();width:100%">Maximum &nbsp;<input type="checkbox" class="custom-control-input" id="solid_size_check"  style="margin-left:5px;" > <label class="custom-control-label" for="solid_size_check" style="margin-top: 3px; margin-left: 15px"></label>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divSolidCont">
							<div class="col-12" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 19%">
											<strong>Solid 농도(ppm)</strong>
										</div>
										<div style="width: 20%">
											<input id="solid_cont" type="number"class="form-control form-control-sm" maxlength="20"style="height: 25px; width: 100%;">
										</div>
										<div style="width: 61%">(1% = 10,000ppm)</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divChloride">
							<div class="col-lg-6 col-sm-12">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 40%">
											<strong>PH</strong>
										</div>
										<div style="width: 60%">
											<input id="PH" type="number"class="form-control form-control-sm" maxlength="20"style="height: 25px; width: 100%;">
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row" id="divBrine">
							<div class="col-11" style="padding-right: 0px;">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<div class="d-flex">
										<div style="width: 20.5%">
											<strong>Brine 구분</strong>
										</div>
										<div style="width: 31.5%; margin-right: 10px">
											<select id="select_brine_gb"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control"></select>
										</div>
										<div style="width: 31.5%">
											<select id="select_brine_sub_gb"style="width: 100%; height: 25px; padding: 0px;"class="selectpicker form-control">
												<option value=''></option>
												<option value='EG'>EG</option>
												<option value='PG'>PG</option>											
												<option value='NONE'>불확실</option>											
											</select>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div class="row">
						    <div class="col"><hr></div>
						    <div class="col-auto">Products 농도</div>
						    <div class="col"><hr></div>
						</div>
						<div id="divProducts"></div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<%-- file upload pop --%>
	<div class="modal" id="excelUpload" role="dialog" aria-hidden="false"
		data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content" style="height: 300px;">

				<!-- Modal Header -->
				<div class="modal-header">
					<div class=" h5 modal-title">Excel File Upload</div>
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>

				<!-- Modal body -->
				<div class="modal-body">
					<div class="popup_area">
						<div id="file_uploader" style="margin-top: 20px;">
							<input name="input_files" id="input_files" type="file"
								aria-label="files" />
						</div>
					</div>
					<div class="row">
						<div class="col-12">
							<!--  Button -->
							<div class="col-12 mt-3 text-right">
								<button type="button" class="btn btn-outline-success"
									id="btn_excel_upload_ok">
									Upload <i class="fa fa-save"></i>
								</button>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="modal" id="featureRangeInfo" role="dialog"
		aria-hidden="false" data-backdrop="static" data-keyboard="false">
		<!-- 0401 -->
		<div class="modal-dialog modal-lg">
			<div class="modal-content" style="height: 820px;">
				<div class="modal-header">
					<div class=" h5 modal-title">Model Feature Range</div>
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>

				<div class="modal-body" style="overflow-y: auto;">
					<div class="card custom-search-card-div pb-2">
						<!-- feature  -->
						<div class="row">
							<div class="col-5" style="padding-left: 30px;">
								<div class="p-2 custom-responsive-p2" style="min-width: 120px;">
									<strong>Feature Name</strong>
								</div>
								<div class="p-2 custom-responsive-p2" id="feature_col_div">
									<input id="feature_col0" type="text"
										style="height: 25px; width: 100%; border: none;"> <input
										id="feature_col1" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col2" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col3" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col4" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col5" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col6" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col7" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col8" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col9" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col10" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col11" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col12" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col13" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col14" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col15" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col16" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col17" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
									<input id="feature_col18" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none;">
								</div>

							</div>

							<div class="col-2"
								style="padding-left: 15px; padding-right: 15px;">
								<div class="p-2 custom-responsive-p2"
									style="min-width: 120px; text-align: center;">
									<strong>Unit</strong>
								</div>
								<div class="p-2 custom-responsive-p2" id="unit_val_div">
									<input id="unit_val0" type="text"
										style="height: 25px; width: 100%; border: none; text-align: center;">
									<input id="unit_val1" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val2" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val3" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val4" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val5" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val6" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val7" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val8" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val9" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val10" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val11" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val12" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val13" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val14" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val15" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val16" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val17" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="unit_val18" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
								</div>
							</div>

							<div class="col-2"
								style="padding-left: 15px; padding-right: 15px;">
								<div class="p-2 custom-responsive-p2"
									style="min-width: 120px; text-align: center;">
									<strong>Min Value</strong>
								</div>
								<div class="p-2 custom-responsive-p2" id="mim_val_div">
									<input id="min_val0" type="text"
										style="height: 25px; width: 100%; border: none; text-align: center;">
									<input id="min_val1" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val2" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val3" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val4" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val5" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val6" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val7" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val8" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val9" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val10" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val11" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val12" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val13" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val14" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val15" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val16" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val17" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="min_val18" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
								</div>
							</div>

							<div class="col-3" style="padding-right: 30px;">
								<div class="p-2 custom-responsive-p2"
									style="min-width: 120px; text-align: center;">
									<strong>Max Value</strong>
								</div>
								<div class="p-2 custom-responsive-p2" id="max_val_div">
									<input id="max_val0" type="text"
										style="height: 25px; width: 100%; border: none; text-align: center;">
									<input id="max_val1" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val2" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val3" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val4" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val5" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val6" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val7" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val8" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val9" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val10" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val11" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val12" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val13" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val14" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val15" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val16" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val17" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
									<input id="max_val18" type="text"
										style="height: 25px; width: 100%; margin-top: 10px; border: none; text-align: center;">
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<%-- file upload pop --%>
	
	<%-- historyMng pop --%>
	<div class="modal" id="historyMng" role="dialog" aria-hidden="false"
		data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<!-- <div class=" h5 modal-title" >이력관리</div> -->
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>

				<div class="modal-body">
					<div class="row">
						<div class="col-2">
							<p class="h5" style="width: 150px;">
								<i class="fas fa-search"></i> <strong class="text-primary">이력관리</strong>
							</p>
						</div>
					</div>
					<div class="row">
						<div class="col-12 pt-1">
							<!--  History Grid  -->
							<div id="jsGrid_history"></div>
						</div>
					</div>	
					<div class="row">
						<div class="col-12">
							<div id="externalPager" style="margin-top:-20px;"></div>			
						</div> 
					</div>
			
					<div class="row">
						<div class="col-12">
							<div class="pt-2 custom-responsive-p2">
								<input id="saveTitle" type="text"class="form-control form-control-sm" placeholder="저장할 제목 표시"maxlength="20">
							</div>
							<div class="pt-2 custom-responsive-p2 flex-grow-1">
								<input id="saveRemark" type="text"class="form-control form-control-sm" placeholder="상세 설명"maxlength="100">
							</div>
						</div>
						<div class="col-12 d-flex flex-row-reverse">
							<div class="p-2 custom-responsive-p2">
								<button id="loadHistory" type="button"class="btn btn-outline-success">선택정보 불러오기 <i class="fas fa-download"></i></button>
								<button id="savHistory" type="button"class="btn btn-outline-success">현재조건 신규저장 <i class="far fa-save"></i></button>
							</div>
						</div>
					</div>	
				</div>
			</div>
		</div>
	</div>
	<%-- historyMng pop --%>
	
	<%-- fileData pop --%>
	<div class="modal" id="fileDataPop" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>

				<div class="modal-body">
					<div class="row">
						<div class="col-2">
							<p class="h5" style="width: 150px;">
								<i class="fas fa-search"></i> <strong class="text-primary">자료실</strong>
							</p>
						</div>
					</div>
					<div class="row">
						<div class="col-12 pt-1">
							<div id="jsGrid_library"></div>
						</div>
					</div>	
				</div>
			</div>
		</div>
	</div>

	<%-- file upload pop up --%>
	<div class="modal" id="pdfModal" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body modal-lg">
					<div id="pdf"></div>
				</div>
			</div>		
		</div>
	</div>
	<div id='viewDiv'></div>
	
	<%-- Download Frame --%>
	<iframe id="fileDownFrame" style="display: none;"></iframe>
	
	<%--Modal: AS History Pop--%>
<!-- Modal -->
<div class="modal fade" id="ASHistoryPopup" tabindex="-1" aria-labelledby="ASHistoryPopup_label" aria-hidden="true">
  <div class="modal-dialog modal-dialog-scrollable modal-xl modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="ASHistoryPopup_label"><strong>AS 이력</strong></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
		<div class="overflow-auto">
			<div id="asHistoryGrid"></div>
		</div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>
<%--Modal: AS History Pop--%>
</body>
</html>