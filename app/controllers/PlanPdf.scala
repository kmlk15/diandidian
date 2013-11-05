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
import models.PlanForm
import models.BagHelp.planFormFmt
import models.BagHelp.planFmt
import models.BagHelp.statusFmt
import models.BagHelp.bagFmt
import models.LocationView
import models.SimpleLocation
import scala.collection.immutable

 

object PlanPdf extends Controller {
  private val log = LoggerFactory.getLogger(Bags.getClass())
  private val bagService = base.BagServiceRegistry.bagService
  private val locationService = base.locationFormRegistry.locationService
  private val cmsService = base.CmsServiceRegistry.cmsService
  
  
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
                
                val allLocationViewList : List[LocationView] = simplelocationList2LocationViewList(plan.list)
                 //从这里  计算出所有的 city 信息， 用于传递到 /home 进行搜索
                val cityListStr = allLocationViewList.map( _.location.address.city).distinct.mkString(",")
                 
                val allLocationViewMap :Map[Option[String], LocationView] = allLocationViewList.map( view => view.location.id -> view ).toMap
                
                log.debug( "allLocationViewList.size={}" , allLocationViewList.size )
                
                /**
                 * 变换 已经安排的数据
                 * 这里 有可能实际已经删除该地点，
                 * 但是 安排的 map 里 还有数据， 需要在 变换后 删除 空地点列表的  日期 
                 * 这里还存在一个问题， 由于删除的不同步，会出现 ，一个地点出现在2个 日期中
                 * 所以还是需要 在 删除的时候 处理这个问题
                 */
                val locationViewMap: Map[String, List[LocationView]] = plan.map.map { kv =>
                  val idList  = kv._2.split(";").toList
                  log.debug("{} -> {}", kv._1 , idList , "" )
                  kv._1 ->  idList.flatMap( id =>allLocationViewMap.get( Some(id) )   )
                }.filter( kv => !kv._2.isEmpty )
                
                
                val tmpSet = plan.map.filter( kv => kv._1 != "t-00_no-assign").flatMap(kv => kv._2.split(";")).toSet
                log.debug("tmpSet={}", tmpSet)
                
                val freeLocationList = plan.list.filter(p => !tmpSet.contains(p.id))
                log.debug("freeLocationList={}", freeLocationList)
                
                //和 未安排的数据合并
                val locationViewMap2 = if (freeLocationList.isEmpty) {
                  locationViewMap
                } else {
                  val idList = freeLocationList.map(p => p.id)
                    log.debug("t-00_no-assign idList={}",idList )
                  locationViewMap + ("t-00_no-assign" -> (   idList.flatMap( id =>allLocationViewMap.get( Some(id) )  ) ))
                }

                val sortmap = immutable.SortedMap[String, List[LocationView]]() ++ locationViewMap2

                val first = sortmap.headOption.map(kv => kv._1).getOrElse("")
                val last = sortmap.lastOption.map(kv => kv._1).getOrElse("")
                val planview = PlanView(id = plan.id , statusName = statusName, name = plan.name, visible=plan.visible,  startDate = plan.startDate, endDate = plan.endDate,
                  first = first, last = last,
                  map = sortmap)
                  //如何输出PDF文件， 生成 静态文件？ 用 ByteArray  ?
                  //需要读取 s3 上的文件 
                  
                 Ok(views.html.plangmap("planning", planview , cityListStr ))
               
            }
        }
    }
  }

  private  def simplelocationList2LocationViewList(list: List[SimpleLocation]) : List[LocationView]= {
     list.flatMap{ simple =>
          locationService.getById(simple.id).map{ location =>
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
      LocationView(location, photo,  simple.note  )
       }
       
     }
   
  }
}