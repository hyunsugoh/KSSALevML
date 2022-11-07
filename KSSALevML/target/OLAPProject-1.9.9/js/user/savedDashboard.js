'use strict';
/******************************************************** 
파일명 : savedDashboard.js 
설 명 : User page의 저장목록 관련 DashBoard JavaScript
수정일	수정자	Version	Function 명
-------	--------	----------	--------------
2019.03.28	최 진	1.0	최초 생성
 *********************************************************/

/**
 * 
 */
var showDetailsDialog = function(dialogType,_items){
	//console.log("_items",dialogType);
   //$("#savedData-sequence-"+item.seqNum).val(title);
   // $("#age").val(description);
    $("#detailsDialog").dialog(dialogType).dialog("open");
            

	
};


var SavedUserDataList = {
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
		Saved_Dashboard_Config : {
			gridElemId: "",
			seqNum: null,
			selectedPageNum:""
		},
		SavedDataSet: {
			fields : [
				{name:"titleText",title:"제목", type:"text", align: "center", width:"48%",headercss:"font-size-down",css:"font-size-down custom-saved-jsgrid-col-css",
					itemTemplate : function(_,item){
						var $rtnDiv = $("<div>");
						$rtnDiv.addClass('custom-main-jsgrid-col text-truncate');
						$rtnDiv.attr('id','savedData-sequence-'+item.seqNum);
						$rtnDiv.attr('data-toggle','tooltip');
						$rtnDiv.attr('data-placement','auto');
						$rtnDiv.attr('title',item.descriptionText);
						$rtnDiv.text(item.titleText)
						return $rtnDiv.hover(function() {
							
							$(this).tooltip({ boundary: 'window' });
							$(this).tooltip('show');
						}, function(){
							$(this).tooltip('hide');
							// 만약 툴팁이 존재할 경우 모든 툴팁 닫기
							if($(".tooltip-inner").length > 0){
								$(".tooltip").tooltip("hide");
							}
						});
					}
				},
				{name:"updateDt", title:"일자", align: "center", type:"text", width:"auto",headercss:"font-size-down",css:"custom-saved-jsgrid-col-css",
					
					itemTemplate: function(value, item){
						if(value == null){
							value = item["createDt"];
						}
						return value.substr(0,4) + "." +value.substr(4,2)+"."+value.substr(6,2);
					}
				},
				{title:"삭제",type:"text", width:"60",align: "center", headercss:"font-size-down",css:"custom-saved-jsgrid-col-css", sorting: false,
					itemTemplate: function(_, item) {
						var $rtnDiv = $("<div>");
						$rtnDiv.append("<button>");
						$rtnDiv.find("button").addClass("jsgrid-button jsgrid-delete-button");
						return $rtnDiv.find("button").on("click", function() {
							confirm({
								message:'<p class="text-center">"'+item.titleText+'" 정보를 삭제하시겠습니까?</p>',
								title:'<h6>삭제</h6>',
								buttons :{
									confirm: {
										label: '확인',
										className: 'btn btn-sm btn-primary'
									},
									cancel: {
										label: '취소',
										className: 'btn btn-sm  btn-secondary'
									}
								},
							},function(result){
								if(result){
									$('#savedData-sequence-'+SavedUserDataList.Saved_Dashboard_Config.seqNum).parent().parent().data("JSGridItem")
									SavedUserDataList.DeleteData(item);
								}
							});

							return false;
						});
					}	
				}
				],
				options : {
					width: "100%",
					paging: true,
					pageSize: 5,
					pageButtonCount: 3,
					pagerContainer: "#externalSavedPager",
					pagerFormat: '{first} {prev} {pages} {next} {last}',
					pagePrevText: "이전",
					pageNextText: "다음",
					pageFirstText: "처음",
					pageLastText: "마지막",
					pageNavigatorNextText: "&#8230;",
					pageNavigatorPrevText: "&#8230;",
					noDataContent: "저장한 이력이 없습니다.",
					pagerContainerClass: "custom-jsgrid-pager-container",
					pagerClass: "custom-jsgrid-pager",
					pagerNavButtonClass: "custom-jsgrid-pager-nav-button",
					pagerNavButtonInactiveClass: "custom-jsgrid-pager-nav-inactive-button",
					pageClass: "custom-jsgrid-pager-page",
					currentPageClass: "custom-jsgrid-pager-current-page",
					fields : [],
					data : [],
					selecting:true,
					sorting: true,
					autoload: true,
					rowClick: function(args) {
						var title = args.item.titleText;
						var description = args.item.descriptionText;
						//console.log(description);
						//console.log("args.item",args.item);
						var $row = this.rowByItem(args.item),
						selectedRow = $("#"+SavedUserDataList.Saved_Dashboard_Config.gridElemId).find('table tr.highlight');
					//	showDetailsDialog("SaveInfo", args.item);
						//showDetailsDialog("SaveInfo", title,description,seqNum);
						// 같은 정보를 클릭하면 return
						if(args.item.hasOwnProperty("seqNum") && SavedUserDataList.Saved_Dashboard_Config.seqNum === args.item.seqNum){return;}
						if (selectedRow.length) {
							selectedRow.toggleClass('highlight');
						};
						$row.toggleClass("highlight");
						
						confirm({
							message:'<p class="text-center">"'+title+'" 정보를 불러오시겠습니까?</p>',
							title:'<h6>조회하기</h6>',
							buttons :{
								confirm: {
									label: '확인',
									className: 'btn btn-sm btn-primary'
								},
								cancel: {
									label: '취소',
									className: 'btn btn-sm  btn-secondary'
								}
							},
						},function(result){
							if(result){
								SavedUserDataList.settingSavedDataDraw(args.item);
							}else{
								if($("#"+SavedUserDataList.Saved_Dashboard_Config.gridElemId).find('table tr.highlight').length > 0 ){
									$("#"+SavedUserDataList.Saved_Dashboard_Config.gridElemId).find('table tr.highlight').removeClass('highlight');

									if(SavedUserDataList.Saved_Dashboard_Config.selectedPageNum !== ""){
										$("#"+SavedUserDataList.Saved_Dashboard_Config.gridElemId).jsGrid('openPage',Number(SavedUserDataList.Saved_Dashboard_Config.selectedPageNum));
									}
									if(SavedUserDataList.Saved_Dashboard_Config.seqNum !== null && $('#savedData-sequence-'+SavedUserDataList.Saved_Dashboard_Config.seqNum).parent().parent().hasClass('highlight') === false){
										$('#savedData-sequence-'+SavedUserDataList.Saved_Dashboard_Config.seqNum).parent().parent().addClass('highlight');
									}
								}
							}
						});
					},
					onRefreshed: function(args) {
						if(SavedUserDataList.Saved_Dashboard_Config.seqNum !== null &&
								$('#savedData-sequence-'+SavedUserDataList.Saved_Dashboard_Config.seqNum).length > 0 && 
								$('#savedData-sequence-'+SavedUserDataList.Saved_Dashboard_Config.seqNum).parent().parent().hasClass('highlight') === false){
							$('#savedData-sequence-'+SavedUserDataList.Saved_Dashboard_Config.seqNum).parent().parent().addClass('highlight');
						}
					}

				},

			userSavedData :[]
		},
		saveAction : function(saveInfo,dataset){
			let isOrdChk = olapDataView.chkOrbyValid();
	    	if(!isOrdChk){
	    		return;
	    	}
			let _that = this;

	    	let userSelectedDataset = {
	    		objectDetailInfo : [],
	    		userSelectedCond : [],
	    		userSelectedOrby : [],
	    	};
	
			// 데이터 세트 만들기
			olapDataView.condNOrderbyCreateDataset();    	
	    	 
	    	//userSelectedDataset.objectDetailInfo = olapDataView.dataset.objectDetailInfo; //전체조건 -> 추후에 불러오기할때 for문을 사용해서 필요한 데이터를 뽑아낼때 사용.
			userSelectedDataset.objectDetailInfo = olapDataView.dataset.objectDetailList; //저장시점시 사용자가 선택한 checkbox 리스트
	    	userSelectedDataset.userSelectedCond = olapDataView.dataset.userSelectedCond; //선택조건
	    	userSelectedDataset.userSelectedOrby = olapDataView.dataset.userSelectedOrby; //선택정렬
	    	
	    	//console.log("userSelectedDataset",userSelectedDataset);
    	
	    	if(saveInfo.title === ""){
				alert("저장할 제목을 입력하여 주십시오.");
				return;
			}
			if(saveInfo.description === ""){
				alert("상세설명을 입력하여 주십시오.");
				return;
			}

			if(JSON.stringify(userSelectedDataset.userSelectedCond).length >= 20000){
				alert("너무 많은 조건을 설정하였습니다.");
				return;
			}
			if(saveInfo.title.length > 20){
				alert("제목은 20글자를 초과하여 입력할 수 없습니다.");
				return;
			}
			if(saveInfo.description.length > 100){
				alert("설명은 100글자를 초과하여 입력할 수 없습니다.");
				return;
			}
			

			var _setData = {
					data:JSON.stringify(userSelectedDataset),
					title:saveInfo.title,
					description:saveInfo.description
			};
			if(saveInfo.hasOwnProperty("seqNum")){
				_setData["seqNum"] = saveInfo.seqNum;
			}
			olapDataView.loadingBar = new loading_bar2();
			olapDataView.loadingBar.show();
			
			$.ajax({
				type : "POST",
				url:OlapUrlConfig.saveUserDataset, //UserDataAPIController
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				data : JSON.stringify(_setData),
				contentType: "application/json;charset=UTF-8",
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				complete:function(){
					olapDataView.loadingBar.hide();	
				},
				success : function(data, status, xhr){
					if(status === "success"){
						SavedUserDataList.loadAction();
					}else{
						// 실패
						alert("데이터 통신이 원활하지 않습니다.");
					}
				},
				error : _that.ajaxFailAction
			});


		},

		loadAction : function(elemId){
			let _that = this;
			if(elemId !== undefined && elemId !== null){
				if(SavedUserDataList.Saved_Dashboard_Config.gridElemId === undefined || 
						SavedUserDataList.Saved_Dashboard_Config.gridElemId === null ||
						SavedUserDataList.Saved_Dashboard_Config.gridElemId === ""){
					SavedUserDataList.Saved_Dashboard_Config.gridElemId = elemId;
				}	
			}

			$.ajax({
				type : "GET",
				url:OlapUrlConfig.selectSavedDataSetList,
				contentType: "application/json;charset=UTF-8",
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				success : function(data, status, xhr){
					if(status === "success"){
//						// console.log("success");	
						_that.SavedDataSet.userSavedData = data;
						SavedUserDataList.refect_data(data, SavedUserDataList.fn_saved_draw_grid);
					}else{
						// 실패
//						console.error("통신fail");
						alert("데이터 통신이 원활하지 않습니다.");
					}
				},
				error :_that.ajaxFailAction
			});
		},
		refect_data : function(data, fn_callback){
			SavedUserDataList.SavedDataSet.userSavedData = data;
			if(data.length > 0){
				fn_callback(data);
			}else{
				fn_callback([]);
			}
		},
		fn_saved_draw_grid: function(records){
			var options= SavedUserDataList.SavedDataSet.options;
			options.fields = SavedUserDataList.SavedDataSet.fields;
			options.data = records;
			$("#"+SavedUserDataList.Saved_Dashboard_Config.gridElemId).jsGrid(options);
		},

		settingSavedDataDraw:function(data){
			olapDataView.receiveData(data); // data 넘겨주기  receiveData실행하면서 olapDataView.submitAction 실행..
			// 문서제목, 상세설명
			SavedUserDataList.SettingTitleNDescript(data); //저장객체의 제목과 상세설명 표시.
			// table1 호출
		},

		playSetLoadData : function(data, savedData){
//			// console.log(data);
			SavedUserDataList.draw_set_objectDetailInfo(savedData);	
			//날짜
			var dateSetState = SavedUserDataList.SettingDate(savedData);

			// 유저가 선택한 저장정보를 임시저장
			if(data.hasOwnProperty("seqNum") && data.seqNum !== undefined && data.seqNum !== null){
				SavedUserDataList.Saved_Dashboard_Config.seqNum = data.seqNum;	
				if($("#externalSavedPager").find(".custom-jsgrid-pager-current-page").text() !==""){
					SavedUserDataList.Saved_Dashboard_Config.selectedPageNum = $("#externalSavedPager").find(".custom-jsgrid-pager-current-page").text();
				}

			}
			//조건
			SavedUserDataList.SettingCondition(savedData);
			//정렬
			SavedUserDataList.SettingOrderBy(savedData);
			// 문서제목, 상세설명
			SavedUserDataList.SettingTitleNDescript(data);
			
			// data
			if(dateSetState){
				setTimeout(function(){
					$("#objectSelectActionBtn").trigger("click");	
				}, 400);										
			}
		},

		draw_set_objectDetailInfo : function(_savedData){
			// 객체세부정보 세팅
			var detailInfo = SavedUserDataList.refect_objectDetailInfodata(_savedData);
			var detailInfoIds = [dashboard_config.objectDetailId1];
			if(_savedData.hasOwnProperty("tbName02") && _savedData.tbName02 !== ""){
				detailInfoIds.push(dashboard_config.objectDetailId2)
			}

			for(var j in detailInfoIds){
				$("#"+detailInfoIds[j]).find('input:checkbox[name="check"]').each(function(idx, item){
					var val = $(this).val();
					if(detailInfo.indexOf(val) > -1){
						$(this).prop('checked',true);
					}
				});
			}
//			// console.log(detailInfo);
			for(var l in detailInfo){
				fn_evt_detailChk_bind(null, detailInfo[l]);	
			}
		},

		refect_objectDetailInfodata: function(_data){
			var _detailInfo = _data.detailInfo,
			_setTextVal="", detailKey="",
			rtnAry=[];
			for(var i in _detailInfo){
				if(_detailInfo[i]["tableName"] === _data.tbName01){
					_setTextVal = dashboard_config.objectDetailId1;
					detailKey = dashboard_config.objectDetailId1;
				}else if(_data.tbName02 !== "" && _detailInfo[i]["tableName"] === _data.tbName02){
					_setTextVal = dashboard_config.objectDetailId2;
					detailKey = dashboard_config.objectDetailId2;
				}else{
					console.error("객체 세부정보와 테이블명이 일치하지 않습니다. 데이터를 확인하십시오.");
					break;
				}

				var k;
				var _searchDataset = dataset[detailKey];

				if(_searchDataset.length === 0){
					break;
				}

				indexSearch:
					for(k in _searchDataset){
						if(_searchDataset[k]["colName"] == _detailInfo[i]["colName"]){
							break indexSearch;
						}
					}

				_setTextVal += "_" + k;
				rtnAry.push(_setTextVal);
			}//for
			return rtnAry;
		},
		SettingDate : function(_savedData){
			var startDtStr =_savedData.startDateStr,
			endDtStr = _savedData.endDateStr,
			dateLimits = new Date(new Date().setFullYear(new Date().getFullYear() - 1)),
			tomarrowDateObj = new Date();


			dateLimits.setDate(new Date().getDate()-1);
			tomarrowDateObj.setDate(new Date().getDate()+1);


			// 날짜는 1년이 넘어갈 경우 경고창을 띄워야 한다.
			if(startDtStr.length === 8){
				var _startYear =Number(startDtStr.substr(0,4)),
				_startMonth = Number(startDtStr.substr(4,2))-1,
				_startDate = Number(startDtStr.substr(6,2));
				var _startDateObj = new Date(_startYear, _startMonth, _startDate);
				// TODO 1년 제한 해제 향후 정해지면 변경
//				if(_startDateObj < dateLimits){
//					// console.log(_startDate)
//					alert("기간 조회는 1년까지만 가능합니다. ("+startDtStr.substr(0,4)+"년"+startDtStr.substr(4,2)+"월"+startDtStr.substr(6,2)+"일)");
//					return false;
//				}
				$("#datepicker_start").datepicker('setDate',_startDateObj);
			}

			if(endDtStr.length === 8){
				var _endYear = Number(endDtStr.substr(0,4)),
				_endMonth = Number(endDtStr.substr(4,2))-1,
				_endDate = Number(endDtStr.substr(6,2));
				var _endDateObj = new Date(_endYear, _endMonth, _endDate);
				// TODO 1년 제한 해제 향후 정해지면 변경
//				if(_endDateObj > tomarrowDateObj){
//					alert("기간 조회는 1년까지만 가능합니다. ("+endDtStr.substr(0,4)+"년"+Number(endDtStr.substr(4,2))+"월"+endDtStr.substr(6,2)+"일)");
//					return false;
//				}
				$("#datepicker_end").datepicker('setDate',_endDateObj);
			}
			return true;
		},

		SettingCondition : function(_setData){
			if(_setData.hasOwnProperty("condition") && _setData.condition.length > 0){
				var _condDataset = _setData.condition;
				// console.log(_setData);
				for(var a=0;a<_condDataset.length;a++){
					var condData = _condDataset[a];
					fn_action_condition_data(function(elemId, _condData){
						var tbName = _condData["tableName"];
						var colName = _condData["columnName"];
						var txtVal = _condData["value"];
						var _DatasetCond = dataset.conditionDataset;
						var k, j;
						var condIdx = null, operIdx = null, operAry = [];
						for(k in _DatasetCond){
							if(tbName === _DatasetCond[k]["tableName"] && 
									colName === _DatasetCond[k]["columnName"]){
								condIdx = k;
								operAry = _DatasetCond[k]["operOption"];
								for(j in operAry){
									if(operAry[j]["qryConCode"] === _condData["operQryConCode"]){
										operIdx = j;
										$("#"+elemId).find(".data-condition-name-select").val(condIdx).trigger('change');
										$("#"+elemId).find(".data-condition-oper-select").val(operIdx).trigger('change');	
										$("#"+elemId).find(".data-condition-value-select").val(txtVal);	
										break;	
									}
								}
							}
						}
					}, condData);
				}

			}
		},
		SettingOrderBy: function(_setData){
			if(_setData.hasOwnProperty("orderby") && _setData.orderby.length > 0){
				var orderByDataAry = _setData.orderby, _idx;
				for(_idx in orderByDataAry){

					fn_action_orderby_data(function(elemId){
						var _orderByData = orderByDataAry[_idx];
						var _GloBalOrderByList = dataset.orderbyDataset;
						var _orderByValAry = dashboard_config.orderByValInfoAry;
						var _gIdx, gOrderData;
						orderBySearch:
							for(_gIdx in _GloBalOrderByList){
								gOrderData = _GloBalOrderByList[_gIdx];
								if(_orderByData["tableName"] === gOrderData["tableName"] && 
										_orderByData["columnName"] === gOrderData["colName"] ){

									$("#"+elemId).find(".data-orderby-name").val(_gIdx).trigger("change");
									orderValSearch:
										for(var d in _orderByValAry){
											if(_orderByData["value"] ===_orderByValAry[d]["value"]){
												$("#"+elemId).find(".data-orderby-value").val(d).trigger("change");
												break orderValSearch;
											}
										}
									break orderBySearch;
								}

							}
					});	
				}
			}
		},
		SettingTitleNDescript : function(_data){
			if(_data.hasOwnProperty("titleText") && _data.hasOwnProperty("descriptionText")){
				$("#saveTitle").val(_data.titleText);	
				$("#saveDescription").val(_data.descriptionText);	
			}
		},
		DeleteData : function(item){
			let _that = this;
			olapDataView.loadingBar = new loading_bar2();
			olapDataView.loadingBar.show();
			
			$.ajax({
				type : "POST",
				url:OlapUrlConfig.deleteUserDataset,
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				data : JSON.stringify(item.seqNum),
				contentType: "application/json;charset=UTF-8",
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				complete:function(){
					olapDataView.loadingBar.hide(); // 로딩바 종료
				},
				success : function(data, status, xhr){
					if(status === "success"){
//						// console.log("DeleteData success");	
						alert("삭제하였습니다.");
						$("#conditionAllReset").trigger('click');
						SavedUserDataList.loadAction();
					}else{
						// 실패
//						console.error("통신fail");
						alert("데이터 통신이 원활하지 않습니다.");
					}
				},
				error : _that.ajaxFailAction
			});
		}
};


