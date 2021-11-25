package example

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.StatusCodes.{InternalServerError, NotFound}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.typesafe.scalalogging.Logger
import example.Model._
import io.circe.generic.auto._
import io.circe.syntax._
import reactivemongo.api.bson.document

import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

class Routes(mongoClient: MongoClient)(implicit val system: ActorSystem[_]) {
  case class User(name: String)

  implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("myapp.routes.ask-timeout"))
  implicit val ec: ExecutionContextExecutor = system.executionContext

  private val logger: Logger = Logger[Routes]

  val routes: Route = {
    path("plaintext") {
      complete(HttpEntity(ContentTypes.`application/json`, """{"name": "Tom"}"""))
    } ~ path("json") {
      complete(HttpEntity(ContentTypes.`application/json`, User("Tom").asJson.spaces2))
    } ~ path("db") {
      onComplete(handleDb()) {
        case Failure(exception) =>
          logger.error("Failed to handle request", exception)
          complete(HttpEntity(ContentTypes.`application/json`, """{"msg": "error"}"""))
        case Success(v) =>
          v match {
            case Some(value) =>
              complete(HttpEntity(ContentTypes.`application/json`, value.asJson.noSpaces))
            case None =>
              complete(NotFound)
          }
      }
    } ~ path("queries") {
      parameter("queries".optional) { queries =>
        onComplete(handleQueries(queries)) {
          case Failure(exception) =>
            logger.error("Failed to handle request", exception)
            complete(InternalServerError)
          case Success(value) =>
            complete(HttpEntity(ContentTypes.`application/json`, value.asJson.noSpaces))
        }
      }
    }
  }

  def handleDb(): Future[Option[World]] = queryOneElement()

  def handleQueries(queries: Option[String]): Future[Seq[Option[World]]] = {
    val queriesParam: Int = queries
      .flatMap(it => Try(it.toInt).toOption)
      .map(_.max(1))
      .map(_.min(500))
      .getOrElse(1)
    Future.sequence((1 to queriesParam).map(_ => queryOneElement()))
  }

  def queryOneElement(): Future[Option[World]] =
    for {
      coll <- mongoClient.worldCollectionFuture
      r <- coll.find(document("id" -> randomWorld())).one[World]
    } yield r

  def randomWorld(): Int = 1 + ThreadLocalRandom.current.nextInt(10000)
}
