package services

import models.v2._
import play.modules.reactivemongo.MongoController
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.WriteConcern

trait LoginServiceComponent {

  val loginService: LoginService

  trait LoginService {
 
    def saveWeiboUser(weiboUser: WeiboUser): WeiboUser

    def getWeiboUser(weiboId: String): Option[WeiboUser]
    
    
  }
}

trait LoginServiceComponentImpl extends LoginServiceComponent {

  this: LoginServiceComponent =>
  val dbname = "topo"
  override val loginService = new LoginService {

  
    lazy val weiboMongoClient = mongoService.getMongoService[WeiboUser]("weibouser", dbname)
   
    
    def saveWeiboUser(weiboUser: WeiboUser): WeiboUser = {
      
         getWeiboUser(weiboUser.weiboId ) match{
         case None => { 
           /**
       *  还没有数据 
       * 
       */
           val userId =   new ObjectId() .toString
           val w1 =  weiboUser.copy(userId =  userId )
           weiboMongoClient.insert( w1 )
           w1 
           }
         case Some(w) => { 
           /**
       * 如果 已经有数据
       * 
       */
           val w1 = weiboUser.copy( userId = w.userId)
           val q = MongoDBObject()
            q.put("weiboId", w1.weiboId )
         
           weiboMongoClient.update(q,  w1,false,false, WriteConcern.Normal )
           
           w1
           
         }
       }
      
     
 
      
    }

    def getWeiboUser(weiboId: String): Option[WeiboUser] = {
      val q = MongoDBObject()
      q.put("weiboId", weiboId)
      try {
        Some(weiboMongoClient.findOne(q))
      } catch {
        case ex: Exception => {
          None
        }
      }

    }
  }
}
 