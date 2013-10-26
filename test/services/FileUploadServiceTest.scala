package services

import org.specs2.mutable._
import java.io.File

class FileUploadServiceTest extends Specification{

  
  def resize(from: String, to: String,  wh: (Int,Int) ): Boolean = {
    import scala.sys.process._
    val (w,h) = wh
    
    val file = new File(from)
    if (file.exists()) {
      /**
       * convert -resize  1280x768   $img    1280.png ;
       */
      val imgsize = Seq("identify" ,"-format","%[fx:w]x%[fx:h]" , from ).!!
      println( imgsize )
      if( imgsize.trim( ) ==  w+"x"+h){
    	  	Seq("cp", "-rpv", from , to ) .! == 0 
      }else{
        Seq("convert", from, "-resize", w + "x" + h + "^", "-gravity", "center", "-extent", w + "x" + h,  "-quality", "100",  to).! == 0
      }
      
    }else{
      
      false
    }
  }
  
  "File resize"  should {
    
    "same size" in{
      val from = "/Users/wuhao/elance/workspace/diandidian/public/tmp/266_522dbe620364e757695ae7ec.jpg"
        
      val to= "/Users/wuhao/elance/workspace/diandidian/public/tmp/266_522dbe620364e757695ae7ec.jpgaaa" 
        
        resize(from ,to ,( 266 , 262))
      
      
    }
  }
}