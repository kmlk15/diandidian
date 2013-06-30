package controllers

import org.slf4j.LoggerFactory
import play.api.libs.json
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.gridfs.Implicits.DefaultReadFileReader
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.BSONValue
import reactivemongo.bson.Producer.nameValue2Producer
import scala.concurrent.Future
import play.api.mvc.Result

object Login extends Controller with MongoController {
  val Logger = LoggerFactory.getLogger(Login.getClass())

  def userCollection: JSONCollection = db.collection[JSONCollection]("user")

  val emailPattern = """\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}\b"""

  val gridFS = new GridFS(db)
  // let's build an index on our gridfs chunks collection if none
  //  gridFS.ensureIndex().onComplete {
  //    case index =>
  //      Logger.info(s"Checked index, result is $index")
  //  }

  def login() = Action {
    Ok(views.html.login())
  }
  /**
   * 必须已经是 通过 weibo/twitter/facebook 登录的
   *
   */
  def registerForm() = Action { implicit request =>

    request.session.get("userId") match {
      case None => {
        Logger.debug("userid is None")
        Ok(views.html.userRegistForm())
      }
      case Some(userId) => {
        Logger.debug("userId=" + userId)
        val cursor = userCollection.find(Json.obj("_id" -> userId)).cursor[json.JsObject]
        val futurList = cursor.toList(1)
        Async {
          val tmp = futurList.map {
            userList =>
              {
                if (userList.isEmpty) {
                  Logger.debug("user not exist")
                  Ok(views.html.userRegistForm())
                } else {

                  val userObj = userList.head
                  Logger.debug("user exist = " + userObj)
                  val username = ((userObj \ "username").asOpt[String]).getOrElse("")
                  val email = ((userObj \ "email").asOpt[String]).getOrElse("")
                  val avatar = ((userObj \ "avatar").asOpt[String]).getOrElse("")
                  Logger.debug((Map("username" -> username, "email" -> email)).toString)

                  Ok(views.html.userRegistForm(username, email, avatar))
                }

              }

          }
          tmp
        }

      }
    }

  }

  /**
   * 注册信息 保存
   * 一个 email 只能注册一次
   *
   */
  def register() = Action(gridFSBodyParser(gridFS)) { request =>
    val paramMap = request.body.asFormUrlEncoded

    def get(key: String): String = {
      paramMap.get(key).getOrElse(Seq()).headOption.getOrElse("")
    }

    Logger.debug("paramMap=" + paramMap)

    val userIdStr = request.session.get("userId").getOrElse(get("userId"))

    val userId = if (userIdStr != "") {
      //验证是合法的 BSONObjectID
      (new BSONObjectID(userIdStr)).stringify
    } else {
      BSONObjectID.generate.stringify

    }

    Logger.debug("userId=" + userId)

    val username = get("username")
    val email = get("email")
    val password = get("password")

    Logger.debug("username=" + username)

    def validateUsername: Boolean = if (username == "" || username.length() < 2 || username.length() > 32) false else true
    def validateEmail: Boolean = if (email == "" || email.length() < 2 || !email.matches(emailPattern)) false else true
    def vaidatePassword: Boolean = if (password == "") true else if (password.length() < 2 || password.length() > 32) false else true

    def redirect2Input(msg: String) = {
      Redirect(routes.Login.registerForm).flashing(
        "userId" -> userId,
        "username" -> username,
        "email" -> email,
        "password" -> password,
        "msg" -> msg)
    }

    if ((!validateUsername || !validateEmail || !vaidatePassword)) {
      redirect2Input(" ERROR ")
    } else {
      /**
       * 1 user  save to  mongodb
       * 2 save avatar ,
       */
      /**
       * email -> userId
       * TODO
       * session -> userId != email -> userId ???
       * 如果 email 已经存在，并且不是同一个useId , 则不允许提交
       */
      val cursor = userCollection.find(Json.obj("email" -> email)).cursor[json.JsObject]
      val futureUserList: Future[List[json.JsObject]] = cursor.toList

      val canUseEmailFuture = futureUserList.map {
        list =>
          list.forall(jsval => {
            (jsval \ "_id") == json.JsString(userId)
          })
      }

      request.body.file("avatar") match {
        case Some(futureFile) => {

          Async {
            for {
              canUseEmail <- canUseEmailFuture

            } yield {
              if (canUseEmail) {
                Logger.debug(" 允许 注册 ，email 可以正常使用")

                Logger.debug(" 删除 已经存在的文件 ")
                val cursor = userCollection.find(Json.obj("_id" -> userId)).cursor[json.JsObject]
                val futureUserList: Future[List[json.JsObject]] = cursor.toList

                val remove = for {
                  users <- futureUserList

                } yield {
                  Logger.debug("users.size=" + users.size)
                  Logger.debug("users=" + users)

                  users.foreach(user => {
                    Logger.debug(""" user \ "avatar" = """ + (user \ "avatar"))
                    (user \ "avatar") match {
                      case json.JsString(avatarId) => {
                        if (avatarId != "") {
                          Logger.debug(" 删除 avatarId=" + avatarId)
                          gridFS.remove(BSONObjectID(avatarId))
                        }
                        avatarId

                      }
                      case _ => ""
                    }

                  })

                  ""
                }

                Logger.debug(" 删除 结束 ")

                val futureUpload = for {
                  //删除已经有头像
                  r <- remove

                  // we wait (non-blocking) for the upload to complete.
                  putResult <- futureFile.ref
                  // when the upload is complete, we add the userId id to the file entry (in order to find the attachments of the userId)
                  result <- gridFS.files.update(BSONDocument("_id" -> putResult.id), BSONDocument("$set" -> BSONDocument("userId" -> userId)))

                } yield {
                  Logger.debug(" upload  file to gridFS")
                  val avatarId: String = putResult.id match {
                    case x: BSONObjectID => {
                      x.stringify
                    }
                    case y: BSONValue => y.toString
                  }

                  val userJsval = Json.obj(
                    "_id" -> userId,
                    "username" -> username,
                    "email" -> email,
                    "password" -> password,
                    "avatar" -> avatarId)
                  Logger.debug("username=" + username)

                  Logger.debug(Json.stringify(userJsval))

                  userCollection.save(userJsval)
                  Logger.debug(" user 已经保存到 mongodb")
                  userJsval
                }

                Async {

                  futureUpload.map {
                    case userJsval => {
                      val avatarId = (userJsval \ "avatar").as[String]
                      Ok(userJsval).withSession("userId" -> userId, "username" -> username, "email" -> email, "avatar" -> avatarId)
                    }
                  }.recover {
                    case _ =>
                      Logger.error("ERROR " + "recover")
                      BadRequest
                  }

                }

              } else {
                redirect2Input("email exist")

              }
            }

          }
        }
        case None => {
          val userJsval = Json.obj(
            "_id" -> userId,
            "username" -> username,
            "email" -> email,
            "password" -> password,
            "avatar" -> "")
          Logger.debug("username=" + username)
          Logger.debug(Json.stringify(userJsval))
          Async {
            canUseEmailFuture.map {
              canUseEmail =>
                if (canUseEmail) {
                  Logger.debug(" 允许 注册 ，email 可以正常使用")
                  userCollection.save(userJsval)
                  val avatarId = (userJsval \ "avatar").as[String]
                  Ok(userJsval).withSession("userId" -> userId, "username" -> username, "email" -> email, "avatar" -> avatarId)
                } else {
                  redirect2Input("email exist")
                }
            }
          }
        }
      }
    }
  }

  def getAttachment(id: String) = Action { request =>
    Async {
      // find the matching attachment, if any, and streams it to the client
      val file = gridFS.find(BSONDocument("_id" -> new BSONObjectID(id)))
      request.getQueryString("inline") match {
        case Some("true") => serve(gridFS, file, CONTENT_DISPOSITION_INLINE)
        case _ => serve(gridFS, file)
      }
    }
  }

  def removeAttachment(id: String) = Action {
    Async {
      gridFS.remove(new BSONObjectID(id)).map(_ => Ok).recover { case _ => InternalServerError }
    }
  }

  def twitter() = TODO

  def twitterCallback() = TODO

  def facebook() = TODO

  def facebookCallback() = TODO

}