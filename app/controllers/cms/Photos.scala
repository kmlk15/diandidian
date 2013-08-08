package controllers.cms

import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue
import play.api.libs.json.Json
import org.slf4j.LoggerFactory


import models.Photo
import models.PhotoHelp
import models.PhotoHelp._

import models.v2.PhotoUser


import play.api.libs.json.Json

object Photos  extends Controller {

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def photoUserList = service.getPhotoUsers()
  
  def location( locationId: String)  = service.getLocationById( locationId )
  
  def list(locationId: String ) = Action{ implicit request =>
    location( locationId) match{
      case None => NotFound
      case Some( location ) =>{
        val list = service.getPhotoList(locationId)
        Ok( Json.toJson(list))
      }
    }
    
    
  }
  
  def add(locationId: String ) = Action{implicit request =>
    location( locationId) match{
      case None => NotFound
      case Some( location ) =>{
        implicit val userList = service.getPhotoUsers()
        implicit val locationImpl = location
        Ok( views.html.cms.photoEdit(None , PhotoHelp.form))
      }
    }
  }
  
  def save() = TODO
  
  def edit(id: String) = TODO
  
  def update(id: String ) = TODO
  
  def del(id: String ) = TODO
  
}