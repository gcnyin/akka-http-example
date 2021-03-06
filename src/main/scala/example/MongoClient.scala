package example

import akka.actor.typed.ActorSystem
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}

import scala.concurrent.{ExecutionContext, Future}

class MongoClient(implicit ec: ExecutionContext, system: ActorSystem[_]) {
  private val mongoUri: String = system.settings.config.getString("mongo.uri")
  private val dbName: String = system.settings.config.getString("mongo.database")
  private val driver: AsyncDriver = AsyncDriver()
  private val parsedUri: Future[ParsedURI] = MongoConnection.fromString(mongoUri)
  private val futureConnection: Future[MongoConnection] = parsedUri.flatMap(it => driver.connect(it))
  private val database: Future[DB] = futureConnection.flatMap(_.database(dbName))

  def getCollectionFuture(collection: String): Future[BSONCollection] = database.map(_.collection(collection))

  def worldCollectionFuture: Future[BSONCollection] = getCollectionFuture("world")

  def fortuneCollectionFuture: Future[BSONCollection] = getCollectionFuture("fortune")
}
