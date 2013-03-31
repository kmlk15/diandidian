package base

import services._
import services.MongodbServiceComponentImpl
import dao.LocationRepositoryComponent
import dao.UserRepositoryComponent

object locationRegistry extends LocationServiceComponentImpl with LocationRepositoryComponent

object mongoService extends MongodbServiceComponentImpl

object userRegistry extends UserServiceComponentImpl with UserRepositoryComponent



