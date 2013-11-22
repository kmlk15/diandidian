package controllers

import org.slf4j.LoggerFactory

import models.v2.FacebookUser
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.Controller
import models.ActionLogHelp

object LoginFacebook extends Controller   {
  val log = LoggerFactory.getLogger(LoginFacebook.getClass())

  val loginService = base.LoginServiceRegistry.loginService
  val actionLogService = base.ActionLogServiceRegistry.actionLogService
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
           *  
           * 需要2次请求
           *
           */
          Async {
            val get = WS.url(accessTokenUrl).post(queryString)

            get.map { response =>
              log.debug("response=" + response.body.toString())
              val myjson = response.body
              Redirect(routes.LoginFacebook.facebookCallback.toString + "?" + myjson.toString, 302)
            }

          }
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
              log.debug(myjson.toString)

              (myjson \ "id").asOpt[String] match {
                case None => { Ok("ERROR get useId  ERROR") }
                case Some(facebookId) => {
                 actionLogService.save( ActionLogHelp.loginLog("facebook", facebookId) )
                 loginService.getFacebookUser(facebookId) match{
                   case None =>  newAccount(myjson, facebookId, access_token, expires)
                   case Some(facebookUser) => Redirect(routes.Home.index()).withSession("userId" -> facebookUser.userId,
                       "username" -> facebookUser.screenName, "avatar" -> facebookUser.avatar, "usertype"-> "facebook")
                 }
                }
              }
            }
          }
        }
      }
    }
  }

  private def newAccount(userinfoJson: play.api.libs.json.JsValue, facebookId: String, access_token: String, expires: String): play.api.mvc.PlainResult = {
    log.debug(Json.prettyPrint(userinfoJson))
    val name = (userinfoJson \ "name").asOpt[String].getOrElse("")
    val avatar = "https://graph.facebook.com/" + facebookId + "/picture"

   val facebookUser =  loginService.saveFacebookUser(FacebookUser(
       facebookId = facebookId,
       screenName = name,
       avatar = avatar ,
       token = Json.stringify( Json.obj("token"-> access_token , "expires"-> expires )),
       profile  = Json.stringify(userinfoJson)
       ))
    Redirect(routes.Home.index()).withSession("userId" ->facebookUser.userId, 
        "username" -> facebookUser.screenName, "avatar" -> facebookUser.avatar, "usertype"-> "facebook")
  }
}