package hello.slick

import java.time.LocalDate
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object PrivateExecutionContext {
  private val executors = Executors.newFixedThreadPool(4)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(executors)
}

object Main {
  import PrivateExecutionContext._
  import slick.jdbc.PostgresProfile.api._

  val shawsharkRedemption = Movie(1L, "The Shawshank Redemption", LocalDate.of(1994, 9, 23), 162)
  val theMatrix = Movie(2L, "The Matrix", LocalDate.of(1999, 3, 31), 134)
  def demoInsertMovie(): Unit = {
    val queryDescription = SlickTables.movieTable += theMatrix
    val futureId: Future[Int] = Connection.db.run(queryDescription)
    futureId.onComplete {
      case Success(newMovieId) => println(s"Query was successful, new id is $newMovieId")
      case Failure(ex)         => println(s"Query failed, reason: $ex")
    }

    Thread.sleep(10000)
  }

  def demoReadAllMovies(): Unit = {
//    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.result)
    val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Matrix%")).result)

    resultFuture.onComplete({
      case Success(movies) => println(s"Fetched ${movies.mkString(", ")}")
      case Failure(ex)     => println(s"Fetching failed, reason: $ex")
    })

    def demoUpdateMovie(): Unit = {
      val resultFuture: Future[Seq[Movie]] = Connection.db.run(SlickTables.movieTable.filter(_.name.like("%Matrix%")).result)

      resultFuture.onComplete({
        case Success(movies) => println(s"Fetched ${movies.mkString(", ")}")
        case Failure(ex) => println(s"Fetching failed, reason: $ex")
      })

    Thread.sleep(10000)
  }
  def main(args: Array[String]): Unit = {
    demoReadAllMovies()
  }
}
