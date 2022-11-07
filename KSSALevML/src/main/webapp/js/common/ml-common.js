document.getElementsByClassName = function (className) {
    return document.querySelectorAll('.' + className)
}
const cv_apiAddr = "http://localhost:5100";
//const cv_apiAddr = "http://192.168.1.104:5100";

var sourceGrid;
var targetGrid;
var controlGrid;
var resultGrid;
var featureGrid;
var labelGrid;
var trainGrid;
var testGrid;
var grid;

$.extend({
	doPost : function(options){
		options.type='POST';
		options.dataType='json';
		options.timeout=3000000;
		options.contentType='application/json; charset=UTF-8';
		if(options.hasOwnProperty("data")){
			options.data.userid=_userid;
			options.data = JSON.stringify(options.data);
		}
		options.beforeSend = function(){
			$('#div_loading').append('<img src="/images/common/icons/loading.gif" style="width:50px;"/>');
		};
		options.complete = function(){
			$('#div_loading').empty();
		};
		$.ajax(options);
	}
});

$(document).ready(function() {
	if($('#iframe_filedown').length==0){
		$('div.row:first').append('<iframe id="iframe_filedown" hidden="true"></iframe>');
	}
	if($('#div_loading').length==0){
		$('body').append('<div id="div_loading" style="position:absolute;top:250px;left:620px;"></div>');
	}
	$('.form-control-number').keyup(function(e){
		if ((e.keyCode < 48) || ((e.keyCode > 57) && e.keyCode!=189 && e.keyCode!=190)){
			$(this).val("");
		}
	});
	
	$('#img_sourceGrid').click(function(e){
		if($('#select_source').length>0){
			var sid = $('#select_source').val();
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/'+sid+'.csv');
		}else{
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/'+sourceIds+'.csv');
		}
	});
	
	$('#img_targetGrid').click(function(e){
		var model = parent.graph.getCell(modelUid);
		var mid = model.attributes.mid;
		if(mid.charAt(0)=="p"){
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/predict/'+modelUid+'.csv');
		}else{
			$('#iframe_filedown').attr('src','/filepath/'+subPjtId+'/'+modelUid+'.csv');
		}
		
	});
});
var imgCnt = 0;
function cfn_setTargetImage(subPjtId,targetId,gb){
	if($('#img_model_visualization').length>0){
		if(typeof gb!="undefined" && gb=="pr"){
			$('#img_model_visualization').attr('src','/filepath/'+subPjtId+'/predict/visualization/'+targetId+'.png' + "?a=" + imgCnt++);
		}else{
			$('#img_model_visualization').attr('src','/filepath/'+subPjtId+'/visualization/'+targetId+'.png' + "?a=" + imgCnt++);
		}
		
	}
}

function cfn_checkCol(cols){
	controlGrid = _SBGrid.getGrid("controlGrid");
	var data = controlGrid.getGridDataAll();
	for(var i=0;i<cols.length;i++){
		for(var j=0;j<data.length;j++){
			if(cols[i]==data[j].COL_NAME){
				controlGrid.setCellData(j+1,0,"Y");
				break;
			}
		}
	}
}

function cfn_getModelIDInPredict(sArr){
	var id = "";
	for(var i=0;i<sArr.length;i++){
		var model = parent.graph.getCell(sArr[i]);
		var mid = model.attributes.mid;
		if(mid.charAt(0)=="t"){
			id=sArr[i];
			break;
		}
	}
	return id;
}

function cfn_getSourceIDInPredict(sArr){
	var sid = "";
	for(var i=0;i<sArr.length;i++){
		var model = parent.graph.getCell(sArr[i]);
		var mid = model.attributes.mid;
		if(mid.charAt(0)!="t"){
			if(mid=="dm005"){
				sid=sArr[i]+"_tr";
			}else{
				sid=sArr[i];
			}
			break;
		}
	}
	return sid;
}

function cfn_setSourceListBox(sid){
	sid = sid.replace("_tr","").replace("_te","");
	var model = parent.graph.getCell(sid);
	var mid = model.attributes.mid;
	if(mid=="dm005"){
		var sDiv = '<div class="card-title mb-0 pt-2"><div class="row" style="height:40px;"><div class="col"><div class="form-group">';
		sDiv+= '<select class="form-control form-control-sm" data-width="200px" id="select_source"></select></div></div></div></div>';
		$('#div_source dl').removeClass('pt-3').addClass('pt-0');
		$('#sourceGrid').css('height','480px');
		$('#div_source').prepend(sDiv);
		$('#select_source').append('<option value="'+sid+'_tr">Train</option>');
		$('#select_source').append('<option value="'+sid+'_te">Test</option>');
		$('#select_source').on('change',function(e){
			var sid = $(this).val();
			sourceIds=sid;
			cfn_setSourceGrid(subPjtId,sid,"sourceGrid");
		});
	}
}

function cfn_postPredictModel(subPjtId,modelUid){
	var controlParam = {};
	controlParam.params = [];
	if($("#select_source").length > 0) {
		controlParam.params.push({id:"select_source",value:$("#select_source").val()});
	}
	$('#div_control').find('.form-group input,.form-group select,.form-group textarea').each(function(){
		if($(this).attr('type')=="checkbox"){
			var value = $(this).is(":checked")?"Y":"N";
			controlParam.params.push({id:$(this).attr('id'),value:value});
		}else{
			controlParam.params.push({id:$(this).attr('id'),value:$(this).val()});
		}
	});
	
	if(controlParam.params.length>0){
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelUid,
			controlparam : controlParam
		};
		$.doPost({
			url : "/mlModel/postPredictModel.do",
			data : param,
			success : function(data){
				setTimeout(function(e){cfn_setDiagramTextPR(modelUid);},1000);
			},
			error : function(jqxXHR, textStatus, errorThrown){
				alert('오류가 발생 했습니다.');
			}
		});
	}
}

function cfn_postTrainModel(subPjtId,modelUid,sourceIds,controlParam, modelCsvFlag){
	var param = {
		subpjtid  : subPjtId,
		modeluid  : modelUid,
		sourceuid : sourceIds,
		controlparam : controlParam
	};
	$.doPost({
		url : "/mlModel/postTrainModel.do",
		data : param,
		success : function(data){
			var cols = [
				{caption : ['Attribute'],	ref : 'attr',		width : '40%',  style : 'text-align:left',		type : 'output'},
				{caption : ['Value'],		ref : 'value',		width : '60%',  style : 'text-align:right',		type : 'output'}
			];
			cfn_createGrid("resultGrid",data.list,cols);
			if(typeof(modelCsvFlag) != undefined && modelCsvFlag) {
				cfn_createCommonDataGrid("targetGrid",data.header,data.data);
			}
			cfn_setDiagramTextTR(modelUid);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			alert('오류가 발생 했습니다.');
		}
	});
}

function cfn_getSourceList(sid){
	var sDiv = '<div class="card-title mb-0 pt-2"><div class="row" style="height:40px;"><div class="col"><div class="form-group">';
	    sDiv+= '<select class="form-control form-control-sm" data-width="200px" id="select_source"></select></div></div></div></div>';
	    
	var model = parent.graph.getCell(sid);
	var mid = model.attributes.mid;
	if(mid=="dm005"){
		$('#div_source dl').removeClass('pt-3').addClass('pt-0');
		$('#sourceGrid').css('height','480px');
		$('#div_source').prepend(sDiv);
		$('#select_source').append('<option value="'+sid+'_tr">Train</option>');
		$('#select_source').append('<option value="'+sid+'_te">Test</option>');
		$('#select_source').on('change',function(e){
			var sid = $(this).val();
			sourceIds=sid;
			cfn_setSourceGrid(subPjtId,sid,"sourceGrid");
		});
		return sid+"_tr";
	}else{
		$('#sourceGrid').css('height','500px');
		return sid;
	}
}

function cfn_getControlParam(){
	var controlParam = {};
	controlParam.cols = [];
	if($('#controlGrid').length>0){
		controlGrid = _SBGrid.getGrid("controlGrid");
		var chkData = controlGrid.getCheckedRowData(0);
		for(var i=0;i<chkData.length;i++){
			controlParam.cols.push(chkData[i].data.COL_NAME);
		}
	}
	controlParam.params = [];
	$('#div_control').find('.form-group input,.form-group select,.form-group textarea').each(function(){
		if($(this).attr('type')=="checkbox"){
			var value = $(this).is(":checked")?"Y":"N";
			controlParam.params.push({id:$(this).attr('id'),value:value});
		}else{
			controlParam.params.push({id:$(this).attr('id'),value:$(this).val()});
		}
	})
	if($("#select_source").length > 0) {
		controlParam.params.push({id:"select_source",value:$("#select_source").val()});
	}
	return controlParam;
}

function cfn_getControlParamML(flag){
	var controlParam = {};
	controlParam.fcols = [];
	if($('#featureGrid').length>0){
		featureGrid = _SBGrid.getGrid("featureGrid");
		var chkData = featureGrid.getCheckedRowData(0);
		for(var i=0;i<chkData.length;i++){
			controlParam.fcols.push(chkData[i].data.COL_NAME);
		}
	}
	controlParam.lcols = [];
	if($('#labelGrid').length>0){
		labelGrid = _SBGrid.getGrid("labelGrid");
		var chkData = labelGrid.getCheckedRowData(0);
		for(var i=0;i<chkData.length;i++){
			controlParam.lcols.push(chkData[i].data.COL_NAME);
		}
	}
	controlParam.params = [];
	$('#div_control').find('.form-group input,.form-group select,.form-group textarea').each(function(){
		if($(this).attr('type')=="checkbox"){
			var value = $(this).is(":checked")?"Y":"N";
			controlParam.params.push({id:$(this).attr('id'),value:value});
		}else{
			controlParam.params.push({id:$(this).attr('id'),value:$(this).val()});
		}
	})
	
	if($("#select_source").length > 0 && flag) {
		controlParam.params.push({id:"select_source",value:$("#select_source").val()});
	}
	return controlParam;
}

function cfn_setTargetGrid(spjtid,modeluid){
	var param = {
		subpjtid  : spjtid,
		modeluid  : modeluid
	};
	var model = parent.graph.getCell(modeluid);
	var mid = model.attributes.mid;
	if(mid.charAt(0)=="p"){
		param.gubun="PR";
	}
	$.doPost({
		url : "/mliframe/loadCSVTgt.do",
		data : param,
		success : function(data){
			if(data.target_header.length>0){
				cfn_createCommonDataGrid("targetGrid",data.target_header,data.target_data);	
			}
		},
		error : function(jqxXHR, textStatus, errorThrown){
			alert('오류가 발생했습니다.');
		}
	}); 
}

function cfn_setSourceGrid(spjtid,srcids,gridId){
	var param = {
		sourceids : srcids,
		subpjtid  : spjtid
	};
	$.doPost({
		url : "/mliframe/loadCSVSrc.do",
		data : param,
		success : function(data){
			if(data.source_header.length>0){
				cfn_createCommonDataGrid(gridId,data.source_header,data.source_data);
			}
		},
		error : function(jqxXHR, textStatus, errorThrown){
			alert('오류가 발생했습니다.');
		}
	}); 
}

function cfn_setDataGrid(spjtid,modeluid,srcids,params){
	var param = {
		sourceids : srcids,
		subpjtid  : spjtid,
		modeluid  : modeluid
	};
	var model = parent.graph.getCell(modeluid);
	var mid = model.attributes.mid;
	if(mid.charAt(0)=="p"){
		param.gubun="PR";
	}
	$.doPost({
		url : "/mliframe/loadCSVSrcTgt.do",
		data : param,
		success : function(data){
			if(data.source_header.length>0){
				cfn_createCommonDataGrid("sourceGrid",data.source_header,data.source_data);
			}
			if(data.target_header.length>0){
				cfn_createCommonDataGrid("targetGrid",data.target_header,data.target_data);
				if(mid.charAt(0)=="p"){
					cfn_setTargetImage(spjtid,modeluid,'pr');
				}
			}
			cfn_createControlPanel(params);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			alert('오류가 발생했습니다.');
		}
	}); 
}

function cfn_createControlGrid(data,event){
	$('#controlGrid').empty();
	var SBGridProperties = {};                
	SBGridProperties.parentid = 'controlGrid';  // [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'controlGrid';          // [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    // [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.

	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;

	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['선택'],			ref : 'SEL_CHK',		width : '20%',  style : 'text-align:center',	
		 type : 'checkbox',	typeinfo : {checkedvalue : 'Y', uncheckedvalue : 'N', fixedcellcheckbox : { usemode : true , rowindex : 0 , deletecaption : true }}},
		{caption : ['칼럼명'],		ref : 'COL_NAME',		width : '80%',  style : 'text-align:left',		type : 'output'}
	];
	controlGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	
	if(typeof event!="undefined"){
		controlGrid.bind(event.type,event.func);
	}
}

function cfn_createFeatureGrid(data,opt){
	$('#featureGrid').empty();
	var SBGridProperties = {};                
	SBGridProperties.parentid = 'featureGrid';  // [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'featureGrid';          // [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    // [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.

	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;

	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['선택'],			ref : 'SEL_CHK',		width : '20%',  style : 'text-align:center',	type : 'checkbox',	
		 typeinfo : {checkedvalue : 'Y', uncheckedvalue : 'N', fixedcellcheckbox : { usemode : true , rowindex : 0 , deletecaption : true }}},
		{caption : ['칼럼명'],		ref : 'COL_NAME',		width : '80%',  style : 'text-align:left',		type : 'output'}
	];
	if(typeof opt!="undefined"){
		if(opt.hasOwnProperty("disabled")){
			SBGridProperties.columns[0].disabled=opt.disabled;
		}
	}
	featureGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
}

function cfn_createLabelGrid(data,opt){
	$('#labelGrid').empty();
	var SBGridProperties = {};                
	SBGridProperties.parentid = 'labelGrid';  // [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'labelGrid';          // [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    // [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.

	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;

	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['선택'],			ref : 'SEL_CHK',		width : '20%',  style : 'text-align:center',	type : 'checkbox',	typeinfo : {checkedvalue : 'Y', uncheckedvalue : 'N'}},
		{caption : ['칼럼명'],		ref : 'COL_NAME',		width : '80%',  style : 'text-align:left',		type : 'output'}
	];
	if(typeof opt!="undefined"){
		if(opt.hasOwnProperty("disabled")){
			SBGridProperties.columns[0].disabled=opt.disabled;
		}
	}
	labelGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
}

function cfn_createGrid(gridId,data,cols,opt){
	$('#'+gridId).empty();
	var SBGridProperties = {};                
	SBGridProperties.parentid = gridId;  // [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = gridId;          // [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    // [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.
	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;
	SBGridProperties.oneclickedit = true;
	
	if(typeof opt!="undefined"){
		for(key in opt){
			if(key!=event){
				SBGridProperties[key]=opt[key];
			}
		}
	}

	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = cols;
	
	if(gridId=="sourceGrid"){
		sourceGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="controlGrid"){
		controlGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="targetGrid"){
		targetGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="resultGrid"){
		resultGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="trainGrid"){
		trainGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="testGrid"){
		testGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="featureGrid"){
		featureGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="labelGrid"){
		labelGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="grid"){
		grid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}
	
	if(typeof opt!="undefined" && typeof opt.event!="undefined"){
		for(key in opt.event){
			_SBGrid.getGrid(gridId).bind(key,opt.event[key]);
		}
	}
}

function cfn_createCommonDataGrid(gridId,header,data,hRefYN){
	$('#'+gridId).empty();
	var SBGridProperties = {};                
	SBGridProperties.parentid = gridId;  // [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = gridId;          // [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    // [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.

	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;

	// [필수] 그리드의 컬럼을 입력합니다.  Z
	SBGridProperties.columns = [];
	for(var i=0;i<header.length;i++){
		//var obj = {caption : [header[i]],		ref : i.toString(),		width: 100/header.length+'%', style : 'text-align:center',	type : 'output'};
		var obj = {caption : [header[i]],		ref : i.toString(),		width: '100px', style : 'text-align:center',	type : 'output'};
		if(typeof hRefYN!="undefined"){
			obj.ref=header[i];
		}
		SBGridProperties.columns.push(obj);
	}
	if(gridId=="sourceGrid"){
		sourceGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="controlGrid"){
		controlGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="targetGrid"){
		targetGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="resultGrid"){
		resultGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="trainGrid"){
		trainGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="testGrid"){
		testGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="featureGrid"){
		featureGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}else if(gridId=="labelGrid"){
		labelGrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	}
	$('#span_'+gridId+'_row_cnt').text(comma(data.length)+'건');
}

function cfn_createControlPanel(data){
	for(var i=0;i<data.length;i++){
		if(data[i].PARAM_ID.indexOf("check")==0){
			if(data[i].PARAM_VALUE=="Y" || data[i].PARAM_VALUE==1){
				$('#'+data[i].PARAM_ID).prop("checked",true);	
			}else{
				$('#'+data[i].PARAM_ID).prop("checked",false);
			}
		}else{
			$('#'+data[i].PARAM_ID).val(data[i].PARAM_VALUE);
			if(data[i].PARAM_ID=="select_source"){
				$('#'+data[i].PARAM_ID).change();
			}
		}
	}
	if(mid=="dp001"){
		if($('#input_deploy_title').val().length>0){
			$('#input_file_deploy_url').val(cv_apiAddr.replace('5100','8080')+"/csv/"+subPjtId+"/"+modelUid+".do");
			$('#input_data_deploy_url').val(cv_apiAddr.replace('5100','8080')+"data/"+subPjtId+"/"+modelUid+".do");
		}
	}
}

function comma(num){
    var len, point, str; 
    num = num + ""; 
    point = num.length % 3 ;
    len = num.length; 
    str = num.substring(0, point); 
    while (point < len) { 
        if (str != "") str += ","; 
        str += num.substring(point, point + 3); 
        point += 3; 
    } 
    return str;
}

function cfn_setDiagramTextTR(modelUid){
	var model = parent.graph.getCell(modelUid);
	var str = [];
	var lineHeight = 0;
	resultGrid = _SBGrid.getGrid("resultGrid");
	var data = resultGrid.getGridDataAll();
	for(var i=0;i<data.length;i++){
		str.push('[ '+data[i].attr+' ]');
		str.push(data[i].value);
	}
	lineHeight += 30+data.length*22;
	sourceGrid = _SBGrid.getGrid("sourceGrid");
	var rowCnt = sourceGrid.getGridDataAll().length;
	//str.push('\n　　　　Rows : '+comma(rowCnt));
	
	model.size(150, lineHeight+50);
	model.attr('bodyText/text', str.join('\n'));
	model.attr('bodyText',{'font-size':12});
	parent.getSVG();
}

function cfn_setDiagramTextPR(modelUid){
	var model = parent.graph.getCell(modelUid);
	var str = [];
	var lineHeight = 0;
	featureGrid = _SBGrid.getGrid("featureGrid");
	var fData = [];
	if(featureGrid!=null){
		fData = featureGrid.getCheckedRowData(0);
	}
	labelGrid = _SBGrid.getGrid("labelGrid");
	var label = labelGrid.getCheckedRowData(0)[0].data.COL_NAME;
	if(fData.length>0){
		str.push('[ Feaures ]');
		for(var i=0;i<fData.length;i++){
			if(i==10){
				str.push('...');
				break;
			}
			str.push(fData[i].data.COL_NAME);
		}
	}
	str.push('[ Label ]');
	str.push(label);
	if(fData.length>10){
		lineHeight += 30+11*11;
	}else{
		lineHeight += 30+fData.length*11;
	}
	
	targetGrid = _SBGrid.getGrid("targetGrid");
	var rowCnt = targetGrid.getGridDataAll().length;
	//str.push('\n　　　　Rows : '+comma(rowCnt));
	
	model.size(150, lineHeight+70);
	model.attr('bodyText/text', str.join('\n'));
	model.attr('bodyText',{'font-size':12});
	parent.getSVG();
}

function cfn_setDiagramText(modelUid){
	var model = parent.graph.getCell(modelUid);
	var str = [];
	var lineHeight = 0;
	$('#div_control label.mliFrame-name').each(function(idx){
		var id = this.id;
		var name = $(this).text();
		if(id=='label_leftKeyGrid'){
			var cols = [];
			var leftKeyGrid = _SBGrid.getGrid("leftKeyGrid");
			var lData = leftKeyGrid.getCheckedRowData(0);
			var rightKeyGrid = _SBGrid.getGrid("rightKeyGrid");
			var rData = rightKeyGrid.getCheckedRowData(0);
			for(var i=0;i<lData.length;i++){
				cols.push("A."+lData[i].data.COL_NAME+"=B."+rData[i].data.COL_NAME);
			}
			str.push('[ Join Keys ]');
			str.push(cols.join('\n'));
			lineHeight += 30+cols.length*11;
		}else if(id=='label_controlGrid'){
			var cols = [];
			controlGrid = _SBGrid.getGrid("controlGrid");
			var chkData = controlGrid.getCheckedRowData(0);
			for(var i=0;i<chkData.length;i++){
				if(i==10){
					cols.push('...');
					break;
				}
				cols.push(chkData[i].data.COL_NAME);
			}
			str.push('[ '+name+' ]');
			if(cols.length>10){
				str.push(cols.join('\n'));
				lineHeight += 30+11*11;
			}else{
				str.push(cols.join('\n'));
				lineHeight += 30+cols.length*11;
			}
		}else{
			var inputName = id.replace('label_','');
			if($('input[name='+inputName+']').length>1){
				var values = [];
				$('input[name='+inputName+']').each(function(subIdx){
					if($(this).attr('type')=="checkbox"){
						if($(this).is(':checked')){
							var spanId = $(this).attr('id').replace('check_','span_');
							var val = $('#'+spanId).text();
							values.push(val);
						}
					}else if($(this).val().length>0){
						values.push($(this).val());
					}
				});
				if(values.length>0){
					str.push('[ '+name+' ]');
					str.push(values.join(','));
					lineHeight += 30;
				}
			}else if($('input[name='+inputName+']').length==1){
				var val = $('input[name='+inputName+']').val();
				if(val.length>0){
					str.push('[ '+name+' ]');
					str.push((val.length>15)?val.substring(0,13)+'...':val);
					lineHeight += 30;
				}
			}else if($('select[name='+inputName+']').length==1){
				var val = $('select[name='+inputName+'] option:checked').text();
				if(val.length>0){
					str.push('[ '+name+' ]');
					str.push((val.length>15)?val.substring(0,13)+'...':val);
					lineHeight += 30;
				}
			}
		}
	});
	
	if(mid=="dm005"){
		trainGrid = _SBGrid.getGrid("trainGrid");
		var trainRowCnt = trainGrid.getGridDataAll().length;
		testGrid = _SBGrid.getGrid("testGrid");
		var testRowCnt = testGrid.getGridDataAll().length;
		//str.push('\nRows : '+comma(trainRowCnt)+' / '+comma(testRowCnt));
	}else{
		targetGrid = _SBGrid.getGrid("targetGrid");
		var rowCnt = targetGrid.getGridDataAll().length;
		//str.push('\n　　　　Rows : '+comma(rowCnt));
	}
	model.size(150, lineHeight+50);
	model.attr('bodyText/text', str.join('\n'));
	model.attr('bodyText',{'font-size':12});
	parent.getSVG();
}