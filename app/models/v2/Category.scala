package models.v2

 import play.api.libs.json._
 
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.data.validation.Constraints._

case class Category (id: String ="" ,  name: String = "" ,  enName: String = ""  , level: Int = 1,  parentId:String ="" ){

}

object CategoryHelp {
  
    implicit val categoryFmt = Json.format[Category]
  
  implicit val  form = Form (
  mapping(
       "id" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId")),
        "name" -> text.verifying(nonEmpty ),
        "enName" -> text.verifying(nonEmpty ),
        "level" ->number.verifying(min(0), max(100)) ,
        "parentId" -> optional(of[String] verifying pattern(
        """[a-fA-F0-9]{24}""".r,
        "constraint.objectId",
        "error.objectId"))
      ){ 
	  	(id , name , enName , level, parentId) =>  
        		Category( id = id.getOrElse("") , name=name ,  enName = enName , level = level ,parentId = parentId.getOrElse("") )
      } 
      {
          c => Some( ( Some( c.id) , c.name, c.enName, c.level , Some(c.parentId)  ) )
      }
        
  
  )
  
}