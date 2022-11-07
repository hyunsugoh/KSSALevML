
/**
 * ML 그룹 정보 js파일
 * require files : olap-common.js , jui framework
 */


let adminMLGroupInfo = {
		treeObj : null,
		state : {
			addParentCode : "",
			add : false,
			update: false
		},
		alertStr : {
				codeStr : "동일한 이름이 존재합니다. 대소문자도 동일 값으로 취급합니다.",
				codeStrEmpty: "코드 이름을 지정하십시오.",
				code:"동일한 코드명이 존재합니다. 대소문자도 동일 값으로 취급합니다.",
				codeEmpty:"적용할 코드 값을 입력하십시오.",
				maxLenStr : "최대 100글자까지만 입력할 수 있습니다.",
				propStr : "등록하고자 하는 속성을 선택하십시오.",
				noChangeStr :"수정하려는 값이 동일합니다.",
				
		},
		dataset : {
			grpCode:[],
			feaCode : [],
			relInfo:[],
			treeList:[],
			mergedFeaCode :[],
			hieCode:[],
			pkMax:0
		},
		init : function(){
			if($("#treeLoadingBar").find("p").hasClass("d-none")){
				$("#treeLoadingBar").find("p").removeClass("d-none")
			}
			
			// CODE - tree 구조
			$.ajax({ 
				type:"GET",
				url:OlapUrlConfig.getAdminTbCode,
				data:{
					flag:"MLCode"
				},
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     }
			}).done(function(_result){
				if(commonFunc.chkValue(_result) && 
						_result.hasOwnProperty("grpCode") &&
						_result.hasOwnProperty("feaCode") &&
						_result.hasOwnProperty("relationList")){
					
					adminMLGroupInfo.dataSettingMLCode(_result);	
					adminMLGroupInfo.drawTableData(adminMLGroupInfo.dataset);
				}else{
					// exception
					alert("데이터 객체가 존재하지 않습니다.");
				}
			}).fail(commonFunc.ajaxFailAction);
			
			// HIERA - jsgrid
			$.ajax({ 
				type:"GET",
				url:OlapUrlConfig.getAdminTbCode,
				data:{
					flag:"Hierarchy"
				},
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     }
			}).done(function(_result){
				if(commonFunc.chkValue(_result) && 
						_result.hasOwnProperty("hierarchy")){
					
					adminMLGroupInfo.dataSettingHieCode(_result);	
					//console.log("adminMLGroupInfo.dataset[HIERA]:",adminMLGroupInfo.dataset);
					adminMLGroupInfo.drawHieData(adminMLGroupInfo.dataset);
				}else{
					// exception
					alert("데이터 객체가 존재하지 않습니다.");
				}
			}).fail(commonFunc.ajaxFailAction);
			
		},
		dataSettingHieCode : function(data){
			adminMLGroupInfo.dataset.hieCode = data.hierarchy;
			let searhAry = [];
			for(let i in data.hierarchy){
				searhAry.push(data.hierarchy[i]["PK_NUM"]);
			}
			adminMLGroupInfo.dataset.pkMax = Math.max.apply(null, searhAry); // PK_NUM 최대값
		},
		
		drawHieData : function(){
			$("#hidCodeGrid").jsGrid({
				height : "auto",
				width:"100%",
				editing : true,
				sorting : true,
				paging: true,
				pageButtonCount: 5,
		        pagerContainer: "#externalPager",
		        pagerFormat: ' {first} {prev} {pages} {next} {last} &nbsp;&nbsp;  &nbsp;&nbsp; 전체 {pageCount} 페이지 중 현재 {pageIndex} 페이지',
		        pagePrevText: "이전",
		        pageNextText: "다음",
		        pageFirstText: "처음",
		        pageLastText: "마지막",
		        pageNavigatorNextText: "&#8230;",
		        pageNavigatorPrevText: "&#8230;",
				loadMessage : "로딩중...",
				pagerContainerClass: "custom-jsgrid-pager-container",
		        pagerClass: "custom-jsgrid-pager",
		        pagerNavButtonClass: "custom-jsgrid-pager-nav-button",
		        pagerNavButtonInactiveClass: "custom-jsgrid-pager-nav-inactive-button",
		        pageClass: "custom-jsgrid-pager-page",
		        currentPageClass: "custom-jsgrid-pager-current-page",
				data : adminMLGroupInfo.dataset.hieCode,
				fields : [{
					name : "PK_NUM",
					title : "pk_num",
					type: "text",
					align:"center",
					width : 100,
					css:"font-size-down",
					visible:false
				},{
					name : "LEVEL_1",
					title : "level_1",
					type: "text",
					align:"center",
					width : 100,
					css:"font-size-down"
				},{
					name : "LEVEL_2",
					title : "level_2",
					type: "text",
					align:"center",
					width : 100,
					css:"font-size-down"
				},{
					name : "LEVEL_3",
					title : "level_3",
					type: "text",
					align:"center",
					width : 100,
					css:"font-size-down"
				},{
					name : "LEVEL_4",
					title : "level_4",
					type: "text",
					align:"center", 
					width : 100,
					css:"font-size-down",
					visible : false
				},{
					name : "LEVEL_5",
					title : "level_5",
					type: "text",
					align:"center",
					width : 100,
					css:"font-size-down",
					visible : false
				}, { type: "control",
					modeSwitchButton: false,
					headerTemplate : function(){
						return $("<button>").attr("type","button").addClass("btn btn-sm btn-outline-primary").text("추가").on("click",function(e){
							$('#hieraAddModal').modal("show");
						});
					},
				} // elem
					], // fields
					
			confirmDeleting : false,
			onItemDeleting : function(args) {
				if(!args.item.deleteConfirmed){
					args.cancel = true;
					confirm("정보를 삭제 하시겠습니까?",
							function(result){ // bootbox js plugin for confirmation dialog
								if (result == true) {
									args.item.deleteConfirmed = true;
									$("#hidCodeGrid").jsGrid('deleteItem', args.item); //call deleting once more in callback
									adminMLGroupInfo.deleteItem(args.item); //Table delete 실행. 
								}
							});
				}
				
				
			},
			onItemUpdating : function(args){
				//console.log(args);
				confirm("정보를 수정 하시겠습니까?",
						function(result){
							if(result == true){
								args.item.updateConfirmed = true; //rgs.item에 updateConfirmed 옵션 추가 후 true 대입.
								adminMLGroupInfo.updateItem(args.item);
							}
				});
			
			},
			
		}); // grid
			
			
		},
		deleteItem : function(item){
			console.log("delete",item)
			return $.ajax({
				type : "POST",
				url : OlapUrlConfig.adminMLGroupInfoDelete,
				contentType : 'application/json',
				data : JSON.stringify(item),
				headers : {
					'X-CSRF-TOKEN' : $(
							'#csrfvalue').val()
				},
				beforeSend: function(xhr) {
                      xhr.setRequestHeader("AJAX", true);
                    },
				dataType : "JSON",
				success : function(data) {
					//console.log(data);

					if (data == 1) {
						console.log(data);
					} else {
						alert("삭제 실패");
					}
				},
				error : function(jqXHR, textStatus, errorThrown){
					  console.log(jqXHR.status);
					if(jqXHR.status === 400){
						alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
					}else if (jqXHR.status == 401) {
			            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
			            	location.href = OlapUrlConfig.loginPage;
			            });
			         } else if (jqXHR.status == 403) {
			            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
			            	location.href = OlapUrlConfig.loginPage;
			            }); 
			         }else if (jqXHR.status == 500) {
			        	 errAlert(jqXHR.status, jqXHR.responseText)
			         }else{
						//alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
					}
				}
			});
		},
		updateItem : function(item) { //update
			//console.log("item",item);
			return $
					.ajax({
						type : "POST",
						url : OlapUrlConfig.adminMLGroupInfoUpdate,
						contentType : 'application/json',
						data : JSON.stringify(item),
						headers : {
							'X-CSRF-TOKEN' : $('#csrfvalue').val()
						},
						beforeSend: function(xhr){
                              xhr.setRequestHeader("AJAX", true);
                            },
						dataType : "JSON",
						success : function(data){
							
							//$("#hidCodeGrid").jsGrid("loadData");																				
						},
						error : function(jqXHR, textStatus, errorThrown){
							//console.log("update!",jqXHR.status);
							if(jqXHR.status === 400){
								alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
							}else if (jqXHR.status == 401) {
					            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
					            	location.href = OlapUrlConfig.loginPage;
					            });
					         } else if (jqXHR.status == 403) {
					            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
					            	location.href = OlapUrlConfig.loginPage;
					            });
					         }else if (jqXHR.status == 500) {
					        	 errAlert(jqXHR.status, jqXHR.responseText)
					         }else{
								//alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오!!.");
							}
						}
					});
		},
		
		// add (추가)
		insertItem : function(insertdata){
			//console.log(insertdata);
			if(insertdata.level1 === ""){
				alert("LEVEL_1 값을 입력하여 주십시오");
				return;
			}
			$.ajax({
				type : "POST",
				url : OlapUrlConfig.adminMLGroupInfoInsert,
				contentType : 'application/json',
				data : JSON.stringify(insertdata),
				headers : {
					'X-CSRF-TOKEN' : $('#csrfvalue').val()
				},
				beforeSend: function(xhr) {
                      xhr.setRequestHeader("AJAX", true);
                    },
                dataType : "JSON",
                /*success : function() {
                	//$('#hieraAddModal').modal("hide");
					$("#hidCodeGrid").jsGrid("loadData");																				
				},*/
				error : function(jqXHR, textStatus, errorThrown){
					//console.log("insert!",jqXHR.status);
					if(jqXHR.status === 400){
						alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
					}else if (jqXHR.status == 401) {
			            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
			            	location.href = OlapUrlConfig.loginPage;
			            });
			        } else if (jqXHR.status == 403) {
			        	alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
			        	   location.href = OlapUrlConfig.loginPage;
			            });
			        }else if (jqXHR.status == 500) {
			        	errAlert(jqXHR.status, jqXHR.responseText)
			        }else{
						//alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오!!.");
			        	adminMLGroupInfo.init();
			        	$('#hieraAddModal').modal("hide");
					}
				}
			})
			//$('#hieraAddModal').modal("hide");

		},
		
		/**
		 * 2019.11.14 데이터 세팅(테이블 구조로 데이터 변경)
		 */
		dataSettingMLCode : function(data){
			adminMLGroupInfo.dataset.grpCode = data.grpCode;
			adminMLGroupInfo.dataset.feaCode = data.feaCode;
			adminMLGroupInfo.dataset.relInfo = data.relationList;
		},
		
		/**
		 * 2019.11.14 변경 데이터 테이블 뷰
		 */
		drawTableData : function(data){
			let grpCodeInfo = data.grpCode;
			let feaCodeInfo = data.feaCode;
			let valCodeInfo = data.relInfo;
			let dataSet = [];
			for(let i=0;i<grpCodeInfo.length;i++){
				dataSet.push(grpCodeInfo[i]);
				let gCode = grpCodeInfo[i]["CODE_NAME"]//
				if(feaCodeInfo.hasOwnProperty(gCode)){
					let feaAry = feaCodeInfo[gCode]; //pumptype data
					for(let j=0;j<feaAry.length;j++){
						dataSet.push(feaAry[j]);
						let feaCode = feaAry[j]["CODE_NAME"]
						if(valCodeInfo.hasOwnProperty(feaCode)){
							let valInfo = valCodeInfo[feaCode]; //맨 마지막값?
							for(let k =0;k<valInfo.length;k++){
								valInfo[k]["PARENT_CODE"] = feaCode;
								valInfo[k]["TOP_GRP_CODE"] = gCode;
								dataSet.push(valInfo[k]);
								mlInfoList = gCode;
							}
						}
					}
				}
			}             
			
			$("#codeJsGrid").jsGrid({ //ML 그룹정보
				  height: "100%",
			      width: "100%",
			      data : dataSet,
			      rowRenderer:function(item) {
			    	  console.log("--------item--------");
			    	  console.log(item);
			    	  let $div = $("<div>");
			    	  let $p =$("<span>"); 
			    	  $div.addClass("tree-elem-style");
			    	  $p.addClass("tree-node-elem-style")
			    	  if(item.hasOwnProperty("DETAIL_VALUES")){
			    		  //value
			    		  let strName = item["DETAIL_VALUES"];
			    		  let $span =$("<span>");
			    		  
			    		  // dataset
			    		  $div.data("prop", "value");
			    		  $div.data("strName", strName);
			    		  $div.data("codeName", strName);
			    		  $div.data("codeValue", strName);
			    		  $div.data("parentCode", item["PARENT_CODE"]);
			    		  $div.addClass(item["TOP_GRP_CODE"] + "-top-node");
			    		  $div.addClass(item["PARENT_CODE"] + "-node");
			    		  $div.addClass("two-dept-node");
			    		  // append
			    		  $div.append($p);
			    		  $span.addClass("badge badge-success");
			    		  $span.append("KeyWord");
			    		  $p.append($span);
			    		  $p.append(" " +strName);
			    	  }else {
				    	  let strName = item["CODE_STR_NAME"];
				    	  let codeName = item["CODE_NAME"];
				    	  
				    	  let $span =$("<span>");
				    	  if(codeName.indexOf("GRML") > -1){
				    		  // groupCode
				    		  $div.data("prop", "grpCode");
				    	  }else{
				    		  //feaCode
				    		  $span.addClass("badge badge-info");
				    		  $span.append("Group");
				    		  // dataset
				    		  $div.data("prop", "feaCode");
				    		  $div.data("parentCode", item["PARENT_CODE"]);
				    		  $div.addClass(item["PARENT_CODE"] + "-top-node");
				    		  $div.addClass("one-dept-node");
				    		  
				    	  }
				    	  $div.data("strName", strName);
				    	  $div.data("codeName", item["CODE_NAME"]);
				    	  $div.data("codeValue", item["CODE_VALUE"]);
				    	  
				    	  if(adminMLGroupInfo.dataset.feaCode.hasOwnProperty(item["CODE_NAME"]) || 
				    			  adminMLGroupInfo.dataset.relInfo.hasOwnProperty(item["CODE_NAME"])){
				    		  let $btn = $("<button>");
				    		  $btn.addClass("tree-icon-btn");
				    		  let $i = $("<i>");
				    		  $i.attr("class","far fa-minus-square");
				    		  $btn.append($i);
				    		  $p.append($btn);
				    		  // tree show/hide
				    		  $btn.on("click",function(e){
				    				 //  action
				    			  e.stopPropagation();
				    			  let codeProp = $(this).parent().parent().data("prop");
				    			  let clsName = "";
			                	  // 버그 해결
			                	  if(codeProp === "grpCode"){
			                		  clsName = $(this).parent().parent().data("codeName")+"-top-node";
			                	  }else{
			                		  clsName = $(this).parent().parent().data("codeName") + "-node";
			                	  }
			                	  if($(this).find("i").hasClass("fa-plus-square")){
			                		  $("."+clsName).removeClass("d-none");
			                		  $(this).find("i").removeClass("fa-plus-square");
		                			  $(this).find("i").addClass("fa-minus-square");
		                			  if($("#codeJsGrid").find("i").hasClass("fa-plus-square")){
		                				  $("#codeJsGrid").find("i").removeClass("fa-plus-square");
		                				  $("#codeJsGrid").find("i").addClass("fa-minus-square");
		                			  }
			                	  }else{
			                		  $("."+clsName).addClass("d-none");
			                		  $(this).find("i").removeClass("fa-minus-square");
		                			  $(this).find("i").addClass("fa-plus-square");
		                			  
		                			
			                	  }
				                	
				    		  });   // tree show/hide end
				    		  
				    	  }//if
				    	  
			    		  $p.append($span);
			    		  $p.append(strName);
				    	  $div.append($p);
			    	  }

			    	  
			    	  //evtBind
			       	  $div.on("click",function(e){
			       		  if(!adminMLGroupInfo.state.add && !adminMLGroupInfo.state.update){
			       			  // 추가나 수정중일 때는 동작안함.
			       			  if($("#codeJsGrid").find(".selected-code").length > 0){
					       			$("#codeJsGrid").find(".tree-elem").removeClass("selected-code");
				       		  }
				       		  if($(this).hasClass("selected-code") === false){
				       			$(this).addClass("selected-code");  
				       		  }
					       		  
				    		  let codeProp = $(this).data("prop");
				    		  let strName = $(this).data("strName");
				    		  let codeValue = $(this).data("codeValue");
				    		  $("#formCodeName").val(codeValue);
				    		  if(codeProp === "grpCode"){
				    			  $("#formProperty").val("항목 (컬럼)");						                	 
				                	 $("#formCodeStrName").val(strName);
				                	 if($("#codeStrGrp").hasClass("d-none")){
				                		 $("#codeStrGrp").removeClass("d-none");	 
				                	 }
				                	 
				    		  }else if(codeProp ==="feaCode"){
				                	 $("#formProperty").val("그룹");
				                	 $("#formCodeStrName").val(strName);
				                	 if($("#codeStrGrp").hasClass("d-none")){
				                		 $("#codeStrGrp").removeClass("d-none");	 
				                	 }
				                }else{
				                	$("#formProperty").val("키워드");
				                	 $("#formCodeStrName").val("");
				                	 if(!$("#codeStrGrp").hasClass("d-none")){
				                		 $("#codeStrGrp").addClass("d-none");	 
				                	 }
				                }	
			       		  }
			    	  }); // on click
			    	  
			    	  return $div;
			      }, // rowRenderer
			      fields: [
			            { title: "ML 그룹 정보" }
			        ]
			});
			
			if(!$("#treeLoadingBar").find("p").hasClass("d-none")){
				$("#treeLoadingBar").find("p").addClass("d-none")
			}
		},
		
		commClearFormAction : function(){
			if($("#codeJsGrid").find(".selected-code").length > 0){
				$("#codeJsGrid").find(".tree-elem-style").removeClass("selected-code");
    		}
			// update
			if(adminMLGroupInfo.state.update){
				$("#propUpdateHelpBlock").text("");
				adminMLGroupInfo.state.update = false;
			}else{
				// add & delete
				adminMLGroupInfo.state.add = false;
				$("#formPropertyGrp").removeClass("d-none");
				$("#formPropertyAddGrp").addClass("d-none");
				$("#formParentAddGrp").addClass("d-none");
				$("#codeStrGrp").removeClass("d-none");
			}
			$("#formAddProperty").val("default");
			$("#formProperty").val("");
			$("#formCodeName").val("");
			$("#formCodeStrName").val("");
			adminMLGroupInfo.state.addParentCode = "";
			// block text clear
			$("#propertyHelpBlock").text("");
			$("#formCodeStrNameHelpBlock").text("");
			$("#formCodeNameHelpBlock").text("");
			$("#formCodeStrNameHelpBlock").text("");
			
			$("#formCodeGrp").find(".form-control").removeClass("form-control").addClass("form-control-plaintext");
			$("#formCodeName").attr("readonly", true);
			$("#formCodeStrName").attr("readonly", true);
			
			$("#addMLCode").attr("disabled" , false);
			$("#updateMLCode").attr("disabled" , false);
			$("#delMLCode").attr("disabled" , false);	
			$("#confirmBtn").addClass("d-none");
			$("#cencelsBtn").addClass("d-none");
		}, 
		evtBind : function(){
			// 추가
			$("#addMLCode").on('click',function(e){
				if(adminMLGroupInfo.treeObj !== null){
					adminMLGroupInfo.treeObj.unselect();	
				}
				
				adminMLGroupInfo.state.add = true;
				$("#addMLCode").attr("disabled" , true);
				$("#updateMLCode").attr("disabled" , true);
				$("#delMLCode").attr("disabled" , true);
				$("#formProperty").val("");
				$("#formPropertyGrp").addClass("d-none");
				$("#formPropertyAddGrp").removeClass("d-none");
				$("#formCodeName").val("");
				$("#formCodeStrName").val("");
				$("#formCodeName").attr("readonly", false);
				$("#formCodeStrName").attr("readonly", false);
				$("#formCodeGrp").find(".form-control-plaintext").removeClass("form-control-plaintext").addClass("form-control");
				$("#confirmBtn").removeClass("d-none");
				$("#cencelsBtn").removeClass("d-none");
			});
			
			// 수정 
			$("#updateMLCode").on('click',function(e){
				if($("#codeJsGrid").find(".selected-code").length === 0){
					alert("먼저 수정할 코드를 선택하십시오.")
					return;
				}
				if($("#codeJsGrid").find(".selected-code").data("prop") === "grpCode"){
					// 최상위는 수정할수 없음
					alert("항목(컬럼)은 수정할 수 없습니다.");
					return;
				}
				
				adminMLGroupInfo.state.update= true;
				$("#addMLCode").attr("disabled" , true);
				$("#updateMLCode").attr("disabled" , true);
				$("#delMLCode").attr("disabled" , true);
				$("#formCodeGrp").find(".form-control-plaintext").removeClass("form-control-plaintext").addClass("form-control");
				$("#propUpdateHelpBlock").text("속성은 수정할 수 없습니다. 변경하려면 삭제 후 신규등록 하십시오.");
				$("#formCodeName").attr("readonly", false);
				$("#formCodeStrName").attr("readonly", false);
				$("#confirmBtn").removeClass("d-none");
				$("#cencelsBtn").removeClass("d-none");
			});
			
			
			// 삭제
			$("#delMLCode").on('click',function(e){
				if($("#codeJsGrid").find(".selected-code").length === 0){
					alert("먼저 삭제할 코드를 선택하십시오.")
					return;
				}
				
				if($("#codeJsGrid").find(".selected-code").data("prop") === "grpCode"){
					// 최상위는 수정할수 없음
					alert("그룹은 삭제할 수 없습니다.");
					return;
				}
				
				$("#addMLCode").attr("disabled" , true);
				$("#updateMLCode").attr("disabled" , true);
				$("#delMLCode").attr("disabled" , true);
				let selectedVal = $("#formCodeName").val();
				confirm(selectedVal + " 을(를) 삭제하시겠습니까?",function(isOk){
					$("#addMLCode").attr("disabled" , false);
					$("#updateMLCode").attr("disabled" , false);
					$("#delMLCode").attr("disabled" , false);
					let selectedVal = $("#formCodeName").val();
					if(isOk){
						adminMLGroupInfo.delAction();
					}else{
						return;
					}
				});
			});
			
			//init fileData grid
			function confExcelGridInit(){
				$("#jsGrid_confExcel").jsGrid('destroy');
				$("#jsGrid_confExcel").jsGrid({
					width: "100%",
				    height: $(document).height()-950,
				    editing: false, //수정 기본처리
				    sorting: false, //정렬
				    paging: false, //조회행넘어가면 페이지버튼 뜸
				    loadMessage : "Now Loading...",
			        confirmDeleting: false,
				    fields: [
				    	{name : "CODE_NAME",		title : "CodeName",			type : "text",	align : "center",	width : 100,visible:false}
				    	,{name : "PRODUCT_GROUP",	title : "Product Group명",	type : "text",	align : "center",	width : 100,css:"text-truncate"}
				    	,{name : "PRODUCT_NAME",	title : "Product Name",		type : "text",	align : "center",	width : 100,css:"text-truncate"}
				    ]
					,controller:  {
						loadData : function(filter) {
							return filter;
						},
						deleteItem: function(item) {
						}
				    }
				});
			}
			
			///////////////////////////////////////////////////////
			// MLGroupInfo Excel Down
			$("#downMLGroupInfo").click(function(){
				 //OlapUrlConfig.downMLGroupInfo("MLCode");
				 $.ajax({ 
						type:"GET",
						url:OlapUrlConfig.downMLGroupInfo,
						data:{
							flag:"MLCode"
							,CODE_NAME: $("#mlGroupSel option:selected").val()
						},
						beforeSend: function(xhr) {
						        xhr.setRequestHeader("AJAX", true);
						},
						success: function (result) { //result 생성된 엑셀파일명
				        	console.log("result",result);
					        if(result.result !== null){
					        	var vFileParam = $.param({
					        			"file_name" : result.result
					        		});
					        	//$("#fileDownFrame").attr("src", "<c:url value="/ml/predictApplyExcelFileDownload2.do" />?"+vFileParam); //? 사용하여 파일명을 GET방식 parameter(vFileParam = 생성된 엑셀 파일명) 넘김.
					        	$("#fileDownFrame").attr('src','/ml/predictApplyExcelFileDownload2.do?'+vFileParam);
					        }else{
					        	alert("처리중 오류가 발생하였습니다.");
					        }
				        },
					}).fail(commonFunc.ajaxFailAction);
			});
			
			//MLGroupInfo Excel Up load
			$("#upLoadMLGroupInfo").click(function(){
				//if(	!chkActivityFunc()) return false;
				confExcelGridInit();
				$('#input_files').val("");
				$('#mlGroupSelUpload').val("GRML0001");
				$('#mLGroupInfoExcelUpload .modal-content').css("height",($(document).height()-660)+"px");	
				$('#mLGroupInfoExcelUpload').modal("show");
			});
			//MLGroupInfo엑셀 업로드 실행 버튼
			$("#mLGroupInfo_btn_excel_upload_ok").click(function(){
				var flag = "mLGroupInfo";
				var confExcel = $ ('#jsGrid_confExcel').jsGrid("option", "data");
				if(confExcel.length == 0){
					alert("파일을 선택해주세요.");
					return;
				}
				adminMLGroupInfo.savInfoProcess(excelGridData); 
				$('#mLGroupInfoExcelUpload').modal("hide");
			});
			
			
			//hiear INFO Excel Up load
			$("#upLoadHieraInfo").click(function(){
				//if(	!chkActivityFunc()) return false;
				$('#hiearExcelUpload').modal("show");
			});
			//hiear엑셀 업로드 실행 버튼
			$("#hiearInfo_btn_excel_upload_ok").click(function(){
				var flag = "hiearInfo";
				adminMLGroupInfo.excelUploadProcess(flag);
				$('#hiearExcelUpload').modal("hide");
			});
			
			
			
			
			
			//hiear Excel down - 1021 완료
			$("#downHieraInfo").click(function(){
			
				var HierData = $("#hidCodeGrid").jsGrid('option','data'); //jsgrid data 받아오기 Object 배열 형태.
				console.log("HierData",typeof(HierData),HierData);
				$.ajax({ 
					type:"POST",
					url:OlapUrlConfig.downHierarchyInfo,
					data: JSON.stringify({
			        	
			        	HierDataInfo : HierData //HierData 
			        	
			        }),
					contentType: 'application/json',
					beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					},
					success: function (result) { //result 생성된 엑셀파일명
			        	//loadingMsg.modal('hide');
			        	console.log("result",result);
				        if(result.result !== null){
				        	var vFileParam = $.param({
				        			"file_name" : result.result
				        		});
				        	//$("#fileDownFrame").attr("src", "<c:url value="/ml/predictApplyExcelFileDownload2.do" />?"+vFileParam); //? 사용하여 파일명을 GET방식 parameter(vFileParam = 생성된 엑셀 파일명) 넘김.
				        	$("#fileDownFrame").attr('src','/ml/predictApplyExcelFileDownload2.do?'+vFileParam);
				        }else{
				        	alert("처리중 오류가 발생하였습니다.");
				        }
			        },
				}).fail(commonFunc.ajaxFailAction);
			});
			
			//다운로드 팝업 오픈
			$("#opemMlDownPop").click(function(){
				$('#mlGroupInfoPop').modal("show");
			});
			
			
			///////////////////////////////////////////////////////
			
			// 확인 버튼
			$("#confirmBtn").on('click',function(e){
				e.preventDefault();
				if(adminMLGroupInfo.state.add){
					// 추가
					adminMLGroupInfo.addConfirmAction(); 
				}
				
				if(adminMLGroupInfo.state.update){
					adminMLGroupInfo.updateConfirmAction();
				}
				
				
			});
			
			// 취소 버튼 
			$("#cencelsBtn").on('click', function(e){
				e.preventDefault();
				adminMLGroupInfo.commClearFormAction();
			});
			
			
			
			// 속성 변경 이벤트
			$("#formAddProperty").on("change", function(e){
				let value = $(this).val();
				let codeList = null;
				if(value !== "default"){
					$("#propertyHelpBlock").empty();
				}
				
				
				$("#formAddParent").empty();
				$("#codeStateBadge").empty();
				if(value === "fea"){
					adminMLGroupInfo.state.addParentCode = "grpCode";
					codeList = adminMLGroupInfo.dataset.grpCode;
					
					if($("#codeStrGrp").hasClass("d-none")){
						$("#codeStrGrp").removeClass("d-none");
					}
				}else if(value ==="value"){
					adminMLGroupInfo.state.addParentCode = "feaCode";
//					codeList = ;
//					mergedFeaCode
					if(adminMLGroupInfo.dataset.mergedFeaCode.length === 0){
						let feaCode =adminMLGroupInfo.dataset.feaCode;
						let meargedDataset = [];
						for(let key in feaCode){
							let feaEAry = feaCode[key];
							for(let i=0;i<feaEAry.length;i++){
								meargedDataset.push(feaEAry[i])
							}
						}
						adminMLGroupInfo.dataset.mergedFeaCode = meargedDataset;
					}
					
					codeList = adminMLGroupInfo.dataset.mergedFeaCode;
					$("#codeStateBadge").removeClass("badge-success");
					$("#codeStateBadge").addClass("badge-info");
					$("#codeStateBadge").append("Group");
					if(!$("#codeStrGrp").hasClass("d-none")){
						$("#codeStrGrp").addClass("d-none");	
					}
				}else{
					adminMLGroupInfo.state.addParentCode = "";
					if($("#codeStrGrp").hasClass("d-none")){
						$("#codeStrGrp").removeClass("d-none");
					}
				}
				
				if(codeList !== null && Array.isArray(codeList)){
					for(let i=0; i < codeList.length; i++){
						let codeStr = codeList[i]["CODE_STR_NAME"];
						$("#formAddParent").append(new Option(codeStr, i));
					}					
				}
				
				if(value === "default"){
					if(!$("#formParentAddGrp").hasClass("d-none")){
						$("#formParentAddGrp").addClass("d-none");	
					}
				}else{
					if($("#formParentAddGrp").hasClass("d-none")){
						$("#formParentAddGrp").removeClass("d-none");	
					}
				}
			});
			
			$("#addHieraBtn").click(function(){
				adminMLGroupInfo.insertItem(
					{
						level1 : $("#level1").val(),
						level2 : $("#level2").val(),
						level3 : $("#level3").val(),
						level4 : $("#level4").val(),
						level5 : $("#level5").val(),
						pkMax : adminMLGroupInfo.dataset.pkMax
					});
						
			});
			
		},
		chkFormValidate : function(){
			$("#propertyHelpBlock").empty();
			$("#formCodeNameHelpBlock").empty();
			$("#formCodeStrNameHelpBlock").empty();
			
			let propertyVal = $("#formAddProperty").val(); 
			let isValid = true;
			
			// add 일때만 실행
			if(adminMLGroupInfo.state.add && propertyVal === "default"){
				$("#propertyHelpBlock").append(adminMLGroupInfo.alertStr.propStr);
				isValid = false;
			}
			
			// contain check
			let codeVal = $.trim($("#formCodeName").val());
			let codeStr = $.trim($("#formCodeStrName").val());
		
			if(commonFunc.chkValue(codeVal) === false || codeVal === ""){
				$("#formCodeNameHelpBlock").append(adminMLGroupInfo.alertStr.codeEmpty);
				isValid = false;
			}			
			if(!$("#codeStrGrp").hasClass("d-none") && (commonFunc.chkValue(codeStr) === false || codeStr === "")){
				$("#formCodeStrNameHelpBlock").append(adminMLGroupInfo.alertStr.codeStrEmpty);
				isValid = false;
			}			
			
			// valid 통과 못하면 다음로직 실행 안함.
			if(isValid === false){
				return isValid;
			}
			
			if(codeVal.length > 100){
				$("#formCodeNameHelpBlock").append(adminMLGroupInfo.alertStr.maxLenStr);
				isValid = false;
				return isValid;
			}
			if(!$("#codeStrGrp").hasClass("d-none") && codeStr.length > 100){
				isValid = false;
				$("#formCodeStrNameHelpBlock").append(adminMLGroupInfo.alertStr.maxLenStr);
				return isValid;
			}
			// 위에서 통과 안되면 아래 로직 실행 안됨.
			
			// update일 경우에만 실행 : 현재 선택한 값과 같은지 비교 같으면 아래 체크로직 건너뜀
			let originCodeVal = $(".selected-code").data("codeValue");
			let originCodeStr = $(".selected-code").data("strName");
			let updateEqualValStr = true;
			let updateEqualVal = true;
			if(adminMLGroupInfo.state.update && codeVal === originCodeVal){
				updateEqualVal = false;
			}
			if(adminMLGroupInfo.state.update && codeStr === originCodeStr){
				updateEqualValStr = false;
			}
			
			if(!updateEqualVal && !updateEqualValStr){
				$("#formCodeNameHelpBlock").append(adminMLGroupInfo.alertStr.noChangeStr);
				$("#formCodeStrNameHelpBlock").append(adminMLGroupInfo.alertStr.noChangeStr);
				isValid = false;
				return isValid;
			}else if($("#codeStrGrp").hasClass("d-none") &&!updateEqualVal){
				$("#formCodeNameHelpBlock").append(adminMLGroupInfo.alertStr.noChangeStr);
				isValid = false;
				return isValid;
			}
			
			
			
			let codeKey = adminMLGroupInfo.state.addParentCode;
			let parentIdx = $("#formAddParent").val();
			let parentCode = null;
			if(commonFunc.chkNotEmptyStr(parentIdx) && commonFunc.chkNotEmptyStr(codeKey)){
				if(codeKey === "feaCode"){
					let mergedCodeKey = "mergedFeaCode";
					parentCode = adminMLGroupInfo.dataset[mergedCodeKey][parentIdx]["PARENT_CODE"];	
				}else{
					parentCode = adminMLGroupInfo.dataset[codeKey][parentIdx]["CODE_NAME"];	
				}
					
			}
			
			// 코드명 과 한글명을 검색한다.
			let grpList = adminMLGroupInfo.dataset.grpCode;
			let feaList = adminMLGroupInfo.dataset.feaCode;
			let codelist = codeKey === "feaCode" ? adminMLGroupInfo.dataset.relInfo : adminMLGroupInfo.dataset.feaCode;
			// GROUP and Feature
			
			for(let i=0;i<codelist.length;i++){
				
				let chkCodeValue = codelist[i]["CODE_VALUE"];
				let chkParentCode = codeKey === "feaCode" ? codelist[i]["PARENT_CODE"] : codelist[i]["CODE_NAME"] ;
				
				
				if(codeVal.toUpperCase() === chkCodeValue && 
					$("#formCodeNameHelpBlock").text() === "" &&
					chkParentCode === parentCode &&
					updateEqualVal){
					$("#formCodeNameHelpBlock").append(adminMLGroupInfo.alertStr.code);	
					isValid = false;
				}
				
				if(codeKey === "grpCode" ){
					let chkValueCodeStr = codelist[i]["CODE_STR_NAME"];
					if( codeStr.toUpperCase() === chkValueCodeStr && 
							$("#formCodeNameHelpBlock").text() === "" &&
							chkParentCode === parentCode &&
							updateEqualValStr){
						$("#formCodeStrNameHelpBlock").append(adminMLGroupInfo.alertStr.codeStr);
						isValid = false;
					}	
				}
				
			}
			
			
			return isValid;
		},
		
		addConfirmAction : function(){
			// valid check
			
			let isValid = adminMLGroupInfo.chkFormValidate();
			let propertyVal = $("#formAddProperty").val(); 
			if(!isValid){
				return;
			}else{ 
				let codeVal = $.trim($("#formCodeName").val());
				let codeStr = $.trim($("#formCodeStrName").val());
				let insertProp = "";
				if(propertyVal === "fea"){
					insertProp ="feature";
				}else if(propertyVal === "value"){
					insertProp ="detailValue";
				}
				let setParam = {
						flag :insertProp,
						codeValue : codeVal,
						codeStrName :codeStr
				}; 
				
				// 2019.11.14 수정으로 키값 변경
				let codeKey = adminMLGroupInfo.state.addParentCode === "feaCode" ? "mergedFeaCode" : adminMLGroupInfo.state.addParentCode;
				let parentIdx = $("#formAddParent").val();
				
				if(commonFunc.chkNotEmptyStr(parentIdx) && commonFunc.chkNotEmptyStr(codeKey)){
					setParam.parentData = adminMLGroupInfo.dataset[codeKey][parentIdx];	
				}
				
				$.ajax({ 
					type:"POST",
					 contentType: 'application/json',
					url:OlapUrlConfig.insertAdminTbCode,
					data: JSON.stringify(setParam),
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     }
				}).done(function(_result){
					alert("코드가 신규 생성되었습니다.",function(){
						adminMLGroupInfo.commClearFormAction();	
						$("#codeJsGrid").jsGrid("destroy");
						adminMLGroupInfo.init();
					});
					
				}).fail(commonFunc.ajaxFailAction);
			}
		},
		
		updateConfirmAction : function(){
			let isValid = adminMLGroupInfo.chkFormValidate();
		
			if(!isValid){
				return;
			}else{
				$("#codeJsGrid").find(".selected-code").data("") // TODO
				let codeVal = $.trim($("#formCodeName").val());
				let codeStr = $.trim($("#formCodeStrName").val());
				let setParam = {
						flag :null,
						codeValue : codeVal,
						codeStrName :codeStr
				}; 
				if($("#codeJsGrid").find(".selected-code").data("prop") === "feaCode"){
					setParam.flag = "feature";
				}else if($("#codeJsGrid").find(".selected-code").data("prop") === "value"){
					setParam.flag = "detailValue";
				}else{
					alert("알 수 없는 속성을 수정하려 하고 있습니다. 수정을 할 수 없습니다.");
					return;
				}
				setParam.originCodeData = $("#codeJsGrid").find(".selected-code").data("JSGridItem");
				
				$.ajax({ 
					type:"POST",
					 contentType: 'application/json',
					url:OlapUrlConfig.updateAdminTbCode,
					data: JSON.stringify(setParam),
					 beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     }
				}).done(function(_result){
					alert("코드가 수정되었습니다.",function(){
						adminMLGroupInfo.commClearFormAction();
						$("#codeJsGrid").jsGrid("destroy");
						adminMLGroupInfo.init();
							
					});
					
				}).fail(commonFunc.ajaxFailAction);
			}
		},
		// 삭제
		delAction : function(){
			let setParam = {
					flag :null,
			}; 
			if($("#codeJsGrid").find(".selected-code").data("prop") === "feaCode"){
				setParam.flag = "feature";
			}else if($("#codeJsGrid").find(".selected-code").data("prop") === "value"){
				setParam.flag = "detailValue";
			}else{
				alert("알 수 없는 속성을 삭제하려 하고 있습니다. 삭제 할 수 없습니다.");
				return;
			}
			setParam.delData = $("#codeJsGrid").find(".selected-code").data("JSGridItem");
			
			$.ajax({ 
				type:"POST",
				 contentType: 'application/json',
				url:OlapUrlConfig.deleteAdminTbCode,
				data: JSON.stringify(setParam),
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     }
			}).done(function(_result){
				alert("코드가 삭제되었습니다.",function(){
					adminMLGroupInfo.commClearFormAction();
					$("#codeJsGrid").jsGrid("destroy");
					adminMLGroupInfo.init();
				});
				
			}).fail(commonFunc.ajaxFailAction);
		},
		//빈값 체크
		isNotEmpty : function (str){
			if(typeof str == "undefined" || str == null || str == "")
		    	return false;
		    else
		        return true ;
		},
		//파일 확장자와 용량 제한
		confExcelGrid : function(obj){
			var file = obj.files;
			var maxSize = 100*1024*1000;  //100M 파일 최대 용량
			var loadingMsg = new loading_bar({message:"데이터를 불러오고 있습니다..."});
			if(file[0].size > maxSize){
				alert("전체 파일업로드 허용용량 "+(maxSize/1024)+" Kbyte를 초과하였습니다.");
			}else{ 
				if(!/\.(xlsx|xls|xlsm)$/i.test(file[0].name)){
					alert("Excel 파일만 선택할 수 있습니다.");
				}else{
					var files = $('#input_files').prop('files')[0];
					var selCode = $('#mlGroupSelUpload option:selected').val();
					var fileOriNm = $("#fileNm").val();
					var formData = new FormData();
					formData.append('selCode', selCode);
					formData.append('files', files);
					if(adminMLGroupInfo.isNotEmpty(fileOriNm)){
						formData.append('fileOriNm', fileOriNm);
					}else{
						formData.append('fileOriNm', "N");
					}
				    $.ajax({
				        url: OlapUrlConfig.excelUploadProcessConf,
				        type: 'POST',
				        data: formData,
				        processData: false,
				        contentType: false,
				        complete: function () {
				        	setTimeout(function() {
				        		loadingMsg.modal('hide');
				        	},500);
					    },
				        success: function (result) {
				        	$("#fileNm").val(result.fileNm);
				        	excelGridData = result.excelDataList;
				        	$("#jsGrid_confExcel").jsGrid("loadData", result.excelDataTop10);
				        }
				    });
					return;
				}
			}
			//초기화
			obj.outerHTML = obj.outerHTML;
		},
		
		//엑셀파일 업로드 및 인서트 처리
		excelUploadProcess : function(flag){
			var fGroupId = "";
			var menuId = "";
			var formData = new FormData();
			if(flag =="mLGroupInfo"){ //id가 같을시 하나는 작동 X
				var files = $('#input_files').prop('files')[0];
			}else if(flag == "hiearInfo"){
				var files = $('#input_files2').prop('files')[0];
			}
			/*var files = $('#input_files').prop('files')[0];*/
			formData.append('files', files);
			formData.append('menuId', menuId);
			formData.append('fGroupId', fGroupId);
			formData.append('flag', flag);
			
			var loadingMsg = new loading_bar({message:"Uploading & Insert..."});
			_fileUploadResult = null;
			$.ajax({
		        url: OlapUrlConfig.excelUploadProcess,
		        type: 'POST',
		        data: formData,
		        processData: false,
		        contentType: false,
		        beforeSend: function () {
		        	loadingMsg.show();
		        },
		        complete: function () {
			     	//loadingMsg.modal('hide');
			     	setTimeout(function() {
			     		loadingMsg.modal('hide');
			     	},500);
			    },
		        success: function (result) {
		        	console.log("result",result);
		        	adminMLGroupInfo.init();
		        },fail : function (jqXHR, textStatus, errorThrown){
					
		        	var eMsg = "";
					if(jqXHR.status === 400){
						eMsg="요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.";
					}else if (jqXHR.status == 401) {
			            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
			            	location.href = OlapUrlConfig.loginPage;
			            });
			        }else if (jqXHR.status == 403) {
			        	//eMsg="세션이 만료가 되었습니다.";
			        	alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
			        		location.href = OlapUrlConfig.loginPage;
			        	});
			        }else if (jqXHR.status == 500) {
			        	 //eMsg=jqXHR.responseText;
			        	eMsg="처리중 에러가 발생하였습니다.";
			        	eMsg = eMsg +"<br/>"+ (jqXHR.responseText).substring(0,200);
			        }else{
			        	eMsg="서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.";
					}
					alert(eMsg);
		        }
		    });
		},
		
		//엑셀파일 업로드 및 인서트 처리 그룹정보 저장
		savInfoProcess : function(excelGridData){
			var fGroupId = "";
			var menuId = "MlGrpInfo";
			var formData = new FormData();
			var files = $('#input_files').prop('files')[0];
			var selCode = $('#mlGroupSelUpload option:selected').val();
			
			formData.append('excelGridData', JSON.stringify(excelGridData));
			formData.append('selCode', selCode);
			var loadingMsg = new loading_bar({message:"Uploading & Insert..."});
			_fileUploadResult = null;
			$.ajax({
				url: OlapUrlConfig.savInfoProcess,
				type: 'POST',
				data: formData,
				processData: false,
				contentType: false,
				beforeSend: function () {
					loadingMsg.show();
				},
				complete: function () {
					setTimeout(function() {
						loadingMsg.modal('hide');
					},400);
				},
				success: function (result) {
					setTimeout(function() {
						alert("저장되었습니다.");
					},400);
					adminMLGroupInfo.init();
				},fail : function (jqXHR, textStatus, errorThrown){
					var eMsg = "";
					if(jqXHR.status === 400){
						eMsg="요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.";
					}else if (jqXHR.status == 401) {
						alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
							location.href = OlapUrlConfig.loginPage;
						});
					}else if (jqXHR.status == 403) {
						//eMsg="세션이 만료가 되었습니다.";
						alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
							location.href = OlapUrlConfig.loginPage;
						});
					}else if (jqXHR.status == 500) {
						//eMsg=jqXHR.responseText;
						eMsg="처리중 에러가 발생하였습니다.";
						eMsg = eMsg +"<br/>"+ (jqXHR.responseText).substring(0,200);
					}else{
						eMsg="서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.";
					}
					alert(eMsg);
				}
			});
		}
};
