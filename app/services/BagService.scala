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
	def addLocation ( bagId: String ,statusName:String , planName: String , location: LocationForm): Boolean
    
    def removeLocation( bagId: String ,statusName:String , planName: String ,location: LocationForm): Boolean
    
    def get( bagId: String ) :Option[Bag]
  }


}

trait  BagServiceComponentImpl extends BagServiceComponent {

  this: BagServiceComponent =>
  val log = LoggerFactory.getLogger(classOf[BagServiceComponent])
  val dbname = "topo"
  override val bagService = new BagService {
    lazy val bagsMongoClient = mongoService.getMongoService[Bag]("bags", dbname)
    
    def addLocation ( bagId: String ,statusName:String , planName: String , location: LocationForm): Boolean ={
      val simpleLocation = SimpleLocation( location.id.get , location.name, location.enName)
      val q = MongoDBObject()
      q.put("_id", bagId)
      bagsMongoClient.find(q).headOption match{
    	  case None => 
    	    
    	    val plan = Plan( planName , List( simpleLocation) ) 
    	    val status = Status(statusName, Map(plan.name -> plan))
    	    val bag = Bag(bagId ,  Map( status.name -> status ))
    	    bagsMongoClient.insert(bag)
    	  case Some( bag ) =>
    	   val newStatus =   bag.map.get(statusName ) match{
    	      case None => Status( statusName , Map( planName -> Plan( planName , List(simpleLocation))))
    	      case Some( status ) =>
    	        val newplan = status.map.get( planName) match {
    	          case None =>  Plan( planName , List(simpleLocation))
    	          case Some( plan ) =>
    	            val newList = simpleLocation :: plan.list
    	            val newPlan  = plan.copy( list = newList)
    	            newPlan
    	        }
    	       val newStatus = status.copy( map= status.map + (newplan.name -> newplan )  )
    	       newStatus
    	    }
    	   val newBag = bag.copy( map = bag.map + ( statusName -> newStatus) )
    	   log.debug("newBag={}", newBag )
    	   update( newBag )
    	}
      /**
       *  val newStatus = status.copy( map= status.map + (plan.name -> plan )  )
    	            val newBag = bag.copy( map = bag.map + ( status.name -> newStatus) )
    	            newBag
       */
      true
    }
     def removeLocation( bagId: String ,statusName:String , planName: String , location: LocationForm):Boolean ={
      
      
      true
      
    }
     def update( bag: Bag) :Option[Bag] ={
       
       val q = MongoDBObject()
            q.put("_id", bag.id)
            bagsMongoClient.update(q, bag, false, false, WriteConcern.Normal)
            Some(bag)
            
     }
      def get( bagId: String ) :Option[Bag] = {
        val q = MongoDBObject()
        		q.put("_id", bagId)
        		bagsMongoClient.find(q).headOption
      }
  }
}
