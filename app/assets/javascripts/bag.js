	function loadBag(){
		var url = $.url();
		var param = url.param()
		$.get("/bag/get",param , function(data){
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
		 
		if($headerNextContent.is(":visible")) {
			$clickedHeader.removeClass("state-active");
			 
		} else {
			//$clickedHeader.removeClass("active-bag");
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
		
		 $.getJSON("/bag/del", param,  function (result){
			 if( result.success){
				 var  dellocationName = $(atag).parents("td").prev().text();

				 var $itemDel = $(atag).parents("li");
					var $ul = $(atag).parents("ul");
					$itemDel.remove();
					updateBagCount($ul);
					
					// DETAIL PAGE 
					if( $("#attraction-detal  div.detal-sidebar  span.added").length > 0 ){
						var e = $("#attraction-detal  div.detal-sidebar  span.added")
						var locationName = $("span#locationName").text();
						if( dellocationName == locationName  ){
							e.text("加入背包")
							e.removeClass("added")
							e.addClass("add-bag")
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
	 				 
	 				 $(atag).parent().next().remove();
	 				  
	 				 $(atag).parent().remove();
	 				 
	 				 
	 			 }
	 			
	 		});
	 		
		} 
		
		return false;
	});
	
	
	//设置 plan , 为 地点的 添加删除 ，准备关联数据  
	$("#my-bag  ").on('click',".accordion .accordion   h4  a.setPlan",function() {
		var atag = this
		
		
		var url = $.url( $(atag).attr("href") );
		var query = url.param()
		window.currentStatusName =  query.statusName
		window.currentPlanName = query.planName
		var param = query ;
		
		//将所有 已经打开的背包 关闭 Edit-10-9-13-REVISED
		$("#my-bag h4.accordion-header ").removeClass("state-active");
		$("#my-bag div.accordion-content  div.accordion-content ").hide();
		
		 //alert( JSON.stringify (param ) ) ;
		//$("div#my-bag h3  span#currentStatus").html(query.statusName);
		//$("div#my-bag h3  span#currentPlan").html(query.planName);
		//将移除 所有 ul 的 css  open
		// $("#my-bag  .accordion .accordion  h4.accordion-header a ").css("color" , "rgb(60, 60, 60)") ; 
		 
		 //$("#my-bag  .accordion .accordion  h4.accordion-header").css("background-image","url(/assets/images/accordion-iteam-icon-active.png)") ;
		
		 $("#my-bag  .accordion .accordion  h4.accordion-header").removeClass( "active-bag" ) ;
		// $(this).css("color" , "rgb(0, 174, 239)") ;
		 
		 $(this).parent().removeClass("state-active");
		 $(this).parent().addClass("active-bag");
		 
		// $(this).parent().css("background-image","url(/assets/images/icon_triangle_blue.png)");
		 $(this).parent().next().show();
		$("#my-bag .accordion .accordion div ul").removeClass("open");
		//当前的 加上 css  open
		//alert( $(atag).parent().html()) ;
		$("ul", $(atag).parent().next()).addClass("open");
		
		//设置 Detail Page 的  addbag 按钮状态
		var e = $("#attraction-detal .user-content .col.col3 span.add-bag")
		if( e.length > 0 ){
			var $ul = $("#my-bag .accordion .accordion div ul.open");//ul to add li
			$ul = $($ul[0]);
			var titleText = $("span#locationName").text();
			if(searchBagItem($ul , titleText)){
				e.text("已在背包");
				e.addClass("added");
			}else{
				e.text("加入背包");
				e.removeClass("added");
				 
			}
		}
			
		 return false;
	});
	
		
	$("#attractions-list").on('click',"ul li .detail", function(e) {
		
		var detailurl = $("a" , this).attr("href");
		//alert(detailurl);
		window.location = detailurl;
		return false;
	});
	
	//home page to add to bag, you should change to use backend to add
	/**
	 * 需要定位到当前活动的 <ul> 是哪一个
	 */
	$("#attractions-list").on('click',"ul li .add-bag", function(e) {
		var thistag = this 
		var titleText = $(this).parent("li").find("h3").text();
		//var locationName = titleText.trim()
		var locationName = titleText ; 
		
		var bagAddress = $("#attractions-list h2 .address").text();//which bag to add
		
		var $ul = $("#my-bag .accordion .accordion div ul.open");//ul to add li
		console.log( "$ul.length=" + $ul.length );
		if(  $ul.length > 0 ){
			$ul = $($ul[0]);
			
			var href =$("a", $ul.parent().prev("h4") ).attr("href");
			 
			console.log(" aTag href  =" + href  ) ;
			if( href ){
				var url = $.url(  href );
				var param = url.param()
				console.log("param=" +  JSON.stringify (param ) ) ;
				
				window.currentStatusName = param.statusName ;
				window.currentPlanName = param.planName;
				var statusName = param.statusName ;
				var planName = param.planName ;
				param.locationName = locationName ;
			}else{
				var param = { locationName:locationName } ;
			}
			
		}else{
			 
			var param = { locationName:locationName } ;
		}
		
		
		 
		
		console.log( JSON.stringify (param ) ) ;
		//return false;
		
		$.getJSON("/bag/add", param , function( result ) { 
			if( result.success){
				var needremove = $("li#needremove") 
				console.log("needremove.length=" + needremove.length )
				if( needremove.length !=0){
					 //重新加载  bag 部分页面
					loadBag();
				}else{ 
					var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td>';
					itemHtml = itemHtml + '<td class="del" ><a href="?locationId='+encodeURI(result.data.id ) +'&statusName='+ encodeURI(statusName)+'&planName='+encodeURI(planName)+'" ></a></td>';
					itemHtml = itemHtml + '</tr></table></li>';
					//alert(  itemHtml ) ;
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
	$("#attraction-detal .detal-sidebar  span.add-bag").click(function(e) {
		if($(this).hasClass("added")) {
			return;
		}
		
		var bagAddress = $("#attractions-list h2 .address").text();//which bag to add
		var $ul = $("#my-bag .accordion .accordion div ul.open");//ul to add li
		$ul = $($ul[0]);
		var titleText = $("span#locationName").text();
		if(searchBagItem($ul , titleText)){
			$(this).text("已在背包");
			$(this).addClass("added");
			return;
		}
		
		var locationName = titleText ; 
		var statusName=  window.currentStatusName ; 
		var planName = window.currentPlanName ; 
		var param = {locationName:locationName ,statusName:statusName , planName: planName }
		//console.log( JSON.stringify (param ) ) ;
		//return false;
		
		var thistag = this
		$.getJSON("/bag/add", param , function( result ) { 
			if( result.success){
				var needremove = $("li#needremove") 
				if( needremove.length !=0){ 
					loadBag();
				} else{
					
					var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td>';
					itemHtml = itemHtml + '<td class="del" ><a href="?locationId='+encodeURI(result.data.id ) +'&statusName='+ encodeURI(statusName)+'&planName='+encodeURI(planName)+'" ></a></td>';
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
	
	//addNewPlan 添加新的背包
	$("div#my-bag" ).on("click" , "a.addNewPlan"  , function(){
		var thistag = this;
		$.getJSON("/bag/createNewplan" , function(result){ 
			
		$("#my-bag .accordion .accordion div ul").removeClass("open");
		$("#my-bag  .accordion .accordion  h4.accordion-header  ").removeClass( "active-bag") ; 
		
		var planName = result.data.planName ; 
		var statusName  =   result.data.statusName  ; 
		
		window.currentStatusName  = statusName ;
		window.currentPlanName = planName ;
		
		var html= '';
		html += '<div class="accordion accordion-content" style="display:block;">';
		html +='<h4 class="accordion-header  active-bag" >';
		html += '<a class="deletePlan" href="?statusName='+ encodeURI( statusName ) +'&amp;planName='+ encodeURI( planName ) +'"></a>';
		html +='<a   href="?statusName='+ encodeURI( statusName ) +'&amp;planName='+ encodeURI( planName ) +'"  class="setPlan">'+ planName +'<span>-</span>';
		html += '<small><span>0</span>个景点</small></a>'
		html +='</h4>' ;
		html += '<div class="accordion-content clearfix   state-active" style="display: block;">';
		html +='<ul class="open">';	
		html +='</ul>';
		html +='';
		html +=' <a class="plan" href="/plan/?statusName='+ encodeURI( statusName ) +'&amp;planName='+ encodeURI( planName ) +'">计划行程</a>';
		html +='</div>';
		html += '</div>';
		
	    //alert( html );
	
        //alert(  $( this ).parent(). html() ) ; 
		
		
	    $( thistag ).parent(). after( html ) ;
		});
		return false;
		
	});
	
	
	//空的背包 不允许进入  Plan 页面
	$("div#my-bag" ).on("click" , "a.plan"  , function(){
		console.log("click a.plan ") ;
	 
		var count = $(this).prev("ul").find("li").length;
		console.log("li count = " + count );
		if( count >0 ){
			return true;
		}else{
			return false;
		}
	});
});
