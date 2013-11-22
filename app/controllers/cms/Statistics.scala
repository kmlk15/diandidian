package controllers.cms


import play.api.libs.json
import play.api.mvc.Action
import play.api.mvc.Controller

import play.api.libs.json.Json
import org.slf4j.LoggerFactory
import models.ActionLogHelp
 
import org.joda.time.DateTime

object Statistics extends Controller  with AuthTrait {

  val log = LoggerFactory.getLogger(Statistics.getClass())

  val service = base.ActionLogServiceRegistry.actionLogService

  def index() = isAuthenticated { username =>
    implicit request =>

      val date = new DateTime
      val day = date.getDayOfMonth()
      val year = date.getYear()
      val month = date.getMonthOfYear()

      val loginStatsYear = service.stats(ActionLogHelp.login, 0, 0)
      val logintotal = loginStatsYear.map(_._2).sum
      val loginStatsMonth = service.stats(ActionLogHelp.login, year, 0)
      val loginStatsDay = service.stats(ActionLogHelp.login, year, month)
      log.debug("loginStatsYear={}", loginStatsYear)
      log.debug("loginStatsMonth={}", loginStatsMonth)
      log.debug("loginStatsDay={}", loginStatsDay)

      val bagStatsYear = service.stats(ActionLogHelp.addBag, 0, 0)
      val bagtotal = bagStatsYear.map(_._2).sum
      val bagStatsMonth = service.stats(ActionLogHelp.addBag, year, 0)
      val bagStatsDay = service.stats(ActionLogHelp.addBag, year, month)
      log.debug("bagStatsYear={}", bagStatsYear)
      log.debug("bagStatsMonth={}", bagStatsMonth)
      log.debug("bagStatsDay={}", bagStatsDay)
      val dataMap = Map[String , List[(Int,Int)]](
    		  "loginStatsYear" -> loginStatsYear,
    		  "loginStatsMonth" -> loginStatsMonth,
    		  "loginStatsDay" -> loginStatsDay,
    		  "bagStatsYear" -> bagStatsYear,
    		  "bagStatsMonth" -> bagStatsMonth,
    		  "bagStatsDay" -> bagStatsDay
      )
      
      Ok(views.html.cms.statistics( date , dataMap))

  }

}