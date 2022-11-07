/**
 *  baseUrl 관련 config 변수
 */
'use strict';
var OLAPBaseUrl = "",
	OlapUrlConfig = {
		
		adminobject : OLAPBaseUrl + "/admin/adminobject.do",
		pwdInitialize : OLAPBaseUrl +  "/pwdInitialize.do",
		idFind : OLAPBaseUrl +  "/idFind.do",
		board : OLAPBaseUrl + "/user/board.do",
		signup:OLAPBaseUrl + "/signup.do",
		loginPage :OLAPBaseUrl +"/logon.do",
		Manager: OLAPBaseUrl +"/admin/Manager.do",
		UserList: OLAPBaseUrl +"/admin/userList.do",
		
		//ID찾기
		actionFind : OLAPBaseUrl + "/actionFind.do",
		// PwdChange
		actionPwdChange : OLAPBaseUrl +"/user/api/data/actionPwdChange.do",
		// PwdInitialize
		getUserHintList: OLAPBaseUrl +"/getUserHintList.do",
		userActionInitialize:OLAPBaseUrl+"/actionInitialize.do",
		
		// signup
		chkUserId:OLAPBaseUrl+"/chkUserId.do",
		chkSubId:OLAPBaseUrl+"/chkSubId.do",
		signUpInsert:OLAPBaseUrl+"/signUpInsert.do",
		
		
		// 대시보드
		getTbName : OLAPBaseUrl + "/user/api/data/getTableName.do",
		unitInfo : OLAPBaseUrl + "/user/api/data/getUnitInfo.do",
		getObjRelInfo : OLAPBaseUrl + "/user/api/data/getObjectRelInfo.do",
		objDetailInfo : OLAPBaseUrl + "/user/api/data/getObjectDetailInfo.do",
		getObjectList : OLAPBaseUrl + "/user/api/data/getObjectList.do",
		getConditionData : OLAPBaseUrl + "/user/api/data/getConditionData.do",
		selectGridData : OLAPBaseUrl + "/user/api/data/selectGridData.do",
		getSelectList : OLAPBaseUrl + "/user/api/data/getSelectList.do",

		// 저장목록
		saveUserDataset : OLAPBaseUrl + "/user/api/data/saveUserDataset.do",
		selectSavedDataSetList : OLAPBaseUrl + "/user/api/data/selectSavedDataSetList.do",
		deleteUserDataset : OLAPBaseUrl + "/user/api/data/deleteUserDataset.do",

		// adminObject
		AdminObjectList : OLAPBaseUrl + "/admin/AdminObjectList.do",
		AdminObjectDelete : OLAPBaseUrl + "/admin/AdminObjectDelete.do",
		AdminObjectUpdate : OLAPBaseUrl + "/admin/AdminObjectUpdate.do",
		AdminObjectActive : OLAPBaseUrl + "/admin/AdminObjectActive.do",
		AdminObjectSelect : OLAPBaseUrl + "/admin/AdminObjectSelect.do",
		AdminObjectInfoList : OLAPBaseUrl + "/admin/AdminObjectInfoList.do",
		AdminObjectInfoUpdate : OLAPBaseUrl + "/admin/AdminObjectInfoUpdate.do",
		AdminObjectInfoDelete : OLAPBaseUrl + "/admin/AdminObjectInfoDelete.do",
		AdminObjectInsert : OLAPBaseUrl + "/admin/AdminObjectInsert.do",
		
		// adminObjectRel
		AdminObjectRelStand : OLAPBaseUrl + "/admin/AdminObjectRelStand.do",
		AdminObjectRelConn : OLAPBaseUrl + "/admin/AdminObjectRelConn.do",
		AdminObjectRelJoinDelete : OLAPBaseUrl + "/admin/AdminObjectRelJoinDelete.do",
		AdminObjectRelJoin : OLAPBaseUrl + "/admin/AdminObjectRelJoin.do",
		AdminObjectRelJoinUpdate : OLAPBaseUrl + "/admin/AdminObjectRelJoinUpdate.do",

		
		// AdminPwdChange
		ActionAdminPwdChange : OLAPBaseUrl + "/admin/ActionAdminPwdChange.do",
		chkManagerId : OLAPBaseUrl + "/admin/chkManagerId.do",
		signUpManager: OLAPBaseUrl + "/admin/signUpManager.do",

		// CodeManagement
		getCodeManagementList: OLAPBaseUrl + "/admin/getCodeManagementList.do",
		insertCodeManagement: OLAPBaseUrl + "/admin/insertCodeManagement.do",
		deleteCodeManagement: OLAPBaseUrl + "/admin/deleteCodeManagement.do",
		
		// infoCriteria
		getInfoCriteria: OLAPBaseUrl + "/admin/api/getInfoCriteria.do",
		getInfoCondition: OLAPBaseUrl + "/admin/api/getInfoCondition.do",
		getSearchChk: OLAPBaseUrl + "/admin/api/getSearchChk.do",
		updateCondifion: OLAPBaseUrl +"/admin/api/updateCondifion.do",
		
		// Manager
		ManagerList: OLAPBaseUrl +"/admin/ManagerList.do",
		ManagerEnabledUpdate: OLAPBaseUrl + "/admin/ManagerEnabledUpdate.do",
		ManagerDelete: OLAPBaseUrl + "/admin/ManagerDelete.do",
		ManagerIpList: OLAPBaseUrl + "/admin/ManagerIpList.do",
		ManagerIpInsert: OLAPBaseUrl + "/admin/ManagerIpInsert.do",
		ManagerIpDelete: OLAPBaseUrl +"/admin/ManagerIpDelete.do",
		ManagerIpUpdate: OLAPBaseUrl +"/admin/ManagerIpUpdate.do",

		// userList
		getUserInfoList: OLAPBaseUrl +"/admin/api/getUserInfoList.do",
		actionInitialize: OLAPBaseUrl +"/admin/api/actionInitialize.do",		
		deleteUserList: OLAPBaseUrl +"/admin/api/deleteUserList.do",
		
		// 회원탈퇴
		actionWithdrawal : OLAPBaseUrl +"/user/api/data/actionWithdrawal.do",
		
		//게시판 등록,수정,삭제
		board : OLAPBaseUrl +"/user/api/data/board.do", //게시판
		boardList : OLAPBaseUrl +"/user/api/data/boardList.do", //user/api = 컨트롤러 전체 value , 그 뒤는 각 value
		boardwriteForm : OLAPBaseUrl + "/user/api/data/boardwriteForm.do", //글쓰기폼
		insertBoard : OLAPBaseUrl + "/user/api/data/insertBoard.do",//게시글 등록
		viewForm : OLAPBaseUrl + "/user/api/data/viewForm.do",//조회 폼
		updateForm : OLAPBaseUrl + "/user/api/data/updateForm.do",//수정폼 게시글 조회
		
};
