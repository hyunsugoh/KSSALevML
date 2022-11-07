/**
 * dashboardDatepickerModule.js
 */
var DatepickerModule = {
	create : function(elemId) {
		const _options = {
			language : 'ko-KR',
			format : 'yyyy년 mm월 dd일',
			autoHide : true
		};
		return $("#" + elemId).datepicker(_options);
	},
	init : function(elemId,unit) {
		var todayDate = new Date(), 
		dateLimits = new Date(new Date().setFullYear(todayDate.getFullYear() - 1)), 
		dateDefault = new Date(new Date().setMonth(todayDate.getMonth() - 1)),
		dayDefault = new Date(new Date().setDate(todayDate.getDate() - 1));
		
		if (elemId === "datepicker_start") {
			
			if(unit == "일"){
				$("#" + elemId).datepicker('reset');	
				$("#" + elemId).datepicker('setDate', dayDefault);
				//console.log(unit ,dayDefault);
			
			}else{
				$("#" + elemId).datepicker('reset');
				$("#" + elemId).datepicker('setDate', dateDefault);
				//console.log(unit ,dateDefault);
			}
			
			// TODO 1년 제한 해제 향후 정해지면 변경
//			$("#" + elemId).datepicker('setStartDate', dateLimits);
//			$("#" + elemId).datepicker('setEndDate', todayDate);
		
		} else {
			var _startDateVal = $("#datepicker_start")
					.datepicker('getDate');
			$("#" + elemId).datepicker('setDate', todayDate);
			// TODO 1년 제한 해제 향후 정해지면 변경
//			$("#" + elemId).datepicker('setStartDate', _startDateVal);
//			$("#" + elemId).datepicker('setEndDate', todayDate);
		}
	},

	evtBind : function(startId, endId ,unit,num) {

        var num = Number(num);
        
        
		
		$("#" + startId).off("pick.datepicker").on('pick.datepicker', function(e) {
			
			if(e.view =="day"){
			
			var _selectedEndDate = $("#" + endId).datepicker('getDate');
			
			if(e.date > _selectedEndDate){
				$("#" + endId).datepicker('setDate',e.date);
			}
			
			if(unit == "개월"){

			 var MonthAgoDate = new Date();
			 MonthAgoDate.setFullYear(_selectedEndDate.getFullYear());
			 MonthAgoDate.setMonth(_selectedEndDate.getMonth() - num);
			 MonthAgoDate.setDate(_selectedEndDate.getDate());

			
			if(MonthAgoDate > e.date){
				$("#" + endId).datepicker('setDate',new Date(e.date.getFullYear(), e.date.getMonth() + num, e.date.getDate()));
			 }
			}else if(unit == "년"){
			 
			  var MonthAgoDate = new Date();
			 MonthAgoDate.setFullYear(_selectedEndDate.getFullYear() - num);
			 MonthAgoDate.setMonth(_selectedEndDate.getMonth());
			 MonthAgoDate.setDate(_selectedEndDate.getDate());

			
			if(MonthAgoDate > e.date){
				$("#" + endId).datepicker('setDate',new Date(e.date.getFullYear() + num, e.date.getMonth(), e.date.getDate()));
			 }
			
			}else if(unit == "일"){
			 
			 var DayAgoDate = new Date();
			 DayAgoDate.setFullYear(_selectedEndDate.getFullYear());
			 DayAgoDate.setMonth(_selectedEndDate.getMonth());
			 DayAgoDate.setDate(_selectedEndDate.getDate() - num);
			 
			
			if(DayAgoDate > e.date){
				$("#" + endId).datepicker('setDate',new Date(e.date.getFullYear(), e.date.getMonth(), e.date.getDate() + num));
			 }
			
			}else {
           var YearAgoDate = new Date();
			 YearAgoDate.setFullYear(_selectedEndDate.getFullYear()-1);
			 YearAgoDate.setMonth(_selectedEndDate.getMonth());
			 YearAgoDate.setDate(_selectedEndDate.getDate());

			
			if(YearAgoDate > e.date){
				$("#" + endId).datepicker('setDate',new Date(e.date.getFullYear()+1, e.date.getMonth(), e.date.getDate()));
			 }
			}
			
			}
			// end 추가
		});
		
		$("#" + endId).off("pick.datepicker").on('pick.datepicker', function(e) {
			
			if(e.view =="day"){
				
			var _selectedStartDate = $("#"+startId).datepicker('getDate');
			
			if(e.date < _selectedStartDate){
				$("#"+startId).datepicker('setDate',e.date);
			}
			if(unit == "년"){
			
			var MonthAgoDate = new Date(e.date.getFullYear() - num, e.date.getMonth(), e.date.getDate());
			if(MonthAgoDate > _selectedStartDate){
				$("#"+startId).datepicker('setDate',MonthAgoDate);
			}
         }else if(unit == "개월"){
		   
		   	var MonthAgoDate = new Date(e.date.getFullYear(), e.date.getMonth() - num, e.date.getDate());
			if(MonthAgoDate > _selectedStartDate){
				$("#"+startId).datepicker('setDate',MonthAgoDate);
			}
		   }else if(unit == "일"){
		   	
			var MonthAgoDate = new Date(e.date.getFullYear(), e.date.getMonth(), e.date.getDate() - num);
			if(MonthAgoDate > _selectedStartDate){
				$("#"+startId).datepicker('setDate',MonthAgoDate);
			}
		   
		   }else{
		    var oneYearAgoDate = new Date(e.date.getFullYear()-1, e.date.getMonth(), e.date.getDate());
			if(oneYearAgoDate > _selectedStartDate){
				$("#"+startId).datepicker('setDate',oneYearAgoDate);
			}
		   
		   }
		}
	 });
      
	
	},
	
	startShow: function(){
		$("#datepicker_start").datepicker('show');
		
	},
	endShow:function(){
		$("#datepicker_end").datepicker('show');
	}
};