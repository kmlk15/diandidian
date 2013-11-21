package models

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
case class ActionLog (id: String ="" , ts: Long =0L , year:Int=0 , month:Int = 0 , day:Int = 0 , action:String=""  , usertype: String="" , userId: String="" ){

}