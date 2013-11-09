// JavaScript Document
$(function() {
	$("#top-nav .dropdown ul").width($("#top-nav .dropdown").width());
	//$("#user-sign.signin .account .user-info").width($("#user-sign.signin .account .user-info span").width());
	//$("#user-sign.signin .account ul").width($("#user-sign.signin .account").width());
	$("#attraction-detal .thumbnails .ul-wrap ul li img").css("opacity", 0.45);
	$("#attraction-detal .thumbnails .ul-wrap ul li.active img").css("opacity", 1);
	$("#attraction-detal .thumbnails .ul-wrap ul li").hover(
		function() {
			$(this).find("img").css("opacity", 1);
		},
		function() {
			if(!$(this).hasClass("active")) {
				$(this).find("img").css("opacity", 0.45);
			}
		}
	);
	
})



