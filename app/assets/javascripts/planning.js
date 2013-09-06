$(function() {
	
	$(window).resize(function(e) {
		var space = 133;
		var height = $(this).height()-space;
		$("#attraction-planning-wrap").height(height);
		$("#plan-attractions-list-wrap, #plan-timeline").height(height-$("#plan-attractions .header").height());
		if($(this).height()>$("#container").height()) {
			$("#shadow-divide").height($(this).height());
		}
		setPlanAttractionsListPaddingBottom();
	}).trigger("resize");;
	
	
	
	$("#plan-attractions-list ul li .bottom").live('mouseenter', function() {
		var $hoverCover = $(this).next(".hover-cover");
		$hoverCover.show();
		$hoverCover.children(".note-input").hide();
	})
	
	$("#plan-attractions-list ul li").live('mouseleave', function() {
		$(this).children(".hover-cover").hide();
	})
	$("#plan-attractions-list ul li .hover-cover").live('click', function() {$(this).hide();})
	$("#plan-attractions-list ul li .hover-cover .btn.remark").live('click', function() {
		$(this).prev(".note-input").show();
		return false;
	})
	$("#plan-attractions-list ul li .hover-cover .note-input span").live('click', function() {
		$(this).parents(".hover-cover").hide();
	})
	$("#plan-attractions-list ul li .hover-cover .note-input").live('click', function() {
		return false;
	})
	
	var planStartDate = $("#attraction-planning-wrap .start-date .datepicker").datepicker(
		{
			changeYear: true,
			yearRange: "-0:+3",
			changeMonth: true,
			minDate: 0,
			onSelect: function(dateText, inst) {
				var startDate = $(this).datepicker("getDate");
				$("#attraction-planning-wrap .end-date .datepicker").datepicker( "option", "minDate", startDate);
			}
		}
	);
	//$("#attraction-planning-wrap .start-date > .ui-datepicker").prepend("<h4>开始日期</h4>");
	var planEndDate = $("#attraction-planning-wrap .end-date .datepicker").datepicker(
		{
			changeYear: true,
			yearRange: "-0:+3",
			changeMonth: true,
			minDate: 0,
			onSelect: function(dateText, inst) {
				var startDate = $("#attraction-planning-wrap .start-date .datepicker").datepicker("getDate");
				var endDate = $(this).datepicker("getDate");
				if (endDate<startDate) {
					return;
				}
				var durationDay = (endDate.getTime()-startDate.getTime())/(24*60*60*1000);
				var timeLinkHtml = '<div class="date-line clearfix"><img src="images/time-line-mark.png" width="8" height="11" /><a class="no-sign" href="no-assign">尚未安排</a>';
				var startLoopDate = startDate;
				var year = startLoopDate.getFullYear();
				var month = startLoopDate.getMonth()+1;
				var formatMonth = month<10 ? "0"+month : month;
				var date = startLoopDate.getDate();
				var formatDate = date<10 ? "0"+date : date;
				var loopTimeHtml = '<a class="year" href="'+year+'">'+year+'</a><a href="'+year+'/'+formatMonth+'/'+formatDate+'">'+month+'月'+date+'日</a>';
				for(var i=0; i<durationDay; i++) {
					startLoopDate = new Date(startLoopDate.getTime()+(24*60*60*1000));
					if (year != startLoopDate.getFullYear()) {
						year = startLoopDate.getFullYear();
						loopTimeHtml = loopTimeHtml + '<a class="year" href="'+year+'">'+year+'</a>';
					}
					date = startLoopDate.getDate();
					formatDate = date<10 ? "0"+date : date;
					if (month != (startLoopDate.getMonth()+1)) {
						month = startLoopDate.getMonth()+1;
						formatMonth = month<10 ? "0"+month : month;
						loopTimeHtml = loopTimeHtml + '<a href="'+year+'/'+formatMonth+'/'+formatDate+'">'+month+'月'+date+'日</a>';
					} else {
						loopTimeHtml = loopTimeHtml + '<a href="'+year+'/'+formatMonth+'/'+formatDate+'">'+date+'日</a>';
					}
				}
				timeLinkHtml = timeLinkHtml + loopTimeHtml + '</div>';
				$("#plan-date-input p").remove();
				$("#plan-timeline .date-line").remove();
				$("#plan-date-input").after(timeLinkHtml);
				$("#attraction-planning-wrap .date").hide();
				initialTimeLineLink();
			}
		}
	);
	
	$("#plan-date-input .calendar-icon").click(function(e) {
		$("#attraction-planning-wrap .date").toggle();
	});
	
	draggableAttraction();
	
	initialTimeLineLink();
	
	initialPlanNameLabel();
})

function draggableAttraction() {
	$("#plan-attractions-list ul li").draggable(
		{
		 	zIndex:2000,
			helper: 'clone',
			containment: '#main',
			appendTo: 'body',
			cursor: "pointer",
			cursorAt: { top: 0, left: 0 }
		}
	);
}

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
	var paddintBottom = visibleHeight - h3Height - lastUlHeight - 5;
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
					if($droppingUl.prev("hr").length>0) {
						$droppingUl.prev("hr").remove();
					} else if ($droppingUl.next("hr").length>0) {
						$droppingUl.next("hr").remove();
					}
					$droppingUl.remove();
				}
			}
		}
	)
}

function initialPlanNameLabel() {
	var $input = $("#plan-attractions .header .plan-name input");
	if($input.val() != "") {
		$input.next("label").hide();
	}
	$input.focus(function(e) {
		$(this).next("label").hide();
	}).blur(function(e) {
		if($(this).val()=="") {
			$(this).next("label").show();
		}
	});
}

function initialTimeLineLink() {
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
					if($droppingUl.prev("hr").length>0) {
						$droppingUl.prev("hr").remove();
					} else if ($droppingUl.next("hr").length>0) {
						$droppingUl.next("hr").remove();
						highlightTopDate();
					}
					$droppingUl.remove();
				}
				setPlanAttractionsListPaddingBottom();
			}
		}
	);
}



function highlightTopDate() {
	var $topHeader = $("#plan-attractions-list h3:first");
	var className = $topHeader.attr('class');
	var timeLineHref = className.substring(2, 6) + "/" + className.substring(6, 8) + "/" + className.substring(8, 10);
	var $timeLink = $("#plan-timeline .date-line a[href='"+timeLineHref+"']");
	$timeLink.trigger("click");
}
