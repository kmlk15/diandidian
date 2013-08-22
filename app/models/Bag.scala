package models

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import org.slf4j.LoggerFactory


/**
 * SimpleLocation ,  根据 planning 的需求 可能要 有时间数据 和 顺序。
 * 
 * plan name , 是由用户定义的 缺省名字 "背包"
 * status name 的值是有限的  计划中，准备前往，过去背包 ; 缺省名字 计划中
 */
case class SimpleLocation(id: String="" , name: String ="" , enName: String="" )
case class Plan( name: String = "背包" ,  list: List[SimpleLocation] = Nil)
case class Status( name: String ="计划中", map :Map[String ,Plan] = Map())

case class Bag(id: String = "", typ: String = "", map: Map[String, Status] = Map()) {

  lazy val locationList: List[SimpleLocation] = locations()
  lazy val isEmpty: Boolean = locationList.isEmpty

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

  
} 

case class BagUpdateFromto( fromStatus: String="" , fromPlan: String="" ,toStatus: String="" , toPlan: String="")

object BagHelp {
  
  import play.api.libs.json._
  
  implicit val simpleLocationFmt = Json.format[SimpleLocation]
  implicit val planFmt = Json.format[Plan]
  implicit  val statusFmt = Json.format[Status]
  implicit  val bagFmt = Json.format[Bag]
  
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

  def addLocation(bag: Bag, statusName: String, planName: String, simpleLocationList: List[ SimpleLocation]) : Bag={
    val newStatus = bag.map.get(statusName) match {
            case None =>
              log.debug("Status 还没有建立，创建新的 Status ")
              Status(statusName, Map(planName -> Plan(planName, simpleLocationList)))
            case Some(status) =>
              val newplan = status.map.get(planName) match {
                case None =>
                  log.debug("Plan 还没有建立，创建新的 Plan ")
                  Plan(planName, simpleLocationList )
                case Some(plan) =>
                    val newList =( simpleLocationList ::: plan.list ) .distinct
                    val newPlan = plan.copy(list = newList)
                    newPlan
               }
              val newStatus =  status.copy(map = status.map + (newplan.name -> newplan))
              newStatus
          }
          val newBag = bag.copy(map = bag.map + (statusName -> newStatus))
          newBag
}
  
 def removeLocation(bag: Bag, statusName: String, planName: String, simpleLocationList: List[SimpleLocation]) : Bag={
   log.debug("status={}, plan={} , remove location={}" , statusName, planName , simpleLocationList)
   bag.map.get(statusName) match {
            case None =>
              log.debug("status  不存在 ")
              bag
            case Some(status) =>
              status.map.get(planName) match {
                case None =>
                  log.debug("plan   不存在 ")
                  bag
                case Some(plan) =>
                  log.debug(" plan.list={}" ,  plan.list )
                  val newList = plan.list.filter(  location => !simpleLocationList.contains( location ))

                  if (newList == plan.list) {
                    log.debug(" simplelocation    不存在 ")
                    bag
                  } else {
                    log.debug(" 删除 location={}",simpleLocationList )
                    val newplan = plan.copy(list = newList)
                  val newStatus =   if( newplan.list.isEmpty){
                      status.copy(map = status.map - newplan.name )
                    }else{
                       status.copy(map = status.map + (newplan.name -> newplan))
                    }
                    
                    val newBag = if(newStatus.map.isEmpty ) {
                      bag.copy(map = bag.map -  statusName)
                    }else{
                      bag.copy(map = bag.map + (statusName -> newStatus))
                    }
                    log.debug("removelocation bag ={}", newBag)
                    newBag
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
   *  
   */
  def update(bag: Bag, change: BagUpdateFromto): Bag = {
    
    val fromMap = bag.map
    val tobagOption = for{
      fromstatus <- fromMap.get(change.fromStatus)
      fromplan <- fromstatus.map.get(change.fromPlan)
    }yield{
    val addList = fromplan.list
    val removeList = fromplan.list
    val bag1 = removeLocation( bag , change.fromStatus,change.fromPlan , removeList)
    val bag2 = addLocation( bag1 , change.toStatus,change.toPlan , addList )
    
    bag2 
//    val toplan = fromplan.copy(name = change.toPlan)
//    //从原来的 status 中移除
//    val fromstatusChanged: models.Status = fromstatus.copy(map = fromstatus.map - change.fromPlan)
//    //更新到 新的 status 中， 注意，这里有个问题， toplan.name 可能和已经存在的冲突？
//    val toStatusChanged = if(change.fromStatus == change.toStatus ){
//      fromstatusChanged.copy(map = fromstatusChanged.map + (toplan.name -> toplan))
//    } else{
//       fromMap.get(change.toStatus) match{
//        case None => Status ( name = change.toStatus , Map[String ,Plan](toplan.name -> toplan ))
//        case Some( status ) => status.copy(map = status.map + (toplan.name -> toplan))
//      }
//      
//    }
//    val toMap: Map[String, models.Status] = (fromMap - change.fromStatus - change.toStatus) +
//      (change.fromStatus -> fromstatusChanged) +
//      (change.toStatus -> toStatusChanged)  
//
//    val tobag = bag.copy(map = toMap)
//    tobag
    
    }
    tobagOption match{
      case None => bag 
      case Some(tobag) => tobag
    }
    
  }
  
}