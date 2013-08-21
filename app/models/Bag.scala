package models

/**
 * SimpleLocation ,  根据 planning 的需求 可能要 有时间数据 和 顺序。
 * 
 * plan name , 是由用户定义的 缺省名字 "背包"
 * status name 的值是有限的  计划中，准备前往，过去背包 ; 缺省名字 计划中
 */
case class SimpleLocation(id: String="" , name: String ="" , enName: String="" )
case class Plan( name: String = "背包" ,  list: List[SimpleLocation] = Nil)
case class Status( name: String ="计划中", map :Map[String ,Plan] = Map())

case class Bag (id: String ="" , typ: String ="" , map :Map[String , Status] = Map() ) {

  
  lazy val locationList: List[ SimpleLocation] = locations()
  lazy val isEmpty : Boolean =  locationList.isEmpty
  
  def  locations( ):List[SimpleLocation]={
   val seq =   for{  
      statusTuple <- map 
      planTuple <- statusTuple._2.map
       location <- planTuple._2.list
    } yield {
      location
    }
    seq.toList
  }
} 

object BagHelp {
  
  import play.api.libs.json._
  
  implicit val simpleLocationFmt = Json.format[SimpleLocation]
  implicit val planFmt = Json.format[Plan]
  implicit  val statusFmt = Json.format[Status]
  implicit  val bagFmt = Json.format[Bag]
  
  val defaultPlanName = "背包"
  val defaultStatusName = "计划中"
}