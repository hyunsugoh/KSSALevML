<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<style>
.tree-elem-style, tree-node-elem-style{
	font-size:14px;
}
.one-dept-node{
	padding-left:30px;
}
.two-dept-node{
	padding-left:60px;
}
.tree-elem-style:hover{
	font-weight : bold;
	cursor:pointer;
}
.tree-icon-btn{
	padding:4px;
}
.font-size-down{
	font-size:12px;
}
</style>
<script src="<c:url value='/js/common/jui/core/jui-core.js'/>"></script>
<script src="<c:url value='/js/common/jui/ui/jui-ui.js'/>"></script>
<script src="<c:url value='/js/common/jui/grid/jui-grid.js'/>"></script>

<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-ui.classic.css'/>">
<link rel="stylesheet" href="<c:url value='/css/common/jui/jui-grid.classic.css'/>">
<script src="<c:url value='/js/admin/adminMLGroupInfo.js'/>"></script>
<script>

var excelGridData = null; 
adminMLGroupInfo.init();
$(document).ready(function() {
	adminMLGroupInfo.evtBind();	
});

/* $("#downHieraBtn").click(function(){
	console.log("Start");
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
	        	$("#fileDownFrame").attr("src", "<c:url value="/ml/predictApplyExcelFileDownload2.do" />?"+vFileParam); //? 사용하여 파일명을 GET방식 parameter(vFileParam = 생성된 엑셀 파일명) 넘김.
	        }else{
	        	alert("처리중 오류가 발생하였습니다.");
	        }
        },
	}).fail(commonFunc.ajaxFailAction);
}); */
</script>
<!-- ML 그룹 정보 -->
<div class="col-md-12 col-lg-12 col-xl-12 col-12 ml-auto mr-auto px-4">
	<div class="row">
		<div class="col-12 pt-3">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="far fa-object-ungroup"></i><span class="ml-1">ML 그룹 정보</span></strong>
					</div>
				</div>
				<div class="col-6">
					<div class="d-flex justify-content-end">
						<button id="opemMlDownPop" type="button" class="btn btn-primary mr-2">다운로드</button>
						<button id="upLoadMLGroupInfo" type="button" class="btn btn-success mr-2">업로드</button>
						<div id="helpIcon" class="pt-0"></div>
					</div>
				</div>
			</div>
		</div>
		<div class="col-12 pt-2">
			<div class="row">
				<div class="col-md-4 mb-3">
				<!-- tree -->
					<div class="card" style="height:370px;" class="overflow-auto">
						<div id="treeLoadingBar" class="d-flex align-items-center justify-content-center">
							<p><i class="fa fa-spin fa-spinner"></i> 데이터를 불러오는 중...</p>
						</div>
						
						<div id="codeJsGrid"></div>
					</div>
				</div>
				
				<div class="col-md-8 pl-3 pr-3">
				<!-- input -->
					<div class="card border-secondary mb-3">
						<div class="card-header">
							<h4><strong>코드 세부정보</strong></h4>
						</div>
						<div class="card-body text-secondary">
							<form id="formCodeGrp" class="p-3">
								<div id="formPropertyGrp" class="form-group row">
									<label for="formProperty" class="col-sm-4 col-form-label">속성</label>
									<div class="col-sm-6">
										<input type="text" readonly class="form-control-plaintext" id="formProperty" value="">
										<small id="propUpdateHelpBlock" class="form-text text-muted"></small>
									</div>
								</div>
								 
								<div id="formPropertyAddGrp" class="form-group row d-none">
									<label for="formAddProperty" class="col-sm-4 col-form-label">속성</label>
									<div class="col-sm-6">
										<select id="formAddProperty" class="form-control" >
											<option value="default"> - 코드 속성을 선택하십시오 - </option>
											<option value="fea">그룹</option>
											<option value="value">키워드</option>
										</select>
										<small id="propertyHelpBlock" class="form-text text-muted"></small>
									</div>
								 </div>
 								 <div id="formParentAddGrp" class="form-group row d-none">
								 	<label for="formAddParent" class="col-sm-4 col-form-label">상위 코드 <span id="codeStateBadge" class="badge"></span></label>
									<div class="col-sm-6">
										<select id="formAddParent" class="form-control"></select>
									</div>
								 </div>
								 <div class="form-group row">
								 	<label for="formCodeName" class="col-sm-4 col-form-label">단어(코드)</label>
									<div class="col-sm-6">
										<input type="text" readonly class="form-control-plaintext" id="formCodeName" value="">
										<small id="formCodeNameHelpBlock" class="form-text text-muted"></small>
									</div>
								</div>
								<div id="codeStrGrp" class="form-group row">
									<label for="formCodeStrName" class="col-sm-4 col-form-label">영문/한글 명</label>
									<div class="col-sm-6">
										<input type="text" readonly class="form-control-plaintext" id="formCodeStrName" value="">
										<small id="formCodeStrNameHelpBlock" class="form-text text-muted"></small>
									</div>
								</div>
								<button id="confirmBtn" class="d-none btn btn-success">확인</button>
								<button id="cencelsBtn" class="d-none btn btn-info">취소</button>
							</form>						
						</div>
						<div class="card-footer bg-transparent border-secondary d-flex justify-content-end">
							<button id="addMLCode" type="button" class="btn btn-primary mr-2">추가</button>
							<button id="updateMLCode" type="button" class="btn btn-success mr-2">수정</button>
							<button id="delMLCode" type="button" class="btn btn-secondary">삭제</button>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="col-12 pt-2">
			<div class="row">
				<div class="col-xl-3 col-lg-4 col-md-3  col-sm-12">
					<div class="h5" style="float: left;">
						<strong><i class="far fa-object-ungroup"></i><span class="ml-1"> Product 그룹 Hierarchy 정보</span></strong>
					</div>
					<div class="h6 text-left mt-1" style="">
						<span class="ml-2" id="search_cnt_title"></span>
					</div>
				</div>
				<div class="col-9 text-right">
					<div class="d-flex justify-content-end">
						<button id="downHieraInfo" type="button" class="btn btn-primary mr-2">다운로드</button>
						<button id="upLoadHieraInfo" type="button" class="btn btn-success mr-2">업로드</button>
					</div>			
				</div>
			</div>
		</div>	
		
		<div class="col-12 pt-2">
			<div id="hidCodeGrid"></div>
			<div class="row">
				<div class="col-12">
					<div id="externalPager" class="p-2" style="text-align: center;"></div>
				</div> 
			</div>
		</div>
	</div>
</div>

<div class="modal fade" id="hieraAddModal">
	
	<div class="modal-dialog" >
		<div class="modal-content" style="height: 600px;width:600px;">

			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title">HIERA 정보 추가</div>
	              	<div style="font-size:24px;float:left;">&nbsp;&nbsp;&nbsp;</div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			<!-- Modal body -->
			<div class="modal-body">
				<div class="row">
					<div class="col-12 mt-3">
						
							<div class="row">
									
							<table class="table">
			                    <tr>
			                        <th>LEVEL_1</th>
			                        <td><input type="text" id="level1" name="level1" placeholder="LEVEL_1" /></td>
			                    </tr>
			                    <tr>
			                        <th>LEVEL_2</th>
			                        <td><input type="text" id="level2" name="level2" placeholder="LEVEL_2" /></td>
			                    </tr>
			                    <tr>
			                        <th>LEVEL_3</th>
			                        <td><input type="text" id="level3" name="level3" placeholder="LEVEL_3" /></td>
			                    </tr>
			               <!-- <tr>
			                        <th>LEVEL_4</th>
			                        <td><input type="text" id="level4" name="level4" placeholder="LEVEL_4" /></td>
			                    </tr>
			                    <tr>
			                        <th>LEVEL_5</th>
			                        <td><input type="text" id="level5" name="level5" placeholder="LEVEL_5" /></td>
			                    </tr> -->
			                    
			                </table>
								
							</div>
	
						
						</div>		
					</div>	
					
					<div class="row">
						<div class="col-12" >
							<!--  Button -->
							<div class="col-12 mt-3 text-right" >
								<button type="button" class="btn btn-outline-success"  id="addHieraBtn">저장 <i class="fa fa-save"></i></button>
							</div>
						</div>
					</div>
							
			</div>

		</div>
	</div>
</div>
<%-- file upload pop --%>
<div class="modal" id=mLGroupInfoExcelUpload  role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg" >
		<div class="modal-content" style="height: 500px;">
			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title" >ML Group Excel File Upload</div>
	            <div style="font-size:24px;float:left;"></div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			<!-- Modal body -->
			<div class="modal-body">
				<div class="row">
					<div class="col-12" >
						<select id="mlGroupSelUpload" class="form-control input-small p-0 pl-1" style="width: 33%; font-size: 20px;">
							<option value='GRML0001'>Product</option>
							<option value='GRML0003'>Equip Mfg</option>
							<option value='GRML0002'>Pump Type</option>
							<option value='GRML0004'>Ulitmate User</option>
						</select>
					</div>
				</div>
				<div class="popup_area">
					<div id="file_uploader" style="margin-top:10px;">
						<input name="input_files"  id="input_files"  type="file" aria-label="files" accept=".xls, .xlsx, .xlsm" onchange="adminMLGroupInfo.confExcelGrid(this)"/>
					</div>
				</div>
				<div class="row">
					<div class="col-12 pt-2">
						<div id="jsGrid_confExcel"></div>
					</div>
					<div class="col-12 pt-1">
						<p class="font-italic text-info">상위 10건의 데이터 입니다.</p>
					</div>	
				</div>	
				<!-- 저장된 temp파일 이름 -->
				<input id="fileNm" type="hidden" class="form-control form-control-sm" placeholder="저장파일 이름">
				<div class="row">
					<div class="col-12" >
						<!--  Button -->
						<div class="col-12 mt-3 text-right" >
							<button type="button" class="btn btn-outline-success"  id="mLGroupInfo_btn_excel_upload_ok">Upload <i class="fa fa-save"></i></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<div class="modal" id=hiearExcelUpload  role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog modal-lg" >
		<div class="modal-content" style="height: 300px;">
		
			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title" >Hier Excel File Upload</div>
	              	<div style="font-size:24px;float:left;"></div>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			
			<!-- Modal body -->
			<div class="modal-body">
				<div class="popup_area">
					<div id="file_uploader" style="margin-top:20px;">
						<input name="input_files2"  id="input_files2"  type="file" aria-label="files" />
					</div>
				</div>	
				<div class="row">
					<div class="col-12" >
						<!--  Button -->
						<div class="col-12 mt-3 text-right" >
							<button type="button" class="btn btn-outline-success"  id="hiearInfo_btn_excel_upload_ok">Upload <i class="fa fa-save"></i></button>
						</div>
					</div>
				</div>
			</div>
			
		</div>
	</div>
</div>

<!-- ML그룹 정보 다운로드 -->
<div class="modal" id=mlGroupInfoPop  role="dialog" aria-hidden="false" data-backdrop="static" data-keyboard="false">
	<div class="modal-dialog" >
		<div class="modal-content">
		
			<!-- Modal Header -->
			<div class="modal-header">
				<div class=" h5 modal-title" >ML Group File Download</div>
	            <div style="font-size:24px;float:left;"></div>
				<button type="button" class="close" data-dismiss="modal">&times;</button>
			</div>
			
			<!-- Modal body -->
			<div class="modal-body">
				<div class="row">
					<div class="col-9">
						<select id="mlGroupSel" class="form-control input-small p-0 pl-1" style="width: 100%; font-size: 20px;">
							<option value='GRML0001'>Product</option>
							<option value='GRML0003'>Equip Mfg</option>
							<option value='GRML0002'>Pump Type</option>
							<option value='GRML0004'>Ulitmate User</option>
						</select>
					</div>
					<div class="col-3 text-right">
						<!--  Button -->
						<button id="downMLGroupInfo" type="button" class="btn btn-primary mr-2">다운로드</button>
					</div>
				</div>	
			</div>
		</div>
	</div>
</div>
<iframe id="fileDownFrame" style="display:none;"></iframe>