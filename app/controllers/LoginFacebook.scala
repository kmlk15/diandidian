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

object LoginFacebook extends Controller with MongoController {
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

  /**
   *
   * http://stackoverflow.com/questions/6490332/facebook-api-get-profile-feed-with-authors-avatars
   * You can simply use the ID as the image with /picture:
   * <img src="https://graph.facebook.com/1337/picture"/>
   */

  def facebookCallback() = Action { implicit request =>
    val errorCode = request.getQueryString("error_code").getOrElse("")
    if (errorCode != "") {
      val error_message = request.getQueryString("error_message").getOrElse("")
      Ok(errorCode + "\t" + error_message)
    } else {
      val code = request.getQueryString("code")
      code match {
        case Some(code) => {
          log.debug("code=" + code)
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

            get.map { response =>
              log.debug("response=" + response.body.toString())
              val myjson = response.body

              log.info(myjson.toString)
              //Ok (myjson.toString    )

              Redirect(routes.LoginFacebook.facebookCallback.toString + "?" + myjson.toString, 302)
            }

          }
          //https://graph.facebook.com/me?access_token=CAAE5KvgCa1wBABkxDrtuQzgPlKi6RfkuiJDmGK4unXydzezoBNay8ysq6XjeLEbno9rQWIDYmg64yJ1MG67xCLZCD5hXDtZAhRU9rDAfkjPORZCZBBQBWp7X1VZA04pygeOkvYygvF3ZBducSOukqzZBq1vElZBoKmQZD

        }
        case None => {
          val access_token = request.getQueryString("access_token").getOrElse("")
          val expires = request.getQueryString("expires").getOrElse("")
          log.debug("access_token=" + access_token + "\texpires=" + expires)
          //

          /**
           *  根据 access_token 得到  userId
           */

          val url = "https://graph.facebook.com/me?access_token=" + access_token
          Async {
            val get = WS.url(url).get()

            get.map { response =>
              log.debug("response=" + response.body.toString())
              val myjson = response.json

              log.info(myjson.toString)
              //Ok (myjson.toString    )
              (myjson \ "id").asOpt[String] match {
                case None => { Ok("ERROR get useId  ERROR") }
                case Some(facebookId) => {

                  val cursor = userFacebookCollection.find(Json.obj("facebookId" -> facebookId)).cursor[json.JsObject]
                  val futurList = cursor.toList(1)
                  Async {
                    futurList.map { jsobjList =>
                      if (jsobjList.isEmpty) {
                        newAccount(myjson, facebookId, access_token, expires)
                      } else {
                        log.debug("user exists ")
                        val facebookUser = jsobjList.head
                        val userId = (facebookUser \ "userId").as[String]
                        val name = ((facebookUser \ "profile" \ "name").asOpt[String]).getOrElse("")
                        val avatar = "https://graph.facebook.com/" + facebookId + "/picture"
                        Redirect(routes.Home.index()).withSession("userId" -> userId, "username" -> name, "avatar" -> avatar)

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
  }

  private def newAccount(userinfoJson: play.api.libs.json.JsValue, facebookId: String, access_token: String, expires: String): play.api.mvc.PlainResult = {

    log.debug(Json.prettyPrint(userinfoJson))
    val first_name = ((userinfoJson \ "first_name").asOpt[String]).getOrElse("")
    val name = (userinfoJson \ "name").asOpt[String].getOrElse("")
    val avatar = "https://graph.facebook.com/" + facebookId + "/picture"

    val userId = BSONObjectID.generate.stringify
    val userJsval = Json.obj(
      "_id" -> userId,
      "username" -> "",
      "email" -> "",
      "password" -> "",
      "avatar" -> "")

    userCollection.save(userJsval)
    val facebookUser = Json.obj(
      "facebookId" -> facebookId,
      "userId" -> userId,
      "access_token" -> access_token,
      "expires" -> expires,
      "profile" -> userinfoJson)

    userFacebookCollection.save(facebookUser)

    Redirect(routes.Home.index()).withSession("userId" -> userId, "username" -> name, "avatar" -> avatar)

  }

}