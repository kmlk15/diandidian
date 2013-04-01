package services

import com.mongodb.casbah.Imports._
import com.mongodb.DBObject
import base.mongoService
import models.Location
import dao.LocationRepositoryComponent

trait LocationServiceComponent {

  val locationService: LocationService

  trait LocationService {
    def list(): List[Location]

    def getById(id: String): DBObject

    def getBySlug(slug: String): Option[DBObject]

    def save(obj: DBObject)

    def update(q: DBObject, obj: DBObject)

    def delete(obj: DBObject)
  }
}

trait LocationServiceComponentImpl extends LocationServiceComponent { 
  
  this: LocationServiceComponent =>

  override val locationService = new LocationService {

    lazy val mongoClient = mongoService.getMongoService("location")
    
    override def list(): List[Location] = {
      mongoClient.list[Location]()
    }

    override def getById(id: String): DBObject = {
      //locationRepository.getById(id)
      null
    }

    override def getBySlug(slug: String): Option[DBObject] = {
      null
    }

    override def save(obj: DBObject) {
      //locationRepository.save(obj)
    }

    override def update(q: DBObject, obj: DBObject) {
      //locationRepository.update(q, obj)
    }

    override def delete(obj: DBObject) {
      //locationRepository.delete(obj)
    }
  }
}