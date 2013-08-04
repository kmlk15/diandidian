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
  
 
}