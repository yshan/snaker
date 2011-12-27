<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.snaker.*,com.snaker.eng.*" %>
<%
	Factory f = Factory.getInstance();
	EngineManager em = f.getEngineManager();
	List<Engine> engines = em.getEngines();
	
%>
<link href="css/droppy.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="js/jquery.droppy.js"></script>

<script type='text/javascript'>
  $(function() {
    $('#nav').droppy();
  });
</script>

<ul id='nav'>
  <li><a href='downloading.jsp'>Downloading</a></li>
  <li><a href='#'>New Download Task</a>
  	<ul>
  	<%for(Engine eng:engines){%>
      <li><a href='newtask.jsp?eng=<%=eng.getName()%>' title='<%=eng.getDescription()%>'><%=eng.getTitle()%></a></li>
    <%}%>  
    </ul>
  </li>
  <li><a href='downloaded.jsp'>Finished</a></li>
  <li><a href='setting.jsp'>Setting</a></li>
</ul>