function show_recognize_task(id){
	var t = $(window).height()-200; 
	$('#recognize').offset({ top: t, left: 0 });
	$('#recognize').show();
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
			   show_recognize_task(msg);
		   },
		   error: function(req,status,err){
		   		alert("read recognize task failed, due to:"+req.statusText);
		   }
	});
}
$(function(){
	check_recognize();
});