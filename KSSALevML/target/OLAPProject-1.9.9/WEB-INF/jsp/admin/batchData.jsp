<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script src="<c:url value='/js/common/jui/core/jui-core.js'/>"></script>
<script src="<c:url value='/js/common/jui/ui/jui-ui.js'/>"></script>
<script src="<c:url value='/js/common/jui/grid/jui-grid.js'/>"></script>
<script src="<c:url value='/js/admin/batchData.js'/>"></script>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">

<script src="<c:url value='/js/common/jquery/jquery-ui.min.js'/>"></script> 
<link rel="stylesheet" type='text/css' href="<c:url value='/css/common/jquery-ui/jquery-ui.css'/>">

<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">	
	<div class="row">
		<div id="div1" style="display: none;"></div>
		<div id="div2" style="display: none;"></div>
		<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}'
			value='${_csrf.token}' />
		<div class="col-12 pt-3">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="far fa-object-ungroup"></i> 
						<span class="ml-1">배치 프로그램 관리</span></strong><br>
					</div>
				</div>
				<div class="col-12 mt-3">
					<p class="text-muted custom-muted-size"><em>미완료된 작업이 존재할 경우 "미완료 작업초기화" 버튼으로 초기화 후 "수동 시작"버튼으로 배치 작업을 수동 시작할 수 있습니다.</em></p>
				</div> 
				<div class="col-12 d-flex justify-content-between mb-3">
					<div class="mr-auto p-2 bd-highlight" id="batchTrue" style="display: none;">
						배치프로그램 작동중&nbsp;<i class="fas fa-circle" style="color: green;"></i>
					</div>
					<div class="mr-auto p-2 bd-highlight" id="batchFalse" style="display: none;">
						배치프로그램 미작동&nbsp;<i class="fas fa-circle" style="color: red;" ></i>
					</div>
					<div class="p-2 bd-highlight">
					</div>
					<div class="p-2 bd-highlight">
						<button id="incomplateBtn"type="button" class="btn btn-outline-info mr-2" onclick="batchReset()"> 미완료 작업초기화 <i class="fab fa-creative-commons-sa"></i></button>
						<button type="button" class="btn btn-outline-primary" onclick="batchStart()" id="batchStartBtn" > 수동시작 <i class="fab fa-creative-commons-sa"></i></button>
					</div>
				</div>
			</div>
		</div>
		<div class="col-6">
			<div class="h6">&nbsp;&nbsp;
			<strong><i class="fas fa-tasks"></i>
			<span class="ml-1">미완료된 작업</span>
			</strong>
			</div>
				<div id="batchstatus" style="text-align: center;"></div>
				<div id="notComplate_externalPager"></div>
		</div>
		<div class="col-6 ">
			<div class="h6">&nbsp;&nbsp;
			<strong><i class="fas fa-info-circle"></i>
			<span class="ml-1">스케줄 정보</span>
			</strong>&nbsp;&nbsp;
				<label for="startDate">Start Date:</label>
				<input type="text" name="startDate" id="startDate" class="datePicker col-2" />&nbsp;&nbsp;
				<label for="endDate">End Date:</label>
				<input type="text" name="endDate" id="endDate" class="datePicker col-2" />
			</div>
				<div id="batchschedule" style="text-align: center; height: 60px;">
					  <br>
					  배치는 <span id="month"></span> <span id="day"></span> <span id="week"></span> <span id="time"></span> <span id="min"></span> <span id="sec"></span>에 실행됩니다.
				</div>
		</div> 
		<div class="col-12 pt-5">
			<div class="h6">&nbsp;&nbsp;
			<strong><i class="fas fa-list-ul"></i>
			<span class="ml-1">작업이력 단계별 상태_BATCH_STEP_EXECUTION</span>
			</strong>
			<div class="d-flex flex-row-reverse bd-highlight">
			<span class="mr-2">전체건수 :&nbsp; ${cnv1TotalCnt} 건&nbsp;
			</span>
			
			</div>
			
			</div>
			
			<div id="step_jsGrid" style="height: 200px;"></div>
			
			<div class="col-12">
				<div id="step_externalPager" class="p-2"></div>			
			</div>
		</div>
	</div>
	
</div>
	
<script>
$(function() {
	    $('.datePicker').datepicker({ 
	      dateFormat: 'yy-mm-dd', // 날짜표현타입
	      monthNamesShort: ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월'],
	      dayNamesMin: ['일','월','화','수','목','금','토'],
	      weekHeader: 'Wk',
	      changeMonth: true, //월변경가능
	      changeYear: true, //년변경가능
	      showMonthAfterYear: true, //년 뒤에 월 표시 
	      //buttonImageOnly: true, //달력 이미지표시  
	      //buttonImage: '<?php echo _SITE_COMMON_LIB?>/script/images/calendar.gif', // 달력 이미지파일
	      buttonText: '날짜를 선택하세요', // 달력이미지에 마우스오버일경우
	      autoSize: false, //오토리사이즈(body등 상위태그의 설정에 따른다)
	      //showOn: 'both', //엘리먼트와 이미지 동시 사용  
	      showButtonPanel:true, // 캘린더 하단에 버튼 패널을 표시한다(오늘날짜로이동버튼, 닫기버튼). 
	      currentText: '오늘', // 오늘날짜로이동되는 버튼의 텍스트 변경 
	      showAnim: "slideDown", //애니메이션을 적용한다.
	      closeText: '닫기',  // 닫기버튼의 텍스트 변경 
	      // 연도 셀렉트 박스 범위(현재연도의 - + 20연도)
	   //   yearRange: (datepicker_year.getFullYear()-20) + ':' + (datepicker_year.getFullYear()+20), 
	      //firstDay: 0,   // 주의 시작일을 일요일로 하려면 0, 월요일은 1 (기본값 0)
	      //isRTL: false,  // 버튼이미지 좌우 위치
	      cleanText: '지우기'  //추가한 기능 jquery-ui.js 파일에 소스 추가해아한다.
	   }); 
	    //$("#startDate").datepicker({ dateFormat: 'yy/mm/dd',  }).datepicker("setDate", new Date().getDay-7);
	    $( "#startDate" ).datepicker( "setDate", -7); // 기본날짜 -7로 설정.
	    $( "#endDate" ).datepicker( "setDate",'today'); // 기본날짜 -7로 설정.
	
	});
function batchStart(){
	var start , end ;
	var Date = [];
	var startDate = $("#startDate").val().replace(/-/gi, "");
	var endDate = $("#endDate").val().replace(/-/gi, "");
	
	
		batchAction.startAction(startDate,endDate);
 	  /* $.ajax({
				type : "GET",
				url : OlapUrlConfig.batchStart ,
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				beforeSend: function(xhr) {
			        xhr.setRequestHeader("AJAX", true);
			     },
			    data : {
			    	startDate : startDate,
			    	endDate : endDate
			    },
			 	success : function(data) {
			 		//alert(data);
			 		batchAction.ListAction(startDate,endDate);
					alert(data.message);

				}, 		
		 		error : function(jqXHR, textStatus, errorThrown){
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
			        	 $("#batchstatus").text("현재 배치프로그램이 실행되지 않았습니다.");
			        	 errAlert(jqXHR.status, jqXHR.responseText)
			         }else{
			        	 $("#batchstatus").text("현재 배치프로그램이 실행되지 않았습니다.");
						alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
					}
				}
			})   */
		 
}  
function batchReset(){
	
	  $.ajax({
		type :"GET",
		url : OlapUrlConfig.batchReset,
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
	    success : function() {
	 		console.log("배치초기화");
		}
	}) 
}
</script>	