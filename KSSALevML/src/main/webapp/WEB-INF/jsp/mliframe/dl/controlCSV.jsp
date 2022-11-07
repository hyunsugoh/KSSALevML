<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var cols = [];

	$(document).ready(function() {
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid
		}
		$.doPost({
			url : "/mliframe/getCSVMeta.do",
			data : param,
			success : function(data){
				if(typeof data.params!="undefined"){
					cols = data.cols;
					cfn_createControlGrid(data.data);
					cfn_checkCol(cols);
					$('#a_file').text(data.params[0].PARAM_VALUE);
					$('#a_file').attr('href',"/filepath/"+subPjtId+"/upload/"+data.params[0].PARAM_VALUE);	
				}
				cfn_setTargetGrid(subPjtId,modelUid);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			var list = [];
			controlGrid = _SBGrid.getGrid("controlGrid");
			var chkData = controlGrid.getCheckedRowData(0);
			if(chkData.length==0){
				alert('칼럼을 체크해 주세요.');
				return;
			}
			
			var cols = [];
			for(var i=0;i<chkData.length;i++){
				list.push(chkData[i].rownum-1);
				cols.push(chkData[i].data.COL_NAME);
			}
			
			var files = $('#input_files').prop('files')[0];
			if(typeof files=="undefined"){
				alert('파일을 선택해 주세요.');
				return;
			}
			
			var formData = new FormData();
			formData.append('files', files);
			formData.append('subpjtid', subPjtId);
			formData.append('modeluid', modelUid);
			formData.append('list', list);
			formData.append('cols', cols);
			formData.append('userid', _userid);
			
			$.ajax({
		        url: '/mliframe/uploadCSV.do',
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
		        	if(data.msg=="success"){
		        		cfn_setTargetGrid(subPjtId,modelUid);
		        		setTimeout(function(){
			        		var model = parent.graph.getCell(modelUid);
							targetGrid = _SBGrid.getGrid("targetGrid");
							var rowCnt = targetGrid.getGridDataAll().length;
							var fileNm = (files.name.length>15)?files.name.substring(0,12)+'...csv':files.name;
							var str = "[ File ]\n"+fileNm;
								str+='\n\n　　　　Rows : '+comma(rowCnt);
							model.attr('bodyText/text', str);
							model.attr('bodyText',{'font-size':12});
							parent.getSVG();
		        		},1000);
		        	}else{
		        		alert('오류가 발생했습니다.');
		        	}
		        }
		    });
		});
		
		$('#input_files').change(function(e){
			var files = $('#input_files').prop('files')[0];
			var formData = new FormData();
			formData.append('files', files);
			
		    $.ajax({
		        url: '/mliframe/getCSVInfo.do',
		        type: 'POST',
		        data: formData,
		        processData: false,
		        contentType: false,
		        success: function (data) {
		        	cfn_createControlGrid(data.data);
		        	cfn_checkCol(cols);
		        }
		    });
		});
	});
</script>
<div class="row">
	<div class="col-5 card mr-30 l-20">
		<dl class="row pt-3 mb-0">
			<dt class="col-8">CONTROL</dt>
			<dd class="col-4" style="text-align: right;">
				<button id="dataSubmitBtn" type="button" class="btn btn-outline-success">실행 <i class="far fa-save"></i></button>
			</dd>
	  	</dl>
	  	<div class="form-group">
			<span>Uploaded File : <a id="a_file" target="_blank"></a></span>
		</div>
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
		<div id ="col-box" class="card">
			<div id="controlGrid" style="width:100%;height:380px;"></div>
	   </div>
	</div>
   	<div class="col-6 card">
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
