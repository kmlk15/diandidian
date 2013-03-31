package dao

import com.mongodb.casbah.commons.Imports._
import dao._
import play.api.Play.current
import base.mongoService

trait UserRepositoryComponent {
  val userRepository = new UserRepository
  class UserRepository {
    val user = mongoService.getMongoService("user")
    def find(q: DBObject): List[DBObject] = {
      user.find(q).toList
    }

    def getById(id: String): DBObject = {
      val q = MongoDBObject()
      q.put("_id", id)
      user.findOne(q)
    }


    def save(obj: DBObject) = {
      user.insert(obj)
    }

    def update(q: DBObject, obj: DBObject) = {
      user.update(q, obj)
    }

    def delete(obj: DBObject) = {
      user.delete(obj)
    }
  }
}
