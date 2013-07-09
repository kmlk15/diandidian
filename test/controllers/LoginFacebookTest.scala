package controllers

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.slf4j.LoggerFactory
import org.specs2.mutable.Specification
import java.util.concurrent.TimeUnit
import org.openqa.selenium.By

class LoginFacebookTest extends Specification {
val log = LoggerFactory.getLogger(classOf[LoginFacebookTest])
  val driver = if (System.getenv("http_proxy") != null) {
    println("use proxy")
    val PROXY = "localhost:3128";
    val proxy = new org.openqa.selenium.Proxy();
    proxy.setHttpProxy(PROXY).setFtpProxy(PROXY)
     .setSslProxy(PROXY);

    val cap = DesiredCapabilities.firefox()
    cap.setCapability(CapabilityType.PROXY, proxy);
    new FirefoxDriver(cap);
  } else {
    new FirefoxDriver();
  }


driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);


"facebook Login " should{
	  "facebook user login" in  {
	    
	    //val step1 = driver.get("http://www.diandidian.com")
	    val step2 = driver.get("http://www.diandidian.com/login/")
	    val link = driver.findElementByCssSelector("div#fbBtn a")
	    link.click()
	     Thread.sleep(10*1000)
	     val title = driver.getTitle()
	     println( title )
	     /**
	      * 123457Ac
	      */
	   Thread.sleep(10*1000)
	   
	    driver.findElementByCssSelector("form#login_form input#email").sendKeys("hzwuhao8@gmail.com")
	    driver.findElementByCssSelector("form#login_form input#pass").sendKeys("1234567Ac")
	    Thread.sleep(1*1000)
	    driver.findElementByCssSelector("form#login_form").submit()
	    Thread.sleep(10*1000)
	   
	    val status = driver.findElementByCssSelector("div.account div.user-info span")
	    println( status.getText() )
	    val txt = status.getText()
	    
	    txt  must  contain("Wu")
	    
	    
	    
	     Thread.sleep(10*1000)
	     driver.quit()
	  }
}
}