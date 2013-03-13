package models

import sjson.json._
import DefaultProtocol._
import JsonSerialization._

case class Address(
  street1: String,
  street2: String,
  city: String,
  postalCode: String,
  stateOrProvince: String,
  country: String,
  latitude: Double,
  longitude: Double)

case class Phone(
  general: String,
  fax: String)

case class Admission(
  currency: String,
  general: Double,
  adults: Double,
  children: Double,
  student: Double,
  seniors: Double)

case class Hours(
  monday: String,
  tuesday: String,
  wednesday: String,
  thursday: String,
  friday: String,
  saturday: String,
  sunday: String,
  holiday: String)

case class Pictures(
  planning: String,
  result: List[String],
  thumbnail: List[String],
  hero: List[String])

case class Location(
  name: String,
  address: Address,
  phone: Phone,
  admission: Admission,
  hours: Hours,
  url: String,
  pictures: Pictures,
  fact: String)

object Address {
  implicit val addressFormat = asProduct8("street1", "street2", "city", "postalCode", "stateOrProvince", "country", "latitude", "longitude")(Address.apply)(Address.unapply(_).get)
}  
  
object Phone {
  implicit val phoneFormat = asProduct2("general", "fax")(Phone.apply)(Phone.unapply(_).get)
}  

object Admission {
  implicit val admissionFormat = asProduct6("currency", "general", "adults", "children", "student", "seniors")(Admission.apply)(Admission.unapply(_).get)
}

object Hours {
  implicit val admissionFormat = asProduct8("monday", "tueday", "wednesday", "thursday", "friday", "saturday", "sunday", "holiday")(Hours.apply)(Hours.unapply(_).get)
}

object Pictures {
  implicit val picturesFormat = asProduct4("planning", "result", "thumbnail", "hero")(Pictures.apply)(Pictures.unapply(_).get)
}

object Location {
  implicit val locationFormat = asProduct8("name", "address", "phone", "admission", "hours", "url", "pictures", "fact")(Location.apply)(Location.unapply(_).get)
}  
