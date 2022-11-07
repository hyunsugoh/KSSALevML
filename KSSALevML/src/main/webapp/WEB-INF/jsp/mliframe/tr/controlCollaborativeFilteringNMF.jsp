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
					label	 		: null,					// label grid에서 선택한 컬럼
			};
			
			
			// feature 정합성 확인 - 2개만 선택가능
			if(controlParam.fcols.length == 0) {
				alert('Feature 컬럼을 선택해 주세요.');
				return;
			} else if (controlParam.fcols.length == 2) {
				trainParam.features = controlParam.fcols;
			} else {
				alert('Feature 컬럼은 두 개를 선택해야 합니다.');
				return;
			}
			
			
			// label 정합성 확인 - 한개만 선택 
			if(controlParam.lcols.length == 0) {
				alert('Label 컬럼을 선택해 주세요.');
				return;
			} else if(controlParam.lcols.length == 1) {
				trainParam.label = controlParam.lcols[0];
			} else {
				alert('Label 컬럼은 하나만 선택 가능합니다.');
				return;
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
			
	
			// ML Api 서버와 통신
			$.doPost({
				url : cv_apiAddr + "/cfnmf/train/",
				crossOrigin : true,
				data : trainParam,
				success : function(data){
					console.log(data);
					if(data.status === "success") {
						var controlParam = cfn_getControlParamML(true);
						
						$(".nav-link").first().tab('show');
						$(".gridTabPane").attr("style", "display : block;")
						
						cfn_postTrainModel(subPjtId,modelUid,sourceIds,controlParam, true);
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
			
			
			<!-- 알고리즘 별 파라미터 -->
			<!-- 1. n_components -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">n_components</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_n_components" value="1" step="1">
			</div>
			
			
			<!-- 2. init -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">init</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_init">
					<option value="">None</option>
					<option value="random">random</option>
					<option value="nndsvd">nndsvd</option>
					<option value="nndsvda">nndsvda</option>
					<option value="nndsvdar">nndsvdar</option>
					<option value="custom">custom</option>
				</select>
			</div>
			
			
			
			<!-- 3. alpha -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">alpha</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_alpha" value="1" step="1">
			</div>
			
			
			<!-- 4. beta_loss -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">beta_loss</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_beta_loss">
					<option value="frobenius">frobenius</option>
					<option value="kullback-leibler">kullback-leibler</option>
					<option value="nndsvda">itakura-saito</option>
				</select>
			</div>
			
			
			<!-- 5. l1_ratio -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">l1_ratio</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_l1_ratio" value="0" max="1" min="0">
			</div>
			
			
			<!-- 6. shuffle -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">shuffle</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_shuffle">
					<option value="true">true</option>
					<option value="false">false</option>
				</select>
			</div>
			
			
			<!-- 7. solver -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">solver</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_solver">
					<option value="cd">cd</option>
					<option value="mu">mu</option>
				</select>
			</div>
			
			
			<!-- 8. tol -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">tol</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_tol" value="0.0001">
			</div>
			
			
		</div>	
	</div>
	
	
	
	<div class="col-4 card" id="div_target">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
				<img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
			</dt>
		</dl>
		<div class="col container" style="padding:15px">
			<ul class="nav nav-tabs">
				<li class="nav-item">
					<a class="nav-link" data-toggle="tab" href="#trainResultAnals">모델 생성결과</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" data-toggle="tab" href="#trainResultData">데이터</a>
				</li>
			</ul>
			
			<div class="tab-content" id="tabContent">
				<div class="tab-pane fade gridTabPane" id="trainResultAnals">
					<div class="card p-2">
						<div id="resultGrid" style="width:100%;height:480px;"></div>
					</div>
				</div>
				<div class="tab-pane fade gridTabPane" id="trainResultData">
					<div class="card p-2">
						<div class="col">
							Predict
							<img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
							<span id="span_targetGrid_row_cnt" style="float:right;">0건</span>
						</div>
						<div id="targetGrid" style="width:100%;height:450px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
