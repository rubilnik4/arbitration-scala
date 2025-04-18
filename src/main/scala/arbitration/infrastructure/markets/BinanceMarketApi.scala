package arbitration.infrastructure.markets

import arbitration.application.AppEnv
import arbitration.domain.MarketError
import arbitration.domain.models.{AssetId, Price}
import zio.ZIO
import sttp.client3.*
import sttp.client3.asynchttpclient.zio.AsyncHttpClientZioBackend
import sttp.client3.ziojson.*
import zio.json.*

case class BinancePrice(symbol: String, price: String, time: Long)

object BinancePrice {
  implicit val decoder: JsonDecoder[BinancePrice] = DeriveJsonDecoder.gen[BinancePrice]
}

final class BinanceMarketApi extends MarketApi { 
  private val baseUrl = "https://dapi.binance.com/dapi/v1"

  // Модели для парсинга ответа
  private case class BinancePriceResponse(
                                           symbol: String,
                                           price: String,
                                           time: Long
                                         )

  private case class BinanceErrorResponse(
                                           code: Int,
                                           msg: String
                                         )

  def getPrice(assetId: AssetId): ZIO[AsyncHttpClientZioBackend with Logger, MarketError, Price] = {
    val program = for {
      _ <- ZIO.logDebug(s"Binance API request for asset: ${assetId.value}")

      response <- sendRequest(assetId)
        .tapError(e => ZIO.logWarning(s"Binance API error: ${e.getMessage}"))

      price <- parseResponse(assetId, response)
        .tap(price => ZIO.logDebug(s"Binance API response for ${assetId.value}: $price"))

    } yield price

    program.catchAll {
      case e: MarketError => ZIO.fail(e)
      case ex: Throwable =>
        ZIO.logErrorCause(s"Binance API request failed for ${assetId.value}", Cause.fail(ex)) *>
          ZIO.fail(ServiceUnavailable(s"Request failed for ${assetId.value}", ex))
    }
  }

  private def sendRequest(assetId: AssetId) = {
    val request = basicRequest
      .get(uri"$baseUrl/ticker/price?symbol=${assetId.value}")
      .readTimeout(5.seconds)

    AsyncHttpClientZioBackend().flatMap { backend =>
      backend.send(request)
        .mapError(ex => ServiceUnavailable("Network error", ex))
        .flatMap { response =>
          response.code match {
            case StatusCode.Ok =>
              ZIO.succeed(response.body)
            case _ =>
              response.body match {
                case Left(errorJson) =>
                  ZIO.fromEither(parseError(errorJson))
                    .mapBoth(
                      _ => ApiError(s"HTTP ${response.code}", Some(response.code.code)),
                      err => ApiError(err.msg, Some(err.code))
                    )
                case Right(_) =>
                  ZIO.fail(ApiError(s"HTTP ${response.code}", Some(response.code.code)))
              }
          }
        }
    }
  }

  private def parseError(json: String): Either[Throwable, BinanceErrorResponse] =
    zio.json.Decoder[BinanceErrorResponse]
      .decodeJson(json)
      .left
      .map(new RuntimeException(_))

  private def parseResponse(assetId: AssetId, body: String) = {
    for {
      binancePrice <- ZIO.from(zio.json.Decoder[BinancePriceResponse].decodeJson(body))
        .mapError(e => ApiError("Invalid response format", None, Some(e)))

      priceValue <- ZIO.attempt(binancePrice.price.toDouble)
        .mapError(e => ApiError("Invalid price format", None, Some(e)))

      timestamp = Instant.ofEpochMilli(binancePrice.time)
    } yield Price(assetId, priceValue, timestamp)
  }
}
