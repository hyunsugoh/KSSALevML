'use strict';
/******************************************************** 
파일명 : board.js 
설 명 : main page의 main Javascript
수정일	수정자	Version	Function 명
-------	--------	----------	--------------
2019.03.04	
2009.03.04	
 *********************************************************/
let dataViewConfig = {
		elemIdSet :{
			pagerId:"externalPager"
		}
}

let commonFunc = {
		chkValue : function(value){
			if(value !== undefined && value !== null){
				return true;
			}else {
				return false;
			}
		},
		chkIsStr : function(value){
			let defaultChk = this.chkValue(value);
			if(defaultChk && value !== ""){
				return true;
			}else{
				return false;
			}
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


let BoardDataList= {
		
		loadAction : function(elemId){ //elemid = BoardList
			$.ajax({
				type : "GET",
				url:OlapUrlConfig.boardList,
				contentType: "application/json;charset=UTF-8",
				 beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
				success : function(data, status, xhr){
					if(status === "success"){
						BoardDataList.onlyColumnGridSetting(data) //그려주기
						
					}else{
						// 실패
						alert("데이터 통신이 원활하지 않습니다.");
					}
					//BoardDataList.drawPager(data);
				},
				error : function(jqxXHR, textStatus, errorThrown){
					if (jqxXHR.status == 401) {
			            alert("인증에 실패 했습니다. 로그인 페이지로 이동합니다.",function(){
			            	window.location.replace(OlapUrlConfig.loginPage);
			            });
			             
			             
			         } else if (jqxXHR.status == 403) {
			            alert("세션이 만료가 되었습니다. 로그인 페이지로 이동합니다.",function(){
			            	window.location.replace(OlapUrlConfig.loginPage);
			            });
			              
			         }else{
			        	 alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
			        	 console.log(jqxXHR.responseText);
//							// console.log(textStatus);
//							// console.log(errorThrown);
			         }
					
				}
				
			});
		},
		onlyColumnGridSetting : function(_data){
			
			 $("#jsGrid").jsGrid({ //게시판 메뉴
			        height: "auto",
			        width: "100%",
			 
			        sorting: true,
			        paging: true,
			        pageSize: 20,
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
	
			        data: _data,
			 
			        fields: [
			            { name: "seqNum", type: "number", width: 15 ,title: "번호" ,align:"center", css:"text-truncate"},
			            { name: "boardTitle", type: "text", title: "제목" , css:"text-truncate" },
			            { name: "boardWriter", type: "text", width: 50, title: "작성자",align:"center", css:"text-truncate" },
			            { name: "createDt", type: "text", items: _data.createDt,width: 30, title: "날짜",align:"center", css:"text-truncate", itemTemplate: function(value, item){
							if(value == null){value = item["createDt"];}return value.substr(0,4) + "." +value.substr(4,2)+"."+value.substr(6,2);} 
			            },
			            { name: "boardViews", type: "text", title: "조회수", width: 15 ,align:"center", css:"text-truncate" }
			            ],
			        rowClick : function(args){
			        	var seqNum = args.item.seqNum;
			        	BoardDataList.viewFormAction(seqNum);
			        }
			    });
			 
			 $("#mainBoard").jsGrid({ //메인화면 게시판부분
			        height: "auto",
			        width: "100%",
			 
			        sorting: true,
			        paging: true,
			        pageSize:10,
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
	
			        data: _data,
			 
			        fields: [
			            { name: "seqNum", type: "number", width: 15 ,title: "번호", align:"center", css:"text-truncate"},
			            { name: "boardTitle", type: "text", title: "제목",css:"text-truncate" },
			            { name: "boardWriter", type: "text", width: 30, title: "작성자",align:"center", css:"text-truncate" },
			            { name: "createDt", type: "text", items: _data.createDt,width: 30, title: "날짜",align:"center", css:"text-truncate", itemTemplate: function(value, item){
							if(value == null){value = item["createDt"];}return value.substr(0,4) + "." +value.substr(4,2)+"."+value.substr(6,2);} 
			            }
			           
			           
			        ],
			        rowClick : function(args){
			        	var seqNum = args.item.seqNum;
			        	BoardDataList.viewFormAction(seqNum);
			        }
			    });
		},

		saveAction : function(insertdata){
			if(insertdata.title ===""){
				alert("저장할 제목을 입력하여 주십시오.");
				return;
			}
			if(insertdata.content ===""){
				alert("저장할 제목을 입력하여 주십시오.");
				return;
			}
			if(insertdata.content.length >4000){
				alert("저장 가능한 범위를 초과했습니다.(한글 최대 2000자)");
				return;
			}
			confirm({
				message:'<p class="text-center">저장하시겠습니까?</p>',
				title:'<h6>저장</h6>',
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
			},function(result){ //result(매개변수)에 confirm의 true or false 값이 담김.
				if(result){ // true실행 false 실행 x
					var loadingBar = new loading_bar2(); //loadingbar 선언. ->olapcommon.js 에 정보 존재.
					loadingBar.show();
					
				}
				$.ajax({
					type : "POST", //post 방식으로 전송
					url : OlapUrlConfig.insertBoard, //OlapUrlConfig.js 의 insertBoard 
					data : JSON.stringify(insertdata),  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
					contentType: "application/json;charset=UTF-8",
					/*beforeSend: function(xhr) {
						xhr.setRequestHeaderxhr.setRequestHeader("AJAX", true);
				     },*/
				    complete:function(){
						loadingBar.hide();	
					},
					success : function(data, status, xhr){
						if(status === "success"){
							location.href = "/user/api/data/board.do"; // 성공시 목록으로 이동
						}else{
							// 실패
							alert("게시물 등록이 등록되지 않았습니다..");
						}
					},
					error : function(jqxXHR, textStatus, errorThrown){
						commonFunc.ajaxFailAction(jqxXHR);
					}
				})
			});
		},
		
		viewFormAction : function(seqNum){
			location.href="/user/api/data/viewForm.do?seqNum="+seqNum; //controller
			//boardViewData.viewAction(seqNum);
		},
		updateFormAction : function(seqNum){
			location.href="/user/api/data/updateForm.do?seqNum="+seqNum; //controller
		},
		deleteFormAction : function(seqNum){
			confirm({
				message:'<p class="text-center">정보를 삭제하시겠습니까?</p>',
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
			},function(result){ //result(매개변수)에 confirm의 true or false 값이 담김.
				if(result){ // true실행 false 실행 x
					alert("삭제");
					location.href="/user/api/data/deleteForm.do?seqNum="+seqNum; //controller
					
				}
			});
			return false;
		}
			
		
		
}	




