<%@ page contentType="text/html; charset="utf8" %> 
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.snaker.*,com.snaker.eng.*" %>
<%
	Factory f = Factory.getInstance();
	Setting setting = f.getSetting();
	
	String action = request.getParameter("action");
	if("getProxy".equals(action)){
		String proxyName= request.getParameter("proxyName");
		Setting.Proxy p = setting.findProxy(proxyName);
		if(p!=null){
			out.print(String.format("{proxyName:'%s',host:'%s',port:%d}",p.getName(),p.getHost(),p.getPort()));
		}
		return;
	}
	else if("removeProxy".equals(action)){
		String proxyName= request.getParameter("proxyName");
		setting.removeProxy(proxyName);
		return;
	}
	else if("updateProxy".equals(action)){
		String proxyName = request.getParameter("proxyName");
		String host = request.getParameter("host");
		int port = Integer.parseInt(request.getParameter("port"));
		Setting.Proxy p = new Setting.Proxy(proxyName);
		p.setHost(host);
		p.setPort(port);
		setting.updateProxy(p);
		return;
	}
	else if("updateSavePath".equals(action)){
		String savePath = request.getParameter("savePath");
		setting.setDefaultPath(savePath);
		return;
	}
	else if("save".equals(action)){
		setting.save();
		return;
	}
	List<Setting.Proxy> proxies = setting.getProxies();
%>
<html>
<head>
<title>Snaker- Power Download Platform</title>
<link href="css/style.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="js/common.js"></script>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script>
	function changeProxy(){
		var proxyName = $('#proxy').val();
		if(proxyName==null || proxyName.length<1) return;
		var parms={};
		parms.proxyName = proxyName;
		parms.action = "getProxy";
		$.ajax({
			   type: "GET",
			   url: "setting.jsp",
			   cache: false,
			   data:parms,
			   success:function(msg){
			 		var p;
			 		eval("p="+msg);
			 		$('#proxyName').val(p.proxyName);
			 		$('#host').val(p.host);
			 		$('#port').val(p.port);
			   },
			   error: function(req,status,err){
			   		report("Find proxy failed, due to:"+req.statusText);
			   }
			 });
	}

	function updateProxy(){
		var proxyName=$.trim($('#proxyName').val());
		var host=$.trim($('#host').val());
		var port=$.trim($('#port').val());
		
		if(proxyName.length==0){
			alert("Please fill the proxy name!");
			return;
		}
		if(host.length==0){
			alert("Please fill the host!");
			return;
		}
		if(port.length==0){
			alert("Please fill the port!");
			return;
		}
		
		var parms={};
		parms.proxyName = proxyName;
		parms.host = host;
		parms.port = port;
		parms.action="updateProxy";
		
		$.ajax({
			   type: "GET",
			   url: "setting.jsp",
			   cache: false,
			   data:parms,
			   success:function(msg){
			 		report("Update proxy succeed!");
			 		var updated = false;
			 		$('#proxy').children().each(function(){
			 			if($(this).val()==proxyName){
			 				updated = true;
			 			}
			 		});
			 		if(!updated){
			 			$('#proxy').append("<option value='"+proxyName+"'>"+proxyName+"</option>");
			 		}
			   },
			   error: function(req,status,err){
			   		report("Update proxy failed, due to:"+req.statusText);
			   }
			 });
	}
	
	function removeProxy(){
		var proxyName=$.trim($('#proxyName').val());
		
		if(proxyName.length==0){
			alert("Please fill the proxy name!");
			return;
		}
	
		var parms={};
		parms.proxyName = proxyName;
		parms.action="removeProxy";
		
		$.ajax({
			   type: "GET",
			   url: "setting.jsp",
			   cache: false,
			   data:parms,
			   success:function(msg){
			 		report("Remove proxy succeed!");
			 		$("#proxy option[value='"+proxyName+"']").remove();
			   },
			   error: function(req,status,err){
			   		report("Remove proxy failed, due to:"+req.statusText);
			   }
			 });
	}
	
	function report(msg){
		$('#message').html(msg);
	}
	
	$(function(){
		changeProxy();
	});
	
	$(window).unload( 
		function () {
			var parms={};
			parms.action="save";
			
			$.ajax({
				   type: "GET",
				   url: "setting.jsp",
				   cache: false,
				   data:parms
				 });
	 	} 
	 );
	
	function updateSavePath(){
		var savePath=$.trim($('#savePath').val());
		
		if(savePath.length==0){
			alert("Please fill the Save Path!");
			return;
		}
	
		var parms={};
		parms.savePath = savePath;
		parms.action="updateSavePath";
		
		$.ajax({
			   type: "GET",
			   url: "setting.jsp",
			   cache: false,
			   data:parms,
			   success:function(msg){
			 		report("Update save path succeed!");
			   },
			   error: function(req,status,err){
			   		report("Update save path failed, due to:"+req.statusText);
			   }
			 });
	}
</script>
</head>

<body>
<jsp:include page="menu.jsp"/>
<br/>
<div id="message"></div>
<br/>

<b>Proxy Setting:</b>
<select id="proxy" onchange="changeProxy()">
<%for(Setting.Proxy p:setting.getProxies()){%>
<option value='<%=p.getName()%>'><%=p.getName()%></option>
<%}%>
</select>

<br/>

<table>
<tr>
<td>Name:</td>
<td><input type="text" id="proxyName"/></td>
</tr>
<tr>
<td>Host:</td>
<td><input type="text" id="host"/></td>
</tr>
<tr>
<td>Port:</td>
<td><input type="text" id="port"/></td>
</tr>
</table>
<input type="button" value="New/Modify Proxy" onclick="updateProxy()"/>
<input type="button" value="Remove Proxy" onclick="removeProxy()"/>

<br/><br/><br/>
<b>Default Save Path:</b><br/>
<input type="text" id="savePath" value="<%=setting.getDefaultPath()%>"/>
<br/>
<input type="button" value="Change" onclick="updateSavePath()"/>

</body>
</html>