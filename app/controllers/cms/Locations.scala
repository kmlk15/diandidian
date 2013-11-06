package controllers.cms

import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller
import sjson.json.JsonSerialization
import dispatch.classic.json.JsValue
import play.api.libs.json.Json
import org.slf4j.LoggerFactory

import models.LocationFormHelp
import models.LocationFormHelp._
import models.LocationJsonHelp._
import models.CategoryForm
import models.LocationForm

import play.api.libs.json.Json

object Locations extends Controller  with AuthTrait  {

  val log = LoggerFactory.getLogger(Locations.getClass())

  val service = base.CmsServiceRegistry.cmsService
  val bagService = base.BagServiceRegistry.bagService
  val loginService = base.LoginServiceRegistry.loginService
 
 
  def categoryList = service.getCategoryList( )
  
  def createCategoryForm(categroyId: String):  CategoryForm = {
    if( categroyId ==""){
      CategoryForm()
    }else{
       service.getCategoryById( categroyId)match{
         case None =>  CategoryForm()
         case Some(c ) => if( c.parentId != "" ){      
           service.getCategoryById(c.parentId) match{
             case None => CategoryForm( categroyId,   c.name ,"" , c.enName , "" )
             case Some( p) => CategoryForm(categroyId, p.name, c.name , p.enName , c.enName)
           }
         }else{ CategoryForm(categroyId,   c.name ,"" , c.enName , "" )  }
      }
      
    }
    
  }
  
  def list() =  isAuthenticated { username => implicit request =>
  val list = service.getLocationList
    Ok(views.html.cms.locations( list ) )

  }

  def add() =  isAuthenticated { username => implicit request =>
  	implicit val cList = categoryList
    Ok(views.html.cms.locationEdit(None, form))

  }

  def save = isAuthenticated { username =>implicit request =>
  	implicit val cList = categoryList
    LocationFormHelp.form.bindFromRequest.fold(
      errors => {
        log.debug("save error={} " , errors)
        Ok(views.html.cms.locationEdit(None, errors))
      },
      locationForm => {

         service.saveLocation(locationForm.copy( category = createCategoryForm( locationForm.category.categoryId ))) match {
          case None => Ok(views.html.cms.locationEdit(None, LocationFormHelp.form.fill(locationForm), "同样 名字的地点已经存在 "))
          case Some(u) => Redirect(routes.Locations.list)
        }
      })

  }

  def edit(id: String) =  isAuthenticated { username => implicit request =>
    implicit val cList = categoryList
    service.getLocationById(id) match {
      case None => NotFound
      case Some(location) => Ok(views.html.cms.locationEdit(location.id, LocationFormHelp.form.fill(location)))
    }
  }

  def update(id: String) =  isAuthenticated { username => implicit request =>
    service.getLocationById(id) match {
      case None => NotFound
      case Some(orignLocation) => {
        implicit val cList = categoryList
        LocationFormHelp.form.bindFromRequest.fold(
          errors => {
            log.debug("update error={} " , errors)
            Ok(views.html.cms.locationEdit(Some(id), errors))
          },
          location => {
        	  	val updateLocation = location.copy( id =orignLocation.id , photo = orignLocation.photo ,
        	  	    category =  createCategoryForm(  orignLocation.category.categoryId ))
        	  	 
            service.updateLocation( updateLocation ) match {
              case None => Ok(views.html.cms.locationEdit(Some(id), LocationFormHelp.form.fill(location), "同样 名字的地点已经存在"))
              case Some(user) => Redirect(routes.Locations.list)
            }

          })
      }
    }
  }

  def del(id: String) =  isAuthenticated { username =>implicit request =>

    service.delLocationById(id)
    Redirect(routes.Locations.list)

  }

  def listSharePlan(page:Int = 0, pagesize :Int = 1000  ) =isAuthenticated { username =>
    implicit request =>
      val  planList = bagService.getSharePlanList( page, pagesize)
      // 如何过滤出 不存在的 plan , 已经删除的 plan
      Ok( views.html.cms.sharePlans( planList))
  }
  
  
  def assignSharePlan(   ) = isAuthenticated { username =>
    implicit request =>
      val postData = request.body.asFormUrlEncoded
      val planId = postData.flatMap(m => m.get("planId").flatMap(seq => seq.headOption)).getOrElse("")
      val action = postData.flatMap(m => m.get("action").flatMap(seq => seq.headOption)).getOrElse("")
       
      val jsVal = ( planId , action )match{
        case ( planId , action) if(planId !="" && ( action=="set" || action == "unset") ) => {
          bagService.setSharePlanAtHome( planId, action ) match{
            case None => Json.obj("success" -> false ,  "msg" ->"参数错误")
            case Some( _ ) => Json.obj("success" -> true,  "msg" ->"")
          }
        }
        case  _ => Json.obj("success" -> false,  "msg" ->" 参数错误")
      }
      Ok( jsVal )
  }
   
  
  
  
  	/**
  	 *   准备删除 
  	 */
   def listPlan(id: String ) = isAuthenticated { username =>
    implicit request =>
      
       service.getLocationById(id)  match {
        case None => NotFound
        case Some(location) => {
          val indexList =  bagService.getLocationPlanIndex( id )
         val indexViewList =  indexList.map( index => {
             bagService.get(index.bagId)  match{
               case None =>
                 log.debug("bag 不存在 bagId={}" , index.bagId)
                 index
               case Some( bag ) =>
                  val optionPlan = bag.getPlan(  index.planId )
                  val optionBaseUser = loginService.getBaseUser(bag.id, bag.usertype)  
                  log.debug("location plan index ,index={},  plan={},  baseUser={}", index , optionPlan, optionBaseUser)
                  index.copy( plan = optionPlan  ,  baseUser =optionBaseUser )
                   
                }
            }).filterNot( index => index.plan ==None || index.baseUser== None   )

            Ok( views.html.cms.locationPlanIndex( location , indexViewList))
        }
      }
  }
  
   /**
    * 准备删除 
    */
 def assignPlan(locationId: String ) = isAuthenticated { username =>
    implicit request =>
      
       service.getLocationById(locationId)  match {
        case None => NotFound
        case Some(location) => {
            val postData = request.body.asFormUrlEncoded
        	 val planId = postData.flatMap(m => m.get("planId").flatMap(seq => seq.headOption)).getOrElse("")
            val action = postData.flatMap(m => m.get("action").flatMap(seq => seq.headOption)).getOrElse("")

            val jsval = if(  planId == ""  ||  (action != "remove"  && action!="assign") )  { 
            		Json.obj("success" -> false,  "msg" ->" 参数错误")
              
            } else  if( action == "remove"){
            	 	service.updateLocation( location.copy( planId = None)) match{
            	 	  case None => Json.obj("success" -> false)
            	 	  case Some( x) => Json.obj("success" -> true)
            	 	}
            	   		
               }else{
                  bagService.getLocationPlanIndex( locationId  ).filter( index => index.planId == planId).headOption  match{
                  	case None => Json.obj("success" -> false)
                  	case Some( index ) => 
                  	 service.updateLocation( location.copy( planId = Some( planId ) )) match{
                  	    case None => Json.obj("success" -> false)
                  	    case Some( x) => Json.obj("success" -> true)
                  	 }
                  }
               }
                Ok(jsval)
            }
      }
  }
      
 
   
}