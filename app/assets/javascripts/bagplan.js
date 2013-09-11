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
			$clickedHeader.addClass("state-active");
			 
			
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
					if( $("#attraction-detal .user-content .col.col3 span.added").length > 0 ){
						var e = $("#attraction-detal .user-content .col.col3 span.added")
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
	/**
	 * 在 PlanPage , 点击， 修改url ,重新载入页面
	 */
	$("#my-bag  ").on('click',".accordion .accordion   h4  a.setPlan",function() {
		var atag = this
		
		
		var url = $.url( $(atag).attr("href") );
		
		 
		 return true;
	});
	
		
	$("#attractions-list").on('click',"ul li .detail", function(e) {
		
		var detailurl = $("a" , this).attr("href");
		//alert(detailurl);
		window.location = detailurl;
		return false;
	});
	
 
	
 
	
});
