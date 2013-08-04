package models

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

case class OpenClose(open: String = "" , close: String = "" )
case class HoursForm(
  monday: OpenClose,
  tuesday: OpenClose,
  wednesday: OpenClose,
  thursday: OpenClose,
  friday: OpenClose,
  saturday: OpenClose,
  sunday: OpenClose,
  holiday: OpenClose)

case class LocationForm(
  name: String,
  address: Address,
  phone: Phone,
  admission: Admission,
  hours: HoursForm,
  url: String,
  category: Category,
  fact: String)
  
object LocationFormHelp {
  import play.api.libs.json.Json
  import LocationJsonHelp._
  
  implicit val ppenCloseFmt = Json.format[OpenClose]
  implicit val hoursFormFmt  = Json.format[HoursForm]
 implicit val locationFormFmt = Json.format[LocationForm]
 
  val form = Form {
    mapping(
      "name" -> text,
      "address" -> mapping(
        "street" -> text,
        "district" -> text,
        "city" -> text,
        "postalCode" -> text,
        "stateProvince" -> text,
        "country" -> text,
        "latitude" -> default( of[Double], 0.0 ),
        "longitude" -> default( of[Double], 0.0 ) )(Address.apply)(Address.unapply),

      "phone" -> mapping(
        "general" -> text,
        "fax" -> text)(Phone.apply)(Phone.unapply),

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
