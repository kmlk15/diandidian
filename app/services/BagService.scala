package services


import models.LocationForm
import models.Photo
import models.v2.PhotoUser
import models.v2.Category
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.WriteConcern
import org.slf4j.LoggerFactory
import com.mongodb.DBObject


trait BagServiceComponent {

  val bagService: BagService

  trait BagService {
  
  }


}

trait  BagServiceComponentImpl extends BagServiceComponent {

  this: BagServiceComponent =>
  val log = LoggerFactory.getLogger(classOf[BagServiceComponent])
  val dbname = "topo"
  override val bagService = new BagService {
    lazy val bagsMongoClient = mongoService.getMongoService[PhotoUser]("bags", dbname)
    
  }
}
