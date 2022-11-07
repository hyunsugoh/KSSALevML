/**
 * require JQuery
 */


var HelpMsgAction = {
		msg : [],
		config:{
			isConsoleShow : false
		},
		chkValidParams : function(flag, elemId, msgObj){
			
			if(flag === undefined || flag === null || flag === ""){
				if(this.config.isConsoleShow){
					console.log("DashboardHelp.chkValidParams :: Parameter Not Valid :: flag ::" + flag);
				}
				return false;
			}
			
			if(elemId === undefined || elemId === null || elemId === ""){
				if(this.config.isConsoleShow){
					console.log("DashboardHelp.chkValidParams :: Parameter Not Valid :: elemId ::" + elemId);
				}
				return false;
			}
			
			if(msgObj === undefined || msgObj === null || msgObj instanceof Object == false){
				if(this.config.isConsoleShow){
					console.log("DashboardHelp.chkValidParams :: Parameter Not Valid :: msgObj ::" );
					console.log(msgObj);
				}
				return false;
			}
		},
		creathHelpBtn : function(flag, elemId, msgObj){
			if(this.chkValidParams){
				var $btn = $("<button>"),
				$iconElem = $("<i>"),
				_brElem = msgObj["brElemSet"],
				_title = msgObj["title"],
			 _msg = msgObj[flag].join(_brElem),
			 _btnClass= msgObj["btnClass"];
			$btn.addClass(_btnClass);
			$btn.attr("data-toggle","popover");
			$btn.attr("title",_title);
			$btn.attr("data-placement","left");
			$btn.attr("data-html","true");
			$btn.attr("data-content",_msg);
			
			$iconElem.addClass("far fa-question-circle");
			$iconElem.append("&nbsp;도움말");
			$btn.append($iconElem);
			
			$("#"+elemId).append($btn);
			
			$('[data-toggle="popover"]').popover({
				  trigger: 'focus'
			});
			}
		}
};