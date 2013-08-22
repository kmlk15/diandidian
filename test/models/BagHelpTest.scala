package models

import org.scalatest.FunSuite
import org.slf4j.LoggerFactory

class BagHelpTest extends FunSuite {
  val log = LoggerFactory.getLogger(classOf[BagHelpTest])
  val statusName = "计划中"
    val planName = "testa"
  def createBag(statusName: String , planName: String ): Bag = {
    val id = "1"
    val location1 = SimpleLocation(id = "1", name = "l1", enName = "l1en")
    val location2 = SimpleLocation(id = "2", name = "l2", enName = "l2en")

    val plan = Plan(name = planName , List(location1, location2))
    val status = Status(statusName, Map(plan.name -> plan))
    val map = Map[String, Status](status.name -> status)
    val bag = Bag(id = id, map = map, typ = "")
    bag
  }
  
  test("原始数据不存在！") {
    val bag = createBag(statusName, planName )
    val change = BagUpdateFromto( )
    val tobag = BagHelp.update(bag, change)
    assert(  bag === tobag ) 
  }
  test("原始数据存在， 但实际没有改变"){
    
    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName , planName, statusName , planName )
    val tobag = BagHelp.update(bag, change)
    assert(  bag === tobag ) 
    
  }

  test("原始数据存在，plan 名字改变"){
    
    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName , planName, statusName , planName+"new" )
    val tobag = BagHelp.update(bag, change)
    
    val newbag = createBag(statusName, planName+"new")
    assert(  newbag === tobag ) 
    
  }
  
  test("原始数据存在，status  名字改变"){
    
    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName , planName, "准备去" , planName )
    val tobag = BagHelp.update(bag, change)
    
    val newbag = createBag("准备去", planName)
    assert(  newbag.map.get("准备去") === tobag.map.get("准备去") ) 
    
  }
  
  test("原始数据存在，status  名字改变 , plan 名字改变 "){
    
    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName , planName, "准备去" , planName+"new" )
    val tobag = BagHelp.update(bag, change)
    
    val newbag = createBag("准备去", planName+"new")
    assert(  newbag.map.get("准备去") === tobag.map.get("准备去") ) 
    
  }
  
  test("原始数据存在，status  名字改变 , plan 名字改变, 但是 plan 的名字在 目标status 已经存在  "){
    
    val bag = createBag(statusName, planName)
    val tmpBag =  BagHelp.update(bag,  BagUpdateFromto(statusName , planName, statusName , planName+"new" ) )
    val change = BagUpdateFromto(statusName , planName, "准备去" , planName+"new" )
    val tobag = BagHelp.update(tmpBag, change)
    
    val newbag = createBag("准备去", planName+"new")
    assert(  newbag.map.get("准备去") === tobag.map.get("准备去") ) 
    
  }
  
}