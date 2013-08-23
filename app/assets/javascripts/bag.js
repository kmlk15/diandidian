	function loadBag(){
		$.get("/bag/get", function(data){
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
	
	window.currentStatusName = "计划中";
	window.currentPlanName = "背包";
	
	loadBag();
	
	//sidebar accordion
	//initial state
	var $initialOpenHeader = $("#sidebar .accordion .accordion-header:nth-child(1)");
	$initialOpenHeader.addClass("state-active");
	$initialOpenHeader.next(".accordion-content").show();
	
	$("#sidebar #my-bag").on("click" , ".accordion .accordion-header" , function(e) {
		
		var $clickedHeader = $(this);
		var $headerNextContent = $clickedHeader.next(".accordion-content");
		var $setPlanA = $( "a.setPlan" , $clickedHeader) ;
		if($headerNextContent.is(":visible")) {
			$clickedHeader.removeClass("state-active");
			$setPlanA.hide();
		} else {
			$clickedHeader.addClass("state-active");
			$setPlanA.show();
			
		}
		$headerNextContent.slideToggle(200);
	});

	//home page delete from bag, you should change to use backend to delete
	$("#my-bag  ").on('click',".accordion .accordion div ul li table td.del a",function() {
		var atag = this
		var url = $.url( $(atag).attr("href") );
		var param = url.param()
		alert( JSON.stringify (param ) ) ;
		//return false;
		
		 $.getJSON("/bag/del", param,  function (result){
			 if( result.success){
				 var $itemDel = $(atag).parents("li");
					var $ul = $(atag).parents("ul");
					$itemDel.remove();
					updateBagCount($ul);
					
					// DETAIL PAGE 
					if( $("#attraction-detal .user-content .col.col3 span.added").length > 0 ){
						var e = $("#attraction-detal .user-content .col.col3 span.added")
						e.text("加入背包")
						e.removeClass("added")
						e.addClass("add-bag")
					}
			 }else{
				 alert( result.msg)
			 }
		 })
		return false;
	});

	
	//设置 plan
	$("#my-bag  ").on('click',".accordion .accordion    a.setPlan",function() {
		var atag = this
		var url = $.url( $(atag).attr("href") );
		var query = url.param()
		window.currentStatusName =  query.statusName
		window.currentPlanName = query.planName
		var param = query ;
		 //alert( JSON.stringify (param ) ) ;
		$("div#my-bag h3  span#currentStatus").html(query.statusName);
		$("div#my-bag h3  span#currentPlan").html(query.planName);
		//将移除 所有 ul 的 css  open
		 
		
		$("#my-bag .accordion .accordion div ul").removeClass("open");
		//当前的 加上 css  open
		$("ul", $(atag).parent().next()).addClass("open");
		
		 return false;
		
		 
	});
	
	
	//home page to add to bag, you should change to use backend to add
	/**
	 * 需要定位到当前活动的 <ul> 是哪一个
	 */
	$("#attractions-list").on('click',"ul li .add-bag", function(e) {
		var thistag = this 
		var bagAddress = $("#attractions-list h2 .address").text();//which bag to add
		var $ul = $("#my-bag .accordion .accordion div ul.open");//ul to add li
		$ul = $($ul[0]);
		var titleText = $(this).prevAll(".title").find("h3").text();
		var locationName = titleText.trim()
		var statusName=  window.currentStatusName
		var planName = window.currentPlanName
		
		var param = {locationName:locationName ,statusName:statusName , planName: planName }
		
		alert( JSON.stringify (param ) ) ;
		//return false;
		
		$.getJSON("/bag/add", param , function( result ) { 
			if( result.success){
				var needremove = $("#needremove", $ul) 
				if( needremove.length !=0){
					 //重新加载  bag 部分页面
					 
					loadBag();
				}else{ 
					var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td>';
					itemHtml = itemHtml + '<td class="del" ><a href="?locationId='+encodeURI(result.data.id ) +'&statusName='+ encodeURI(statusName)+'&planName='+encodeURI(planName)+'" ></a></td>';
					itemHtml = itemHtml + '</tr></table></li>';
					alert(  itemHtml )
					$ul.append(itemHtml);
				}
				//searchBagItem($ul,'');
				$(thistag).next(".added-bag").css("display", "block");
				$(thistag).css("display", "none");
				
				updateBagCount($ul);
			}else{
				alert( result.msg )
			}
		})
		
		return false;
	})
	

	
	//detail page to add to bag, you should change to use backend to add, add you should avoid duplicate items
	$("#attraction-detal .user-content .col.col3 span.add-bag").click(function(e) {
		if($(this).hasClass("added")) {
			return;
		}
		var bagAddress = $("#attractions-list h2 .address").text();//which bag to add
		var $ul = $("#my-bag .accordion .accordion div ul.open");//ul to add li
		$ul = $($ul[0]);
		var titleText = $("span#locationName").text();
		var locationName = titleText.trim() 
		var statusName=  window.currentStatusName
		var planName = window.currentPlanName
		var param = {locationName:locationName ,statusName:statusName , planName: planName }
		//console.log( JSON.stringify (param ) ) ;
		//return false;
		
		var thistag = this
		$.getJSON("/bag/add", param , function( result ) { 
			if( result.success){
				var needremove = $("#needremove", $ul) 
				if( needremove.length !=0){
					loadBag();
				} else{
					
					var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td>';
					itemHtml = itemHtml + '<td class="del" ><a href="?locationId='+encodeURI(result.data.id ) +'&statusName='+ encodeURI(statusName)+'&planName='+encodeURI(planName)+' ></a></td>';
					itemHtml = itemHtml +'</tr></table></li>';
					
					$ul.append(itemHtml);
					updateBagCount($ul);
				}
				//searchBagItem($ul,'');
				$(thistag).text("已在背包");
				$(thistag).addClass("added");
				
			}else{
				alert( result.msg )
			}
		})
		
		 
		
		return false;
	});
	
	
});