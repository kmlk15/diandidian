package controllers

import play.api.test.WithApplication
import org.slf4j.LoggerFactory
import org.specs2.mutable.Specification
import play.api.libs.json._
import models.v2._
import models.v2.MognoLocationJson._
import play.api.test._
import play.api.test.Helpers._
import java.io.File
import org.openqa.selenium.firefox.FirefoxDriver


class LoginSpec extends Specification {
	val log = LoggerFactory.getLogger(classOf[LoginSpec])
	
	"User register " should{
	  "new user register " in new WithBrowser( webDriver=   classOf[FirefoxDriver]   ) {
	    
  browser.goTo("/login/user/registerForm")
  val html = browser.pageSource()
  println(html)
  println(browser.$("title").getTexts())
  
  
  browser.$("title").getTexts().get(0) must equalTo("TODO")
    
  
 
	Thread.sleep(30000)    
	    
	    
	  }
	  
	}

}