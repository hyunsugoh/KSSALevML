<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
var sourceIds = parent.sourceidStr;
var modelUid = "${modelUid}";
var mid = "${mid}";

$(document).ready(function() {
	sourceIds=cfn_getSourceList(sourceIds);
	var param = {
		subpjtid  : subPjtId,
		modeluid  : modelUid,
		sourceuid : sourceIds
	};
	
	$.doPost({
		url : "/mliframe/getControlInfo.do",
		data : param,
		success : function(data){
			console.log(data);
			var map = data.cols.map(x => x.COL_NAME);
			changeSelectOpt(map);
			
			cfn_setDataGrid(subPjtId,modelUid,sourceIds,data.params);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			alert('오류가 발생 했습니다.');
		}
	});
	
	
	$("#dataSubmitBtn").click(function(e) {
		var controlParam = cfn_getControlParam();
		var param = {
				subpjtid		: subPjtId,
				modeluid 		: modelUid,
				sourceid		: sourceIds,
				
				controlparam	: controlParam,
				params			: new Object()
		};
		

		for(var i in controlParam.params) {
			var item = controlParam.params[i];
			param.params[item.id.replace(/^(select_|input_)/, '')] = item.value;
		}
		
		
		// 중복선택 되지 않도록
		var prm = param.params;
		if(prm.col == prm.row || prm.col == prm.val || prm.row == prm.val) {
			alert('중복된 컬럼을 선택하실 수 없습니다.');
			return;
		}
		
		
		$.doPost({
			url : "/mliframe/pivot.do",
			data : param,
			success : function(data) {
				if(data.status == "success") {
					cfn_createCommonDataGrid("targetGrid",data.target_header,data.target_data);
					//cfn_setDiagramText(modelUid);
				} else {
					alert(data.message);
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생하였습니다.');
			}
		});
		
	});
	
	
	
	// split data 가 앞에 있는경우
	// source data 변경 시 select option 변경 필요
	$("#select_source").change(function() {
		
	});
	
	
	
	// cols : source Data 의 column list 
	function changeSelectOpt(cols) {
		// 기존 옵션 삭제
		$(".div_select select").empty();
		
		// 새로운 옵션 append
		for(var i in cols) {
			var opt = $("<option value='" + cols[i] + "'>" + cols[i] + "</option>");
			$(".div_select select").append(opt);	
		}	
	}
	
});
</script>

<div class="row">
	<!-- source panel -->
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
						<div id="sourceGrid" style="width:100%;height:500px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	
	<!-- control panel -->
	<div class="col-3 card mr-10" id="div_control">
		<dl class="row pt-3 mb-0">
			<dt class="col-8">CONTROL</dt>
			<dd class="col-4">
				<button id="dataSubmitBtn" type="button" class="btn btn-outline-success">실행 <i class="far fa-save"></i></button>
			</dd>
	  	</dl>
	  	
	  	
	  	<div class="container pr-1 pl-1">
			<!-- 행 -->
			<div class="div_select">
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">Row</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<select class="form-control form-control-sm" data-width="200px" id="select_row"></select>
				</div>
			</div>
			
			<!-- 열 -->
			<div class="div_select">
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">Column</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<select class="form-control form-control-sm" data-width="200px" id="select_col"></select>
				</div>
			</div>
			
			<!-- 값 -->
			<div class="div_select">
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">Value</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<select class="form-control form-control-sm" data-width="200px" id="select_val"></select>
				</div>
			</div>
		</div>
  	</div>
	
	
	
	<!-- result panel -->
	<div class="col-4 card">
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
						<div id="targetGrid" style="width:100%;height:500px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>