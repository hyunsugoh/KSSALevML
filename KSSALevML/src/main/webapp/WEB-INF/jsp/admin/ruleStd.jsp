<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<style>
	tr.highlight td.jsgrid-cell {
		background-color: #BBDEFB;
	}
	.jsgrid-header-row  {
	    text-align: center;
	}
	.jsgrid>.jsgrid-pager-container{
		color:#007bff;
	}
	.btn btn-sm btn-outline-primary{
		width:70px;
	}
    .grid-cell-grey{
        background: #f3f3f3;
        cursor: pointer;
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
// combo data 가져오기
$(document).ready(function () {
	//그리드 init
	mgGridView.initView(); 
	sowGridView.initView(); 
	stGridView.initView(); 
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
	}else if(tabStr == "SOW"){
	    $("#div_mg").hide();
	    $("#div_st").hide();
	    $("#tabLi_mg").removeClass("active");
	    $("#tabLi_st").removeClass("active");
	    $("#tabLi_sow").addClass("active");
	    $("#div_sow").show();	
	}else if(tabStr == "ST"){
	    $("#div_sow").hide();
	    $("#div_mg").hide();
	    $("#tabLi_sow").removeClass("active");
	    $("#tabLi_mg").removeClass("active");
	    $("#tabLi_st").addClass("active");
	    $("#div_st").show();	
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
	var mgProduct = $("#mg_product").val();
	var paramData = {
		PRODUCT : mgProduct
	};
	$.ajax({ 
		type : "POST",
		url: "callMgList.do",
		data : JSON.stringify(paramData),
		contentType: "application/json;charset=UTF-8",
		success : function(data){
			mgGridView.setData(data);
		},
		error : function(){
			alert("리스트를 불러오는데 실패하였습니다.");
		}
	});
}

function callSowList(){
	var sowSealType = $("#sow_sealType").val();
	var paramData = {
		SEAL_TYPE : sowSealType
	};
	$.ajax({ 
		type : "POST",
		url: "callSowList.do",
		data : JSON.stringify(paramData),
		contentType: "application/json;charset=UTF-8",
		success : function(data){
			sowGridView.setData(data);
		},
		error : function(){
			alert("리스트를 불러오는데 실패하였습니다.");
		}
	});
}

function callStList(){
	var stSealType = $("#st_sealType").val();
	var paramData = {
		SEAL_TYPE : stSealType
	};
	$.ajax({ 
		type : "POST",
		url: "callStList.do",
		data : JSON.stringify(paramData),
		contentType: "application/json;charset=UTF-8",
		success : function(data){
			stGridView.setData(data);
		},
		error : function(){
			alert("리스트를 불러오는데 실패하였습니다.");
		}
	});
}

//init
var mgGridView = {
	initView: function(){
		this.target = new ax5.ui.grid();
		this.target.setConfig({
			target : $('[data-ax5grid="first-grid"]'),
			frozenRowIndex: 0,
			showLineNumber: true,
			showRowSelector: false,
			multipleSelect: true,
			lineNumberColumnWidth: 40,
			rowSelectorColumnWidth: 28,
			sortable: true, // 모든 컬럼에 정렬 아이콘 표시
			multiSort: false, // 다중 정렬 여부
			remoteSort: false,
			header: {
	        	align: "center",
	            columnHeight: 28
	        },
	        body: {
	        	align: "center",
	            columnHeight: 28
			},
	    	columns: [
	    		{key: "R_NO",		width: 150, label: "No.", 	align: "center", editor: {type: "number"}}
	    		,{key: "PRODUCT",	width: 150,	label: "PRODUCT(FLUID)", 	align: "center", editor: {type: "text"}}
	    		,{key: "PRODUCT_GB",width: 150, label: "구분", 	align: "center", editor: {type: "text"}}
	    		,{key: "CONT_FR",	width: 150, label: "농도(%) FR", 	align: "center", editor: {type: "text"}}
	    		,{key: "CONT_TO",	width: 150, label: "농도(%) TO", 	align: "center", editor: {type: "text"}}
	    		,{key: "TEMP_FR",	width: 150, label: "온도(C) FR", 	align: "center", editor: {type: "text"}}
	    		,{key: "TEMP_TO",	width: 150, label: "온도(C) TO", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_TYPE",	width: 150, label: "재질 Type",	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD",	width: 150, label: "재질 코드",	align: "center", editor: {type: "text"}}
	    		,{key: "GRADE",		width: 150, label: "GRADE",	align: "center", editor: {type: "text"}}
	    		,{key: "RMKS",		width: 150, label: "비고", 	align: "center", editor: {type: "text"}}
	    	]
		});
		return this;
	},
	setData: function (data) {
		var list = [];
		for (var i = 0, l = data.getMgList.length; i < l; i++) {
	    	list.push(data.getMgList[i]);
	    }
		this.target.setData({
			list: list
			,page: {}
	    });
	    return this;
	}
};

var sowGridView = {
	initView: function(){
		this.target = new ax5.ui.grid();
		this.target.setConfig({
			target : $('[data-ax5grid="second-grid"]'),
			frozenRowIndex: 0,
			showLineNumber: true,
			showRowSelector: false,
			multipleSelect: true,
			lineNumberColumnWidth: 40,
			rowSelectorColumnWidth: 28,
			sortable: true, // 모든 컬럼에 정렬 아이콘 표시
			multiSort: false, // 다중 정렬 여부
			remoteSort: false,
			header: {
	        	align: "center",
	            columnHeight: 28
	        },
	        body: {
	        	align: "center",
	            columnHeight: 28
			},
	    	columns: [
	    		{key: "R_IDX",			width: 150, label: "인덱스", 	align: "center",   editor: {type: "text"}}
	    		,{key: "R_NO",			width: 150, 	label: "No.", 	align: "center", editor: {type: "text"}}
	    		,{key: "SEAL_TYPE",		width: 150, label: "Seal Type", 	align: "center", editor: {type: "text"}}
	    		,{key: "SEAL_STD_GB",	width: 150, label: "Seal 표준구분(DURA,BWIP...)", 	align: "center", editor: {type: "text"}}
	    		,{key: "SEAL_GB_TYPE",	width: 150, label: "Seal 구분 Type", 	align: "center", editor: {type: "text"}}
	    		,{key: "RS_GB",			width: 150, label: "Rotation/Stationary 구분", 	align: "center", editor: {type: "text"}}
	    		,{key: "PV_CURVE",		width: 150, label: "PV Curve",	align: "center", editor: {type: "text"}}
	    		,{key: "SHAFT_SIZE_MIN",	width: 150, label: "Shaft Size Min (MM)",	align: "center", editor: {type: "text"}}
	    		,{key: "SHAFT_SIZE_MAX",	width: 150, label: "Shaft Size Max (MM)",	align: "center", editor: {type: "text"}}
	    		,{key: "SEAL_SIZE_MIN",		width: 150, label: "Seal Size Min (MM)", 	align: "center", editor: {type: "text"}}
	    		,{key: "SEAL_SIZE_MAX",		width: 150, label: "Seal Size Max (MM)", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_M1",	width: 150, label: "Material 1", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_M2",	width: 150, label: "Material 2", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_M3",	width: 150, label: "Material 3", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_M4",	width: 150, label: "Material 4", 	align: "center", editor: {type: "text"}}
	    		,{key: "PRESS_OUT_D",	width: 150, label: "Press Outer Dynamic (BARG)", 	align: "center", editor: {type: "text"}}
	    		,{key: "SHAFT_SPEED_1_MIN",	width: 150, label: "Shaft Speed Min RPM", 	align: "center", editor: {type: "text"}}
	    		,{key: "SHAFT_SPEED_2_MIN",	width: 150, label: "Shaft Speed Min Line Speed (ft/s)", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "SHAFT_SPEED_1_MAX",	width: 150, label: "Shaft Speed Max RPM", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "SHAFT_SPEED_2_MAX",	width: 150, label: "Shaft Speed Max Line Speed (ft/s)", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "VISCOSITY_MIN",		width: 150, label: "Viscosity Min", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "VISCOSITY_MAX",		width: 150, label: "Viscosity Max", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "VISCOSITY_MIN_UNIT",width: 150, label: "Viscosity Min Unit", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "VISCOSITY_MAX_UNIT",width: 150, label: "Viscosity Max Unit", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "TEMP_MIN",		width: 150, label: "Temperature Min (C)", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "TEMP_MAX",		width: 150, label: "Temperature Max ⓒ", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "SG_MIN",		width: 150, label: "S.G. Min", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "SG_MAX",		width: 150, label: "S.G. Max", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "VAP_PRES_MARGIN_MIN",		width: 150, label: "Vapor Press Margin Min (Barg)", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "VAP_PRES_MARGIN_MAX",		width: 150, label: "Vapor Press Margin Max (Barg)", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "SOLID",			width: 150, label: "Solid (%)", 	align: "center",	 editor: {type: "text"}}
	    		,{key: "RMKS",			width: 150, label: "비고", 	align: "center",	 editor: {type: "text"}}
	    	]
		});
		return this;
	},
	setData: function (data) {
		var list = [];
		for (var i = 0, l = data.getSowList.length; i < l; i++) {
	    	list.push(data.getSowList[i]);
	    }
		this.target.setData({
			list: list
			,page: {}
	    });
	    return this;
	}
};

var stGridView = {
	initView: function(){
		this.target = new ax5.ui.grid();
		this.target.setConfig({
			target : $('[data-ax5grid="third-grid"]'),
			frozenRowIndex: 0,
			showLineNumber: true,
			showRowSelector: false,
			multipleSelect: true,
			lineNumberColumnWidth: 40,
			rowSelectorColumnWidth: 28,
			sortable: true, // 모든 컬럼에 정렬 아이콘 표시
			multiSort: false, // 다중 정렬 여부
			remoteSort: false,
			header: {
	        	align: "center",
	            columnHeight: 28
	        },
	        body: {
	        	align: "center",
	            columnHeight: 28
			},
	    	columns: [
	    		{key: "R_IDX",			width: 150, label: "인덱스", 	align: "center",   editor: {type: "text"}}
	    		,{key: "R_NO",			width: 150, label: "No.", 	align: "center", editor: {type: "text"}}
	    		,{key: "SEAL_TYPE",		width: 150, label: "Seal Type(Seal Model)", 	align: "center", editor: {type: "text"}}
	    		,{key: "SEAL_GB",		width: 150, label: "구분", 	align: "center", editor: {type: "text"}}
	    		,{key: "CONFIG",		width: 150, label: "Configuration", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_IN_M1",	width: 150, label: "Material-In-1", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_IN_M2",	width: 150, label: "Material-In-2",	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_IN_M3",	width: 150, label: "Material-In-3",	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_IN_M4",	width: 150, label: "Material-In-4",	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_OUT_M1",	width: 150, label: "Material-out-1", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_OUT_M2",	width: 150, label: "Material-out-2", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_OUT_M3",	width: 150, label: "Material-out-3", 	align: "center", editor: {type: "text"}}
	    		,{key: "MTRL_CD_OUT_M4",	width: 150, label: "Material-out-4", 	align: "center", editor: {type: "text"}}
	    		,{key: "S_MTRL",	width: 150, label: "Small Part Material", 	align: "center", editor: {type: "text"}}
	    		,{key: "S_MTRL_YN",	width: 150, label: "Small Part Y/N", 	align: "center", editor: {type: "text"}}
	    		,{key: "GS_MTRL",	width: 150, label: "Grand & Sleeve Material For Bellows", 	align: "center", editor: {type: "text"}}
	    		,{key: "DB_RMKS",	width: 150, label: "Database", 	align: "center", editor: {type: "text"}}
	    		,{key: "RMKS",		width: 150, label: "비고", 	align: "center",	 editor: {type: "text"}}
	    	]
		});
		return this;
	},
	setData: function (data) {
		var list = [];
		for (var i = 0, l = data.getStList.length; i < l; i++) {
	    	list.push(data.getStList[i]);
	    }
		this.target.setData({
			list: list
			,page: {}
	    });
	    return this;
	}
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
							<div data-ax5grid="first-grid" data-ax5grid-config="{}" style="width:100%; height:600px;"></div>
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
							<div data-ax5grid="second-grid" data-ax5grid-config="{}" style="width:100%; height:600px;"></div>
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
							<div data-ax5grid="third-grid" data-ax5grid-config="{}" style="width:100%; height:600px;"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
</body>