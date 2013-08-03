package controllers.cms

 
import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue
import play.api.libs.json.Json
import org.slf4j.LoggerFactory

import models.v2.PhotoUserHelp
import models.v2.PhotoUser

object  Users  extends Controller{
  
   val log = LoggerFactory.getLogger(Users.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def list() = Action { implicit request =>
     val users = service.getPhotoUsers 
     
     Ok( views.html.cms.users( users ))
    
   }
 
   def add( ) = Action { implicit request =>
     
     Ok(views.html.cms.userEdit (None ,  PhotoUserHelp.form )(session ))
     
   }
   
   def edit(id: String ) =  Action  { implicit request =>
       service.getPhotoUserById(id)match{
         case None => NotFound
         case Some(user) =>   Ok(views.html.cms.userEdit (Some(user.id) ,  PhotoUserHelp.form.fill(user) )(session ))
       }
     
   
     
   }
   
   
  def save () = Action { implicit request =>
   
     
    
     PhotoUserHelp.form.bindFromRequest.fold(
      errors => Ok(views.html.cms.userEdit( None, errors)),
      user => {
    	  		   service.savePhotoUser(user)  match{
    	  		     case None => Ok(views.html.cms.userEdit( None, PhotoUserHelp.form.fill(user) , "同样 UserId 的用户已经存在 "))
    	  		     case Some( u ) => Redirect( routes.Users.list )
    	  		   }
    	  		
    	  		
        	 
      }
       )
    
   } 
  
  def update(id: String) = Action { implicit request =>
     PhotoUserHelp.form.bindFromRequest.fold(
      errors => Ok(views.html.cms.userEdit( Some( id), errors)),
      user => {
        
    	  	  service.updatePhotoUser(user.copy(id = id ))  match{
    	  	    case None =>Ok(views.html.cms.userEdit( Some( id ), PhotoUserHelp.form.fill(user) , "同样 UserId 的用户已经存在 "))
    	  	    case Some (user) => Redirect( routes.Users.list )
    	  	  }
    	  		
        	 
      }
       )
   } 
  
  def del( id: String  ) =  Action {
      val user =  service.getPhotoUserById( id  ) 
      service.delPhotoUserById(  id )
      //outJson(user)
      Redirect( routes.Users.list )
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
         val json = Json.obj( "success" -> true , "data" ->Json.toJson(user)(PhotoUserHelp.photoUserFmt))
         Ok ( json )
       }
       case None => {
         val  json = Json.obj( "success" -> false)
         Ok( json )
       }
     }
     
   }
   
}