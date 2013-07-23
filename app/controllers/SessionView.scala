package controllers

import play.api._
import play.api.mvc._



object SessionView extends Controller {
	def index = Action { implicit request =>
	  
	  val count = session.get("count").getOrElse("0").toInt + 1
	  
	  Ok("count=" + count).withSession( session + ("count" -> count.toString))
	  
	  
	  }
}