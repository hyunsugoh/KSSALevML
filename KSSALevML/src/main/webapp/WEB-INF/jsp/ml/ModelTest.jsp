<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<script type="text/javascript">
$(document).ready(function() {

	// 전체 저장된 모델 가져오기
	$.doPost({
		url : "/mlDeploy/getDeployList.do",
		data : {},
		success : function(data){
			console.log(data);
			var list = data.list;
			
			for(var i=0;i<list.length;i++){
				$('#select_deploy_model').append('<option value="'+list[i].SUB_PJT_ID+'/'+list[i].DEPLOY_ID+'">'+list[i].DEPLOY_NAME+'</option>');
			}
		}
	});
	
	
	// 전체 테이블 리스트 가져오기
	$.doPost({
		url : "/mliframe/getTableList.do",
		data : {},
		success : function(data){
			var list = data.data;
			for(var i=0;i<list.length;i++){
				$('#select_data').append('<option value="'+list[i].TABLE_NAME+'">'+list[i].OBJ_NAME+'</option>');
			}
		},
		error : function(jqxXHR, textStatus, errorThrown){
			alert('오류가 발생 했습니다.');
		}
	});
	
	
	// 파일 불러오는 부분 hide - 초기화 
	$("#div_input_file").hide();
	
	// 테스트 데이터 선택 변경 이벤트
	$("#select_data_type").on('change', function() {
		var val = $("#select_data_type option:selected").val();
		console.log('change val : ' + val);
		
		if(val=="data") {
			$("#div_input_data").show();
			$("#div_input_file").hide();
			
			$("#btn_reselect_column").removeClass("invisible");
		} else if (val == "file") {
			$("#div_input_data").hide();
			$("#div_input_file").show();
			
			$("#btn_reselect_column").attr("disabled", "").addClass("invisible");
		}
	});
	
	// 파일 선택 시 csv 불러오기
	$("#input_file").change(function() {
		console.log($(this).val());
	});
	
	
	// 테이블 선택 시 테이블 컬럼 불러오기
	$("#select_data").change(function() {
		$("#modal_select_column").modal('show');
		
		$.doPost({
			url : "/mliframe/getTableColInfo.do",
			data : { TABLE_NAME : $(this).val() },
			success : function(data){
				console.log("before createControlGrid :: ", data);
				cfn_createControlGrid(data.data);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
	});
	
	
	// 테이블 컬럼 재선택 버튼
	$("#btn_reselect_column").click(function(){
		$("#modal_select_column").modal('show');
	});
	
	
	// 모달에서 불러오기 버튼 클릭 시 테이블 데이터 불러오기 
	$("#btn_load_columns").click(function() {
		var list = [];
		var controlGrid = _SBGrid.getGrid("controlGrid");
		var chkData = controlGrid.getCheckedRowData(0);
		// check 박스 미생성문제로 테스트를 위해 임시로 막아둠.
		/* if(chkData.length==0){
			alert('칼럼을 체크해 주세요.');
			return;
		} */
		for(var i=0;i<chkData.length;i++){
			list.push(chkData[i].data.COL_NAME);
		}
		
		var param = {
				// check 박스 미생성문제로 테스트를 위해 임시로 막아둠.
				//TABLE_NAME : $('#formControlObjectSelect1').val(),
				//list : list,
				TABLE_NAME : "M_DATA_KR_SIM",
				list : ["ATC_CD", "ATC_NM"]
			}
		
		$.doPost({
			url : "/mlTest/getTableData.do",
			data : param,
			success : function(data){
				console.log(data);
				
				if(data.msg=="success"){
					cfn_createCommonDataGrid("sourceGrid", data.header, data.data);
					
					$("#modal_select_column").modal('hide');
					$("#btn_reselect_column").removeAttr('disabled');
				}else{
					alert('오류가 발생 했습니다.');
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
	});
	
	
	
	$("#btn_exe_test").click(function() {
		
		var testModel = $("#select_deploy_model").val();
		var testType = $("#select_data_type").val();
		
		// 테스트 데이터를 csv로 선택한 경우
		if(testType == "file") {
			
			var files = $('#input_file').prop('files')[0];
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
			
			$.ajax({
		        url: "/csv/"+testModel+".do",
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
		} 
		// 테스트 데이터를 테이블로 선택한 경우 
		else if(testType == "data"){
			var sourceGrid = _SBGrid.getGrid("sourceGrid");
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
			
			$.doPost({
				url : "/data/"+testModel+".do",
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
});
</script>
<style>
.sbgrid_common input[type=checkbox] {
	width: 15px ;
    height: 15px;
    border: 1px solid #655f5f;
    display: block;
    position: relative;
    margin-left: 40px;
}
</style>
<body>
	<div class="col mt-4">
		<div class="row mt-4">
			<div class="h5 align-middle">
				<i class="far fa-object-group p-2"></i><span>Model 테스트</span>
			</div>
		</div>
		<div class="row justify-content-end">
			<!-- 1. Test할 모델 - Dropdown -->
			<div class="form-group col-3">
				<div class="form-inline">
					<span class="col-4">테스트 모델</span>
				</div>
				<select class="form-control mt-3" id="select_deploy_model">
					<option value="">모델을 선택하세요.</option>
				</select>
			</div>
			
			<!-- 2. Test할 데이터 소스 - 라디오버튼 Table/CSV 선택 가능하도록 -->
			<div class="form-group col-3">
				<div class="form-inline p-0">
					<span class="col-4">테스트 데이터</span>
					<select class="form-control form-control-sm col-8" id="select_data_type">
						<option value="data">데이터</option>
						<option value="file">CSV</option>
					</select>
				</div>
				
				
				
				<div class="form-group mt-2" id="div_input_data">
					<select class="form-control" id="select_data">
						<option value="">데이터를 선택하세요.</option>
					</select>
				</div>
					
				<div class="form-group mt-2" id="div_input_file" >
					<input name="input_files"  id="input_file"  type="file" aria-label="files" />
				</div>
				
			</div>
			
			
			<!-- 3. API Key -->
			<div class="form-group col-3">
				<div class="form-inline">
					<span class="col-4 required" for="input_api_key">API Key</span>
				</div>
				<input class="form-control mt-3" id="input_api_key"></input>
			</div>
			
			<!-- 실행버튼 -->
			<div class="form-group mr-4">
				<button type="button" id="btn_exe_test" class="btn btn-outline-success mt-auto">실행</button>
			</div>
		</div>
		<div class="row">
			<!-- Source Data -->
			<div class="col-6 pr-2">
				<div class="card m-2 p-2">
					<dl class="row pt-3 pr-3 pl-3 mb-0">
						<dt class="col-12">
							Source
							<button type="button" id="btn_reselect_column" class="btn btn-outline-success btn-sm mr-2 ml-2" disabled>컬럼 재선택</button>
							<!-- <img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/> -->
							<span id="span_sourceGrid_row_cnt" style="float:right;">0건</span>
						</dt>
				  	</dl>
				  	<div class="col-12 p-0">
						<div class="card m-2 p-2">
							<div class="row">
								<div class="col-12">
									<div id="sourceGrid" style="width:100%;height:600px;"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			
			
			<!-- Predict Data -->
			<div class="col-6 pl-2">
				<div class="card m-2 p-2">
					<dl class="row pt-3 pr-3 pl-3 mb-0">
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
									<div id="targetGrid" style="width:100%;height:600px;"></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	
	<!-- 테이블 컬럼 선택 modal -->
	<div class="modal" id="modal_select_column">
		<div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
			<div class="modal-content">
				<div class="modal-header">
					<div class="h5 modal-title">테이블 컬럼 선택</div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
			
				<div class="modal-body">
					<div class="card p-2">
						<div id="controlGrid" style="width:100%; height:500px"></div>
					</div>
				</div>
				
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary" data-dismiss="modal">취소</button>
					<button type="button" class="btn btn-primary" id="btn_load_columns">불러오기</button>
				</div>
			</div>
		</div>
	</div>
</body>
