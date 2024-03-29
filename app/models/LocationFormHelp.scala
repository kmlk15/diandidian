package models

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import scala.collection.mutable
import org.apache.commons.lang3.StringUtils
 
case class OpenClose(open: String = "" , close: String = "" ){
  
 override  def toString(): String = if( close==""){ open }else if( open==""){ close }else{ open +" - " + close }
}

case class HoursForm(
  monday: OpenClose = OpenClose(),
  tuesday: OpenClose = OpenClose(),
  wednesday: OpenClose = OpenClose(),
  thursday: OpenClose = OpenClose(),
  friday: OpenClose = OpenClose(),
  saturday: OpenClose = OpenClose(),
  sunday: OpenClose = OpenClose(),
  holiday: OpenClose = OpenClose(),
  closed: String = ""
 

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
     
   val totalist =   listA ++ listB ++ List( "假日: " -> (hours.holiday ) )
   //过滤 未设置时间的
    totalist.filter(  kv => kv._2 != OpenClose())
  }
  
  
   def opentimetable(hours: HoursForm ): String = {
     if(hours == HoursForm () ){
        ""
     }else{
    val trList =  view( hours)
    val html= 
      <table border="0" >
      {
      trList.map( str => <tr><td  style='padding-right:10px;text-align:right;'  >{str._1}</td><td>{str._2.toString}</td></tr>)
      }
     {if( hours.closed != "") 
     <tr><td colspan="2"  style="width:250px">休馆:{hours.closed}</td></tr>
      }
      </table>
       
    html.toString
  }
   }
  def opentimetablePlanningPagePdf(hours: HoursForm ): String = {
    val trList =  view( hours)
    val html= 
      <table  style="width:320px">
      <tr><td colspan="2">开放时间:</td></tr>
      {
      trList.map( str => <tr colspan="2"><td  style='padding-right:2px;text-align:left;'  >{str._1} {str._2.toString}</td></tr>)
      }
     {if( hours.closed != "") 
     <tr><td colspan="2">休馆:{hours.closed}</td></tr>
      }
      </table>
       
    html.toString
  }
      
 def opentimetablePlanningPage(hours: HoursForm ): String = {
    val trList =  view( hours)
    val html= trList.map( str =>str._1  + str._2.toString( ) ).mkString("\n")
      
       
    html 
  }
}


case class CategoryForm(
    categoryId: String = "",
  level_1: String = "",
  level_2: String ="" ,
  level_1_en: String ="" ,
  level_2_en: String = "" 
)  
  
case class AdmissionForm(
  currency: String = "",
  general: String = "",
  adults: String = "",
  children: String = "",
  student: String = "",
  seniors: String = "",
  free: String = ""
){
  
  def isEmpty(): Boolean = {
    ( StringUtils.isBlank( currency )  ||  currency=="0" )&& 
    (StringUtils.isBlank( general )  ||  general=="0" ) && 
    ( StringUtils.isBlank( adults ) ||  adults=="0"  ) && 
    ( StringUtils.isBlank( children )  ||  children=="0"  )&& 
    ( StringUtils.isBlank( student )  ||  student=="0" ) && 
    ( StringUtils.isBlank( seniors )  ||  seniors=="0" ) && 
   (  StringUtils.isBlank( free )  ||  free=="0" )
         
  }
  
}
  
/**
 * 2014-01-03 增加字段
 * notDisplayAtHomePage :Option[String] = None 
 */
case class LocationForm(
  id: Option[String] = None, 
  name: String = "" ,
  enName: String = ""  ,
  address: Address = Address(street = "", district = "", city = "", postalCode ="", stateProvince="",country="",latitude=0.0 , longitude=0.0 ),
  phone: Phone = Phone( general="" , fax=""),
  admission: AdmissionForm = AdmissionForm (),
  hours: HoursForm = HoursForm(),
  url: String = "" ,
  category: CategoryForm = CategoryForm(  ),
  fact: String = "",
  photo: String = "" ,
  planId: Option[String] =  None,
  notDisplayAtHomePage: Option[String]= None
)
 
  
object LocationFormHelp {
  
  import LocationJsonHelp._
  
  implicit val ppenCloseFmt = Json.format[OpenClose]
  implicit val hoursFormFmt  = Json.format[HoursForm]
  implicit val categoryForm  = Json.format[CategoryForm]
  implicit val admissionForm = Json.format[AdmissionForm]
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
        "currency" -> default(text,"") ,
        "general" -> default(text,"") ,
        "adults" -> default(text,""),
        "children" -> default(text,""),
        "student" -> default(text,""),
        "seniors" ->default(text,""),
        "free" ->default(text,"")
      )(AdmissionForm.apply)(AdmissionForm.unapply),

      "hours" -> mapping(
        "monday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply) ,
        "tuesday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "wednesday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "thursday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "friday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "saturday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "sunday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
        "holiday" -> mapping( "open" -> text , "close"-> text)( OpenClose.apply)(OpenClose.unapply),
         "closed" ->  default(text ,"")
        )
        (HoursForm.apply)(HoursForm.unapply),

      "url" -> text,

      "category" -> mapping(
        "categoryId" -> text ,
        "level_1" -> default(text,"") ,
        "level_2" -> default(text,""),
        "level_1_en" -> default(text,""),
        "level_2_en" -> default(text,"")
      )(CategoryForm.apply)(CategoryForm.unapply),

      "fact" -> text,
      "photo" -> default(text , ""),
      "planId" -> optional(of[String]),
      "notDisplayAtHomePage" -> optional( default(text , "false"))
      
    )(LocationForm.apply)(LocationForm.unapply)

  }

}

/**
 * 1 管理员上传的 picture
 * 2 用户上传的 picture
 * 2013-09-04 
 * 
 * imgDetailPage  详细页面大图   780x435 
 * imgDetailPageSmall       DetailPage 页面的小图  102x57
 * imgHomePage    HomePage  图片  266x262
 * imgPlanPage    PlanPage 图片   193x190 
 * 
 * 有规则的保存图片  原图id  HomePage原图  780_  102_ 266_ 193_ 
 * 
 * 用户上传的，只有  780_ 102_  这2种
 */
case class Photo(id: Option[String]=None , locationId: String ="", userId: String="" , username:String = "" , avatar: String ="",
   imgId:String="none",   extension: String = "jpg" , uploadhistory: String = "" ,  imgurl: String="", brief: String="" , uploadtype: String="" , atHomepage:Boolean = false ){

  /**
   * 上传的 原始文件 保存的名字 
   */
  val  detailPageOrignImg =if(extension=="" || extension==PhotoHelp.NotUpload)  PhotoHelp.defaultImg else "detail_" +  imgId + "."+ extension
  val  homepageOrignImg = if(extension=="" || extension==PhotoHelp.NotUpload)  PhotoHelp.defaultImg else "home_" + imgId + "."+ extension
  val normalOrignImg  = if(extension=="" || extension==PhotoHelp.NotUpload)  PhotoHelp.defaultImg else  imgId + "." + extension
  
  
  val  homepageImg = if(extension=="" || extension==PhotoHelp.NotUpload)  PhotoHelp.defaultImg else "266_" + imgId +"."+ extension
  val  planpageImg =if(extension=="" || extension==PhotoHelp.NotUpload)  PhotoHelp.defaultImg else "193_" + imgId +"."+ extension
  val  detailpageImg =if(extension=="" || extension==PhotoHelp.NotUpload)  PhotoHelp.defaultImg else "780_" + imgId +"."+ extension
  val  detailpageThumbnailImg =if(extension=="" || extension==PhotoHelp.NotUpload)  PhotoHelp.defaultImg else "102_" + imgId +"."+ extension
  

  
  
  
}

object PhotoHelp {

   val NotUpload = "NotUpload"
   val defaultImg = ""
  
     /**
   * 上传的 原始文件 保存的名字 
   */
  def detailpageOrignImg(imgId: String, extension: String) = "detail_"+imgId + "." + extension
  def homepageOrignImg(imgId: String, extension: String) = "home_" + imgId + "." + extension
  def normalOrignImg(imgId: String, extension: String) = imgId + "." + extension
   
  def homepageImg(imgId: String, extension: String) = "266_" + imgId + "." + extension
  def planpageImg(imgId: String, extension: String) = "193_" + imgId + "." + extension
  def detailpageImg(imgId: String, extension: String) = "780_" + imgId + "." + extension
  def detailpageThumbnailImg(imgId: String, extension: String) = "102_" + imgId + "." + extension
  
  /**
   * 在更新前 计算最后一次 HomePage 图片有没有上传
   * 历史数据保存如下
   * h,d;x,x;
   * h 上传了h
   * d 上传了d
   * x  没有上传
   * 只要有一次上传过，以后都需要 上传 HomepageImg ,才能够更改
   */
  def  isHomepageImgupload( uploadhistory: String):Boolean= {
    
    uploadhistory.split(";") match{
      case Array() => false
      case  x => 
       x.exists( str => str.split(",") match{
          case Array(homepageExtension,_) if( homepageExtension != NotUpload)=> true
          case Array(homepageExtension,_,_) if( homepageExtension != NotUpload)=> true
          case _  => false
        })
    }
  }
  
  val homepageImgsize = (266, 262)
  val planpageImgsize = (193, 190)
  val detailPageImgsize = (780, 435)
  val detailpageThumbnailImgsize = (102, 57)

  implicit val photoFmt = Json.format[Photo]
  val form = Form {
    mapping(
      "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),

      "locationId" -> text,
      "userId" -> text,
      "username" -> default(text, ""),
      "avatar" -> text,
      "imgId" -> default(text, ""),
      "extension" -> default(text, ""),
      "uploadhistory" -> default(text, ""),
      "imgurl" -> text,
      "brief" -> text,
      "uploadtype" -> default(text, ""),
      "atHomepage" -> default(boolean, false))(Photo.apply)(Photo.unapply)
  }

}
