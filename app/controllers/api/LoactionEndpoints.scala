package controllers.api

import base._
import services._
import models._

import play.api._
import play.api.mvc._

import sjson.json._
import JsonSerialization._
import dispatch.classic.json._

object LoactionEndpoints extends Controller {

  val ls = registry locationService

  /*
   * Test of converting json string into object and back to json string by using sjson
   */
  def test = Action {
    val js = """{"planning":"1234567", "result":["234456"], "thumbnail":[], "hero":[]}"""
    val jv = JsValue.fromString(js)
    val ph = fromjson[Pictures](jv)
    
    Ok(JsValue.toJson(tojson(ph)))  
  }

}