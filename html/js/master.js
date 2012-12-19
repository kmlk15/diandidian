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
	//center Attraction List
	var itemWidth = $("#attractions-list ul li").outerWidth(true);
	$win.resize(function(e) {
		var mainWidth = $("#main").width();
		var rowCount = Math.floor(mainWidth / itemWidth);
		var mainWrapWidth = itemWidth*rowCount;
		$("#attractions-list").width(mainWrapWidth);
	}).trigger("resize");
	//header fix top and sidebar not scroll up
	$("#header").scrollToFixed({zIndex: 99});
	$("#sidebar").scrollToFixed({zIndex: 98, marginTop: $("#header").outerHeight(true)});
	$("#attractions-list h2").scrollToFixed({zIndex: 97, marginTop: $("#header").outerHeight(true)});
	
	//sidebar accordion
	//initial state
	var $initialOpenHeader = $("#sidebar .accordion .accordion-header:nth-child(1)");
	$initialOpenHeader.addClass("state-active");
	$initialOpenHeader.next(".accordion-content").show();
	$("#sidebar .accordion .accordion-header").click(function(e) {
		var $clickedHeader = $(this);
		var $headerNextContent = $clickedHeader.next(".accordion-content");
		if($headerNextContent.is(":visible")) {
			$clickedHeader.removeClass("state-active");
		} else {
			$clickedHeader.addClass("state-active");
		}
		$headerNextContent.slideToggle(200);
	});
	
	
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
	    
	
	
	$("#attractions-list ul li .add-bag").click(function(e) {
		var bagAddress = $("#attractions-list h2 .address").text();//which bag to add
		var $ul = $("#my-bag .accordion .accordion div ul");//ul to add li
		$ul = $($ul[0]);
		var titleText = $(this).prevAll(".title").find("h3").text();
		var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td><td class="del"><a href="#"></a></td></tr></table></li>';
		$ul.append(itemHtml);
		updateBagCount($ul);
		//searchBagItem($ul,'');
		$(this).next(".added-bag").css("display", "block");
		$(this).css("display", "none");
		return false;
	});
	
	$("#my-bag .accordion .accordion div ul li table td.del a").live('click',function() {
		var $itemDel = $(this).parents("li");
		var $ul = $(this).parents("ul");
		$itemDel.remove();
		updateBagCount($ul);
		return false;
	});
	
	
	$("#attractions-list ul li").hover(
		function(){
			var addressName = $(this).find(".title h3").text();
			var $ul = $("#my-bag .accordion .accordion div ul");
			$ul = $($ul[0]);
			//alert(searchBagItem($ul, addressName));
			if(!searchBagItem($ul, addressName)) {
				$(this).find(".add-bag").css("display", "block");
			} else {
				$(this).find(".added-bag").css("display", "block");
			}
			$(this).find(".detail").css("display", "block");
		},
		function(){
			$(this).find(".add-bag").css("display", "none");
			$(this).find(".added-bag").css("display", "none");
			$(this).find(".detail").css("display", "none");
		}
	);
	
	initDetailGallery();
	
	$("#attraction-detal .user-content .col2 .content .switch .more").click(function(e) {
		$(this).parents(".content").find("p span.more").show();
		$(this).hide();
		$(this).next(".less").show();
	});
	$("#attraction-detal .user-content .col2 .content .switch .less").click(function(e) {
		$(this).parents(".content").find("p span.more").hide();
		$(this).hide();
		$(this).prev(".more").show();
	});
	
})

/**
detail page gallery
*/
function initDetailGallery() {
	var stepWidth = 3;
	var moveTimeout = 1;
	var ulWrapWidth = $("#attraction-detal .thumbnails .ul-wrap").width();
	//preview ul width
	var $ul = $("#attraction-detal .thumbnails ul");
	var ulWidth = 0;
	$ul.find("li").each(function(index, element) {
		ulWidth = ulWidth + $(element).outerWidth(true);
	});
	$ul.width(ulWidth);
	//remove arrow if gallery not enough
	if (ulWidth < ulWrapWidth) {
		$ul.css("position", "static");
		$("#attraction-detal .gallery .thumbnails .arrow").hide();
	}
	
	
	$ul.find("li a").click(function(e) {
		var href = $(this).attr("href");
		var $detail = $(this).parents(".gallery").children(".detail");
		$detail.attr("src", href);
		return false;
	});
	var moveLeftInterval = null;
	var moveRightInterval = null;
	$("#attraction-detal .thumbnails .left-arrow").hover(
		function(){
			moveLeftInterval = setInterval(moveLeft, moveTimeout);
		},
		function(){
			if (moveLeftInterval) {
				clearInterval(moveLeftInterval);
			}
		}
	);
	$("#attraction-detal .thumbnails .right-arrow").hover(
		function() {
			moveRightInterval = setInterval(moveRight,moveTimeout);
		},
		function() {
			if (moveRightInterval) {
				clearInterval(moveRightInterval);
			}
		}
	);
	function moveLeft() {
		var leftWidth = (parseInt($ul.css("left")))*-1;
		
		leftWidth = leftWidth-stepWidth;
		if(leftWidth<0) leftWidth = 0;
		$ul.css("left", "-"+leftWidth+"px");
	}
	function moveRight() {
		var leftWidth = (parseInt($ul.css("left")))*-1;
		leftWidth = leftWidth + stepWidth;
		console.log(leftWidth)
		var maxLeftWidth = ulWidth-ulWrapWidth;
		console.log(maxLeftWidth)
		if (leftWidth > maxLeftWidth) leftWidth = maxLeftWidth;
		$ul.css("left", "-"+leftWidth+"px");
	}
}

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




















































































