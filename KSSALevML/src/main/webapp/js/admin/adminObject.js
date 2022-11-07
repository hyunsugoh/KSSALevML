/**
 * AdminObject.js
 */
//객체 추가를 위한 전역변수
var set_object_name="";

$(document).ready(function(){
	/*HelpMsgAction.creathHelpBtn("adminObject","helpIcon",OLAPAdminHelpMsg);*/
	$.ajax({
		type : "GET",
		url : OlapUrlConfig.AdminObjectList,
		headers : {
			'X-CSRF-TOKEN' : $('#csrfvalue').val()
		},
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
		success : function(data) {
			//console.log(data);

		},
		error : function(jqXHR, textStatus, errorThrown){
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
				alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
			}
		}
	})
	.done(function() {
				//  편집 아이콘  이미지 투명하게  하기위한 CSS 클래스 선언
				jsGrid.ControlField.prototype.deleteButtonClassCustom = "jsgrid-delete-button-custom ";
				jsGrid.ControlField.prototype.editButtonClassCustom = "jsgrid-edit-button-custom ";
				$("#jsGrid").jsGrid(
								{
									height : "500px",
									width : "100%",

									editing : true,
									sorting : false,
									autoload : true,
									paging : true,
									pageSize : 10,
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
									invalidNotify : function(args) {
										var messages = $
												.map(
														args.errors,
														function(error) {
															return error.message
																	+ '<br />';

														});

										alert(messages);
									},
									loadMessage : "로딩중...",
									confirmDeleting : false,
//									onItemDeleting : function(args) {
//										console.log("삭제 하기전 확인");
//										if (!args.item.deleteConfirmed) { // custom property for confirmation
//											args.cancel = true; // cancel deleting
//											
//													confirm(
//															"객체를 삭제하면 연관된 객체 정보,관계 관리,조회 조건 등이 <br>삭제됩니다. 삭제 하시겠습니까?",
//															function(
//																	result) { // bootbox js plugin for confirmation dialog
//																if (result == true) {
//																	args.item.deleteConfirmed = true;
//																	$(
//																			"#jsGrid")
//																			.jsGrid(
//																					'deleteItem',
//																					args.item); //call deleting once more in callback
//																}
//															});
//										}
//									},
									controller : {
										loadData : function(filter) {
											return $
													.ajax({
														type : "GET",
														url : OlapUrlConfig.AdminObjectList,
														data : filter,
														headers : {
															'X-CSRF-TOKEN' : $(
																	'#csrfvalue').val()
														},
														beforeSend: function(xhr) {
			                                              xhr.setRequestHeader("AJAX", true);
			                                            },
														error : function(jqXHR, textStatus, errorThrown){
															  
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
																alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
															}
														}
													});
										},
//										deleteItem : function(item) {
//											console.log(item);
//											return $
//													.ajax({
//														type : "POST",
//														url : OlapUrlConfig.AdminObjectDelete,
//														contentType : 'application/json',
//														data : JSON
//																.stringify(item),
//														headers : {
//															'X-CSRF-TOKEN' : $(
//																	'#csrfvalue')
//																	.val()
//														},
//														beforeSend: function(xhr) {
//				                                              xhr.setRequestHeader("AJAX", true);
//				                                            },
//														dataType : "JSON",
//														success : function(
//																data) {
//															console.log(data);
//
//															if (data == 1) {
//
//																
//																console.log(data);
//
//															}
//
//															else if (data == -1) {
//																alert("활성화 상태에서는 삭제하실 수 없습니다");
//
//																console
//																		.log(data);
//															} else {
//
//																alert("삭제 실패");
//
//															}
//
//															// 그리드 reloading 
//															$("#jsGrid")
//																	.jsGrid(
//																			"loadData");
//															$(
//																	"#jsGridSelect")
//																	.jsGrid(
//																			"loadData");
//
//														},
//														error : function(jqXHR, textStatus, errorThrown){
//															  
//															if(jqXHR.status === 400){
//																alert("요청한 작업을 실패하였습니다. 관리자에게 문의하여 주십시오.");
//															}else if (jqXHR.status == 401) {
//													            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
//													            	location.href = OlapUrlConfig.loginPage;
//													            });
//													             
//													             
//													         } else if (jqXHR.status == 403) {
//													            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
//													            	location.href = OlapUrlConfig.loginPage;
//													            });
//													              
//													         }else if (jqXHR.status == 500) {
//													        	 errAlert(jqXHR.status, jqXHR.responseText)
//													         }else{
//																alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
//															}
//														}
//													});
//										},

										updateItem : function(item) {
											return $
													.ajax({
														type : "POST",
														url : OlapUrlConfig.AdminObjectUpdate,
														contentType : 'application/json',
														data : JSON
																.stringify(item),
														headers : {
															'X-CSRF-TOKEN' : $(
																	'#csrfvalue')
																	.val()
														},
														beforeSend: function(xhr) {
				                                              xhr.setRequestHeader("AJAX", true);
				                                            },
														dataType : "JSON",
														success : function(
																data) {
															console
																	.log(data
																			+ "업데이트");

//															if (data == -1) {
//
//																alert("활성화 상태에서는 수정하실 수 없습니다");
//
//																console
//																		.log(data);
//
//															}
															$("#jsGrid")
																	.jsGrid(
																			"loadData");

														},
														error : function(jqXHR, textStatus, errorThrown){
															
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
																alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
															}
														}
													});
										},

									},
									fields : [
											{
												name : "table_name",
												type : "text",
												title : "테이블명",
												width : 250
											},
											{
												name : "obj_name",
												title : "객체명",
												type : "text",
												width : 250,
												validate : {
													message : "객체명은 50자 이하로 작성해야 합니다",
													validator : "rangeLength",
													param : [ 1, 50 ]
												},
												//css:"text-truncate"
											},
											{
												name : "obj_desc",
												title : "객체 설명",
												type : "text",
												width : 400,
												validate : {
													message : "객체설명은 100자 이하로 작성해야 합니다",
													validator : "rangeLength",
													param : [ 1, 100 ]
												},
												//css:"text-truncate"
											},
											{
												name : "activ_yn",
												title : "활성화",
												align : "center",
												width : 70,
												visible:false
											},
											{
												type : "control",
												width : 80,
												itemTemplate : function(
														value, item) {
													var $result = $([]);

													$result = $result
															.add(this
																	._createEditButton(item));
													$result = $result
															.add('<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>');
//													$result = $result
//															.add(this
//																	._createDeleteButton(item));

													return $result;

												},

												//활성화가 n일때 편집 아이콘 투명 처리 ,동작 기능 제거 

												_createEditButton : function(
														item) {
													
													return this
													._createGridButton(
															this.editButtonClass,
															this.editButtonTooltip,
															function(
																	grid,
																	e) {
																grid
																		.editItem(item);
																e
																		.stopPropagation();

															});
//													if (item.activ_yn == 'N') {
//														return this
//																._createGridButton(
//																		this.editButtonClass,
//																		this.editButtonTooltip,
//																		function(
//																				grid,
//																				e) {
//																			grid
//																					.editItem(item);
//																			e
//																					.stopPropagation();
//
//																		});
//													} else {
//
//														return this
//																._createGridButton(
//																		this.editButtonClassCustom,
//																		this.editButtonTooltip,
//																		function(
//																				grid,
//																				e) {
//																		});
//
//													}
												
												},

//												_createDeleteButton : function(
//														item) {
//													if (item.activ_yn == 'N') {
//														return this
//																._createGridButton(
//																		this.deleteButtonClass,
//																		this.deleteButtonTooltip,
//																		function(
//																				grid,
//																				e) {
//																			grid
//																					.deleteItem(item);
//																			e
//																					.stopPropagation();
//
//																		});
//													} else {
//
//														return this
//																._createGridButton(
//																		this.deleteButtonClassCustom,
//																		this.deleteButtonTooltip,
//																		function(
//																				grid,
//																				e) {
//																		});
//
//													}
//												},

												editTemplate : function() {
													// 업데이트 버튼과 캔슬 에디트 버튼 사이에 여뱍을 추가 
													return this
															._createUpdateButton()
															.add(
																	'<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>')
															.add(
																	this
																			._createCancelEditButton());
												},
												headerTemplate : function() {
													return '<b>제목편집</b>';
												}
											} ],

									rowClick : function(args) {
										var table_name = args.item.table_name;

										console.log(table_name);

										var activ_yn = args.item.activ_yn;

										console.log(activ_yn);

										//로우를 클릭 했을 때 table_name,activ_yn을 html의 id =div1,div2 인 곳에 각각 저장 
										document.getElementById("div1").innerHTML = table_name;

										document.getElementById("div2").innerHTML = activ_yn;

										//그리드의 행 데이터를 클릭 했을 때 하이라이트 처리 
										var $row = this
												.rowByItem(args.item), selectedRow = $(
												"#jsGrid").find(
												'table tr.highlight');

										if (selectedRow.length) {
											selectedRow
													.toggleClass('highlight');
										}
										;

										$row.toggleClass("highlight");
									},

									//로우 더블 클릭 했을 때  객체정보 관리 그리드 그리고 모달 연다 
									rowDoubleClick : function(args) {
										var param = args.item

										var table_name = args.item.table_name;
										document.getElementById("div4").innerHTML = table_name;

										infoGrid(param);

										$('#myModal2').modal("show");
									}

								});

			});
});
	

	function active() {

		var param = new Object();

		param.table_name = $("#div1").text();

		param.activ_yn = $("#div2").text();

		console.log(param);

		$.ajax({
			type : "POST",
			url : OlapUrlConfig.AdminObjectActive,
			contentType : 'application/json',
			data : JSON.stringify(param),
			headers : {
				'X-CSRF-TOKEN' : $('#csrfvalue').val()
			},
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
			success : function(data) {

				if (data == 1) {
                    
					if(param.activ_yn == 'Y'){
					alert(param.table_name+" 객체가 비활성화 되었습니다.");
					
					}else{
						alert(param.table_name+" 객체가 활성화 되었습니다.");	
					}
					console.log(data);
				} else if (data == -2) {
					alert("객체명을 입력하세요");

					console.log(data);
				} else if (data == -3) {
					alert("해당 테이블을 더블클릭(객체 정보 관리)하여 <br> 최소 1개 컬럼이상을 등록해야 활성화할 수 있습니다.");

					console.log(data);
				} else if(data == -4){
	             	   alert("객체 정보 관리에서 기준일자를 설정하세요"); 
	                  	
	              	   console.log(data);      	 
	       	    } else if (data == -10) {
					alert("활성화시킬 객체를 선택하세요");

					console.log(data);
				} else {

					alert("활성화  실패");
					console.log(data);

				}

				$("#jsGrid").jsGrid("loadData");
				$("#div1").empty();
				$("#div2").empty();
			},
			error : function(jqXHR, textStatus, errorThrown){
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
					alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
				}
			}
		});
	}

	//마트에서 테이블,뷰 목록 불러오는 그리드  
	function addPopup() {
		
		set_object_name = "";
		
		$("#jsGridSelect")
				.jsGrid(
						{
							height : "500px",
							width : "100%",

							sorting : false,
							paging : false,
							autoload : true,
							loadMessage : "로딩중...",
							controller : {
								loadData : function(filter) {
									return $.ajax({
										type : "GET",
										url : OlapUrlConfig.AdminObjectSelect,
										headers : {
											'X-CSRF-TOKEN' : $('#csrfvalue')
													.val()
										},
										beforeSend: function(xhr) {
									        xhr.setRequestHeader("AJAX", true);
									     },
										contentType : 'application/json',
										data : filter,
										error : function(jqXHR, textStatus, errorThrown){
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
												alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
											}
										}
									});
								},

							},
							fields : [ {
								name : "OBJECT_NAME",
								title : "객체명",
								type : "text",
								width : 100
							}

							],

							rowClick : function(args) {
								//로우를 클릭 했을 때 object_name을 전역변수 set_object_name 에 저장
								 set_object_name = args.item.OBJECT_NAME;

								console.log(set_object_name);

						
								var $row = this.rowByItem(args.item), selectedRow = $(
										"#jsGridSelect").find(
										'table tr.highlight');

								if (selectedRow.length) {
									selectedRow.toggleClass('highlight');
								}
								;

								$row.toggleClass("highlight");
							}

						});

	}
	
	/**
	 *  '계산함수' valid Check함수
	 *  문자일 경우 value가 ''값만 허용
	 *  숫자일 경우 허용 
	 * @param value
	 * @param item
	 * @param param
	 * @returns
	 */
	function validCalcFunc(value, item, param){
		var rtnState = false;
		if(item !== undefined && item !== null && item.hasOwnProperty("DATA_TYPE")){
			if(item.DATA_TYPE ==="숫자"){
				rtnState = true;
			} else{
				if(value === "N"){
					rtnState = true;
				}
			}
		}
		return rtnState;
	}
	
	/**
	 *  '조회 기간 단위 설정' valid Check함수
	 *  기준일자에만 설정 허용 
	 */
	function validSearchPeriodUnit(value, item, param){
		var rtnState = false;
		if(item !== undefined && item !== null && item.hasOwnProperty("STAND_DATE")){
			if(item.STAND_DATE ==="Y" &&item.VIEW_DURATION_NUM !== "" &&value !== ""){
				rtnState = true;
			} else if(item.STAND_DATE ==="N"){
				if(value == ""&&item.VIEW_DURATION_NUM ==""){
					rtnState = true;
				}
				
			}else{
				
			}
		}
		return rtnState;
	}


	/**
	 *  '조회 기간 숫자 설정' valid Check함수
	 *  기준일자에만 설정 허용 
	 */
	 
	function validSearchPeriodNum(value, item, param){
		var rtnState = false;
		if(item !== undefined && item !== null && item.hasOwnProperty("STAND_DATE")){
			if(item.STAND_DATE ==="Y" &&item.VIEW_DURATION_UNIT !=="" && item.VIEW_DURATION_UNIT !== undefined && item.VIEW_DURATION_UNIT !== null &&value !== ""&& /^[0-9+]*$/.test(value)){
				rtnState = true;
			} else if(item.STAND_DATE ==="N"){
				if(value == ""&&item.VIEW_DURATION_UNIT ==""){
					rtnState = true;
				}
				
			}else{
				
			}
		}
		return rtnState;
	}
	
	
	/**  노출 여부 validation 
	 *  기준일자가 Y 일때 노출여부 N 불가  
	 * @param value
	 * @param item
	 * @param param
	 * @returns
	 */

	
	//객체정보 관리 그리드 그리는 함수 
	function infoGrid(param) {
		$("#jsGridInfo").jsGrid('destroy');
		//SELECT BOX 
		var calcFuntion = [ {
			id : "",
			name : ""
		}, {
			id : "SUM",
			name : "SUM"
		}, {
			id : "AVG",
			name : "AVG"
		}, {
			id : "MAX",
			name : "MAX"
		}, {
			id : "MIN",
			name : "MIN"
		} ];
		var dataType = [ {
			id : "문자",
			name : "문자"
		}, {
			id : "숫자",
			name : "숫자"
		} ];
		var standDate = [ {
			id : "N",
			name : "N"
		}, {
			id : "Y",
			name : "Y"
		} ];
		var searchPeriodUnit = [ {
			id : "",
			name : ""
		}, {
			id : "일",
			name : "일"
		}, {
			id : "개월",
			name : "개월"
		}, {
			id : "년",
			name : "년"
		} ];
		
 		var disply = [ {
			id : "N",
			name : "N"
		}, {
			id : "Y",
			name : "Y"
		} ];
 
 
        var calcFuntionYn = [ {
			id : "N",
			name : "N"
		}, {
			id : "Y",
			name : "Y"
		} ];
        
        
        
		//  편집 아이콘  이미지 투명하게  하기위한 CSS 클래스 선언
		jsGrid.ControlField.prototype.deleteButtonClassCustom = "jsgrid-delete-button-custom ";

		jsGrid.ControlField.prototype.editButtonClassCustom = "jsgrid-edit-button-custom ";
		$("#jsGridInfo")
				.jsGrid(
						{
							height : "600px",
							width : "100%",
                            
							editing : true,
							sorting : false,
							paging : false,
							autoload : true,
							loadMessage : "로딩중...",

							invalidNotify : function(args) {
								var messages = $.map(args.errors, function(
										error) {
									return error.message + '<br />';

								});
								
								alert(messages);
								
							},
							confirmDeleting : false,
							onItemDeleting : function(args) {
								console.log("삭제 하기전 확인");
								if (!args.item.deleteConfirmed) { // custom property for confirmation
									args.cancel = true; // cancel deleting
									confirm("객체 정보를 삭제하시면 연관된 객체 정보별 조회 조건이 삭제됩니다.<br>삭제 하시겠습니까?", function(
											result) { // bootbox js plugin for confirmation dialog
										if (result == true) {
											args.item.deleteConfirmed = true;
											$("#jsGridInfo").jsGrid(
													'deleteItem', args.item); //call deleting once more in callback
										}
									});
								}
							},

							controller : {
								loadData : function(filter) {

									var data = param;
									return $.ajax({
										type : "POST",
										url : OlapUrlConfig.AdminObjectInfoList,
										contentType : 'application/json',
										data : JSON.stringify(data),
										headers : {
											'X-CSRF-TOKEN' : $('#csrfvalue')
													.val()
										},
										beforeSend: function(xhr) {
									        xhr.setRequestHeader("AJAX", true);
									     },
										error : function(jqXHR, textStatus, errorThrown){
											  
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
												alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
											}
										}
									});
								},

								updateItem : function(item) {
									console.log(item);
									return $
											.ajax({
												type : "POST",
												url : OlapUrlConfig.AdminObjectInfoUpdate,
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
													console.log(data
															+ "info 업데이트");

													if (data == -5) {

														alert("기준일자는 하나만 선택할 수 있습니다");
														console.log(data);

													}

													$("#jsGridInfo").jsGrid(
															"loadData");

												},
												error : function(jqXHR, textStatus, errorThrown){
													  
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
														alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
													}
												}
											});
								},
								deleteItem : function(item) {
									console.log(item);
									return $
											.ajax({
												type : "POST",
												url : OlapUrlConfig.AdminObjectInfoDelete,
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
													console.log(data);

													if (data == 1) {

														
														console.log(data);

													} else {

														alert("삭제 실패");

													}

													// 그리드 reloading 
													$("#jsGridInfo").jsGrid(
															"loadData");

												},
												error : function(jqXHR, textStatus, errorThrown){
													  
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
														alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
													}
												}
											});
								}

							},

							fields : [
							    {
									name : "DISPLAY_YN",
									title : "사용자<br/>노출여부",
									type : "select",
									items : disply,
									valueField : "id",
									textField : "name",
									width : 40,
									css:'font-size-down',
									validate: {
										    validator: 	function validDisplay(value, item){
												var rtnState = true;
												if(item !== undefined && item !== null && item.hasOwnProperty("DISPLAY_YN")){
													if(item.STAND_DATE ==="Y"&&value == "N"){
														rtnState = false;
													} 
												}
												return rtnState;
											} ,
										    message: "기준 일자가 Y 일 때는 컬럼 노출 여부가 항상 Y 여야 합니다.",   
										}
								  },
									{
										name : "TABLE_NAME",
										title : "테이블",
										visible : false,
										width : 150,
										css:'font-size-down'
									},
									{
										name : "COLUMN_NAME",
										title : "컬럼(DataMart)",
										width : 100,
										css:'font-size-down'
									},
									{
										name : "COL_NAME",
										title : "컬럼",
										width : 100,
										css:'font-size-down',
										editTemplate : function(value, item) {

											if (item.COLUMN_NAME !== undefined) {
												value = item.COLUMN_NAME;

											}
											this._value = value;
											return this.itemTemplate(value,
													item);
										},
										editValue : function() {

											return this._value;
										},
									},
									{
										name : "OBJINFO_NAME",
										title : "객체정보명",
										type : "text",
										width : 100,
										css:'font-size-down',
										validate : {
											message : "객체정보명은 50자 이하로 작성해야 합니다",
											validator : "rangeLength",
											param : [ 1, 50 ]
										}
									},
									{
										name : "OBJINFO_DESC",
										title : "객체정보설명",
										type : "text",
										width : 150,
										css:'font-size-down',
										validate : {
											message : "객체정보설명은 100자 이하로 작성해야 합니다",
											validator : "rangeLength",
											param : [ 1, 100 ]
										}
									},
									
									{
										name : "CALC_FUNC_YN",
										title : "계산 함수 설정",
										type : "select",
										items : calcFuntionYn,
										valueField : "id",
										textField : "name",
										width : 40,
										css:'font-size-down',
										validate: {
										    validator: validCalcFunc, // built-in validator name or custom validation function
										    message: "계산함수는 형식이 숫자일 경우에만 허용됩니다.",                       // validation message or a function(value, item) returning validation message
										},
										visible:false
									},	
									{
										name : "PK_GUBUN",
										title : "PK 키",
										align : "center",
										width : 30,
										visible: false,
										css:"custom-inner-relative font-size-down",
										itemTemplate : function(value, item) {
											var elemId = item["TABLE_NAME"] +"_"+ item["COLUMN_NAME"]; 
											var rtnTemplate=$("<div>"),$input = $("<input>");
						        			var $label = $("<label>");
						        			var $span = $("<span>");
						        			$input.attr("id",elemId);
					        				$label.attr("for",elemId);
						        			//체크 박스 값이  true,false이기  때문에 디비에 저장된 Y,N 값을 변경
											if (value == "Y") {
												value =true;
											} else{
												value =false;												
											}
						        			$input.attr("type","checkbox").attr("checked", value || item.Checked);
						        			$input.attr("Disabled",true);
						        			$input.addClass("chbox");
						        			$span.addClass("custom-checkbox");
						        			$label.append($span);
						        			rtnTemplate.append($input);
						        			rtnTemplate.append($label);
						        			return rtnTemplate.html();
										},
									    editTemplate: function(value,item) {
								    		var elemId = item["TABLE_NAME"] +"_"+ item["COLUMN_NAME"]+"_edit"; 
											var rtnTemplate=$("<div>"),$input = $("<input>");
						        			var $label = $("<label>");
						        			var $span = $("<span>");
						        			$input.attr("id",elemId);
					        				$label.attr("for",elemId);
						        			//체크 박스 값이  true,false이기  때문에 디비에 저장된 Y,N 값을 변경
											if (value == "Y") {
												value =true;
											} else{
												value =false;												
											}
						        			$input.attr("type","checkbox").attr("checked", value || item.Checked);
						        			$input.addClass("chbox");
						        			$span.addClass("custom-checkbox");
						        			$input.on("change", function() {
						        				item.Checked = $(this).is(":checked");
						        				if(item.Checked){
						        					item.PK_GUBUN = "Y";	
						        				}else{
						        					item.PK_GUBUN = "N";
						        				}
						                    });
						        			$label.append($span);
						        			rtnTemplate.append($input);
						        			rtnTemplate.append($label);
						        			return rtnTemplate;
									    },
									},
									{
										name : "DATA_TYPE",
										title : "형식",
										type : "select",
										items : dataType,
										valueField : "id",
										textField : "name",
										width : 50,
										css:'font-size-down'
									},
//									{
//										name : "STAND_DATE",
//										title : "기준일자",
//										type : "select",
//										items : standDate,
//										valueField : "id",
//										textField : "name",
//										width : 40,
//										css:'font-size-down'
//									},
//									  
//									
//									
//									 {	name : "VIEW_DURATION_NUM",
//										title : "조회 기간 숫자",
//										type : "text",
//										
//										width : 40,
//										css:'font-size-down',
//										validate: {
//										    validator: validSearchPeriodNum ,
//										    message: "조회 기간 숫자(양수만 입력)와 단위를 설정해 주세요.기준일자 컬럼만 설정 가능합니다.",   
//										}
//									},
//									
//									
//									{	name : "VIEW_DURATION_UNIT",
//										title : "조회 기간 단위",
//										type : "select",
//										items : searchPeriodUnit,
//										valueField : "id",
//										textField : "name",
//										width : 50,
//										css:'font-size-down',
//										validate: {
//										    validator: validSearchPeriodUnit,                               // built-in validator name or custom validation function
//										    message: "기준일자가 아닐 경우 설정하지 마십시오.",   // validation message or a function(value, item) returning validation message
//										}
//									},	
									
									{   name : "SEQ",
										title : "순서",
										type : "number",
										align:"center",
										
										width : 40,
										css:'font-size-down',
										validate : {
											message : "0 이상 입력하십시오.",
											validator : "min",
											param :  0 
										},
										
									},		
									{
										name : "ACTIV_YN",
										title : "활성화",
										visible : false,
										width : 5,
										css:'font-size-down'
									},

									{
										type : "control",
										width : 50,
										css:'font-size-down',
										itemTemplate : function(value, item) {
											var $result = $([]);

											$result = $result.add(this
													._createEditButton(item));

											$result = $result
													.add('<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>');
											$result = $result.add(this
													._createDeleteButton(item));

											return $result;

										},
										_createDeleteButton : function(item) {
											
											return this
											._createGridButton(
													this.deleteButtonClass,
													this.deleteButtonTooltip,
													function(grid,
															e) {
														grid
																.deleteItem(item);
														e
																.stopPropagation();

													});
//											if (item.ACTIV_YN == 'N'
//													&& item.COL_NAME !== undefined) {
//
//												return this
//														._createGridButton(
//																this.deleteButtonClass,
//																this.deleteButtonTooltip,
//																function(grid,
//																		e) {
//																	grid
//																			.deleteItem(item);
//																	e
//																			.stopPropagation();
//
//																});
//
//											} else {
//
//												return this
//														._createGridButton(
//																this.deleteButtonClassCustom,
//																this.deleteButtonTooltip,
//																function(grid,
//																		e) {
//																});
//
//											}
										},

										_createEditButton : function(item) {
											return this
											._createGridButton(
													this.editButtonClass,
													this.editButtonTooltip,
													function(grid,
															e) {
														grid
																.editItem(item);
														e
																.stopPropagation();

													});
//											if (item.ACTIV_YN == 'N') {
//												return this
//														._createGridButton(
//																this.editButtonClass,
//																this.editButtonTooltip,
//																function(grid,
//																		e) {
//																	grid
//																			.editItem(item);
//																	e
//																			.stopPropagation();
//
//																});
//											} else {
//
//												return this
//														._createGridButton(
//																this.editButtonClassCustom,
//																this.editButtonTooltip,
//																function(grid,
//																		e) {
//																});
//
//											}
										},

										editTemplate : function() {
											// 업데이트 버튼과 캔슬 에디트 버튼 사이에 여뱍을 추가 
											return this._createUpdateButton().add('<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>').add(this._createCancelEditButton());
										},
										headerTemplate : function() {
											return '<b>편집 삭제 </b>';
										}
									} ],
							rowClick : function(args) {

								console.log(args.item);

							},

							//COLUMN_NAME 값이  undefined, 즉 존재하지 않는 데이터는  빨간색으로 표사해서 주의를 준다 
							rowClass : function(item, itemIndex) //item is the data in a row, index is the row number.
							{
								return item.COLUMN_NAME == undefined ? 'red'
										: '';
							}

						});
		

	}

	//마트의 테이블 ,뷰 목록을 repo의 객체관리 테이블로 저장
	function insert() {

		if(set_object_name == ""||set_object_name == null){
			
			return alert("겍체를 선택하십시오.");
			
		}
		$.ajax({
			type : "POST",
			url : OlapUrlConfig.AdminObjectInsert,
			contentType : 'application/json',
			data : set_object_name,
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
			headers : {
				'X-CSRF-TOKEN' : $('#csrfvalue').val()
			},
			success : function(data) {
				//console.log(data);
				alert("저장 되었습니다.");

				$("#jsGridSelect").jsGrid("loadData");
				$('#myModal').modal("hide");
				$("#jsGrid").jsGrid("loadData");
			},
			error : function(jqXHR, textStatus, errorThrown){
				  
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
					alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
				}
			}
		});
	}
	
