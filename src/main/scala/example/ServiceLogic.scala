package example

import example.Model.World
import reactivemongo.api.bson.document

import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.{ExecutionContextExecutor, Future}

class ServiceLogic(mongoClient: MongoClient)(implicit val ec: ExecutionContextExecutor) {
  def jsonLogic(): Future[Either[String, World]] = Future.successful(Right(World(1234, 4321)))

  def queriesLogic(queries: Option[Int]): Future[Either[String, Seq[Option[World]]]] =
    Future
      .sequence((1 to getQueriesValue(queries)).map(_ => queryOneElement()))
      .map(Right(_))

  def updatesLogic(queries: Option[Int]): Future[Either[String, Seq[World]]] =
    Future
      .sequence((1 to getQueriesValue(queries)).map(_ => getAndUpdateWorld(randomNumber(), randomNumber())))
      .map(Right(_))

  private def getAndUpdateWorld(id: Int, randomNumber: Int): Future[World] =
    for {
      op <- findOne(id)
      res <- op.fold(Future.successful(World(-1, -1)))(_ => updateOne(id, randomNumber))
    } yield res

  private def updateOne(id: Int, randomNumber: Int): Future[World] =
    for {
      coll <- mongoClient.worldCollectionFuture
      _ <- coll
        .update(ordered = false)
        .element(q = document("id" -> id), u = document("randomNumber" -> randomNumber), upsert = false, multi = false)
    } yield World(id, randomNumber)

  private def findOne(id: Int): Future[Option[World]] =
    for {
      coll <- mongoClient.worldCollectionFuture
      w <- coll.find(document("id" -> id)).one[World]
    } yield w

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
}
