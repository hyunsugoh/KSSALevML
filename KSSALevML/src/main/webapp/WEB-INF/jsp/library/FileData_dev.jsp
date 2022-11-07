<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<link rel="shortcut icon" href="<c:url value='/images/common/ci/ksm_favi.ico'/>">

<meta charset="utf-8">
<title></title>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">
<!-- 뷰어 -->
<script src="<c:url value='/js/common/pdfobject/pdfobject.min.js'/>"></script>
<script>
	$(document).ready(function(){
		libraryGridInit();
		$("#jsGrid_library").jsGrid("loadData");
		//관리자가 아닌 경우 관리 컬럼 hide
		if("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]"){
			$("#jsGrid_library").jsGrid("fieldOption", "MNG_YN", "visible", true);
		}else{
			$("#jsGrid_library").jsGrid("fieldOption", "MNG_YN", "visible", false);
		}
		
		<%-- file upload button Event --%>
		$("#btn_fileUpload").click(function(){
			clearPop();
			//button hide & show
			$('#editFileData').css("display", "none");
			$('#delFileData').css("display", "none");
			$('#divFileOrgNm').css("display", "none");
			$('#divFileSize').css("display", "none");
			$('#savFileData').css("display", "block");
			//popup show
			$('#fileUpload').modal("show");
		});
		
		<%-- file save button Event --%>
		$("#savFileData").click(function(){
			var formData = new FormData();
			
			//필수값 체크
			if(isEmpty($("#refNm").val())){
				alert("파일명을 입력해 주세요");
				return;
			}
			formData.append("REF_ID", uuidv4());
			formData.append("REF_NM", $("#refNm").val());
			formData.append("target1_check", $("#target1checked").is(":checked"));
			formData.append("target2_check", $("#target2checked").is(":checked"));
			
			if($('#input_files').prop('files').length==0){
				formData.append("NEW_YN", "Y");
				formData.append("FILE_PATH", "");
				formData.append("FILE_NAME", "");
				$.ajax({
		        	url: "<c:url value='/library/noFile.do'/>",
		            type: 'POST',
		            data: formData,
		            processData: false,
		            contentType: false,
		            success: function (result) {
		            	alert("저장 되었습니다.");
		            	$('#fileUpload').modal("hide");
		            	$("#jsGrid_library").jsGrid("loadData");
		            }
		    	});
			}else{
				formData.append("files", $('#input_files').prop('files')[0]);
				$.ajax({
		        	url: '/library/savLibrary.do',
		            type: 'POST',
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
		
		<%-- file delete button Event --%>
		$("#delFileData").click(function(){
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
		});
		
		<%-- file save button Event --%>
		$("#editFileData").click(function(){
			var formData = new FormData();
			//필수값 체크
			if(isEmpty($("#refNm").val())){
				alert("파일명을 입력해 주세요");
				return;
			}
		
			formData.append("REF_ID", $("#refId").val());
			formData.append("REF_NM", $("#refNm").val());
			formData.append("FILE_PATH", $("#filePath").val());
			formData.append("FILE_NAME", $("#fileNm").val());
			formData.append("target1_check", $("#target1checked").is(":checked"));
			formData.append("target2_check", $("#target2checked").is(":checked"));

			if($('#input_files').prop('files').length==0){
				formData.append("NEW_YN", "N");
				$.ajax({
		        	url: "<c:url value='/library/noFile.do'/>",
		            type: 'POST',
		            data: formData,
		            processData: false,
		            contentType: false,
		            success: function (result) {
		            	alert("저장 되었습니다.");
		            	$('#fileUpload').modal("hide");
		            	$("#jsGrid_library").jsGrid("loadData");
		            }
		    	});
			}else{
				formData.append("files", $('#input_files').prop('files')[0]);
		        $.ajax({
		        	url: "<c:url value='/library/editLibrary.do'/>",
		            type: 'POST',
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
		
// 		const zNodes =[
// 		    { id : "1", name:"최상위1" },
// 		    { id : "11", pId : "1", name:"최상위1의 하위1"},
// 		    { id : "12", pId : "1", name:"최상위1의 하위2"},
// 		    { id : "2", name:"최상위2" },
// 		    { id : "21", pId : "2", name:"최상위2의 하위1"},
// 		    { id : "22", pId : "2", name:"최상위2의 하위2"},
// 		];
// 		const setting = {
// 		    data: {
// 		        simpleData: {
// 		            enable: true,
// 		        }
// 		    }
// 		}
		
// 		$.fn.zTree.init($("#treeDemo"), setting, zNodes);
	}); <%-- end  $(document).ready(function(){ --%>
	
	//init grid
	function libraryGridInit(){
		$("#jsGrid_library").jsGrid('destroy');
		$("#jsGrid_library").jsGrid({
			width: "100%",
		    height: $(document).height()-200,
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
	                    	$("#jsGrid_library").jsGrid('deleteItem', args.item); //call deleting once more in callback
	                	}
					});
				}
	        },
		    fields: [
		    	{name : "REF_ID",		title : "REF_ID",		type : "text",	align : "center",	width : 80, visible:false}
		    	,{name : "REF_NM",		title : "자료명",			type : "text",	align : "left",		width : 250,css:"text-truncate"}
		    	,{name : "ACC_SCH_YN",	title : "조회",			type : "text",	align : "center",	width : 80, css:"text-truncate",
		    		itemTemplate : function (value, item) {
                    	var iconClass = "";
                    	//권한 체크
                    	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
                        	iconClass = "fa fa-eye"; 
                    	}else{
	                    	if (value == "Y" && ("${ref_data_role}"== "R1" || "${ref_data_role}"== "R5")) {
	                        	iconClass = "fa fa-eye";
	                    	}
                    	}
                    	return $("<span>").attr("class", iconClass);
                	}
                }
		    	,{name : "ACC_SAV_YN",	title : "저장",			type : "text",	align : "center",	width : 80, css:"text-truncate",
		    		itemTemplate : function (value, item) {
                    	var iconClass = "";
                    	//권한 체크
                    	if ("${role}"=="[ROLE_ADMIN]" || "${role}"=="[ROLE_SUPER]") {
                    		iconClass = "fa fa-download";
                    	}else{
	                    	if (value == "Y" && "${ref_data_role}"== "R5") {
	                    		iconClass = "fa fa-download"; 
	                    	}
                    	}
                    	return $("<span>").attr("class", iconClass);
                	}
                }
		    	,{name : "REG_DT",		title : "등록일",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "REG_ID",		title : "등록자",			type : "text",	align : "center",	width : 100,css:"text-truncate"}
		    	,{name : "MNG_YN",		title : "관리",			type : "text",	align : "center",	width : 80,	css:"text-truncate",
	            	itemTemplate: function(_, item) {
			            var $rtnDiv = $("<div>");
						$rtnDiv.append("<button>");
						$rtnDiv.find("button").addClass("jsgrid-button jsgrid-edit-button");
						return $rtnDiv.find("button").on("click", function() {
							clearPop();
							//data set
							$('#refId').val(item.REF_ID);
							$('#filePath').val(item.FILE_PATH);
							$('#fileNm').val(item.FILE_NM);
							$('#refNm').val(item.REF_NM);
							if(isEmpty(item.FILE_ORG_NM)){
								$('#divFileOrgNm').css("display", "none");
							}else{
								$('#divFileOrgNm').css("display", "inline");
								$('#fileOrgNm').text("파일명 : "+item.FILE_ORG_NM);
							}
							if(isEmpty(item.FILE_SIZE)){
								$('#divFileSize').css("display", "none");
							}else{
								$('#divFileSize').css("display", "inline");
								$('#fileSize').text("용량 : "+item.FILE_SIZE+" KB");
							}
							if(item.ACC_SCH_YN=="Y"){
								$('#target1checked').prop("checked", true);
							}else{
								$('#target1checked').prop("checked", false);
							}
							if(item.ACC_SAV_YN=="Y"){
								$('#target2checked').prop("checked", true);
							}else{
								$('#target2checked').prop("checked", false);
							}
							//button hide & show
							$('#editFileData').css("display", "inline");
							$('#delFileData').css("display", "inline");
							$('#savFileData').css("display", "none");
							//popup show
							$('#fileUpload').modal("show");						
							return false;
						});
					}	
		    	}
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
					return $.ajax({
						type:"POST",
						url:"<c:url value='/library/getLibraryList.do'/>",
						headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
						contentType: "application/json",
						data: JSON.stringify({})
					})
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
				alert("Excel, PDF 파일만 선택할 수 있습니다.")
			}else{
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
	}
	
	//팝업 초기화
	function clearPop() {
		$('#refId').val("");
		$('#filePath').val("");
		$('#fileNm').val("");
		$('#refNm').val("");
		$('#input_files').val("");
		$('#fileOrgNm').text("");
		$('#fileSize').text("");
		$('#target1checked').prop("checked", true);
		$('#target2checked').prop("checked", true);
	}
	
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
							<strong><i class="far fa-object-ungroup"></i> <span class="ml-1">자료실</span></strong>
						</div>
					</div>
					<div class="col-6">
						<div class="pt-2 mr-2 text-right">
							<c:if test="${role == '[ROLE_SUPER]' || role =='[ROLE_ADMIN]'}">        
								<button id="btn_fileUpload" type="button"class="btn btn-outline-success">자료 등록 <i class="fas fa-file-upload"></i></button>
							</c:if>
						</div>
					</div>
				</div>
				<div class="row">
<!-- 					<div class="col-4 pt-2"> -->
<!-- 					tree -->
<!-- 						<div class="card" style="height:370px;" class="overflow-auto"> -->
<!-- 							<ul id="treeDemo" class="ztree"></ul> -->
<!-- 						</div> -->
<!-- 					</div> -->
					<div class="col-12 pt-2">
						<div id="jsGrid_library"></div>
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
					<input id="refId" 		type="hidden" class="form-control form-control-sm" placeholder="자료 ID">
					<input id="filePath" 	type="hidden" class="form-control form-control-sm" placeholder="파일위치">
					<input id="fileNm" 		type="hidden" class="form-control form-control-sm" placeholder="저장파일 이름">
					<div class="row">
						<div class="col-12">
							<div class="pt-2 custom-responsive-p2">
								<input id="refNm" type="text" class="form-control form-control-sm" placeholder="자료명" maxlength="20">
							</div>
						</div>
						<div id="divFileOrgNm" class="col-8 mt-2">
							<div class="custom-control" style="width: 100%; margin-left: -10px">
								<label style="margin-top: -3px;"><span id="fileOrgNm"></span></label>
							</div>
						</div>
						<div id="divFileSize" class="col-3 mt-2">
							<div class="custom-control" style="width: 100%; margin-left: -10px">
								<label style="margin-top: -3px;"><span id="fileSize"></span></label>
							</div>				
						</div>
						<div class="col-12 mt-2">
							<div id="custom-responsive-p2" style="padding: 8px">
								<input name="input_files" id="input_files" type="file" aria-label="files" accept=".pdf, .xls, .xlsx, .xlsm" onchange="fileCheck(this)"/>
							</div>
						</div>						
						<div class="col-3 mt-3">
							<div class="custom-control" style="width: 100%; margin-left: -10px">
								<label style="margin-top: -3px;"> 허용권한 :</label>
							</div>
						</div>
						<div class="col-8 ml-2 mt-3">
							<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
								<input type="checkbox" class="custom-control-input" id="target1checked" checked> 
								<label class="custom-control-label" for="target1checked" style="margin-top: -3px;"> 조회</label>
							</div>
							<div class="custom-control custom-checkbox " style="width: 30%; background: url();">
								<input type="checkbox" class="custom-control-input" id="target2checked" checked> 
								<label class="custom-control-label" for="target2checked" style="margin-top: -3px;"> 저장</label>
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
	<iframe id="fileDownFrame" style="display:none;"></iframe>
</body>
</html>