<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<script src="<c:url value='/js/admin/adminObjectRel.js'/>"></script>

<style>
tr.highlight td.jsgrid-cell {
	background-color: #BBDEFB;
}

.jsgrid-header-row {
	text-align: center;
}

.jsgrid-delete-button-custom {
	background-position: 0 -80px;
	width: 16px;
	height: 16px;;
	opacity: .2;
}

.custom-rel-none-div {
	border: 1px solid #d3d3d3;
}

.jsgrid-cell {
	word-wrap: break-word;
}
</style>

<div class="col-md-9 col-lg-10 col-xl-10 col-10 ml-auto px-4">
	<div class="row">
		<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}'
			value='${_csrf.token}' />
		<div class="col-12">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="far fa-object-group"></i><span
							class="ml-1">객체 관계 관리</span></strong>
					</div>
				</div>
				<div class="col-6">
					<div class="d-flex justify-content-end">
						<div id="helpIcon" class="pt-0"></div>
					</div>
				</div>
			</div>

			<div class="row pt-5">
				<div class="col-6">
					<div id="jsGrid"></div>
				</div>
				<div id="jsGridResultArea"
					class="col-6 d-flex align-items-center justify-content-center custom-rel-none-div">
					<div id="jsGridConn"></div>
					<span id="descriptMsg"
						class="badge badge-secondary custom-badge-font">관계를 정의하려면
						테이블을 선택하여 주십시오.</span>
				</div>
			</div>



			<!-- 객체 관계 관리 조인식 팝업 -->
			<div class="modal" id="myModalJoin">
				<div class="modal-dialog  modal-xl">
					<div class="modal-content">

						<!-- Modal Header -->
						<div class="modal-header">
							<h4 class="modal-title">객체 관계 관리 조인식</h4>
							<button type="button" class="close" data-dismiss="modal">&times;</button>
						</div>

						<!-- Modal body -->
						<div class="modal-body">
							<div id="jsGridJoin"></div>
						</div>

						<!-- Modal footer -->
						<div class="modal-footer"></div>

					</div>
				</div>
			</div>

		</div>

	</div>

</div>
