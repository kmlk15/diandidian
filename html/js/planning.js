$(function() {
	var planItemWidth = $("#plan-attractions-list ul li").outerWidth(true);
	
	$(window).resize(function(e) {
		var space = 133;
		var height = $(this).height()-space;
		$("#attraction-planning-wrap").height(height);
		$("#plan-attractions-list-wrap, #plan-timeline").height(height-$("#plan-attractions .header").height());
		
		//center plan-attractions-list
		//var wrapWidth = $("#plan-attractions-list-wrap").width();
		//var rowCount = Math.floor(wrapWidth / planItemWidth);
		//var mainWidth = planItemWidth*rowCount;
		//$("#plan-attractions-list").width(mainWidth);
		
		if($(this).height()>$("#container").height()) {
			$("#shadow-divide").height($(this).height());
		}
		setPlanAttractionsListPaddingBottom();
	}).trigger("resize");;
	
	
	$("#plan-attractions-list ul li .bottom").hover(
		function() {
			var $hoverCover = $(this).next(".hover-cover");
			$hoverCover.show();
			$hoverCover.children(".note-input").hide();
		},
		function() {}
	);
	
	$("#plan-attractions-list ul li").hover(
		function() {
			
		},
		function() {
			$(this).children(".hover-cover").hide();
		}
	);
	$("#plan-attractions-list ul li .hover-cover").click(function(e) {
		$(this).hide();
	});
	$("#plan-attractions-list ul li .hover-cover .btn.remark").click(function(e) {
		$(this).prev(".note-input").show();
	});
	$("#plan-attractions-list ul li .hover-cover .note-input span").click(function(e) {
		$(this).parents(".hover-cover").hide();
	});
	
	var planStartDate = $("#attraction-planning-wrap .start-date .datepicker").datepicker(
		{
			changeYear: true,
			yearRange: "-0:+3",
			changeMonth: true
		}
	);
	//$("#attraction-planning-wrap .start-date > .ui-datepicker").prepend("<h4>开始日期</h4>");
	
	
	var planEndDate = $("#attraction-planning-wrap .end-date .datepicker").datepicker(
		{
			changeYear: true,
			yearRange: "-0:+3",
			changeMonth: true
		}
	);
	//$("#attraction-planning-wrap .end-date > .ui-datepicker").prepend("<h4>结束日期</h4>");
	
	$("#plan-date-input .calendar-icon").click(function(e) {
		$("#attraction-planning-wrap .date").toggle();
	});
	var dropped = false;
	$("#plan-attractions-list ul li").draggable(
		{
		 	zIndex:2000,
			helper: 'clone',
			containment: '#main',
			appendTo: 'body',
			handle: "img",
			cursor: "pointer",
			cursorAt: { top: 0, left: 0 },
			drag: function( event, ui ) {
				 //console.log(ui.position.top);
				 // console.log($("#plan-timeline .date-line a.active").offset().top);
			}
		}
	);
	
	$("#plan-timeline .date-line a:not('.year')").droppable(
		{
		 	hoverClass: 'drop-hover',
			tolerance: "top-left-touch",
			drop: function(event, ui) {
				var $droppingUl = ui.draggable.parent("ul");
				var href = $(this).attr("href");
				var dateValue = href.replace(new RegExp("/", "g") ,"");
				var $targetHeader = $("#plan-attractions-list").find(".t-"+dateValue);
				if ($targetHeader.length>0) {
					var $ul = $targetHeader.next("ul");
					//$ul.append(ui.helper);
					ui.draggable.appendTo($ul);
				} else {
					var dateArr = href.split("/");
					var year = dateArr[0];
					var month = dateArr[1];
					var day = dateArr[2];
					var targetHtml =  '<h3 class="t-'+dateValue+'">'+month+'月'+day+'日</h3><ul class="clearfix"></ul>';
					if($(this).hasClass("no-sign")) {
						targetHtml =  '<h3 class="t-'+dateValue+'">尚未安排</h3><ul class="clearfix"></ul>';
						$("#plan-attractions-list").prepend("<hr />");
						$("#plan-attractions-list").prepend(targetHtml);
						$("#plan-attractions-list ul:first").append(ui.draggable);
						return;
					}
					var beforeTarget = findDropHeaderPos(dateValue);
					if(beforeTarget) {
						beforeTarget.before(targetHtml).before("<hr />");
						beforeTarget.prev().prev().append(ui.draggable);
					} else {
						$("#plan-attractions-list").append("<hr />");
						$("#plan-attractions-list").append(targetHtml);
						$("#plan-attractions-list ul:last").append(ui.draggable);
					}
					addDroppable();
				}
				//remove header if drag to empty
				if ($droppingUl.find("li").length<=0) {
					$droppingUl.prev("h3").remove();
					$droppingUl.prev("hr").remove();
					$droppingUl.remove();
				}
				setPlanAttractionsListPaddingBottom();
			}
		}
	);
	
	var attractionsApi = $("#plan-attractions-list-wrap").data('jsp');
	$("#plan-timeline .date-line a").click(function(e) {
		
		var href = $(this).attr("href");
		href = href.replace(new RegExp("/", "g") ,"");
		var $target = $("#plan-attractions-list").find(".t-"+href);
		if ($target.length>0) {
			var arrowTopPosstion = 3;
			var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
			arrowTopPosstion = arrowTopPosstion + linkHeight * ($(this).index()-1);
			$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
			attractionsApi.scrollToElement($target,true, 1000);
			$("#plan-timeline .date-line a.active").removeClass("active");
			$(this).addClass("active");
			$("#plan-attractions-list h3.active").removeClass("active");
			$target.addClass("active");
		}
		return false;
	});
	
	$("#plan-attractions-list-wrap").bind("jsp-scroll-y",function(event, scrollPositionY, isAtTop, isAtBottom) {
					console.log('Handle jsp-scroll-y',
								'scrollPositionY=', scrollPositionY);
	})
		
})

function findDropHeaderPos(date) {
	var dateValue = parseInt(date);
	var returnObj = null;
	$("#plan-attractions-list h3").each(function(index, element) {
		var headerClass = $(element).attr("class");
		if (headerClass) {
			var classDateValue = parseInt(headerClass.replace("t-", ""));
			if(classDateValue > dateValue) {
				returnObj = $(element);
				return (false);
			}
		}
	});
	return returnObj;
}

function setPlanAttractionsListPaddingBottom() {
	var visibleHeight = $("#plan-attractions-list-wrap").height();
	var h3Height = $("#plan-attractions-list h3").outerHeight(true);
	var lastUlHeight = $("#plan-attractions-list ul:last").outerHeight(true);
	var paddintBottom = visibleHeight - h3Height - lastUlHeight;
	$("#plan-attractions-list").css("padding-bottom", paddintBottom+"px");
}


function addDroppable() {
	$("#plan-attractions-list ul").droppable(
		{
			drop: function(event, ui) {
				var $droppingUl = ui.draggable.parent("ul");
				ui.draggable.appendTo($(this));
				if ($droppingUl.find("li").length<=0) {
					$droppingUl.prev("h3").remove();
					$droppingUl.prev("hr").remove();
					$droppingUl.remove();
				}
			}
		}
	)
}
























































































