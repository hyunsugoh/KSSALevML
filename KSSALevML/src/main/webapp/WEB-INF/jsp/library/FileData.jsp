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
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">
<script src="<c:url value='/js/common/jquery/jquery-ui.min.js'/>"></script> 
<link rel="stylesheet" type='text/css' href="<c:url value='/css/common/jquery-ui/jquery-ui.css'/>">
<!-- 뷰어 -->
<script src="<c:url value='/js/common/pdfobject/pdfobject.min.js'/>"></script>
<script>
	var myTree;
	var gFolderId;
	var gRefId = "";
	var gFileNm = "";
	$(document).ready(function(){
		
		$("#folder_tree").empty();
		initTree();
		libraryGridInit();
		
		$("#jsGrid_library").jsGrid("loadData");
		//관리자가 아닌 경우 관리 컬럼 hide
		if("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]"){
			$("#jsGrid_library").jsGrid("fieldOption", "MNG_YN", "visible", true);
		}else{
			$("#jsGrid_library").jsGrid("fieldOption", "MNG_YN", "visible", false);
		}
		
		$('#btn_folder_paste').css("display", "none");
		
		<%-- file upload button Event --%>
		$("#btn_fileUpload").click(function(){
			if(isEmpty(gFolderId)){
				alert("등록하실 폴더를 선택해주세요.");
				return;
			};
			clearPop();
			//button hide & show
			$('#editFileData').css("display", "none");
			$('#delFileData').css("display", "none");
			$('#divFileOrgNm').css("display", "none");
			$('#divFileSize').css("display", "none");
			$('#savFileData').css("display", "block");
			//popup show
			$('#fileUpload').modal("show");
			$('#fileUpload').css("z-index","1800");
		});
		
	    $.datepicker.setDefaults({
	        dateFormat: 'yymmdd',
	        prevText: '이전 달',
	        nextText: '다음 달',
	        monthNames: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
	        monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
	        dayNames: ['일', '월', '화', '수', '목', '금', '토'],
	        dayNamesShort: ['일', '월', '화', '수', '목', '금', '토'],
	        dayNamesMin: ['일', '월', '화', '수', '목', '금', '토'],
	        showMonthAfterYear: true,
	        yearSuffix: '년',
			changeMonth: true, //월변경가능
		    changeYear: true, //년변경가능
			showMonthAfterYear: true, //년 뒤에 월 표시 
			buttonText: '날짜를 선택하세요', // 달력이미지에 마우스오버일경우
		    autoSize: false, //오토리사이즈(body등 상위태그의 설정에 따른다)
			showButtonPanel:true, // 캘린더 하단에 버튼 패널을 표시한다(오늘날짜로이동버튼, 닫기버튼). 
		    currentText: '오늘', // 오늘날짜로이동되는 버튼의 텍스트 변경 
		    showAnim: "slideDown", //애니메이션을 적용한다.
		    closeText: '닫기',  // 닫기버튼의 텍스트 변경 
			cleanText: '지우기'  //추가한 기능 jquery-ui.js 파일에 소스 추가해아한다.
	    });
		
		<%-- 일자 검색조건 --%>
		$("#creDt").datepicker({
		   dateFormat: 'yymmdd',
		   buttonImage: "/images/common/icons/calendar.png",
		   showOn: "button",
		   buttonImageOnly: false,
		   currentText: "Now"
		});
		
		<%-- file save button Event --%>
		$("#savFileData").click(function(){
			var formData = new FormData();
			formData.append("FOLDER_ID", $("#folderId").val());
			formData.append("REF_ID", uuidv4());
			formData.append("DOC_NO", $("#docNo").val());
			formData.append("REF_NM", $("#refNm").val());
			formData.append("CRE_DT", $("#creDt").val());
			formData.append("REVI_LEV", $("#reviLev").val());
			formData.append("KEYWORD_1", $("#keyword1").val());
			formData.append("KEYWORD_2", $("#keyword2").val());
			formData.append("KEYWORD_3", $("#keyword3").val());
			formData.append("KEYWORD_4", $("#keyword4").val());
			formData.append("KEYWORD_5", $("#keyword5").val());
			formData.append("KEYWORD_6", $("#keyword6").val());
			formData.append("KEYWORD_7", $("#keyword7").val());
			formData.append("KEYWORD_8", $("#keyword8").val());
			formData.append("KEYWORD_9", $("#keyword9").val());
			formData.append("KEYWORD_10", $("#keyword10").val());
			formData.append("target1_check", $("#target1checked").is(":checked"));
			formData.append("target2_check", $("#target2checked").is(":checked"));
			formData.append("target3_check", $("#target3checked").is(":checked"));

			if($('#input_files').prop('files').length==0){
				alert("등록하실 파일을 선택해 주세요.");
// 				var v_url = "<c:url value='/library/noFile.do'/>";
// 				formData.append("NEW_YN", "Y");
// 				formData.append("FILE_PATH", "");
// 				formData.append("FILE_NAME", "");
// 				checkConfirm(v_url, formData);
			}else{
				var v_url = "<c:url value='/library/savLibrary.do'/>";
				formData.append("files", $('#input_files').prop('files')[0]);
				checkConfirm(v_url, formData);
			}
		});
		
		<%-- file delete button Event --%>
		$("#delFileData").click(function(){
			confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
            	if(result == true){
					$.ajax({
			        	url: "<c:url value='/library/deleteLibrary.do'/>",
						type:"POST",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify({
							REF_ID : $("#refId").val()
							,filePath: $("#filePath").val()
							,fileName: $("#fileNm").val()
						}),
			            success: function (result) {
			            	alert("삭제 되었습니다.");
			            	$('#fileUpload').modal("hide");
			            	$("#jsGrid_library").jsGrid("loadData");
			            }
					})
            	}
			});
		});
		
		<%-- file save button Event --%>
		$("#editFileData").click(function(){
			var formData = new FormData();
			formData.append("FOLDER_ID", $("#folderId").val());
			formData.append("REF_ID", $("#refId").val());
			formData.append("DOC_NO", $("#docNo").val());
			formData.append("REF_NM", $("#refNm").val());
			formData.append("CRE_DT", $("#creDt").val());
			formData.append("REVI_LEV", $("#reviLev").val());
			formData.append("KEYWORD_1", $("#keyword1").val());
			formData.append("KEYWORD_2", $("#keyword2").val());
			formData.append("KEYWORD_3", $("#keyword3").val());
			formData.append("KEYWORD_4", $("#keyword4").val());
			formData.append("KEYWORD_5", $("#keyword5").val());
			formData.append("KEYWORD_6", $("#keyword6").val());
			formData.append("KEYWORD_7", $("#keyword7").val());
			formData.append("KEYWORD_8", $("#keyword8").val());
			formData.append("KEYWORD_9", $("#keyword9").val());
			formData.append("KEYWORD_10", $("#keyword10").val());
			formData.append("FILE_PATH", $("#filePath").val());
			formData.append("FILE_NAME", $("#fileNm").val());
			formData.append("target1_check", $("#target1checked").is(":checked"));
			formData.append("target2_check", $("#target2checked").is(":checked"));
			formData.append("target3_check", $("#target3checked").is(":checked"));

			if($('#input_files').prop('files').length==0){
				//체크 & 저장
				var v_url = "<c:url value='/library/noFile.do'/>";
				formData.append("NEW_YN", "N");
				checkConfirm(v_url, formData);
			}else{
				//등록정보의 파일명과 실제 파일명이 다르면 알림 (등록진행불가)
// 				var fileNmIndex = $('#input_files').prop('files')[0].name.lastIndexOf('.');
// 				var fileRealNm = $('#input_files').prop('files')[0].name.substring(0, fileNmIndex);
// 				if(fileRealNm != $("#refNm").val()){
// 					alert("파일명이 선택된 파일명과 다릅니다.");
// 					return;
// 				};
				//체크 & 저장
				var v_url = "<c:url value='/library/editLibrary.do'/>";
				formData.append("files", $('#input_files').prop('files')[0]);
				checkConfirm(v_url, formData);
			}
		});
		
		<%-- folder 등록 --%>
		$("#btn_folder_sav").click(function(){
			var pId = "";
			var pNo = "";
			if(isEmpty(myTree.getSelectedItemId())){
				pId = 1;
				pNo = 1;
			}else{
				pId = myTree.getSelectedItemId();
				pNo = Number(pId.split('-', 1));
			};
			var folderId = uuidv3(pNo);
			myTree.insertNewItem(pId,folderId,"생성된 폴더");
			$.ajax({
				type:"POST",
				url:"<c:url value='/library/savDocTree.do'/>",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
				contentType: "application/json",
				data: JSON.stringify({
					FOLDER_ID : folderId
					,P_ID : pId
					,FOLDER_NM : "생성된 폴더"
				})
			})
		});
		
		<%-- folder 삭제 --%>
		$("#btn_folder_del").click(function(){
			if(isEmpty(myTree.getSelectedItemId())){
				alert("삭제하실 폴더를 선택해 주세요.");
				return;
			};
			var folderId = myTree.getSelectedItemId();
			if(folderId == '1'){
				alert("최상위 폴더는 삭제하실 수 없습니다.");
				return;
			};
			confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
            	if(result == true){
            		//하위폴더가 존재하는지 체크
            		$.ajax({
						type:"POST",
						url:"<c:url value='/library/getSubfolderYn.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify({
							FOLDER_ID : folderId
						})
					}).done(function(result){
						if(result > 0){
							alert("하위폴더가 있는 경우 삭제하실 수 없습니다.");
							return;
						}else{
							$.ajax({
								type:"POST",
								url:"<c:url value='/library/delDocTree.do'/>",
								headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
								contentType: "application/json",
								data: JSON.stringify({
									FOLDER_ID : folderId
								})
							}).done(function(){
								$("#jsGrid_library").jsGrid("loadData");
								myTree.deleteItem(folderId,false);
								myTree.clearSelection();
							})
						}
					});
            	}
			});
		});
		
		<%-- 붙여 넣기 --%>
		$("#btn_folder_paste").click(function(){
			confirm("파일을 이동하시겠습니까 ? <br> "+gFileNm, function(result) {  // bootbox js plugin for confirmation dialog
            	if(result == true){
					$.ajax({
						type:"POST",
						url:"<c:url value='/library/dataPaste.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify({
							REF_ID : gRefId
							,FOLDER_ID : gFolderId
						})
					}).done(function(){
						$("#jsGrid_library").jsGrid("loadData");
						myTree.clearSelection();
						$('#btn_folder_paste').css("display", "none");
					})	
            	}
			});
		});
		
		<%-- grid 검색 --%>
		$("#btn_std_sch").click(function(){
			if(isEmpty(gFolderId)){
				alert("검색하실 폴더를 선택해주세요.");
				return;
			};
			$("#jsGrid_library").jsGrid("loadData");
		});
		
		<%-- grid 키워드 검색 --%>
		$("#btn_key_sch").click(function(){
			if(isEmpty(gFolderId)){
				alert("검색하실 폴더를 선택해주세요.");
				return;
			};
			$("#jsGrid_library").jsGrid("loadData","key");
		});
		
		<%-- grid 전체검색 --%>
		$("#btn_all_sch").click(function(){
			allDataGridInit();
			$('#schAll').modal("show");
			$('#schAll').css("z-index","1500");
			$("#popRefNm").val("");
			if($("#v_refNm").val()!=""){
				$("#popRefNm").val($("#v_refNm").val());
				$("#jsGrid_allData").jsGrid("loadData");
			}
		});
		
		<%-- 폴더 이동 --%>
		$("#btn_folder_mov").click(function(){
			if(isEmpty(gRefId)){
				alert("이동하실 자료를 선택해 주세요.");
				return;
			};
			$('#btn_folder_paste').css("display", "inline");
		});
		
		<%-- 전체검색 --%>
		$("#pop_btn_sch").click(function(){
			$("#jsGrid_allData").jsGrid("loadData");
		});
		
		<%-- 전체 키워드 검색 --%>
		$("#pop_key_sch").click(function(){
			$("#jsGrid_allData").jsGrid("loadData","key");
		});
	}); <%-- end  $(document).ready(function(){ --%>
	
	//init grid
	function libraryGridInit(){
		$("#jsGrid_library").jsGrid('destroy');
		$("#jsGrid_library").jsGrid({
			width: "100%",
		    height: $(document).height()-250,
		    editing: false, //수정 기본처리
		    sorting: true, //정렬
		    paging: false, //조회행넘어가면 페이지버튼 뜸
		    loadMessage : "Now Loading...",
	        confirmDeleting: false,
	        onItemDeleting: function(args) {
				if(!args.item.deleteConfirmed){  // custom property for confirmation
	            	args.cancel = true; // cancel deleting
					confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
	                	if(result == true){
	                    	args.item.deleteConfirmed = true;
	                    	$("#jsGrid_library").jsGrid('deleteItem', args.item); //call deleting once more in callback
	                	}
					});
				}
	        },
		    fields: [
		    	{name : "REF_ID",		title : "REF_ID",		type : "text",	align : "center",	width : 80, visible:false}
		    	,{name : "REF_NM",		title : "파일명",			type : "text",	align : "left",		width : 250,css:"text-truncate"}
		    	,{name : "DOC_NO",		title : "문서번호",		type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "REVI_LEV",	title : "Rev.",			type : "text",	align : "center",	width : 80,css:"text-truncate"}
		    	,{name : "CRE_DT",		title : "작성일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "ACC_SCH_YN",	title : "조회",			type : "text",	align : "center",	width : 80, css:"text-truncate",
		    		itemTemplate : function (value, item) {
                    	var iconClass = "";
                    	//권한 체크 
                    	// R1 : 조회,  R5 : 저장
//                     	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
//                         	iconClass = "fa fa-eye"; 
//                     	}else 
                    		
						// 파일권한 조회인 경우
						// 사용자가 조회권한 이상을 가지고 있으면 조회가능 
                    	if(value == "Y" 
                    			&& ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5" ||
                    					"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                    			){
                    		iconClass = "fa fa-eye";
                    		
                   		// 파일권한 미지정일때 : 사용자권한 체크	
                   		// - 사용자 권한에 따라 
                        }else if((item.ACC_USF_YN == "Y" || value == "N") 
                        			&& ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5" ||
                        				"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                        		){
                        	iconClass = "fa fa-eye";
                        	
                        // 그 외    		
                    	}else {
                    		iconClass = "";
                    	}
                    	return $("<span>").attr("class", iconClass);
                	}
                }
		    	,{name : "ACC_SAV_YN",	title : "저장",			type : "text",	align : "center",	width : 80, css:"text-truncate",
		    		itemTemplate : function (value, item) {
                    	var iconClass = "";
                    	//권한 체크
//                     	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
//                     		iconClass = "fa fa-download";
//                     	}else 
                    	
						// 파일권한 저장인 경우
						// - 사용가 조회권한 이상을 가지고 있으면 저장가능 
                    	if(value == "Y" 
                    				&& ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5" ||
                    					"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                    			){
                    		iconClass = "fa fa-download";
                    		
                    	// 파일권한이 조회인 경우
                    	// - 모든 유저 저장 불가
                    	}else if(item.ACC_SCH_YN == "Y"){
                    		iconClass = "";
                    		
                    	// 파일권한 미지정일때 : 사용자권한 체크
                    	// - 사용자 권한에 따라 
                    	}else if(item.ACC_USF_YN == "Y" 
                    				&& ("${ref_data_role}"== "R5" ||
                    					"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                    			){
                    		iconClass = "fa fa-download";
                    	
                    	// 그 외	
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
							clearPop();
							//data set
							$('#folderId').val(item.FOLDER_ID);
							$('#refId').val(item.REF_ID);
							$('#refNm').val(item.REF_NM);
							$('#docNo').val(item.DOC_NO);
							$('#filePath').val(item.FILE_PATH);
							$('#fileNm').val(item.FILE_NM);
							$('#reviLev').val(item.REVI_LEV);
							$('#creDt').val(item.CRE_DT);
							$('#regId').val(item.REG_ID);
							$('#regDt').val(item.REG_DT);
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
							if(item.ACC_USF_YN=="Y"){
								$('#target1checked').prop("checked", true);
							}else{
								$('#target1checked').prop("checked", false);
							}
							if(item.ACC_SCH_YN=="Y"){
								$('#target2checked').prop("checked", true);
							}else{
								$('#target2checked').prop("checked", false);
							}
							if(item.ACC_SAV_YN=="Y"){
								$('#target3checked').prop("checked", true);
							}else{
								$('#target3checked').prop("checked", false);
							}
							$('#keyword1').val(item.KEYWORD_1);
							$('#keyword2').val(item.KEYWORD_2);
							$('#keyword3').val(item.KEYWORD_3);
							$('#keyword4').val(item.KEYWORD_4);
							$('#keyword5').val(item.KEYWORD_5);
							$('#keyword6').val(item.KEYWORD_6);
							$('#keyword7').val(item.KEYWORD_7);
							$('#keyword8').val(item.KEYWORD_8);
							$('#keyword9').val(item.KEYWORD_9);
							$('#keyword10').val(item.KEYWORD_10);
							//button hide & show
							$('#editFileData').css("display", "inline");
							$('#delFileData').css("display", "inline");
							$('#savFileData').css("display", "none");
							//popup show
							$('#fileUpload').modal("show");		
							$('#fileUpload').css("z-index","1800");
							return false;
						});
					}	
		    	}
		    ]
	        ,rowClick: function(args) {
	        	//폴더 이동 초기화
	        	gRefId = "";
	        	gFileNm = "";
	        	$("#btn_folder_paste").css("display", "none");
	        	//그리드의 행 데이터를 클릭 했을 때 하이라이트 처리 
				var $row = this.rowByItem(args.item), selectedRow = $("#jsGrid_library").find('table tr.highlight');
				if (selectedRow.length) {
					selectedRow.toggleClass('highlight');
				};
				$row.toggleClass("highlight");
	        	
				gRefId = args.item.REF_ID;
				var fileLastIndx = args.item.FILE_ORG_NM.lastIndexOf('.');
				gFileNm = args.item.FILE_ORG_NM.substring(0, fileLastIndx);
				
	        	var $target = $(args.event.target);
	        	if($target.closest(".fa.fa-download").length) {
	        		//파일이 없는 경우 체크
	        		if(isEmpty(args.item.FILE_NM)){
	        			alert("파일이 존재하지 않습니다.");
		        		return;
	        		}
	        		var vFileParam = $.param({
	        			"file_name"		: args.item.FILE_NM
	        		    ,"file_name_org": args.item.FILE_ORG_NM
	        		    ,"file_path"	: args.item.FILE_PATH
	        		});
	        		$("#fileDownFrame").attr("src", "<c:url value="/library/downLibraryFile.do" />?"+vFileParam); //exceldown 실행? "src"에 "<c:url value="/ml/predictApplyExcelFileDownload.do" />?"+vFileParam 부여.	        	
	        	}
	        	if($target.closest(".fa.fa-eye").length) {
	        		//파일이 없는 경우 체크
	        		if(isEmpty(args.item.FILE_NM)){
	        			alert("파일이 존재하지 않습니다.");
		        		return;
	        		}
	        		//size set
			       	$('#pdfModal .modal-content').css("height",($(document).height()-80)+"px");	
					$('#pdf').css("height",($(document).height()-170)+"px");	
					$('#pdf').css("max-width","100%");	
					$('#pdf').css("margin","0px");	
					
					$('#pdfModal').modal("show");
					$('#pdfModal').css("z-index","2500");
	        		var vFileParam = $.param({
	        			"file_name"		: args.item.FILE_NM
	        		    ,"file_name_org": args.item.FILE_ORG_NM
	        		    ,"file_path"	: args.item.FILE_PATH
	        		});
					var options = {
	        			pdfOpenParams: {
	        				navpanes: 0
							,toolbar: 0
	        				,statusbar: 0
	        			    ,view:"FitV"
	        			    ,pagemode:"none"
	        				,page: 1
	        			}
						,forcePDFJS: true
	        			,PDFJS_URL:"<c:url value='/js/common/pdfjs/web/viewer.html'/>"
	        		};
	        		var myPDF = PDFObject.embed("<c:url value="/library/downLibraryFile.do" />?"+vFileParam,"#pdf", options);
	        	}
	        }
			,controller:  {
				loadData : function(filter) {
					var folderId = myTree.getSelectedItemId();
					if(isEmpty(folderId)){
						return;
					};
					if(filter == "key"){
						return $.ajax({
							type:"POST",
							url:"<c:url value='/library/getKeySchList.do'/>",
							headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
							contentType: "application/json",
							data: JSON.stringify({ 
								FOLDER_ID : folderId
								,REF_NM : $("#v_refNm").val()
							})
						})
					}else{
						return $.ajax({
							type:"POST",
							url:"<c:url value='/library/getLibraryList.do'/>",
							headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
							contentType: "application/json",
							data: JSON.stringify({ 
								FOLDER_ID : folderId
								,REF_NM : $("#v_refNm").val()
							})
						})
					}
				}
		        ,deleteItem: function(item) {
					$.ajax({
						type:"POST",
						url:"<c:url value='/ml/getDeleteHistory.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify(item)
					}).done(function(result){
						alert("삭제하였습니다.");
					})
		        }
		    }
		});
	}		
	
	//전체검색 init grid
	function allDataGridInit(){
		$("#jsGrid_allData").jsGrid('destroy');
		$("#jsGrid_allData").jsGrid({
			width: "100%",
		    height: $(document).height()-350,
		    editing: false, //수정 기본처리
		    sorting: true, //정렬
		    paging: false, //조회행넘어가면 페이지버튼 뜸
		    loadMessage : "Now Loading...",
	        confirmDeleting: false,
	        onItemDeleting: function(args) {
				if(!args.item.deleteConfirmed){  // custom property for confirmation
	            	args.cancel = true; // cancel deleting
					confirm("삭제 하시겠습니까?", function(result) {  // bootbox js plugin for confirmation dialog
	                	if(result == true){
	                    	args.item.deleteConfirmed = true;
	                    	$("#jsGrid_allData").jsGrid('deleteItem', args.item); //call deleting once more in callback
	                	}
					});
				}
	        },
		    fields: [
		    	{name : "FOLDER_ID",	title : "FOLDER_ID",	type : "text",	align : "center",	width : 200, visible:false}
		    	,{name : "FOLDER_NM",	title : "폴더명",			type : "text",	align : "center",	width : 150, visible:false}
		    	,{name : "depth_fullname",title : "폴더명",		type : "text",	align : "left",	width : 250, css:"text-truncate"}
		    	,{name : "REF_ID",		title : "REF_ID",		type : "text",	align : "center",	width : 80, visible:false}
		    	,{name : "REF_NM",		title : "파일명",			type : "text",	align : "left",		width : 250,css:"text-truncate"}
		    	,{name : "DOC_NO",		title : "문서번호",		type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "REVI_LEV",	title : "Rev.lev",		type : "text",	align : "center",	width : 80,css:"text-truncate"}
		    	,{name : "CRE_DT",		title : "작성일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "ACC_SCH_YN",	title : "조회",			type : "text",	align : "center",	width : 80, css:"text-truncate",
		    		itemTemplate : function (value, item) {
		    			var iconClass = "";
                    	//권한 체크 -R1 조회 R5 저장
//                     	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
//                         	iconClass = "fa fa-eye"; 
//                     	}else 
//                     	if(value == "Y" && ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5")){
//                     		iconClass = "fa fa-eye";
//                     	}else if(value == "N" && "${ref_data_role}"== "R1" || "${ref_data_role}"== "R5"){
//                     		iconClass = "fa fa-eye";                   		
//                     	}else {
//                     		iconClass = "";
//                     	}
                    	
                    	// 파일권한 조회인 경우
						// 사용자가 조회권한 이상을 가지고 있으면 조회가능 
                    	if(value == "Y" 
                    			&& ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5" ||
                    				"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                    			){
                    		iconClass = "fa fa-eye";
                    		
                   		// 파일권한 미지정일때 : 사용자권한 체크	
                   		// - 사용자 권한에 따라 
                        }else if((item.ACC_USF_YN == "Y" || value == "N") 
                        			&& ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5" ||
                        				"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                        		){
                        	iconClass = "fa fa-eye";
                        	
                        // 그 외    		
                    	}else {
                    		iconClass = "";
                    	}
                    	
                    	return $("<span>").attr("class", iconClass);
                	}
                }
		    	,{name : "ACC_SAV_YN",	title : "저장",			type : "text",	align : "center",	width : 80, css:"text-truncate",
		    		itemTemplate : function (value, item) {
                    	var iconClass = "";
                    	//권한 체크
//                     	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
//                     		iconClass = "fa fa-download";
//                     	}else 
	
//                     	if(value == "Y" && ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5")){
//                     		iconClass = "fa fa-download";
//                     	}else if(value == "N" && item.ACC_SCH_YN == "Y"){//저장 N 읽기 Y면 읽기만 가능 저장 불가
//                     		iconClass = "";                    		
//                     	}else if(item.ACC_USF_YN == "Y" && "${ref_data_role}"== "R5"){
//                     		iconClass = "fa fa-download";            
//                     	}else {
//                     		iconClass = "";
//                     	}
                    	
                    	// 파일권한 저장인 경우
						// - 사용가 조회권한 이상을 가지고 있으면 저장가능 
                    	if(value == "Y" 
                    				&& ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5" ||
                    					"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                    			){
                    		iconClass = "fa fa-download";
                    		
                    	// 파일권한이 조회인 경우
                    	// - 모든 유저 저장 불가
                    	}else if(item.ACC_SCH_YN == "Y"){
                    		iconClass = "";
                    		
                    	// 파일권한 미지정일때 : 사용자권한 체크
                    	// - 사용자 권한에 따라 
                    	}else if(item.ACC_USF_YN == "Y" 
                    				&& ("${ref_data_role}"== "R5" ||
                    					"${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]")
                    			){
                    		iconClass = "fa fa-download";
                    	
                    	// 그 외	
                    	}else {
                    		iconClass = "";
                    	}
                    	
                    	return $("<span>").attr("class", iconClass);
                	}
                }
		    	,{name : "MOD_DT",		title : "등록일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MOD_ID",		title : "등록자",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    ]
	        ,rowClick: function(args) {
	        	var $target = $(args.event.target);
	        	if($target.closest(".fa.fa-download").length) {
	        		//파일이 없는 경우 체크
	        		if(isEmpty(args.item.FILE_NM)){
	        			alert("파일이 존재하지 않습니다.");
		        		return;
	        		}
	        		var vFileParam = $.param({
	        			"file_name"		: args.item.FILE_NM
	        		    ,"file_name_org": args.item.FILE_ORG_NM
	        		    ,"file_path"	: args.item.FILE_PATH
	        		});
	        		$("#fileDownFrame").attr("src", "<c:url value="/library/downLibraryFile.do" />?"+vFileParam); //exceldown 실행? "src"에 "<c:url value="/ml/predictApplyExcelFileDownload.do" />?"+vFileParam 부여.	        	
	        	}
	        	if($target.closest(".fa.fa-eye").length) {
	        		//파일이 없는 경우 체크
	        		if(isEmpty(args.item.FILE_NM)){
	        			alert("파일이 존재하지 않습니다.");
		        		return;
	        		}
	        		//size set
			       	$('#pdfModal .modal-content').css("height",($(document).height()-80)+"px");	
					$('#pdf').css("height",($(document).height()-170)+"px");	
					$('#pdf').css("max-width","100%");	
					$('#pdf').css("margin","0px");	
					$('#pdfModal').css("z-index","2500");
					$('#pdfModal').modal("show");
	        		var vFileParam = $.param({
	        			"file_name"		: args.item.FILE_NM
	        		    ,"file_name_org": args.item.FILE_ORG_NM
	        		    ,"file_path"	: args.item.FILE_PATH
	        		});
					var options = {
	        			pdfOpenParams: {
	        				navpanes: 0
							,toolbar: 0
	        				,statusbar: 0
	        			    ,view:"FitV"
	        			    ,pagemode:"none"
	        				,page: 1
	        			}
						,forcePDFJS: true
	        			,PDFJS_URL:"<c:url value='/js/common/pdfjs/web/viewer.html'/>"
	        		};
	        		var myPDF = PDFObject.embed("<c:url value="/library/downLibraryFile.do" />?"+vFileParam,"#pdf", options);
	        	}
	        }
			,controller:  {
				loadData : function(filter) {
					if(filter == "key"){
						return $.ajax({
							type:"POST",
							url:"<c:url value='/library/getKeyAllData.do'/>",
							headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
							contentType: "application/json",
							data: JSON.stringify({ 
								REF_NM : $("#popRefNm").val()
							})
						})
					}else{
						return $.ajax({
							type:"POST",
							url:"<c:url value='/library/getAllData.do'/>",
							headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
							contentType: "application/json",
							data: JSON.stringify({ 
								REF_NM : $("#popRefNm").val()
							})
						})
					}
				}
		        ,deleteItem: function(item) {
					$.ajax({
						type:"POST",
						url:"<c:url value='/ml/getDeleteHistory.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify(item)
					}).done(function(result){
						alert("삭제하였습니다.");
					})
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
			if(!/\.(xlsx|xls|xlsm|pdf)$/i.test(file[0].name)){
				alert("Excel, PDF 파일만 선택할 수 있습니다.");
				$("#refNm").val("");
			}else{
				var fileLastIndx = file[0].name.lastIndexOf('.');
				$("#refNm").val(file[0].name.substring(0, fileLastIndx));
				return;
			}
		}
		//초기화
		obj.outerHTML = obj.outerHTML;
	}
	
	//REF ID 생성
	function uuidv4() {
		return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
			return v.toString(16);
		});
	};
	
	//Folder ID 생성
	function uuidv3(pNo) {
		var rNo = Number(pNo)+1;
		return (Number(pNo+1))+'-xxxxxxxx-xxxx-3xxx-yxxx-xxxxxxx'.replace(/[xy]/g, function(c) {
			var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
			return v.toString(16);
		});
	};
	
	//팝업 초기화
	function clearPop() {
		$('#folderId').val(gFolderId);
		$('#docNo').val("");
		$('#refId').val("");
		$('#filePath').val("");
		$('#reviLev').val("");
		$('#refNm').val("");
		$('#creDt').val("");
		$('#regId').val("${userName}");
		$('#regDt').val("<%= sf.format(nowTime) %>");
		$('#input_files').val("");
		$('#fileNm').val("");
		$('#fileOrgNm').text("");
		$('#fileSize').text("");
		$('#target1checked').prop("checked", true);
		$('#target2checked').prop("checked", false);
		$('#target3checked').prop("checked", false);
		$('#keyword1').val("");
		$('#keyword2').val("");
		$('#keyword3').val("");
		$('#keyword4').val("");
		$('#keyword5').val("");
		$('#keyword6').val("");
		$('#keyword7').val("");
		$('#keyword8').val("");
		$('#keyword9').val("");
		$('#keyword10').val("");
	};
	
	//빈값 체크
	function isEmpty(value) { 
		if( value == "" || value == null || value == undefined || ( value != null && typeof value == "object" && !Object.keys(value).length ) ){ return true }else{ return false }
	};
	
	//트리 초기화
	function initTree(){
		myTree = new dhtmlXTreeObject('folder_tree', '100%', '100%', 0);
		myTree.setImagePath("/js/common/dhtmlxtree/imgs/csh_folder/");
		
		myTree.enableDragAndDrop(false); // 폴더 드래깅 불가
		if("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]"){
			myTree.enableItemEditor(true); //항목의 텍스트를 편집 가능
		}else{
			myTree.enableItemEditor(false); //항목의 텍스트를 편집 불가
		}
		
		myTree.attachEvent("onClick", function(id){//클릭시 grid 조회
			$("#jsGrid_library").jsGrid("loadData");
			gFolderId = id;
		});
		myTree.attachEvent("onEdit", function(state, id, tree, value){//edit시 저장
			if(state == 0){
				folderNm = value;
			}
			if(state == 2 && folderNm != value){
				if(id == 1){
					alert("자료실 폴더는 수정이 불가능 합니다.");
					return;
				}
				$.ajax({
					type:"POST",
					url:"<c:url value='/library/editDocTree.do'/>",
					headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
					contentType: "application/json",
					data: JSON.stringify({
						FOLDER_ID : id
						,FOLDER_NM : value
					})
				})
 			}
			return true;
		});
		$.ajax({
			type:"POST",
			url:"<c:url value='/library/searchDocTree.do'/>",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			data: JSON.stringify({
			})
		}).done(function(result){
			myTree.parse({id:0, item: getTreeData(result)}, "json");
			//1레벨 펼치기
        	var folLev = 1;
			var fileIdArray = myTree.getAllFatItems().split(",");
        	for(var i=0; i<fileIdArray.length; i++){
	       		if(myTree.getLevel(fileIdArray[i]) <= folLev){
        			myTree.openItem(fileIdArray[i]);
        		}
        	}
		});
		
		$("#folder_tree").css("height", ($(document).height()-240)+"px");
	};
	
	//트리 구조로 변경
	function getTreeData(array){
		if(array.length>0){
			var map = {};
			for(var i = 0; i < array.length; i++){
				var obj = {"id" : array[i]['FOLDER_ID'], "text" : array[i]['FOLDER_NM']};
		      	obj.item = [];
		      	map[obj.id] = obj;
		      	var parent = array[i]['P_ID'];
		      	if(!map[parent]){
		        	map[parent] = {
		        		item: []
		        	};
		      	}
		      	map[parent].item.push(obj);
		   	}
			return map[0].item;
		}
	}
	
	//1개만 checked 
	function checkOnlyOne(element) {
		const checkboxes = document.getElementsByName("targetChecked");
		for(var i=0; i<checkboxes.length; i++){
			if(checkboxes[i] != element){
				checkboxes[i].checked = false;
		    }
		}
	}	
	
	/*	
		1.문서번호가 동일한데 파일명 다르면 알림(등록진행불가)
		2.파일명이 동일한데 문서번호가 다르면 알림(확인후 등록은 가능)
		3.문서번호와 파일명이 동일하면, 작성일 & 레벨이 달라야 등록
	*/
	function checkConfirm(v_url, formData){
		$.ajax({
	    	url: "<c:url value='/library/confData.do'/>",
			type:"POST",
			headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
			contentType: "application/json",
			async: true,
			data: JSON.stringify({
				DOC_NO : $("#docNo").val()
				,REF_NM : $("#refNm").val()
				,REVI_LEV : $("#reviLev").val()
				,CRE_DT : $("#creDt").val()
				,REF_ID : $("#refId").val()
			}),
	        success: function (result) {
	        	if(result.confFirst>0){
	        		alert("동일한 문서번호가 다른 파일명으로 존재합니다.");
	        		return;
	        	};
	        	if(result.confThird>0){
	        		alert("동일한 문서번호와 파일명이 존재합니다.");
	        		return;
	        	};
	        	if(result.confSecond>0){
	        		confirm("동일한 파일명이 존재합니다. 저장하시겠습니까?", function(result) { 
	                	if(result == true){
	     					$.ajax({
	     			        	url: v_url,
	     			            type: 'POST',
	     			           	enctype: 'multipart/form-data',
	     			            async: true,
	     			            data: formData,
	     			            processData: false,
	     			            contentType: false,
	     			            success: function (result) {
	     			            	alert("저장 되었습니다.");
	     			            	$('#fileUpload').modal("hide");
	     			            	$("#jsGrid_library").jsGrid("loadData");
	     			            }
	     			    	});
	                	}
					});
	        	}else{
	        		$.ajax({
 			        	url: v_url,
 			            type: 'POST',
 			           	enctype: 'multipart/form-data',
 			            async: true,
 			            data: formData,
 			            processData: false,
 			            contentType: false,
 			            success: function (result) {
 			            	alert("저장 되었습니다.");
 			            	$('#fileUpload').modal("hide");
 			            	$("#jsGrid_library").jsGrid("loadData");
 			            }
 			    	});
	        	}	
	        }
		});
	}	
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
	
	
	@media (max-width: 1000px) {
  		#folder_tree {
  			height: 150px !important;
  		}
  		
  		#jsGrid_library {
  			height: 250px !important;
  		}
 	}
	
</style>
</head>

<body>
	<!-- ================  Contents ================  -->
	<div class="container-fluid">
		<div class ="row">	
			<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">
				<div class="row">
					<div class="col-8">
				    	<div class="h5 pt-3">
							<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">자료실</span></strong>
						</div>
					</div>
					
					<div class="col-xl-4  col-lg-4 col-sm-12 text-right pt-1 ">
						<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">        
						<!--  Button -->
							<button id="btn_fileUpload" type="button"class="btn btn-outline-success">자료 등록 <i class="fas fa-file-upload"></i></button>
						</c:if>		
					</div>			
				</div>
				<div class="row">
					<div class="col-lg-3 col-sm-12 pt-2">
						<div class="row">
							<div class="col-12">
								<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">       
								<button id="btn_folder_sav" type="button"class="btn btn-outline-success">폴더 등록 <i class="fas fa-file-upload"></i></button>
								<button id="btn_folder_del" type="button"class="btn btn-outline-success">폴더 삭제 <i class="fas fa-trash-alt"></i></button>							
								<button id="btn_folder_paste"type="button" class="btn btn-outline-secondary">파일 붙여넣기 <i class="far fa-clone"></i></button>
								</c:if>							
							</div>
						</div>
						<div class="row pt-2">
							<div class="col-12">
								<div id="folder_tree" class="dhxtree_basic" ></div>
							</div>
						
						</div>
					</div>
					
					<div class="col-lg-9 col-sm-12 pt-2">
						<div class="row">
							<div class="col-lg-3 col-sm-12 pt-2">
								<input id="v_refNm" type="text" class="form-control form-control-sm"/>
							</div>
							<div class="col-lg-9 col-sm-12 pt-2">
								<button id="btn_std_sch" type="button"class="btn btn-outline-success">파일명 검색 <i class="fa fa-search"></i></button>
								<button id="btn_key_sch" type="button"class="btn btn-outline-success">키워드 검색 <i class="fas fa-key"></i></button>
								<button id="btn_all_sch" type="button"class="btn btn-outline-primary">전체 검색 <i class="fas fa-globe"></i></button>
								<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">       
								<button id="btn_folder_mov"type="button" class="btn btn-outline-secondary">파일 이동 <i class="far fa-paper-plane"></i></button>
								</c:if>
							</div>
						</div>
						<div class="row">
							<div class="col-12 pt-2">
								<div id="jsGrid_library"></div>
							</div>	
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<%-- file upload pop up --%>
	<div class="modal" id="fileUpload" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
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
								<i class="fas fa-search"></i> <strong class="text-primary">자료 등록</strong>
							</p>
						</div>
					</div>
					<input id="folderId" 	type="hidden" class="form-control form-control-sm" placeholder="폴더 ID">
					<input id="refId" 		type="hidden" class="form-control form-control-sm" placeholder="자료 ID">
					<input id="filePath" 	type="hidden" class="form-control form-control-sm" placeholder="파일위치">
					<input id="fileNm" 		type="hidden" class="form-control form-control-sm" placeholder="저장파일 이름">
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> 문서번호  </label>
							</div>
						</div>
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id="docNo" type="text" class="form-control form-control-sm" placeholder="문서번호" maxlength="20">
							</div>
						</div>
					</div>					
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> 파일명 </label>
							</div>
						</div>
						<div class="col-10">
							<div class="pt-2 custom-responsive-p2">
								<input id="refNm" type="text" class="form-control form-control-sm" placeholder="파일명" readonly>
							</div>
						</div>
					</div>	
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label style="font-size: small;"> Rev. </label>
							</div>
						</div>
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id="reviLev" type="text" class="form-control form-control-sm" placeholder="Revision Level" maxlength="20">
							</div>
						</div>
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> 작성일 </label>
							</div>
						</div>					
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id="creDt" class="form-control form-control-sm" type="text" placeholder="작성일" style="display: inline; width: 90%; margin-right: 2%;" readonly/>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> 등록자 </label>
							</div>
						</div>	
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id="regId" type="text" class="form-control form-control-sm" placeholder="등록자" maxlength="20" readonly>
							</div>
						</div>
						<div class="col-2">
							<div class="pt-3 custom-responsive-p2">
								<label> 등록일 </label>
							</div>
						</div>						
						<div class="col-4">
							<div class="pt-2 custom-responsive-p2">
								<input id="regDt" type="text" class="form-control form-control-sm" placeholder="등록일" maxlength="20" readonly>
							</div>
						</div>
					</div>
					<div class="row">
						<div class="col-2 mt-3">
							<div class="custom-responsive-p2">
								<label style="margin-top: -3px;"> 허용권한 </label>
							</div>
						</div>
						<div class="col-8 ml-2 mt-3">
							<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
								<input type="checkbox" class="custom-control-input" id="target1checked" name="targetChecked" onclick='checkOnlyOne(this)'> 
								<label class="custom-control-label" for="target1checked" style="margin-top: -3px;"> 미지정</label>
							</div>
							<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
								<input type="checkbox" class="custom-control-input" id="target2checked" name="targetChecked" onclick='checkOnlyOne(this)'> 
								<label class="custom-control-label" for="target2checked" style="margin-top: -3px;"> 조회</label>
							</div>
							<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
								<input type="checkbox" class="custom-control-input" id="target3checked" name="targetChecked" onclick='checkOnlyOne(this)'> 
								<label class="custom-control-label" for="target3checked" style="margin-top: -3px;"> 저장</label>
							</div>
						</div>
					</div>						
					<div class="row">
						<div id="divFileOrgNm" class="col-6 mt-3">
							<div class="custom-control" style="width: 100%; margin-left: -23px">
								<label style="margin-top: -3px;"><span id="fileOrgNm"></span></label>
							</div>
						</div>
						<div id="divFileSize" class="col-3 mt-3">
							<div class="custom-control" style="width: 100%; margin-left: -10px">
								<label style="margin-top: -3px;"><span id="fileSize"></span></label>
							</div>				
						</div>
					</div>	
					<div class="row">
						<div class="col-12 mt-2">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input name="input_files" id="input_files" type="file" aria-label="files" accept=".pdf, .xls, .xlsx, .xlsm" onchange="fileCheck(this)"/>
							</div>
						</div>						
					</div>	
					<div class="row">
						<div class="col-12">
							<div class="pt-3 custom-responsive-p3">
								<label> 키워드  </label>
							</div>
						</div>						
					</div>	
					<div class="row">
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword1" type="text" class="form-control form-control-sm" placeholder="키워드1" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword2" type="text" class="form-control form-control-sm" placeholder="키워드2" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword3" type="text" class="form-control form-control-sm" placeholder="키워드3" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword4" type="text" class="form-control form-control-sm" placeholder="키워드4" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword5" type="text" class="form-control form-control-sm" placeholder="키워드5" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword6" type="text" class="form-control form-control-sm" placeholder="키워드6" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword7" type="text" class="form-control form-control-sm" placeholder="키워드7" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword8" type="text" class="form-control form-control-sm" placeholder="키워드8" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword9" type="text" class="form-control form-control-sm" placeholder="키워드9" maxlength="20">
							</div>
						</div>	
						<div class="col-4 mt-1">
							<div id="custom-responsive-p2" style="padding-top: 8px">
								<input id="keyword10" type="text" class="form-control form-control-sm" placeholder="키워드10" maxlength="20">
							</div>
						</div>	
					</div>
					<div class="col-12 d-flex flex-row-reverse">
						<div class="p-2 custom-responsive-p2">
							<button id="savFileData" type="button"class="btn btn-outline-success">저장 <i class="far fa-save"></i></button>
							<button id="editFileData" type="button"class="btn btn-outline-success">수정 <i class="far fa-edit"></i></button>
							<button id="delFileData" type="button"class="btn btn-outline-success">삭제 <i class="fas fa-trash-alt"></i></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<%-- file upload pop up --%>
	<div class="modal" id="pdfModal" role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body modal-lg">
					<div id="pdf"></div>
				</div>
			</div>		
		</div>
	</div>
	
	<%-- sch all pop up --%>
	<div class="modal" id="schAll">
		<div class="modal-dialog modal-lg">
			<div class="modal-content">
			
				<div class="modal-header">
					<div style="font-size: 24px; float: left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body">
					<div class="row">
						<div class="col-12">
							<p class="h5" style="width: 100%;">
								<i class="fas fa-search"></i> <strong class="text-primary">전체 검색</strong>
							</p>
						</div>
					</div>
					<div class="row">
						<div class="col-lg-5 col-sm-12 pt-2">
							<input id="popRefNm" type="text" class="form-control form-control-sm"/>
						</div>
						<div class="col-lg-7 col-sm-12 pt-2">
							<button id="pop_btn_sch" type="button"class="btn btn-outline-success">파일명 검색 <i class="fa fa-search"></i></button>
							<button id="pop_key_sch" type="button"class="btn btn-outline-success">키워드 검색 <i class="fas fa-key"></i></button>
						</div>
					</div>
					<div class="row">
						<div class="col-12 pt-2">
							<div id="jsGrid_allData"></div>
						</div>		
					</div>		
				</div>
			</div>		
		</div>
	</div>
	<iframe id="fileDownFrame" style="display:none;"></iframe>
</body>
</html>