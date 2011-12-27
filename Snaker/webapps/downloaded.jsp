<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.snaker.*,com.snaker.eng.*" %>
<%
	Factory f = Factory.getInstance();
	DownloadManager dm = f.getDownloadManager();
	Collection<Downloader> downloadings = dm.getDownloaded();
	
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
			result.append("\""+d.getUrl()+"\",");
			result.append("\""+d.getStatus()+"\",");
			result.append("\""+Util.formatPeroid(d.getTimeCost())+"\",");
			result.append("\""+Util.formatSize(d.getFileSize())+"\",");
			result.append("\""+Util.formatDate(d.getStartTime())+"\",");
			result.append("\""+Util.formatDate(d.getEndTime())+"\",");
			result.append("\""+d.getDescription()+"\"");
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
			"sAjaxSource": 'downloaded.jsp?action=query',
			"sPaginationType": "full_numbers",
			"aaSorting": [[5,'desc']],
			"aoColumns":[
            		{//the url
            			"sClass":"url"
            		},
            		{ // the Status
						"sWidth":"8em"
					},
					{// time cost
						"sWidth":"8em"
					},
					{ // the Size
						"sWidth":"5em"
					}
					,
					{ // start time
						"sWidth":"8em"
					}
					,
					{ // end time
						"sWidth":"8em"
					}
					,
					{ // description
						"sWidth":"10em"
					}
			]
		} );
	});
	
	function refreshData(){
		$('#down').dataTable().fnReloadAjax();
	}
	
	$(function(){
		window.setInterval(refreshData,10000);
	});
</script>
</head>

<body>
<jsp:include page="menu.jsp"/>
<p/>
<table width="100%" border="0" cellpadding="0" cellspacing="0" id="down" class="pretty">
<thead>
<tr>
	<th>Url</th>
	<th>Status</th>
	<th>Time Cost</th>
	<th>Size</th>
	<th>Start Time</th>
	<th>End Time</th>
	<th>Description</th>
</tr>
</thead>
<tbody>
</tbody>
</table>

</body>
</html>
</html>