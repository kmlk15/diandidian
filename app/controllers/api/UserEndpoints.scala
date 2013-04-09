package controllers.api

import base._
import services._
import models._
import play.api._
import play.api.mvc._
import sjson.json._
import JsonSerialization._
import dispatch.classic.json._
import com.mongodb.casbah.commons.MongoDBObjectBuilder
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.util.JSON
import com.mongodb.DBObject
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

object UserEndpoints extends Controller {

  val us = userRegistry.userService
  def user(id: String) = Action {
    val user = "%s".format(us.getById(id))
    Ok(user).as("application/json")
  }

  def saveUser(user:String) = Action {
    val js = JsValue.fromString(user);
    val userObj = fromjson[User](js)
    val savedUser = us.save(userObj)
    Ok("%s".format(savedUser)).as("application/json")
  }

  def loginWithFB(fbId: String) = Action {request =>
    val q = MongoDBObject()
    q.put("facebookId", fbId)
    val user = us.getUser(q)
    val userJson = "%s".format(user)
    Ok(userJson).as("application/json").withSession(request.session +("user"->userJson))
  }

  def loginWithTwitter(twitterId: String) = Action {request =>
    val q = MongoDBObject()
    q.put("twitterId", twitterId)
    val user = us.getUser(q)
    val userJson = "%s".format(user)
    Ok(userJson).as("application/json").withSession(request.session +("user"->userJson))
  }

  def loginWithWeibo(weiboId: String) = Action {request =>
    val q = MongoDBObject()
    q.put("weiboId", weiboId)
    val user = us.getUser(q)
    val userJson = "%s".format(user)
    Ok(userJson).as("application/json").withSession(request.session +("user"->userJson))
  }
  
  def logout=Action{
    Ok("success").withNewSession
  }
  }