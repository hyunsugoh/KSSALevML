<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>	
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>

<script>
$(document).ready(function(){
	

	HelpMsgAction.creathHelpBtn("manager", "helpIcon",OLAPAdminHelpMsg);
	$.ajax({
	    type: "GET",
	    url: OlapUrlConfig.ManagerList,
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, 
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
	    success:function(data){
	    	
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
	        	 errAlert(jqXHR.status, jqXHR.responseText);
	         }else{
				alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
			}
		}
	}).done(function () {
	    $("#jsGrid").jsGrid({
	        height: "417px",
	      	width:"100%",
	        editing: true,
	        sorting: false,
	        paging: false,
	        autoload: true,
	        loadMessage: "로딩중...",
	        confirmDeleting : false,
			onItemDeleting : function(args) {
				
				if (!args.item.deleteConfirmed) { // custom property for confirmation
					args.cancel = true; // cancel deleting
					
							confirm(
									"삭제 하시겠습니까?",
									function(
											result) { // bootbox js plugin for confirmation dialog
										if (result == true) {
											args.item.deleteConfirmed = true;
											$(
													"#jsGrid")
													.jsGrid(
															'deleteItem',
															args.item); //call deleting once more in callback
										}
									});
				}
			},
	        controller: {
	            loadData: function (filter) {
	                return $.ajax({
	                    type: "GET",
	                    url: OlapUrlConfig.ManagerList,
	                    data: filter,
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
	    			        	 errAlert(jqXHR.status, jqXHR.responseText);
	    			         }else{
	    						alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
	    					}
	    				}
	                });
	            }
	    ,updateItem : function(item) {
			
			return $
					.ajax({
						type : "POST",
						url : OlapUrlConfig.ManagerEnabledUpdate,
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
							
							if (data == 1) {
							   if(item.ENABLED == 1){
								alert(item.MANAGER_ID+"가 활성화 되었습니다.");
								
								}else{
								alert(item.MANAGER_ID+"가 비활성화 되었습니다.");
								
								}
							}else{
                                alert("업데이트 실패"); 
                              }

				             // 그리드 reloading 
				             $("#jsGrid").jsGrid("loadData");
							 $("#jsGridResultArea").addClass("custom-rel-none-div");
							 $("#jsGridip").hide();	
							 $("#descriptMsg").show();
							 
							 if(!$("#descript").hasClass("custom-decript-div")){
								 $("#descript").addClass("custom-decript-div")
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
					        	 errAlert(jqXHR.status, jqXHR.responseText);
					         }else{
								alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
							}
						}
					});
		},
        deleteItem: function (item) {
   	      
          return $.ajax({
             type: "POST",
             url: OlapUrlConfig.ManagerDelete,
             contentType: 'application/json',
             data: JSON.stringify(item),
             beforeSend: function(xhr) {
			        xhr.setRequestHeader("AJAX", true);
			     },
             headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
             dataType:"JSON", 
             success:function(data){
           	
             
           	if(data == 1){
           	  
           	  alert("삭제가 완료 되었습니다");
                      
           	
              }else{
           		
           		alert("삭제 실패"); 
           		
           		
           	}	 
                    
             // 그리드 reloading 
             $("#jsGrid").jsGrid("loadData");
			 $("#jsGridResultArea").addClass("custom-rel-none-div");
			 $("#jsGridip").hide();	
			 $("#descriptMsg").show();
			 if(!$("#descript").hasClass("custom-decript-div")){
				 $("#descript").addClass("custom-decript-div")
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
		        	 errAlert(jqXHR.status, jqXHR.responseText);
		         }else{
					alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
				}
			}
       });
   }
	        },
	        fields: [
	            {name: "MANAGER_ID", title: "관리자 ID" , width: 200},
	            {name: "ENABLED", title: "활성화" , width: 30,align : "center",	css:"custom-inner-relative",								
		            	itemTemplate : function(value, item) {
		            
						var rtnTemplate=$("<div>"),$input = $("<input>");
						
	        			var $label = $("<label>");
						
	        			var $span = $("<span>");
						
	        			$input.attr("id",item["MANAGER_ID"]);
	    				$label.attr("for",item["MANAGER_ID"]);
	        			//체크 박스 값이  true,false이기  때문에 디비에 저장된 1,0 값을 변경
						if (value == 1) {
							value =true;
						} else{
							value =false;												
						}
	        			$input.attr("type","checkbox").attr("checked", value || item.Checked);
	        			$input.attr("Disabled",true);
	        			$input.addClass("chbox");
	        			$span.addClass("custom-checkbox");
	        			$input.on("change", function() {
	        				item.Checked = $(this).is(":checked");
	        				if(item.Checked){
	        					item.ENABLED = 1;	
	        				}else{
	        					item.ENABLED = 0;
	        				}
	                    });
	        			$label.append($span);
	        			rtnTemplate.append($input);
	        			rtnTemplate.append($label);
	        			console.log($input);
	        			return rtnTemplate.html();
						},
						editTemplate: function(value,item) {     
				    		
							var rtnTemplate=$("<div>"),$input = $("<input>");
		        			var $label = $("<label>");
		        			var $span = $("<span>");
		        			$input.attr("id","chbox_"+item["MANAGER_ID"]);
	        				$label.attr("for","chbox_"+item["MANAGER_ID"]);
		        			//체크 박스 값이  true,false이기  때문에 디비에 저장된 1,0 값을 변경
							if (value == 1) {
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
		        					item.ENABLED = 1;	
		        				}else{
		        					item.ENABLED = 0;
		        				}
		                    });
		        			$label.append($span);
		        			rtnTemplate.append($input);
		        			rtnTemplate.append($label);
		        			console.log($input);
		        			return rtnTemplate;
					    },	
					},
					  {type: "control" ,   width: 70 ,
						itemTemplate : function(value, item) {
							var $result = $([]);

							$result = $result.add(this
									._createEditButton(item));

							$result = $result
									.add('<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>');
							$result = $result.add(this
									._createDeleteButton(item));

							return $result;

						},

						editTemplate : function() {
							// 업데이트 버튼과 캔슬 에디트 버튼 사이에 여백을 추가 
							return this
									._createUpdateButton()
									.add(
											'<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>')
									.add(
											this
													._createCancelEditButton());
						},
						headerTemplate : function() {
													return '<b>편집 삭제 </b>';
												}}
	           
	        ],
	        rowClick: function (args) {
	        	 var param = args.item;
	        	
	   
	        	 if($("#jsGridResultArea").hasClass("custom-rel-none-div")){
	        		 $("#jsGridResultArea").removeClass("custom-rel-none-div");
	        		 
	        			 $("#descriptMsg").hide();
	        			 if($("#descript").hasClass("custom-decript-div")){
							 $("#descript").removeClass("custom-decript-div")
						 }
	        		 
	        	 }
    			 $("#jsGridip").show();	        	 
// 	        	 ipGrid(param); // 2019.10.29 ip 로직 제거 
	        	  //로우 클릭 했을 때 하이라이트 처리
					var $row = this.rowByItem(args.item),
	             selectedRow = $("#jsGrid").find('table tr.highlight');

	         if (selectedRow.length) {
	             selectedRow.toggleClass('highlight');
	         };
	         $row.toggleClass("highlight");
	            },
	       });
	   });
	   
});
 
function ipGrid(param){
	  

    
    $("#jsGridip").jsGrid({
        height: "417px",
        width: "100%",
        inserting: true,
        editing: true,
        sorting: false,
        paging: false,
        autoload: true,
        loadMessage: "로딩중...",
        confirmDeleting: false,
        onItemDeleting: function (args) {
            
             if (!args.item.deleteConfirmed) {  // custom property for confirmation
                args.cancel = true; // cancel deleting
                confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
                    if(result == true){
                        args.item.deleteConfirmed = true;
                        $("#jsGridip").jsGrid('deleteItem', args.item); //call deleting once more in callback
                    }
                });
             }
        },		
        onItemInserting: function(args) {
        
       
            var ipRules = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/;
            if(args.item.MANAGER_IP === "") {
                args.cancel = true;
                alert("ip를 입력해 주세요");
            }else if(!ipRules.test(args.item.MANAGER_IP)) {
    		    args.cancel = true;
                alert("ip 입력범위는 0.0.0.0 에서 255.255.255.255까지 입니다");
    		
    	    }
      		 var gridData = $("#jsGridip").jsGrid("option", "data");
    		 
       	     for (i = 0; i < gridData.length; i++) {                                		 
                 if(args.item.MANAGER_IP == gridData[i].MANAGER_IP ){
                	 
                	 alert("중복된 ip입니다");
                	 return args.cancel = true;
                 }
             }
       	     
            

        },
        onItemUpdating: function(args) {
            
            
            var ipRules = /^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$/;
            if(args.item.MANAGER_IP === "") {
                args.cancel = true;
                alert("ip를 입력해 주세요");
            }else if(!ipRules.test(args.item.MANAGER_IP)) {
    		    args.cancel = true;
                alert("ip 입력범위는 0.0.0.0 에서 255.255.255.255까지 입니다");
    		
    	    }
      		 var gridData = $("#jsGridip").jsGrid("option", "data");
    		 
       	     for (i = 0; i < gridData.length; i++) {                                		 
                 if(args.item.MANAGER_IP == gridData[i].MANAGER_IP ){
                	 
                	 alert("중복된 ip입니다");
                	 return args.cancel = true;
                 }
             }
       	     
            

        },
        controller: {
            loadData: function (filter) {
                return $.ajax({
                    type: "POST",
                    url: OlapUrlConfig.ManagerIpList,
                    contentType: 'application/json',
                    data: JSON.stringify(param),
                    headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
                    beforeSend: function(xhr) {
				        xhr.setRequestHeader("AJAX", true);
				     },
                    dataType:"JSON"	,
                     success:function(data){
                       		
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
    			        	 errAlert(jqXHR.status, jqXHR.responseText);
    			         }else{
    						alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
    					}
    				}
                });
            },
			insertItem : function(item) {
				
			 var value = param.MANAGER_ID;
			 item.MANAGER_ID = value;
				return $
						.ajax({
							type : "POST",
							url : OlapUrlConfig.ManagerIpInsert,
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


								if (data == 1) {

									alert("입력 되었습니다");
								

								}else{
								    alert("입력 실패");
									
								}

								$("#jsGridip").jsGrid("loadData");

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
						        	 errAlert(jqXHR.status, jqXHR.responseText);
						         }else{
									alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
								}
							}
						});
			},			
			deleteItem : function(item) {
				return $
				.ajax({
					type : "POST",
					url : OlapUrlConfig.ManagerIpDelete,
					contentType : 'application/json',
					data : JSON.stringify(item),
					headers : {
						'X-CSRF-TOKEN' : $(
								'#csrfvalue').val()
					},
					dataType : "JSON",
					beforeSend: function(xhr) {
                        xhr.setRequestHeader("AJAX", true);
                      },
					success : function(data) {
		

						if (data >= 1) {

							alert("삭제 되었습니다");
						

						}

						$("#jsGridip").jsGrid("loadData");

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
				        	 errAlert(jqXHR.status, jqXHR.responseText);
				         }else{
							alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
						}
					}
				});
	     },
			updateItem : function(item) {
				return $
						.ajax({
							type : "POST",
							url : OlapUrlConfig.ManagerIpUpdate,
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
								
								if (data >= 1) {

									alert("수정 되었습니다");
									

								}

								$("#jsGridip").jsGrid("loadData");

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
						        	 errAlert(jqXHR.status, jqXHR.responseText);
						         }else{
									alert("서버와 통신에 실패하였습니다. 잠시후 다시 시도하여주십시오.");
								}
							}
						});
			},
         	            		            		                
        },
												        
        fields: [
        	{name: "MANAGER_ID", title: "관리자 ID" ,  width: 10 ,visible : false,insertTemplate : function() {
				value = param.MANAGER_ID;
			this._value = value;
			return this.itemTemplate(value
					);
		},
		insertValue : function() {

			return this._value;
		},								        		
        		},
        	{name: "MANAGER_IP", title: "연결 허용 IP" ,type : "text", width: 100},
        	{name: "STAND_MANAGER_IP", title: "업데이트 기준 IP", visible : false, width: 50},
            {type: "control" ,   width: 50 ,
				itemTemplate : function(value, item) {
					var $result = $([]);

					$result = $result.add(this
							._createEditButton(item));

					$result = $result
							.add('<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>');
					$result = $result.add(this
							._createDeleteButton(item));

					return $result;

				},

				editTemplate : function() {
					// 업데이트 버튼과 캔슬 에디트 버튼 사이에 여뱍을 추가 
					return this
							._createUpdateButton()
							.add(
									'<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>')
							.add(
									this
											._createCancelEditButton());
				},}
           
        ],
        rowClick: function (args) {
        	 var param = args.item;
        	  
            },

      });
};	


</script>

<style>
tr.highlight td.jsgrid-cell {
	background-color: #BBDEFB;
}

.jsgrid-header-row {
	text-align: center;
}

.jsgrid-delete-button-custom { background-position: 0 -80px; width: 16px; height: 
16px;;opacity: .2;}

.custom-rel-none-div{
border : 1px solid #d3d3d3;
}

.custom-decript-div {
	width:100%;
	height:300px;
}

</style>
<div class="col-md-9 col-lg-10 col-xl-10 col-10 ml-auto px-4">
	<div class="row">
		<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
		
                  <div class="col-12">
              <div class="row">
		<div class="col-6">
		<div class="h5">
		      <strong><i class="fas fa-users-cog"></i><span class="ml-1">관리자 관리</span></strong>  					
			</div>
		</div>	
	   
		<div class="col-6">
				<div class="d-flex justify-content-end">
					<div id="helpIcon" class="pt-0"></div>
				</div>
			</div>
		  </div>		
		</div>
	 <div class="col-12">

		<div class="row pt-5">
			 <div class="col-12 form-horizontal d-flex justify-content-end" style="margin-bottom: 20px;" >
				<form action="<c:url value='/admin/adminsignup.do'/>"  >
				  <button type="submit" class="btn btn-outline-primary" ><i class="fas fa-user-plus"></i>관리자 가입</button>
	            </form>
			</div>
			
			<div class="col-12">
				<div id="jsGrid"></div>
			</div>
<!-- 			<div id="jsGridResultArea" class="col-6 align-items-center justify-content-center custom-rel-none-div"> -->
<!-- 				<div class="d-flex align-items-center"> -->
<!-- 					<div id="jsGridip" ></div> -->
<!-- 					<div id="descript" class="d-flex align-items-center justify-content-center custom-decript-div"> -->
<!-- 						<div id="descriptMsg" class="badge badge-secondary custom-badge-font" style="float:right;">ip를 추가,수정 -->
<!-- 				하려면 관리자를 선택하여 주십시오.</div> -->
<!-- 					</div> -->
					
<!-- 				</div> -->
<!-- 			</div>	 -->
		
	</div>	
	</div>	
		
	</div>

</div>
