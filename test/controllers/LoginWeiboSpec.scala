package controllers

import java.util.concurrent.TimeUnit

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.slf4j.LoggerFactory
import org.specs2.mutable.Specification



class LoginWeiboSpec extends Specification {
	val log = LoggerFactory.getLogger(classOf[LoginWeiboSpec])
	
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
	
	driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
	
	"Weibo Login " should{
	  "new user login" in  {
	    val step2 = driver.get("http://www.diandidian.com/login/")
	    val link = driver.findElementByCssSelector("div#weiboBtn a")
	    link.click()
	    // Thread.sleep(10*1000)
	    val title = driver.getTitle()
	    
	    title must contain("应用授权") 
	
	    
	    driver.findElementByCssSelector("input#userId").sendKeys("wuhaohz@sina.cn")
	    driver.findElementByCssSelector("input#passwd").sendKeys("6543217a")
	    //Thread.sleep(1000)
	    driver.findElementByCssSelector("a[action-type=submit]").click()
	    
	   // Thread.sleep(1000)
	    if(!driver.getTitle().contains("应用授权")){
	    	
		    val status = driver.findElementByCssSelector("div.account div.user-info span")
		    println( status.getText() )
		    val txt = status.getText()
		    txt  must  contain("wuhaohz88")
	       
	    }else{
	    	driver.getTitle() must contain("应用授权") 
	    	driver.findElementByCssSelector("a[action-type=submit]").click()
	    	
	    	val status = driver.findElementByCssSelector("div.account div.user-info span")
		    println( status.getText() )
		    val txt = status.getText()
		    txt  must  contain("wuhaohz88")
	    }
	   
	   
	    driver.quit()
	  }

	}
}