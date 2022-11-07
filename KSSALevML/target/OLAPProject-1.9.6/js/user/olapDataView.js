/******************************************************** 
파일명 : olapDataView.js 
설 명 : OLAP 화면의 "데이터 조회 화면" Javascript 파일
수정일	수정자	Version	Function 명

필수 조건
jquery가 필요
jquery-ui가 필요
JSGrid Library가 필요

-------	--------	----------	--------------
2019.10.15	최 진	1.0	최초 생성
 *********************************************************/
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
			if(olapDataView.loadingBar !== null){
				olapDataView.loadingBar.hide();
				olapDataView.loadingBar = null;
			}
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

// Config
let dataViewConfig = {
		elemIdSet :{
			gridElemId :"objectViewGrid",
			pagerId:"externalPager",
			condAddBtn:"conditionAdd",
			condAreaId:"conditionArea",
			orbyAddBtn:"orderbyAdd",
			orbyAreaId:"orderbyArea",
			submitBtn:"dataSubmitBtn",
			clearBtn:"clearCondBtn"
			//dashdaveBtn : "dashSaveBtn"
		},
		classSet : {
			condForm:"data-condition-form",
			condName:"data-condition-name-select",
			condVal :"data-condition-value-select",
			condOper: "data-condition-oper-select",
			condUnit :  "data-condition-unit-select"
		},
		txtRefect : {
			conditionNoticeText : "조건을 추가하려면<br> [추가]버튼을 클릭하십시오.",
			orderbyNoticeText : "정렬을 추가하려면 항목을 선택 후<br> [추가]버튼을 클릭하십시오.",
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
			],
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
			unitInfo :{}
		},
	
		grid : {
			fieldset : []
		},
};


let olapDataView = {
		tbName : null,
		dataset : {
			objectDetailInfo:[],
			detailOptionInfo:[],
			userSelectedCond:[],
			userSelectedOrby:[],
			resultGridDataset:{
				fields:[],
				records:[]
			},
			unitRelInfo : {}
		},
		loadingBar : null,
		condDialogObj : null,
		condGridObj : null,
		// 기본 정보 가져오기
		init : function(gridElemId, pagerId){
			let _that = this;

			if(commonFunc.chkValue(gridElemId)){
				dataViewConfig.elemIdSet.gridElemId = gridElemId;
			}
			
			if(commonFunc.chkValue(pagerId)){
				dataViewConfig.elemIdSet.pagerId = pagerId;
			}
			
			// 테이블 명 가져오기
			$.ajax({ 
				type:"GET",
				url:OlapUrlConfig.getTbName, 
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     }
			}).done(function(_result){
				// 세부정보 가져오기
				_that.tbName = _result.tbName;
				
				$.ajax({
					type:"GET",
					url:OlapUrlConfig.objDetailInfo,
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
					data : {
						tbName : _that.tbName 
					}
				}).done(function(_result){
					_that.dataset.objectDetailInfo = _result;
					_that.onlyColumnGridSetting(_result); // init에는 로딩바 표현안함.
				}).fail(commonFunc.ajaxFailAction); // AJAX
				
			}).fail(commonFunc.ajaxFailAction);
			
			$.ajax({ 
				type:"GET",
				url:OlapUrlConfig.unitInfo, 
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				     data :{
				    	 flag : "unitInfo"
				     }
			}).done(function(_result){
				// 세부정보 가져오기
				
				if(commonFunc.chkAry(_result)){
					for(let i=0;i<_result.length;i++){
						let codeKey = _result[i]["GRP_CODE"];
						if(dataViewConfig.txtRefect.unitInfo.hasOwnProperty(codeKey)){
							dataViewConfig.txtRefect.unitInfo[codeKey].push(_result[i]);
						}else{
							dataViewConfig.txtRefect.unitInfo[codeKey] = [_result[i]];
						}
					}// for
				}else{
					commonFunc.ajaxFailAction({status:500});
				}
				
				
			}).fail(commonFunc.ajaxFailAction); // AJAX
			
			
		},
		
		// dialog 그리기
		drawCondAddDialogForm : function(dialogId, btnId){
			let _that = this;
			let fieldSetAry = [
				{key: "catagory",name:"항목", type:"text"},
				{key: "value",name:"값", type:"text"},
				{key: "option",name:"옵션", type:"select"},
				]; 
			
			
			 $("#"+dialogId).attr("title","조회조건 추가하기");
			 let $form = $("<form>");
			 let $fieldSet = $("<fieldset>");
			 for(let i=0;i<fieldSetAry.length;i++){
				 let $label = $("<label>");
				 let $input = $("<input>");
				 let type = fieldSetAry[i]["type"],
				 	keyName = fieldSetAry[i]["key"],
				 	name = fieldSetAry[i]["name"];
				 $label.attr("for",keyName);
				 $label.append(name);
				 $input.attr("id", keyName);
				 $input.attr("name", keyName);
				 $input.attr("type", type);
				 if(type === "text"){
					 $input.attr("class", "text ui-widget-content ui-corner-all");	 
				 }else{
					 $input.attr("class", "text ui-widget-content ui-corner-all");
				 }
				 
				 $fieldSet.append($label);
				 $fieldSet.append($input);
				 				 
			 }
			 $form.append($fieldSet);
			 $("#"+dialogId).append($form);
			 _that.condDialogObj = $("#"+dialogId).dialog({
			      autoOpen: false,
			      height: 400,
			      width: 350,
			      modal: true,
			      buttons: {
			    	OK : function(){
			    		
			    	},  
			        Cancel: function() {
			        	_that.condDialogObj.dialog( "close" );
			        }
			      },
			      close: function() {
			        
			        
			      }
			    });
			 
			 _that.showCondAddDialogBtnEvtBind(btnId);
		},
		showCondAddDialogBtnEvtBind : function(btnId) {
			let _that = this;
			$("#"+btnId).on("click",function(e){
				if(_that.condDialogObj !== null){
					_that.condDialogObj.dialog('open');
				}
				
			});
			
	    },
	    
	    initDrawClear : function(){
	    	olapDataView.onlyColumnGridSetting(olapDataView.dataset.objectDetailInfo);
	    	if($("#"+dataViewConfig.elemIdSet.condAreaId).find("div").hasClass("custom-none-div") === false){
    			$("#"+dataViewConfig.elemIdSet.condAreaId).empty();
    			olapDataView.fn_init_append_notice_div(dataViewConfig.elemIdSet.condAreaId, dataViewConfig.txtRefect.conditionNoticeText);
    		}
    		
    		if($("#"+dataViewConfig.elemIdSet.orbyAreaId).find("div").hasClass("custom-none-div") === false){
    			$("#"+dataViewConfig.elemIdSet.orbyAreaId).empty();
    			olapDataView.fn_init_append_notice_div(dataViewConfig.elemIdSet.orbyAreaId, dataViewConfig.txtRefect.orderbyNoticeText);
    		}
	    },
	    
	    evtHandler : function(idSetObj){
	    	let _that = this;
				
	    	if(idSetObj.hasOwnProperty("condBtn") && commonFunc.chkValue(idSetObj.condBtn)){
	    		dataViewConfig.elemIdSet.condAddBtn = idSetObj.condBtn;
	    	} 	
	    	if(idSetObj.hasOwnProperty("condArea") && commonFunc.chkValue(idSetObj.condArea)){
	    		dataViewConfig.elemIdSet.condAreaId = idSetObj.condArea;
	    	}
	    	if(idSetObj.hasOwnProperty("orbyBtn") && commonFunc.chkValue(idSetObj.orbyBtn)){
	    		dataViewConfig.elemIdSet.orbyAddBtn = idSetObj.orbyBtn;
	    	}
	    	if(idSetObj.hasOwnProperty("orbyArea") && commonFunc.chkValue(idSetObj.orbyArea)){
	    		dataViewConfig.elemIdSet.orbyAreaId = idSetObj.orbyArea;
	    	}
	    	if(idSetObj.hasOwnProperty("submitBtn") && commonFunc.chkValue(idSetObj.submitBtn)){
	    		dataViewConfig.elemIdSet.submitBtn = idSetObj.submitBtn;
	    	}
	    	if(idSetObj.hasOwnProperty("clearBtn") && commonFunc.chkValue(idSetObj.clearBtn)){
	    		dataViewConfig.elemIdSet.clearBtn = idSetObj.clearBtn;
	    	}
	    /*	if(idSetObj.hasOwnProperty("dashdSaveBtn") && commonFunc.chkValue(idSetObj.dashdSaveBtn)){
	    		dataViewConfig.elemIdSet.dashdSaveBtn = idSetObj.dashdSaveBtn;
	    	}*/
	    	
	    	// 조건 추가 버튼 이벤트
	    	$("#"+dataViewConfig.elemIdSet.condAddBtn).on("click",function(e){

	    		if(olapDataView.dataset.objectDetailInfo.length > 0){
	    			if($("#"+dataViewConfig.elemIdSet.condAreaId).find("div").hasClass("custom-none-div")){
		    			$("#"+dataViewConfig.elemIdSet.condAreaId).empty();	
		    		}
	    			olapDataView.condAddAction();	
	    		}
	    		
	    		
	    	});
	    	
	    	// 정렬 버튼 이벤트
	    	$("#"+dataViewConfig.elemIdSet.orbyAddBtn).on("click",function(e){
	    		if(olapDataView.dataset.objectDetailInfo.length > 0){
	    			if($("#"+dataViewConfig.elemIdSet.orbyAreaId).find("div").hasClass("custom-none-div")){
		    			$("#"+dataViewConfig.elemIdSet.orbyAreaId).empty();	
		    		}
	    			olapDataView.orderbyAction();	
	    		}
	    		
	    		
	    	});
	    	
	    	// 초기화 버튼 이벤트
	    	$("#"+dataViewConfig.elemIdSet.clearBtn).on("click",function(e){
	    		// area 초기화
	    		olapDataView.initDrawClear();
	    		olapDataView.dataset.userSelectedCond = [];
	    		olapDataView.dataset.userSelectedOrby = [];
	    		
	    	});
	    	
	    	// 조회 버튼 이벤트
	    	$("#"+dataViewConfig.elemIdSet.submitBtn).on("click",function(e){
	    		olapDataView.submitAction();
	    	});
	    	
	    },
	  
	    //Dashboard 페이지의 안내 문구를 생성하는 함수
	    fn_init_append_notice_div : function(elemId, _noticeText){
	    	var $div = $("<div>"), $p = $("<p>");
	    	var txtAry = _noticeText.split("<br>");
	    	$div.addClass("custom-none-div");
	    	$div.addClass("d-flex justify-content-center align-items-center");
	    	$p.addClass("div-shadow-text text-center");
	    	$p.append(_noticeText);
	    	$div.append($p);
	    	$("#"+elemId).append($div);
	    },
	    /*************************************
	     * 조건 추가
	     * ***********************************
	     */
	    // 조건추가 
	    condAddAction : function(condDataset){
	    	let _that = this;
	
	    	let elemConfig = {
	    		condSetClass : "form-inline d-flex justify-content-center" ,
	    		categorySelectBoxClass : "form-control form-control-sm my-1 mr-1",
	    		unitSelectBoxClass :  "form-control form-control-sm my-1 mr-1",
	    		inputTypeClass : "form-control form-control-sm my-1 mr-1",
	    		operSelectBoxClass : "form-control form-control-sm my-1 mr-1",
	    		delIconBtnClass:"btn btn-sm btn-outline-secondary fn-delConditionIcon",
	    		awesomeIcon:"fas fa-times-circle",
	    		delBtnStyle:"height:30px;",
	    	};
	    	elemConfig.condSetClass +=" " + dataViewConfig.classSet.condForm;
	    	elemConfig.categorySelectBoxClass +=" " + dataViewConfig.classSet.condName;
	    	elemConfig.inputTypeClass += " " + dataViewConfig.classSet.condVal;
	    	elemConfig.operSelectBoxClass += " "+ dataViewConfig.classSet.condOper;
	    	elemConfig.unitSelectBoxClass += " "+ dataViewConfig.classSet.condUnit;
	    	
	    	
	    	
	    	// 조건 Element Draw
	    	let addDrawCondElem = function(_condDataset){
//	    		fn_draw_condition_area
	    		// 조건 중 옵션 select value 그리기
	    		let drawOperOption = function(dom, operData){
	    			for(let k=0;k<operData.length;k++){
	    				dom.append(new Option(operData[k]["qryConCodeNm"], k));
					}	
	    		};
	    		
	    		/**
	    		 * 단위 옵션 draw
	    		 */
	    		let drawUnitOption = function(dom,colName){
	    			if(_that.dataset.unitRelInfo.hasOwnProperty(colName)){
	    				dom.empty();
	    				if(dom.hasClass("d-none")){
	    					dom.removeClass("d-none");
	    				}
						let unitRel = _that.dataset.unitRelInfo[colName];
						for(let k=0;k<unitRel.length;k++){
							let grpKey = unitRel[k]["GRP_UNIT"];
							if(dataViewConfig.txtRefect.unitInfo.hasOwnProperty(grpKey)){
								let unitInfo = dataViewConfig.txtRefect.unitInfo[grpKey];
								for(let m=0;m<unitInfo.length;m++){
									dom.append(new Option(unitInfo[m]["UNIT_NAME"], grpKey + "-"+m));	
								}
									
							}
								
						}
						
					}else{
						if(!dom.hasClass("d-none")){
	    					dom.addClass("d-none");
	    					dom.empty();
	    				}
					}
	    		}
	    		
	    		// input -> select 
	    		let replaceInputToSelectElem = function(operData, callback){
	    			let selBox = $("<select>");
	    			let param = {
	    					columnName: operData.columnName,
	    					tableName : operData.tableName
	    			};
	    			selBox.attr("class",elemConfig.inputTypeClass);
	    			
	    			$.ajax({
						type:"POST",
						url:OlapUrlConfig.getSelectList,
						 beforeSend: function(xhr) {
						        xhr.setRequestHeader("AJAX", true);
						     },
						 contentType : 'application/json',
						 data : JSON.stringify(param)
	    			}).done(function(_result){
    					for(let m in _result){
    						let valueObj = _result[m];
    						for(let k in valueObj){
    							let value = valueObj[k];		
    							selBox.append(new Option(value, value));
    						} 
						}// for
    					
    					callback(selBox);
					}).fail(commonFunc.ajaxFailAction); // AJAX
	    			
	    			
	    		}; // replaceInputToSelectElem
	    		let detailInfoData = _that.dataset.objectDetailInfo;
	    		let $condSetDiv = $("<div>");
	    		
	    		let $selectElem = $("<select>");
	    		let $input = $("<input>");
	    		let $operSelect = $("<select>"); // 조건 oper 옵션
	    		let parentBtnDiv = $("<div>");
	    		let closeIconElem = $("<button>");
	    		let btnIcon = $("<i>");
	    			$condSetDiv.attr("class", elemConfig.condSetClass);
	    			$selectElem.attr("class",elemConfig.categorySelectBoxClass);
	    			$input.attr("type","text"); //TODO 향후 type이 추가되면 변경될 수 있음
	    			$input.attr("class",elemConfig.inputTypeClass); 
	    			$input.attr("placeholder","조건 입력"); 
	    			$operSelect.attr("class",elemConfig.operSelectBoxClass)
	    			closeIconElem.attr("class",elemConfig.delIconBtnClass);
	    			closeIconElem.attr("style",elemConfig.delBtnStyle);
	    			btnIcon.attr("class",elemConfig.awesomeIcon);
	    			
	    			
	    			// TODO 단위변환 버튼 추가해야함
	    			
	    			let $unitSelect = $("<select>");  // 단위변환
	    			$unitSelect.attr("class",elemConfig.unitSelectBoxClass);
	    			$unitSelect.addClass("d-none");
	    			
	    			
	    			$condSetDiv.append($selectElem);
	    			$condSetDiv.append($unitSelect);
	    			$condSetDiv.append($input);
	    			$condSetDiv.append($operSelect);
	    			$condSetDiv.append(closeIconElem);
	    			closeIconElem.append(btnIcon);
	    			$("#"+dataViewConfig.elemIdSet.condAreaId).append($condSetDiv);
	    			let selectNameValue = null;
	    			let operDataset = [];
	    			if(detailInfoData.length > 0){
	    				for(let i=0;i<detailInfoData.length;i++){
	    					let operData = detailInfoData[i]["operOption"];
	    					
	    					// 단위
	    					
    						let colName =detailInfoData[i]["colName"];
    						drawUnitOption($unitSelect, colName);	
	    					
	    					if(operData.length > 0){
	    						// 항목
	    						
	    						$selectElem.append(new Option(detailInfoData[i]["objInfoName"],i));
	    						if(commonFunc.chkValue(_condDataset)){
	    							if(_condDataset["columnName"] === colName){
//	    								console.log("같음 항목 셋 :: ", _condDataset["columnName"]);
	    								selectNameValue = "" +i;
	    								$selectElem.val(selectNameValue);
	    							}
	    						}else{
	        						if($operSelect.find('option').length === 0){
		    							// 조건 옵션
		    							drawOperOption($operSelect, operData);
		    							if(operData[0]["qryConCode"] === "K"){
		    								// 목록에서 선택
		    							
		    								replaceInputToSelectElem(operData[0], function(selBoxDom){
		    									if(selBoxDom !== null){
		    										$input.replaceWith(selBoxDom);	
		    									}
		    								});
		    							}
			    					}
	    						}// else commonFunc.chkValue(_condDataset)
	
	    						
	    					}// if
	    					
	    				}//for
	    			}//if
	    			
	    			// 저장목록 불러오기 : 데이터 세팅
	    			if(selectNameValue !== null){
	    				let operOptionSet = _that.dataset.objectDetailInfo[selectNameValue]["operOption"];
	    				drawOperOption($operSelect, detailInfoData[selectNameValue]["operOption"]);
	    				let savedOperValue = null;
	    				for(o_idx in operOptionSet){
	    					if(operOptionSet[o_idx]["qryConCode"] === _condDataset["operQryConCode"]){
	    						savedOperValue = "" + o_idx;
	    					}
	    				}	    				
	    				if(savedOperValue !== null){
	    					$operSelect.val(savedOperValue);	
	    				}
	    				
	    				
//	    				
	    				
	    				if(_condDataset["unitOption"] !== null && _condDataset.unitOption.hasOwnProperty("UNIT_CODE")){
	    					let colName = _condDataset["columnName"];
	    					let unitGrp =  _condDataset["unitOption"]["GRP_CODE"];
	    					drawUnitOption($unitSelect, colName);
//	    					dataViewConfig.txtRefect.unitInfo[uKey][uValue]
	    					let unitData = dataViewConfig.txtRefect.unitInfo[unitGrp];
	    					let getUnitVal = null;
	    					for(let idx in unitData){
	    						if(unitData[idx]["UNIT_CODE"] === _condDataset["unitOption"]["UNIT_CODE"]){
	    							getUnitVal = "" + idx;
	    						}
	    					}
	    					if(getUnitVal !== null){
	    						$unitSelect.val(unitGrp + "-" + getUnitVal);	
	    					}
	    					
	    				}
	    				
	    				if(_condDataset["operQryConCode"] === "K"){
	    					// 목록에서 선택
	    					
							replaceInputToSelectElem({
	    						columnName: _condDataset.columnName,
		    					tableName : _condDataset.tableName	
	    					}, function(selBoxDom){
								if(selBoxDom !== null){
									$input.replaceWith(selBoxDom);	
									selBoxDom.val(_condDataset["value"]);
								}
							});
	    				}else{
	    					$input.val(_condDataset["value"]);
	    				}
	    			}
	    			
	    			
	    			
	    			closeIconElem.on("click", function(evt){
	    				let _that = this;
	    				$(this).off("click");
	    				$(this).parent().remove();
	    				if($(".fn-delConditionIcon").length === 0){
	    					_that.fn_init_append_notice_div(dataViewConfig.elemIdSet.condAreaId, dataViewConfig.txtRefect.conditionNoticeText);
	    				}
	    			});
	    			
	    			
	    			
	    			/**
	    			 * '목록에서 선택' 옵션을 위한 element change action
	    			 */
	    			let changeOptionAction = function(dom, operData, code){
	    				if(code === "K"){
							// 목록에서 선택
							replaceInputToSelectElem(operData[0], function(selBoxDom){
								if(selBoxDom !== null){
									dom.replaceWith(selBoxDom);	
								}
									
							});
						}else{
	   						 if(dom.prop('tagName') ==="SELECT"){
								 let $input = $("<input>");	
					    			$input.attr("type","text"); //TODO 향후 type이 추가되면 변경될 수 있음
					    			$input.attr("class",elemConfig.inputTypeClass); 
					    			dom.replaceWith($input);
							 }
						} //if
    				}; //
	    			
	    			// 항목 변경시 이벤트 
	    			$selectElem.on('change', function(e, callback){
	    				let idx = $selectElem.val();
	    				let _selThat = this;
	    				$(this).parent().find("."+dataViewConfig.classSet.condOper).empty();	
	    				$(this).parent().find("."+dataViewConfig.classSet.condVal).val("");	
	    				var operSelect = $(this).parent().find("."+dataViewConfig.classSet.condOper);
	    				var operData = _that.dataset.objectDetailInfo[idx]["operOption"];
	    				drawOperOption(operSelect, operData);
	    				let optionElem = $(_selThat).parent().find("."+dataViewConfig.classSet.condVal);
	    				changeOptionAction(optionElem, operData, operData[0]["qryConCode"]);
	    				
	    				
	    				// 단위
	    				let detailInfoData = _that.dataset.objectDetailInfo;
						let colName =detailInfoData[idx]["colName"];
						let $unitSelect = $(this).parent().find("."+dataViewConfig.classSet.condUnit);
						drawUnitOption($unitSelect, colName);
	    			});
	    			
	    			// 옵션 변경시 이벤트
	    			$operSelect.on('change',function(e){
	    				let _operThat = this;
	    				let operIdx = $(_operThat).val();
	    				let parentIdx = $(this).parent().find("."+dataViewConfig.classSet.condName).val();
	    				let operDataObj = _that.dataset.objectDetailInfo[parentIdx]["operOption"][operIdx];
	    				let operElem = $(_operThat).parent().find("."+dataViewConfig.classSet.condVal);
	    				changeOptionAction(operElem, [operDataObj], operDataObj["qryConCode"]);
	    			});
	    	}; 
	    	
	    	if(_that.dataset.detailOptionInfo.length > 0){
	    		addDrawCondElem(condDataset);	
	    		
	    	}else{
	    		let tbName = _that.tbName;
	    		$.ajax({
					type:"GET",
					url:OlapUrlConfig.getConditionData,
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
					data : {
						tbNames : [tbName] // 속성 배열로 넣어야 함 
					}
				}).done(function(_result){
					_that.dataset.detailOptionInfo = _result[tbName];
 
//					console.log("unit", _result["unit"]);
					let unitRel = _result["unit"];
					let setObj = {};
					for(let k=0;k<unitRel.length;k++){
						let objKey = unitRel[k]["COL_NAME"];
						if(setObj.hasOwnProperty(objKey)){
							setObj[objKey].push(unitRel[k]);
						}else{
							setObj[objKey] = [unitRel[k]];
						}
					}
					_that.dataset.unitRelInfo = setObj;					
//					console.log("unitRelInfo ", _that.dataset.unitRelInfo);
					let detailInfo = _that.dataset.objectDetailInfo;
					let optionInfo = _result[tbName];
					for(let i=0;i<detailInfo.length;i++){
						detailInfo[i]["operOption"] = [];
						for(let j=0;j<optionInfo.length;j++){
							
							if(detailInfo[i]["tableName"] === optionInfo[j]["tableName"] &&
									detailInfo[i]["colName"] === optionInfo[j]["columnName"]){
								let operObj = {
										qryConCode : optionInfo[j]["qryConCode"],
										qryConCodeNm :optionInfo[j]["qryConCodeNm"],
										operSym :optionInfo[j]["operSym"],
										columnName : optionInfo[j]["columnName"],
										tableName: optionInfo[j]["tableName"] 
								};
								detailInfo[i]["operOption"].push(operObj);	// 기존 dataset객체에 operation option 값을 추가한다.
							}
							
						} //for
					}// for
					addDrawCondElem(condDataset);	
					
				}).fail(commonFunc.ajaxFailAction); // AJAX
	    	}
	    },
	    
	    // 정렬 추가
	    orderbyAction: function(orbyData){
	    	let _that = this;
	    	if(_that.dataset.objectDetailInfo.length > 0){
		    	let elemConfig = {
		    			parentClass : "form-inline data-orderby-form d-flex justify-content-center",
		    			selectOrbyNameElemClass : "form-control form-control-sm my-1 data-orderby-name",
		    			spanClass: "my-1 orderby-name-center text-center",
		    			selectOrbyValClass:"form-control form-control-sm my-1 mr-sm-1 data-orderby-value",
		    			delBtnClass:"btn btn-sm btn-outline-secondary fn-delOrderByIcon",
		    			delIconClass:"fas fa-times-circle",
		    			delBtnStyle:"height:30px;",
		    			spanText:"기준으로"
		    	};
		    	
		    	// Create element 
		    	let $parentDiv = $("<div>");
		    	let $selectOrbyNameElem = $("<select>");
		    	let $span = $("<span>");
		    	let $selectOrbyValElem = $("<select>");
		    	let $delBtnElem = $("<button>");
		    	let $delIconElem = $("<i>");
		    	$parentDiv.attr("class", elemConfig.parentClass);
		    	$selectOrbyNameElem.attr("class", elemConfig.selectOrbyNameElemClass);
		    	$span.attr("class", elemConfig.spanClass);
		    	$span.text(elemConfig.spanText);
		    	$selectOrbyValElem.attr("class",elemConfig.selectOrbyValClass);
		    	$delBtnElem.attr("class",elemConfig.delBtnClass);
		    	$delBtnElem.attr("style",elemConfig.delBtnStyle);
		    	$delIconElem.attr("class",elemConfig.delIconClass);
		    	
		    	
		    	$delBtnElem.append($delIconElem);
		    	$parentDiv.append($selectOrbyNameElem);
		    	$parentDiv.append($span);
		    	$parentDiv.append($selectOrbyValElem);
		    	$parentDiv.append($delBtnElem);
		    	
		    		
		    	// Object select box
		    	let objDetailInfo = _that.dataset.objectDetailInfo;
		    	let savedColIdxData = null;
	    		for(let i=0; i< objDetailInfo.length;i++){
	    			$selectOrbyNameElem.append(new Option(objDetailInfo[i]["objInfoName"],i));
	    			if(commonFunc.chkValue(orbyData) && objDetailInfo[i]["colName"] === orbyData["columnName"]){
	    				savedColIdxData = "" + i;
	    			}
	    			
	    		}
	    		
		    	// DESC / ASC Setting
		    	let orbyVal = dataViewConfig.txtRefect.orderByValInfoAry;
		    	let savedOrbyIdx = null;
		    	for(let j=0;j<orbyVal.length;j++){
		    		$selectOrbyValElem.append(new Option(orbyVal[j]["drawNm"],j));
		    		if(commonFunc.chkValue(orbyData) && orbyData["value"] == orbyVal[j]["value"]){
		    			savedOrbyIdx = "" + j;
		    		}
		    	}
		    	$("#"+dataViewConfig.elemIdSet.orbyAreaId).append($parentDiv);
		    	
		    	if(commonFunc.chkNotEmptyStr(savedColIdxData) &&  commonFunc.chkNotEmptyStr(savedOrbyIdx)){
		    		$selectOrbyNameElem.val(savedColIdxData);
		    		$selectOrbyValElem.val(savedOrbyIdx);
		    	}
		    	
		    	
		    	
		    	$delBtnElem.on("click",function(e){
		    		$(this).off("click");
		    		$(this).parent().remove();
		    		if($(".fn-delOrderByIcon").length === 0){
		    			_that.fn_init_append_notice_div(dataViewConfig.elemIdSet.orbyAreaId, dataViewConfig.txtRefect.orderbyNoticeText);
		    		}
		    	});
		    	
	    	}else{
	    		alert("데이터가 존재하지 않습니다.");
	    	}
	    	
	    },
	    
	    condNOrderbyCreateDataset :function(){
	    	let _that = this;
	    	_that.dataset.userSelectedCond = [];
	    	_that.dataset.userSelectedOrby = [];
	    	
			var _conditionInfoObj = {
					tableName : null,
					columnName: null,
					objInfoName : null,
					unitOption : null,
					value: null,
					operSym: null,
					operQryConCode:null,
					operQryConCodeNm:null
			};
			
			
			// 조건 데이터 setting
			$("#"+dataViewConfig.elemIdSet.condAreaId).find("."+dataViewConfig.classSet.condForm).each(function(){
				var setObj = $.extend(true, {}, _conditionInfoObj);
				var dataNameIdx = $(this).find("."+dataViewConfig.classSet.condName).val();
				var dataOperIdx = $(this).find("."+dataViewConfig.classSet.condOper).val(); 
				setObj.tableName = _that.dataset.objectDetailInfo[dataNameIdx]["tableName"];
				setObj.columnName =_that.dataset.objectDetailInfo[dataNameIdx]["colName"];
				setObj.objInfoName = _that.dataset.objectDetailInfo[dataNameIdx]["objInfoName"];
				// 단위 세팅
				let unitVal =$(this).find("."+dataViewConfig.classSet.condUnit).val(); 
				if(commonFunc.chkValue(unitVal)){
					let uValSet = unitVal.split("-");
					if(uValSet.length > 0){
						let uKey =uValSet[0], uValue = uValSet[1]; 
						setObj.unitOption = dataViewConfig.txtRefect.unitInfo[uKey][uValue];	
					}
					
				}
				setObj.value = $(this).find("."+dataViewConfig.classSet.condVal).val(); 
				setObj.operSym = _that.dataset.objectDetailInfo[dataNameIdx]["operOption"][dataOperIdx]["operSym"];  
				setObj.operQryConCode = _that.dataset.objectDetailInfo[dataNameIdx]["operOption"][dataOperIdx]["qryConCode"];  
				setObj.operQryConCodeNm = _that.dataset.objectDetailInfo[dataNameIdx]["operOption"][dataOperIdx]["qryConCodeNm"]; 
			
				_that.dataset.userSelectedCond.push(setObj);	
			});
		
			// 정렬 데이터 setting
			var _orderbyInfoObj = {
					tableName : null,
					columnName: null,
					objInfoName : null,
					calcFunc  : null,
					valueNm : null,
					value : null
			};
			$("#"+dataViewConfig.elemIdSet.orbyAreaId).find(".data-orderby-form").each(function(){
				var _setOrderObj = $.extend(true, {}, _orderbyInfoObj);
				var dataOrderNameIdx = $(this).find(".data-orderby-name").val();
				var dataOrderValIdx = $(this).find(".data-orderby-value").val();
				_setOrderObj.tableName = _that.dataset.objectDetailInfo[dataOrderNameIdx]["tableName"];
				_setOrderObj.columnName = _that.dataset.objectDetailInfo[dataOrderNameIdx]["colName"];
				_setOrderObj.objInfoName = _that.dataset.objectDetailInfo[dataOrderNameIdx]["objInfoName"];
				_setOrderObj.calcFunc = _that.dataset.objectDetailInfo[dataOrderNameIdx]["calcFunc"];
				_setOrderObj.valueNm = dataViewConfig.txtRefect.orderByValInfoAry[dataOrderValIdx]["drawNm"];
				_setOrderObj.value = dataViewConfig.txtRefect.orderByValInfoAry[dataOrderValIdx]["value"];
				_that.dataset.userSelectedOrby.push(_setOrderObj);
			});
		    	
	    },
	    //저장하기
	    
	    
	    // 조회하기
	    submitAction : function(isExtSavedData){
	    	
	    	// 조건 정렬 세팅
	    	let _that = this;
	    	if(!commonFunc.chkValue(isExtSavedData) || !isExtSavedData){
	    		_that.condNOrderbyCreateDataset();	
	    	}
	    	
	    	// ajax 호출
			let dataParams = {
					"tbName01" : _that.tbName,
					"detailInfo" : _that.dataset.objectDetailInfo,
					"condition" : _that.dataset.userSelectedCond,
					"orderby": _that.dataset.userSelectedOrby
			}
			
			_that.loadingBar= new loading_bar2(); 
	    	// 그리드 그리기
			$.ajax({ 
				type:"POST",
				url:OlapUrlConfig.selectGridData, 
				 beforeSend: function(xhr) {xhr.setRequestHeader("AJAX", true); _that.loadingBar.show();},
					contentType: "application/json;charset=UTF-8",
			     data : JSON.stringify(dataParams),
			}).done(function(data){
//				 console.log("data",data);	
				if(data.hasOwnProperty("fields") && data.hasOwnProperty("records")){
					 _that.drawGrid("draw", data,_that.loadingBar);
					// TODO 엑셀 데이터 셋도 만들어야 ExcelDownloadObject.datasetInit
					 _that.dataset.resultGridDataset.fields = data.fields;
					 _that.dataset.resultGridDataset.records = data.records;
					 
					 if(commonFunc.chkValue(ExcelDownloadObject) && ExcelDownloadObject.hasOwnProperty("datasetInit")){
						 ExcelDownloadObject.datasetInit(data); 
					 }
				}else{
					_that.onlyColumnGridSetting(_that.dataset.objectDetailInfo);
				}
			}).fail(commonFunc.ajaxFailAction);
			
	    
	    },
	    
	    // jsGrid 컬럼 세팅
	    onlyColumnGridSetting : function(_data, loadingBarObj){
	    	
	    	let _convertedObj = {
	    			name :"",
	    			title:"",
	    			type:"",
	    			headercss : "font-size-down"
	    	}, rtnObj={
	    			fields : [],
	    			records:[]
	    	};
	    	
	    	for(let i = 0; i < _data.length;i++){
	    		let _detailData = _data[i];
    			let _fieldObj = $.extend(true,{},_convertedObj);
    		
    			_fieldObj.name = _detailData["tableName"]+"_"+_detailData["colName"];
    			// 
    			_fieldObj.title = commonFunc.txtChangeAggreate(_detailData, dataViewConfig.txtRefect.detailObjAggreateInfoAry);
    			
    			// TODO 문자 숫자 + 날짜
    			if(_detailData["dataType"] === "문자"){
    				_fieldObj.type =  "text";	
    			}else if(_detailData["dataType"] === "숫자"){
    				_fieldObj.type =  "number";
    			}
    			
    			rtnObj.fields.push(_fieldObj);
	    		
	    	}// for
	    	
	    	dataViewConfig.grid.fieldset = rtnObj;
	    	this.drawGrid("only",rtnObj, loadingBarObj);
	    }, // fn_draw_only_column_jsGrid
	    
	    // grid 그리기
	    drawGrid : function(_flag, _data, loadingBarObj){
	    	// 설정 값에 따라 WIDTH값 조정
	    	let setFieldWidth = function(fieldName){
	    		let widthSet = {
	    				"SEAL_TYPE" : 120,
	    				"EQUIP_MODEL" : 160,
	    				"EQUIP_MFG" : 160,
	    				"SEAL_CONFIG" : 240,
	    				"PRODUCT" : 240,
	    				"SEAL_CODE":140,
	    				"ITEN_NO":140
	    		};
	    		
	    		if(widthSet.hasOwnProperty(fieldName)){
	    			return widthSet[fieldName];
	    		}else{
	    			return 100;
	    		}
	    		
	    		
	    	};
	    
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
//	    				pageSize: 15,
	    		        pageButtonCount: 5,
	    		        pagerContainer: "#"+dataViewConfig.elemIdSet.pagerId,
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
	    				fields[i]["headercss"] =  "font-size-down";
	    				fields[i]["css"] =  "custom-main-jsgrid-col-css p-1";
	    				fields[i]["align"] =  "center";
//	    				fields[i]["width"] =  "auto";
	    				
	    				let fieldName = fields[i]["title"];
	    				fields[i]["width"] =  setFieldWidth(fieldName);
	    				
	    			}
	    			options.data = _data.records;
	    			options.fields=  fields;
	    		$("#"+dataViewConfig.elemIdSet.gridElemId).jsGrid('destroy');
	    		$("#"+dataViewConfig.elemIdSet.gridElemId).jsGrid(options);	
	    		if(loadingBarObj !== undefined && loadingBarObj !== null){
	    			loadingBarObj.hide();	
	    			olapDataView.loadingBar =null;
	    		}
	    	}else{
	    		$("#"+dataViewConfig.elemIdSet.gridElemId).jsGrid('destroy');//reset
	    	}
	    	
	    },
	 
	    /**
	     * saveDashboard.js에서 호출하는 메서드
	     * 유저가 저장한 데이터를 그리기
	     */
	    receiveData : function(_data){
	    	let _that = this;

	    	
	    	olapDataView.onlyColumnGridSetting(olapDataView.dataset.objectDetailInfo);
//	    	console.log("receiveData",_data);//저장된 값
	    	if(_data.hasOwnProperty("qryStr")){
	    		let qryStr = JSON.parse(_data.qryStr);
	    		for(let key in qryStr){
	    			olapDataView.dataset[key] = qryStr[key];	
	    		}
	    		
//	    		olapDataView.dataset.userSelectedOrby

	    		if(olapDataView.dataset.userSelectedCond.length > 0){
		    		$("#"+dataViewConfig.elemIdSet.condAreaId).empty();
	    			for(let idx=0;idx< olapDataView.dataset.userSelectedCond.length;idx++){
		    			olapDataView.condAddAction(olapDataView.dataset.userSelectedCond[idx]);	
		    		}
	    		}
  		
	    		
	    		
	    		
	    		if(olapDataView.dataset.userSelectedOrby.length > 0){
		    		$("#"+dataViewConfig.elemIdSet.orbyAreaId).empty();
		    		for(let o_idx =0;o_idx < olapDataView.dataset.userSelectedOrby.length;o_idx++){
		    			olapDataView.orderbyAction(olapDataView.dataset.userSelectedOrby[o_idx]);
		    		}
	    			
	    		}
	    		
	    		olapDataView.submitAction(true);
	    		
	    	}else{
	    		console.log("Not Exist Data");
	    		alert("저장된 정보가 없거나 데이터 파싱 중 오류가 발생했습니다.")
	    	}
	    	
	    	
	    }
			
};