package controllers

import play.api.Logger
import play.api.libs.json

import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue


object Detail extends Controller   {

 val locationService = base.locationRegistry.locationService
 

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
       
         val str = JsValue.toJson(JsonSerialization.tojson(location))
         
         Ok(  json.Json.parse(str)  ) 
         }
       case None => { NotFound }
    }
    
    
  }
  
}