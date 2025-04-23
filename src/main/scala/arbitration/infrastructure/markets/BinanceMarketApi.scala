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

final class BinanceMarketApi(backend: SttpBackend[Task, Any]) extends MarketApi {
  private final val baseUrl = "https://fapi.binance.com/fapi/v1"
  private final val timeout = 30.seconds

  override def getPrice(assetId: AssetId): ZIO[Any, MarketError, Price] = {
    val request = getResponse(assetId)

    for {
      response <- backend
        .send(request)
        .mapError(e => ServiceUnavailable(s"Request failed", e))

      price <- ZIO.fromEither(response.body).foldZIO(
        {
          case HttpError(apiError: BinanceErrorResponse, _) =>
            ZIO.logError(s"Binance API error for $assetId: ${apiError.code} - ${apiError.msg}") *>
              ZIO.fail(ApiError("Binance API error", apiError.code, apiError.msg))

          case decodeErr =>
            ZIO.logErrorCause(s"Binance API unexpected error format for $assetId", Cause.fail(decodeErr)) *>
              ZIO.fail(ApiError("Binance API unexpected error format", 500, decodeErr.getMessage))
        },
        binancePrice =>
          ZIO
            .attempt(binancePrice.price.toDouble)
            .tapBoth(
              e => ZIO.logErrorCause(s"Binance API failed to parse price for $assetId", Cause.fail(e)),
              value => ZIO.logDebug(s"Binance API successfully get price for $assetId")
            )
            .mapBoth(
              e => ApiError("Binance API invalid price format", 500, e.getMessage),
              value => Price(assetId, value, Instant.ofEpochMilli(binancePrice.time))
            )
      )
    } yield price
  }

  private def getResponse(assetId: AssetId) =
    basicRequest
      .get(uri"$baseUrl/ticker/price?symbol=${assetId.id}")
      .readTimeout(timeout)
      .response(asJsonEither[BinanceErrorResponse, BinancePriceResponse])
}
