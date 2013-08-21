$(function() {
	function loadBag(){
		$.get("/bag/get", function(data){
			 
			var html = $( "div#my-bag " ,$(data)).html() ;
			$("div#my-bag").html( html )
			//alert( html );
		});
		
	}
	
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
		var locationId = $(this).attr( "id")
		 $.getJSON("/bag/del", {locationId: locationId} , function (result){
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
	
	
});