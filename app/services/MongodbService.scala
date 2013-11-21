package services

import com.mongodb.DBObject
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.WriteConcern
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.Imports._
import models._
import play.api.Play
import play.api.Play.current

package object mongoContext {
  implicit val context = {
    val context = new Context {
      val name = "global"
      override val typeHintStrategy = StringTypeHintStrategy(when = TypeHintFrequency.WhenNecessary, typeHint = "_t")
    }
    context.registerGlobalKeyOverride(remapThis = "id", toThisInstead = "_id")
    context.registerClassLoader(Play.classloader)
    context
  }
}

import mongoContext._
trait MongodbServiceComponent {

  def getMongoService[T <: AnyRef](collection: String, dbname: String= "topo")(implicit manifest: Manifest[T]): MongodbService[T]

  trait MongodbService[T <: AnyRef] {
    def list(): List[T]
    def find(q: DBObject): List[T]
    def findOne(q: DBObject): T
    def insert(obj: T)
    def update(q: DBObject, obj: T, upsert: Boolean, multi: Boolean, concern: WriteConcern): Int
    def update(q: DBObject, obj: T): Int
    def delete(obj: DBObject, concern: WriteConcern): Int
    def delete(obj: DBObject): Int
    def getCollction( ) : MongoCollection
  }

}
trait MongodbServiceComponentImpl extends MongodbServiceComponent {

  override def getMongoService[T <: AnyRef](collection: String , dbname: String= "topo")(implicit manifest: Manifest[T]): MongodbService[T] = new MongodbService[T] {
    val mongoDB = MongoConnection()( dbname )
    val col = mongoDB(collection)

    def getCollction( ) : MongoCollection = col 
    
    override def list(): List[T] = {
      col.find(MongoDBObject()).toList.map(item => {
        //println(item);
        grater[T].asObject(item)
      })
    }
    override def find(q: DBObject): List[T] = {
      val result = col.find(q)
      result.toList.map(item => { grater[T].asObject(item) })
    }

    override def findOne(q: DBObject): T = {
      val result = col.findOne(q)
      grater[T].asObject(result.head)
    }
    override def insert(obj: T) {
      col.insert(grater[T].asDBObject(obj))
    }
    override def update(q: DBObject, obj: T, upsert: Boolean, multi: Boolean, concern: WriteConcern): Int = {
      val result = col.update(q, grater[T].asDBObject(obj), upsert, multi, concern)
      result.getN()
    }
    override def update(q: DBObject, obj: T) = {
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
