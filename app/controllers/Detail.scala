package controllers

import play.api.Logger
import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import models.LocationJsonHelp.locationFmt
import models.LocationFormHelp.locationFormFmt
import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import java.net.URLEncoder


object Detail extends Controller   {
	val log = LoggerFactory.getLogger(Detail.getClass())
	//val locationService = base.locationRegistry.locationService
 
      val locationService = base.locationFormRegistry.locationService
   // val locationService = locationRegistry.locationService
      

  def view(name: String ) = Action { implicit request =>{
    Logger.debug("name=" + name )
    
    Ok( views.html.detailview( name ))
  }
  }
  
  def viewJson(name: String) = Action {
    Logger.debug("name=" + name)
    /**
     * name 不适合作为主键
     * 需要将 数据保存到 mongodb 中
     *
     */
    locationService.getByName(name) match{
       case Some(location) => {
         val jsVal  = Json.toJson( location).as[ json.JsObject]
        val path =   json.JsPath \ "pictures"
         if(  path(jsVal) .isEmpty   ){
          val jsVal2 =  jsVal.++ (  Json.obj( "pictures" ->
          Json.obj( "thumbnail" -> Json.toJson( List[String]()) ,   
            "planning" ->  ("http://diandidian.s3-us-west-1.amazonaws.com/" + location.photo.replace("266_", "780_" ) )  ) ,
           "cityhref" -> ("/home?country="+ URLEncoder .encode(location.address.country, "utf-8") + 
               "&city=" +URLEncoder .encode(location.address.city, "utf-8")  ),
           "districthref" -> ("/home?country="+ URLEncoder .encode(location.address.country, "utf-8") + 
               "&city=" +URLEncoder .encode(location.address.city, "utf-8")  +
               "&district=" + URLEncoder .encode(location.address.district, "utf-8")  )
          
          ) )
           log.debug( Json.prettyPrint( jsVal2))
           Ok( jsVal2 ) 
         }else{
           log.debug( Json.prettyPrint( jsVal))
           log.debug( Json.prettyPrint( jsVal \ "pictures"))
             Ok( jsVal )
         }
         
         
         
         }
       case None => { NotFound }
    }
    
    
  }
  
}