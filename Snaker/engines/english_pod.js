// ==UserScript==
// @name		EnglishPod
// @title		English Pod
// @description	download lessons from englishpod.com
// @parameter   *email Email
// @parameter   *password Password
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
	$.print("email="+$.email+",password="+$.password);

	//send the login query
	var param = {};
	param.email = $.email;
	param.password = $.password;
	var url = "https://englishpod.com/accounts/signin";
	var response = $.post(url,param);
	var statusCode = response.statusCode;
	var location;
	var result = false;
	if(statusCode == 302){
		location = response.headers["Location"];
		if(location.length>0 && location!=url){
			$.get(location);
			$.print("Login successfully!");
			result = true;
		}
	}
	if(!result){
		$.print("Login failed!");
	}
	return result;
} 

if(login()){
	for(page=1;page<=47;++page){
		getLessons(page);
	}
	for(les in allLessons){
		downloadLesson(les);
	}
}

