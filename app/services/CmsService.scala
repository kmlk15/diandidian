package services

import models.LocationForm
import models.Photo
import models.v2.PhotoUser
import models.v2.Category
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.WriteConcern
import org.slf4j.LoggerFactory
import com.mongodb.DBObject

trait CmsServiceComponent {

  val cmsService: CmsService

  trait CmsService {

    def savePhotoUser(user: PhotoUser): Option[PhotoUser]

    def updatePhotoUser(user: PhotoUser): Option[PhotoUser]

    def getPhotoUserById(id: String): Option[PhotoUser]

    def getPhotoUserByUserId(userId: String): Option[PhotoUser]

    def getPhotoUserByUserName(userName: String): Option[PhotoUser]

    def delPhotoUserById(id: String): Int

    def getPhotoUsers(): List[PhotoUser]

    def saveCategory(category: Category): Option[Category]
    def updateCategory(category: Category): Option[Category]
    def getCategoryList(parent: String = ""): List[Category]

    def delCategoryById(id: String): Int

    def getCategoryById(id: String): Option[Category]

    def saveLocation(location: LocationForm): Option[LocationForm]
    def updateLocation(location: LocationForm): Option[LocationForm]
    def getLocationList(): List[LocationForm]

    def delLocationById(id: String): Int

    def getLocationById(id: String): Option[LocationForm]

    def savePhoto(photo: Photo): Option[Photo]

    def updatePhoto(photo: Photo): Option[Photo]

    def getPhotoList(locationId: String): List[Photo]
    
    def getAdminUploadPhotoList(locationId: String): List[Photo] 
    
    def getPhotoListByUserId(userId: String): List[Photo]

    def delPhotoById(id: String): Int

    def getPhotoById(id: String): Option[Photo]

    def getPhotoByImgId(imgId: String): Option[Photo]

  }
}

trait CmsServiceComponentImpl extends CmsServiceComponent {

  this: CmsServiceComponent =>
  val log = LoggerFactory.getLogger(classOf[CmsServiceComponentImpl])
  val dbname = "topo"
  override val cmsService = new CmsService {
    lazy val photoUserMongoClient = mongoService.getMongoService[PhotoUser]("photouser", dbname)
    lazy val categoryMongoClient = mongoService.getMongoService[Category]("category", dbname)

    lazy val locationMongoClient = mongoService.getMongoService[LocationForm]("locationform", dbname)
    lazy val photoMongoClient = mongoService.getMongoService[Photo]("photo", dbname)
    /**
     * 如果 已经存在 该 userId ， 则不覆盖，
     * 需要客户端 明确调用  update 覆盖
     */
    def savePhotoUser(user: PhotoUser): Option[PhotoUser] = {

      getPhotoUserByUserId(user.userId) match {
        case None => {
          val id = new ObjectId().toString
          val w1 = user.copy(id = id)
          photoUserMongoClient.insert(w1)
          Some(w1)
        }
        case Some(uu) => None
      }
    }

    def getPhotoUser(q: DBObject): Option[PhotoUser] = {

      photoUserMongoClient.find(q).headOption

    }
    def getPhotoUserByUserId(userId: String): Option[PhotoUser] = {

      val q = MongoDBObject()
      q.put("userId", userId)
      getPhotoUser(q)

    }

    def getPhotoUserByUserName(userName: String): Option[PhotoUser] = {

      val q = MongoDBObject()
      q.put("userName", userName)
      getPhotoUser(q)

    }

    def getPhotoUserById(id: String): Option[PhotoUser] = {
      val q = MongoDBObject()
      q.put("_id", id)
      getPhotoUser(q)

    }

    def delPhotoUserById(id: String): Int = {
      val q = MongoDBObject()
      q.put("_id", id)
      photoUserMongoClient.delete(q, WriteConcern.Normal)

    }

    def updatePhotoUser(user: PhotoUser): Option[PhotoUser] = {
      getPhotoUserById(user.id) match {
        case None => None
        case Some(uu) => {
          val uu2 = getPhotoUserByUserId(user.userId)
          if (uu2 == None || uu == uu2.get) {
            val w1 = uu.copy(userName = user.userName, userId = user.userId)
            val q = MongoDBObject()
            q.put("_id", w1.id)
            photoUserMongoClient.update(q, w1, false, false, WriteConcern.Normal)
            Some(w1)
          } else {
            //存在同样 userid， 并且不是自己 
            None
          }
        }
      }

    }

    def getPhotoUsers(): List[PhotoUser] = {
      val q = MongoDBObject()

      val lista = photoUserMongoClient.find(q)
      lista.sortBy((u: PhotoUser) => u.userName)
    }

    def saveCategory(category: Category): Option[Category] = {
      getCategoryByName(category.name) match {
        case None => {
          val id = new ObjectId().toString
          val cat = category.copy(id = id)
          categoryMongoClient.insert(cat)
          Some(cat)
        }
        case Some(c) => None
      }

    }

    def updateCategory(category: Category): Option[Category] = {

      getCategoryById(category.id) match {
        case None => None
        case Some(c) => {
          val cbyName = getCategoryByName(category.name)
          if (cbyName == None || cbyName.get == c) {
            val q = MongoDBObject()
            q.put("_id", c.id)
            categoryMongoClient.update(q, category, false, false, WriteConcern.Normal)
            Some(category)
          } else {
            //不允许 重名 
            None
          }
        }
      }

    }

    def getCategoryList(parent: String = ""): List[Category] = {
      val q = MongoDBObject("parentId" -> parent)

      val lista = categoryMongoClient.find(q)
      val listb = lista.map(parent => parent :: getCategoryList(parent.id))

      listb.flatten
    }

    def delCategoryById(id: String): Int = {
      val q = MongoDBObject()
      q.put("_id", id)
      categoryMongoClient.delete(q, WriteConcern.Normal)

    }

    def getCategoryById(id: String): Option[Category] = {
      val q = MongoDBObject()
      q.put("_id", id)
      categoryMongoClient.find(q).headOption
    }

    def getCategoryByName(name: String): Option[Category] = {
      val q = MongoDBObject()
      q.put("name", name)
      categoryMongoClient.find(q).headOption
    }

    def saveLocation(location: LocationForm): Option[LocationForm] = {

      getLocationByName(location.name) match {
        case None => {
          val id = new ObjectId().toString
          val location2 = location.copy(id = Some(id))
          locationMongoClient.insert(location2)
          Some(location2)
        }
        case Some(c) => None
      }

    }

    def updateLocation(location: LocationForm): Option[LocationForm] = {

      getLocationById(location.id.getOrElse("")) match {
        case None => None
        case Some(c) => {
          val cbyName = getLocationByName(location.name)
          if (cbyName == None || cbyName.get == c) {
            val q = MongoDBObject()
            q.put("_id", c.id)
            locationMongoClient.update(q, location, false, false, WriteConcern.Normal)
            Some(location)
          } else {
            //不允许 重名 
            None
          }
        }
      }

    }

    def getLocationList(): List[LocationForm] = {
      val q = MongoDBObject()
      val lista = locationMongoClient.find(q)
      lista.sortBy((location: LocationForm) => location.name)
    }

    def delLocationById(id: String): Int = {
      val q = MongoDBObject()
      q.put("_id", id)
      locationMongoClient.delete(q, WriteConcern.Normal)
    }

    def getLocationById(id: String): Option[LocationForm] = {

      val q = MongoDBObject()
      q.put("_id", id)
      locationMongoClient.find(q).headOption

    }

    def getLocationByName(name: String): Option[LocationForm] = {

      val q = MongoDBObject()
      q.put("name", name)
      locationMongoClient.find(q).headOption

    }

    def savePhoto(photo: Photo): Option[Photo] = {
      
      
      photoMongoClient.insert(photo)
      Some(photo)
    }

    def updatePhoto(photo: Photo): Option[Photo] = {
      photo.id match {
        case None => None
        case Some(idStr) => {
          getPhotoById(idStr) match {
            case None => None
            case Some(p) => {
              val q = MongoDBObject()
              q.put("_id", idStr)
              photoMongoClient.update(q, photo, false, false, WriteConcern.Normal)
              Some(photo)
            }
          }
        }
      }

    }

    def getPhotoList(locationId: String): List[Photo] = {

      val q = MongoDBObject()
      q.put("locationId", locationId)
      photoMongoClient.find(q)

    }

    def getAdminUploadPhotoList(locationId: String): List[Photo] = {

      val q = MongoDBObject()
      q.put("locationId", locationId)
      q.put("uploadtype" , "admin" )
      photoMongoClient.find(q)

    }
      
    
    def getPhotoListByUserId(userId: String): List[Photo] = {

      val q = MongoDBObject()
      q.put("userId", userId)
      photoMongoClient.find(q)

    }

    def delPhotoById(id: String): Int = {
      val q = MongoDBObject()
      q.put("_id", id)
      photoMongoClient.delete(q, WriteConcern.Normal)
    }

    def getPhotoById(id: String): Option[Photo] = {
      val q = MongoDBObject()
      q.put("_id", id)
      photoMongoClient.find(q).headOption

    }

    def getPhotoByImgId(imgId: String): Option[Photo] ={
      val q = MongoDBObject()
      q.put("imgId", imgId)
      photoMongoClient.find(q).headOption

    }
    
  }

}