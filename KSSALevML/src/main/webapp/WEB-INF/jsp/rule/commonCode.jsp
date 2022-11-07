<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<style>
	tr.highlight td.jsgrid-cell {
		background-color: #BBDEFB;
	}
	.jsgrid-header-row  {
	    text-align: center;
	}
	.jsgrid>.jsgrid-pager-container{
		color:#007bff;
	}
	.btn btn-sm btn-outline-primary{
		width:70px;
	}
    .grid-cell-grey{
        background: #f3f3f3;
        cursor: pointer;
    }
</style>
<script>
var list =[];
// combo data 가져오기
$(document).ready(function () {
	createElements(list);
});

//버튼 기능
$(function(){
	//조회
	$('#btn_search').click(function(){ //조회버튼 클릭
		getProjectList();
	});

	//추가
	$('#btn_add').click(function () {
		datagrid.addRow(true,{add_yn: "Y"},true);
		var nRow = datagrid.getRow();
		var nCol = datagrid.getColRef('MCD');
		console.log("nCol : "+nCol+" nRow :"+nRow);
	    datagrid.setCellDisabled(nRow, nCol, nRow, nCol, false, true, true);
	    datagrid.removeCellStyle(nRow, nCol);
	});

	//저장
	$('#btn_save').click(function(){
		var dataList = datagrid.getGridDataAll();
		var param = [];
		for(var i=0; i<dataList.length; i++){
			if(datagrid.getRowStatus(i+1) != 0){
				if(!dataList[i].MCD){
					alert("주코드  필수값 입니다.");
					return;
				};
				for(var j=0; j<i; j++) {
					if(dataList[i].MCD == dataList[j].MCD && dataList[i].SCD == dataList[j].SCD){
						alert("주코드 값은 중복될 수 없습니다.");
						return;
					}
				}
				param.push(dataList[i]);	
			}
		};
		if(param.length == 0){
			alert("저장할 데이터가 없습니다.");
			return;
		};
		$.ajax({
			type : "POST",
			url :  "ruleListSave.do",
			data : JSON.stringify(param),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
			contentType: "application/json;charset=UTF-8",
			success : function(data, status){
				if(status === "success"){
					alert("저장되었습니다.");
					getProjectList();
				}else{
					alert("정보가 저장되지 않았습니다..");
				}
			},
			error : function(){
				console.log("실패!");
			}
		})
	});
	
	//pop 추가
	$('#btn_pop_add').click(function () {
		var nPopCol = sbPopGrid.getColRef('SCD');
		var nPopRow = sbPopGrid.getRows();
		sbPopGrid.addRow(true,{MCD: $('#vPopMcd').val(), add_yn: "Y"},true);
		console.log("nRow :: "+nPopRow+" nCol :: "+nPopCol);
		sbPopGrid.setCellDisabled(nPopRow, nPopCol, nPopRow, nPopCol, false, false, true);
		sbPopGrid.removeCellStyle(nPopRow, nPopCol);
	});
	
	//pop 저장
	$('#btn_pop_save').click(function(){
		var dataList = sbPopGrid.getGridDataAll();
		var param = [];
		for(var i=0; i<dataList.length; i++){
			if(sbPopGrid.getRowStatus(i+1) != 0){
				if(!dataList[i].MCD){
					alert("주코드는  필수값 입니다.");
					return;
				};
				if(!dataList[i].SCD){
					alert("부코드는 필수값 입니다.");
					return;
				};
				for(var j=0; j<i; j++) {
					if(dataList[i].MCD == dataList[j].MCD && dataList[i].SCD == dataList[j].SCD){
						alert("주코드 값은 중복될 수 없습니다.");
						return;
					}
				}
				param.push(dataList[i]);	
			}
		};
		if(param.length == 0){
			alert("저장할 데이터가 없습니다.");
			return;
		};
		$.ajax({
			type : "POST",
			url :  "ruleListPopSave.do",
			data : JSON.stringify(param),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
			contentType: "application/json;charset=UTF-8",
			success : function(data, status){
				if(status === "success"){
					alert("저장되었습니다.");
					getPopProjectList();
				}else{
					alert("정보가 저장되지 않았습니다..");
				}
			},
			error : function(){
				console.log("실패!");
			}
		})
	});
});

function getProjectList(){
	var url = "/rule/callRuleList.do";
	var param = {
		ruleNm : $("#ruleName").val()
	};
	$.doPost({
		url:url,
		data : param,
		success : function(data, status, xhr){
			createElements(data);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
};

var datagrid;
function createElements(data){
	$('#sbGridArea').empty();
	var SBGridProperties = {}; 
	SBGridProperties.parentid = 'sbGridArea';  	// [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'datagrid';         	// [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    		// [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.
	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.extendlastcol = 'scroll';
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;
    SBGridProperties.rowheader = ['seq', 'update'];
    SBGridProperties.height = ($(document).height()-250)+"px";
	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['삭제'],     	ref: 'DEL_CHK',	width:'50px',   style:'text-align:center',	type : 'checkbox', typeinfo : { checkedvalue : 'Y', uncheckedvalue : 'N'}}
		,{caption : ['코드'],		ref: 'MCD',		width:'150px',  style:'text-align:center',	type : 'input'}
		,{caption : ['코드명'], 	ref: 'CD_NM',	width:'350px', 	style:'text-align:left',	type : 'input'}
		,{caption : ['순서'], 	ref: 'ORD',		width:'60px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성1'], 	ref: 'ATTR1',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성2'], 	ref: 'ATTR2',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성3'], 	ref: 'ATTR3',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성4'], 	ref: 'ATTR4',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성5'], 	ref: 'ATTR5',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성6'], 	ref: 'ATTR6',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성7'], 	ref: 'ATTR7',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성8'], 	ref: 'ATTR8',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성9'],	ref: 'ATTR9',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성10'], 	ref: 'ATTR10',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성11'], 	ref: 'ATTR11',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성12'], 	ref: 'ATTR12',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성13'], 	ref: 'ATTR13',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성14'], 	ref: 'ATTR14',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성15'], 	ref: 'ATTR15',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['비고'], 	ref: 'rmks',	width:'400px',  style:'text-align:left',	type : 'input'}
		,{ref: 'add_yn', hidden:true}
		,{ref: 'user_id', hidden:true}
	];			
	datagrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
    var nCol = datagrid.getColRef("MCD");
	var nRow = datagrid.getRows()-1;
	//개탄스러운 일이지만 setColDisabled는 Cell과 연동되지 않는다.
	for(var i=0; i<nRow; i++){
		datagrid.setCellDisabled((i+1), nCol, (i+1), nCol, true, false, true);
	};
    datagrid.setColStyle(nCol, "data", "background-color", "#F5F5F5");
	datagrid.bind('click','gridClick');
};

function gridClick(){
	//팝업 open
    var nRow = datagrid.getRow();
    var nCol = datagrid.getCol();
    var nColId = datagrid.getRefOfCol(nCol);
    var nData = datagrid.getRowData(nRow,false)
    if(nColId == 'MCD' && nData.add_yn !="Y"){
    	$('#vPopMcd').val(nData.MCD);
    	$('#popMcd').text(nData.CD_NM);
    	getPopProjectList();
    	$('#myModal').modal("show");
    };
	
	//추가된 데이터 삭제체크시 제거
	var checkedInfo = datagrid.getCheckedRowData(datagrid.getColRef('DEL_CHK'));
	if(checkedInfo.length != 0){
		for(var i=0; i<checkedInfo.length; i++){
			if(checkedInfo[i].data.DEL_CHK == 'Y' && checkedInfo[i].data.add_yn == "Y"){
				datagrid.deleteRow(checkedInfo[i].rownum);
			}
		}
	};
};

var sbPopGrid;
function createPopElements(data){
	$('#sbPopGrid').empty();
	var SBPopGridProperties = {}; 
	SBPopGridProperties.parentid = 'sbPopGrid';  	// [필수] 그리드 영역의 div id 입니다.            
	SBPopGridProperties.id = 'sbPopGrid';         	// [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBPopGridProperties.jsonref = data;    		// [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.
	// 그리드의 여러 속성들을 입력합니다.
	SBPopGridProperties.extendlastcol = 'scroll';
	SBPopGridProperties.tooltip = true;
	SBPopGridProperties.ellipsis = true;
	SBPopGridProperties.rowheader = ['seq', 'update'];
	// [필수] 그리드의 컬럼을 입력합니다.  
	SBPopGridProperties.columns = [
		{caption : ['삭제'],     	ref: 'DEL_CHK',	width:'50px',   style:'text-align:center',	type : 'checkbox', typeinfo : { checkedvalue : 'Y', uncheckedvalue : 'N'}}
		,{caption : ['코드'],		ref: 'SCD',		width:'150px',  style:'text-align:center',	type : 'input'}
		,{caption : ['코드명'], 	ref: 'CD_NM',	width:'350px', 	style:'text-align:left',	type : 'input'}
		,{caption : ['순서'], 	ref: 'ORD',		width:'60px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성1'], 	ref: 'ATTR1',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성2'], 	ref: 'ATTR2',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성3'], 	ref: 'ATTR3',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성4'], 	ref: 'ATTR4',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성5'], 	ref: 'ATTR5',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성6'], 	ref: 'ATTR6',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성7'], 	ref: 'ATTR7',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성8'], 	ref: 'ATTR8',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성9'],	ref: 'ATTR9',	width:'150px', 	style:'text-align:center', 	type : 'input'}
		,{caption : ['속성10'], 	ref: 'ATTR10',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성11'], 	ref: 'ATTR11',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성12'], 	ref: 'ATTR12',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성13'], 	ref: 'ATTR13',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성14'], 	ref: 'ATTR14',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['속성15'], 	ref: 'ATTR15',	width:'150px',  style:'text-align:center', 	type : 'input'}
		,{caption : ['비고'], 	ref: 'rmks',	width:'400px',  style:'text-align:left',	type : 'input'}
		,{ref: 'MCD', hidden:true}
		,{ref: 'add_yn', hidden:true}
	];			
	sbPopGrid = _SBGrid.create(SBPopGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
    var nPopCol = sbPopGrid.getColRef('SCD');
	var nPopRow = sbPopGrid.getRows()-1;
	for(var i=0; i<nPopRow; i++){
		sbPopGrid.setCellDisabled((i+1), nPopCol, (i+1), nPopCol, true, false, true);
	};
	sbPopGrid.setColStyle(nPopCol, "data", "background-color", "#F5F5F5");
	sbPopGrid.bind('click','popGridClick');
};

function getPopProjectList(){
	var url = "/rule/callPopRuleList.do";
	console.log($('#vPopMcd').val());
	var paramData = {
		popMcd : $('#vPopMcd').val()
	};
	$.doPost({
		url:url,
		data : paramData,
		success : function(data, status, xhr){
			createPopElements(data);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
};

function popGridClick(){
	//추가된 데이터 삭제체크시 제거
	var checkedPopInfo = sbPopGrid.getCheckedRowData(sbPopGrid.getColRef('DEL_CHK'));
	var infoLength = checkedPopInfo.length;
	if(infoLength != 0){
		for(var i=0; i<infoLength; i++){
			if(checkedPopInfo[i].data.DEL_CHK == 'Y' && checkedPopInfo[i].data.add_yn == "Y"){
				sbPopGrid.deleteRow(checkedPopInfo[i].rownum);
			}
		}
	};
};
</script>
<body>
	<!--  화면이름 / 버튼 -->
	<div class="col-12 p-3">
		<div class="row">
			<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
			<div class="col-6" >
				<div class="h5" style="float:left;width:50%;">
					<strong><i class="fas fa-sitemap"></i> <span class="ml-1">Rule-Based 기준</span></strong>
				</div>	
			</div>
			<div class="col-6  text-right" >
				<button type="button" class="btn btn-outline-success"  id="btn_search">조회 <i class="fa fa-search"></i></button>
				<button type="button" class="btn btn-outline-primary"  id="btn_add">추가 <i class="fa fa-plus"></i></button>
				<button type="button" class="btn btn-outline-success"  id="btn_save">저장 <i class="fa fa-save"></i></button>
			</div>
		</div>
	</div>

	<!--  Search Conds. -->
	<div class="card  ml-3 mr-3">
		<div class="row" style="height:60px;">
			<div class="col-lg-1 text-center col-sm-12 my-auto"  style="min-width:120px;">
				<span class="font-weight-bold">기준명</span>
			</div>
			<div class="col-lg-2 text-center col-sm-12 my-auto">
				<input type="text" id="ruleName" name="ruleName"/>
			</div>
		</div>
	</div>

	<!--  Grid  -->
	<div class="card  ml-3 mr-3 mt-1">
		<div class="row">
			<div class="col-12">
				<div id="sbGridArea" style="width:100%; height:100%"></div>
			</div>
		</div>
	</div>

	<!-- 디테일 팝업 -->
	<div class="modal" id="myModal">
		<div class="modal-dialog modal-xl">
			<div class="modal-content">
				<!-- Modal Header -->
				<div class="modal-header">
					<div class=" h5 modal-title"><i class="fas fa-sitemap"></i> Rule-Based 기준 세부</div>
					<button type="button" class="close" data-dismiss="modal" style="float:right;!important;">&times;</button>
				</div>

				<!-- Modal body -->
				<div class="modal-body">
					<div class="row mb-2">
						<div class="col-12" >
							<div class="h5" style="float:left;width:50%;">
								<strong>코드명 : <span class="ml-1" id="popMcd"></span></strong>
							</div>
							<div class="col-12 mt-3 text-right" style="float:left; margin-top:0px !important; width:50%">
								<button type="button" class="btn btn-outline-primary"  id="btn_pop_add">추가 <i class="fa fa-plus"></i></button>
								<button type="button" class="btn btn-outline-success"  id="btn_pop_save">저장 <i class="fa fa-save"></i></button>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-12" >
							<div id="sbPopGrid" style="width:100%; height:600px;"></div>
						</div>
					</div>
					<input type="hidden" id="vPopMcd"/>
				</div>
			</div>
		</div>
	</div>
</body>