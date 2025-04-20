package arbitration.infrastructure.markets

import arbitration.application.dto.{BinanceErrorResponse, BinancePriceResponse}
import arbitration.domain.MarketError
import arbitration.domain.MarketError.{ApiError, ServiceUnavailable}
import arbitration.domain.models.{AssetId, Price}
import sttp.client3.*
import sttp.client3.ziojson.asJsonEither
import zio.{Cause, Task, ZIO}

import java.time.Instant
import scala.concurrent.duration.DurationInt

case class BinancePrice(symbol: String, price: String, time: Long)

final class BinanceMarketApi(backend: SttpBackend[Task, Any]) extends MarketApi {
  private val baseUrl = "https://dapi.binance.com/dapi/v1"
  private val timeout = 30.seconds

  override def getPrice(assetId: AssetId): ZIO[Any, MarketError, Price] = {
    val request = basicRequest
      .get(uri"$baseUrl/ticker/price?symbol=${assetId.id}")
      .readTimeout(timeout)
      .response(asJsonEither[BinanceErrorResponse, BinancePriceResponse])

    for {
      _ <- ZIO.logDebug(s"Binance API request for asset: $assetId")

      response <- backend
        .send(request)
        .mapError(e => ServiceUnavailable(s"Request failed", e))

      price <- response.body match {
        case Right(binancePrice) =>
          for {
            value <- ZIO.attempt(binancePrice.price.toDouble)
              .mapError(e => ApiError("Binance API invalid price format", 500, e.getMessage))

            time = Instant.ofEpochMilli(binancePrice.time)
          } yield Price(assetId, value, time)

        case Left(HttpError(apiError: BinanceErrorResponse, _)) =>
          for {
            _ <- ZIO.logError(s"Binance API error for $assetId: ${apiError.code} - ${apiError.msg}")
            err <- ZIO.fail(ApiError(s"Binance API error", apiError.code, apiError.msg))
          } yield err

        case Left(decodeErr) =>
          for {
            _ <- ZIO.logErrorCause(s"[Binance] Unexpected error format for $assetId", Cause.fail(decodeErr))
            err <- ZIO.fail(ApiError("Binance API unexpected error format", 500, decodeErr.getMessage))
          } yield err
      }
    } yield price
  }
}
