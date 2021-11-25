package example

import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

object Model {
  final case class World(id: Int, randomNumber: Int)

  implicit val userHandler: BSONDocumentHandler[World] = Macros.handler[World]

  final case class Fortune(id: Int, message: String)

  implicit val fortuneHandler: BSONDocumentHandler[Fortune] = Macros.handler[Fortune]
}
