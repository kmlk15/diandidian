package models

/**
 * SimpleLocation ,  根据 planning 的需求 可能要 有时间数据 和 顺序。
 * 
 * plan name , 是由用户定义的
 * status name 的值是有限的 
 */
case class SimpleLocation(id: String="" , name: String ="" , enName: String="" )
case class Plan( name: String = "" ,  list: List[SimpleLocation] = Nil)
case class Status( name: String , map :Map[String ,Plan] = Map())

case class Bag (id: String ="" , typ: String ="" , map :Map[String , Status] = Map() ) {

}

object BagHelp {
  
  import play.api.libs.json._
  
  implicit val simpleLocationFmt = Json.format[SimpleLocation]
  implicit val planFmt = Json.format[Plan]
  implicit  val statusFmt = Json.format[Status]
  implicit  val bagFmt = Json.format[Bag]
  
}