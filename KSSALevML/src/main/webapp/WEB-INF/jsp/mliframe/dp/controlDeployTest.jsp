<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var sourceIds = parent.sourceidStr;
	var cols = [];
	
	$(document).ready(function() {
		sourceIds=cfn_getSourceList(sourceIds);
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid,
			sourceuid : sourceIds
		};
		$.doPost({
			url : "/mlDeploy/getDeployList.do",
			data : param,
			success : function(data){
				var list = data.list;
				for(var i=0;i<list.length;i++){
					$('#select_deploy_name').append('<option value="'+list[i].SUB_PJT_ID+'/'+list[i].DEPLOY_ID+'">'+list[i].DEPLOY_NAME+'</option>');
				}
				$.doPost({
					url : "/mliframe/getControlInfo.do",
					data : param,
					success : function(data){
						cfn_setDataGrid(subPjtId,modelUid,sourceIds,data.params);
					},
					error : function(jqxXHR, textStatus, errorThrown){
						alert('오류가 발생 했습니다.');
					}
				});
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			var dpType = $('#select_deploy_type').val();
			var dpValue = $('#select_deploy_name').val();
			if(dpType=="file"){
				var files = $('#input_files').prop('files')[0];
				if(typeof files=="undefined"){
					alert('파일을 선택해 주세요.');
					return;
				}
				var apiKey = $('#input_api_key').val();
				if(apiKey.length==0){
					alert('API Key를 입력해 주세요.');
					return;
				}
				var formData = new FormData();
				formData.append('source_file', files);
				formData.append('api_key', apiKey);
				formData.append('userid', _userid);
				
				var url = "/csv/"+dpValue+".do";
				$.ajax({
			        url: url,
			        type: 'POST',
			        data: formData,
			        processData: false,
			        contentType: false,
			        beforeSend : function(){
						$('#div_loading').append('<img src="/images/common/icons/loading.gif" style="width:50px;"/>');
					},
					complete : function(){
						$('#div_loading').empty();
					},
			        success: function (data) {
			        	if(data.status=="success"){
							cfn_createCommonDataGrid("targetGrid",data.header,data.data);
						}else{
							alert(data.message);
						}
			        }
			    });
			}else{
				var url = "/data/"+dpValue+".do";
				sourceGrid = _SBGrid.getGrid("sourceGrid");
				var data = sourceGrid.getGridDataAll();
				var header = [];
				var columns = sourceGrid.getColumns();
				for(var i=0;i<columns.length;i++){
					header.push(columns[i].caption[0]);
				}
				var apiKey = $('#input_api_key').val();
				if(apiKey.length==0){
					alert('API Key를 입력해 주세요.');
					return;
				}
				var param = {
					api_key	  : apiKey,
					data	  : data,
					header	  : header
				};
				
				console.log(param);
				
				$.doPost({
					url : url,
					data : param,
					success : function(data){
						if(data.status=="success"){
							cfn_createCommonDataGrid("targetGrid",data.header,data.data);
						}else{
							alert(data.message);
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						alert('오류가 발생 했습니다.');
					}
				});
			}
		});
		
		$('#select_deploy_type').change(function(e){
			var value = $(this).val();
			if(value=="file"){
				$('#div_deploy_file_form').show();
			}else{
				$('#div_deploy_file_form').hide();
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
	  	<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name required">Select Deploy Name</strong>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_deploy_name">
			</select>
		</div>
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name required">Select Deploy Type</strong>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_deploy_type">
				<option value="data">Data Upload</option>
				<option value="file">CSV File</option>
			</select>
		</div>
		<div class="card-title mb-1">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name required">API Key</strong>
					</p>
				</div>
			</div>
		</div>
		<div class="form-group">
			<input name="api_key"  id="input_api_key"  type="text" class="form-control-text" />
		</div>
		<div id="div_deploy_file_form" style="display:none;">
			<div class="card-title mb-1">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<i class="fas fa-search"></i> <strong class="mliFrame-name">파일 선택</strong>
						</p>
					</div>
				</div>
			</div>
			<div class="form-group">
				<input name="input_files"  id="input_files"  type="file" aria-label="files" />
			</div>
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
