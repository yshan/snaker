// ==UserScript==
// @name		TrainTicket
// @title		2012火车购票
// @description	2012火车购票
// @parameter   *userName 登录名
// @parameter   *password 密码
// @parameter   *from 出发地
// @parameter   *to 目的地
// @parameter   *date 日期(YYYY-MM-DD)
// @parameter   *trainCode 车次
// @parameter   *trueName 姓名
// @parameter   *pepoleId 身份证
// @parameter   *telephone 电话号码
// ==/UserScript==

function getStationCode(sta){
}

function login(){
	//get a rand code
	var pic = "https://dynamic.12306.cn/otsweb/passCodeAction.do?rand=lrand";
	var code = $.recognize(pic,true); 
	if(code==null || code=='' || code.length!=4) {
		$.print("Bad code:"+code);
		return false;
	}
	//send the login query
	var param = {};
	param["loginUser.user_name"] = $.userName;
	param["user.password"] = $.password;
	param["randCode"]=code;
	param["org.apache.struts.taglib.html.TOKEN"]="835e9d72d94e2c1f691c6566790810b5";
	var url = "https://dynamic.12306.cn/otsweb/loginAction.do?method=login";
	var response = $.post(url,param);
	var body= response.body;
	$.print(body);
	if(body.indexOf("您最后一次登录时间为")!=-1){
		$.print("登录成功");
		return true;
	}
	else if(body.indexOf("请输入正确的验证码")!=-1){
		$.print("验证码错误");
		return false;
	}
	else{
		$.print("登录失败");
		return false;
	}
}

function queryTrain(){
	var url = "https://dynamic.12306.cn/otsweb/order/querySingleAction.do?method=queryststrainall";
	var param={};
	param.date=$.date;
	param.fromstation=getStationCode($.from);
	param.tostation=getStationCode($.to);
	param.starttime="00:00--24:00";
	var response = $.post(url,param);
	var body= response.body;
	$.print(body);
}

while(!login()){$.sleep(1000);}
queryTrain();


