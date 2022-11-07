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
// Config
//1227
commonFunc.ajaxFailAction = function(_failObj){
			if(olapDataView.loadingBar !== null){
				olapDataView.loadingBar.hide();
				olapDataView.loadingBar = null;
			}
			
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
		};

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
			clickedObjectDetailInfo:[],
			objectDetailList:[],
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
						tbName : _that.tbName //table name
					}
				}).done(function(_result){//TODO 
					//console.log(_result);
					_that.dataset.objectDetailInfo = _result; //
					_that.fn_draw_object_detail("objectDetailInfo", _result); 
					//_that.onlyColumnGridSetting(_result); // 단순히 grid에 컬럼 fields 그리기.
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
		// 조회조건 dialog 그리기
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
	    		// 조회조건 및 정렬조건 area 초기화
	    		olapDataView.initDrawClear();
	    		
	    		//objectDetailinfo
	    		olapDataView.dataset.userSelectedCond = [];
	    		olapDataView.dataset.userSelectedOrby = [];
	    		
	    		olapDataView.dataset.clickedObjectDetailInfo = []; //초기화시 빈값 선언.
	    		olapDataView.dataset.objectDetailList = [];
	    		$("#saveTitle").val("");
	    		$("#saveDescription").val("");
	    		_that.fn_draw_selected_data_Grid('reset');  //조회결과 jsgrid 초기화
	    		//$("#"+dataViewConfig.elemIdSet.gridElemId).jsGrid('destroy');
	    		_that.fn_reset();// checkbox list 초기화 및 jsrgid 그리기
				
	    	});
	    	
	    	// 조회 버튼 이벤트
	    	$("#"+dataViewConfig.elemIdSet.submitBtn).on("click",function(e){
	    		olapDataView.submitAction();
	    	});
	    	
	    },
	  
	    fn_reset : function(){ //초기화 버튼 클릭시 실행되는 함수 
	    	$("#objectDetailinfo").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true);//전체선택 초기화 checked 상태로
    		$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true); //컬럼 리스트 초기화 checked 상태로
    		let clickAry = [];
			$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').each(function(index, item){
				clickAry.push($(item).val()); //checkbox checked  value값 받아와서 배열에 넣기.
			});
			
			olapDataView.dataset.clickedObjectDetailInfo = clickAry;
    		olapDataView.fn_draw_only_column_jsGrid(clickAry);
			
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
	    		let $condSetDiv = $("<div>"); // 조회조건추가시 생성해주는 코드.
	    		
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
	    				$(this).off("click");
	    				$(this).parent().remove();
	    				if($(".fn-delConditionIcon").length === 0){
	    					olapDataView.fn_init_append_notice_div(dataViewConfig.elemIdSet.condAreaId, dataViewConfig.txtRefect.conditionNoticeText);
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
	    				var operData = olapDataView.dataset.objectDetailInfo[idx]["operOption"];
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
	    		//console.log(tbName);
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
	    
	    chkOrbyValid : function(){
	    	let userSelectedLen = $("#orderbyArea").find(".data-orderby-name").length;
	    	let usrSelAry = [];
	    	let isPlay = true
	    	if(userSelectedLen >0){
	    		for(let k=0;k<userSelectedLen;k++){
	    			let selVal = $($("#orderbyArea").find(".data-orderby-name")[k]).val();
	    			if(usrSelAry.indexOf(selVal) > -1){
	    				isPlay =false;
	    				break;
	    			}
	    			usrSelAry.push(selVal);
	    		}
	    	}
	    	
	    	if(!isPlay){
	    		alert("동일한 항목을 중복해서 정렬할 수 없습니다. 조회 규칙에 위배됩니다.");
	    	} 
	    	
	    	return isPlay;
	    },
	    
	    // 조회하기
	    submitAction : function(isExtSavedData){
	    	//console.log(olapDataView.dataset.objectDetailInfo);
	    	let isOrdChk = olapDataView.chkOrbyValid();
	    	if(!isOrdChk){
	    		return;
	    	}
	    	// 조건 정렬 세팅
	    	let _that = this;
	    	if(!commonFunc.chkValue(isExtSavedData) || !isExtSavedData){
	    		_that.condNOrderbyCreateDataset();	
	    	}
	    	

	    	// ajax 호출
			let dataParams = {
					"tbName01" : _that.tbName,
					//"detailInfo" : _that.dataset.objectDetailInfo, // => 전체를  paramter로 사용.
					"detailInfo" : _that.dataset.objectDetailList, //사용자가 선택한 체크박스 값만 가져온뒤 조회 후 중복값만 출력하기.
					"condition" : _that.dataset.userSelectedCond,
					"orderby": _that.dataset.userSelectedOrby
			}
			console.log(dataParams);
			_that.loadingBar= new loading_bar2(); 
	    	// 그리드 그리기
			$.ajax({ 
				type:"POST",
				url:OlapUrlConfig.selectGridData, 
				 beforeSend: function(xhr) {xhr.setRequestHeader("AJAX", true); _that.loadingBar.show();},
					contentType: "application/json;charset=UTF-8",
			     data : JSON.stringify(dataParams),
			}).done(function(data){
				// console.log("결과:data값",data); //	
				if(data.hasOwnProperty("fields") && data.hasOwnProperty("records")){
				
					// TODO 엑셀 데이터 셋도 만들어야 ExcelDownloadObject.datasetInit
					
					_that.dataset.resultGridDataset.fields = data.fields; //전체 데이터 보관용
					_that.dataset.resultGridDataset.records = data.records; //전체 데이터보관용
					_that.fn_draw_selected_data_Grid("draw",data, _that.loadingBar);
						
						
					 if(commonFunc.chkValue(ExcelDownloadObject) && ExcelDownloadObject.hasOwnProperty("datasetInit")){
						 ExcelDownloadObject.datasetInit(data); 
					 }
				}else{
					//_that.onlyColumnGridSetting(_that.dataset.objectDetailInfo);
					_that.fn_draw_object_detail(_that.dataset.objectDetailInfo);
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
    			//console.log(rtnObj);
	    		
	    	}// for
	    	
	    	//dataViewConfig.grid.fieldset = rtnObj;
	    	//this.drawGrid("only",rtnObj, loadingBarObj);
	    	this.fn_draw_selected_data_Grid("only",rtnObj, loadingBarObj);
	    }, 
	    
	    /**
	     * saveDashboard.js에서 호출하는 메서드
	     * 유저가 저장한 데이터를 그리기
	     */
	    receiveData : function(_data){
	    	let _that = this;
	    	
	    	if(_data.hasOwnProperty("qryStr")){ 
	    		let qryStr = JSON.parse(_data.qryStr); //_data의 qryStr를 javascript 객체로
	    		for(let key in qryStr){ //* qryStr을 가지고 checked 했던 value값을 찾기위한 코드.
	    			if(key === "objectDetailInfo"){
	    				olapDataView.dataset.objectDetailList = qryStr[key]; //유저가 선택했던 항목속성값을 넣어준다.
	    			}else{
	    				olapDataView.dataset[key] = qryStr[key];	
	    			}
	    				
	    		}
	    		   		
	    		
	    		// 유저가 선택한 checkbox 값만 출력
	    		let userSelectedObj = qryStr.objectDetailInfo;  //유저가 저장했던 항목값
	    		let originObjDetail = olapDataView.dataset.objectDetailInfo; //기존 항목리스트
	    		let userSelectedIds = []; //저장시 checked 했던 checkbox value.
	    		for(let idx in userSelectedObj){
	    			let tbName =  userSelectedObj[idx]["tableName"], colName =userSelectedObj[idx]["colName"];
	    			for(let j=0;j<originObjDetail.length;j++){
	    				let originTbName = originObjDetail[j]["tableName"], originColName = originObjDetail[j]["colName"];
	    				if(originTbName === tbName && originColName === colName){
	    					let userSelectedId = "objectDetailInfo_"+j; //id,value 값을 뽑아냄.
	    					userSelectedIds.push(userSelectedId);
	    					if(idx === (userSelectedObj.length-1)){ //값이 작거나 같기 때문에 break를 걸어줘서 작을때는 의미없는 for문을 돌지않게해준다.
	    						break;
	    					}
	    				}
	    			}
	    		}
	    		//console.log("userSelectedIds",userSelectedIds);
	    		olapDataView.dataset.clickedObjectDetailInfo = userSelectedIds; //유저가 저장했던 항목값을 clickedObjectDetailInfo에 넣어주어 조회 파라미터로 사용.
	    		
    			//전체선택되어있든 일부만 되어있든 상관없이 체크박스 전부~ 해제!
	    		$("#objectDetailinfo").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);//전체선택 초기화 checkbox 해제
	    		$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false); //컬럼 리스트 초기화
	    		    		
	    		// userSelectedIds 값(저장목록에서 불러온 데이터 값)을 보고 해당하는 ID를 가진 element에 체크박스 true	    		
	    		for(let i in userSelectedIds){
	    			chkId = userSelectedIds[i];
	    			console.log(chkId);
	    			$("#"+chkId).prop("checked", true);
	    		}
	    		
	    		if(olapDataView.dataset.userSelectedCond.length > 0){ //조회조건 셋팅
		    		$("#"+dataViewConfig.elemIdSet.condAreaId).empty();
	    			for(let idx=0;idx< olapDataView.dataset.userSelectedCond.length;idx++){
		    			olapDataView.condAddAction(olapDataView.dataset.userSelectedCond[idx]);	
		    		}
	    		}
	    		if(olapDataView.dataset.userSelectedOrby.length > 0){ //정렬조건 셋팅
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
	    	
	    },
	    
	    fn_draw_object_detail : function(_objDetailDomId, _objectDetailInfoAry){ // jsgrid 컬럼 리스트 그리기.
	    	//console.log(_objectDetailInfoAry);
	    	let _that = this;
	    	var _objValue_01 = ""+_objDetailDomId;
	    	//_objDetailDomId = "#" + _objDetailDomId;
	    	_objDetailDomId = "#objectDetailinfo" ; //M_DATA_CNV1 컬럼리스트출력
	    	var _createElem01 = '<div  data-toggle="tooltip" data-placement="bottom" title="',
	    	_createElem02 = '" class="form-check custom-detail-div custom-detail-div-font-size" ><input type="checkbox" class="d-inline-block chbox" name="check" value="',
	    	_createElem03 = '" id="',
	    	_createElem04 = '"><label for="',
	    	_createElem05 =  '"><span class="custom-checkbox"></span></label>',
	    	_createElem06 =  '<div class="d-inline-block text-left evt-select-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width text-truncate" >',
	    	_createElem06_ifFuncExtElem =  '<div class="d-inline-block text-left evt-select-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width-funcext text-truncate" >',
	    	_createElem07_ifFuncExtElem = '</div>',
	    	_createElem07 = '</div></div>'
	    	
	    	
	    	//2019-06-26 추가
	    	
	    	var selectAll ='<div class="form-check custom-detail-div custom-detail-div-font-size" >'+
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
	    			//console.log("_chkName",_chkName);
	    			// 06.28  집계 함수 분기
	    			if(_objectDetailInfoAry[i]["calcFuntionYn"] === "Y"){ //column list 그리는 부분
	    				$(_objDetailDomId).append(_createElem01+_chkDescript+_createElem02+_chkValue+_createElem03+_chkValue+_createElem04+_chkValue+_createElem05+_createElem06_ifFuncExtElem+_chkName+_createElem07_ifFuncExtElem+_funcSelectBox+_createElem07_ifFuncExtElem);
	    				var $selectElem =$("#"+_chkValue).siblings(".custom-detail-chkbox-selctboxdiv-width-funcext").find("select");
	    				for(_opidx in dashboard_config.detailObjAggreateInfoAry){
	    					$selectElem.append(new Option(dashboard_config.detailObjAggreateInfoAry[_opidx]["drawNm"], _opidx));
	    				}	    				
	    			}else{
	    				$(_objDetailDomId).append(_createElem01+_chkDescript+_createElem02+_chkValue+_createElem03+_chkValue+_createElem04+_chkValue+_createElem05+_createElem06+_chkName+_createElem07);	
	    			}
	    			
	    		}

	    		// 03.28 css 변경
	    		$(_objDetailDomId).find(".evt-select-detail-chkbox").on("click",function(e,allObj){ //check box 텍스트 선택
	    			var objState = $(this).siblings('input:checkbox[name="check"]').prop('checked');
	    			var allState=null; //check 시 true 
    			
	    			if(allObj !== undefined && allObj.hasOwnProperty("allSelectSatate")){
	    				allState = allObj.allSelectSatate;   // allState : true
	    			}
	    			if(allObj === undefined || allState !== objState){ //전체선택시 allstate:true , objstate : false => 실행.
	    				$(this).siblings('input:checkbox[name="check"]').prop('checked',function(){
	    			        return !$(this).prop('checked');
	    				});
	    				var chkVal = $(this).siblings('input:checkbox[name="check"]').val();
	    				//console.log(chkVal); //전체 선택  value objectDetailInfo_0 ~68
	    				_that.fn_evt_detailChk_bind(e, chkVal); //all check value
	    			 
	    				if(allObj === undefined){
	    					var _chkAllState = $(_objDetailDomId).find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked');
	    					
	    					if(_chkAllState){
	    						$(_objDetailDomId).find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);
	    					}
	    					
	    				}

	    			}
	    			
	    		});
	    		 
	    		// 03.18 이벤트 바인드 로직 및 데이터 세팅 추가
	    		$(_objDetailDomId).find(".evt-select-detail-chkbox").siblings('input:checkbox[name="check"]').on('change',function(e){ //check box 클릭시
	    			var chkVal = $(this).val(); //컬럼 순서값
	    			
	    		    _that.fn_evt_detailChk_bind(e,chkVal); //check box 선택시 발생 함수.
	    		    var _chkAllState =$(_objDetailDomId+"_checkAll").prop('checked');
					if(_chkAllState){
						$(_objDetailDomId+"_checkAll").prop('checked', false); //전체선택이 체크되었으면 해제
					}
	    		});
	    		
	    		$(_objDetailDomId).find('.form-check').hover(function() { //마우스오버시 효과
	    			  $(this).tooltip({ boundary: 'window' });
	    			  $(this).tooltip('show');

	    		}, function(){
	    		  $(this).tooltip('hide');
	    		  // 만약 툴팁이 존재할 경우 모든 툴팁 닫기
	    		  if($(".tooltip-inner").length > 0){
	    			  $(".tooltip").tooltip("hide");
	    		  }
	    		});
	    		
	    		
	    		$(_objDetailDomId).find('.evt-select-all-detail-chkbox').on('click',function(e){ //전체선택 텍스트 부분	    			
	    			$(this).siblings('input:checkbox[name="check"]').prop('checked',function(){  //텍스트 클릭시 checkbox 해제 
	    		        return !$(this).prop('checked');
	    		  });
	    			var _checkedState = $(this).siblings('input:checkbox[name="check"]').prop('checked')
	    			_that.fn_all_check_bind(_checkedState);
	    	    });
	    		
	    		$(_objDetailDomId+"_checkAll").on("change",function(e){ //전체선택 checkbox 부분.
	    			var _checkedState =$(_objDetailDomId+"_checkAll").prop('checked'); 
	    			_that.fn_all_check_bind(_checkedState);
	    		});
	    
	    		
	    	}else if(_objectDetailInfoAry instanceof Array && _objectDetailInfoAry.length === 0){
	    		 console.log("_objectDetailInfoAry length >> 0 : fn_draw_object_detail");
	    	}else{
	    		 console.log("파라미터 없음 : fn_draw_object_detail");
	    	}
	    	
	    	//check box checked된 상태로 만들기 
	    	$("#objectDetailinfo").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true);//셋팅시 checked 
			$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true); //셋팅시 checked
			let clickAry = [];
			$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').each(function(index, item){
				clickAry.push($(item).val()); //checkbox checked  value값 받아와서 배열에 넣기.
			});
			//console.log(clickAry);
			olapDataView.dataset.clickedObjectDetailInfo = clickAry;
			olapDataView.fn_draw_only_column_jsGrid(clickAry);
		/*	for(let i=0; i<clickAry.length; i++){
				let elemIdx = clickAry[i];
				olapDataView.fn_evt_detailChk_bind(null, elemIdx); //checkbox click function 으로 이동 =>최종적으로  jsgrid field 에 하나씩 그려준다.
			}	*/
			
	    },
	    fn_all_check_bind : function(_flag){ //전체선택 체크함수
	    	let clickAry = [];
			$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').each(function(index, item){
				clickAry.push($(item).val()); //checkbox checked  value값 받아와서 배열에 넣기.
			});
	    	if(_flag == true){
	    		$("#objectDetailinfo").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true);//셋팅시 checked 
				$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true); //셋팅시 checked
				olapDataView.dataset.clickedObjectDetailInfo = clickAry; //clickedObjectDetailInfo 전체값
	    		olapDataView.fn_draw_only_column_jsGrid(clickAry);  //jsgrid 그리기
	    	}else{ //(_checkedState ==false)
	    		$("#objectDetailinfo").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);//셋팅시 check 해제
				$("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false); //셋팅시 checked	해제
				olapDataView.fn_draw_selected_data_Grid('reset');  //조회결과 jsgrid 초기화
				olapDataView.dataset.clickedObjectDetailInfo = []; //clickedObjectDetailInfo 초기화
			
	    	}
	    },
	    
	    fn_evt_detailChk_bind : function(e, _value){ //check box 선택시 발생 함수.
    		var _setDetailVal = _value !== undefined && _value !== null ? _value : $(this).val(); //_value 유효성 체크 	    	
    		if(_setDetailVal !== undefined && _setDetailVal !== null){ 
	    		var _aryIndex = olapDataView.dataset.clickedObjectDetailInfo.indexOf(_setDetailVal); //indexof : 문자열내의 특정한 문자열의 index값을 리턴 . 존재하지 않을시 -1 리턴.
	    		if(_aryIndex > -1){
	    			olapDataView.dataset.clickedObjectDetailInfo.splice(_aryIndex,1); //값이 있을경우 속성을 뽑아낸다 splice.
	    		}else{
	    			olapDataView.dataset.clickedObjectDetailInfo.push(_setDetailVal);//값이없을경우 값을 넣어준다.
	    		}
	    		olapDataView.fn_draw_only_column_jsGrid(olapDataView.dataset.clickedObjectDetailInfo); //checkbox 클릭시 실행.=> 선택한 checkbox에 대한 속성값 추출 후 grid 그리기.	    		
	    	}
	    },
	    
	    fn_draw_only_column_jsGrid : function(_data, loadingBarObj){
	    	let _that = this;
	    	var _locate = [], _key,_index,detailDataset,
	    	_convertedObj = {
	    			name :"",
	    			title:"",
	    			type:"",
	    			headercss : "custom-main-jsgrid-header"
	    	}, rtnObj={
	    			fields : [],
	    			records:[]
	    	},selectDataset ={
	    			info:[]
	    	};
	    	
	    	for(var i = 0; i < _data.length;i++){
	    		_locate = _data[i].split("_");
	    		if(_locate.length === 2){
	    			_key=_locate[0];
	    			_index = _locate[1];
	    			detailDataset = olapDataView.dataset[_key][_index]; //해당 컬럼속성값을 _detailDataset 에 넣어줌. 함수내의 변수선선은 그함수가 끝날시 초기화된다.
	    			var _rtnObj = $.extend(true,{},_convertedObj);
	    			_rtnObj.name = detailDataset["tableName"]+"_"+detailDataset["colName"];
	    			_rtnObj.title = commonFunc.txtChangeAggreate(detailDataset, dataViewConfig.txtRefect.detailObjAggreateInfoAry);
	    		 			
	    			if(detailDataset["dataType"] === "문자"){
	    				_rtnObj.type =  "text";	
	    			}else if(detailDataset["dataType"] === "숫자"){
	    				_rtnObj.type =  "number";
	    			}
	    			rtnObj.fields.push(_rtnObj);
	    			selectDataset.info.push(detailDataset); //사용자가 선택한 체크박스 값
	    			detailDataset = null;
	    		}
	    	}
	    
	    	_that.fn_draw_selected_data_Grid("only",rtnObj, loadingBarObj);
	    	olapDataView.dataset.objectDetailList = selectDataset.info; //조회시 파라미터로 넘겨주기위해 olapDataView.dataset.objectDetailList 에 사용자가 선택한 체크박스 값들 넣어줌 (선택한 순서대로.)
	    },
	    fn_draw_selected_data_Grid : function(_flag,_data , loadingBarObj){
	    	//console.log("_data",_data);
	    	let _that = this;
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
	    				heading: true,
	    				height: "500px",
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
 			
	    			options.data = _data.records; //전체 데이터를 조회 후 선택한 부분만 보여주도록 중간작업이 필요.
	    			options.fields= _data.fields; // fields 를 넘기는데 그중에 _data.fields[i]title을 fields 로 할수있는지?

	    		$("#"+dataViewConfig.elemIdSet.gridElemId).jsGrid('destroy');
	    		$("#"+dataViewConfig.elemIdSet.gridElemId).jsGrid(options);	//objectViewGrid jsgrid 로 그려주기.
	    		if(loadingBarObj !== undefined && loadingBarObj !== null){
	    			loadingBarObj.hide();	
	    			olapDataView.loadingBar =null;
	    		}
	    	}else{
	    		$("#"+dataViewConfig.elemIdSet.gridElemId).jsGrid('destroy');//reset
	    	}
	    		
	    }
			
};
