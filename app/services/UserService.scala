package services

import com.mongodb.DBObject
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import models.User

trait UserServiceComponent {
  val userService: UserService
  trait UserService {
    def list(): List[User]

    def getById(id: String): User

    def save(obj: User)

    def update(q: DBObject, obj: User)

    def delete(obj: DBObject)
  }
}

trait UserServiceComponentImpl extends UserServiceComponent { this: UserServiceComponent =>
  override val userService = new UserService {
    lazy val mongoClient = mongoService.getMongoService[User]("location")
    override def list(): List[User] = {
      val q = MongoDBObject()
      mongoClient.find(q)
    }
    override def getById(id: String): User = {
      val q = MongoDBObject()
      q.put("_id", id)
      mongoClient.findOne(q)
    }

    override def save(obj: User) = {
      mongoClient.insert(obj)
    }

    override def update(q: DBObject, obj: User) {
      mongoClient.update(q, obj)
    }

    override def delete(obj: DBObject) {
      mongoClient.delete(obj)
    }
  }
}