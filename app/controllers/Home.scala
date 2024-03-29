package controllers

import play.api._
import play.api.mvc._

object Home extends Controller {
  
  def index = Action { implicit request =>
    Ok(views.html.home("Home"))
  }
  
  def coming = Action {
    Ok(views.html.coming())
  }
  
}