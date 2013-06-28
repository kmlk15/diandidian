package controllers

import play.api.Logger
import play.api.libs.json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


object  Login extends Controller with MongoController {

  def userCollection: JSONCollection = db.collection[JSONCollection]("user")
  
  def login() = Action{
    Ok( views.html.login())
  }
  /**
   * 必须已经是 通过 weibo/twitter/facebook 登录的
   * 
   */
  def registerForm()= Action{
    Ok( views.html.userRegistForm( ))
  }
  
  
  def register()= TODO
  
  
 
  
  def weibo()= TODO
  
  def weiboCallbak()= TODO
  
  def twitter()= TODO
  
  def twitterCallbak()= TODO
   
  def facebook()= TODO
    
  def facebookCallbak()= TODO
     
  

}