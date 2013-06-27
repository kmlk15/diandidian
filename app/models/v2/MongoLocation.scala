package models.v2

 
case class Address(
  street: String = "",
  district: String = "",
  city: String = "",
  postalCode: String = "",
  stateProvince: String = "",
  country: String = "",
  latitude: Double = 0.0,
  longitude: Double = 0.0)

case class Phone(
  general: String = "",
  fax: String = "")

case class Admission(
  currency: String = "",
  general: Double = 0.0,
  adults: Double = 0.0,
  children: Double = 0.0,
  student: Double = 0.0,
  seniors: Double = 0.0)

case class Hours(
  monday: String = "",
  tuesday: String = "",
  wednesday: String = "",
  thursday: String = "",
  friday: String = "",
  saturday: String = "",
  sunday: String = "",
  holiday: String = "")

case class Pictures(
  planning: String = "",
  result: List[String] = Nil,
  thumbnail: List[String] = Nil,
  hero: List[String] = Nil)

case class Category(
  level_1: String = "",
  level_2: String = "")

case class Location(
  name: String = "",
  address: Address = Address(),
  phone: Phone = Phone(),
  admission: Admission = Admission(),
  hours: Hours = Hours(),
  url: String = "",
  pictures: Pictures = Pictures(),
  category: Category = Category(),
  fact: String = "")

  
object MognoLocationJson {
  import play.api.libs.json._
  implicit val addressFmt = Json.format[Address]
  implicit val phoneFmt = Json.format[Phone]
  implicit val admissionFmt = Json.format[Admission]
  implicit val hoursFmt = Json.format[Hours]
  implicit val picturesFmt = Json.format[Pictures]
  implicit val categoryFmt = Json.format[Category]
  implicit val locationFmt = Json.format[Location]
}


object MognoLocationBson {
  import  reactivemongo.bson.Macros
  implicit val addressHandler  = Macros.handler[Address]
  implicit val phoneHandler = Macros.handler[Phone]
  implicit val admissionHandler = Macros.handler[Admission]
  implicit val hoursHandler = Macros.handler[Hours]
  implicit val picturesHandler = Macros.handler[Pictures]
  implicit val categoryHandler = Macros.handler[Category]
  implicit val locationHandler = Macros.handler[Location]
}
  
 