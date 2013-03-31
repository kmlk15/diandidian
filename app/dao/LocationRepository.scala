package dao
import play.api.Play
import play.api.Play.current
import com.mongodb.casbah.commons.Imports._
import com.mongodb.casbah.MongoConnection
import models.Location
import services.MongodbServiceComponentImpl
import base.mongoService

trait LocationRepositoryComponent {
  val locationRepository = new LocationRepository
  class LocationRepository {
    val location = mongoService.getMongoService("location")
    def find(q: DBObject): List[DBObject] = {
      location.find(q).toList
    }

    def getById(id: String): DBObject = {
      val q = MongoDBObject()
      q.put("_id", id)
      location.findOne(q)
    }

    def getBySlug(slug: String): Option[DBObject] = { null }

    def save(obj: DBObject) = {
      location.insert(obj)
    }

    def update(q: DBObject, obj: DBObject) = {
      location.update(q, obj)
    }

    def delete(obj: DBObject) = {
      location.delete(obj)
    }
  }
}

