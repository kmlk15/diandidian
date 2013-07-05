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

object  LoginFacebook extends Controller with MongoController {
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
  def userFacebookCollection: JSONCollection = db.collection[JSONCollection]("userfacebook")
  
 
  //facebook
  val facebookappkey = Play.configuration.getString("facebookappkey", None).getOrElse("")
  val facebookappSecret = Play.configuration.getString("facebookappSecret", None).getOrElse("")
  val facebookcallbackurl = Play.configuration.getString("facebookcallbackurl", None).getOrElse("")
 
    
    
  def facebook() = Action {

    val url = "https://www.facebook.com/dialog/oauth"
    val queryString = Map(
      "client_id" -> Seq(facebookappkey),
      "response_type" -> Seq("code"),
      "redirect_uri" -> Seq(facebookcallbackurl))
      
       log.debug("Redirect to accessTokenUrl=" + url)
       
    Redirect(url, queryString, 302)
  }
  


def facebookCallback() = Action { implicit request =>

    val code = request.getQueryString("code") 
    code  match{
      case Some(code) =>{
        log.debug("code=" + code )
	    val accessTokenUrl = "https://graph.facebook.com/oauth/access_token"
	    val queryString = Map(
	      "client_id" -> Seq(facebookappkey),
	      "client_secret" -> Seq(facebookappSecret),
	
	      "redirect_uri" -> Seq(facebookcallbackurl),
	      "code" -> Seq(code))
	      log.debug("Redirect to accessTokenUrl=" + accessTokenUrl)
	    //Redirect(accessTokenUrl, queryString, 302)
        
	    /**
	     * get user id
	     * 奇怪， 不直接包含 id 信息 
	     * 需要2次请求
	     * 
	     */
	     Async {
        		val get = WS.url(accessTokenUrl).post(queryString)
        		
          
        		get.map{response =>
        		  log.debug( "response=" + response.body.toString())
        		    val myjson = response.body
        		    
        		    log.info(myjson.toString)
        		    //Ok (myjson.toString    )
        		    
        		    Redirect( routes.LoginFacebook.facebookCallback.toString +"?" +myjson.toString ,    302)
        		}
        		
        }
	    //https://graph.facebook.com/me?access_token=CAAE5KvgCa1wBABkxDrtuQzgPlKi6RfkuiJDmGK4unXydzezoBNay8ysq6XjeLEbno9rQWIDYmg64yJ1MG67xCLZCD5hXDtZAhRU9rDAfkjPORZCZBBQBWp7X1VZA04pygeOkvYygvF3ZBducSOukqzZBq1vElZBoKmQZD
	    
	    
      }
      case None => {
         val access_token = request.getQueryString("access_token").getOrElse("")
        		 val expires = request.getQueryString("expires").getOrElse("")
        		 log.debug("access_token="+ access_token +"\texpires=" + expires )
        		 //
        
        		 /**
        		  *  根据 access_token 得到  userId 
        		  */
        		 
        		 val url="https://graph.facebook.com/me?access_token=" + access_token
        		 Async {
        		val get = WS.url(url).get()
        		
          
        		get.map{response =>
        		  log.debug( "response=" + response.body.toString())
        		    val myjson = response.json
        		    
        		    log.info(myjson.toString)
        		    //Ok (myjson.toString    )
        		     (myjson \ "id") .asOpt[String] match{
        		    case None => {   Ok("ERROR get useId  ERROR")}
        		    case Some(facebookId) =>{
        		      
        		      val cursor = userFacebookCollection.find(Json.obj("facebookId" -> facebookId)).cursor[json.JsObject]
              val futurList = cursor.toList(1)
              Async {
                futurList.map { jsobjList =>
                  if (jsobjList.isEmpty) {
                    newAccount(myjson, facebookId)
                  } else {
                   val userId = (jsobjList.head \ "userId").as[String]
                    existsAccount(userId)
                  }
                }
              }
        		      
        		      
        		    }
        		  }
        		      
        		}
        		
        }
        		 
        		 //Ok("access_token="+ access_token +"\texpires=" + expires)
        		 
      }
      
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

  private def newAccount(myjson: play.api.libs.json.JsValue, facebookId: String): play.api.mvc.PlainResult = {
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
      "facebookId" -> facebookId,
      "userId" -> userId,
      "token" -> myjson)

    userFacebookCollection.save(twitterUser)

    Redirect(routes.Login.registerForm).withSession("userId" -> userId)
  }
  
}