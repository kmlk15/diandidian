package services

import com.mongodb.DBObject
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject

trait UserServiceComponent {
  val userService: UserService
  trait UserService {
    def list(): List[DBObject]

    def getById(id: String): DBObject

    def save(obj: DBObject)

    def update(q: DBObject, obj: DBObject)

    def delete(obj: DBObject): Int
  }
}

trait UserServiceComponentImpl extends UserServiceComponent {

  override val userService = new UserService {
    val user = mongoService.getMongoService("user")

    override def list(): List[DBObject] = {
      val q = MongoDBObject()
      val ls = user.find(q)
      ls.toList
    }

    override def getById(id: String): DBObject = {
      val q = MongoDBObject()
      q.put("_id", id)
      user.findOne(q)
    }

    override def save(obj: DBObject) = {
      user.insert(obj)
    }

    override def update(q: DBObject, obj: DBObject) {
      user.update(q, obj)
    }

    override def delete(obj: DBObject) {
      user.delete(obj)
    }
  }
}