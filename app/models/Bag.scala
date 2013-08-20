package models

case class SimpleLocation(id: String="" , name: String ="" , enName: String="" )
case class Plan( name: String = "" ,  list: List[SimpleLocation] = Nil)
case class Status( name: String , map :Map[String ,Plan] = Map())

case class Bag (id: String ="" , map :Map[String , Status] = Map() ) {

}