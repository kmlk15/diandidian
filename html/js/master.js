// JavaScript Document
$(function() {
	
	/* pre-load images */
	(function($) {
  		var cache = [];
  		// Arguments are image paths relative to the current page.
  		$.preLoadImages = function() {
    		var args_len = arguments.length;
    		for (var i = args_len; i--;) {
      			var cacheImage = document.createElement('img');
      			cacheImage.src = arguments[i];
      			cache.push(cacheImage);
    		}
  		}
	})(jQuery)
	jQuery.preLoadImages("images/button02.png", "images/closeBtnHover.png", "images/add-bag-btn-hover.png", "images/detail-btn-hover.png");
	
	
	var $win = $(window);
	
	
	//header fix top and sidebar not scroll up
	$("#header").scrollToFixed({zIndex: 99});
	$("#sidebar").scrollToFixed({zIndex: 98, marginTop: $("#header").outerHeight(true)});
	
	//center Attraction List
	var itemWidth = $("#main ul li").outerWidth(true);
	//content min-width
	
	$win.resize(function(e) {
		var mainWidth = $("#main").width();
		var rowCount = Math.floor(mainWidth / itemWidth);
		$("#main .wrap").width(itemWidth*rowCount);
	}).trigger("resize");
	
	//sidebar accordion
	$( "#sidebar .accordion").accordion({heightStyle: "content"});
	
	//token input

        
	
	
	
	
	
	
	
	
	
})

























































































