package example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import example.Model.World
import io.circe.generic.auto._
import reactivemongo.api.bson.document
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.{ExecutionContextExecutor, Future}

class Api(mongoClient: MongoClient)(implicit val ec: ExecutionContextExecutor) {
  private val worldBody: EndpointIO.Body[String, World] =
    jsonBody[World].description("A world").example(World(123456, 654321))

  private val worldsBody: EndpointIO.Body[String, Seq[Option[World]]] =
    jsonBody[Seq[Option[World]]].description("The world").example(Seq(Some(World(123456, 654321))))

  private val basicEndpoint =
    endpoint
      .errorOut(stringBody)

  private val jsonApi: PublicEndpoint[Unit, String, World, Any] =
    basicEndpoint
      .description("Return a world entity")
      .get
      .in("json")
      .out(worldBody)

  private def jsonLogic(): Future[Either[String, World]] = Future.successful(Right(World(1234, 4321)))

  private val queriesApi: PublicEndpoint[Option[Int], String, Seq[Option[World]], Any] =
    basicEndpoint
      .description("Return a list of worlds")
      .get
      .in("queries")
      .in(query[Option[Int]]("queries"))
      .out(worldsBody)

  private def queriesLogic(queries: Option[Int]): Future[Either[String, Seq[Option[World]]]] =
    Future
      .sequence((1 to getQueriesValue(queries)).map(_ => queryOneElement()))
      .map(Right(_))

  private val updatesApi: PublicEndpoint[Option[Int], String, Seq[Option[World]], Any] =
    basicEndpoint
      .description("Update and return a list of worlds")
      .get
      .in("updates")
      .in(query[Option[Int]]("queries"))
      .out(worldsBody)

  private def updatesLogic(queries: Option[Int]): Future[Either[String, Seq[Option[World]]]] =
    Future
      .sequence((1 to getQueriesValue(queries)).map(_ => getAndUpdateWorld(randomNumber(), randomNumber())))
      .map(Right(_))

  private def getAndUpdateWorld(id: Int, randomNumber: Int): Future[Option[World]] =
    for {
      coll <- mongoClient.worldCollectionFuture
      res <- coll.findAndUpdate(
        document("id" -> id),
        document("$set" -> document("randomNumber" -> randomNumber)),
        fetchNewObject = true)
    } yield res.result[World]

  private def getQueriesValue(queries: Option[Int]): Int =
    queries
      .map(_.max(1))
      .map(_.min(500))
      .getOrElse(1)

  private def queryOneElement(): Future[Option[World]] =
    for {
      coll <- mongoClient.worldCollectionFuture
      r <- coll.find(document("id" -> randomNumber())).one[World]
    } yield r

  private def randomNumber(): Int =
    1 + ThreadLocalRandom.current.nextInt(10000)

  private val jsonRoute: Route = AkkaHttpServerInterpreter().toRoute(jsonApi.serverLogic(_ => jsonLogic()))

  private val queriesRoute: Route = AkkaHttpServerInterpreter().toRoute(queriesApi.serverLogic(queriesLogic))

  private val updatesRoute: Route = AkkaHttpServerInterpreter().toRoute(updatesApi.serverLogic(updatesLogic))

  private val apiList: List[AnyEndpoint] = List(jsonApi, queriesApi, updatesApi)

  private val value: List[ServerEndpoint[Any, Future]] =
    SwaggerInterpreter().fromEndpoints[Future](apiList, "My App", "1.0")

  private val openApiRoute: Route = AkkaHttpServerInterpreter().toRoute(value)

  val route: Route = jsonRoute ~ queriesRoute ~ updatesRoute ~ openApiRoute
}
