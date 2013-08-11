package controllers.cms

import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.ClientConfiguration
import java.io.File
import org.slf4j.LoggerFactory

class AwsS3Client( pathprefix: String=""  , useProxy: Boolean = false ) {
 val log = LoggerFactory.getLogger(classOf[AwsS3Client])
  val cred = new BasicAWSCredentials("AKIAIISCPZLWZ4CCRD3A", "4pEN9ikkShjm/h1ST7fqYRjneaqvJdaiWBcwJKl1")

  val config = new ClientConfiguration()
 if( useProxy ){
  config.setProxyHost("127.0.0.1")
  config.setProxyPort(3128)
 }
  val bucket = "diandidian"

  val client = new AmazonS3Client(cred, config)

   
  def upload(filename: String) = {

    val file = new File(pathprefix +  filename  )
    if(file.exists() ){
       log.debug("upload filename={}, filesize={}" , filename , file.length() )
	    val putobj = new PutObjectRequest(bucket, filename, file)
	    putobj.withCannedAcl(CannedAccessControlList.PublicRead)
	    val res = client.putObject(putobj)
	    log.debug("upload over")
	    res
    }
  }

  
  def remove( filename: String ) ={
     log.debug("remove filename={},  " , filename   )
    client.deleteObject( bucket  , filename)
    log.debug("remove over")
  }

}