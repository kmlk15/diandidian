package services

import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.ClientConfiguration
import java.io.File
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.GetObjectRequest
import models.PhotoHelp
import java.io.File
import org.apache.commons.io.FilenameUtils
    
/**
 * 文件 上传和resize
 */
trait FileUploadService {

  val log: Logger

  val pathprefix = new File("public/tmp/").getAbsolutePath() + "/"
   
  val proxy = if (System.getenv("http_proxy") != null) true else false

  val s3client = new AwsS3Client(pathprefix, proxy)
  
  val NotUpload =  PhotoHelp.NotUpload
  /**
   * 调用 convert 命令， 对图像进行 resize
   * 精确的大小
   */

  def resize(from: String, to: String,  wh: (Int,Int) ): Boolean = {
    import scala.sys.process._
    val (w,h) = wh
    
    val file = new File(from)
    if (file.exists()) {
      /**
       * convert -resize  1280x768   $img    1280.png ;
       */
      Seq("convert", from, "-resize", w + "x" + h + "^", "-gravity", "center", "-extent", w + "x" + h, to).! == 0
    }else{
      
      false
    }
  }

  /**
   * 可以处理成一个边 ， 保持比例
   */
  def resize1(from: String, to: String,wh: (Int,Int) ) = {
    import scala.sys.process._
    val (w,h) = wh
    val file = new File(from)
    if (file.exists()) {
      /**
       * convert -resize  1280x768   $img    1280.png ;
       */
      Seq("convert", from, "-resize", w + "x" + h, to).! == 0
    }
  }

  def upload2S3(imgId:String,extension:String, atHomepage: Boolean) = {
    
    s3client.upload( PhotoHelp.detailpageOrignImg(imgId, extension) )
    s3client.upload( PhotoHelp.detailpageImg(imgId, extension))
    s3client.upload( PhotoHelp.detailpagesmallImg(imgId, extension) )
    if (atHomepage) {
      s3client.upload( PhotoHelp.homepageImg(imgId, extension)  )
      s3client.upload( PhotoHelp.planpageImg(imgId, extension)  )
    }
  }

  
  def removeFile(imgId:String,extension:String, atHomepage: Boolean) = {

    new File(pathprefix + PhotoHelp.detailpageOrignImg(imgId, extension)).delete()
    new File(pathprefix + PhotoHelp.detailpageImg(imgId, extension)).delete()
    new File(pathprefix + PhotoHelp.detailpagesmallImg(imgId, extension)).delete()
    if (atHomepage) {
      new File(pathprefix + PhotoHelp.homepageImg(imgId, extension)).delete()
      new File(pathprefix + PhotoHelp.planpageImg(imgId, extension)).delete()
    }
    log.debug(" remove file to s3 , imgId=" + imgId)
    s3client.remove(PhotoHelp.detailpageOrignImg(imgId, extension))
    s3client.remove(PhotoHelp.detailpageImg(imgId, extension))
    s3client.remove(PhotoHelp.detailpagesmallImg(imgId, extension))
    if (atHomepage) {
      s3client.remove(PhotoHelp.homepageImg(imgId, extension))
      s3client.remove(PhotoHelp.planpageImg(imgId, extension))
    }
    log.debug(" remove file to s3 , over")
  }

  val  imageFileExtensionSet = Set("jpg", "jpeg","png")
  
  /**
   *  处理普通的图片
   * 
   */
  def parseDetailPageFile(picture: FilePart[TemporaryFile], atHomepage: Boolean , imgId:String): String = {

    val extension = FilenameUtils.getExtension(picture.filename)
    
    val filename =   PhotoHelp.detailpageOrignImg(imgId, extension)
    
    log.debug("img filename={}", filename)
     
    picture.ref.moveTo(new File(pathprefix + filename) , true)
    if (atHomepage) {
      //创建  for home page 
      val home =  PhotoHelp.homepageImg(imgId, extension)
      
      resize(pathprefix + filename, pathprefix + home, PhotoHelp.homepageImgsize)
      
      val plan = PhotoHelp.planpageImg(imgId, extension)
      
      resize(pathprefix + filename, pathprefix + plan, PhotoHelp.planpageImgsize)
    }

    // 大图和小图
    val large = PhotoHelp.detailpageImg(imgId, extension)
    resize(pathprefix + filename, pathprefix + large, PhotoHelp.detailPageImgsize  )
    val small =  PhotoHelp.detailpagesmallImg(imgId, extension)
    resize(pathprefix + filename, pathprefix + small,  PhotoHelp.detailPagesmallImgsize)
    //将图片保存到 s3 
    /**
     * 这里 可能会很慢， 特别是在 测试环境中
     */
    log.debug(" upload file to s3 , filename=" + filename)
    upload2S3( imgId , extension,  atHomepage)
    log.debug(" upload file to s3 , over")
    
    extension

  }
  
   /**
   *  处理首页的图片
   * 
   */
  def parseHomePageFile(picture: FilePart[TemporaryFile],  imgId:String): String = {

    val extension = FilenameUtils.getExtension(picture.filename)
    val filename =   PhotoHelp.homepageOrignImg(imgId, extension)
    
    log.debug("img filename={}", filename)
     
    picture.ref.moveTo(new File(pathprefix + filename) , true)
    
      //创建  for home page 
      val home =  PhotoHelp.homepageImg(imgId, extension)
      
      resize(pathprefix + filename, pathprefix + home, PhotoHelp.homepageImgsize)
      upload2S3(home )
      
      val plan = PhotoHelp.planpageImg(imgId, extension)
      
      resize(pathprefix + filename, pathprefix + plan, PhotoHelp.planpageImgsize)
     upload2S3(plan )

        
    extension

  }
  
  def getObjectAndSavetoLocal( filename: String  ) : (ObjectMetadata , File)={
    s3client.getObjectAndSavetoLocal( filename)
  }
  
   def upload2S3(filename: String) = {
     s3client.upload(filename)
   }
}

class AwsS3Client(pathprefix: String = "", useProxy: Boolean = false) {
  val log = LoggerFactory.getLogger(classOf[AwsS3Client])
  val cred = new BasicAWSCredentials("AKIAIISCPZLWZ4CCRD3A", "4pEN9ikkShjm/h1ST7fqYRjneaqvJdaiWBcwJKl1")

  val config = new ClientConfiguration()
  if (useProxy) {
    config.setProxyHost("127.0.0.1")
    config.setProxyPort(3128)
  }
  val bucket = "diandidian"

  val client = new AmazonS3Client(cred, config)

  def upload(filename: String) = {

    val file = new File(pathprefix + filename)
    if (file.exists()) {
      log.debug("upload filename={}, filesize={}", filename, file.length())
      val putobj = new PutObjectRequest(bucket, filename, file)
      putobj.withCannedAcl(CannedAccessControlList.PublicRead)
      val res = client.putObject(putobj)
      log.debug("upload over")
      res
    }else{
      log.error( "文件不存在 {}" ,   file.getAbsolutePath())
    }
  }

  def remove(filename: String) = {
    log.debug("remove filename={},  ", filename)
    client.deleteObject(bucket, filename)
    log.debug("remove over")
  }

  def getObjectAndSavetoLocal( filename: String  ) : (ObjectMetadata , File)={
    log.debug("getObjectSavetoLocal filename={},  ", filename)
    val req = new GetObjectRequest(bucket, filename )
    val localFile = new File( pathprefix + filename)
    val meta = try{ 
      client.getObject( req , localFile )
    }catch{
      case ex:Exception => null
    }
    log.debug("getObjectSavetoLocal over")
    (meta ,  localFile)
  }
}