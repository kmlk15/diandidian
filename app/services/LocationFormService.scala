package services


import com.mongodb.casbah.Imports._
import com.mongodb.DBObject
import base.mongoService
import models.LocationForm
import java.util.regex.Pattern

trait LocationFormServiceComponent {

  val locationService: LocationFormService

  trait LocationFormService {
    def list(): List[LocationForm]

    def list(city: String ): List[LocationForm] 
    
    def list( city:String , district: String): List[LocationForm] 
    
    def search(q: String): List[LocationForm]
    
    def getById(id: String): LocationForm

    def getByName(name: String): Option[LocationForm]

    def getBySlug(slug: String): Option[LocationForm]

    def save(obj: LocationForm)

    def update(q: DBObject, obj: LocationForm)

    def delete(obj: DBObject)
  }
}

trait LocationFormServiceComponentImpl extends LocationFormServiceComponent {

  this: LocationFormServiceComponent =>
	val dbname = "topo"
  
  override val locationService = new LocationFormService {

    lazy val mongoClient = mongoService.getMongoService[LocationForm]("locationform" , dbname)

    def list(): List[LocationForm] = {
      mongoClient.list()
    }

   def list(city: String ): List[LocationForm] = {
     val q = MongoDBObject()
      q.put("address.city", city)
      mongoClient.find(q)
   }
   
   def list( city:String , district: String): List[LocationForm] = {
     val q = MongoDBObject()
      q.put("address.city", city)
      q.put("address.district" , district)
      mongoClient.find(q)
   }
   
   def search(query: String): List[LocationForm] ={
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
   
     def getById(id: String): LocationForm = {
      val q = MongoDBObject()
      mongoClient.findOne(q)
    }

    def getByName(name: String): Option[LocationForm] = {
      val q = MongoDBObject()
      q.put("name", name)
      try {
        Some(mongoClient.findOne(q))
      } catch {
        case ex: Exception => None
      }

    }
    override def getBySlug(slug: String): Option[LocationForm] = {
      null
    }

    override def save(obj: LocationForm) {
      mongoClient.insert(obj)
    }

    override def update(q: DBObject, obj: LocationForm) {
      mongoClient.update(q, obj)
    }

    override def delete(obj: DBObject) {
      mongoClient.delete(obj)
    }
  }
}