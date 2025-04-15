package arbitration.domain

sealed trait MarketError

object MarketError {
  case class NotFound(message: String) extends MarketError
  case class DatabaseError(message: String, ex: Exception) extends MarketError
  case class CacheError(message: String, ex: Exception) extends MarketError
  case class ApiError(provider: String, code: Int, message: String) extends MarketError
  case class ServiceUnavailable(service: String, ex: Exception) extends MarketError
  case object Unknown extends MarketError
}
