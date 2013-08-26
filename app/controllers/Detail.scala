package controllers

import play.api.Logger
import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import models.LocationJsonHelp.locationFmt
import models.LocationFormHelp.locationFormFmt
import models.Photo
import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import models.LocationForm


object Detail extends Controller   {
	val log = LoggerFactory.getLogger(Detail.getClass())
	//val locationService = base.locationRegistry.locationService
 
      val locationService = base.locationFormRegistry.locationService
   // val locationService = locationRegistry.locationService
      val cmsService = base.CmsServiceRegistry.cmsService
val awsbase  = "http://diandidian.s3-us-west-1.amazonaws.com/"
      
  def view(name: String ) = Action { implicit request =>{
    Logger.debug("name=" + name )
    
    Ok( views.html.detailview( name ))
  }
  }
  
  def viewJson(name: String) = Action {
    Logger.debug("name=" + name)
    /**
     * name 不适合作为主键
     * 需要将 数据保存到 mongodb 中
     *
     */
    locationService.getByName(name) match{
       case Some(location) => {
         val jsVal  = Json.toJson( location).as[ json.JsObject]
         // TODO 这里是临时的 代码 ， 最后统一转到 新设计的 LocationForm 类
        val path =   json.JsPath \ "pictures"
         if(  path(jsVal) .isEmpty   ){
           
           /**
            * 
            * 图片的list  在这里 直接转成   html 还是通过 knockout  转？
            * 还有图片附加的数据如何 显示, 所有都在 html  文件中， 通过 display 属性进行控制？
            * 1  初始的数据
            * 2  运行用的 list 数据 ； list 的排序是否有要求？
            * 
            */
          
           
          val jsVal2 =  jsVal.++ (  Json.obj( "pictures" ->
          Json.obj( "thumbnail" -> Json.toJson( List[String]()) ,   
            "planning" ->  ( awsbase + location.photo.replace("266_", "780_" ) )  ) ,
            
           "cityhref" -> ("/home?country="+ encode(location.address.country) + 
               "&city=" +encode(location.address.city)  ),
           "districthref" -> ("/home?country="+ encode(location.address.country) + 
               "&city=" +encode(location.address.city)  +
               "&district=" + encode(location.address.district)  ),
               
           "addPhotohref" -> ( "/cms/userphoto/add/" + location.id.get) ,
           
           "opentimetable" -> models.HoursFormHelp.opentimetable(location.hours ) ,
            "galleryview" -> ( "/detail/galleryview/" + location.id.get ),
            "gallery" ->  galleryHtml (  (gallery(  location  )) ),
             "galleryContent" ->  galleryHtml (  (galleryContent(  location  )) )
          ) )
           log.debug( Json.prettyPrint( jsVal2))
           Ok( jsVal2 ) 
         }else{
           log.debug( Json.prettyPrint( jsVal))
           log.debug( Json.prettyPrint( jsVal \ "pictures"))
           Ok( jsVal )
         }
         
         
         
         }
       case None => { NotFound }
    }
    
    
  }
  
  def encode( str: String ): String = URLEncoder .encode(str, "utf-8")

  
  def galleryview( locationId: String ) =Action{
    locationService.getById(locationId)match{
      case None =>  NotFound
      case Some(location) => Ok( galleryHtml (gallery(location ) )  )
    }
    
  }
  
  def galleryHtml( xml: scala.xml.Elem)={
    val str  = (xml.child).mkString("\n")
    str 
  }
  
  def galleryHtml( xmlList: List[scala.xml.Elem])={
    xmlList.map( xml =>  (xml.child).mkString("\n")).mkString("\n")
  }
  
   def gallery(location : LocationForm) : scala.xml.Elem = {
     val photoList =  cmsService.getPhotoList(location.id.get)
    val xml =  if( photoList.isEmpty){
       emptyGallery()
     }else{
       gallery( photoList , location ) 
     }
    xml
   }
   
  def emptyGallery(): scala.xml.Elem = {
    <div class="gallery">
      <img class="detail" src="" width="780" height="435" alt=""/>
      <div class="thumbnails">
        <span class="arrow left-arrow"><img src="/assets/images/left-arrow.png" width="10" height="12"/></span>
        <span class="arrow right-arrow"><img src="/assets/images/right-arrow.png" width="10" height="12"/></span>
        <div class="ul-wrap">
          <ul class="clearfix">
          </ul>
        </div>
      </div>
    </div>
    
  }
  
  def gallery(photoList: List[Photo] , location : LocationForm ) : scala.xml.Elem = {
     val tempList = photoList ::: photoList ::: photoList ::: photoList ::: photoList
     val firstPhoto =   photoList.head
     val lastPhoto = photoList.reverse.head
     var flag = true 
    val xml = <div class="gallery">
                <img class="detail" src={awsbase + "780_"+firstPhoto.imgsrc} width="780" height="435" alt={location.name}/>
                <div class="thumbnails">
                  <span class="arrow left-arrow"><img src="/assets/images/left-arrow.png" width="10" height="12"/></span>
                  <span class="arrow right-arrow"><img src="/assets/images/right-arrow.png" width="10" height="12"/></span>
                  <div class="ul-wrap">
                    <ul class="clearfix">
    					{ for( photo <- tempList )yield  {
    					 if( photo == firstPhoto  && flag ){
    					   flag = false 
                      <li class="active" >
    							<a href={awsbase + "780_"+photo.imgsrc}><img src={awsbase + "102_"+photo.imgsrc}  /></a>
                      </li>  
    					   
    					 }else if( photo == lastPhoto){
    					 <li class="last" >
    							<a href={awsbase + "780_"+photo.imgsrc}><img src={awsbase + "102_"+photo.imgsrc}  /></a>
                      </li>  
    					 }else{
                      <li >
    							<a href={awsbase + "780_"+photo.imgsrc}><img src={awsbase + "102_"+photo.imgsrc}  /></a>
                      </li>  

    					 }
                      }
    					}
                    </ul>
                  </div>
                </div>
              </div>
       
       xml
  }
  
   def galleryContent(location : LocationForm) : List[scala.xml.Elem] = {
     val photoList =  cmsService.getPhotoList(location.id.get)
    val xml =  if( photoList.isEmpty){
       emptyGalleryContent()
     }else{
       galleryContent( photoList , location ) 
     }
    xml
   }
   
    def emptyGalleryContent(): List[scala.xml.Elem] = {
      
 
   val xml = <div><div class="galleryContent">
      <div class="col col1">
        <img src="" width="49" height="49" alt="User Icon"/>
      </div>
      <div class="col col2">
        <div class="title">
          <h2></h2><span></span>
        </div>
        <div class="content">
          <p>
          </p>
          <div class="switch clearfix"><span class="more">更多</span><span class="less">关闭</span></div>
        </div>
      </div>
    </div></div>
     
    List(xml)
 
  }
     def galleryContent(photoList: List[Photo] , location : LocationForm ) : List[scala.xml.Elem] = {
       val firstPhoto =   photoList.head
       
        {for( photo <- photoList )yield  {
          if( firstPhoto == photo ){
            <div><div class="galleryContent">
                <div class="col col1">
                  <img src={photo.avatar}  width="49" height="49" alt="User Icon"/>
                </div>
                <div class="col col2">
                  <div class="title">
                    <h2>{location.name}</h2><span>-  上传图片的时候，不知道 对应的 plann!!! ???</span>
                  </div>
                {if(photo.brief.size > 100){ 
                  <div class="content">
                    <p>
                    	{photo.brief}
                      海洋公园我已经去过不下5次, 每次去好像都有更多的游乐设施和展馆。这次和同事去, 她还未去过香港, 所以这次我就当她的导游。其实这位同事是个胆子非常小的女孩, 所以她连过山车都没坐过, 这次我
                      <span class="more">特意带她到冰极天地里坐极地时速过山车, 好让她体验一下坐过山车有多爽！想不到她因此而喜欢上过山车,我们玩完了极地时速, 还坐了动感快车,疯狂过山车,越矿飞车等等, 胆子太小的朋友记得坐一下极地时速, 说不定你也因此克服了恐高.</span>
                    </p>
                    <div class="switch clearfix"><span class="more">更多</span><span class="less">关闭</span></div>
                  </div>
                    }else{
                     <div class="content">
                    <p>
                    	{photo.brief}
                      海洋公园我已经去过不下5次, 每次去好像都有更多的游乐设施和展馆。这次和同事去, 她还未去过香港, 所以这次我就当她的导游。其实这位同事是个胆子非常小的女孩, 所以她连过山车都没坐过, 这次我
                    </p>
                    
                  </div>
                    }
                 }
                </div>
              </div></div>
          }else{
         <div><div class="galleryContent"  style="display:none">
                <div class="col col1">
                  <img src={photo.avatar}  width="49" height="49" alt="User Icon"/>
                </div>
                <div class="col col2">
                  <div class="title">
                    <h2>{location.name}</h2><span>-  上传图片的时候，不知道 对应的 plann!!! ???</span>
                  </div>
                  {if(photo.brief.size > 100){ 
                  <div class="content">
                    <p>
                    	{photo.brief}
                      海洋公园我已经去过不下5次, 每次去好像都有更多的游乐设施和展馆。这次和同事去, 她还未去过香港, 所以这次我就当她的导游。其实这位同事是个胆子非常小的女孩, 所以她连过山车都没坐过, 这次我
                      <span class="more">特意带她到冰极天地里坐极地时速过山车, 好让她体验一下坐过山车有多爽！想不到她因此而喜欢上过山车,我们玩完了极地时速, 还坐了动感快车,疯狂过山车,越矿飞车等等, 胆子太小的朋友记得坐一下极地时速, 说不定你也因此克服了恐高.</span>
                    </p>
                    <div class="switch clearfix"><span class="more">更多</span><span class="less">关闭</span></div>
                  </div>
                    }else{
                     <div class="content">
                    <p>
                    	{photo.brief}
                      海洋公园我已经去过不下5次, 每次去好像都有更多的游乐设施和展馆。这次和同事去, 她还未去过香港, 所以这次我就当她的导游。其实这位同事是个胆子非常小的女孩, 所以她连过山车都没坐过, 这次我
                    </p>
                    
                  </div>
                    }
                  }
                </div>
              </div></div>
          }
        }
        }
          
     }
}