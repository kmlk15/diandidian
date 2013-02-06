package dependencies;

import com.diandidian.services.api.TestService;
import com.diandidian.services.impl.TestServiceImpl;
import com.google.inject.AbstractModule;

public class AppModule extends AbstractModule {
	
	public void configure() {
		bind(TestService.class).to(TestServiceImpl.class);
		//bind(LocationService.class).to(LocationServiceImpl.class);
	}
	
}
