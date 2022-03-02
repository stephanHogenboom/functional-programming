package org.example.customexceptions

final case class UserErrorException(private val message: String = "",
                                 private val cause: Throwable = None.orNull)
  extends Exception(message, cause)