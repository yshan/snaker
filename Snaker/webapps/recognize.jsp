<%@ page language="java" import="java.util.*,java.io.*" %><%@ page language="java" import="com.snaker.*" %><%
	Factory f = Factory.getInstance();
	RecognizerManager rm = f.getRecognizerManager();
	String action = request.getParameter("action");
	if("peek".equals(action)){
		RecognizerManager.RecognizeItem item = rm.peek();
		if(item!=null){
			out.print(item.getId()+"");
		}
		else{
			out.print("0");
		}
		return;
	}
	else if("getImage".equals(action)){
		String id = request.getParameter("id");
		RecognizerManager.RecognizeItem item = rm.findItem(Long.parseLong(id));
		if(item!=null){
			response.setHeader("Cache-Control","no-store"); 
			response.setDateHeader("Expires",0); 
			response.setContentType("image/jpeg"); 
			OutputStream os = response.getOutputStream();
			os.write(item.getImage());
			os.close();
		}
	}
%>