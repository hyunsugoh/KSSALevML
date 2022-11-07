'use strict';
/******************************************************** 
파일명 : dashboard.js 
설 명 : User page의 DashBoard JavaScript
수정일	수정자	Version	Function 명
-------	--------	----------	--------------
2019.03.04	최 진	1.0	최초 생성
2009.03.04	최 진	1.0	fn_check_period
 *********************************************************/

/**
 * 
Object Dataset 
설	명 : 전역변수로 화면제어를 위한 DataSet
 */
var dashboard_jsconfig = {
		objectDetailId1:"",
		objectDetailId2:"",
		conditionDomId : "",
		datepickerStartId :"",
		datepickerEndId:"",
		areaDomId : "",
		orderbyDomId : "",
		orderbyAreaId : "",
		gridElemId : "",
		createConditionElemCnt : 0,
		drawConditionBaseElem:{
			selectId:'conditionSelect',
			parentElem01:'<div class="form-inline data-condition-form d-flex justify-content-center" id="',
			closeTag : '">',
			parentElem02: '',
			selectElem01 : '<select class="form-control form-control-sm my-1 mr-1 data-condition-name-select" >',
			setOptionElem01: '<option value="',
			setOptionCloseElem:'</option>',
			parentCloseElem01: '</select>',
			inputElem01: '<input type="text" class="form-control form-control-sm my-1 mr-1 data-condition-value-select" placeholder="텍스트 입력">',
			parentElem03: '<select class="form-control form-control-sm my-1 mr-1 data-condition-oper-select">',
			parentCloseElem02: '</select>',
			closeIconElem:'<div>'+
			'<button class="btn btn-sm btn-outline-secondary fn-delConditionIcon">'+
			'<i class="fas fa-times-circle"></i>'+
			'</button></div></div>'
		}
		
		
}

var dashboard_config = {
		objectDetailId1:"",
		objectDetailId2:"",
		conditionDomId : "",
		datepickerStartId :"",
		datepickerEndId:"",
		areaDomId : "",
		orderbyDomId : "",
		orderbyAreaId : "",
		gridElemId : "",
		createConditionElemCnt : 0,
		drawConditionBaseElem:{
			selectId:'conditionSelect',
			parentElem01:'<div class="form-inline data-condition-form d-flex justify-content-center" id="',
			closeTag : '">',
			parentElem02: '',
			selectElem01 : '<select class="form-control form-control-sm my-1 mr-1 data-condition-name-select" >',
			setOptionElem01: '<option value="',
			setOptionCloseElem:'</option>',
			parentCloseElem01: '</select>',
			inputElem01: '<input type="text" class="form-control form-control-sm my-1 mr-1 data-condition-value-select" placeholder="텍스트 입력">',
			parentElem03: '<select class="form-control form-control-sm my-1 mr-1 data-condition-oper-select">',
			parentCloseElem02: '</select>',
			closeIconElem:'<div>'+
			'<button class="btn btn-sm btn-outline-secondary fn-delConditionIcon">'+
			'<i class="fas fa-times-circle"></i>'+
			'</button></div></div>'
		},
		createOrderByElemCnt:0,
		drawOrderByBaseElem : {
			selectId:'colOrderbySelect',
			closeElemTag:'">',
			template01:'<div class="form-inline data-orderby-form d-flex justify-content-center" id="',
			template02:'<select class="form-control form-control-sm my-1 data-orderby-name">',
			template03:'</select>'+
						'<span class="my-1 orderby-name-center text-center"> 기준으로 </span>'+
						'<select class="form-control form-control-sm my-1 mr-sm-1 data-orderby-value">',
			template04:'</select>',
			delBtnTemplate:'<button class="btn btn-sm btn-outline-secondary fn-delOrderByIcon"><i class="fas fa-times-circle"></i></button>'
		},
		orderByValInfoAry : [
			{
				drawNm : "오름차순",
				value:"ASC"
			},
			{
				drawNm : "내림차순",
				value:"DESC"
			}
		],
		fn_datepicker_clear : function(){},
		conditionNoticeText : "조건을 추가하려면<br> [조건추가]버튼을 클릭하십시오.",
		orderbyNoticeText : "정렬을 추가하려면 항목을 선택 후<br> [정렬추가]버튼을 클릭하십시오.",
		objDetailAggreateConnTxt:"의 ",
		detailObjAggreateInfoAry: [
			{
				drawNm:"없음",
				value: null
			},
			{
				drawNm:"평균",
				value:"AVG"
			},{
				drawNm:"최소",
				value:"MIN"
			},{
				drawNm:"최대",
				value:"MAX"
			},{
				drawNm:"합계",
				value:"SUM"
			}
		]
};
var userDataset = {
		objSelect1 : "",			// 유저가 선택한 선택박스 value
		selectedObjInfo1 : {},		// 유저가 선택한 객체 정보 1
		objSelect2 : "",			// 유저가 선택한 객체 value
		selectedObjInfo2 : {},		// 유저가 선택한 객체 정보 2,
		drawingCondition:{},			// 유저가 '조건추가' 버튼을 클릭하여 생성한 정보
		clickedObjectDetailInfo:[],
		resultDataset : {}
};
var dataset = {
		object : [], 				// 객체 기본 정보
		objectRelInfo : {},			// 객체 관계 정보
		objectDetailInfo1:[],		// 첫 번째 객체 세부 정보
		objectDetailInfo2:[],		// 두 번째 객체 세부 정보
		objectConditionInfo1:[],	// 첫 번째 객체 조건 정보
		objectConditionInfo2:[],	// 두 번째 객체 조건 정보
		conditionDataset:[],
		orderbyDataset:[]
};

/************************************************************************/ 
/*
함수명 : fn_view_object_selectbox_initNDraw
설	명 : Dashboard 페이지의 '객체선택' 선택박스를 그리는 function
인	자 : domId( SelectBox Element Id),	
		data( 데이터 type: Array or JSON), 
		fnEvt( 선택박스를 변경할때 수행할 이벤트 바인드 type: function)
사용법 : 

작성일 : 2019-03-05
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.05	최진	최초 작성
 */
/************************************************************************/
var fn_view_object_selectbox_initNDraw = function(domId, data, fnEvt){
	var drawSelectOptionElem = function(domId, _dataElem){
		if(_dataElem.hasOwnProperty("tableName")){
			$(domId).append('<option value="'+_dataElem["tableName"]+
					'" data-toggle="tooltip" data-placement="bottom" title="'+_dataElem["objectDescription"]+'" >'+
					_dataElem["objectName"]+'</option>');
		}else{
			// console.log("The key does not exist in the data object. [tableName]");
		}
	};

	domId = "#"+domId;
	$(domId).empty();
	$(domId).append("<option value='notSelected'>- 선택하십시오 -</option>");
	if(data instanceof Array && data.length > 0){
		var _dataObj = {};
		for(var i in data){
			_dataObj = data[i];
			drawSelectOptionElem(domId, _dataObj);
		}
	}


	if($(domId+" > option").length > 1){
		if(fnEvt !== undefined && fnEvt !== null){
			$(domId).off("change").bind("change", fnEvt);	
		}

	}else{
		$(domId).off("change");
	}

};

/************************************************************************/ 
/*
함수명 : fn_search_dataset_selected_objInfo
설	명 : 기본객체 정보 dataset에서 두번째 인자값에 해당하는 데이터를 검색
인	자 : _selectedValue(유저가 선택한 selectBox Value type:String), fnCallback(리턴받을 콜백함수(파라미터:결과데이터:type:array) type: function)
사용법 : 
호출 시 첫번째 인자에 SelectBox에서 추출한 value를 넣고 두번째인자에 콜백함수를 넣음.
유저가 선택한 객체의 관계된 데이터 배열을 리턴
작성일 : 2019-03-05
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.05	최진	최초 작성
 */
/************************************************************************/
var fn_search_dataset_selected_objInfo = function(_datasetAry, searchName){
	var rtnData = {}; 

	if(_datasetAry !== undefined && _datasetAry !== null && _datasetAry instanceof Array && searchName !== undefined && searchName !== null && searchName !== ""){
		var i, _dataObj;
		for(i in _datasetAry){
			_dataObj = _datasetAry[i];
			if(_dataObj.hasOwnProperty("tableName") &&
					_dataObj["tableName"] === searchName){
				rtnData = _dataObj;
				break;
			}
		}
	}else{
		// console.log("파라미터 값이 없음");

	}
	return rtnData;
};
/************************************************************************/ 
/*
함수명 : fn_call_object_rel_info
설	명 : 첫 번째 선택박스에서 '객체선택' 시 선택 객체를 가져오는 function
인	자 : _selectedValue(유저가 선택한 selectBox Value type:String), fnCallback(리턴받을 콜백함수(파라미터:결과데이터:type:array) type: function)
사용법 : 
호출 시 첫번째 인자에 SelectBox에서 추출한 value를 넣고 두번째인자에 콜백함수를 넣음.
유저가 선택한 객체의 관계된 데이터 배열을 리턴
작성일 : 2019-03-05
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.05	최진	최초 작성
 */
/************************************************************************/
var fn_call_object_rel_info = function(_stdName, fnCallback){
	if(_stdName !== undefined && 
			_stdName !==null &&
			_stdName !== "" && 
			_stdName !== "notSelected"){


		if(dataset.objectRelInfo.hasOwnProperty(_stdName) === false){
			// ajax 통신
			$.ajax({
				type:"GET",
				url:OlapUrlConfig.getObjRelInfo,
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				data: {
					"stdName" : _stdName
				}
			}).done(function(_result) {
				if(_result !== undefined && _result !== null &&
						_result.hasOwnProperty(_stdName)){
					// 결과 callback
					if(fnCallback !== undefined && fnCallback !== null){
						fnCallback(_result[_stdName]);
					}else{
						// console.log("fn_call_object_rel_info 리턴할 콜백함수가 파라미터에 없습니다.");
					}
				}else{
					// console.log("fn_call_object_rel_info 통신에 성공하였으나 데이터가 없습니다.");
				}
			}).fail(function(_failObj){
				if (_failObj.status == 401) {
		            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
		            	window.location.replace(OlapUrlConfig.loginPage);
		            });
		             
		             
		         } else if (_failObj.status == 403) {
		            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
		            	window.location.replace(OlapUrlConfig.loginPage);
		            });
		              
		         }
				// console.log("fn_call_object_rel_info 통신실패");
			});
		}else{
			// dataset return
			if(fnCallback !== undefined && fnCallback !== null){
				fnCallback(dataset.objectRelInfo[_stdName]);
			}
		}

	}
	// else 일경우 아무동작 안함
};

/************************************************************************/ 
/*
함수명 : fn_search_second_object
설	명 : 유저가 첫번째 선택박스를 선택할 경우 그와 관계된 Object들을 찾아서 리턴
인	자 : _objectDataset(), _joinObjectDataset()
사용법 : 

작성일 : 2019-03-05
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.05	최진	최초 작성
	2019.03.11	최진    버그 수정
 */ 
/************************************************************************/
var fn_search_second_object = function(_objectDataset, _joinObjectDataset){
	var _joinedObjectDataKeys = [],
	_returnObjDataset = [],
	_searchedObj = {},
	_joinedObj = {};

	// 선택한 객체와 관계있는 테이블 리스트 추출
	for(var i in _joinObjectDataset){
		_joinedObj = _joinObjectDataset[i];
		if(_joinedObj.hasOwnProperty("stdTable") && _joinedObjectDataKeys.indexOf(_joinedObj["stdTable"]) === -1){
			_joinedObjectDataKeys.push(_joinedObj["connTable"]);
		} 
	}
	// 테이블 리스트의 정보 추출
	for(var j in _objectDataset){
		_searchedObj = _objectDataset[j];
		if(_searchedObj.hasOwnProperty("objectName") && _joinedObjectDataKeys.indexOf(_searchedObj["tableName"]) > -1){
			_returnObjDataset.push(_searchedObj);
		}
	}
	return _returnObjDataset;
};

/************************************************************************/
/*
함수명 : fn_call_object_detail_info
설	명 : DB에서 객체 세부정보 호출(개체정보관리)
인	자 : tableName(테이블 명, type:String), fnCallback(통신 후 콜백함수 type:function)
사용법 : 

작성일 : 2019-03-04
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.03.04	최진	최초 작성
 */
/************************************************************************/
var fn_call_object_detail_info = function(_tableName, _fnCallback){

	$.ajax({
		type:"GET",
		url:OlapUrlConfig.objDetailInfo,
		 beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
		data : {
			tbName : _tableName
		}
	}).done(function(_result){
		_fnCallback(_result);
	}).fail(function(_failObj){
		// console.log("fail fn_call_object_detail_info");
		// console.log(_failObj);
		_fnCallback([]);
		if (_failObj.status == 401) {
            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
            	window.location.replace(OlapUrlConfig.loginPage);
            });
         } else if (_failObj.status == 403) {
            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
            	window.location.replace(OlapUrlConfig.loginPage);
            });
         }else if(_failObj.status === 400){
			alert("데이터를 불러오는 데에 실패하였습니다. 관리자에게 문의하여 주십시오.");
		}else{
			alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
		}
	}); // AJAX
};
/************************************************************************/ 
/*
함수명 : fn_init_userNdataset
설	명 : 유저가 선택한 dataset을 초기화하는 함수
인	자 : _flag(구분값) : 
사용법 : 
_flag값이 select2일 경우 두번째 선택값만 초기화
작성일 : 2019-03-06
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.06	최진	최초 작성
 */
/************************************************************************/
var fn_init_userNdataset = function(_flag){
	// TODO 객체 자체를 복사해서 초기화 하는 방법이 효율적인지 고민해보자
	if(_flag === "total"){
		// 유저가 클릭한 객체 세부정보 clear
		fn_clear_user_clicked_objectDetail_dataset("total");
		
		// dataset Clear
		dataset.objectDetailInfo1 = [];
		dataset.objectConditionInfo1 = [];
		
		// userDataset Clear
		userDataset.objSelect1 = "";
		userDataset.selectedObjInfo1 = {};
		dashboard_config.fn_datepicker_clear();
	}

	// dataset Clear
	// 유저가 클릭한 객체 세부정보 clear
	var _clearDataAryLen = dataset.objectDetailInfo2.length;
	fn_clear_user_clicked_objectDetail_dataset("objectDetailInfo2", _clearDataAryLen);
	dataset.objectDetailInfo2 = [];
	dataset.objectConditionInfo2 = [];
	// userDataset Clear
	userDataset.objSelect2 = "";
	userDataset.selectedObjInfo2 = {};

	userDataset.drawingCondition = {};

	
	
	dataset.conditionDataset=[];
	dataset.orderbyDataset = [];
	
	
	dashboard_config.createConditionElemCnt = 0;
	dashboard_config.createOrderByElemCnt = 0;
};

/************************************************************************/ 
/*
함수명 : fn_clear_user_clicked_objectDetail_dataset
설	명 : 유저가 선택한  객체 세부정보 dataset 을 초기화하는 함수
인	자 : _flag(구분값) : 
사용법 : 
_flag값이 select2일 경우 두번째 선택값만 초기화
작성일 : 2019-03-06
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.06	최진	최초 작성
 */
/************************************************************************/
var fn_clear_user_clicked_objectDetail_dataset = function(_flag, _dataLen){
	if(_flag === "objectDetailInfo2"){
		var _searchVal = "", _searchedIdx="";
		for(var a=0;a<_dataLen;a++){
			_searchVal = ""+_flag+"_"+a;
			_searchedIdx =userDataset.clickedObjectDetailInfo.indexOf(_searchVal); 
			if(_searchedIdx > -1){
				userDataset.clickedObjectDetailInfo.splice(_searchedIdx,1);
			}
		}
	}else{
		userDataset.clickedObjectDetailInfo = [];
	}
};
/************************************************************************/ 
/*
함수명 : fn_draw_object_detail
설	명 : 유저가 선택한 객체의 세부정보(객체정 체크박스 그리기
인	자 : _objDetailDomId(CheckBox를 그리기 위한 부모 id type:String), objectDetailInfoAry()
사용법 : 

작성일 : 2019-03-05
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.05	최진	최초 작성
 */
/************************************************************************/
var fn_draw_object_detail = function(_objDetailDomId, _objectDetailInfoAry){
	var _objValue_01 = ""+_objDetailDomId;
	_objDetailDomId = "#" + _objDetailDomId;
	var _createElem01 = '<div  data-toggle="tooltip" data-placement="bottom" title="',
	_createElem02 = '" class="form-check custom-detail-div" ><input type="checkbox" class="d-inline-block chbox" name="check" value="',
	_createElem03 = '" id="',
	_createElem04 = '"><label for="',
	_createElem05 =  '"><span class="custom-checkbox"></span></label>',
	_createElem06 =  '<div class="d-inline-block text-left evt-select-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width text-truncate" >',
	_createElem06_ifFuncExtElem =  '<div class="d-inline-block text-left evt-select-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width-funcext text-truncate" >',
	_createElem07_ifFuncExtElem = '</div>',
	_createElem07 = '</div></div>',
	
	
	//TODO 2019-06-26 추가
	_funcSelectBox = '<div class="d-inline custom-detail-chkbox-selctboxdiv-width-funcext">'+
	    '<div class="funcext-message-add"><span>의</span></div>'+'<div class="d-inline-block custom-detail-chkbox-selctboxdiv-width-funcext"><select style="height:32px;" class="form-control form-control-sm ml-1">'+
		'</select></div></div>';
	
	var selectAll ='<div class="form-check custom-detail-div" >'+
		'<input type="checkbox" class="checkAll d-inline-block chbox" name="check">'+
		'<label><span class="custom-checkbox"></span></label>'+
		'<div class="d-inline-block text-left evt-select-all-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width text-truncate" style="border: none !important;" >전체 선택</div></div>';
		
		

	if(_objDetailDomId !== undefined && _objDetailDomId !== null&& _objectDetailInfoAry instanceof Array && _objectDetailInfoAry.length > 0){
		var i, _chkValue, _chkName, _chkDescript, _opidx;
		
		$(_objDetailDomId).append(selectAll);
		$(_objDetailDomId).find(".checkAll").attr("id",_objDetailDomId.substr(1, _objDetailDomId.length-1)+"_checkAll");
		$(_objDetailDomId).find(".checkAll").siblings('label').attr('for', _objDetailDomId.substr(1, _objDetailDomId.length-1)+"_checkAll");
		
		for(i in _objectDetailInfoAry){
			_chkValue = _objValue_01 + "_"+i;
			_chkName = _objectDetailInfoAry[i]["objInfoName"];
			_chkDescript = _objectDetailInfoAry[i]["objInfoNameDesc"];
			
			// 06.28  집계 함수 분기
			if(_objectDetailInfoAry[i]["calcFuntionYn"] === "Y"){
				$(_objDetailDomId).append(_createElem01+_chkDescript+_createElem02+_chkValue+_createElem03+_chkValue+_createElem04+_chkValue+_createElem05+_createElem06_ifFuncExtElem+_chkName+_createElem07_ifFuncExtElem+_funcSelectBox+_createElem07_ifFuncExtElem);
				var $selectElem =$("#"+_chkValue).siblings(".custom-detail-chkbox-selctboxdiv-width-funcext").find("select");
				for(_opidx in dashboard_config.detailObjAggreateInfoAry){
					$selectElem.append(new Option(dashboard_config.detailObjAggreateInfoAry[_opidx]["drawNm"], _opidx));
				}
				//집계함수 셀렉트 박스 위치 조정을 위한 클래스 추가 
				$(".funcext-message-add").parent().siblings('label').addClass("funcext-chkbox-adjust");

				// 집계함수 default값 추가
				if(dashboard_config.detailObjAggreateInfoAry.length >0){
					dataset[_objValue_01][i]["calcFunc"] = dashboard_config.detailObjAggreateInfoAry[0]["value"];
				}
				
			}else{
				$(_objDetailDomId).append(_createElem01+_chkDescript+_createElem02+_chkValue+_createElem03+_chkValue+_createElem04+_chkValue+_createElem05+_createElem06+_chkName+_createElem07);	
			}
			
		}
		
		// 06.28 추가:집계함수 이벤트 바인드
		$(".custom-detail-chkbox-selctboxdiv-width-funcext").find("select").on("change", function(e){
			var _aggreatedSelectedVal =$(this).val(),
				_selectedDatasetLocateAry = $(this).parent().parent().siblings('input:checkbox[name="check"]').val().split("_");
			
			if(_selectedDatasetLocateAry.length === 2){
				var _datasetObjInfoName =_selectedDatasetLocateAry[0],
				_datasetObjInfoIdx = _selectedDatasetLocateAry[1];
				// 전역 객체에 집계함수 값 input
				dataset[_datasetObjInfoName][_datasetObjInfoIdx]["calcFunc"] = dashboard_config.detailObjAggreateInfoAry[_aggreatedSelectedVal]["value"];
				
				// 만약 체크박스가 true라면 한번 더 호출
				if($(this).parent().parent().siblings('input:checkbox[name="check"]').prop('checked')){
					fn_draw_only_column_jsGrid(userDataset.clickedObjectDetailInfo);
				}
				
				
			}
		});
		

		
		// 03.28 css 변경
		$(_objDetailDomId).find(".evt-select-detail-chkbox").on("click",function(e,allObj){

			var objState = $(this).siblings('input:checkbox[name="check"]').prop('checked');
			var allState=null;

			if(allObj !== undefined && allObj.hasOwnProperty("allSelectSatate")){
				allState = allObj.allSelectSatate;
			}
			
			if(allObj === undefined || allState !== objState){
				$(this).siblings('input:checkbox[name="check"]').prop('checked',function(){
			        return !$(this).prop('checked');
				});
				var chkVal = $(this).siblings('input:checkbox[name="check"]').val();
				fn_evt_detailChk_bind(e, chkVal);
								
				if(allObj === undefined){
					var _chkAllState = $(_objDetailDomId).find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked');
					
					if(_chkAllState){
						//$(_objDetailDomId+"_checkAll").prop('checked', false);
						$(_objDetailDomId).find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);
						
						//console.log($(_objDetailDomId+"_checkAll").prop('checked'));
					}
					
				}

			}
			
		});
		 
		// 03.18 이벤트 바인드 로직 및 데이터 세팅 추가
		$(_objDetailDomId).find(".evt-select-detail-chkbox").siblings('input:checkbox[name="check"]').on('change',function(e){
			
			var chkVal = $(this).val();
		    fn_evt_detailChk_bind(e,chkVal);
			
			
		    var _chkAllState =$(_objDetailDomId+"_checkAll").prop('checked');
					
					if(_chkAllState){
						$(_objDetailDomId+"_checkAll").prop('checked', false);
					
						//console.log($(_objDetailDomId+"_checkAll").prop('checked'));
					}
			 
		});
		
		$(_objDetailDomId).find('.form-check').hover(function() {
			  $(this).tooltip({ boundary: 'window' });
			  $(this).tooltip('show');

		}, function(){
		  $(this).tooltip('hide');
		  // 만약 툴팁이 존재할 경우 모든 툴팁 닫기
		  if($(".tooltip-inner").length > 0){
			  $(".tooltip").tooltip("hide");
		  }
		});
		
	     //전체체크 (그리드 실행후 동작)
		$(_objDetailDomId).find('.evt-select-all-detail-chkbox').on('click',function(e){
			
			$(this).siblings('input:checkbox[name="check"]').prop('checked',function(){
		        return !$(this).prop('checked');
		  });
			
			var _checkedState = $(this).siblings('input:checkbox[name="check"]').prop('checked');
			$(_objDetailDomId).find(".evt-select-detail-chkbox").trigger('click',{allSelectSatate:_checkedState});
			
	    });
		
		$(_objDetailDomId+"_checkAll").on("change",function(e){
			var _checkedState =$(_objDetailDomId+"_checkAll").prop('checked');
			
		     $(_objDetailDomId).find(".evt-select-detail-chkbox").trigger('click',{allSelectSatate:_checkedState});
			 
		});
		
	}else if(_objectDetailInfoAry instanceof Array && _objectDetailInfoAry.length === 0){
		// console.log("_objectDetailInfoAry length >> 0 : fn_draw_object_detail");
	}else{
		// console.log("파라미터 없음 : fn_draw_object_detail");
	}


};


/************************************************************************/
/*
함수명 : fn_clear_object_detail_view
설	명 : 객체 정보 내용 view 초기화
인	자 : elemId
사용법 : 

작성일 : 2019-03-18
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.03.18	최진	최초 작성
 */
/************************************************************************/
var fn_clear_object_detail_view = function(elemId){
	if($("#"+elemId + " > div").length > 0){
		$("#"+elemId).empty();
	}
};

/************************************************************************/
/*
함수명 : fn_reset_user_selected_detail_info
설	명 : 유저가 선택한 객체세부정보 reset
인	자 : userDataset.clickedObjectDetailInfo
사용법 : 

작성일 : 2019-03-18
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.03.18	최진	최초 작성
 */
/************************************************************************/
var fn_reset_user_selected_detail_info = function(elemId, _data){
	var deleteId = elemId+"_", _dataVal ,rtnAry = [];
	
	for(var i = 0; i < _data.length; i ++){
		_dataVal =_data[i]; 
		if(_dataVal.indexOf(deleteId) == -1){
			rtnAry.push(_dataVal);
		}
	}
	
	
	userDataset.clickedObjectDetailInfo = rtnAry;
	
	fn_draw_only_column_jsGrid(userDataset.clickedObjectDetailInfo);
	
}


/************************************************************************/
/*
함수명 : fn_evt_detailChk_bind
설	명 : 객체 세부정보의 체크박스 클릭 시 동작하는 이벤트 함수
인	자 : e(해당 동작 수행 시 발생하는 이벤트 객체 type: Object), 
	_value(호출 위치에 따라 바인딩 시 value와 SavedDashboard.js에서 세팅하는 value가 될 수 있음)
사용법 : 

작성일 : 2019-03-18
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.03.18	최진	최초 작성
 */
/************************************************************************/
var fn_evt_detailChk_bind =function(e, _value){
	var _setDetailVal = _value !== undefined && _value !== null ? _value : $(this).val();
	
	
	if(_setDetailVal !== undefined && _setDetailVal !== null){
		var _aryIndex = userDataset.clickedObjectDetailInfo.indexOf(_setDetailVal);
		if(_aryIndex > -1){
			userDataset.clickedObjectDetailInfo.splice(_aryIndex,1);
		}else{
			userDataset.clickedObjectDetailInfo.push(_setDetailVal);
		}
		
		if(userDataset.clickedObjectDetailInfo.length > 0){
			fn_init_condition_view(false, [dashboard_config.orderbyDomId]);	
		}else{
			fn_init_condition_view(true, [dashboard_config.orderbyDomId]);
		}
		fn_clear_condition_area_elem();	
		fn_draw_only_column_jsGrid(userDataset.clickedObjectDetailInfo);
	}
};
/************************************************************************/
/*
함수명 : fn_set_object_detail01_refact
설	명 : 첫번째 객체 세부정보에 objCondInfoName 추가
인	자 : objectDetailInfo01(객체 세부정보 데이터 type: Array)
사용법 : 

작성일 : 2019-03-04
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.03.04	최진	최초 작성
 */
/************************************************************************/
var fn_set_object_detail01_refact = function(objectDetailInfo01){
	for(var k in objectDetailInfo01){
		objectDetailInfo01[k]["objCondInfoName"]  = ""+objectDetailInfo01[k]["objInfoName"];
	}
	
	dataset.objectDetailInfo1 = objectDetailInfo01;
};
/************************************************************************/
/*
함수명 : fn_set_object_detailNrefect_Dataset
설	명 : 두번째 객체 세부정보와 첫번째 객체 세부정보를 비교하여 같은 이름이 존재할 경우 
Name(이름 명)으로 변경
인	자 : _fn_datePicker_clear(리셋 시 초기화 콜백 함수 type: function)
사용법 : 

작성일 : 2019-03-04
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.03.04	최진	최초 작성
 */
/************************************************************************/

var fn_set_object_detailNrefect_Dataset = function(_flag, _dataAry, _fnCallback){
	var objectDetailInfo01 = dataset.objectDetailInfo1;
	
	fn_set_object_detail01_refact(objectDetailInfo01);
	
	if(_flag === "detail2Add"){
		
		for(var i=0;i<_dataAry.length;i++){
			
			_dataAry[i]["objCondInfoName"] =""+_dataAry[i]["objInfoName"];
			for(var j=0;j<objectDetailInfo01.length;j++){
				
				if(_dataAry[i]["objInfoName"] === objectDetailInfo01[j]["objInfoName"]){
//					_dataAry[i]["objCondInfoName"] += "("+userDataset.selectedObjInfo2.objectName+")";
//					objectDetailInfo01[j]["objCondInfoName"] += "("+userDataset.selectedObjInfo1.objectName+")";
					_dataAry[i]["objCondInfoName"] += "(B)";
					objectDetailInfo01[j]["objCondInfoName"] += "(A)";
				}
			}
		}
		dataset.objectDetailInfo1 = objectDetailInfo01;
		dataset.objectDetailInfo2 = _dataAry;
		
		_fnCallback(dataset.objectDetailInfo1, dataset.objectDetailInfo2);
	}else{
		// clear
//		var tbObjName = "("+userDataset.selectedObjInfo1.objectName+")",
		var tbObjName = "(A)",
			existIndex;
		for(var k=0;k<objectDetailInfo01.length;k++){
			existIndex = objectDetailInfo01[k]["objCondInfoName"].indexOf(tbObjName); 
			if(existIndex > -1){
				objectDetailInfo01[k]["objCondInfoName"] = objectDetailInfo01[k]["objCondInfoName"].substring(0,existIndex);
			}
		}
		dataset.objectDetailInfo1 = objectDetailInfo01;
		_fnCallback(dataset.objectDetailInfo1, null);
	}
	
};

/************************************************************************/
/*
함수명 : fn_action_selectBox2
설	명 : 객체 추가를 누른 후 두번째 선택박스를 선택 시 동작하는 액션 함수
 - 참조 SavedDashboard.js에서도 참조하고 있음.

인	자 : value, _fn_callback(액션이 끝난 후 동작하는 콜백함수, type:Function)
사용법 : 

작성일 : 2019-04-01
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.04.01	최진	최초 작성
 */
/************************************************************************/
var fn_action_selectBox2 = function(value, _fn_callback){
	// 'change' event bind >> second select box 
	fn_init_userNdataset("select2");
	userDataset.objSelect2 = value;
	userDataset.selectedObjInfo2 = fn_search_dataset_selected_objInfo(dataset.object, userDataset.objSelect2);

	// 객체 정보 내용 view 초기화
	fn_clear_object_detail_view(dashboard_config.objectDetailId2);
	// '조건추가' view 초기화
	fn_clear_condition_area_elem("all");
	// 03.28 grid 및 유저가 선택한 데이터 reset&새로그리기
	fn_reset_user_selected_detail_info(dashboard_config.objectDetailId2,userDataset.clickedObjectDetailInfo);
	
	
	if(userDataset.objSelect1 === userDataset.objSelect2){
		if(userDataset.objSelect1 !== "notSelected"){
			$("#objectDetailInfo2").append("<p>동일한 객체는 비교할 수 없습니다.</p>");	
		}
	}else{
		// selectbox 2 객체 세부 정보 데이터 호출
		if(userDataset.objSelect2 !== "notSelected"){
			
			fn_call_object_detail_info(userDataset.selectedObjInfo2["tableName"], function(_resultAry){
				if(_resultAry.length > 0){
					// 03.20 이름에 '(객체명)'추가
					fn_set_object_detailNrefect_Dataset("detail2Add", _resultAry, function(detailObj01, detailObj02){
						fn_clear_object_detail_view(dashboard_config.objectDetailId2);
						fn_draw_object_detail("objectDetailInfo2", detailObj02);
						if(_fn_callback !== undefined && _fn_callback !== null){
							_fn_callback();
						}
					});
				}
			});
			
			// 03.26 object2 제목 표시
			$("#objectDetailName2").text(userDataset.selectedObjInfo2.objectName+"(B)");
			$("#objectDetailName1").text(userDataset.selectedObjInfo1.objectName+"(A)");
		}else{
			// selectbox 2 == "notSelected"
			// 03.26 object2 제목 표시 초기화
			$("#objectDetailName1").text(userDataset.selectedObjInfo1.objectName);
			$("#objectDetailName2").text("");
			
			// 03.20 이름에 '(객체명)'제거
			fn_set_object_detailNrefect_Dataset("detail1Clear", null, function(detailObj01){
				fn_clear_object_detail_view(dashboard_config.objectDetailId2);
			});
		}
	}
}
/************************************************************************/
/*
함수명 : fn_action_object_list
설	명 : DB에서 객체 리스트 불러와 SelectBox 그리기 액션 수행(첫 번째 진입점)
인	자 : _fn_datePicker_clear(리셋 시 초기화 콜백 함수 type: function)
사용법 : 

작성일 : 2019-03-04
작성자 : 최 진 
수정일	수정자	수정내용
------	------	-------------------
2019.03.04	최진	최초 작성
2019.03.20	최진	이름에 '(객체명)'추가
 */
/************************************************************************/

var fn_action_object_list = function(_elemObj, _fn_datePicker_clear){
	dashboard_config.objectDetailId1 = _elemObj.detailElemId1; 
	dashboard_config.objectDetailId2 = _elemObj.detailElemId2;
	dashboard_config.datepickerStartId = _elemObj.datepickerStartId;
	dashboard_config.datepickerEndId = _elemObj.datepickerEndId;
	
	dashboard_config.fn_datepicker_clear = _fn_datePicker_clear
	$.ajax({
		type:"GET",
		url:OlapUrlConfig.getObjectList,
		 beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
	}).done(function(result) {
		if (result !== undefined && result !== null
				&& result["record"] !== undefined
				&& result["record"] !== null
				&& result["record"] instanceof Array) {

			dataset.object = result.record;

			// selectbox Draw and event binding
			fn_view_object_selectbox_initNDraw(
					"formControlObjectSelect1", dataset.object,
					function() {
						// 'change' event bind first select box
						fn_init_userNdataset("total");
						userDataset.objSelect1 = this.value; 
						userDataset.selectedObjInfo1 = fn_search_dataset_selected_objInfo(dataset.object, userDataset.objSelect1);

						// '조건추가' view 초기화
						fn_clear_condition_area_elem("all");
						// 03.28 grid 및 유저가 선택한 데이터 reset&새로그리기
						fn_reset_user_selected_detail_info(dashboard_config.objectDetailId1,userDataset.clickedObjectDetailInfo);
						
						if (userDataset.objSelect1 !== "notSelected") {
							// 관계된 객체 정보 호출
							fn_call_object_rel_info(userDataset.objSelect1, function(_rtnData){
								//set
								dataset.objectRelInfo[userDataset.objSelect1] = _rtnData;

								var joinedObjectList = fn_search_second_object(dataset.object, _rtnData);
								
								// draw selectbox 2 이벤트 바인드
								fn_view_object_selectbox_initNDraw("formControlObjectSelect2",joinedObjectList,
										function(){
									fn_action_selectBox2(this.value);
								});// 두번째 select box draw


							}); //fn_call_object_rel_info

							// 만약 selectbox 1 객체 세부 정보가 존재할 경우 삭제
							var _objDetailDomId = dashboard_config.objectDetailId2;
							fn_clear_object_detail_view(_objDetailDomId);

							// selectbox 1 객체 정보 호출
							fn_call_object_detail_info(userDataset.selectedObjInfo1["tableName"], function(_resultAry){
								var _objDetailDomId = dashboard_config.objectDetailId1;
								fn_clear_object_detail_view(_objDetailDomId);
								if(_resultAry.length > 0){
									fn_set_object_detail01_refact(_resultAry);
									fn_draw_object_detail(_objDetailDomId,dataset.objectDetailInfo1);
								}
								
								//console.log(_resultAry);
								/*조회기간 필수 일 때 DatePicker에 값설정하는 로직*/
//								var newArr = _resultAry.filter(function(item){    
//									  return item.standDate === "Y";
//									  
//									}); 
								//console.log("단위",newArr[0].durationUnit);
								//console.log("숫자",newArr[0].durationNum);
//								var datePicker_start_id = "datepicker_start", datePicker_end_id = "datepicker_end";
//								DatepickerModule.init(datePicker_start_id,newArr[0].durationUnit);
//								DatepickerModule.init(datePicker_end_id);
//								DatepickerModule.evtBind(datePicker_start_id,datePicker_end_id,newArr[0].durationUnit,newArr[0].durationNum);
//								$('#durationUnit').text(newArr[0].durationNum + newArr[0].durationUnit);
								
							});

							// selectbox Enable 처리
							if ($("#formControlObjectSelect2").prop('disabled')) {
								$('#formControlObjectSelect2').removeAttr('disabled');
							}

							fn_init_condition_view(false,[dashboard_config.conditionDomId]);
							
							// 03.26 object1 제목 표시
							$("#objectDetailName1").text(userDataset.selectedObjInfo1.objectName);
						} else {
							//  selectbox1 유저가 "객체선택"에서 '선택하십시오'를 선택하였을 때
							// selectbox Disable 처리
							if (!$("#formControlObjectSelect2").prop('disabled')) {
								$("#"+dashboard_config.objectDetailId2).empty();	
								$("#formControlObjectSelect2").empty();
								$("#formControlObjectSelect2").append("<option value='notSelected'>- 선택하십시오 -</option>");
								$('#formControlObjectSelect2').prop('disabled', true);
								var _objDetailDomId = dashboard_config.objectDetailId1;
								fn_clear_object_detail_view(_objDetailDomId);
								// 조건 초기화 조건 추가 버튼 Disabled
								fn_init_condition_view(true,[dashboard_config.conditionDomId, dashboard_config.orderbyDomId]);
								
								// 03.26 object1 제목 표시 초기화
								$("#objectDetailName1").text("");
								// 03.26 object2 제목 표시 초기화
								$("#objectDetailName2").text("");
								
							}
						}//
					});

		}
	}).fail(function(fail) {
		// console.log(fail);
		if (fail.status == 401) {
            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
            	window.location.replace(OlapUrlConfig.loginPage);
            });
             
             
         } else if (fail.status == 403) {
            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
            	window.location.replace(OlapUrlConfig.loginPage);
            });
              
         }
	});
};



/************************************************************************/ 
/*
함수명 : fn_event_bind_condition
설	명 : DashBoard 페이지의 조회조건(조건/정렬)추가 버튼 이벤트 바인딩 함수 
인	자 : _domId (button element id type : String), _areaId (content Div Id)
사용법 : 

작성일 : 2019-03-06
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.06	최진	최초 작성
 */
/************************************************************************/
var fn_event_bind_condition = function(elemInfoObj){

	dashboard_config.conditionDomId = elemInfoObj.conditionId;
	dashboard_config.areaDomId = elemInfoObj.conditionArea;
	dashboard_config.orderbyDomId = elemInfoObj.orderById;
	dashboard_config.orderbyAreaId = elemInfoObj.orderByArea;
	dashboard_config.gridElemId = elemInfoObj.gridElemId;
	
	
	fn_init_condition_view(true, [dashboard_config.conditionDomId, dashboard_config.orderbyDomId]);

	// click event bind
	$("#"+elemInfoObj.conditionId).off("click").on('click', function(event){
		event.preventDefault(); //event.preventDefault() 이벤트 전파하지않고 취소..
		fn_action_condition_data();
	});
	$("#"+elemInfoObj.orderById).off("click").on('click', function(event){
		event.preventDefault();
		fn_action_orderby_data();
	});


};


/************************************************************************/ 
/*
함수명 : fn_call_condition_data
설	명 : Dashboard 페이지의 조건추가 시 필요한 데이터를 호출하는 함수
	인	자 : _tbNames( 유저가 선택한 테이블 아이디 type:Array)
사용법 : 

작성일 : 2019-03-06
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.06	최진	최초 작성
 */
/************************************************************************/
var fn_call_condition_data = function(_tbNames, _fnCallback){

	if(_tbNames !== undefined && _tbNames !== null && _tbNames instanceof Array){

		$.ajax({
			type:"GET",
			url:OlapUrlConfig.getConditionData,
			 beforeSend: function(xhr) {
			        xhr.setRequestHeader("AJAX", true);
			     },
			data : {
				tbNames : _tbNames
			}
		}).done(function(result) {
			if(_fnCallback !== undefined && _fnCallback !== null && _fnCallback instanceof Function){
				_fnCallback(result);	
			}
			//console.log(result);
		}).fail(function(failobj){
			if (failobj.status == 401) {
	            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
	            	window.location.replace(OlapUrlConfig.loginPage);
	            });
	             
	             
	         } else if (failobj.status == 403) {
	            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
	            	window.location.replace(OlapUrlConfig.loginPage);
	            });
	              
	         }
		});
	}else{
		// console.log("fn_call_condition_data 파라미터가 설정되지 않음.");
	}
};


/************************************************************************/ 
/*
함수명 : fn_action_orderby_data
설	명 : Dashboard 페이지의 정렬 조건  dataset setting 완료 후  draw 함수 호출
인	자 : 
사용법 : 

작성일 : 2019-03-18
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.18	최진	최초 작성
	2019.04.04	최진	_fn_callback 인자 추가
 */
var fn_action_orderby_data = function(_fn_callback){
	if(userDataset.clickedObjectDetailInfo.length > 0){
		
		fn_clear_noticeText_div(dashboard_config.orderbyAreaId); // notice clear
		
		var datasetAry = [], _selectedVal, _objKey, _idx;
		for(var _i=0;_i<userDataset.clickedObjectDetailInfo.length;_i++){
			_selectedVal = userDataset.clickedObjectDetailInfo[_i].split("_");
			if(_selectedVal.length > 0 ){
				_objKey = _selectedVal[0], 
				_idx =  Number(_selectedVal[1]);
				datasetAry.push(dataset[_objKey][_idx]);	
			}
		}
		fn_draw_orderby_area(datasetAry, dashboard_config, _fn_callback); 
		
	}
};

/************************************************************************/ 
/*
함수명 : fn_init_condition_view
설	명 : Dashboard 페이지의 조회조건 (조건/정렬)추가 버튼초기화
	인	자 : _isDisable( 버튼 Disable 여부 type : boolean)
사용법 : 

작성일 : 2019-03-06
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.06	최진	최초 작성
 */
/************************************************************************/
var fn_init_condition_view = function(_isDisable, _elemIdAry){
	var _elemId = "";
	for(var _i=0;_i<_elemIdAry.length;_i++){
		_elemId = _elemIdAry[_i];
		if(_isDisable){
			//view init
			if(_elemId !== "" && $("#"+_elemId).prop('disabled') === false){
				$("#"+_elemId).prop('disabled', true);
			}
		}else{
			//view init
			if(_elemId !== "" && $("#"+_elemId).prop('disabled') === true){
				$("#"+_elemId).removeAttr('disabled');
			}
		}
	}
	
	
	
}


/************************************************************************/ 
/*
함수명 : fn_clear_condition_area_elem
설	명 : Dashboard 페이지의 조건추가 View 화면 Clear
인	자 : 
사용법 : 

작성일 : 2019-03-07
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.07	최진	최초 작성
 */
/************************************************************************/
var fn_clear_condition_area_elem = function(_flag){
	if(_flag === "all"){
		$("#"+dashboard_config.areaDomId).empty();	
		fn_init_append_notice_div(dashboard_config.areaDomId,dashboard_config.conditionNoticeText);
	} 
	
	$("#"+dashboard_config.orderbyAreaId).empty();
	fn_init_append_notice_div(dashboard_config.orderbyAreaId,dashboard_config.orderbyNoticeText);
	
	
}

/************************************************************************/ 
/*
함수명 : fn_action_condition_data
설	명 : Dashboard 페이지의 조건추가 기본 Action
인	자 : _flag ("condition":조건, "orderby":정렬 type:String)
사용법 : 
 click 이벤트 호출 시, 해당 함수를 실행하여 데이터를 가공한 후, draw를 수행함.
작성일 : 2019-03-07
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.07	최진	최초 작성
	2019.03.18	최진	orderby와 condition 분리
	2019.04.04	최진	_fn_callback 인자 추가
 */
/************************************************************************/
var fn_action_condition_data = function(_fn_callback, _condData){

	var _tbNameAry = [];

	if(
			userDataset.objSelect1 !== "notSelected" && 
			userDataset.objSelect1 !== "" && 
			userDataset.objSelect1 !== undefined &&
			userDataset.objSelect1 !== null){

		_tbNameAry.push(userDataset.objSelect1);
	}

	if(
			userDataset.objSelect2 !== "notSelected" && 
			userDataset.objSelect2 !== "" && 
			userDataset.objSelect2 !== undefined &&
			userDataset.objSelect2 !== null){

		_tbNameAry.push(userDataset.objSelect2);
	}
	fn_clear_noticeText_div(dashboard_config.areaDomId); //clear 함수
	if(_tbNameAry.length > 0){
		fn_call_condition_data(_tbNameAry, function(_result){ //조건추가 Data 호출
			// Dataset Complete
			if(_tbNameAry.length === 2){
				dataset.objectConditionInfo1 = _result[_tbNameAry[0]];
				dataset.objectConditionInfo2 = _result[_tbNameAry[1]];
				
				
			}else{
				dataset.objectConditionInfo1 = _result[_tbNameAry[0]];
			}

			// 조건 draw 
			fn_refact_condition_data(dataset, userDataset, function(_data1, _data2){
				// Draw
				var _setData;
				if(_data2 !== null){
					_setData = _data1.concat(_data2)
				}else{
					_setData=_data1;
				}
				console.log("_setData",_setData);
				
				dataset.conditionDataset = _setData;
				fn_draw_condition_area(dataset.conditionDataset, dashboard_config, _fn_callback, _condData);
				fn_draw_condition_jsgrid(dataset.conditionDataset);
			});

		});	 
	}else{
		// 데이터 통신 X -> Draw만
		fn_draw_condition_area(dataset.conditionDataset, dashboard_config, _fn_callback, _condData);
	} // if

};


/************************************************************************/ 
/*
함수명 : fn_refact_condition_data
설	명 : Dashboard 페이지의 조건추가 View 화면을 Draw하기 위해 데이터 가공하는 함수
인	자 :  _datasetParams(전역객체 : dataset), _userDatasetParams(전역객체 : userDataset)
사용법 : 

작성일 : 2019-03-07
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.07	최진	최초 작성
 */
/************************************************************************/
var fn_refact_condition_data = function(_datasetParams, _userDatasetParams, _fnCallback){

	var _fn_set_data_struct = function(_detailDataset, _condedDataset){
		var setConditionElemObjTemp = {
				tableName : "",
				columnName : "",
				objInfoName :"",
				objCondInfoName:"",
				calcFunc : "",
				operOption :[]
		};

		var operOptionElemObjTemp = {
				qryConCode : "",
				qryConCodeNm :"",
				operSym :""
		};

		var i,k,setElemObj, setOperElemObj,
		rtnElemInfoAry = [];

		for(i = 0;i<_detailDataset.length;i++){

			setElemObj = $.extend(true, {}, setConditionElemObjTemp);
			setElemObj.tableName = _detailDataset[i]["tableName"];
			setElemObj.columnName = _detailDataset[i]["colName"];
			setElemObj.objInfoName = _detailDataset[i]["objInfoName"];
			setElemObj.objCondInfoName = _detailDataset[i]["objCondInfoName"];
			setElemObj.calcFunc =  _detailDataset[i]["calcFunc"] !== undefined && _detailDataset[i]["calcFunc"] !== null ? _detailDataset[i]["calcFunc"] : "";

			for(k=0;k<_condedDataset.length;k++){

				if(setElemObj.tableName === _condedDataset[k]["tableName"] &&
						setElemObj.columnName === _condedDataset[k]["columnName"]){

					setOperElemObj = $.extend(true, {}, operOptionElemObjTemp);
					setOperElemObj.qryConCode = _condedDataset[k]["qryConCode"];
					setOperElemObj.qryConCodeNm = _condedDataset[k]["qryConCodeNm"];
					setOperElemObj.operSym = _condedDataset[k]["operSym"];
					setElemObj.operOption.push(setOperElemObj);
				}

			}

			rtnElemInfoAry.push(setElemObj); //data set complete
		}// for
		return rtnElemInfoAry;
	};// _fn

	var _conditionDataset = {
			user : {
				selectedTb1: _userDatasetParams["objSelect1"],
				selectedTb2: _userDatasetParams["objSelect2"],
			},
			dataset : {
				detail1: _datasetParams["objectDetailInfo1"],
				detail2: _datasetParams["objectDetailInfo2"],
				condition1: _datasetParams["objectConditionInfo1"],
				condition2: _datasetParams["objectConditionInfo2"],
			}
	}; 


	// value 값 세팅
	// Draw하기위해 데이터 가공 >> 앞에서 undefined 체크를 했기 때문에 생략

	var _conditionRefectedDataset01 = null, 
	_conditionRefectedDataset02 = null;
	if(_conditionDataset.user.selectedTb1 !== "" && _conditionDataset.user.selectedTb1 !== "notSelected"){
		_conditionRefectedDataset01 = _fn_set_data_struct(_conditionDataset.dataset.detail1, _conditionDataset.dataset.condition1);
	}
	if(_conditionDataset.user.selectedTb2 !== "" && _conditionDataset.user.selectedTb2 !== "notSelected"){
		_conditionRefectedDataset02 = _fn_set_data_struct(_conditionDataset.dataset.detail2, _conditionDataset.dataset.condition2);
	}

	_fnCallback(_conditionRefectedDataset01, _conditionRefectedDataset02);



};

/************************************************************************/ 
/*
함수명 : fn_draw_condition_
설	명 : Dashboard 페이지의 조건추가 View 화면 Draw 함수
인	자 :  _fn_callback(저장목록 불러오기에서 값을 세팅하기 위한 콜백 함수 type:Function)
사용법 : 

작성일 : 
작성자 : 

	수정일	수정자	수정내용
	------	------	-------------------
	
 */
/************************************************************************/
var fn_draw_condition_jsgrid = function(_drawDataAry){
	
/*	var _fn_create_condtion_id = function(){
		var elemId = "";
		// 생성한 조건 elemId 카운트
		elemId = dashboard_config.drawConditionBaseJsgrid.selectId+"_"+dashboard_config.createConditionElemCnt;
		dashboard_config.createConditionElemCnt++;
		return elemId;
	};
	if(_drawDataAry !== undefined && _drawDataAry !== null && _drawDataAry instanceof Array && _drawDataAry.length > 0){
		var selectId =_fn_create_condtion_id();
		var createElem ="";
		
		createElem += _elemObj.drawConditionBaseJsgrid.selectId; //그려주는 부분
		createElem += selectId;
		createElem += _elemObj.drawConditionBaseJsgrid.parentElem01;
		createElem += _elemObj.drawConditionBaseJsgrid.closeTag;
		createElem += _elemObj.drawConditionBaseJsgrid.parentElem02;
		createElem += _elemObj.drawConditionBaseJsgrid.selectElem01;
		createElem += _elemObj.drawConditionBaseJsgrid.setOptionElem01;
		createElem += _elemObj.drawConditionBaseJsgrid.setOptionCloseElem;
		createElem += _elemObj.drawConditionBaseJsgrid.parentCloseElem01; 
		createElem += _elemObj.drawConditionBaseJsgrid.inputElem01;
		createElem += _elemObj.drawConditionBaseJsgrid.parentElem03;
		createElem += _elemObj.drawConditionBaseJsgrid.parentCloseElem02;
		createElem += _elemObj.drawConditionBaseJsgrid.closeIconElem;
	}
	
	for(var i=0;i<_drawDataAry.length;i++){
		
		if(_drawDataAry[i]["operOption"].length > 0){
			// oper가 있을 때에만 조건이 나와야 함.
			$("#jsGrid").find(".data-condition-name-select").append(new Option(_drawDataAry[i]["objCondInfoName"],i));
			//$("#jsGrid").find(".data-condition-name-select").append(new Option(_drawDataAry[i]["objCondInfoName"],i));
		}
	}*/
}

/************************************************************************/ 
/*
함수명 : fn_draw_condition_area
설	명 : Dashboard 페이지의 조건추가 View 화면 Draw 함수
인	자 :  _fn_callback(저장목록 불러오기에서 값을 세팅하기 위한 콜백 함수 type:Function)
사용법 : 

작성일 : 2019-03-08
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.08	최진	최초 작성
	2019.04.04	최진	추가 인자 _fn_callback추가
 */
/************************************************************************/
var fn_draw_condition_area = function(_drawDataAry, _elemObj, _fn_callback, _condData){
	// id 생성
	var _fn_create_condtion_id = function(){
		var elemId = "";
		// 생성한 조건 elemId 카운트
		elemId = dashboard_config.drawConditionBaseElem.selectId+"_"+dashboard_config.createConditionElemCnt;
		dashboard_config.createConditionElemCnt++;
		return elemId;
	};
	
	if(_drawDataAry !== undefined && _drawDataAry !== null && _drawDataAry instanceof Array && _drawDataAry.length > 0){
		var selectId =_fn_create_condtion_id();
		var createElem ="";
		
			// 조건 
			createElem += _elemObj.drawConditionBaseElem.parentElem01; //그려주는 부분
			createElem += selectId;
			createElem += _elemObj.drawConditionBaseElem.closeTag;
			createElem += _elemObj.drawConditionBaseElem.parentElem02;
			createElem += _elemObj.drawConditionBaseElem.selectElem01;
			createElem += _elemObj.drawConditionBaseElem.parentCloseElem01;
			createElem += _elemObj.drawConditionBaseElem.inputElem01;
			createElem += _elemObj.drawConditionBaseElem.parentElem03;
			createElem += _elemObj.drawConditionBaseElem.parentCloseElem02; 
			createElem += _elemObj.drawConditionBaseElem.closeIconElem;
			if(_drawDataAry.length > 0){
				$("#"+dashboard_config.areaDomId).append(createElem);	
			}else{
				// 예외처리
				$("#"+dashboard_config.areaDomId).append("<p>조건을 설정할 수 없습니다.</p>");
			}
			for(var i=0;i<_drawDataAry.length;i++){
				
				if(_drawDataAry[i]["operOption"].length > 0){
					// oper가 있을 때에만 조건이 나와야 함.
					$("#"+selectId).find(".data-condition-name-select").append(new Option(_drawDataAry[i]["objCondInfoName"],i));
					//$("#jsGrid").find(".data-condition-name-select").append(new Option(_drawDataAry[i]["objCondInfoName"],i));
				}
			}
			var _defaultViewOp = _drawDataAry.length > 0 ? _drawDataAry[0]["operOption"] : [];
			for(var k=0;k < _defaultViewOp.length;k++){
				$("#"+selectId).find(".data-condition-oper-select").append(new Option(_defaultViewOp[k]["qryConCodeNm"],k));	
			}
			
			// '조건 추가' 삭제 이벤트 바인드
			$("#"+selectId).find(".fn-delConditionIcon").on("click", function(evt){
				$(this).off("click");
				$(this).parent().parent().remove();
				if($(".fn-delConditionIcon").length === 0){
					fn_init_append_notice_div(dashboard_config.areaDomId,dashboard_config.conditionNoticeText);
				}
			});
			
		
			// change 이벤트
			$("#"+selectId).find(".data-condition-name-select").on("change",function(e){
				var idx = $(this).val();
				var _operAry = dataset.conditionDataset[idx]["operOption"];
				$(this).parent().find(".data-condition-oper-select").empty();	
				$(this).parent().find(".data-condition-value-select").val("");	
				
				for(var _k in _operAry){
					$(this).parent().find(".data-condition-oper-select").append(new Option(_operAry[_k]["qryConCodeNm"],_k));
				}
				
				//console.log(_operAry);
				// 셀렉트 리스트 y인 조건일 경우 input 태그를 select 태그로 변경
                
				if($(".data-condition-oper-select").find("option[value='"+$(".data-condition-oper-select").val()+"']").text() == '목록에서선택'){
					
					$(this).parent().find(".data-condition-value-select").replaceWith('<select class="form-control form-control-sm my-1 mr-1 data-condition-value-select" ></select>');
					//console.log(dataset.conditionDataset[idx]["tableName"]);
					//console.log(dataset.conditionDataset[idx]["columnName"]);
					//console.log(dataset.conditionDataset[idx]);
					
					var param = {};
					param.tableName = 	dataset.conditionDataset[idx]["tableName"];
					param.columnName = 	dataset.conditionDataset[idx]["columnName"];
					//console.log(param.columnName);
					
					var fn_call_select_list_condition = function(fnCallback){
					// 선택한 조건의 distinct 리스트를 불러온다
		
					$.ajax({
						type:"POST",
						url:OlapUrlConfig.getSelectList,
						 beforeSend: function(xhr) {
						        xhr.setRequestHeader("AJAX", true);
						     },
						 contentType : 'application/json',
						 data : JSON.stringify(param),
						 success : function(data) {
								//for(var M in data){
									//$('.data-condition-value-select').append(new Option(data[M][param.columnName],M));
									//var option = $("<option>"+data[M][param.columnName]+"</option>");
									//$('.data-condition-value-select').append(option);
									//console.log(data[M][param.columnName]);
								//}

							},
					
					}).done(function(_result){
						//console.log(_result);
						fnCallback(_result);
						
						
					}).fail(function(_failObj){
						// console.log("fail fn_call_getSelectList");
						// console.log(_failObj);
						
						if (_failObj.status == 401) {
				            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
				            	window.location.replace(OlapUrlConfig.loginPage);
				            });
				         } else if (_failObj.status == 403) {
				            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
				            	window.location.replace(OlapUrlConfig.loginPage);
				            });
				         }else if(_failObj.status === 400){
							alert("데이터를 불러오는 데에 실패하였습니다. 관리자에게 문의하여 주십시오.");
						}else{
							alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
						}
					 }); // AJAX
					
					};
					
					fn_call_select_list_condition(function(_result){
						//console.log(_result);
						for(var M in _result){
							
							if(_result[M]!== null && _result[M] !==  undefined){
							//$('.data-condition-value-select').append(new Option(_result[M][param.columnName],M));
							var option = $("<option>"+_result[M][param.columnName]+"</option>");
							$('.data-condition-value-select').append(option);
							//console.log(data[M][param.columnName]);
							   }
						}
					})
					
				}else{
					$(this).parent().find(".data-condition-value-select").replaceWith('<input type="text" class="form-control form-control-sm my-1 mr-1 data-condition-value-select" placeholder="텍스트 입력">');
					
				}
				
								
			});
			
			
			// change 조건 추가의 조회 조건 이벤트
			$("#"+selectId).find(".data-condition-oper-select").on("change",function(e){

		if($(".data-condition-oper-select").find("option[value='"+$(".data-condition-oper-select").val()+"']").text() == '목록에서선택'){
					
					$(this).parent().find(".data-condition-value-select").replaceWith('<select class="form-control form-control-sm my-1 mr-1 data-condition-value-select" ></select>');
			
					
					var param = {};
					param.tableName = 	dataset.conditionDataset[$(".data-condition-name-select").val()]["tableName"];
					param.columnName = 	dataset.conditionDataset[$(".data-condition-name-select").val()]["columnName"];
				 		
					var fn_call_select_list_condition = function(fnCallback){

		
					$.ajax({
						type:"POST",
						url:OlapUrlConfig.getSelectList,
						 beforeSend: function(xhr) {
						        xhr.setRequestHeader("AJAX", true);
						     },
						 contentType : 'application/json',
						 data : JSON.stringify(param),
						 success : function(data) {
		
							},
					
					}).done(function(_result){
						
						fnCallback(_result);
						
						
					}).fail(function(_failObj){
			
						
						if (_failObj.status == 401) {
				            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
				            	window.location.replace(OlapUrlConfig.loginPage);
				            });
				         } else if (_failObj.status == 403) {
				            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
				            	window.location.replace(OlapUrlConfig.loginPage);
				            });
				         }else if(_failObj.status === 400){
							alert("데이터를 불러오는 데에 실패하였습니다. 관리자에게 문의하여 주십시오.");
						}else{
							alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
						}
					 }); // AJAX
					
					};
					
					fn_call_select_list_condition(function(_result){
						//console.log(_result);
						for(var M in _result){
							
							if(_result[M]!== null && _result[M] !==  undefined){
							
							var option = $("<option>"+_result[M][param.columnName]+"</option>");
							$('.data-condition-value-select').append(option);
							
							   }
						}
					})
					
				}else{
					$(this).parent().find(".data-condition-value-select").replaceWith('<input type="text" class="form-control form-control-sm my-1 mr-1 data-condition-value-select" placeholder="텍스트 입력">');
					
				}
				
								
			});
			
			
			//   $("#"+selectId).find(".data-condition-name-select").on("change" .... 이벤트가 실행되기 전 즉, 조건 추가 버튼을 누르고  처음 뜨는 (ex 날짜) 조건이 셀렉트 리스트를 불러오는 조건이 y인 경우를 대비해서 트리거 이벤트를 걸어놈  
			$("#"+selectId).find(".data-condition-name-select").trigger('change');
			
			if(_fn_callback !== undefined && _fn_callback !== null && _fn_callback instanceof Function){
				_fn_callback(selectId, _condData);
			}
			
	}
};

/************************************************************************/ 
/*
함수명 : fn_draw_orderby_area
설	명 : Dashboard 페이지의 리셋 버튼 이벤트 바인딩 함수
인	자 :  _drawDataAry(draw 하기 위한 DataSet), _elemObj(draw 엘리먼트 id type:String), 
		_fn_callback(Draw과 완료된 후 실행할 콜백 함수)
사용법 : 

작성일 : 2019-03-11
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.11	최진	최초 작성
 */
/************************************************************************/
var fn_draw_orderby_area = function(_drawDataAry, _elemObj, _fn_callback){
	var createElem ="";
	var selectId = dashboard_config.drawOrderByBaseElem.selectId+"_"+dashboard_config.createOrderByElemCnt;
	dashboard_config.createOrderByElemCnt++;
	// 정렬
	createElem += _elemObj.drawOrderByBaseElem.template01;
	createElem += selectId,
	createElem += _elemObj.drawOrderByBaseElem.closeElemTag;
	
	createElem += _elemObj.drawOrderByBaseElem.template02;
	
	createElem += _elemObj.drawOrderByBaseElem.template03;
	createElem += _elemObj.drawOrderByBaseElem.template04;
	createElem += _elemObj.drawOrderByBaseElem.delBtnTemplate;
	 
	$("#"+dashboard_config.orderbyAreaId).append(createElem);
	for(var i=0;i<_drawDataAry.length;i++){
		$("#"+selectId).find(".data-orderby-name").append(new Option(_drawDataAry[i]["objCondInfoName"],i));	
	}
	
	for(var k=0;k<_elemObj.orderByValInfoAry.length;k++){
		$("#"+selectId).find(".data-orderby-value").append(new Option(_elemObj.orderByValInfoAry[k]["drawNm"],k));	
	}
	dataset.orderbyDataset = _drawDataAry;
	
	// '정렬 추가' 삭제 이벤트 바인드
	$("#"+selectId).find(".fn-delOrderByIcon").on("click", function(evt){
		$(this).off("click");
		$(this).parent().remove();
		if($(".fn-delOrderByIcon").length === 0){
			fn_init_append_notice_div(dashboard_config.orderbyAreaId,dashboard_config.orderbyNoticeText);
		}
	});
	
	if(_fn_callback !== undefined && _fn_callback !== null && _fn_callback instanceof Function){
		_fn_callback(selectId);
	}
};

/************************************************************************/ 
/*
함수명 : fn_event_bind_reset_btn
설	명 : Dashboard 페이지의 리셋 버튼 이벤트 바인딩 함수
인	자 :  resetBtnId(리셋버튼 엘리먼트 아이디 : type:String)
사용법 : 

작성일 : 2019-03-11
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.11	최진	최초 작성
 */
/************************************************************************/
var fn_event_bind_reset_btn = function(resetBtnId, fn_callbak){
	$("#"+resetBtnId).on('click',function(evt){
		if(SavedUserDataList !== undefined && SavedUserDataList !== null &&
				SavedUserDataList.hasOwnProperty("Saved_Dashboard_Config") && SavedUserDataList.Saved_Dashboard_Config.hasOwnProperty("seqNum")){
			SavedUserDataList.Saved_Dashboard_Config.seqNum = null;
			SavedUserDataList.Saved_Dashboard_Config.selectedPageNum ="";
			
			if($("#"+SavedUserDataList.Saved_Dashboard_Config.gridElemId).find('table tr.highlight').length > 0 ){
				$("#"+SavedUserDataList.Saved_Dashboard_Config.gridElemId).find('table tr.highlight').removeClass('highlight');
			}
		}
		
		$("#saveTitle").val("");
		$("#saveDescription").val("");
		$("#objectDisableDiv").show();
		fn_action_reset();
		if(fn_callbak !== undefined && fn_callbak !== null && fn_callbak instanceof Function){
			fn_callbak();
		}
		
	});
};	

var fn_action_reset = function(){
	$("#formControlObjectSelect1").val("notSelected").trigger('change');
	fn_draw_selected_data_Grid('reset');
	if($("#objectAddDelBtn").is(":visible")){
		$("#objectAddDelBtn").trigger("click");
	}
};


/************************************************************************/ 
/*
함수명 : fn_get_value_object_detail_info
설	명 : 유저가 선택한 객체 컬럼 정보 출력
인	자 :  _data (userDataset.clickedObjectDetailInfo type:String)
사용법 : 

작성일 : 2019-03-14
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.14	최진	최초 작성
 */
/************************************************************************/
var fn_get_value_object_detail_info = function(_data){
	var isSetting = true;
	var _rtnValAry = [],_locate, _key,_index,_detailDataset;
	
	for(var i = 0; i < _data.length;i++){
		_locate = _data[i].split("_");
		if(_locate.length === 2){
			_key=_locate[0];
			_index = _locate[1];
			_detailDataset = dataset[_key][_index];
			_rtnValAry.push(_detailDataset);
		}
	}
	
	return _rtnValAry;
}


/************************************************************************/ 
/*
함수명 : fn_get_value_conditionNorderbyNdate_info
설	명 : 유저가 선택한 객체 컬럼 정보 출력
인	자 :  _elemId (객체세부정보 체크박스 리스트의 element id type:String)
사용법 : 

작성일 : 2019-03-14
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.14	최진	최초 작성
 */
/************************************************************************/
var fn_get_value_conditionNorderbyNdate_info = function(){
	var rtnValObject = {},
	isValidOk = true,
	
	// Date 객체를 다음의 String으로 변환
	// '20190101'
	_fn_convert_date = function(dateObj){
		dateObj = new Date(dateObj);
		var _year = dateObj.getFullYear(),
			_month = (dateObj.getMonth()+1) < 10  ? "0"+(dateObj.getMonth()+1) : ""+(dateObj.getMonth()+1),
			_date = dateObj.getDate() < 10 ? "0"+dateObj.getDate() : ""+dateObj.getDate();
			return ""+_year+_month+_date;
	};
	
	
	// 날짜 정보 get (필수)
	if(dashboard_config.datepickerStartId !== undefined &&
			dashboard_config.datepickerStartId !== null &&
			dashboard_config.datepickerStartId !== ""){
		rtnValObject["startDate"] = $("#"+dashboard_config.datepickerStartId).datepicker('getDate');	
		rtnValObject["startDateStr"] = _fn_convert_date(rtnValObject["startDate"]);
	}else{
		isValidOk = false;
	}
	
	if(dashboard_config.datepickerEndId !== undefined &&
			dashboard_config.datepickerEndId !== null &&
			dashboard_config.datepickerEndId !== ""){
		rtnValObject["endDate"] = $("#"+dashboard_config.datepickerEndId).datepicker('getDate');
		rtnValObject["endDateStr"] = _fn_convert_date(rtnValObject["endDate"]);
	}else{
		isValidOk = false;
	}
	
	
	// 조건정보 get
	if(dataset.conditionDataset.length > 0 && 
			$("#"+dashboard_config.areaDomId).find(".data-condition-form").length > 0){
		rtnValObject["condition"] = []; 
		var _conditionInfoObj = {
				tableName : null,
				columnName: null,
				objInfoName : null,
				value: null,
				operSym: null,
				operQryConCode:null,
				operQryConCodeNm:null
		};
		$("#"+dashboard_config.areaDomId).find(".data-condition-form").each(function(){
			var setObj = $.extend(true, {}, _conditionInfoObj);
			var dataNameIdx = $(this).find(".data-condition-name-select").val();
			var dataOperIdx = $(this).find(".data-condition-oper-select").val(); 
			setObj.tableName = dataset.conditionDataset[dataNameIdx]["tableName"];
			setObj.columnName = dataset.conditionDataset[dataNameIdx]["columnName"];
			setObj.objInfoName = dataset.conditionDataset[dataNameIdx]["objInfoName"];
			
			setObj.value = $(this).find(".data-condition-value-select").val(); 
			setObj.operSym = dataset.conditionDataset[dataNameIdx]["operOption"][dataOperIdx]["operSym"];  
			setObj.operQryConCode = dataset.conditionDataset[dataNameIdx]["operOption"][dataOperIdx]["qryConCode"];  
			setObj.operQryConCodeNm = dataset.conditionDataset[dataNameIdx]["operOption"][dataOperIdx]["qryConCodeNm"];  
			if(setObj.value !== undefined && setObj.value !== null && setObj.value !==""){
				rtnValObject["condition"].push(setObj);	
			}
		});
	}else{
		// console.log("조건을 설정하지 않음");
	}
	
	// order by 정보 get
	if(dataset.orderbyDataset.length > 0 && 
			$("#"+dashboard_config.orderbyAreaId).find(".data-orderby-form").length > 0){
		rtnValObject["orderby"] = [];
		var _orderbyInfoObj = {
				tableName : null,
				columnName: null,
				objInfoName : null,
				calcFunc  : null,
				valueNm : null,
				value : null
		};
		$("#"+dashboard_config.orderbyAreaId).find(".data-orderby-form").each(function(){
			var _setOrderObj = $.extend(true, {}, _orderbyInfoObj);
			var dataOrderNameIdx = $(this).find(".data-orderby-name").val();
			var dataOrderValIdx = $(this).find(".data-orderby-value").val();
			_setOrderObj.tableName = dataset.orderbyDataset[dataOrderNameIdx]["tableName"];
			_setOrderObj.columnName = dataset.orderbyDataset[dataOrderNameIdx]["colName"];
			_setOrderObj.objInfoName = dataset.orderbyDataset[dataOrderNameIdx]["objInfoName"];
			_setOrderObj.calcFunc = dataset.orderbyDataset[dataOrderNameIdx]["calcFunc"];
			_setOrderObj.valueNm = dashboard_config.orderByValInfoAry[dataOrderValIdx]["drawNm"];
			_setOrderObj.value = dashboard_config.orderByValInfoAry[dataOrderValIdx]["value"];
			rtnValObject["orderby"].push(_setOrderObj);
		});
	}else{
		// console.log("정렬을 선택하지 않음");
	}
	
	return rtnValObject;
};


/************************************************************************/ 
/*
함수명 : fn_draw_selected_data_Grid
설	명 : Dashboard 페이지에 Data로 Grid 그리기
인	자 :  _data(key:fields, records Type:Object)
사용법 : 

작성일 : 2019-03-18
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.18	최진	최초 작성
 */
/************************************************************************/
var fn_draw_selected_data_Grid = function(_flag,_data , loadingBarObj){
	if(_flag === "draw" || _flag === "only"){
		if(_data.hasOwnProperty("fields") && _data.hasOwnProperty("records")){
			if(_data.records == undefined || _data.records === null){
				_data.records = [];	
			}
			if(_data.records.length > 0 && (_data.records[0] == undefined || _data.records[0] === null)){
				_data.records = [];
			}
			if(_data.fields == undefined || _data.fields === null){
				_data.fields=  [];
			}
		}else{
			_data = {};
			_data.records = [];
			_data.fields=  [];
		}
		var options = {
				width: "100%",
				paging: true,
				sorting: true,
//				pageSize: 15,
		        pageButtonCount: 5,
		        pagerContainer: "#externalPager",
		        pagerFormat: ' {first} {prev} {pages} {next} {last} &nbsp;&nbsp;  &nbsp;&nbsp; 전체 {pageCount} 페이지 중 현재 {pageIndex} 페이지',
		        pagePrevText: "이전",
		        pageNextText: "다음",
		        pageFirstText: "처음",
		        pageLastText: "마지막",
		        pageNavigatorNextText: "&#8230;",
		        pageNavigatorPrevText: "&#8230;",
				noDataContent: "데이터가 없습니다.",
				loadMessage: "데이터를 불러오는 중입니다...",
				pagerContainerClass: "custom-jsgrid-pager-container",
		        pagerClass: "custom-jsgrid-pager",
		        pagerNavButtonClass: "custom-jsgrid-pager-nav-button",
		        pagerNavButtonInactiveClass: "custom-jsgrid-pager-nav-inactive-button",
		        pageClass: "custom-jsgrid-pager-page",
		        currentPageClass: "custom-jsgrid-pager-current-page",
		};
		
			var fields =_data.fields; 
			for(var i=0;i<fields.length; i++){
				fields[i]["headercss"] =  "custom-main-jsgrid-header";
				fields[i]["css"] =  "custom-main-jsgrid-col-css";
				fields[i]["align"] =  "center";
				fields[i]["width"] =  "auto";
				
				if(fields[i]["type"] === "number"){
					fields[i]["itemTemplate"] = function(value, item){
						value = Math.round(value);
						value = value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
						return value;
	                };
				}
			}
			options.data = _data.records;
			options.fields=  fields;
		
		$("#"+dashboard_config.gridElemId).jsGrid('destroy');
		$("#"+dashboard_config.gridElemId).jsGrid(options);	
		if(loadingBarObj !== undefined && loadingBarObj !== null){
			loadingBarObj.hide();	
		}
	}else{
		$("#"+dashboard_config.gridElemId).jsGrid('destroy');
	}
	
};

/************************************************************************/ 
/*
함수명 : fn_draw_only_column_jsGrid
설	명 : Dashboard 페이지에 field 만 Grid 그리기
인	자 :  _data(Type:array)
사용법 : 

작성일 : 2019-03-28
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.28	최진	최초 작성
 */
/************************************************************************/
var fn_draw_only_column_jsGrid = function(_data, loadingBarObj){
	var _locate = [], _key,_index, _detailDataset,
	_convertedObj = {
			name :"",
			title:"",
			type:"",
			headercss : "custom-main-jsgrid-header"
	}, rtnObj={
			fields : [],
			records:[]
	};
	
	for(var i = 0; i < _data.length;i++){
		_locate = _data[i].split("_");
		if(_locate.length === 2){
			_key=_locate[0];
			_index = _locate[1];
			_detailDataset = dataset[_key][_index];
			
			var _rtnObj = $.extend(true,{},_convertedObj);
		
			_rtnObj.name = _detailDataset["tableName"]+"_"+_detailDataset["colName"];
			_rtnObj.title = fn_txtChange_aggreate(_detailDataset, dashboard_config.detailObjAggreateInfoAry);
			
			if(_detailDataset["dataType"] === "문자"){
				_rtnObj.type =  "text";	
			}else if(_detailDataset["dataType"] === "숫자"){
				_rtnObj.type =  "number";
			}
			rtnObj.fields.push(_rtnObj);
		}
	}
	
	fn_draw_selected_data_Grid("only",rtnObj, loadingBarObj);
};

/************************************************************************/ 
/*
함수명 : fn_refect_userSelected_dataset
설	명 : Dashboard 조회 시 유저가 선택한 Dataset을 리턴
인	자 :  
사용법 : 

작성일 : 2019-03-13
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.13	최진	최초 작성
 */
/************************************************************************/
var fn_refect_userSelected_dataset = function(){
	var _dataParams = {}, isTwoObjPlay = true;
	
	
	if(userDataset.objSelect2 === undefined || 
			userDataset.objSelect2 === null || 
			userDataset.objSelect1 === "" || 
			userDataset.objSelect2 === "notSelected"){
		isTwoObjPlay = false;
	}
	
	// select [컬럼] 리스트 출력
	var _objectDetailInfo = fn_get_value_object_detail_info(userDataset.clickedObjectDetailInfo);

	// from
	_dataParams["tbName01"] = userDataset.objSelect1; 
	_dataParams["detailInfo"] =  _objectDetailInfo;
	
	// 조인 관계 출력 "두번째 테이블이 존재할 때만" 
	if(isTwoObjPlay){
		_dataParams["tbName02"] = userDataset.objSelect2;
		
		// 선택한 테이블의 해당 join만 가공해야한다.
		var relInfoAry = dataset["objectRelInfo"][userDataset.objSelect1];
		for(var k in relInfoAry){
			if(_dataParams["tbName01"] == relInfoAry[k]["stdTable"] &&
					_dataParams["tbName02"] == relInfoAry[k]["connTable"]){
				_dataParams["relationInfo"] = relInfoAry[k]["joinExpr"];
				break;
			}
		}
		
	}
	
	var _copyDataset = $.extend(true, {}, _dataParams),
	// where 절의 시간 출력 (필수)
	// where 절의 조건 절 출력
	// where 절의 order by 절 출력
	_conditionData = fn_get_value_conditionNorderbyNdate_info();
	_dataParams["condition"] = _conditionData; 
	
	return Object.assign(_copyDataset, $.extend(true, {} , _conditionData));
};

/************************************************************************/ 
/*
함수명 : fn_action_select_Action
설	명 : Dashboard 페이지에서 최종 조회버튼 클릭시 동작하는 함수
인	자 :  
사용법 : 

작성일 : 2019-03-13
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.03.13	최진	최초 작성
 */
/************************************************************************/

var fn_action_select_Action= function(gridElemId, _fnCallback){
	var isPlay = true;
	var loadingBar  = null;
	var Dashboard_loading = null; 
	// validation check
	if(userDataset.objSelect1 === undefined || 
			userDataset.objSelect1 === null || 
			userDataset.objSelect1 === "" || 
			userDataset.objSelect1 === "notSelected"){
		isPlay = false;
	}
	
	if(dataset.objectDetailInfo1.length === 0){
		isPlay = false;
	}
	
	
	if(isPlay){
		
		var _selectDataset = fn_refect_userSelected_dataset();
		var _detail = _selectDataset.detailInfo;
		var _deIdx;
		for(_deIdx in _detail){
			_detail[_deIdx]["objCondInfoName"] = fn_txtChange_aggreate(_detail[_deIdx],dashboard_config.detailObjAggreateInfoAry) 
		}
		if(_detail.length > 0){
//			// console.log(_selectDataset);
			var loadIndicator = new loading_bar2(); 
			$.ajax({
				type : "POST",
				url:OlapUrlConfig.selectGridData,
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				data : JSON.stringify(_selectDataset),
				contentType: "application/json;charset=UTF-8",
				beforeSend : function(xhr){
			        xhr.setRequestHeader("AJAX", true);
					loadIndicator.show(); },
				success : function(data, status, xhr){
					if(status === "success"){
						// console.log("success");	
						if(data.hasOwnProperty("fields") && data.hasOwnProperty("records")){
							fn_draw_selected_data_Grid("draw", data, loadIndicator);	
							userDataset.resultDataset = data;
							_fnCallback(data);
						}else{
							// Object에 값이 없음.
							fn_draw_only_column_jsGrid(userDataset.clickedObjectDetailInfo, loadIndicator);
						}
					}else{
						// 실패
						console.error("통신fail");
						loadIndicator.hide();

					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					loadIndicator.hide();
					if(jqxXHR.status === 400){
						alert("조회에 실패하였습니다. 관리자에게 문의하여 주십시오.");
					}else if (jqxXHR.status == 401) {
			            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
			            	window.location.replace(OlapUrlConfig.loginPage);
			            });
			             
			             
			         } else if (jqxXHR.status == 403) {
			            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
			            	window.location.replace(OlapUrlConfig.loginPage);
			            });
			              
			         }else{
						alert("서버와 통신에 문제가 있거나 조회가 불가능한 항목이 있습니다. <br>관리자에게 문의하여 주십시오.");
					}
				}
			});
			
		}else{
			alert("최소 한개 이상의 객체 정보를 선택해야 합니다.");
		}
	}else{
		alert("조회를 하기 위한 객체 정보를 지정하십시오.");
	}// isplay
}

/************************************************************************/ 
/*
함수명 : fn_init_append_notice_div
설	명 : Dashboard 페이지의 안내 문구를 생성하는 함수
인	자 :  elemId, _noticeText
사용법 : 

작성일 : 2019-04-12
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.12	최진	최초 작성
 */
/************************************************************************/
var fn_init_append_notice_div = function(elemId, _noticeText){
	var $div = $("<div>"), $p = $("<p>");
	var txtAry = _noticeText.split("<br>");
	$div.addClass("custom-none-div");
	if(elemId === dashboard_config.areaDomId){
		$div.addClass("custom-none-condition-div");	
	}else{
		$div.addClass("custom-none-orderby-div");
	}
	$p.addClass("div-shadow-text text-center");
	$p.append(_noticeText);
	$div.append($p);
	$("#"+elemId).append($div);
}



/************************************************************************/ 
/*
함수명 : fn_init_append_notice_div
설	명 : Dashboard 페이지의 안내 문구를 Clear하는 함수
인	자 :  elemId, _noticeText
사용법 : 

작성일 : 2019-04-12
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.12	최진	최초 작성
 */
/************************************************************************/
var fn_clear_noticeText_div = function(elemId){
	if($("#"+elemId).find("div").hasClass("custom-none-div")){
		$("#"+elemId).empty();	
	}
}


/************************************************************************/ 
/*
함수명 : fn_txtChange_aggreate
설	명 : 그리드에 draw하기전 objCondInfoName + "집계함수 명"을 리턴하는 함수
인	자 :  _data, argTxtAry
사용법 : 

작성일 : 2019-04-12
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.06.28	최진	최초 작성
 */
/************************************************************************/
var fn_txtChange_aggreate = function(_data, argTxtAry){
	var _aggreateIdx, _aggTxt = "";
	// 06.28 집계함수 컬럼명 변경
	if(_data["calcFuntionYn"] === "Y" && _data["calcFunc"] !== null){
		
		for(_aggreateIdx in argTxtAry){
			if(argTxtAry[_aggreateIdx]["value"] === _data["calcFunc"]){
				_aggTxt =  dashboard_config.objDetailAggreateConnTxt + argTxtAry[_aggreateIdx]["drawNm"]; 
				break;
			}
		}
		
	}

	return _data["objCondInfoName"] + _aggTxt;
}

/**
*
*TEST
*
*/

var searchList = {
		searchDataSet : {				
				fields : [
					{name : "순서" , type: "number" , width: 50, validate : "required"},
					{name : "항목" , type: "text" , width : 150 },
					{name : "값" , type: "text" , width : 150},
					{name : "옵션" , type:"select", width : 150},
					{name : "삭제", type:"control"},
					]
			
			
		
		}
}
		
	

	

