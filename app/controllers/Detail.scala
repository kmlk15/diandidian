package controllers

import play.api.Logger
import play.api.libs.json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object Detail extends Controller with MongoController {

  def jsoncollection: JSONCollection = db.collection[JSONCollection]("location")

  def view(name: String ) = Action {
    Logger.debug("name=" + name )
    
    Ok( views.html.detailview( name ))
  }
  
  def viewJson(name: String) = Action {
    Logger.debug("name=" + name)
    /**
     * name 不适合作为主键
     * 需要将 数据保存到 mongodb 中
     *
     */
    Async {
      val builder = jsoncollection.find (json.Json.obj("name" -> name))
      val cursor = builder.cursor[json.JsValue]
      cursor.headOption.map {
        case Some(location) => { Ok( location ) }
        case None => { NotFound }
      }
    }
  }
  
}