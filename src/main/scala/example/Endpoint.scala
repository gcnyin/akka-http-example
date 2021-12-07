package example

import example.Model.World
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.PartialServerEndpoint

import scala.concurrent.Future

object Endpoint {
  private val worldBody: EndpointIO.Body[String, World] =
    jsonBody[World].description("A world").example(World(123456, 654321))

  private val worldsBody: EndpointIO.Body[String, Seq[Option[World]]] =
    jsonBody[Seq[Option[World]]].description("The world").example(Seq(Some(World(123456, 654321))))

  private val queries: EndpointInput.Query[Option[Int]] =
    query[Option[Int]]("queries").example(Some(10))

  private val basicEndpoint: Endpoint[Unit, Unit, String, Unit, Any] =
    endpoint
      .errorOut(stringBody)

  val jsonApi: PublicEndpoint[Unit, String, World, Any] =
    basicEndpoint
      .description("Return a world entity")
      .get
      .in("json")
      .out(worldBody)

  val queriesApi: PublicEndpoint[Option[Int], String, Seq[Option[World]], Any] =
    basicEndpoint
      .description("Return a list of worlds")
      .get
      .in("queries")
      .in(queries)
      .out(worldsBody)

  val updatesApi: PublicEndpoint[Option[Int], String, Seq[World], Any] =
    basicEndpoint
      .description("Update and return a list of worlds")
      .get
      .in("updates")
      .in(queries)
      .out(jsonBody[Seq[World]])
}
