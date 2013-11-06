package services

import base.mongoService
import com.mongodb.casbah.commons.MongoDBObject
import org.bson.types.ObjectId
import com.mongodb.casbah.WriteConcern
import org.slf4j.LoggerFactory
import com.mongodb.DBObject
import play.api.libs.json.Json

import models._

trait BagServiceComponent {

  val bagService: BagService

  trait BagService {

    /**
     * 创建一个空的背包， 这里 实际时得到一个合适的名字
     * 只有 statusName == defaultStatus  下 可以创建
     * 名字规律 “背包” “背包1” “背包2” 。。。
     */
    def createNewplan(bagId: String): String

    def addLocation(bagId: String, typ: String, usertype: String, statusName: String, planName: String, location: LocationForm): Boolean

    def removeLocation(bagId: String, statusName: String, planName: String, location: LocationForm): Boolean
    //地点分配日期
    def locationSignDate(bagId: String, statusName: String, planName: String, locationId: String, date: String): Option[Bag]

    def get(bagId: String): Option[Bag]

    def del(bagId: String): Int

    def save(bag: Bag): Option[Bag]

    /**
     * 根据 plan visible
     * 1 private , 执行删除操作
     * 2 public , 先执行删除操作， 在执行加入操作，
     */
    def indexPlan(bagId: String, plan: Plan): Boolean

    /**
     * 设置 Plan 的可见性
     */
    def updatePlanVisible(bagId: String, statusName: String, planName: String, visible: String): play.api.libs.json.JsObject

    /**
     * 根据地点，查 公开的 Plan
     * 这里 实际使用时，是需要 planName , user name , user avatar
     */
    def getLocationPlanIndex(locationId: String): List[LocationPlanIndex]

    /**
     * 根据ID 得到 某一个具体 Index
     */
    def getLocationPlanIndexByPlanId(locationId: String , planId: String ): Option[LocationPlanIndex]

    /**
     * 得到 分享 plan 的 数据
     */
    def getSharePlan( planId: String): Option[SharePlan]
    
    /**
     * 更新 分享plan 数据
     */
    def updateSharePlan( plan: SharePlan): Option[SharePlan]
    
    /**
     * 删除 分享plan 
     */
    def delSharePlan( planId: String) : Option[SharePlan]
    
    /**
     * 
     * 得到 系统 中分享的背包 列表 
     */
    def getSharePlanList(page: Int , pagesize: Int) : List[SharePlan]
    
    /**
     * 得到显示在 首页上的  分享背包
     */
    def getHomePageSharePlanList( ): List[SharePlan]
    /**
     * 设置是否显示在 首页
     */
    def setSharePlanAtHome(planId: String , status: String): Option[SharePlan]
    
    /**
     * 设置 是否 进入 CMS 管理页面 
     */
     def setSharePlanShareIt(planId: String , status: Boolean): Option[SharePlan]
  }

}

trait BagServiceComponentImpl extends BagServiceComponent {

  this: BagServiceComponent =>
  val log = LoggerFactory.getLogger(classOf[BagServiceComponent])
  val dbname = "topo"
  override val bagService = new BagService {
    lazy val bagsMongoClient = mongoService.getMongoService[Bag]("bags", dbname)
    lazy val locationPlanIndexMongoClient = mongoService.getMongoService[LocationPlanIndex]("locationPlanIndex", dbname)
    lazy val shareplanMongoClient = mongoService.getMongoService[SharePlan]("shareplan", dbname)
    def createNewplan(bagId: String): String = {

      val newPlanname = get(bagId) match {
        case None => {
          log.error("整个 背包不存在")
          ""

        }
        case Some(bag) => {
          val planName = bag.map.get(BagHelp.defaultStatusName) match {
            case None =>
              log.error("status 不存在 ")
              BagHelp.defaultPlanName
            case Some(status) =>
              val planmap = status.map
              var i = 0
              var bagname = BagHelp.defaultPlanName;
              while (planmap.get(bagname) != None) {
                i = i + 1
                bagname = BagHelp.defaultPlanName + i
              }
              bagname
          }
          val newBag = BagHelp.addLocation(bag, BagHelp.defaultStatusName, planName, List())._1
          update(newBag)

          planName
        }

      }
      //更新 mongo

      newPlanname
    }

    def addLocation(bagId: String, typ: String, usertype: String, statusName: String, planName: String, location: LocationForm): Boolean = {
      val simpleLocation = SimpleLocation(location.id.get, location.name, location.enName)

      log.debug("bag  bagId={} ,  typ={}, usertype={}, statusName={}, planName={},simpleLocation={} ",
        bagId, typ, usertype, statusName, planName, simpleLocation)

      get(bagId) match {
        case None =>
          log.debug("还没有建立，创建新的 bag ")
          val plan = Plan(id = (new ObjectId().toString), name = planName, list = List(simpleLocation))
          val status = Status(statusName, Map(plan.name -> plan))
          val bag = Bag(bagId, typ, usertype, Map(status.name -> status))
          bagsMongoClient.insert(bag)
        case Some(bag) =>
          val (newBag, optionPlan) = BagHelp.addLocation(bag, statusName, planName, List(simpleLocation))
          log.debug("addlocation Bag={}", newBag)

          optionPlan match {
            case None =>
            case Some(plan) => indexPlan(bagId, plan)
          }

          update(newBag)

      }

      true
    }

    def removeLocation(bagId: String, statusName: String, planName: String, location: LocationForm): Boolean = {
      val simpleLocation = SimpleLocation(location.id.get, location.name, location.enName)
      get(bagId) match {
        case None =>
          log.debug("bag  不存在 ")
          false
        case Some(bag) =>
          val (newBag, optionPlan) = BagHelp.removeLocation(bag, statusName, planName, List(simpleLocation))
          val step1 = if (newBag == bag) {
            false
          } else {
            update(newBag)
            true
          }

          val step2: Boolean = optionPlan match {
            case None => true
            case Some(plan) => indexPlan(bagId, plan)

          }

          step1 && step2
      }

    }

    def locationSignDate(bagId: String, statusName: String, planName: String, locationId: String, date: String): Option[Bag] = {
      val splitChar = ";"
      get(bagId) match {
        case Some(bag) => {
          val result: Option[Option[Bag]] = for {
            status <- bag.map.get(statusName)
            plan <- status.map.get(planName)
          } yield {
            //将地点移到合适的日期中
            // 如果已经分配，先需要删除
            log.debug("map={}", plan.map)
            val afterRemove: Map[String, String] = plan.map.map(kv => kv._1 -> (kv._2.split(splitChar).filter(str => str != locationId).mkString(splitChar)))
            log.debug("afterRemove={}", afterRemove)
            val str: String = afterRemove.get(date).map(str => {
              val arr = str.split(splitChar)
              log.debug("arr={}", arr)
              val afterAdd = (arr ++ Array(locationId)).filterNot(_.isEmpty())
              log.debug("afterAdd={}", afterAdd)

              val afterDistinct = afterAdd.distinct
              log.debug("afterDistinct={}", afterDistinct)
              val newstr = afterDistinct.mkString(splitChar)
              log.debug("newstr={}", newstr)

              newstr
            }).getOrElse(locationId)
            log.debug("date->str = {} ->{} ", date, str, "")
            val afterAdd = afterRemove + (date -> str)
            log.debug("afterAdd={}", afterAdd)
            val newplan = plan.copy(map = afterAdd)
            val newstatus = status.copy(map = status.map + (newplan.name -> newplan))
            val newbag = bag.copy(map = bag.map + (newstatus.name -> newstatus))
            update(newbag)
          }
          result.getOrElse(None)
        }
        case None => None
      }
    }

    def update(bag: Bag): Option[Bag] = {

      val q = MongoDBObject()
      q.put("_id", bag.id)
      bagsMongoClient.update(q, bag, false, false, WriteConcern.Normal)
      Some(bag)

    }

    def save(bag: Bag): Option[Bag] = {
      get(bag.id) match {
        case None =>
          bagsMongoClient.insert(bag)
          Some(bag)
        case Some(existBag) => None
      }

    }
    def get(bagId: String): Option[Bag] = {
      if (bagId.trim() == "") {
        None
      } else {
        val q = MongoDBObject()
        q.put("_id", bagId)
        bagsMongoClient.find(q).headOption
      }
    }

    def del(bagId: String): Int = {

      val q = MongoDBObject()
      q.put("_id", bagId)
      bagsMongoClient.delete(q, WriteConcern.Normal)
    }

    def updatePlanVisible(bagId: String, statusName: String, planName: String, visible: String): play.api.libs.json.JsObject = {
      val normalVisible = if (visible == "public" || visible == "private") {
        visible
      } else {
        "private"
      }
      log.debug("normalVisible={}", normalVisible)
      get(bagId) match {
        case None =>
          Json.obj("success" -> false, "msg" -> "bag 不存在")
        case Some(bag) =>
          val planOption: Option[play.api.libs.json.JsObject] = for {
            status <- bag.map.get(statusName)
            plan <- status.map.get(planName)
          } yield {
            if (plan.visible != normalVisible || normalVisible == "public") {
              val newplan = plan.copy(visible = normalVisible)
              val newstatus = status.copy(map = status.map + (newplan.name -> newplan))
              val newbag = bag.copy(map = bag.map + (newstatus.name -> newstatus))
              update(newbag) match {
                case None => Json.obj("success" -> false, "msg" -> " update newbag ERROR")
                case Some(updatedBag) => {
                  //建立和修改  索引( locationId, bagId , planId , visible)
                  //如果是  private , 则执行删除
                  //如果是 public , 则执行加入
                  //这里的索引 用于  地点和 plan 建立关联，  实际 使用时， 还需要 再确认比较一次，因为 location 有可能被删除了
                  indexPlan(bagId, newplan)

                  Json.obj("success" -> true, "msg" -> "")
                }
              }
            } else {
              Json.obj("success" -> true, "msg" -> "no change")
            }
          }
          planOption.getOrElse(Json.obj("success" -> false, "msg" -> " plan 不存在"))

      }
    }

    /**
     * 建立 Location Plan 索引
     */
    def indexPlan(bagId: String, plan: Plan): Boolean = {

      if (plan.visible == "private") {
        val q = MongoDBObject("planId" -> plan.id)
        locationPlanIndexMongoClient.delete(q, WriteConcern.Normal)

      } else if (plan.visible == "public") {
        log.debug("public plan. plan.list={}", plan.list)
        val q = MongoDBObject("planId" -> plan.id)
        val existsLocationList = locationPlanIndexMongoClient.find(q).map(index => index.locationId)
        val newLocationList = plan.list.map(simplelocation => simplelocation.id)
        val needDelList = existsLocationList.diff(newLocationList)
        val needAddList = newLocationList.diff(existsLocationList)
        log.debug("existsLocationList={}", existsLocationList)
        log.debug("newLocationList={}", newLocationList)
        log.debug("needDelList={}", needDelList)
        log.debug("needAddList={}", needAddList)

        needDelList.map(locationId => {
          val q = MongoDBObject("planId" -> plan.id, "locationId" -> locationId)
          locationPlanIndexMongoClient.delete(q, WriteConcern.Normal)
        })

        needAddList.map(locationId => {
          val index = LocationPlanIndex(id = Some(new ObjectId().toString),
            bagId = bagId,
            planId = plan.id,
            locationId = locationId)
          log.debug("index={}", index)
          locationPlanIndexMongoClient.insert(index)
        })

      }

      true
    }

    def getLocationPlanIndex(locationId: String): List[LocationPlanIndex] = {

      val q = MongoDBObject("locationId" -> locationId)
      locationPlanIndexMongoClient.find(q)

    }

    def getLocationPlanIndexByPlanId(locationId: String, planId: String ): Option[LocationPlanIndex] = {

      val q = MongoDBObject("locationId" -> locationId , "planId" -> planId )
      locationPlanIndexMongoClient.find(q).headOption
    }

    
     /**
     * 得到 分享 plan 的 数据
     */
    def getSharePlan( planId: String): Option[SharePlan]= {
      val q = MongoDBObject("_id" -> planId  )
      shareplanMongoClient.find(q).headOption
    }
    
    /**
     * 更新 分享plan 数据
     */
    def updateSharePlan( plan: SharePlan): Option[SharePlan]= {
      
      getSharePlan(plan.id) match{
        case None =>
          shareplanMongoClient.insert(plan )
          Some(plan)
        case Some( _ ) =>
          val q = MongoDBObject("_id" -> plan.id  )
          shareplanMongoClient.update(q, plan , false, false, WriteConcern.Normal)
          Some(plan)
      }
      
    }
    
    /**
     * 删除 分享plan 
     */
    def delSharePlan( planId: String) : Option[SharePlan]= None
    
    
    /**
     * 
     * 得到 系统 中分享的背包 列表 
     */
    def getSharePlanList(page: Int , pagesize: Int) : List[SharePlan] = {
      val q = MongoDBObject( "shareIt" -> true  )
       shareplanMongoClient.find(q).toList 
    }
    
    /**
     * 得到显示在 首页上的  分享背包
     */
    def getHomePageSharePlanList( ): List[SharePlan] = {
       val q = MongoDBObject( "atHomePage" -> true  )
       shareplanMongoClient.find(q).toList
    }
    
    /**
     * 设置是否显示在 首页
     */
    def setSharePlanAtHome(planId: String , status: String): Option[SharePlan] = {
       getSharePlan(planId) match{
        case None => None
        case Some( plan  ) =>
          val q = MongoDBObject("_id" -> plan.id  )
           status match{
            case "set" =>  shareplanMongoClient.update(q, plan.copy( atHomePage = true) , false, false, WriteConcern.Normal)
            case "unset" =>  shareplanMongoClient.update(q, plan.copy( atHomePage = false), false, false, WriteConcern.Normal)
            case  _ => 
          }
          Some(plan)
      }
    }
    
     def setSharePlanShareIt(planId: String , status: Boolean): Option[SharePlan] = {
       getSharePlan(planId) match{
        case None => None
        case Some( plan  ) =>
          val q = MongoDBObject("_id" -> plan.id  )
          shareplanMongoClient.update(q, plan.copy( shareIt = status , atHomePage=if(!status) {false} else{ plan.atHomePage}) , false, false, WriteConcern.Normal)
          Some(plan)
      }
    }   
  }
}

  