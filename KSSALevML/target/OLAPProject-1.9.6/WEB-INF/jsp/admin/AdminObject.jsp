<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>



<script src="<c:url value='/js/admin/adminObject.js'/>"></script>

<style>
tr.highlight td.jsgrid-cell {
	background-color: #BBDEFB;
}

.jsgrid-header-row {
	text-align: center;
}

.red td {
	color: #f08080 !important;
}

.jsgrid-delete-button-custom {
	background-position: 0 -80px;
	width: 16px;
	height: 16px;;
	opacity: .2;
}

.jsgrid-edit-button-custom {
	background-position: 0 -120px;
	width: 16px;
	height: 16px;;
	opacity: .2;
}

.font-size-down {
	font-size: 12px;
}

.jsgrid-cell {
	word-wrap: break-word;
}
</style>

<div class="col-md-9 col-lg-10 col-xl-10 col-10 ml-auto px-4">
	<div class="row">
		<!-- style="display: none;" -->
		<div id="div1" style="display: none;"></div>
		<div id="div2" style="display: none;"></div>
		<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}'
			value='${_csrf.token}' />
		<div class="col-12">
			<div class="row">
				<div class="col-6">
					<div class="h5">
						<strong><i class="far fa-object-ungroup"></i> <span
							class="ml-1">객체 관리</span></strong>
					</div>
				</div>
				<div class="col-6">
					<div class="d-flex justify-content-end">
						<div id="helpIcon" class="pt-0"></div>
					</div>
				</div>
			</div>
		</div>

		<div class="col-12 pt-5">
			<div id="jsGrid"></div>
		</div>
		<div class="col-12">
			<div class="row">
				<div class="col-12 mt-3 text-right">
					<button type="button" class="btn btn-outline-primary"
						onclick="active();">
						활성화/비활성화 <i class="fab fa-creative-commons-sa"></i>
					</button>

					<button type="button" class="btn btn-outline-success"
						data-toggle="modal" data-target="#myModal" onclick="addPopup();">
						추가 <i class="fa fa-plus"></i>
					</button>
				</div>
			</div>
		</div>
		<div class="modal" id="myModal">
			<div class="modal-dialog  modal-md">
				<div class="modal-content">

					<!-- Modal Header -->
					<div class="modal-header">
						<h4 class="modal-title">객체 추가</h4>
						<button type="button" class="close" data-dismiss="modal">&times;</button>
					</div>

					<!-- Modal body -->
					<div class="modal-body">


						<div id="jsGridSelect"></div>



					</div>

					<!-- Modal footer -->
					<div class="modal-footer">
						<button type="button" class="btn btn-sm btn-outline-primary"
							onclick="insert();">
							추가 <i class="fa fa-plus"></i>

						</button>
					</div>

				</div>
			</div>
		</div>

		<!-- 객체 정보 관리 팝업 -->
		<div class="modal" id="myModal2">
			<div class="modal-dialog modal-xl  modal-dialog-scrollable">
				<div class="modal-content">

					<!-- Modal Header -->
					<div class="modal-header">
						<div class="modal-title h4">객체 정보 관리</div>

						<div style="font-size: 24px; float: left;">&nbsp;&nbsp;&nbsp;(테이블명
							:&nbsp;</div>
						<div id="div4" style="font-size: 24px; float: left;"></div>
						<div style="font-size: 24px; float: left;">)</div>

						<button type="button" class="close" data-dismiss="modal">&times;</button>
					</div>

					<!-- Modal body -->
					<div class="modal-body">
						<div class="col-12">
							<div id="jsGridInfo"></div>
						</div>
					</div>


					<!-- Modal footer -->
					<div class="modal-footer">
						<p class="font-italic text-info">글자가 빨간색으로 표시된 경우는 OLAP 관리자 DB에
						저장된 해당 행의 컬럼 이름이 마트의 실제 컬럼 이름과 매핑되지 않는 경우 입니다. 이 경우 해당 칼럼은 데이터 조회에
						사용할 수 없습니다</p>
					</div>

				</div>
			</div>
		</div>

	</div>

</div>
