package models.v2

 import play.api.libs.json._
 
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

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
case class PhotoUser( id:  String ="" , userName: String = "" , userId: String = "" )

object PhotoUserHelp {
 
  implicit val photoUserFmt = Json.format[PhotoUser]
  
  implicit val form = Form (
  mapping(
       "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),
        "userName" -> nonEmptyText , 
        "userId" -> nonEmptyText
      
      ){ 
	  	(id , userName , userId) =>  
        		PhotoUser( id = id.getOrElse("") , userName=userName , userId = userId)
      } 
      {
          u => Some( ( Some( u.id) , u.userName , u.userId ) )
      }
        
  
  )
  
}
