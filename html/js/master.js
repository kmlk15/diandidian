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
	$("#shadow-divide img").scrollToFixed({zIndex: 99});
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
	$( "#sidebar .accordion").accordion({heightStyle: "content", collapsible: true, active: false});
	
	//token input
	$("#search-address").tokenInput([
                {id: 7, name: "香港"},
                {id: 11, name: "澳门"},
                {id: 13, name: "JavaScript JavaScript"}
            ],{ theme: "facebook"});
     
	$("#search-address-wrap #token-input-search-address").focus(function(e) {
		$("#search-address-wrap label").hide();
	}).blur(function(e) {
		var val = $("#search-address").tokenInput("get");
		if(val == "") {
			$("#search-address-wrap label").show();
		}
	});
	$("#search-address-wrap label").click(function(e) {
		$("#search-address-wrap #token-input-search-address").focus();
	});
	    
	
	
	$("#main ul li .add-bag").click(function(e) {
		var bagAddress = $("#main h2 .address").text();//which bag to add
		var $ul = $("#my-bag .accordion .accordion div ul");//ul to add li
		$ul = $($ul[0]);
		var titleText = $(this).prevAll(".title").find("h3").text();
		var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td><td class="del"><a href="#"></a></td></tr></table></li>';
		$ul.append(itemHtml);
		updateBagCount($ul);
		searchBagItem($ul,'');
		return false;
	});
	
	$("#my-bag .accordion .accordion div ul li table td.del a").live('click',function() {
		var $itemDel = $(this).parents("li");
		var $ul = $(this).parents("ul");
		$itemDel.remove();
		updateBagCount($ul);
		return false;
	});
	
	
	$("#main ul li").hover(
		function(){
			var addressName = $(this).find(".title h3").text();
			var $ul = $("#my-bag .accordion .accordion div ul");
			$ul = $($ul[0]);
			//alert(searchBagItem($ul, addressName));
			if(!searchBagItem($ul, addressName)) {
				$(this).find(".add-bag").css("display", "block");
			}
			$(this).find(".detail").css("display", "block");
		},
		function(){
			$(this).find(".add-bag").css("display", "none");
			$(this).find(".detail").css("display", "none");
		}
	);
	
})



/**
update bag item account
*/
function updateBagCount($ul) {
	var $countHold = $("#my-bag h4 small span");
	var count = $ul.find("li").length;
	$countHold.text(count)
}

function searchBagItem($ul, name) {
	var exists = false;
	$ul.find("li");
	$ul.find("li").each(function(index, element) {
		var $td = $(this).find("table tr td:nth-child(1)");
		if ($td.text() == name) {exists = true;}
	});
	return exists;
}




















































































