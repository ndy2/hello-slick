package hello.slick

import java.time.LocalDate

object Model {

}

case class Movie(id: Long, name: String, releaseDate: LocalDate, lengthInMin: Int)
