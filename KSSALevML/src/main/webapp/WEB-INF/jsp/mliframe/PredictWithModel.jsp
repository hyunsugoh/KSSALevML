<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html  lang="ko">
<script>
var graph = new joint.dia.Graph;
var subPjtId = getParameterByName('subprtid');
$(document).ready(function() {
	if(subPjtId != null){
		var param = { 
			subProjectId : subPjtId,
			display : "display" //이 파라미터로 구분해서 display value만 추출하게끔 해야한다.
		};
		$.doPost({
			url : "/project/getSubProjectInfo.do", //OlapUrlConfig.js 의 insertBoard 
			data : param,  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
			success : function(data, status, xhr){
				if(data!=null){
					if(data.hasOwnProperty("CONTENTS") && data.CONTENTS != null){
						const obj_display = JSON.parse(data.CONTENTS); //json 문자열을 javascrip 값 또는 객체로 변환
						var display = obj_display.display
						if(display != null){
							 setSVG(display);	
						}
					}
				}else{
					alert("데이터를 받아오지 못했습니다.");
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				commonFunc.ajaxFailAction(jqxXHR);
			}
		});
	}
	
	$('#maindiv').css('width',window.innerWidth-400);
    $('#maindiv').css('height',window.innerHeight-130);
    $('#maindiv').css('overflow-y','scroll');
	
	$( "#paletteZone" ).accordion({collapsible: false});
    $( ".draggable" ).draggable({ //왼쪽 메뉴에서  draggable 할경우
    	helper: 'clone'
    }); 
    
    $('#div_dl').css('height','200px');
    $('#div_dm').css('height','500px');
    $('#div_ml').css('height','500px');
    $('#div_dp').css('height','100px');
    $('.ui-accordion-content').css('padding','15px');
    
    var paper = new joint.dia.Paper({ 
		el: $('#myDiagramDiv'), 
    	width: window.innerWidth-410, 
    	height: 2000, 
    	gridSize: 1, 
    	model: graph,
    	defaultLink: new joint.dia.Link({
	    	router: 	{ name: 'manhattan', args: { step: 20 } }
    		/*
    		,
			connector: 	{ name: 'rounded' },
			attrs: 		{  '.connection' 	: { stroke: 'gray', 'stroke-width': 2 }, //stroke : 화살표 라인 색상                    
	 						   '.marker-target' : { fill: 'gray', stroke: 'gray', d: 'M 10 0 L 0 5 L 10 10 z' } } //fill : 화살표 색상 . d: 화살표 머리 모양
			*/
    	}),
    	validateConnection: function(cellViewS, magnetS, cellViewT, magnetT, end, linkView) {
    		// Prevent linking from input ports.
    	    if (magnetS && magnetS.getAttribute('type') === 'input') return false;
            // Prevent linking from output ports to input ports within one element.
    	    if (cellViewS === cellViewT) return false;
    	    // Prevent linking to input ports.
    	    return magnetT && magnetT.getAttribute('type') === 'input';
		},
    	validateMagnet: function(cellView, magnet) {
    		// Note that this is the default behaviour. Just showing it here for reference.
    	    // Disable linking interaction for magnets marked as passive (see below `.inPorts circle`).
    	    return magnet.getAttribute('magnet') !== 'passive';
    	},
    	perpendicularLinks: true,
    	linkPinning: false,
    	elementView: joint.dia.ElementView.extend({ //0915
    		pointerup:function(e){
    			getSVG();
    		},
        	contextmenu : function(e,x,y){
        		$('.joint-cell.selected-cell').not('[linked=true]').each(function(idx){
        			$(this).removeClass('selected-cell');
        		});
        		
        		var modelid = this.model.id;
        		var mid = this.model.attributes.mid;
        		var gHeight = $('g[model-id='+modelid+'] rect[joint-selector=body]').attr('height')/2; 
            	$('g[model-id='+modelid+']').addClass('selected-cell');
            	var x = $('g[model-id='+modelid+']').offset().left+5;
            	var y = $('g[model-id='+modelid+']').offset().top+gHeight;
            	$('.div_diagram_menu').css('left',x);
        		$('.div_diagram_menu').css('top',y);
        		$('.div_diagram_menu').show();
        		
        		$('.diagram-btn').attr('model-id',modelid);
        		$('.diagram-btn').attr('mid',mid);
    		}
    	}) 
	});
 	
  	//아이콘을 드롭했을 때 옵션 기존 클래스를 지우고 dropped 클래스 추가 및 css 설정
    var dropOpts = {
   		tolerance: 'fit',
        drop: function(e, ui) {
        	if(ui.draggable.hasClass('draggable')) {
            	var cloneImg = ui.draggable.clone(), //div 복사
                	cloneDragOpts = {
                    	containment: 'parent'
                	};
                cloneImg.css({
	                position: 'absolute', 
	                top: ui.offset.top ,// 객체 생성위치.
	                left: ui.offset.left ,  
				}).draggable(cloneDragOpts); //style 추가 하면서 draggable.
				var uuid = uuidv4();
				var name = cloneImg.text().trim();
				var nameArr = name.split(" ");
				var rName = "";
				var refY = 0.4;
				if(nameArr.length>3){
					rName=nameArr.splice(0,2).join(" ")+'\n'+nameArr.join(" ");
					//refY=0.2;
				}else{
					rName=name;
				}
				
				var m1 = new joint.shapes.standard.HeaderedRectangle();
				m1.size(150, 100);
				m1.position(ui.offset.left-395, e.offsetY-20);
				m1.attr('root/title', 'joint.shapes.standard.HeaderedRectangle');
				m1.attr('header/fill', 'lightblue');
				m1.attr('headerText/text', rName);
				m1.attr('headerText',{'font-size':12});
				
				//드레그한 원본 컴포넌트의 id로  m1 의 id를 변경
                m1.attributes.id	=uuid; //cloneImg.attr('id')
                m1.id				=uuid; //위아래가 같아야지 오류발생 안함. //TODO 이 값 고유 id로 지정하여 DB에 저장할때 object ID로 변경해야한다 0708
                m1.attributes.mid   =cloneImg.attr('id');
                m1.mid				=cloneImg.attr('id');
				graph.addCell(m1);
        	}
            getSVG();
		} //drop end
	};
	$('#myDiagramDiv').droppable(dropOpts);
    
 	// 이 클래스를 제거해야 드레그 문제가 해결됨
    $('#myDiagramDiv').removeClass('joint-theme-default');
	
	$('body').click(function(e){
		if(e.target.tagName=="svg"){
			$('.div_diagram_menu').hide();
			$('.joint-cell.selected-cell').removeClass('selected-cell');
		}
	});
	
	$('#div_open,#div_del,#div_link,#div_run')
	.mouseenter(function(e){
		$(this).css('border','2px solid black');	
	}).mouseleave(function(e){
		$(this).css('border','1px solid black');
	});
	
	$('#div_open').click(function(e){
		var modelid = $(this).attr('model-id');
		opneIframe(modelid);
		$('.div_diagram_menu').hide();
		$('.joint-cell.selected-cell').removeClass('selected-cell');
	});
	
	$('#div_del').click(function(e){
		var modelid = $(this).attr('model-id');
		var mid = $(this).attr('mid');
		var param = {
			subpjtid  : subPjtId,
			modeluid  : modelid,
			modeltype : mid
		};
		$.doPost({
			url : "/mliframe/removeModelCSV.do",
			data : param,
			success : function(data){
				if(data.msg=="success"){
					var model = graph.getCell(modelid);
					var link = graph.getConnectedLinks(model);
					graph.removeCells(model);
					graph.removeLinks(link);
					getSVG();
				}
			},
			error : function(jqxXHR, textStatus, errorThrown){
				commonFunc.ajaxFailAction(jqxXHR);
			}
		});
		$('.div_diagram_menu').hide();
	});
	
	$('#div_link').click(function(e){
		if($('.joint-cell[linked=true]').length>0){
			var aModelId = $('.joint-cell[linked=true]').attr('model-id');
			var bModelId = $(this).attr('model-id');
			var link = new joint.dia.Link({
			    source: { id: aModelId },
			    target: { id: bModelId },
			    attrs: 	{
					'.connection' 	: { stroke: 'gray', 'stroke-width': 2 },                    
					'.marker-target' : { fill: 'gray', stroke: 'gray', d: 'M 10 0 L 0 5 L 10 10 z' } 
			    }
			});
			link.router('manhattan');
			if(aModelId!=bModelId){
				link.addTo(graph);	
			}
			$('g[model-id='+aModelId+']').removeClass('selected-cell');
			$('g[model-id='+bModelId+']').removeClass('selected-cell');
			$('g[model-id='+aModelId+']').attr('linked','false');
			getSVG();
		}else{
			var modelid = $(this).attr('model-id');
			$('g[model-id='+modelid+']').attr('linked','true');
		}
		$('.div_diagram_menu').hide();
	});
	
	$('#div_run').click(function(e){
		alert('준비중 입니다.');
		$('.joint-cell.selected-cell').removeClass('selected-cell');
		$('.div_diagram_menu').hide();
	});
});

graph.on('change:source change:target', 
	function(link) { 
		if (link.get('source').id && link.get('target').id) {
			var param = {
				key : "linked",
				sourceId : link.get('source').id,
				targetId : link.get('target').id,
				subProjectId : subPjtId
			};
			setSourceTarget(param); 
		}	
	}
);

graph.on('remove',function(cell){
	if(cell.isLink()){
		getSVG();
	}
});

function uuidv4() { //objectID를 유니크하게 생성하기 위해서 사용.
	return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    	var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    	return v.toString(16);
	});
}
	
function getParameterByName(name) { //파라미터값 추출 함수.
	name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),results = regex.exec(location.search);
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var sourceidStr = "";
function opneIframe(modelUid){
	var sourceIds = [];
	var model = graph.getCell(modelUid);
	var links = graph.getLinks();
	for(var i=0;i<links.length;i++){
		if(modelUid==links[i].attributes.target.id){
			sourceIds.push(links[i].attributes.source.id);
		}
	}
	sourceidStr = sourceIds.join("|");
	var mid = model.attributes.mid;
	var viewName = $('div#'+mid+' div.modalbtn').text().split(" ").join("");
	$('.modal-title').text($('div#'+mid+' div.modalbtn').text());
	var srcPath = "/mliframe/controlView.do?modelUid="+modelUid+"&&viewName="+viewName+"&&mid="+mid;
	$("#ifraList").show();
	$("#ifraList").attr("src",srcPath);  
    $('#myModal').modal("show");
    $('#myModal').draggable({handle : ".modal-header"});
    
    // Olap 팝업일 경우
    if(mid == "da001") {
    	$(".modal-dialog").addClass("modal-olap");
    } else {
    	$(".modal-dialog").removeClass("modal-olap");
    } 
}

function setSourceTarget(updatedata){ //source 와 target setting function
	 $.ajax({
		type : "POST", 
		url : OlapUrlConfig.updateSubContents, 
		data : JSON.stringify(updatedata),
		contentType: "application/json;charset=UTF-8",
		success : function(data, status, xhr){
			if(status === "success"){
				getSVG();
			}else{
				console.log("flowchart save failed")
			}
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
}
   
function getSVG(){ //getSVG 값을 DB에 저장.
	var param = {
		key:"display",
		value : JSON.stringify(graph), //js값이나 객체를 json 형태의 String 객체로 변환
		subProjectId : subPjtId
	};
	$.doPost({
		url : "/project/updateSubContents.do", //subProject update => projectServiceimpl 에서 분기처리
		data : param,
		success : function(data, status, xhr){
			if(data.msg === "success"){
				//console.log("flowchart save success")
			}else{
				console.log("flowchart save failed")
			}
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});  
}
function setSVG(contents){ //DB에 저장된 contents값(json data)을 graph로. 
	var json=contents;
	graph.fromJSON(JSON.parse(json));
};
</script>
<body>
  <div style="width: 100%; display: flex; justify-content: space-between; margin-top: 20px;">
  	<div style="width: 370px">
    	<div id="paletteZone" style="width: 98%;">
      		<h3 class="acc-toggler">Data I/O</h3>
       		<div id="div_dl">
	  			<div id="dl001" class="draggable">
	  				<div class="bd-highlight"></div>
	  				<div class="bd-highlight modalbtn">Table</div>
	  			</div>
	      	 	<div id="dl002" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">CSV</div>
	      	 	</div>
	      	 	<div id="dl003" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">SQL</div>
	      	 	</div>
	      	 	<div id="dl005" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Image</div>
	      	 	</div>
	      	 	<div id="dl004" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Data Import</div>
	      	 	</div>
      	 	</div>
      	 	<h3 class="acc-toggler">Data Pre-processing</h3>
      	 	<div id="div_dm">
	      	 	<div id="dm001" class="draggable">
	       			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Replace Abnormal Number</div>
	       		</div>
	       		<div id="dm002" class="draggable">
	       			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Replace Abnormal String</div>
	       		</div>
	      	 	<div id="dm003" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Delete Missing Data</div>
	      	 	</div>
	      	 	<div id="dm012" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Remove Abnormal String</div>
	      	 	</div>
	      	 	<div id="dm010" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">String to Column</div>
	      	 	</div>
	      	 	<div id="dm013" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Trim Whitespace</div>
	      	 	</div>
	      	 	<div id="dm014" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Substring</div>
	      	 	</div>
	      	 	<div id="dm017" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Concatenate</div>
	      	 	</div>
	      	 	<div id="dm004" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Distinct</div>
	      	 	</div>
	      	 	<div id="dm009" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Sort</div>
	      	 	</div>
	      	 	<div id="dm006" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Encoder</div>
	      	 	</div>
	      	 	<div id="dm011" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Decoder</div>
	      	 	</div>      	
	      	 	<div id="dm007" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Normalization</div>
	      	 	</div>
	      	 	<div id="dm005" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Split Data</div>
	      	 	</div>
	      	 	<div id="dm008" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Join</div>
	      	 	</div>
	      	 	<div id="dm015" class="draggable">
	      	 		<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Correlation</div>
	      	 	</div>
	      	 	<div id="dm016" class="draggable">
      	 			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Evaluation</div>
	      	 	</div>
	      	 	<div id="dm017" class="draggable">
      	 			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Image To Vector</div>
	      	 	</div>
	      	 	<div id="dm018" class="draggable">
      	 			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Pivot</div>
	      	 	</div>
	      	 	<div id="dm019" class="draggable">
      	 			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Unpivot</div>
	      	 	</div>
	      	 	<div id="dm020" class="draggable">
      	 			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Capitalize</div>
	      	 	</div>
	      	 	<div id="dm021" class="draggable">
      	 			<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Uncapitalize</div>
	      	 	</div>
      	 	</div>
      	 	<h3 class="acc-toggler">Model 생성 및 예측</h3>
      	 	<div id="div_ml">
	      	    <div id="tr001" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Linear Regression Train</div>
	       		</div>
	       		<div id="pr001" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Linear Regression Predict</div>
	       		</div>	
	       		<div id="tr002" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Logistic Regression Train</div>
	       		</div>
	       		<div id="pr002" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Logistic Regression Predict</div>
	       		</div>
	       		<div id="tr003" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">KNN Train</div>
	       		</div>
	       		<div id="pr003" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">KNN Predict</div>
	       		</div>
	       		<div id="tr004" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Gaussian Naive bayes Train</div>
	       		</div>
	       		<div id="pr004" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Gaussian Naive bayes Predict</div>
	       		</div>
	       		<div id="tr005" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Support Vector Machine Train</div>
	       		</div>
	       		<div id="pr005" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Support Vector Machine Predict</div>
	       		</div>
	       		<div id="tr006" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Decision Tree Classification Train</div>
	       		</div>
	       		<div id="pr006" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Decision Tree Classification Predict</div>
	       		</div>
	       		<div id="tr016" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Decision Tree Regression Train</div>
	       		</div>
	       		<div id="pr016" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Decision Tree Regression Predict</div>
	       		</div>
	       		<div id="tr007" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Random Forest Classification Train</div>
	       		</div>
	       		<div id="pr007" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Random Forest Classification Predict</div>
	       		</div>
	       		<div id="tr017" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Random Forest Regression Train</div>
	       		</div>
	       		<div id="pr017" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Random Forest Regression Predict</div>
	       		</div>
	       		<div id="tr008" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">XGBoost Classification Train</div>
	       		</div>
	       		<div id="pr008" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">XGBoost Classification Predict</div>
	       		</div>
	       		<div id="tr009" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">XGBoost Regression Train</div>
	       		</div>
	       		<div id="pr009" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">XGBoost Regression Predict</div>
	       		</div>
	       		<div id="tr010" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">K-means</div>
	       		</div>
	       		<div id="tr011" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">DBScan</div>
	       		</div>
	       		<div id="tr012" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Arima Train</div>
	       		</div>
	       		<div id="pr012" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Arima Predict</div>
	       		</div>
	       		<div id="tr013" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Collaborative Filtering NMF</div>
	       		</div>
	       		<div id="tr018" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Collaborative Filtering Similarity</div>
	       		</div>
	       		<div id="tr014" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">LSTM Train</div>
	       		</div>
	       		<div id="pr014" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">LSTM Predict</div>
	       		</div>
	       		<div id="tr015" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">CNN Train</div>
	       		</div>
	       		<div id="pr015" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">CNN Predict</div>
	       		</div>
      	 	</div>
      	 	<h3 class="acc-toggler">Model 배포</h3>
      	 	<div id="div_dp">
	      	    <div id="dp001" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Model Deploy</div>
	       		</div>
	       		<div id="dp002" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Deploy Test</div>
	       		</div>
      	 	</div>
      	 	<h3 class="acc-toggler">데이터 분석</h3>
      	 	<div id="div_dp">
      	 		<div id="da001" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Olap</div>
	       		</div>
	       		<div id="da002" class="draggable">
	      	    	<div class="bd-highlight"></div>
	      	 		<div class="bd-highlight modalbtn">Visualization</div>
	       		</div>
	       	</div>
    	</div>
    </div>
    <div id="maindiv">
    	<div id="myDiagramDiv">
    		<div id="canvas">	
     			<svg id='connector_canvas'></svg>
    		</div>
    	</div>
    </div>
  </div>
<ul class="rightmenu">
	<li><a href="#" class="delbtn">삭제</a></li>
</ul>
<!-- 작업 그리드에 객체 클릭 했을 경우 모달 창 -->
 <div class="modal" id="myModal">
	<div class="modal-dialog modal-xl" style="max-width: 1400px; height : 680px">
		<div class="modal-content" style="height : inherit;">
		<!-- Modal Header -->
		<div class="modal-header" style="padding: 5px;">
			<div class=" h5 modal-title"></div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
		</div>
		<div class="modal-body">
			<iframe id="ifraList" name="ifraList"  width="100%" height="100%" frameborder="0" scrolling="no" style="display: none;"></iframe>			
		</div>
		</div>
	</div>
</div>  
<div class="div_diagram_menu">
	<div id="div_open" class="diagram-btn"><img src="/images/common/icons/diagram_open.png" style="width:28px;"/></div>
	<div id="div_run" class="diagram-btn"><img src="/images/common/icons/diagram_run.png" style="width:28px;"/></div>
	<div id="div_link" class="diagram-btn"><img src="/images/common/icons/diagram_link.png" style="width:25px;"/></div>
	<div id="div_del" class="diagram-btn"><img src="/images/common/icons/diagram_del.png" style="width:25px;"/></div>
</div>
</body>
</html>