<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<link rel="stylesheet" href="<c:url value='/css/user/dashboard.css'/>">
<style>
	tr.highlight td.jsgrid-cell {
		background-color: #BBDEFB;
	}
	.jsgrid-header-row  {
	    text-align: center;
	}
	.jsgrid>.jsgrid-pager-container{
		color:#007bff;
	}
	.btn btn-sm btn-outline-primary{
		width:70px;
	}
    .grid-cell-grey{
        background: #f3f3f3;
        cursor: pointer;
    }
</style>
<script>
var list =[];
var popMcd = "";


//버튼 기능
$(function(){
	//조회
	$('#btn_search').click(function(){ //조회버튼 클릭
		getGraphResult();
	});
	
	
	$("#f_graph_sel").change(function(){
		setGraph();
		setGraphParam();
		setMaterial('materials');
		setMaterial('speed');
		setMaterial('product_grp');
		setMaterial('bellows_mtrl');
		setMaterial('arrangement');
		setMaterial('temp');
		setMaterial('curve_no');
		$("#input_size").val("")
	});
	
});


function setMaterial(sType){
	console.log(sType);
	var vGraphNo = $("#f_graph_sel").val();
	
	if(!(vGraphNo == "A2" || vGraphNo == "A3" || vGraphNo == "A4" || vGraphNo == "A5" || vGraphNo == "A6" ||
			vGraphNo == "A7") && sType=="curve_no"){
		$('#f_'+sType+'_sel').empty();
		return;
	}
	
	$.ajax({
		type:"POST",
		url:"<c:url value='/rb/getRbGraphSel.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({
			GRAPH_NO : $("#f_graph_sel").val(),
			SEL_TYPE : sType
		})
	}).done(function(result){
		$('#f_'+sType+'_sel').empty();
		//var option = $("<option selected value></option>");
		//$('#f_materials_sel').append(option);
		var option = "";
		for(var i = 0; i<result.length; i++){
			option = $("<option value="+result[i].SEL_CD+">"+result[i].SEL_NM+"</option>");
			$('#f_'+sType+'_sel').append(option);
		}
	})	
}


function setGraph(){
	var vGraphNo = $("#f_graph_sel").val();
	
	$("#graph_image").attr("src","<c:url value='/images/r_graph/"+vGraphNo+".png'/>");;
}

function setGraphParam(){
	var vGraphNo = $("#f_graph_sel").val();
	if(vGraphNo == "A2" || vGraphNo == "A3" || vGraphNo == "A4" || vGraphNo == "A5" || vGraphNo == "A6" ||
			vGraphNo == "A7" ){
		
		$("#f_curve_no_sel").attr("disabled", false);
		$("#f_materials_sel").attr("disabled", true);
		$("#f_speed_sel").attr("disabled", true);
		$("#f_product_grp_sel").attr("disabled", true);
		$("#f_bellows_mtrl_sel").attr("disabled", true);
		$("#f_arrangement_sel").attr("disabled", true);
		$("#f_temp_sel").attr("disabled", true);
		
	}else{
		$("#f_curve_no_sel").attr("disabled", true);
		$("#f_materials_sel").attr("disabled", false);
		$("#f_speed_sel").attr("disabled", false);
		$("#f_product_grp_sel").attr("disabled", false);
		$("#f_bellows_mtrl_sel").attr("disabled", false);
		$("#f_arrangement_sel").attr("disabled", false);
		$("#f_temp_sel").attr("disabled", false);
	}
			
}


function getGraphResult(){
	var vGraphNo = $("#f_graph_sel").val();
	
	$.ajax({
		type:"POST",
		url:"<c:url value='/rb/getRbGraphResult.do'/>",
		headers: {'X-CSRF-TOKEN': $('#csrfvalue').val()},
		contentType: "application/json",
		data: JSON.stringify({
			GRAPH_NO : $("#f_graph_sel").val(),
			MATERIALS : $("#f_materials_sel").val(),
			SPEED : $("#f_speed_sel").val(),
			PRODUCT_GRP : $("#f_product_grp_sel").val(),
			BELLOWS_MTRL : $("#f_bellows_mtrl_sel").val(),
			ARRANGEMENT : $("#f_arrangement_sel").val(),
			TEMP : $("#f_temp_sel").val(),
			INPUT_SIZE : $("#input_size").val(),
			CURVE_NO : $("#f_curve_no_sel").val()
			
		})
	}).done(function(result){
		//alert(result.result);
		var vResult = "";
		
		$("#dis_curve_no").html(result.curve_no);
		$("#dis_value").html(result.result);
		
		//vResult += "Curve No. : " + result.curve_no +"<br/>";
		//vResult += "Value : " + result.result +"<br/>";
		//$("#graph_result").html(vResult);
	})	
}



</script>
<body>
	<!--  화면이름 / 버튼 -->
	<div class="col-12 p-3">
		<div class="row">
			<input id="csrfvalue" type='hidden' name='${_csrf.parameterName}' value='${_csrf.token}' />
			<div class="col-6" >
				<div class="h5" style="float:left;width:50%;">
					<strong><i class="fas fa-sitemap"></i> <span class="ml-1">Graph Data</span></strong>
				</div>	
			</div>
			<div class="col-6  text-right" >
				<button type="button" class="btn btn-outline-success"  id="btn_search">조회 <i class="fa fa-search"></i></button>
			</div>
		</div>
	</div>

	<!--  Search Conds. -->
	<div class="card  ml-3 mr-3">
		<div class="row" style="height:50px;">
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:120px;">
				<span class="font-weight-bold">Graph</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_graph_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				    <option value='-'>-</option>
					<option value='A1-1'>A1-1</option>
					<option value='A1-2'>A1-2</option>
					<option value='A1-3'>A1-3</option>
					<option value='A1-4'>A1-4</option>
					<option value='A1-5'>A1-5</option>
					<option value='A1-6'>A1-6</option>
					<option value='A1-7'>A1-7</option>
					<option value='A1-8'>A1-8</option>
					<option value='A1-9'>A1-9</option>
					<option value='A1-10'>A1-10</option>
					<option value='A1-11'>A1-11</option>
					<option value='A1-12'>A1-12</option>
					<option value='A1-13'>A1-13</option>
					<option value='A1-14'>A1-14</option>
					<option value='A1-15'>A1-15</option>
					<option value='A1-16'>A1-16</option>
					<option value='A1-17'>A1-17</option>
					<option value='A1-18'>A1-18</option>
					<option value='A1-19'>A1-19</option>
					<option value='A1-20'>A1-20</option>
					<option value='A1-21'>A1-21</option>
					<option value='A1-22'>A1-22</option>
					<option value='A1-23'>A1-23</option>
					<option value='A1-24'>A1-24</option>
					<option value='A1-25'>A1-25</option>
					<option value='A1-26'>A1-26</option>
					<option value='A1-27'>A1-27</option>
					<option value='A2'>A2</option>
					<option value='A3'>A3</option>
					<option value='A4'>A4</option>
					<option value='A5'>A5</option>
					<option value='A6'>A6</option>
					<option value='A7'>A7</option>
				</select>
			</div>
<!-- 			<div class="col-lg-1 text-center col-sm-12 my-auto"  style="min-width:100px;"> -->
<!-- 				<span class="font-weight-bold">Curve No.</span> -->
<!-- 			</div> -->
<!-- 			<div class="col-lg-2 text-center col-sm-12 my-auto" > -->
<!-- 				<select id="f_curveno_sel" class="form-control input-small p-0 pl-1" style="width: 50px; height: 25px; font-size: 12px;"> -->
<!-- 				</select> -->
<!-- 			</div> -->
			
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:100px;">
				<span class="font-weight-bold">Materials</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_materials_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				</select>
			</div>
			
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:100px;">
				<span class="font-weight-bold">Speed (RPM)</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_speed_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				</select>
			</div>
			
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:120px;">
				<span class="font-weight-bold">Product Group</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_product_grp_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				</select>
			</div>
			
			
		</div>
		
		<div class="row" style="height:50px;">
			
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:120px;">
				<span class="font-weight-bold">Bellows Material</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_bellows_mtrl_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				</select>
			</div>
			
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:120px;">
				<span class="font-weight-bold">Arrangement</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_arrangement_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				</select>
			</div>
			
			
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:120px;">
				<span class="font-weight-bold">Temp.</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_temp_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				</select>
			</div>
			
				
		</div>
		<div  class="row" style="height:50px;">
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:100px;">
				<span class="font-weight-bold" style="color:blue;">Input Value</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<input type="text" id="input_size" name="input_size"  class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
			</div>
			<div class="col-lg-1 text-center col-sm-1 my-auto"  style="min-width:100px;">
				<span class="font-weight-bold" >Curve No.</span>
			</div>
			<div class="col-lg-2 text-center col-sm-2 my-auto" >
				<select id="f_curve_no_sel" class="form-control input-small p-0 pl-1" style="width: 150px; height: 25px; font-size: 12px;">
				</select>
			</div>
		</div>
		
	</div>

	<!--  Grid  -->
	<div class="card  ml-3 mr-3 mt-1 pt-3">
		<div class="row">
			<div class="col-6">
				<img id="graph_image" style="height:500px;padding:10px;"/>
			</div>
			<div class="col-6">
				<div class="card  ml-3 mr-3 p-2">
					<div class="row">
						<div class="col-lg-3 text-center col-sm-3 my-auto"  style="min-width:100px;">
							<span class="font-weight-bold">Cruve No : </span>
						</div>
						<div class="col-lg-3 text-center col-sm-3 my-auto" >
							<div id="dis_curve_no" ></div>
						</div>
					</div>
					<div class="row">
						<div class="col-lg-3 text-center col-sm-3 my-auto "  style="min-width:100px;">
							<span class="font-weight-bold">Result Value : </span>
						</div>
						<div class="col-lg-3 text-center col-sm-3 my-auto" >
							<div id="dis_value" ></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>


</body>