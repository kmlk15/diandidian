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


/**
 * 文件 上传和resize
 */
trait FileUploadService {
  
  
  val log :Logger
  
   val pathprefix = new File("public/tmp/").getAbsolutePath() + "/"
  log.debug("pathprefix={}", pathprefix)

  val proxy = if (System.getenv("http_proxy") != null) true else false

  val s3client = new AwsS3Client(pathprefix, proxy)
  
  /**
   * 调用 convert 命令， 对图像进行 resize
   * 精确的大小
   */

  def resize(from: String, to: String, w: Int, h: Int) = {
    import scala.sys.process._
    val file = new File(from)
    if (file.exists()) {
      /**
       * convert -resize  1280x768   $img    1280.png ;
       */
      Seq("convert", from, "-resize", w + "x" + h + "^", "-gravity", "center", "-extent", w + "x" + h, to).! == 0
    }
  }

  /**
   * 可以处理成一个边 ， 保持比例
   */
  def resize1(from: String, to: String, w: Int, h: Int) = {
    import scala.sys.process._
    val file = new File(from)
    if (file.exists()) {
      /**
       * convert -resize  1280x768   $img    1280.png ;
       */
      Seq("convert", from, "-resize", w + "x" + h, to).! == 0
    }
  }

  def upload2S3(filename: String , atHomepage: Boolean ) = {
    s3client.upload(filename)
    s3client.upload("780_" + filename)
    s3client.upload("102_" + filename)
    if (atHomepage) {
      s3client.upload("266_" + filename)
    }
  }
  
    def removeFile( filename: String , atHomepage:Boolean  ) = {
    

    new File(pathprefix + filename).delete()
    new File(pathprefix + "780_" + filename).delete()
    new File(pathprefix + "102_" + filename).delete()
    if (atHomepage) {
      new File(pathprefix + "266_" + filename).delete()
    }
  log.debug(" remove file to s3 , filename=" + filename )
    s3client.remove(filename)
    s3client.remove("780_" + filename)
    s3client.remove("102_" + filename)
    if (atHomepage) {
      s3client.remove("266_" + filename)
    }
log.debug(" remove file to s3 , over"  )
  }
    
      def parseFile(picture: FilePart[TemporaryFile], atHomepage: Boolean): String = {

    import java.io.File
    import org.apache.commons.io.FilenameUtils
    import org.bson.types.ObjectId
    val filename = new ObjectId().toString.reverse + "." + FilenameUtils.getExtension(picture.filename)
    log.debug("img filename={}", filename)
    val contentType = picture.contentType
    picture.ref.moveTo(new File(pathprefix + filename))
    if (atHomepage) {
      //创建  for home page 
      val home = "266_" + filename
      resize(pathprefix + filename, pathprefix + home, 266, 262)

    }

    // 大图和小图
    val large = "780_" + filename
    resize(pathprefix + filename, pathprefix + large, 780, 435)
    val small = "102_" + filename
    resize(pathprefix + filename, pathprefix + small, 102, 57)
    //将图片保存到 s3 
    /**
     * 这里 可能会很慢， 特别是在 测试环境中
     */
    log.debug(" upload file to s3 , filename=" + filename )
    upload2S3( filename , atHomepage )
      log.debug(" upload file to s3 , over" )
    filename

  }
}

class AwsS3Client( pathprefix: String=""  , useProxy: Boolean = false ) {
 val log = LoggerFactory.getLogger(classOf[AwsS3Client])
  val cred = new BasicAWSCredentials("AKIAIISCPZLWZ4CCRD3A", "4pEN9ikkShjm/h1ST7fqYRjneaqvJdaiWBcwJKl1")

  val config = new ClientConfiguration()
 if( useProxy ){
  config.setProxyHost("127.0.0.1")
  config.setProxyPort(3128)
 }
  val bucket = "diandidian"

  val client = new AmazonS3Client(cred, config)

   
  def upload(filename: String) = {

    val file = new File(pathprefix +  filename  )
    if(file.exists() ){
       log.debug("upload filename={}, filesize={}" , filename , file.length() )
	    val putobj = new PutObjectRequest(bucket, filename, file)
	    putobj.withCannedAcl(CannedAccessControlList.PublicRead)
	    val res = client.putObject(putobj)
	    log.debug("upload over")
	    res
    }
  }

  
  def remove( filename: String ) ={
     log.debug("remove filename={},  " , filename   )
    client.deleteObject( bucket  , filename)
    log.debug("remove over")
  }

}