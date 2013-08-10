package controllers.cms

import play.api.mvc
import play.api.mvc._
import org.slf4j.LoggerFactory

trait AuthTrait {

  def username(request: RequestHeader) = request.session.get("cmsAdmin")

  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Auth.login)

  def isAuthenticated(f: => String => Request[AnyContent] => Result) = {

    Security.Authenticated(username, onUnauthorized) { user =>
    	
      Action(request => f(user)(request))

    }

  }
}