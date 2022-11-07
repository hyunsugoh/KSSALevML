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
				cfn_createControlPanel(data.params);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			var uuid = modelUid;
			var modelid = "";
			var modelnm = "";
			var list = [];
			var flag = true;
			var cnt = 1;
			while(flag){
				var model = parent.graph.getCell(uuid);
				var sCell = parent.graph.getNeighbors(model,{inbound:true});
				for(var i=0;i<sCell.length;i++){
					if(sCell[i].attributes.mid=="dm005"){
						uuid=sCell[i].attributes.id;
					}else{
						if(sCell[i].attributes.mid.indexOf('dl')>=0){
							flag=false;
							break;
						}else if(sCell[i].attributes.mid.indexOf('tr')>=0){
							modelid=sCell[i].attributes.id;
							modelnm=sCell[i].attributes.attrs.headerText.text.split('\n').join(' ').replace(' Train','');
						}
						uuid=sCell[i].attributes.id;
						var obj = {};
						obj.subpjtid=subPjtId;
						obj.deployid=modelUid;
						obj.id=uuid;
						obj.mid=sCell[i].attributes.mid;
						list.unshift(obj);
					}
				}
			}
			
			for(var i=0;i<list.length;i++){
				list[i].ordno=parseInt(i)+1;
			}
			
			if(modelid==""){
				alert('모델이 없습니다.');
				return;
			}
			
			var deployType = $('#select_deploy_type').val();
			var deployNm = $('#input_deploy_title').val();
			var deployDesc = $('#input_deploy_desc').val();
			var apiKey = $('#input_api_key').val();
			
			if(apiKey.length==0){
				alert('API Key를 입력해 주세요.');
				return;
			}
			
			var controlParam = cfn_getControlParam();
			
			var param = {
				list  	 : list,
				subpjtid : subPjtId,
				deployid : modelUid,
				modeluid : modelUid,
				modelid  : modelid,
				modelnm  : modelnm,
				deploytype : deployType,
				deploynm   : deployNm,
				deploydesc : deployDesc,
				apikey	   : apiKey,
				controlparam : controlParam
			};
			console.log("------------param------------");
			console.log(param);
			$.doPost({
				url : "/mlDeploy/deploy.do",
				data : param,
				success : function(data){
					if(data.msg=="success"){
						$('#input_file_deploy_url').val(cv_apiAddr+"/csv/"+subPjtId+"/"+modelUid+".do");
						$('#input_data_deploy_url').val(cv_apiAddr+"/data/"+subPjtId+"/"+modelUid+".do");
						
						var model = parent.graph.getCell(modelUid);
						var str = [];
						str.push('[ Deploy Name ]');
						str.push(deployNm);
						str.push('[ API Key ]');
						str.push(apiKey);
						model.size(150, 100);
						model.attr('bodyText/text', str.join('\n'));
						model.attr('bodyText',{'font-size':12});
						parent.getSVG();
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		});
		
		$('#testBtn').click(function(e){
			var files = $('#input_files').prop('files')[0];
			if(typeof files=="undefined"){
				alert('파일을 선택해 주세요.');
				return;
			}
			
			var formData = new FormData();
			formData.append('source_file', files);
			formData.append('api_key', "test123");
			
			var url = "/csv/"+subPjtId+"/"+modelUid+".do";
			
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
		        		console.log(data);
		        	}else{
		        		alert(data.message);
		        	}
		        }
		    });
		});
	});
</script>
<div class="row">
	<div class="col-5 card mr-30 l-20" id="div_control">
		<dl class="row pt-3 mb-0">
			<dt class="col-8">CONTROL</dt>
			<dd class="col-4" style="text-align: right;">
				<button id="dataSubmitBtn" type="button" class="btn btn-outline-success">실행 <i class="far fa-save"></i></button>
			</dd>
	  	</dl>
	  	<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name required">Deploy Type</strong>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_deploy_type">
				<option value="PR">Predict Only</option>
				<option value="TRPR">Train and Predict</option>
			</select>
		</div>
		<div class="card-title mb-1">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name required">Deploy Name</strong>
					</p>
				</div>
			</div>
		</div>
		<div class="form-group">
			<input name="deploy_title"  id="input_deploy_title"  type="text" class="form-control-text" />
		</div>
		<div class="card-title mb-1">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name">Description</strong>
					</p>
				</div>
			</div>
		</div>
		<div class="form-group">
			<input name="deploy_desc"  id="input_deploy_desc"  type="text" class="form-control-text" />
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
	</div>
   	<div class="col-6 card">
	   <dl class="row pt-3 mb-0">
			<dt class="col-8">Result</dt>
			<dd class="col-4" style="text-align: right;">
				<!-- <button id="testBtn" type="button" class="btn btn-outline-success">테스트</button> -->
			</dd>
	  	</dl>
		<div class="col-12 p-0">
			<div class="card-title mb-1">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">배포 URL(File Upload)</strong>
						</p>
					</div>
				</div>
			</div>
			<div class="form-group">
				<input name="file_deploy_url"  id="input_file_deploy_url"  type="text" class="form-control-text"/>
			</div>
			<div class="card-title mb-1">
				<div class="row">
					<div class="col">
						<p class="h5 mb-1">
							<strong class="mliFrame-name">배포 URL(Data Upload)</strong>
						</p>
					</div>
				</div>
			</div>
			<div class="form-group">
				<input name="data_deploy_url"  id="input_data_deploy_url"  type="text" class="form-control-text" />
			</div>
			<!-- 
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
			</div> -->
		</div>
	</div>
</div>
