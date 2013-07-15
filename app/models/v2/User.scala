package models.v2

import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.JsValue


case class WeiboUser( weiboId: String = "" , userId: String = "" ,screenName: String="" , avatar: String = ""  , 
    token: String="{}" , profile: String = "{}"  )

case class TwitterUser( twitterId: String = "" , userId: String = "" ,screenName: String="" , avatar: String = ""  ,
    token: String="{}" , profile: String = "{}"  )
    
case class FacebookUser( facebookId: String = "" , userId: String = "" ,screenName: String="" , avatar: String = ""  ,
    token: String="{}" , profile: String = "{}"  )