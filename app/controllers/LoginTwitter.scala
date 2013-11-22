package controllers

import org.slf4j.LoggerFactory
import play.api.libs.json
import play.api.libs.json.Json

import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.Play
import play.api.Play.current


import play.api.mvc.Results

import play.api.libs.oauth.ConsumerKey
import play.api.libs.oauth.OAuth
import play.api.libs.oauth.ServiceInfo
import play.api.libs.oauth.RequestToken
import play.api.libs.oauth.OAuthSupportProxy

import play.api.mvc.RequestHeader
import twitter4j.TwitterFactory
import twitter4j.Twitter
import twitter4j.conf.ConfigurationBuilder

import models.v2.TwitterUser
import models.ActionLogHelp


object LoginTwitter extends Controller {
  val log = LoggerFactory.getLogger(LoginTwitter.getClass())

  val loginService = base.LoginServiceRegistry.loginService
  val actionLogService = base.ActionLogServiceRegistry.actionLogService
  val key = Play.configuration.getString("twitterappkey", None).getOrElse("")
  val secret = Play.configuration.getString("twitterappSecret", None).getOrElse("")
  val callbackurl = Play.configuration.getString("twittercallbackurl", None).getOrElse("")
  val usertype="twitter"
  
  val KEY = ConsumerKey(key, secret)
  val twitterServiceInfo = ServiceInfo(
    "https://api.twitter.com/oauth/request_token",
    "https://api.twitter.com/oauth/access_token",
    "https://api.twitter.com/oauth/authorize", KEY)

  /**
   *
   * 类似OAuthSupport 封装的， 支持 proxy
   * OAuthSupportProxy
   */
  val proxy = if (System.getenv("http_proxy") != null) Some(("127.0.0.1", 3128)) else None

  val TWITTER = OAuthSupportProxy(twitterServiceInfo, false, proxy)

  def twitter() = Action { implicit request =>
    request.getQueryString("oauth_verifier").map { verifier =>
       sessionTokenPair(request) match{
         case None => {
           log.error("ERROR get sessionTokenPair")
            Ok("ERROR get sessionTokenPair")
         }
         case Some( tokenPair ) => {
           log.debug( "tokenPair=" + tokenPair)
	      // We got the verifier; now get the access token, store it and back to index
	      TWITTER.retrieveAccessToken(tokenPair, verifier) match {
	        case Right(t) => {
	          // We received the authorized tokens in the OAuth object - store it before we proceed
	
	          log.debug("Right(t)=" + t)
	          // 获取 token 后， 返回到首页
	          t.token.split("-") match {
	            case x: Array[String] if x.length == 2 => {
	              val twitterId = x(0)
	              log.debug("twitterId=" + twitterId)
	               
	              loginService.getTwitterUser(twitterId) match {
	                case None => {
	                  log.debug("第一次登录的用户")
	                  newAccount(t, twitterId)
	                }
	                case Some(twitterUser) => {
	                  log.debug("已有用户登录 twitterUser= " + twitterUser)
	                  actionLogService.save( ActionLogHelp.loginLog(usertype, twitterUser.userId) )
	                	Redirect(routes.Home.index()).withSession("userId" -> twitterUser.userId, 
	                	    "username" -> twitterUser.screenName, "avatar" -> twitterUser.avatar , "usertype"-> usertype)
	                }
	                
	              }
	            }
	            case _ => Ok("can not get userid from " + t)
	          }
	        }
	        case Left(e) => {
	          log.error(e.getMessage(), e)
	          Ok("ERROR=" + e.getMessage())
	          
	        }
	      }
         }
       }
     
    }.getOrElse(

      TWITTER.retrieveRequestToken(callbackurl) match {
        case Right(t) => {
          log.debug(" Right(t) =" + t )
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          log.debug( "TWITTER.redirectUrl(t.token) = " + TWITTER.redirectUrl(t.token))
  Redirect(TWITTER.redirectUrl(t.token)).withSession("mytoken" -> t.token, "mysecret" -> t.secret)        
//          Ok(views.html.logintwitter( TWITTER.redirectUrl(t.token))) .withSession("mytoken" -> t.token, "mysecret" -> t.secret)
        }
        case Left(e) => { 
          log.error(e.getMessage(), e)
          Ok("ERROR=" + e.getMessage()) 
          }
      })
  }

  def twitterCallback() = Action { implicit request =>

    log.info(request.queryString.toString)
    val oauth_verifier = request.getQueryString("oauth_verifier").getOrElse("")

    log.debug(oauth_verifier)
    
    //Ok( oauth_verifier)
    if(oauth_verifier != ""){
    	Redirect(routes.LoginTwitter.twitter.toString(), Map("oauth_verifier" -> Seq(oauth_verifier)))
    }else{
        log.info("User denied")
        Redirect( routes.Home.index())
    }
    

  }

  def sessionTokenPair(implicit request: RequestHeader): Option[RequestToken] = {
    for {
      token <- request.session.get("mytoken")
      secret <- request.session.get("mysecret")
    } yield {
      RequestToken(token, secret)
    }
  }

  private def newAccount(reqToken: RequestToken, twitterId: String): play.api.mvc.PlainResult = {

    val cb = new ConfigurationBuilder()
    cb.setDebugEnabled(true)
      .setOAuthConsumerKey(key)
      .setOAuthConsumerSecret(secret)
      .setOAuthAccessToken(reqToken.token)
      .setOAuthAccessTokenSecret(reqToken.secret)

    val tf = new TwitterFactory(cb.build())
    val twitter = tf.getInstance()
    val profile = twitter.showUser(twitterId.toLong)
    log.debug("profile=" + profile)
    val name = profile.getName()
    val avatar = profile.getProfileImageURL()

    if (name != "") {
      val twitterUser = loginService.saveTwitterUser(TwitterUser(
        twitterId = twitterId,
        screenName = name ,
        avatar = avatar,
        token = Json.stringify(Json.obj("token" -> reqToken.token, "secret" -> reqToken.secret)),
        profile = Json.stringify(Json.obj("name" -> name, "avatar" -> avatar))))
        
         actionLogService.save( ActionLogHelp.loginLog(usertype, twitterUser.userId) )
         
      Redirect(routes.Home.index()).withSession("userId" -> twitterUser.userId, 
          "username" -> twitterUser.screenName, "avatar" -> twitterUser.avatar , "usertype"-> usertype)
    } else {
      Ok("ERROR profile= " + profile.toString)
    }
  }
}
