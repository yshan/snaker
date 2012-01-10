// ==UserScript==
// @name		TrainTicket
// @title		2012火车购票
// @description	2012火车购票
// @parameter   *userName 登录名
// @parameter   *password 密码
// ==/UserScript==

var lessonRegex = /http:\/\/englishpod.com\/lessons\/[^\"\/]*/mg;
var mp3Regex = /http:\/\/s3.amazonaws.com\/englishpod\.com\/.*englishpod.*pr\.mp3/;
var allLessons ={};

function getLessons(page){
	var url = "http://englishpod.com/lessons?page=" + page;
	var response = $.get(url);
	if (response.statusCode / 100 == 2) {
		$.print("download Lesson successfully,url:" + url);
		var body = response.body;
		var lessons = body.match(lessonRegex);
		for(i=0;i<lessons.length;i++){
			$.print(lessons[i]);
			allLessons[lessons[i]]=true;
		}
	} else {
		$.("download failed,err:" + response.statusCode);
	}
}

function downloadLesson(lesson){
		var response = $.get(lesson);
		if (response.statusCode / 100 == 2) {
			$.print("download Lesson successfully");
			var mp3 = response.body.match(mp3Regex);
			if(mp3!=null && mp3.length>0){
				mp3 = mp3[0];
			}
			if (mp3!=null) {
				$.save(mp3);
			}
		} else {
			$.print("download lesson failed,err:" + response.statusCode);
		}
	}

function login(){
	//get a rand code
	var pic = "https://dynamic.12306.cn/otsweb/passCodeAction.do?rand=lrand";
	var code = $.recognize(pic); 
	if(code==null || code=='') return false;
	//send the login query
	var param = {};
	param["loginUser.user_name"] = $.userName;
	param["user.password"] = $.password;
	param["randCode"]=code;
	param["org.apache.struts.taglib.html.TOKEN"]="835e9d72d94e2c1f691c6566790810b5";
	var url = "https://dynamic.12306.cn/otsweb/loginAction.do?method=login";
	var response = $.post(url,param);
	$.print(response.body);
	return true;
} 

while(!login()){}



