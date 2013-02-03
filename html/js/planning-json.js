function loadAttractions() {
	$.getJSON("planning-json.js", null, function(json){
		var attractionListHtml = '';
		if (json.length > 0) {
			attractionListHtml += '<h3 class="t-no-assign active">尚未安排</h3><ul class="clearfix">';
			
			$.each(json, function(index, item){
				var attractionHtml = '';
				attractionHtml += '<li>';
				if(item.pictures.planning) {
					attractionHtml += '<img src="'+item.pictures.planning+'" width="200" height="181" alt="" />';
				} else {
					attractionHtml += '<img src="images/dummy/planning/img04.jpg" width="200" height="181" alt="" />';
				}
				attractionHtml += '<div class="bottom">';
				attractionHtml += '<h4>'+item.name+'</h4>';
				attractionHtml += '<span>'+item.addresses.city+': '+item.addresses.district+'</span>';
				attractionHtml += '</div>';
				attractionHtml += '<div class="hover-cover">';
				attractionHtml += '<table>';
				attractionHtml += '<tr>';
				attractionHtml += '<td class="label">地址:</td>';
				attractionHtml += '<td>'+item.addresses.street+'</td>';
				attractionHtml += '</tr>';
				attractionHtml += '<tr>';
				attractionHtml += '<td class="label">时间:</td>';
				attractionHtml += '<td>每日上午10时至下午6时, 逢星期六上午10时至晚上8时 逢星期四（公众假期除外）休馆 逢星期三免费入场</td>';
				attractionHtml += '</tr>';
				attractionHtml += '<tr>';
				attractionHtml += '<td class="label">成人:</td>';
				attractionHtml += '<td>$'+item.admissions.adults+', 儿童:$'+item.admissions.children+'</td>';
				attractionHtml += '</tr>';
				attractionHtml += '<tr>';
				attractionHtml += '<td class="label">电话:</td>';
				attractionHtml += '<td>'+item.phones.General+'</td>';
				attractionHtml += '</tr>';
				attractionHtml += '</table>';
				attractionHtml += '<div class="note-input">';
				attractionHtml += '<textarea>早晨会见麦克</textarea>';
				attractionHtml += '<span>完成</span>';
				attractionHtml += '</div>';
				attractionHtml += '<span class="btn remark">备注</span>';
				attractionHtml += '<span class="btn detail">详情</span>';
				attractionHtml += '</div>';
				attractionHtml += '</li>';
				attractionListHtml += attractionHtml;
			});
			attractionListHtml += '</ul>';
		}
		$("#plan-attractions-list").html(attractionListHtml);
		draggableAttraction();
	});
	
	
}

$(function() {
	loadAttractions();
})