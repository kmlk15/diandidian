package services

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import com.mongodb.casbah.MongoConnection
import models._
import models.LocationJsonHelp._

class LocationServiceTest  extends Specification {
  
  val testdbname = "mytestxx"
    
  object LL extends LocationServiceComponentImpl {
    override val dbname = testdbname
  }
  
  
"Location  Service " should {

  
    "Location  Collection CRUD Search" in new WithApplication {

      val mongoDB = MongoConnection()("mytestxx")
      val col = mongoDB("location")
      col.drop()

      val locationService = LL.locationService
      val citya = "香港"  ; val sizea = 5 ;
      val  cityb = "澳门";  val sizeb = 7;
      
      val  cityc = "旧金山"  ; val sizec = 13;
      
      val districtA="d1"; val districtB="d2" ; val districtC = "d3"
       val districtList = List("d1","d2","d3" )
        
     
      val cityaList =  for( i <- 1 to sizea )yield {
        defaultLocation.copy( name="a1" +"_" + i  , address =defaultAddress.copy( city= citya , district =districtList( i % districtList.size )  )  )
      }
      cityaList.foreach{ location => locationService.save(location )}
      
      val citybList =  for( i <- 1 to sizeb )yield {
        defaultLocation.copy( name="b1" +"_" + i  , address =defaultAddress.copy( city= cityb)  )
      }
      citybList.foreach{ location => locationService.save(location )}
      
       val citycList =  for( i <- 1 to sizec )yield {
        defaultLocation.copy( name="c1" +"_" + i  , address =defaultAddress.copy( city= cityc)  )
      }
      citycList.foreach{ location => locationService.save(location )} 
      
      
      val all = locationService.list( )
      all.size  === ( sizea + sizeb + sizec )
      
      val cityaList2 = locationService.list( citya)
      cityaList2.size === sizea 
      cityaList2 ===  cityaList.toList
      
      val citybList2 = locationService.list( cityb)
      citybList2.size === sizeb 
      citybList2 ===  citybList.toList
      
      
      val districtListA = locationService.list( citya , districtList(0 ))
      districtListA.size ===( 1 to sizea ).filter( x=> x % districtList.size == 0)  .size
      districtListA(0).name === "a1_3"
      
      val districtListB = locationService.list( citya , districtList( 1 ))
      districtListB.size ===( 1 to sizea ).filter( x=> x % districtList.size == 1)  .size
      districtListB(1).name === "a1_4"
        
       val districtListC = locationService.list( cityc , districtList( 0 ))
       districtListC.size === 0 
       
       
    }
}

  

}