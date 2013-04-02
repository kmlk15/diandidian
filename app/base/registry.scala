package base

import services._
import services.MongodbServiceComponentImpl

object locationRegistry extends LocationServiceComponentImpl 

object mongoService extends MongodbServiceComponentImpl

object userRegistry extends UserServiceComponentImpl



