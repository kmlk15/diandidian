function Test(options) {
	this.options = options;
	this.host = "http://127.0.0.1:8000";

	this.lock = false;
	this.init();
}

Test.prototype.init = function() {
	var self = this;
	$(this.options.submit).on("click", function(e) {
		if (!self.lock) {
			self.lock = true;
			var fieldA = $(self.options.fieldA).val();
			var fieldB = $(self.options.fieldB).val();
			var data = {
				a : fieldA,
				b : fieldB
			};
			var json = JSON.stringify(data);
			var options = {
				method : "POST",
				url : self.host + "/test.json",
				data : json
			}
			self.ajax(options, self.result);
		}
	});
}

Test.prototype.ajax = function(options, callbacks) {
	var self = this;
	$.ajax({
		type : options.method,
		datatype : 'json',
		data : options.data,
		url : options.url,
		contentType: "application/json",
		statusCode : {
			200 : function(d) {
				callbacks(self, d);
			},
			404 : function(d) {
				alert("404");
			},
			500 : function(d) {
				alert("500");
			}
		}
	});
}

Test.prototype.result = function(self, result) {
	self.lock = false;
	$(self.options.answer).html(result.answer);
}