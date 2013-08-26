$(function() {
	
	function loadDetail( name ) {
		 
		$.getJSON("/detail/json/" + name , null, function(json){
			//  
			var htmldata = json.gallery ;
			$("div#gallerycontain").html( htmldata )
			
			var galleryContentData = json.galleryContent; 
			$("div.user-content").prepend( galleryContentData )
			 var viewModel = ko.mapping.fromJS( json );
			 ko.applyBindings(viewModel);
			 
			 initDetailGallery();
			 
		});
		
		
	}
	
	loadDetail(  detailname  );
	 
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
	//处理 小图 点击事件 
	//还需要更新相应的 内容部分
	//内容部分的数据从哪里来？
	//
	$ul.find("li a").click(function(e) {
		var href = $(this).attr("href");
		//alert("href=" + href );
		
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
