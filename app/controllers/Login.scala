package controllers

import org.slf4j.LoggerFactory

import play.api.mvc.Action
import play.api.mvc.Controller


object Login extends Controller   {
  val Logger = LoggerFactory.getLogger(Login.getClass())
  
  def login() = Action { implicit request => 
    Ok(views.html.login())
  }
  
   def logout() = Action {
    Redirect(routes.Home.index()).withNewSession
  }

}