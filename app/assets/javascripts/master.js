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
			var $ul = $("#my-bag .accordion .accordion div ul.open");
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
