package controllers

import play.api.Logger
import play.api.libs.json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.Json
import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.api.gridfs.DefaultFileToSave
import reactivemongo.api.gridfs.ReadFile
import reactivemongo.bson.BSONValue

object Login extends Controller with MongoController {

  def userCollection: JSONCollection = db.collection[JSONCollection]("user")
  val emailPattern = """\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b"""
  val gridFS = new GridFS(db)
  // let's build an index on our gridfs chunks collection if none
  gridFS.ensureIndex().onComplete {
    case index =>
      Logger.info(s"Checked index, result is $index")
  }

  def login() = Action {
    Ok(views.html.login())
  }
  /**
   * 必须已经是 通过 weibo/twitter/facebook 登录的
   *
   */
  def registerForm() = Action { implicit request =>
    Ok(views.html.userRegistForm())
  }

  def register() = Action(parse.multipartFormData) { request =>
    val paramMap = request.body.asFormUrlEncoded

    def get(key: String): String = {
      paramMap.get(key).getOrElse(Seq()).headOption.getOrElse("")
    }

    Logger.debug("paramMap=" + paramMap)
    val userId = get("userId")
    val username = get("username")
    val email = get("email")
    val password = get("password")

    Logger.debug("username=" + username)

    def validateUsername: Boolean = if (username == "" || username.length() < 2 || username.length() > 32) false else true
    def validateEmail: Boolean = if (email == "" || email.length() < 2 || !email.matches(emailPattern)) false else true
    def vaidatePassword: Boolean = if (password == "") true else if (password.length() < 2 || password.length() > 32) false else true

    if ((!validateUsername || !validateEmail || !vaidatePassword)) {
      Redirect(routes.Login.registerForm).flashing(
        "id" -> userId,
        "username" -> username,
        "email" -> email,
        "password" -> password,
        "msg" -> "ERROR")
    } else {
      /**
       * 1 user  save to  mongodb
       * 2 save avatar ,
       */
      val avatarId = request.body.file("avatar").map { img =>
        {	
           
        	import scala.concurrent._
        	import java.io.FileInputStream
          val fileToSave = DefaultFileToSave(
            filename = img.filename,
            contentType = img.contentType)
            val futureResult: Future[ReadFile[BSONValue]] = 
              gridFS.writeFromInputStream(fileToSave, new FileInputStream(   img.ref.file  ))
              
           
              
          "avatarId"
        }
      }.getOrElse("")
      val userJsval = Json.obj(
        "id" -> userId,
        "username" -> username,
        "email" -> email,
        "password" -> password,
        "avatar" -> avatarId)
      Logger.debug("username=" + username)

      Logger.debug(Json.stringify(userJsval))

      /**
       * TODO
       * 这里 实际需要的是update
       */
      userCollection.save(userJsval);

      //重定向到首页！并保持 用户信息 
      //      Redirect(routes.Home.index).withSession( 
      //          "username" -> username,"email" -> email ,"avatar" ->  avatarId
      //      )
      Ok(Json.stringify(userJsval))
    }

  }

  def weibo() = TODO

  def weiboCallbak() = TODO

  def twitter() = TODO

  def twitterCallbak() = TODO

  def facebook() = TODO

  def facebookCallbak() = TODO

}