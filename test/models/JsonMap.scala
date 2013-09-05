package models


import org.scalatest.FunSuite
import org.slf4j.LoggerFactory
import play.api.libs.json._

class JsonMap extends FunSuite {
  val log = LoggerFactory.getLogger(classOf[JsonMap])

  val str="""{ "_id" : "520757c0e4b0ba2a6baf92e4", "imgsrc" : "3e29fab6a2ab0b4efb757025.jpg" }
{ "_id" : "520757fae4b0ba2a6baf92e6", "imgsrc" : "5e29fab6a2ab0b4e9f757025.jpg" }
{ "_id" : "520755d40364282014c45533", "imgsrc" : "43554c410282463071657025.jpg" }
{ "_id" : "520e296703649d3aaacda0a7", "imgsrc" : "6a0adcaaa3d946308592e025.jpg" }
{ "_id" : "520e33d00364cc9760d2c2ad", "imgsrc" : "ca2c2d0679cc46309b33e025.jpg" }
{ "_id" : "521aff68e4b0e138f3285353", "imgsrc" : "2535823f831e0b4e56ffa125.jpg" }
{ "_id" : "521c3618e4b0441b962bdc6d", "imgsrc" : "c6cdb269b1440b4e6163c125.jpg" }
{ "_id" : "520e291703649d3aaacda0a5", "imgsrc" : "4a0adcaaa3d94630cf82e025.jpg" }
{ "_id" : "520e33a30364cc9760d2c2ab", "imgsrc" : "aa2c2d0679cc4630b833e025.jpg" }
{ "_id" : "520e34e30364cc9760d2c2b1", "imgsrc" : "0b2c2d0679cc4630cc43e025.jpg" }"""
  
    
    
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