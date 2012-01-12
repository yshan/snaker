// ==UserScript==
// @name		RecognizeTest
// @title		Recognize Test
// @description	a simple test for recognize
// @parameter   *url textarea URL 
// ==/UserScript==

var url =  $.url;
var result = $.recognize(url,true);
$.print(result);