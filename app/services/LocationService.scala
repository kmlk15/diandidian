package services

import com.mongodb.casbah.Imports._
import com.mongodb.DBObject
import base.mongoService
import models.Location
import java.util.regex.Pattern

trait LocationServiceComponent {

  val locationService: LocationService

  trait LocationService {
    def list(): List[Location]

    def list(city: String ): List[Location] 
    
    def list( city:String , district: String): List[Location] 
    
    def search(q: String): List[Location]
    
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
	val dbname = "topo"
  
  override val locationService = new LocationService {

    lazy val mongoClient = mongoService.getMongoService[Location]("location" , dbname)

    def list(): List[Location] = {
      mongoClient.list()
    }

   def list(city: String ): List[Location] = {
     val q = MongoDBObject()
      q.put("address.city", city)
      mongoClient.find(q)
   }
   
   def list( city:String , district: String): List[Location] = {
     val q = MongoDBObject()
      q.put("address.city", city)
      q.put("address.district" , district)
      mongoClient.find(q)
   }
   
   def search(query: String): List[Location] ={
     if( query == "" ){
       Nil
     }else{
     val pattern = Pattern.compile(Pattern.quote (query) , Pattern.CASE_INSENSITIVE);   
     
     val q1  = MongoDBObject( "address.city" -> pattern)
     val q4  = MongoDBObject( "address.district" -> pattern)
     val q2  = MongoDBObject( "name" -> pattern)
     val q3  = MongoDBObject( "enName" -> pattern)
     val q5  = MongoDBObject( "category.level_1" -> pattern)
     val q6  = MongoDBObject( "category.level_2" -> pattern)
     
     val q = MongoDBObject( "$or" ->List(q1,q2,q3,q4,q5,q6))
  
     
     mongoClient.find(q)
     }
   }
   
     def getById(id: String): Location = {
      val q = MongoDBObject()
      mongoClient.findOne(q)
    }

    def getByName(name: String): Option[Location] = {
      val q = MongoDBObject()
      q.put("name", name)
      try {
        Some(mongoClient.findOne(q))
      } catch {
        case ex: Exception => None
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