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
			controlGrid = _SBGrid.getGrid("controlGrid");
			var chkData = controlGrid.getCheckedRowData(0);
			if(chkData.length == 0) {
				alert('Feature 컬럼을 선택해 주세요.');
				return;
			}
			var features = [];
			for(var i=0;i<chkData.length;i++){
				features.push(chkData[i].data.COL_NAME);
			}
			
			var param = {
				subpjtid  	: subPjtId,
				targetid  	: modelUid,
				sourceid 	: sourceIds,
				features 	: features,
				algparam	: {
					method	: $('#select_correlation_type').val()
				}
			};
			
			$.doPost({
				url : cv_apiAddr + "/corrcoef/",
				crossOrigin : true,
				data : param,
				success : function(data){
					if(data.status === "success") {
						var cols = [
							{caption:['gb'],ref:"gb", width:150, style : 'text-align:left', type : 'output'}
						];
						for(var i=0;i<features.length;i++){
							var obj = {caption : [features[i]], ref:"f"+i, width:150, style : 'text-align:right', type : 'output'};
							cols.push(obj);
						}
						var list = [];
						var max = 0;
						for(var i=0;i<features.length;i++){
							var obj = {};
							for(var j=0;j<features.length;j++){
								var colStr = "f"+j;
								for(var k=0;k<data.result.length;k++){
									if((features[i]==data.result[k][0] && features[j]==data.result[k][1]) 
											|| (features[i]==data.result[k][1] && features[j]==data.result[k][0])){
										obj[colStr]=data.result[k][2].toFixed(3);
										if(max<data.result[k][2] && data.result[k][2]!=1){
											max=data.result[k][2].toFixed(3);
										}
									}
								}
							}
							obj.gb=features[i];
							list.push(obj);
						}
						cfn_createGrid("targetGrid",list,cols,{frozencols:1});
						
						var controlParam = cfn_getControlParam();
						targetGrid = _SBGrid.getGrid("targetGrid");
						var gData = targetGrid.getGridDataAll();
						var columns = targetGrid.getColumns();
						var cols = [];
						for(var i=0;i<columns.length;i++){
							cols.push(columns[i].caption[0]);
						}
						var param = {
							subpjtid  : subPjtId,
							modeluid  : modelUid,
							sourceuid : sourceIds,
							list	  : gData,
							cols	  : cols,
							controlparam : controlParam
						};
						$.doPost({
							url : "/mliframe/correlation.do",
							data : param,
							success : function(data){
								//cfn_createCommonDataGrid("targetGrid",data.header,data.list);
								cfn_setDiagramText(modelUid);
							},
							error : function(jqxXHR, textStatus, errorThrown){
								alert('오류가 발생 했습니다.');
							}
						});
					} else {
						alert('오류가 발생하였습니다.');
						return;
					}
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
					<p class="h5 mb-1">
						<label class="mliFrame-name required" id="label_controlGrid"><strong>Input Cols</strong></label>
					</p>
				</div>
			</div>
		</div>
		<div class="card">
			<div id="controlGrid" style="width:100%;height:180px;"></div>
		</div>
	  	<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<label class="mliFrame-name required" id="label_correlation_type"><strong>Correlation Type</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_correlation_type" name="correlation_type">
				<option value="pearson">pearson</option>
				<option value="spearman">spearman</option>
				<option value="kendall">kendall</option>
			</select>
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
