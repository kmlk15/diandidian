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
     def createNewplan( bagId: String ):String 
     
    def addLocation(bagId: String, typ: String,usertype: String , statusName: String, planName: String, location: LocationForm): Boolean

    def removeLocation(bagId: String, statusName: String, planName: String, location: LocationForm): Boolean
    //地点分配日期
    def locationSignDate( bagId: String, statusName: String, planName: String, locationId: String , date: String ): Option[Bag]
     
    def get(bagId: String): Option[Bag]
    
    def del( bagId: String ): Int
    
    def save( bag: Bag ):Option[Bag] 
    
     /**
      * 根据 plan visible 
      * 1 private , 执行删除操作
      * 2 public , 先执行删除操作， 在执行加入操作， 
      */
    def indexPlan( bagId: String , plan: Plan ):Boolean
    
    
  }

}

trait BagServiceComponentImpl extends BagServiceComponent {

  this: BagServiceComponent =>
  val log = LoggerFactory.getLogger(classOf[BagServiceComponent])
  val dbname = "topo"
  override val bagService = new BagService {
    lazy val bagsMongoClient = mongoService.getMongoService[Bag]("bags", dbname)
    lazy val locationPlanIndexMongoClient = mongoService.getMongoService[LocationPlanIndex]("locationPlanIndex", dbname)		
    
     def createNewplan( bagId: String ):String ={
      
     val newPlanname =  get(bagId) match {
        case None =>{
          log.error("整个 背包不存在")
           ""
            
        }
       case Some( bag ) => {
         val planName = bag.map.get( BagHelp.defaultStatusName  ) match{
          case None =>
            log.error("status 不存在 ")
            BagHelp.defaultPlanName
          case Some( status ) =>
            val planmap  = status.map
            var i = 0 
            var bagname = BagHelp.defaultPlanName ; 
            while( planmap.get( bagname) != None){
              i = i +1 
              bagname = BagHelp.defaultPlanName  + i 
            }
            bagname
        }
         val newBag = BagHelp.addLocation(bag, BagHelp.defaultStatusName, planName, List(  ))
         update(newBag)
         
         planName
      }
      
      
       
      }
     //更新 mongo
     
     
     
     newPlanname
    }
 
    def addLocation(bagId: String, typ: String, usertype: String ,  statusName: String, planName: String, location: LocationForm): Boolean = {
      val simpleLocation = SimpleLocation(location.id.get, location.name, location.enName)
      get(bagId) match {
        case None =>
          log.debug("bag 还没有建立，创建新的 bag ")
          val plan = Plan( id = (new ObjectId().toString) , name= planName, list = List(simpleLocation))
          val status = Status(statusName, Map(plan.name -> plan))
          val bag = Bag(bagId, typ, usertype, Map(status.name -> status))
          bagsMongoClient.insert(bag)
        case Some(bag) =>
          val newBag = BagHelp.addLocation(bag, statusName, planName, List( simpleLocation ))
          log.debug("addlocation Bag={}", newBag)
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
        val newBag =   BagHelp.removeLocation(bag, statusName, planName, List(simpleLocation))
           if( newBag == bag ){
             false
           }else{
               update(newBag)
                true
           }
         
      }

    }
    
    def locationSignDate( bagId: String, statusName: String, planName: String, locationId: String , date: String ): Option[Bag] ={
     val splitChar = ";"
      get(bagId) match {
          case Some(bag) => {
          val result :Option[Option[Bag]]=   for{ 
              status <- bag.map.get(statusName)
              plan <- status.map.get(planName)
            }yield{
              //将地点移到合适的日期中
              // 如果已经分配，先需要删除
              log.debug( "map={}", plan.map )
            val afterRemove :Map[String, String] = plan.map.map( kv =>  kv._1 -> ( kv._2.split(splitChar).filter( str => str != locationId ).mkString(splitChar) ))
            log.debug ("afterRemove={}", afterRemove )
            val str : String =  afterRemove.get( date ).map(  str =>{ 
              val  arr = str.split(  splitChar )
              log.debug("arr={}", arr )
              val afterAdd =( arr ++ Array( locationId ) ).filterNot( _.isEmpty())
              log.debug("afterAdd={}", afterAdd )
              
              val afterDistinct = afterAdd.distinct
              log.debug("afterDistinct={}", afterDistinct )
              val newstr = afterDistinct.mkString( splitChar )
              log.debug("newstr={}", newstr )
              
              newstr
              }).getOrElse( locationId )
            log.debug( "date->str = {} ->{} "  ,  date  , str ,""  )
            val afterAdd =   afterRemove + ( date -> str )
            log.debug ("afterAdd={}", afterAdd )
             val newplan = plan.copy( map = afterAdd)
             val newstatus = status.copy(  map = status.map + (newplan.name -> newplan ))
             val newbag = bag.copy( map = bag.map + ( newstatus.name -> newstatus))
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
    
    def save( bag: Bag ):Option[Bag] ={
      get( bag.id) match{
        case None => 
           bagsMongoClient.insert(bag)
           Some( bag )
        case Some( existBag) => None
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
    
    def del( bagId: String ): Int ={
      
        val q = MongoDBObject()
        q.put("_id", bagId)
        bagsMongoClient.delete(q, WriteConcern.Normal)
    }
    
    /**
     * 建立 Location Plan 索引 
     */
    def indexPlan( bagId: String , plan: Plan ): Boolean ={
      
       val q = MongoDBObject("planId" -> plan.id)
    	locationPlanIndexMongoClient.delete(q, WriteConcern.Normal)
    	
      if( plan.visible=="public"){
        plan.list.map( simplelocation =>{
    	 val index = LocationPlanIndex( id= new ObjectId().toString, 
    			 bagId = bagId,
    			 planId = plan.id,
    			 locationId = simplelocation.id 
    	 )
    	 locationPlanIndexMongoClient.insert( index )
        })
      } 
      
     true
    }
  }
  
}

  