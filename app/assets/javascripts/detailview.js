



$(function() {
 
	
 
	
	function loadDetail( name ) {
		 
		$.getJSON("/detail/json/" + name , null, function(json){
			//  
			 
			 var viewModel = ko.mapping.fromJS( json );
			 
			 ko.applyBindings(viewModel)
		});
		
		
	}
	
	
	
	
	loadDetail(  detailname  );
})