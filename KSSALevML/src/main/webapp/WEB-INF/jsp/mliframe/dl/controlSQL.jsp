<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	
	$(document).ready(function() {
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid
		}
		$.doPost({
			url : "/mliframe/getParamInfo.do",
			data : param,
			success : function(data){
				if(data.params.length>0){
					cfn_createControlPanel(data.params);
				}
				cfn_setTargetGrid(subPjtId,modelUid);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			var sql = $('#ta_sql').val();
			if(sql.length==0){
				alert('SQL문을 입력해 주세요.');
				return;
			}
			if(sql.toUpperCase().indexOf('INSERT')>=0 || sql.toUpperCase().indexOf('UPDATE')>=0 || sql.toUpperCase().indexOf('DELETE')>=0){
				alert('SELECT문만 입력해 주세요.');
				return;
			}
			
			var stIdx = sql.toUpperCase().indexOf("SELECT")+6;
			var edIdx = sql.toUpperCase().indexOf("FROM");
			var list = sql.substring(stIdx,edIdx).trim().split(",");
			for(var i=0;i<list.length;i++){
				if(list[i].indexOf(' AS ')>-1){
					list[i]=list[i].substring(list[i].indexOf(' AS ')+4,list[i].length).trim()
				}else{
					list[i]=list[i].trim();	
				}
			}
			
			var controlParam = cfn_getControlParam();
			
			var param = {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
				sql  : sql,
				list : list,
				controlparam : controlParam
			}
			$.doPost({
				url : "/mliframe/getSQLData.do",
				data : param,
				success : function(data){
					if(data.msg=="success"){
						cfn_setTargetGrid(subPjtId,modelUid);
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
	<div class="col-5 card mr-30 l-20" id="div_control">
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
						<strong class="mliFrame-name">SQL문 작성</strong>
					</p>
				</div>
			</div>
		</div>
		<div class="form-group">
			<textarea id="ta_sql" style="width:100%;height:450px;">
			</textarea>
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
				<div class="row" >
					<div class="col-12">
						<div id="targetGrid" style="width:100%;height:500px;"></div>
					</div>
				</div>			
			</div>
		</div>
	</div>
</div>
