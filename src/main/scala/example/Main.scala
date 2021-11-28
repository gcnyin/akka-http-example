package example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


object Main {
  private val logger: Logger = Logger[Main.type]

  private val s =
    """     ___       __  ___  __  ___      ___          __    __  .___________..___________..______
      |    /   \     |  |/  / |  |/  /     /   \        |  |  |  | |           ||           ||   _  \
      |   /  ^  \    |  '  /  |  '  /     /  ^  \       |  |__|  | `---|  |----``---|  |----`|  |_)  |
      |  /  /_\  \   |    <   |    <     /  /_\  \      |   __   |     |  |         |  |     |   ___/
      | /  _____  \  |  .  \  |  .  \   /  _____  \     |  |  |  |     |  |         |  |     |  |
      |/__/     \__\ |__|\__\ |__|\__\ /__/     \__\    |__|  |__|     |__|         |__|     | _|      """.stripMargin

  s.split("\n").foreach(it => logger.info(it))

  def main(args: Array[String]): Unit = {
    ActorSystem(root(), "root")
  }

  def root(): Behavior[String] = Behaviors.setup[String] { ctx =>
    implicit val system: ActorSystem[Nothing] = ctx.system
    implicit val context: ExecutionContextExecutor = system.executionContext

    val mongoClient = new MongoClient
    val httpRoutes = new Routes(mongoClient)
    val port = System.getProperty("http.port", "8080").toInt

    Http()
      .newServerAt("0.0.0.0", port)
      .bindFlow(new Api(new ServiceLogic(mongoClient)).route)
      .onComplete {
        case Failure(exception) =>
          logger.error("error", exception)
        case Success(value) =>
          logger.info("{}", value)
      }

    Behaviors.empty
  }
}
