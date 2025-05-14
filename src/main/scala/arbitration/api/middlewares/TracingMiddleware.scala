package arbitration.api.middlewares

import arbitration.layers.AppEnv
import io.opentelemetry.api.trace.SpanKind
import zio.ZIO
import zio.http.{Handler, Middleware, Request, Routes}
import zio.telemetry.opentelemetry.context.IncomingContextCarrier
import zio.telemetry.opentelemetry.tracing.propagation.TraceContextPropagator

object TracingMiddleware {
  def tracing: Middleware[AppEnv] =
    new Middleware[AppEnv] {
      override def apply[Env1 <: AppEnv, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
        routes.transform { handler =>
          Handler.scoped[Env1] {
            Handler.fromFunctionZIO[Request] { req =>
              for {
                tracing <- ZIO.serviceWith[AppEnv](_.marketTracing.tracing)

                headersMap = req.headers.toList
                  .map(h => h.headerName -> h.renderedValue)
                  .toMap
                
                response <- tracing.extractSpan(
                  propagator = TraceContextPropagator.default,
                  carrier = SimpleIncomingCarrier(headersMap),
                  spanName = s"${req.method.toString} ${req.url.path.toString}",
                  spanKind = SpanKind.SERVER
                ) {
                  handler.runZIO(req)
                }
              } yield response
            }
          }
        }
    }
}