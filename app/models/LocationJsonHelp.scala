package models

 import play.api.libs.json._
 
object LocationJsonHelp {
  
  implicit val addressFmt =  Json.format[Address]
  implicit val phoneFmt =  Json.format[Phone]
  implicit val AdmissionFmt = Json.format[Admission]
  implicit val HoursFmt =  Json.format[Hours]
  implicit val PicturesFmt =  Json.format[Pictures]
  implicit val categoryFmt =  Json.format[Category]
  
  implicit val locationFmt = Json.format[Location]
  
  val  defaultAddress: Address = Address(street = "", district = "", city = "", postalCode ="", stateProvince="",country="",latitude=0.0 , longitude=0.0 )
  val defaultPhone: Phone = Phone( general="" , fax="")
  val defaultAdmission: Admission = Admission (currency="", general=0.0, adults=0.0 , children=0.0, student=0.0 , seniors=0.0 )
  val defaultCategory: Category = Category(level_1="", level_2="" )
  val defaultHours = Hours("" , "", "" ,"" , "" , "" , "", "" )
  val defaultPictures = Pictures( "", List[String]() , List[String]() , List[String] ())
  val defaultLocation = Location(name="", defaultAddress , defaultPhone , defaultAdmission, defaultHours, "", defaultPictures , defaultCategory , "" )
  
}