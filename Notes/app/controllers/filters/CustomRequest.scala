package controllers.filters

import models.User
import play.api.libs.Files
import play.api.mvc.{AnyContent, MultipartFormData, Request, WrappedRequest}

case class CustomRequest[T](userOption: Option[User],
                            bodyOption: Option[T],
                            request: Request[AnyContent]) extends WrappedRequest (request){
  lazy val parsedBody: T = bodyOption.get
  lazy val user: User = userOption.get

  def withParsedBody[K](body: K): CustomRequest[K] = CustomRequest(
    userOption,
    Some(body),
    request
  )

}

object CustomRequest {
  val CookieUserId = "userId"
  type MultipartFile = MultipartFormData.FilePart[Files.TemporaryFile]
}
