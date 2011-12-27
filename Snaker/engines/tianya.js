// ==UserScript==
// @name		tianya
// @title		tianya
// @description	download picture from tianya.cn
// ==/UserScript==

/*example:http://www.tianya.cn/techforum/content/607/1/2692.shtml*/
var threadRegex = /http:\/\/www\.tianya\.cn\/techforum\/content\/[^\"]+\.shtml/mg;
/*example:http://www.tianya.cn/new/techforum/ArticlesList.asp?pageno=9&iditem=607&part=0&nextarticle=2011%2D12%2D1+15%3A14%3A15&subitem=&strpart=%D7%EE%D0%C2%B8%FC%D0%C2*/
var nextPageRegex = /http:\/\/www\.tianya\.cn\/new\/techforum\/ArticlesList.asp\?pageno=[0-9]+&iditem=[0-9]+&part=0&nextarticle=[^\"]+/mg;
var picRegex = /original=\"http:\/\/[^\"]+\.jpg/mg;
var allThreads ={};
var forumid = 607;

function getThreads(page){
	var response = $.get(page);
	if (response.statusCode / 100 == 2) {
		$.print("download page successfully,url:" + page);
		var body = response.body;
		var threads = body.match(threadRegex);
		for(i=0;i<threads.length;i++){
			$.print(threads[i]);
			allThreads[threads[i]]=true;
		}
		var nextPage = body.match(nextPageRegex);
		if(nextPage.length>0){
			$.print("next page url:" + nextPage[0]);
			return nextPage[0];
		}
	} else {
		$.("download failed,err:" + response.statusCode);
	}
	return null;
}

function getThreadId(thread){
	var t1 = thread.lastIndexOf('/');
	var t2 = thread.lastIndexOf('.');
	return thread.substring(t1+1,t2);
}

function downloadPicture(thread){
	var response = $.get(thread);
	var t = getThreadId(thread);
	if (response.statusCode / 100 == 2) {
		$.print("download thread successfully");
		var pic = response.body.match(picRegex);
		if(pic!=null){
			for(i=0;i<pic.length;++i){
				var p = pic[i].substring(10);
				$.print("find picture:"+p);
				$.save(p,"",t+"_"+i+".jpg");
			}
		}
	} else {
		$.print("download thread failed,err:" + response.statusCode);
	}
}

var url = "http://www.tianya.cn/techforum/articleslist/0/"+forumid+".shtml";
for(page=1;page<=1;++page){
	url = getThreads(url);
}

for(thread in allThreads){
	downloadPicture(thread);
}