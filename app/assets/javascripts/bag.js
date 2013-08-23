	function loadBag(){
		$.get("/bag/get", function(data){
			var html = $( "div#my-bag " ,$(data)).html() ;
			$("div#my-bag").html( html )
			//alert( html );
		});
	}
$(function() {

	
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
		var tdTag =$( atag).parent()
		//alert( JSON.stringify (tdTag ) ) ;
		var locationId = $(this).attr( "id")
		var statusName =  $("input[name=statusName]" , tdTag).attr("value")
		var planName =$("input[name=planName]" , tdTag).attr("value")
		var param =  {locationId: locationId ,statusName: statusName , planName: planName }
		//alert( JSON.stringify (param ) ) ;
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

	
	
	//home page to add to bag, you should change to use backend to add
	$("#attractions-list").on('click',"ul li .add-bag", function(e) {
		var thistag = this 
		var bagAddress = $("#attractions-list h2 .address").text();//which bag to add
		var $ul = $("#my-bag .accordion .accordion div ul");//ul to add li
		$ul = $($ul[0]);
		var titleText = $(this).prevAll(".title").find("h3").text();
		var locationName = titleText.trim()
		var statusName= ""
		var planName = ""
		var param = {locationName:locationName ,statusName:statusName , planName: planName }
		
		//alert( JSON.stringify (param ) ) ;
		//return false;
		
		$.getJSON("/bag/add", param , function( result ) { 
			if( result.success){
				var needremove = $("#needremove", $ul) 
				if( needremove.length !=0){
					 //重新加载  bag 部分页面
					 
					loadBag();
				}else{ 
					var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td><td class="del" ><a href="#"  id="' + result.data.id +'"></a></td></tr></table></li>';
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
		var $ul = $("#my-bag .accordion .accordion div ul");//ul to add li
		$ul = $($ul[0]);
		var titleText = $("span#locationName").text();
		var locationName = titleText.trim() 
		var thistag = this
		$.getJSON("/bag/add",{locationName:locationName} , function( result ) { 
			if( result.success){
				var needremove = $("#needremove", $ul) 
				if( needremove.length !=0){
					loadBag();
				} else{
					var itemHtml = '<li><table cellpadding="0" cellspacing="0"><tr><td>'+titleText+'</td><td class="del" ><a href="#"  id="' + result.data.id +'"></a></td></tr></table></li>';
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