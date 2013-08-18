$(function() {
	
	var $win = $(window);
	
	$win.resize(function(e) {
		var itemWidth = $("#attractions-list ul li").outerWidth(true);
		//center Attraction List
		var mainWidth = $("#main").width();
		var rowCount = Math.floor(mainWidth / itemWidth);
		var mainWrapWidth = itemWidth*rowCount;
		$("#attractions-list").width(mainWrapWidth);
		
		//sidebar scroll
		var headerHeight = $("#header").height();
		var sidebarPaddingTop = parseInt($("#sidebar").css("padding-top"));
		var sideBarHeight = $win.height() - headerHeight - sidebarPaddingTop;
		if (isNaN(sideBarHeight) || sideBarHeight < 0) sideBarHeight = 0;
		$("#sidebar").height(sideBarHeight);
		
		//sidebar fixed
		updateSideBarPosition($(this));
		
		//planning page
		var topBottomSpace = 183;
		var sideHeight = $(this).height() - topBottomSpace;
		$("#attraction-planning .mid .left, #attraction-planning .mid .right").height(sideHeight);
		
		
		var attractionsHeight = $("#attraction-planning .mid .attractions").height();
		if(sideHeight>attractionsHeight) {
			$("#attraction-planning .mid").height(sideHeight);
		} else {
			$("#attraction-planning .mid").height(attractionsHeight);
		}
		
		if($(this).height()>$("#container").height()) {
			$("#shadow-divide").height($(this).height());
		} else {
			$("#shadow-divide").css("height", "100%");
		}
		
	}).trigger("resize");
	
	$win.scroll(function(event) {
		if (event.target.nodeName == "DIV") {
		  // Dump scroll event if the target is a DIV, because that means the event is coming
		  // from a scrollable div and so there's no need to make adjustments to our layout
		  return;
		}
		updateSideBarPosition($(this));
	});
	
	
	//header fix top and sidebar not scroll up
	$("#header").scrollToFixed({zIndex: 99});
	$("#attractions-list h2").scrollToFixed({zIndex: 97, marginTop: $("#header").outerHeight(true)});
	$(".customized-scroll").jScrollPane({autoReinitialise: true, autoReinitialiseDelay: 0, hideFocus: true});
	
	
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
	$("#search-address").tokenInput( "/locationsearch",{ theme: "facebook"});
     
	$("#search-address-wrap #token-input-search-address").focus(function(e) {
		//alert("focus")
		$("#search-address-wrap label").hide();
	}).blur(function(e) {
		var val = $("#search-address").tokenInput("get");
		if(val == "") {
			$("#search-address-wrap label").show();
		}else{
			// 这里是 需要 实际处理 搜索结果的， 考虑采用页面 跳转的方式
			// 将 id  作为 参数传递
			var ids = "";
				function get( i , item) {
					ids += item.id +","
				}
				$.each(val, get) 
				window.location = "/home?ids=" + ids
			 
		}
	});
	$("#search-address-wrap label").click(function(e) {
		$("#search-address-wrap #token-input-search-address").focus();
	});
	
	
	//home page to add to bag, you should change to use backend to add
	$("#attractions-list").on('click',"ul li .add-bag", function(e) {
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
	})
	
	//home page delete from bag, you should change to use backend to delete
	$("#my-bag .accordion .accordion div ul ").on('click',"li table td.del a",function() {
		var $itemDel = $(this).parents("li");
		var $ul = $(this).parents("ul");
		$itemDel.remove();
		updateBagCount($ul);
		
		// DETAIL PAGE 
		if( $("#attraction-detal .user-content .col.col3 span.added").length > 0 ){
			var e = $("#attraction-detal .user-content .col.col3 span.added")
			e.text("加入背包")
			e.removeClass("added")
			e.addClass("add-bag")
		}
		
		return false;
	});
	
	
	//detail page to add to bag, you should change to use backend to add, add you should avoid duplicate items
	$("#attraction-detal .user-content .col.col3 span.add-bag").click(function(e) {
		if($(this).hasClass("added")) {
			return;
		}
		var bagAddress = $("#attractions-list h2 .address").text();//which bag to add
		var $ul = $("#my-bag .accordion .accordion div ul");//ul to add li
		$ul = $($ul[0]);
		var titleText = $("#attraction-detal   h2").text();
		var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td><td class="del"><a href="#"></a></td></tr></table></li>';
		$ul.append(itemHtml);
		updateBagCount($ul);
		$(this).text("已在背包");
		$(this).addClass("added");
		return false;
	});
	
	homeAttractionHover();
	initDetailGallery();
	
	$("#attraction-detal .user-content .col2 .content .switch .more").click(function(e) {
		$(this).parents(".content").find("p span.more").show();
		$(this).hide();
		$(this).next(".less").show();
		$("#shadow-divide").height($(document).height());
	});
	$("#attraction-detal .user-content .col2 .content .switch .less").click(function(e) {
		$(this).parents(".content").find("p span.more").hide();
		$(this).hide();
		$(this).prev(".more").show();
		$("#shadow-divide").height($(document).height());
	});
	
	$("#avatarimg").imgr({ radius:"16px"});
	
})


function homeAttractionHover() {
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
}



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
		alert("href=" + href )
		var $detail = $(this).parents(".gallery").children(".detail");
		$detail.attr("src", href);
		$ul.find("li.active").removeClass("active");
		$(this).parent("li").addClass("active");
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



function updateSideBarPosition($win) {
	var contentMinWidth = parseInt($("#container").css("min-width"));
	var leftPos = $win.width() - contentMinWidth;
	leftPos = leftPos + $win.scrollLeft();
	if (leftPos>0) leftPos=0;
	$("#sidebar").css("right", leftPos + "px");
}
