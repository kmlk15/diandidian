package controllers

import play.api.test.WithApplication
import org.slf4j.LoggerFactory
import org.specs2.mutable.Specification
import models.Location
import play.api.test._
import play.api.test.Helpers._
import sjson.json.JsonSerialization
import play.api.libs.json.Json
import models.LocationForm
import models.LocationFormHelp.locationFormFmt
class DetailSpec extends Specification {
  val log = LoggerFactory.getLogger(classOf[DetailSpec])
  "Detail Json test " should {

    "澳门艺术博物馆" in {

      running(FakeApplication()) {
        
        val locationName = "澳门艺术博物馆"
        log.debug(" 测试 " + locationName )
          
        val result = route(FakeRequest(GET, "/detail/json/" + locationName )).get

        status(result) must equalTo(OK)
        contentType(result) must beSome("application/json")
        charset(result) must beSome("utf-8")
        val jsonStr =  contentAsString(result) 
        val jsonval = Json.parse( jsonStr)
        
      
        val location =  Json.fromJson(jsonval).asOpt.get
        jsonStr  must contain(locationName )
        
        location.name must equalTo(locationName    ) 
        
        location.address.city must equalTo( "澳门" )
      }

    }

  }
}