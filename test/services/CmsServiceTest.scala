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
    
    
     "Category  CRUD" in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("category")
      col.drop()

      val service = LL.cmsService
      
      val name = "test name "
      val enName = "test  englisht name "
        val level = 1 
      val updateName ="new name  "
      val updateEnName = "new name  named "
      val updateLevel = 2 
        
      service.getCategoryById( "not exist ") === None
        
      
      val c1 = Category( name = name , enName = enName , level = level )
      val c2 = service.saveCategory(c1 ) 
      c2  must beSome
      c2.get.name === name 
      c2.get.enName === enName 
      service.saveCategory( c1 ) must beNone
      
      val c3 = service.getCategoryById( c2.get.id )
      c3 must beSome
      
      c3 === c2 
      
      val c4 = service.updateCategory( c2.get.copy( name = updateName))
      c4 must beSome
      
      val c5 = service.saveCategory( c2.get.copy( name = updateName))
      c5 must beNone
      
      val x = service.delCategoryById(  c2.get.id)
      x === 1 
      val c6 = service.getCategoryByName(  name )
      c6 must beNone
      
      //测试 c1.name = n1 , c2.name = n2 ,  c2.update name = c1 , return none
      
      val c10 = Category( name= name )
      val c10a = service.saveCategory( c10 ) 
      c10a must beSome
      
      val c20 = Category( name = updateName)
      val c20a = service.saveCategory( c20 )
      c20a must beSome 
      
      //更新是， 不允许 重名  
      val c30 = c20a.get.copy( name = name  )
      val c30a = service.updateCategory( c30 )
      c30a must beNone
      
      val c40 = c20a.get.copy( name = updateName , level =3  )
      val c40a = service.updateCategory( c40 )
      c40a must beSome
      
     }
     
  }
}