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
			url : "/mliframe/getTableList.do",
			data : param,
			success : function(data){
				var list = data.data;
				for(var i=0;i<list.length;i++){
					$('#formControlObjectSelect1').append('<option value="'+list[i].TABLE_NAME+'">'+list[i].OBJ_NAME+'</option>');
				}
				cfn_setTargetGrid(subPjtId,modelUid);
				cfn_createControlPanel(data.params);
				cols = data.cols;
				$('#formControlObjectSelect1').change();
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#formControlObjectSelect1').change(function(e){
			$.doPost({
				url : "/mliframe/getTableColInfo.do",
				data : {
					TABLE_NAME : $(this).val()
				},
				success : function(data){
					cfn_createControlGrid(data.data);
					cfn_checkCol(cols);
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		});
		
		$('#dataSubmitBtn').click(function(e){
			var list = [];
			controlGrid = _SBGrid.getGrid("controlGrid");
			var chkData = controlGrid.getCheckedRowData(0);
			if(chkData.length==0){
				alert('칼럼을 체크해 주세요.');
				return;
			}
			for(var i=0;i<chkData.length;i++){
				list.push(chkData[i].data.COL_NAME);
			}
			var controlParam = cfn_getControlParam();
			
			var param = {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
				TABLE_NAME : $('#formControlObjectSelect1').val(),
				list : list,
				controlparam : controlParam
			}
			$.doPost({
				url : "/mliframe/getTableData.do",
				data : param,
				success : function(data){
					if(data.msg=="success"){
						cfn_setTargetGrid(subPjtId,modelUid);
						
						setTimeout(function(){
							var model = parent.graph.getCell(modelUid);
							targetGrid = _SBGrid.getGrid("targetGrid");
							var rowCnt = targetGrid.getGridDataAll().length;
							var tbNm = $('#formControlObjectSelect1 option:checked').text();
							var tableNm = (tbNm.length>15)?tbNm.substring(0,14):tbNm;
							var str = "[ Table ]\n"+tableNm;
								//str+='\n\n　　　　Rows : '+comma(rowCnt);
							model.attr('bodyText/text', str);
							model.attr('bodyText',{'font-size':12});
							parent.getSVG();	
						},1000);
					}else{
						alert('오류가 발생 했습니다.');
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		});
	});
</script>
<div class="row">
	<div id="div_control" class="col-5 card mr-30 l-20">
		<dl class="row pt-3 mb-0">
			<dt class="col-8">CONTROL</dt>
			<dd class="col-4" style="text-align: right;">
				<button id="dataSubmitBtn" type="button" class="btn btn-outline-success">실행 <i class="far fa-save"></i></button>
			</dd>
	  	</dl>
		<div class="card-title mb-1">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<i class="fas fa-search"></i> <strong class="mliFrame-name">테이블 선택</strong>
					</p>
				</div>
			</div>
		</div>
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="formControlObjectSelect1">
				<option value="notSelected">- 선택하십시오 -</option>
			</select>
		</div>
		<div id ="col-box" class="card">
			<div id="controlGrid" style="width:100%;height:410px;"></div>
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
