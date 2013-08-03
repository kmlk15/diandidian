package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.mongodb.casbah.MongoConnection
import models.v2._


class CmsServiceTest extends Specification{
  val testdbname = "mytestxx"
  object LL extends CmsServiceComponentImpl {
    override val dbname = "mytestxx"
  }
  
  "Cms service" should {
    
    "PhotoUser CRUD" in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("photouser")
      col.drop()

      val service = LL.cmsService
      
      val userId = "test id"
      val userName = "test name "
      val updateuserName ="new test"
      val updateUserId = "new userid "
        service.getPhotoUserById("not exist")  === None
        service.getPhotoUserByUserId(userId) === None 
        
        val user = PhotoUser("", userId , userName)
        
        val user2 = service.savePhotoUser(user).get
        user2.userName === user.userName
        user2.id  !== user.id
       
        
        
        val user3 = service.getPhotoUserById( user2.id )
        
        val user4 = service.getPhotoUserByUserId( user.userId )
        
        val user5 = service.getPhotoUserByUserName( user.userName)
        
        user3 !== None 
        
        user3 === user4 
        user4 === user5 
        
        
        val updateuser = PhotoUser( user2.id , updateuserName , updateUserId )
         
        val u1 = service.updatePhotoUser( updateuser)
        
        u1 !== None 
        
        u1.get.id === user2.id 
        u1.get.userName === updateuserName
        
        
        val x = service.delPhotoUserById( user2.id )
        
        x === 1 
        
        val deletedUser = service.getPhotoUserById( user2.id )
        deletedUser  === None 
        
        
    }
    
  }
}