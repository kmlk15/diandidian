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

  val TWITTER = OAuthSupportProxy(twitterServiceInfo, false, Some(("127.0.0.1", 3128)))

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
                   val userId = (jsobjList.head \ "userId").as[String]
                    existsAccount(userId)
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
  
  
  
  
  
  private def existsAccount(userId: String ): play.api.mvc.AsyncResult = {
    /**
     * 用户信息是否 完整
     * 1 完整， 进入 首页
     * 2 不完整， 进入  registerForm
     */
     
    val cursor = userCollection.find(Json.obj("_id" -> userId)).cursor[json.JsObject]
    val futurList = cursor.toList(1)
    Async {
      futurList.map { userList =>
        {
          if (userList.isEmpty) {
            /**
             * user 对象被删除了？
             * 重新填写
             */
            Redirect(routes.Login.registerForm).withSession("userId" -> userId)
          } else {
            val userObj = userList.head
            val username = ((userObj \ "username").asOpt[String]).getOrElse("")
            val email = ((userObj \ "email").asOpt[String]).getOrElse("")
            val avatar = ((userObj \ "avatar").asOpt[String]).getOrElse("")
            if (username == "" || email == "") {

              Redirect(routes.Login.registerForm).withSession("userId" -> userId).flashing("username" -> username, "email" -> email)

            } else {
              Redirect(routes.Home.index).withSession("userId" -> userId, "username" -> username, "email" -> email, "avatar" -> avatar)
            }

          }

        }
      }
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
    Redirect(routes.Home.index()).withSession("username" -> name, "avatar" -> avatar)

    
  }
  private def newAccount2(myjson: play.api.libs.json.JsValue, twitterId: String): play.api.mvc.PlainResult = {
    /**
     * 1 创建一个 user
     * 2 建立 user 和 weibo 的关系
     * 3 页面重定向到 注册页面
     * 4 这里最好能够 获取  用户在 weibo 的 nickname
     * 这里有许多异步？
     */
    val userId = BSONObjectID.generate.stringify
    val userJsval = Json.obj(
      "_id" -> userId,
      "username" -> "",
      "email" -> "",
      "password" -> "",
      "avatar" -> "")

    userCollection.save(userJsval)
    val twitterUser = Json.obj(
      "twitterId" -> twitterId,
      "userId" -> userId,
      "token" -> myjson)

    userTwitterCollection.save(twitterUser)

    Redirect(routes.Login.registerForm).withSession("userId" -> userId)
  }
}