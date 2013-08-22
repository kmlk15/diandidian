package services

import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.WriteConcern
import org.slf4j.LoggerFactory
import com.mongodb.DBObject
import play.api.libs.json.Json

import models._

trait BagServiceComponent {

  val bagService: BagService

  trait BagService {
    def addLocation(bagId: String, typ: String, statusName: String, planName: String, location: LocationForm): Boolean

    def removeLocation(bagId: String, statusName: String, planName: String, location: LocationForm): Boolean

    def get(bagId: String): Option[Bag]
    
    def del( bagId: String ): Int
    
     def save( bag: Bag ):Option[Bag] 
  }

}

trait BagServiceComponentImpl extends BagServiceComponent {

  this: BagServiceComponent =>
  val log = LoggerFactory.getLogger(classOf[BagServiceComponent])
  val dbname = "topo"
  override val bagService = new BagService {
    lazy val bagsMongoClient = mongoService.getMongoService[Bag]("bags", dbname)

    def addLocation(bagId: String, typ: String, statusName: String, planName: String, location: LocationForm): Boolean = {
      val simpleLocation = SimpleLocation(location.id.get, location.name, location.enName)
      get(bagId) match {
        case None =>
          log.debug("bag 还没有建立，创建新的 bag ")
          val plan = Plan(planName, List(simpleLocation))
          val status = Status(statusName, Map(plan.name -> plan))
          val bag = Bag(bagId, typ, Map(status.name -> status))
          bagsMongoClient.insert(bag)
        case Some(bag) =>
          val newBag = BagHelp.addLocation(bag, statusName, planName, List( simpleLocation ))
          log.debug("addlocation Bag={}", newBag)
          update(newBag)
      }

      true
    }

    def removeLocation(bagId: String, statusName: String, planName: String, location: LocationForm): Boolean = {
      val simpleLocation = SimpleLocation(location.id.get, location.name, location.enName)
      get(bagId) match {
        case None =>
          log.debug("bag  不存在 ")
          false
        case Some(bag) =>
        val newBag =   BagHelp.removeLocation(bag, statusName, planName, List(simpleLocation))
           if( newBag == bag ){
             false
           }else{
               update(newBag)
                true
           }
         
      }

    }
    def update(bag: Bag): Option[Bag] = {

      val q = MongoDBObject()
      q.put("_id", bag.id)
      bagsMongoClient.update(q, bag, false, false, WriteConcern.Normal)
      Some(bag)

    }
    
    def save( bag: Bag ):Option[Bag] ={
      get( bag.id) match{
        case None => 
           bagsMongoClient.insert(bag)
           Some( bag )
        case Some( existBag) => None
      }
      
    }
    def get(bagId: String): Option[Bag] = {
      if (bagId.trim() == "") {
        None
      } else {
        val q = MongoDBObject()
        q.put("_id", bagId)
        bagsMongoClient.find(q).headOption
      }
    }
    
    def del( bagId: String ): Int ={
      
        val q = MongoDBObject()
        q.put("_id", bagId)
        bagsMongoClient.delete(q, WriteConcern.Normal)
    }
  }
}

  