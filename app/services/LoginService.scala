package services

import models.v2._

import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.WriteConcern

trait LoginServiceComponent {

  val loginService: LoginService

  trait LoginService {

    def saveWeiboUser(weiboUser: WeiboUser): WeiboUser

    def getWeiboUser(weiboId: String): Option[WeiboUser]

    def saveTwitterUser(twitterUser: TwitterUser): TwitterUser

    def getTwitterUser(twitterId: String): Option[TwitterUser]

    def saveFacebookUser(facebookUser: FacebookUser): FacebookUser

    def getFacebookUser(facebookId: String): Option[FacebookUser]

    def getBaseUser(userId: String ,  usertype: String): Option[ BaseUser ]
  }
}

trait LoginServiceComponentImpl extends LoginServiceComponent {

  this: LoginServiceComponent =>
  val dbname = "topo"
  override val loginService = new LoginService {

    lazy val weiboMongoClient = mongoService.getMongoService[WeiboUser]("weibouser", dbname)
    lazy val twitterMongoClient = mongoService.getMongoService[TwitterUser]("twitteruser", dbname)
    lazy val facebookMongoClient = mongoService.getMongoService[FacebookUser]("facebookuser", dbname)

    def saveWeiboUser(weiboUser: WeiboUser): WeiboUser = {
      getWeiboUser(weiboUser.weiboId) match {
        case None => {
          /**
           *  还没有数据
           *
           */
          val userId = new ObjectId().toString
          val w1 = weiboUser.copy(userId = userId)
          weiboMongoClient.insert(w1)
          w1
        }
        case Some(w) => {
          /**
           * 如果 已经有数据
           *
           */
          val w1 = weiboUser.copy(userId = w.userId)
          val q = MongoDBObject()
          q.put("weiboId", w1.weiboId)
          weiboMongoClient.update(q, w1, false, false, WriteConcern.Normal)
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
    
    
    def saveTwitterUser(twitterUser: TwitterUser): TwitterUser = {
      getTwitterUser(twitterUser.twitterId) match {
        case None => {
          /**
           *  还没有数据
           *
           */
          val userId = new ObjectId().toString
          val w1 = twitterUser.copy(userId = userId)
          twitterMongoClient.insert(w1)
          w1
        }
        case Some(w) => {
          /**
           * 如果 已经有数据
           *
           */
          val w1 = twitterUser.copy(userId = w.userId)
          val q = MongoDBObject()
          q.put("twitterId", w1.twitterId)
          twitterMongoClient.update(q, w1, false, false, WriteConcern.Normal)
          w1
        }
      }
    }

    def getTwitterUser(twitterId: String): Option[TwitterUser] = {
      val q = MongoDBObject()
      q.put("twitterId", twitterId)
      try {
        Some(twitterMongoClient.findOne(q))
      } catch {
        case ex: Exception => {
          None
        }
      }
    }    

     def saveFacebookUser(facebookUser: FacebookUser): FacebookUser = {
      getFacebookUser(facebookUser.facebookId) match {
        case None => {
          /**
           *  还没有数据
           *
           */
          val userId = new ObjectId().toString
          val w1 = facebookUser.copy(userId = userId)
          facebookMongoClient.insert(w1)
          w1
        }
        case Some(w) => {
          /**
           * 如果 已经有数据
           *
           */
          val w1 = facebookUser.copy(userId = w.userId)
          val q = MongoDBObject()
          q.put("facebookId", w1.facebookId)
          facebookMongoClient.update(q, w1, false, false, WriteConcern.Normal)
          w1
        }
      }
    }

    def getFacebookUser(facebookId: String): Option[FacebookUser] = {
      val q = MongoDBObject()
      q.put("facebookId", facebookId)
      try {
        Some(facebookMongoClient.findOne(q))
      } catch {
        case ex: Exception => {
          None
        }
      }
    }

    def getBaseUser(userId: String ,  usertype: String): Option[ BaseUser ] ={
      val q = MongoDBObject( "userId"  -> userId)
      usertype match{
        case "weibo" =>
           weiboMongoClient.find(q).headOption.map( u => 
             BaseUser( userId = userId , screenName = u.screenName, avatar= u.avatar,usertype = usertype) )
        case "twitter" =>
           twitterMongoClient.find(q).headOption.map( u => 
             BaseUser( userId = userId , screenName = u.screenName, avatar= u.avatar,usertype = usertype) )
        case "facebook" =>
           facebookMongoClient.find(q).headOption.map( u => 
             BaseUser( userId = userId , screenName = u.screenName, avatar= u.avatar,usertype = usertype) )
           
      } 
      
      
    }
    
  }
}
 