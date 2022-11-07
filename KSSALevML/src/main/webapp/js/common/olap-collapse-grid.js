/**
 * @author Jin. Choi.
 * 생성 날짜: 2022-10-26
 * 설명: 콜랩스 기능을 가진 그리드
 * Jquery가 로드된 후에 해당 함수 호출
 */
 
/**
 * 1. 클릭 이벤트 : 클래스 click-evt-as_cnt 를 추가해야함.
 * 2. 아래 코드를 jsp에 붙여넣기 해야함.
<%--Modal: AS History Pop--%>
<!-- Modal -->
<div class="modal fade" id="ASHistoryPopup" tabindex="-1" aria-labelledby="ASHistoryPopup_label" aria-hidden="true">
  <div class="modal-dialog modal-dialog-scrollable modal-xl modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="ASHistoryPopup_label"><strong>AS 이력</strong></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
		<div class="overflow-auto">
			<div id="asHistoryGrid"></div>
		</div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-outline-secondary" data-dismiss="modal">닫기</button>
      </div>
    </div>
  </div>
</div>
<%--Modal: AS History Pop--%>

 */

let CustomCollapseGrid = function(elemId, config, data) {
	const self = this;

//	let testD = [];
//	let testD2 = [];
//	let testD3 = [];
//	let testD4 = [];
//	let testD5 = [];
//	for (let i in _data) {
//		testD.push(_data[i]);
//		testD2.push(_data[i]);
//		testD3.push(_data[i]);
//		testD4.push(_data[i]);
//		testD5.push(_data[i]);
//	}
//	const data = [...testD, ..._data,
//	...testD2,
//	...testD3,
//	...testD4,
//	...testD5
//
//	];
	self.data = data;
	// valid chk 
	if (!commonFunc.chkNotEmptyStr(elemId)) { console.error('element id is not string'); return; }

	self.elemId = elemId;
	self.setConfig = function(key, value) { config[key] = value; }

	$("#" + elemId).empty();
	let $table = $("<table></table>").addClass("table table-bordered");
	let $thead = $("<thead></thead>").attr("style","background-color:#176ad0;color:white;");
	let $tr = $("<tr></tr>");

	if (config.hasOwnProperty("field")) {
		const field = config.field;
		self.isNum = config.hasOwnProperty("isNum") && config.isNum;
		if (isNum) {
			let $th = $("<th></th>").attr("scope", "col").append("No.").addClass("text-center");
			$tr.append($th);
		}

		for (let i = 0; i < field.length; i++) {
			const fieldOption = field[i];
			let $th = $("<th></th>").attr("scope", "col").append(fieldOption["title"]);
			$th.addClass("text-center");
			$tr.append($th);
		}

		$table.append($thead.append($tr));

		// setData
		let $tbody = $("<tbody></tbody>");
		for (let j = 0; j < data.length; j++) {
			let $bTr = $("<tr></tr>").addClass("olap-data-row-" + j);
			$bTr.attr("data-row", j);
			if (isNum) {
				let $numTh = $("<th></th>").attr("scope", "row");
				$numTh.append(j + 1).addClass("text-center");
				$bTr.append($numTh);
			}

			const dataRow = data[j];

			for (let k = 0; k < field.length; k++) {
				const fieldOption = field[k];
				const keyName = fieldOption["name"];
				if (dataRow.hasOwnProperty(keyName)) {
					let $td = $("<td></td>");

					if (fieldOption.hasOwnProperty("align")) {
						let alignOption = fieldOption["align"];
						if (alignOption === "left") {
							alignOption = "text-left";
						} else if (alignOption === "right") {
							alignOption = "text-right";
						} else if (alignOption === "center") {
							alignOption = "text-center";
						} else {
							alignOption = "";
						}

						if (alignOption !== "") {
							$td.addClass(alignOption);
						}
					}
					let fieldStyle = "";
					
					if(fieldOption.hasOwnProperty("font_size") && !isNaN(fieldOption["font_size"])){
						fieldStyle +="font-size:"+fieldOption["font_size"]+"pt;";
						
					}
					if(fieldOption.hasOwnProperty("width")){
						fieldStyle += "width:"+fieldOption["width"];
						if(!isNaN(fieldOption["width"])) fieldStyle +="px;";
					}
					
					if(fieldStyle !== ""){
						$td.attr("style",fieldStyle);
					}

					let value = dataRow[keyName];
					
					if(fieldOption.hasOwnProperty("type")){
						if(fieldOption.type === "date"){
							const convertDate = new Date(value);
							value = convertDate.toLocaleString("ko-kr",{
								year:'numeric',month:'numeric',day:'numeric'
							});
						}else if(fieldOption.type === "number"){
							value = value.toLocaleString();
						}
					}
					
					$td.append(value);
					$bTr.append($td);
				}
			}

			$tbody.append($bTr);

			if (config.hasOwnProperty("collapseField")) {

				const cField = config.collapseField,
					colSpanLen = isNum ? field.length + 1 : field.length;
				let $colTr = $("<tr></tr>"),
					$colTd = $("<td></td>").attr("colspan", colSpanLen).attr("style", "padding:0;"),
					$colDiv = $("<div></div>").addClass("collapse"),
					$colDiv2 = $("<div></div>");
				$colDiv.attr("id", "olap-data-col-row-" + j);
				$colDiv2.attr("style", "padding:20px;");

				for (let m = 0; m < cField.length; m++) {
					const cFieldOpt = cField[m];
					const titleName = cFieldOpt["title"],
						cFieldName = cFieldOpt["name"];

					let $titleArea = $("<p></p>").addClass("font-weight-bold").attr("style","font-size:12pt !important;");
					if(cFieldOpt.hasOwnProperty("title_icon_str")){
						$titleArea.append(cFieldOpt.title_icon_str);
						$titleArea.append("&nbsp;");
					}
					$titleArea.append(titleName);
					$colDiv2.append($titleArea);

					if (dataRow.hasOwnProperty(cFieldName)) {
						let cValue = dataRow[cFieldName];
						if (cValue.includes('\n')) {
							cValue = cValue.replace(/\n/g, '<br>');
						}

						let $descriptionArea = $("<pre></pre>").addClass("ml-3 pb-2").append(cValue);
						let descriptionStyle = "";
						
						if (cFieldOpt.hasOwnProperty("font_size") && !isNaN(cFieldOpt["font_size"])) {
							descriptionStyle += "font-size:" + cFieldOpt["font_size"] + "pt;";
						}
						
						if (cFieldOpt.hasOwnProperty("width")) {
							descriptionStyle += "font-width:" + cFieldOpt["width"];
							if(!isNaN(cFieldOpt["width"])) {
								descriptionStyle  += "px;";
							}
						}
						
						if(descriptionStyle !== ""){
							$descriptionArea.attr("style", descriptionStyle);
						}
						
						$colDiv2.append($descriptionArea);
					}

				}
				$tbody.append($colTr.append($colTd.append($colDiv.append($colDiv2))));
				
				$bTr.on("click", function(e) {
					const idx = $(this).attr("data-row");
					$("#olap-data-col-row-" + idx).collapse('toggle');
					if($(this).hasClass("collapse-selected-row")=== false){
						$(this).addClass("collapse-selected-row");
					}else{
						$(this).removeClass("collapse-selected-row");
					}
				});
			}


		}

		$table.append($tbody);

		$("#" + elemId).append($table);

	}











};