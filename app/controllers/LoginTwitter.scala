package controllers

import org.slf4j.LoggerFactory
import play.api.libs.json
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.Play
import play.api.Play.current
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONValue
import reactivemongo.bson.Producer.nameValue2Producer
import scala.concurrent.Future
import play.api.mvc.Result
import play.api.libs.ws.WS
import play.api.libs.oauth.ConsumerKey
import play.api.libs.oauth.OAuth
import play.api.libs.oauth.ServiceInfo
import play.api.libs.oauth.RequestToken
import play.api.libs.oauth.OAuthSupportProxy
import play.api.libs.ws.WS.WSRequestHolder
import play.api.mvc.RequestHeader
import twitter4j.TwitterFactory
import twitter4j.Twitter
import twitter4j.conf.ConfigurationBuilder


object LoginTwitter extends Controller with MongoController {
  val log = LoggerFactory.getLogger(LoginTwitter.getClass())

  /**
   * use
   * 看来最好还是定义一个 user对象, 并编写一个 service 类
   */
  def userCollection: JSONCollection = db.collection[JSONCollection]("user")

  /**
   * weiboid <-> userid
   * token <-> userid
   */
  def userTwitterCollection: JSONCollection = db.collection[JSONCollection]("usertwitter")
  
  
  //twitter
  /**
   *  https://dev.twitter.com/docs/auth
   *
   *  https://dev.twitter.com/docs/auth/implementing-sign-twitter
   *  Oauth 1.0 的流程
   *  can you playframework example
   *  proxy 无法直接访问
   *  OAuth 在哪里可以设置 porxy?
   *  所以还是需要自己实现一遍
   *  HZSSO
   *  hzwuhao8@gmail.com
   */
  /**
   * 自己实现的
   *
   */
    val key = Play.configuration.getString("twitterappkey", None).getOrElse("")
  val secret = Play.configuration.getString("twitterappSecret", None).getOrElse("")
  val callbackurl = Play.configuration.getString("twittercallbackurl", None).getOrElse("")

  
 
  val KEY = ConsumerKey( key , secret )
  val twitterServiceInfo = ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize", KEY)

  /**
   *
   * 不支持 代理的 Oauth1.0
   * OAuthSupportProxy
   */
 val proxy = if(System.getenv("http_proxy")!=null)  Some(("127.0.0.1", 3128)) else None
 
 val TWITTER = OAuthSupportProxy(twitterServiceInfo, false, proxy )

  def twitter() = Action { request =>
    request.getQueryString("oauth_verifier").map { verifier =>
      val tokenPair = sessionTokenPair(request).get
      // We got the verifier; now get the access token, store it and back to index
      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
        case Right(t) => {
          // We received the authorized tokens in the OAuth object - store it before we proceed
          log.debug(" oauth_verifier ?? 这里是2次进入？ 从twittercallbak 再到 login? 带上参数？")
          log.debug("Right(t)" + t)
          // 获取 token 后， 返回到首页
          
          //Redirect(routes.Application.index).withSession("token" -> t.token, "secret" -> t.secret)
          
            t.token.split("-") match{
            case x: Array[ String] if  x.length == 2  =>{ 
              val twitterId = x(0)
              log.debug("twitterId=" + twitterId  )
              val myjson = json.Json.obj("token"-> t.token , "secret" -> t.secret)
              
                val cursor = userTwitterCollection.find(Json.obj("twitterId" -> twitterId)).cursor[json.JsObject]
              val futurList = cursor.toList(1)
              Async {
                futurList.map { jsobjList =>
                  if (jsobjList.isEmpty) {
                    newAccount( t , twitterId)	
                  } else {
                    val userId = (jsobjList.head \ "userId" ).as[String]
                   val  name = (jsobjList.head \ "profile" \"name").as[String]
                   val  avatar = (jsobjList.head \ "profile" \"avatar").as[String]
                   Redirect(routes.Home.index()).withSession("userId"->userId , "username" -> name, "avatar" -> avatar)
                   
                  }
                }
              }
              
            }
            case _ =>{   Ok("ERROR")  }
          }
        }
        case Left(e) => throw e
      }
    }.getOrElse(

      TWITTER.retrieveRequestToken( callbackurl ) match {
        case Right(t) => {
          log.debug(" Right(t) ")
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          Redirect(TWITTER.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => throw e
      })
  }

  def twitterCallback() = Action { implicit request =>
    //这里会有什么呢？

    log.info(request.queryString.toString)
    val oauth_verifier = request.getQueryString("oauth_verifier").getOrElse("")
    
    
   log.debug(oauth_verifier )
   //Ok( oauth_verifier)
   Redirect(routes.LoginTwitter.twitter.toString() , Map("oauth_verifier" -> Seq(oauth_verifier) ))

  }
  
  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("token")
      secret <- request.session.get("secret")
    } yield {
      RequestToken(token, secret)
    }
  }
  
  
  
  


  private def newAccount( reqToken: RequestToken , twitterId: String): play.api.mvc.PlainResult = {
 
    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(key)
      .setOAuthConsumerSecret(secret)
      .setOAuthAccessToken(reqToken.token)
      .setOAuthAccessTokenSecret(reqToken.secret)
      
      //.setHttpProxyHost("")
      //.setHttpProxyPort()
      
    val tf = new TwitterFactory(cb.build())
    val twitter = tf.getInstance()
    val profile = twitter.showUser(  twitterId.toLong)
    log.debug("profile=" + profile )
    val name =  profile.getName()
    val avatar = profile.getProfileImageURL()
    
    if(name !=""){
            //save to  mongodb
	            val userId = BSONObjectID.generate.stringify
	            val userJsval = Json.obj(
	              "_id" -> userId,
	              "username" -> "",
	              "email" -> "",
	              "password" -> "",
	              "avatar" -> "")
	              
	              userCollection.save ( userJsval)
	              
	              
	            val twitterUserJsval = Json.obj(
	              "twitterId" -> twitterId,
	              "userId" -> userId,
	              "token" -> reqToken.token,
	              "secret" -> reqToken.secret,
	              "profile" ->  Json.obj(
	            		  "name" -> name,
	            		  "avatar" -> avatar 
	              ))
	
	            userTwitterCollection.save(twitterUserJsval)
	            Redirect(routes.Home.index()).withSession("userId" -> userId, "username" -> name, "avatar" -> avatar)

            }else{
              
              Ok("ERROR profile= "  + profile.toString)
            }
    

    
  }
  

}