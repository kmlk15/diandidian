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

/**
 *
 * 1  未登录用户
 * 2  已登录用户
 * 3  未登录的时候操作过， 转变成登录用户
 * 4  已经在登录用户操作， 又在未登录用户状态下操作，  后有处于登录状态，
 *
 * 背包 整体作为一个 Doc ,
 * Doc { 基础信息， 计划中， 准备去， 已去过， 未分类}
 *
 * 未登录用户， Doc  {   基础信息， 未分类}
 *
 * 登录用户，  某些 地点的集合， 这个集合 整体 存在一个状态 ，  这个状态可以从  未分类 迁移到 计划中
 * 从 计划中， 迁移到  准备去， 最后到 已去过
 *
 * 未登录的  处理比较简单
 *
 * 登录用户的，  加的时候， 加在 哪里？
 * 删除， 已经 计划的 已经去的 等等， 是否允许 删除？ （ 好像应该允许删除的 ， 计划是计划， 实际是实际 ）
 *
 * 删除应该比较简单， 有明确的对象。
 *
 * 加是个 问题， 特别是在登录
 *
 */

object Bags extends Controller {
  val log = LoggerFactory.getLogger(Bags.getClass())
  val bagService = base.BagServiceRegistry.bagService
  val locationService = base.locationFormRegistry.locationService

  val bagIdCookieName = "tmpbagId"

  def add(locationName: String) = Action { implicit request =>
    def userAdd(location: LocationForm): json.JsObject = {

      val data = Json.obj("name" -> location.name, "id" -> location.id.get)
      val result = Json.obj("success" -> true, "data" -> data)
      result
    }

    def anonymousAdd(location: LocationForm): (String, json.JsObject) = {
      val typ = "anonymous"
      val statusName =  defaultStatusName
      val planName =defaultPlanName
      val (bagId, flag) = request.cookies.get(bagIdCookieName) match {
        case None =>
          val bagId = (new ObjectId()).toString()

          //如何 写cookie?

          (bagId, bagService.addLocation(bagId, typ, statusName, planName, location))

        case Some(bagIdcookie) =>
          val bagId = getBagId(bagIdcookie)
          (bagId, bagService.addLocation(bagId, typ, statusName, planName, location))
      }

      if (flag) {
        val data = Json.obj("name" -> location.name, "id" -> location.id.get)
        val result = Json.obj("success" -> true, "data" -> data)
        (bagId, result)
      } else {
        val result = Json.obj("success" -> false, "msg" -> "")
        (bagId, result)
      }

    }

    locationService.getByName(locationName) match {
      case None => Ok(Json.obj("success" -> false, "msg" -> "该地点不存在"))
      case Some(location) => {
        session.get("userId") match {
          case None =>
            val (bagId, result) = anonymousAdd(location)
            Ok(result).withCookies(Cookie(bagIdCookieName, cookieVal(bagId)))
          case Some(userId) => Ok(userAdd(location))
        }
      }
    }

  }

  /**
   * 这里需要做 安全认证
   */
  def getBagId(cookie: Cookie) = {
    cookie.value
  }
  /**
   * 需要增加 cookie 加密
   */
  def cookieVal(bagId: String) = {
    bagId
  }

  def del(locationId: String) = Action { implicit request =>

    def userDel(location: LocationForm): json.JsObject = {

      val data = Json.obj("name" -> location.name, "id" -> location.id.get)
      val result = Json.obj("success" -> true, "data" -> data)
      result
    }

    def anonymousDel(location: LocationForm): json.JsObject = {
       val typ = "anonymous"
       val statusName =  defaultStatusName
       val planName =defaultPlanName
      
      request.cookies.get(bagIdCookieName) match {
        case None => Json.obj("success" -> false, "msg" -> "数据不存在")
        case Some(bagIdcookie) =>
          bagService.removeLocation(getBagId(bagIdcookie), statusName, planName, location)
          val data = Json.obj("name" -> location.name, "id" -> location.id.get)
          val result = Json.obj("success" -> true, "data" -> data)
          result
      }

    }

    val result = locationService.getById(locationId) match {
      case None => Json.obj("success" -> false, "msg" -> "该地点不存在")
      case Some(location) => {
        session.get("userId") match {
          case None => anonymousDel(location)
          case Some(userId) => userDel(location)
        }
      }
    }
    Ok(result)
  }

  def get() = Action { implicit request =>
    val bagId: String = session.get("userId") match {
      case Some(userId) => userId
      case None => request.cookies.get(bagIdCookieName) match {
        case None => ""
        case Some(cookie) => getBagId(cookie)
      }
    }

      bagService.get(bagId) match{
        case None =>  Ok( views.html.bagEmpty ( ))
        case Some(bag) if( bag.isEmpty) => Ok( views.html.bagEmpty ( ))
        case Some( bag ) if (bag.typ == "user" )=>  Ok( views.html.bagUser( bag))
        case Some( bag ) => Ok( views.html.bagAnonymous( bag ))
        
      }
      
    // 返回  JSON 数据
    // 由页面的脚本 去处理吗？
    // 还是 这里 直接 生成  html 片段？
    // 直接 生成 便于 测试， 不需要 讨厌的  js 生成 html 代码
   
    

  }

}