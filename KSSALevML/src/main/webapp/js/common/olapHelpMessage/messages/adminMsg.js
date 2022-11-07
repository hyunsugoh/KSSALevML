/**
 * 
 */

var OLAPAdminHelpMsg = {
	"btnClass" : "btn btn-lg custom-help-icon-btn custom-admin-help-icon-btn", // 적용할 class
	"brElemSet" :'<hr>',	

	//
	"title" : "도움말",
	 "adminObject": [ // 객체관리 페이지 도움말
					'<strong>1. 테이블 추가하기</strong><br>'+
						'Mart DB의 테이블을 추가하려면 <span class="badge custom-badge-outline-success">추가 <i class="fa fa-plus"></i></span> 버튼을 클릭합니다.'+
						'<br> 팝업 창에서 테이블을 선택한 후 <span class="badge custom-badge-outline-primary">추가 <i class="fa fa-plus"></i></span> 버튼을 클릭합니다.',
					'<strong>2. 객체 입력</strong>'+
						'<br> 객체명, 객체설명을 입력하려면 셀 오른쪽의 <strong><i class="fas fa-pencil-alt" style="color:#2f90b2;"></i> 편집</strong> 을 클릭하고 정보를 입력 후<strong> <i class="fas fa-check" style="color:#008344;"></i> 저장</strong>하십시오.',
					'<strong>3. 컬럼 등록</strong>'+
						'<br> Mart DB에서 불러온 테이블의 컬럼을 등록하시려면 해당 테이블 셀을 <strong>더블 클릭</strong>하십시오.'+
						'<br> 노출된 팝업 창에서 <strong>"컬럼(DataMart)"</strong>은 MartDB에서 불러온 컬럼 명입니다. 정보입력 시 <strong>"컬럼"</strong>은 MartDB와 동일하게 자동입력됩니다.'+
						'<br>  <strong><i class="fas fa-pencil-alt" style="color:#2f90b2;"></i> 편집</strong> 버튼을 클릭하고 해당 컬럼 정보를 입력합니다. '+
						'<br> 기준일자는 컬럼 리스트 중 반드시 1개가 존재해야 합니다.',
					'<strong>4. 활성화</strong>'+
					'<br> 활성화가 <strong>"N"</strong> 상태인 테이블은 사용자에게 노출되지 않습니다.'+
					'<br> 테이블을 활성화하려면 최소한 <strong>"객체명"</strong>, <strong>"객체설명"</strong>, <strong>"기준일자 컬럼"</strong>이 등록되어 있어야 합니다.'+
					'<br> 해당 정보들을 입력한 테이블 셀을 클릭한 후 <span class="badge custom-badge-outline-primary">활성화 <i class="fab fa-creative-commons-sa"></i></span> 버튼을 클릭하면 해당 테이블은 사용자에게 노출됩니다.',
					'<strong>주의사항</strong>'	+
					'<br> 테이블 명을 <strong>삭제 <i class="fas fa-trash-alt" style="color:#cc0033;"></i></strong> 하면 해당 테이블과 연관된  객체 정보,관계 관리,조회 조건이 자동 삭제됩니다.'
	 		],
	"adminObjectRel":[ // 객체 관계관리 페이지 도움말
	
		'<strong>1. 기준 테이블 선택</strong><br>'+
		'객체 관계를 설정하려는 기준 테이블을 클릭합니다.'+
		'<br>',
		'<strong>2. 연결할 테이블 선택</strong><br>'+
		'객체 관계를 설정하려는 연결 테이블의 우측에 있는 <strong><i class="fas fa-pencil-alt" style="color:#2f90b2;"></i> 편집</strong> 버튼을 클릭합니다.'+
		'<br>',
		'<strong>3. 조인식 설정</strong><br>'+
		'편집 버튼을 누르면 조인식을 설정할 수 있는 팝업창이 뜹니다. 기본적으로 기준 테이블과 연결 테이블간의 같은 이름을 가진 키값을 자동으로 조인시킨 조인식을 보여주고(저장x), '+ 
		'조인식을 저장,편집하려면<strong><i class="fas fa-pencil-alt" style="color:#2f90b2;"></i> 편집</strong> 버튼을 클릭합니다. '+
		'조인식을 편집한 뒤  <strong> <i class="fas fa-check" style="color:#008344;"></i></strong> 버튼을 클릭하면 조인식이  저장됩니다.'+
		'<br>',
		
	],
	"infoCriteria":[ // 조회조건 관리
		'<strong>1. 조회 조건을 설정할 컬럼 선택</strong><br>'+
		'조회 조건을 설정할 컬럼을 클릭하면 팝업창이 뜹니다.'+
		'<br>',
		'<strong>2. 컬럼에 조회조건 설정</strong><br>'+
		'해당 컬럼에 설정할 조회조건들을 선택하면 체크박스가 활성화 됩니다. 그리고 팝업창 맨 아래  <span class="badge custom-badge-outline-success">저장 <i class="fa fa-save"></i></span> 을 클릭하면 해당 컬럼에 설정한 조회조건들이 저장됩니다.'+
		'<br>',
		
	],
	"codeManagement":[ // 조회조건 코드 관리
		'<strong>1. 조회 조건 코드 등록</strong><br>'+
		'코드,코드명,계산식,예시를 입력하고 <span class="badge custom-badge-outline-success">저장 <i class="fa fa-save"></i></span> 을 클릭하면 코드가 저장되고,조회 조건 코드 관리 리스트에서 조회됩니다.'+
		'<br>',
		'<strong>2. 조회 조건 코드 삭제</strong><br>'+
		'조회 조건 코드 관리 리스트에서 삭제하려는 코드를 선택하면 체크박스가 활성화 됩니다. 그리고 우측 하단의 <strong>삭제 <i class="fas fa-trash-alt" style="color:#007bff;"></i></strong>를 클릭하면 삭제됩니다. '+
		'<br>',
		'<strong style="color:red;">주의사항</strong>'	+
		'<br> - 코드를 <strong>삭제 <i class="fas fa-trash-alt" style="color:#007bff;"></i></strong> 하면 해당 코드값을 연결한 컬럼에서도 설정한 관계가 모두 삭제 됩니다.'+
		'<br> - 계산식<strong>(LIKE, LIKE1, LIKE2, NOT LIKE)</strong>는 값이 고정되어 있습니다. 특별한 경우가 없는 한 삭제하지 마십시오.'
		
	],
	"userList":[	// 회원목록 조회
		'<strong>1. 비밀번호 초기화</strong>'+
			'<br> 초기화하려는 아이디를 클릭 후 <span class="badge custom-badge-outline-primary">비밀번호 초기화</span><br> 버튼을 클릭합니다.'+
			'<br> 팝업창에 초기화된 <br> 비밀번호(난수)가 노출됩니다.'+
			'<br> 한번에 한개의 아이디만 <br>초기화할 수 있습니다.',
		'<strong>2. 아이디 삭제</strong>'+
		'<br> 삭제하려는 아이디들을 클릭 후 <span class="badge custom-badge-outline-info">삭제</span> 버튼을 클릭합니다.'+
		'<br> 여러 개의 아이디를 한번에 삭제할 수 <br>있습니다.'
	],
	"manager":[ // 관리자 관리
		'<strong>1. 관리자 추가</strong><br>'+
		'관리자를 추가하려면  <span class="badge custom-badge-outline-success"> <i class="fas fa-user-plus" style="color:#007bff;">관리자 가입</i></span>을 클릭합니다. '+
		'<br>',
		'<strong>2. 관리자 삭제</strong><br>'+
		'관리자를 삭제하려면 <strong>삭제 <i class="fas fa-trash-alt" style="color:#cc0033;"></i></strong>를 클릭합니다. '+
		'<br>',
		'<strong>3. 연결 허용 IP 관리</strong><br>'+
		'관리자를 선택하면 연결 허용된 IP 리스트를 볼 수 있습니다. 새  IP를 추가하려면 <strong> <i class="fas fa-plus" style="color:#28a745;"></i></strong>버튼을  클릭합니다. '+
		'기존 IP를 수정하려면 <strong><i class="fas fa-pencil-alt" style="color:#2f90b2;"></i> 편집</strong> 버튼을 클릭,IP를 삭제하려면  <strong><i class="fas fa-trash-alt" style="color:#dc3545;"></i>삭제</strong> 버튼을 클릭합니다.'+
		'<br>',
		
	],
	"adminPwdChange":[// 관리자 비밀번호 변경 도움말
		'<strong>1. 관리자  비밀번호 변경</strong><br>'+
		'현재 로그인된 관리자의 비밀번호를 변경할 수 있습니다. 현재 비밀번호와 새 비밀번호,새비밀번호 확인을 입력한 뒤 <span class="badge custom-badge-outline-primary">비밀번호 변경</span> 버튼을 큭릭합니다.'+
		'<br>',
	],
	"adminSignUp" :[ // 관리자 가입 도움말
		'<strong>1. 관리자  가입</strong><br>'+
		'접속 허용 IP 항목에는 해당 관리자 아이디가 접속할 수 있는 위치의 IP를 입력합니다. IP는 해당 접속 위치의 외부 접속 IP를 입력 하셔야 합니다. '+
		'<br>',
		
	],
	
	

};