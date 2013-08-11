package controllers.cms

import java.io.File
import play.api.libs.json
import play.api.mvc._

import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files.TemporaryFile

import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import models.Photo
import models.PhotoHelp
import models.PhotoHelp._
import models.v2.PhotoUser
import play.api.libs.json.Json
import models.LocationForm

object Photos extends Controller with AuthTrait {

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
  val pathprefix = new File("public/tmp/").getAbsolutePath() + "/"
  log.debug("pathprefix={}", pathprefix)

  val proxy = if (System.getenv("http_proxy") != null) true else false

  val s3client = new AwsS3Client(pathprefix, proxy)

  def photoUserList = service.getPhotoUsers()

  def location(locationId: String) = service.getLocationById(locationId)

  def list(locationId: String) = isAuthenticated { username =>
    implicit request =>
      location(locationId) match {
        case None => NotFound
        case Some(location) => {
          val list = service.getPhotoList(locationId)
          //Ok( Json.toJson(list))

          Ok(views.html.cms.photos(locationId, list))
        }
      }

  }

  def add(locationId: String) = isAuthenticated { username =>
    implicit request =>
      location(locationId) match {
        case None => NotFound
        case Some(location) => {
          implicit val userList = service.getPhotoUsers()
          implicit val locationImpl = location
          Ok(views.html.cms.photoEdit(None, PhotoHelp.form.fill(Photo(locationId = locationId))))
        }
      }
  }

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

  def save(locationId: String) = isAuthenticated { username =>
    implicit request =>
      implicit val userList = service.getPhotoUsers()
      implicit val locationImpl = location(locationId).get

      PhotoHelp.form.bindFromRequest.fold(
        errors => {

          log.debug("errors={}", errors)
          Ok(views.html.cms.photoEdit(None, errors))
        },
        photo => {

          val filename: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseFile(_, photo.atHomepage) }).getOrElse("")

          if (photo.atHomepage && filename != "") {
            service.updateLocation(locationImpl.copy(photo = "266_" + filename))
          }
          val photo2 = service.savePhoto(photo.copy(imgsrc = filename))

          //Ok( Json.prettyPrint( Json.toJson( photo2 ))) 
          Redirect(routes.Photos.list(locationId))
        })
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

  def edit(id: String) = isAuthenticated { username =>
    implicit request =>
      service.getPhotoById(id) match {
        case None => NotFound
        case Some(photo) => {
          implicit val userList = service.getPhotoUsers()
          implicit val locationImpl = location(photo.locationId).get

          Ok(views.html.cms.photoEdit(photo.id, PhotoHelp.form.fill(photo), msg = "", imgsrc = photo.imgsrc))
        }
      }
  }

  def update(id: String) = isAuthenticated { username =>
    implicit request =>
      service.getPhotoById(id) match {
        case None => NotFound
        case Some(originPhoto) => {
          val locationId = originPhoto.locationId

          implicit val userList = service.getPhotoUsers()
          implicit val locationImpl = location(locationId).get

          PhotoHelp.form.bindFromRequest.fold(
            errors => {
              log.debug("errors={}", errors)
              Ok(views.html.cms.photoEdit(None, errors))
            },
            photo => {
              val filename: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseFile(_, photo.atHomepage) }).getOrElse("")

              val updatePhoto = if (filename != "") {
                removeFile(originPhoto)
                if (photo.atHomepage) {
                  service.updateLocation(locationImpl.copy(photo = "266_" + filename))
                }
                photo.copy(imgsrc = filename, id = originPhoto.id)
              } else {
                if (photo.atHomepage) {
                  service.updateLocation(locationImpl.copy(photo = "266_" + originPhoto.imgsrc))
                }

                photo.copy(id = originPhoto.id, imgsrc = originPhoto.imgsrc)
              }

              log.debug("updatePhoto={}", updatePhoto)
              val photo2 = service.updatePhoto(updatePhoto)

              //Ok( Json.prettyPrint( Json.toJson( photo2 ))) 
              Redirect(routes.Photos.list(locationId))
            })

        }
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

  def removeFile(photo: Photo) = {
    val filename: String = photo.imgsrc;

    new File(pathprefix + filename).delete()
    new File(pathprefix + "780_" + filename).delete()
    new File(pathprefix + "102_" + filename).delete()
    if (photo.atHomepage) {
      new File(pathprefix + "266_" + filename).delete()
    }
  log.debug(" remove file to s3 , filename=" + filename )
    s3client.remove(filename)
    s3client.remove("780_" + filename)
    s3client.remove("102_" + filename)
    if (photo.atHomepage) {
      s3client.remove("266_" + filename)
    }
log.debug(" remove file to s3 , over"  )
  }

  def del(id: String) = isAuthenticated { username =>
    implicit request =>
      val photo = service.getPhotoById(id)
      photo match {
        case None => NotFound
        case Some(p) => {
          if (p.atHomepage) {
            val locationTmp = location(p.locationId).get
            if (locationTmp != None && locationTmp.photo.contains(p.imgsrc)) {
              service.updateLocation(locationTmp.copy(photo = ""))
            }
          }
          if (service.delPhotoById(id) == 1) {
            //删除实际的文件

            removeFile(p)
          }

          Redirect(routes.Photos.list(p.locationId))
        }
      }
  }

}