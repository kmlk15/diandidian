package models

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import org.slf4j.LoggerFactory
import scala.collection.immutable
import play.api.libs.json
import org.bson.types.ObjectId
import models.v2.BaseUser

/**
 * SimpleLocation ,  根据 planning 的需求 可能要 有时间数据 和 顺序。
 *
 * plan name , 是由用户定义的 缺省名字 "背包"
 * status name 的值是有限的  计划中，准备前往，过去背包 ; 缺省名字 计划中
 */
case class SimpleLocation(id: String = "", name: String = "", enName: String = "", note: String = "")

/**
 * list 中 有所有的 simplelocation
 * map 中， 是分组后的 simplelocation
 *
 */
case class Plan(id: String=""   , name: String = "背包", startDate: Long = 0L, endDate: Long = 0L,
  list: List[SimpleLocation] = Nil, map: Map[String, String] = Map() , visible: String ="private")

/**
 * 用于页面展示时的对象
 */
case class PlanView(statusName:String = "" , name: String = "", visible: String ="private" , startDate: Long = 0L, endDate: Long = 0L,
  first: String = "", last: String = "", map: immutable.SortedMap[String, List[LocationView]] = immutable.SortedMap()) {
  val pattern = """t-(\d{4})(\d{2})(\d{2})""".r
  val weeknameArr = Array( "", "星期日" , "星期一", "星期二" ,"星期三" ,"星期四" ,"星期五" ,"星期六" )
  def getTtitle(cssClassname: String): String = {
    if (cssClassname == "t-00_no-assign") {
      "尚未安排"
    } else {
      pattern.findFirstMatchIn(cssClassname) match {
        case None => cssClassname
        case Some(m) =>{
          val year = m.group(1)
          val month = m.group(2)
          val date = m.group( 3)
          val week = {
            val cal = java.util.Calendar.getInstance()
            cal.set(year.toInt ,month.toInt - 1,  date.toInt )
            val w = cal.get( java.util.Calendar.DAY_OF_WEEK )
            weeknameArr( w )
          }
          
         month + "月" + date + "日" +" - " + week
        }
      }
    }

  }

}

case class LocationView(location: LocationForm = LocationForm(), photo: Photo = Photo(), note: String = "")

/**
 * 提交的数据格式
 *
 */
case class PlanForm(name: String = "", list: List[String] = Nil)

case class Status(name: String = "计划中", map: Map[String, Plan] = Map())

case class Bag(id: String = "", typ: String = "", usertype: String = ""  ,  map: Map[String, Status] = Map()) {

  lazy val locationList: List[SimpleLocation] = locations()
  lazy val planList: List[Plan] = plans()

  lazy val isEmpty: Boolean = planList.isEmpty

  def locations(): List[SimpleLocation] = {
    val seq = for {
      statusTuple <- map
      planTuple <- statusTuple._2.map
      location <- planTuple._2.list
    } yield {
      location
    }
    seq.toList
  }

  def plans(): List[Plan] = {
    val seq = for {
      statusTuple <- map
      planTuple <- statusTuple._2.map

    } yield {
      planTuple._2
    }
    seq.toList

  }

  def getPlan( id: String ) : Option[Plan] ={
    planList.find(  p => p.id == id )
  }
  
}

//用于 更改名字时， 传递参数 
case class BagUpdateFromto(fromStatus: String = "", fromPlan: String = "", toStatus: String = "", toPlan: String = "")

//locaionId 和 plan 之间的关联关系
//用于通过 locationId 搜索到对应的 plan
//目前，只记录  public 的plan 
//   planName: Option[ String ] = None  , userName: Option[ String ] = None  , avatar: Option[ String ] = None在 实际使用时候 填充 
case class LocationPlanIndex(id: Option[String] =None, locationId: String , 
    bagId: String, planId: String , visible: String="public", 
    plan: Option[ Plan ] = None  , baseUser:Option[BaseUser] = None )


object BagHelp {

  import play.api.libs.json._

  implicit val simpleLocationFmt = Json.format[SimpleLocation]
  implicit val planFmt = Json.format[Plan]
  implicit val statusFmt = Json.format[Status]
  implicit val bagFmt = Json.format[Bag]
  implicit val planFormFmt = Json.format[PlanForm]

  val log = LoggerFactory.getLogger(BagHelp.getClass())
  val defaultPlanName = "背包"
  val defaultStatusName = "计划中"

  val bagUpdateFromtoform = Form {
    mapping(
      "fromStatus" -> text,
      "fromPlan" -> text,
      "toStatus" -> text,
      "toPlan" -> text)(BagUpdateFromto.apply)(BagUpdateFromto.unapply)
  }

  def addLocation(bag: Bag, statusName: String, planName: String, simpleLocationList: List[SimpleLocation]):( Bag ,Option[Plan] )= {
    val newStatus = bag.map.get(statusName) match {
      case None =>
        log.debug("Status 还没有建立，创建新的 Status ")
   
        Status(statusName, Map(planName -> Plan(id = (new ObjectId().toString), name = planName, list = simpleLocationList)))
        
      case Some(status) =>
        val newplan = status.map.get(planName) match {
          case None =>
            log.debug("Plan 还没有建立，创建新的 Plan ")
            Plan(id = (new ObjectId().toString) , name = planName, list = simpleLocationList)
          case Some(plan) =>
            val newList = (simpleLocationList ::: plan.list).distinct
            val newPlan = plan.copy(list = newList)
            newPlan
        }
        val newStatus = status.copy(map = status.map + (newplan.name -> newplan))
        newStatus
    }
    val newBag = bag.copy(map = bag.map + (statusName -> newStatus))
    (newBag,  newStatus.map.get(planName) )
  }

  def removeLocation(bag: Bag, statusName: String, planName: String, 
      simpleLocationList: List[SimpleLocation], removEmptyplan: Boolean = false): (Bag,Option[Plan]) = {
    log.debug("status={}, plan={} , remove location={} , removEmptyplan={}", statusName, planName, simpleLocationList, removEmptyplan.toString)
    bag.map.get(statusName) match {
      case None =>
        log.debug("status  不存在 ")
        (bag,None)
      case Some(status) =>
        status.map.get(planName) match {
          case None =>
            log.debug("plan   不存在 ")
            (bag, None)
          case Some(plan) =>
            log.debug(" plan.list={}", plan.list)
            /**
             * 存在 名字改变的情况，
             * 所以这里要根据  id 来比较
             */
            val simpleLocationIdList = simpleLocationList.map(location => location.id)
            val newList = plan.list.filter(location => !simpleLocationIdList.contains(location.id))

            val newMap = plan.map.map(kv => {
              val idListStr = kv._2.split(";").filter(id => !simpleLocationIdList.contains(id)).mkString(";")
              kv._1 -> idListStr
            }).filter(kv => kv._2 != "")

            if (newList == plan.list && !plan.list.isEmpty) {
              log.debug(" simplelocation    不存在 ")
              (bag, Some(plan))
            } else {
              log.debug(" 删除 location={}", simpleLocationList)
              val newplan = plan.copy(list = newList, map = newMap)
              log.debug(" plan={}", plan)
               log.debug(" newplan={}", newplan)
               
              val newStatus = if (removEmptyplan && newplan.list.isEmpty) {
                status.copy(map = status.map - newplan.name)
              } else {
                status.copy(map = status.map + (newplan.name -> newplan))
              }

              val newBag = if (newStatus.map.isEmpty && statusName != defaultStatusName) {
                bag.copy(map = bag.map - statusName)
              } else {
                bag.copy(map = bag.map + (statusName -> newStatus))
              }
              log.debug("removelocation bag ={}", newBag)
              ( newBag , Some( newplan))
            }
        }

    }
  }

  /**
   * 这个方式实际是
   * removelocation
   * addlocation
   * 的组合！
   *  需要定义好 removelocation 和 addlocation 再来定义  update 方法！
   *  2013-09-11 吴昊
   *  这里的 update 不再 等价于   removelocation 和 addlocation  的组合 ，
   *  在 进行 行程安排后， 不能 简单的 组合操作， 实际 应该是  plan 的 直接 变换
   *
   */
   
  def update2(bag: Bag, change: BagUpdateFromto, isRemove: Boolean = false): Bag = {

    val fromMap = bag.map
    val tobagOption = for {
      fromstatus <- fromMap.get(change.fromStatus)
      fromplan <- fromstatus.map.get(change.fromPlan)
    } yield {
      val addList = fromplan.list
      val removeList = fromplan.list
      log.debug("isRemove={}", isRemove)
      if (isRemove) {
        val bag1 = removeLocation(bag, change.fromStatus, change.fromPlan, removeList, true)._1
        bag1
      } else {
        val bag1 = removeLocation(bag, change.fromStatus, change.fromPlan, removeList, true)._1
        val bag2 = addLocation(bag1, change.toStatus, change.toPlan, addList)._1
        bag2

      }
    }
    tobagOption match {
      case None => bag
      case Some(tobag) => tobag
    }

  }

  /**
   * change 的目标 如果 已经存在， 则不做任何改变
   * 如果要合并， 则需要明确的 调用合并方法！先删除， 再合并
   *  保持不变， 这里 需要有办法 告知 调用者!, 或者要求 调用者在 调用前判断 目标是否已经存在
   */
  def update(bag: Bag, change: BagUpdateFromto, isRemove: Boolean = false): Bag = {

    val fromMap = bag.map
    val tobagOption = for {
      fromstatus <- fromMap.get(change.fromStatus)
      fromplan <- fromstatus.map.get(change.fromPlan)
    } yield {

      log.debug("isRemove={}", isRemove)
      if (isRemove) {
        val tostatus = fromstatus.copy(map = fromstatus.map - fromplan.name)
        val lastbag = bag.copy(map = bag.map + (tostatus.name -> tostatus))
        Some(lastbag)
      } else {

        val toplan = fromplan.copy(name = change.toPlan)
        val bagchanged: Option[Bag] = fromMap.get(change.toStatus) match {
          //目标 status 还不存在 
          case None => {
            val fromstatusChanged = fromstatus.copy(map = fromstatus.map - fromplan.name)
            val tostatus = Status(name = change.toStatus, map = Map(toplan.name -> toplan))
            val lastbag = bag.copy(map = bag.map + (fromstatusChanged.name -> fromstatusChanged) + (tostatus.name -> tostatus))
            Some(lastbag)
          }
          //目标 status 已经存在 
          case Some(tostatus) => {
            /**
             * 如果是 同一个 status中 重命名
             * 如果 已经存在 同名 plan  ， 则不允许  操作
             */
            /*
   	         * 在同一个 status 中操作 
   	         */
            if (fromstatus == tostatus) {
              tostatus.map.get(toplan.name) match {
                case None =>
                  val laststatus = tostatus.copy(map = tostatus.map + (toplan.name -> toplan) - fromplan.name)
                  val lastbag = bag.copy(map = bag.map + (laststatus.name -> laststatus))
                  Some(lastbag)
                case Some(plan) =>
                  log.error("在同一个 status 中 已经存在 同名 plan ")
                  None
              }

            } else {
              tostatus.map.get(toplan.name) match {
                case None =>
                  val fromstatusChanged = fromstatus.copy(map = fromstatus.map - fromplan.name)
                  val laststatus = tostatus.copy(map = tostatus.map + (toplan.name -> toplan))
                  val lastbag = bag.copy(map = bag.map + (laststatus.name -> laststatus) + (fromstatusChanged.name -> fromstatusChanged))
                  Some(lastbag)
                case Some(plan) =>
                  log.error("在不同 status 中 已经存在 同名 plan ")
                  None
              }
            }
          }
        }
        bagchanged

      }
    }
    tobagOption match {
      case None => bag
      case Some(tobag) => tobag match {
        case None => bag
        case Some(lastbag) => lastbag
      }
    }

  }

  def gmap(planview: PlanView): JsObject = {
    val map = planview.map
    val list: List[JsObject] = map.map(kv => {
      val dataList: List[JsObject] = kv._2.map { view =>
        Json.obj(
          "name" -> view.location.name,
          "imgsrc" -> view.photo.detailpageThumbnailImg,
          "address" -> (view.location.address.city + view.location.address.district + view.location.address.street),
          "data" -> planview.getTtitle(kv._1),
          "position" -> (view.location.address.latitude + "," + view.location.address.longitude),
          "timeline" -> "timeline",
          "id" -> view.location.id.get)
      }
      val jsArr = Json.toJson(dataList)
      Json.obj(kv._1 -> jsArr)
    }).toList

    Json.obj()
  }

}