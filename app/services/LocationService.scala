package services

import com.mongodb.casbah.Imports._
import com.mongodb.DBObject
import base.mongoService

trait LocationServiceComponent {

  val locationService: LocationService

  trait LocationService {
    def list(): List[DBObject]

    def getById(id: String): DBObject

    def getBySlug(slug: String): Option[DBObject]

    def save(obj: DBObject)

    def update(q: DBObject, obj: DBObject)

    def delete(obj: DBObject): Int
  }
}

trait LocationServiceComponentImpl extends LocationServiceComponent {

  override val locationService = new LocationService {
    val location = mongoService.getMongoService("location")

    override def list(): List[DBObject] = {
      val q = MongoDBObject()
      val ls = location.find(q)
      ls.toList
    }

    override def getById(id: String): DBObject = {
      val q = MongoDBObject()
      q.put("_id", id)
      location.findOne(q)
    }

    override def getBySlug(slug: String): Option[DBObject] = {
      null
    }

    override def save(obj: DBObject) {
      location.insert(obj);
    }
    
    override def update(q: DBObject, obj: DBObject){
      location.update(q, obj)
    }

    override def delete(obj: DBObject){
      location.delete(obj)
    }
  }
}