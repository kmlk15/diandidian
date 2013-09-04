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

object UserPhotos extends Controller  with  _root_.controllers.UserAuthTrait with services.FileUploadService {

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
   
  

  def userId( session :Session) = session.get("userId").getOrElse("")
  
  def avatar( session: Session ) = session.get("avatar").getOrElse("")
  
  def usertype( session: Session ) = session.get("userType").getOrElse("")
  

  def location(locationId: String) = service.getLocationById(locationId)

  def list() = isAuthenticated { username =>
    implicit request =>
      
        
          val list = service.getPhotoListByUserId(  userId(session) )
          //Ok( Json.toJson(list))

          Ok(views.html.cms.uesrPhotos( list))
       
      

  }

  def add(locationId: String) = isAuthenticated { username =>
    implicit request =>
      location(locationId) match {
        case None => NotFound
        case Some(location) => {
          
          implicit val locationImpl = location
          Ok(views.html.cms.userPhotoEdit(None, PhotoHelp.form.fill(Photo(locationId = locationId))))
        }
      }
  }

 

 

  def save(locationId: String) = isAuthenticated { username =>
    implicit request =>
     
      implicit val locationImpl = location(locationId).get

      PhotoHelp.form.bindFromRequest.fold(
        errors => {

          log.debug("errors={}", errors)
          Ok(views.html.cms.userPhotoEdit(None, errors))
        },
        photo => {
         import  org.bson.types.ObjectId
          val id = new ObjectId().toString
         val imgId = id 
          val extension: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseDetailPageFile(_, photo.atHomepage, imgId) }).getOrElse("")
 
          val photo2 = service.savePhoto(photo.copy(id = Some(id),imgId = imgId ,extension=extension, userId = userId(session ) , username = username,
          avatar =avatar( session) , atHomepage = false  , uploadtype= usertype( session)    )    )

          //Ok( Json.prettyPrint( Json.toJson( photo2 ))) 
          Redirect(routes.UserPhotos.list())
        })
  }

  def edit(id: String) = isAuthenticated { username =>
    implicit request =>
      service.getPhotoById(id) match {
        
        case Some(photo)  if( photo.userId == userId( session) )=> {
        
          implicit val locationImpl = location(photo.locationId).get

          Ok(views.html.cms.userPhotoEdit(photo.id, PhotoHelp.form.fill(photo), msg = "", imgsrc = photo.detailpagesmallImg))
        }
        case _  => NotFound
      }
  }

  def update(id: String) = isAuthenticated { username =>
    implicit request =>
      service.getPhotoById(id) match {
      
        case Some(originPhoto) if ( originPhoto.userId == userId( session))=> {
          val locationId = originPhoto.locationId

         
          implicit val locationImpl = location(locationId).get

          PhotoHelp.form.bindFromRequest.fold(
            errors => {
              log.debug("errors={}", errors)
              Ok(views.html.cms.userPhotoEdit(None, errors))
            },
            photo => {
               val imgId =  id
              val extension: String = request.body.asMultipartFormData.flatMap(data => data.file("imgsrc").map { parseDetailPageFile(_, photo.atHomepage ,imgId) }).getOrElse("")

              val updatePhoto = if (extension != "") {
                photo.copy(imgId = imgId, extension = extension , id = originPhoto.id)
              } else {
                photo.copy(id = originPhoto.id, imgId = originPhoto.imgId)
              }

              log.debug("updatePhoto={}", updatePhoto)
              
              val photo2 = service.updatePhoto(updatePhoto.copy(  userId = userId(session ) , username = username,
          avatar =avatar( session) , atHomepage = false  , uploadtype= usertype( session)  ))

              //Ok( Json.prettyPrint( Json.toJson( photo2 ))) 
              Redirect(routes.UserPhotos.list())
            })

        }
          case _ => NotFound
      }

  }

 



  def del(id: String) = isAuthenticated { username =>
    implicit request =>
      val photo = service.getPhotoById(id)
      photo match {
        case Some(p) if( p.userId  == userId( session ) ) => {
          if (service.delPhotoById(id) == 1) {
            //删除实际的文件
            removeFile(p.imgId , p.extension , p.atHomepage)
          }
          Redirect(routes.UserPhotos.list())
        }
          case _ => NotFound
      }
  }

}