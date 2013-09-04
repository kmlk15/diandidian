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
import org.bson.types.ObjectId

object Photos extends Controller with AuthTrait with services.FileUploadService {

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
         val id = new ObjectId().toString
         val imgId = id 
         val homepageExtension: String = request.body.asMultipartFormData.flatMap(data => data.file("homepageimgsrc").map { parseHomePageFile(_,  imgId) }).getOrElse("")
         val detailpageextension: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseDetailPageFile(_, photo.atHomepage && homepageExtension== "" , imgId) }).getOrElse("")
        if( homepageExtension !="" && detailpageextension!="" && detailpageextension!=homepageExtension){
          log.error(" 2张图片都上传了， 但是 后缀名不一样 homepageExtension={}, detailpageextension={}" ,homepageExtension,detailpageextension,"" )
        }
         val extension = if(detailpageextension !="") detailpageextension else  homepageExtension
          log.debug("extension={}" ,extension )
          
          if (photo.atHomepage && ( extension!= "") ) {
            service.updateLocation(locationImpl.copy(photo = PhotoHelp.homepageImg(imgId, extension)))
          }
          val photoUser = service.getPhotoUserById(photo.userId).getOrElse(PhotoUser())
          val photo2 = service.savePhoto(photo.copy(id = Some(id), imgId = imgId, extension=extension,
            username = photoUser.userName,
             atHomepage = (photo.atHomepage && extension != "")))

            log.debug("saved photo={}" , photo2 )
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

          Ok(views.html.cms.photoEdit(photo.id, PhotoHelp.form.fill(photo), msg = "", imgsrc = photo.detailpagesmallImg))
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
              val imgId =  id
              
              /**
               * 1 只上传了  homepage img  这个 只更新了 266/193 图片， 不会有其他影响
               * 2 只上传了 detailpage img， 这个 需要判断 是否 需要生成 266/193 的图片， 这个 有点  难
               * 生成 266 图片的 条件， 原来的 266 不存在， 而且当前 atHomePage == true， 才需要生成 266 的图片
               * 这里的问题时， 如果有266的图片， 如果想 替换成 Detail 的图片， 必须 自己 重新上传。
               * 
               * 3 都上传了 这个 最简单， 
               * 4 都没有传 这个也可以， 
               * 
               * 这2个  extension 必须一样，
               */
               val homepageExtension: String = request.body.asMultipartFormData.flatMap(data => data.file("homepageimgsrc").map { parseHomePageFile(_,  imgId) }).getOrElse("")
        
              val detailpageextension: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseDetailPageFile(_, photo.atHomepage && homepageExtension=="",imgId) }).getOrElse("")

               if( homepageExtension !="" && detailpageextension!="" && detailpageextension!=homepageExtension){
            	   		log.error(" 2张图片都上传了， 但是 后缀名不一样 homepageExtension={}, detailpageextension={}" ,homepageExtension,detailpageextension,"" )
               }
               val extension = if(detailpageextension !="") detailpageextension else  homepageExtension
            		 log.debug("extension={}" ,extension )
              val updatePhoto = if (extension != "") {
                 
                if (photo.atHomepage) {
                  service.updateLocation(locationImpl.copy(photo =  PhotoHelp.homepageImg(imgId, extension)))
                }
                photo.copy( imgId=imgId , extension = extension, id = originPhoto.id )
              } else {
                if (photo.atHomepage && !originPhoto.atHomepage && originPhoto.imgId != "") {

                  /**
                   * 这里需要处理 有可能还没有生成  atHomePage 需要的图片的情况
                   * 就是原来  atHomepage == false , 在不改变图片的情况下，设置为  atHomepage == true
                   * 0 判断图片是否存在，
                   * 1 下载 原图，
                   * 2 在本地执行 convert
                   * 3 再上传 图片
                   */
                  val filename = originPhoto.homepageImg
                  val (meta, file266) = getObjectAndSavetoLocal(filename)
                  if (meta == null) {
                    log.debug("file={}, not at s3", filename)
                    val (origmeta, file) = getObjectAndSavetoLocal(originPhoto.detailPageOrignImg)
                    if (origmeta != null) {
                      if (resize(file.getAbsolutePath(), file266.getAbsolutePath(), PhotoHelp.homepageImgsize)) {
                        upload2S3(filename)
                        service.updateLocation(locationImpl.copy(photo = filename))
                      } else {
                        log.error("resize or  upload homepage image  to s3 error ")
                      }
                      
                      if (resize(file.getAbsolutePath(), pathprefix + originPhoto.planpageImg, PhotoHelp.planpageImgsize)) {
                        upload2S3( originPhoto.planpageImg)
                        
                      } else {
                        log.error("resize or  upload  planpage image  to s3 error ")
                      }

                    } else {
                      log.error("orig photo not exists. {}", originPhoto.imgId)
                    }
                  } else {
                    log.debug("266 缩略图存在 {}", filename)
                    service.updateLocation(locationImpl.copy(photo = filename))
                  }

                }

                if (originPhoto.imgId == "none") {
                  photo.copy(id = originPhoto.id, imgId = originPhoto.imgId,  extension =originPhoto.extension,  atHomepage = false)
                } else {
                  photo.copy(id = originPhoto.id, imgId = originPhoto.imgId , extension =originPhoto.extension )
                }
              }

              /**
               *  取消设置为首页！
               *  1 现在没有设置为首页
               *  2 原来是设置为首页的
               *  3 location 的首页图片确实是  这个 photo 设置的
               */
              if (!photo.atHomepage && originPhoto.atHomepage && locationImpl.photo == originPhoto.homepageImg) {
                service.updateLocation(locationImpl.copy(photo = ""))
              }

              val photoUser = service.getPhotoUserById(photo.userId).getOrElse(PhotoUser())

              val updatePhoto2 = updatePhoto.copy(username = photoUser.userName)

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
            if (locationTmp != None && locationTmp.photo.contains(p.homepageImg)) {
              service.updateLocation(locationTmp.copy(photo = ""))
            }
          }
          if (service.delPhotoById(id) == 1) {
            //删除实际的文件

            removeFile(p.imgId,p.extension, p.atHomepage)
          }

          Redirect(routes.Photos.list(p.locationId))
        }
      }
  }

}