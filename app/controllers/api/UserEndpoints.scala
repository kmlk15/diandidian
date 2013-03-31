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

class UserEndpoints extends Controller{

  val us =userRegistry.userService
    def user(id:String)=Action{
     val user= "%s".format(us.getById(id))
    Ok(user).as("application/json")
  }
  
  def saveUser(user:String)=Action{
    val userObj=  com.mongodb.util.JSON.parse(user).asInstanceOf[DBObject];
    val savedUser=us.save(userObj)
     Ok("%s".format(savedUser)).as("application/json")
  }
}