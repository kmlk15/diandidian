package controllers.cms


import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue
import play.api.libs.json.Json
import org.slf4j.LoggerFactory

import models.LocationFormHelp
import models.LocationFormHelp._
import models.LocationJsonHelp._

import models.LocationForm
import models.Location
import play.api.libs.json.Json

object Locations extends Controller{
  
   val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def list() = Action { implicit request =>
     
     
     Ok( "list" )
    
   }
 
   def add( ) = Action { implicit request =>
     
     Ok(views.html.cms.locationEdit (None ,  form )(session ))
     
   }

   
   def save = Action{ implicit request =>
     
      LocationFormHelp.form.bindFromRequest.fold(
      errors => { 
        log.debug(errors.toString)
        Ok(views.html.cms.locationEdit( None, errors)) 
      },
      locationForm => {
        
    	  		   Ok( Json.prettyPrint( Json.toJson(locationForm)) )
      }
       )
       
   }
   
   def edit(id: String ) = TODO
   
   def update(id: String ) = TODO
   
   def del(id : String ) = TODO
   
   
}