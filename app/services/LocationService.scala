package services

import com.mongodb.casbah.Imports._
import com.mongodb.DBObject
import base.mongoService
import models.Location

trait LocationServiceComponent {

  val locationService: LocationService

  trait LocationService {
    def list(): List[Location]

    def getById(id: String): Location

    def getByName(name: String): Option[Location]
    
    def getBySlug(slug: String): Option[Location]

    def save(obj: Location)

    def update(q: DBObject, obj: Location)

    def delete(obj: DBObject)
  }
}

trait LocationServiceComponentImpl extends LocationServiceComponent { 
  
  this: LocationServiceComponent =>

  override val locationService = new LocationService {

    lazy val mongoClient = mongoService.getMongoService[Location]("location")
    
    override def list(): List[Location] = {
      mongoClient.list()
    }

    override def getById(id: String): Location = {
       val q = MongoDBObject()
      mongoClient.findOne(q)
    }
    
	def getByName(name: String): Option[Location] ={
    	val q = MongoDBObject()
    	q.put("name", name)
    	try{
    		Some( mongoClient.findOne(q) )
    	}catch{
    	  case ex:Exception =>  None
    	}
	  
	}
    override def getBySlug(slug: String): Option[Location] = {
      null
    }

    override def save(obj: Location) {
      mongoClient.insert(obj)
    }

    override def update(q: DBObject, obj: Location) {
     mongoClient.update(q, obj)
    }

    override def delete(obj: DBObject) {
      mongoClient.delete(obj)
    }
  }
}