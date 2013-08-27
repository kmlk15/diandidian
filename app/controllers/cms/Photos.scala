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

object Photos extends Controller with AuthTrait  with services.FileUploadService{

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
 

  def photoUserList = service.getPhotoUsers()

  def location(locationId: String) = service.getLocationById(locationId)

  def list(locationId: String) = isAuthenticated { username =>
    implicit request =>
      location(locationId) match {
        case None => NotFound
        case Some(location) => {
          val list = service.getAdminUploadPhotoList(locationId)
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
          val photoUser = service.getPhotoUserById(   photo.userId).getOrElse( PhotoUser())
          val photo2 = service.savePhoto(photo.copy(imgsrc = filename ,
               username = photoUser.userName ,
               avatar =  photoUser.userId   , atHomepage = ( photo.atHomepage && filename!="")
          ))

          //Ok( Json.prettyPrint( Json.toJson( photo2 ))) 
          Redirect(routes.Photos.list(locationId))
        })
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
                removeFile(originPhoto.imgsrc ,originPhoto.atHomepage )
                if (photo.atHomepage) {
                  service.updateLocation(locationImpl.copy(photo = "266_" + filename))
                }
                photo.copy(imgsrc = filename, id = originPhoto.id)
              } else {
                if (photo.atHomepage && !originPhoto.atHomepage && originPhoto.imgsrc!= "") {

                  /**
                   * 这里需要处理 有可能还没有生成  atHomePage 需要的图片的情况
                   * 就是原来  atHomepage == false , 在不改变图片的情况下，设置为  atHomepage == true
                   * 0 判断图片是否存在，
                   * 1 下载 原图，
                   * 2 在本地执行 convert
                   * 3 再上传 图片
                   */
                  val filename = "266_" + originPhoto.imgsrc
                  val (meta, file266) = getObjectAndSavetoLocal(filename)
                  if (meta == null) {
                    log.debug("file={}, not at s3", filename)
                    val (origmeta, file) = getObjectAndSavetoLocal(originPhoto.imgsrc)
                    if (origmeta != null) {
                      if (resize(file.getAbsolutePath(), file266.getAbsolutePath(), 266, 262)) {
                        upload2S3(filename)
                        service.updateLocation(locationImpl.copy(photo = filename))
                      }else{
                        log.error("resize or  upload to s3 error ")
                      }

                    }else{
                      log.error("orig photo not exists. {}" , originPhoto.imgsrc )
                    }
                  }else{
                    log.debug("266 缩略图存在 {}",filename )
                     service.updateLocation(locationImpl.copy(photo = filename))
                  }
                  
                  
                }
                
                /**
               * TODO 取消设置为首页！
                 */
                
                
                if( originPhoto.imgsrc == ""){
                	photo.copy(id = originPhoto.id, imgsrc = originPhoto.imgsrc , atHomepage = false )
                }else{
                  photo.copy(id = originPhoto.id, imgsrc = originPhoto.imgsrc)
                }
              }
              val photoUser = service.getPhotoUserById( photo.userId).getOrElse( PhotoUser())

              	val updatePhoto2 = updatePhoto.copy( username = photoUser.userName , avatar =  photoUser.userId    )

              log.debug("updatePhoto={}", updatePhoto2)
              
              val photo2 = service.updatePhoto(updatePhoto2)

              //Ok( Json.prettyPrint( Json.toJson( photo2 ))) 
              Redirect(routes.Photos.list(locationId))
            })

        }
      }

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

            removeFile(p.imgsrc , p.atHomepage)
          }

          Redirect(routes.Photos.list(p.locationId))
        }
      }
  }

}