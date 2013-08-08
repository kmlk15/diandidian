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

object Locations extends Controller {

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def categoryList = service.getCategoryList
  
  def list() = Action { implicit request =>
  val list = service.getLocationList
    Ok(views.html.cms.locations( list ) )

  }

  def add() = Action { implicit request =>
  	implicit val cList = categoryList
    Ok(views.html.cms.locationEdit(None, form))

  }

  def save = Action { implicit request =>
  	implicit val cList = categoryList
    LocationFormHelp.form.bindFromRequest.fold(
      errors => {
        log.debug(errors.toString)
        Ok(views.html.cms.locationEdit(None, errors))
      },
      locationForm => {

         service.saveLocation(locationForm) match {
          case None => Ok(views.html.cms.locationEdit(None, LocationFormHelp.form.fill(locationForm), "同样 名字的地点已经存在 "))
          case Some(u) => Redirect(routes.Locations.list)
        }
      })

  }

  def edit(id: String) = Action { implicit request =>
    implicit val cList = categoryList
    service.getLocationById(id) match {
      case None => NotFound
      case Some(location) => Ok(views.html.cms.locationEdit(location.id, LocationFormHelp.form.fill(location)))
    }
  }

  def update(id: String) = Action { implicit request =>
  	implicit val cList = categoryList
    LocationFormHelp.form.bindFromRequest.fold(
      errors => Ok(views.html.cms.locationEdit(Some(id), errors)),
      location => {

        service.updateLocation(location.copy(id = Some(id))) match {
          case None => Ok(views.html.cms.locationEdit(Some(id), LocationFormHelp.form.fill(location), "同样 名字的地点已经存在"))
          case Some(user) => Redirect(routes.Locations.list)
        }

      })
  }

  def del(id: String) = Action {

    service.delLocationById(id)
    Redirect(routes.Locations.list)

  }

}