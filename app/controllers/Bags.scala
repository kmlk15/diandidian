package controllers

import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import java.net.URLEncoder
import play.api.mvc.Request
import models.LocationForm
import play.api.mvc.Cookie
import org.bson.types.ObjectId
import models.BagHelp.bagFmt
import models.BagHelp.defaultPlanName
import models.BagHelp.defaultStatusName
import models.BagHelp.bagUpdateFromtoform
import models.Bag
import play.api.mvc.Session

/**
 *
 * 1  未登录用户
 * 2  已登录用户
 * 3  未登录的时候操作过， 转变成登录用户
 * 4  已经在登录用户操作， 又在未登录用户状态下操作，  后有处于登录状态，
 *
 * 背包 整体作为一个 Doc ,
 * Doc { 基础信息， 计划中， 准备去， 已去过， 未分类}
 *
 * 未登录用户， Doc  {   基础信息， 未分类}
 *
 * 登录用户，  某些 地点的集合， 这个集合 整体 存在一个状态 ，  这个状态可以从  未分类 迁移到 计划中
 * 从 计划中， 迁移到  准备去， 最后到 已去过
 *
 * 未登录的  处理比较简单
 *
 * 登录用户的，  加的时候， 加在 哪里？
 * 删除， 已经 计划的 已经去的 等等， 是否允许 删除？ （ 好像应该允许删除的 ， 计划是计划， 实际是实际 ）
 *
 * 删除应该比较简单， 有明确的对象。
 *
 * 加是个 问题， 特别是在登录
 *
 */

object Bags extends Controller {
  private val log = LoggerFactory.getLogger(Bags.getClass())
  private val bagService = base.BagServiceRegistry.bagService
  private val locationService = base.locationFormRegistry.locationService

  private val bagIdCookieName = "tmpbagId"

  def add(locationName: String , statusName: String = defaultStatusName ,planName : String = defaultPlanName ) = Action { implicit request =>
    
    def userAdd(location: LocationForm): json.JsObject = {
   
      val bagId = getBagId( session)
      val typ="user"
      
      log.debug("bagId={}, statusNam={},planName={}",  bagId ,statusName,planName  )
           
      val success = bagService.addLocation(bagId, typ, getUsertype(session),statusName, planName, location)
      if( success){
         val data = Json.obj("name" -> location.name, "id" -> location.id.get)
	      val result = Json.obj("success" -> true, "data" -> data)
	      result
      }else{
         val data = Json.obj("name" -> location.name, "id" -> location.id.get)
	      val result = Json.obj("success" -> false, "data" -> data , "msg" -> "msg")
	      result
      }
     
    }

    def anonymousAdd(location: LocationForm): (String, json.JsObject) = {
      val typ = "anonymous"
      val statusName =  defaultStatusName
      val planName =defaultPlanName
      val (bagId, flag) = request.cookies.get(bagIdCookieName) match {
        case None =>
          val bagId = (new ObjectId()).toString()

          //如何 写cookie?

          (bagId, bagService.addLocation(bagId, typ,getUsertype(session), statusName, planName, location))

        case Some(bagIdcookie) =>
          val bagId = getBagId(bagIdcookie)
          (bagId, bagService.addLocation(bagId, typ,getUsertype(session), statusName, planName, location))
      }

      if (flag) {
        val data = Json.obj("name" -> location.name, "id" -> location.id.get)
        val result = Json.obj("success" -> true, "data" -> data)
        (bagId, result)
      } else {
        val result = Json.obj("success" -> false, "msg" -> "")
        (bagId, result)
      }

    }

    locationService.getByName(locationName) match {
      case None => Ok(Json.obj("success" -> false, "msg" -> "该地点不存在"))
      case Some(location) => {
        session.get("userId") match {
          case None =>
            val (bagId, result) = anonymousAdd(location)
            Ok(result).withCookies(Cookie(bagIdCookieName, cookieVal(bagId) , Some(30*24*3600) ))
          case Some(userId) => Ok(userAdd(location))
        }
      }
    }

  }

  /**
   * 复制分享的背包数据
   * 1 自己不允许复制自己的
   * 2 名字如何命名？
   *
   */
  def copySharePlan(planId: String) = Action { implicit request =>

   val jsVal = session.get("userId") match {
      case None => Json.obj("success" -> false, "msg" -> "请先登录。")
      case Some(userId) =>
        bagService.getSharePlan(planId) match {
          case None => Json.obj("success" -> false, "msg" -> "分享的背包不存在")
          case Some(shareplan) if(userId == shareplan.bagId)=> 
            Json.obj("success" -> false, "msg" -> "不能复制自己分享的背包")
            
          case Some(shareplan) =>
            val bagId = getBagId(session)
            val statusName =  defaultStatusName
            val planName : String = bagService.get(bagId)match{
              case None => shareplan.name
              case Some( bag )=>
                 bag.map.get(statusName).map( status =>{
                	 if(status.map.get( shareplan.name)==None){
                		 shareplan.name
                	 }else{
                	   var i = 1
                	   while( status.map.get( shareplan.name + i)!= None){
                	     i = i + 1
                	   }
                	   shareplan.name + i
                	 }
                 }).getOrElse( shareplan.name )
                 
            } 
            
            val typ = "user"
            log.debug("bagId={}, statusNam={},planName={}", bagId, statusName, planName)
            val locationList: List[LocationForm] = shareplan.locationList.flatMap( location => locationService.getById( location.id))
            val addresultList = locationList.map(location => bagService.addLocation(bagId, typ, getUsertype(session), statusName, planName, location) )
             if(addresultList.forall( x=>x )){
               Json.obj("success" -> true, "msg" -> "复制完成")
             }else{
               Json.obj("success" -> true, "msg" -> ( "存在粗误" + addresultList.count( x=> !x )) )
             }
        }
    }
    Ok(jsVal)
  }

  /**
   * 这里需要做 安全认证
   */
  private def getBagId(cookie: Cookie) : String  = {
    cookie.value
  }
  
  private def getBagId( session: Session ) : String ={
    session.get("userId").getOrElse("")
  }
  
  private def getUsertype(session: Session ) : String ={
    session.get("usertype").getOrElse("")
  }
  /**
   * 需要增加 cookie 加密
   */
  private def cookieVal(bagId: String) = {
    bagId
  }

  def del(locationId: String , statusName: String =defaultStatusName,planName: String=defaultPlanName ) = Action { implicit request =>

    def userDel(location: LocationForm): json.JsObject = {
      val bagId = getBagId( session)
      
    	val success = bagService.removeLocation(bagId, statusName, planName, location)
    	if( success ){
    	   val data = Json.obj("name" -> location.name, "id" -> location.id.get)
      val result = Json.obj("success" -> success, "data" -> data)
      result
    	}else{
    	   val data = Json.obj("name" -> location.name, "id" -> location.id.get)
      val result = Json.obj("success" -> success, "data" -> data  ,"msg" -> "msg" )
      result
    	}
     
    }

    def anonymousDel(location: LocationForm): json.JsObject = {
       val typ = "anonymous"
       val statusName =  defaultStatusName
       val planName =defaultPlanName
      
      request.cookies.get(bagIdCookieName) match {
        case None => Json.obj("success" -> false, "msg" -> "数据不存在")
        case Some(bagIdcookie) =>
          bagService.removeLocation(getBagId(bagIdcookie), statusName, planName, location)
          val data = Json.obj("name" -> location.name, "id" -> location.id.get)
          val result = Json.obj("success" -> true, "data" -> data)
          result
      }

    }

    val result = locationService.getById(locationId) match {
      case None => Json.obj("success" -> false, "msg" -> "该地点不存在")
      case Some(location) => {
        session.get("userId") match {
          case None => anonymousDel(location)
          case Some(userId) => userDel(location)
        }
      }
    }
    Ok(result)
  }

  /**
   * 这里 直接 生成  html 页面， 通过 bag.js 的 loadBag 方法， 获取到html 片段，并更新页面
   * 生成的html 页面可以 独立调试  bag 操作有关的功能
   * 
   * 登陆状态 需要检测 是否  cookie 中有数据， 
   * 是否 在未登陆状态下 增加了locatin ， 如果有， 需要在 这里进行合并！
   * 合并后，删除 cookie 中的数据 
   */
  def get() = Action { implicit request =>
    
   val anonymousBagOption :Option[Bag]=  request.cookies.get(bagIdCookieName) match {
        case None => None 
        case Some(cookie) =>bagService.get ( getBagId(cookie) )
      }
    
    val bagOption  :Option[Bag] = session.get("userId") match {
      case Some(userId) => {
        bagService.get (userId ) match{
          case None =>{  
            anonymousBagOption match{
              case None => anonymousBagOption
              case Some(anonymousBag ) =>
                bagService.del(anonymousBag.id )
                bagService.save( anonymousBag.copy( id = userId ,  typ = "user") )
            }
            }
          case Some( userBag)   => anonymousBagOption match{
            case None => Some( userBag )
            case Some( anonymousBag ) =>
               //userBag ,   anonymousBag 都有内容 ， 需要进行合并
            val typ="user"
              
              for{
                bag <- anonymousBagOption
                status <-  bag.map
               plan <-  status._2.map
               location <- plan._2.list
              }{
            	  bagService.addLocation(userId, typ,getUsertype(session), defaultStatusName, defaultPlanName,
            			  LocationForm( id =Some( location.id) , name = location.name , enName = location.enName )
            	  )
              }
             //清空 anonymousBag 的数据 
              bagService.del( anonymousBag.id )
              /**
               * 如何 清除  cookie ? 
               */
              bagService.get( userId)
          }
         }
       
      }
      case None => anonymousBagOption
       
    }
        val mock =  request.getQueryString("mock").getOrElse("")
        val plan =  request.getQueryString("plan").getOrElse("")
        val openStatusName = request.getQueryString("statusName").getOrElse(  defaultStatusName )
        val openPlanName = request.getQueryString("planName").getOrElse(  defaultPlanName )
        
         log.debug("plan={}, openStatusName={}, openPlanName={}", plan , openStatusName ,openPlanName  )
         
     bagOption match{
        case None =>  Ok( views.html.bagEmpty ( ))
        case Some(bag) if( bag.isEmpty) => Ok( views.html.bagEmpty ( ))
        case Some( bag ) if (mock != "") => Ok( views.html.bagMock (  bag ))  // 用于mock
        case Some( bag ) if (getBagId( session) != ""  && plan != "") =>{
          Ok( views.html.bagPlan (  bag ,openStatusName ,openPlanName))  // 用于plan page
        }
        case Some( bag ) if ( getBagId( session) != "" )=>{
          Ok( views.html.bagUser( bag ,openStatusName ,openPlanName ))
        }
        case Some( bag ) => Ok( views.html.bagAnonymous( bag ))
        
      }

   
    

  }

  /**
   * 得到可用的 新的背包的名字 
   */
  def createNewplan() = Action  {implicit request =>
    val bagId = getBagId(session)
    
   val planName =  bagService.createNewplan(bagId)
   val jsobj = Json.obj( "success" -> true , "data" -> Json.obj("planName" -> planName , "statusName" -> defaultStatusName ) )
   Ok( jsobj)
   
  }
  
  
  def updateJson() = Action { implicit request =>
    session.get("userId") match {
      case Some(userId) => {
        bagService.get(userId) match {
          case Some(bag) =>
            bagUpdateFromtoform.bindFromRequest().fold(
              errors => { Ok("输入错误") },
              change => {
            	  val s = System.currentTimeMillis()
            	  val isRemove = if( request.getQueryString("cmd").getOrElse("") == "delete") true else false
            	  
                val tobag = models.BagHelp.update(bag, change , isRemove)
                val e1 = System.currentTimeMillis()
                log.debug("更新bag 时间: ={}" , ( e1 -s ))
                if(bag != tobag ){
                    bagService.update(tobag)
                    tobag
                  }else{
                      log.debug(" 没有任何改变")
                    tobag
                  }
            	  val e2 = System.currentTimeMillis()
                log.debug("更新 bag  到 mongo 时间: ={}" , ( e2 -e1 ))
                val data = Json.obj( )
                val jsval = Json.obj("success"-> true, "data" -> data)	
                Ok( jsval)
              })
          case None => NotFound
        }
      }
      case None => NotFound
    }
  }
  
  
  /**
   * bag 的更新
   *  实际是 元组 (statusName , planName)  的 3 种改变
   *  (statusName , planName) 可以唯一确定一个 地点集合， 如果存在 重名，如何处理？ 提示用户合并吗?
   *  是否还需要用户 地点集合  分裂 ？这个操作 用户 可以通过 组合 + -  完成 （ 估计意义不大）
   *  (statusName , planName)  -> (statusName , planName') 修改 plan 名字， 如从  "背包" -> "香港背包"
   *  (statusName , planName)  -> (statusName' , planName) 修改 status 状态， 计划中 -> 准备去 -> 已去过
   *  (statusName , planName)  -> (statusName' , planName') 2个都修改， 一般是不可能的， 也不需要这样的操作 ; 
   *
   * 如何保证只修改自己的？
   * 以后 实际上 一次只修改个 plan
   * 返回修改后的Bag?
   * 或者 重新加载一次？
   *
   */
  def update() = Action { implicit request =>
    session.get("userId") match {
      case Some(userId) => {
        bagService.get(userId) match {
          case Some(bag) =>
            bagUpdateFromtoform.bindFromRequest().fold(
              errors => { Ok("输入错误") },
              change => {
            	  val s = System.currentTimeMillis()
            	  val isRemove = if( request.getQueryString("cmd").getOrElse("") == "delete")true else false
            	  
                val tobag = models.BagHelp.update(bag, change , isRemove)
                val e1 = System.currentTimeMillis()
                log.debug("更新bag 时间: ={}" , ( e1 -s ))
                if(bag != tobag ){
                    bagService.update(tobag)
                    tobag
                  }else{
                      log.debug(" 没有任何改变")
                    tobag
                  }
            	  val e2 = System.currentTimeMillis()
                log.debug("更新 bag  到 mongo 时间: ={}" , ( e2 -e1 ))
                Redirect( "/bag/get?mock=mock")
                //Ok( views.html.bagMock (  tobag )) 
              })
          case None => NotFound
        }
      }
      case None => NotFound
    }
  }

  //地点 分配日期 
  def locationSignDate(locationId: String, statusName: String, planName: String, date: String) = Action { implicit request =>
    session.get("userId") match {
      case Some(bagId) => {
        bagService.locationSignDate(bagId, statusName, planName, locationId, date) match {
          case None => Ok(Json.obj("success" -> false, "msg" -> ""))
          case Some(updatedBag) => Ok(Json.obj("success" -> true, "msg" -> ""))
        }
      }
      case None => NotFound
    }
  }
}