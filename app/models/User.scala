package models

import sjson.json._
import DefaultProtocol._
import JsonSerialization._

case class Work(
  employer: String,
  location: Location,
  position: String,
  startDate: String,
  endDate: String,
  current: Boolean)

case class Name(
  firstName: String,
  middleName: String,
  lastName: String)

case class Interest(
  name: String,
  category: String,
  createTime: String)
  
case class Url(
  fbProfile: String,
  fbProfilePic: String,
  website: String)

case class Currency(
  name: String,
  symbol: String)

case class User(
  name: Name,
  gender: String,
  locale: String,
  languages: List[String],
  url: Url,
  ageRange: String,
  timezone: String,
  bio: String,
  birthday: String,
  email: String,
  hometown: String,
  location: String,
  quotes: String,
  currency: Currency,
  work: List[Work],
  interest: List[Interest],
  facebookId:String,
  twitterId:String,
  weiboId:String)

object Work {
  implicit val workFormat = asProduct6("employer", "location", "position", "startDate", "endDate", "current")(Work.apply)(Work.unapply(_).get)
}

object Name {
  implicit val nameFormat = asProduct3("firstName", "middleName", "lastName")(Name.apply)(Name.unapply(_).get)
}

object Interest {
  implicit val interestFormat = asProduct3("name", "category", "createTime")(Interest.apply)(Interest.unapply(_).get)
}

object Url {
  implicit val urlFormat = asProduct3("fbProfile", "fbProfilePic", "website")(Url.apply)(Url.unapply(_).get)
}

object Currency {
  implicit val currencyFormat = asProduct2("name", "symbol")(Currency.apply)(Currency.unapply(_).get)
}
object User {
 implicit val userFormat = asProduct19("name", "gender", "locale", "languages", "url", "ageRange","timezone","bio","birthday","email","hometown","location","quotes","currency","work","interest","facebookId","twitterId","weiboId")(User.apply)(User.unapply(_).get)
}