<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var sourceIds = parent.sourceidStr;
	
	$(document).ready(function() {
		var sourceIdArr = sourceIds.split("|");
		var sourceId = cfn_getSourceIDInPredict(sourceIdArr);
		var modelId = cfn_getModelIDInPredict(sourceIdArr);
		cfn_setSourceListBox(sourceId);
		
		var input_shape = [];
		
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid,
			sourceuid : sourceId,
			modelid   : modelId,
			gubun	  : "PR"
		};
		$.doPost({
			url : "/mliframe/getControlInfo.do",
			data : param,
			success : function(data){
				//cfn_setDataGrid(subPjtId,modelUid,sourceId,data.params);
				cfn_createFeatureGrid(data.fcols,{disabled:true});
				cfn_createLabelGrid(data.lcols,{disabled:true});
				
				var param = {
					subpjtid  : subPjtId,
					modeluid  : modelId,
					sourceuid : sourceId
				};
				$.doPost({
					url : "/mliframe/getParamInfo.do",
					data : param,
					success : function(data){
						for(var i=0;i<data.params.length;i++){
							if(data.params[i].PARAM_ID=="input_input_shape"){
								input_shape=data.params[i].PARAM_VALUE.split(',').map(e => parseInt(e));
								break;
							}
						}
					}
				});
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			
			var controlParam = cfn_getControlParamML();
			
			if($("#select_source").length > 0) {
				sourceId = $("#select_source").val();
			}
			
			var predictParam = {
					subpjtid	: subPjtId,
					sourceid	: sourceId,
					modelid 	: modelId,
					targetid	: modelUid,
					algparam	: {input_shape:input_shape},
					features 	: controlParam.fcols,
					label		: controlParam.lcols[0]
			};
			
			
			// ML Api 서버와 통신
			$.doPost({
				url : cv_apiAddr + "/cnn/predict/",
				crossOrigin : true,
				data : predictParam,
				success : function(data){
					if(data.status === "success") {
						//cfn_setTargetGrid(subPjtId,modelUid);
						//cfn_postPredictModel(subPjtId,modelUid);
						//cfn_setTargetImage(subPjtId,modelUid,'pr');
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
	  	<div class="container pr-1 pl-1">
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-0">
							<label class="mliFrame-name required"><strong>Features</strong></label>
						</p>
					</div>
				</div>
			</div>		
			<div class="card">
				<div id="featureGrid" style="width:100%;height:200px;"></div>
			</div>
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-0">
							<label class="mliFrame-name required"><strong>Label</strong></label>
						</p>
					</div>
				</div>
			</div>		
			<div class="card">
				<div id="labelGrid" style="width:100%;height:200px;"></div>
			</div>
		</div>
	</div>
	<div class="col-4 card" id="div_target">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
			</dt>
		</dl>
		<div class="col container pt-3 pb-3 pr-0 pl-0">
			<ul class="nav nav-tabs">
				<li class="nav-item">
					<a class="nav-link active" data-toggle="tab" href="#modelPredictResult">모델 예측결과</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" data-toggle="tab" href="#visualization">시각화</a>
				</li>
			</ul>
			
			<div class="tab-content" id="tabContent">
				<div class="tab-pane fade show active" id="modelPredictResult">
					<div class="card p-2">
						<div class="col">
							Predict
							<img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
							<span id="span_targetGrid_row_cnt" style="float:right;">0건</span>
						</div>
						<div id="targetGrid" style="width:100%;height:450px;"></div>
					</div>
				</div>
				<div class="tab-pane fade" id="visualization">
					<img id="img_model_visualization" style="width:100%;height: 300px;"/>
				</div>
			</div>
		</div>
	</div>
</div>
