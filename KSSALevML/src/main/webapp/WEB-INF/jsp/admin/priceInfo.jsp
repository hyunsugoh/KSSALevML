<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<!-- Date -->
<%
	Date nowTime = new Date();
	SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link rel="shortcut icon" href="<c:url value='/images/common/ci/ksm_favi.ico'/>">

<meta charset="utf-8">
<title></title>
<script src="<c:url value='/js/common/jui/core/jui-core.js'/>"></script>
<script src="<c:url value='/js/common/jui/ui/jui-ui.js'/>"></script>
<script src="<c:url value='/js/common/jui/grid/jui-grid.js'/>"></script>
<!-- 이미지 뷰어 -->
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/js/common/imgViewer/viewer.css'/>">
<script src="<c:url value='/js/common/jquery/jquery-ui.min.js'/>"></script> 
<script src="<c:url value='/js/common/imgViewer/viewer.min.js'/>"></script> 
<link rel="stylesheet" type='text/css' href="<c:url value='/css/common/jquery-ui/jquery-ui.css'/>">
<!-- 뷰어 -->
<script src="<c:url value='/js/common/pdfobject/pdfobject.min.js'/>"></script>
<script>
	var fGrpId = "";
	var fSealData;
	var fSheetData;
	$(document).ready(function(){
		grpGridInit();
		$("#jsGrid_grp").jsGrid("loadData");
		sealGridInit();
		sheetGridInit();
		//관리자가 아닌 경우 관리 컬럼 hide
		if("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]"){
			$("#jsGrid_grp").jsGrid("fieldOption", "MNG_YN", "visible", true);
			$("#jsGrid_seal").jsGrid("fieldOption", "MNG_YN", "visible", true);
			$("#jsGrid_sheet").jsGrid("fieldOption", "MNG_YN", "visible", true);
		}else{
			$("#jsGrid_grp").jsGrid("fieldOption", "MNG_YN", "visible", false);
			$("#jsGrid_seal").jsGrid("fieldOption", "MNG_YN", "visible", false);
			$("#jsGrid_sheet").jsGrid("fieldOption", "MNG_YN", "visible", false);
		}
		
		$("#btn_regGrp").click(function(){
			clearGrpPop();
			//button hide & show
			$('#editGrp').css("display", "none");
			$('#delGrp').css("display", "none");
			$('#savGrp').css("display", "block");
			//popup show
			$('#grpPop').modal("show");
			$('#grpPop').css("z-index","1800");
		});
		
		$("#btn_regSeal").click(function(){
			clearSealPop();
			if(isEmpty(fGrpId)){
				alert("등록하실 그룹정보를 선택해 주세요");
				return;
			};
			$("#sealType").attr("readonly",false);
			//button hide & show
			$('#delSeal').css("display", "none");
			$('#savSeal').css("display", "block");
			//popup show
			$('#sealPop').modal("show");
			$('#sealPop').css("z-index","1800");
		});
		
		$("#btn_regSheet").click(function(){
			clearSheetPop();
			if(isEmpty(fGrpId)){
				alert("등록하실 그룹정보를 선택해 주세요");
				return;
			};
			$("#sheetNo").attr("readonly",false);
			//button hide & show
			$('#editSheet').css("display", "none");
			$('#delSheet').css("display", "none");
			$('#savSheet').css("display", "block");
			$('#divFileOrgNm').css("display", "none");
			$('#divFileSize').css("display", "none");
			//popup show
			$('#sheetPop').modal("show");
			$('#sheetPop').css("z-index","1800");
		});
		
		<%-- 저장 --%>
		$("#savGrp").click(function(){
			if(isEmpty($("#grpNm").val())){
				alert("그룹명은 필수 입력값 입니다.");
				return;
			};
			$.ajax({
				type:"POST",
				url:"<c:url value='/admin/savGrp.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					GRP_ID : uuidv4()
					,GRP_NM : $("#grpNm").val()
					,RMK : $("#rmk").val()
				})
			}).done(function(result){
				alert("저장 되었습니다.");
				$('#grpPop').modal("hide");
				$("#jsGrid_grp").jsGrid("loadData");
			})
		});
		
		$("#savSeal").click(function(){
			var sealType = $("#sealType").val();
			if(isEmpty(sealType)){
				alert("Seal Type은 필수 입력값 입니다.");
				return;
			};
			//중복 체크
			for(var i=0; i<fSealData.length; i++){
				if(fSealData[i].SEAL_TYPE == sealType){
					alert("같은 Seal Type이 이미 존재합니다.");
					return;
				};
			};
			$.ajax({
				type:"POST",
				url:"<c:url value='/admin/savSeal.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					GRP_ID : $("#sealGrpId").val()
					,SEAL_TYPE : $("#sealType").val()
				})
			}).done(function(result){
				alert("저장 되었습니다.");
				$('#sealPop').modal("hide");
				$("#jsGrid_seal").jsGrid("loadData");
			})
		});
		
		$("#savSheet").click(function(){
			var formData = new FormData();
			var sheetNo = $("#sheetNo").val();
			if(isEmpty(sheetNo)){
				alert("Sheet No 은 필수 입력값 입니다.");
				return;
			};
			//중복 체크
			for(var i=0; i<fSheetData.length; i++){
				if(fSheetData[i].SHEET_NO == sheetNo){
					alert("같은 Sheet No가 이미 존재합니다.");
					return;
				}
			};
			formData.append("GRP_ID", $("#sheetGrpId").val());
			formData.append("SHEET_NO", $("#sheetNo").val());
			if($('#input_files').prop('files').length==0){
				formData.append("NEW_YN", "Y");
				formData.append("FILE_PATH", "");
				formData.append("FILE_NAME", "");
				$.ajax({
 			       	url: "<c:url value='/admin/noFile.do'/>",
 			    	type: 'POST',
 			        enctype: 'multipart/form-data',
 			        async: true,
 			        data: formData,
 			        processData: false,
 			        contentType: false,
 			        success: function (result) {
 			        	alert("저장 되었습니다.");
 			        	$('#sheetPop').modal("hide");
 			        	$("#jsGrid_sheet").jsGrid("loadData");
 			        }
 			    });
			}else{
				formData.append("files", $('#input_files').prop('files')[0]);
				$.ajax({
 			       	url: "<c:url value='/admin/savSheet.do'/>",
 			    	type: 'POST',
 			        enctype: 'multipart/form-data',
 			        async: true,
 			        data: formData,
 			        processData: false,
 			        contentType: false,
 			        success: function (result) {
 			        	alert("저장 되었습니다.");
 			        	$('#sheetPop').modal("hide");
 			        	$("#jsGrid_sheet").jsGrid("loadData");
 			        }
 			    });
			}
		});
		
		<%-- 수정 --%>
		$("#editGrp").click(function(){
			if(isEmpty($("#grpNm").val())){
				alert("그룹명은 필수 입력값 입니다.");
				return;
			};
			$.ajax({
				type:"POST",
				url:"<c:url value='/admin/editGrp.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					GRP_ID : $("#sheetGrpId").val()
					,GRP_NM : $("#grpNm").val()
					,RMK : $("#rmk").val()
				})
			}).done(function(result){
				alert("저장 되었습니다.");
				$('#grpPop').modal("hide");
				$("#jsGrid_grp").jsGrid("loadData");
			})
		});
		
		$("#editSheet").click(function(){
			var formData = new FormData();
			if(isEmpty($("#sheetNo").val())){
				alert("Sheet No 은 필수 입력값 입니다.");
				return;
			};
			formData.append("GRP_ID",  $("#sheetGrpId").val());
			formData.append("SHEET_NO", $("#sheetNo").val());
			formData.append("FILE_PATH", $("#filePath").val());
			formData.append("FILE_NAME", $("#fileNm").val());
			if($('#input_files').prop('files').length==0){
				formData.append("NEW_YN", "N");
				$.ajax({
 			       	url: "<c:url value='/admin/noFile.do'/>",
 			    	type: 'POST',
 			        enctype: 'multipart/form-data',
 			        async: true,
 			        data: formData,
 			        processData: false,
 			        contentType: false,
 			        success: function (result) {
 			        	alert("저장 되었습니다.");
 			        	$('#sheetPop').modal("hide");
 			        	$("#jsGrid_sheet").jsGrid("loadData");
 			        }
 			    });
			}else{
				formData.append("files", $('#input_files').prop('files')[0]);
				$.ajax({
 			       	url: "<c:url value='/admin/editSheet.do'/>",
 			    	type: 'POST',
 			        enctype: 'multipart/form-data',
 			        async: true,
 			        data: formData,
 			        processData: false,
 			        contentType: false,
 			        success: function (result) {
 			        	alert("저장 되었습니다.");
 			        	$('#sheetPop').modal("hide");
 			        	$("#jsGrid_sheet").jsGrid("loadData");
 			        }
 			    });
			}
		});
		
		<%-- 삭제 --%>
		$("#delGrp").click(function(){
			$.ajax({
	        	url: "<c:url value='/admin/delGrp.do'/>",
				type:"POST",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					GRP_ID: $("#grpId").val()
				}),
	            success: function (result) {
	            	alert("삭제 되었습니다.");
	            	$('#grpPop').modal("hide");
	            	$("#jsGrid_grp").jsGrid("loadData");
	            	$("#jsGrid_seal").jsGrid("loadData");
	            	$("#jsGrid_sheet").jsGrid("loadData");
	            }
			})
		});
		
		$("#delSeal").click(function(){
			$.ajax({
	        	url: "<c:url value='/admin/delSeal.do'/>",
				type:"POST",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					GRP_ID : $("#sealGrpId").val()
					,SEAL_TYPE : $("#sealType").val()
				}),
	            success: function (result) {
	            	alert("삭제 되었습니다.");
	            	$('#sealPop').modal("hide");
	            	$("#jsGrid_seal").jsGrid("loadData");
	            }
			})
		});
		
		$("#delSheet").click(function(){
			$.ajax({
	        	url: "<c:url value='/admin/delSheet.do'/>",
				type:"POST",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					GRP_ID : $("#sheetGrpId").val()
					,SHEET_NO : $("#sheetNo").val()
					,filePath: $("#filePath").val()
					,fileName: $("#fileNm").val()
				}),
	            success: function (result) {
	            	alert("삭제 되었습니다.");
	            	$('#sheetPop').modal("hide");
	            	$("#jsGrid_sheet").jsGrid("loadData");
	            }
			})
		});
	}); <%-- end  $(document).ready(function(){ --%>
	
	//init grid
	function grpGridInit(){
		$("#jsGrid_grp").jsGrid('destroy');
		$("#jsGrid_grp").jsGrid({
			width: "100%",
		    //height: $(document).height()-650,
		    height: 200,
		    editing: false, //수정 기본처리
		    sorting: false, //정렬
		    paging: false, //조회행넘어가면 페이지버튼 뜸
		    loadMessage : "Now Loading...",
	        confirmDeleting: false,
	        onItemDeleting: function(args) {
				if(!args.item.deleteConfirmed){  // custom property for confirmation
	            	args.cancel = true; // cancel deleting
					confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
	                	if(result == true){
	                    	args.item.deleteConfirmed = true;
	                    	$("#jsGrid_grp").jsGrid('deleteItem', args.item); //call deleting once more in callback
	                	}
					});
				}
	        },
		    fields: [
		    	{name : "GRP_ID",		title : "GRP_ID",		type : "text",	align : "center",	width : 80, visible:false}
		    	,{name : "GRP_NM",		title : "그룹명",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "RMK",			title : "비고",			type : "text",	align : "center",	width : 200,css:"text-truncate"}
		    	,{name : "MOD_DT",		title : "등록일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MOD_ID",		title : "등록자",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MNG_YN",		title : "관리",			type : "text",	align : "center",	width : 80,	css:"text-truncate",
	            	itemTemplate: function(_, item) {
			            var $rtnDiv = $("<div>");
						$rtnDiv.append("<button>");
						$rtnDiv.find("button").addClass("jsgrid-button jsgrid-edit-button");
						return $rtnDiv.find("button").on("click", function() {
							clearGrpPop();
							//data set
							$('#grpId').val(item.GRP_ID);
							$('#grpNm').val(item.GRP_NM);
							$('#rmk').val(item.RMK);
							$('#grpRegId').val(item.REG_ID);
							$('#grpRegDt').val(item.REG_DT);
							//button hide & show
							$('#editGrp').css("display", "inline");
							$('#delGrp').css("display", "inline");
							$('#savGrp').css("display", "none");
							//popup show
							$('#grpPop').modal("show");		
							$('#grpPop').css("z-index","1800");
							return false;
						});
					}	
		    	}
		    ]
	        ,rowClick: function(args) {
				//그리드의 행 데이터를 클릭 했을 때 하이라이트 처리 
				var $row = this.rowByItem(args.item), selectedRow = $("#jsGrid_grp").find('table tr.highlight');
				if (selectedRow.length) {
					selectedRow.toggleClass('highlight');
				};
				$row.toggleClass("highlight");
				
				fGrpId = args.item.GRP_ID;
				//클릭시 Group ID 전달
	        	$("#jsGrid_seal").jsGrid("loadData", fGrpId);
	        	$("#jsGrid_sheet").jsGrid("loadData", fGrpId);
	        	$("#sealGrpId").val(fGrpId);
	        	$("#sheetGrpId").val(fGrpId);
	        	
	        }
			,controller:  {
				loadData : function(filter) {
					return $.ajax({
						type:"POST",
						url:"<c:url value='/admin/getGrpList.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json"
					})
				}
		        ,deleteItem: function(item) {
		        }
		    }
		});
	}
	
	var vSubGridHeight=$(document).height()-500;
	
	function sealGridInit(){
		$("#jsGrid_seal").jsGrid('destroy');
		$("#jsGrid_seal").jsGrid({
			width: "100%",
		    height: vSubGridHeight,
		    editing: false, //수정 기본처리
		    sorting: false, //정렬
		    paging: false, //조회행넘어가면 페이지버튼 뜸
		    loadMessage : "Now Loading...",
	        confirmDeleting: false,
	        onItemDeleting: function(args) {
				if(!args.item.deleteConfirmed){  // custom property for confirmation
	            	args.cancel = true; // cancel deleting
					confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
	                	if(result == true){
	                    	args.item.deleteConfirmed = true;
	                    	$("#jsGrid_seal").jsGrid('deleteItem', args.item); //call deleting once more in callback
	                	}
					});
				}
	        },
		    fields: [
		    	{name : "GRP_ID",		title : "GRP_ID",		type : "text",	align : "center",	width : 80, visible:false}
		    	,{name : "SEAL_TYPE",	title : "Seal Type",	type : "text",	align : "center",	width : 200,css:"text-truncate"}
		    	,{name : "MOD_DT",		title : "등록일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MOD_ID",		title : "등록자",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MNG_YN",		title : "관리",			type : "text",	align : "center",	width : 80,	css:"text-truncate",
	            	itemTemplate: function(_, item) {
			            var $rtnDiv = $("<div>");
						$rtnDiv.append("<button>");
						$rtnDiv.find("button").addClass("jsgrid-button jsgrid-edit-button");
						return $rtnDiv.find("button").on("click", function() {
							clearSealPop();
							$("#sealType").attr("readonly",true);
							//data set
							$('#sealGrpId').val(item.GRP_ID);
							$('#sealType').val(item.SEAL_TYPE);
							$('#sealRegId').val(item.REG_ID);
							$('#sealRegDt').val(item.REG_DT);
							//button hide & show
							$('#delSeal').css("display", "inline");
							$('#savSeal').css("display", "none");
							//popup show
							$('#sealPop').modal("show");		
							$('#sealPop').css("z-index","1800");
							return false;
						});
					}	
		    	}
		    ]
	        ,rowClick: function(args) {
	        }
			,controller:  {
				loadData : function(filter) {
					return $.ajax({
						type:"POST",
						url:"<c:url value='/admin/getSealList.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify({
							GRP_ID : fGrpId
						})
					}).done(function(result){
						fSealData = result;
					});	
				}
		    }
		});
	}		
	
	//init grid
	function sheetGridInit(){
		$("#jsGrid_sheet").jsGrid('destroy');
		$("#jsGrid_sheet").jsGrid({
			width: "100%",
		    height: vSubGridHeight,
		    editing: false, //수정 기본처리
		    sorting: false, //정렬
		    paging: false, //조회행넘어가면 페이지버튼 뜸
		    loadMessage : "Now Loading...",
	        confirmDeleting: false,
	        onItemDeleting: function(args) {
				if(!args.item.deleteConfirmed){  // custom property for confirmation
	            	args.cancel = true; // cancel deleting
					confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
	                	if(result == true){
	                    	args.item.deleteConfirmed = true;
	                    	$("#jsGrid_sheet").jsGrid('deleteItem', args.item); //call deleting once more in callback
	                	}
					});
				}
	        },
		    fields: [
		    	{name : "GRP_ID",		title : "GRP_ID",		type : "text",	align : "center",	width : 80, visible:false}
		    	,{name : "SHEET_NO",	title : "Sheet No",		type : "text",	align : "center",	width : 200,css:"text-truncate"}
		    	,{name : "VIEW_PRICE",	title : "가격정보",		type : "text",	align : "center",	width : 100, css:"text-truncate",
		    		itemTemplate : function (value, item) {
                    	var iconClass = "";
                    	//권한 체크 -R1 조회 R5 저장
                    	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
                        	iconClass = "fa fa-search"; 
                    	}else if(value == "Y" && ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5")){
                    		iconClass = "fa fa-search";
                    	}else if(value == "N" && "${ref_data_role}"== "R1" || "${ref_data_role}"== "R5"){
                    		iconClass = "fa fa-search";                   		
                    	}else {
                    		iconClass = "";
                    	}
                    	return $("<span>").attr("class", iconClass);
                	}
                }
		    	,{name : "MOD_DT",		title : "등록일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MOD_ID",		title : "등록자",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MNG_YN",		title : "관리",			type : "text",	align : "center",	width : 80,	css:"text-truncate",
	            	itemTemplate: function(_, item) {
			            var $rtnDiv = $("<div>");
						$rtnDiv.append("<button>");
						$rtnDiv.find("button").addClass("jsgrid-button jsgrid-edit-button");
						return $rtnDiv.find("button").on("click", function() {
							clearSheetPop();
							$("#sheetNo").attr("readonly",true);
							//data set
							$('#sheetGrpId').val(item.GRP_ID);
							$('#sheetNo').val(item.SHEET_NO);
							$('#filePath').val(item.FILE_PATH);
							$('#fileNm').val(item.FILE_NM);
							if(isEmpty(item.FILE_ORG_NM)){
								$('#divFileOrgNm').css("display", "none");
							}else{
								$('#divFileOrgNm').css("display", "inline");
								$('#fileOrgNm').text(item.FILE_ORG_NM);
							}
							if(isEmpty(item.FILE_SIZE)){
								$('#divFileSize').css("display", "none");
							}else{
								$('#divFileSize').css("display", "inline");
								$('#fileSize').text("용량 : "+item.FILE_SIZE+" KB");
							}
							$('#sheetRegId').val(item.REG_ID);
							$('#sheetRegDt').val(item.REG_DT);
							//button hide & show
							$('#editSheet').css("display", "inline");
							$('#delSheet').css("display", "inline");
							$('#savSheet').css("display", "none");
							//popup show
							$('#sheetPop').modal("show");		
							$('#sheetPop').css("z-index","1800");
							return false;
						});
					}	
		    	}
		    ]
	        ,rowClick: function(args) {
	        	var $target = $(args.event.target);
	        	if($target.closest(".fa.fa-search").length) {
	        		//파일이 없는 경우 체크
	        		if(isEmpty(args.item.FILE_NM)){
	        			alert("파일이 존재하지 않습니다.");
		        		return;
	        		}
//					다운로드 방식
// 	        		var vFileParam = $.param({
// 	        			"file_name"		: args.item.FILE_NM
// 	        		    ,"file_name_org": args.item.FILE_ORG_NM
// 	        		    ,"file_path"	: args.item.FILE_PATH
// 	        		});
// 					document.getElementById("viewImg").src = "<c:url value="/library/downLibraryFile.do" />?"+vFileParam;

					var strNo = args.item.FILE_PATH.indexOf("/");
					var vPath = args.item.FILE_PATH.substr(strNo);
					document.getElementById("viewImg").src = "/uploadpath/"+vPath+"/"+args.item.FILE_NM;

					var viewer = new Viewer(document.getElementById("viewImg"), {
						navbar : true,
						toolbar : true
					});
					$('#viewImg').trigger("click");
	        	}
	        }
			,controller:  {
				loadData : function(filter) {
					return $.ajax({
						type:"POST",
						url:"<c:url value='/admin/getSheetList.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify({
							GRP_ID : fGrpId
						})
					}).done(function(result){
						fSheetData = result;
					});
				}
		        ,deleteItem: function(item) {
		        }
		    }
		});
	}		
	
	//파일 확장자와 용량 제한
	function fileCheck(obj){
		var file = obj.files;
		var maxSize = 100*1024*1000;  //100M 파일 최대 용량
		if(file[0].size > maxSize){
			alert("전체 파일업로드 허용용량 "+(maxSize/1024)+" Kbyte를 초과하였습니다.");
		}else{ 
			if(!/\.(bmp|jpg|png|jpeg)$/i.test(file[0].name)){
				alert("이미지 파일만 선택할 수 있습니다.");
				//초기화
				obj.outerHTML = obj.outerHTML;
				return;
			}
		}
	}
	
	//PRICE ID 생성
	function uuidv4() {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
			return v.toString(16);
		});
	};
	
	//팝업 초기화
	function clearGrpPop() {
		$('#grpId').val("");
		$('#grpNm').val("");
		$('#rmk').val("");
		$('#grpRegId').val("${userName}");
		$('#grpRegDt').val("<%= sf.format(nowTime) %>");
	};

	function clearSealPop() {
		$('#sealType').val("");
		$('#sealRegId').val("${userName}");
		$('#sealRegDt').val("<%= sf.format(nowTime) %>");
	};

	function clearSheetPop() {
		$('#sheetNo').val("");
		$('#input_files').val("");
		$('#fileNm').val("");
		$('#fileOrgNm').text("");
		$('#fileSize').text("");
		$('#sheetRegId').val("${userName}");
		$('#sheetRegDt').val("<%= sf.format(nowTime) %>");
	};
	
	//빈값 체크
	function isEmpty(value) { 
		if( value == "" || value == null || value == undefined || ( value != null && typeof value == "object" && !Object.keys(value).length ) ){ return true }else{ return false }
	};
</script>

<style>
	.pdfobject-container {
	    width: 100%;
	    max-width: 600px;
	    height: 600px;
	    margin: 2em 0;
	    overflow:hidden !important;
	}
	
	.pdfobject { border: solid 1px #666; }
	
	.form-control[readonly]{
		background-color: #F5F5F5;
	}
	
	.bootbox-alert {
    	position:fixed;
    	z-index: 3500;
	}
	
	.bootbox-confirm {
    	position:fixed;
    	z-index: 3500;
	}
	
	.datepicker-container {
    	position:fixed;
    	z-index: 3200 !important;
	}
	
</style>
</head>

<body>
	<!-- ================  Contents ================  -->
	<div class="container-fluid">
		<div class ="row">	
			<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">
				<div class="row">
					<div class="col-6">
				    	<div class="h5 pt-3">
							<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">가격정보</span></strong>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-6 pt-2">
				    	<div class="h5 pt-3">
							<strong><span class="ml-1">그룹정보</span></strong>
						</div>
					</div>
					<div class="col-6 pt-2 text-right">
						<div class="mr-2">
							<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">        
								<button id="btn_regGrp" type="button"class="btn btn-outline-success">그룹정보 등록 <i class="fa fa-save"></i></button>
							</c:if>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-12 mt-2">
						<div id="jsGrid_grp" style="height:300px;"></div>
					</div>
				</div>
				<div class="row">
					<div class="col-3 pt-2">
				    	<div class="h5 pt-3">
							<strong><span class="ml-1">Seal 정보</span></strong>
						</div>
					</div>
					<div class="col-3 pt-2 text-right">
						<div class="mr-2">
							<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">        
								<button id="btn_regSeal" type="button"class="btn btn-outline-success">Seal 등록 <i class="fa fa-save"></i></button>
							</c:if>
						</div>
					</div>
					<div class="col-3 pt-2">
				    	<div class="h5 pt-3">
							<strong><span class="ml-1">Sheet 정보</span></strong>
						</div>
					</div>
					<div class="col-3 pt-2 text-right">
						<div class="mr-2">
							<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">        
								<button id="btn_regSheet" type="button"class="btn btn-outline-success">Sheet 등록 <i class="fas fa-file-upload"></i></button>
							</c:if>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-6 mt-2">
						<div id="jsGrid_seal" style="height:300px;"></div>
					</div>
					<div class="col-6 mt-2">
						<div id="jsGrid_sheet" style="height:300px;"></div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<%-- group pop up --%>
	<div class="modal" id="grpPop" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body">
					<div class="row">
						<div class="col-2">
							<p class="h5" style="width: 150px;">
								<i class="fa fa-images"></i> <strong class="text-primary">그룹정보 등록</strong>
							</p>
						</div>
					</div>
					<input id="grpId" 	type="hidden" class="form-control form-control-sm" placeholder="GROUP ID">
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> 그룹명  </label>
							</div>
						</div>
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id=grpNm type="text" class="form-control form-control-sm" placeholder="그룹명" maxlength="20">
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> 비고  </label>
							</div>
						</div>
						<div class="col-10">
							<div class="pt-2 custom-responsive-p2">
								<input id=rmk type="text" class="form-control form-control-sm" placeholder="비고" maxlength="20">
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-2">
							<div class="pt-3 mt-3 custom-responsive-p2">
								<label> 등록자 </label>
							</div>
						</div>	
						<div class="col-4">
							<div class="pt-2 mt-3 custom-responsive-p2">
								<input id="grpRegId" type="text" class="form-control form-control-sm" placeholder="등록자" maxlength="20" readonly>
							</div>
						</div>
						<div class="col-2">
							<div class="pt-3 mt-3 custom-responsive-p2">
								<label> 등록일 </label>
							</div>
						</div>						
						<div class="col-4">
							<div class="pt-2 mt-3 custom-responsive-p2">
								<input id="grpRegDt" type="text" class="form-control form-control-sm" placeholder="등록일" maxlength="20" readonly>
							</div>
						</div>
					</div>
					<div class="col-12 d-flex flex-row-reverse">
						<div class="p-2 custom-responsive-p2">
							<button id="savGrp" type="button"class="btn btn-outline-success">저장 <i class="far fa-save"></i></button>
							<button id="editGrp" type="button"class="btn btn-outline-success">수정 <i class="far fa-edit"></i></button>
							<button id="delGrp" type="button"class="btn btn-outline-success">삭제 <i class="fas fa-trash-alt"></i></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<%-- seal pop up --%>
	<div class="modal" id="sealPop" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body">
					<div class="row">
						<div class="col-2">
							<p class="h5" style="width: 150px;">
								<i class="fas fa-images"></i> <strong class="text-primary">Seal 등록</strong>
							</p>
						</div>
					</div>
					<input id="sealGrpId" 	type="hidden" class="form-control form-control-sm" placeholder="GROUP ID">
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> Seal Type  </label>
							</div>
						</div>
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id="sealType" type="text" class="form-control form-control-sm" placeholder="Seal Type" maxlength="20">
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-2">
							<div class="pt-3 mt-3 custom-responsive-p2">
								<label> 등록자 </label>
							</div>
						</div>	
						<div class="col-4">
							<div class="pt-2 mt-3 custom-responsive-p2">
								<input id="sealRegId" type="text" class="form-control form-control-sm" placeholder="등록자" maxlength="20" readonly>
							</div>
						</div>
						<div class="col-2">
							<div class="pt-3 mt-3 custom-responsive-p2">
								<label> 등록일 </label>
							</div>
						</div>						
						<div class="col-4">
							<div class="pt-2 mt-3 custom-responsive-p2">
								<input id="sealRegDt" type="text" class="form-control form-control-sm" placeholder="등록일" maxlength="20" readonly>
							</div>
						</div>
					</div>
					<div class="col-12 d-flex flex-row-reverse">
						<div class="p-2 custom-responsive-p2">
							<button id="savSeal" type="button"class="btn btn-outline-success">저장 <i class="far fa-save"></i></button>
							<button id="delSeal" type="button"class="btn btn-outline-success">삭제 <i class="fas fa-trash-alt"></i></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<%-- sheet pop up --%>
	<div class="modal" id="sheetPop" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body">
					<div class="row">
						<div class="col-2">
							<p class="h5" style="width: 150px;">
								<i class="fas fa-images"></i> <strong class="text-primary">이미지 등록</strong>
							</p>
						</div>
					</div>
					<input id="sheetGrpId" 	type="hidden" class="form-control form-control-sm" placeholder="폴더 ID">
					<input id="filePath" 	type="hidden" class="form-control form-control-sm" placeholder="파일위치">
					<input id="fileNm" 		type="hidden" class="form-control form-control-sm" placeholder="저장파일 이름">
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> Sheet No  </label>
							</div>
						</div>
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id="sheetNo" type="text" class="form-control form-control-sm" placeholder="Sheet No" maxlength="20">
							</div>
						</div>
					</div>
					<div class="row">
						<div id="divFileOrgNm" class="col-6">
							<div class="custom-control" style="width: 100%; margin-left: -23px">
								<label style="margin-top: -3px;"><span id="fileOrgNm"></span></label>
							</div>
						</div>
						<div id="divFileSize" class="col-3">
							<div class="custom-control" style="width: 100%; margin-left: -10px">
								<label style="margin-top: -3px;"><span id="fileSize"></span></label>
							</div>				
						</div>
					</div>	
					<div class="row">
						<div class="col-12 mt-2">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input name="input_files" id="input_files" type="file" aria-label="files" accept=".png, .jpg, .jpeg, .bmp" onchange="fileCheck(this)"/>
							</div>
						</div>						
					</div>	
					<div class="row">
						<div class="col-2">
							<div class="pt-3 mt-3 custom-responsive-p2">
								<label> 등록자 </label>
							</div>
						</div>	
						<div class="col-4">
							<div class="pt-2 mt-3 custom-responsive-p2">
								<input id="sheetRegId" type="text" class="form-control form-control-sm" placeholder="등록자" maxlength="20" readonly>
							</div>
						</div>
						<div class="col-2">
							<div class="pt-3 mt-3 custom-responsive-p2">
								<label> 등록일 </label>
							</div>
						</div>						
						<div class="col-4">
							<div class="pt-2 mt-3 custom-responsive-p2">
								<input id="sheetRegDt" type="text" class="form-control form-control-sm" placeholder="등록일" maxlength="20" readonly>
							</div>
						</div>
					</div>
					<div class="col-12 d-flex flex-row-reverse">
						<div class="p-2 custom-responsive-p2">
							<button id="savSheet" type="button"class="btn btn-outline-success">저장 <i class="far fa-save"></i></button>
							<button id="editSheet" type="button"class="btn btn-outline-success">수정 <i class="far fa-edit"></i></button>
							<button id="delSheet" type="button"class="btn btn-outline-success">삭제 <i class="fas fa-trash-alt"></i></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<img id='viewImg' src='' style="display:none;">
</body>
</html>