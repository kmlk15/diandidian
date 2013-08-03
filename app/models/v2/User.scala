package models.v2

 import play.api.libs.json._

case class WeiboUser( weiboId: String = "" , userId: String = "" ,screenName: String="" , avatar: String = ""  , 
    token: String="{}" , profile: String = "{}"  )

case class TwitterUser( twitterId: String = "" , userId: String = "" ,screenName: String="" , avatar: String = ""  ,
    token: String="{}" , profile: String = "{}"  )
    
case class FacebookUser( facebookId: String = "" , userId: String = "" ,screenName: String="" , avatar: String = ""  ,
    token: String="{}" , profile: String = "{}"  )
    
 /**
  *    location 图片上传时的 专用 user , 用于 保持  版权 和 引用  
  *    id  是  mongodb objectId  
  */   
case class PhotoUser( id: String = "" , userName: String = "" , userId: String = "" )

object PhotoUserJson {
 
  implicit val photoUserFmt = Json.format[PhotoUser]
}