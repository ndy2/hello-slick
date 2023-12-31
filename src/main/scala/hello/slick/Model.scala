package hello.slick

import java.time.LocalDate

object SlickTables {
  import slick.jdbc.PostgresProfile.api._

  class MovieTable(tag: Tag) extends Table[Movie](tag, Some("movies"), "Movie") {

    def id = column[Long]("movie_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def releaseDate = column[LocalDate]("release_date")
    def lengthInMin = column[Int]("length_in_min")

    // mapping function to the case class
    override def * =
      (id, name, releaseDate, lengthInMin) <> (Movie.tupled, Movie.unapply)
  }
  lazy val movieTable = TableQuery[MovieTable]

  class ActorTable(tag: Tag) extends Table[Actor](tag, Some("movies"), "Actor") {

    def id = column[Long]("actor_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    // mapping function to the case class
    override def * =
      (id, name) <> (Actor.tupled, Actor.unapply)
  }
  lazy val actorTable = TableQuery[ActorTable]

  class MovieActorMappingTable(tag: Tag) extends Table[MovieActorMapping](tag, Some("movies"), "MovieActorMapping") {

    def id = column[Long]("movie_actor_id", O.PrimaryKey, O.AutoInc)
    def movieId = column[Long]("movie_id")
    def actorId = column[Long]("actor_id")

    // mapping function to the case class
    override def * =
      (id, movieId, actorId) <> (MovieActorMapping.tupled, MovieActorMapping.unapply)
  }
  lazy val movieActorMappingTable = TableQuery[MovieActorMappingTable]
}

case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
case class Actor(id: Long, name: String)
case class MovieActorMapping(id: Long, movieId: Long, actorId: Long)
