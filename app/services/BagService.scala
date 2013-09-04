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
    
    /**
     * 创建一个空的背包， 这里 实际时得到一个合适的名字
     * 只有 statusName == defaultStatus  下 可以创建
     * 名字规律 “背包” “背包1” “背包2” 。。。
     */
     def createNewplan( bagId: String ):String 
     
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
    		
    
     def createNewplan( bagId: String ):String ={
      
     val newPlanname =  get(bagId) match {
        case None =>{
          log.error("整个 背包不存在")
           ""
            
        }
       case Some( bag ) => {
         val planName = bag.map.get( BagHelp.defaultStatusName  ) match{
          case None =>
            log.error("status 不存在 ")
            BagHelp.defaultPlanName
          case Some( status ) =>
            val planmap  = status.map
            var i = 0 
            var bagname = BagHelp.defaultPlanName ; 
            while( planmap.get( bagname) != None){
              i = i +1 
              bagname = BagHelp.defaultPlanName  + i 
            }
            bagname
        }
         val newBag = BagHelp.addLocation(bag, BagHelp.defaultStatusName, planName, List(  ))
         update(newBag)
         
         planName
      }
      
      
       
      }
     //更新 mongo
     
     
     
     newPlanname
    }
 
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

  