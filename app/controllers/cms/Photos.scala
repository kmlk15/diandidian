package controllers.cms

import java.io.File
import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue
import play.api.libs.json.Json
import org.slf4j.LoggerFactory


import models.Photo
import models.PhotoHelp
import models.PhotoHelp._

import models.v2.PhotoUser


import play.api.libs.json.Json

object Photos  extends Controller {

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
  
  def photoUserList = service.getPhotoUsers()
  
  def location( locationId: String)  = service.getLocationById( locationId )
  
  def list(locationId: String ) = Action{ implicit request =>
    location( locationId) match{
      case None => NotFound
      case Some( location ) =>{
        val list = service.getPhotoList(locationId)
        Ok( Json.toJson(list))
      }
    }
    
    
  }
  
  def add(locationId: String ) = Action{implicit request =>
    location( locationId) match{
      case None => NotFound
      case Some( location ) =>{
        implicit val userList = service.getPhotoUsers()
        implicit val locationImpl = location
        Ok( views.html.cms.photoEdit(None , PhotoHelp.form.fill(  Photo( locationId = locationId ))))
      }
    }
  }
  
  /**
   * 调用 convert 命令， 对图像进行 resize
   * 精确的大小
   */
    
  def resize( from : String, to: String , w:Int , h:Int )={
    import scala.sys.process._
    val file = new File(from)
    if (file.exists()) {
      /**
       * convert -resize  1280x768   $img    1280.png ;
       */
      Seq("convert", from , "-resize", w+"x"+h + "^", "-gravity", "center" ,"-extent",  w+"x"+h ,  to ).! == 0
  }
 }
  
   /**
    * 可以处理成一个边 ， 保持比例
    */
  def resize1( from : String, to: String , w:Int , h:Int )={
    import scala.sys.process._
    val file = new File(from)
    if (file.exists()) {
      /**
       * convert -resize  1280x768   $img    1280.png ;
       */
      Seq("convert", from , "-resize", w+"x"+h ,    to ).! == 0
  }
 }
  
  
  def save(locationId: String) = Action(parse.multipartFormData){implicit request =>
    PhotoHelp.form.bindFromRequest.fold(
      errors =>{
        implicit val userList = service.getPhotoUsers()
        implicit val locationImpl =  location( locationId ).get
        log.debug( "errors={}", errors )
        Ok(views.html.cms.photoEdit(None, errors) ) 
      },
      photo => { 
         val filename: String = request.body.file("imgsrc").map{picture =>
          import java.io.File
          import org.apache.commons.io.FilenameUtils
          import org.bson.types.ObjectId
          val filename = new ObjectId().toString.reverse  +"."+ FilenameUtils.getExtension( picture.filename )
          val contentType = picture.contentType
          picture.ref.moveTo(new File("/tmp/"+ filename ))
          if( photo.atHomepage){
           //创建  for home page 
            val home = "266_" + filename
           resize("/tmp/"+ filename , "/tmp/"+ home , 266,262)
             
         }
          
           // 大图和小图
           val large = "780_" + filename
           resize("/tmp/"+ filename , "/tmp/"+ large , 780,435)
            val small = "102_" + filename 
           resize("/tmp/"+ filename , "/tmp/"+ small , 102,57)
           //将图片保存到 s3 
           /**
            * 这里 可能会很慢， 特别是在 测试环境中
            */
      
          
          filename
         }.getOrElse("")
         
        
        Ok( Json.prettyPrint( Json.toJson(photo.copy(imgsrc = filename )))) 
        
        }
      )
  }
  
  def edit(id: String) = TODO
  
  def update(id: String ) = TODO
  
  def del(id: String ) = TODO
  
}