'use strict';
/******************************************************** 
파일명 : dashboardExcelDownload.js 
설 명 : User page의 엑셀 다운로드 DashBoard JavaScript

j2excel library가 있어야 한다.
수정일	수정자	Version	Function 명
-------	--------	----------	--------------
2019.03.28	최 진	1.0	최초 생성
 *********************************************************/

 
var ExcelDownloadObject = {
		Dataset : [],
		convertNumType : function(value){
			value = Math.round(value);
//			value = value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
			return value;
		},
		resetDataset : function(){
			ExcelDownloadObject.Dataset = []; //excel dataset reset
		},
		onClickEvt : function(elemId,titleId){
			elemId = "#"+elemId;
			$(elemId).on("click",function(e){
				
				var _dataSet = ExcelDownloadObject.Dataset;
				var _titleName = $("#"+titleId).val();
				if(_dataSet.length > 0){
					var _dateObj = new Date(),
						_dateStr = ConvertDateObjStr(_dateObj);
					
					_titleName = _titleName !== undefined && _titleName !== null && _titleName !=="" ? _titleName+"_"+_dateStr : "OLAP_"+_dateStr
					var options = {
							fileName: _titleName,
							header: true,
							maxCellWidth: 20
					};
					
					var tableData = [
							{
								"sheetName": _titleName,
								"data":_dataSet
							}
						];
					
					Jhxlsx.export(tableData, options);
				}else{
					alert("다운로드할 데이터가 없습니다.");
				}

			});
		},
		datasetInit : function(_data){
			var _setDataset = [];
			var isVaild = false;
			if(_data !== undefined && _data !== null && _data.hasOwnProperty("fields") && _data.hasOwnProperty("records")){
				isVaild = true;
			}
			
			
			if(isVaild && _data.records.length > 0){
				var _setRecord = [], 
					 _objectTemp = {
									"text":""
									},
					_setObj = {},
					_seObjKeys = [];
				
				// fields
				for(var i in _data.fields){
					_setObj = $.extend(true,{}, _objectTemp);
					_setObj.text = _data.fields[i]["title"];
					_setRecord.push(_setObj);
					_seObjKeys.push({
						name: _data.fields[i]["name"],
						type : _data.fields[i]["type"]
					});
				}
				_setDataset.push(_setRecord);
				_setRecord=[];
				
				
				// data
				for(var j in _data.records){
					var recordVal = _data.records[j];
					for(var k in _seObjKeys){
						var _fieldKey = _seObjKeys[k]["name"],
							_fieldType = _seObjKeys[k]["type"];
						_setObj = $.extend(true,{}, _objectTemp);
						
						var _value = recordVal[_fieldKey];
//						if(_fieldType === "number"){
//							_value = ExcelDownloadObject.convertNumType(_value);
//						}
						_setObj.text = _value;
						
						_setRecord.push(_setObj);
					}
					_setDataset.push(_setRecord);
					_setRecord=[];
				}
				
				ExcelDownloadObject.Dataset = _setDataset;
			}
		}

};