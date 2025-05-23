package arbitration.domain

import java.sql.SQLException

sealed trait MarketError

object MarketError {
  case class NotFound(message: String) extends MarketError
  case class DatabaseError(message: String, ex: SQLException) extends MarketError
  case class CacheError(message: String, ex: Throwable) extends MarketError
  case class ApiError(provider: String, code: Int, message: String) extends MarketError
  case class ServiceUnavailable(service: String, ex: Throwable)extends MarketError
  case object Unknown extends MarketError
}
