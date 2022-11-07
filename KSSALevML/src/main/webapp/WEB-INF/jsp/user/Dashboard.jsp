<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/olap-collapse-grid.css'/>">
<script src="<c:url value='/js/user/dashboardDatepickerModule.js'/>"></script>
<%-- <script src="<c:url value='/js/user/dashboard.js'/>"></script> --%>
<script src="<c:url value='/js/common/olap-collapse-grid.js'/>"></script>
<script src="<c:url value='/js/user/olapDataView.js'/>"></script>
<script src="<c:url value='/js/user/savedDashboard.js'/>"></script>
<script src="<c:url value='/js/user/dashboardExcelDownload.js'/>"></script>
<script
	src="<c:url value='/js/common/olapHelpMessage/messages/dashboardMsg.js'/>"></script>
<script
	src="<c:url value='/js/common/olapHelpMessage/OLAPHelpMessage.js'/>"></script>
<style>
/* jsgrid 스크롤바 테스트용 */
/* .jsgrid-grid-header, .jsgrid-grid-body { */
/* 	overflow: auto; */
/* } */
</style>
<script>
$(document).ready(function() {
	var grid;
	var datePicker_start_id = "datepicker_start", datePicker_end_id = "datepicker_end";
	var unit = null;
	var num = null;

	/*  조회*/
	/* $("#objectSelectActionBtn").on('click', function(e) {
		fn_action_select_Action('objectViewGrid',ExcelDownloadObject.datasetInit);
	}); */

	// 기본정보 가져오기
	olapDataView.init("objectViewGrid", "externalPager"); // 가장먼저 실행.
	olapDataView.evtHandler({
		condBtn : "conditionAdd",
		condArea : "conditionArea",
		orbyBtn : "orderbyAdd",
		orbyArea : "orderbyArea",
		submitBtn:"dataSubmitBtn",
		clearBtn:"clearCondBtn",
		//dashSaveBtn : "dashboardSave"
	});
	

	$(".list-group-item").tooltip({
		boundary : 'window'
	});

	$("#collapseBtn").on('click', function(e) {
		if ($(this).find("i").hasClass("fa-angle-up")) {
			$(this).find("span").text("펼치기");
			$(this).find("i").removeClass("fa-angle-up");
			$(this).find("i").addClass("fa-angle-down");
		} else {
			$(this).find("span").text("숨기기");
			$(this).find("i").removeClass("fa-angle-down");
			$(this).find("i").addClass("fa-angle-up");
		}
	});

	SavedUserDataList.loadAction("objectSavedViewGrid");
	// 엑셀 다운로드
	ExcelDownloadObject.onClickEvt("dashboardExcelDown","saveTitle");
	
	HelpMsgAction.creathHelpBtn("dashboard", "helpIcon",
			OLAPDashboardHelpMsg);
	
	/*M_DATA_CNV1 COLUMNS SETTING 1224  */
		
	 $("#dashboardSave").on("click",function() {
		confirm({
				message : '<p class="text-center">검색한 정보를 저장하시겠습니까?</p>',
				title : '<h6>저장하기</h6>',
				buttons : {
					confirm : {
						label : '확인',
						className : 'btn btn-sm btn-primary'
					},
					cancel : {
						label : '취소',
						className : 'btn btn-sm  btn-secondary'
					}
				},
			},
			function(result) {
				if (result) {
					if (SavedUserDataList.Saved_Dashboard_Config.seqNum !== null) {
						var _confirmSaveDataset = $('#savedData-sequence-'+ SavedUserDataList.Saved_Dashboard_Config.seqNum).parent().parent().data("JSGridItem");
						var _msg = "기존에 선택한 저장정보에 덮어쓰겠습니까?";
						if (_confirmSaveDataset
								.hasOwnProperty("titleText")
								&& _confirmSaveDataset.titleText !== "") {
							_msg = "''"
									+ _confirmSaveDataset.titleText
									+ "' 저장정보에 덮어쓰겠습니까?"
						}

						var seqNumber = Number(SavedUserDataList.Saved_Dashboard_Config.seqNum);
						if (_confirmSaveDataset
								.hasOwnProperty("seqNum")) {
							seqNumber = _confirmSaveDataset.seqNum;
						}
						confirm(
								{
									message : _msg, // 메시지(필수)
									title : '<h6>저장하기</h6>',
									buttons : { // 버튼 스타일 (선택)
										confirm : {
											label : '예',
											className : 'btn btn-sm btn-primary'
										},
										cancel : {
											label : '아니오',
											className : 'btn btn-sm btn-warning'
										}
									}
								},
								function(result) {
									if (result) {
										SavedUserDataList.saveAction(
														{
															title : $(
																	"#saveTitle")
																	.val(),
															description : $(
																	"#saveDescription")
																	.val(),
															seqNum : seqNumber
														},
														fn_refect_userSelected_dataset);
									} else {
										//console.log(_confirmSaveDataset.titleText,$("#saveTitle").val());
										//console.log(_confirmSaveDataset.titleText === $("#saveTitle").val());
										if (_confirmSaveDataset.titleText === $("#saveTitle").val()) {
											alert("동일한 제목은 저장할 수 없습니다.");
										} else {
											SavedUserDataList.saveAction(
											{
												title : $(
														"#saveTitle")
														.val(),
												description : $(
														"#saveDescription")
														.val()

											},fn_refect_userSelected_dataset);
										} //if
									} // if
								}); // func
					} else {
						SavedUserDataList.saveAction(
									{
										title : $("#saveTitle").val(),
										description : $("#saveDescription").val()
									});
					}// if
				}
			});

}); 
	 
	 <%-- 머신러닝예측 버튼  --%>
	$("#btn_predictWithSearch").click(function(e){
		e.preventDefault();
		
		var searchGridObj = $("#objectViewGrid").jsGrid("option", "data");
		
		// 조회 데이터 유무 확인 
		if (typeof searchGridObj[0] == "undefined" || $("#objectViewGrid").jsGrid("option", "data") ==0	){ 
			alert("조회된 데이터가 없습니다");
			return false;
		}
		
		
		// 조회건수 10000건으로 제한
		if (searchGridObj.length > 10000){
			alert("조회된 데이터가 너무 많습니다<br/><br/>머신러닝분석/Seal 추천 메뉴를 이용하세요.");
			return false;
		}
		
		
		// 필수필드 선택 체크
		if( searchGridObj[0].DWG_NO == undefined || 
				searchGridObj[0].SHEET_NO == undefined ||
				searchGridObj[0].JOB_NO == undefined ){
			alert("Dwg No. Sheet No. Job No. 항목은 필수로 선택되어야 합니다.");
			return false;
		}
						
			
		$('#modalPredict .modal-content').css("height",($(window).height()-50)+"px");
		$('#modalPredictFrame').css("height",($(window).height()-200)+"px");
		$("#predictWithSearch").attr('src',"<c:url value='/ml/modelPredictWithSearchView.do'/>");
		$("#modalPredict").show();
	});

	 // Test
	 $("#testCase").on("click",function(e){
		 // 불러오기 쿼리 짜기 
		 // 오브젝트 세팅
		 // draw
		 // 저장하기 불러오기 객체 삽입
		 // 불러오기 세팅
	 });
	 // Test End
	 
	
});

</script>



<div class="container-fluid">
	<div class="row">
		<div id="searchArea" class="top-search-div collapse show mt-3">
			<div class="col-12">
				<div class="row">

					<!-- 리뉴얼 버전 -->
					<div class="col-lg-4 col-xl-3">
						<div class="card custom-search-card-div-height">
							<div class="card-body object-select-card-body">
								<div class="card-title">
									<div class="row">
										<div class="col">
											<p class="h5">
												<i class="fas fa-search"></i> <strong>항목 선택</strong>
											</p>
										</div>
									</div>
								</div>
								<div class="row">
									<div class="col-12 mb-3">
										<div class="card">
											<form id="objectDetailinfo" class="custom-scroll-div"><!--M_DATA_CNV1 columm list  -->
											</form>
										</div>

									</div>

								</div>
							</div>
						</div>
					</div>
					<!-- <div class="col-lg-12 col-xl-8 mt-xs-9 mt-sm-9 mt-md-9 mt-lg-0"> -->
					<div class="col-lg-8 col-xl-5 mt-xs-3 mt-sm-3 mt-md-3 mt-lg-0">
						<div class="card custom-search-card-div-height">
							<div class="col-12">
								<div class="mt-3 ml-3">
									<p class="h5">
										<i class="fas fa-search"></i> <strong>조회조건</strong>
										<button id="conditionAdd" class="btn btn-sm btn-primary">추가</button>
									</p>
								</div>
								<div id="conditionArea" class="scroll-height-fix-two card " style="height: 120px;">
									<div class="custom-none-div d-flex justify-content-center align-items-center">
										<p class="div-shadow-text text-right ">
											조건을 추가하려면<br> [추가]버튼을 클릭하십시오.
										</p>
									</div>
								</div>
								<div class="mt-3 ml-3">
									<p class="h5">
										<i class="fas fa-search"></i> <strong>정렬 조건</strong>
										<button id="orderbyAdd" class="btn btn-sm btn-primary">추가</button>
									</p>
								</div>
								<div id="orderbyArea" class="scroll-height-fix-two card"
									style="height: 120px;">
									<div
										class="custom-none-div d-flex justify-content-center align-items-center">
										<p class="div-shadow-text text-center">
											정렬을 추가하려면<br> [추가]버튼을 클릭하십시오.
										</p>
									</div>
								</div>
								<div class="pt-3">
									<div class="d-flex justify-content-end">
										<button id="dataSubmitBtn" class="btn btn-sm btn-primary mr-2">조회하기</button>
										<button id="clearCondBtn" class="btn btn-sm btn-outline-primary ">초기화 하기</button>
										<!-- <button id="testCase" class="btn btn-sm btn-primary">테스트</button> -->
									</div>
								</div>
							</div>
						</div>
					</div>

					<div
						class="col-lg-12 col-xl-4 mt-xs-3 mt-sm-3 mt-md-3 mt-lg-3 mt-xl-0">
						<div class="card custom-search-card-div-height">
							<div class="card-body custom-right-div">
								<div class="row">
									<div class="col-12">
										<div class="mt-3 ml-3">
											<p class="h5">
												<i class="fas fa-save"></i> <strong>저장 목록</strong>
											</p>
										</div>
									</div>
									<div class="saved-list-pager">
										<div class="col-12 ">
											<div class="saved-list-div">
												<div id="objectSavedViewGrid"></div>
											</div>
										</div>
	
										<div class="col-12">
											<div id="externalSavedPager"></div>
										</div>
									</div>

									<div class="col-12">
										<div class="pt-2 custom-responsive-p2">
											<input id="saveTitle" type="text"
												class="form-control form-control-sm" placeholder="저장할 제목 표시"
												maxlength="20">
										</div>
										<div class="pt-2 custom-responsive-p2 flex-grow-1">
											<input id="saveDescription" type="text"
												class="form-control form-control-sm" placeholder="상세 설명"
												maxlength="100">
										</div>
									</div>
									<div class="col-12 d-flex flex-row-reverse bd-highlight">
										<div class="p-2 bd-highlight">
											<button id="dashboardSave" type="button"
												class="btn btn-sm cutom-btn-outline-savebtn"
												data-target="#saveConfirmModal">
												현재 조건 저장하기 <i class="far fa-save"></i>
											</button>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

		</div>

		<div class="col-12 text-right custom-show-hide-div mt-3 pr-4">
			<a id="collapseBtn"
				class="btn btn-sm btn-primary custom-show-hide-btn"
				data-toggle="collapse" data-target="#searchArea"> <span>숨기기</span>
				<i class="fa fa-angle-up" aria-hidden="true"></i>
			</a>
		</div>
	</div>


	<div class="row">
		<div class="col-12 mt-0">
			<div class="d-flex bd-highlight">
				<div
					class="h5 p-2 custom-responsive-p2 p-2 flex-grow-1 bd-highlight">
					<i class="fas fa-save"></i> <strong>조회결과</strong>
				</div>

				<div class="p-2 p-2 bd-highlight">
				<button id="btn_predictWithSearch"  type="button" class="btn btn-sm cutom-btn-outline-lagoon" data-toggle="modal" data-target="#modalPredict"> 머신러닝예측 <i class="far fa-file-excel"></i></button>
					<button id="dashboardExcelDown" type="button"
						class="btn btn-sm cutom-btn-outline-lagoon">
						엑셀다운로드 <i class="far fa-file-excel"></i>
					</button>
				</div>

	
			</div>
		</div>
		
		<div class="col-12 pb-2">
			<div class="card m-2 p-2">
				<div class="row" >
					<!-- <div style="width: 100%;"> --> 
					<div class="col-12" style="width: 100%;">
						<div id="objectViewGrid"></div>
					</div>
					
					<div class="col-12">
						<div class="row">
							<div class="col-12">
								<div id="externalPager"></div>
							</div>
						</div>
					</div>
					<!-- </div> -->
				</div>			
			</div>


		</div>
		
		
	</div>
</div>



<%--Modal: Predict Pop--%>
<div class="modal fade" id="modalPredict" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg" role="document"  style="max-width:1400px;">
    <!--Content-->
    <div class="modal-content">
      <div class="modal-header  justify-content-center" style="height:5px;"> </div>
      <!--Body-->
      <div class="modal-body mb-0 p-0">
        <!--Google map-->
        <div id="modalPredictFrame" class="z-depth-1-half map-container" style="height: 530px">
          <iframe id="predictWithSearch" src=""  frameborder="0" style="border:0;width:100%;height:100%" allowfullscreen></iframe>
        </div>
      </div>
      <!--Footer-->
      <div class="modal-footer justify-content-center">
        <button type="button" class="btn btn-outline-secondary btn-md" data-dismiss="modal">Close <i class="fas fa-times ml-1"></i></button>
      </div>
    </div>
    <!--/.Content-->
  </div>
</div>
<%--Modal: Predict Pop--%>

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
