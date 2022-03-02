package org.example

import org.example.customexceptions.UserErrorException
import org.example.service.{PersistenceService, RestService}
import org.springframework.http.ResponseEntity

import java.util.UUID
import scala.util.{Failure, Success, Try}

class ForComprehension(val persistenceService: PersistenceService, val restService: RestService) {

  def processResponseWithTries(id: UUID): Either[ResponseEntity[String], ResponseEntity[UUID]] = {

    val result: Try[UUID] = for {
      //TODO add a concurrent aspect to the service
      newId: UUID <- persistenceService.retrieveData(id) //TODO these services could be upgraded with a layer of complexity to test the framework more extensively
      anotherId <- restService.callService(newId)
    } yield anotherId
    matchResult(result)
  }

  def processResponseWithEithersAndMatch(id: UUID): Either[ResponseEntity[String], ResponseEntity[UUID]] = {
    val result: Try[UUID] = for {
      //TODO find a better way to properly flatmap the eithers
      //TODO use fold or flatmap
      newId: UUID <- persistenceService.persistData(id) match {
        case Right(uuid) => Success(uuid)
        case Left(message: String) => Failure(new RuntimeException(message))
      }
      anotherId <- restService.callOtherService(newId) match {
        case Right(uuid) => Success(uuid)
        case Left(userErrorException: UserErrorException) => Failure(userErrorException)
        case Left(exception: Exception) => Failure(exception)
      }
    } yield anotherId
    matchResult(result)
  }

  private def matchResult(result: Try[UUID]) = {
    result match {
      case Success(uuid) => Right(ResponseEntity.ok(uuid))
      case Failure(uee: UserErrorException) => Left(ResponseEntity.badRequest().body(s"user error: '${uee.getMessage}'"))
      case Failure(re: RuntimeException) => Left(ResponseEntity.internalServerError().body(re.getMessage))
      case Failure(e: Exception) => Left(ResponseEntity.internalServerError().body(e.getMessage))
    }
  }
}