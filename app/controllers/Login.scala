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


object Login extends Controller with MongoController {
 val Logger = LoggerFactory.getLogger(Login.getClass() )
 
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

  def register() = Action(gridFSBodyParser(gridFS)) { request =>
    val paramMap = request.body.asFormUrlEncoded

    def get(key: String): String = {
      paramMap.get(key).getOrElse(Seq()).headOption.getOrElse("")
    }

    Logger.debug("paramMap=" + paramMap)
    
    val userIdStr = request.session.get("userId").getOrElse( get("userId") )
    
    val userId = if( userIdStr != "") {
       ( new BSONObjectID( userIdStr )).stringify 
    }else {
        BSONObjectID.generate.stringify
           
    } 
    
    Logger.debug("userId=" + userId )
    
    val username = get("username")
    val email = get("email")
    val password = get("password")

    Logger.debug("username=" + username)

    def validateUsername: Boolean = if (username == "" || username.length() < 2 || username.length() > 32) false else true
    def validateEmail: Boolean = if (email == "" || email.length() < 2 || !email.matches(emailPattern)) false else true
    def vaidatePassword: Boolean = if (password == "") true else if (password.length() < 2 || password.length() > 32) false else true

    if ((!validateUsername || !validateEmail || !vaidatePassword)) {
      Redirect(routes.Login.registerForm).flashing(
        "userId" -> userId,
        "username" -> username,
        "email" -> email,
        "password" -> password,
        "msg" -> "ERROR")
    } else {
      /**
       * 1 user  save to  mongodb
       * 2 save avatar ,
       */

      request.body.file("avatar") match {
        case Some(futureFile) => {
       
          
          Logger.debug(" 删除 已经存在的文件 ")
          val cursor  =userCollection.find(Json.obj("_id" -> userId)).cursor[json.JsObject]
             val futureUserList: Future[List[json.JsObject]] = cursor.toList
          
             val remove = for{
                users <- futureUserList
                 
                
              }yield{
                Logger.debug( "users.size=" + users.size)
                Logger.debug("users=" + users )
                
                 users.foreach( user =>{
                   Logger.debug(""" user \ "avatar" = """ + (user \ "avatar" ) )
                    ( user \ "avatar" ) match{
                     case json.JsString( avatarId ) =>{
                       Logger.debug(" 删除 avatarId=" + avatarId )
                       gridFS.files.remove(  BSONDocument("_id" -> BSONObjectID(avatarId ) ) ) 
                       avatarId
                     }
                     case _  =>""
                   }
                  
                 })
                 
                  ""
              }
              
             Logger.debug(" 删除 结束 ")  
              
          val futureUpload = for {
             r <- remove
             
            // we wait (non-blocking) for the upload to complete.
            putResult <- futureFile.ref
            // when the upload is complete, we add the userId id to the file entry (in order to find the attachments of the userId)
            result <- gridFS.files.update(BSONDocument("_id" -> putResult.id), BSONDocument("$set" -> BSONDocument("userId" -> userId)))
            
          } yield {
             
             
             val avatarId : String = putResult.id match {
               case x: BSONObjectID =>{
                 x.stringify
               }
               case y : BSONValue  =>  y.toString
             }
            val userJsval = Json.obj(
                  "_id" -> userId,
                  "username" -> username,
                  "email" -> email,
                  "password" -> password,
                  "avatar" ->  avatarId)
                Logger.debug("username=" + username)

                Logger.debug(Json.stringify(userJsval))

                userCollection.save(userJsval)
            userJsval
          }

          Async {
            futureUpload.map {
              case userJsval => {
                 Ok(userJsval).withSession( "userId" -> userId)
              }
            }.recover {
              case _ => BadRequest
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
            userCollection.save(userJsval).map(
              err => Ok(userJsval).withSession( "userId" -> userId)
              )
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

  def weibo() = TODO

  def weiboCallbak() = TODO

  def twitter() = TODO

  def twitterCallbak() = TODO

  def facebook() = TODO

  def facebookCallbak() = TODO

}