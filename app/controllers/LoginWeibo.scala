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

object LoginWeibo extends Controller with MongoController {
  val log = LoggerFactory.getLogger(LoginWeibo.getClass())

  /**
   * use
   * 看来最好还是定义一个 user对象, 并编写一个 service 类
   */
  def userCollection: JSONCollection = db.collection[JSONCollection]("user")

  /**
   * weiboid <-> userid
   * token <-> userid
   */
  def userWeiboCollection: JSONCollection = db.collection[JSONCollection]("userweibo")

  //获取 新浪授权

  val sinaappkey = Play.configuration.getString("sinaappkey", None).getOrElse("2454366401")
  val sinaappSecret = Play.configuration.getString("sinaappSecret", None).getOrElse("9067b727d0f051496d3df4175c251fed")
  val sinacallbackurl = Play.configuration.getString("sinacallbackurl", None).getOrElse("http://127.0.0.1:8084/login/callback/weibo")

  def weibo() = Action {

    val url = "https://api.weibo.com/oauth2/authorize"
    val queryString = Map(
      "client_id" -> Seq(sinaappkey),
      "response_type" -> Seq("code"),
      "redirect_uri" -> Seq(sinacallbackurl))

    log.debug("queryString=" + queryString)
    Redirect(url, queryString, 302)
  }

  def weiboCallback() = Action { implicit request =>

    val code = request.getQueryString("code").getOrElse("")
    val accessTokenUrl = "https://api.weibo.com/oauth2/access_token"
    val queryString = Map(
      "client_id" -> Seq(sinaappkey),
      "client_secret" -> Seq(sinaappSecret),
      "grant_type" -> Seq("authorization_code"),
      "redirect_uri" -> Seq(sinacallbackurl),
      "code" -> Seq(code))
    log.debug("accessTokenUrl=" + accessTokenUrl)

    Async {

      val get = WS.url(accessTokenUrl).post(queryString)
      get.map { response =>
        {
          val accesstokenJson = response.json
          log.info(accesstokenJson.toString)

          //如果 还没有绑定，  进入绑定流程； 如果已经绑定 ，则直接登录
          //进入 如果没有帐号，则需要创建一个用户名
          // 如果有，执行一次登录， 并将 进行账户绑定
          (accesstokenJson \ "uid").asOpt[String] match {
            case None => {
              //有可能 出错了， 提示， 请重试

              Ok(accesstokenJson.toString)
            }
            case Some(weiboId) => {
              /**
               *           1 是否有  weiboid <-> userid 对应？
               *           2 有，登陆成功，
               *           3 没有，进入注册页面
               */
              val cursor = userWeiboCollection.find(Json.obj("weiboId" -> weiboId)).cursor[json.JsObject]
              val futurList = cursor.toList(1)
              Async {
                futurList.map { jsobjList =>
                  if (jsobjList.isEmpty) {
                    newAccount(accesstokenJson, weiboId)
                  } else {
                	  log.debug("get from  mongodb ")
                    val userinfoJson = (jsobjList.head \ "profile")
                    log.debug("userinfoJson=" + Json.prettyPrint(userinfoJson))
                    val screenname = ((userinfoJson \ "screen_name").asOpt[String]).getOrElse("")
                    val name = (userinfoJson \ "name").asOpt[String].getOrElse("")
                    val avatar = (userinfoJson \ "profile_image_url").asOpt[String].getOrElse("")
                    Redirect(routes.Home.index()).withSession("username" -> screenname, "avatar" -> avatar)

                  }
                }
              }
            }

          }

        }
      }

    }
  }



  /**
   * 获取数据，并 跳转到主页
   */
  private def newAccount(accesstokenJson: play.api.libs.json.JsValue, weiboId: String): play.api.mvc.AsyncResult = {
    (accesstokenJson \ "access_token").asOpt[String] match {
      case None => {
        //错误提示页面 
        Async {
          Future(Ok("error"))
        }
      }
      case Some(token) => {
        val url = "https://api.weibo.com/2/users/show.json?uid=" + weiboId + "&access_token=" + token
        Async {
          val get = WS.url(url).get
          get.map { response =>
            val userinfoJson = response.json
            log.debug(userinfoJson.toString)
            	/**
            	 * {"error":"applications over the unaudited use restrictions!","error_code":21321,"request":"/2/users/show.json"}
            	 */
            	val  errormsg = ((userinfoJson \ "error").asOpt[String]).getOrElse("")
            	if(errormsg!=""){
            	  
            		Ok(Json.stringify( userinfoJson ))
            		
            	}else{
            /**
             *
             */
            val screenname = ((userinfoJson \ "screen_name").asOpt[String]).getOrElse("")
            val name = (userinfoJson \ "name").asOpt[String].getOrElse("")
            val avatar = (userinfoJson \ "profile_image_url").asOpt[String].getOrElse("")
            if(screenname !=""){
            //save to  mongodb
	            val userId = BSONObjectID.generate.stringify
	            val userJsval = Json.obj(
	              "_id" -> userId,
	              "username" -> "",
	              "email" -> "",
	              "password" -> "",
	              "avatar" -> "")
	              
	              userCollection.save ( userJsval)
	              
	              
	            val weiboUserJsval = Json.obj(
	              "weiboId" -> weiboId,
	              "userId" -> userId,
	              "token" -> accesstokenJson,
	              "profile" -> userinfoJson)
	
	            userWeiboCollection.save(weiboUserJsval)
            }
            Redirect(routes.Home.index()).withSession("username" -> screenname, "avatar" -> avatar)

          }
          }
        }

      }
    }

  }



}