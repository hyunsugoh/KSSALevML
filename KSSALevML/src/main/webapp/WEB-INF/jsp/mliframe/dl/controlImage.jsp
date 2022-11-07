<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var cols = [];
	var inputFiles = new Array();

	$(document).ready(function() {
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid
		}
		
		/* control / target grid 초기화 */
		var controlGridCols = [
			//{caption:['이미지'],		ref:"img_path",  width:'20%', style : 'text-align:center',  type : 'image', typeinfo:{imagewidth:32, imageheight:32}},
			{caption:['파일명'],	ref:"file_nm",  width:'60%', style : 'text-align:left',  type : 'output'},
			{caption:['Y value'],	ref:"y", 		width:'20%', style : 'text-align:left',  type : 'output'}
		];
		var targetGridCols = [
			{caption:['파일명'],	ref:"file_nm",	width:'60%',	style:'text-align:left',	type : 'output',
					renderer : function(objGrid, nRow, nCol, strValue, objRowData) {
						return '<a href = "/filepath/' + subPjtId + '/upload/' + strValue + '" download>' + strValue + '</a>'
					}},
			{caption:['파일명'],	ref:"file_nm",	width:'60%',	style:'text-align:left',	type : 'output', hidden : true},
			{caption:['사이즈'],	ref:"size",		width:'20%',	style:'text-align:center',	type : 'output'},
			{caption:['Y value'],	ref:"y",		width:'20%',	style:'text-align:center',	type : 'output'}
		];
		
		var gridOpt = {
			rowheight : 40,
			//emptyimage : null
			//emptyimagestyle = 'width: 0px; height: 0px;';
		};
		
		cfn_createGrid("controlGrid","data",controlGridCols,gridOpt);
		cfn_createGrid("targetGrid","targetData",targetGridCols,gridOpt);
		
		
		
	 	$.doPost({
			url : "/mliframe/loadCSVTgt.do",
			data : param,
			success : function(data) {
				if(data.target_header.length>0){
					var gridData = [];
					
					
					 //데이터 가공
					 //   => 0 : ~~ -> header : ~~
					data.target_data.forEach(item => {
						var tmpData = {};
						var header = data.target_header;
						for(var i=0; i<header.length; i++) {
							tmpData[header[i]] = item[i];
						}
						gridData.push(tmpData);
					});
					
					targetGrid.setGridData(gridData);
					targetGrid.refresh();
					
					//appendLink(data.target_data);
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생했습니다.');
			}
		});
		
		
		$('#dataSubmitBtn').click(function(e){
			if(inputFiles.length == 0) {
				alert('파일을 선택해 주세요.');
				return;
			}
			
			controlGrid = _SBGrid.getGrid("controlGrid");
			var data = controlGrid.getGridDataAll();
			var list = [];
			for(var i=0;i<data.length;i++){
				if(typeof data[i].y=="undefined" || data[i].y.length==0){
					alert('Y값을 입력해 주세요.');
					return;
				}
				list.push(data[i].y);
			}
			
			var formData = new FormData();
			for(var i=0;i<inputFiles.length;i++){
				formData.append('files', inputFiles[i]);	
			}
			formData.append('subpjtid', subPjtId);
			formData.append('modeluid', modelUid);
			formData.append('list', list);
			formData.append('userid', _userid);
			
			$.ajax({
		        url: '/mliframe/uploadImage.do',
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
		        		var gridData = [];
		        		
		        		data.target_data.forEach(item => {
							var tmpData = {};
							var header = data.target_header;
							for(var i=0; i<header.length; i++) {
								tmpData[header[i]] = item[i];
							}
							gridData.push(tmpData);
						});
						
						targetGrid.setGridData(gridData);
						targetGrid.refresh();
						
						//appendLink(data.target_data);
						
					
		        		/* 파일 input 초기화 */
		        		inputFiles = [];

		        		
		        		/* 다이어그램 메세지 설정 */
		        		setTimeout(function(){
		        			
		        			// 업로드 된 전체 파일명 리스트
		        			var fileList = targetGrid.getGridDataAll().map(item => {
		        				return item.file_nm;
		        			});
							
		        			var lineHeight = 0;
							var str = "[ Images ]\n";
							for(var idx in fileList) {
								
								var fileNm = fileList[idx];
								var fileExt = fileNm.slice(fileNm.search(/\.(\w)*$/g), fileNm.length);
								
								fileNm = fileNm.length>15 ? fileNm.substring(0, 12) + '..' + fileExt : fileNm;
								str += fileNm + "\n";
								
								lineHeight += 30
								
								if(idx > 5) {
									str += "...\n";
									
									break;
								}
							}
							str += '\n\n　　　 Files : '+comma(fileList.length);
							
							var model = parent.graph.getCell(modelUid);
							model.size(150, lineHeight+50);
							model.attr('bodyText/text', str);
							model.attr('bodyText',{'font-size':12});
							parent.getSVG();
		        		},1000);
		        	}else{
		        		alert('오류가 발생했습니다.');
		        	}
		        },
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
		    });
		});
		
		$('#input_files').change(function(e){
			var yVal = $("#input_y").val();
			if(yVal) {
				var files = $('#input_files').prop('files');
				if(files.length > 0) {
					var data = [];
					for(var i=0;i<files.length;i++){
						var obj = {};
						obj.file_nm=files[i].name;
						obj.y=yVal;
						//obj.img_path='/filepath/'+subPjtId+'/upload/'+files[i].name;
						data.push(obj);
					}
					controlGrid.addRows(data);
					
					// 파일 정보 추가
					for(var i=0; i<files.length; i++) {
						inputFiles.push(files[i]);
					}	
				}
				
			} else {
				alert('y label을 입력해 주세요.');
				$(this).val(null);
				return null;
			}
		});
	});
	
	function dataToUrl(data) {
		return '<a href = "/filepath/' + subPjtId + '/upload/' + data + '" download>' + data + '</a>';
	}
	
	function appendLink(data) {
		var cell = $("#SBHE_DA_targetGrid tr td.sbgrid_cell[data-colindex='0']");
		
		for(var i=0; i<data.length; i++){
			var item = data[i][0];
			
			if($(cell[i]).data('rowindex') == i+1) {
				$(cell[i]).find('span').html('').append('<a href = "/filepath/' + subPjtId + '/upload/' + item + '" download>' + item + '</a>');
			}
		}
			
	}
</script>
<div class="row">
	<div class="col-5 card mr-30 l-20">
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
						<label class="mliFrame-name" id="label_yy"><strong>y label</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<input type="text" class="form-control-text" id="input_y" name="y" />
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
			<input name="input_files"  id="input_files"  type="file" aria-label="files" multiple/>
		</div>
		<div id ="col-box" class="card">
			<div id="controlGrid" style="width:100%;height:340px;"></div>
	   </div>
	</div>
   	<div class="col-6 card">
	   <dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
				<!-- <img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/> -->
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