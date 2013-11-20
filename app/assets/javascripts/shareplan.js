 
 
  
$(function(){
	
	
	
	
	
//	$(window).resize(function(e) {
//		var space = 260;
//		var height = $(this).height()-space;
//		console.log( "height=" + height ) ;
//		$("#contentmain").height(height);
//	 
//		 
//	}).trigger("resize");
	
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
	

	$( "#shareIt >span > a  ").click(function(event){
		event.preventDefault() ;
		var url = $(this).attr("href");
		$.getJSON(url ,{},function(result){ 
			if( result.success){
				alert( result.msg)
			}else{
				alert( result.msg );
			}
			});
		return false;
	});
	
	$("textarea").on("keypress",function(){
		$(window).on('beforeunload', function(){
		    return "你现在正编辑中，如果你离开这页面，这些编辑将丢失。如果你想保存资料，请选择取消，并点击保存按钮。";
		});
		
	});
	
	$("#updateShare > span >   a  ").click(function(event){
		event.preventDefault() ;
		console.log("click updateShare ") ;
		var url = $('form#form1').attr("action");
		console.log("url=" + url ) ;
		$.post(url, $('form#form1').serialize(), function( result ){
			if( result.success){
				if( result.msg==""){
					alert("没有任何改变");
				}else{
					alert("保存成功");
				}
				
				$(window).off('beforeunload');
				
			}else{
				alert( result.msg );
			}
		});
		
		 
		  
		  return false 
		
	});
});