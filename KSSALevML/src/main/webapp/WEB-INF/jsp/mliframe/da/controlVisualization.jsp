<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script src="https://code.highcharts.com/highcharts.js"></script>
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
				cfn_setSourceGrid(subPjtId,sourceIds,"sourceGrid");
				cfn_createControlGrid(data.cols);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			sourceGrid = _SBGrid.getGrid("sourceGrid");
			controlGrid = _SBGrid.getGrid("controlGrid");
			var series = [];
			var srcData = sourceGrid.getGridDataAll();
			var cols = controlGrid.getCheckedRowData(0);
			var stIdx = $('#input_start_range').val();
			var edIdx = $('#input_end_range').val();
			if(srcData.length<edIdx){
				alert('데이터 범위가 소스데이터를 초과했습니다.');
				return;
			}
			if(stIdx.length==0){
				alert('데이터 범위 입력해 주세요.');
				return;
			}
			if(edIdx.length==0){
				alert('데이터 범위 입력해 주세요.');
				return;
			}
			for(var i=0;i<cols.length;i++){
				var colIdx = cols[i].rownum-1;
				var data = [];
				for(var j=stIdx;j<edIdx;j++){
					data.push(parseFloat(srcData[j][colIdx]));
				}
				var obj = {
					name : cols[i].data.COL_NAME,
					data : data
				}
				series.push(obj);
			}
			
			Highcharts.chart('container', {
				chart: {
			        type: $('#select_chart_type').val()
			    },
			    title: {
			        text: 'Visualization'
			    },
			    legend: {
			        align: 'center',
			        verticalAlign: 'bottom'
			    },
			    series: series
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
					<p class="h5 mb-1">
						<label class="mliFrame-name required" id="label_sort_type"><strong>Chart Type</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_chart_type" name="sort_type">
				<option value="line">Line Chart</option>
				<option value="bar">Bar Chart</option>
				<option value="column">Column Chart</option>
				<option value="pie">Pie Chart</option>
			</select>
		</div>
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
						<label class="mliFrame-name required" id="label_data_range"><strong>Data Range</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<input type="text" class="form-control-number" id="input_start_range" name="data_range" style="width:48%;float:left;" value="0">
			<input type="text" class="form-control-number" id="input_end_range" name="data_range" style="width:48%;float:right;" value="20">
		</div>
	</div>
	<div class="col-4 card">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
			</dt>
		</dl>
		<div class="col-12 p-0">
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<figure class="highcharts-figure">
						    <div id="container"></div>
						</figure>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
