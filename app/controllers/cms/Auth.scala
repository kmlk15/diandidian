package controllers.cms

import play.api.mvc
import play.api.mvc._
import org.slf4j.LoggerFactory

object Auth extends Controller  with AuthTrait {
  val log = LoggerFactory.getLogger(Auth.getClass())

  val pp = "pwd!1336688"


  def login = Action { implicit request =>
    Ok(views.html.cms.login())

  }

  def loginPost() = Action { implicit request =>

    val qmap = request.body.asFormUrlEncoded

    val result = for {
      m <- qmap
      name <- m.getOrElse("name", Seq[String]()).headOption

      passwd <- m.getOrElse("passwd", Seq[String]()).headOption
    } yield {
      log.debug("name={}, passwd={}", name, passwd, "")
      if (passwd == pp) {
        
        Redirect(routes.Locations.list).withSession(session + ("cmsAdmin" -> name))
      } else {
        Ok(views.html.cms.login())
      }

    }

    result.head

  }

  def loginTest( ) = isAuthenticated { username => implicit request =>
    
    Ok( username)
    
  }
  
  def logout = Action { implicit request =>
    Ok(views.html.cms.login()).withSession(session - "cmsAdmin")
  }
}