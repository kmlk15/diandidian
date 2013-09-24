package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.mongodb.casbah.MongoConnection
import models._
import models.LocationJsonHelp._
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import models.BagHelp._

class BagServiceTest extends Specification {
  val log = LoggerFactory.getLogger(classOf[BagServiceTest])
  val testdbname = "mytestxx"

  object LL extends BagServiceComponentImpl {
    override val dbname = testdbname
  }

  "Bag  Service " should {

    "Bag  Collection CRUD " in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("bags")
      col.drop()

      val location1 = LocationForm(id = Some("1"), name = "l1", enName = "l1en")
      val location2 = LocationForm(id = Some("2"), name = "l2", enName = "l2en")

      val service = LL.bagService
      val bagId = (new ObjectId()).toString
      val typ= "user"
        val usertype="weibo"
      val statusName =  defaultStatusName
      val planName =  defaultPlanName
      
      val result = service.addLocation(bagId, typ,usertype, statusName, planName, location1)

      result === true
      val bagOption = service.get(bagId)
      bagOption.size === 1
      val bag = bagOption.get
      log.debug("bag={}", bag)
      bag.id === bagId

      val status = bag.map( statusName)
      val plan = status.map(planName)
      val locationlist = plan.list

      locationlist.size === 1
      locationlist(0) === SimpleLocation(location1.id.get, location1.name, location1.enName)

      val resutl2 = service.addLocation(bagId, typ, usertype, statusName, planName, location2)
      resutl2 === true

      val bagOption2 = service.get(bagId)
      bagOption2.size === 1
      val bag2 = bagOption2.get
      log.debug("bag2={}", bag2)

      val locationlist2 = bag2.map.get(statusName).get.map.get(planName).get.list
      locationlist2.size === 2
      
      locationlist2(0) === SimpleLocation(location2.id.get, location2.name, location2.enName)
      
      service.addLocation(bagId, typ,usertype, statusName, planName, location2)
      
      val locationlist22 = service.get(bagId).get.map.get(statusName).get.map.get(planName).get.list
      locationlist22.size === 2
      
      
      
      val removereuslt = service.removeLocation(bagId, statusName, planName, location1)
      removereuslt === true

      val bag3 = service.get(bagId).get
      val locationlist3 = bag3.map.get(statusName).get.map.get(planName).get.list
      locationlist3.size === 1
      locationlist3(0) === SimpleLocation(location2.id.get, location2.name, location2.enName)
      
      service.removeLocation(bagId, statusName, planName, location1) === false 
      
    }
    
    "createNewplan test"  in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("bags")
      col.drop()

      val location1 = LocationForm(id = Some("1"), name = "l1", enName = "l1en")
      val location2 = LocationForm(id = Some("2"), name = "l2", enName = "l2en")

      val service = LL.bagService
      val bagId = (new ObjectId()).toString
      val typ= "user"
       val usertype="facebook"
      val statusName =  defaultStatusName
      val planName =  defaultPlanName
      
      service.createNewplan(bagId) === ""
      
      val result = service.addLocation(bagId, typ, usertype, statusName, planName, location1)
      
      service.createNewplan(bagId) === defaultPlanName +"1"
      
      service.createNewplan(bagId) === defaultPlanName +"2"
    }
  }

 
}