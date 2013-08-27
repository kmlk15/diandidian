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
import models.CategoryForm
import models.LocationForm
import models.Location
import play.api.libs.json.Json

object Locations extends Controller  with AuthTrait  {

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def categoryList = service.getCategoryList( )
  
  def createCategoryForm(categroyId: String):  CategoryForm = {
    if( categroyId ==""){
      CategoryForm("","","")
    }else{
       service.getCategoryById( categroyId)match{
         case None =>  CategoryForm("","","")
         case Some(c ) => if( c.parentId != "" ){      
           service.getCategoryById(c.parentId) match{
             case None => CategoryForm("","","")
             case Some( p) => CategoryForm(categroyId, p.name, c.name)
           }
         }else{ CategoryForm(categroyId,   c.name ,"")  }
      }
      
    }
    
  }
  
  def list() =  isAuthenticated { username => implicit request =>
  val list = service.getLocationList
    Ok(views.html.cms.locations( list ) )

  }

  def add() =  isAuthenticated { username => implicit request =>
  	implicit val cList = categoryList
    Ok(views.html.cms.locationEdit(None, form))

  }

  def save = isAuthenticated { username =>implicit request =>
  	implicit val cList = categoryList
    LocationFormHelp.form.bindFromRequest.fold(
      errors => {
        log.debug("save error={} " , errors)
        Ok(views.html.cms.locationEdit(None, errors))
      },
      locationForm => {

         service.saveLocation(locationForm.copy( category = createCategoryForm( locationForm.category.categoryId ))) match {
          case None => Ok(views.html.cms.locationEdit(None, LocationFormHelp.form.fill(locationForm), "同样 名字的地点已经存在 "))
          case Some(u) => Redirect(routes.Locations.list)
        }
      })

  }

  def edit(id: String) =  isAuthenticated { username => implicit request =>
    implicit val cList = categoryList
    service.getLocationById(id) match {
      case None => NotFound
      case Some(location) => Ok(views.html.cms.locationEdit(location.id, LocationFormHelp.form.fill(location)))
    }
  }

  def update(id: String) =  isAuthenticated { username => implicit request =>
    service.getLocationById(id) match {
      case None => NotFound
      case Some(orignLocation) => {
        implicit val cList = categoryList
        LocationFormHelp.form.bindFromRequest.fold(
          errors => {
            log.debug("update error={} " , errors)
            Ok(views.html.cms.locationEdit(Some(id), errors))
          },
          location => {
        	  	val updateLocation =if(location.category.categoryId == orignLocation.category.categoryId  ) {
        	  	  location.copy( id =orignLocation.id , photo = orignLocation.photo ,category =  orignLocation.category )
        	  	}else{
        	  	   location.copy( id =orignLocation.id , photo = orignLocation.photo ,category =  createCategoryForm(  location.category.categoryId ))
        	  	}
            service.updateLocation( updateLocation ) match {
              case None => Ok(views.html.cms.locationEdit(Some(id), LocationFormHelp.form.fill(location), "同样 名字的地点已经存在"))
              case Some(user) => Redirect(routes.Locations.list)
            }

          })
      }
    }
  }

  def del(id: String) =  isAuthenticated { username =>implicit request =>

    service.delLocationById(id)
    Redirect(routes.Locations.list)

  }

}