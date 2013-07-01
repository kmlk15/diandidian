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

import org.openqa.selenium.By
class LoginWeiboSpec extends Specification {
	val log = LoggerFactory.getLogger(classOf[LoginSpec])
	
	"Weibo Login " should{
	  "new user login" in new WithBrowser( webDriver=   classOf[FirefoxDriver] ,port=8084  ) {
	    browser.goTo("/login/")
	    Thread.sleep(15000)
	    browser.$("a[herf=/login/weibo]").click()
	    browser.title().contains("应用授权")
	    browser.$("input#userId").text("wuhaohz@sina.cn")
	    browser.$("input#passwd").text("6543217a")
	    Thread.sleep(5000)
	    browser.$("a[action-type=submit]").click()
	    
	    Thread.sleep(5000)
	  }

	}
}