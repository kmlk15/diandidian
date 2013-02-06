package controllers.api;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import com.diandidian.models.TestModel;
import com.diandidian.services.api.TestService;
import com.google.inject.Guice;

import dependencies.AppModule;

public class TestEndpoint extends Controller {
	
	private static TestService ss = Guice.createInjector(new AppModule()).getInstance(TestService.class);
	
	@BodyParser.Of(BodyParser.Json.class)
	public static Result index() throws JsonParseException, JsonMappingException, IOException {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} 
		TestModel model = new ObjectMapper().readValue(json, TestModel.class);
		Long answer = ss.sum(model);
		ObjectNode result = Json.newObject();
		result.put("answer", answer);
		return ok(result);
	}

}