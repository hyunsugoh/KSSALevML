<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>	
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<script type="text/javascript">			
	var SBpath = "../";
	var _userid = "${userName}";
</script>

<script src="<c:url value='/SBGrid/SBGrid_Lib.js'/>"></script>
<script src="<c:url value='/SBGrid/SBGrid_min.js'/>"></script>
<script src="<c:url value='/js/common/ml-common.js'/>"></script>
<link rel="stylesheet" type="text/css" href="<c:url value='/SBGrid/css/SBGrid_Default.css'/>" />
<link rel="stylesheet" type="text/css" href="<c:url value='/SBGrid/css/SBGrid.css'/>" />
<script>
var dpStatCombo = [{ "label" : "Running" ,	 "value" : "Running"},	{ "label" : "Stop" , 	"value" : "Stop"}];

$(document).ready(function(){
	getDeployList(true);
	
	console.log(_userid);
	
	$('#btn_save').click(function(e){
		controlGrid = _SBGrid.getGrid("controlGrid");
		var data = JSON.parse(controlGrid.getUpdateData());
		var list = [];
		for(var i=0;i<data.length;i++){
			list.push(data[i].data);
		}
		var param = {
			list : list	
		};
		
		$.doPost({
			url : "/mlDeploy/updateDeployListDetail.do", 
			data : param,
			success : function(data){
				getDeployList(false);
			}
		});
	});
});

function getDeployList(gb){
	$.doPost({
		url : "/mlDeploy/getDeployListDetail.do", 
		data : {},
		success : function(data){
			if(gb){
				var cols = [
					{caption:['Deploy ID'],				ref:"DEPLOY_ID", 		width:'300px', style : 'text-align:left',  type : 'output'},
					{caption:['Deploy Name'],			ref:"DEPLOY_NAME",  	width:'300px', style : 'text-align:left',  type : 'input'},
					{caption:['Deploy Description'],	ref:"DEPLOY_DESC", 		width:'500px', style : 'text-align:left',  type : 'input'},
					{caption:['API Key'],				ref:"API_KEY", 			width:'150px', style : 'text-align:left',  type : 'input'},
					{caption:['Status'],				ref:"DEPLOY_STATUS", 	width:'150px', style : 'text-align:center',  type : 'input', type : 'combo', typeinfo :{ ref : 'dpStatCombo', displayui : true, label : 'label', value : 'value'}},
					{caption:['Last Modified'],			ref:"UPDATE_DT", 		width:'150px', style : 'text-align:center',  type : 'output'}
				];
				var opt = {
					extendcol : 2,
					event : {
						click : 'gridClick'
					}
				};
				cfn_createGrid("controlGrid",data.list,cols,opt);
			}else{
				controlGrid.setGridData(data.list);
			}
		}
	});
}

function gridClick(){
	var nRow = controlGrid.getRow();
	var nCol = controlGrid.getCol();
	if(nRow>0 && nCol==0){
		var value = controlGrid.getCellData(nRow,nCol);
		var param = {
			DEPLOY_ID : value	
		};
		$.doPost({
			url : "/mlDeploy/getDeployLog.do", 
			data : param,
			success : function(data){
				var cols = [
					{caption:['Access Time'],		ref:"UPDATE_TIME", 	width:'200px', style : 'text-align:center',  type : 'output'},
					{caption:['Host IP'],			ref:"HOST_IP", 		width:'200px', style : 'text-align:center',  type : 'output'},
					{caption:['Status'],			ref:"STATUS",  		width:'200px', style : 'text-align:center',  type : 'output'},
					{caption:['Data Size'],			ref:"DATA_SIZE", 	width:'200px', style : 'text-align:center',  type : 'output', format : {type:'number', rule:'#,###.##'}}
				];
				cfn_createGrid("grid",data.list,cols);
			}
		});
	}
}
</script>
<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">
	<div class="row">
		<div class="col-12 pt-3">
        	<div class="row">
				<div class="col-6">
					<div class="h5">
		      			<strong><span class="ml-1">모델 배포 관리</span></strong>  					
					</div>
				</div>	
				<div class="col-6">
					<div class="d-flex justify-content-end">
						<div id="helpIcon" class="pt-0"></div>
					</div>
				</div>
		  	</div>		
		</div>
	 	<div class="col-12">
			<div class="row">
				<div class="col-12">
					<button type="submit" id="btn_save" class="btn btn-outline-primary float-right" ><i class="fas fa-save"></i>저장</button>
				</div>
				<div class="col-12 mt-2">
					<div id="controlGrid" style="height:300px;"></div>
				</div>
			</div>
			<div class="row mt-2">
				<div class="col-6">
					<div class="h5">
		      			<strong><span class="ml-1">모델 사용 로그</span></strong>  					
					</div>
				</div>	
				<div class="col-12 mt-2">
					<div id="grid" style="height:300px;width:820px"></div>
				</div>
			</div>
		</div>	
	</div>
</div>
