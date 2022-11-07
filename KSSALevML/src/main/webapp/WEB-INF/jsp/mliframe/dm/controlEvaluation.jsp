<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var sourceIds = parent.sourceidStr;
	
	$(document).ready(function() {
		sourceIds=cfn_getSourceList(sourceIds);
		$("#div_moe").hide();
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid,
			sourceuid : sourceIds
		};
		
		$.doPost({
			url : "/mliframe/getControlInfo.do",
			data : param,
			success : function(data){
				cfn_setDataGrid(subPjtId,modelUid,sourceIds,data.params);
				cfn_createControlGrid(data.cols);
				
				/* var mmword;
				for(var idx in data.params) {
					if(data.params[idx].PARAM_ID.match(/mismatch_word$/) != null){
						mmword = data.params[idx].PARAM_VALUE;
						break;
					}
				}
				
				var colList = [];
				data.forEach(x -> colList.add(x.COL_NAME));)
				coloredGrid(colList, data.list, mmword); */
				
				// span에 통계값 입력
				var sttparam = data.params;
				for(var idx in sttparam){
					var obj = sttparam[idx];
					if(obj.PARAM_ID.match(/^span/) != null){
						
						var val = obj.PARAM_ID.match(/per$/) != null 
										? obj.PARAM_VALUE : comma(obj.PARAM_VALUE);
						
						$("#" + obj.PARAM_ID).text(val);
					}
				}
				
				//$("#셀렉트박스ID option:selected").val()
				var type = (data.param.filter(x => x.PARAM_ID === "selected_predict_type")).PARAM_VALUE;
				if(type == "regression") {
					$("#div_moe").show();
				} else {
					$("#div_moe").hide();
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			var colIdx = [];
			controlGrid = _SBGrid.getGrid("controlGrid");
			var chkData = controlGrid.getCheckedRowData(0);
			
			
			var predtype = $("#select_predict_type").val();
			
			var matchword = $("#input_match_word").val().trim();
			var mismatchword = $("#input_mismatch_word").val().trim();
			
			var moe;
			if(predtype === "regression") {
				moe = $("#input_moe").val();
			}
			
			
			
			
			if(chkData.length==0){
				alert('칼럼을 체크해 주세요.');
				return;
			} else if(chkData.length != 2) {
				alert('컬럼은 2개를 선택해야 합니다.');	// 컬럼 2개 선택하라는 문구
				return;
			}
			
			if( matchword.length == 0 || mismatchword.length == 0) {
				alert('일치/불일치 문구를 입력해 주세요.');
				return;
			} else if(matchword == mismatchword){
				alert('일치/불일치 문구를 다르게 입력해 주세요.');
				return;
			}
			
			if(predtype === "regression" && (moe === undefined || moe == null) ) {
				alert('허용오차를 입력해 주세요');
				return;
			}
			
			
			
			for(var i=0;i<chkData.length;i++){
				colIdx.push(chkData[i].rownum-1);
			}
			
			sourceGrid = _SBGrid.getGrid("sourceGrid");
			var data = sourceGrid.getGridDataAll();
			
			var controlParam = cfn_getControlParam();
			
			// span 추가
			$(".eval_span").each(function() {
				controlParam.params.push({id : $(this).attr('id'), value : null});
			});
			
			
			var param = {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
				sourceuid : sourceIds,
				list	  : data,
				cols	  : colIdx,
				moe		  : moe,
				predtype  : predtype,
				matchword	 : matchword,
				mismatchword : mismatchword,
				controlparam : controlParam
			};
			
			console.log(param);
			
			$.doPost({
				url : "/mliframe/evaluation.do",
				data : param,
				success : function(data){
					if(data.header.length>0){
						cfn_createCommonDataGrid("targetGrid",data.header,data.list);
						cfn_setDiagramText(modelUid);
						
						
						// 통계 데이터 Set
						var sttsList = Object.keys(data.statistics);
						
						for(var idx in sttsList) {
							var id = sttsList[idx];
							var val = id.match(/per$/) != null
										? data.statistics[id] : comma(data.statistics[id]);
										
							$("#span_" + id).text(val);
						}
						
						
						// false Data style 입히기
						//coloredGrid(data.header, data.list, mismatchword);
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		});
		
		
		$("#select_predict_type").change(function() {
			if($(this).val() == "classification") {
				$("#div_moe").hide();
			} else {
				$("#div_moe").show();
			}
		});
		
	});
	

 	/* function coloredGrid(header, data, containword) {
 		var evalIdx = header.findIndex(elmt => elmt.match(/_eval$/));
 		
 		if(evalIdx != -1) {
 			for(var idx in data) {
				var item = data[idx];
				
				if(item[evalIdx] == containword) {
					targetGrid.setRowStyle(parseInt(idx, 10), 'all', 'background', '#ffeeee');
				}
			}		
 		} 
	}	*/
</script>
<style>
th {
	border-bottom : 2px solid grey;
}
</style>
<div class="row">
	
	<!-- Source Div -->
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
	
	
	<!-- Control Div -->
	<div class="col-3 card mr-10" id="div_control">
		<dl class="row pt-3 mb-0">
			<dt class="col-8">CONTROL</dt>
			<dd class="col-4">
				<button id="dataSubmitBtn" type="button" class="btn btn-outline-success">실행 <i class="far fa-save"></i></button>
			</dd>
	  	</dl>
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-0">
						<label class="mliFrame-name required" id="label_controlGrid"><strong>Input Columns</strong></label>
					</p>
				</div>
			</div>
		</div>
		<div class="card">
			<div id="controlGrid" style="width:100%;height:250px;"></div>
		</div>
		
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name">Predict Type</strong>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_predict_type" name="select_predict_type">
				<option value="classification">classification</option>
				<option value="regression">regression</option>
			</select>
		</div>
		
		
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<label class="mliFrame-name required" id="label_evaluation"><strong>Match / Mismatch word</strong></label>
					</p>
				</div>
			</div>
		</div>
		<div class="form-group">
			<div>
				<input type="text" class="form-control-text" id="input_match_word" name="evaluation_word" style="width:48%;float:left;" placeholder="True">
				<input type="text" class="form-control-text" id="input_mismatch_word" name="evaluation_word" style="width:48%;float:right;" placeholder="False">
			</div>
		</div>
		
		
		<div id="div_moe">
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name required">Margin of error(허용오차)</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_moe">
			</div>
		</div>
		
	</div>
	
	
	<!-- Result Div -->
	<div class="col-4 card" id="div_result">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
				<img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
				<span id="span_targetGrid_row_cnt" class="float-right">0건</span>
			</dt>
		</dl>
		
		
		<div class="col-12 p-0">
			<div class="card m-2 pt-1 pb-0 px-2">
				<div class="card-body px-2 py-0">
					<table class="table table-borderless m-0">
						<thead>
							<tr style="border-bottom : 2px solid #dfdfdf;">
								<th style="width:33.3%;" scope="col" class="text-center py-2">일치</th>
								<th style="width:33.3%;" scope="col" class="text-center py-2">불일치</th>
								<th style="width:33.3%;" scope="col" class="text-center py-2">전체</th>
							</tr>
						</thead>
						<tbody>
							<tr>
								<td class="py-1">
									<p class="text-center my-2"><span id="span_match_cnt" class="eval_span">0</span>건</p>
									<p class="text-center my-2"><span id="span_match_per" class="eval_span">-</span>%</p>
								</td>
								<td class="py-1">
									<p class="text-center my-2"><span id="span_mismatch_cnt" class="eval_span">0</span>건</p>
									<p class="text-center my-2"><span id="span_mismatch_per" class="eval_span">-</span>%</p>
								</td>
								<td class="py-1">
									<p class="text-center my-2"><span id="span_entire_cnt" class="eval_span">0</span>건</p>
									<p class="text-center my-2"><span id="span_entire_per" class="eval_span">-</span>%</p>
								</td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
		</div>

		<div class="col-12 p-0">
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<div id="targetGrid" style="width:100%;height:410px;"></div>
					</div>
				</div>
			</div>
		</div>
		
	</div>
</div>