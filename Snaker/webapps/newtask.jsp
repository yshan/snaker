<%@ page contentType="text/html;charset=utf8" %> 
<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.snaker.*" %>
<%
	Factory f = Factory.getInstance();
	Setting setting = f.getSetting();
	TaskManager tm = f.getTaskManager();
	EngineManager em = f.getEngineManager();
	String engineName = request.getParameter("eng");
	
	Engine def = em.getEngine(engineName);
	if(def == null){
		out.println("Bad Engine :"+engineName);
		return;
	}
	List<Engine.EngineProperty> properties = def.getProperties();
	
	String download = request.getParameter("download");
	if(download!=null){
		String proxy = request.getParameter("proxy");
		String path = request.getParameter("path");
		Task sft = em.createTask(engineName,request.getParameterMap());
		if(sft!=null){
			if(!proxy.equals("0000")){
				sft.setProxy(setting.findProxy(proxy));
			}
			sft.setSavePath(path);
			tm.startTask(sft);
		}
		else{
			response.sendError(500);
		}
		return;
	}
%>
<html>
<head>
<title>Snaker- Power Download Platform</title>
<link href="css/style.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="js/common.js"></script>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script>
	function downloading(){
		var parms={};
		var tmp;
		<%for(Engine.EngineProperty property:properties){%>
		tmp =  $.trim($('#<%=property.getName()%>').val());
		if(tmp.length<=1 && <%=!property.isOptional()%>){
			alert("Please fill the <%=property.getName()%>!");
			return;
		}
		parms.<%=property.getName()%>= tmp;
		<%}%>
		parms.eng = '<%=engineName%>';
		parms.password = $('#password').val();
		parms.proxy = $('#proxy').val();
		parms.path=$('#path').val();
		parms.download = 1;
		
		$.ajax({
			   type: "GET",
			   url: "newtask.jsp",
			   cache: false,
			   data:parms,
			   success:function(msg){
			 		report("Create download task succeed!");
			   },
			   error: function(req,status,err){
			   		report("Create download task failed, due to:"+req.statusText);
			   }
			 });  
		
	}
	
	function report(msg){
		$('#message').html(msg);
	}
</script>
</head>
<body>
<jsp:include page="menu.jsp"/>
<br/>
<div id="description"><%=def.getDescription()%></div>
<br/>
<table>
<%for(Engine.EngineProperty property:properties){%>
<tr>
<td>
	<%=property.getTitle()%>:
	<span style="color:red;"><%=property.isOptional()?"":"*"%></span>
</td>
</tr>
<tr>
<td>
<%String inputType=property.getInputType();
if("textarea".equals(inputType)){%>
<textarea id="<%=property.getName()%>"></textarea>
<%} else {%>
<input type="<%=inputType%>" id="<%=property.getName()%>"></input>
<%}%>
</td>
</tr>
<%}%>
<tr>
<td>Proxy:</td>
</tr>
<tr>
<td>
<select id="proxy">
<option value='0000'>Direct Connection</option>
<%for(Setting.Proxy p:setting.getProxies()){%>
<option value='<%=p.getName()%>'><%=p.getName()%></option>
<%}%>
</select>
</td>
</tr>
<tr>
<td>Save to:</td>
</tr>
<tr>
<td><input type="text" id="path" value="<%=setting.getDefaultPath()%>" ></input>
</td>
</tr>
<tr>
<td>
<input type="button" id="download" value="Download" onclick="downloading()"/>
</td>
</tr>
</table>
<br/>
<div id="message" style="color:red"></div>
</body>
</html>