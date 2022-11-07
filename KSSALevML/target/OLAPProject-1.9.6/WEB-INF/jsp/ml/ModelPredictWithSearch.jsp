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
	
	<%-- 데이터조회화면 조회데이터 --%>
	var searchList = parent.$("#objectViewGrid").jsGrid("option", "data");
	
	<%-- 모델예측  --%>
	$("#btn_predict_model").click(function(){
			var confirmMsg = "조회된 데이터를 기반으로 예측을 진행합니다";
			
			<%-- 조회된 데이터가 없는 경우 - training data가 없는경우 --%>
			if(parent.$("#objectViewGrid").jsGrid("option", "data").length  <= 0){
				alert("조회된 데이터가 없습니다.");
				return;
			}
			
			<%-- Product Feature가 체크된경우 --%>
			if($("#productApplychecked").is(":checked")){
				confirmMsg = confirmMsg + "<br/><br/><font color=red>(Product</font> 항목을 적용하면 처리시간이 많이 소요됩니다)";
			}
			
			confirm({
				message:confirmMsg,
				title:'Predict with Retrieved Data',
				buttons :{
						  confirm: {label: '확인',className: 'btn btn-sm btn-primary'},
						  cancel: {label: '취소',className: 'btn btn-sm  btn-secondary'}
				 },
			},function(result){
				if(result){
					var loadingMsg = new loading_bar({message:"Predicting..."}); // 모델생성 bar
					$.ajax({
						type:"POST",
						url:"<c:url value='/ml/modelPredictWithSearch.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify({
							search_list:searchList, <%-- 데이터조회화면 조회데이터 --%>
							<%--MODEL_ID:$("#jsGrid").jsGrid("option", "data")[0].MODEL_ID,--%>
							PRODUCT:$("#f_product").val(),
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
							seal_config_check:$("#sealConfigchecked").is(":checked"),
							product_apply_check:$("#productApplychecked").is(":checked")  <%-- product 인자 Feature 적용여부 --%>
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
					}).fail(function(jqXHR, textStatus, errorThrown){
						loadingMsg.modal('hide');	
						ajaxFailMsg(jqXHR);
					}); // AJAX
				}
			});

		});//end $("#btn_predict_model").click(function()
				

	$("#sealAllchecked").click(function(){
		$("#predict_title_seal_all").toggle();
		//$("#jsGrid_SEAL_ALL").jsGrid('destroy');
	});
	
	$("#sealTypechecked").click(function(){
		$("#predict_title_seal_type").toggle();
		//$("#jsGrid_SEAL_TYPE").jsGrid('destroy');
	});
	
	$("#sealSizechecked").click(function(){
		$("#predict_title_seal_size").toggle();
		//$("#jsGrid_SEAL_SIZE").jsGrid('destroy');
	});
	
	$("#sealConfigchecked").click(function(){
		$("#predict_title_seal_config").toggle();
		//$("#jsGrid_SEAL_CONFIG").jsGrid('destroy');
	});
	
	$("#productApplychecked").change(function(){
		if($(this).is(":checked")){
			$("#f_product").attr("disabled", false);
		}else{
			$("#f_product").attr("disabled", true);
			$("#f_product").val("");
		}
	});
	
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
	
	$("#search_cnt_title").html(  "( Retrieved Data : "+parent.$("#objectViewGrid").jsGrid("option", "data").length + "건 )"   );
	//parent.$("#objectViewGrid")
	
	
	//Product Feature Set
	var arr = new Array();
	for(var i=0; i<searchList.length; i++){
		if(!isContains(arr,searchList[i].FPRODUCT)){
			arr.push(searchList[i].FPRODUCT);
		}
	}
	arr.sort();//정렬
	for(var i=0; i<arr.length;i++){
		$("#f_product").append("<option value='"+arr[i]+"'>"+arr[i]+"</option>");
	}
	
    
}); <%-- end  $(document).ready(function(){ --%>





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


</script>

<style>
tr.highlight td.jsgrid-cell {background-color: #BBDEFB;}
.jsgrid-header-row {text-align: center;}
.red td {color: #f08080 !important;}
.jsgrid-delete-button-custom {background-position: 0 -80px;width: 16px;height: 16px;;opacity: .2;}
.jsgrid-edit-button-custom {background-position: 0 -120px;width: 16px;height: 16px;;opacity: .2;}
.font-size-down{font-size:12px;}
.jsgrid-cell {word-wrap: break-word;}

#jsGrid_SEAL_ALL .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_TYPE .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_SIZE .jsgrid-pager { font-size:12px;}
#jsGrid_SEAL_CONFIG .jsgrid-pager { font-size:12px;}

.modal-backdrop.show {opacity: 0.6;}

</style>
</head>
<body>
	<!-- ================  Contents ================  -->
	<div class=" col-lg-12 col-xl-12 col-12  px-4 pl-1">
		<div class="row">
			<!-- style="display: none;" -->
			<div id="div1" style="display: none;"></div>
			<div id="div2" style="display: none;"></div>
			<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
			<div class="col-12">
				<div class="row">
					<div class="col-6" style="width:100%; flex:0 0 100%;max-width:100%;">
						<div class="h5" style="float:left;width:20%;">
							<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">Model 예측</span></strong> 
						</div>
						<div class="h6 text-left mt-1" style="float:left;width:30%;">
							<span class="ml-2" id="search_cnt_title"></span> 
						</div>
						<!--  Button -->
						<div class="col-12 mt-3 text-right" style="float:left;margin-top:0px !important;width:50%">
							<button type="button" class="btn btn-outline-primary"  id="btn_predict_model">예측 <i class="fa fa-caret-square-right"></i></button>
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
			
			<div class="container-fluid">
				<div class="row">
					<div class="top-search-div collapse show mt-3" style="width:100%">
						<div class="col-12">
							<div class="row">
							
								<!--  feature -->
								<div class="col-lg-3  col-xl-3 mb-2" style="min-width:310px;">
														
									<div class="card-title" style="min-width:310px;">
										<div class="row">
											<div class="col">
												<p class="h5">
													<i class="fas fa-edit"></i> <strong class="text-success">Feature</strong>
												</p>
											</div>
										</div>
										
										<div class="card custom-search-card-div pb-2">
										<!-- feature  -->
										<div class="row">
											<div class="col-sm-4">
												<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
													<strong>Product</strong>
												</div>
											</div>
											<div class="col-sm-6">
												<div class="pt-2 custom-responsive-p2 pr-1">
													<%--  <input id="f_product" type="text" class="form-control form-control-sm"
														placeholder="Product" maxlength="20"  style="height:25px;width:100%;" disabled> --%>
													<select id="f_product" class="form-control input-small p-0 pl-1"  style="height:25px;font-size:12px;" disabled>
														<option value=''>-</option>
													</select>	
												</div>
											</div>
											<div class="col-sm-2">
												<div class="custom-control custom-checkbox " style="width:80px;margin-left:-30px !important;margin-top:10px;background:url();">
												     <input type="checkbox" class="custom-control-input" id="productApplychecked" >
												    <label class="custom-control-label" for="productApplychecked" style="margin-top:-3px;"> 적용</label>
												</div>
											</div>
										</div>
										<!-- Temperature -->
										<div class="row">
											<div class="col-sm-4" >
												<div class="p-2 custom-responsive-p2"  style="min-width:120px;" >
													<strong>Temperature</strong>
												</div>
											</div>
											<div class="col-sm-6" >	
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
											<div class="col-sm-2" style="margin-left:-40px;">
												<div class="pt-2 custom-responsive-p2 pl-1">
													<div>
														<select id="f_temp_nor_sel" class="form-control input-small p-0 pl-1"  style="width:55px;height:25px;font-size:12px;">
															<option value=''>-----</option>
														</select>
													</div>
													<div>
														<select id="f_temp_min_sel"  class="form-control input-small p-0 pl-1"  style="width:55px;height:25px;font-size:12px;">
															<option value=''>-----</option>
														</select>
													</div>
													<div>
														<select id="f_temp_max_sel"  class="form-control input-small p-0 pl-1"  style="width:55px;height:25px;font-size:12px;">
															<option value=''>-----</option>
														</select>
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
												<div class="p-2 custom-responsive-p2 "    style="min-width:120px;" >
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
											<div class="col-sm-4" style="padding-right:0px;">
												<div class="p-2 custom-responsive-p2 "   style="min-width:120px;" >
													<strong>Seal Chamber<br/> Pressure</strong>
												</div>
											</div>
											<div class="col-sm-6"> 
												<div class="pt-2 custom-responsive-p2">
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
											<div class="col-sm-2" style="margin-left:-40px;">
												<div class="pt-2 custom-responsive-p2">
													<div>
														<select id="f_seal_cham_nor_sel" class="form-control input-small p-0 pl-1"  style="width:55px;height:25px;font-size:12px;">
															<option>-----</option>
														</select>
													</div>
													<div>
														<select id="f_seal_cham_min_sel" class="form-control input-small p-0 pl-1"  style="width:55px;height:25px;font-size:12px;">
															<option value=''>-----</option>
														</select>
													</div>
													<div>
														<select id="f_seal_cham_max_sel" class="form-control input-small p-0 pl-1"  style="width:55px;height:25px;font-size:12px;">
															<option value=''>-----</option>
														</select>
													</div>
												</div>
											</div>
										</div>		
										
									</div>
							
								</div>
							</div><!--  end feature -->
						
								<!--  예측 영역 -->
								<div class="col-lg-9  col-xl-9" >
								
									<div class="card-title">
										<div class="row">
										
											<div class="col-lg-2">
												<p class="h5">
													<i class="fas fa-search"></i> <strong class="text-primary">Predict</strong>
												</p>
											</div>
											
											<%-- predict checkbox --%>
											<div class="col-lg-10 mt-1">
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
											<div class="col-lg-12 card custom-search-card-div "  style="min-height:385px;">
												<div class="row">
											
													<div class="col-lg-4 "  id="predict_title_seal_type"  style="padding-left:0px;">
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
													<div class="col-lg-4" id="predict_title_seal_size" style="padding-left:0px;">		
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
													<div class="col-lg-4" id="predict_title_seal_config" style="padding-left:0px;">		
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
													<div class="col-lg-12" style="padding-left:0px;">
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
										
							</div> <!--  end row -->
								
						</div>
					</div>
						
				</div>
			</div>
		</div>
		
	</div>

	

</body>
</html>