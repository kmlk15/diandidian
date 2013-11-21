package services

import com.mongodb.DBObject
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import models.ActionLog
import com.mongodb.DBObject
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory

trait ActionLogServiceComponent {
  val actionLogService: ActionLogService
  trait ActionLogService {
    /**
     * 保存日志信息
     */
    def save(obj: ActionLog): Option[ActionLog]

    /**
     * 按动作和日期进行统计
     */
    def stats(action: String, year: Int = 0, month: Int = 0): List[(Int, Int)]

    /**
     * 用户只计算一次
     */
    def statsDistinct(action: String, year: Int = 0, month: Int = 0): List[(Int, Int)]
  }
}

trait ActionLogServiceComponentImpl extends ActionLogServiceComponent { this: ActionLogServiceComponent =>
  val log = LoggerFactory.getLogger(classOf[ActionLogServiceComponentImpl])
  val dbname = "topo"

  override val actionLogService = new ActionLogService {
    lazy val mongoClient = mongoService.getMongoService[ActionLog]("actionlog", dbname)

    def save(obj: ActionLog): Option[ActionLog] = {
      val id = (new ObjectId().toString)
      val actionlog = if (obj.id == None) obj.copy(id = Some(id)) else { obj }
      mongoClient.insert(actionlog)
      Some(actionlog)
    }

    def stats(action: String, year: Int = 0, month: Int = 0): List[(Int, Int)] = {

      val result = if (month == 0 && year != 0 ) {
        mongoClient.getCollction.aggregate(
          MongoDBObject("$match" -> MongoDBObject("action" -> action, "year" -> year)),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$month", "count" -> MongoDBObject("$sum" -> 1))));

      } else if (year == 0 && month ==0 ) {
        
        mongoClient.getCollction.aggregate(
          MongoDBObject("$match" -> MongoDBObject("action" -> action)),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$year", "count" -> MongoDBObject("$sum" -> 1))));

      } else {

        mongoClient.getCollction.aggregate(
          MongoDBObject("$match" -> MongoDBObject("action" -> action, "year" -> year, "month" -> month)),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$day", "count" -> MongoDBObject("$sum" -> 1))));

      }

      val lista = result.results.map(obj => {
        val key: Int = obj.get("_id") match { case x: Number => x.intValue(); case _ => 0 }
        val count: Int = obj.get("count") match { case x: Number => x.intValue(); case _ => 0 }
        (key, count)
      }).toList
      lista.sortBy(_._1)

    }

    def statsDistinct(action: String, year: Int = 0, month: Int = 0): List[(Int, Int)] = {

      val result = if (month == 0 && year != 0 ){
        mongoClient.getCollction.aggregate(
          MongoDBObject("$match" -> MongoDBObject("action" -> action, "year" -> year)),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$month", "users" -> MongoDBObject("$addToSet" -> "$userId"))),
          MongoDBObject("$unwind" -> "$users"),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$_id", "count" -> MongoDBObject("$sum" -> 1))));

      } else if  (year == 0 && month ==0 ) {
        mongoClient.getCollction.aggregate(
          MongoDBObject("$match" -> MongoDBObject("action" -> action)),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$year", "users" -> MongoDBObject("$addToSet" -> "$userId"))),
          MongoDBObject("$unwind" -> "$users"),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$_id", "count" -> MongoDBObject("$sum" -> 1))));

      } else {

        mongoClient.getCollction.aggregate(
          MongoDBObject("$match" -> MongoDBObject("action" -> action, "year" -> year, "month" -> month)),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$day", "users" -> MongoDBObject("$addToSet" -> "$userId"))),
          MongoDBObject("$unwind" -> "$users"),
          MongoDBObject("$group" -> MongoDBObject("_id" -> "$_id", "count" -> MongoDBObject("$sum" -> 1))));

      }

      val lista = result.results.map(obj => {
        val key: Int = obj.get("_id") match { case x: Number => x.intValue(); case _ => 0 }
        val count: Int = obj.get("count") match { case x: Number => x.intValue(); case _ => 0 }
        (key, count)
      }).toList
      lista.sortBy(_._1)

    }

  }

}
 