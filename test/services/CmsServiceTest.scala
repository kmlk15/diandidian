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
     
     "Location  CRUD" in new WithApplication {
    	 import models.LocationForm
    	 
      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("locationform")
      col.drop()

      val service = LL.cmsService
      val name1 = "locatoin 1 "
      val name2 = "location 2"
       
      service.getLocationById("not exist") === None
       
      val location1 = LocationForm (id=None, name = name1)
      val location2Option = service.saveLocation(location1) 
      location2Option must beSome
      val location2 = location2Option.get
      location2.id must beSome
      location2.name ===  name1 
      
      val location3 = LocationForm( name = name1)
      service.saveLocation(location3) must beNone
      
      val location4 = LocationForm( name = name2)
      val location5Option =  service.saveLocation(location4)
      location5Option must beSome
      val location5 = location5Option.get
      
     
      val location6 = service.getLocationByName( name2).get 
      location6 === location5 
      
      
      val location7 = location6.copy( name="new name")
      val location8 = service.updateLocation(location7).get
      location8.id ==== location7.id
      
      val location9 = location6.copy( name = name1 )
      
      service.updateLocation(location9) must beNone
      
      service.delLocationById( location2.id.get) === 1 
      
      
       }
     
     "Photo  CRUD" in new WithApplication {
    	 import models.Photo
    	 import models.LocationForm
    	 
      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("pciture")
      col.drop()

      val service = LL.cmsService
      service.getPhotoById("not exist") must beNone
      
      val photoUser =service.savePhotoUser(PhotoUser(userName="photUser" , userId="http://www.diandidian.com")).get
      
      val location =service.saveLocation( LocationForm( name="testlocation")).get
      val imgsrc="/tmp/x.jpg"
      val imgurl="http://www.diandidian.com/"
       val brief = "this is brief"

      val picture = Photo(
        locationId = location.id.get,
        userId = photoUser.id,
        imgsrc = imgsrc,
        imgurl = imgurl,
        brief = brief)
        
      val picture2 = service.savePhoto(picture).get
      picture2.id must beSome
      
      val picture3 = service.getPhotoById( picture2.id.get ).get
      picture2 === picture3 
      
      val pictureList = service.getPhotoList(location.id.get)
      pictureList.size === 1 
      pictureList === List( picture2 )
      
      service.updatePhoto(picture) must beNone
        
      
     val picture4 = service.updatePhoto( picture2.copy( imgsrc ="new imgsrc")).get
     picture4.id === picture2.id
     picture4.imgurl === imgurl
     
     service.delPhotoById(  picture2.id.get) === 1 
     
     }
       
  }
}