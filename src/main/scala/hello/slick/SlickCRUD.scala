package hello.slick

import java.time.LocalDate
import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PrivateExecutionContext {
  val executors: ExecutorService = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executors)
}

object SlickCRUD extends App {

  import PrivateExecutionContext._
  import slick.jdbc.PostgresProfile.api._

  val shawsharkRedemption = Movie(1L, "The Shawshank Redemption", LocalDate.of(1994, 9, 23), 162)
  val theMatrix = Movie(2L, "The Matrix", LocalDate.of(1999, 3, 31), 134)
  val phantomMenace = Movie(10L, "Star Wars: A Phantom Menace", LocalDate.of(1999, 5, 16), 133)
  val tomHanks = Actor(1L, "Tom Hanks")
  val juliaRoberts = Actor(2L, "Julia Roberts")
  val liamNeeson = Actor(3L, "Liam Neeson")

  def demoInsertMovie(): Unit = {
    val queryDescription = SlickTables.movieTable += theMatrix
    val futureId: Future[Int] = Connection.db.run(queryDescription)
    futureId.onComplete {
      case Success(newMovieId) => println(s"Query was successful, new id is $newMovieId")
      case Failure(ex)         => println(s"Query failed, reason: $ex")
    }
  }

  def demoReadAllMovies(): Unit = {
    //    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result)
    val resultFuture: Future[Seq[Movie]] =
      Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Matrix%")).result)

    resultFuture.onComplete({
      case Success(movies) => println(s"Fetched ${movies.mkString(", ")}")
      case Failure(ex)     => println(s"Fetching failed, reason: $ex")
    })
  }
  def demoUpdateMovie(): Unit = {
    val resultFuture: Future[Int] =
      Connection.db.run(
        SlickTables.movieTable.filter(_.id === 1L).update(shawsharkRedemption.copy(lengthInMin = 150))
      )

    resultFuture.onComplete {
      case Success(updateMovieId) => println(s"Update was successful, updated id is $updateMovieId")
      case Failure(ex)            => println(s"Update failed, reason: $ex")
    }
  }

  def demoDeleteMovie(): Unit = {
    val resultFuture: Future[Int] =
      Connection.db.run(SlickTables.movieTable.filter(_.id === 2L).delete)

    resultFuture.onComplete {
      case Success(updateMovieId) => println(s"Delete was successful, updated id is $updateMovieId")
      case Failure(ex)            => println(s"Delete failed, reason: $ex")
    }
  }

  def demoInsertActors(): Unit = {
    val queryDescription = SlickTables.actorTable ++= Seq(tomHanks, juliaRoberts)
    val futureId = Connection.db.run(queryDescription)

    futureId.onComplete {
      case Success(_)  => println(s"Insert was successful")
      case Failure(ex) => println(s"Insert failed, reason: $ex")
    }
  }

  def multipleQueriesSingleTransaction(): Unit = {
    val insertMovie = SlickTables.movieTable += phantomMenace
    val insertActor = SlickTables.actorTable += liamNeeson

    val queryDescription = DBIO.seq(insertMovie, insertActor)
    Connection.db.run(queryDescription.transactionally).onComplete {
      case Success(_)  => println(s"Insert was successful")
      case Failure(ex) => println(s"Insert failed, reason: $ex")
    }
  }

  def findAllActorsByMovie(movieId: Long): Future[Seq[Actor]] = {
    val joinQuery = SlickTables.movieActorMappingTable
      .filter(_.movieId === movieId)
      .join(SlickTables.actorTable)
      .on(_.actorId === _.id) // select * from movieActorMappingTable m join actorTable a on m.actorId == a.id
      .map(_._2)

    Connection.db.run(joinQuery.result)
  }

//  multipleQueriesSingleTransaction()
  findAllActorsByMovie(4L).onComplete {
    case Success(actors) => println(s"Actors from Star Warrs : $actors")
    case Failure(ex)     => println(s"Query failed. reason : $ex")
  }

  Thread.sleep(10000)
  PrivateExecutionContext.executors.shutdown()
}
