package services

import com.mongodb.DBObject
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import models.ActionLog
import com.mongodb.DBObject
import org.bson.types.ObjectId

trait ActionLogServiceComponent {
  val actionLogService: ActionLogService
  trait ActionLogService {
    /**
     * 保存日志信息
     */
    def save(obj: ActionLog): Option[ActionLog]
    
    /**
     * 安动作和日期进行统计
     */
    def stats( action:String , year:Int=0 , month:Int=0 , day:Int=0): List[Int]
  }
}

 trait ActionLogServiceComponentImpl extends ActionLogServiceComponent { this: ActionLogServiceComponent =>
  override val actionLogService = new ActionLogService {
    lazy val mongoClient = mongoService.getMongoService[ActionLog]("actionLog")
    
    override def save(obj: ActionLog): Option[ActionLog]={
      val id = (new ObjectId().toString)
      val  actionlog = obj.copy(id = id )  
      mongoClient.insert( actionlog )
      Some( actionlog )
    }
    
     override def stats(action:String , year:Int=0 , month:Int=0 , day:Int=0): List[Int] = {
      
       
       
      Nil
    }
     
  }
 }
 