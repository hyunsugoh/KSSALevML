<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link rel="shortcut icon" href="<c:url value='/images/common/ci/ksm_favi.ico'/>">

<link rel="stylesheet" href="<c:url value='/css/common/bootstrap/bootstrap.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/awesome/css/all.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/datepicker-master/datepicker.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jsgrid/jsgrid.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jsgrid/jsgrid-theme.min.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/olap-common.css'/>">
<link rel="stylesheet" href="<c:url value='/css/admin/admin-common.css'/>">


<script src="<c:url value='/js/common/jquery/jquery-3.3.1.min.js'/>"></script>
<script src="<c:url value='/js/common/bootstrap/bootstrap.bundle.js'/>"></script>
<script src="<c:url value='/js/common/jsgrid/jsgrid.min.js'/>"></script>
<script src="<c:url value='/js/common/bootbox/bootbox.all.min.js'/>"></script>
<script src="<c:url value='/js/common/datepicker-master/datepicker.min.js'/>"></script>
<script	src="<c:url value='/js/common/datepicker-master/i18n/datepicker.ko-KR.js'/>"></script>

<script src="<c:url value='/js/common/olap-common.js'/>"></script>
<script src="<c:url value='/js/common/olap-baseConfig.js'/>"></script>
<script src="<c:url value='/js/admin/admin-menu.js'/>"></script>
<script src="<c:url value='/js/common/olapHelpMessage/messages/adminMsg.js'/>"></script>
<script src="<c:url value='/js/common/olapHelpMessage/OLAPHelpMessage.js'/>"></script>
<script src="<c:url value='/js/user/dashboardDatepickerModule.js'/>"></script>

<meta charset="utf-8">

<title></title>

<script>
$(document).ready(function(){
	
	<%-- 모델조회  --%>
	doSelect();
	
	<%-- 모델예측  --%>
	$("#btn_predict_model").click(function(){
			var loadingMsg = new loading_bar({message:"Predicting..."}); // 모델생성 bar
			$.ajax({
				type:"POST",
				url:"<c:url value='/ml/modelPredict.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					MODEL_ID:$("#jsGrid").jsGrid("option", "data")[0].MODEL_ID,
					PRODUCT:$("#f_product").val()==""?"-":$("#f_product").val(),
					TEMP_NOR: $("#f_temp_nor").val(),
					TEMP_MIN: $("#f_temp_min").val(),
					TEMP_MAX: $("#f_temp_max").val(),
					SPEC_GRAVITY_NOR: $("#f_spec_gravity_nor").val(),
					SPEC_GRAVITY_MIN: $("#f_spec_gravity_min").val(),
					SPEC_GRAVITY_MAX: $("#f_spec_gravity_max").val(),
					VISC_NOR: $("#f_visc_nor").val(),
					VISC_MIN: $("#f_visc_min").val(),
					VISC_MAX: $("#f_visc_max").val(),
					SEAL_CHAM_NOR: $("#f_seal_cham_nor").val(),
					SEAL_CHAM_MIN: $("#f_seal_cham_min").val(),
					SEAL_CHAM_MAX: $("#f_seal_cham_max").val(),
					seal_all_check:$("#sealAllchecked").is(":checked"),
					seal_type_check:$("#sealTypechecked").is(":checked"),
					seal_size_check:$("#sealSizechecked").is(":checked"),
					seal_config_check:$("#sealConfigchecked").is(":checked")
				}),
				beforeSend: function(xhr) {
			       xhr.setRequestHeader("AJAX", true);
			       loadingMsg.show();
			     }
			}).done(function(data){
				loadingMsg.modal('hide');	
				if($("#sealTypechecked").is(":checked")) setPredictData("SEAL_TYPE", data.SEAL_TYPE);
				if($("#sealSizechecked").is(":checked")) setPredictData("SEAL_SIZE", data.SEAL_SIZE);
				if($("#sealConfigchecked").is(":checked")) setPredictData("SEAL_CONFIG", data.SEAL_CONFIG);
				if($("#sealAllchecked").is(":checked")) setPredictData("SEAL_ALL", data.SEAL_ALL);
				setDBbyPredictData(data.DB_BY_PREDICT);
			}).fail(function(jqXHR, textStatus, errorThrown){
				loadingMsg.modal('hide');	
				ajaxFailMsg(jqXHR);
			}); // AJAX

		});//end $("#btn_predict_model").click(function()
				

	<%-- 모델 변경  --%>
	$("#btn_change_model").click(function(){
		$('#mlChgModal').modal("show");
		doSelect_ChgModel();<%-- 변경모델 리스트 --%>
	});
	
	$("#history_toggle").click(function(){
		$("#history_table").toggle(function(){
			if ($(this).is(':visible')) {
	             $("#history_toggle a").html("Hide");
	             $("#jsGrid_DB_LIST").jsGrid("option", "height", 400);
	        } else {
	        	$("#history_toggle a").html("Show");
	        }    
		});
	});
	
	$("#sealAllchecked").click(function(){
		$("#predict_title_seal_all").toggle();
	});
	
	$("#sealTypechecked").click(function(){
		$("#predict_title_seal_type").toggle();
	});
	
	$("#sealSizechecked").click(function(){
		$("#predict_title_seal_size").toggle();
	});
	
	$("#sealConfigchecked").click(function(){
		$("#predict_title_seal_config").toggle();
	});
	
	// select box set
	<c:forEach var="code" items="${TRAN_TXT_VAL}" varStatus="status">
		<c:if test="${code.GRP_CODE=='grp_unit_temp'}">
			var option = $("<option value='${code.VAL}'>"+"${code.VAL_CODE}"+"</option>");
			$("#f_temp_nor_sel, #f_temp_min_sel, #f_temp_max_sel").append(option);
		</c:if>
		<c:if test="${code.GRP_CODE=='grp_unit_pressure'}">
			var option = $("<option value='${code.VAL}'>"+"${code.VAL_CODE}"+"</option>");
			$("#f_seal_cham_nor_sel, #f_seal_cham_min_sel, #f_seal_cham_max_sel").append(option);
		</c:if>
	</c:forEach>	

	$("#f_temp_nor_sel, #f_temp_min_sel, #f_temp_max_sel, #f_seal_cham_nor_sel, #f_seal_cham_min_sel, #f_seal_cham_max_sel"
			).change(function(){
		$("#"+$(this).attr("id").substr(0,$(this).attr("id").length-4) ).val($(this).val());
	});
	
	$("#f_temp_nor, #f_temp_min, #f_temp_max, #f_seal_cham_nor, #f_seal_cham_min, #f_seal_cham_max"
		).keydown(function(){
		 $("#"+ $(this).attr("id")+"_sel" ).find("option:eq(0)").prop("selected", true);
	});
	
    
}); <%-- end  $(document).ready(function(){ --%>



//조회
function doSelect(modelId){
	<%-- 
		IS_RECENT_DATA: 최근데이터1건조회
		MODEL_ID : Model ID로 조회 시
	--%>
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/modelList.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({
			"IS_RECENT_DATA" : "Y",  
			"MODEL_ID" : modelId
		}),
		beforeSend: function(xhr) {
	       xhr.setRequestHeader("AJAX", true);
	     }
	}).done(function(data){
		userGrid(data);
	}).fail(function(jqXHR, textStatus, errorThrown){
		ajaxFail(jqXHR);
	}); // AJAX
}


//조회 - 모델변경 선택 목록 리스트
function doSelect_ChgModel(){
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/modelList.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({}),
		beforeSend: function(xhr) {
	       xhr.setRequestHeader("AJAX", true);
	     }
	}).done(function(data){
		setMlChgData(data);
	}).fail(function(jqXHR, textStatus, errorThrown){
		ajaxFail(jqXHR);
	}); // AJAX
}

//그리드 Data Load
function userGrid(data){
	$("#jsGrid").jsGrid('destroy');
	$("#jsGrid").jsGrid({
    	width: "100%",
        height: "80px",
        editing: false, //수정 기본처리
        sorting: false, //정렬
        paging: false, //조회행넘어가면 페이지버튼 뜸
        loadMessage : "Now Loading...",
        scrollOffset: 0,
        data: data, 
        fields: [
        	{name : "TITLE",title : "Title",align : "left",width : 150, css:"font-size-down" },
        	{name : "MODEL_ID",title : "모델ID",align : "center",width : 100, css:"font-size-down"},
        	{name : "BUILD_TYPE",title : "Build Type",align : "center",width : 60, css:"font-size-down"},
        	{name : "PRODUCT_APPLY",title : "Product Apply",align : "center",width : 60, css:"font-size-down"},
			{name : "CORRECT_RATE_SEAL_TYPE",title : "Correctly rate<br/>[Seal Type]",type : "number",width : 100,align : "center",css:"font-size-down"},
			{name : "CORRECT_RATE_SEAL_SIZE",title : "Correctly rate<br/>[Seal Size]",type : "number",width : 100,align : "center",css:"font-size-down"},
			{name : "CORRECT_RATE_SEAL_CONFIG",title : "Correctly rate<br/>[Seal Config]",type : "number",width : 100,align : "center",css:"font-size-down"},
			{name : "CORRECT_RATE_SEAL_ALL",title : "Correctly rate<br/>[ALL]",type : "number",width : 100,align : "center",css:"font-size-down"},
			{name : "CREATE_DT",title : "생성일",align : "center",type : "text",width : 130, css:"font-size-down"},
			{name : "CREATE_USER",title : "생성자",align : "center",type : "text",width : 100, css:"font-size-down"}
        ],
        onRefreshed: function(args){
        	var vProdutApply = $("#jsGrid").jsGrid("option", "data")[0].PRODUCT_APPLY;
        	if ("Y"==vProdutApply) {
        		$("#f_product").attr("disabled", false);
   				setProductSelect($("#jsGrid").jsGrid("option", "data")[0].MODEL_ID);	// product apply select list reset
        		
        	}else{
        		$("#f_product").attr("disabled", true);
        		$("#f_product").val("");
        	}
        	
        },
    	onPageChanged: function() {
    		//페이지 변경시
    		var gridData = $("#jsGrid").jsGrid("option", "data");
    	}
    });
}

function setPredictData(classType, data){
	var vTitle = "";
	var vHeight = 0;
	if(classType=="SEAL_TYPE") {
		vTitle = "Seal Type"; 
		vHeight = "200px";
	}else if(classType=="SEAL_SIZE") {
		vTitle = "Seal Size";
		vHeight = "200px";
	}else if(classType=="SEAL_CONFIG") {
		vTitle = "Seal Config";
		vHeight = "200px";
	}else if(classType=="SEAL_ALL") {
		vTitle = "Seal Type / Seal Size / Seal Config";
		vHeight = "150px";
	}
	
	$("#jsGrid_"+classType).jsGrid('destroy');
	$("#jsGrid_"+classType).jsGrid({
    	width: "100%",
        height: vHeight,
        editing: true, //수정 기본처리
        sorting: true, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
        loadMessage : "Now Loading...",
        data: data, 
        fields: [
        	{	name : "NO",title : "NO",align : "center",width : 30, css:"font-size-down" },
			{	name : "CLASS",title : vTitle,type : "number",width : (classType=="SEAL_ALL"?300:100),align : "left",css:"font-size-down"},
			{	name : "PROB",title : "Probability(%)",type : "number",width : 80,align : "right",css:"font-size-down", format:"#,##0.00000"},
        ],
        rowClick: function(args) {
        },
    	onPageChanged: function() {
    		//페이지 변경시
    		var gridData = $("#jsGrid_"+classType).jsGrid("option", "data");
    	}
    });
}

function setDBbyPredictData(data){
	$("#jsGrid_DB_LIST").jsGrid('destroy');
	$("#jsGrid_DB_LIST").jsGrid({
    	width: "100%",
        height: "300px",
        editing: false, //수정 기본처리
        sorting: true, //정렬
        paging: false, //조회행넘어가면 페이지버튼 뜸
        loadMessage : "Now Loading...",
        data: data, 
        fields: [
			{	name : "SEAL_TYPE",title : "Seal Type",type : "text",width : 100,align : "center",css:"font-size-down"},
			{	name : "SEAL_SIZE",title : "Seal Size",type : "text",width : 90,align : "center",css:"font-size-down"},
			{	name : "SEAL_CONFIG",title : "Seal Config",type : "text",width : 160,align : "left",css:"font-size-down"},
			
			{	name : "TEMP_NOR",title : "Temp.<br/>Normal ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "TEMP_MIN",title : "Min ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "TEMP_MAX",title : "Max ",type : "number",width : 60,align : "center",css:"font-size-down"},
			
			{	name : "SPEC_GRAVITY_NOR",title : "S.G.<br/>Normal ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "SPEC_GRAVITY_MIN",title : "Min. ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "SPEC_GRAVITY_MAX",title : "Max. ",type : "number",width : 60,align : "center",css:"font-size-down"},
			
			{	name : "VISC_NOR",title : "Visc. <br/>Normal ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "VISC_MIN",title : "Min ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "VISC_MAX",title : "Max ",type : "number",width : 60,align : "center",css:"font-size-down"},
			
			{	name : "SEAL_CHAM_NOR",title : "Seal.Cham.<br/>Normal ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "SEAL_CHAM_MIN",title : "Min ",type : "number",width : 60,align : "center",css:"font-size-down"},
			{	name : "SEAL_CHAM_MAX",title : "Max ",type : "number",width : 60,align : "center",css:"font-size-down"}
        ],
       	onPageChanged: function() {
    		//페이지 변경시
    		var gridData = $("#jsGrid_DB_LIST").jsGrid("option", "data");
    	}
    });
}



function setMlChgData(data){
	$("#jsGrid_mlChg").jsGrid('destroy');
	$("#jsGrid_mlChg").jsGrid({
    	width: "100%",
        height: ($(document).height()-350)+"px",
        editing: true, //수정 기본처리
        sorting: true, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
        loadMessage : "Now Loading...",
        data: data, 
        fields: [
        	{ name : "TITLE",title : "Title",align : "left",width : 150, css:"font-size-down" },
        	{ name : "BUILD_TYPE",title : "Build Type",align : "center",width : 60, css:"font-size-down"},
        	{ name : "PRODUCT_APPLY",title : "Product Apply",align : "center",width : 60, css:"font-size-down"},
        	{ name : "MODEL_ID",title : "모델ID",align : "center",width : 100, css:"font-size-down"},
        	{ name : "CORRECT_RATE_ALL",title : "Correct Rate<br/>Type/Size/Config/All",align : "center",width : 150, css:"font-size-down"},
			{ name : "CREATE_DT",title : "생성일",align : "center",type : "text",width : 130, css:"font-size-down"},
			{ name : "CREATE_USER",title : "생성자",align : "center",type : "text",width : 100, css:"font-size-down"}
        ],
        rowClick: function(args) {
            //var selectData = args.item; //선택한 로우 데이터
            //$('#myModal').modal("show");
            //$("#tableName2").text(selectData.tableName);
            //$("#columnName2").text(selectData.columnName);
            //doSearch();
        },
        rowDoubleClick : function(args) {
        	doSelect(args.item.MODEL_ID);
        	$('#mlChgModal').modal("hide");
        },
    	onPageChanged: function() {
    		//페이지 변경시
    		var gridData = $("#jsGrid_mlChg").jsGrid("option", "data");
    	}
    });
}

function setProductSelect(modelId){
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/modelProductList.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({
			"MODEL_ID" : modelId
		}),
		beforeSend: function(xhr) {
	       xhr.setRequestHeader("AJAX", true);
	     }
	}).done(function(data){
		
		//Product Feature Set
		$("#f_product").empty();
		for(var i=0; i<data.length;i++){
			$("#f_product").append("<option value='"+data[i]+"'>"+data[i]+"</option>");
		}
		$("#f_product").val("-");
		
	}).fail(function(jqXHR, textStatus, errorThrown){
		ajaxFail(jqXHR);
	}); // AJAX
}



</script>

<style>
tr.highlight td.jsgrid-cell {background-color: #BBDEFB;}
.jsgrid-header-row {text-align: center;}
.red td {color: #f08080 !important;}
.jsgrid-delete-button-custom {background-position: 0 -80px;width: 16px;height: 16px;;opacity: .2;}
.jsgrid-edit-button-custom {background-position: 0 -120px;width: 16px;height: 16px;;opacity: .2;}
.font-size-down{font-size:12px;}
.jsgrid-cell {word-wrap: break-word;}

#jsGrid_SEAL_TYPE .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_SIZE .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_CONFIG .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_ALL .jsgrid-pager { font-size:12px;}

</style>
</head>

<body style="overflow-x: hidden;">

	<!-- ================  Contents ================  -->
	<div class="col-md-9 col-lg-11 col-xl-11 col-11 ml-auto mr-auto" style="padding-left:5%;">
		<div class="row">
			<!-- style="display: none;" -->
			<div id="div1" style="display: none;"></div>
			<div id="div2" style="display: none;"></div>
			<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
			<div class="col-12">
				<div class="row">
					<div class="col-6" style="width:100%; flex:0 0 100%;max-width:100%;">
						<div class="h5" style="float:left;width:50%;">
							<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">Model 예측</span></strong>
						</div>
						<!--  Button -->
						<div class="col-12 mt-3 text-right" style="float:left;margin-top:0px !important;width:50%">
							<button type="button" class="btn btn-outline-primary"  id="btn_predict_model">예측 <i class="fa fa-caret-square-right"></i></button>
							<button type="button" class="btn btn-outline-success"   id="btn_change_model">모델변경 <i class="fa fa-edit"></i></button>
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
			<div class="col-12 pt-1" >
				<!--  Model Grid  -->
				<div id="jsGrid"></div>
			</div>
		</div>
		<div class="row">	
			<div class="container-fluid">
				<div class="row">
					<div class="top-search-div collapse show mt-3" style="width:100%;margin-left:5px;">
						<div class="col-12">
						
							<div class="row">
							
								<!--  feature -->
								<div class="col-lg-3  col-xl-3 mb-2" >
														
									<div class="card-title" >
										<div class="row">
											<div class="col-12">
												<p class="h5">
													<i class="fas fa-edit"></i> <strong class="text-success">Feature</strong>
												</p>
											</div>
										</div>
										
										<div class="card custom-search-card-div pb-2">
											<!-- feature  -->
											<div class="row">
													<div class="col-sm-4">
														<div class="p-2 custom-responsive-p2"  style="min-width:100px;" >
															<strong>Product</strong>
														</div>
													</div>
													<div class="col-sm-8">
														<div class="pt-2 custom-responsive-p2 pr-1">
															<!-- input id="f_product" type="text" class="form-control form-control-sm"
																placeholder="Product" maxlength="20"  style="height:25px;width:100%;" disabled -->
															<select id="f_product" class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;" disabled>
																<option value=''>-</option>
															</select>		
														</div>
													</div>
											</div>
											
											<!-- Temperature -->
											<div class="row">
													<div class="col-sm-4" >
														<div class="p-2 custom-responsive-p2"  style="min-width:100px;" >
															<strong>Temperature</strong>
														</div>
													</div>
													<div class="col-sm-5" > 
														<div class="pt-2 custom-responsive-p2"  style="width:130px;">
															<div >
																<input id="f_temp_nor" type="number" class="form-control form-control-sm"
																	placeholder="Normal" maxlength="20"   style="height:25px;width:90px;float:left" ><span>℃</span>
															</div>
															<div>		
																<input id="f_temp_min" type="number" class="form-control form-control-sm"
																	placeholder="Min" maxlength="20"  style="height:25px;width:90px;float:left" ><span>℃</span>
															</div>
															<div>
																<input id="f_temp_max" type="number" class="form-control form-control-sm"
																	placeholder="Max" maxlength="20"  style="height:25px;width:90px;float:left" ><span>℃</span>
															</div>
														</div>
													</div>
													
													<div class="col-sm-3"  style="padding-left:3px;">	
														<div class="pt-2 custom-responsive-p2 " >
															<div style="margin-right:3px;">
																<select id="f_temp_nor_sel" class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;">
																	<option value=''>-----</option>
																</select>
															</div>
															<div style="margin-right:3px;">
																<select id="f_temp_min_sel"  class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;">
																	<option value=''>-----</option>
																</select>
															</div>
															<div style="margin-right:3px;">
																<select id="f_temp_max_sel"  class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;">
																	<option value=''>-----</option>
																</select>
															</div>
														</div>
													</div>
													
											</div>			
											<!-- Specific Gravity -->
											<div class="row">
													<div class="col-sm-4">
														<div class="p-2 custom-responsive-p2"   style="min-width:100px;" >
															<strong>Specific Gravity</strong>
														</div>
													</div>	
													<div class="col-sm-8">
														<div class="pt-2 custom-responsive-p2 pr-1">
															<input id="f_spec_gravity_nor" type="number" class="form-control form-control-sm"
																placeholder="Normal" maxlength="20"  style="height:25px;width:100%;" >
															<input id="f_spec_gravity_min" type="number" class="form-control form-control-sm"
																placeholder="Min" maxlength="20"  style="height:25px;width:100%;" >
															<input id="f_spec_gravity_max" type="number" class="form-control form-control-sm"
																placeholder="Max" maxlength="20"  style="height:25px;width:100%;" >		
														</div>
													</div>
											</div>			
											<!-- Viscosity -->
											<div class="row">
													<div class="col-sm-4">
														<div class="p-2 custom-responsive-p2 "    style="min-width:100px;" >
															<strong>Viscosity</strong>
														</div>
													</div>
													<div class="col-sm-8">
														<div class="pt-2 custom-responsive-p2" style="width:130px;">
															<div>
																<input id="f_visc_nor" type="number" class="form-control form-control-sm"
																	placeholder="Normal" maxlength="20"  style="height:25px;width:90px;float:left" ><span> CP</span></div>
															<div>		
															<input id="f_visc_min" type="number" class="form-control form-control-sm"
																placeholder="Min" maxlength="20"  style="height:25px;width:90px;float:left" ><span> CP</span></div>
															<div>	
															<input id="f_visc_max" type="number" class="form-control form-control-sm"
																placeholder="Max" maxlength="20"  style="height:25px;width:90px;float:left" ><span> CP</span></div>		 
														</div>
													</div>
											</div>		
											<!-- Seal Chamber Pressure 씰 챔버 압력-->
											<div class="row">
													<div class="col-sm-4" >
														<div class="p-2 custom-responsive-p2 "   style="min-width:100px;" >
															<strong>Seal Chamber<br/> Pressure</strong>
														</div>
													</div>
													<div class="col-sm-5" >
														<div class="pt-2 custom-responsive-p2"  style="width:130px;">
															<div style="height:25px;">
																<input id="f_seal_cham_nor" type="number" class="form-control form-control-sm"
																	placeholder="Normal" maxlength="20"  style="height:25px;width:90px;float:left" ><span style="font-size:12px">BARG</span></div>
															<div style="height:25px;">		
																<input id="f_seal_cham_min" type="number" class="form-control form-control-sm"
																	placeholder="Min" maxlength="20"  style="height:25px;width:90px;float:left" ><span style="font-size:12px">BARG</span></div>
															<div style="height:25px;">	
																<input id="f_seal_cham_max" type="number" class="form-control form-control-sm"
																	placeholder="Max" maxlength="20"  style="height:25px;width:90px;float:left" ><span style="font-size:12px">BARG</span></div>
														</div>
													</div>
													<div class="col-sm-3"  style="padding-left:3px;">
														<div class="pt-2 custom-responsive-p2">
															<div style="margin-right:3px;">
																<select id="f_seal_cham_nor_sel" class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;">
																	<option value=''>-----</option>
																</select>
															</div>
															<div style="margin-right:3px;">
																<select id="f_seal_cham_min_sel" class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;">
																	<option value=''>-----</option>
																</select>
															</div>
															<div style="margin-right:3px;">
																<select id="f_seal_cham_max_sel" class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;">
																	<option value=''>-----</option>
																</select>
															</div>
														</div>
													</div>
											</div>		
											
										</div>
									</div>
							
								</div>
						
								<!--  Predict -->
								<div class="col-lg-9  col-xl-9" >
								
									<div class="card-title">
										<div class="row">
										
											<div class="col-2">
												<p class="h5">
													<i class="fas fa-search"></i> <strong class="text-primary">Predict</strong>
												</p>
											</div>
											
											<%-- predict checkbox --%>
											<div class="col-10 mt-1">
												<div class="d-flex flex-row pl-2">
													<div class="custom-control custom-checkbox "  style="width:100px;background:url();" >
													    <input type="checkbox" class="custom-control-input" id="sealTypechecked"  checked>
													    <label class="custom-control-label" for="sealTypechecked" style="margin-top:-3px;"> Seal Type</label>
													 </div>   
													 <div class="custom-control custom-checkbox " style="width:100px;background:url();" >
													     <input type="checkbox" class="custom-control-input" id="sealSizechecked" checked>
													    <label class="custom-control-label" for="sealSizechecked" style="margin-top:-3px;"> Seal Size</label>
													 </div>   
													 <div class="custom-control custom-checkbox " style="width:130px;background:url();" >
													     <input type="checkbox" class="custom-control-input" id="sealConfigchecked" checked>
													    <label class="custom-control-label" for="sealConfigchecked" style="margin-top:-3px;"> Seal Config</label>
													</div>
													<div class="custom-control custom-checkbox " style="width:200px;background:url();">
													     <input type="checkbox" class="custom-control-input" id="sealAllchecked" checked>
													    <label class="custom-control-label" for="sealAllchecked" style="margin-top:-3px;"> Seal Type+Size+Config</label>
													</div>
												</div>
											</div>
										</div>
										
										<div class="row">
											<div class="col-12"   style="margin-right:20px;margin-left:15px;">
												<div class="row" style="padding-right:30px">
													<div class="col-12 card"  style="min-height:385px;">
														<div class="row">
															<div class="col-4 "  id="predict_title_seal_type"  style="padding-left:0px;">
																<div class="d-flex flex-column flex-md-row">
																	<div class="custom-responsive-p2">
																		<strong class="pl-3"  >Seal Type</strong>
																		<!--  Grid  -->
																		<div class="col-12 pt-1">
																			<div id="jsGrid_SEAL_TYPE"></div>
																		</div>
																	</div>
																</div>
															</div>
															<div class="col-4" id="predict_title_seal_size"   style="padding-left:0px;">
																<div class="d-flex flex-column flex-md-row">
																	<div class="custom-responsive-p2">
																		<strong class="pl-3" >Seal Size</strong>
																		<!--  Grid  -->
																		<div class="col-12 pt-1">
																			<div id="jsGrid_SEAL_SIZE"></div>
																		</div>
																	</div>
																</div>
															</div>
															<div class="col-4" id="predict_title_seal_config"   style="padding-left:0px;">
																<div class="d-flex flex-column flex-md-row">		
																	<div class="custom-responsive-p2">
																		<strong class="pl-3" >Seal Config</strong>
																		<!--  Grid  -->
																		<div class="col-12 pt-1">
																			<div id="jsGrid_SEAL_CONFIG"></div>
																		</div>
																	</div>
																</div>
															</div>
														</div>
														<div class="row"  id="predict_title_seal_all">
															<div class="col-12" style="padding-left:0px;">
																<div class="d-flex flex-column flex-md-row">
																	<div class="custom-responsive-p2">
																			<strong class="pl-3">Seal Type+Seal Size+Seal Config[Concat]</strong>
																			<!--  Grid  -->
																			<div class="col-12 pt-1">
																				<div id="jsGrid_SEAL_ALL"></div>
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
								</div>
												
							</div>
								
						</div>
					</div>
						
				</div>
			</div>
		</div>
		
		<!--  history table -->							
		<div class="row"  style="height:30px">
			<div class="col-lg-10 mb-0 pb-0 ">
				<p class="h5">
					<i class="fas fa-search"></i> <strong class="text-muted">History(DB)</strong>
				</p>
			</div>
			<div class="col-lg-2 text-right pr-3 mb-0 pb-0 ">
				<p class="btn h6"    id="history_toggle"><a href="#" class="text-info">Show </a></p>
			</div>
		</div>
		<div class="row mb-3" id="history_table" style="display:none;">
			<div class="col-12" style="padding-left:5px;" >
				<div class="custom-responsive-p2">
						<!--  Grid  -->
						<div id="jsGrid_DB_LIST"></div>
				</div>
			</div>
		</div>
			
	</div>

		
<%--   pop up  --%>
<%-- ML 모델 변경 팝업 --%>
<div class="modal" id="mlChgModal">
	<div class="modal-dialog modal-lg" style="width:1000px;">
		<div class="modal-content" style="height: 78%;">
			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title">Model List</div>
	              	<div style="font-size:24px;float:left;">&nbsp;&nbsp;&nbsp;</div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			<!-- Modal body -->
			<div class="modal-body">
				<div class="row">
					<div class="col-12 mt-3">
						<div class="card">
							<div class="row">
								<div class="col-12">
									<div class="form-group">
										<div id="jsGrid_mlChg"></div>
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
<!--  pop up  -->

</body>
</html>