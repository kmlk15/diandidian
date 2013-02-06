package com.diandidian.services.impl;

import com.diandidian.models.TestModel;
import com.diandidian.services.api.TestService;

public class TestServiceImpl implements TestService {
	
	public Long sum(TestModel model) {
		return model.a + model.b;
	}
}
