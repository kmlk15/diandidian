$(function() {
	
	loadDetail(  detailname  );
	 
})

function loadDetail( name ) {
		 
		$.getJSON("/detail/json/" +encodeURI( name ) , null, function(json){
			//  
			
			var addPhotohref = json.addPhotohref ; 
			
			var htmldata = json.gallery ;
			$("div#gallerycontain").html( htmldata )
			
			var galleryContentData = json.galleryContent; 
			
			 
			$("div.detal-text div.user-content").remove();
			$("div.detal-text").prepend( galleryContentData )
			
			 var viewModel = ko.mapping.fromJS( json );
			 ko.applyBindings(viewModel);
			 
			 initDetailGallery();
			 spanlessmore();
			 
			 $("span.upload-photo").magnificPopup(  {
					items:{
						src: addPhotohref, 
					    type:  "iframe"		
					}
				});
			 
			 $("img.ravatarimg"  ).imgr({ radius:"16px"});	
			 if(json.opentimetable == ""  ){
				 $("#opentime").hide();
			 }
		});
		
		
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
	//处理 小图 点击事件 
	//还需要更新相应的 内容部分
	//内容部分的数据从哪里来？
	//
	$ul.find("li a").click(function(e) {
		var href = $(this).attr("href");
		//alert("href=" + href );
		
		$("#attraction-detal .user-content  ").hide();
		var id =  $(this).attr("id");
		//alert(id );
		var cssselect =  "div#content_" + id ;
		//alert(  cssselect ) ;
		$( cssselect ).show();
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

function spanlessmore( ){
	
	$("#attraction-detal .user-content    .col2 .content .switch .more").click(function(e) {
		$(this).parents(".content").find("p span.more").show();
		$(this).hide();
		$(this).next(".less").show();
		$("#shadow-divide").height($(document).height());
	});
	$("#attraction-detal .user-content   .col2 .content .switch .less").click(function(e) {
		$(this).parents(".content").find("p span.more").hide();
		$(this).hide();
		$(this).prev(".more").show();
		$("#shadow-divide").height($(document).height());
	});
	
}