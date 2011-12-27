<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.snaker.*,com.snaker.eng.*" %>
<%
	Factory f = Factory.getInstance();
	DownloadManager dm = f.getDownloadManager();
	Collection<Downloader> downloadings = dm.getDownloading();
	
	String action = request.getParameter("action");
	if("query".equals(action)){
		StringBuilder result = new StringBuilder();
		result.append("{ \"aaData\": [");
		boolean first = true;
		for(Downloader d:downloadings){
			if(first){
				first = false;
			}
			else{
				result.append(",");
			}
			result.append("[");
			result.append("\""+d.getStatus()+"\",");
			result.append("\""+d.getUrl()+"\",");
			result.append("\""+Util.formatSize(d.getFileSize())+"\",");
			result.append("\""+Util.formatDate(d.getStartTime())+"\",");
			result.append(d.getProgress()+",");
			result.append("\""+Util.formatSize(d.getSpeed())+"/s\",");
			result.append("\""+Util.formatPeroid(d.getTimeLeft())+"\"");
			result.append("]");
		}
		result.append("] }");
		out.print(result);
		return;
	}
%>
<html>
<head>
<title>Snaker- Power Download Platform</title>
<link type="text/css" href="css/style.css" rel="Stylesheet" />
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="js/dataTables.reloadAjax.js"></script>
<script>
	$(function(){
		$('#down').dataTable( {
			"bProcessing": true,
			"sAjaxSource": 'downloading.jsp?action=query',
			"sPaginationType": "full_numbers",
			"aaSorting": [[3,'desc']],
			"aoColumns":[
					{ //the status
						"sWidth":"3em",
						"fnRender": function ( oObj ) {
							var data = oObj.aData[0];
							if(data=='RUNNING'){
								return '>>>'
							}
							else if(data=='STARTED'){
								return '>--'
							}
							else{
								return '---'
							}
            			}
            		}
            		,
            		{//the url
 						"sClass":"url"           		
            		}
					,
					{ // the Size
						"sWidth":"5em"
					}
					,
					{ // start time
						"sWidth":"8em"
					}
					,
				{ // the progress
					 "sWidth":"220px",
                     "fnRender": function ( oObj ) {
							var data = oObj.aData[4];
							if(data == -1){
								return "";
							}
							else{
								data = Math.round(data*100);
								return '<div class="graph" style="text-align:left"><strong class="bar" style="width: '+data+'%;"><span>'+data+'%</span></strong></div>';
							}	
							
            			}
				}
				,
				{//speed
					"sWidth":"7em"
				}
				,
				{ //time left
					"sWidth":"8em"
				}
			]
		} );
	});
	
	function refreshData(){
		$('#down').dataTable().fnReloadAjax();
	}
	
	$(function(){
		window.setInterval(refreshData,5000);
	});
</script>
</head>

<body>
<jsp:include page="menu.jsp"/>
<p/>
<table width="100%" border="0" cellpadding="0" cellspacing="0" id="down" class="pretty">
<thead>
<tr>
	<th>&nbsp;</th>
	<th>Url</th>
	<th>Size</th>
	<th>Start Time</th>
	<th>Percent</th>
	<th>Speed</th>
	<th>Time Left</th>
</tr>
</thead>
<tbody>
</tbody>
</table>

</body>
</html>