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
			gubun	  : "ML"
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
				
				if(data.xmldata.length > 0) { 
					var cols = [
						{caption : ['Attribute'],	ref : 'attr',		width : '40%',  style : 'text-align:left',		type : 'output'},
						{caption : ['Value'],		ref : 'value',		width : '60%',  style : 'text-align:right',		type : 'output'}
					];
					cfn_createGrid("resultGrid",data.xmldata,cols);
					cfn_setTargetImage(subPjtId,modelUid);
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){

			var controlParam = cfn_getControlParamML();
			var trainParam = {
					subpjtid  		: subPjtId,
					targetid  		: modelUid,
					sourceid 		: sourceIds,
					
					features 		: null,					// feature grid에서 선택한 컬럼
					label	 		: null,					// label grid에서 선택한 컬럼
			};
			
			
			// feature 정합성 확인 - 한개이상 선택 필수
			if(controlParam.fcols.length == 0) {
				alert('Feature 컬럼을 선택해 주세요.');
				return;
			} else {
				trainParam.features = controlParam.fcols;
			}
			
			
			// label 정합성 확인 - 한개만 선택 
			if(controlParam.lcols.length == 0) {
				alert('Label 컬럼을 선택해 주세요.');
				return;
			} else if(controlParam.lcols.length > 1) {
				alert('Label 컬럼은 하나만 선택 가능합니다.');
				return;
			} else {
				trainParam.label = controlParam.lcols[0];
			}
			
			
			
			if(controlParam.params.length > 0) {
				trainParam.algparam = new Object();
				var interaction_constraints = [];
				for(var idx in controlParam.params) {
					var item = controlParam.params[idx];
					
					// 공백이거나 값이 없는 경우 파라미터에 추가하지 않음.
					if(item.value == "" || item.value == undefined || item.value == null) {
						continue;
					}
					
					if(item.id==="input_monotone_constraints"){
						var value = $('#input_monotone_constraints').val().split(',').map(e => parseInt(e));
						trainParam.algparam.monotone_constraints = value;
					}else if(item.id==="input_interaction_constraints_0"){
						interaction_constraints.push($('#input_interaction_constraints_0').val().split(',').map(e => parseInt(e)));
					}else if(item.id==="input_interaction_constraints_1"){
						interaction_constraints.push($('#input_interaction_constraints_1').val().split(',').map(e => parseInt(e)));
						trainParam.algparam.interaction_constraints = interaction_constraints;
					}else{
						trainParam.algparam[item.id.replace(/^(select_|input_)/, '')] = item.value;	
					}
				}
			}
			console.log(trainParam);
	
			var algp = trainParam.algparam;
			if(algp.subsample < 0 || 1 < algp.subsample){
				alert('subsample은 0과 1 사이의 수만 가능합니다.');
				return;
			}
	
			// ML Api 서버와 통신
			$.doPost({
				url : cv_apiAddr + "/xgbr/train/",
				crossOrigin : true,
				data : trainParam,
				success : function(data){
					console.log(data);
					if(data.status === "success") {
						console.log('success');
						var controlParam = cfn_getControlParamML(true);
						cfn_postTrainModel(subPjtId,modelUid,sourceIds,controlParam);
						cfn_setTargetImage(subPjtId,modelUid);
					} else {
						alert('오류가 발생하였습니다.\n' + data.message);
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
			
			
			<!-- 알고리즘 별 파라미터 -->
			<!-- 1. max_depth -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Max Depth</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_max_depth" value="6" step="1">
			</div>
			
			
			
			
			<!-- 2. learning_rate -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Learning Rate</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_learning_rate" value="0.3">
			</div>
			
			
			
			<!-- 3. objective -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">objective</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_objective">
					<option value="reg:squarederror">reg:squarederror</option>
					<option value="reg:squaredlogerror">reg:squaredlogerror</option>
					<option value="reg:logistic">reg:logistic</option>
					<option value="reg:pseudohubererror">reg:pseudohubererror</option>
				</select>
			</div>
			
			
			
			<!-- 4. booster -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">booster</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_booster">
					<option value="gbtree">gbtree</option>
					<option value="gblinear">gblinear</option>
					<option value="dart">dart</option>
				</select>
			</div>
			
			
			<!-- 5. tree_method -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">tree_method</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_tree_method">
					<option value="auto">auto</option>
					<option value="exact">exact</option>
					<option value="approx">approx</option>
					<option value="hist">hist</option>
					<option value="gpu_hist">gpu_hist</option>
				</select>
			</div>
			
			
			
			<!-- 6. gamma -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">gamma</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_gamma" value="0">
			</div>
			
			
			
			<!-- 7. min_child_weight -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">min_child_weight</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_min_child_weight" value="1">
			</div>
			
			
			<!-- 8. max_delta_step-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">max_delta_step</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_max_delta_step" value="0" step="1">
			</div>
			
			
			
			<!-- 9. subsample-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">subsample</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_subsample" value="1">
			</div>
			
			
			<!-- 10. colsample_bytree-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">colsample_bytree</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_colsample_bytree" value="1">
			</div>
			
			
			
			<!-- 11. colsample_bylevel-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">colsample_bylevel</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_colsample_bylevel" value="1">
			</div>
			
			
			<!-- 12. colsample_bynode-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">colsample_bynode
							</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_colsample_bynode" value="1">
			</div>
		
			
			
			<!-- 13. reg_alpha-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">reg_alpha</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_reg_alpha" value="0">
			</div>
			
			
			<!-- 14. reg_lambda-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">reg_lambda</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_reg_lambda" value="1">
			</div>
			
			
			
			<!-- 15. scale_pos_weight-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">scale_pos_weight</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_scale_pos_weight" value="1">
			</div>
			
			
			
			<!-- 16. base_score-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">base_score</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_scale_base_score" value="0.5">
			</div>
			
			
			
			<!-- 17. num_parallel_tree-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">num_parallel_tree</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_scale_num_parallel_tree" value="1" step="1">
			</div>
			
			
			
			<!-- 18. monotone_constraints -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">monotone_constraints</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-text" id="input_monotone_constraints" />
			</div>
			
			
			<!-- 19. interaction_constraints-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">interaction_constraints</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-text" id="input_interaction_constraints_0" style="float:left;width:49%;" />
				<input type="text" class="form-control-text" id="input_interaction_constraints_1" style="float:right;width:49%;" />
			</div>
			
			
			<!-- 20. importance_type-->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">importance_type</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_importance_type">
					<option value="gain">gain</option>
					<option value="weight">weight</option>
					<option value="cover">cover</option>
					<option value="total_gain">total_gain</option>
					<option value="total_cover">total_cover</option>
				</select>
			</div>
			
			
		</div>	
	</div>
	
	
	
	<div class="col-4 card" id="div_target">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
			</dt>
		</dl>
		<div class="col container" style="padding:15px">
			<ul class="nav nav-tabs">
				<li class="nav-item">
					<a class="nav-link active" data-toggle="tab" href="#modelResult">모델 생성결과</a>
				</li>
				<li class="nav-item">
					<a class="nav-link" data-toggle="tab" href="#visualization">시각화</a>
				</li>
			</ul>
			
			<div class="tab-content" id="tabContent">
				<div class="tab-pane fade show active" id="modelResult">
					<div class="card p-2">
						<div id="resultGrid" style="width:100%;height:480px;"></div>
					</div>
				</div>
				<div class="tab-pane fade" id="visualization">
					<img id="img_model_visualization" style="width:100%;height: 300px;"/>
				</div>
			</div>
		</div>
	</div>
</div>
