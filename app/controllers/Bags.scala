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
	val locationService = base.locationFormRegistry.locationService
 
	def add( locationName: String ) = Action{ implicit request =>
	   locationService.getByName( locationName) match{
	    case None =>   Ok( Json.obj("success"->false, "msg"-> "该地点不存在" ) )
	    case Some( location ) =>{
	      val data = Json.obj("name" -> locationName , "id" -> location.id.get)
	      val result = Json.obj( "success"->true , "data" -> data )
	      Ok( result)
	    }
	  }
	  
	 
	}
	
	def del(locationId: String )  =  Action{ implicit request =>
	  locationService.getById( locationId) match{
	    case None =>   Ok( Json.obj("success"->false, "msg"-> "该地点不存在" ) )
	     case Some( location ) =>{
	      val data = Json.obj("name" -> location.name , "id" -> location.id.get)
	      val result = Json.obj( "success"->true , "data" -> data )
	      Ok( result)
	    }
	  }
	}
	
	def get() = Action{ implicit request =>
	 val locationNameList =   (1 to 10).map( i => "location" + i ).toList
	  Ok ( Json.toJson( locationNameList)  )
	}
	
	
}