	function loadBag(){
		
		var url = $.url();
		var param = url.param()
		param.plan = true;
		$.get("/bag/get", param ,  function(data){
			var html = $( "div#my-bag " ,$(data)).html() ;
			$("div#my-bag").html( html )
			//alert( html );
		});
	}
	
	/**
	update bag item account
	*/
	function updateBagCount($ul) {
		var parentDiv = $ul.parent().prev();
		//alert( parentDiv.html ())
		var $countHold = $(" small span" ,parentDiv);
		var count = $ul.find("li").length;
		$countHold.text(count)
	}

	
	
$(function() {
	
$.ajaxSetup({ cache: false });

	var url = $.url();
	var param = url.param()

	window.currentStatusName = param.statusName;
	window.currentPlanName = param.planName;
	
	loadBag();
	
	//sidebar accordion
	//initial state
	var $initialOpenHeader = $("#sidebar .accordion .accordion-header:nth-child(1)");
	$initialOpenHeader.addClass("state-active");
	$initialOpenHeader.next(".accordion-content").show();
	
	$("#sidebar #my-bag").on("click" , ".accordion .accordion-header" , function(e) {
		
		var $clickedHeader = $(this);
		var $headerNextContent = $clickedHeader.next(".accordion-content");
		 
		if($headerNextContent.is(":visible")) {
			$clickedHeader.removeClass("state-active");
			 
		} else {
			if( $clickedHeader.hasClass( "active-bag" )){
				 
			 }else{
				 $clickedHeader.addClass("state-active");
			 }
			 
			
		}
		$headerNextContent.slideToggle(200);
	});

	//home page delete from bag, you should change to use backend to delete
	$("#my-bag  ").on('click',".accordion .accordion div ul li table td.del a",function() {
		var atag = this
		var url = $.url( $(atag).attr("href") );
		var param = url.param()
		//alert( JSON.stringify (param ) ) ;
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
					if(typeof markerMap === 'undefined'  ) {
					}else{
					//删除地图中的数据
					console.log(markerMap );
					console.log( locationId);
					console.log(markerMap[locationId] );
					if(markerMap !=null && markerMap[locationId] != undefined ){
						markerMap[locationId].setMap( null );
						delete markerMap[locationId] ;
						//还需要从已经分配的的日期中删除？？ 
						delete locationMap[locationId];
						
					}
					}
			 }else{
				 alert( result.msg)
			 }
		 })
		return false;
	});

 
	//删除背包
	$("#my-bag  ").on('click',".accordion .accordion   h4  a.deletePlan",function() {
		//是否为空
		var atag = this
		var ul = $("ul", $(atag).parent().next())
		var count = ul.find("li").length;
		if( count==0  || confirm("不是空的背包，确认要删除吗?")){
			
			var url = $.url( $(atag).attr("href") );
	 		var query = url.param()
	 		var q = {"fromStatus": query.statusName , "fromPlan":query.planName,"toStatus": query.statusName , "toPlan":query.planName, "cmd":"delete" }
	 		 
	 		$.getJSON("/bag/updateJson" ,q , function (result){
	 			 if( result.success){
	 				 
	 				 //需要转移到合适的 背包
	 				 // 不是空的下一个背包
	 				var nexturl = $(atag).parent().nextAll( "h4 ").filter(function(index){
	 					var count = $( " a.setPlan small span", $(this)).text();
	 					console.log( "count=" + count );
	 					if( count == "0"){
	 						return false ; 
	 					}else{
	 						return true;
	 					}
	 				 }).first(); 
	 				console.log("nexturl=" + nexturl )
	 				if( nexturl.length== 1  ){
	 					console.log( "next plan  href=" + $( " a.setPlan", nexturl).attr("href")  ) ;
	 					window.location=  $( " a.setPlan", nexturl).attr("href") ;
	 				}else{
	 					console.log(" goto home ");
	 					window.location= "/home" ;
	 				}
	 				 $(atag).parent().next().remove();
	 				 $(atag).parent().remove();
	 				 
	 				
	 				 
	 			 }
	 			
	 		});
	 		
		} 
		
		return false;
	});
	
	
	
	 
	/**
	 * 在 PlanPage , 点击， 如果背包非空， 重新载入页面
	 */
	$("#my-bag  ").on('click',".accordion .accordion   h4  a.setPlan",function() {
		var atag = this
		
		var count = $( "small span", $(this)).text();
		console.log( "count=" + count );
		if( count == "0"){
			return false ; 
		}
		
		var u1 = $.url( $(atag).attr("href") );
		var p1 = u1.param();
		
		var u2 = $.url( )
		var p2 = u2.param();
		
		console.log( "p1=" + JSON.stringify( p1))
		console.log( "p2=" + JSON.stringify( p2))
		if (  JSON.stringify( p1) ==  JSON.stringify( p2) ){
			return false;
		}
		
		 
		 return  true  ;
	});
	
	//search, 
	$("#my-bag  ").on('click',"div.accordion div.accordion  div.accordion-content     a.search",function() {
		var atag = this
		console.log( "cityListStr=" + cityListStr )
		var url = $.url(  );
	    var param = url.param() ;
	    param.cityList = cityListStr ;
	    
		var  query = "?"   + jQuery.param( param );
		console.log( "query=" + query ) ;
		
		$(this).attr("href" , "/home" + query )
		return  true  ;
	});
	
		
	$("#attractions-list").on('click',"ul li .detail", function(e) {
		
		var detailurl = $("a" , this).attr("href");
		//alert(detailurl);
		window.location = detailurl;
		return false;
	});
	
 
	
 
	
});
