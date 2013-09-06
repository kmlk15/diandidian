package controllers

import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import play.api.mvc.Request
import models.LocationForm
import play.api.mvc.Cookie
import org.bson.types.ObjectId
import models.BagHelp.bagFmt
import models.BagHelp.defaultPlanName
import models.BagHelp.defaultStatusName
import models.BagHelp.bagUpdateFromtoform
import models.Bag
import play.api.mvc.Session
import org.apache.commons.lang3.StringUtils
import models.Photo
import models.PlanView
import models.LocationView
import models.SimpleLocation
import scala.collection.immutable

object Plans extends Controller {
  val log = LoggerFactory.getLogger(Bags.getClass())
  val bagService = base.BagServiceRegistry.bagService
  val locationService = base.locationFormRegistry.locationService
  val cmsService = base.CmsServiceRegistry.cmsService

  val bagIdCookieName = "tmpbagId"

  def view() = Action { implicit request =>
    val statusName = request.getQueryString("statusName").getOrElse("")
    val planName = request.getQueryString("planName").getOrElse("")

    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) =>
        val bagId = userId
        bagService.get(bagId) match {
          case None => Ok("请先创建背包")
          case Some(bag) =>
            val planOption = for {
              status <- bag.map.get(statusName)
              plan <- status.map.get(planName)
            } yield {
              plan
            }
            planOption match {
              case None => Ok("Plan 不存在 ")
              case Some(plan) if (plan.list.isEmpty) => Ok("Plan 为空，请先添加地点")
              case Some(plan) =>
                val locationViewList = simplelocationList2LocationViewList(plan.list)
                val locationViewMap: Map[String, List[LocationView]] = plan.map.map(kv =>
                  kv._1 -> simplelocationList2LocationViewList(kv._2))
                val sortmap = immutable.SortedMap[String, List[LocationView]]() ++ locationViewMap

                val planview = PlanView(name = plan.name, beginDate = plan.beginDate, endDate = plan.endDate,
                  list = locationViewList, map = sortmap)

                Ok(views.html.plan("planning" , planview ))
            }

        }

    }

  }
  def simplelocationList2LocationViewList(list: List[SimpleLocation]) = {
    val locationList = list.flatMap(simplelocation => locationService.getById(simplelocation.id))
    locationList.map { location =>
      val photo = if (location.photo.startsWith("266_")) {
        val imgId = StringUtils.substringBetween(location.photo, "266_", ".")
        log.debug("imgId={}", imgId)
        cmsService.getPhotoByImgId(imgId) match {
          case None => Photo()
          case Some(photo) => photo
        }
      } else {
        Photo()
      }
      LocationView(location, photo)
    }
  }
}