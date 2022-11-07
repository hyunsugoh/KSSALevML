<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script>
var mainProjectId;
var selectSubProjectID;
var subProjectList = [];

$(document).ready(function() {
	getProjectList();
	
	$("#createProjectBtn").click(function(e){ //메인 프로젝트 생성 버튼
		$('#modalProjectCreate').modal("show");
	});
	 
	$("#modalProjectCreate_confirm_ok").click(function(e){ //메인 프로젝트 저장 버튼
		var param = { //insert parameter
			label : $("#label").val(),
			description  : $("#description").val()
		};
		if(param.label == "" || param.description == "" ){
			alert("내용을 모두 입력하여 주십시오.")
			return;
		}else{
			$.doPost({
				url : "/project/insertProject.do", 
				data : param,
				success : function(data, status, xhr){
					if(data.msg === "success"){
						alert("저장되었습니다.");
						getProjectList(); //끝날시 프로젝트 리스트 함수 재호출
					    $('#label').val("");
						$('#description').val("");
					}else{
						alert("등록되지 않았습니다.");
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					commonFunc.ajaxFailAction(jqxXHR);
				}
			});
		}
	});
	 
	//서브 생성버튼
	$("#createSubProjectBtn").click(function(e){ 
		$('#modalSubProjectCreate').modal("show");
	});
	 
	$("#modalSubProjectCreate_confirm_ok").click(function(e){ //서브 프로젝트 저장버튼
		var param = { //insert parameter
			label : $("#subProjectLabel").val(),
			description  : $("#subProjectDescription").val(),
			mainProjectId : mainProjectId,
			status : "I"
		};
		if(param.label ==  "" || param.description == "" ){
			alert("내용을 모두 입력하여 주십시오.")
			return false;
		}else{
			$.doPost({
				url : "/project/mngSubProject.do", 
				data : param,
				success : function(data, status, xhr){
					if(data.msg === "success"){
						selectProject(mainProjectId);
					   	$('#subProjectLabel').val("");
					   	$('#subProjectDescription').val("");
					}else{
						alert("등록되지 않았습니다..");
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					commonFunc.ajaxFailAction(jqxXHR);
				}
			})
		}
	});
	
	$("#modalSubProjectUpdate_confirm_ok").click(function(e){ //서브 프로젝트 수정버튼
		var param = { //insert parameter
			label : $("#subProjectLabelUpdate").val(),
			description  : $("#subProjectDescriptionUpdate").val(),
			subProjectId : selectSubProjectID,
			status : "U"
		};
		if(param.label ==  "" || param.description == "" ){
			alert("수정항목 내용을 모두 입력하여 주십시오.")
			return false;
		}else{
			$.doPost({
				url : "/project/mngSubProject.do", 
				data : param,  // JSON 문자열 형식의 데이터로 전송 ex: JSON.stringify (value,replacer,space)
				success : function(data, status, xhr){
					if(data.msg === "success"){
						selectProject(mainProjectId);
					   	$('#subProjectLabel').val("");
					   	$('#subProjectDescription').val("");
					}else{
						alert("등록되지 않았습니다..");
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					commonFunc.ajaxFailAction(jqxXHR);
				}
			})
		}
	});
	 
	$(document).on("mouseenter",'.levml-va-workspace-model-list-item',function(){ //mouserenter
		$(this).css('border','1px solid #673ab7');	
		$(this.lastChild).css('display','block'); //마우스 enter시 열기 버튼 block	
	});
	$(document).on("mouseleave",'.levml-va-workspace-model-list-item',function(){
		$(this).css('border','');
		$(this.lastChild).css('display','none');
	}); 
});

function getProjectList(){
	$.doPost({
		url:"/project/getProjectList.do",
		success : function(data, status, xhr){
			console.log("------------------------data------------------------");
			console.log(data);
			createElements(data);
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
}	

function selectProject(selectId){
	$.doPost({
		data : { projectId : selectId },
		url:"/project/selectProject.do",
		success : function(data, status, xhr){
			if(status === "success"){
				setProjectInfo(data);
			}else{
				alert("데이터를 받아오지 못했습니다.");
			}
		},
		error : function(jqxXHR, textStatus, errorThrown){
			commonFunc.ajaxFailAction(jqxXHR);
		}
	});
}

function setProjectInfo(data){
	$("#createSubProjectBtn").show();
	var projectid = data.projectInfo.ID //메인프로젝트ID
	mainProjectId = projectid; //메인프로젝트 ID값 전역변수에 대입
	//main project setting
	var projectLabel = data.projectInfo.LABEL //label
	$(".lev-va-workspace-model-list-title").attr('title',projectLabel); //title에 삽입
	var labelText = $('.lev-va-workspace-model-list-title').attr('title'); //title의 text값 추출
	$("#projectLabel").text(labelText); // text 값 삽입.
	$("#mainProjectLabel").text(labelText);
	
	var projectCreateDt = data.projectInfo.CREATE_DT;
	$("#projectCreateDt").text(projectCreateDt);
	var projectCreateId = data.projectInfo.CREATE_ID;
	$("#projectCreateId").text(projectCreateId);
	
	//subProjectList info
	subProjectList = data.record;
	str = "";
	for(var i=0; i<data.record.length; i++){ //subproject list 길이만큼 생성필요 ==> div 동적생성?
			str += '<div class="levml-va-workspace-model-list-cell" >'
			str += '<div class="levml-va-workspace-model-list-item">';
			str += '<div class="levml-va-workspace-model-list-item-header">';
			str += '<div class="levml-va-workspace-model-list-item-icon"></div>';
			str += '<div class="levml-va-workspace-model-list-item-type levml-style-width-minus-250"></div>';
			str += '<div class="levml-va-workspace-model-list-item-version"></div>';
			str += '<div class="levml-va-workspace-model-list-item-select jqx-widget jqx-widget-office jqx-checkbox jqx-checkbox-office"></div>';
			str += '<div class="levml-va-workspace-model-list-item-edit"></div>';
			str += '</div>';
			str += '<div class="d-flex justify-content-between">';
			str += '<div class="levml-va-workspace-model-list-item-label" title=""><span id="subProjectInfoLabel'+i+'"></span>'
				 + '</div>';
			str += '<div></div>';
			str += '<div class="subproject col-4"><button type="button" class="btn btn-outline-info" style="margin-right:2px;" id="subProjectUpdataBtn" onclick="select(\'subProjectId'+i+'\',\'subProjectInfoLabel'+i+'\',\'subProjectInfoDescription'+i+'\');"><i class="far fa-edit"></i></button>';
			str +=  '<button type="button" class="btn btn-outline-danger" id="subProjectDeleteBtn" style="margin-right:2px;" onclick="subDelete(\'subProjectId'+i+'\');"><i class="far fa-trash-alt"></i></button>';
			str +=  '<button type="button" class="btn btn-outline-info" id="subProjectCopyBtn" onclick="subCopy(\'subProjectId'+i+'\');"><i class="far fa-copy"></i></button>';
			str += '</div>'
			str += '</div>'
			str += '<div class="levml-va-workspace-model-list-item-updatetime">';
			str += '<div id="subProjectId'+i+'" style="display:none;"></div>';
			str += '<div>수정일 :</div>&nbsp;&nbsp;';				
			str += '<div id="subProjectInfoUpdateDt'+i+'"></div>&nbsp;&nbsp;&nbsp;&nbsp;'; 				
			str += '<div>수정ID :</div>&nbsp;&nbsp;';				
			str += '<div id="subProjectInfoUpdateId'+i+'"></div>';				
			str += '</div>';			
			str += '<div class="levml-va-workspace-model-list-item-createtime">';			
			str += '<div>생성일 :</div>&nbsp;&nbsp;';			
			str += '<div id="subProjectInfoCreateDt'+i+'"></div>&nbsp;&nbsp;&nbsp;&nbsp;';				
			str += '<div>생성ID :</div>&nbsp;&nbsp;';				
			str += '<div id="subProjectInfoCreateId'+i+'"></div>';				
			str += '</div>';			
			str += '<div class="levml-va-workspace-model-list-item-description">';		
			str += '<textarea readonly="readonly" rows="4" cols="8" id="subProjectInfoDescription'+i+'"></textarea>';			
			str += '</div>';			
			str += '<div class="levml-va-workspace-model-list-item-open" id="openBtn" onclick="openPage(\'subProjectId'+i+'\'); ">열기</div>';		
			str += '</div>';		
			str += '</div>';
	}
	$('#subProjectDiv').html(str);
	
	for(var j=0; j<data.record.length; j++){ // subproject info data 삽입
		$("#subProjectId"+j).text(data.record[j].ID);
		$("#subProjectInfoLabel"+j).text(data.record[j].LABEL); //서브프로젝트 subProjectLabel
		$("#subProjectInfoUpdateDt"+j).text(data.record[j].UPDATE_DT); //서브프로젝트 updateDt
		$("#subProjectInfoUpdateId"+j).text(data.record[j].UPDATE_ID); //서브프로젝트 updateId
		$("#subProjectInfoCreateDt"+j).text(data.record[j].CREATE_DT); //서브프로젝트 createDt
		$("#subProjectInfoCreateId"+j).text(data.record[j].CREATE_ID); //서브프로젝트 createId
		$("#subProjectInfoDescription"+j).text(data.record[j].DESCRIPTION); //서브프로젝트 description
	}
}

function select(subProjectId,subProjectLabel,subProjectDescription){
	$('#modalSubProjectUpdate').modal("show");
	var labelText = $('.lev-va-workspace-model-list-title').attr('title'); 
 	$("#mainProjectLabelUpdate").text(labelText);
 	var subprtid = $("#"+subProjectId).text(); //선택한 subprojctID
	selectSubProjectID = subprtid; //선택한 서브프로젝트 전역변수 대입.
 	var subprtlabel = $("#"+subProjectLabel).text();
 	var subprtdescription =$("#"+subProjectDescription).text()
 	$("#subProjectLabelUpdate").val(subprtlabel); //input 
 	//$("#subProjectDescriptionUpdate").text(subprtdescription); //textarea
 	$("#subProjectDescriptionUpdate").val(subprtdescription); //textarea
}

function subDelete(subProjectId){
	var subprtid = $("#"+subProjectId).text(); //선택한 subprojctID
	var param = { //insert parameter
		subProjectId : subprtid,
		status:"D"
	};
	confirm({
		message:'<p class="text-center">해당 서브 프로젝트를 삭제하시겠습니까?</p>',
		title:'<h6>삭제하기</h6>',
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
	},function(result){
		if(result){
			$.doPost({
				url:"/project/mngSubProject.do",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 CSRF
			    data : param,
				success : function(data, status, xhr){
					if(data.msg === "success"){
						selectProject(mainProjectId);
					}else{
						alert("데이터를 삭제하지 못했습니다.")
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					commonFunc.ajaxFailAction(jqxXHR);
				}
			});
		}
	});
}

function subCopy(subProjectId){
	var subprtid = $("#"+subProjectId).text(); //선택한 subprojctID
	var param = { //insert parameter
		copyProjectId : subprtid,
		status:"C"
	};
	confirm({
		message:'<p class="text-center">해당 서브 프로젝트를 복제하시겠습니까?</p>',
		title:'<h6>복제하기</h6>',
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
	},function(result){
		if(result){
			$.doPost({
				url:"/project/mngSubProject.do",
				headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()}, //스프링시큐리티 CSRF
			    data : param,
				success : function(data, status, xhr){
					if(data.msg === "success"){
						selectProject(mainProjectId);
					}else{
						alert("데이터를 복제하지 못했습니다.")
					}
				},
				error : function(jqxXHR, textStatus, errorThrown){
					commonFunc.ajaxFailAction(jqxXHR);
				}
			});
		}
	});
}

function openPage(subProjectId){
	var subprtid = $("#"+subProjectId).text(); //선택한 subprojctID
	location.href="/ml/predictWithModelView.do?subprtid="+subprtid; //page 이동.
}

var datagrid;
function createElements(data){
	var SBGridProperties = {};                
	SBGridProperties.parentid = 'sbGridArea';  // [필수] 그리드 영역의 div id 입니다.            
	SBGridProperties.id = 'datagrid';          // [필수] 그리드를 담기위한 객체명과 동일하게 입력합니다.                
	SBGridProperties.jsonref = data;    // [필수] 그리드의 데이터를 나타내기 위한 json data 객체명을 입력합니다.

	// 그리드의 여러 속성들을 입력합니다.
	SBGridProperties.extendlastcol = 'scroll';
	SBGridProperties.tooltip = true;
	SBGridProperties.ellipsis = true;

	// [필수] 그리드의 컬럼을 입력합니다.  
	SBGridProperties.columns = [
		{caption : ['제목'],		ref : 'LABEL',		width : '100px',  style : 'text-align:center',	type : 'output'},
		{caption : ['생성일'],	ref : 'CREATE_DT',	width : '100px',  style : 'text-align:center',	type : 'output'},
		{ref : 'ID', hidden:true }
	];			
	datagrid = _SBGrid.create(SBGridProperties); // 만들어진 SBGridProperties 객체를 파라메터로 전달합니다.
	
	datagrid.bind('click','gridClick');
}

function gridClick(){
	var nRow = datagrid.getRow();
	var nCol = datagrid.getCol();
	
	if(nRow != '-1'){
		var nCol = datagrid.getColRef("ID");
		var selectId = datagrid.getCellData(nRow,nCol);
		selectProject(selectId);
	}
}
</script>
<body>
<div style="width: 100%;">
	<div class="levml-workspace-project-list-area">
	  	<div class="card custom-search-card-div-height" >
			<div class="card-body custom-right-div">
				<div class="row">
					<div class="col-8">
						<div class="mt-2 ml-2">
							<p class="h5">
								<i class="fas fa-save"></i> <strong>프로젝트</strong>
							</p>
						</div>
					</div>
					<div class="col-4">
						<div class="mt-1 ml-1 mb-1">
						<button type="button" class="btn btn-outline-primary" id="createProjectBtn"><i class="fa fa-plus"></i></button>
						</div>
					</div>
					<div class="saved-list-pager">
						<div class="col-12 ">
							<div id="sbGridArea" style="width:300px;height:500px;"></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="levml-va-workspace-model-list-area">
		
			<div class="levml-va-workspace-model-list-header" style="overflow-y: scroll !important;">
				<div class="lev-va-workspace-model-list-title" title=""><span id="projectLabel"></span></div> <!--제목  -->
				<div class="levml-va-workspace-model-list-edit"></div>
				<div class="levml-va-workspace-model-list-author d-flex justify-content-between">
					<div class="levml-style-editor-toolitem levml-style-s-editor-toolitem"><i class="fas fa-info-circle" style="padding-right: 10px;"></i>
					<span>생성일:</span>&nbsp;
					<span id="projectCreateDt"></span>&nbsp;&nbsp;
					<span>생성자:</span>&nbsp;
					<span id="projectCreateId"></span>
					</div>
					<div class=""></div>
					<div class="mt-1 ml-1 mb-1">
						<button type="button" class="btn btn-outline-primary" id="createSubProjectBtn" style="display: none;"><i class="fa fa-plus"></i></button>
					</div>
				</div>
				
				<div class="levml-va-workspace-model-list-content model ps-theme-default" id="subProjectDiv"><!--subprojectList  -->
				
				</div>
			</div>
		
	</div>
</div>


<!--project생성 modal  -->
<div class="modal fade" id="modalProjectCreate" tabindex="-1" role="dialog"  aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg" role="document"  style="max-width:700px;">
    <!--Content-->
    <div class="modal-content">
      <!--Body-->
      <div class="modal-body mb-0 p-1">
        <!--Google map-->
        <div id="map-container-google-2" class="z-depth-1-half map-container" style="height: 300px">
          	
			<p class="h6 pt-3 pb-4 text-center">
				<strong >새로운 프로젝트를 생성합니다</strong>
			</p>
				 <table class="table mt-3">
                    <tr>
                        <th>프로젝트명</th>
                        <td><input type="text" id="label" name="label" placeholder="프로젝트명 입력하세요." /></td>
                    </tr>
                    <tr>
                        <th>설명</th>
                        <td><textarea  rows="5" cols="10" id="description" name="description" placeholder="내용을 입력하세요."></textarea></td>
                    </tr>
                </table>														
        </div>
      </div>
      <!--Footer-->
      <div class="modal-footer justify-content-center">
      	<button type="button" class="btn btn-outline-primary btn-md" data-dismiss="modal"  id="modalProjectCreate_confirm_ok">생성 <i class="fas fa-plus ml-1"></i></button>
        <button type="button" class="btn btn-outline-secondary btn-md" data-dismiss="modal" >취소 <i class="fas fa-times ml-1"></i></button>
      </div>
    </div>
    <!--/.Content-->
  </div>
</div>

<!--subProject생성 modal -->
<div class="modal fade" id="modalSubProjectCreate" tabindex="-1" role="dialog"  aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg" role="document"  style="max-width:700px;">
    <!--Content-->
    <div class="modal-content">
      <!--Body-->
      <div class="modal-body mb-0 p-1">
        <!--Google map-->
        <div id="map-container-google-2" class="z-depth-1-half map-container" style="height: 300px">
          	
			<p class="h6 pt-3 pb-4 text-center">
				<strong >서브 프로젝트를 생성합니다</strong>
			</p>
				 <table class="table mt-3">
				 	<tr>
                        <th>메인 프로젝트명</th>
                        <td><span id="mainProjectLabel"></span></td>
                    </tr>
                    <tr>
                        <th>서브 프로젝트명</th>
                        <td><input type="text" id="subProjectLabel" name="subProjectLabel" placeholder="서브 프로젝트명 입력하세요." /></td>
                    </tr>
                    <tr>
                        <th>설명</th>
                        <td><textarea  rows="3" cols="10" id="subProjectDescription" name="subProjectDescription" placeholder="내용을 입력하세요."></textarea></td>
                    </tr>
                </table>														
        </div>
      </div>
      <!--Footer-->
      <div class="modal-footer justify-content-center">
      	<button type="button" class="btn btn-outline-primary btn-md" data-dismiss="modal"  id="modalSubProjectCreate_confirm_ok">생성 <i class="fas fa-plus ml-1"></i></button>
        <button type="button" class="btn btn-outline-secondary btn-md" data-dismiss="modal" >취소 <i class="fas fa-times ml-1"></i></button>
      </div>
    </div>
    <!--/.Content-->
  </div>
</div>

<!--서브 프로젝트 수정  -->
<div class="modal fade" id="modalSubProjectUpdate" tabindex="-1" role="dialog"  aria-hidden="true" data-backdrop="static" data-keyboard="false">
  <div class="modal-dialog modal-lg" role="document"  style="max-width:700px;">
    <!--Content-->
    <div class="modal-content">
      <!--Body-->
      <div class="modal-body mb-0 p-1">
        <!--Google map-->
        <div id="map-container-google-2" class="z-depth-1-half map-container" style="height: 300px">
          	
			<p class="h6 pt-3 pb-4 text-center">
				<strong >서브 프로젝트 내용을 수정합니다</strong>
			</p>
				 <table class="table mt-3">
				 	<tr>
                        <th>메인 프로젝트명</th>
                        <td><span id="mainProjectLabelUpdate"></span></td>
                    </tr>
                    <tr>
                        <th>서브 프로젝트명</th>
                        <td><input type="text" id="subProjectLabelUpdate" name="subProjectLabelUpdate" placeholder="변경할 프로젝트명 입력하세요." /></td>
                    </tr>
                    <tr>
                        <th>설명</th>
                        <td><textarea  rows="3" cols="10" id="subProjectDescriptionUpdate" name="subProjectDescriptionUpdate" placeholder="변경내용을 입력하세요."></textarea></td>
                    </tr>
                </table>														
        </div>
      </div>
      <!--Footer-->
      <div class="modal-footer justify-content-center">
      	<button type="button" class="btn btn-outline-primary btn-md" data-dismiss="modal"  id="modalSubProjectUpdate_confirm_ok">수정 <i class="fas fa-plus ml-1"></i></button>
        <button type="button" class="btn btn-outline-secondary btn-md" data-dismiss="modal" >취소 <i class="fas fa-times ml-1"></i></button>
      </div>
    </div>
    <!--/.Content-->
  </div>
</div>
</body>
