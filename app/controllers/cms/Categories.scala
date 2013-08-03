package controllers.cms

 
import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue
import play.api.libs.json.Json
import org.slf4j.LoggerFactory

import models.v2.CategoryHelp
import models.v2.Category

object  Categories  extends Controller{
  
   val log = LoggerFactory.getLogger(Category.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def list() = Action { implicit request =>
     val  list = service.getCategoryList
     
     Ok( views.html.cms.category( list ))
    
   }
 
   def add( ) = Action { implicit request =>
     
     Ok(views.html.cms.categoryEdit (None ,  CategoryHelp.form )(session ))
     
   }
   
   def edit(id: String ) =  Action  { implicit request =>
       service.getCategoryById(id)match{
         case None => NotFound
         case Some( category) =>   Ok(views.html.cms.categoryEdit (Some(category.id) ,  CategoryHelp.form.fill( category) )(session ))
       }
     
   
     
   }
   
   
  def save () = Action { implicit request =>
   
     
    
     CategoryHelp.form.bindFromRequest.fold(
      errors => Ok(views.html.cms.categoryEdit( None, errors)),
      category => {
    	  		   service.saveCategory( category)  match{
    	  		     case None => Ok(views.html.cms.categoryEdit( None, CategoryHelp.form.fill(category) , "同样 名字的分类已经存在 "))
    	  		     case Some( u ) => Redirect( routes.Categories.list )
    	  		   }
    	  		
    	  		
        	 
      }
       )
    
   } 
  
  def update(id: String) = Action { implicit request =>
     CategoryHelp.form.bindFromRequest.fold(
      errors => Ok(views.html.cms.categoryEdit( Some( id), errors)),
      category => {
        
    	  	  service.updateCategory(category.copy(id = id ))  match{
    	  	    case None =>Ok(views.html.cms.categoryEdit( Some( id ),  CategoryHelp.form.fill(category) , "同样 名字的分类已经存在"))
    	  	    case Some (user) => Redirect( routes.Categories.list )
    	  	  }
    	  		
        	 
      }
       )
   } 
  
  def del( id: String  ) =  Action {
     
      service.delCategoryById(  id )
      
      Redirect( routes.Categories.list )
   }
  
 
   

 
 
   
}