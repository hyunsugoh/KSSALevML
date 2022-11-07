/**
* batchData.js
*/
/*$(document).ready(function(){	
}
*/
$(document).ready(function(){
	batchAction.ListAction("batchStart!"); //status 실행	
		
	$.ajax({
		type : "GET",
		url : OlapUrlConfig.batchDataList,
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
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
	.done(function(){
		$("#step_jsGrid").jsGrid(
				{
					height : "auto",
					width : "100%",

					editing : false,
					sorting : false,
					paging: true,
					pageSize : 20,
					pageButtonCount: 5,
			        pagerContainer: "#step_externalPager",
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
					autoload : true,
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
				
					controller : {
						loadData : function(filter) {
							return $
									.ajax({
										type : "GET",
										url : OlapUrlConfig.batchDataList,
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
									})
									/*.done(function(_result){
										console.log("_result11",_result);
										batchAction.batchListData(_result) //마지막 시간값 구하기.
									});*/
						},
					},
					fields : [
							{
								name : "step_exe_id",
								title : "번호",
								align:"center",
								width : 40,
								css:"font-size-down"
							},
							{
								name : "version",
								title : "버전",
								align:"center",
								width : 40,
								css:"font-size-down"
							},
							{
								name : "step_name",
								title : "단계명",
								align:"center",
								width : 150,
								css:"font-size-down"
							},
							{
								name : "start_time",
								title : "시작시간",
								align:"center",
								width : 150,
								css:"font-size-down"
							},
							{
								name : "last_updated",
								title : "마지막업데이트시간",
								align:"center",
								width : 150,
								css:"font-size-down"
							},
							{
								name : "status",
								title : "상태",
								align:"center",
								width : 90,
								css:"font-size-down"
							},
							{
								name : "commit_count",
								title : "커밋 한 수",
								align:"center",
								width : 50,
								css:"font-size-down",
								visible: false
							},
							{
								name : "read_count",
								title : "읽은항목 수",
								align:"center",
								width : 70,
								css:"font-size-down"
							},
							{
								name : "filter_count",
								title : "필터링항목 수",
								align:"center",
								width : 70,
								css:"font-size-down",
								visible: false
								
							},
							{
								name : "write_count",
								title : "반영항목 수",
								align:"center",
								width : 60,
								css:"font-size-down"
								
							},
							{
								name : "read_skip_count",
								title : "읽기건너띈항목 수",
								align:"center",
								width : 80,
								css:"font-size-down",
								visible: false
								
							},
							{
								name : "write_skip_count",
								title : "쓰기건너띈 항목 수",
								align:"center",
								width : 80,
								css:"font-size-down",
								visible: false
								
							},
							{
								name : "process_skip_count",
								title : "처리건너띈 항목 수",
								align:"center",
								width : 80,
								css:"font-size-down",
								visible: false
								
							},
							{
								name : "rollback_count",
								title : "롤백 수",
								align:"center",
								width : 50,
								css:"font-size-down",
								visible: false
								
							},
							{
								name : "cnv1_count",
								title : "전체 수",
								align:"center",
								width : 50,
								css:"font-size-down",
								visible: false
							},
							{
								name : "exit_code",
								title : "실행종료코드",
								align:"center",
								width : 100,
								css:"font-size-down"
							},
							/*{
								name : "exit_message",
								title : "작업종료방법",
								width : 50,
								css:"font-size-down"
							},*/
						],
						options :{
							width: "100%",
							
						}
				});
	})//step	
	
	
	
	

	
	//스케줄 정보
	$.ajax({
	type : "GET",
	//url : "http://localhost:9090/ksm/scheduledtasks",
	url : OlapUrlConfig.batchSchedule,
	headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
	beforeSend: function(xhr) {
        xhr.setRequestHeader("AJAX", true);
     }, 
 	success : function(data) {
// 		console.log("스케줄정보api",data);
 		$("batchTrue").show();
 		$("batchFalse").hide();
 		batchinfo(data);

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
        	 $("#batchFalse").show();
        	 $("#batchstatus").text("현재 배치프로그램이 실행되지 않습니다.");
        	 $("#batchschedule").text("배치프로그램 정보를 불러올 수 없습니다.");
         }else{
			alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
		}
	}
})
	
	
	
});//document ready





let batchAction = { 
		
		startAction : function(startDate,endDate){
			confirm({
				message:'<p class="text-center">배치를 시작하시겠습니까?</p>',
				title:'<h6>수동시작</h6>',
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
					$.ajax({
						type : "GET",
						url : OlapUrlConfig.batchStart ,
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
					    data : {
						    	startDate : startDate,
						    	endDate : endDate
						},
					 	success : function(data) {
					 		batchAction.ListAction(startDate,endDate);
							alert(data.message);

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
					        	 $("#batchstatus").text("현재 배치프로그램이 실행되지 않았습니다.");
					        	 errAlert(jqXHR.status, jqXHR.responseText)
					         }else{
					        	 $("#batchstatus").text("현재 배치프로그램이 실행되지 않았습니다.");
								alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
							}
						}
					})
					
				}
			});
			return false;
		},
		
		ListAction : function(startDate,endDate){ //status
		$.ajax({
			type : "GET",
			//url : "http://localhost:9090/batch/status",
			url : OlapUrlConfig.batchStatus ,
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			beforeSend: function(xhr) {
		        xhr.setRequestHeader("AJAX", true);
		     },
		    data : {
			    	startDate : startDate,
			    	endDate : endDate
			 },
		 	success : function(data) {
		 		$("#batchTrue").show();
		 		$("#batchFalse").hide();
		 	//console.log(data);
			// Grid 그리기
	 		let gridData = [];
		 	if(data.currentIsPlay ==true){
		 		for(let i = 0;i<data.status.length;i++){
		 			gridData.push({
		 				jobId : data.status[i]["jobId"],
		 				jobName : data.status[i]["jobName"],
		 				version : data.status[i]["version"],
		 				instanceId : data.status[i]["instanceId"],
		 				startTime : data.status[i]["startTime"],
		 				endTime : data.status[i]["endTime"],
		 				status : data.status[i]["status"]
		 			});
		 		}
		 	}
			$("#batchstatus").jsGrid({
	 			height : "auto",
				width : "100%",

				editing : false,
				sorting : false,
				paging: true,
				pageSize : 3,
				pageButtonCount: 2,
		        pagerContainer: "#notComplate_externalPager",
		        pagerFormat: ' {first} {prev} {pages} {next} {last} &nbsp;&nbsp;  &nbsp;&nbsp; 전체 {pageCount} 페이지 중 현재 {pageIndex} 페이지',
		        pagePrevText: "이전",
		        pageNextText: "다음",
		        pageFirstText: "처음",
		        pageLastText: "마지막",
		        pageNavigatorNextText: "&#8230;",
		        pageNavigatorPrevText: "&#8230;",
				noDataContent: "이력이 없습니다.",
				loadMessage: "데이터를 불러오는 중입니다...",
				pagerContainerClass: "custom-jsgrid-pager-container",
		        pagerClass: "custom-jsgrid-pager",
		        pagerNavButtonClass: "custom-jsgrid-pager-nav-button",
		        pagerNavButtonInactiveClass: "custom-jsgrid-pager-nav-inactive-button",
		        pageClass: "custom-jsgrid-pager-page",
		        currentPageClass: "custom-jsgrid-pager-current-page",
		        //fields : fields,
		        data : gridData,
		        fields :[{
					name : "jobId",
					title : "jobId",
					align:"center",
					width : 50,
					css:"font-size-down"
				},{
					name : "jobName",
					title : "이름",
					align:"center",
					width : 150,
					css:"font-size-down"
				},{
					name : "version",
					title : "Version.",
					align:"center",
					width : 50,
					css:"font-size-down"
				},{
					name : "instanceId",
					title : "instanceId",
					align:"center",
					width : 60,
					css:"font-size-down"
				},{
					name : "startTime",
					title : "구동시간",
					align:"center",
					width : 120,
					css:"font-size-down"
				},{
					name : "endTime",
					title : "종료시간",
					align:"center",
					width : 120,
					css:"font-size-down"
				},{
					name : "status",
					title : "상태",
					align:"center",
					width : 120,
					css:"font-size-down"
				}]
	 		});
			
			if(gridData.length === 0){
				$("#incomplateBtn").attr("disabled", true);
			}

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
		        	 $("#batchFalse").show();
		        	 $("#batchstatus").text("현재 배치프로그램이 실행되지 않습니다.");
		        	 $("#batchschedule").text("배치프로그램 정보를 불러올 수 없습니다.");
		        	 $("#incomplateBtn").attr("disabled", true);
		        	
		         }else{
					alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
				}
			}
		})
		},
		
}//batchAction

function batchinfo(data){

	var batcharray = data.cron[0].expression;
	var cornSplit = batcharray.split(" ");
	for(var i in cornSplit){
		//console.log(cornSplit[i]);
	}
	var cornSplit0 = cornSplit[0]; //초
	var cornSplit1 = cornSplit[1]; //분
	var cornSplit2 = cornSplit[2]; //시
	var cornSplit3 = cornSplit[3]; //일
	var cornSplit4 = cornSplit[4]; //월
	var cornSplit5 = cornSplit[5]; //요일 , 연도는 생략.
	
	//초
	if(cornSplit0 >= 0 || cornSplit0 <= 59){
		//console.log(cornSplit0 + "초");
		cornSplit0 = cornSplit0 + "초";
		$("#sec").text(cornSplit0)
	}else if(cornSplit0 === "*"){
		cornSplit0 = "*"
	}else{
		return 0;
	}
	
	//분
	if(cornSplit1 >= 0 || cornSplit1 <= 59){
		//console.log(cornSplit1 + "분");
		cornSplit1 = cornSplit1 + "분";
		$("#min").text(cornSplit1)
	}else if(cornSplit1 === "*"){
		cornSplit1 ="*"
	}else{
		return 0;
	}
	

	//시
	if(cornSplit2 >= 0 || cornSplit2 <= 11){
		//console.log("오전" +cornSplit2 + "시");
		cornSplit2 = cornSplit2 + "시";
		$("#time").text(cornSplit2);
	}else if(cornSplit2 >= 12 && cornSplit2 <= 23){
		//console.log("오후" +cornSplit2 + "시");
		cornSplit2 = cornSplit2 + "시";
		$("#time").text(cornSplit2);
	}else {
		//console.log(cornSplit2);
	}
	
	//일
	if(cornSplit3 >= 1 || cornSplit3 <= 31){
		cornSplit3 = cornSplit3 + "일";
		$("#day").text(cornSplit3)
	}else if(cornSplit3 === "*"){
		//console.log("*");
	}else{
		return 0;
	}
	
	//월
	if(cornSplit4 == 1 || cornSplit4 == "JAN"){
		cornSplit4 = 1;
		//console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 2 || cornSplit4 == "FEB"){
		cornSplit4 = 2;
		//console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 3 || cornSplit4 == "MAR"){
		cornSplit4 = 3;
		//console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 4 || cornSplit4 == "APR"){
		cornSplit4 = 4;
		//console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 5 || cornSplit4 == "MAY"){
		cornSplit4 = 5;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 6 || cornSplit4 == "JUN"){
		cornSplit4 = 6;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 7 || cornSplit4 == "JUL"){
		cornSplit4 = 7;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 8 || cornSplit4 == "AUG"){
		cornSplit4 = 8;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 9 || cornSplit4 == "SEP"){
		cornSplit4 = 9;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 10 || cornSplit4 == "OCT"){
		cornSplit4 = 10;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 11 || cornSplit4 == "NOV"){
		cornSplit4 = 11;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 == 12 || cornSplit4 == "DEC"){
		cornSplit4 = 12;
	//	console.log(cornSplit4 + "월");
		$("#month").text(cornSplit4 + "월")
	}else if(cornSplit4 =="*" ){
		cornSplit4 = cornSplit4;
	} 
	
	//요일
	if(cornSplit5 == 1 || cornSplit5 == "SUN" ){
		cornSplit5 = "일요일";
		$("#week").text(cornSplit5);
	//	console.log(cornSplit5);
	}else if(cornSplit5 == 2 || cornSplit5 == "MON" ){
		cornSplit5 = "월요일";
		$("#week").text(cornSplit5);
	//	console.log(cornSplit5);
	}else if(cornSplit5 == 3 || cornSplit5 == "TUE" ){
		cornSplit5 = "화요일";
		$("#week").text(cornSplit5);
	//	console.log(cornSplit5);
	}else if(cornSplit5 == 4 || cornSplit5 == "WED" ){
		cornSplit5 = "수요일";
		$("#week").text(cornSplit5);
	//	console.log(cornSplit5);
	}else if(cornSplit5 == 5 || cornSplit5 == "THU" ){
		cornSplit5 = "목요일";
		$("#week").text(cornSplit5);
	//	console.log(cornSplit5);
	}else if(cornSplit5 == 6 || cornSplit5 == "FRI" ){
		cornSplit5 = "금요일";
		$("#week").text(cornSplit5);
	//	console.log(cornSplit5);
	}else if(cornSplit5 == 7 || cornSplit5 == "SAT" ){
		cornSplit5 = "토요일";
		$("#week").text(cornSplit5);
	//	console.log(cornSplit5);
	}else if(cornSplit5 == "*"){
		cornSplit5 = "*";
	}
}