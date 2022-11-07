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
				
				// 이미 훈련된 데이터가 있을 경우 화면 로딩시에 데이터를 그리드에 노출 
				if(data.xmldata.length > 0) { 
					var cols = [
						{caption : ['Attribute'],	ref : 'attr',		width : '40%',  style : 'text-align:left',		type : 'output'},
						{caption : ['Value'],		ref : 'value',		width : '60%',  style : 'text-align:right',		type : 'output'}
					];
					cfn_createGrid("resultGrid",data.xmldata,cols);
					cfn_setTargetImage(subPjtId,modelUid);
				}
				
				// penalty 옵션에 따라 div_l1_ratio show/hide 
				if(data.params.length > 0) {
					for(var i=0;i<data.params.length;i++){
						console.log(data.params[i].PARAM_ID,data.params[i].PARAM_VALUE);
						if(data.params[i].PARAM_ID=="select_penalty"){
							if(data.params[i].PARAM_VALUE=="elasticnet"){
								$('#div_l1_ratio').show();	
							}else{
								$('#div_l1_ratio').hide();	
							}
						}
					}
				} else {
					
					/*
					 * 기본옵션
					 * penalty : l2 -> l1_ratio hide
					 */
					$('#div_l1_ratio').hide();
				}
				
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
				for(var idx in controlParam.params) {
					var item = controlParam.params[idx];
					
					// 공백이거나 값이 없는 경우 파라미터에 추가하지 않음.
					if(item.value == "" || item.value == undefined || item.value == null) {
						continue;
					}
					
					trainParam.algparam[item.id.replace(/^(select_|input_)/, '')] = item.value;
				}
			}
			
			var algp = trainParam.algparam;
			
			if(algp.solver == "liblinear" && algp.multi_class == "multinomial") {
				alert("liblinear은 multinomial을 지원하지 않습니다.");
				return;
			}
			
			
			if(algp.dual == "true" && algp.solver != "liblinear") {
				alert("liblinear인 경우에만 dual을 true로 설정 가능합니다.");
				return;
			}
			
			
			if(algp.penalty == 'elasticnet' && algp.solver != 'saga') {
				alert('panalty = elasticnet은 solver가 saga인 경우에만 사용 가능합니다.');
				return;
			}
			
			if(algp.l1_ratio < 0 || 1 < algp.l1_ratio) {
				alert('l1_ratio의 범위는 0 ~ 1입니다.');
				return;
			}
			

			// ML Api 서버와 통신
			$.doPost({
				url : cv_apiAddr + "/logistic/train/",
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
		
		$('#select_penalty').change(function(e){
			var value = $(this).val();
			if(value=="elasticnet"){
				$('#div_l1_ratio').show();
			}else{
				$('#div_l1_ratio').hide();
			}
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
			
			<!-- 1. solver : solver -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Solver</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_solver">
					<option value="lbfgs">lbfgs</option>
					<option value="newton-cg">newton-cg</option>
					<option value="liblinear">liblinear</option>
					<option value="sag">sag</option>
					<option value="saga">saga</option>
				</select>
			</div>
			
			
			<!-- 2. penalty -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Penalty</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_penalty">
					<option value="l1">l1</option>
					<option value="l2" selected>l2</option>
					<option value="elasticnet">elasticnet</option>
					<option value="">None</option>
				</select>
			</div>
			
			<!-- 9. l1 ratio -->
			<div id="div_l1_ratio">
				<div class="card-title mb-0">
					<div class="row">
						<div class="col">
							<p class="h5 mb-1">
								<strong class="mliFrame-name">L1 Ratio</strong>
							</p>
						</div>
					</div>
				</div>		
				<div class="form-group">
					<input type="text" class="form-control-number" id="input_l1_ratio" value="1">
				</div>
			</div>			
			
			<!-- 3. multi_class -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Multi Class</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_multi_class">
					<option value="auto">auto</option>
					<option value="ovr">ovr</option>
					<option value="multinomial">multinomial</option>
				</select>
			</div>
			
			
			<!-- 4. class weight -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Class Weight</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_class_weight">
					<option value="">None</option>
					<option value="balanced">balanced</option>
				</select>
			</div>
			
			<!-- 5. tol -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Tol</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_tol" value="0.0001">
			</div>
			
			
			<!-- 6. C = 0.1 -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">C</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_C" value="1">
			</div>
			
			
			<!-- 7. intercept scaling -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Intercept Scaling</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<input type="text" class="form-control-number" id="input_intercept_scaling" value="1">
			</div>
			
			
			
			<!-- 8. dual -->
			<div class="card-title mb-0">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">Dual</strong>
						</p>
					</div>
				</div>
			</div>		
			<div class="form-group">
				<select class="form-control form-control-sm" data-width="200px" id="select_dual">
					<option value="true">True</option>		<!-- solver:liblinear 만 해당 -->
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
