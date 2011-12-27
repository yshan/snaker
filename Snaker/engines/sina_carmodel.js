// ==UserScript==
// @name		sinacarmodel
// @title		Sina Car Model 
// @description	download picture from photo.auto.sina.com.cn
// ==/UserScript==

/*example:href="/chezhan/2011shanghai/chemotuji/467/"*/
var threadRegex = /\/chezhan\/2011shanghai\/chemotuji\/[0-9]+\//mg;
/*example:http://www.sinaimg.cn/qc/photo_auto/chezhan/2011/42/13/38/4949_src.jpg*/
var picRegex = /http:\/\/www\.sinaimg\.cn\/qc\/photo_auto\/chezhan\/[^\"]+_src.jpg/mg;
/*exmaple:href="/chezhan/2011shanghai/chemotuji/26/994/#pic" class="NextC"*/
var nextPicRegex = /\/chezhan\/2011shanghai\/chemotuji\/[0-9]+\/[0-9]+\/#pic\"\s+class=\"NextC\"/mg;
var allThreads ={};
var forumid = 607;

function getThreads(page){
	var url = "http://photo.auto.sina.com.cn/exhi_new/custom/42/jiansuo.php?shengao=0&xiongwei=0&yaowei=0&fuse=0&tunwei=0&faxing=0&p="+page;
	var response = $.get(url);
	if (response.statusCode / 100 == 2) {
		$.print("download page successfully,url:" + url);
		var body = response.body;
		var threads = body.match(threadRegex);
		for(i=0;i<threads.length;i++){
			$.print(threads[i]);
			allThreads[threads[i]]=true;
		}
	} else {
		$.("download failed,err:" + response.statusCode);
	}
	return null;
}

function getModelId(thread){
	var tmp = 'chemotuji/';
	var t1 = thread.indexOf(tmp);
	var t2 = thread.indexOf('/',t1+tmp.length);
	return thread.substring(t1+tmp.length,t2);
}

function downloadPicture(thread,model_id){
	var url = "http://photo.auto.sina.com.cn"+thread;
	var response = $.get(url);
	if (response.statusCode / 100 == 2) {
		$.print("download model successfully:"+model_id);
		var pic = response.body.match(picRegex);
		if(pic!=null){
			for(i=0;i<pic.length;++i){
				var p = pic[i];
				$.print("find picture:"+p);
				$.save(p);
			}
		}
		//find the next picture
		var nextPic = response.body.match(nextPicRegex);
		if(nextPic!=null && nextPic.length>0){
			var np = nextPic[0].substring(0,nextPic[0].indexOf('#pic'));
			$.print("find next picture:"+np);
			if(getModelId(np)==model_id){
				downloadPicture(np,model_id);
			}
		}
	} else {
		$.print("download thread failed,err:" + response.statusCode);
	}
}

for(page=1;page<=10;++page){
	getThreads(page);
}

for(thread in allThreads){
	var model_id = getModelId(thread);
	downloadPicture(thread,model_id);
}