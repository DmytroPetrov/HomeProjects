package services.helpers.impl

import java.util.UUID

import services.helpers.TokenHelper

class TokenHelperImpl extends TokenHelper {

  override def generateToken(): String = UUID.randomUUID().toString

}
