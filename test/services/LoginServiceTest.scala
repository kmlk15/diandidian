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

  
    "WeiboUser Collection CRUD" in new WithApplication {

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

  }

}