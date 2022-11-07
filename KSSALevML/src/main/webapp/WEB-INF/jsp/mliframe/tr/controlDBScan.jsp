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
			sourceuid : sourceIds,
			gubun	  : "ML",
			mid		  : mid
		};
		$.doPost({
			url : "/mliframe/getControlInfo.do",
			data : param,
			success : function(data){
				cfn_setDataGrid(subPjtId,modelUid,sourceIds,data.params);
				
				if(data.fcols.length > 0) {
					cfn_createFeatureGrid(data.fcols);
				}
				
				if(data.lcols.length > 0) {
					cfn_createLabelGrid(data.lcols);
				}
				
				$(".gridTabPane").addClass('active').addClass('hide');

				if(data.xmldata.length > 0) { 
					var cols = [
						{caption : ['Attribute'],	ref : 'attr',		width : '40%',  style : 'text-align:left',		type : 'output'},
						{caption : ['Value'],		ref : 'value',		width : '60%',  style : 'text-align:right',		type : 'output'}
					];
					cfn_createGrid("resultGrid",data.xmldata,cols);
					cfn_setTargetImage(subPjtId,modelUid);
				}
				
				if(data.header.length>0){
					cfn_createCommonDataGrid("targetGrid",data.header,data.data);
				}
				
				$(".gridTabPane").removeClass('active').removeClass('hide');
				$(".nav-link").first().tab("show");
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		/*
			실행 버튼 클릭 시 호출되는 이벤트
		*/
		$('#dataSubmitBtn').click(function(e){
			
			var controlParam = cfn_getControlParamML();
			var trainParam = {
					subpjtid  		: subPjtId,
					targetid  		: modelUid,
					sourceid 		: sourceIds,
					
					features 		: null,					// feature grid에서 선택한 컬럼
			};
			
			// feature 정합성 확인 - 한개이상 선택 필수
			if(controlParam.fcols.length == 0) {
				alert('Feature 컬럼을 선택해 주세요.');
				return;
			} else {
				trainParam.features = controlParam.fcols;
			}
			
			if(controlParam.params.length > 0) {
				trainParam.algparam = new Object();
				for(var idx in controlParam.params) {
					var item = controlParam.params[idx];
					
					// 공백이거나 값이 없는 경우 파라미터에 추가하지 않음.
					if(item.value == "" || item.value == undefined || item.value == null) {
						continue;
					}
					
					trainParam.algparam[item.id.replace(/^(select_|input_)/, '')] = item.value;
				}
			}
			console.log(trainParam);
	
			// ML Api 서버와 통신
			$.doPost({
				url : cv_apiAddr + "/dbscan/train/",
				crossOrigin : true,
				data : trainParam,
				success : function(data){
					console.log(data);
					if(data.status === "success") {
						var controlParam = cfn_getControlParamML(true);
						
						$(".nav-link").first().tab('show');
						$(".gridTabPane").attr("style", "display : block;");
						
						cfn_postTrainModel(subPjtId,modelUid,sourceIds,controlParam,true);
						cfn_setTargetImage(subPjtId,modelUid);
					} else {
						alert('오류가 발생하였습니다.\n' + data.message);
						return;
					}
				}
			});
		});
		
		$(".nav-link").click(function(e) {
			$(".gridTabPane").removeAttr('style');
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
			
			
			<!-- 알고리즘 별 파라미터 -->
			
			<!-- 1. eps -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">EPS</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_eps" value="0.5">
			</div>
			
			
			<!-- 2. min samples -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Minimum Samples</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_min_samples" value="5" step="1">
			</div>
			
			
			
			<!-- 3. metric -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Metric</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_metric">
					<option value="euclidean">euclidean</option>
					<option value="manhattan">manhattan</option>
					<option value="chebyshev">chebyshev</option>
					<option value="minkowski">minkowski</option>
					<option value="seuclidean">seuclidean</option>
					<option value="mahalanobis">mahalanobis</option>
				</select>
			</div>
			
			
			<!-- 4. algorithm -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Algorithm</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_algorithm">
					<option value="auto">auto</option>
					<option value="ball_tree">ball_tree</option>
					<option value="kd_tree">kd_tree</option>
					<option value="brute">brute</option>
				</select>
			</div>
			
			
			<!-- 5. leaf size -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Leaf Size</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_leaf_size" value="30" step="1">
			</div>
			
				
			
			<!-- 6. p -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">P</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_p">
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
					<a class="nav-link" id="clusterResultAnalsTab" data-toggle="tab" href="#clusterResultAnals">분석 결과</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" id="clusterResultDataTab" data-toggle="tab" href="#clusterResultData">데이터</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" id="visualizationTab" data-toggle="tab" href="#visualization">시각화</a>
				</li>
			</ul>
			
			<div class="tab-content" id="tabContent">
				<div class="tab-pane fade gridTabPane" id="clusterResultAnals">
					<div class="card p-2">
						<div id="resultGrid" style="width:100%;height:480px;"></div>
					</div>
				</div>
				<div class="tab-pane fade gridTabPane" id="clusterResultData">
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
