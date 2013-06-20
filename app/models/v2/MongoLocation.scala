package models.v2

import org.jboss.netty.buffer._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import reactivemongo.bson._

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

case class Location(id: Option[BSONObjectID] = None,
  name: String = "",
  address: Address = Address(),
  phone: Phone = Phone(),
  admission: Admission = Admission(),
  hours: Hours = Hours(),
  url: String = "",
  pictures: Pictures = Pictures(),
  category: Category = Category(),
  fact: String = "")

object MognoLocation {

  implicit object AddressBSONReader extends BSONDocumentReader[Address] {
    def read(doc: BSONDocument): Address =
      Address(
        doc.getAs[String]("street").get,
        doc.getAs[String]("district").get,
        doc.getAs[String]("city").get,
        doc.getAs[String]("postalCode").get,
        doc.getAs[String]("stateProvince").get,
        doc.getAs[String]("country").get,
        doc.getAs[Double]("latitude").get,
        doc.getAs[Double]("longitude").get)
  }

  implicit object AddressBSONWriter extends BSONDocumentWriter[Address] {
    def write(addr: Address): BSONDocument =
      BSONDocument(
        "street" -> addr.street,
        "district" -> addr.district,
        "city" -> addr.city,
        "postalCode" -> addr.postalCode,
        "stateProvince" -> addr.stateProvince,
        "country" -> addr.country,
        "latitude" -> addr.latitude,
        "latitude" -> addr.latitude)

  }

  implicit object PhoneBSONReader extends BSONDocumentReader[Phone] {
    def read(doc: BSONDocument): Phone =
      Phone(
        doc.getAs[String]("general").get,
        doc.getAs[String]("fax").get)
  }

  implicit object PhoneBSONWriter extends BSONDocumentWriter[Phone] {
    def write(phone: Phone): BSONDocument =
      BSONDocument(
        "general" -> phone.general,
        "fax" -> phone.fax)

  }

  implicit object AdmissionBSONReader extends BSONDocumentReader[Admission] {
    def read(doc: BSONDocument): Admission =
      Admission(
        doc.getAs[String]("currency").get,
        doc.getAs[Double]("general").get,
        doc.getAs[Double]("adults").get,
        doc.getAs[Double]("children").get,
        doc.getAs[Double]("student").get,
        doc.getAs[Double]("seniors").get)
  }

  implicit object AdmissionBSONWriter extends BSONDocumentWriter[Admission] {
    def write(admission: Admission): BSONDocument =
      BSONDocument(
        "currency" -> admission.currency,
        "general" -> admission.general,
        "adults" -> admission.adults,
        "children" -> admission.children,
        "student" -> admission.student,
        "seniors" -> admission.seniors)
  }

  implicit object HoursBSONReader extends BSONDocumentReader[Hours] {
    def read(doc: BSONDocument): Hours =
      Hours(
        doc.getAs[String]("monday").get,
        doc.getAs[String]("tuesday").get,
        doc.getAs[String]("wednesday").get,
        doc.getAs[String]("thursday").get,
        doc.getAs[String]("friday").get,
        doc.getAs[String]("saturday").get,
        doc.getAs[String]("sunday").get,
        doc.getAs[String]("holiday").get)
  }

  implicit object HoursBSONWriter extends BSONDocumentWriter[Hours] {
    def write(hours: Hours): BSONDocument =
      BSONDocument(
        "monday" -> hours.monday,
        "tuesday" -> hours.tuesday,
        "wednesday" -> hours.wednesday,
        "thursday" -> hours.thursday,
        "friday" -> hours.friday,
        "saturday" -> hours.saturday,
        "sunday" -> hours.sunday,
        "holiday" -> hours.holiday)
  }

  implicit object PicturesBSONReader extends BSONDocumentReader[Pictures] {
    def read(doc: BSONDocument): Pictures =
      Pictures(
        doc.getAs[String]("planning").get,
        doc.getAs[List[String]]("result").get,
        doc.getAs[List[String]]("thumbnail").get,
        doc.getAs[List[String]]("hero").get)
  }

  implicit object PicturesBSONWriter extends BSONDocumentWriter[Pictures] {
    def write(pictures: Pictures): BSONDocument =
      BSONDocument(
        "planning" -> pictures.planning,
        "result" -> pictures.result,
        "thumbnail" -> pictures.thumbnail,
        "hero" -> pictures.hero)

  }

  implicit object CategoryBSONReader extends BSONDocumentReader[Category] {
    def read(doc: BSONDocument): Category =
      Category(
        doc.getAs[String]("level_1").get,
        doc.getAs[String]("level_2").get)
  }

  implicit object CategoryBSONWriter extends BSONDocumentWriter[Category] {
    def write(category: Category): BSONDocument =
      BSONDocument(
        "level_1" -> category.level_1,
        "level_2" -> category.level_2)

  }

  implicit object LocationBSONReader extends BSONDocumentReader[Location] {
    def read(doc: BSONDocument): Location =
      Location(
        doc.getAs[BSONObjectID]("_id"),
        doc.getAs[String]("name").get,
        doc.getAs[Address]("address").get,
        doc.getAs[Phone]("phone").get,
        doc.getAs[Admission]("admission").get,
        doc.getAs[Hours]("hours").get,
        doc.getAs[String]("url").get,
        doc.getAs[Pictures]("pictures").get,
        doc.getAs[Category]("category").get,
        doc.getAs[String]("fact").get)
  }

  implicit object LocationBSONWriter extends BSONDocumentWriter[Location] {
    def write(location: Location): BSONDocument =
      BSONDocument(
        "_id" -> location.id.getOrElse(BSONObjectID.generate),
        "name" -> location.name,
        "address" -> location.address,
        "phone" -> location.phone,
        "admission" -> location.admission,
        "hours" -> location.hours,
        "url" -> location.url,
        "pictures" -> location.pictures,
        "category" -> location.category,
        "fact" -> location.fact)

  }
}
  
 