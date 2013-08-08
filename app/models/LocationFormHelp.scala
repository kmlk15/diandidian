package models

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json

case class OpenClose(open: String = "" , close: String = "" )

case class HoursForm(
  monday: OpenClose = OpenClose(),
  tuesday: OpenClose = OpenClose(),
  wednesday: OpenClose = OpenClose(),
  thursday: OpenClose = OpenClose(),
  friday: OpenClose = OpenClose(),
  saturday: OpenClose = OpenClose(),
  sunday: OpenClose = OpenClose(),
  holiday: OpenClose = OpenClose())

case class LocationForm(
  id: Option[String] = None, 
  name: String = "" ,
  enName: String = ""  ,
  address: Address = Address(street = "", district = "", city = "", postalCode ="", stateProvince="",country="",latitude=0.0 , longitude=0.0 ),
  phone: Phone = Phone( general="" , fax=""),
  admission: Admission = Admission (currency="", general=0.0, adults=0.0 , children=0.0, student=0.0 , seniors=0.0 ),
  hours: HoursForm = HoursForm(),
  url: String = "" ,
  category: Category = Category(level_1="", level_2="" ),
  fact: String = "" )
  
object LocationFormHelp {
  
  import LocationJsonHelp._
  
  implicit val ppenCloseFmt = Json.format[OpenClose]
  implicit val hoursFormFmt  = Json.format[HoursForm]
 implicit val locationFormFmt = Json.format[LocationForm]
 
  val form = Form {
    mapping(
        "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),
      "name" -> text,
      "enName" -> text ,
      "address" -> mapping(
        "street" -> text,
        "district" -> text,
        "city" -> text,
        "postalCode" -> default(text, "" ) ,
        "stateProvince" -> text,
        "country" -> text,
        "latitude" -> default( of[Double], 0.0 ),
        "longitude" -> default( of[Double], 0.0 ) )(Address.apply)(Address.unapply),

      "phone" -> mapping(
        "general" -> text,
        "fax" -> default(text, "" ) )(Phone.apply)(Phone.unapply),

      "admission" -> mapping(
        "currency" -> text,
        "general" -> default( of[Double]  ,0.0) ,
        "adults" -> default( of[Double]  ,0.0),
        "children" -> default( of[Double]  ,0.0),
        "student" -> default( of[Double]  ,0.0),
        "seniors" -> default( of[Double]  ,0.0) )(Admission.apply)(Admission.unapply),

      "hours" -> mapping(
        "monday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply) ,
        "tuesday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "wednesday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "thursday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "friday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "saturday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "sunday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "holiday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply))
        (HoursForm.apply)(HoursForm.unapply),

      "url" -> text,

      "category" -> mapping(
        "level_1" -> text,
        "level_2" -> default(text,"")  )(Category.apply)(Category.unapply),

      "fact" -> text)(LocationForm.apply)(LocationForm.unapply)

  }

}
