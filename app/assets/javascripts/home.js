function twoajax( query ){
	
	$.when( $.getJSON("/location", query) , $.getJSON("/plan/listShare", query )  ).done(function( a1, a2 ){
		
		console.log( a1[0].data  );
		console.log(a2[0].data ) ;
		var  shareplanHtml =  loadSharePlan( a2[0]) ;
		
		loadHomeAttractions(a1[0] , shareplanHtml ) ;
	});
			
}

function loadHomeAttractions( result , shareplanHtml ) {
	 
		var attractionListHtml = '';
		 var json = result.data.list ; 
		 
		 //alert( query.country == null )
			if( result.data.country == ""     ){
				attractionListHtml += '<h2>  </h2>';
			}else {
				attractionListHtml+='<h2>'
					var  tmptxt = ""
					if( result.data.country != "" ){
						attractionListHtml += '<a href="/home?country=' + encodeURI( result.data.country ) +'">' + result.data.country + '</a>';
					}
				  if(result.data.province!= ""  ){
					   attractionListHtml += ' &gt;<a href="/home?province=' + encodeURI( result.data.province ) +'">' + result.data.province + '</a>';
				   }
				 
				   if(result.data.city!= ""  ){
					   attractionListHtml += ' &gt;<a href="/home?city=' + encodeURI( result.data.city ) +'">' + result.data.city + '</a>';
				   }
				   
				attractionListHtml+='</h2>'
			} 
			
		if (json.length > 0) {
			
			attractionListHtml += '<ul class="clearfix">';
		 
			$.each(json, function(index, data){
				var item = data.location ;
				var attractionHtml = '';
				attractionHtml += '<li>';
//				if(item.pictures && item.pictures.planning) {
//					attractionHtml += '<img src="' + item.pictures.planning + '" width="266" height="262" alt="" />';
//				} else {
//					attractionHtml += '<img src="/assets/images/dummy/img01.jpg" width="266" height="262" alt="" />';//default image here
//				}
				if(item.photo) {
					attractionHtml += '<img src="http://diandidian.s3-us-west-1.amazonaws.com/' + item.photo + '" width="264" height="260" alt="" />';
				} else {
					attractionHtml += '<img src="/assets/images/dummy/img01.jpg" width="264" height="260" alt="" />';//default image here
				}				 
				
				if( item.name.length >= 7 ){
					attractionHtml += '<div class="title2">';
					attractionHtml += '<h3>'+item.name+'</h3>';
					attractionHtml += '<span>'+item.address.city+': '+item.address.district+'</span>';
					attractionHtml += '</div>';
				}else{
					attractionHtml += '<div class="title">';
					attractionHtml += '<h3>'+item.name+'</h3>';
					attractionHtml += '<span>'+item.address.city+': '+item.address.district+'</span>';
					attractionHtml += '</div>';
				}
				
				if( data.photo.uploadtype =="admin"){
					attractionHtml += '<div class="user">';
					if( data.photo.avatar!=""){
						attractionHtml += '<a href="' + data.photo.avatar +'" target="_blank" >图片来源: '+ data.photo.username +'</a>'  ;
						attractionHtml += '<span>'+ data.photo.brief  +'</span>';
					}else{
						if(   data.photo.brief != "" ){
							attractionHtml += '<table><tr><td style="vertical-align:top;padding-left:10px;line-height:16px">'+ data.photo.username  +':</td>';
							attractionHtml += '<td style="width:216px;text-align:left;line-height:16px">'+ data.photo.brief +"</td></tr></table>";
						}else{
							attractionHtml += '<span>'+ data.photo.username  +'</span>';
						}
						
					}
					
					
				 
					attractionHtml += '</div>';
				}else if(  data.photo.uploadtype =="user" ){
					attractionHtml += '<div class="user">';
					attractionHtml += '<a href="#"><img src="' + data.photo.avatar +'" width="32" height="32" alt="" /></a>';
					attractionHtml += '<div>';
					attractionHtml += '<span>照片</span>';
					attractionHtml += '<p>攻略作者 @ 香港8天的行程</p>';
					attractionHtml += '</div>';
					attractionHtml += '</div>';					
				}else{
					attractionHtml += '<div class="user">';
					console.log( data.photo.avatar )
					if( data.photo.avatar!=""){
						attractionHtml += '<a href="' + data.photo.avatar +'" target="_blank" >图片来源:</a><br/>';
					}
					attractionHtml += '<p>'+ data.photo.username +'</p>';
					attractionHtml += '<div>';
					attractionHtml += '<span></span>';
					attractionHtml += '<p></p>';
					attractionHtml += '</div>';
					attractionHtml += '</div>';
					
				}

				
				
				attractionHtml += '<span class="add-bag">加入背包</span >';
				attractionHtml += '<span class="added-bag">已在背包</span >';
				attractionHtml += '<span class="detail"><a href="/detail/view/' + encodeURI(item.name) + '">详情</a></span >';
				attractionHtml += '</li>';
				attractionListHtml += attractionHtml;
				 
			});
			
			attractionListHtml += shareplanHtml  ;
			attractionListHtml += '</ul>';
		}else{
//			attractionListHtml += '<h2>' + query.country + ' &gt;' + query.city+'</h2>';
			attractionListHtml += '<ul class="clearfix">';
 		
			attractionListHtml += shareplanHtml  ;
 			attractionListHtml += '</ul>';
		}
		
		$("#attractions-list").html(attractionListHtml);
		
		$(window).trigger("resize");
		$("#attractions-list h2").scrollToFixed({zIndex: 97, marginTop: $("#header").outerHeight(true)});
		
		$("img.shareplan_avataimage").imgr({ radius:"16px"});
		
		homeAttractionHover();
 
	
}

function loadSharePlan( result  ){
 
		if( result.success){
			console.log( JSON.stringify(result) ) ;
			var jsonArr = result.data ;
			var html = "" ;
			html += "<!-- share plan -->";
			$.each(jsonArr , function(index,  data){
				var lihtml = "<li >";
				var item = data ; 
				lihtml += "<a href='/plan/viewShare?planId=" + item.id + "' style='text-decoration: none'>"
				if(item.img) {
					lihtml += '<img src="http://diandidian.s3-us-west-1.amazonaws.com/' + item.img + '" width="264" height="260" alt="" />';
				} else {
					lihtml += '<img src="/assets/images/dummy/img01.jpg" width="264" height="260" alt="" />';//default image here
				}
				
				lihtml += '<div class="title2">';
				lihtml += '<h3>'+item.name+'</h3>';
				lihtml += '<span></span>';
				lihtml += '</div>';
				
				lihtml += '<div class="user">';
				lihtml += '<div style="margin-top:5px;margin-right:5px"><img src="' +item.avatar +'" width="32" height="32" alt=""     class="shareplan_avataimage" /></div>';
				
				lihtml += '<span style="margin-top:18px;">'+ item.username +'</span>';
				lihtml += '<div>';
				lihtml += '<span></span>';
				lihtml += '<p></p>';
				lihtml += '</div>';
				lihtml += '</div>';
				lihtml += "</a>"
				lihtml += "</li>" ; 
				html += lihtml  +"\n" ;
				
			} );
			html += "<!-- share plan -->";
			console.log( html ) ;
			return html ;
			 
			//$("img.shareplan_avataimage").imgr({ radius:"16px"});
		}
	 
	
}
$(function() {
 
	var url = $.url();
	var query = url.param()
	twoajax( query );
})
