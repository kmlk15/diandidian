package models

import org.scalatest.FunSuite
import org.slf4j.LoggerFactory

class BagHelpTest extends FunSuite {
  val log = LoggerFactory.getLogger(classOf[BagHelpTest])
  val statusName = "计划中"
  val planName = "testa"
  def createBag(statusName: String, planName: String): Bag = {
    val id = "1"
    val location1 = SimpleLocation(id = "1", name = "l1", enName = "l1en")
    val location2 = SimpleLocation(id = "2", name = "l2", enName = "l2en")

    val plan: Plan = Plan(id="aaaa",name = planName, list = List(location1, location2))
    val status = Status(statusName, Map(plan.name -> plan))
    val map = Map[String, Status](status.name -> status)
    val bag = Bag(id = id, map = map, typ = "" , usertype="")
    bag
  }

  test("原始数据不存在！") {
    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto()
    val tobag = BagHelp.update(bag, change)
    assert(bag === tobag)
  }
  test("原始数据存在， 但实际没有改变") {

    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName, planName, statusName, planName)
    val tobag = BagHelp.update(bag, change)
    assert(bag === tobag)

  }

  test("原始数据存在，plan 名字改变") {

    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName, planName, statusName, planName + "new")
    val tobag = BagHelp.update(bag, change)

    val newbag = createBag(statusName, planName + "new")
    assert(newbag.map.get(statusName).get.map.get(planName + "new") === tobag.map.get(statusName).get.map.get(planName + "new") )

  }

  test("原始数据存在，status  名字改变") {

    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName, planName, "准备去", planName)
    val tobag = BagHelp.update(bag, change)

    val newbag = createBag("准备去", planName)
    assert(newbag.map.get("准备去") === tobag.map.get("准备去"))

  }

  test("原始数据存在，status  名字改变 , plan 名字改变 ") {

    val bag = createBag(statusName, planName)
    val change = BagUpdateFromto(statusName, planName, "准备去", planName + "new")
    val tobag = BagHelp.update(bag, change)

    val newbag = createBag("准备去", planName + "new")
    assert(newbag.map.get("准备去").get.map.get(planName + "new") === tobag.map.get("准备去").get.map.get(planName + "new"))

  }

  test("原始数据存在，status  名字改变 , plan 名字改变, 但是 plan 的名字在 目标status 已经存在  ") {
    val location3 = SimpleLocation(id = "3", name = "l3", enName = "l3en")

    val bag = createBag(statusName, planName)
    val (bag2, optionPlan) = BagHelp.addLocation(bag, "准备去", "exist", List(location3))
    log.error("bag2={}", bag2)

    val change = BagUpdateFromto(statusName, planName, "准备去", "exist")
    val tobag = BagHelp.update(bag2, change)
    log.error("tobag={}", tobag)
    //保持不变， 这里 需要有办法 告知 调用者!, 或者要求 调用者在 调用前判断 目标是否已经存在
    assert( bag2 === tobag )
    
    val newbag = createBag("准备去", "exist")
    
    val lastbag = BagHelp.addLocation(newbag, "准备去", "exist", List(location3))

    assert(tobag.map.get("准备去").get.map.get("exist").get.list.toSet === Set(location3 ))

  }

  test("简单的性能估计") {
    val s = System.currentTimeMillis()
 
    var bag = createBag(statusName, planName)
    /*
     * 加入 10个点
     * 10个 plan
     * status 的数量 只有   4 个 
     */
    for {
      status <- List("计划去", " 准备去", "已经去")
      plan <- (1 to 10).map(i => "plan" + i)
      i <- 1 to 10
    } {
      val location3 = SimpleLocation(i.toString, "name" + i, "enName" + i)
      bag  = BagHelp.addLocation(bag, status, plan, List(location3))._1
    }

    log.debug("bag={}", bag)
    val e = System.currentTimeMillis()
    log.error("简单的性能估计 创建 对象 e-s={}", (e - s))

    val change = BagUpdateFromto(statusName, planName, "准备去", "exist")

    val bag2 = BagHelp.update(bag, change)

    log.debug("bag2={}", bag2.map.get("准备去").get.map.get("exist"))
    val e2 = System.currentTimeMillis()
    log.error("简单的性能估计 更新 plan  e2-e={}", (e2 - e))

    assert(true === true)
  }
}