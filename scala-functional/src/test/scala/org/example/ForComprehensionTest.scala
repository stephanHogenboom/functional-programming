package org.example

import org.example.customexceptions.UserErrorException
import org.example.service.{PersistenceService, RestService}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.http.ResponseEntity

import java.util.UUID
import scala.util.{Failure, Success}


class ForComprehensionTest extends AnyFlatSpec with MockFactory with Matchers {
  private val restService: RestService = mock[RestService]
  private val persistenceService: PersistenceService = mock[PersistenceService]
  private val comprehension: ForComprehension = new ForComprehension(persistenceService, restService)

 "processWithTry" should "return a 200 ok when all services return success " in {
   val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
   (persistenceService.retrieveData _)expects(id) returning(Success(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
   (restService.callService _)expects(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")) returning(Success(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
   comprehension.processResponseWithTries(id) shouldBe Right(ResponseEntity.ok(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
 }

  it should "return a bad request when a services return Failure with userErrorException " in {
    val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
    (persistenceService.retrieveData _)expects(id) returning(Success(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
    (restService.callService _)expects(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")) returning(Failure(new UserErrorException(("non existing uuid: 715896a8-99a6-11ec-b909-0242ac120002"))))
    comprehension.processResponseWithTries(id) should matchPattern { case Left(r: ResponseEntity[String]) if r.getBody == "user error: 'non existing uuid: 715896a8-99a6-11ec-b909-0242ac120002'" =>}

    (ResponseEntity.badRequest().body("User error: 'non existing uuid: 715896a8-99a6-11ec-b909-0242ac120002'"))
  }

  it should "return a 500 when rest service returns an general failure" in {
    val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
    (persistenceService.retrieveData _)expects(id) returning(Success(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
    (restService.callService _)expects(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")) returning(Failure(new RuntimeException("connection refused")))
    comprehension.processResponseWithTries(id) shouldBe Left(ResponseEntity.internalServerError().body("connection refused"))
  }

  it should "return a 500 when persistence service returns a failure" in {
    val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
    (persistenceService.retrieveData _)expects(id) returning(Failure(new RuntimeException("database connection un available")))
    comprehension.processResponseWithTries(id) shouldBe Left(ResponseEntity.internalServerError().body("database connection un available"))
  }

  "processWithEithersUsingMach" should "return a Right either containing a 200 ok response" in {
    val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
    (persistenceService.persistData _)expects(id) returning(Right(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
    (restService.callOtherService _)expects(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")) returning(Right(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
    comprehension.processResponseWithEithersAndMatch(id) shouldBe Right(ResponseEntity.ok(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
  }

  it should "return a bad request when a services return Failure with userErrorException " in {
    val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
    (persistenceService.persistData _)expects(id) returning(Right(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
    (restService.callOtherService _)expects(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")) returning(Left(UserErrorException(("non existing uuid: 715896a8-99a6-11ec-b909-0242ac120002"))))
    comprehension.processResponseWithEithersAndMatch(id) should matchPattern { case Left(r: ResponseEntity[String]) if r.getBody == "user error: 'non existing uuid: 715896a8-99a6-11ec-b909-0242ac120002'" =>}

    (ResponseEntity.badRequest().body("User error: 'non existing uuid: 715896a8-99a6-11ec-b909-0242ac120002'"))
  }

  it should "return a 500 when rest service returns an general failure" in {
    val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
    (persistenceService.persistData _)expects(id) returning(Right(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")))
    (restService.callOtherService _)expects(UUID.fromString("715896a8-99a6-11ec-b909-0242ac120005")) returning(Left(new RuntimeException("connection refused")))
    comprehension.processResponseWithEithersAndMatch(id) shouldBe Left(ResponseEntity.internalServerError().body("connection refused"))
  }

  it should "return a 500 when persistence service returns a failure" in {
    val id = UUID.fromString("715896a8-99a6-11ec-b909-0242ac120002")
    (persistenceService.persistData _)expects(id) returning(Left("database connection un available"))
    comprehension.processResponseWithEithersAndMatch(id) shouldBe Left(ResponseEntity.internalServerError().body("database connection un available"))
  }
}
