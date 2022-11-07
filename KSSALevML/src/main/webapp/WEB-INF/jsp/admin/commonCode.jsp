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
var popMcd = "";
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
		//var dataList = gridView.getList();
		var dataList = gridView.target.getList("modified");
		return;
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
				if(dataList[i].MCD == dataList[j].MCD && dataList[i].SCD == dataList[j].SCD){
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
					callRuleList();
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
	
	//pop 추가
	$('#btn_pop_add').click(function () {
		gridPopView.addRow();
	});
	
	//pop 저장
	$('#btn_pop_save').click(function(){
		var dataList = gridPopView.target.getList("modified");
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
				if(dataList[i].MCD == dataList[j].MCD && dataList[i].SCD == dataList[j].SCD){
					alert("주코드 값은 중복될 수 없습니다.");
					return;
				}
			}
		}
		$.ajax({
			type : "POST",
			url :  "ruleListPopSave.do",
			data : JSON.stringify(dataList),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
			contentType: "application/json;charset=UTF-8",
			success : function(data, status){
				if(status === "success"){
					alert("저장되었습니다.");
					callPopRuleList();
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

function callRuleList(){
	var ruleName = $("#ruleName").val();
	var paramData = {
		ruleNm : ruleName
	};
	$.ajax({ 
		type : "POST",
		url: "callRuleList.do",
		data : JSON.stringify(paramData),
		contentType: "application/json;charset=UTF-8",
		success : function(data){
			gridView.setData(data);
// 			$('tr[class ^= tr').css('background-color','white');
		},
		error : function(){
			alert("리스트를 불러오는데 실패하였습니다.");
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
	            		var nullChk = this.item.addYn;
	            		console.log(nullChk);
	            		if(nullChk == 'Y'){
	            			gridView.removeRow();
	            		}
	            	};
	            	if(this.column.key == "MCD" && this.item.addYn != 'Y'){
	            		$('#myModal').modal("show");
	            		gridPopView.initView(); //그리드 init
	            		popMcd = this.item.MCD;
	            		$('#popMcd').text(this.item.CD_NM);
	            		callPopRuleList();
	            		//header 변경
	            		var popHeader = [
	            			this.item.ATTR1
	            			,this.item.ATTR2
	            			,this.item.ATTR3
	            			,this.item.ATTR4
	            			,this.item.ATTR5
	            			,this.item.ATTR6
	            			,this.item.ATTR7
	            			,this.item.ATTR8
	            			,this.item.ATTR9
	            			,this.item.ATTR10
	            			,this.item.ATTR11
	            			,this.item.ATTR12
	            			,this.item.ATTR13
	            			,this.item.ATTR14
	            			,this.item.ATTR15
	            		];
	            		var j = 18;
	        			for(var i=0; i<popHeader.length; i++){
	        				if(!popHeader[i]){
	        					gridPopView.removeColumn(j);
	        					j--;
	        				}
	        			}
	        			for(var i=0; i<popHeader.length; i++){
	        				if(popHeader[i]){
	        					$('[data-ax5grid="second-grid"]').find('[data-ax5grid-column-key="ATTR'+(i+1)+'"]').find('span[data-ax5grid-cellholder]').text(popHeader[i]);
	        				}
	        			}
	            	}
	        	}
			},
	    	columns: [
	    		{key: "delChk", width: 50, 	label: "삭제", sortable: false, editor: {type: "checkbox", config: {height: 17, trueValue: "Y", falseValue: "N"}}}
	    		,{key: "MCD",	width: 150, label: "코드", 	align: "center", editor: { type: "text", disabled: function () {
	    					return (this.item.addYn == "Y") ? false : true;
						}
					},
                    styleClass: function () {
                        return (this.item.addYn != "Y") ? "grid-cell-grey" : "";
                    }
				}
	    		,{key: "CD_NM",		width: 250, label: "코드명", 	align: "left",   editor: {type: "text"}}
	    		,{key: "ORD",		width: 60, 	label: "순서", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR1",		width: 150, label: "속성1", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR2",		width: 150, label: "속성2", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR3",		width: 150, label: "속성3", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR4",		width: 150, label: "속성4", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR5",		width: 150, label: "속성5",	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR6",		width: 150, label: "속성6",	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR7",		width: 150, label: "속성7",	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR8",		width: 150, label: "속성8", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR9",		width: 150, label: "속성9", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR10",	width: 150, label: "속성10", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR11",	width: 150, label: "속성11", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR12",	width: 150, label: "속성12", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR13",	width: 150, label: "속성13", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR14",	width: 150, label: "속성14", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR15",	width: 150, label: "속성15", 	align: "center", editor: {type: "text"}}
	    		,{key: "rmks",		width: 400, label: "비고", 	align: "left",	 editor: {type: "text"}}
	    		,{key: "user_id",	width: 0}
	    		,{key: "addYn",		width: 0}
	    	]
		});
		return this;
	},
	setData: function (data) {
		var list = [];
		for (var i = 0, l = data.callRuleList.length; i < l; i++) {
	    	list.push(data.callRuleList[i]);
	    }
		this.target.setData({
			list: list
			,page: {}
	    });
	    return this;
	},
	addRow: function (){
		this.target.addRow($.extend({}), "first");
		this.target.updateRow({user_id: '${userName}', addYn: 'Y'}, 0);
		return this;
	},
	removeRow: function (){
		this.target.removeRow("first");
		return this;
	}/* ,
	getList: function (type){
		return this.target.getList(type);
	} */
};

function callPopRuleList(){
	var paramData = {
		popMcd : popMcd
	};
	$.ajax({ 
		type : "POST",
		url: "callPopRuleList.do",
		data : JSON.stringify(paramData),
		contentType: "application/json;charset=UTF-8",
		success : function(data){
			gridPopView.setData(data);
		},
		error : function(){
			alert("구동기 리스트를 불러오는데 실패하였습니다.");
		}
	});
}

var gridPopView = {
	initView: function(){
		this.target = new ax5.ui.grid();
		this.target.setConfig({
			target : $('[data-ax5grid="second-grid"]'),
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
	            		var nullChk = this.item.REG_ID;
	            		if(!nullChk){
	            			gridPopView.removeRow();
	            		}
	            	};
	        	}
			},
	    	columns: [
	    		{key: "delChk", 	width: 50, 	label: "삭제", sortable: false, editor: {type: "checkbox", config: {height: 17, trueValue: "Y", falseValue: "N"}}}
	    		,{key: "SCD",		width: 150, label: "코드", 	align: "center", editor: { type: "text", disabled: function () {
	    					return (this.item.addYn == "Y") ? false : true;
						}
					},
                    styleClass: function () {
                        return (this.item.addYn != "Y") ? "grid-cell-grey" : "";
                    }
				}
	    		,{key: "CD_NM",		width: 150, label: "코드명", 	align: "left", 	 editor: {type: "text"}}
	    		,{key: "ORD",		width: 60, 	label: "순서", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR1",		width: 150, label: "속성1", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR2",		width: 150, label: "속성2", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR3",		width: 150, label: "속성3", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR4",		width: 150, label: "속성4", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR5",		width: 150, label: "속성5",	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR6",		width: 150, label: "속성6",	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR7",		width: 150, label: "속성7",	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR8",		width: 150, label: "속성8", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR9",		width: 150, label: "속성9", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR10",	width: 150, label: "속성10", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR11",	width: 150, label: "속성11", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR12",	width: 150, label: "속성12", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR13",	width: 150, label: "속성13", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR14",	width: 150, label: "속성14", 	align: "center", editor: {type: "text"}}
	    		,{key: "ATTR15",	width: 150, label: "속성15", 	align: "center", editor: {type: "text"}}
	    		,{key: "rmks",		width: 400, label: "비고", 	align: "left",	 editor: {type: "text"}}
	    		,{key: "user_id",	width: 0}
	    		,{key: "addYn",		width: 0}
	    	]
		});
		return this;
	},
	setData: function (data) {
		var list = [];
		for (var i = 0, l = data.callPopRuleList.length; i < l; i++) {
	    	list.push(data.callPopRuleList[i]);
	    }
		this.target.setData({
			list: list
			,page: {}
	    });
	    return this;
	},
	addRow: function (){
		this.target.addRow($.extend({}), "first");
		this.target.updateRow({user_id: '${userName}', MCD: popMcd, addYn: 'Y'}, 0);
		return this;
	},
	removeRow: function (){
		this.target.removeRow("first");
		return this;
	},
	removeColumn: function (index){
		this.target.removeColumn(index);
		return this;
	}
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
			<div class="col-lg-2 text-center col-sm-12 my-auto" >
				<input type="text" id="ruleName" name="ruleName"/>
			</div>
		</div>
	</div>

	<!--  Grid  -->
	<div class="card  ml-3 mr-3 mt-1">
		<div class="row">
			<div class="col-12">
				<div data-ax5grid="first-grid" data-ax5grid-config="{}" style="width:100%; height:600px;"></div>
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
							<div class="col-12 mt-3 text-right" style="float:left;margin-top:0px !important;width:50%">
								<button type="button" class="btn btn-outline-primary"  id="btn_pop_add">추가 <i class="fa fa-plus"></i></button>
								<button type="button" class="btn btn-outline-success"  id="btn_pop_save">저장 <i class="fa fa-save"></i></button>
							</div>
						</div>
					</div>
					<div class="row">	
						<div class="col-12" >
							<div data-ax5grid="second-grid" data-ax5grid-config="{}" style="width:100%; height:600px;"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>