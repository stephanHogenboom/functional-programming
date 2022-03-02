package org.example.service

import java.util.UUID
import scala.util.Try

class RestService {

  def callOtherService(uuid: UUID): Either[Exception, UUID] = {
    Right(UUID.randomUUID())
  }

  def callService(uuid: UUID): Try[ UUID] = {
    Try(UUID.randomUUID())
  }
}
