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
  
  def login() = Action { implicit request => 
    Ok(views.html.login())
  }
  
   def logout() = Action {
    Redirect(routes.Home.index()).withNewSession
  }

}