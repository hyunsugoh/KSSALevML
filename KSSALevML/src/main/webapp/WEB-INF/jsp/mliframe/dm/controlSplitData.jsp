<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
	var subPjtId = parent.subPjtId; //parent에서 subPjtId 추출.
	var modelUid = "${modelUid}";
	var mid = "${mid}";
	var sourceIds = parent.sourceidStr;
	
	$(document).ready(function() {
		sourceIds=cfn_getSourceList(sourceIds);
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid,
			sourceuid : sourceIds
		};
		$.doPost({
			url : "/mliframe/getControlInfo.do",
			data : param,
			success : function(data){
				setDataGrid(subPjtId,modelUid,sourceIds);
				cfn_createControlPanel(data.params);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
		
		$('#dataSubmitBtn').click(function(e){
			var trratio = $('#input_train_ratio').val();
			var teratio = $('#input_test_ratio').val();
			var ordval = $('#select_ordering').val();
			var seed = $('#input_seed').val();
			
			if(trratio.length==0){
				alert('Ratio를 확인해 주세요.');
				return;
			}
			sourceGrid = _SBGrid.getGrid("sourceGrid");
			var data = sourceGrid.getGridDataAll();
			
			var controlParam = cfn_getControlParam();
			
			var param = {
				subpjtid  : subPjtId,
				modeluid  : modelUid,
				sourceuid : sourceIds,
				trratio	  : trratio,
				teratio	  : teratio,
				ordval	  : ordval,
				seed	  : seed,
				data	  : data,
				controlparam : controlParam
			};

			$.doPost({
				url : "/mliframe/splitData.do",
				data : param,
				success : function(data){
					if(data.target_header.length>0){
						cfn_createCommonDataGrid("trainGrid",data.target_header,data.train_data);
						cfn_createCommonDataGrid("testGrid",data.target_header,data.test_data);
						cfn_setDiagramText(modelUid);
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					alert('오류가 발생 했습니다.');
				}
			});
		});
		
		$('#input_train_ratio, #input_test_ratio').blur(function(e){
			if(this.id.indexOf("train")>0){
				var scale = $(this).val().length-2;
				var val = (1-$(this).val()).toFixed(scale);
				$('#input_test_ratio').val(val);
			}else{
				var scale = $(this).val().length-2;
				var val = (1-$(this).val()).toFixed(scale);
				$('#input_train_ratio').val(val);
			}
		});
		
		$('#img_trainGrid').click(function(e){
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/'+modelUid+'_tr.csv');
		});
		$('#img_testGrid').click(function(e){
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/'+modelUid+'_te.csv');
		});
	});
	
	function setDataGrid(spjtid,modeluid,srcids){
		var param = {
			sourceids : srcids,
			subpjtid  : spjtid,
			modeluid  : modeluid
		};
		$.doPost({
			url : "/mliframe/loadSplitCSVSrcTgt.do",
			data : param,
			success : function(data){
				if(data.source_header.length>0){
					cfn_createCommonDataGrid("sourceGrid",data.source_header,data.source_data);
				}
				if(data.target_header.length>0){
					cfn_createCommonDataGrid("trainGrid",data.target_header,data.train_data);
					cfn_createCommonDataGrid("testGrid",data.target_header,data.test_data);	
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		}); 
	}
</script>
<div class="row">
	<div class="col-4 card l-20 mr-30" id="div_source">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">
				SOURCE
				<img id="img_sourceGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
				<span id="span_sourceGrid_row_cnt" style="float:right;">0건</span>
			</dt>
		</dl>
		<div class="col-12 p-0">
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<div id="sourceGrid" style="width:100%;height:500px;"></div>
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
						<label class="mliFrame-name required" id="label_ratio"><strong>Ratio<font style="font-size:12px;">(0 ~ 1)</font></strong></label>
					</p>
				</div>
			</div>
		</div>
		<div class="form-group">
			<div>
				<input type="text" class="form-control-number" id="input_train_ratio" name="ratio" style="width:48%;float:left;" placeholder="Train">
				<input type="text" class="form-control-number" id="input_test_ratio" name="ratio" style="width:48%;float:right;" placeholder="Test">
			</div>
		</div>
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<label class="mliFrame-name" id="label_seed"><strong>Seed</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<input type="text" class="form-control-number" id="input_seed" name="seed" />
		</div>
		<div class="card-title mb-0">
			<div class="row">
				<div class="col">
					<p class="h5 mb-1">
						<label class="mliFrame-name" id="label_ordering"><strong>Ordering</strong></label>
					</p>
				</div>
			</div>
		</div>		
		<div class="form-group">
			<select class="form-control form-control-sm" data-width="200px" id="select_ordering" name="ordering">
				<option value="T">True</option>
				<option value="F">False</option>
			</select>
		</div>
	</div>
	<div class="col-4 card">
		<dl class="row pt-3 mb-0">
			<dt class="col-12">Result</dt>
		</dl>
		<div class="col-12 p-0">
			<p style="margin-top: 10px;margin-bottom: 5px;">
				Train
				<img id="img_trainGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
				<span id="span_trainGrid_row_cnt" style="float:right;">0건</span>
			</p>
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<div id="trainGrid" style="width:100%;height:200px;"></div>
					</div>
				</div>
			</div>
			<p style="margin-top: 10px;margin-bottom: 5px;">
				Test
				<img id="img_testGrid" style="width:15px;vertical-align:initial;cursor:pointer;" src="/images/common/icons/download_csv.png"/>
				<span id="span_testGrid_row_cnt" style="float:right;">0건</span>
			</p>
			<div class="card m-2 p-2">
				<div class="row">
					<div class="col-12">
						<div id="testGrid" style="width:100%;height:200px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
