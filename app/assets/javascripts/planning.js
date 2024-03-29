var dateColor = Array();
dateColor[0] ="CD5C5C" ;
dateColor[1] ="9370DB" ;
dateColor[2] ="D2B48C" ;
dateColor[3] ="00FFFF" ;
dateColor[4] ="ADFF2F" ;
dateColor[5] ="6495ED" ;
dateColor[6] ="CD5C5C" ;
dateColor[7] ="AFEEEE" ;
dateColor[8] ="00FF7F" ;
dateColor[9] ="98FB98" ;
dateColor[10] ="B0C4DE" ;
dateColor[11] ="CD853F" ;
dateColor[12] ="D2691E" ;
dateColor[13] ="FF1493" ;
dateColor[14] ="32CD32" ;
dateColor[15] ="BDB76B" ;
dateColor[16] ="6B8E23" ;
dateColor[17] ="ADD8E6" ;
dateColor[18] ="FFDAB9" ;
dateColor[19] ="8FBC8F" ;
dateColor[20] ="87CEFA" ;
dateColor[21] ="F0E68C" ;
dateColor[22] ="00FA9A" ;
dateColor[23] ="00FF00" ;
dateColor[24] ="FFD700" ;
dateColor[25] ="FFDEAD" ;
dateColor[26] ="90EE90" ;
dateColor[27] ="87CEEB" ;
dateColor[28] ="FFE4B5" ;
dateColor[29] ="D8BFD8" ;
dateColor[30] ="FFE4E1" ;
dateColor[31] ="B0E0E6" ;


function getIconUrl( date ){
	console.log("date=" + date  ) ;
	if(date){
		var color = dateColor[ parseInt(date) ] ; 
		var iconurl = 'http://chart.googleapis.com/chart?chst=d_map_spin&chld=0.70|0|'+color+'|13|b|' + date ;
	}else{
		var iconurl = 'http://chart.googleapis.com/chart?chst=d_map_spin&chld=0.70|0|'+ dateColor[0] +'|13|b|'  ;
	}
	return iconurl;
}

function getIconUrlOnlyColor( date  ){
	if(date){
		var color = dateColor[parseInt( date) ] ; 
		var iconurl = 'http://chart.googleapis.com/chart?chst=d_map_spin&chld=0.70|0|'+color+'|13|b|'  ;
	}else{
		var iconurl = 'http://chart.googleapis.com/chart?chst=d_map_spin&chld=0.70|0|'+ dateColor[0] +'|13|b|'  ;
	}
	return iconurl;
}



function getDateOfMonth( timeline){
	console.log("line 60 \t timeline=" + timeline ) ;
	var result  = 0; 
	
	if( timeline == "t-00_no-assign"){
		result =0 ;
	}else{
		var dateregexp = new RegExp("t-(\\d{4})(\\d{2})(\\d{2})") ;
		var match =  dateregexp.exec(timeline);
		if( match){
			result = match[3];
		}else{	
			result = 0 ;
		}
	}
	return result ; 
}

$(function() {
	
	/**
	 * 提取 页面上的状态， 再 更新到 后端
	 */
	$("#plansave").click(function() {
		var param =getCurrentStatusnamePlanName();
	   var startDate = 0;
	  if( window.planStartDate != null) {
		  startDate = window.planStartDate.getTime() ;
	   } 
	    var endDate =  0;
	    if( window.planEndDate !=null ){
	    		endDate = window.planEndDate.getTime() ;
	    }
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

		//console.log(JSON.stringify(plan));

		var planStr = JSON.stringify(plan);
		param.data = planStr ;
		
		$.post("/plan/update", param , function(result) {
			//console.log(JSON.stringify(result))
			console.log( result.success);
		});
		return false;
	});
	
	$("#planpublic").click(function() {
		var param =getCurrentStatusnamePlanName();
		param.visible="public";
		$.getJSON("/plan/updateVisible", param, function(result){
			console.log( result.success);
			 
		});
	});
	
	$("#planprivate").click(function() {
		var param =getCurrentStatusnamePlanName();
		param.visible="private";
		$.getJSON("/plan/updateVisible", param, function(result){
			console.log( result.success);
			 
		});
	});
	
//	$("#plansavepdf").click( function(){
//		var param = getCurrentStatusnamePlanName();
//		$.getJSON("/plan/outpdf", param, function(result){
//			console.log( result );
//			if( result.success){
//				var href="/plan/sendfile/" + result.data.randomstr +"/" + encodeURI( result.data.planName) +".pdf";
//				console.log("href=" + href ) ;
//				$("#pdfdownload").attr("href", href)
//				
//				$("#pdfdownload")[0].click();
//				
//			}else{
//				alert( result.msg );
//			}
//		});
//	});
	
	
	$(window).resize(function(e) {
		var space = 210;
		var height = $(this).height()-space;
		$("#attraction-planning-wrap").height(height);
		$("#plan-attractions-list-wrap, #plan-timeline").height(height-$("#plan-attractions .header").height());
		if($(this).height()>$("#container").height()) {
			$("#shadow-divide").height($(this).height());
		}
		setPlanAttractionsListPaddingBottom();
	}).trigger("resize");;
	
	
	
	$("#plan-attractions-list ").on('mouseenter', "ul li .bottom", function() {
		 
		var $hoverCover = $(this).next(".hover-cover");
		$hoverCover.show();
		$hoverCover.children(".note-input").hide();
	})
	
	$("#plan-attractions-list ").on('mouseleave', "ul li", function() {
		$(this).children(".hover-cover").hide();
	})
	
	$("#plan-attractions-list").on('click', " ul li .hover-cover" ,  function() {$(this).hide();})
	
//	$("#plan-attractions-list ").on('click', "ul li .hover-cover .btn.remark" ,  function() {
//		var locationId = $(this).parent().parent().attr("id") ; 
//		console.log("locationId=" +  locationId) ;
//		 $("div#note-input  input[name=locationId]").val( locationId  ) 
//		 $("div#note-input  textarea[name=note]").val(   $("textarea#note_" +locationId ) .val( )   ) 
//		$("div#note-input").show();
//		return false;
//	})
	
	$("#plan-attractions-list ").on('click', "h3" ,  function() {
		var param = getCurrentStatusnamePlanName() 
		var datestr = $(this).next().attr("id") ;
		console.log( datestr );
		 if( "t-00_no-assign" ==  datestr ){
			 return false;
		 }
		 $("div#note-input  input[name=datestr]").val( datestr  ) 
		 console.log(noteArr );
		 var note="";
		 if(  noteArr[ datestr ]  ){
			  note = noteArr[ datestr ]
		 }else{
			 noteArr[ datestr ] = "" ;
		 }
		 $("div#note-input  textarea[name=note]").val(    note   ) 
		 
		 
		 $("div#note-input").modal( {"closeClass": "close"  , "overlayCss":{ "background-color": "#000000"}  , "opacity":75 });
		 
		return false;
	})
	
 
	
	$("div#note-input span.savebtn  ").click( function() {
	    var param = getCurrentStatusnamePlanName() 
	    var datestr = $("div#note-input  input[name=datestr]").val(   ) 
	    console.log("datestr=" +  datestr) ;
	    	var note = $("textarea" ,  $(this).parent( )).val();
	    	
	    	noteArr[ datestr ]  = note ;
	    	 
	   console.log( note ) ;
		
	    	param.datestr = datestr ;
	    	param.note = note ;
	    
	    	$.post("/plan/updateNote" , param , function ( result){
	    		console.log( result.success  );
	    		$("div#note-input").hide();
	    		$.modal.close() ;
	    	});
	    
		
	});
	
 
	
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
				createMapTimeline(startDate , endDate );
				
				//TODO ， 数据更新到 后端
				var param = getCurrentStatusnamePlanName();
				param.startDate = startDate.getTime ();
				param.endDate  = endDate.getTime ()  ; 
				$.getJSON("/plan/updateDate" , param, function( result){
					if( result.success){
						
					}else{
						console.log("update ERROR");
					}
					
				});
				
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
	
	timelineimgPosition();
	
	
}

function timelineimgPosition(){
	
	//定位到和   右边的 第一条一致
	if( $("#plan-attractions-list h3").length == 0 ) {
		return false ;
	}
	var cssName = $("#plan-attractions-list h3.active , #plan-attractions-list h3:first").first().attr("class").replace(/\s*active\s*/g ,"");
	
	cssName = jQuery.trim( cssName);
	console.log("cssName=" + cssName  +"xxxx");
	if( cssName == "t-00_no-assign"){
		$( "#plan-timeline a[href=00_no-assign]").addClass("active");
	}else{
		var dateregexp = /t-(\d{4})(\d{2})(\d{2})/g ;
		var match =  dateregexp.exec(cssName);
		var href = match[1]+"/" + match[2] +"/" + match[3];
		console.log( "href=" + href );
		var  positionindex = 0 ;
		$( "#plan-timeline a").filter( function(  index){ 
			if( $(this).attr("href")== href){
				positionindex = index ; 
				return true;
			}else{
				return false;
			}
		}).addClass("active");
		console.log( "positionindex=" + positionindex );
		
		var arrowTopPosstion = 3;
		var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
		arrowTopPosstion = arrowTopPosstion + linkHeight * (positionindex -1 );
		$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
		
	}
	
	
}


function createTimeLinkHtml(startDate , endDate ){
	
	var durationDay = (endDate.getTime()-startDate.getTime())/(24*60*60*1000);
	var timeLinkHtml = '<div class="date-line clearfix"><img src="/assets/images/time-line-mark-blue.png" width="8" height="11" />' ;
	if( window.gmapon )	{
		timeLinkHtml +=	'<a class="all" href="00_all" style="display:block">全部显示 </a>';
	}else{
		timeLinkHtml +=	'<a class="all" href="00_all" style="display:none">全部显示 </a>';
	}
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

	console.log( "timeLinkHtml=" + timeLinkHtml ) ;
	 
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
	var lastdaycss="";
	for(var i=0; i<durationDay; i++) {
		if( i == durationDay -1 ){
			lastdaycss="lastday" ;
		}
		console.log("i=" + i +"\tlastdaycss="+ lastdaycss );
		
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
			loopTimeHtml = loopTimeHtml + '<a href="'+year+'/'+formatMonth+'/'+formatDate+'" class="'+ lastdaycss +'">'+month+'月'+date+'日</a>';
		} else {
			loopTimeHtml = loopTimeHtml + '<a href="'+year+'/'+formatMonth+'/'+formatDate+'" class="'+ lastdaycss +'">'+date+'日</a>';
		}
	}
	timeLinkHtml = timeLinkHtml + loopTimeHtml + '</div>';
	return timeLinkHtml ;
}


function initialMapTimeLineLink() {
	 
	$("#infowindowtimeline").on("click" , ".map-date-line a" , function(e) {
		if( $(this).hasClass("year")){
			return false;
		}
		var atag = this ; 
		var href = $(this).attr("href");
       console.log("href="+ href);
       
		href = href.replace("http://www.diandidian.com/plan/",""); //IE7
		var dateArr = href.split("/");
		 
		var year = dateArr[0];
		var month = dateArr[1];
		var day = dateArr[2];
		$("#infowindowtimeline").hide();
		 
		var week = getWeek(year ,  month , day ) ;
		
		href = href.replace(new RegExp("/", "g") ,"");
		if( window.gmapon){
			var locationId = window.currentLocationid ;
			var newkey = "t-"+href ;
			console.log("newkey=" + newkey ) ;
			var oldkey = locationMap[locationId].timeline ;
			if( oldkey == newkey ){
				console.log(" oldkey == newkey " + (oldkey == newkey) );
				return false ;
			}
			
			locationMap[locationId].timeline = newkey ;
			
			var param = getCurrentStatusnamePlanName()
			param.date = newkey ; 
			param.locationId = locationId ; 
			console.log("param=" +  JSON.stringify(param))
			
			$.getJSON("/bag/locationSignDate" , param , function( result){
				
				if(  result.success ){
					console.log(" /bag/locationSignDate ok ");		
				}else{
					consloe.log("locationSignDate ERROR");
					return  false ;
				}
				
			});
			
			locationMap[locationId].content.date = month+"月" + day + "日" ;
			if( href == "00_no-assign"){
				locationMap[locationId].content.date = "尚未安排" ;
			}
			
			var content = locationMap[locationId].content.contentString();
			//更新 内容
			infowindow.setContent( content ) ;
			console.log("locationId=" + locationId);
  		    console.log("key=" + "t-"+href );
  		    
  		    //如果当前不是全部显示
  		    //则 hide 当前 mark
  		    
  		  if( $("#plan-timeline .date-line a[href=00_all]").hasClass("active") ){  
  		  }else{
  			 if(infowindow){ infowindow.close( ) ; $("#infowindowtimeline").hide() ; }
  			  var   marker = markerMap[locationId];
  			  marker.setMap (  null  ) ; 
  			 
  		  }
//  		    //全部关闭
//			  for(index in markerMap) {
//				  var   marker = markerMap[index]; 
//				  marker.setMap (  null  ) ; 
//			  } 
//			  if(infowindow){ infowindow.close( ) ; $("#infowindowtimeline").hide() ; }
//			  
//			 //只显示满足要求的 地点 
//			 $.each(locationMap , function( index,value){
//				 console.log( "index -> value=" + index + '->' +  value  ) ;
//				 if( value.timeline == newkey){
//					 var   marker = markerMap[index]; 
//					 console.log("show marker ") ;
//					 if( marker != null ){
//						 marker.setMap (   map  ) ; 
//					 }else{
//						 console.log("这个地点已经被删除了 id=" + value);
//					 }
//				 }
//			 });
  		    /**
  		     * 只是 改边 图片的 颜色
  		     * 需要找到 当前的图标
  		     * 并改变颜色
  		     * 这里的问题是， 当页面再次 刷新的时候， 需要 重置 图标的 颜色
  		     */
  		    var   marker = markerMap[locationId];
  		    var iconurl = getIconUrl ( day ) ; 
  		    console.log( "icon url= " + iconurl)
  		    marker.setIcon( iconurl ) 
  		    
  		    
			 //移动 li
			 console.log(" move  li "); 
			 var $targetHeader = $("#plan-attractions-list").find("h3."+ newkey);
				if ($targetHeader.length>0) {
					 $("li#" + locationId ).appendTo($( "ul#"+ newkey));
				} else {
					var dateValue = "" + year + month + day ;
					var targetHtml =  '<h3 class="'+newkey+'">'+month+'月'+day+'日 - ' +week+' </h3><ul class="clearfix" id="'+newkey+'" ></ul>';
					if($(this).hasClass("no-sign")) {
						targetHtml =  '<h3 class="'+newkey+'">尚未安排</h3><ul class="clearfix ui-droppable" id="'+newkey+'" ></ul>';
						$("#plan-attractions-list").prepend("<hr />");
						$("#plan-attractions-list").prepend(targetHtml);
						$("#plan-attractions-list ul:first").append( $("li#" + locationId ) );
					}else{
						var beforeTarget = findDropHeaderPos(dateValue);
						if(beforeTarget) {
							beforeTarget.before(targetHtml).before("<hr />");
							beforeTarget.prev().prev().append( $("li#" + locationId ) );
						} else {
							$("#plan-attractions-list").append("<hr />");
							$("#plan-attractions-list").append(targetHtml);
							$("#plan-attractions-list ul:last").append( $("li#" + locationId ) );
						}
						
					}
				}
				console.log(" addDroppable ");
				addDroppable();
				//remove header if drag to empty
				var $droppingUl = $("ul#"+ oldkey);
				
				if ($droppingUl.find("li").length<=0) {
					$droppingUl.prev("h3").remove();
					if($droppingUl.prev("hr").length>0) {
						$droppingUl.prev("hr").remove();
					} else if ($droppingUl.next("hr").length>0) {
						$droppingUl.next("hr").remove();
						//highlightTopDate();
					}
					$droppingUl.remove();
				}
				 
			
				setPlanAttractionsListPaddingBottom();
				
			 
			
			 //如果 目标 ul 不存在， 构造 目标ul
			 /**
			  * Edit-11-06-13.pdf 
			  * 日期的  保持不变 
			  */
			 //移动时间线图标
//				console.log(" move timeline ") ;
//				var arrowTopPosstion = 3;
//				var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
//				console.log("$(this).index()=" + $(this).index() ) ;
//				
//				arrowTopPosstion = arrowTopPosstion + linkHeight * ($(this).index() +1);
//				
//				$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
//				//attractionsApi.scrollToElement($target,true, 1000);
//				$("#plan-timeline .date-line a.active").removeClass("active");
//				$("#plan-timeline .date-line a").each( function( index,element){
//					console.log( $(element).attr("href"));
//					if( $(element).attr("href") == $(atag).attr("href") ){
//						$(element).addClass("active");
//					}
//				});
				
				 
			 
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
	if( gmapon ){
		var tag = $("div#map-canvas") ; 
		var w = $( "div#plan-attractions").width() - $("div#plan-timeline-wrap").width() - 11 ;
		var h = $( "div#plan-attractions").height() -  60 ;
		tag.width( w );
		tag.height( h );
	}
}


function addDroppable() {
	$("#plan-attractions-list ul").droppable(
		{
			drop: function(event, ui) {
				console.log("drop , event = " + event ) ;
				
				
				var $droppingUl = ui.draggable.parent("ul");
				ui.draggable.appendTo($(this));
				
				//同步更新 locationMap 信息
				var locationId = $(ui.draggable).attr("id");
				var timeline = $(this).attr("id");
				console.log( "locationId=" + locationId + "\ttimeline=" + timeline);
				
				var param = getCurrentStatusnamePlanName()
				param.date = timeline ; 
				param.locationId = locationId ; 
				console.log(  JSON.stringify(param))
				
				$.getJSON("/bag/locationSignDate" , param , function( result){
					
					if(  result.success ){
						
					}else{
						consloe.log("locationSignDate ERROR");
						return  false ;
					}
					
				});
				
				if(  locationMap[locationId] ) {
					locationMap[locationId].timeline = timeline  ;
					locationMap[locationId].content.date = getDateStr( timeline ) ;
				}
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
		$(this).addClass("focus");
		//$submitcmd.show();
		//$cancelcmd.show();
	}).blur(function(e) {
		$(this).removeClass("focus");
		$input.val( planname );
		if( planname == ""){
			$label.show();
		}
	});
	$input.keyup( function(e){
		//console.log("e.which=" + e.which ); 
		if (   e.which === 27) {
		    e.preventDefault();
		    $(this).removeClass("focus");
			$input.val( planname );
			if( planname == ""){
				$label.show();
			}
		    
		}else{
			return  true ;
		}
	});
	
	$input.keypress( function(e){
		console.log("e.which=" + e.which ); 
		if ( e.which === 13  || e.which === 27) {
		    e.preventDefault();
		}else{
			return  true ;
		}
		
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
	//jquery.jscrollpane.js
	var attractionsApi = $("#plan-attractions-list-wrap").data('jsp');
	console.log( "attractionsApi=" +  JSON.stringify (attractionsApi ) ) ;
	if(typeof attractionsApi === 'undefined'  ){
		//暂时注释，CPU
	$(".customized-scroll").jScrollPane({autoReinitialise: true, autoReinitialiseDelay: 0, hideFocus: true , verticalGutter:0 });
		
		attractionsApi = $("#plan-attractions-list-wrap").data('jsp');
		console.log( "attractionsApi=" +  JSON.stringify (attractionsApi ) ) ;
	}
	
	
	$("#plan-timeline .date-line a").click(function(e) {
		var href = $(this).attr("href");
		href = href.replace("http://www.diandidian.com/plan/",""); 
		href = href.replace(new RegExp("/", "g") ,"");

		if( window.gmapon){
			 console.log("gmapon=" + window.gmapon );
			 console.log("key=" + "t-"+href );
			 
			 if(infowindow){ infowindow.close( ) ; $("#infowindowtimeline").hide();}
			 
 
			 
			
			var $target = $("#plan-attractions-list").find(".t-"+href);
			if ($target.length>0 ) {
				//移动图标
				$("#date-line-tooltip").hide( );
				var arrowTopPosstion = 3;
				var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
				console.log("$(this).index()=" + $(this).index() ) ;
				var index = $(this).index() ;
				 	
				 
				
				arrowTopPosstion = arrowTopPosstion + linkHeight * (index-1);
				$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
				
				attractionsApi.scrollToElement($target,true, 1000);
				$("#plan-timeline .date-line a.active").removeClass("active");
				$(this).addClass("active");
				
				
				
				 for(index in markerMap) {
					  var   marker = markerMap[index]; 
					  marker.setMap (  null  ) ; 
					   
				  }
				//保持 图标 带有数字
					for(index in locationMap) {
						var  locatoindata = locationMap[index] ;
						var   marker = markerMap[index]; 
						var day = getDateOfMonth(  locatoindata.timeline ) ;
						marker.setIcon( getIconUrlOnlyColor( day ));
					}
					
					 var key = "t-"+href   ;
					 $.each(locationMap , function( index,value){
						 console.log( "index -> value=" + index + '->' +  value  ) ;
						 if( value.timeline == key){
							 var   marker = markerMap[index]; 
							 console.log("show marker ") ;
							 if( marker != null ){
								 marker.setMap (   map  ) ; 
								 
							 }else{
								 console.log("这个地点已经被删除了 id=" + value);
							 }
						 }
					 });
					 
				
			}else{
				if( $(this).hasClass("year")){
					
				}else if( href =="00_all" ){
					
					$("#date-line-tooltip").hide( );
					var arrowTopPosstion = 3;
					var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
					console.log("$(this).index()=" + $(this).index() ) ;
					var index = $(this).index() ;
					 
					arrowTopPosstion = arrowTopPosstion + linkHeight * ( index -1);
					$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
					
					$("#plan-timeline .date-line a.active").removeClass("active");
					$(this).addClass("active");
					
					//保持 图标 带有数字
					for(index in locationMap) {
						var  locatoindata = locationMap[index] ;
						var   marker = markerMap[index]; 
						var day = getDateOfMonth(  locatoindata.timeline ) ;
						marker.setIcon( getIconUrl( day ));
					}
					
					 for(index in markerMap) { 
						 var   marker = markerMap[index]; 
						  marker.setMap (   map  ) ; 
						  
					 }
					 
				}else{
					var offset = $(this).offset();
					offset.position="absolute";
					offset.left = offset.left  + 90;
					offset.top = offset.top  - 5 ;
					console.log( offset  ) ;
					
					$("#date-line-tooltip").css( offset );
					$("#date-line-tooltip").show( ).delay(  2000 ).hide( "slow" );
				}	
				 
			}
			
			return false ;
		}else{
			$("#date-line-tooltip").hide( );
			var $target = $("#plan-attractions-list").find(".t-"+href);
			if ($target.length>0) {
				var arrowTopPosstion = 3;
				var linkHeight = $("#plan-timeline .date-line a").outerHeight(true)+0;
				console.log("$(this).index()=" + $(this).index() ) ;
				 
				arrowTopPosstion = arrowTopPosstion + linkHeight * ($(this).index()-2);
				$("#plan-timeline .date-line > img").animate({top: arrowTopPosstion+"px"},1000);
				attractionsApi.scrollToElement($target,true, 1000);
				$("#plan-timeline .date-line a.active").removeClass("active");
				$(this).addClass("active");
				$("#plan-attractions-list h3.active").removeClass("active");
				$target.addClass("active");
			}else{
				if( $(this).hasClass("year")){
					
				}else{
					var offset = $(this).offset();
					offset.position="absolute";
					offset.left = offset.left  + 90;
					offset.top = offset.top  - 5 ;
					console.log( offset  ) ;
					
					$("#date-line-tooltip").css( offset );
					$("#date-line-tooltip").show( ).delay(  2000 ).hide( "slow" );
				}
			}
			return false;
		}
	});
	
	 
	
	$("#plan-timeline .date-line a:not('.year')").droppable(
		{
		 	hoverClass: 'drop-hover',
			tolerance: "top-left-touch",
			drop: function(event, ui) {
				 
				var $droppingUl = ui.draggable.parent("ul");
				
				
				
				
				
				
				var href = $(this).attr("href");
				href = href.replace("http://www.diandidian.com/plan/","");	
				var dateValue = href.replace(new RegExp("/", "g") ,"");

				var timeline = 't-'+dateValue ;
				var locationId = $(ui.draggable).attr("id");
				console.log( "locationId=" + locationId + "\ttimeline=" + timeline);
				
				var param = getCurrentStatusnamePlanName()
				param.date = timeline ; 
				param.locationId = locationId ; 
				console.log(  JSON.stringify(param))
				
				$.getJSON("/bag/locationSignDate" , param , function( result){
					
					if(  result.success ){
						
					}else{
						consloe.log("locationSignDate ERROR");
						return  false ;
					}
					
				});
				if(  locationMap[locationId] ) {
					locationMap[locationId].timeline = timeline  ;
					locationMap[locationId].content.date = getDateStr( timeline ) ;
				}
				
				
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
					var week = getWeek(year ,  month , day );
					
					var targetHtml =  '<h3 class="t-'+dateValue+'">'+month+'月'+day+'日 - '+ week +'</h3><ul class="clearfix" id="t-'+dateValue+'" ></ul>';
					if($(this).hasClass("no-sign")) {
						targetHtml =  '<h3 class="t-'+dateValue+'">尚未安排</h3><ul class="clearfix ui-droppable" id="t-'+dateValue+'" ></ul>';
						$("#plan-attractions-list").prepend("<hr />");
						$("#plan-attractions-list").prepend(targetHtml);
						$("#plan-attractions-list ul:first").append(ui.draggable);
						
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
	
	$("div#plan-attractions-list").on('click',"  div.bottom   a",function() {
		
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
								delete locationMap[locationId];
							 
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

function getDateStr(timeline){
	var arr = timeline.match("t-(\\d{4})(\\d{2})(\\d{2})");
	console.log( arr );
	if(arr!=null && arr.length == 4){
		var dateStr = arr[2]+"月" + arr[3] + "日" ;
		return dateStr;
		
	}else{
		return "尚未安排";
	}
}

function getWeek( year , month , day ){
	
	var d = new Date(year, month-1, day, 0, 0, 0, 0); 
	var week = weekNameArr[ d.getDay( )];
	console.log( "week=" + week ) ;
	console.log("d="+d +"  day of week = " + d.getDay( ) )
	return week ;
}
var weekNameArr = Array( "星期日" , "星期一", "星期二" ,"星期三" ,"星期四" ,"星期五" ,"星期六" );
