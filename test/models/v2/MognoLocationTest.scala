package models.v2

import org.specs2.mutable.Specification
/**
 * 测试
 * case class location  <-> json <-> bson
 */

class MognoLocationTest  extends  Specification{
  
  "case class location  <-> json <-> bson " should{
    
    " case class <-> json " in{
      import models.v2.MognoLocationJson._
      import play.api.libs.json._
      val location = Location ()
      val jsonval = Json.toJson(location)
      val fromJson = jsonval.as[Location]
      location === fromJson
      
      val location2 =Location( name="testname" , address= Address( city = "test city" ))
      
      val jsonval2 = Json.toJson(location2)
      val fromJson2 = jsonval2.as[Location]
      location2 === fromJson2
      
      
      
    }
    "class class <-> bson" in{
      import models.v2.MognoLocationBson._
      import reactivemongo.bson.BSON
      val location = Location ()
      val bsonval =  BSON.writeDocument(location)
      val fromBson =  BSON.readDocument[Location](bsonval)
      location === fromBson
      
      val location2 =Location( name="testname" , address= Address( street = "test street" ))
      val bsonval2 =  BSON.writeDocument(location2)
      val fromBson2 =  BSON.readDocument[Location](bsonval2)
      location2 === fromBson2
      
    }

    "class calss <-> json <-> bson " in {
      import models.v2.MognoLocationBson._
     import play.modules.reactivemongo.json.BSONFormats

      import reactivemongo.bson.BSON
      import models.v2.MognoLocationJson._
      import play.api.libs.json._
     //class -> bson -> json -> class

      val location1 =Location( name="testname" , address= Address( city = "test city",street = "test street" ))
      val bsonval1 =  BSON.writeDocument(location1)
      val jsonval1 = BSONFormats.BSONDocumentFormat.writes(bsonval1)
      val fromBson1 =  jsonval1.as[Location]
      location1 ===  fromBson1 

      
      // class -> json -> bson -> class 
      val location2 =Location( name="testname" , address= Address( city = "test city",street = "test street" ))
      val jsonval2 = Json.toJson(location2)
      val bsonval2 =  BSONFormats.BSONDocumentFormat.reads(jsonval2).get
      val fromBson2 =  BSON.readDocument[Location](bsonval2)
      location2 ===  fromBson2 

 
      
    }
  }

}