package models

import org.joda.time.DateTime
/**
 * 用户活动日志记录
 * 1 登录
 * 2 +背包
 * 3 -背包
 * 4 +分享
 * 5 -分享
 * 年 月 日
 * 
 */
case class ActionLog (id:Option[ String] =None , ts: Long =0L , year:Int=0 , month:Int = 0 , day:Int = 0 , action:String=""  , usertype: String="" , userId: String="" ){

}

object ActionLogHelp{
  val login="login"
  val addPlan = "addPlan"
  val delPlan = "delPlan"
  val addShare = "addShare"
  val delShare = "delShare"
  val addBag = "addBag"
  
  def getDate:(Long , Int, Int, Int)={
    val ts = System.currentTimeMillis()
    val date = new DateTime
    val day = date.getDayOfMonth()
    val year = date.getYear()
    val month = date.getMonthOfYear()
    (ts, year , month, day)
  }
  
  def getTemplate( action:String = "" ,  usertype: String="" , userId: String="" ): ActionLog={
    val  (ts, year , month, day) = getDate
    ActionLog( ts = ts , year = year , month = month , day = day , action = action ,   usertype = usertype , userId =userId )
  }
  
  def  loginLog(   usertype: String="" , userId: String=""): ActionLog =  getTemplate(login ,    usertype , userId) 

  def  addBagLog( usertype: String="" , userId: String=""): ActionLog =  getTemplate(addBag ,  usertype , userId) 

  def  addPlanLog( usertype: String="" , userId: String=""): ActionLog =  getTemplate(addPlan ,  usertype , userId) 
  def  delPlanLog( usertype: String="" , userId: String=""): ActionLog =  getTemplate(delPlan ,  usertype , userId) 
  def  addShareLog(usertype: String="" , userId: String=""): ActionLog =  getTemplate(delShare , usertype , userId)  
  def  delShareLog(usertype: String="" , userId: String=""): ActionLog =  getTemplate(delShare , usertype , userId)    

}