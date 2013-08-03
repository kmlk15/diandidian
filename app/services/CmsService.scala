package services

import models.v2.PhotoUser
import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.WriteConcern
import org.slf4j.LoggerFactory
import com.mongodb.DBObject


trait CmsServiceComponent {

  val  cmsService: CmsService

  trait CmsService {

    def savePhotoUser(user: PhotoUser): PhotoUser
    
	def updatePhotoUser(user: PhotoUser): Option[PhotoUser]
    
    def getPhotoUserById(id: String): Option[PhotoUser]
    
    def getPhotoUserByUserId( userId: String): Option[PhotoUser]
    
   def getPhotoUserByUserName( userName : String): Option[PhotoUser]
    
    def delPhotoUserById(id: String): Int

    def getPhotoUsers( ) : List[ PhotoUser ]
     
  }
}

trait CmsServiceComponentImpl extends CmsServiceComponent {
	
  this: CmsServiceComponent =>
     val log = LoggerFactory.getLogger( classOf[CmsServiceComponentImpl]  )
  val dbname = "topo"
  override val cmsService = new CmsService {
    lazy val photoUserMongoClient = mongoService.getMongoService[PhotoUser]("photouser", dbname)
    
    /**
     * 如果 已经存在 该 userId ， 则不覆盖，
     * 需要客户端 明确调用  update 覆盖 
     */
    def savePhotoUser(user: PhotoUser): PhotoUser = {
      
      getPhotoUserByUserId( user.userId) match{
        case None => {
           val userId = new ObjectId().toString
	       val w1 = user.copy(id = userId)
	       photoUserMongoClient.insert(w1)
	       w1
        }
        case Some( uu ) => uu 
      }
    }

    
    def getPhotoUser ( q: DBObject) : Option[PhotoUser] ={
       
         photoUserMongoClient.find(q).headOption
       
    }
    def getPhotoUserByUserId(userId: String): Option[PhotoUser] ={
      
      val q = MongoDBObject()
      q.put("userId", userId)
      getPhotoUser( q )
      
      
    }
    
    def getPhotoUserByUserName(userName: String): Option[PhotoUser] ={
      
      val q = MongoDBObject()
      q.put("userName", userName)
      getPhotoUser( q )
      
      
    }
    
      def getPhotoUserById( id: String): Option[PhotoUser] ={
      val q = MongoDBObject()
      q.put("_id", id)
      getPhotoUser( q )
      
    }
      
       def delPhotoUserById( id: String): Int ={
      val q = MongoDBObject()
      q.put("_id", id)
      photoUserMongoClient.delete( q , WriteConcern.Normal )
      
    }     
    
    	def updatePhotoUser(user: PhotoUser): Option[PhotoUser] ={
    	  getPhotoUserById ( user.id ) match{
    	    case None => None
    	    case Some( uu) =>{ 
    	      val w1 = uu.copy(  userName = user.userName , userId = user.userId )
          val q = MongoDBObject()
          q.put("_id", w1.id)
          photoUserMongoClient.update(q, w1, false, false, WriteConcern.Normal)
    	      Some(w1)
    	    }
    	  }
    	  
    	  
    	}
    
    def getPhotoUsers( ) : List[ PhotoUser ] = {
      val q = MongoDBObject()
      photoUserMongoClient.find( q  )
      
    }
    
    
    
    
  }
  
}