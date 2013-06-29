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
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.safari.SafariDriver
import org.jsoup.Jsoup
import org.openqa.selenium.By

class LoginSpec extends Specification {
	val log = LoggerFactory.getLogger(classOf[LoginSpec])
	
	"User register " should{
	  "new user register " in new WithBrowser( webDriver=   classOf[FirefoxDriver]   ) {
	    
  browser.goTo("/login/user/registerForm")
  Thread.sleep(1000)
  
  val html = browser.pageSource()
  //println(html)
  //println("title="+  browser.title() ) 
  browser.title().contains("User Register") 
  
 
  browser.$("input#username").first().text("w1aa")
  browser.$("input#email").first().text("w1aa@bokee.net")
  
 // val upload = browser.webDriver.findElement(   By.id("avatar"))
  //upload.sendKeys("/tmp/t1.jpg")
   
  Thread.sleep(15000)
   
  browser.$("form#thismyForm").submit()
  //browser.$("title").getTexts().get(0) must equalTo("Diandidian - User Register ")
   
  val jsonStr = browser.pageSource()
  println( "jsonstr=" + jsonStr )
 jsonStr.contains( """"email":"w1@bokee.net","""") 	    
	 Thread.sleep(5000)   
	  }
	  
	}

}