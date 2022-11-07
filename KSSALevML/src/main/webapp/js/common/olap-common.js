
/******************************************************** 
파일명 : olap-common.js 
설 명 : 공통
수정일	수정자	Version	Function 명
-------	--------	----------	--------------
2019.03.04	최 진	1.0	최초 생성
 *********************************************************/

'use strict';


Object.equals = function(x, y) { 
	if (x === y) return true; 
	// if both x and y are null or undefined and exactly the same 
	if (!(x instanceof Object) || !(y instanceof Object)) return false; 
	// if they are not strictly equal, they both need to be Objects 
	if (x.constructor !== y.constructor) return false; 
	// they must have the exact same prototype chain, the closest we can do is 
	// test there constructor. 
	for (var p in x) { if (!x.hasOwnProperty(p)) continue; 
	// other properties were tested using x.constructor === y.constructor 
	if (!y.hasOwnProperty(p)) return false; 
	// allows to compare x[ p ] and y[ p ] when set to undefined
	if (x[p] === y[p]) continue;
	// if they have the same strict value or identity then they are equal
	if (typeof(x[p]) !== "object") return false;
	// Numbers, Strings, Functions, Booleans must be strictly equal
	if (!Object.equals(x[p], y[p])) return false;
	// Objects and Arrays must be tested recursively
	}
	for (p in y) { if (y.hasOwnProperty(p) && !x.hasOwnProperty(p)) return false;
	// allows x[ p ] to be set to undefined
	}
	return true; 
}



//Number.isNaN() 함수가 적용되지 않을 때 추가할 내용 ( ie 11 이하 ) 
if (!Number.isNaN){ 
	Number.isNaN = function isNaN ( value ){  return value !== value;  };
} 
/**
 * 
크롬 버전에서는 정상동작하지만, Internet Explorer버전에서 
assign 함수가 동작하지 않는 문제 해결을 위한 로직
 */
if (typeof Object.assign != 'function') {
	// Must be writable: true, enumerable: false, configurable: true
	Object.defineProperty(Object, "assign", {
		value: function assign(target, varArgs) { // .length of function is 2
			'use strict';
			if (target == null) { // TypeError if undefined or null
				throw new TypeError('Cannot convert undefined or null to object');
			}

			var to = Object(target);

			for (var index = 1; index < arguments.length; index++) {
				var nextSource = arguments[index];

				if (nextSource != null) { // Skip over if undefined or null
					for (var nextKey in nextSource) {
						// Avoid bugs when hasOwnProperty is shadowed
						if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
							to[nextKey] = nextSource[nextKey];
						}
					}
				}
			}
			return to;
		},
		writable: true,
		configurable: true
	});
}



function fn_comm_isParamChk(_param){
	var _rtnVal = false;
	if(_param !== undefined && _param !== null){
		_rtnVal = true;
	}
	return _rtnVal;
}

/************************************************************************/ 
/*
함수명 : alert
설	명 : "경고창" 공통 함수
인	자 : msg(메시지 String), _fncallback(콜백함수 Function)  
사용법 : 
첫번째 인자에 String을 입력 콜백함수가 필요할 경우에만 두번째 인자에 Function 삽입 

작성일 : 2019-04-02
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.02	최진	최초 작성
 */
/************************************************************************/
function alert(msg, _fn_callback){
	
	var _config = {
			message: msg,
			buttons :{
				ok : {
					label: '확인',
					className: 'btn btn-sm btn-primary'
				}
			},
			callback:function(){ 
				// 확인창 클릭 후 유효성 문제 있어서 빨간색으로 표시된 컬럼 원래대로 복구
				//$("#jsGrid").find(".jsgrid-cell").removeClass('jsgrid-invalid');
				//$("#jsGridInfo").find(".jsgrid-cell").removeClass('jsgrid-invalid');
				$("div[id*='jsGrid']").find(".jsgrid-cell").removeClass('jsgrid-invalid');
			}
	};

	if(_fn_callback !== undefined && _fn_callback !== null && _fn_callback instanceof Function){
		_config.callback=_fn_callback;
	}

	bootbox.setLocale("ko");
	bootbox.alert(_config);
}

/************************************************************************/ 
/*
함수명 : errAlert
설	명 : "에러 메시지 창" 공통 함수
인	자 : errState(에러코드 String), msg(메시지 String), _fncallback(콜백함수 Function)  
사용법 : 
 콜백함수가 필요할 경우에만 세번째 인자에 Function 삽입 

작성일 : 2019-04-29
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.29	최진	최초 작성
	2022.10.27 최진 일부 수정
 */
/************************************************************************/
function errAlert(errState, msg, _fn_callback){
	
	 var response = $.parseHTML(msg);
	 var $div =$("<div>");
	 var $p = $("<p>");
	 var $pre = $("<pre>");
	 
	 if(commonFunc.chkValue(errState)){
		 $p.append("문제가 발생했습니다. Error Code: "+errState);
	 }
	 $pre.addClass("custom-error-textarea");
	 $pre.html(response);
	 $div.append($p);
	 $div.append($pre);
	 alert($div, _fn_callback);
}
/************************************************************************/ 
/*
함수명 : confirm
설	명 : "확인" 공통 함수
인	자 : msg(필수:메시지 String or Object), _fncallback(필수:콜백함수 Function)  
사용법 : 
첫번째 인자에 String or Object(옵션)을 입력,  두번째 인자에 Function 삽입 
콜백함수의 첫 번째 인자 Argument가 boolean으로 전달한다(확인일 경우 True, else false) 
Object(옵션)
{
	message : "", // 메시지(필수)
	title :"",	  // 상단 제목 표시(선택)
	size: "small", // 창 사이즈 기본 'large' (선택)
	 buttons: {						// 버튼 스타일 (선택)
        confirm: {
            label: 'Yes',
            className: 'btn-success'
        },
        cancel: {
            label: 'No',
            className: 'btn-danger'
        }
    },
    centerVertical: true // 창을 화면 가운데로 위치(기본)

}


작성일 : 2019-04-02
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.02	최진	최초 작성
 */
/************************************************************************/

function confirm(msgNOptions, _fn_callback){
	var _config = {
			locale: 'ko',
			swapButtonOrder:true,
			centerVertical: true
	}, isPlay = true;

	if(typeof msgNOptions === "string"){
		_config.message = msgNOptions;
	}else if(msgNOptions instanceof Object){
		var _extendConfig = $.extend({}, _config, msgNOptions);
		_config = $.extend(true,{},_extendConfig);
	}else{
		isPlay  = false;
		console.log("No Vaild Arguments");
	}


	if(_fn_callback !== undefined && _fn_callback !== null && _fn_callback instanceof Function && isPlay){
		_config.callback=_fn_callback;
		bootbox.confirm(_config);	  
	}
}


/************************************************************************/ 
/*
함수명 : loading_bar
설	명 : "로딩바" 공통 함수
인	자 : options(선택:옵션값 String or Object)  
첫번째 인자에 String or Object(옵션)을 입력
Object(옵션)
{
	message : "", // 메시지(선택)
	title :"",	  // 상단 제목 표시(선택)
	size: "small", // 창 사이즈 기본 'large' (선택)
    centerVertical: true // 창을 화면 가운데로 위치(기본:true)

}
사용법 :
return 값이 객체로 리턴

로딩바 노출 :
	var loadingBar = loading_bar(); // 호출하거나 아래와 같이 옵션값을 추가하여 호출
 	var loadingBar = loading_bar({message:'로딩중', size: 'small'}); // 변수 할당 즉시 호출

로딩바 제거 : // 비동기 통신일 경우 setTimeout을 부여해야 한다.

 setTimeout(function(){
	loadingBar.modal('hide');	
},300);


작성일 : 2019-04-02
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.02	최진	최초 작성
 */
/************************************************************************/
function loading_bar(options){
	var msg="Loading",
	_config = {
			message: '',
			centerVertical: true,
			closeButton: false
	};

	if(options !== undefined && options !== null && options instanceof Object){
		if(options.hasOwnProperty("message")){
			msg = options["message"];	
		}

		if(options.hasOwnProperty("title")){
			_config.title = options["title"];	
		}

		if(options.hasOwnProperty("size")){
			_config.size = options["size"];	
		}

		if(options.hasOwnProperty("centerVertical")){
			_config.centerVertical = options["centerVertical"];	
		}
	}

	_config.message ='<p><i class="fa fa-spin fa-spinner"></i> '+msg+'...</p>'; 

	
	return bootbox.dialog(_config);
	
	

}

/************************************************************************/ 
/*
함수명 : loading_bar2
설	명 : "로딩바" 공통 함수
인	자 : 
사용법 :

기본 객체 생성

var loadIndicator = new loading_bar2(); // 초기화 

로딩바 :
	loadIndicator.show(); // 로딩바 호출
 	loadIndicator.hide(); // 로딩바 종료

작성일 : 2019-04-05
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.05	최진	최초 작성
 */
/************************************************************************/
var loading_bar2 = function(){
	this.loading_bar = null;
	if(jsGrid !== undefined && jsGrid !== null){
		this.loading_bar = new jsGrid.LoadIndicator({message:"로딩중..."});	
	}
};

loading_bar2.prototype.show =function(){
 	if(this.loading_bar !== undefined && this.loading_bar !== null){
		this.loading_bar.show();	
	}
	return this;
}
loading_bar2.prototype.hide = function(){
	if(this.loading_bar !== undefined && this.loading_bar !== null){
		this.loading_bar.hide();		
	}
	return this;
}

/************************************************************************/ 
/*
함수명 : ConvertDateObjStr
설	명 : Date 객체를 String으로 변환하는  공통 함수
인	자 : _dateObj(필수: Date Object), exec (구분값 : String  ex: "/"로 입력할 경우 2019/01/01)  

리턴 값 type : String

작성일 : 2019-04-02
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
	2019.04.02	최진	최초 작성
 */
/************************************************************************/
function ConvertDateObjStr(_dateObj, exec){
	var _dateStr = ""+_dateObj.getFullYear();

	if(exec === undefined || exec === null || exec === ""){
		exec = "_";
	}

	if(_dateObj.getMonth()+1 < 10){
		_dateStr += exec+"0"+(_dateObj.getMonth()+1);
	}else{
		_dateStr += exec+(_dateObj.getMonth()+1);
	}

	if(_dateObj.getDate() < 10){
		_dateStr += exec + "0"+_dateObj.getDate();
	}else{
		_dateStr += exec + _dateObj.getDate();
	}


	return _dateStr;
}

/************************************************************************/ 
/*
함수명 : isContains
설	명 : Array에 인자유무 확인
인	자 : element : 비교값

리턴 값 type : boolean

작성일 : 2019-04-02
작성자 : 최진

	수정일	수정자	수정내용
	------	------	-------------------
 */
/************************************************************************/
function isContains(_array, element){
	for (var i = 0; i < _array.length; i++) {
		if (_array[i] == element) {
			return true;
		}
	}
	return false;
}




let commonFunc = {
		chkValue : function(value){
			if(value !== undefined && value !== null){
				return true;
			}else {
				return false;
			}
		},
		chkNotEmptyStr : function(value){
			let defaultChk = this.chkValue(value);
			if(defaultChk && value !== ""){
				return true;
			}else{
				return false;
			}
		},
		chkAry : function(value){
			let defaultChk = this.chkValue(value);
			if(defaultChk  && Array.isArray(value)){
				return true;
			}else{
				return false;
			}
		},
		
	    // grid의 필드를 표시하기 위해 조건에 따라 텍스트 컨버트
	    txtChangeAggreate : function(_data, argTxtAry){
	    	let _aggreateIdx, _aggTxt = "";
	    	// 06.28 집계함수 컬럼명 변경
	    	if(_data["calcFuntionYn"] === "Y" && _data["calcFunc"] !== null){
	    		
	    		for(_aggreateIdx in argTxtAry){
	    			if(argTxtAry[_aggreateIdx]["value"] === _data["calcFunc"]){
	    				_aggTxt =  dataViewConfig.txtRefect.objDetailAggreateConnTxt + argTxtAry[_aggreateIdx]["drawNm"]; 
	    				break;
	    			}
	    		}
	    		
	    	}
	    	
	    	return _data["objInfoName"] + _aggTxt;
	    },
		ajaxFailAction : function(_failObj){
			// console.log("fail fn_call_object_detail_info");
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
		},
			    
};
