package controllers.api

import base._
import services._
import models._
import play.api._
import play.api.mvc._
import sjson.json._
import JsonSerialization._
import dispatch.classic.json._
import com.mongodb.casbah.commons.MongoDBObjectBuilder
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.util.JSON
import com.mongodb.DBObject
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject

object LocationEndpoints extends Controller {

  val ls = locationRegistry locationService

  /*
   * Test of converting json string into object and back to json string by using sjson
   */
  def test = Action {
    val js = """{"planning":"1234567", "result":["234456"], "thumbnail":[], "hero":[]}"""
    val jv = JsValue.fromString(js)
    val ph = fromjson[Pictures](jv)
    Ok(JsValue.toJson(tojson(ph)))
  }

  def location=Action{
     val locations= "[%s]".format(ls.list.mkString(","))
    Ok(locations).as("application/json")
  }
  
  def example = Action {
    
    val json = """
[
{
	"name":"Ã¤Â¼Å¡Ã¨Â®Â®Ã¥Â±â€¢Ã¨Â§Ë†Ã¤Â¸Â­Ã¥Â¿Æ’",
	"addresses": {
		"street":"Ã¥ï¿½Å¡Ã¨Â§Ë†Ã©ï¿½â€œ1Ã¥ï¿½Â·",
		"district":"Ã¦Â¹Â¾Ã¤Â»â€�",
		"city":"Ã©Â¦â„¢Ã¦Â¸Â¯",
		"stateProvince":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã§â€°Â¹Ã¥Ë†Â¥Ã¨Â¡Å’Ã¦â€�Â¿Ã¥ï¿½â‚¬",
		"country":"Ã¤Â¸Â­Ã¥â€ºÂ½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": 0,
		"adults": 0,
		"children": 0,
		"students": 0,
		"seniors": 0,
		"currency":"HKD"
	},
	"categories":{
		"level_1":"Park",
		"level_2":"Garden Park"
	},
	"hours":{
		"monday" :"",
		"tuesday" :"",
		"wednesday" :"",
		"thursday" :"",
		"friday" :"",
		"saturday":"",
		"sunday":"",
		"holiday":""
	},
	"phones":{
		"General":"(852) 25374591"
	},
	"url":"",
	"pictures":{
		"planning": "/assets/images/dummy/planning/img01.jpg",
		"result":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_4.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_5.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_6.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_7.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_8.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_9.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_10.jpg"
		],
		"thumbnail":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_4.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_5.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_6.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_7.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_8.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_9.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_10.jpg"
		],
		"hero":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_4.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_5.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_6.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_7.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_8.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_9.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_10.jpg"
		]
		
	},
	"fact":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã¦Å“Æ’Ã¨Â­Â°Ã¥Â±â€¢Ã¨Â¦Â½Ã¤Â¸Â­Ã¥Â¿Æ’Ã¦ËœÂ¯Ã©Â¦â„¢Ã¦Â¸Â¯Ã§Å¡â€žÃ¤Â¸Â»Ã¨Â¦ï¿½Ã¥Â¤Â§Ã¥Å¾â€¹Ã¦Å“Æ’Ã¨Â­Â°Ã¥ï¿½Å Ã¥Â±â€¢Ã¨Â¦Â½Ã¥Â Â´Ã¥Å“Â°Ã¯Â¼Å’Ã¤Â½ï¿½Ã¦â€“Â¼Ã©Â¦â„¢Ã¦Â¸Â¯Ã¥Â³Â¶Ã§ï¿½Â£Ã¤Â»â€�Ã¥Å’â€”Ã¥Â²Â¸Ã¯Â¼Å’Ã¦ËœÂ¯Ã©Â¦â„¢Ã¦Â¸Â¯Ã¥Å“Â°Ã¦Â¨â„¢Ã¤Â¹â€¹Ã¤Â¸â‚¬. 2012Ã¥Â¹Â´Ã¯Â¼Å’Ã©Â¦â„¢Ã¦Â¸Â¯Ã¦Å“Æ’Ã¨Â­Â°Ã¥Â±â€¢Ã¨Â¦Â½Ã¤Â¸Â­Ã¥Â¿Æ’Ã¥Å“Â¨ CEI Asia magazineÃ¯Â¼Å’Ã§Â¬Â¬10Ã¥ÂºÂ¦Ã¦Â¦Â®Ã¨â€ ÂºÃ¤ÂºÅ¾Ã¦Â´Â²Ã¦Å“â‚¬Ã¤Â½Â³Ã¦Å“Æ’Ã¨Â­Â°Ã¥Â±â€¢Ã¨Â¦Â½Ã¤Â¸Â­Ã¥Â¿Æ’Ã¦Â®Å Ã¦Â¦Â®Ã£â‚¬â€šÃ¥ï¿½Å’Ã¥Â¹Â´Ã¯Â¼Å’Ã©Â¦â„¢Ã¦Â¸Â¯Ã¦Å“Æ’Ã¨Â­Â°Ã¥Â±â€¢Ã¨Â¦Â½Ã¤Â¸Â­Ã¥Â¿Æ’Ã¥Å“Â¨Ã§Â¬Â¬23Ã¥Â±â€ TTGÃ¦â€”â€¦Ã©ï¿½Å Ã¦Â¥Â­Ã¥Â¤Â§Ã§ï¿½Å½Ã¤Â¸Â­Ã¯Â¼Å’Ã§Â¬Â¬4Ã¥ÂºÂ¦Ã§ï¿½Â²Ã©ï¿½Â¸Ã§â€šÂºÃ¦Å“â‚¬Ã¤Â½Â³Ã¦Å“Æ’Ã¨Â­Â°Ã¥ï¿½Å Ã¥Â±â€¢Ã¨Â¦Â½Ã¤Â¸Â­Ã¥Â¿Æ’Ã¦Â®Å Ã¦Â¦Â®Ã£â‚¬â€š"
}
,
{
	"name":"Ã¦ÂµÂ·Ã¦Â´â€¹Ã¥â€¦Â¬Ã¥â€ºÂ­",
	"addresses": {
		"street":"Ã©Â»Æ’Ã§Â«Â¹Ã¥ï¿½â€˜Ã©ï¿½â€œ180Ã¨â„¢Å¸",
		"district":"Ã¥ï¿½â€”Ã¥ï¿½â‚¬",
		"city":"Ã©Â¦â„¢Ã¦Â¸Â¯",
		"stateProvince":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã§â€°Â¹Ã¥Ë†Â¥Ã¨Â¡Å’Ã¦â€�Â¿Ã¥ï¿½â‚¬",
		"country":"Ã¤Â¸Â­Ã¥â€ºÂ½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": 120,
		"adults": 120,
		"children": 60,
		"students": 90,
		"seniors": 60,
		"currency":"HKD"
	},
	"categories":{
		"level_1":"Park",
		"level_2":"Public Park"
	},
	"hours":{
		"monday" :"10am - 7pm",
		"tuesday" :"10am - 7pm",
		"wednesday" :"10am - 7pm",
		"thursday" :"10am - 7pm",
		"friday" :"10am - 7pm",
		"saturday":"10am - 7pm",
		"sunday":"10am - 7pm",
		"holiday":"10am - 7pm"
	},
	"phones":{
		"General":"(852) 39232323"
	},
	"url":"",
	"pictures":{
		"planning": "/assets/images/dummy/planning/img02.jpg",
		"result":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		],
		"thumbnail":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"
		],
		"hero":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		]
		
	},
	"fact":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã¦ÂµÂ·Ã¦Â´â€¹Ã¥â€¦Â¬Ã¥Å“â€™Ã¦ËœÂ¯Ã¤Â¸â‚¬Ã¥â‚¬â€¹Ã¤Â¸â€“Ã§â€¢Å’Ã§Â´Å¡Ã§Å¡â€žÃ¦ÂµÂ·Ã¦Â´â€¹Ã¥â€¹â€¢Ã§â€°Â©Ã¤Â¸Â»Ã©Â¡Å’Ã¦Â¨â€šÃ¥Å“â€™Ã¯Â¼Å’Ã©â€ºâ€ Ã¥Â¨â€ºÃ¦Â¨â€šÃ£â‚¬ï¿½Ã¦â€¢â„¢Ã¨â€šÂ²Ã¥ï¿½Å Ã¤Â¿ï¿½Ã¨â€šÂ²Ã¦â€“Â¼Ã¤Â¸â‚¬Ã©Â«â€�Ã£â‚¬â€šÃ¨â€¡Âª1977Ã¥Â¹Â´Ã©â€“â€¹Ã¥Â¹â€¢Ã¤Â»Â¥Ã¤Â¾â€ Ã¯Â¼Å’Ã©Â¦â„¢Ã¦Â¸Â¯Ã¦ÂµÂ·Ã¦Â´â€¹Ã¥â€¦Â¬Ã¥Å“â€™Ã¤Â¸ï¿½Ã¦â€“Â·Ã¦â€ºÂ´Ã¦â€“Â°Ã¨Â¨Â­Ã¦â€“Â½Ã¯Â¼Å’Ã¥Â»Â£Ã¥ï¿½â€”Ã©ï¿½Å Ã¥Â®Â¢Ã¦Â­Â¡Ã¨Â¿Å½Ã¯Â¼Å’2012Ã¥Â¹Â´Ã¦â€ºÂ´Ã¥â€¹â€¡Ã¥Â¥ÂªÃ¦Â¥Â­Ã§â€¢Å’Ã¦Å“â‚¬Ã©Â«ËœÃ¦Â®Å Ã¦Â¦Â®Ã§Å¡â€žÃ£â‚¬Å’Ã¥â€¦Â¨Ã§ï¿½Æ’Ã¦Å“â‚¬Ã¤Â½Â³Ã¤Â¸Â»Ã©Â¡Å’Ã¥â€¦Â¬Ã¥Å“â€™Ã£â‚¬ï¿½Ã¥Â¤Â§Ã§ï¿½Å½(Applause Award)Ã¯Â¼Å’Ã¦ËœÂ¯Ã¤ÂºÅ¾Ã¦Â´Â²Ã§Â¬Â¬Ã¤Â¸â‚¬Ã©â€“â€œÃ§ï¿½Â²Ã¥Â¾â€”Ã©â‚¬â„¢Ã¥â‚¬â€¹Ã§ï¿½Å½Ã©Â â€¦Ã§Å¡â€žÃ¤Â¸Â»Ã©Â¡Å’Ã¥â€¦Â¬Ã¥Å“â€™Ã¯Â¼ï¿½"
}
,
{
	"name":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã¥Â²â€ºÃ§â€�ÂµÃ¨Â½Â¦",
	"addresses": {
		"street":"",
		"district":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã¥Â²â€º",
		"city":"Ã©Â¦â„¢Ã¦Â¸Â¯",
		"stateProvince":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã§â€°Â¹Ã¥Ë†Â¥Ã¨Â¡Å’Ã¦â€�Â¿Ã¥ï¿½â‚¬",
		"country":"Ã¤Â¸Â­Ã¥â€ºÂ½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": 0,
		"adults": 0,
		"children": 0,
		"students": 0,
		"seniors": 0,
		"currency":"HKD"
	},
	"categories":{
		"level_1":"Park",
		"level_2":"Theme Park"
	},
	"hours":{
		"monday" :"",
		"tuesday" :"",
		"wednesday" :"",
		"thursday" :"",
		"friday" :"",
		"saturday":"",
		"sunday":"",
		"holiday":""
	},
	"phones":{
		"General":"(852)3923 2888"
	},
	"url":"www.oceanpark.com.hk",
	"pictures":{
		"planning": "/assets/images/dummy/planning/img05.jpg",
		"result":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		],
		"thumbnail":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"
		],
		"hero":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		]
		
	},
	"fact":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã©â€ºÂ»Ã¨Â»Å Ã¦ËœÂ¯Ã©Â¦â„¢Ã¦Â¸Â¯Ã§Å¡â€žÃ¤Â¸â‚¬Ã¥â‚¬â€¹Ã¨Â·Â¯Ã©ï¿½Â¢Ã©â€ºÂ»Ã¨Â»Å Ã§Â³Â»Ã§ÂµÂ±Ã¯Â¼Å’Ã¤Â¾â€ Ã¥Â¾â‚¬Ã©Â¦â„¢Ã¦Â¸Â¯Ã¥Â³Â¶Ã¥ï¿½â‚¬Ã§Å¡â€žÃ§Â­Â²Ã§Â®â€¢Ã§ï¿½Â£Ã¥ï¿½Å Ã¥Â â€¦Ã¥Â°Â¼Ã¥Å“Â°Ã¥Å¸Å½Ã¯Â¼Å’Ã¥ï¿½Â¦Ã¦Å“â€°Ã§â€™Â°Ã¥Â½Â¢Ã¦â€�Â¯Ã§Â·Å¡Ã¤Â¾â€ Ã¥Â¾â‚¬Ã¨Â·â€˜Ã©Â¦Â¬Ã¥Å“Â°Ã¯Â¼Å’Ã¦Â¯ï¿½Ã¦â€”Â¥Ã¥Â¹Â³Ã¥ï¿½â€¡Ã¦Å½Â¥Ã¨Â¼â€°22Ã¨ï¿½Â¬Ã¤ÂºÂºÃ¦Â¬Â¡Ã§Å¡â€žÃ¤Â¹ËœÃ¥Â®Â¢Ã¯Â¼Å’Ã¦ËœÂ¯Ã¥â€¦Â¨Ã§ï¿½Æ’Ã§ï¿½Â¾Ã¥Â­ËœÃ¥â€�Â¯Ã¤Â¸â‚¬Ã¥â€¦Â¨Ã¦â€¢Â¸Ã¦Å½Â¡Ã§â€�Â¨Ã©â€ºâ„¢Ã¥Â±Â¤Ã©â€ºÂ»Ã¨Â»Å Ã§Å¡â€žÃ©â€ºÂ»Ã¨Â»Å Ã§Â³Â»Ã§ÂµÂ±Ã¯Â¼Ë†Ã¥ï¿½Â¦Ã¦Å“â€°Ã¨â€¹Â±Ã¥Å“â€¹Ã©Â»â€˜Ã¦Â±Â Ã©â€ºÂ»Ã¨Â»Å Ã¥ï¿½Å Ã¥Å¸Æ’Ã¥ï¿½Å Ã¤ÂºÅ¾Ã¦Â­Â·Ã¥Â±Â±Ã¥Â¤Â§Ã¦Â¸Â¯Ã©â€ºÂ»Ã¨Â»Å Ã©Æ’Â¨Ã¥Ë†â€ Ã¨Â·Â¯Ã§Â·Å¡Ã¤Â½Â¿Ã§â€�Â¨Ã©â€ºâ„¢Ã¥Â±Â¤Ã©â€ºÂ»Ã¨Â»Å Ã¯Â¼Å’Ã©ï¿½Å¾Ã¥â€¦Â¨Ã¦â€¢Â¸Ã¦Å½Â¡Ã§â€�Â¨Ã¯Â¼â€°Ã£â‚¬â€šÃ©Â¦â„¢Ã¦Â¸Â¯Ã©â€ºÂ»Ã¨Â»Å Ã¥Å“Â¨1904Ã¥Â¹Â´Ã¦Å â€¢Ã¥â€¦Â¥Ã¦Å“ï¿½Ã¥â€¹â„¢Ã¯Â¼Å’Ã¦ËœÂ¯Ã©Â¦â„¢Ã¦Â¸Â¯Ã¦Â­Â·Ã¥ï¿½Â²Ã¦Å“â‚¬Ã§â€šÂºÃ¦â€šÂ Ã¤Â¹â€¦Ã§Å¡â€žÃ¤ÂºÂ¤Ã©â‚¬Å¡Ã¥Â·Â¥Ã¥â€¦Â·Ã¤Â¹â€¹Ã¤Â¸â‚¬Ã£â‚¬â€šÃ©Â¦â„¢Ã¦Â¸Â¯Ã©â€ºÂ»Ã¨Â»Å Ã¤Â¸ï¿½Ã¥Æ’â€¦Ã¦ËœÂ¯Ã¦Â¸Â¯Ã¥Â³Â¶Ã¥ï¿½â‚¬Ã¥Â±â€¦Ã¦Â°â€˜Ã§Å¡â€žÃ©â€¡ï¿½Ã¨Â¦ï¿½Ã¤ÂºÂ¤Ã©â‚¬Å¡Ã¥Â·Â¥Ã¥â€¦Â·Ã¯Â¼Å’Ã¤Â¹Å¸Ã¦Ë†ï¿½Ã§â€šÂºÃ¥Â¤â€“Ã¥Å“Â°Ã¦â€”â€¦Ã¥Â®Â¢Ã¨Â§â‚¬Ã¥â€¦â€°Ã§Å¡â€žÃ¨â€˜â€”Ã¥ï¿½ï¿½Ã¦â„¢Â¯Ã©Â»Å¾."
}
,
{
	"name":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã¤ÂºÂ¤Ã¦Ëœâ€œÃ¥Â»Â£Ã¥Â Â´",
	"addresses": {
		"street":"Ã¥ÂºÂ·Ã¦Â¨â€šÃ¥Â»Â£Ã¥Â Â´8Ã¨â„¢Å¸",
		"district":"Ã¤Â¸Â­Ã¨Â¥Â¿Ã¥ï¿½â‚¬",
		"city":"Ã©Â¦â„¢Ã¦Â¸Â¯",
		"stateProvince":"Ã©Â¦â„¢Ã¦Â¸Â¯Ã§â€°Â¹Ã¥Ë†Â¥Ã¨Â¡Å’Ã¦â€�Â¿Ã¥ï¿½â‚¬",
		"country":"Ã¤Â¸Â­Ã¥â€ºÂ½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": 0,
		"adults": 0,
		"children": 0,
		"students": 0,
		"seniors": 0,
		"currency":"HKD"
	},
	"categories":{
		"level_1":"Architecture",
		"level_2":"Skyscraper"
	},
	"hours":{
		"monday" :"",
		"tuesday" :"",
		"wednesday" :"",
		"thursday" :"",
		"friday" :"",
		"saturday":"",
		"sunday":"",
		"holiday":""
	},
	"phones":{
		"General":"(852)2748 8080"
	},
	"url":"www.oceanpark.com.hk",
	"pictures":{
		"planning": "/assets/images/dummy/planning/img07.jpg",
		"result":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		],
		"thumbnail":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"
		],
		"hero":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		]
		
	},
	"fact":"Ã¤ÂºÂ¤Ã¦Ëœâ€œÃ¥Â»Â£Ã¥Â Â´Ã¦ËœÂ¯Ã©Â¦â„¢Ã¦Â¸Â¯Ã§Å¡â€žÃ¤Â¸â‚¬Ã§Âµâ€žÃ¨Â¾Â¦Ã¥â€¦Â¬Ã¥Â®Â¤Ã¥Â¤Â§Ã¦Â¨â€œÃ¯Â¼Å’Ã¤Â½ï¿½Ã¦â€“Â¼Ã©Â¦â„¢Ã¦Â¸Â¯Ã¥Â³Â¶Ã¤Â¸Â­Ã§â€™Â°Ã¥ÂºÂ·Ã¦Â¨â€šÃ¥Â»Â£Ã¥Â Â´8Ã¨â„¢Å¸Ã¯Â¼Å’Ã©â€žÂ°Ã¦Å½Â¥Ã¦Â¸Â¯Ã©ï¿½ÂµÃ©Â¦â„¢Ã¦Â¸Â¯Ã§Â«â„¢Ã£â‚¬â€šÃ¤ÂºÂ¤Ã¦Ëœâ€œÃ¥Â»Â£Ã¥Â Â´Ã¥â€¦Â±Ã¦Å“â€°3Ã¥ÂºÂ§Ã¨Â¾Â¦Ã¥â€¦Â¬Ã¥Â¤Â§Ã¦Â¨â€œÃ¯Â¼Å’Ã¥Âºâ€¢Ã¥Â±Â¤Ã¨Â¨Â­Ã¦Å“â€°Ã¥â€¢â€ Ã¥Â Â´Ã¯Â¼Å’Ã¥Å“Â°Ã¤Â¸â€¹Ã¨Â¨Â­Ã¦Å“â€°Ã¥Â·Â´Ã¥Â£Â«Ã§Â¸Â½Ã§Â«â„¢Ã£â‚¬â€šÃ¤Â¸Â¦Ã¦Å“â€°Ã¥Â¤Å¡Ã¦Â¢ï¿½Ã¨Â¡Å’Ã¤ÂºÂºÃ¥Â¤Â©Ã¦Â©â€¹Ã©â‚¬Â£Ã¦Å½Â¥Ã¥Å“â€¹Ã©Å¡â€ºÃ©â€¡â€˜Ã¨Å¾ï¿½Ã¤Â¸Â­Ã¥Â¿Æ’Ã¯Â¼Å’Ã¦â‚¬Â¡Ã¥â€™Å’Ã¥Â¤Â§Ã¥Â»Ë†Ã¥ï¿½Å Ã§â€™Â°Ã§ï¿½Æ’Ã¥Â¤Â§Ã¥Â»Ë†Ã£â‚¬â€šÃ¥Å“Â¨Ã§Â¬Â¬Ã¤ÂºÅ’Ã¥ÂºÂ§Ã¦Â­Â£Ã©â€“â‚¬Ã¥â„¢Â´Ã¦Â°Â´Ã¦Â±Â Ã¦â€œÂºÃ¦â€�Â¾Ã¦Å“â€°Ã¥ï¿½Â°Ã§ï¿½Â£Ã©â€ºâ€¢Ã¥Â¡â€˜Ã¥Â¤Â§Ã¥Â¸Â«Ã¦Å“Â±Ã©Å ËœÃ©â€˜â€žÃ©â‚¬Â Ã§Å¡â€žÃ¥Â¤ÂªÃ¦Â¥ÂµÃ©â€ºâ€¢Ã¥Â¡â€˜Ã¯Â¼Å’Ã¥ï¿½Å Ã¤Â¸â‚¬Ã¥Â°ï¿½Ã¦Â°Â´Ã§â€°â€ºÃ©â€ºâ€¢Ã¥Â¡â€˜Ã£â‚¬â€šÃ¥Å“Â¨Ã§Â¬Â¬Ã¤Â¸â‚¬Ã¥ÂºÂ§Ã§Å¡â€žÃ¦Â­Â£Ã©â€“â‚¬Ã¦Â°Â´Ã¦Â±Â Ã¤Â¸Â­Ã¥Â¤Â®Ã¯Â¼Å’Ã¨Â£ï¿½Ã¦â€�Â¾Ã¦Å“â€°Ã¤Â¸â‚¬Ã¥â‚¬â€¹8Ã¥Â­â€”Ã¥Â½Â¢Ã§Å¡â€žÃ©â€ºâ€¢Ã¥Â¡â€˜Ã£â‚¬â€š"
}
,
{
	"name":"Ã¦Â¾Â³Ã©â€”Â¨Ã¦â€”â€¦Ã©ï¿½Å Ã¥Â¡â€�",
	"addresses": {
		"street":"Ã¨Â§â‚¬Ã¥â€¦â€°Ã¥Â¡â€�Ã¥â€°ï¿½Ã¥Å“Â°",
		"district":"Ã¥ï¿½â€”Ã§ï¿½Â£",
		"city":"Ã¦Â¾Â³Ã©â€”Â¨",
		"stateProvince":"Ã¦Â¾Â³Ã©â€”Â¨Ã§â€°Â¹Ã¥Ë†Â¥Ã¨Â¡Å’Ã¦â€�Â¿Ã¥ï¿½â‚¬",
		"country":"Ã¤Â¸Â­Ã¥â€ºÂ½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": "58Ã¦Â¨â€œÃ¨Â§â‚¬Ã¥â€¦â€°Ã¤Â¸Â»Ã¥Â±Â¤Ã¯Â¼Å¡45Ã¥â€¦Æ’Ã¯Â¼Å’ 61Ã¦Â¨â€œÃ¨Â§â‚¬Ã¥â€¦â€°Ã¥Â±Â¤Ã¯Â¼Å¡30Ã¥â€¦Æ’Ã¯Â¼Å’ 70Ã¥â€¦Æ’Ã¥Â¥â€”Ã§Â¥Â¨",
		"adults": 0,
		"children": 0,
		"students": 0,
		"seniors": 0,
		"currency":"HKD"
	},
	"categories":{
		"level_1":"Religious Site",
		"level_2":"Taoism Temple"
	},
	"hours":{
		"monday" :"",
		"tuesday" :"",
		"wednesday" :"",
		"thursday" :"",
		"friday" :"",
		"saturday":"",
		"sunday":"",
		"holiday":""
	},
	"phones":{
		"General":"Ã¯Â¼Ë†853Ã¯Â¼â€° 933 339"
	},
	"url":"www.oceanpark.com.hk",
	"pictures":{
		"planning": "/assets/images/dummy/planning/img03.jpg",
		"result":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		],
		"thumbnail":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"
		],
		"hero":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		]
		
	},
	"fact":"Ã§â€žÂ¡Ã¨Â«â€“Ã¦ËœÂ¯Ã¥Å“Â¨Ã¦â„¢Â¨Ã¥â€¦â€°Ã¤Â¸â€¹Ã©Â£Â½Ã¨Â¦Â½Ã§ï¿½Â Ã¦ÂµÂ·Ã¥Â°ï¿½Ã¥Â²Â¸Ã¦â„¢Â¯Ã§Â·Â»Ã¯Â¼Å’Ã¥ï¿½Ë†Ã¦Ë†â€“Ã¦ËœÂ¯Ã¦â€“Â¼Ã¦â„¢Å¡Ã¤Â¸Å Ã¥ÂºÅ“Ã§Å¾Â°Ã§â€™â‚¬Ã§â€™Â¨Ã§Å¡â€žÃ©â€ â€°Ã¤ÂºÂºÃ§Â¾Å½Ã¦â„¢Â¯Ã¯Â¼Å’Ã¦Â¾Â³Ã©â€“â‚¬Ã¦â€”â€¦Ã©ï¿½Å Ã¥Â¡â€�Ã¦Å“Æ’Ã¥Â±â€¢Ã¥Â¨â€ºÃ¦Â¨â€šÃ¤Â¸Â­Ã¥Â¿Æ’Ã¥ï¿½â€¡Ã¦ËœÂ¯Ã¦â€šÂ¨Ã§Å¡â€žÃ¦Å“â‚¬Ã¤Â½Â³Ã©ï¿½Â¸Ã¦â€œâ€¡Ã£â‚¬â€šÃ¦â€šÂ¨Ã¥ï¿½Â¯Ã¤Â¹ËœÃ¥ï¿½ï¿½Ã¥Â­ï¿½Ã¥Â½Ë†Ã¥ï¿½â€¡Ã©â„¢ï¿½Ã¦Â©Å¸Ã§â€�Â±Ã¤Â¸â€¹Ã¨â‚¬Å’Ã¤Â¸Å Ã¨ÂµÂ°Ã¨Â¨ÂªÃ¥Â¡â€�Ã¥â€¦Â§Ã¦Â¯ï¿½Ã¥Â±Â¤Ã§Å¡â€žÃ§â€°Â¹Ã¨â€°Â²Ã¯Â¼Å’Ã¥â€¦Ë†Ã¥Ë†Â°Ã¨Â§â‚¬Ã¥â€¦â€°Ã¥Â±Â¤Ã¦Â¬Â£Ã¨Â³Å¾Ã¥â€ºâ€ºÃ¥â€˜Â¨Ã©Â¢Â¨Ã¦â„¢Â¯Ã¯Â¼Å’Ã¨Â·Å¸Ã¨â€˜â€”Ã¥â€œï¿½Ã¥Å¡ï¿½Ã¤Â¸â‚¬Ã¤Â¸â€¹360Ã‚Â°Ã¦â€”â€¹Ã¨Â½â€°Ã©Â¤ï¿½Ã¥Â»Â³Ã§Å¡â€žÃ§Â¾Å½Ã¥â€˜Â³Ã¨â€¡ÂªÃ¥Å Â©Ã©Â¤ï¿½Ã¯Â¼Å’Ã§â€žÂ¶Ã¥Â¾Å’Ã¥â€ ï¿½Ã¥â€ºâ€ºÃ¨â„¢â€¢Ã¨ÂµÂ°Ã¨ÂµÂ°Ã§â€ºÂ¡Ã¤ÂºÂ«Ã¦â€šÂ Ã©â€“â€™Ã£â‚¬â€š"
}
,
{
	"name":"Ã¥Â¤Â§Ã¤Â¸â€°Ã¥Â·Â´Ã§â€°Å’Ã¥ï¿½Å ",
	"addresses": {
		"street":"Ã¥ï¿½Å Ã¥Â²â€ºÃ§â€šÂ®Ã¥ï¿½Â°Ã¥Â±Â±Ã¤Â¸â€¹Ã¥Å“Â£Ã¤Â¿ï¿½Ã§Â½â€”Ã¦â€¢â„¢Ã¥Â â€šÃ¥â€°ï¿½Ã¥Â£ï¿½Ã©ï¿½â€”Ã¨Â¿Â¹",
		"district":"Ã¥Â¤Â§Ã¤Â¸â€°Ã¥Â·Â´",
		"city":"Ã¦Â¾Â³Ã©â€”Â¨",
		"stateProvince":"Ã¦Â¾Â³Ã©â€”Â¨Ã§â€°Â¹Ã¥Ë†Â¥Ã¨Â¡Å’Ã¦â€�Â¿Ã¥ï¿½â‚¬",
		"country":"Ã¤Â¸Â­Ã¥â€ºÂ½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": 0,
		"adults": 0,
		"children": 0,
		"students": 0,
		"seniors": 0,
		"currency":"HKD"
	},
	"categories":{
		"level_1":"Religious Site",
		"level_2":"Taoism Temple"
	},
	"hours":{
		"monday" :"",
		"tuesday" :"",
		"wednesday" :"",
		"thursday" :"",
		"friday" :"",
		"saturday":"",
		"sunday":"",
		"holiday":""
	},
	"phones":{
		"General":"Ã¯Â¼Ë†853Ã¯Â¼â€° 933 339"
	},
	"url":"www.oceanpark.com.hk",
	"pictures":{
		"result":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		],
		"thumbnail":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"
		],
		"hero":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		]
		
	},
	"fact":"Ã¥Â¤Â§Ã¤Â¸â€°Ã¥Â·Â´Ã§â€°Å’Ã¥ï¿½Å Ã¯Â¼Å’Ã¥â€¦Â¶Ã¦Â­Â£Ã¥Â¼ï¿½Ã¥ï¿½ï¿½Ã§Â§Â°Ã¤Â¸ÂºÃ¥Å“Â£Ã¤Â¿ï¿½Ã§Â¦â€žÃ¥Â¤Â§Ã¦â€¢â„¢Ã¥Â â€šÃ©ï¿½â€”Ã¥ï¿½â‚¬Ã¯Â¼Ë†Ã¨â€˜Â¡Ã¨ï¿½â€žÃ§â€°â„¢Ã¨ÂªÅ¾Ã¯Â¼Å¡RuÃƒÂ­nas da Antiga Catedral de SÃƒÂ£o PauloÃ¯Â¼â€°Ã¯Â¼Å’Ã¤Â¸â‚¬Ã¨Ë†Â¬Ã§Â¨Â±Ã§â€šÂºÃ¥Â¤Â§Ã¤Â¸â€°Ã¥Â·Â´Ã¦Ë†â€“Ã§â€°Å’Ã¥ï¿½Å Ã¯Â¼Å’Ã¦ËœÂ¯Ã¦Â¾Â³Ã©â€“â‚¬Ã¥Â¤Â©Ã¤Â¸Â»Ã¤Â¹â€¹Ã¦Â¯ï¿½Ã¦â€¢â„¢Ã¥Â â€šÃ¯Â¼Ë†Ã¨ï¿½â€“Ã¤Â¿ï¿½Ã§Â¥Â¿Ã¦â€¢â„¢Ã¥Â â€šÃ¯Â¼â€°Ã¦Â­Â£Ã©ï¿½Â¢Ã¥â€°ï¿½Ã¥Â£ï¿½Ã§Å¡â€žÃ©ï¿½ÂºÃ¥ï¿½â‚¬Ã£â‚¬â€šÃ¥Â¤Â§Ã¤Â¸â€°Ã¥Â·Â´Ã§â€°Å’Ã¥ï¿½Å Ã¦ËœÂ¯Ã¦Â¾Â³Ã©â€“â‚¬Ã§Å¡â€žÃ¦Â¨â„¢Ã¨ÂªÅ’Ã¦â‚¬Â§Ã¥Â»ÂºÃ§Â¯â€°Ã§â€°Â©Ã¤Â¹â€¹Ã¤Â¸â‚¬Ã¯Â¼Å’Ã¥ï¿½Å’Ã¦â„¢â€šÃ¤Â¹Å¸Ã§â€šÂºÃ£â‚¬Å’Ã¦Â¾Â³Ã©â€“â‚¬Ã¥â€¦Â«Ã¦â„¢Â¯Ã£â‚¬ï¿½Ã¤Â¹â€¹Ã¤Â¸â‚¬Ã£â‚¬â€š2005Ã¥Â¹Â´Ã¨Ë†â€¡Ã¦Â¾Â³Ã©â€“â‚¬Ã¦Â­Â·Ã¥ï¿½Â²Ã¥Å¸Å½Ã¥ï¿½â‚¬Ã§Å¡â€žÃ¥â€¦Â¶Ã¤Â»â€“Ã¦â€“â€¡Ã§â€°Â©Ã¦Ë†ï¿½Ã§â€šÂºÃ¨ï¿½Â¯Ã¥ï¿½Ë†Ã¥Å“â€¹Ã¤Â¸â€“Ã§â€¢Å’Ã¦â€“â€¡Ã¥Å’â€“Ã©ï¿½ÂºÃ§â€�Â¢Ã£â‚¬â€š"
}
,
{
	"name":"Ã¦Â¾Â³Ã©â€”Â¨Ã¨â€°ÂºÃ¦Å“Â¯Ã¥ï¿½Å¡Ã§â€°Â©Ã©Â¦â€ ",
	"addresses": {
		"street":"Ã¥â€ Â¼Ã¦ËœÅ¸Ã¦ÂµÂ·Ã¥Â¤Â§Ã©Â¦Â¬Ã¨Â·Â¯",
		"district":"Ã¦â€“Â°Ã¥ï¿½Â£Ã¥Â²Â¸Ã¥Â¡Â«Ã¦ÂµÂ·Ã¥ï¿½â‚¬",
		"city":"Ã¦Â¾Â³Ã©â€”Â¨",
		"stateProvince":"Ã¦Â¾Â³Ã©â€”Â¨Ã§â€°Â¹Ã¥Ë†Â¥Ã¨Â¡Å’Ã¦â€�Â¿Ã¥ï¿½â‚¬",
		"country":"Ã¤Â¸Â­Ã¥â€ºÂ½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": 0,
		"adults": 0,
		"children": 0,
		"students": 0,
		"seniors": 0,
		"currency":"HKD"
	},
	"categories":{
		"level_1":"Religious Site",
		"level_2":"Taoism Temple"
	},
	"hours":{
		"monday" :"",
		"tuesday" :"",
		"wednesday" :"",
		"thursday" :"",
		"friday" :"",
		"saturday":"",
		"sunday":"",
		"holiday":""
	},
	"phones":{
		"General":"Ã¯Â¼Ë†853Ã¯Â¼â€° 933 339"
	},
	"url":"www.oceanpark.com.hk",
	"pictures":{
		"planning": "/assets/images/dummy/planning/img06.jpg",
		"result":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		],
		"thumbnail":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"
		],
		"hero":[
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg", 
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg",
			"/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"
		]
		
	},
	"fact":"Ã¦Â¾Â³Ã©â€”Â¨Ã¨â€°ÂºÃ¦Å“Â¯Ã¥ï¿½Å¡Ã§â€°Â©Ã©Â¦â€ Ã©Å¡Â¶Ã¥Â±Å¾Ã¦Â°â€˜Ã¦â€�Â¿Ã¦â‚¬Â»Ã§Â½Â²Ã¯Â¼Å’Ã¤Â¸ÂºÃ¦Â¾Â³Ã©â€”Â¨Ã¦â€“â€¡Ã¥Å’â€“Ã¤Â¸Â­Ã¥Â¿Æ’Ã¤Â¸â‚¬Ã¤Â¸ÂªÃ§Â»â€žÃ¦Ë†ï¿½Ã©Æ’Â¨Ã¥Ë†â€ Ã£â‚¬â€šÃ¦â‚¬Â»Ã©ï¿½Â¢Ã§Â§Â¯Ã¤Â¸â‚¬Ã¤Â¸â€¡Ã©â€ºÂ¶Ã¤Â¸â‚¬Ã§â„¢Â¾Ã¤Â¹ï¿½Ã¥ï¿½ï¿½Ã¤ÂºÅ’Ã¥Â¹Â³Ã¦â€“Â¹Ã§Â±Â³Ã¯Â¼Å’Ã¥Â±â€¢Ã¨Â§Ë†Ã©ï¿½Â¢Ã§Â§Â¯Ã¨Â¿â€˜Ã¥â€ºâ€ºÃ¥ï¿½Æ’Ã¥Â¹Â³Ã¦â€“Â¹Ã§Â±Â³Ã¯Â¼Å’Ã¦ËœÂ¯Ã¦Â¾Â³Ã©â€”Â¨Ã¨Â§â€žÃ¦Â¨Â¡Ã¦Å“â‚¬Ã¥Â¤Â§Ã§Å¡â€žÃ¦â€“â€¡Ã§â€°Â©Ã¨â€°ÂºÃ¦Å“Â¯Ã§Â±Â»Ã¥ï¿½Å¡Ã§â€°Â©Ã©Â¦â€ Ã£â‚¬â€šÃ¦Â¾Â³Ã©â€”Â¨Ã¦ËœÂ¯Ã¤Â¸â‚¬Ã¤Â¸ÂªÃ¤Â¸Â­Ã¨Â¥Â¿Ã¦â€“â€¡Ã¥Å’â€“Ã¤ÂºÂ¤Ã¦Âµï¿½Ã§Å¡â€žÃ¥Å½â€ Ã¥ï¿½Â²Ã¥ï¿½ï¿½Ã¥Å¸Å½Ã¯Â¼Å’Ã¨Å¾ï¿½Ã¥ï¿½Ë†Ã¤Â¸Å“Ã£â‚¬ï¿½Ã¨Â¥Â¿Ã¦â€“Â¹Ã¦â€“â€¡Ã¥Å’â€“Ã§â€°Â¹Ã¨â€°Â²Ã£â‚¬â€šÃ¤Â½Å“Ã¤Â¸ÂºÃ¨â€°ÂºÃ¦Å“Â¯Ã¥ï¿½Å¡Ã§â€°Â©Ã©Â¦â€ Ã¯Â¼Å’Ã¦Ë†â€˜Ã¤Â»Â¬Ã¦Æ’Â³Ã¥Â±â€¢Ã§Â¤ÂºÃ§Å¡â€žÃ¦Â­Â£Ã¦ËœÂ¯Ã¨Â¿â„¢Ã§Â§ï¿½Ã¦â€”Â¢Ã¦Å“â€°Ã¤Â¸Å“Ã¦â€“Â¹Ã¤Â¼Â Ã§Â»Å¸Ã¦â€“â€¡Ã¥Å’â€“Ã§â€°Â¹Ã§â€šÂ¹Ã¯Â¼Å’Ã¥ï¿½Ë†Ã¦Â¸â€”Ã©â‚¬ï¿½Ã¨Â¥Â¿Ã¦â€“Â¹Ã¦â€“â€¡Ã¦ËœÅ½Ã¨â€°Â²Ã¥Â½Â©Ã§Å¡â€žÃ¨â€°ÂºÃ¦Å“Â¯Ã©Â£Å½Ã¥â€˜Â³Ã£â‚¬â€šÃ©â‚¬Å¡Ã¨Â¿â€¡Ã¦Ë†â€˜Ã¤Â»Â¬Ã§Å¡â€žÃ¥Â±â€¢Ã¨Â§Ë†Ã¯Â¼Å’Ã¥Â¸Å’Ã¦Å“â€ºÃ¥ï¿½Â¯Ã¤Â»Â¥Ã¤Â»Â¤Ã¨Â§â€šÃ¤Â¼â€”Ã¦â€žÅ¸Ã¥ï¿½â€”Ã¥Ë†Â°Ã¨Â¿â„¢Ã©â€¡Å’Ã§â€¹Â¬Ã§â€°Â¹Ã§Å¡â€žÃ¦â€“â€¡Ã¥Å’â€“Ã¨â€°ÂºÃ¦Å“Â¯Ã¦Â°â€ºÃ¥â€ºÂ´ Ã£â‚¬â€š"
}
]           
      """

      
     /*val location=  com.mongodb.util.JSON.parse(json).asInstanceOf[BasicDBList];
        location.toArray.foreach(l=>ls.save(l.asInstanceOf[BasicDBObject]))*/
   // ls.save(location.get(0).asInstanceOf[BasicDBObject])
  //  val locations= "[%s]".format(ls.list.mkString(","))
    Ok(json).as("application/json")
  }
}