package models

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import scala.collection.mutable
 
case class OpenClose(open: String = "" , close: String = "" )

case class HoursForm(
  monday: OpenClose = OpenClose(),
  tuesday: OpenClose = OpenClose(),
  wednesday: OpenClose = OpenClose(),
  thursday: OpenClose = OpenClose(),
  friday: OpenClose = OpenClose(),
  saturday: OpenClose = OpenClose(),
  sunday: OpenClose = OpenClose(),
  holiday: OpenClose = OpenClose()
  
 

)
object HoursFormHelp{
  
   /**
   * 合并相同的时间
   * 以  monday 为基准
   * 通过  值 合并 分组
   */
  def view(hours: HoursForm ):List[(String,OpenClose)] ={
     
     val map = mutable.Map[OpenClose, String]()
      
     val opencloseBuffer = mutable.ArrayBuffer[OpenClose]()
     if( !opencloseBuffer.contains( hours.monday )){
       opencloseBuffer.append(  hours.monday)
     }
     
     if( !opencloseBuffer.contains( hours.tuesday )){
       opencloseBuffer.append(  hours.tuesday)
     }
     
     if( !opencloseBuffer.contains( hours.wednesday )){
       opencloseBuffer.append(  hours.wednesday)
     }
     
     if( !opencloseBuffer.contains( hours.thursday )){
       opencloseBuffer.append(  hours.thursday)
     }
     
     if( !opencloseBuffer.contains( hours.friday )){
       opencloseBuffer.append(  hours.friday)
     }
     
     
     val listA: List[(String,OpenClose)] =  if(opencloseBuffer.size == 1){
    	 	List( "周一~周五: " ->   hours.monday )
     }else{
      
        val m1 =  opencloseBuffer.map( openclose =>{
            openclose -> mutable.ArrayBuffer[String]()
          }).toMap
          
          m1.get( hours.monday).map( buf=> buf.append("周一"))
          m1.get( hours.tuesday).map( buf=> buf.append("周二"))
          m1.get( hours.wednesday).map( buf=> buf.append("周三"))           
          m1.get( hours.thursday).map( buf=> buf.append("周四"))         
          m1.get( hours.friday).map( buf=> buf.append("周五"))
          
          val buf  = opencloseBuffer.map( openclose =>{ openclose -> m1.get( openclose).map(buf=> buf.mkString(",")) }).
          map( kv => kv._2.getOrElse("") + ": " -> ( kv._1   ))

          buf.toList
     }
     
    val listB : List[(String,OpenClose)] =  if( hours.saturday == hours.sunday){
      List ( "周六,周日: " -> (hours.saturday))
      
    }else{
      List ( "周六: "  ->(  hours.saturday),
           "周日: " ->( hours.sunday ) 
      )
      
    }
     
    listA ++ listB ++ List( "假日: " -> (hours.holiday ) )
  }
  
  
   def opentimetable(hours: HoursForm ): String = {
    val trList =  view( hours)
    val html= 
      <table border="1" >
      {
      trList.map( str => <tr><td>{str._1}</td><td>{str._2.open}</td><td>{str._2.close}</td></tr>)
      }
      </table>
       
    html.toString
  }
   
}


case class CategoryForm(
    categoryId: String = "",
  level_1: String = "",
  level_2: String ="" )  
  
  

case class LocationForm(
  id: Option[String] = None, 
  name: String = "" ,
  enName: String = ""  ,
  address: Address = Address(street = "", district = "", city = "", postalCode ="", stateProvince="",country="",latitude=0.0 , longitude=0.0 ),
  phone: Phone = Phone( general="" , fax=""),
  admission: Admission = Admission (currency="", general=0.0, adults=0.0 , children=0.0, student=0.0 , seniors=0.0 ),
  hours: HoursForm = HoursForm(),
  url: String = "" ,
  category: CategoryForm = CategoryForm(categoryId  = ""  , level_1="", level_2="" ),
  fact: String = "",
  photo: String = ""  
)
  
object LocationFormHelp {
  
  import LocationJsonHelp._
  
  implicit val ppenCloseFmt = Json.format[OpenClose]
  implicit val hoursFormFmt  = Json.format[HoursForm]
  implicit val categoryForm  = Json.format[CategoryForm]
 implicit val locationFormFmt = Json.format[LocationForm]

  
  val form = Form {
    mapping(
        "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),
      "name" -> text,
      "enName" -> text ,
      "address" -> mapping(
        "street" -> text,
        "district" -> text,
        "city" -> text,
        "postalCode" -> default(text, "" ) ,
        "stateProvince" -> text,
        "country" -> text,
        "latitude" -> default( of[Double], 0.0 ),
        "longitude" -> default( of[Double], 0.0 ) )(Address.apply)(Address.unapply),

      "phone" -> mapping(
        "general" -> text,
        "fax" -> default(text, "" ) )(Phone.apply)(Phone.unapply),

      "admission" -> mapping(
        "currency" -> text,
        "general" -> default( of[Double]  ,0.0) ,
        "adults" -> default( of[Double]  ,0.0),
        "children" -> default( of[Double]  ,0.0),
        "student" -> default( of[Double]  ,0.0),
        "seniors" -> default( of[Double]  ,0.0) )(Admission.apply)(Admission.unapply),

      "hours" -> mapping(
        "monday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply) ,
        "tuesday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "wednesday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "thursday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "friday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "saturday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "sunday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "holiday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply))
        (HoursForm.apply)(HoursForm.unapply),

      "url" -> text,

      "category" -> mapping(
          "categoryId" -> text ,
        "level_1" -> default(text,"") ,
        "level_2" -> default(text,"")  )(CategoryForm.apply)(CategoryForm.unapply),

      "fact" -> text,
      "photo" -> default(text , "")
      
    )(LocationForm.apply)(LocationForm.unapply)

  }

}

/**
 * 1 管理员上传的 picture
 * 2 用户上传的 picture
 */
case class Photo(id: Option[String]=None , locationId: String ="", userId: String="" , username:String = "" , avatar: String ="",
    imgsrc: String="", imgurl: String="", brief: String="" , uploadtype: String="" , atHomepage:Boolean = false )

object PhotoHelp{
  
   implicit val pictureFmt = Json.format[Photo]
   val form = Form {
    mapping(
        "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),
        
      "locationId" -> text,
      "userId" -> text ,
      "username" -> default(text , ""),
      "avatar" -> default(text ,""),
      "imgsrc" -> default(text ,"") ,
      "imgurl" -> text,
      "brief" -> text,
      "uploadtype" -> default(text,"") ,
      "atHomepage" -> default(boolean, false)
      )( Photo.apply )( Photo.unapply)
   }
   
}
