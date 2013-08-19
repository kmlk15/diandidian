package controllers

import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller

import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import java.net.URLEncoder


object Bags  extends Controller  {
	val log = LoggerFactory.getLogger(Bags.getClass())
	val  bagService = base.BagServiceRegistry.bagService
	
 
	def add( locationName: String ) = Action{ implicit request =>
	  
	  val result = Json.obj(  "name" -> locationName)
	  Ok( result)
	}
	
	def del(locationName: String )  =  Action{ implicit request =>
	  
	  val result = Json.obj(  "name" -> locationName)
	  Ok( result)
	}
	
	def get() = Action{ implicit request =>
	 val locationNameList =   (1 to 10).map( i => "location" + i ).toList
	  Ok ( Json.toJson( locationNameList)  )
	}
	
	
}