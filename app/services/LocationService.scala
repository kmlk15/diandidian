package services

import com.mongodb.casbah.Imports._
import com.mongodb.DBObject

trait LocationServiceComponent {

  val locationService: LocationService

  trait LocationService {
    def list(): List[DBObject]

    def getById(id: String): DBObject

    def getBySlug(slug: String): Option[DBObject]
  }
}

trait LocationServiceComponentImpl extends LocationServiceComponent {

  override val locationService = new LocationService {
    val mongoDB = MongoConnection()("topo")
    val topo = mongoDB("location")

    override def list(): List[DBObject] = {
      val q = MongoDBObject()
      val location = topo.find(q)
      location.toList
    }

    override def getById(id: String): DBObject = {
      null
    }

    override def getBySlug(slug: String): Option[DBObject] = {
      null
    }
  }
}