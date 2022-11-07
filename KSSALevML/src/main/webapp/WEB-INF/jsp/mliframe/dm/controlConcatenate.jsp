<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var sourceIds = parent.sourceidStr;

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
				cfn_setDataGrid(subPjtId,modelUid,sourceIds,data.params);
				cfn_createControlGrid(data.cols);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			sourceGrid = _SBGrid.getGrid("sourceGrid");
			var data = sourceGrid.getGridDataAll();
			var colIdx = [];
			controlGrid = _SBGrid.getGrid("controlGrid");
			var chkData = controlGrid.getCheckedRowData(0);
			var delimiter = $('#input_delimiter').val();
			if(chkData.length==0){
				alert('칼럼을 체크해 주세요.');
				return;
			}
			if(delimiter.length==0){
				alert('Start Index를 체크해 주세요.');
				return;
			}
			for(var i=0;i<chkData.length;i++){
				colIdx.push(chkData[i].rownum-1);
			}
			var controlParam = cfn_getControlParam();
			
			var param = {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
				sourceuid : sourceIds,
				list	  : data,
				delimiter : delimiter,
				cols	  : colIdx,
				controlparam : controlParam
			};
			
			$.doPost({
				url : "/mliframe/concatenate.do",
				data : param,
				success : function(data){
					if(data.header.length>0){
						cfn_createCommonDataGrid("targetGrid",data.header,data.list);
						cfn_setDiagramText(modelUid);
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		});
	});
</script>
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
						<div id="sourceGrid" style="width:100%;height:500px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
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
			<div id="controlGrid" style="width:100%;height:300px;"></div>
		</div>
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<label class="mliFrame-name" id="label_delimiter"><strong>Delimiter</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<input type="text" class="form-control-text" id="input_delimiter" name="delimiter" />
		</div>
	</div>
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
