package controllers

import play.api._
import play.api.mvc._



object SessionView extends Controller {
	def index = Action { implicit request =>
	  
	  val count = session.get("count").getOrElse("0").toInt + 1
	  
	  Ok("应从11 开始，如果是从 index2 进入这个页面；  count=" + count).withSession( session + ("count" -> count.toString))
	  
	  
	  }
	
	def  index2 = Action { implicit request =>
	  Redirect( routes.SessionView.index()).withSession( session + ( "count" ->"10")) 
	}
}