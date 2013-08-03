package controllers.cms

 
import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue
import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import models.v2.PhotoUserJson._
import models.v2.PhotoUser

object  Users  extends Controller{
  
   val log = LoggerFactory.getLogger(Users.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def list() = TODO
  
  def save () = TODO 
  
  def update() = TODO 
  
  def del( id: String  ) =  Action {
      val user =  service.getPhotoUserById( id  ) 
      service.delPhotoUserById(  id )
      outJson(user)
     
   }
  
  def get( id: String) = Action{
      val user =  service.getPhotoUserById( id  ) 
      outJson(user)
     
   }
   

  def getByuserName( userName: String) = Action {
      val user =  service.getPhotoUserByUserName( userName ) 
      outJson(user)
     
   }
  
  def getByuserId(userId: String) = Action {
	   val user =  service.getPhotoUserByUserId(userId) 
      outJson(user)
   } 
  
   def  outJson( u: Option[PhotoUser]) = {
     
     u match{
       case Some( user ) =>{
         val json = Json.obj( "success" -> true , "data" ->  user)
         Ok ( json )
       }
       case None => {
         val  json = Json.obj( "success" -> false)
         Ok( json )
       }
     }
     
   }
   
}