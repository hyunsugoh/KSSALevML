<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script type="text/javascript">
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var sourceIds = parent.sourceidStr;
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var cols = [];
	var inputFiles = new Array();
	
	$(document).ready(function() {
		
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid,
			sourceuid : sourceIds
		};
		
		/* control / target grid 초기화 */
		var controlGridCols = [
			{caption:['파일명'],	ref:"file_nm",	width:'60%',	style:'text-align:left',	type : 'output',
						renderer : function(objGrid, nRow, nCol, strValue, objRowData) {
							return '<a href = "/filepath/' + subPjtId + '/upload/' + strValue + '" download>' + strValue + '</a>'
						}},
			{caption:['파일명'],	ref:"file_nm",	width:'60%',	style:'text-align:left',	type : 'output', hidden : true},
			{caption:['사이즈'],	ref:"size",		width:'20%',	style:'text-align:center',	type : 'output'},
			{caption:['Y value'],	ref:"y",		width:'20%',	style:'text-align:center',	type : 'output'}
		];
		
		/* 그리드 옵션 */
		var gridOpt = {
			rowheight : 40
		};
		
		// controlGrid 초기화
		cfn_createGrid("controlGrid","data",controlGridCols,gridOpt);
		
		// targetGrid 초기화
		//cfn_setTargetGrid(subPjtId,modelUid);
		
		// controlGrid Data load
		$.doPost({
			url : "/mliframe/loadCSVTgt.do",
			data : {
				subpjtid  : subPjtId,
				modeluid  : sourceIds,
			},
			success : function(data) {
				if(data.target_header.length>0) {
					var gridData = [];
					var header = data.target_header;
					
					data.target_data.forEach(item => {
						var tmpData = {};
						
						for(var i=0; i<header.length; i++) {
							//if(header[i] != "file_nm") {
								tmpData[header[i]] = item[i];
							//}
						}
						gridData.push(tmpData);
					});
					
					controlGrid.setGridData(gridData);
					controlGrid.refresh();
					
					//appendLink(data.target_data);
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생했습니다.');
			}
		});
		
		// targetGrid Data load
		//   - 컬럼이 너무 많아 로드시 페이지 다운 됨 (결과를 표시할 다른 방안이 필요함.)
	/* 	$.doPost({
			url : "/mliframe/loadCSVTgt.do",
			data : {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
			},
			success : function(data) {
				//cfn_setDataGrid(subPjtId,modelUid,sourceIds,data.params);
				//cfn_createControlGrid(data.cols);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생했습니다.');
			}
		}); */
		 
		
		 
		$("#dataSubmitBtn").click(function() {
		
			var params = {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
				userId : _userid,
				data : controlGrid.getGridDataAll()
			};
			
			/* 이미지 파일 사이즈 동일한지 확인 필요 */
			for(var i=1; i<params.data.length; i++) {
				if(params.data[0].size != params.data[i].size) {
					alert('이미지 사이즈가 모두 동일해야 합니다.');
					return;
				}
			}
			
			$.doPost({
				url : "/mliframe/convertImageToPixel.do",
				data : params,
				success : function(data) {
					console.log(data);
					if(data.status === "success") {		
						
						setTimeout(function(){
							var str = "[Image To Vector]\n";
							str += "Encoded " + (params.data.length) + " files";
							
							var model = parent.graph.getCell(modelUid);
							//model.size(150, lineHeight+50);
							model.attr('bodyText/text', str);
							model.attr('bodyText',{'font-size':12});
							parent.getSVG();
						}, 1000);
						
						alert("변환이 완료 되었습니다.");
					} else {
						alert(data.msg);
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생했습니다.');
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
	  	
	  	<div class="col-12 p-0">
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<div id="controlGrid" style="width:100%;height:500px;"></div>
					</div>
				</div>			
			</div>
		</div>
	</div>
	
	
	<div class="col-6 card">
	   <dl class="row pt-3 mb-0">
			<dt class="col-12">
				Result
				<img id="img_targetGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
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