
$(function(){
	$(window).resize(function(e) {
		var space = 260;
		var height = $(this).height()-space;
		console.log( "height=" + height ) ;
		$("#contentmain").height(height);
	 
		 
	}).trigger("resize");
	
	$("#useravatarimg").imgr({ radius:"16px"});
	
	
	$("#coverimage").magnificPopup(  {
		items:{
			src: "/plan/shareplanCoverImage/"+ planId, 
		    type:  "iframe"		
		}
	});
	
	$('.shareplan_location_photo').on('mfpBeforeOpen', function(e /*, params */) {
		
		 var magnificPopup = $.magnificPopup.instance;
		 var locationId =  $(magnificPopup.st.el).attr("id") ; 
		 magnificPopup.items[0]= { 
				 src: "/plan/shareplanLocationImage/"+ planId+"/" + locationId,
				 type: "iframe"
		 	};
		});
	
	$(".shareplan_location_photo").magnificPopup(  {
		items:{
		    type:  "iframe"		
		}
	});
	

});