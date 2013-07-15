package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.mongodb.casbah.MongoConnection
import models.v2._

class LoginServiceTest extends Specification {
  val testdbname = "mytestxx"
  object LL extends LoginServiceComponentImpl {
    override val dbname = "mytestxx"
  }

  "Login Service " should {

  
    "WeiboUser Collection CRU" in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("weibouser")
      col.drop()

      val loginService = LL.loginService
      val weiboId = "test id"
      val w1 = WeiboUser(weiboId = weiboId)
      val w2 = loginService.saveWeiboUser(w1)
      w2.userId !== ""

      val w3 = loginService.getWeiboUser(weiboId)
      w3 must beSome

      w3.get === w2

      
      val w4 = WeiboUser(weiboId = weiboId , screenName="new Name")
      val w5 =  loginService.saveWeiboUser(w4)
      w2.userId  === w5.userId
      w2.screenName !== w5.screenName
      
    }

    "TwitterUser Collection CRU" in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("twitteruser")
      col.drop()

      val loginService = LL.loginService
      val twitterId = "my twitter test id"
      val w1 = TwitterUser(twitterId = twitterId)
      val w2 = loginService.saveTwitterUser(w1)
      w2.userId !== ""

      val w3 = loginService.getTwitterUser(twitterId)
      w3 must beSome

      w3.get === w2

      
      val w4 = TwitterUser(twitterId = twitterId , screenName="new Name")
      val w5 =  loginService.saveTwitterUser(w4)
      w2.userId  === w5.userId
      w2.screenName !== w5.screenName
      
    }

     "FacebookUser Collection CRU" in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("facebookuser")
      col.drop()

      val loginService = LL.loginService
      val facebookId = "my facebooke test id"
      val w1 = FacebookUser(facebookId = facebookId)
      val w2 = loginService.saveFacebookUser(w1)
      w2.userId !== ""

      val w3 = loginService.getFacebookUser(facebookId)
      w3 must beSome

      w3.get === w2

      
      val w4 = FacebookUser(facebookId = facebookId , screenName="new facebook Name")
      val w5 =  loginService.saveFacebookUser(w4)
      w2.userId  === w5.userId
      w2.screenName !== w5.screenName
      
    }
     
  }

}