package controllers

import org.slf4j.LoggerFactory

import models.v2.WeiboUser
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.ws.WS
import play.api.mvc.Action
import play.api.mvc.Controller
import models.ActionLogHelp

object LoginWeibo extends Controller {
  val log = LoggerFactory.getLogger(LoginWeibo.getClass())

  val loginService = base.LoginServiceRegistry.loginService
  val actionLogService = base.ActionLogServiceRegistry.actionLogService
  
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
    if(code == ""){
       log.info("User denied")
        Redirect( routes.Home.index())
    }else{
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

          (accesstokenJson \ "uid").asOpt[String] match {
            case None => {
              //有可能 出错了， 提示， 请重试

              Ok(accesstokenJson.toString)
            }
            case Some(weiboId) => {
            	actionLogService.save( ActionLogHelp.loginLog("weibo", weiboId) )
              loginService.getWeiboUser(weiboId)  match {
                case None =>  newAccount(accesstokenJson, weiboId)
                 
                case Some(weiboUser) => Redirect(routes.Home.index()).withSession(
                    "userId" -> weiboUser.userId, "username" -> weiboUser.screenName, 
                    "avatar" -> weiboUser.avatar , "usertype"-> "weibo")
                
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
  private def newAccount(accesstokenJson: play.api.libs.json.JsValue, weiboId: String) = {
    (accesstokenJson \ "access_token").asOpt[String] match {
      case None => {
        //错误提示页面 
        Ok("error")
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
            val errormsg = ((userinfoJson \ "error").asOpt[String]).getOrElse("")
            if (errormsg != "") {
              Ok(Json.stringify(userinfoJson))

            } else {

              val screenName = ((userinfoJson \ "screen_name").asOpt[String]).getOrElse("")
              val avatar = (userinfoJson \ "profile_image_url").asOpt[String].getOrElse("")
              if (screenName != "") {
                val w = WeiboUser(weiboId = weiboId, userId = "", screenName = screenName, avatar = avatar, token = Json.stringify(accesstokenJson), profile = Json.stringify(userinfoJson))
                val w2 =  loginService.saveWeiboUser(w)
                Redirect(routes.Home.index()).withSession("userId" -> w2.userId,
                    "username" -> screenName, "avatar" -> avatar , "usertype"-> "weibo")
              } else {
                Ok("error screenname is empty")
              }
            }
          }
        }
      }
    }
  }
}