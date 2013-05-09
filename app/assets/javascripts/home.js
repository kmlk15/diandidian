function loadHomeAttractions() {
	$.getJSON("/location", null, function(json){
		var attractionListHtml = '';
		if (json.length > 0) {
			attractionListHtml += '<h2>中国 &gt; 香港</h2><ul class="clearfix">';
			
			$.each(json, function(index, item){
				var attractionHtml = '';
				attractionHtml += '<li>';
				if(item.pictures.planning) {
					attractionHtml += '<img src="' + item.pictures.planning + '" width="266" height="262" alt="" />';
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
				attractionHtml += '<span class="detail">详情</span >';
				attractionHtml += '</li>';
				attractionListHtml += attractionHtml;
			});
			attractionListHtml += '</ul>';
		}
		
		$("#attractions-list").html(attractionListHtml);
		
		$(window).trigger("resize");
		$("#attractions-list h2").scrollToFixed({zIndex: 97, marginTop: $("#header").outerHeight(true)});
		homeAttractionHover();
	});
	
	
}

$(function() {
	loadHomeAttractions();
})