
/******************************************************** 
파일명 : dataLoadTable.js 
설 명 : project 모델 테스트 페이지 관련 js (OLAP 데이터 조회 js참고)
수정일	수정자	Version	Function 명

필수 조건


-------	--------	----------	--------------
2020.							최초작성
 *********************************************************/
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

/*let dataViewConfig = {
		elemIdSet :{
			gridElemId :"objectViewGrid",
			pagerId:"externalPager",
		},

		grid : {
			fieldset : []
		},
};*/

let olapDataView = {
		objectId : null,
		userId : null,
		subPjtId : null,
		tbName : null,		
		dataset : {
			objSelect1 : "",
			selectedObjInfo1 : {},
			object : [],
			objectDetailInfo:[],
			clickedObjectDetailInfo:[],
			objectDetailList:[],
			detailOptionInfo:[],
			
			resultGridDataset:{
				fields:[],
				records:[]
			},
		
		},
		csvLoadData : {},
		loadingBar : null,
		condDialogObj : null,
		condGridObj : null,
		modelobj : null, //0714 decisiontree 컴포넌트 data 저장용
		// 기본 정보 가져오기
		init : function(gridElemId, pagerId, setSubPjtId,userid,objectid){
			subPjtId = setSubPjtId; //subPjtID 전역변수화.
			userId = userid;
			objectId = objectid;
			console.log("objectId",objectId)
			let _that = this;
			
			if(commonFunc.chkValue(gridElemId)){
				dataViewConfig.elemIdSet.gridElemId = gridElemId;
			}
			
			if(commonFunc.chkValue(pagerId)){
				dataViewConfig.elemIdSet.pagerId = pagerId;
			}
			
			/************************************************************************/
			/*
			함수명 : fn_action_object_list
			설	명 : DB에서 객체 리스트 불러와 SelectBox 그리기 액션 수행(첫 번째 진입점)
			인	자 : 
			사용법 : 

			작성일 : 2020-03-27
			작성자 : 조형욱
			수정일	수정자	수정내용
			------	------	-------------------
			2020-03-27	

			 */
			/************************************************************************/
				$.ajax({
					type:"GET",
					url:OlapUrlConfig.getObjectList,
					async : false,
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
				}).done(function(result) {
					if (result !== undefined && result !== null
							&& result["record"] !== undefined
							&& result["record"] !== null
							&& result["record"] instanceof Array) {
						
						_that.dataset.object = result.record;
						
						// selectbox Draw and event binding
						olapDataView.fn_view_object_selectbox_initNDraw(
								"formControlObjectSelect1", _that.dataset.object,
								function() {
									_that.dataset.objSelect1 = this.value; 
									_that.dataset.selectedObjInfo1 = olapDataView.fn_search_dataset_selected_objInfo(_that.dataset.object, _that.dataset.objSelect1);
									//console.log("_that.dataset.selectedObjInfo1",_that.dataset.selectedObjInfo1);
									// 03.28 grid 및 유저가 선택한 데이터 reset&새로그리기
									olapDataView.fn_reset_user_selected_detail_info("objectDetailInfo",_that.dataset.clickedObjectDetailInfo);									
									if (_that.dataset.objSelect1 !== "notSelected") {

										// selectbox 1 객체 정보 호출
										olapDataView.fn_call_object_detail_info(_that.dataset.selectedObjInfo1["tableName"], function(_resultAry){
											var _objDetailDomId = "objectDetailInfo";
											
											olapDataView.fn_clear_object_detail_view("objectDetailinfo"); //필요.
											if(_resultAry.length > 0){
												_that.dataset.objectDetailInfo = _resultAry; //table정보		
												//console.log("선택한 테이블의 컬럼리스트: ",_that.dataset.objectDetailInfo );
												 olapDataView.fn_draw_object_detail(_objDetailDomId,_that.dataset.objectDetailInfo); //selectbox form id , table info
											}
										});
										
									} else {
										olapDataView.fn_clear_object_detail_view("objectDetailinfo");						
										
									}
								});
					}
					
				}).fail(commonFunc.ajaxFailAction); // AJAX
		},
	   saveAction : function(saveInfo,dataset){ //DataLoad - 실행 시작 => TB_PROJECT_SUB UPDATE
	    	var result = "";
			let _that = this;
			//console.log("olapDataView.dataset.objectDetailList",olapDataView.dataset.objectDetailList);
			
			var tableName = olapDataView.dataset.objectDetailList[0].tableName;
			var updatedata = {
					value : JSON.stringify(olapDataView.dataset.objectDetailList),
					key : objectId + "|"+ "property_info",
					tableName : tableName,
					subProjectId : subPjtId,
		    		userid : userId
			};
			olapDataView.loadingBar = new loading_bar2();
			olapDataView.loadingBar.show();
			console.log("saveaction params:",updatedata);
			$.ajax({
				type : "POST",
				url:OlapUrlConfig.updateSubContents, //updateSubContents
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				data : JSON.stringify(updatedata),
				async: false, //동기
				contentType: "application/json;charset=UTF-8",
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				complete:function(){
					olapDataView.loadingBar.hide();	
				},
				success : function(data, status, xhr){
					if(status === "success"){
						olapDataView.csvAction(subPjtId,objectId);
					}else{
						alert("데이터 통신이 원활하지 않습니다.");// 실패
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					commonFunc.ajaxFailAction(jqxXHR);
				}
			});
		},
		csvAction : function(subPjtId,objectId){
			var tableName = olapDataView.dataset.objectDetailList[0].tableName;
			actionData = {
					subPjtId : subPjtId,
					objectId : objectId,
					value : JSON.stringify(olapDataView.dataset.objectDetailList),
					tableName : tableName
			}
			console.log("csvAction_actionData start:",actionData);
			$.ajax({
				type : "POST",
				url:OlapUrlConfig.csvAction,
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				data : JSON.stringify(actionData),
				async: false, //동기
				contentType: "application/json;charset=UTF-8",
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				complete:function(){
					
				},
				success : function(data, status, xhr){
					if(status === "success"){
						console.log("csv Write Action End!");
						olapDataView.csvLoadAction(subPjtId,objectId);
					}else{
						alert("데이터 통신이 원활하지 않습니다.");// 실패
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					commonFunc.ajaxFailAction(jqxXHR);
				}
			});
		},
		csvLoadAction : function(subPjtId,objectId,filePath,flag){ //저장된 csv 파일 읽어오기
			console.log("csvLoadAction start :",filePath);
			console.log("csvLoadAction start flag:",flag);
			if(filePath == null || undefined){
				filePath = "D:\\lev_ml\\"+subPjtId+".csv";
			}
			let _that = this;
			searchdata = { 
					subProjectId : subPjtId,
					key : objectId + "|"+ "property_info",
					filePath : filePath
					//filePath : "D:\\lev_ml\\"+subPjtId1+".csv"
			 };
			$.ajax({
				type : "POST", 
				url : OlapUrlConfig.csvLoadAction, 
				data : JSON.stringify(searchdata), 
				contentType: "application/json;charset=UTF-8",
				async: false,
				success : function(data, status, xhr){
					if(status === "success"){		
						olapDataView.csvLoadData = data; //jsgrid 데이터.
						if(objectId == "Table"){
							olapDataView.loadAction(subPjtId,objectId,flag); 
						}else if(objectId == "decisionTree" || "splitData"){
							$("#csvFilePath").text(filePath); //decisionTree 컴포넌트에 train filePaht 경로-> python으로 보낼 csv 경로
							olapDataView.drawGrid(data); //처음 로드할때 SOURCE jsgrid에 data를 그리는 작업.
						}else if(objectId == "decisionTree_predict"){
							$("#testDataFilePath").text(filePath); //decisionTree 컴포넌트에 testdata filePaht 경로-> python으로 보낼 csv 경로
						}else{
							olapDataView.loadAction(subPjtId,objectId,flag);
						}

					}else{
						alert("데이터를 받아오지 못했습니다.")
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					//commonFunc.ajaxFailAction(jqxXHR);
				}
			});
		},
		drawGrid : function(data){
			var headerdata = data.fileds; // 헤더 List
			var list = [];
			for(var i=0; i<headerdata.length; i++){ //header for문으로 추출하여 fileds 채우기.
				var filed = {name : headerdata[i],title : headerdata[i],align : "center", css:"font-size-down"};
				list.push(filed);
			}
			//console.log("objectViewGrid data:", data);
			$("#objectViewGrid").jsGrid('destroy');
			$("#objectViewGrid").jsGrid({
		    	width: "100%",
		        height: ($(document).height()-200)+"px",
		        sorting: true, //정렬
		        paging: true, //조회행넘어가면 페이지버튼 뜸
		        loadMessage : "Now Loading...",
		        data: data.record, 
		        fields:list,
		    	onPageChanged: function() { //페이지 변경시
		    		var gridData = $("#jsGrid").jsGrid("option", "data");
		    	}
		    });
			
			// decision Tree 컨트롤 Feature영역 _
			//////////////////////////////////////////////////////////////////////////////////////CONTROL Feature 부분 시작.
			let _that = this;
			var _objDetailDomId = "featureList";
	    	var _objValue_01 = ""+_objDetailDomId;
	    	
	    	_objDetailDomId = "#featureList"; 
			
			var _createElem01 = '<div  data-toggle="tooltip" data-placement="bottom" title="',
	    	_createElem02 = '" class="form-check custom-detail-div custom-detail-div-font-size" ><input type="checkbox" class="d-inline-block chbox" name="check" value="',
	    	_createElem03 = '" id="',
	    	_createElem04 = '"><label for="',
	    	_createElem05 =  '"><span class="custom-checkbox"></span></label>',
	    	_createElem06 =  '<div class="d-inline-block text-left evt-select-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width text-truncate" >',
	    	_createElem06_ifFuncExtElem =  '<div class="d-inline-block text-left evt-select-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width-funcext text-truncate" >',
	    	_createElem07_ifFuncExtElem = '</div>',
	    	_createElem07 = '</div></div>'
	    		
    		var selectAll ='<div class="form-check custom-detail-div custom-detail-div-font-size" >'+
    		'<input type="checkbox" class="checkAll d-inline-block chbox" name="check">'+
    		'<label><span class="custom-checkbox"></span></label>'+
    		'<div class="d-inline-block text-left evt-select-all-detail-chkbox custom-detail-chkbox-div custom-detail-chkbox-div-width text-truncate" style="border: none !important;" >전체 선택</div></div>';
			
			
			
			var i, _chkValue, _chkName, _chkDescript, _opidx;
    		
    		$(_objDetailDomId).append(selectAll);
    		$(_objDetailDomId).find(".checkAll").attr("id",_objDetailDomId.substr(1, _objDetailDomId.length-1)+"_checkAll");  //id 속성 추가
    		$(_objDetailDomId).find(".checkAll").siblings('label').attr('for', _objDetailDomId.substr(1, _objDetailDomId.length-1)+"_checkAll"); //label 속성 추가
			
			for(i in list){
    			//_chkValue = _objValue_01 + "_"+i;
    			_chkValue = list[i]["name"];
    			_chkName = list[i]["name"]; //or list[i]["name"]; ?
    			_chkDescript = list[i]["title"];

    			// 06.28  집계 함수 분기
    			
    		$(_objDetailDomId).append(_createElem01+_chkDescript+_createElem02+_chkValue+_createElem03+_chkValue+_createElem04+_chkValue+_createElem05+_createElem06+_chkName+_createElem07);	
    		}
			
			$(_objDetailDomId).find(".evt-select-detail-chkbox").on("click",function(e,allObj){ //check box 텍스트 부분 클릭시 .
				
    			var objState = $(this).siblings('input:checkbox[name="check"]').prop('checked');
    			console.log("objState",objState); //체크시 false
    			var allState=null; //check 시 true 
    			if(allObj !== undefined && allObj.hasOwnProperty("allSelectSatate")){
    				allState = allObj.allSelectSatate;   // allState : true
    			}
    			if(allObj === undefined || allState !== objState){ //전체선택시 allstate:true , objstate : false => 실행.
    				$(this).siblings('input:checkbox[name="check"]').prop('checked',function(){
    			        return !$(this).prop('checked');
    				});
    				
    				var chkVal = $(this).siblings('input:checkbox[name="check"]').val();
    				
    				if(allObj === undefined){ //전체선택 상태에서 하나 해제시 전체선택 체크해제.
    					var _chkAllState = $(_objDetailDomId).find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked');
    					
    					if(_chkAllState){
    						$(_objDetailDomId).find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);
    					}
    				}

    			}
    			
    		});
    		 
			$(_objDetailDomId).find('.evt-select-all-detail-chkbox').on('click',function(e){ //전체선택 텍스트 부분	    			
    			$(this).siblings('input:checkbox[name="check"]').prop('checked',function(){  //텍스트 클릭시 checkbox 해제 
    		        return !$(this).prop('checked');
    		  });
    			var _checkedState = $(this).siblings('input:checkbox[name="check"]').prop('checked') //체크시 true
    			
    			let clickAry = [];
    			$("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').each(function(index, item){
    				clickAry.push($(item).val()); //checkbox checked  value값 받아와서 배열에 넣기.
    			});
    	    	if(_checkedState == true){
    	    		$("#featureList").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true);//셋팅시 checked 
    				$("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true); //셋팅시 checked
    				olapDataView.dataset.clickedObjectDetailInfo = clickAry; //clickedObjectDetailInfo 전체값
    				console.log("clickAry",clickAry);
    	    	}else{ //(_checkedState ==false)
    	    		$("#featureList").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);//셋팅시 check 해제
    				$("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false); //셋팅시 checked	해제
    				olapDataView.dataset.clickedObjectDetailInfo = []; //clickedObjectDetailInfo 초기화
    	    	}
    			
    	    });
    		
    		$(_objDetailDomId+"_checkAll").on("change",function(e){ //전체선택 checkbox 부분.
    			var _checkedState =$(_objDetailDomId+"_checkAll").prop('checked'); 
    			if(_checkedState == true){
    	    		$("#featureList").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true);//셋팅시 checked 
    				$("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true); //셋팅시 checked
    				olapDataView.dataset.clickedObjectDetailInfo = clickAry; //clickedObjectDetailInfo 전체값

    	    	}else{ //(_checkedState ==false)
    	    		$("#featureList").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);//셋팅시 check 해제
    				$("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false); //셋팅시 checked	해제
    				olapDataView.dataset.clickedObjectDetailInfo = []; //clickedObjectDetailInfo 초기화
    	    	}
    		});
			
    		//check box checked된 상태로 만들기 
	    	$("#featureList").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true);//셋팅시 checked 
			$("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', true); //셋팅시 checked
			let clickAry = [];
			$("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').each(function(index, item){
				clickAry.push($(item).val()); //checkbox checked  value값 받아와서 배열에 넣기.
			});
			
			//////////////////////////////////////////////////////////////////////////////////////CONTROL Feature 부분 끝.
			
			////////////////////////////////////////////////////////////////////////////////////CONTROL Label 부분 시작.
			$.each(list, function(key, value){
				$("#labelSelextBox").append("<option value='"+value.name+"' data-toggle='tooltip' data-placement='bottom' "+ 
						"title="+value.title+">"+value.name+"</option>");
			});
			////////////////////////////////////////////////////////////////////////////////////CONTROL Label 부분 끝.
			
			
		},
		loadAction : function(subPjtId,objectId,flag){ 
			
			let _that = this;
			var subPjtId1 = subPjtId;
			var objectId1 = objectId
			if(objectId == "Table"){
				searchdata = { 
						subProjectId : subPjtId1,
						key : objectId + "|"+ "property_info",
				 };
				$.ajax({
					type : "POST", //post 방식으로 전송
					url : OlapUrlConfig.getSubProjectContents, //OlapUrlConfig.js 의 insertBoard
					data : JSON.stringify(searchdata),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
					contentType: "application/json;charset=UTF-8",
					async:false,
					success : function(data, status, xhr){
						if(status === "success"){						
							var contents = data.record;
							if(contents == null){
								//console.log("저장된 값이 없는상태.");
							}else{
								var obj = JSON.parse(contents.property_info);
								if(obj != null){
									var tableName = obj[0].tableName;
									olapDataView.receiveData(obj); //결과 데이터를 파라미터로 넘겨서 checked setting 하는 부분 TODO: fn_call_object_detail_info 에서 에러발생 처리필요.
								}
							}
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			}else if(objectId =="splitData"){
				searchdata = { 
						subProjectId : subPjtId1,
						key : objectId + "|"+ "splitData_info",
				 };
				$.ajax({
					type : "POST", //post 방식으로 전송
					url : OlapUrlConfig.getSubProjectContents, //OlapUrlConfig.js 의 insertBoard
					data : JSON.stringify(searchdata),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
					contentType: "application/json;charset=UTF-8",
					async:false,
					success : function(data, status, xhr){
						if(status === "success"){						
							var contents = data.record;
							//console.log("contents:",contents);
							if(contents == null){ //실패
								//console.log("저장된 값이 없는상태.");
							}else{
								var obj = JSON.parse(contents.splitData_info);
								console.log("load acition splitData 저장된 값 : ",obj);
								$("#trainRatio").val(obj.trainRatio);
								$("#testRatio").val(obj.testRatio);
								$("#seed").val(obj.seed);
								olapDataView.splitCsvLoad(obj.subPjtId,obj.objectId,obj.trainDataFilePath);
								if(flag == "decisionTree"){ //decisionTree 컴포넌트 SOURCE 부분 - trainDataFilePath 파일 불러와서 출력.
									olapDataView.csvLoadAction(obj.subPjtId,obj.objectId,obj.trainDataFilePath);
								}else if(flag == "decisionTree_predict"){ 
									olapDataView.csvLoadAction(obj.subPjtId,obj.objectId,obj.testDataFilePath); //decisionTree_predict 컴포넌트 SOURCE 부분 - testDataFilePath 파일 불러와서 출력.
									$("#testCsvFilePath").text(obj.testDataFilePath);
								}
								
							}		
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});		
			}else if(objectId =="decisionTree"){
				searchdata = { 
						subProjectId : subPjtId1,
						key : objectId + "|"+ "modelsave_info",
				 };
				$.ajax({
					type : "POST", //post 방식으로 전송
					url : OlapUrlConfig.getSubProjectContents, //OlapUrlConfig.js 의 insertBoard
					data : JSON.stringify(searchdata),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
					contentType: "application/json;charset=UTF-8",
					async:false,
					success : function(data, status, xhr){
						if(status === "success"){						
							var contents = data.record;
							if(contents == null){
								//console.log("저장된 값이 없는상태.");
							}else{
								var obj = JSON.parse(contents.modelsave_info);
								console.log("decisionTree 저장된 값 : ",obj);
								modelobj = obj; //modelobj는 restapi 에서 저장될 데이터로 사용
								
								if(flag == "decisionTree_predict"){ //decisionTree_predict 컴포넌트 SOURCE 부분 - testDataFilePath 파일 불러와서 출력.
									$("#dctModelFilePath").text(obj.dctModelPath);
									$("#dctModelId").text(obj.dctModelId);
								}else{
									olapDataView.mLTestGridDraw(obj); 
									$("#dctModelImgPath").text("");
									$("#dctModelImgPath").text(obj.dctModelImgPath);
									$("#dctModelId").text("");
									$("#dctModelId").text(obj.dctModelId);
								}
							}		
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			}else if(objectId =="decisionTree_predict"){
				searchdata = { 
						subProjectId : subPjtId,
						key : objectId + "|"+ "dctPredict_info",
				 };
				$.ajax({
					type : "POST", //post 방식으로 전송
					url : OlapUrlConfig.getSubProjectContents, //OlapUrlConfig.js 의 insertBoard
					data : JSON.stringify(searchdata),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
					contentType: "application/json;charset=UTF-8",
					async:false,
					success : function(data, status, xhr){
						if(status === "success"){						
							var contents = data.record;
							if(contents == null){
								//console.log("저장된 값이 없는상태.");
							}else{
								var obj = JSON.parse(contents.dctPredict_info);
								console.log("decisionTree_predict 저장된 값 : ",obj);
								//modelobj = obj; //modelobj는 restapi 에서 저장될 데이터로 사용
								$("#score").text(obj.score);
								olapDataView.mLTestGridDraw(obj); //0724 RESULT 부분에 grid를 그려서 보여줄 필요가 없다. => img , Feature importance 같은걸로 대체.
								
								if(flag == "restapi"){
									featureList = JSON.parse(obj.featureList);
									label = obj.label;
									$("#modelId").val(obj.modelId);
									$("#modelFilePath").text(obj.modelFilePath);
									$("#modelLabel").val(label);
									$("#restApi_featureList").empty();
									var resultList =document.getElementById("restApi_featureList");
									for(var i =0; i<featureList.length; i++){
										resultList.innerHTML += "<input type=\"text\" readonly class=\"form-control-plaintext\" id=\""+featureList[i]+"\" value=\""+featureList[i]+"\"><br>";
									}
									
									$("#restApi_TestFeatureList").empty();
									var testResultList =document.getElementById("restApi_TestFeatureList");
									for(var i =0; i<featureList.length; i++){
										testResultList.innerHTML += "<div class=\"form-group row\"><label class=\"col-sm-3 col-form-label form-control-plaintext\">"+featureList[i]+"  :" +"</label>"+
												"<div class=\"col-sm-5\"><input type=\"text\" class=\"form-control-plaintext\" name=\"testFeaturetList\" id=\"testResultList"+i+"\" placeholder=\""+ featureList[i] + 
												"\"></div></div><br>";
										
									}
								}
								
								
							}		
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			}else if(objectId =="restapi"){ //TODO decisiontree 의 json 데이터를추출하여 Flask서버에 데이터를 전송할생각..
				searchdata = { 
						subProjectId : subPjtId1,
						key : "restapi" + "|"+ "modelapi_info",
				 };
				$.ajax({
					type : "POST", //post 방식으로 전송
					url : OlapUrlConfig.getSubProjectContents, //OlapUrlConfig.js 의 insertBoard
					data : JSON.stringify(searchdata),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
					contentType: "application/json;charset=UTF-8",
					async:false,
					success : function(data, status, xhr){
						if(status === "success"){						
							var contents = data.record;
							//console.log("restapi modelapi_info :",contents);
							if(contents == null){
								//console.log("저장된 값이 없는상태.");
							}else{
								var obj = JSON.parse(contents.modelapi_info);
								console.log("restapi loadaction obj : ",obj);
								$("#modelId").val(obj.modelId);
								$("#modelUrl").val(obj.url);
								$("#modelLabel").val(obj.label);
								
								featureList = obj.featureList;
								$("#restApi_featureList").empty();
								var modelresultList =document.getElementById("restApi_featureList");
								for(var i =0; i<featureList.length; i++){
									modelresultList.innerHTML += "<input type=\"text\" readonly class=\"form-control-plaintext\" id=\""+featureList[i]+"\" value=\""+featureList[i]+"\"><br>";
									
								}
								$("#restApi_TestFeatureList").empty();
								var testResultList =document.getElementById("restApi_TestFeatureList");
								for(var i =0; i<featureList.length; i++){
									testResultList.innerHTML += "<div class=\"form-group row\"><label class=\"col-sm-3 col-form-label form-control-plaintext\">"+featureList[i]+"  :" +"</label>"+
											"<div class=\"col-sm-5\"><input type=\"text\" class=\"form-control-plaintext\" name=\"testFeaturetList\" id=\"testResultList"+i+"\" placeholder=\""+ featureList[i] + 
											"\"></div></div><br>";
									
								}
							}		
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
				
			}
			
		},
	    receiveData : function(_data){
	    	console.log("receiveData",_data);
	    	let _that = this;
	    	var savedData= _data;
	    	//console.log("receiveData_objdetailinfo",savedData);
	    	if(savedData.length > 0){
				 $("#formControlObjectSelect1").val(savedData[0].tableName);
				 _that.dataset.objSelect1 = savedData[0].tableName; 
				 
				 _that.dataset.selectedObjInfo1 = olapDataView.fn_search_dataset_selected_objInfo( _that.dataset.object,  _that.dataset.objSelect1);
 
				 olapDataView.fn_reset_user_selected_detail_info("objectDetailInfo",_that.dataset.clickedObjectDetailInfo);
				 olapDataView.fn_clear_object_detail_view("objectDetailinfo");
										// selectbox 1 객체 정보 호출
				 console.log("_that.dataset.selectedObjInfo1[tableName]",_that.dataset.selectedObjInfo1["tableName"]);
				 olapDataView.fn_call_object_detail_info(_that.dataset.selectedObjInfo1["tableName"], function(_resultAry){  //컬럼 리스트를 반환한다.
					 //console.log("_resultAry",_resultAry);
					 var _objDetailDomId = "objectDetailInfo";
					 if(_resultAry.length > 0){
						 _that.dataset.objectDetailInfo = _resultAry;											
						 olapDataView.fn_draw_object_detail(_objDetailDomId,_that.dataset.objectDetailInfo); //checkbox 그리기
					 }
					 // 유저가 선택한 checkbox 값만 출력
					 let userSelectedObj = savedData;  //유저가 저장했던 항목값
					 let originObjDetail = _that.dataset.objectDetailInfo; //기존 항목리스트
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
					 
					olapDataView.dataset.clickedObjectDetailInfo = userSelectedIds; //유저가 저장했던 항목값을 clickedObjectDetailInfo에 넣어주어 조회 파라미터로 사용.
				    
					 //전체선택되어있든 일부만 되어있든 상관없이 체크박스 전부~ 해제!
					 $("#objectDetailinfo").find('.evt-select-all-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false);//전체선택 초기화 checkbox 해제
					 $("#objectDetailinfo").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false); //컬럼 리스트 초기화
			    		
					 for(let i in userSelectedIds){ // userSelectedIds 값(저장목록에서 불러온 데이터 값)을 보고 해당하는 ID를 가진 element에 체크박스 true
						 chkId = userSelectedIds[i];

						 $("#"+chkId).prop("checked", true);
					 }//조회시 유저가 체크했던 selectbox checked 상태 유지.
					 if(userSelectedObj.length == _that.dataset.objectDetailInfo.length){ // length 체크하여 저장값과 리스트값이 같은경우 전체선택 checked.
						 $("#objectDetailinfo_checkAll").prop("checked", true);
					 }
				 });
			    	
	    		}else{
	    			console.log("Not Exist Data");
	    			alert("저장된 정보가 없거나 데이터 파싱 중 오류가 발생했습니다.")
	    		}
	    	olapDataView.drawGrid(olapDataView.csvLoadData); //checkbox checked 후 그리는 함수 실행.
	    	},
	
	    fn_draw_object_detail : function(_objDetailDomId, _objectDetailInfoAry){ // jsgrid 컬럼 리스트 그리기.
	    	
	    	let _that = this;
	    	var _objValue_01 = ""+_objDetailDomId;
	    	//_objDetailDomId = "#" + _objDetailDomId;
	    	_objDetailDomId = "#objectDetailinfo" ; 
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

	    			// 06.28  집계 함수 분기
	    			if(_objectDetailInfoAry[i]["calcFuntionYn"] === "Y"){ //column list 그리는 부분
	    				$(_objDetailDomId).append(_createElem01+_chkDescript+_createElem02+_chkValue+_createElem03+_chkValue+_createElem04+_chkValue+_createElem05+_createElem06_ifFuncExtElem+_chkName+_createElem07_ifFuncExtElem+_funcSelectBox+_createElem07_ifFuncExtElem);
	    				var $selectElem =$("#"+_chkValue).siblings(".custom-detail-chkbox-selctboxdiv-width-funcext").find("select");
	    				  				
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
			
			olapDataView.dataset.clickedObjectDetailInfo = clickAry;
			olapDataView.fn_draw_only_column_jsGrid(clickAry);
	
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
	    
	    	//_that.fn_draw_selected_data_Grid("only",rtnObj, loadingBarObj);
	    	olapDataView.dataset.objectDetailList = selectDataset.info; //조회시 파라미터로 넘겨주기위해 olapDataView.dataset.objectDetailList 에 사용자가 선택한 체크박스 값들 넣어줌 (선택한 순서대로.)
	    },
	    fn_draw_selected_data_Grid : function(_flag,_data , loadingBarObj){
	    	//console.log("checked된 컬럼 리스트",_data);
	    	let _that = this;
	    	
	    
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
	    				let fieldName = fields[i]["title"];	    				
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
	    ,fn_search_dataset_selected_objInfo : function(_datasetAry, searchName){
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
	    }


	    /************************************************************************/
	    /*
	    함수명 : fn_reset_user_selected_detail_info
	    설	명 : 유저가 선택한 객체세부정보 reset
	    인	자 : dataset.clickedObjectDetailInfo
	    사용법 : 

	    작성일 : 2019-03-18
	    작성자 : 최 진 
	    수정일	수정자	수정내용
	    ------	------	-------------------
	    2019.03.18	최진	최초 작성
	     */
	    /************************************************************************/
	    ,fn_reset_user_selected_detail_info : function(elemId, _data){
	    	let _that = this;
	    	var deleteId = elemId+"_", _dataVal ,rtnAry = [];	    	
	    	for(var i = 0; i < _data.length; i ++){
	    		_dataVal =_data[i]; 
	    		if(_dataVal.indexOf(deleteId) == -1){
	    			rtnAry.push(_dataVal);
	    		}
	    	}
	    	
	    	
	    	_that.dataset.clickedObjectDetailInfo = rtnAry;
	    	olapDataView.fn_draw_only_column_jsGrid(_that.dataset.clickedObjectDetailInfo);
	    	
	    }

	  
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
	     ,fn_call_object_detail_info : function(_tableName, _fnCallback){
	    	 
	    	let _that = this;
	    	_that.tbName = _tableName
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
	    	}).fail(commonFunc.ajaxFailAction);
	    }

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
	     ,fn_view_object_selectbox_initNDraw : function(domId, data, fnEvt){
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

	     }
	     /************************************************************************/
	     /*
	     함수명 : 
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
	     ,fn_clear_object_detail_view : function(elemId){
	     	if($("#"+elemId + " > div").length > 0){
	     		$("#"+elemId).empty();
	     	}
	     },
	     
	     /**
	      * python으로 전송 데이터.
	      * 
	      * 
	      */
	     decisionTreeTrain : function(subPjtId){ 
	    	 console.log("decisionTreeTrain start!! subPjtId:",subPjtId);
	    	 var feature_count = document.getElementsByName("check").length;
	    	 let feature_list = []; //feature List
	    	 for(var i=1; i<feature_count; i++){
	    		 if(document.getElementsByName("check")[i].checked == true){
	    			 feature_list.push(document.getElementsByName("check")[i].value);
	    		 }
	    	 }
	    	 var label = $("#labelSelextBox").val(); //label
	    	 var csvFilePath = $("#csvFilePath").text();
	    	 submitdata = { 
	    			 subPjtId : subPjtId,
	    			 featureList : JSON.stringify(feature_list),
	    			 label : label,
	    			 filePath : csvFilePath
					 //filePath : "D:\\lev_ml\\"+subPjtId+".csv"
			  };
	    	 $.ajax({
					type : "POST", 
					url : OlapUrlConfig.decisionTreeTrain, 
					data : JSON.stringify(submitdata), 
					contentType: "application/json;charset=UTF-8",
					async: false,
					success : function(data, status, xhr){
						if(status === "success"){
							console.log("decisionTreeTrain return data:::",data);
							$("#dctModelId").text(data.dctModelId);
							$("#dctModelImgPath").text(data.dctModelImgPath);
							olapDataView.modelInfoSaveAction(data); //TB_PROJECT_SUB - contents의 update 데이터
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
	    	
	     },
	     mLTestGridDraw : function(data){ //model 테스트 결과값을 그리는 부분
	    	 //console.log("mLTestGridDraw data : ",data);
	    	 var featureList = JSON.parse(data.featureList);
	    	 
	    	 featureListLength = $("input:checkbox[name=check]").length;
	    	 if(featureListLength-1 == featureList.length){ // length 체크하여 저장값과 리스트값이 같은경우 전체선택 checked.
	    		 $("#featureList_checkAll").prop("checked", true);
	    	 }else{
	    		 $("#featureList_checkAll").prop("checked", false);
	    	 }
	    	 $("#featureList").find('.evt-select-detail-chkbox').siblings('input:checkbox[name="check"]').prop('checked', false); //feature 컬럼 리스트 초기화
	    	 for(let i in featureList){ // 불러온 값 checked
				 chkId = featureList[i];
				 $("#"+chkId).prop("checked", true);
			 }
    		 $("#labelSelextBox").val();
    		 $("#score").text();
    		 $("#labelSelextBox").val(data.label);
			 $("#score").text(data.score);
			 var headerdata = data.header; // 헤더 List
				var list = [];
				for(var i=0; i<headerdata.length; i++){ //header for문으로 추출하여 fileds 채우기.
					var filed = {name : headerdata[i],title : headerdata[i],align : "center", css:"font-size-down"};
					list.push(filed);
				}
			 $("#dct_resultGrid").jsGrid('destroy');
				$("#dct_resultGrid").jsGrid({
			    	width: "100%",
			        height: ($(document).height()-200)+"px",
			        sorting: true, //정렬
			        paging: true, //조회행넘어가면 페이지버튼 뜸
			        loadMessage : "Now Loading...",
			        data: data.featureData, 
			        fields:list,
			    	onPageChanged: function() { //페이지 변경시
			    		var gridData = $("#jsGrid").jsGrid("option", "data");
			    	}
			    });
	     },
	     
	     modelInfoSaveAction : function(data){ 
		    	//console.log("modelInfoSaveAction data:",data)
				let _that = this;
				
				var updatedata = {
						value : JSON.stringify(data),	//featureList , label , accuartScore , dataList , modelpath
						key : objectId + "|"+ "modelsave_info",
						subProjectId : subPjtId,
			    		userid : userid
				};
				
				$.ajax({
					type : "POST",
					url:OlapUrlConfig.updateSubContents, //updateSubContents
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					data : JSON.stringify(updatedata),
					contentType: "application/json;charset=UTF-8",
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
					complete:function(){
						//olapDataView.loadingBar.hide();
					},
					success : function(data, status, xhr){
						if(status === "success"){

						}else{
							alert("데이터 통신이 원활하지 않습니다.");// 실패
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			},
			modelApiSaveAction : function(data,userid,featureList,label){ //rest api 배포버튼 클릭시 updateSubContents 로 UPDATE
		    	data.featureList = featureList;
		    	data.label = label;
				//console.log("modelApiSaveAction data:",typeof(data),data)
				let _that = this;
				
				var updatedata = {
						value : JSON.stringify(data),	//featureList , label , accuartScore , dataList
						key : objectId + "|"+ "modelapi_info",
						subProjectId : subPjtId,
			    		userid : userid
				};
				console.log("modelInfoSaveAction params:",updatedata);
				
				$.ajax({
					type : "POST",
					url:OlapUrlConfig.updateSubContents, //updateSubContents
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					data : JSON.stringify(updatedata),
					contentType: "application/json;charset=UTF-8",
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
					complete:function(){
						//olapDataView.loadingBar.hide();
						olapDataView.insertToModelInfo(data); //TB_ML_MODEL 테이블에
					},
					success : function(data, status, xhr){
						if(status === "success"){

							
						}else{
							alert("데이터 통신이 원활하지 않습니다.");// 실패
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			},
			insertToModelInfo : function(data){
				//console.log("insertToModelInfo : ", data);
				var featureList = data.featureList;
				var modelId = data.modelId;
				var subPjtId = data.subPjtId
				var url = data.url;
				var insertdata = {						
						subProjectId : subPjtId,
						modelId : modelId,
						url : url,
						featureList : JSON.stringify(featureList),
			    		userId : userid
				};
				
				$.ajax({
					type : "POST",
					url:OlapUrlConfig.insertToModelInfo, // TB_ML_MODEL insert
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					data : JSON.stringify(insertdata),
					contentType: "application/json;charset=UTF-8",
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
					complete:function(){
						//olapDataView.loadingBar.hide();
					},
					success : function(data, status, xhr){
						if(status === "success"){
							
						}else{
							alert("데이터 통신이 원활하지 않습니다.");// 실패
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			},
			splitData : function(data){
				//console.log("split data :",data);
				$.ajax({
					type : "POST", 
					url : OlapUrlConfig.splitData, 
					data : JSON.stringify(data), 
					contentType: "application/json;charset=UTF-8",
					async: false,
					success : function(data, status, xhr){
						if(status === "success"){
							console.log("splitData return data:::",data); //data.objectId , data.subPjtId, data.testDataFilePath, data.trainDataFilePath
							var subPjtId = data.subPjtId;
							var objectId = data.objectId;
							var testDataFilePath = data.testDataFilePath
							var trainDataFilePath = data.trainDataFilePath
							olapDataView.splitCsvLoad(subPjtId,objectId,trainDataFilePath); //jsgrid 그리기
							olapDataView.splitDataSaveAction(data);
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			},
			splitCsvLoad : function(subPjtId,objectId,filePath){ //저장된 csv 파일 읽어오기
				let _that = this;
				searchdata = { 
						subProjectId : subPjtId,
						key : objectId + "|"+ "property_info",
						filePath : filePath
				 };
				$.ajax({
					type : "POST", 
					url : OlapUrlConfig.csvLoadAction, 
					data : JSON.stringify(searchdata), 
					contentType: "application/json;charset=UTF-8",
					async: false,
					success : function(data, status, xhr){
						if(status === "success"){		
							//console.log("splitCsvLoad result data:",data);
							olapDataView.mLTrainGridDraw(data);
						}else{
							alert("데이터를 받아오지 못했습니다.")
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						//commonFunc.ajaxFailAction(jqxXHR);
					}
				});
			},
			mLTrainGridDraw : function(data){ //splite Result => Train Data그리는 부분
				//console.log("mLTrainGridDraw data :",data);
				var headerdata = data.fileds; // 헤더 List
				var list = [];
				for(var i=0; i<headerdata.length; i++){ //header for문으로 추출하여 fileds 채우기.
					var filed = {name : headerdata[i],title : headerdata[i],align : "center", css:"font-size-down"};
					list.push(filed);
				}
				 $("#resultGrid").jsGrid('destroy');
					$("#resultGrid").jsGrid({
				    	width: "100%",
				        height: ($(document).height()-200)+"px",
				        sorting: true, //정렬
				        paging: true, //조회행넘어가면 페이지버튼 뜸
				        loadMessage : "Now Loading...",
				        data: data.record, 
				        fields:list,
				    	onPageChanged: function() { //페이지 변경시
				    		var gridData = $("#jsGrid").jsGrid("option", "data");
				    	}
				    });
		     },
		     splitDataSaveAction : function(data){ //rest api 배포버튼 클릭시 updateSubContents 로 UPDATE
			    	console.log("splitDataSaveAction data : ",data);
					var updatedata = {
							value : JSON.stringify(data),	//featureList , label , accuartScore , dataList
							key : objectId + "|"+ "splitData_info",
							subProjectId : subPjtId,
				    		userid : userid
					};
					console.log("splitDataSaveAction params:",updatedata);
					
					$.ajax({
						type : "POST",
						url:OlapUrlConfig.updateSubContents, //updateSubContents
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						data : JSON.stringify(updatedata),
						contentType: "application/json;charset=UTF-8",
						 beforeSend: function(xhr) {
						        xhr.setRequestHeader("AJAX", true);
						     },
						complete:function(){
							//olapDataView.loadingBar.hide();
							
						},
						success : function(data, status, xhr){
							if(status === "success"){

							}else{
								alert("데이터 통신이 원활하지 않습니다.");// 실패
							}
						},
						error : function(jqxXHR, textStatus, errorThrown){
							commonFunc.ajaxFailAction(jqxXHR);
						}
					});
				},
				decisionTreePredict : function(subPjtId){ 
			    	 //console.log("submitData start!! subPjtId:",subPjtId);
			    	 var feature_count = document.getElementsByName("check").length;
			    	 let feature_list = []; //feature List
			    	 for(var i=1; i<feature_count; i++){
			    		 if(document.getElementsByName("check")[i].checked == true){
			    			 feature_list.push(document.getElementsByName("check")[i].value);
			    		 }
			    	 }
			    	 var label = $("#labelSelextBox").val(); //label
			    	 var csvFilePath = $("#testCsvFilePath").text();
			    	 var modelFilePath = $("#dctModelFilePath").text();
			    	 var modelId = $("#dctModelId").text();
			    	 submitdata = { 
			    			 subPjtId : subPjtId,
			    			 featureList : JSON.stringify(feature_list),
			    			 label : label,
			    			 csvFilePath : csvFilePath,
			    			 modelFilePath : modelFilePath,
			    			 modelId : modelId

					  };
			    	 $.ajax({
							type : "POST", 
							url : OlapUrlConfig.decisionTreePredict, 
							data : JSON.stringify(submitdata), 
							contentType: "application/json;charset=UTF-8",
							async: false,
							success : function(data, status, xhr){
								if(status === "success"){
									console.log("decisionTreePredict return data:::",data);
									olapDataView.mLTestGridDraw(data); //grid에 그려주는 함수
									olapDataView.dctPredictSaveAction(data); //TB_PROJECT_SUB - contents의 update 데이터
								}else{
									alert("데이터를 받아오지 못했습니다.")
								}
							},
							error : function(jqxXHR, textStatus, errorThrown){
								commonFunc.ajaxFailAction(jqxXHR);
							}
						});
			    	
			     },
			     dctPredictSaveAction : function(data){
			    	 var updatedata = {
								value : JSON.stringify(data),	//featureList , label , accuartScore , dataList
								key : objectId + "|"+ "dctPredict_info",
								subProjectId : subPjtId,
					    		userid : userid
						};
						console.log("dctPredictSaveAction params:",updatedata);
						
						$.ajax({
							type : "POST",
							url:OlapUrlConfig.updateSubContents, //updateSubContents
							headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
							data : JSON.stringify(updatedata),
							contentType: "application/json;charset=UTF-8",
							 beforeSend: function(xhr) {
							        xhr.setRequestHeader("AJAX", true);
							     },
							complete:function(){
								//olapDataView.loadingBar.hide();
								
							},
							success : function(data, status, xhr){
								if(status === "success"){

								}else{
									alert("데이터 통신이 원활하지 않습니다.");// 실패
								}
							},
							error : function(jqxXHR, textStatus, errorThrown){
								commonFunc.ajaxFailAction(jqxXHR);
							}
						});
			     },
};
