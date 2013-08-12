package models

import org.scalatest.FunSuite
import org.slf4j.LoggerFactory
 

class HoursFormHelpTest extends  FunSuite{
val log = LoggerFactory.getLogger( classOf[HoursFormHelpTest])
  test("周一， 周五 一样"){
    val openclose = OpenClose("8:00","17:00")
    
    val hours = HoursForm( openclose , openclose ,openclose ,openclose ,openclose,openclose,openclose,openclose)
    val list =  HoursFormHelp.view(hours)
    list.foreach(  x=> log.error(x))
    assert ( list.size === 3  )
    
    val oc2 = OpenClose("7:00","14:00")
    val hours2 = HoursForm( openclose , oc2 ,openclose ,openclose ,openclose,openclose,openclose,openclose)
    val list2 = HoursFormHelp.view(hours2)
     list2.foreach(  x=> log.error(x))
    assert( list2.size === 4 )
    
    val oc3 = OpenClose("9:00","19:00")
    
    val hours3 = HoursForm( openclose , oc2 ,openclose ,openclose ,openclose,openclose,oc3,openclose)
    val list3 = HoursFormHelp.view(hours3)
     list3.foreach(  x=> log.error(x))
    assert( list3.size === 5 )
    

}
  
}