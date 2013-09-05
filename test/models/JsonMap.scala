package models


import org.scalatest.FunSuite
import org.slf4j.LoggerFactory
import play.api.libs.json._

class JsonMap extends FunSuite {
  val log = LoggerFactory.getLogger(classOf[JsonMap])

  val str="""{ "_id" : "520755d40364282014c45533", "imgsrc" : "43554c410282463071657025.jpg" }
{ "_id" : "52107cd50364599001b2d14b", "imgsrc" : "a41d2b1009954630c5c70125.jpg" }
{ "_id" : "5210808d0364599001b2d14d", "imgsrc" : "c41d2b100995463077080125.jpg" }
{ "_id" : "520e34e30364cc9760d2c2b1", "imgsrc" : "0b2c2d0679cc4630cc43e025.jpg" }
{ "_id" : "521b40aa03643ac6c2a61dae", "imgsrc" : "dad16a2c6ca346306604b125.jpg" }
{ "_id" : "521b415103643ac6c2a61db0", "imgsrc" : "fad16a2c6ca346300414b125.jpg" }
{ "_id" : "521b41c103643ac6c2a61db2", "imgsrc" : "1bd16a2c6ca346300b14b125.jpg" }
{ "_id" : "520e33a30364cc9760d2c2ab", "imgsrc" : "aa2c2d0679cc4630b833e025.jpg" }
{ "_id" : "520e33d00364cc9760d2c2ad", "imgsrc" : "ca2c2d0679cc46309b33e025.jpg" }
{ "_id" : "520e291703649d3aaacda0a5", "imgsrc" : "4a0adcaaa3d94630cf82e025.jpg" }
{ "_id" : "520e296703649d3aaacda0a7", "imgsrc" : "6a0adcaaa3d946308592e025.jpg" }
{ "_id" : "52209c450364ac1e2b3368fc", "imgsrc" : "bf8633b2e1ca463003c90225.jpg" }"""
  
    
    
    test("格式转换 cmd 语句"){
    val arr = str.split("\n")

   val cmd =   arr.map{str => 
    	log.error("str={}", str)
    	 val jsval =  Json.parse( str)
      
    	 val id = (jsval \"_id" ).as[String]
    	 val imgsrc = (jsval \"imgsrc" ).as[String]
    	log.error( "imgsrc={}" , imgsrc)
    	val(imgId , extension) = imgsrc.split('.') match{
    	  case Array( x ,y ) => ( x , y.toLowerCase())
    	  case  xx => log.error("xx={}",xx) ; ("","")
    	}
    	val set="set"
    	val cmdstr =s"""db.photo.update( {  "_id" : "$id" }, { $$set: {  "imgId" : "$imgId", "extension" : "$extension" } } );"""
    	log.error(cmdstr  )
    	cmdstr
    }.mkString("\n")
    log.error("cmd={}","\n"+cmd)
  }
}