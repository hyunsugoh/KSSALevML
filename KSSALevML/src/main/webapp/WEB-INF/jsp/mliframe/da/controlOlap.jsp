<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript">
var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
var modelUid = "${modelUid}";
var mid = "${mid}";
var sourceIds = parent.sourceidStr;

var cols_def = [
	{caption : ['선택'],	ref : 'SEL_CHK',	width : '20%',  style : 'text-align:center',	type : 'checkbox',	typeinfo : {checkedvalue : 'Y', uncheckedvalue : 'N'}},
	{caption : ['칼럼명'],	ref : 'COL_NAME',	width : '80%',  style : 'text-align:left',		type : 'output'}
]

$(document).ready(function() {
	sourceIds=cfn_getSourceList(sourceIds);
	$('#sourceGrid').css('height','720px');	// getSourceList에서 sourceGrid를 500px로 설정함.
	
	
	var colList = [];
	var orderCnt = 1;
	var searchCnt = 1;
	
	var param = {
		subpjtid  : subPjtId,
		modeluid  : modelUid,
		sourceuid : sourceIds,
	};
	$.doPost({
		url : "/mlDeploy/getDeployList.do",
		data : param,
		success : function(data){
			var list = data.list;
			console.log(list);
			for(var i=0;i<list.length;i++){
				$('#select_deploy_name').append('<option value="'+list[i].SUB_PJT_ID+'/'+list[i].DEPLOY_ID+'">'+list[i].DEPLOY_NAME+'</option>');
			}
			$.doPost({
				url : "/mliframe/getControlInfo.do",
				data : param,
				success : function(data){
					console.log(data);
					cfn_setDataGrid(subPjtId,modelUid,sourceIds,data.params);
					
					data.cols.forEach(x => colList.push(x.COL_NAME));
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		},
		error : function(jqxXHR, textStatus, errorThrown){
			alert('오류가 발생 했습니다.');
		}
	});
	
	
	// 실행 버튼 클릭 시 이벤트
	$("#dataSubmitBtn").click(function() {
		
		var formData = $("form").serializeArray();
		console.log(formData);
		/*
			param = {
				search : [{COL_NAME : "", VALUE : "", TYPE : ""}],
				order : [{COL_NAME : "", TYPE : ""}]
			}
		*/
		
	});
	
	
	// 조회 조건 추가 버튼 클릭 시 이벤트
	$("#btn_add_search_condition").click(function() {
		var template = $("#div_search_template").clone(true);
		
		// 템플릿 id, display 옵션 삭제
		template.removeClass("d-none");
		template.removeAttr("id");
		
		
		// 조회 조건 attribute 셋팅
		template.find("input[type=radio]").attr('name', 'search[' + searchCnt + '][compare_type]');
		template.find("input[type=radio][value=col]").attr('id', 'radio_search_compare_type_col' + searchCnt);
		template.find("input[type=radio][value=val]").attr('id', 'radio_search_compare_type_val' + searchCnt);
		
		
		// 컬럼명 셋팅
		colList.forEach(x => {
			var option = "<option value='" + x + "' name='col_name'>" + x + "</option>";
			template.find(".select_search_column").append(option);
			template.find(".select_search_compare_column").append(option);
		});
		template.find(".select_search_column").attr('name', 'search[' + searchCnt + '][column]')
		
		template.find(".select_search_type").attr('name', 'search[' + searchCnt + '][type]');
		
		
		template.appendTo($("#div_search_condition"));
		searchCnt++;
	});
	
	
	// 조회 조건 선택 시 컬럼 혹은 값 input 추가
	$("input[type=radio].radio_search_type").change(function() {
		var _this = $(this);
		
		var value = _this.val();
		var card = _this.closest(".card");
		
		
		// 컬럼 선택시  class : d-none 추가/삭제로
		if(value == 'col') {
			card.find(".div_search_compare_column").removeClass('d-none');
			card.find(".div_search_compare_value").addClass('d-none');
			card.find(".div_search_compare_value_added").addClass('d-none');
			
			card.find("[value=between]").attr('disabled', true);
		} else if (value == 'val') {
			card.find(".div_search_compare_column").addClass('d-none');
			card.find(".div_search_compare_value").removeClass('d-none');
			
			card.find("[value=between]").removeAttr('disabled');
		} else {
			alert('선택된 항목이 없습니다.');
		}
	});
	
	
	$(".select_search_type").change(function() {
		var _this = $(this);
		
		var value = _this.val();
		var card = _this.closest('.card');
		var selected_type = card.find('input[type=radio]:checked').val();

		
		if(value == 'between') {
			
			if(selected_type == 'col') {
				alert("비교 유형이 '값'이 아닌 경우 '사이에 있다' 조건을 사용할 수 없습니다.");
				return;
			}
			
			card.find(".div_search_compare_value_added").removeClass('d-none');
		
		} else {
			card.find(".div_search_compare_value_added").addClass('d-none');
		}
	});
	
	
	// 정렬 조건 추가 버튼 클릭 시 이벤트
	$("#btn_add_order_condition").click(function() {
		
		var template = $("#div_order_template").clone(true);
		 
		
		// 템플릿 id, display 옵션 삭제
		template.removeClass("d-none");
		template.removeAttr("id");
		
		
		// select attr 수정
		template.find("select").attr('name', 'order[' + orderCnt + '][column]');
		
		template.find("select").attr('id', 'select_order_column' + orderCnt);
		template.find("select").prev("label").attr('for', '#select_order_column' + orderCnt);
		
		
		// radio attr 수정
		template.find("input[type=radio]").attr('name', 'order[' + orderCnt +'][order]');
		template.find("input[type=radio][value=asc]").attr('id', 'radio_order_type_asc' + orderCnt);
		template.find("input[type=radio][value=desc]").attr('id', 'radio_order_type_desc' + orderCnt);
		
		
		template.find(".label_order_asc").attr('for', '#radio_order_type_asc' + orderCnt);
		template.find(".label_order_desc").attr('for', '#radio_order_type_desc' + orderCnt);
		
		
		colList.forEach(x => {
			var option = "<option value='" + x + "' name='col_name'>" + x + "</option>";
			template.find("select").append(option);
		});
		
		
		template.appendTo($("#div_order_condition"));
		orderCnt++;
	});
	
	
	$(".btn_close_card").click(function() {
		$(this).closest(".card").remove();
	});
	
});
</script>
<style>
	#div_source,#div_control,#div_target{
		height:800px !important;
	}
	
	label.form-control-sm {
		margin : 0px;
	}
	.form-row {
		padding-top : 4px;
		padding-bottom : 4px;
	}
	option:disabled {
		color : #e1e1e1;
	}
</style>
<div class="row">
	<div class="col-4 card l-20 mr-30" id="div_source">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				SOURCE
				<img id="img_sourceGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
				<span id="span_sourceGrid_row_cnt" style="float:right;">0건</span>
			</dt>
		</dl>
		<div class="col-12 p-0">
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<div id="sourceGrid" style="width:100%;height:720px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="col-3 card mr-10" id="div_control">
		<dl class="row pt-3 mb-0">
			<dt class="col-8">CONTROL</dt>
			<dd class="col-4">
				<button id="dataSubmitBtn" type="button" class="btn btn-outline-success float-right">실행 <i class="far fa-save"></i></button>
			</dd>
	  	</dl>
	  	
	  	<!-- 조회 조건 -->
	  	<div class="container pr-1 pl-1">
	  		<div class="container p-0 my-2">
		  		<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-0">
								<label class="mliFrame-name"><strong>조회 조건</strong></label>
								<button id="btn_add_search_condition" class="btn btn-outline-success btn-sm float-right"><i class="fas fa-plus fa-xs"></i></button>
							</p>
						</div>
					</div>
				</div>		
				
				
				<!-- 조회조건 추가 div -->
				<div class="container my-2 p-0" id="div_search_condition">
				</div>
			</div>
			
			
			<!-- 구분선 있으면 괜찮을 것 같음. -->
			
			<!-- 정렬 조건 -->
			<div class="container p-0 my-2">
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-0">
								<label class="mliFrame-name"><strong>정렬 조건</strong></label>
								<button id="btn_add_order_condition" class="btn btn-outline-success btn-sm float-right"><i class="fas fa-plus fa-xs"></i></button>
							</p>
						</div>
					</div>
				</div>
			</div>
			
			<!-- 정렬조건 추가 div -->
			<div class="container my-2 p-0" id="div_order_condition">
			</div>


			
	  	</div>
	</div>
	
	<div class="col-4 card" id="div_target">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
				<img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
				<span id="span_targetGrid_row_cnt" style="float:right;">0건</span>
			</dt>
		</dl>
		<div class="col-12 p-0">
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<div id="targetGrid" style="width:100%;height:720px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>





<!-- 조회조건 추가 div -->
<!-- 
	1. 조건 추가 시 아래 d-none 삭제 후 추가
	2. 제목 text 추가
	3. input text, select 에 value/id 혹은 name

 --> 
<!-- <div class="card my-2 card_order_condition d-none" id="div_order_template"> -->
<div class="card my-2 card_search_condition d-none" id="div_search_template">
	<div class="container py-2">
		<div class="row col">
			<button type="button" class="btn_close_card close btn-sm">×</button>
		</div>
		
		<form class="form_search_condition">
			<div class="form-row">
				<label class="form-control-sm col-3">비교 유형</label>
				<div class="form-check form-check-inline">
					<input type="radio" class="form-check-input radio_search_type" value="col">
					<label class="form-check-label">컬럼</label>
				</div>
				<div class="form-check form-check-inline">
					<input type="radio" class="form-check-input radio_search_type" value="val">
					<label class="form-check-label">값</label>
				</div>
			</div>
			
			<!-- 조회 기준 컬럼 -->
			<div class="form-row">
				<label class="form-control-sm col-3">기준 컬럼</label>
				<select class="form-control form-control-sm col mb-2 select_search_column"></select>
			</div>
			
			<!-- 조회 비교 타입 -->
			<div class="form-row">
				<label class="form-control-sm col-3">비교 조건</label>
				<select class="form-control form-control-sm col mb-2 select_search_type">
					<option value="">크다</option>
					<option value="">작다</option>
					<option value="">같거나 크다</option>
					<option value="">같거나 작다</option>
					<option value="">같다</option>
					<option value="">포함하다</option>
					<option value="between">사이에 있다</option>
				</select>
			</div>
			
			
			<!-- 조회 비교 컬럼 -->
			<div class="form-row d-none div_search_compare_column">
				<label class="form-control-sm col-3">비교 컬럼</label>
				<select class="form-control form-control-sm col mb-2 select_search_compare_column"></select>
			</div>
			
			<!-- 조회 비교 값 -->
			<div class="form-row d-none div_search_compare_value">
				<label class="form-control-sm col-3">비교 값</label>
				<input class="form-control form-control-sm col mb-2">
			</div>
			
			<!-- Between 추가 비교 값 -->
			<div class="form-row d-none div_search_compare_value_added">
				<label class="form-control-sm col-3">추가 비교 값</label>
				<input class="form-control form-control-sm col mb-2">
			</div>
			
		</form>
	</div>
</div>





<!-- 정렬조건 추가 div -->
<div class="card my-2 card_order_condition d-none" id="div_order_template">
	<div class="container py-2">
		<div class="row col">
			<button type="button" class="btn_close_card close btn-sm">×</button>
		</div>
		<form>
			<div class="form-row">
				<label class="form-control-sm">정렬 컬럼</label>
				<select class="form-control form-control-sm col mb-2"></select>
			</div>
			
			<div class="form-row">
				<label class="form-control-sm">정렬 조건</label>
				<div class="form-check form-check-inline">
					<input type="radio" class="form-check-input" value="asc">
					<label class="form-check-label label_order_asc">오름차순</label>
				</div>
				<div class="form-check form-check-inline">
					<input type="radio" class="form-check-input" value="desc">
					<label class="form-check-label label_order_desc">내림차순</label>
				</div>
			</div>
		</form>
	</div>
</div>
