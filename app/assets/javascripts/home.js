function loadHomeAttractions( query) {
	$.getJSON("/location", query, function(json){
		var attractionListHtml = '';
		 
		if (json.length > 0) {
			 //alert( query.country == null )
			if( query.country == null && query.city==null){
				attractionListHtml += '<h2>  </h2>';
			}else{
				attractionListHtml += '<h2>' + query.country + ' &gt;' + query.city+'</h2>';
			}
			attractionListHtml += '<ul class="clearfix">';
		 
			$.each(json, function(index, item){
				 
				var attractionHtml = '';
				attractionHtml += '<li>';
//				if(item.pictures && item.pictures.planning) {
//					attractionHtml += '<img src="' + item.pictures.planning + '" width="266" height="262" alt="" />';
//				} else {
//					attractionHtml += '<img src="/assets/images/dummy/img01.jpg" width="266" height="262" alt="" />';//default image here
//				}
				if(item.photo) {
				attractionHtml += '<img src="http://diandidian.s3-us-west-1.amazonaws.com/' + item.photo + '" width="266" height="262" alt="" />';
				} else {
					attractionHtml += '<img src="/assets/images/dummy/img01.jpg" width="266" height="262" alt="" />';//default image here
				}				 
				attractionHtml += '<div class="title">';
				attractionHtml += '<h3>'+item.name+'</h3>';
				attractionHtml += '<span>'+item.address.city+': '+item.address.district+'</span>';
				attractionHtml += '</div>';
				attractionHtml += '<div class="user">';
				attractionHtml += '<a href="#"><img src="/assets/images/dummy/user01.png" width="32" height="32" alt="" /></a>';
				attractionHtml += '<div>';
				attractionHtml += '<span>照片</span>';
				attractionHtml += '<p>攻略作者 @ 香港8天的行程</p>';
				attractionHtml += '</div>';
				attractionHtml += '</div>';
				attractionHtml += '<span class="add-bag">加入背包</span >';
				attractionHtml += '<span class="added-bag">已在背包</span >';
				attractionHtml += '<span class="detail"><a href="/detail/view/' +  item.name + '">详情</a></span >';
				attractionHtml += '</li>';
				attractionListHtml += attractionHtml;
				 
			});
			attractionListHtml += '</ul>';
		}else{
//			attractionListHtml += '<h2>' + query.country + ' &gt;' + query.city+'</h2>';
//			attractionListHtml += '<ul class="clearfix">';
//			attractionListHtml += '</ul>';
		}
		
		$("#attractions-list").html(attractionListHtml);
		
		$(window).trigger("resize");
		$("#attractions-list h2").scrollToFixed({zIndex: 97, marginTop: $("#header").outerHeight(true)});
		homeAttractionHover();
	});
	
	
}

$(function() {
 
	var url = $.url();
	var query = url.param()
	loadHomeAttractions( query );
})