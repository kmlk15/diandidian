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

  val ls = registry locationService

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
	"name":"ä¼šè®®å±•è§ˆä¸­å¿ƒ",
	"addresses": {
		"street":"å�šè§ˆé�“1å�·",
		"district":"æ¹¾ä»”",
		"city":"é¦™æ¸¯",
		"stateProvince":"é¦™æ¸¯ç‰¹åˆ¥è¡Œæ”¿å�€",
		"country":"ä¸­å›½",
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
	"fact":"é¦™æ¸¯æœƒè­°å±•è¦½ä¸­å¿ƒæ˜¯é¦™æ¸¯çš„ä¸»è¦�å¤§åž‹æœƒè­°å�Šå±•è¦½å ´åœ°ï¼Œä½�æ–¼é¦™æ¸¯å³¶ç�£ä»”åŒ—å²¸ï¼Œæ˜¯é¦™æ¸¯åœ°æ¨™ä¹‹ä¸€. 2012å¹´ï¼Œé¦™æ¸¯æœƒè­°å±•è¦½ä¸­å¿ƒåœ¨ CEI Asia magazineï¼Œç¬¬10åº¦æ¦®è†ºäºžæ´²æœ€ä½³æœƒè­°å±•è¦½ä¸­å¿ƒæ®Šæ¦®ã€‚å�Œå¹´ï¼Œé¦™æ¸¯æœƒè­°å±•è¦½ä¸­å¿ƒåœ¨ç¬¬23å±†TTGæ—…é�Šæ¥­å¤§ç�Žä¸­ï¼Œç¬¬4åº¦ç�²é�¸ç‚ºæœ€ä½³æœƒè­°å�Šå±•è¦½ä¸­å¿ƒæ®Šæ¦®ã€‚"
}
,
{
	"name":"æµ·æ´‹å…¬å›­",
	"addresses": {
		"street":"é»ƒç«¹å�‘é�“180è™Ÿ",
		"district":"å�—å�€",
		"city":"é¦™æ¸¯",
		"stateProvince":"é¦™æ¸¯ç‰¹åˆ¥è¡Œæ”¿å�€",
		"country":"ä¸­å›½",
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
	"fact":"é¦™æ¸¯æµ·æ´‹å…¬åœ’æ˜¯ä¸€å€‹ä¸–ç•Œç´šçš„æµ·æ´‹å‹•ç‰©ä¸»é¡Œæ¨‚åœ’ï¼Œé›†å¨›æ¨‚ã€�æ•™è‚²å�Šä¿�è‚²æ–¼ä¸€é«”ã€‚è‡ª1977å¹´é–‹å¹•ä»¥ä¾†ï¼Œé¦™æ¸¯æµ·æ´‹å…¬åœ’ä¸�æ–·æ›´æ–°è¨­æ–½ï¼Œå»£å�—é�Šå®¢æ­¡è¿Žï¼Œ2012å¹´æ›´å‹‡å¥ªæ¥­ç•Œæœ€é«˜æ®Šæ¦®çš„ã€Œå…¨ç�ƒæœ€ä½³ä¸»é¡Œå…¬åœ’ã€�å¤§ç�Ž(Applause Award)ï¼Œæ˜¯äºžæ´²ç¬¬ä¸€é–“ç�²å¾—é€™å€‹ç�Žé …çš„ä¸»é¡Œå…¬åœ’ï¼�"
}
,
{
	"name":"é¦™æ¸¯å²›ç”µè½¦",
	"addresses": {
		"street":"",
		"district":"é¦™æ¸¯å²›",
		"city":"é¦™æ¸¯",
		"stateProvince":"é¦™æ¸¯ç‰¹åˆ¥è¡Œæ”¿å�€",
		"country":"ä¸­å›½",
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
	"fact":"é¦™æ¸¯é›»è»Šæ˜¯é¦™æ¸¯çš„ä¸€å€‹è·¯é�¢é›»è»Šç³»çµ±ï¼Œä¾†å¾€é¦™æ¸¯å³¶å�€çš„ç­²ç®•ç�£å�Šå …å°¼åœ°åŸŽï¼Œå�¦æœ‰ç’°å½¢æ”¯ç·šä¾†å¾€è·‘é¦¬åœ°ï¼Œæ¯�æ—¥å¹³å�‡æŽ¥è¼‰22è�¬äººæ¬¡çš„ä¹˜å®¢ï¼Œæ˜¯å…¨ç�ƒç�¾å­˜å”¯ä¸€å…¨æ•¸æŽ¡ç”¨é›™å±¤é›»è»Šçš„é›»è»Šç³»çµ±ï¼ˆå�¦æœ‰è‹±åœ‹é»‘æ± é›»è»Šå�ŠåŸƒå�Šäºžæ­·å±±å¤§æ¸¯é›»è»Šéƒ¨åˆ†è·¯ç·šä½¿ç”¨é›™å±¤é›»è»Šï¼Œé�žå…¨æ•¸æŽ¡ç”¨ï¼‰ã€‚é¦™æ¸¯é›»è»Šåœ¨1904å¹´æŠ•å…¥æœ�å‹™ï¼Œæ˜¯é¦™æ¸¯æ­·å�²æœ€ç‚ºæ‚ ä¹…çš„äº¤é€šå·¥å…·ä¹‹ä¸€ã€‚é¦™æ¸¯é›»è»Šä¸�åƒ…æ˜¯æ¸¯å³¶å�€å±…æ°‘çš„é‡�è¦�äº¤é€šå·¥å…·ï¼Œä¹Ÿæˆ�ç‚ºå¤–åœ°æ—…å®¢è§€å…‰çš„è‘—å��æ™¯é»ž."
}
,
{
	"name":"é¦™æ¸¯äº¤æ˜“å»£å ´",
	"addresses": {
		"street":"åº·æ¨‚å»£å ´8è™Ÿ",
		"district":"ä¸­è¥¿å�€",
		"city":"é¦™æ¸¯",
		"stateProvince":"é¦™æ¸¯ç‰¹åˆ¥è¡Œæ”¿å�€",
		"country":"ä¸­å›½",
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
	"fact":"äº¤æ˜“å»£å ´æ˜¯é¦™æ¸¯çš„ä¸€çµ„è¾¦å…¬å®¤å¤§æ¨“ï¼Œä½�æ–¼é¦™æ¸¯å³¶ä¸­ç’°åº·æ¨‚å»£å ´8è™Ÿï¼Œé„°æŽ¥æ¸¯é�µé¦™æ¸¯ç«™ã€‚äº¤æ˜“å»£å ´å…±æœ‰3åº§è¾¦å…¬å¤§æ¨“ï¼Œåº•å±¤è¨­æœ‰å•†å ´ï¼Œåœ°ä¸‹è¨­æœ‰å·´å£«ç¸½ç«™ã€‚ä¸¦æœ‰å¤šæ¢�è¡Œäººå¤©æ©‹é€£æŽ¥åœ‹éš›é‡‘èž�ä¸­å¿ƒï¼Œæ€¡å’Œå¤§å»ˆå�Šç’°ç�ƒå¤§å»ˆã€‚åœ¨ç¬¬äºŒåº§æ­£é–€å™´æ°´æ± æ“ºæ”¾æœ‰å�°ç�£é›•å¡‘å¤§å¸«æœ±éŠ˜é‘„é€ çš„å¤ªæ¥µé›•å¡‘ï¼Œå�Šä¸€å°�æ°´ç‰›é›•å¡‘ã€‚åœ¨ç¬¬ä¸€åº§çš„æ­£é–€æ°´æ± ä¸­å¤®ï¼Œè£�æ”¾æœ‰ä¸€å€‹8å­—å½¢çš„é›•å¡‘ã€‚"
}
,
{
	"name":"æ¾³é—¨æ—…é�Šå¡”",
	"addresses": {
		"street":"è§€å…‰å¡”å‰�åœ°",
		"district":"å�—ç�£",
		"city":"æ¾³é—¨",
		"stateProvince":"æ¾³é—¨ç‰¹åˆ¥è¡Œæ”¿å�€",
		"country":"ä¸­å›½",
		"latitude": 100,
		"longitude": 100
	},
	"admissions": {
		"general": "58æ¨“è§€å…‰ä¸»å±¤ï¼š45å…ƒï¼Œ 61æ¨“è§€å…‰å±¤ï¼š30å…ƒï¼Œ 70å…ƒå¥—ç¥¨",
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
		"General":"ï¼ˆ853ï¼‰ 933 339"
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
	"fact":"ç„¡è«–æ˜¯åœ¨æ™¨å…‰ä¸‹é£½è¦½ç� æµ·å°�å²¸æ™¯ç·»ï¼Œå�ˆæˆ–æ˜¯æ–¼æ™šä¸Šåºœçž°ç’€ç’¨çš„é†‰äººç¾Žæ™¯ï¼Œæ¾³é–€æ—…é�Šå¡”æœƒå±•å¨›æ¨‚ä¸­å¿ƒå�‡æ˜¯æ‚¨çš„æœ€ä½³é�¸æ“‡ã€‚æ‚¨å�¯ä¹˜å��å­�å½ˆå�‡é™�æ©Ÿç”±ä¸‹è€Œä¸Šèµ°è¨ªå¡”å…§æ¯�å±¤çš„ç‰¹è‰²ï¼Œå…ˆåˆ°è§€å…‰å±¤æ¬£è³žå››å‘¨é¢¨æ™¯ï¼Œè·Ÿè‘—å“�åš�ä¸€ä¸‹360Â°æ—‹è½‰é¤�å»³çš„ç¾Žå‘³è‡ªåŠ©é¤�ï¼Œç„¶å¾Œå†�å››è™•èµ°èµ°ç›¡äº«æ‚ é–’ã€‚"
}
,
{
	"name":"å¤§ä¸‰å·´ç‰Œå�Š",
	"addresses": {
		"street":"å�Šå²›ç‚®å�°å±±ä¸‹åœ£ä¿�ç½—æ•™å ‚å‰�å£�é�—è¿¹",
		"district":"å¤§ä¸‰å·´",
		"city":"æ¾³é—¨",
		"stateProvince":"æ¾³é—¨ç‰¹åˆ¥è¡Œæ”¿å�€",
		"country":"ä¸­å›½",
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
		"General":"ï¼ˆ853ï¼‰ 933 339"
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
	"fact":"å¤§ä¸‰å·´ç‰Œå�Šï¼Œå…¶æ­£å¼�å��ç§°ä¸ºåœ£ä¿�ç¦„å¤§æ•™å ‚é�—å�€ï¼ˆè‘¡è�„ç‰™èªžï¼šRuÃ­nas da Antiga Catedral de SÃ£o Pauloï¼‰ï¼Œä¸€èˆ¬ç¨±ç‚ºå¤§ä¸‰å·´æˆ–ç‰Œå�Šï¼Œæ˜¯æ¾³é–€å¤©ä¸»ä¹‹æ¯�æ•™å ‚ï¼ˆè�–ä¿�ç¥¿æ•™å ‚ï¼‰æ­£é�¢å‰�å£�çš„é�ºå�€ã€‚å¤§ä¸‰å·´ç‰Œå�Šæ˜¯æ¾³é–€çš„æ¨™èªŒæ€§å»ºç¯‰ç‰©ä¹‹ä¸€ï¼Œå�Œæ™‚ä¹Ÿç‚ºã€Œæ¾³é–€å…«æ™¯ã€�ä¹‹ä¸€ã€‚2005å¹´èˆ‡æ¾³é–€æ­·å�²åŸŽå�€çš„å…¶ä»–æ–‡ç‰©æˆ�ç‚ºè�¯å�ˆåœ‹ä¸–ç•Œæ–‡åŒ–é�ºç”¢ã€‚"
}
,
{
	"name":"æ¾³é—¨è‰ºæœ¯å�šç‰©é¦†",
	"addresses": {
		"street":"å†¼æ˜Ÿæµ·å¤§é¦¬è·¯",
		"district":"æ–°å�£å²¸å¡«æµ·å�€",
		"city":"æ¾³é—¨",
		"stateProvince":"æ¾³é—¨ç‰¹åˆ¥è¡Œæ”¿å�€",
		"country":"ä¸­å›½",
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
		"General":"ï¼ˆ853ï¼‰ 933 339"
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
	"fact":"æ¾³é—¨è‰ºæœ¯å�šç‰©é¦†éš¶å±žæ°‘æ”¿æ€»ç½²ï¼Œä¸ºæ¾³é—¨æ–‡åŒ–ä¸­å¿ƒä¸€ä¸ªç»„æˆ�éƒ¨åˆ†ã€‚æ€»é�¢ç§¯ä¸€ä¸‡é›¶ä¸€ç™¾ä¹�å��äºŒå¹³æ–¹ç±³ï¼Œå±•è§ˆé�¢ç§¯è¿‘å››å�ƒå¹³æ–¹ç±³ï¼Œæ˜¯æ¾³é—¨è§„æ¨¡æœ€å¤§çš„æ–‡ç‰©è‰ºæœ¯ç±»å�šç‰©é¦†ã€‚æ¾³é—¨æ˜¯ä¸€ä¸ªä¸­è¥¿æ–‡åŒ–äº¤æµ�çš„åŽ†å�²å��åŸŽï¼Œèž�å�ˆä¸œã€�è¥¿æ–¹æ–‡åŒ–ç‰¹è‰²ã€‚ä½œä¸ºè‰ºæœ¯å�šç‰©é¦†ï¼Œæˆ‘ä»¬æƒ³å±•ç¤ºçš„æ­£æ˜¯è¿™ç§�æ—¢æœ‰ä¸œæ–¹ä¼ ç»Ÿæ–‡åŒ–ç‰¹ç‚¹ï¼Œå�ˆæ¸—é€�è¥¿æ–¹æ–‡æ˜Žè‰²å½©çš„è‰ºæœ¯é£Žå‘³ã€‚é€šè¿‡æˆ‘ä»¬çš„å±•è§ˆï¼Œå¸Œæœ›å�¯ä»¥ä»¤è§‚ä¼—æ„Ÿå�—åˆ°è¿™é‡Œç‹¬ç‰¹çš„æ–‡åŒ–è‰ºæœ¯æ°›å›´ ã€‚"
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