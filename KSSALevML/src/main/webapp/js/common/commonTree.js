;
(function($) {

    $.fn.setClickEvent = function() {
	/*
	 * var _obj = this; _obj.setOnClickHandler(function(id, pId){ alert(1);
	 * });
	 */
    },

    /**
     *
     */
    $.fn.initTree = function(options) {
	/**
	 * loadDataType : jsonobj, jsonurl, xml
	 */
	var defaults = {
	    divId : $(this).attr('id'), iconType : 'folder' // 아이콘 모양 변경
	    , url : EARC_URL.COMMON.TREE
		, method: 'get'
		, contentType: 'application/x-www-form-urlencoded'
	    , loadDataType : 'jsonobj' // 로드 타입
	    , enableKeyboardNavigation : true // 키보드 방향키로 선택가능
	    , enableMultiselection : false // 멀티선택 가능
	    , enableKeySearch : false // 키보드로 찾기
	    , enableCheckBoxes : false // 트리 node 체크박스
	    , enableDragAndDrop : false // 트리 드래그앤드롭
	    , enableItemEditor : false // itemEdit
	    , modifyFn : function(){return true;}
		, chkFn : function(){return true;}
	    , enableThreeStateCheckboxes : false //
	    , openAllItems : false // 모든 node 펼치기
	    , openRoot : false // 상위 1레벨 펼치기
	    , openAllOneDepth : false // 트리의 상위 1레벨 모두 펼치기
	    , selectedItem : false
	    , treeMenu : "treeMenu" // 트리 contextMenu Id
	    , funcTop : false // 트리 최상위 추가 옵션
	    , queryId : 'Required'
	    , stepLoadData : false //트리 단계적으로 로딩(트리 데이터 전체 로딩여부) 2depth 까지만 로딩
	    , stepQueryId : 'Required' // stepLoadData true시 필수
    	, callback : function(tree, data) {}
	    , setOnClickHandler : function(id, pid) {}
	    , setOnDblClickHandler : function(id, pid){return true}
	    , getTopNodeId : function(nodeId) {}
	    , parseFn : function(){}
	};

	$.extend(defaults, options);

	var callback_simpleAjax = function(data) {
		if(data.treeData){
			data.jsonData = data.treeData;

		}
	    tree = new dhtmlXTreeObject(defaults.divId, "100%", "100%", 0);
	    if ("folder" == defaults.iconType) {
			tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_folder/");

	    } else if ("group" == defaults.iconType) {
	    	tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_group/");

		} else if ("group1" == defaults.iconType) {
	    	tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_group1/");

	    } else if ("group2" == defaults.iconType) {
	    	tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_group2/");

	    } else if ("group3" == defaults.iconType) {
	    	tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_group3/");

	    } else if ("group4" == defaults.iconType) {
	    	tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_group4/");

	    } else if("menu" == defaults.iconType){
	    	tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_menu/");
	    }
	    else {
		tree.setImagePath(ROOT_CONTEXT + "/resource/earc/script/lib/dhtmlxtree/imgs/csh_scbrblue/");

	    }

	    var getTopNodeId = function(nodeId) {
	    	var level = tree.getLevel(nodeId);

	    	if(1==level){
	    		return nodeId;
	    	}

	    	var uid = tree.getParentId(nodeId);
	    	for(var i=1; i<level-1; i++){
	    		uid = tree.getParentId(uid);
	    	}

	    	return uid;
	    }

	    tree.getTopNodeId = getTopNodeId;

	    // tree.enableKeyboardNavigation(defaults.enableKeyboardNavigation);
	    tree.enableCheckBoxes(defaults.enableCheckBoxes);

	    if (defaults.enableMultiselection) {
		tree.enableMultiselection(1, 0);
	    }

	    tree.enableThreeStateCheckboxes(defaults.enableThreeStateCheckboxes);

	    tree.enableItemEditor(defaults.enableItemEditor);

	    if(defaults.enableItemEditor){
	    	tree.setEditStartAction(false,true);
	    }

	    tree.setOnClickHandler(defaults.setOnClickHandler);

	    tree.setOnLoadingEnd(defaults.setOnLoadingEnd);

	    tree.setOnDblClickHandler(defaults.setOnDblClickHandler);

	    tree.enableDragAndDrop(defaults.enableDragAndDrop);

	    tree.attachEvent("onDrag", defaults.onDragFn);

	    tree.attachEvent("onEdit", defaults.modifyFn);
	    
	    tree.attachEvent("onCheck", defaults.chkFn);

	    tree.parse(data.treeData,defaults.parseFn, "json");
	    
	    if (defaults.openAllItems) {
	    	tree.openAllItems(0);
	    }
	    if (defaults.openRoot) {
			var topNodeId = tree.getItemIdByIndex(0, 0);
			tree.openItem(topNodeId);
	    }

	    if(defaults.openAllOneDepth) {
	    	var fileIdArray = tree.getAllFatItems().split(",");
	    	for(var i=0; i<fileIdArray.length; i++){
	    		if(tree.getLevel(fileIdArray[i]) == 3){ // 4레벨까지 닫힘
	    			tree.openItem(fileIdArray[i]);
	    		}
	    	}
	    }

	    if (defaults.selectedItem) {
	    	tree.selectItem(defaults.selectedItem);
	    }

	    defaults.callback(tree, data);
	    // return tree;
	};

	// 최초시에만 생성
	// stepLoadData false 시에만생성
	if ((!defaults.stepLoadData) && ($('#' + defaults.treeMenu).length == 0)) {
	    var contextMenuOption;
	    if (defaults.funcTop != false) {
		contextMenuOption = {
		    domId : defaults.treeMenu, menuNum : 3, subMenuNames : {
			1 : 'Add top node', 2 : 'Close tree all', 3 : 'Open tree all'
		    }, width : 125
		};
	    } else {
		contextMenuOption = {
		    domId : defaults.treeMenu, menuNum : 3, subMenuNames : {
			2 : 'Close tree all', 3 : 'Open tree all'
		    }, width : 120
		};
	    }

	    $('#' + defaults.divId).gfn_contextMenu(contextMenuOption);

	    $('#' + defaults.treeMenu).click(function(e) {
			if (e.target.id == defaults.treeMenu + "1") {
			    defaults.funcTop();
			} else if (e.target.id == defaults.treeMenu + "2") {
			    tree.closeAllItems(0);
			} else if (e.target.id == defaults.treeMenu + "3") {
			    tree.openAllItems(0);
			}
	    });
	}

	$.gfn_ajax({
	    url 			: defaults.url
	    , method 		: defaults.method
	    , contentType 	: defaults.contentType
	    , callback 		: callback_simpleAjax
	    , blockId 		: '#' + defaults.divId
	    , parameters 	: $.extend({
	    	queryId : defaults.queryId
	    }
	    , options.parameters)
	}).execute();

    };
})(jQuery);