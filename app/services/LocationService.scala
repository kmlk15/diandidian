package services

import com.mongodb.casbah.Imports._
import com.mongodb.DBObject
import base.mongoService
import models.Location
import dao.LocationRepositoryComponent

trait LocationServiceComponent {

  val locationService: LocationService

  trait LocationService {
    def list(): List[DBObject]

    def getById(id: String): DBObject

    def getBySlug(slug: String): Option[DBObject]

    def save(obj: DBObject)

    def update(q: DBObject, obj: DBObject)

    def delete(obj: DBObject)
  }
}

trait LocationServiceComponentImpl extends LocationServiceComponent { this: LocationRepositoryComponent =>

  override val locationService = new LocationService {

    override def list(): List[DBObject] = {
      val q = MongoDBObject()
      locationRepository.find(q)
    }

    override def getById(id: String): DBObject = {
      locationRepository.getById(id)
    }

    override def getBySlug(slug: String): Option[DBObject] = {
      null
    }

    override def save(obj: DBObject) {
      locationRepository.save(obj)
    }

    override def update(q: DBObject, obj: DBObject) {
      locationRepository.update(q, obj)
    }

    override def delete(obj: DBObject) {
      locationRepository.delete(obj)
    }
  }
}