package base

import services._
import services.MongodbServiceComponentImpl

object locationRegistry extends LocationServiceComponentImpl 

object locationFormRegistry extends LocationFormServiceComponentImpl 

object mongoService extends MongodbServiceComponentImpl

object userRegistry extends UserServiceComponentImpl


object LoginServiceRegistry extends LoginServiceComponentImpl


object CmsServiceRegistry extends CmsServiceComponentImpl
