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
</style>
<script>
var list =[];
// combo data 가져오기
$(document).ready(function () {
	gridView.initView(); //그리드 init
});

//버튼 기능
$(function(){
	//조회
	$('#btn_search').click(function(){ //조회버튼 클릭
		callRuleList();
	});

	//추가
	$('#btn_add').click(function () {
		gridView.addRow();
	});

	//저장
	$('#btn_save').click(function(){
		var dataList = gridView.getList();
		//중복체크 && 필수값 체크
		if(!dataList.length){
			alert("저장할 데이터가 없습니다.");
			return;
		}
		for(var i in dataList){
			if(!dataList[i].MCD){
				alert("주코드  필수값 입니다.");
				return;
			}
			if(!dataList[i].SCD){
				alert("부코드는 필수값 입니다.");
				return;
			}
			for(var j=0; j<i; j++) {
				if(dataList[i].MCD == dataList[j].MCD){
					alert("주코드 값은 중복될 수 없습니다.");
					return;
				}
			}
		}
		$.ajax({
			type : "POST",
			url :  "ruleListSave.do",
			data : JSON.stringify(dataList),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
			contentType: "application/json;charset=UTF-8",
			success : function(data, status){
				if(status === "success"){
					callActuatorList();
					alert("저장되었습니다.");
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

function callRuleList(page){
	var ruleName = $("#ruleName").val();
	var paramData = {
		ruleNm : ruleName
		,page : page
	};
	$.ajax({ //구동기 리스트 받아오기 ()
		type : "POST",
		url: "callRuleList.do",
		data : JSON.stringify(paramData),
		contentType: "application/json;charset=UTF-8",
		success : function(data){
			gridView.setData(data);
		},
		error : function(){
			alert("구동기 리스트를 불러오는데 실패하였습니다.");
		}
	});
}

var gridView = {
	initView: function(){
		this.target = new ax5.ui.grid();
		this.target.setConfig({
			target : $('[data-ax5grid="first-grid"]'),
			frozenColumnIndex: 4,
			frozenRowIndex: 0,
			showLineNumber: true,
			showRowSelector: false,
			multipleSelect: true,
			lineNumberColumnWidth: 40,
			rowSelectorColumnWidth: 28,
			sortable: true, // 모든 컬럼에 정렬 아이콘 표시
			multiSort: false, // 다중 정렬 여부
			remoteSort: false,
			header: {
	        	align: "center",
	            columnHeight: 28
	        },
	        body: {
	        	align: "center",
	            columnHeight: 28,
	            onClick: function () {
	            	//추가된 행 delChk 클릭시 row remove
	            	if(this.column.key == "delChk"){
	            		var nullChk = this.item.reg_id;
	            		if(!nullChk){
	            			gridView.removeRow();
	            		}
	            	};
	        	},
			},
			page: {
			    navigationItemCount: 9,
			    height: 30,
			    display: true,
			    firstIcon: '<i class="fa fa-step-backward" aria-hidden="true"></i>',
			    prevIcon: '<i class="fa fa-caret-left" aria-hidden="true"></i>',
			    nextIcon: '<i class="fa fa-caret-right" aria-hidden="true"></i>',
			    lastIcon: '<i class="fa fa-step-forward" aria-hidden="true"></i>',
			    onChange: function () {
			    	callRuleList(this.page.selectPage);
			    }
			},
	    	columns: [
	    		{key: "delChk", width: 50, 	label: "삭제", sortable: false, editor: {type: "checkbox", config: {height: 17, trueValue: "Y", falseValue: "N"}}},
	    		{key: "MCD",	width: 150, label: "주코드", 	align: "center", editor: {type: "text"}},
	    		{key: "SCD",	width: 150, label: "부코드", 	align: "center", editor: {type: "text"}},
	    		{key: "CD_NM",	width: 250, label: "코드명", 	align: "center", editor: {type: "text"}},
	    		{key: "ORD",	width: 60, 	label: "순서", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR1",	width: 150, label: "속성1", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR2",	width: 150, label: "속성2", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR3",	width: 150, label: "속성3", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR4",	width: 150, label: "속성4", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR5",	width: 150, label: "속성5",	align: "center", editor: {type: "text"}},
	    		{key: "ATTR6",	width: 150, label: "속성6",	align: "center", editor: {type: "text"}},
	    		{key: "ATTR7",	width: 150, label: "속성7",	align: "center", editor: {type: "text"}},
	    		{key: "ATTR8",	width: 150, label: "속성8", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR9",	width: 150, label: "속성9", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR10",	width: 150, label: "속성10", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR11",	width: 150, label: "속성11", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR12",	width: 150, label: "속성12", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR13",	width: 150, label: "속성13", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR14",	width: 150, label: "속성14", 	align: "center", editor: {type: "text"}},
	    		{key: "ATTR15",	width: 150, label: "속성15", 	align: "center", editor: {type: "text"}},
	    		{key: "rmks",	width: 400, label: "비고", 	align: "left",	editor: {type: "text"}}
	    	]
		});
		return this;
	},
	setData: function (data,_pageNo) {
// 		var totalPage = Math.ceil(data.actuatorCount/20);
		var list = [];
		for (var i = 0, l = data.callRuleList.length; i < l; i++) {
	    	list.push(data.callRuleList[i]);
	    }
		this.target.setData({
			list: list,
			page: {
				currentPage: data.page || 0,
				totalElements: data.callRuleList.length,
// 				totalPages: totalPage	//쪽수
			}
	    });
	    return this;
	},
	addRow: function (){
		this.target.addRow($.extend({}), "first");
		return this;
	},
	removeRow: function (){
		this.target.removeRow("first");
		return this;
	},
	getList: function (){
		return this.target.getList();
	}
};
</script>
<body>
	<!--  화면이름 / 버튼 -->
	<div class="col-12 pt-3">
		<div class="row">
			<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
			<div class="col-12" >
				<div class="h5" style="float:left;width:50%;">
					<strong><i class="fas fa-sitemap"></i> <span class="ml-1">Rule-Based 기준</span></strong>
				</div>
				<div class="col-12 mt-3 text-right" style="float:left;margin-top:0px !important;width:50%">
					<button type="button" class="btn btn-outline-success"  id="btn_search">조회 <i class="fa fa-search"></i></button>
					<button type="button" class="btn btn-outline-primary"  id="btn_add">추가 <i class="fa fa-plus"></i></button>
					<button type="button" class="btn btn-outline-success"  id="btn_save">저장 <i class="fa fa-save"></i></button>
				</div>
			</div>
		</div>
	</div>

	<!--  Search Conds. -->
	<div class="col-12 card m-1 p-1">
		<div class="row" style="height:150px;">
			<div class="col-lg-1 text-center col-sm-12 my-auto"  style="min-width:120px;">
				<span class="font-weight-bold">기준명</span>
			</div>
			<div class="col-lg-2 text-center col-sm-12 my-auto" >
				<input type="text" id="ruleName" name="ruleName"/>
			</div>
		</div>
	</div>

	<!--  Grid  -->
	<div class="col-12 pt-3 ">
		<div class="row mt-3">
			<div data-ax5grid="first-grid" data-ax5grid-config="{}" style="width:100%; height:600px;"></div>
		</div>
	</div>

	<!-- 디테일 팝업 -->
	<div class="modal" id="myModal">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<!-- Modal Header -->
				<div class="modal-header">
					<div class=" h5 modal-title">객체정보별 조회조건 등록</div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>

				<!-- Modal body -->
				<div class="modal-body">
					<div class="card my-3 mx-3">
						<dl class="row pt-3">
							<dt class="col-sm-3">테이블</dt>
							<dd class="col-sm-9" id="tableName2"></dd>
							<dt class="col-sm-3">컬럼</dt>
							<dd class="col-sm-9" id="columnName2"></dd>
			  			</dl>
					</div>
					<p class="h5">
						<i class="fas fa-search"></i> <strong>조회조건</strong>
					</p>
					<div class="form-group">
						<div id="jsPopGrid"></div>
					</div>
				</div>
				<div class="modal-footer">
					<div class="text-right">
						<button onclick="doSave()" type="button" class="btn btn-outline-success">저장 <i class="far fa-save"></i></button>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>