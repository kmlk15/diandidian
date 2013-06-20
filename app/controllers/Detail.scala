package controllers


import play.api._
import play.api.mvc._


object Detail extends Controller {
  
  def view(name: String) = Action {
    
    Ok(views.html.detail("detail"))
    
  }

}