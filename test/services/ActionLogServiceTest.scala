package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.mongodb.casbah.MongoConnection
import models._
import models.LocationJsonHelp._
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import com.mongodb.casbah.commons.MongoDBObject

class ActionLogServiceTest extends Specification {
  val log = LoggerFactory.getLogger(classOf[BagServiceTest])
  val testdbname = "mytestxx"

  object LL extends ActionLogServiceComponentImpl {
    override val dbname = testdbname
  }

  "ActionLog  Service " should {

    "ActionLog  Collection CRUD " in new WithApplication {
      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("actionlog")
      col.drop()

      val service = LL.actionLogService

      val log1 = ActionLog(year = 2013, month = 11, day = 21, action = "login", usertype = "weibo", userId = "1")
      val log2 = ActionLog(year = 2013, month = 11, day = 22, action = "login", usertype = "weibo", userId = "1")
      val log3 = ActionLog(year = 2013, month = 11, day = 22, action = "login", usertype = "weibo", userId = "1")

      service.save(log1)
      service.save(log2)
      service.save(log3)
      for (i <- 1 to 12) {
        service.save(ActionLog(year = 2013, month = i, day = 22, action = "login", usertype = "weibo", userId = "1" + i))
      }
       val resulty = service.stats("login", 0, 0)
      log.debug("resultYear={}", resulty)

      
      val resultMonth = service.stats("login", 2013, 0)
      log.debug("resultMonth={}", resultMonth)

      val resultDay = service.stats("login", 2013, 11)
      log.debug("resultDay={}", resultDay)

       val resultdYear = service.statsDistinct("login", 0, 0)
      log.debug("resultd={}", resultdYear)
      
      val resultd = service.statsDistinct("login", 2013, 0)
      log.debug("resultd={}", resultd)

    }
  }

}