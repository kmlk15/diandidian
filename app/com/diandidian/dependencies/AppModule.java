package com.diandidian.dependencies;

import com.diandidian.services.api.LocationService;
import com.diandidian.services.impl.LocationServiceImpl;
import com.google.inject.AbstractModule;
public class AppModule extends AbstractModule {
	
	public void configure() {
		bind(LocationService.class).to(LocationServiceImpl.class);
	}
	
}
