package controllers

import org.slf4j.LoggerFactory
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.RequestHeader
import play.api.mvc.Request
import play.api.mvc.Results
import play.api.mvc.AnyContent
import play.api.mvc.Result
import play.api.mvc.Security


object Login extends Controller   {
  val Logger = LoggerFactory.getLogger(Login.getClass())
  
  def login() = Action { implicit request => 
    Ok(views.html.login())
  }
  
   def logout() = Action {
    Redirect(routes.Home.index()).withNewSession
  }

}




trait UserAuthTrait{
  
   def username(request: RequestHeader) = request.session.get("username")

  def onUnauthorized(request: RequestHeader) = Results.Redirect( controllers.routes.Login.login )

  def isAuthenticated(f: => String => Request[AnyContent] => Result) = {

    Security.Authenticated(username, onUnauthorized) { user =>
    	
      Action(request => f(user)(request))

    }

  }
}
