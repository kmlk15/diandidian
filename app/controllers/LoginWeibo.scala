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
      log.debug("accessTokenUrl=" + accessTokenUrl )
     
    Async {
      
      val get = WS.url(accessTokenUrl).post(queryString)
      get.map { response =>
        {
          val myjson = response.json
          log.info(myjson.toString)

          //如果 还没有绑定，  进入绑定流程； 如果已经绑定 ，则直接登录
          //进入 如果没有帐号，则需要创建一个用户名
          // 如果有，执行一次登录， 并将 进行账户绑定
          (myjson \ "uid").asOpt[String] match {
            case None => {
              //有可能 出错了， 提示， 请重试

              Ok(myjson.toString)
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
                    newAccount(myjson, weiboId)
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

  private def newAccount(myjson: play.api.libs.json.JsValue, weiboId: String): play.api.mvc.PlainResult = {
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
    val weiboUserJsval = Json.obj(
      "weiboId" -> weiboId,
      "userId" -> userId,
      "token" -> myjson)

    userWeiboCollection.save(weiboUserJsval)

    Redirect(routes.Login.registerForm).withSession("userId" -> userId)
  }

}