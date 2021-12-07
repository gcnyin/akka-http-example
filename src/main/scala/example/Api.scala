package example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import example.Endpoint._
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.Future

class Api(serviceLogic: ServiceLogic) {
  private val jsonRoute: Route =
    AkkaHttpServerInterpreter().toRoute(jsonApi.serverLogic(_ => serviceLogic.jsonLogic()))

  private val queriesRoute: Route =
    AkkaHttpServerInterpreter().toRoute(queriesApi.serverLogic(serviceLogic.queriesLogic))

  private val updatesRoute: Route =
    AkkaHttpServerInterpreter().toRoute(updatesApi.serverLogic(serviceLogic.updatesLogic))

  private val apiList: List[AnyEndpoint] = List(jsonApi, queriesApi, updatesApi)

  private val swaggerRoute: List[ServerEndpoint[Any, Future]] =
    SwaggerInterpreter().fromEndpoints[Future](apiList, "My App", "1.0")

  private val openApiRoute: Route = AkkaHttpServerInterpreter().toRoute(swaggerRoute)

  val route: Route = jsonRoute ~ queriesRoute ~ updatesRoute ~ openApiRoute
}
