package services

import com.mongodb.DBObject
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.WriteConcern

trait MongodbServiceComponent {

  def getMongoService(collection: String)

  trait MongodbService {
    def find(q: DBObject): List[DBObject]
    def findOne(q: DBObject): DBObject
    def insert(obj: DBObject)
    def update(q: DBObject, obj: DBObject, upsert: Boolean, multi: Boolean, concern: WriteConcern): Int
    def update(q: DBObject, obj: DBObject): Int
    def delete(obj: DBObject, concern: WriteConcern): Int
    def delete(obj: DBObject): Int
  }

  trait MongodbServiceComponentImpl extends MongodbServiceComponent {
    
    override def getMongoService(collection: String) = new MongodbService {
      val mongoDB = MongoConnection()("topo")
      val col = mongoDB(collection)

      override def find(q: DBObject): List[DBObject] = {
        val result = col.find(q)
        result.toList
      }
      override def findOne(q: DBObject): DBObject = {
        val result = col.findOne(q)
        result.head
      }
      override def insert(obj: DBObject) {
        col.insert(obj)
      }
      override def update(q: DBObject, obj: DBObject, upsert: Boolean, multi: Boolean, concern: WriteConcern): Int = {
        val result = col.update(q, obj, upsert, multi, concern)
        result.getN()
      }
      override def update(q: DBObject, obj: DBObject) = {
        update(q, obj, false, false, null)
      }
      override def delete(obj: DBObject, concern: WriteConcern): Int = {
        val result = col.remove(obj, concern);
        result.getN()
      }
      override def delete(obj: DBObject): Int = {
        delete(obj, null)
      }
    }
  }
}