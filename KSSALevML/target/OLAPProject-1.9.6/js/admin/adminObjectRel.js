/**
 * adminObjectRel.js
 */


$(document).ready(function(){
	HelpMsgAction.creathHelpBtn("adminObjectRel","helpIcon",OLAPAdminHelpMsg);
	$.ajax({
	    type: "GET",
	    url: OlapUrlConfig.AdminObjectRelStand,
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, 
		beforeSend: function(xhr) {
	        xhr.setRequestHeader("AJAX", true);
	     },
	    success:function(data){
	    	console.log(data); 
	   },
		error : function(jqXHR, textStatus, errorThrown){
			loadIndicator.hide();
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
	}).done(function () {
	    $("#jsGrid").jsGrid({
	        height: "500px",
	      	width:"100%",
	        editing: false,
	        sorting: false,
	        paging: false,
	        autoload: true,
	        loadMessage: "로딩중...",
	        controller: {
	            loadData: function (filter) {
	                return $.ajax({
	                    type: "GET",
	                    url: OlapUrlConfig.AdminObjectRelStand,
	                    data: filter,
	                    beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
	    				error : function(jqXHR, textStatus, errorThrown){
	    					loadIndicator.hide();
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
	        fields: [
	            {name: "TABLE_NAME", title: "기준 테이블" , width: 130,css:"text-truncate"	},
	            {name: "OBJ_NAME", title: "기준 객체" , width: 170,css:"text-truncate"}
	           
	        ],
	        rowClick: function (args) {
	        	 var param = args.item;
	        	 console.log(param);
	        	 if($("#jsGridResultArea").hasClass("custom-rel-none-div")){
	        		 $("#jsGridResultArea").removeClass("custom-rel-none-div");
	        		 if($("#descriptMsg").is(":visible")){
	        			 $("#descriptMsg").hide();
	        		 }
	        	 }
	        	 //combo(param);
	        	 connGrid(param);
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


 
   
   
  
   function connGrid(param){
	  
       // 조인식이 없을 경우 스레기통 이미지 투명하게  하기위한 CSS 클래스 선언
	   jsGrid.ControlField.prototype.deleteButtonClassCustom = "jsgrid-delete-button-custom ";
	    $("#jsGridConn").jsGrid({
	        height: "500px",
	        width: "100%",
	        
	 
	        
	        inserting: false,
	        editing: true,
	        sorting: false,
	        paging: false,
	        autoload: true,
	        loadMessage: "로딩중...",
	        confirmDeleting: false,
            onItemDeleting: function (args) {
                console.log("삭제 하기전 확인");
                 if (!args.item.deleteConfirmed) {  // custom property for confirmation
                    args.cancel = true; // cancel deleting
                    confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
                        if(result == true){
                            args.item.deleteConfirmed = true;
                            $("#jsGridConn").jsGrid('deleteItem', args.item); //call deleting once more in callback
                        }
                    });
                 }
            },		
	      	
	       
	        controller: {
	            loadData: function (filter) {
	                return $.ajax({
	                    type: "POST",
	                    url: OlapUrlConfig.AdminObjectRelConn,
	                    contentType: 'application/json',
	                    data: JSON.stringify(param),
	                    beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
	                    headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
	                    dataType:"JSON"	,
	                     success:function(data){
	                    	 
	                    	   
	                       		console.log(data); 
	                       		
	                     
	                     },
	     				error : function(jqXHR, textStatus, errorThrown){
	    					loadIndicator.hide();
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
	            deleteItem: function (item) {
           	      console.log(item);
                  return $.ajax({
                     type: "POST",
                     url: OlapUrlConfig.AdminObjectRelJoinDelete,
                     contentType: 'application/json',
                     data: JSON.stringify(item),
                     beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
                     headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
                     dataType:"JSON", 
                     success:function(data){
                   	console.log(data); 
                     
                   	if(data == 1){
                   	  
                   	 
	                           console.log(data); 
                   	
                      }else{
                   		
                   		alert("삭제 실패"); 
                   		
                   		
                   	}	 
                            
                     // 그리드 reloading 
                     $("#jsGridConn").jsGrid("loadData");
                     
                     
                  },
				error : function(jqXHR, textStatus, errorThrown){
					loadIndicator.hide();
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
	
					
						
	        
	        fields: [
	        	{name: "STAND", title: "선택된 테이블" , visible: false, width: 150},
	        	{name: "TABLE_NAME", title: "연결  테이블" , width: 80 ,css:"text-truncate"},
	        	{name: "CONN_TABLE", title: "연결", align: "center",   width: 20, 
	        		
                 itemTemplate: function(value, item) {
	                    
	            		
	                    if(item.TABLE_NAME == item.CONN_TABLE){
	                    	
	                    	return $("<i>").attr("class", "fas fa-check").attr("style", "color:#20c997");
	                    }else{
	                    	
	                    	return value;
							
	                    }            	                    
	                
	            	}
	   },
	        
	            {type: "control" ,   width: 30,     
			            		
			      
                  headerTemplate: function() {
        	           return '<b>편집 </b>';
        	       },
        	       
      			 itemTemplate: function(value, item) {
                     var $result = $([]);
                    
                   
                         $result = $result.add(this._createEditButton(item));
                         $result = $result.add('<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>');
                         $result = $result.add(this._createDeleteButton(item));                   
                        
                     
         
                     return $result;
   
                 },
        	       
        	       // 객체 관계 관리에서 edit 버튼을 누르면 팝업창 실행되게 커스터마이징
        	    _createEditButton: function(item) {
        	       return this._createGridButton(this.editButtonClass, 
        	              this.editButtonTooltip, function(grid, e) {
        	    	        
        	    	        console.log(item);
        	    	        joinGrid(item);
        	    	        $('#myModalJoin').modal("show");
        	                e.stopPropagation();
        	            });
        	        },
        	     // 조인식이 설정된 행은 그대로  조인식이 없는 행은  쓰레기통 작동 메소드 제외시키고 커스터 마이징한 CSS 적용 (투명 처리)
        		_createDeleteButton: function(item) {
        			if(item.CONN_TABLE == item.TABLE_NAME){
        				 return this._createGridButton(this.deleteButtonClass, 
            	                   this.deleteButtonTooltip, function(grid, e) {
            	                     grid.deleteItem(item);
            	                     e.stopPropagation();
          	                    
          	               });
  	                }else{
  					
          	                     
          	             return this._createGridButton(this.deleteButtonClassCustom, 
                  	               this.deleteButtonTooltip, function(grid, e) {
          	              });
  			
  			            }	
        	        },
	            }
	           
	        ],
	        rowClick: function (args) {
	        	 var param = args.item;
	        	 console.log(param); 
	            },

	      });
   };	
   
   //조인식 편집 
   function joinGrid(item){
	    
	    $("#jsGridJoin").jsGrid({
	        height: "250px",
	        width: "100%",
	        	        
	        inserting: false,
	        editing: true,
	        sorting: false,
	        paging: false,
	        autoload: true,
	        loadMessage: "로딩중...",
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
	      
	       
	        controller: {
	            loadData: function (filter) {
	                return $.ajax({
	                    type: "POST",
	                    url: OlapUrlConfig.AdminObjectRelJoin,
	                    contentType: 'application/json',
	                    data: JSON.stringify(item),
	                    beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
	                    headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
	                    dataType:"JSON"	,
	                     success:function(data){
	                    	 
	                    	   
	                       		console.log(data); 
	                       		
	                     
	                     },
	     				error : function(jqXHR, textStatus, errorThrown){
	    					loadIndicator.hide();
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
	            updateItem: function(item) {
                    return $.ajax({
                        type: "POST",
                        url: OlapUrlConfig.AdminObjectRelJoinUpdate,
                        contentType: 'application/json',
                        data: JSON.stringify(item),
                        headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
                        dataType:"JSON", 
                        beforeSend: function(xhr) {
					        xhr.setRequestHeader("AJAX", true);
					     },
                        success:function(data){
                        	console.log(data+"조인관계 업데이트"); 
                        	if(data == 1){
                           	  
                           	  alert("저장 되었습니다");
      	                           console.log(data); 
                           	
                              }else{
                           		
                           		alert("저장 실패"); 
                           		
                           		
                           	}	                       	                                        	
                        
                          $("#jsGridJoin").jsGrid("loadData");
                          $("#jsGridConn").jsGrid("loadData");
                          $('#myModalJoin').modal("hide");
                          
                       },
       				error : function(jqXHR, textStatus, errorThrown){
    					loadIndicator.hide();
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
	
									        
	        fields: [
	        	{name: "STD_TABLE", title: "기준 테이블" , width: 150},
	        	
	        	{name: "CONN_TABLE", title: "연결 테이블",  width: 150},
				{name: "JOIN_EXPR", title: "조인식" ,  type: "textarea", width: 300,
					validate : {
						message : "조인식은 300자 이하로 작성해야 합니다",
						validator : "rangeLength",
						param : [ 1, 300 ]
					} , 
	        		//편집 버튼 눌렀을때 textarea크기 고정시키기 위해 커스터마이징
	        	    editTemplate: function(value) {
	                    if(!this.editing)
	                        return this.itemTemplate.apply(this, arguments);

	                    var $result = this.editControl = this._createTextArea();
	                    $result.val(value);
	                    return $result;
	                },
	                _createTextArea: function() {
	                    return $("<textarea>").attr("style","height: 130px;");
	                }
	        		},
	        
	            {type: "control" ,   width: 80,     
			            		
			      
                 headerTemplate: function() {
       	           return '<b>편집 </b>';
       	       },
               itemTemplate: function(value, item) {
                   var $result = $([]);
                  		                      
                       $result = $result.add(this._createEditButton(item));
                     		                        		            
                   return $result;
 
               },
               editTemplate: function() {
               	// 업데이트 버튼과 캔슬 에디트 버튼 사이에 여뱍을 추가 
   	            return this._createUpdateButton().add('<i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</i>').add(this._createCancelEditButton());
   	        },
       	               	
	            }
	           
	        ],
	        rowClick: function (args) {

	        	 var param = args.item;
	        	 console.log(param); 
	     
	  
	            },

	      });

  };	
   