package models

import org.scalatest.FunSuite
import play.api.libs.json._
import models.LocationJsonHelp._


class LocationJsonHelpTest  extends  FunSuite{
  
  test("obj <-> json"){
        val jsonstr = """
{
  "name" : "会议展览中心",
  "address" : {
    "street" : "博览道1号",
    "district" : "湾仔",
    "city" : "香港",
    "postalCode" : "",
    "stateProvince" : "香港特別行政區",
    "country" : "中国",
    "latitude" : 100.0,
    "longitude" : 100.0
  },
  "phone" : {
    "general" : "(852) 25374591",
    "fax" : ""
  },
  "admission" : {
    "currency" : "HKD",
    "general" : 0.0,
    "adults" : 0.0,
    "children" : 0.0,
    "student" : 0.0,
    "seniors" : 0.0
  },
  "hours" : {
    "monday" : "",
    "tuesday" : "",
    "wednesday" : "",
    "thursday" : "",
    "friday" : "",
    "saturday" : "",
    "sunday" : "",
    "holiday" : ""
  },
  "url" : "",
  "pictures" : {
    "planning" : "/assets/images/dummy/planning/img01.jpg",
    "result" : [ "/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_4.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_5.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_6.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_7.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_8.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_9.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_10.jpg" ],
    "thumbnail" : [ "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_4.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_5.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_6.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_7.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_8.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_9.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/thb_10.jpg" ],
    "hero" : [ "/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_4.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_5.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_6.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_7.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_8.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_9.jpg", "/assets/images/china/sars/hong_kong/southern/ocean_park/result_10.jpg" ]
  },
  "category" : {
    "level_1" : "Park",
    "level_2" : "Garden Park"
  },
  "fact" : "香港會議展覽中心是香港的主要大型會議及展覽場地，位於香港島灣仔北岸，是香港地標之一. 2012年，香港會議展覽中心在 CEI Asia magazine，第10度榮膺亞洲最佳會議展覽中心殊榮。同年，香港會議展覽中心在第23屆TTG旅遊業大獎中，第4度獲選為最佳會議及展覽中心殊榮。"
}         
      """
       val jsonval = Json.parse( jsonstr)   
    
       
       val locationA =  jsonval.as[Location]
       locationA.name === "会议展览中心"
      
       val  jsonStr =   Json.prettyPrint(  Json.toJson ( locationA ))
       locationA === Json.parse(jsonStr).as[Location]
       
  }

}