import com.google.inject.AbstractModule
import java.time.Clock

import daos.{EmailTokenDAO, UserDAO}
import daos.slick.{EmailTokenDAOImpl, UserDAOImpl}
import monix.execution.Scheduler
import services.helpers.{TimeHelper, TokenHelper}
import services.helpers.impl.{RealTimeHelper, TokenHelperImpl}
import services.impl.{AuthServiceImpl, EmailTokenServiceImpl, MailerServiceImpl, RecordServiceImpl, UserServiceImpl}
import services.{ApplicationTimer, AtomicCounter, AuthService, Counter, EmailTokenService, MailerService, RecordService, UserService}

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[Scheduler]).toInstance(Scheduler.Implicits.global)
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    bind(classOf[UserService]).to(classOf[UserServiceImpl])
    bind(classOf[MailerService]).to(classOf[MailerServiceImpl])
    bind(classOf[AuthService]).to(classOf[AuthServiceImpl])
    bind(classOf[EmailTokenService]).to(classOf[EmailTokenServiceImpl])
    bind(classOf[RecordService]).to(classOf[RecordServiceImpl])
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    bind(classOf[UserDAO]).to(classOf[UserDAOImpl])
    bind(classOf[EmailTokenDAO]).to(classOf[EmailTokenDAOImpl])

    // Set AtomicCounter as the implementation for Counter.
    bind(classOf[Counter]).to(classOf[AtomicCounter])
    bind(classOf[TimeHelper]).to(classOf[RealTimeHelper])
    bind(classOf[TokenHelper]).to(classOf[TokenHelperImpl])
  }

}
