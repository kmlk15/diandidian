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
import org.slf4j.LoggerFactory
import models.LocationJsonHelp.locationFmt
import models.LocationFormHelp.locationFormFmt
import models.PhotoHelp.photoFmt
import play.api.libs.json.Json
import org.apache.commons.lang3.StringUtils
import scala.util.Random

object LocationEndpoints extends Controller {
	val log = LoggerFactory.getLogger(LocationEndpoints.getClass())
     val ls = locationFormRegistry.locationService
     val cmsService = CmsServiceRegistry.cmsService
     val  bagService = BagServiceRegistry.bagService
     val loginService = base.LoginServiceRegistry.loginService
     
     val  randomList = ( 0 to 24 ).map( i => Random.shuffle( ( 0 to 100).toList ) )
     val cookiename = "home_r_index" 
      val defaultIndex = -1 
   // val ls = locationRegistry.locationService
  /*
   * Test of converting json string into object and back to json string by using sjson
   */
  def test = Action {
    val js = """{"planning":"1234567", "result":["234456"], "thumbnail":[], "hero":[]}"""
    val jv = JsValue.fromString(js)
    val ph = fromjson[Pictures](jv)
    Ok(JsValue.toJson(tojson(ph)))
  }
	
/**
 *  根据 不同的搜索条件 返回 相应的结果
 *  
 *  2013-11-12 增加 只有 country 的情况
 *  通过 country 得到  cityList  ； 再通过 cityList 进行实际查？
 */
  def location=Action{ implicit request =>
    val country =  request.getQueryString("country").getOrElse("")
    val province=  request.getQueryString("province").getOrElse("")
    val city = request.getQueryString("city").getOrElse("")
    val district = request.getQueryString("district").getOrElse("")
    val ids = request.getQueryString("ids").getOrElse("")
    val cityList =  request.getQueryString("cityList").getOrElse("") 
    
    val (locationsList, randomIndex): (List[LocationForm] ,Int) = if (city != "" && district != "") {
      //按地区搜索
     ( ls.list(city, district) ,defaultIndex)
    } else if (city != "") {
      //按城市搜索
      ( ls.list(city) , defaultIndex)
    }else if( province != "" ){
      ( ls.listByProvince(province) , defaultIndex)
    }else if( country !="" ){
       ( ls.listByCountry(country) , defaultIndex)
    } else if( ids != "" ){
      //搜索框 提交的搜索
      log.debug( "ids={}", ids )
      val idsArr = ids.split(",")
      val list = idsArr.toSet.flatMap( ( id: String ) => { 
        if( id.startsWith("city:")){
          id.split(":") match{
            case  Array(_,_,city) =>{
              ls.list( city)
            }
            case  _ => Nil
          }
        }else{
        		ls.getById( id)
        }
      }).toList
      (list , defaultIndex )
    }else if(cityList != ""){
      //同时搜索多个城市， 用于 planning page   继续搜索 按钮
      val list = cityList.split(",").toList.flatMap(  city => ls.list(city))
      (list , defaultIndex )
    } else {
      //返回 所有的 地点 
      /**
       * 2014-01-03 增加过滤条件， 
       * 过滤掉 不需要显示在首页的 景点
       */
     val locationlist  =  ls.list().filterNot( _.notDisplayAtHomePage == Some("true"))
      
      /**
       * For the home page, right now the attractions loaded is in the sequence how I added it, example:

香港会议展览中心, 香港公园, 香港科学馆, 中环广场, 圣保罗大教堂遗址-大三巴牌坊, 金门大桥, 渔人码头

Please make it random loaded whenever it is the first time load to browser, example:

香港科学馆, 圣保罗大教堂遗址-大三巴牌坊, 金门大桥, 香港会议展览中心, 香港公园,  渔人码头, 中环广场
       * 
       * 有预先定义的 24个 整数 随机 序列，
       * 每日 更新一次？
       * 或者每次重启的时候更新？
       * 或者 类似 cache 的方式？
       * Map( id ,   IntList)
       * 在 cookie 中保存， 用的是那种 序列
       * 
       *  
       */
      val index = request.cookies.get( cookiename) match{
       case None =>  Random.nextInt(24)
       case Some( x ) =>
         log.debug("cookie={}"+ x  )
         math.abs( ( x.value.toInt) % 24)
     }
     
     val random = randomList( index )
     val rList = (locationlist zip random).sortBy( x=> x._2).map( x=> x._1 )
     (rList , index)
    }
    
    val locations = locationsList map {location =>
     val photo =  if( location.photo.startsWith("266_")){
    	  val imgId = StringUtils.substringBetween( location.photo , "266_","." )
    	  log.debug("imgId={}" , imgId )
    	  cmsService.getPhotoByImgId(imgId) match{
    	    case None => Photo()
    	    case Some( photo ) => photo
    	  }
      }else{
        Photo()
      }
     
      
     
     Json.obj( "photo" -> Json.toJson( photo )) ++ Json.obj("location" -> Json.toJson( location ) )  
    }
    
    //提取 结果中的  地点数据  国家和城市， 用于 在 显示在 导航条上
    //如果是 多个国家， 就不显示了， 只显示 一个国家 一个或多个城市的情况
//   val countryCityMap :Map[String ,String] =  locationsList.map( location => location.address).map(  address => (address.country, address.city) ).
//    distinct.groupBy( kv=> kv._1).
//    map( kv => kv._1 -> kv._2.map( x => x._2).mkString(",") )
//    
//     log.debug("countryCityMap{}", countryCityMap) 
//     
//   val(resultcountry ,  resultcity ) =  if( countryCityMap.size == 1 ){
//       val country= countryCityMap.keySet.head
//       val city = countryCityMap.values.head
//       ( country , city)
//    }else if (countryCityMap.size == 0){
//     
//      (country,city)
//      
//    }else{
//      ( "" , "" )
//    }
    //根据 Edit-10-9-13-REVISED  修改
    val resultprovince : String = if(province!="") province else {
      if( city ==""  || city=="香港" || city =="澳门" ){ 
    	  ""
      }else{  
        locationsList.headOption.map( location=> location.address.stateProvince).getOrElse("")
        }
     }
    val resultcountry : String=  if(  country!="") country else{ 
      if( resultprovince !=""){
         locationsList.headOption.map( location=> location.address.country).getOrElse("") 
      }else{
	      if( city!="" ) {
	        locationsList.headOption.map( location=> location.address.country).getOrElse("") 
	      }else{""}
	      }
    }
    val( resultcity ) =  (city)
   
   log.debug("(resultcountry ,  resultcity )={}", (resultcountry ,  resultcity ) ) 
   
    val data = Json.obj( "country" ->resultcountry.trim( ), "province" -> resultprovince.trim( ), "city" -> resultcity.trim( ) , 
        "list" -> Json.toJson( locations) )
        
    val resultJson = Json.obj("success" -> true , "data" -> data )
    log.debug( "randomIndex={}" , randomIndex  )
    if( randomIndex > defaultIndex){
       Ok(  resultJson ) .withCookies( Cookie(cookiename,randomIndex.toString) )
    }else{
       Ok(  resultJson )  
    }
   
    
  }
  
  /**
   * 为搜索框 提供选项数据
   * 2013-09-19 
   * 如何提供 城市数据？
   * 城市没有独立的表
   * 城市 只有中文名称
   * 从 搜索的结果中 提取 地点？
   */
  def search = Action{ implicit request =>
     import play.api.libs.json.Json
    val q= request.getQueryString("q").getOrElse("").trim()
    if( q==""){
      val jsVal =  Json.arr( )
      Ok(jsVal)
    }else{
      /**
       * 这里要实现 具体的 搜索逻辑 
       */
      log.debug("search q='{}'",q)
      val locationList =  ls.search( q )
      val cityList :List[( String , String)] = locationList.map( location => location.address).map( address => (address.country , address.city)).distinct
      val cityIdList = cityList.map( kv => Json.obj("id"->  ("city:"+kv._1 +":"  + kv._2) , "name" -> ("城市:"+ kv._2) ) )
      val idnameList = locationList.map( location => Json.obj("id" -> location.id , "name" -> location.name))
      log.debug( Json.prettyPrint(  Json.toJson( idnameList )) )
      Ok( Json.toJson(  cityIdList ++ idnameList ))
    }
  }
  
  def example = Action {
    
    val json = """
[
{"name":"会议展览中心","address":{"street":"博览道1号","district":"湾仔","city":"香港","stateProvince":"香港特別行政區","country":"中国","latitude":100,"longitude":100,"postalCode":""},"admission":{"general":0.0,"adults":0.0,"children":0.0,"student":0.0,"seniors":0.0,"currency":"HKD"},"category":{"level_1":"Park","level_2":"Garden Park"},"hours":{"monday":"","tuesday":"","wednesday":"","thursday":"","friday":"","saturday":"","sunday":"","holiday":""},"phone":{"general":"(852) 25374591","fax":""},"url":"","pictures":{"planning":"/assets/images/dummy/planning/img01.jpg","result":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_4.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_5.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_6.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_7.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_8.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_9.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_10.jpg"],"thumbnail":["/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_4.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_5.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_6.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_7.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_8.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_9.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_10.jpg"],"hero":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_4.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_5.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_6.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_7.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_8.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_9.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_10.jpg"]},"fact":"香港會議展覽中心是香港的主要大型會議及展覽場地，位於香港島灣仔北岸，是香港地標之一. 2012年，香港會議展覽中心在 CEI Asia magazine，第10度榮膺亞洲最佳會議展覽中心殊榮。同年，香港會議展覽中心在第23屆TTG旅遊業大獎中，第4度獲選為最佳會議及展覽中心殊榮。"},
{"name":"海洋公园","address":{"street":"黃竹坑道180號","district":"南區","city":"香港","stateProvince":"香港特別行政區","country":"中国","latitude":100,"longitude":100,"postalCode":""},"admission":{"general":120.0,"adults":120.0,"children":60.0,"student":90.0,"seniors":60.0,"currency":"HKD"},"category":{"level_1":"Park","level_2":"Public Park"},"hours":{"monday":"10am - 7pm","tuesday":"10am - 7pm","wednesday":"10am - 7pm","thursday":"10am - 7pm","friday":"10am - 7pm","saturday":"10am - 7pm","sunday":"10am - 7pm","holiday":"10am - 7pm"},"phone":{"general":"(852) 39232323","fax":""},"url":"","pictures":{"planning":"/assets/images/dummy/planning/img02.jpg","result":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"],"thumbnail":["/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"],"hero":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"]},"fact":"香港海洋公園是一個世界級的海洋動物主題樂園，集娛樂、教育及保育於一體。自1977年開幕以來，香港海洋公園不斷更新設施，廣受遊客歡迎，2012年更勇奪業界最高殊榮的「全球最佳主題公園」大獎(Applause Award)，是亞洲第一間獲得這個獎項的主題公園！"},
{"name":"香港岛电车","address":{"street":"","district":"香港岛","city":"香港","stateProvince":"香港特別行政區","country":"中国","latitude":100,"longitude":100,"postalCode":""},"admission":{"general":0.0,"adults":0.0,"children":0.0,"student":0.0,"seniors":0.0,"currency":"HKD"},"category":{"level_1":"Park","level_2":"Theme Park"},"hours":{"monday":"","tuesday":"","wednesday":"","thursday":"","friday":"","saturday":"","sunday":"","holiday":""},"phone":{"general":"(852)3923 2888","fax":""},"url":"www.oceanpark.com.hk","pictures":{"planning":"/assets/images/dummy/planning/img05.jpg","result":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"],"thumbnail":["/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"],"hero":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"]},"fact":"香港電車是香港的一個路面電車系統，來往香港島區的筲箕灣及堅尼地城，另有環形支線來往跑馬地，每日平均接載22萬人次的乘客，是全球現存唯一全數採用雙層電車的電車系統（另有英國黑池電車及埃及亞歷山大港電車部分路線使用雙層電車，非全數採用）。香港電車在1904年投入服務，是香港歷史最為悠久的交通工具之一。香港電車不僅是港島區居民的重要交通工具，也成為外地旅客觀光的著名景點."},
{"name":"香港交易廣場","address":{"street":"康樂廣場8號","district":"中西區","city":"香港","stateProvince":"香港特別行政區","country":"中国","latitude":100,"longitude":100,"postalCode":""},"admission":{"general":0.0,"adults":0.0,"children":0.0,"student":0.0,"seniors":0.0,"currency":"HKD"},"category":{"level_1":"Architecture","level_2":"Skyscraper"},"hours":{"monday":"","tuesday":"","wednesday":"","thursday":"","friday":"","saturday":"","sunday":"","holiday":""},"phone":{"general":"(852)2748 8080","fax":""},"url":"www.oceanpark.com.hk","pictures":{"planning":"/assets/images/dummy/planning/img07.jpg","result":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"],"thumbnail":["/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"],"hero":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"]},"fact":"交易廣場是香港的一組辦公室大樓，位於香港島中環康樂廣場8號，鄰接港鐵香港站。交易廣場共有3座辦公大樓，底層設有商場，地下設有巴士總站。並有多條行人天橋連接國際金融中心，怡和大廈及環球大廈。在第二座正門噴水池擺放有台灣雕塑大師朱銘鑄造的太極雕塑，及一對水牛雕塑。在第一座的正門水池中央，裝放有一個8字形的雕塑。"},
{"name":"澳门旅遊塔","address":{"street":"觀光塔前地","district":"南灣","city":"澳门","stateProvince":"澳门特別行政區","country":"中国","latitude":100,"longitude":100,"postalCode":""},"admission":{"general":58.0,"adults":0.0,"children":0.0,"student":0.0,"seniors":0.0,"currency":"HKD"},"category":{"level_1":"Religious Site","level_2":"Taoism Temple"},"hours":{"monday":"","tuesday":"","wednesday":"","thursday":"","friday":"","saturday":"","sunday":"","holiday":""},"phone":{"general":"（853） 933 339","fax":""},"url":"www.oceanpark.com.hk","pictures":{"planning":"/assets/images/dummy/planning/img03.jpg","result":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"],"thumbnail":["/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"],"hero":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"]},"fact":"無論是在晨光下飽覽珠海對岸景緻，又或是於晚上府瞰璀璨的醉人美景，澳門旅遊塔會展娛樂中心均是您的最佳選擇。您可乘坐子彈升降機由下而上走訪塔內每層的特色，先到觀光層欣賞四周風景，跟著品嚐一下360°旋轉餐廳的美味自助餐，然後再四處走走盡享悠閒。"},
{"name":"大三巴牌坊","address":{"street":"半岛炮台山下圣保罗教堂前壁遗迹","district":"大三巴","city":"澳门","stateProvince":"澳门特別行政區","country":"中国","latitude":100,"longitude":100,"postalCode":""},"admission":{"general":0.0,"adults":0.0,"children":0.0,"student":0.0,"seniors":0.0,"currency":"HKD"},"category":{"level_1":"Religious Site","level_2":"Taoism Temple"},"hours":{"monday":"","tuesday":"","wednesday":"","thursday":"","friday":"","saturday":"","sunday":"","holiday":""},"phone":{"general":"（853） 933 339","fax":""},"url":"www.oceanpark.com.hk","pictures":{"planning":"","result":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"],"thumbnail":["/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"],"hero":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"]},"fact":"大三巴牌坊，其正式名称为圣保禄大教堂遗址（葡萄牙語：Ruínas da Antiga Catedral de São Paulo），一般稱為大三巴或牌坊，是澳門天主之母教堂（聖保祿教堂）正面前壁的遺址。大三巴牌坊是澳門的標誌性建築物之一，同時也為「澳門八景」之一。2005年與澳門歷史城區的其他文物成為聯合國世界文化遺產。"},
{"name":"澳门艺术博物馆","address":{"street":"冼星海大馬路","district":"新口岸填海區","city":"澳门","stateProvince":"澳门特別行政區","country":"中国","latitude":100,"longitude":100,"postalCode":""},"admission":{"general":0.0,"adults":0.0,"children":0.0,"student":0.0,"seniors":0.0,"currency":"HKD"},"category":{"level_1":"Religious Site","level_2":"Taoism Temple"},"hours":{"monday":"","tuesday":"","wednesday":"","thursday":"","friday":"","saturday":"","sunday":"","holiday":""},"phone":{"general":"（853） 933 339","fax":""},"url":"www.oceanpark.com.hk","pictures":{"planning":"/assets/images/dummy/planning/img06.jpg","result":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"],"thumbnail":["/assets/images/china/sars/hong_kong/southern/ocean_park/thb_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/thb_3.jpg"],"hero":["/assets/images/china/sars/hong_kong/southern/ocean_park/result_1.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_2.jpg","/assets/images/china/sars/hong_kong/southern/ocean_park/result_3.jpg"]},"fact":"澳门艺术博物馆隶属民政总署，为澳门文化中心一个组成部分。总面积一万零一百九十二平方米，展览面积近四千平方米，是澳门规模最大的文物艺术类博物馆。澳门是一个中西文化交流的历史名城，融合东、西方文化特色。作为艺术博物馆，我们想展示的正是这种既有东方传统文化特点，又渗透西方文明色彩的艺术风味。通过我们的展览，希望可以令观众感受到这里独特的文化艺术氛围 。"}
]           
      """
     
    /* val location=  com.mongodb.util.JSON.parse(json).asInstanceOf[BasicDBList];
        location.toArray.foreach(l=>ls.save(l.asInstanceOf[BasicDBObject]))*/
   // ls.save(location.get(0).asInstanceOf[BasicDBObject])
  //  val locations= "[%s]".format(ls.list.mkString(","))
    Ok(json).as("application/json")
  }
}