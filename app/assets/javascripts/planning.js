$(function() {
	
	/**
	 * 提取 页面上的状态， 再 更新到 后端
	 */
	$("#mytest").click(function() {
		 
		 
		var param =getCurrentStatusnamePlanName();
		 
	   var startDate = 0;
	  if( window.planStartDate != null) {
		  startDate = window.planStartDate.getTime() ;
	   } 
		 
	    var endDate =  0;
	    if( window.planEndDate !=null ){
	    		endDate = window.planEndDate.getTime() ;
	    }
	    
				
	 //alert("startDate=" + startDate) ;
	 // alert("endDate=" + endDate) ;
	 
	 param.startDate  = startDate ;
	 param.endDate = endDate ; 
	 
		var plan = $.map($("#plan-attractions-list h3"), function(val, index) {
			var name = $(val).attr("class")

			var list = $.map($("li", $(val).next()), function(val, i) {
				var id = $(val).attr("id")
				return id;
			});
			var one = {
				"name" : name,
				"list" : list
			}

			return one;
		});

		//alert(JSON.stringify(plan));

		var planStr = JSON.stringify(plan);
		param.data = planStr ;
		
		$.post("/plan/update", param , function(result) {
			//alert(JSON.stringify(result))
			alert( result.success);
		});

		return false;

	});
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
	    var param = getCurrentStatusnamePlanName() 
	    var locationId = $(this).parent().parent().parent().attr("id") ; 
	    	//alert( locationId) ;
	    	var note = $("textarea" ,  $(this).parent( )).val()
	    //	alert( note ) ;
		
	    	param.locationId = locationId ;
	    	param.note = note ;
	    	$.post("/plan/updateNote" , param , function ( result){
	    		alert( result.success  );
	    		$(this).parents(".hover-cover").hide();
	    	});
		
		
	})
	$("#plan-attractions-list ul li .hover-cover .note-input").live('click', function() {
		
		return false;
	})
	
	var planStartDate = $("#attraction-planning-wrap .start-date .datepicker").datepicker(
		{
			changeYear: true,
			yearRange: "-1:+3",
			changeMonth: true,
			//minDate: 0,
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
			yearRange: "-1:+3",
			changeMonth: true,
			//minDate: 0,
			onSelect: function(dateText, inst) {
				var startDate = $("#attraction-planning-wrap .start-date .datepicker").datepicker("getDate");
				var endDate = $(this).datepicker("getDate");
				if (endDate<startDate) {
					return;
				}
				window.planStartDate = startDate;
				window.planEndDate = endDate;
				
				createTimeline(startDate , endDate );
				
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

function createTimeline( startDate , endDate ){
	
	var timeLinkHtml = createTimeLinkHtml(startDate , endDate );

	$("#plan-date-input p").remove();
	$("#plan-timeline .date-line").remove();
	$("#plan-date-input").after(timeLinkHtml);
	$("#attraction-planning-wrap .date").hide();
	initialTimeLineLink();
	
}

function createTimeLinkHtml(startDate , endDate ){
	
	var durationDay = (endDate.getTime()-startDate.getTime())/(24*60*60*1000);
	var timeLinkHtml = '<div class="date-line clearfix"><img src="/assets/images/time-line-mark-blue.png" width="8" height="11" />' ;
		timeLinkHtml +=	'<a class="all" href="00_all">全部显示 </a>';
		timeLinkHtml +=	'<a class="no-sign" href="00_no-assign">尚未安排</a>';
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
	return timeLinkHtml ;
}

function createMapTimeline( startDate , endDate ){
	
	var timeLinkHtml = createMapTimeLinkHtml(startDate , endDate );

	 
	 
	$("#infowindowtimeline").html(timeLinkHtml);
	 
	
	initialMapTimeLineLink();
	
}


function createMapTimeLinkHtml(startDate , endDate ){
	
	var durationDay = (endDate.getTime()-startDate.getTime())/(24*60*60*1000);
	var timeLinkHtml = '<div class="map-date-line clearfix">' ;
		timeLinkHtml +=	'<a class="no-sign" href="00_no-assign">尚未安排</a>';
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
	return timeLinkHtml ;
}


function initialMapTimeLineLink() {
	 
	$("#infowindowtimeline").on("click" , ".map-date-line a" , function(e) {
		$("#infowindowtimeline").hide();
		var href = $(this).attr("href");
		href = href.replace(new RegExp("/", "g") ,"");
		if( window.gmapon){
			var locationId = window.currentLocationid ;
			var newkey = "t-"+href ;
			var oldkey = location2timeMap[locationId] ;
			location2timeMap[locationId] = newkey ;
			console.log("locationId=" + locationId);
			
			 console.log("gmapon=" + window.gmapon );
			 console.log("key=" + "t-"+href );
			 //console.log( timelineMap["t-"+href]  ) ;
			 //从 原来的 object 中删除
			 // 加入到新的 object 
			 console.log("原来的位置 =" + oldkey  ) ;
			 console.log("新的位置 =" + newkey  ) ;
			 
			  for(index in markerMap) {
				  var   marker = markerMap[index]; 
				  marker.setMap (  null  ) ; 
			  } 
			  
			 $.each(location2timeMap , function( index,value){
				 console.log( "index -> value=" + index + '->' +  value  ) ;
				 if( value == newkey){
					 var   marker = markerMap[index]; 
					 console.log("show marker ") ;
					 if( marker != null ){
						 marker.setMap (   map  ) ; 
					 }else{
						 console.log("这个地点已经被删除了 id=" + value);
					 }
				 }
			 });
				 
			
			return false ;
		}
		var $target = $("#plan-attractions-list").find(".t-"+href);
		if ($target.length>0) {
			var arrowTopPosstion = 3;
			var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
			arrowTopPosstion = arrowTopPosstion + linkHeight * ($(this).index()-1);
			$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
			//attractionsApi.scrollToElement($target,true, 1000);
			$("#plan-timeline .date-line a.active").removeClass("active");
			$(this).addClass("active");
			$("#plan-attractions-list h3.active").removeClass("active");
			$target.addClass("active");
		}
		return false;
	});
	
}


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
				//alert("drop , event = " + event ) ;
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
	
	var $input = $("#plan-attractions .header .plan-name input[name=planName]");
	var $label = $input.next();
	var $submitcmd = $label.next();
    var $cancelcmd = $submitcmd.next();
    
	if($input.val() != "") {
		$label.hide();
	}
	var  planname = $input.val() ;
	
	$input.focus(function(e) {
		planname = $(this).val() ;
		$label.hide();
		$submitcmd.show();
		$cancelcmd.show();
	}).blur(function(e) {
		
	});
	
	$submitcmd.click( function(e){
		var currentParam  = getCurrentStatusnamePlanName();
		var newname = $input.val() ;
		console.log("currentParam=" + currentParam.planName ) ;
		console.log( "newname=" + newname );
		if(newname == "" ){
			$submitcmd.hide();
			$cancelcmd.hide();
			$input.val( currentParam.planName );
			if( planname == ""){
				$label.show();
			}
			return ;
		}
		
		if( newname == currentParam.planName){
			$submitcmd.hide();
			$cancelcmd.hide();
			$input.val( planname );
			
			if( planname == ""){
				$label.show();
			}
			return;
		}else{
			
			var existFlag = false ; 
			//如果 要修改的名字已经存在，则不允许修改
			$("a.setPlan[href*=planName]").each( function( index) { 
				 
				var url = $.url(  $(this).attr("href") );
				var param = url.param() ;
				//console.log( JSON.stringify (  param )) ;
				if( param.statusName == currentParam.statusName && param.planName ==  newname ){
					existFlag = true; 
					
				}
			});
			if( existFlag ){
				alert( "存在同名的背包，" + newname );
				return ; 
			}
			
			//修改 背包中的 链接
			//找到 所有的链接
			//找出 statusName =  , planName =   的 链接
			//进行相应的更新
			
			 
			

			
	 		var q = {"fromStatus": currentParam.statusName , "fromPlan":currentParam.planName,
	 				"toStatus": currentParam.statusName , "toPlan":newname, "cmd":"update" }
	 		
	 		$.getJSON("/bag/updateJson" ,q , function (result){
	 			if( result.success ){
	 				
//	 				console.log( $("a[href*=planName]").size()) ; 
//	 				$("a[href*=planName]").each( function( index) { 
//	 					 
//	 					var url = $.url(  $(this).attr("href") );
//	 					var param = url.param() ;
//	 					console.log( JSON.stringify (  param )) ;
//	 					if( param.statusName == currentParam.statusName && param.planName ==currentParam.planName ){
//	 						//需要更新
//	 						param.planName = newname;
//	 						var newquery = "?"+$.param( param) ;
//	 						console.log("newquery=" + newquery );
//	 						$(this).attr("href" , newquery)
//	 						
//	 						if( $(this).attr("class") == "setPlan"){
//	 							console.log(" replace name ");
//	 							var htmlstr = $(this).html();
//	 							htmlstr = htmlstr.replace(  currentParam.planName   ,  newname )
//	 							$(this).html( htmlstr ) ;
//	 						}
//	 					}
//	 				});
//	 				window.currentPlanName = newname ; 
	 				//如果不 reload当前页面，则下次 reload 就会出现   plan 找不到
	 				var url = $.url(  );
 					var param = url.param() ;
 					param.planName = newname;
 					var newquery = "?"+$.param( param) ;
 					window.location = "/plan/" + newquery ;
 					
 					
	 			}else{
	 				alert(result.msg ) ; 
	 			}
	 			
	 		});
			
			
			
			$submitcmd.hide();
			$cancelcmd.hide();
		}
	});
	
	$cancelcmd.click( function (e){
		$submitcmd.hide();
		$cancelcmd.hide();
		$input.val( planname );
		if( planname == ""){
			$label.show();
		}
	});
	
}

function initialTimeLineLink() {
	var attractionsApi = $("#plan-attractions-list-wrap").data('jsp');
	$("#plan-timeline .date-line a").click(function(e) {
		var href = $(this).attr("href");
	 
		href = href.replace(new RegExp("/", "g") ,"");
		 
		
		 
		if( window.gmapon){
			 console.log("gmapon=" + window.gmapon );
			 console.log("key=" + "t-"+href );
			 //console.log( timelineMap["t-"+href]  ) ;
			 if( href =="00_all"){
				 for(index in markerMap) { 
					 var   marker = markerMap[index]; 
					  marker.setMap (   map  ) ; 
				 }
			 }else{
				
			  for(index in markerMap) {
				  var   marker = markerMap[index]; 
				  marker.setMap (  null  ) ; 
			  } 
				 
				 var key = "t-"+href   ;
				 
				 $.each(location2timeMap , function( index,value){
					 console.log( "index -> value=" + index + '->' +  value  ) ;
					 if( value == key){
						 var   marker = markerMap[index]; 
						 console.log("show marker ") ;
						 if( marker != null ){
							 marker.setMap (   map  ) ; 
						 }else{
							 console.log("这个地点已经被删除了 id=" + value);
						 }
					 }
				 });
				 
			 }
			   
			   
			  
			  
			  
			return false ;
		}
		var $target = $("#plan-attractions-list").find(".t-"+href);
		if ($target.length>0) {
			var arrowTopPosstion = 3;
			var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
			arrowTopPosstion = arrowTopPosstion + linkHeight * ($(this).index()-1);
			$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
			//attractionsApi.scrollToElement($target,true, 1000);
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
						targetHtml =  '<h3 class="t-'+dateValue+'">尚未安排</h3><ul class="clearfix ui-droppable"></ul>';
						$("#plan-attractions-list").prepend("<hr />");
						$("#plan-attractions-list").prepend(targetHtml);
						$("#plan-attractions-list ul:first").append(ui.draggable);
						// why return ? 2013-09-07
						addDroppable();
						//删除空白的 ul 
						
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

//del 
$(function(){
	
	$("div#plan-attractions-list").on('click',"    a",function() {
		
		var urla =  $.url( $(this).attr("href"));
		
		var parama = urla.param()
		var locationId = parama.locationId ;
		console.log("locationId=" + locationId  );
		
		var atag = $("ul.open li.li_" +locationId +" table  td.del a" );
		
		console.log("atag.html=" + atag.html() );
		
		//return false ;
		var url = $.url( $(atag).attr("href") );
		var param = url.param()
		console.log( JSON.stringify (param ) ) ;
		//return false;
		var locationId = param.locationId ;
		 $.getJSON("/bag/del", param,  function (result){
			 if( result.success){
				 var  dellocationName = $(atag).parents("td").prev().text();

				 var $itemDel = $(atag).parents("li");
					var $ul = $(atag).parents("ul");
					$itemDel.remove();
					updateBagCount($ul);
					//删除  plan  中的
					$li = $("li#" + locationId  ) 
					
					var $droppingUl = $li.parent("ul");
					$li. remove() ;
					//删除空白的 内容
					
					
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
					//删除地图中的数据
					if(typeof markerMap === 'undefined'  ) {
					}else{
					console.log(markerMap );
					console.log( locationId);
					console.log(markerMap[locationId] );
					if(markerMap !=null && markerMap[locationId] != undefined ){
						markerMap[locationId].setMap( null );
						delete markerMap[locationId] ;
						//还需要从已经分配的的日期中删除？？ 
					}
					}
			 }else{
				 alert( result.msg)
			 }
		 })
		return false;
	});
	
	
});


function highlightTopDate() {
	var $topHeader = $("#plan-attractions-list h3:first");
	var className = $topHeader.attr('class');
	var timeLineHref = className.substring(2, 6) + "/" + className.substring(6, 8) + "/" + className.substring(8, 10);
	var $timeLink = $("#plan-timeline .date-line a[href='"+timeLineHref+"']");
	$timeLink.trigger("click");
}

function getCurrentStatusnamePlanName( ) {
	var url = $.url(  );
	var param = url.param() ;
	
	if( window.currentPlanName !=  null ){
		param.planName = window.currentPlanName ; 
	}
	console.log("param.planName=" + param.planName  ) ;
	return   param ; 
}
