package org.example.service

import java.util.UUID
import scala.util.Try

class PersistenceService{

  def persistData(uuid: UUID) : Either[String, UUID] = {
   Right(UUID.randomUUID())
  }

  def retrieveData(uuid: UUID) : Try[UUID] = {
    Try(UUID.randomUUID())
  }
}
