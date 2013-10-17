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
import models.BagHelp.planFormFmt
import models.BagHelp.planFmt
import models.BagHelp.statusFmt
import models.BagHelp.bagFmt

import models.LocationView
import models.SimpleLocation
import scala.collection.immutable
import org.apache.commons.lang3.RandomStringUtils

object Plans extends Controller {
  private val log = LoggerFactory.getLogger(Bags.getClass())
  private val bagService = base.BagServiceRegistry.bagService
  private val locationService = base.locationFormRegistry.locationService
  private val cmsService = base.CmsServiceRegistry.cmsService

   
  def getUserId( request: Request[AnyContent] ): Option[String] ={
    if( request.getQueryString("forpdf") != None){
       if( request.getQueryString("userId") != None){
         request.getQueryString("userId")
       }else{
    	   request.session.get( "userId")
       	}
    }else{
    		request.session.get( "userId") 
    }
  }
  
  def view() = Action { implicit request =>
    val statusName = request.getQueryString("statusName").getOrElse("")
    val planName = request.getQueryString("planName").getOrElse("")

   getUserId(request)  match {
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
                val planview = PlanView(statusName = statusName, name = plan.name, visible=plan.visible,  
                    startDate = plan.startDate, endDate = plan.endDate,
                  first = first, last = last,
                  map = sortmap , noteMap = plan.noteMap )
                 request.getQueryString("forpdf") match{
                  case None => Ok(views.html.plan("planning", planview , cityListStr ))
                  case Some(x) => Ok(views.html.planforpdf("planning", planview , cityListStr ))
                }
            }
        }
    }
  }

  //更新某一个location 的 note 
  def updateNote() = Action { implicit request =>
    val postData = request.body.asFormUrlEncoded

    val statusName = postData.flatMap(m => m.get("statusName").flatMap(seq => seq.headOption)).getOrElse("")
    val planName = postData.flatMap(m => m.get("planName").flatMap(seq => seq.headOption)).getOrElse("")
   val datestr = postData.flatMap(m => m.get("datestr").flatMap(seq => seq.headOption)).getOrElse("")
    val note = postData.flatMap(m => m.get("note").flatMap(seq => seq.headOption)).getOrElse("")

    log.debug( "statusName={},planName={}, datestr={},  note={} " ,statusName ,planName ,  datestr , note )
    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) =>
        val bagId = userId
        log.debug("bagId={}", bagId)
        bagService.get(bagId) match {
          case None =>
            Ok(Json.obj("success" -> false, "msg" -> "bag 不存在"))
          case Some(bag) =>
            val planOption = for {
              status <- bag.map.get(statusName)
              plan <- status.map.get(planName)
            } yield {

             
              val noteMap = plan.noteMap  +  ( datestr -> note )
 
              log.debug("plan ={}", Json.prettyPrint(Json.toJson(plan)))

              val changedPlan = plan.copy(noteMap = noteMap)

              log.debug("changedPlan={}", Json.prettyPrint(Json.toJson(changedPlan)))

              val changedStatus = status.copy(map = status.map + (changedPlan.name -> changedPlan))

              log.debug("changedStatus={}", Json.prettyPrint(Json.toJson(changedStatus)))

              val changedBag = bag.copy(map = bag.map + (changedStatus.name -> changedStatus))
              bagService.update(changedBag) match {
                case None =>
                  Ok(Json.obj("success" -> false, "msg" -> "update bag error "))
                case Some(updatedbag) =>
                  Ok(Json.obj("success" -> true))
              }

            }

            planOption.getOrElse(Ok(Json.obj("success" -> false, "msg" -> "plan  不存在")))

        }
    }

  }
  /**
   * 时间线 更新
   */
  def updateDate(statusName: String, planName: String, startDate: Long, endDate: Long) = Action { 
    implicit request =>
    session.get("userId") match {
      case None => NotFound
      case Some(userId) =>
        val bagId = userId
        log.debug("bagId={}", bagId)
        bagService.get(bagId) match {
          case None =>
            Ok(Json.obj("success" -> false, "msg" -> "bag 不存在"))
          case Some(bag) =>
            val planOption = for {
              status <- bag.map.get(statusName)
              plan <- status.map.get(planName)
            } yield {
              if (startDate > 0 && endDate > 0 && endDate >= startDate &&
                (plan.startDate != startDate || plan.endDate != endDate)) {
                val newplan = plan.copy(startDate = startDate, endDate = endDate)
                val newstatus = status.copy(map = status.map + (newplan.name -> newplan))
                val newbag = bag.copy(map = bag.map + (newstatus.name -> newstatus))
                bagService.update(newbag) match {
                  case None => Ok(Json.obj("success" -> false, "msg" -> " update newbag ERROR"))
                  case Some(updatedBag) => Ok(Json.obj("success" -> true, "msg" -> ""))
                }

              } else {
                Ok(Json.obj("success" -> true, "msg" -> "no change"))
              }
            }
            planOption.getOrElse(Ok(Json.obj("success" -> false, "msg" -> " plan not exists")))

        }
    }
  }

  /**
   * 可见状态更新
   * visible public or private 
   */
  def updateVisible(statusName: String, planName: String, visible: String) = Action {
    implicit request =>
      session.get("userId") match {
        case None => NotFound
        case Some(userId) =>
          val bagId = userId
          log.debug("bagId={}", bagId)
          val jsVal = bagService.updatePlanVisible(bagId , statusName ,planName , visible  )
          Ok( jsVal)
      }
  }
  
  
  def update() = Action { implicit request =>
    val postData = request.body.asFormUrlEncoded
    val statusName = postData.flatMap(m => m.get("statusName").flatMap(seq => seq.headOption)).getOrElse("")
    val planName = postData.flatMap(m => m.get("planName").flatMap(seq => seq.headOption)).getOrElse("")
    val startDate =  postData.flatMap(m => m.get("startDate").flatMap(seq => seq.headOption)).getOrElse("0").toLong
    val endDate =   postData.flatMap(m => m.get("endDate").flatMap(seq => seq.headOption)).getOrElse("0").toLong
   
    val data = postData.flatMap(m => m.get("data").flatMap(seq => seq.headOption)).getOrElse("{}")
    val tmpList = Json.parse(data).asOpt[List[PlanForm]].getOrElse(Nil)
    val planFormList = tmpList.filter(p => !p.name.contains("t-00_no-assign")).map(p => p.copy(name = p.name.replace("active", "").trim()))

    session.get("userId") match {
      case None => Redirect(routes.Login.login())
      case Some(userId) =>
        val bagId = userId
        log.debug("bagId={}", bagId)
        bagService.get(bagId) match {
          case None =>
            Ok(Json.obj("success" -> false, "msg" -> "bag 不存在"))
          case Some(bag) =>
            val planOption = for {
              status <- bag.map.get(statusName)
              plan <- status.map.get(planName)
            } yield {

             
              val simplelocationList = plan.list
              def exists(id: String): Boolean = {
                simplelocationList.exists(p => p.id == id)
              }
              val map = planFormList.map { planform =>
                val name = planform.name
                val idList = planform.list

                name -> (idList.filter(id => exists(id))).mkString( ";")

              }.toMap.filter( kv => !kv._2.isEmpty() )

              log.debug("plan ={}", Json.prettyPrint(Json.toJson(plan)))
              val changedPlan = if( startDate > 0 && endDate >0 && endDate >= startDate ){
                plan.copy(map = map , startDate = startDate , endDate = endDate )
              } else { 
                plan.copy(map = map)
              }

              log.debug("changedPlan={}", Json.prettyPrint(Json.toJson(changedPlan)))

              val changedStatus = status.copy(map = status.map + (changedPlan.name -> changedPlan))

              log.debug("changedStatus={}", Json.prettyPrint(Json.toJson(changedStatus)))

              val changedBag = bag.copy(map = bag.map + (changedStatus.name -> changedStatus))
              bagService.update(changedBag) match {
                case None =>
                  Ok(Json.obj("success" -> false, "msg" -> "update bag error "))
                case Some(updatedbag) =>
                  Ok(Json.obj("success" -> true))
              }

            }

            planOption.getOrElse(Ok(Json.obj("success" -> false, "msg" -> "plan  不存在")))

        }
    }

  }

  /**
   * 保存并 输出 PDF 
   * 1 这里的保存 实际是不需要的， 在每一步操作的时候，已经保存
   * 2 实现pdf 输出
   * a 生成 url 
   * b 调用 shell , phantomjs 输出 pdf 文件
   * c 将 pdf 文件输出到 客户端
   * d 这里的 pdf 文件， 不能让用户直接访问到， 以后 是否需要考虑 缓存?
   */
  def  outpdf(  statusName: String , planName: String )= Action {implicit request =>

    session.get("userId") match {
      	case None => Redirect(routes.Login.login())
      	case Some(userId) =>
      	  val name = session.get("username").getOrElse("")
      	  val url = "http://www.diandidian.com/plan/?statusName=" + URLEncoder.encode( statusName,"utf-8") +
      	  "&planName=" +  URLEncoder.encode( planName,"utf-8") +
      	  "&forpdf=true&userId="+ userId +"&name=" + URLEncoder.encode( name ,"utf-8")
      	  log.debug("url={}", url )
      	  val randomstr = RandomStringUtils.randomAlphanumeric(4) ;
      	  val  filename = "/opt/phantomjs/tmp/" + userId +  randomstr   + ".pdf"
      	  val cmdSeq = Seq("/opt/phantomjs/bin/phantomjs","--debug=true", "--disk-cache=true", 
      	      "/opt/phantomjs/js/rasterize.coffee" ,     url   , filename, "A4" )
      	  log.debug( "cmdSeq={}", cmdSeq.mkString( " " ))
      	  import scala.sys.process._
      	  if(  cmdSeq.! == 0 )  {
      	     Ok(Json.obj("success"->true, "data"->Json.obj("randomstr"-> randomstr)))
      	  }else{
      	     Ok(Json.obj("success"->false,"msg" -> " 生成PDF失败"))
      	  }
 	  }
  }
  
  //发送pdf 文件
  def sendfile( randomstr: String ) = Action{ implicit request => 
  	 session.get("userId") match {
      	case None => Redirect(routes.Login.login())
      	case Some( userId) =>
      	  val  filename = "/opt/phantomjs/tmp/" + userId +  randomstr   + ".pdf"
      	  Ok.sendFile(
      			  content = new java.io.File(filename),
      			  fileName = _ => randomstr + ".pdf" ,
      			  inline = true
      		)
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
  
  /**
   * map 页面 弹出的div css 调试
   */
  def testmap() =Action {
    Ok( views.html.maptest( "xxx" ))
  }
}