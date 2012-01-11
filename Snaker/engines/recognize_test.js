// ==UserScript==
// @name		RecognizeTest
// @title		Recognize Test
// @description	a simple test for recognize
// @parameter   *url text URL 
// ==/UserScript==

var url =  $.url;
var result = $.recognize(url);
$.print(result);