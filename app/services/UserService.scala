package services

import com.mongodb.DBObject
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import dao.UserRepositoryComponent

trait UserServiceComponent {
  val userService: UserService
  trait UserService {
    def list(): List[DBObject]

    def getById(id: String): DBObject

    def save(obj: DBObject)

    def update(q: DBObject, obj: DBObject)

    def delete(obj: DBObject)
  }
}

trait UserServiceComponentImpl extends UserServiceComponent { this: UserRepositoryComponent =>
  override val userService = new UserService {
    override def list(): List[DBObject] = {
      val q = MongoDBObject()
      userRepository.find(q)
    }
    override def getById(id: String): DBObject = {
      userRepository.getById(id)
    }

    override def save(obj: DBObject) = {
      userRepository.save(obj)
    }

    override def update(q: DBObject, obj: DBObject) {
      userRepository.update(q, obj)
    }

    override def delete(obj: DBObject) {
      userRepository.delete(obj)
    }
  }
}