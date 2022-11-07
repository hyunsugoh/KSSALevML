<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<style>
	.btn btn-sm btn-outline-primary{
		width:70px;
	}
	/*TAB CSS*/
	ul.tabs {
	    margin: 0;
	    padding: 0;
	    float: left;
	    list-style: none;
	    height: 32px; /*--Set height of tabs--*/
	    border-bottom: 1px solid #999;
	    border-left: 1px solid #999;
	    width: 100%;
	}
	ul.tabs li {
	    float: left;
	    margin: 0;
	    padding: 0;
	    height: 31px; /*--Subtract 1px from the height of the unordered list--*/
	    line-height: 31px; /*--Vertically aligns the text within the tab--*/
	    border: 1px solid #999;
	    border-left: none;
	    margin-bottom: -1px; /*--Pull the list item down 1px--*/
	    overflow: hidden;
	    position: relative;
	    background: #e0e0e0;
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
	    background: #ccc;
	}
	html ul.tabs li.active, html ul.tabs li.active a:hover  {
	     /*--Makes sure that the active tab does not listen to the hover properties--*/
	    background: #fff;
	    /*--Makes the active tab look like it's connected with its content--*/
	    border-bottom: 1px solid #fff; 
	}
	/*Tab Conent CSS*/
	.tab_container {
	    border: 1px solid #999;
	    border-top: none;
	    overflow: hidden;
	    clear: both;
	    float: left; 
	    width: 100%;
	    background: #fff;
	}
	.tab_content {
	    padding: 5px;
	    font-size: 1.2em;
	}    
</style>
<script>
var mgGrid;
var sowGrid;
var stGrid;
var list =[];

$(document).ready(function () {
	//div hide
	$("#div_mg").hide();
	$("#div_sow").hide();
	$("#div_st").hide();
});

//tab event
function tabEvent(tabStr) {
	if(tabStr == "MG"){
	    $("#div_sow").hide();
	    $("#div_st").hide();
	    $("#tabLi_sow").removeClass("active");
	    $("#tabLi_st").removeClass("active");
	    $("#tabLi_mg").addClass("active");
	    $("#div_mg").show();	
	    mgCreateElements(list);
	}else if(tabStr == "SOW"){
	    $("#div_mg").hide();
	    $("#div_st").hide();
	    $("#tabLi_mg").removeClass("active");
	    $("#tabLi_st").removeClass("active");
	    $("#tabLi_sow").addClass("active");
	    $("#div_sow").show();	
	    sowCreateElements(list);
	}else if(tabStr == "ST"){
	    $("#div_sow").hide();
	    $("#div_mg").hide();
	    $("#tabLi_sow").removeClass("active");
	    $("#tabLi_mg").removeClass("active");
	    $("#tabLi_st").addClass("active");
	    $("#div_st").show();
	    stCreateElements(list);
	}
    return false;
}

//조회
$(function(){
	$('#btn_mg_search').click(function(){
		callMgList();
	});
	$('#btn_sow_search').click(function(){ 
		callSowList();
	});
	$('#btn_st_search').click(function(){
		callStList();
	});
});

function callMgList(){
	var url = "/rule/callMgList.do";
	var param = {
		PRODUCT : $("#mg_product").val()
	};
	$.doPost({
		url:url,
		data : param,
		success : function(data, status, xhr){
			mgCreateElements(data);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
};

function callSowList(){
	var url = "/rule/callSowList.do";
	var param = {
		SEAL_TYPE : $("#sow_sealType").val()
	};
	$.doPost({
		url:url,
		data : param,
		success : function(data, status, xhr){
			sowCreateElements(data);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
};

function callStList(){
	var url = "/rule/callStList.do";
	var param = {
		SEAL_TYPE : $("#st_sealType").val()
	};
	$.doPost({
		url:url,
		data : param,
		success : function(data, status, xhr){
			stCreateElements(data);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
};

function mgCreateElements(data){
	$('#mgGrid').empty();
	var SBGridProperties = {}; 
	SBGridProperties.parentid = 'mgGrid';  	// [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'mgGrid';         	// [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    		// [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.
	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.extendlastcol = 'scroll';
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;
    SBGridProperties.rowheader = ['seq', 'update'];
    SBGridProperties.height = ($(document).height()-350)+"px";
	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['No.'],			ref: 'R_NO',		width:'150px',  style:'text-align:center',	type : 'output'}
		,{caption : ['PRODUCT(FLUID)'], ref: 'PRODUCT',	width:'200px', 	style:'text-align:center',	type : 'output'}
		,{caption : ['구분'], 		ref: 'PRODUCT_GB',		width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['농도(%) FR'], 	ref: 'CONT_FR',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['농도(%) TO'], 	ref: 'CONT_TO',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['온도(C) FR'], 	ref: 'TEMP_FR',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['온도(C) TO'], 	ref: 'TEMP_TO',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['재질 Type'], 	ref: 'MTRL_TYPE',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['재질 코드'], 		ref: 'MTRL_CD',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['GRADE'], 		ref: 'GRADE',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['비고'], 		ref: 'RMKS',	width:'300px', 	style:'text-align:left', 	type : 'output'}
	];			
	mgGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
};

function sowCreateElements(data){
	$('#sowGrid').empty();
	var SBGridProperties = {}; 
	SBGridProperties.parentid = 'sowGrid';  	// [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'sowGrid';         	// [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    		// [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.
	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.extendlastcol = 'scroll';
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;
    SBGridProperties.rowheader = ['seq', 'update'];
    SBGridProperties.height = ($(document).height()-350)+"px";
	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['인덱스'],							ref: 'R_NO',		width:'150px',  style:'text-align:center',	type : 'output'}
		,{caption : ['No.'], 						ref: 'PRODUCT',		width:'150px', 	style:'text-align:center',	type : 'output'}
		,{caption : ['Seal Type'], 					ref: 'PRODUCT_GB',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Seal 표준구분(DURA,BWIP...)'], 	ref: 'CONT_FR',	width:'230px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Seal 구분 Type'], 				ref: 'CONT_TO',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Rotation/Stationary 구분'], 	ref: 'TEMP_FR',	width:'230px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['PV Curve'], 					ref: 'TEMP_TO',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Shaft Size Min (MM)'], 		ref: 'SHAFT_SIZE_MIN',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Shaft Size Max (MM)'], 		ref: 'SHAFT_SIZE_MAX',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Seal Size Min (MM)'], 		ref: 'SEAL_SIZE_MIN',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Seal Size Max (MM)'], 		ref: 'SEAL_SIZE_MAX',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material 1'], 				ref: 'MTRL_CD_M1',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material 2'], 				ref: 'MTRL_CD_M2',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material 3'], 				ref: 'MTRL_CD_M3',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material 4'], 				ref: 'MTRL_CD_M4',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Press Outer Dynamic (BARG)'], ref: 'PRESS_OUT_D',	width:'250px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Shaft Speed Min RPM'], 		ref: 'SHAFT_SPEED_1_MIN',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Shaft Speed Min Line Speed (ft/s)'],ref: 'SHAFT_SPEED_2_MIN',	width:'250px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Shaft Speed Max RPM'], 		ref: 'SHAFT_SPEED_1_MAX',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Shaft Speed Max Line Speed (ft/s)'],ref: 'SHAFT_SPEED_2_MAX',	width:'250px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Viscosity Min'], 				ref: 'VISCOSITY_MIN',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Viscosity Max'], 				ref: 'VISCOSITY_MAX',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Viscosity Min Unit'], 		ref: 'VISCOSITY_MIN_UNIT',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Viscosity Max Unit'], 		ref: 'VISCOSITY_MAX_UNIT',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Temperature Min (C)'], 		ref: 'TEMP_MIN',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Temperature Max (C)'], 		ref: 'TEMP_MAX',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['S.G. Min'], 					ref: 'SG_MIN',		width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['S.G. Max'], 					ref: 'SG_MAX',		width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Vapor Press Margin Min (Barg)'],	ref: 'VAP_PRES_MARGIN_MIN',	width:'250px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Vapor Press Margin Max (Barg)'],	ref: 'VAP_PRES_MARGIN_MAX',	width:'250px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Solid (%)'], 		ref: 'SOLID',	width:'150px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['비고'], 		ref: 'RMKS',	width:'300px', 	style:'text-align:left', 	type : 'output'}
		
		
	];			
	sowGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
};

function stCreateElements(data){
	$('#stGrid').empty();
	var SBGridProperties = {}; 
	SBGridProperties.parentid = 'stGrid';  	// [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'stGrid';         	// [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    		// [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.
	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.extendlastcol = 'scroll';
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;
    SBGridProperties.rowheader = ['seq', 'update'];
    SBGridProperties.height = ($(document).height()-350)+"px";
	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['인덱스'],				ref: 'R_IDX',		width:'150px',  style:'text-align:center',	type : 'output'}
		,{caption : ['No.'],			ref: 'R_NO',		width:'150px',  style:'text-align:center',	type : 'output'}
		,{caption : ['Seal Type(Seal Model)'], 	ref: 'SEAL_TYPE',	width:'250px', 	style:'text-align:center',	type : 'output'}
		,{caption : ['구분'], 			ref: 'SEAL_GB',		width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Configuration'], 	ref: 'CONFIG',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-In-1'], 	ref: 'MTRL_CD_IN_M1',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-In-2'], 	ref: 'MTRL_CD_IN_M2',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-In-3'], 	ref: 'MTRL_CD_IN_M3',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-In-4'], 	ref: 'MTRL_CD_IN_M4',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-out-1'], 	ref: 'MTRL_CD_OUT_M1',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-out-2'], 	ref: 'MTRL_CD_OUT_M2',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-out-3'], 	ref: 'MTRL_CD_OUT_M3',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Material-out-4'], 	ref: 'MTRL_CD_OUT_M4',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Small Part Material'],ref: 'S_MTRL',		width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Small Part Y/N'], 	ref: 'S_MTRL_YN',	width:'180px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Grand & Sleeve Material For Bellows'], 	ref: 'GS_MTRL',	width:'300px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['Database'], 		ref: 'DB_RMKS',	width:'300px', 	style:'text-align:center', 	type : 'output'}
		,{caption : ['비고'], 		ref: 'RMKS',	width:'300px', 	style:'text-align:left', 	type : 'output'}
	];			
	stGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
};

</script>
<body>
	<!--  화면이름 / 버튼 -->
	<div class="col-12 p-3">
		<div class="row">
			<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
			<div class="col-6" >
				<div class="h5" style="float:left;width:50%;">
					<strong><i class="fas fa-sitemap"></i> <span class="ml-1">Rule-Based 표준</span></strong>
				</div>	
			</div>
		</div>
	</div>

	<div id='wrapper' style='padding-top:5px;clear:both;'>
		<ul class='tabs'>
			<li id='tabLi_mg' class='btn text-right'><a onclick='tabEvent("MG");' style='font-size:0.8em;'>Material Guide</a></li>
			<li id='tabLi_sow' class='btn text-right'><a onclick='tabEvent("SOW");' style='font-size:0.8em;'>Seal Operation WIndow</a></li>
			<li id='tabLi_st' class='btn text-right'><a onclick='tabEvent("ST");' style='font-size:0.8em;'>Seal Type별 표준재질</a></li>
		</ul>
		<div class='tab_container'>
			<div id='div_mg' class='tab_content'>
				<!-- first -->
				<div class="card  ml-3 mr-3">
					<div class="row" style="height:60px;">
						<div class="col-lg-1 text-center col-sm-12 my-auto"  style="min-width:120px;">
							<span class="font-weight-bold">Product</span>
						</div>
						<div class="col-lg-2 text-center col-sm-12 my-auto" >
							<input type="text" id="mg_product" name="ruleName"/>
						</div>
						<div class="col-lg-1 text-right col-sm-12 my-auto" >
							<button type="button" class="btn btn-outline-success"  id="btn_mg_search">조회 <i class="fa fa-search"></i></button>
						</div>
					</div>
				</div>
				<div class="card  ml-3 mr-3 mt-1">
					<div class="row">
						<div class="col-12">
							<div id="mgGrid" style="width:100%; height:100%"></div>
						</div>
					</div>
				</div>
			</div>
			<div id='div_sow' class='tab_content'>
				<!-- second -->
				<div class="card  ml-3 mr-3">
					<div class="row" style="height:60px;">
						<div class="col-lg-1 text-center col-sm-12 my-auto"  style="min-width:120px;">
							<span class="font-weight-bold">Seal Type</span>
						</div>
						<div class="col-lg-2 text-center col-sm-12 my-auto" >
							<input type="text" id="sow_sealType" name="sow_sealType"/>
						</div>
						<div class="col-lg-1 text-right col-sm-12 my-auto" >
							<button type="button" class="btn btn-outline-success"  id="btn_sow_search">조회 <i class="fa fa-search"></i></button>
						</div>
					</div>
				</div>
				<div class="card  ml-3 mr-3 mt-1">
					<div class="row">
						<div class="col-12">
							<div id="sowGrid" style="width:100%; height:100%"></div>
						</div>
					</div>
				</div>
			</div>
			<div id='div_st' class='tab_content'>
				<!-- third -->
				<div class="card  ml-3 mr-3">
					<div class="row" style="height:60px;">
						<div class="col-lg-1 text-center col-sm-12 my-auto"  style="min-width:120px;">
							<span class="font-weight-bold">Seal Type</span>
						</div>
						<div class="col-lg-2 text-center col-sm-12 my-auto" >
							<input type="text" id="st_sealType" name="st_sealType"/>
						</div>
						<div class="col-lg-1 text-right col-sm-12 my-auto" >
							<button type="button" class="btn btn-outline-success"  id="btn_st_search">조회 <i class="fa fa-search"></i></button>
						</div>						
					</div>
				</div>
				<div class="card  ml-3 mr-3 mt-1">
					<div class="row">
						<div class="col-12">
							<div id="stGrid" style="width:100%; height:100%"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
</body>