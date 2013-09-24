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
import models.AdmissionForm


object Detail extends Controller   {
	val log = LoggerFactory.getLogger(Detail.getClass())
	//val locationService = base.locationRegistry.locationService
 
      val locationService = base.locationFormRegistry.locationService
   // val locationService = locationRegistry.locationService
      val cmsService = base.CmsServiceRegistry.cmsService
val awsbase  = "http://diandidian.s3-us-west-1.amazonaws.com/"
      
  def view(name: String ) = Action { implicit request =>{
    log.debug("name={}"  , name )
    
    Ok( views.html.detailview( name ))
  }
  }
  
  def viewJson(name: String) = Action {
    log.debug("name={}"  ,  name)
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
             "galleryContent" ->  galleryHtml (  (galleryContent(  location  )) ) ,
             "admissionView" ->  admissionView( location.admission)
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

  def admissionView( admission: AdmissionForm): String = {
    if( (admission.adults=="" || admission.adults=="0") && 
    	 (admission.general=="" || admission.general=="0") && 
    	 (admission.children=="" || admission.children=="0") && 
    	 (admission.student=="" || admission.student=="0") && 
    	 (admission.seniors=="" || admission.seniors=="0") && 
    	 (admission.free=="" || admission.free=="0") 
    ){
      
     "" 
    }else{
     val html = views.html.detail.admission( admission )
     html.toString
    }
  }
  
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
    
     val firstPhoto =   photoList.head
     val lastPhoto = photoList.reverse.head
 
    val xml = <div class="gallery">
                <img class="detail" src={awsbase +  firstPhoto.detailpageImg} width="780" height="435" alt={location.name}/>
                <div class="thumbnails">
                  <span class="arrow left-arrow"><img src="/assets/images/left-arrow.png" width="10" height="12"/></span>
                  <span class="arrow right-arrow"><img src="/assets/images/right-arrow.png" width="10" height="12"/></span>
                  <div class="ul-wrap">
                    <ul class="clearfix">
    					{ for( photo <- photoList )yield  {
    					 if( photo == firstPhoto  ){
    					   
                      <li class="active" >
    							<a href={awsbase +  photo.detailpageImg}  id= {photo.id.get} ><img src={awsbase +  photo.detailpageThumbnailImg}  width="102"  height="57" /></a>
                      </li>  
    					   
    					 }else if( photo == lastPhoto){
    					 <li class="last" >
    							<a href={awsbase +  photo.detailpageImg}  id= {photo.id.get} ><img src={awsbase + photo.detailpageThumbnailImg}   width="102"  height="57"  /></a>
                      </li>  
    					 }else{
                      <li >
    							<a href={awsbase +  photo.detailpageImg} id= {photo.id.get }><img src={awsbase +  photo.detailpageThumbnailImg }   width="102"  height="57"  /></a>
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
      
 
   val xml = <div>
					<div class="user-content clearfix">
						<div class="col col1">
							
						</div>
						<div class="col col2">
							<div class="title">
								<h2></h2><span></span>
							</div>
							<div class="content">
								<p>
									
								</p>
								 
							</div>
						</div>						
					</div>
</div>
     
    List(xml)
 
  }

  def displayUserPhotoContent( photo: Photo): scala.xml.Elem = {
    val  brief = photo.brief
    if(brief.trim().isEmpty()){
      <div class="content">
        <p>
          { photo.username }
        </p>
      </div>
    }else{
    if (brief.size > 100) {
      <div class="content">
        <p>
          { brief.substring(0, 100) }
          <span class="more">	{ brief.substring(100) }</span>
        </p>
        <div class="switch clearfix"><span class="more">更多</span><span class="less">关闭</span></div>
      </div>
    } else {
      <div class="content">
        <p>
          { brief }
        </p>
      </div>
    }
    }
  }

   def displayAdminPhotoContent( photo: Photo): scala.xml.Elem = {
    val  brief = photo.brief
    if(brief.trim().isEmpty()){
      <div class="content">
        <p>
        
        </p>
      </div>
    }else{
    if (brief.size > 100) {
      <div class="content">
        <p>
          { brief.substring(0, 100) }
          <span class="more">	{ brief.substring(100) }</span>
        </p>
        <div class="switch clearfix"><span class="more">更多</span><span class="less">关闭</span></div>
      </div>
    } else {
      <div class="content">
        <p>
          { brief }
        </p>
      </div>
    }
    }
  }
  
 
  
  def displayContent( photo: Photo , location : LocationForm, isFirst: Boolean  ): scala.xml.Elem = {
    if( photo.uploadtype == "admin"){
      <div>
        <div class="user-content clearfix" style={ if (isFirst) { "" } else { "display:none" } } id={ "content_" + photo.id.get }>
          <div class="col col1">
        {if (photo.avatar =="" ){
        	   <div class="title">图片来源 : </div>
        	 }else{
        	   <div class="title"><a href={ photo.avatar } target="_blank">图片来源</a> :</div>
        	 }
        	  }
          </div>
          <div class="col col2">
             
        	  {if (photo.avatar =="" ){
        	   <div class="title"> { photo.username } </div>
        	 }else{
        	   <div class="title"> { photo.username } </div>
        	 }
        	  }
        	  
            
 
        	  { displayAdminPhotoContent(photo ) }
          </div>
        </div>
      </div>
                    
      
    }else{
      <div>
        <div class="user-content clearfix " style={ if (isFirst) { "" } else { "display:none" } } id={ "content_" + photo.id.get }>
          <div class="col colImg">
            <img class="ravatarimg" src={ photo.avatar } width="36" height="36" alt={ photo.username }/>
          </div>
          <div class="col col2">
              {if (photo.brief.trim().isEmpty ){
            	  <div class="title">图片来源:</div>
              }else{
	            <div class="title">
            	  <h2>{ photo.username }:</h2> 
            	 </div>
              }
              }
            { displayUserPhotoContent(photo ) }
          </div>

        </div>
      </div>             
    }
    
  }
  
     def galleryContent(photoList: List[Photo] , location : LocationForm ) : List[scala.xml.Elem] = {
       val firstPhoto =   photoList.head
       
        {for( photo <- photoList )yield  {
          displayContent(photo , location , firstPhoto == photo )
          
        }
        }
          
     }
}