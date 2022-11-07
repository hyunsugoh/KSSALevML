<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var sourceIds = parent.sourceidStr;
	
	$(document).ready(function() {
		init();
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
			}
			trainParam.features = controlParam.fcols;
			console.log(trainParam);
			
			
			// label 정합성 확인 - 한개만 선택 
			if(controlParam.lcols.length == 0) {
				alert('Label 컬럼을 선택해 주세요.');
				return;
			} else if(controlParam.lcols.length > 1) {
				alert('Label 컬럼은 하나만 선택 가능합니다.');
				return;
			}
			trainParam.label = controlParam.lcols[0];
			
			
			if(controlParam.params.length > 0) {
				trainParam.algparam = new Object();
				var interaction_constraints = [];
				for(var idx in controlParam.params) {
					var item = controlParam.params[idx];
					
					// 공백이거나 값이 없는 경우 파라미터에 추가하지 않음.
					if(item.value == "" || item.value == undefined || item.value == null) {
						continue;
					}
					
					if(item.id==="input_loss_weights"){
						var value = $('#input_loss_weights').val().split(',').map(e => parseInt(e));
						trainParam.algparam.monotone_constraints = value;
					}else{
						trainParam.algparam[item.id.replace(/^(select_|input_)/, '')] = item.value;	
					}
				}
			}
			console.log(trainParam);
			
			// ML Api 서버와 통신
			$.doPost({
				url : cv_apiAddr + "/lstm/train/",
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
						alert('오류가 발생하였습니다.');
						return;
					}
				}
			});
			
		});
		
		
		$("#select_optimizer").change(function() {
			console.log($(this).val());
			$(".div_optimizer_opt").hide();
			$(".div_" + $(this).val()).show();
		});
	});
	
	
	function init() {
		$(".div_optimizer_opt").hide();
		$(".div_rmsprop").show();
	}
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
			
			<!-- 공통 -->
			<!-- 1. optimizer -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">optimizer</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_optimizer">
					<option value="sgd">sgd</option>
					<option value="adam">adam</option>
					<option value="adamax">adamax</option>
					<option value="nadam">nadam</option>
					<option value="rmsprop" selected>rmsprop</option>
				</select>
			</div>
			
			
			
			<!-- optimizer 별 파라미터 -->
			<!--  1. optimizer = sgd -->
			<div class="div_optimizer_opt div_sgd">
			
				<!-- 1-1. learning_rate  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">learning_rate</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_learning_rate" value="0.01">
				</div>
				
				
				<!-- 1-2. momentum  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">momentum</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_momentum" value="0">
				</div>
				
				
				<!-- 1-3. nesterov -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">nesterov</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<select class="form-control form-control-sm" data-width="200px" id="select_nesterov">
						<option value="true">True</option>
						<option value="false" selected>False</option>
					</select>
				</div>
			</div>
			
			
			<!--  2. optimizer = adam -->
			<div class="div_optimizer_opt div_adam">
			
				<!-- 1-1. learning_rate  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">learning_rate</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_learning_rate" value="0.001">
				</div>
				
				<!-- 1-2. beta_1  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">beta_1</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_beta_1" value="0.9">
				</div>
				
				<!-- 1-3. beta_2  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">beta_2</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_beta_2" value="0.999">
				</div>
				
				
				<!-- 1-4. epsilon  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">epsilon</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_epsilon" value="0.0000001">
				</div>
				
				
				<!-- 1-5. amsgrad -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">amsgrad</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<select class="form-control form-control-sm" data-width="200px" id="select_amsgrad">
						<option value="true">True</option>
						<option value="false" selected>False</option>
					</select>
				</div>
			</div>
			
			
			
			<!-- 3. optimizer = adamax && nadam -->
			<div class="div_optimizer_opt div_nadam div_adamax">
			
				<!-- 1-1. learning_rate  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">learning_rate</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_learning_rate" value="0.001">
				</div>
			
				<!-- 1-2. beta_1  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">beta_1</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_beta_1" value="0.9">
				</div>
				
				<!-- 1-3. beta_2  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">beta_2</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_beta_2" value="0.999">
				</div>
				
				
				<!-- 1-4. epsilon  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">epsilon</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_epsilon" value="0.0000001">
				</div>
			</div>
			
			
			
			<!-- 4. optimizer = rmsprop -->
			<div class="div_optimizer_opt div_rmsprop">
			
				<!-- 1-1. learning_rate  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">learning_rate</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_learning_rate" value="0.001">
				</div>
				
				<!-- 1-2. rho  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">rho</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_rho" value="0.9">
				</div>
				
				<!-- 1-3. momentum -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">momentum</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_momentum" value="0">
				</div>
				
				<!-- 1-4. epsilon  -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">epsilon</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_epsilon" value="0.0000001">
				</div>
				
				
				<!-- 1-5. centered -->
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">centered</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<select class="form-control form-control-sm" data-width="200px" id="select_centered">
						<option value="true">True</option>
						<option value="false" selected>False</option>
					</select>
				</div>
			</div>
			
			
			
			
			
			<!-- 2. loss_weights 배열 -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">loss_weights</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-text" id="input_loss_weights" />
			</div>
			
			
			<!-- 3. epochs -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">epochs</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_epochs" value="1" step="1">
			</div>
			
			
			<!-- 4. steps_per_epoch -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">steps_per_epoch</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_steps_per_epoch" value="40" step="1">
			</div>
			
			
			<!-- 5. shuffle -->
			<!-- <div class="card-title mb-0">
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
					<option value="true">True</option>
					<option value="false" selected>False</option>
				</select>
			</div> -->
			
			
			
			<!-- 6. use_bias -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">use_bias</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_use_bias">
					<option value="true">True</option>
					<option value="false" selected>False</option>
				</select>
			</div>
			
			
			
			<!-- 7. kernel_initializer -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">kernel_initializer</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_kernel_initializer">
					<option value="random_normal">random_normal</option>
					<option value="random_uniform">random_uniform</option>
					<option value="truncated_normal">truncated_normal</option>
					<option value="zeros">zeros</option>
					<option value="ones">ones</option>
					<option value="glorot_normal">glorot_normal</option>
					<option value="glorot_uniform" selected>glorot_uniform</option>
					<option value="orthogonal">orthogonal</option>
					<option value="variance_scaling">variance_scaling</option>
				</select>
			</div>
			
			
			<!-- 8. bias_initializer -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">bias_initializer</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_bias_initializer">
					<option value="random_normal">random_normal</option>
					<option value="random_uniform">random_uniform</option>
					<option value="truncated_normal">truncated_normal</option>
					<option value="zeros" selected>zeros</option>
					<option value="ones">ones</option>
					<option value="glorot_normal">glorot_normal</option>
					<option value="glorot_uniform">glorot_uniform</option>
					<option value="orthogonal">orthogonal</option>
					<option value="variance_scaling">variance_scaling</option>
				</select>
			</div>
			
			
			<!-- 9. dropout -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">dropout</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_dropout" value="0">
			</div>
			<!-- /공통 -->
			
			
			<!-- 10. n_past -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">n_past</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_n_past" value="1">
			</div>
			
			
			<!-- 11. n_batches -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">n_batches</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_n_batches" value="50">
			</div>
			
			
			<!-- 12. units -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">units</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_units" value="1">
			</div>
			
			
		
			<!-- 13. loss -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">loss</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_loss">
					<option value="mse" selected>mse</option>
					<option value="mae">mae</option>
					<option value="mape">mape</option>
					<option value="msle">msle</option>
					<option value="logcosh">logcosh</option>
				</select>
			</div>
			
			
			
			<!-- 14. activation -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">activation</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_activation">
					<option value="tanh" selected>tanh</option>
					<option value="sigmoid">sigmoid</option>
					<option value="relu">relu</option>
					<option value="softmax">softmax</option>
					<option value="softplus">softplus</option>
					<option value="elu">elu</option>
					<option value="selu">selu</option>
					<option value="exponential">exponential</option>
				</select>
			</div>
			
			
			<!-- 15. unit_forget_bias -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">unit_forget_bias</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_unit_forget_bias">
					<option value="true" selected>True</option>
					<option value="false">False</option>
				</select>
			</div>
			
			

			<!-- 16. recurrent_activation -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">recurrent_activation</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_recurrent_activation">
					<option value="tanh">tanh</option>
					<option value="sigmoid" selected>sigmoid</option>
					<option value="relu">relu</option>
					<option value="softmax">softmax</option>
					<option value="softplus">softplus</option>
					<option value="elu">elu</option>
					<option value="selu">selu</option>
					<option value="exponential">exponential</option>
				</select>
			</div>
			
			
		
			<!-- 17. recurrent_initializer -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">recurrent_initializer</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_recurrent_initializer">
					<option value="random_normal">random_normal</option>
					<option value="random_uniform">random_uniform</option>
					<option value="truncated_normal">truncated_normal</option>
					<option value="zeros">zeros</option>
					<option value="ones">ones</option>
					<option value="glorot_normal">eglorot_normallu</option>
					<option value="glorot_uniform">glorot_uniform</option>
					<option value="orthogonal" selected>orthogonal</option>
					<option value="variance_scaling">variance_scaling</option>
				</select>
			</div>
			
			
			
			<!-- 18. recurrent_dropout -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">recurrent_dropout</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_recurrent_dropout" value="0">
			</div>
			
			
			
			<!-- 19. return_sequences -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">return_sequences</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_return_sequences">
					<option value="true">True</option>
					<option value="false" selected>False</option>
				</select>
			</div>
			
			
			
			<!-- 20. return_state -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">return_state</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_return_state">
					<option value="true">True</option>
					<option value="false" selected>False</option>
				</select>
			</div>
			
			
			
			<!-- 21. stateful -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">stateful</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_stateful">
					<option value="true">True</option>
					<option value="false" selected>False</option>
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
