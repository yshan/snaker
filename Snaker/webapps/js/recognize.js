function complete_recognize(id,result){
	var parms = {};
	parms.action='complete';
	parms.id=id;
	parms.result=result;
	$.ajax({
		   type: "GET",
		   url: "recognize.jsp",
		   cache: false,
		   data:parms,
		   success:function(msg){
			   check_recognize();
		   },
		   error: function(req,status,err){
		   		alert("complete recognize failed, due to:"+req.statusText);
		   }
	});
}

function show_recognize_task(id){
	$('#recognize').show();
	$('#reco_image').attr("src","recognize.jsp?action=getImage&id="+id);
	var t = $(window).height()-100-$('#recognize').height();
	$('#recognize').offset({ top: t, left: 0 });
	
	$('#reco_result').keydown(function(event){
		if (event.keyCode == '13') {
     		var result = $('#reco_result').val();
			$('#recognize').hide();
			complete_recognize(id,result);
   		}
	});
}

function check_recognize(){
	var parms = {};
	parms.action='peek';
	$.ajax({
		   type: "GET",
		   url: "recognize.jsp",
		   cache: false,
		   data:parms,
		   success:function(msg){
		   		if(msg!='0'){
			   		show_recognize_task(msg);
			   	}
			   	else{
			   		window.setTimeout(check_recognize,1000);
			   	}
		   },
		   error: function(req,status,err){
		   		window.setTimeout(check_recognize,10000);
		   }
	});
}
$(function(){
	check_recognize();
});