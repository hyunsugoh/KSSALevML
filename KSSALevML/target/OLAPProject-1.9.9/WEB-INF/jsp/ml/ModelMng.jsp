<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link rel="shortcut icon" href="<c:url value='/images/common/ci/ksm_favi.ico'/>">

<link rel="stylesheet" href="<c:url value='/css/admin/admin-common.css'/>">
<script src="<c:url value='/js/user/dashboardDatepickerModule.js'/>"></script>

<meta charset="utf-8">

<title></title>
<script>
$(document).ready(function(){
	
	<%-- 일자 검색조건 --%>
	var datePicker_start_id = "datepicker_start", datePicker_end_id = "datepicker_end";
    var unit = null;
    var num = null;
	DatepickerModule.create(datePicker_start_id);
	DatepickerModule.create(datePicker_end_id);
	DatepickerModule.init(datePicker_start_id,unit);
	DatepickerModule.init(datePicker_end_id);
	DatepickerModule.evtBind(datePicker_start_id,datePicker_end_id,unit,num);
	// date picker
	$("#datepickerStartBtn").off("click").bind('click',function(e){
		e.stopPropagation();
		DatepickerModule.startShow();
	});
	$("#datepickerEndBtn").on("click",function(e){
		e.stopPropagation();
		DatepickerModule.endShow();
	});
	
	
	<%-- 모델조회  --%>
	doSelect();
	
	var isRun = false;
	
	<%-- 모델생성  --%>
	$("#btn_create_model").click(function(e){
		
		//$("#productApplyChecked").prop("checked", false);
		$('#modalModelCreate').modal("show");
		
		$("#modalModelCreate_confirm_ok").click(function(){
			if(isRun) return;
			isRun = true;
			var loadingMsg = new loading_bar({message:"Model Creating..."}); // 모델생성 bar
			$.ajax({
				type:"POST",
				url:"<c:url value='/ml/modelCreate.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					//product_apply_check : $("#productApplyChecked").is(":checked")
				}),
				beforeSend: function(xhr) {
			       xhr.setRequestHeader("AJAX", true);
			       loadingMsg.show(); // 진행다이얼로그 호출
			     },
			     complete: function () {
			     	//loadingMsg.modal('hide');
			     	setTimeout(function() {
			     		loadingMsg.modal('hide');
			     	},500);
			     }
			}).done(function(data){
				//loadingMsg.modal('hide');	
				var doneMsg = "모델이 생성되었습니다.<br/><br/>Correct Rate<br/>";
				if (data.SEAL_ALL != undefined) doneMsg += "ALL : " + data.SEAL_ALL.eval.correct_rate +"<br/>";
				if (data.SEAL_TYPE != undefined) doneMsg += "Seal Type : " + data.SEAL_TYPE.eval.correct_rate + "<br/>";
				if (data.API_PLAN != undefined) doneMsg += "API Plan : " + data.API_PLAN.eval.correct_rate + "<br/>";
				if (data.CONN_COL != undefined) doneMsg += " Seal Type+Material+API Plan: " + data.CONN_COL.eval.correct_rate + "";
				alert(doneMsg);
				doSelect(); //재조회
				isRun = false;
			}).fail(function(jqXHR, textStatus, errorThrown){
				//loadingMsg.modal('hide');	
				//ajaxFailMsg(jqXHR);
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
				isRun = false;
			}); // AJAX
		});
	});
    	
	$("#btn_search_model").click(function(){
		doSelect();
	});
	
	$("#btn_save_model_info").click(function(){
		//var loadingMsg = new loading_bar({message:"Model Info Saving..."}); 
		$.ajax({
			type:"POST",
			url:"<c:url value='/ml/modelInfoSave.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			data: JSON.stringify({
				"MODEL_ID" : $("#modelInfoPop_model_id").val(),
				"TITLE" : 	$("#modelInfoPop_title").val()
			}),
			beforeSend: function(xhr) {
		       xhr.setRequestHeader("AJAX", true);
		       //loadingMsg.show(); // 진행다이얼로그 호출
		     },
		     complete: function () {
		     }
		}).done(function(data){
			//loadingMsg.modal('hide');
			if(data.result =="ok"){
				$('#mlModalInfo').modal("hide");	
				alert("저장되었습니다");
				doSelect(); //재조회
			}else{
				alert("처리중 오류가 발생하였습니다.");
			}
		}).fail(function(jqXHR, textStatus, errorThrown){
			//loadingMsg.modal('hide');	
			//ajaxFailMsg(jqXHR);
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
		}); // AJAX
	});
	
	//데이텨 변환
	$("#btn_data_conversion").click(function(e){
		var loadingMsg = new loading_bar({message:"Data Conversion 진행중..."}); // 모델생성 bar
		$.ajax({
			type:"POST",
			//<c:url value='/ext/orgToCnv.do'/>"
			url:"<c:url value='/ext/orgToCnv.do'/>", //KSM용
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			beforeSend: function(xhr) {
			       xhr.setRequestHeader("AJAX", true);
			       loadingMsg.show(); // 진행다이얼로그 호출
			     },
		
		    complete: function () {
		     	//loadingMsg.modal('hide');
		     	setTimeout(function() {
		     		loadingMsg.modal('hide');
		     	},500);
		     }
			 
		}).done(function(data){
			if(data ==="ok"){
				alert("Data Conversion이 완료되었습니다.");	
			}
		}).fail(function(jqXHR, textStatus, errorThrown){
			//loadingMsg.modal('hide');	
			//ajaxFailMsg(jqXHR);
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
		})
	});
		
	
});


//조회
function doSelect(){
	
	$.ajax({
		type:"POST",
		url:"<c:url value='/ml/modelList.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({
			DATE_FR : convertDateFormat($("#datepicker_start").datepicker("getDate")),
			DATE_TO : convertDateFormat($("#datepicker_end").datepicker("getDate"))
		}),
		beforeSend: function(xhr) {
	       xhr.setRequestHeader("AJAX", true);
	     }
	}).done(function(data){
		userGrid(data);
	}).fail(function(jqXHR, textStatus, errorThrown){
		//ajaxFail(jqXHR);
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
	}); // AJAX
}



//그리드 Data Load
function userGrid(data){
	$("#jsGrid").jsGrid('destroy');
	$("#jsGrid").jsGrid({
    	width: "100%",
        height: ($(document).height()-200)+"px",
        editing: true, //수정 기본처리
        sorting: true, //정렬
        paging: true, //조회행넘어가면 페이지버튼 뜸
        loadMessage : "Now Loading...",
        data: data, 
        fields: [
        	{name : "TITLE",title : "Title",align : "left",width : 150, css:"font-size-down" },
        	{name : "MODEL_ID",title : "모델ID",align : "center",width : 100, css:"font-size-down"},
        	{name : "BUILD_TYPE",title : "Build<br/>Type",align : "center",width : 60, css:"font-size-down"},
			{name : "CORRECT_RATE_SEAL_TYPE",title : "Correctly rate<br/>[Seal Type]",type : "number",width : 100,align : "center",css:"font-size-down"},
			{name : "TRAINING_CNT_SEAL_TYPE",title : "대상건수",type : "number",width : 70,align : "center",css:"font-size-down"},
			
			{name : "CORRECT_RATE_API_PLAN",title : "Correctly rate<br/>[API Plan]",type : "number",width : 100,align : "center",css:"font-size-down"},
			{name : "TRAINING_CNT_API_PLAN",title : "대상건수",type : "number",width : 70,align : "center",css:"font-size-down"},
			
			{name : "CORRECT_RATE_CONN_COL",title : "Correctly rate<br/>[Seal Type+Material+API Plan]",type : "number",width : 150,align : "center",css:"font-size-down"},
			{name : "TRAINING_CNT_CONN_COL",title : "대상건수",type : "number",width : 70,align : "center",css:"font-size-down"},
			
			{name : "CREATE_DT",title : "생성일",align : "center",type : "text",width : 100, css:"font-size-down"},
			{name : "CREATE_USER",title : "생성자",align : "center",type : "text",width : 100, css:"font-size-down"}
        ],
        rowClick: function(args) {
        	$('#mlModalInfo').modal("show");
        	$("#modelInfoPop_model_id").val(args.item.MODEL_ID);
        	$("#modelInfoPop_title").val(args.item.TITLE);
        },
        rowDoubleClick : function(args) {
        	//
        },
    	onPageChanged: function() {
    		//페이지 변경시
    		var gridData = $("#jsGrid").jsGrid("option", "data");
    	}
    });
}

function convertDateFormat(dateObj){
	dateObj = new Date(dateObj);
	var _year = dateObj.getFullYear(),
		_month = (dateObj.getMonth()+1) < 10  ? "0"+(dateObj.getMonth()+1) : ""+(dateObj.getMonth()+1),
		_date = dateObj.getDate() < 10 ? "0"+dateObj.getDate() : ""+dateObj.getDate();
	return ""+_year+_month+_date;
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
.font-size-down{

font-size:12px;
}

.jsgrid-cell { 
    word-wrap: break-word; 
}

.bootbox-input-checkbox{
	width:20px; height:20px;
}

<%--
.modal {
        text-align: center;
}
 
@media screen and (min-width: 768px) { 
        .modal:before {
                display: inline-block;
                vertical-align: middle;
                content: " ";
                height: 100%;
        }
}
 
.modal-dialog {
        display: inline-block;
        text-align: left;
        vertical-align: middle;
}
--%>
</style>
</head>
<body>
		
		<!-- ================  Contents ================  -->
		<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto mt-2">
			<div class="row">
				<!-- style="display: none;" -->
				<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />

				<div class="col-12" >
				
					<div class="row">
						<div class="col-12" >
							<div class="h5" style="float:left;width:50%;">
								<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">Model 관리</span></strong>
							</div>
							
							<!--  Button -->
							<div class="col-12 mt-3 text-right" style="float:left;margin-top:0px !important;width:50%">
								<button type="button" class="btn btn-outline-success"  id="btn_search_model">조회 <i class="fa fa-search"></i></button>
								<button type="button" class="btn btn-outline-primary"  id="btn_create_model">모델 생성 <i class="fa fa-plus"></i></button>
								<button type="button" class="btn btn-outline-success"  id="btn_data_conversion">데이터 변환 <i class="fa fa-plus"></i></button>
							</div>
				
						</div>
						
						<div class="col-6">
							<div class="d-flex justify-content-end">
								<div id="helpIcon" class="pt-0"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			<!--  Search Conds. -->
			<div class="card m-1 p-1" > 
				<div class="row" >
					<div class="col-lg-1 text-center"  style="min-width:120px;">
						<span class="font-weight-bold">생성일</span>
					</div>
					<div class="col-lg-2  text-center " >
						<div class="input-group input-group-sm"  style="min-width: 150px;">
							<input id="datepicker_start" readonly
								class="form-control form-control-sm " type="text"
								data-toggle="datepicker" > <span
								class="input-group-append">
								<button id="datepickerStartBtn"
									class="btn btn-sm btn-outline-secondary border-left-0 custom-datepicker-icon">
									<i class="fa fa-calendar"></i>
								</button>
							</span>
						</div>
					</div>
					<div class="col-lg-1  text-left"  style="max-width:10px;">
						<span class="font-weight-bold">~</span>
					</div>
					<div class="col-lg-2  text-left" >
						<div class="input-group input-group-sm"  style="min-width: 150px;">
							<input id="datepicker_end" readonly
								class="form-control form-control-sm" type="text"
								data-toggle="datepicker" data-placement="bottom">
							<span class="input-group-append">
								<button id="datepickerEndBtn"
									class="btn btn-sm btn-outline-secondary border-left-0 custom-datepicker-icon">
									<i class="fa fa-calendar"></i>
								</button>
							</span>
						</div>
						
					</div>
					<div class="col-lg-7 " >
					</div>
				</div>
			</div>
						
			<!--  Grid  -->				
			<div class="row">										
				<div class="col-12 pt-1">
					<div id="jsGrid"></div>
				</div>
			
			</div>

		</div>



<!--Modal: Create Pop-->
<div class="modal fade" id="modalModelCreate" tabindex="-1" role="dialog"  aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg" role="document"  style="max-width:350px;">
    <!--Content-->
    <div class="modal-content">
      <!--Body-->
      <div class="modal-body mb-0 p-1">
        <!--Google map-->
        <div id="map-container-google-2" class="z-depth-1-half map-container" style="height: 100px">
          	
			<p class="h6 pt-3 pb-4 text-center">
				<strong >전체 데이터를 기준으로 Model을 생성합니다</strong>
			</p>
											
          	<!-- div class="custom-control custom-checkbox"  style="width:200px;padding-left:50px;background:url()">
			     <input type="checkbox" class="custom-control-input" id="productApplyChecked"  >
			    <label class="custom-control-label" for="productApplyChecked" style="margin-top:-3px;"> Product 적용 </label>
			</div -->
													
        </div>
      </div>
      <!--Footer-->
      <div class="modal-footer justify-content-center">
      	<button type="button" class="btn btn-outline-primary btn-md" data-dismiss="modal"  id="modalModelCreate_confirm_ok">생성 <i class="fas fa-plus ml-1"></i></button>
        <button type="button" class="btn btn-outline-secondary btn-md" data-dismiss="modal" >취소 <i class="fas fa-times ml-1"></i></button>
      </div>
    </div>
    <!--/.Content-->
  </div>
</div>
<!--Modal: Create Pop-->


<!--  pop up  -->
<!-- ML 모델 정보 팝업 -->
<div class="modal" id="mlModalInfo">
	<input type="hidden" id="modelInfoPop_model_id" />
	<div class="modal-dialog modal-sm" >
		<div class="modal-content" style="height: 300px;width:400px;">

			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title">Model Info</div>
	              	<div style="font-size:24px;float:left;">&nbsp;&nbsp;&nbsp;</div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			<!-- Modal body -->
			<div class="modal-body">
				<div class="row">
					<div class="col-12 mt-3">
						<div class="card">
							<div class="row">
								<div class="col-3">
									<div class="d-flex flex-column flex-md-row">
										<div class="p-2 custom-responsive-p2 ">
											<strong>Title</strong>
										</div>
									</div>
								</div>		
								<div class="col-9  input-group-sm">
									<div class="d-flex flex-column flex-md-row">
										<div class="pt-2 custom-responsive-p2">
											<input id="modelInfoPop_title" type="text" class="form-control form-control-sm"
												placeholder="Title..." maxlength="100"  style="height:25px;width:250px;" >
										</div>
									</div>
								</div>
								
							</div>
	
						</div>
						</div>		
					</div>	
					
					<div class="row">
						<div class="col-12" >
							<!--  Button -->
							<div class="col-12 mt-3 text-right" >
								<button type="button" class="btn btn-outline-success"  id="btn_save_model_info">저장 <i class="fa fa-save"></i></button>
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