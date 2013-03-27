package base

import services._
import services.MongodbServiceComponentImpl

object registry 
extends LocationServiceComponentImpl

object mongoService extends MongodbServiceComponentImpl

