<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var sourceIds = parent.sourceidStr;
	var tHeaders = [];
	
	$(document).ready(function() {
		var sourceIdArr = sourceIds.split("|");
		for(var i=0;i<sourceIdArr.length;i++){
			var model = parent.graph.getCell(sourceIdArr[i]);
			var mid = model.attributes.mid;
			if(mid=="dm005"){
				if(i==0){
					var sDiv = '<div class="card-title mb-0 pt-2"><div class="row" style="height:40px;"><div class="col"><div class="form-group">';
			    		sDiv+= '<select class="form-control form-control-sm" data-width="200px" id="select_left_source"></select></div></div></div></div>';
					$('#div_left_source').prepend(sDiv);
					$('#select_left_source').append('<option value="'+sourceIdArr[i]+'_tr">Train</option>');
					$('#select_left_source').append('<option value="'+sourceIdArr[i]+'_te">Test</option>');
					$('#select_left_source').on('change',function(e){
						var sid = $(this).val();
						sourceIdArr[0]=sid;
						cfn_setSourceGrid(subPjtId,sid,"leftGrid");
					});
					$('#leftGrid').css('height','150px');
				}else{
					var sDiv = '<div class="card-title mb-0 pt-2"><div class="row" style="height:40px;"><div class="col"><div class="form-group">';
		    			sDiv+= '<select class="form-control form-control-sm" data-width="200px" id="select_right_source"></select></div></div></div></div>';
					$('#div_right_source').prepend(sDiv);
					$('#select_right_source').append('<option value="'+sourceIdArr[i]+'_tr">Train</option>');
					$('#select_right_source').append('<option value="'+sourceIdArr[i]+'_te">Test</option>');
					$('#select_right_source').on('change',function(e){
						var sid = $(this).val();
						sourceIdArr[1]=sid;
						cfn_setSourceGrid(subPjtId,sid,"rightGrid");
					});
					$('#rightGrid').css('height','150px');
				}
				sourceIdArr[i]+="_tr";
			}
		}
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid,
			sourceids : sourceIdArr
		};
		$.doPost({
			url : "/mliframe/loadCSVMultiSrcTgt.do",
			data : param,
			success : function(data){
				if(data.source_headers.length>0){
					cfn_createCommonDataGrid("leftGrid",data.source_headers[0],data.source_datas[0]);	
					
					for(var i=0;i<data.source_headers[0].length;i++){
						tHeaders.push(data.source_headers[0][i]+"_left");
					}
					
					var list = [];
					var cols = [
						{caption : ['선택'],			ref : 'SEL_CHK',		width : '20%',  style : 'text-align:center',	type : 'checkbox',	typeinfo : {checkedvalue : 'Y', uncheckedvalue : 'N'}},
						{caption : ['칼럼명'],		ref : 'COL_NAME',		width : '80%',  style : 'text-align:left',		type : 'output'}
					];
					for(var j=0;j<data.source_headers[0].length;j++){
						var map = {};
						map.COL_NAME=data.source_headers[0][j];
						list.push(map);
					}
					cfn_createGrid("leftKeyGrid",list,cols);
				}
				if(data.source_headers.length>1){
					cfn_createCommonDataGrid("rightGrid",data.source_headers[1],data.source_datas[1]);
					
					for(var i=0;i<data.source_headers[1].length;i++){
						tHeaders.push(data.source_headers[1][i]+"_right");
					}
					
					var list = [];
					var cols = [
						{caption : ['선택'],			ref : 'SEL_CHK',		width : '20%',  style : 'text-align:center',	type : 'checkbox',	typeinfo : {checkedvalue : 'Y', uncheckedvalue : 'N'}},
						{caption : ['칼럼명'],		ref : 'COL_NAME',		width : '80%',  style : 'text-align:left',		type : 'output'}
					];
					for(var j=0;j<data.source_headers[1].length;j++){
						var map = {};
						map.COL_NAME=data.source_headers[1][j];
						list.push(map);
					}
					cfn_createGrid("rightKeyGrid",list,cols);
				}
				
				if(data.target_header.length>0){
					cfn_createCommonDataGrid("targetGrid",data.target_header,data.target_data);	
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			var joinType = $('#select_join_type').val();
			var leftGrid = _SBGrid.getGrid("leftGrid");
			var rightGrid = _SBGrid.getGrid("rightGrid");
			var leftKeyGrid = _SBGrid.getGrid("leftKeyGrid");
			var rightKeyGrid = _SBGrid.getGrid("rightKeyGrid");
			if(typeof leftGrid=="undefined"){
				alert('Left Grid가 없습니다.');
				return;
			}
			
			if(typeof rightGrid=="undefined"){
				alert('Right Grid가 없습니다.');
				return;
			}
			var lkDataAll = leftKeyGrid.getGridDataAll();
			var rkDataAll = rightKeyGrid.getGridDataAll();
			var lData = leftGrid.getGridDataAll();
			var rData = rightGrid.getGridDataAll();
			var lkData = leftKeyGrid.getCheckedRowData(0);
			var rkData = rightKeyGrid.getCheckedRowData(0);
			
			if(lkData.length==0){
				alert('Left Key를 선택해 주세요.');
				return;
			}
			
			if(rkData.length==0){
				alert('Right Key를 선택해 주세요.');
				return;
			}
			
			if(lkData.length!=rkData.length){
				alert('Key의 숫자는 서로 동일해야합니다.');
				return;
			}
			
			var controlParam = cfn_getControlParam();
			
			var jt = "FULL OUTER JOIN";
			if(joinType=="LOJ"){
				jt="LEFT OUTER JOIN";
			}else if(joinType=="ROJ"){
				jt="RIGHT OUTER JOIN";
			}else if(joinType=="IJ"){
				jt="INNER JOIN";
			}
			
			var joinArr = [];
			var cnt = 0;
			for(var i=0;i<lkDataAll.length;i++){
				if(lkDataAll[i].SEL_CHK=="Y"){
					joinArr.push("L.COL"+i+"=R.COL"+(rkData[cnt++].rownum-1));
				}
			}
			
			var leftList = [];
			for(var i=0;i<lData.length;i++){
				var map = {};
				for(key in lData[i]){
					var colName = "COL"+key;
					map[colName]=lData[i][key];
				}
				leftList.push(map);
			}
			
			var rightList = [];
			for(var i=0;i<rData.length;i++){
				var map = {};
				for(key in rData[i]){
					var colName = "COL"+key;
					map[colName]=rData[i][key];
				}
				rightList.push(map);
			}
			
			var param = {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
				left_list  : leftList,
				right_list : rightList,
				left_cols  : lkDataAll,
				right_cols : rkDataAll,
				joinstr    : joinArr.join(" AND "),
				jointype   : jt
			}
			$.doPost({
				url : "/mliframe/joinData.do",
				data : param,
				success : function(data){
					if(tHeaders.length>0){
						cfn_createCommonDataGrid("targetGrid",tHeaders,data.list,"Y");
						cfn_setDiagramText(modelUid);
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		});
		
		$('#img_leftGrid').click(function(e){
			var sid = sourceIdArr[0];
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/'+sid+'.csv');
		});
		
		$('#img_rightGrid').click(function(e){
			var sid = sourceIdArr[1];
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/'+sid+'.csv');
		});
	});
</script>
<div class="row">
	<div class="col-4 card l-20 mr-30">
		<dl class="row pt-3 mb-0 pt-0">
			<dt class="col-12">SOURCE</dt>
		</dl>
		<div class="col-12 p-0">
			<div id="div_left_source">
				<p id="p_left" style="margin-top: 10px;margin-bottom: 5px;">
					Left
					<img id="img_leftGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
					<span id="span_leftGrid_row_cnt" style="float:right;">0건</span>
				</p>
				<div class="card m-2 p-2">
					<div class="row">
						<div class="col-12">
							<div id="leftGrid" style="width:100%;height:200px;"></div>
						</div>
					</div>
				</div>
			</div>
			<div id="div_right_source">
				<p id="p_right" style="margin-top: 10px;margin-bottom: 5px;">
					Right
					<img id="img_rightGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
					<span id="span_rightGrid_row_cnt" style="float:right;">0건</span>
				</p>
				<div class="card m-2 p-2">
					<div class="row">
						<div class="col-12">
							<div id="rightGrid" style="width:100%;height:200px;"></div>
						</div>
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
						<label class="mliFrame-name required" id="label_join_type"><strong>Join Type</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_join_type" name="join_type">
				<option value="FOJ">Full Outer Join</option>
				<option value="LOJ">Left Outer Join</option>
				<option value="ROJ">Right Outer Join</option>
				<option value="IJ">Inner Join</option>
			</select>
		</div>
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<label class="mliFrame-name required" id="label_leftKeyGrid"><strong>Left Keys</strong></label>
					</p>
				</div>
			</div>
		</div>
		<div class="card">
			<div id="leftKeyGrid" style="width:100%;height:180px;"></div>
		</div>
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<strong class="mliFrame-name required">Right Keys</strong>
					</p>
				</div>
			</div>
		</div>
		<div class="card">
			<div id="rightKeyGrid" style="width:100%;height:180px;"></div>
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
