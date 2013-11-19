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
import play.api.mvc.AnyContent
import org.apache.commons.lang3.StringUtils
import models.Photo
import models.PlanView
import models.PlanForm
import models.SharePlan
import models.ShareLocation
import models.BagUpdateFromto

import models.BagHelp.planFormFmt
import models.BagHelp.planFmt
import models.BagHelp.statusFmt
import models.BagHelp.bagFmt

import models.LocationView
import models.SimpleLocation
import scala.collection.immutable
import org.apache.commons.lang3.RandomStringUtils

object SharePlans extends Controller with services.FileUploadService {

  val log = LoggerFactory.getLogger(SharePlans.getClass())
  private val bagService = base.BagServiceRegistry.bagService
  private val locationService = base.locationFormRegistry.locationService
  private val cmsService = base.CmsServiceRegistry.cmsService
  /**
   * 分享背包
   * 1 将背包转移到  “分享的背包” 下
   * 2 建立 SharePlan 对象
   * 3 输出 到 SharePlan 管理页面
   *
   */
  def share(planId: String, statusName: String, planName: String) = Action { implicit request =>

    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) => {

        bagService.get(userId) match {
          case Some(bag) =>
            val change = BagUpdateFromto(statusName, planName, "分享的背包", planName)
            val tobag = models.BagHelp.update(bag, change, false)
            if( bag == tobag){
            		InternalServerError("在分享背包下，存在同名背包，请先更改背包的名字！")
            }else{
	            bagService.update(tobag) match {
	              case Some(bag) =>
	                val url = "/plan/editShare?planId=" + planId + "&statusName=" + URLEncoder.encode(change.toStatus, "utf-8") +
	                  "&planName=" + URLEncoder.encode(change.toPlan, "utf-8")
	                Redirect(url)
	              case None => InternalServerError("share update bag ")
	            }
            }
          case None => NotFound
        }
        
      }
    }
  }

  /**
   *  取消分享，返回到 计划中
   *  1 shareplan 的状态， 是否在主页显示，设置为  false
   *   2  将被告 转移到   计划中
   *   3 页面返回到 plan 页面
   */
  def cancelShare(planId: String, statusName: String, planName: String) = Action { implicit request =>

    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) => {
        bagService.get(userId) match {
          case Some(bag) =>
            
            val change = BagUpdateFromto(statusName, planName, "计划中", planName)
            
            val tobag = models.BagHelp.update(bag, change, false)
            if( bag == tobag){
              InternalServerError("在\"计划中\"下，存在同名背包，请先更改\"计划中\"背包的名字！")
            }else{
	            bagService.update(tobag) match {
	              case Some(bag) =>
	                bagService.setSharePlanShareIt(planId, false)
	                val url = "/plan/?statusName=" + URLEncoder.encode(change.toStatus, "utf-8") +
	                  "&planName=" + URLEncoder.encode(change.toPlan, "utf-8")
	                Redirect(url)
	              case None => InternalServerError("share update bag ")
	            }
            }
          case None => NotFound
        }
      }
    }
  }

  /**
   * 生成 share  plan 编辑表单
   */
  def editShare(planId: String) = Action { implicit request =>

    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) => {
        val bagId = userId
        bagService.get(userId) match {
          case None => NotFound
          case Some(bag) =>
            bag.getPlan(planId) match {
              case None => NotFound
              case Some(plan) =>

                val sharePlan: SharePlan = bagService.getSharePlan(planId) match {
                  case None => 
                    log.debug("not found shareplan planid={}", planId)
                    
                    val locationList: List[ShareLocation] = plan.list.flatMap { simplelocation =>
                      locationService.getById(simplelocation.id).map { location =>
                        val address = location.address.city + location.address.district + location.address.street
                        val url = location.url
                        ShareLocation(id = location.id.get, name = location.name, address = address, url = url ,
                        country = location.address.country , province = location.address.stateProvince, city = location.address.city )
                      }
                    }
                    val sharePlan = SharePlan(id = plan.id, name = plan.name, bagId = bag.id,
                      locationList = locationList,
                      userId = userId,
                      usertype = session.get("usertype").getOrElse(""),
                      username = session.get("username").getOrElse(""),
                      avatar = session.get("avatar").getOrElse(""))
                      bagService.updateSharePlan(sharePlan)
                      
                    sharePlan

                  case Some(sharePlan) =>
                    //考虑了 地点的 变动， 删除了地点，增加了地点
                    val locationMap = sharePlan.locationList.map(location => (location.id, location)).toMap

                    val locationList: List[ShareLocation] = plan.list.flatMap { simplelocation =>
                      locationService.getById(simplelocation.id).map { location =>
                        val address = location.address.city + location.address.district + location.address.street
                        val url = location.url
                        locationMap.get(location.id.get).map(sharelocation =>
                          sharelocation.copy(name = location.name, address = address, url = url,
                              country = location.address.country , province = location.address.stateProvince, city = location.address.city )).
                          getOrElse(ShareLocation(id = location.id.get, name = location.name, address = address, url = url,
                              country = location.address.country , province = location.address.stateProvince, city = location.address.city ))
                      }
                    }
                    val sharePlan2 = sharePlan.copy(name = plan.name,
                      locationList = locationList,
                      avatar = session.get("avatar").getOrElse(""))
                      if( sharePlan2 != sharePlan){
                        bagService.updateSharePlan(sharePlan2)
                      	}
                    sharePlan2
                }

                Ok(views.html.shareplanEdit("planning", sharePlan, ""))

            }
        }
      }
    }

  }

  /**
   * 浏览 share  plan
   */
  def viewShare(planId: String) = Action { implicit request =>

    bagService.getSharePlan(planId) match {
      case None => NotFound
      case Some(sharePlan) =>

        val flag: Boolean = if (sharePlan.shareIt && sharePlan.atHomePage) {
          true
        } else if (Some(sharePlan.userId) == session.get("userId")) {
          true
        } else if (session.get("cmsAdmin") != None) {
          true
        } else {
          false
        }
        if (flag) {
          Ok(views.html.shareplanView("planning", sharePlan, ""))
        } else {
          NotFound
        }
    }
  }

  def listShare() = Action { implicit request =>
     val country =  request.getQueryString("country").getOrElse("")
    val province=  request.getQueryString("province").getOrElse("")
    val city = request.getQueryString("city").getOrElse("")
    
    val shareplanList =if(city!="") {
      bagService.getHomePageSharePlanList(  city=city )
    }else if(province != "" ){
      bagService.getHomePageSharePlanList(  province=province )
    }else if( country != ""){
      bagService.getHomePageSharePlanList(  country=country )
    }else{
      bagService.getHomePageSharePlanList(   )
    }
     
    import models.BagHelp.sharePlanFmt
    val jsvalList = shareplanList.map( plan => {
      Json.obj("name" -> plan.name , "img" -> plan.homepageimg, "id"-> plan.id,
          "avatar" -> plan.avatar, "username" -> plan.username )
    })
    val result = Json.obj("success" -> true, "data" -> Json.toJson(jsvalList))
    Ok(result)
  }

  /**
   * 更新 share plan 数据
   * 这里 考虑 只更新文本数据
   */
  def updateShare(planId: String) = Action { implicit request =>
    val postData = request.body.asFormUrlEncoded
    def getNote(id: String): String = postData.flatMap(m => m.get("note_" + id).flatMap(seq => seq.headOption)).getOrElse("")

    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) => {
        val bagId = userId
        bagService.get(userId) match {
          case None => NotFound
          case Some(bag) =>
            bag.getPlan(planId) match {
              case None => NotFound
              case Some(plan) =>

                val (sharePlan , status):( SharePlan, String) = bagService.getSharePlan(planId) match {
                  case None =>
                    val locationList: List[ShareLocation] = plan.list.flatMap { simplelocation =>
                      locationService.getById(simplelocation.id).map { location =>
                        val address = location.address.city + location.address.district + location.address.street
                        val url = location.url
                        ShareLocation(id = location.id.get, name = location.name, address = address, url = url, note = getNote(location.id.get))
                      }
                    }
                  val sharePlan = SharePlan(id = plan.id, name = plan.name, bagId = bag.id,
                  locationList = locationList,
                  userId = userId,
                  usertype = session.get("usertype").getOrElse(""),
                  username = session.get("username").getOrElse(""),
                  avatar = session.get("avatar").getOrElse(""))
                     
                 (sharePlan , "new")
                  
                  case Some(sharePlan) =>
                    //考虑了 地点的 变动， 删除了地点，增加了地点
                    val locationMap = sharePlan.locationList.map(location => (location.id, location)).toMap

                    val locationList: List[ShareLocation] = plan.list.flatMap { simplelocation =>
                      locationService.getById(simplelocation.id).map { location =>
                        val address = location.address.city + location.address.district + location.address.street
                        val url = location.url
                        locationMap.get(location.id.get).map(sharelocation => sharelocation.copy(name = location.name, address = address, url = url, note = getNote(location.id.get))).
                          getOrElse(ShareLocation(id = location.id.get, name = location.name, address = address, url = url, note = getNote(location.id.get)))
                      }
                    }
                    
                      val sharePlan2 = sharePlan.copy(name = plan.name,
                      locationList = locationList,
                      avatar = session.get("avatar").getOrElse(""))

                    (sharePlan2, if(sharePlan2==sharePlan) "same" else "update")
                    
                }
                
                if(status=="same"){
                  log.debug("没有任何改变， 不需要更新")
                  	Ok( Json.obj("success"-> true , "msg" -> ""))
                }else{
                   bagService.updateSharePlan(sharePlan) match {
                   	case None => Ok( Json.obj("success"-> false , "msg" -> "内部错误"))
                   	case Some(x) => Ok( Json.obj("success"-> true , "msg" -> "reload"))
                   }
                }
            }
        }
      }
    }

  }

  /**
   * 设置状态为  允许share , 这个 可能是多余的
   * 主要是 为了  能够 出现在 CMS 中， 或者 说明  这个 share 的内容 已经完整了
   */
  def shareIt(planId: String) = Action { implicit request =>
    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) => {
        val bagId = userId
        bagService.get(userId) match {
          case None => NotFound
          case Some(bag) =>
            bag.getPlan(planId) match {
              case None => NotFound
              case Some(plan) =>
                bagService.getSharePlan(planId) match{
                  case None => NotFound
                  
                  case Some( share) if (!share.readyToShare) =>
                    	Ok(Json.obj("success" -> false, "msg" -> "请将内容输入完整，所有的地点需要有图片和文字说明。"))
                  case Some( share) if (share.readyToShare) =>
                    bagService.setSharePlanShareIt(planId, true) match {
                    case None => Ok(Json.obj("success" -> false, "msg" -> "发生内部错误！"))
                    case Some(x) => Ok(Json.obj("success" -> true, "msg" -> "分享成功！"))
                    }
                }
            }
        }
      }
    }
  }

  def shareplanCoverImage(planId: String) = Action { implicit request =>
    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) => {
        bagService.getSharePlan(planId) match {
          case Some(plan) if (plan.bagId == userId) =>
            Ok(views.html.shareplanCoverImage(plan, ""))
          case _ => NotFound
        }
      }
    }
  }

  def shareplanCoverImageSave(planId: String) = Action { implicit request =>
    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) => {
        bagService.getSharePlan(planId) match {
          case Some(plan) if (plan.bagId == userId) =>

            import org.bson.types.ObjectId
            val id = new ObjectId().toString
            val imgId = id
            //TODO 需要专门 写一个 方法， 处理 这一类图片的上传， 这个和上传到 首页 还是不同的

            val extension: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseDetailPageFile(_, imgId) }).getOrElse(NotUpload)
            if (extension == NotUpload) {
              Ok(views.html.shareplanCoverImage(plan, msg = "必须上传图片"))
            } else {
              val updatePlan = plan.copy(imgId = imgId, extension = extension)
              bagService.updateSharePlan(updatePlan);
              Ok(views.html.shareplanCoverImageCallback(updatePlan))
            }
          case _ => NotFound
        }
      }
    }
  }

  def shareplanLocationImage(planId: String, locationId: String) = Action { implicit request =>
    session.get("userId") match {
      case None =>
        log.debug("login please")
        Redirect(routes.Login.login())
      case Some(userId) => {
        bagService.getSharePlan(planId) match {
          case Some(plan) if (plan.bagId == userId) =>
            log.debug("是自己 分享的 plan ")
            plan.locationList.filter(location => location.id == locationId).headOption match {
              case None => NotFound
              case Some(location) =>
                Ok(views.html.shareplanLocationImage(plan, location, ""))
            }
          case _ => NotFound
        }
      }
    }
  }

  def shareplanLocationImageSave(planId: String, locationId: String) = Action { implicit request =>
    session.get("userId") match {
      case None =>
        log.debug("login please")
        Redirect(routes.Login.login())
      case Some(userId) => {
        bagService.getSharePlan(planId) match {
          case Some(plan) if (plan.bagId == userId) =>
            log.debug("是自己 分享的 plan ")
            plan.locationList.filter(location => location.id == locationId).headOption match {
              case None => NotFound
              case Some(location) =>
                import org.bson.types.ObjectId
                val id = new ObjectId().toString
                val imgId = id
                //TODO 需要专门 写一个 方法， 处理 这一类图片的上传， 这个和上传到 首页 还是不同的

                val extension: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseHomePageFile(_, imgId) }).getOrElse(NotUpload)
                if (extension == NotUpload) {
                  Ok(views.html.shareplanLocationImage(plan, location, msg = "必须上传图片"))
                } else {

                  val locaiontListUpdate = plan.locationList.map(location => {
                    if (location.id == locationId) {
                      if (location.imgId != "") {
                        log.debug("存在图片，需要删除 imgId = {}", location.imgId)
                        removeFile(location.img)
                      }
                      location.copy(imgId = imgId, extension = extension)
                    } else {
                      location
                    }
                  })

                  bagService.updateSharePlan(plan.copy(locationList = locaiontListUpdate));

                  Ok(views.html.shareplanLocationImageCallback(location.copy(imgId = imgId, extension = extension)))
                }
            }
          case _ => NotFound
        }
      }
    }
  }

}