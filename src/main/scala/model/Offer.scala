package model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

case class Offer(
                        id: String,
                        description: String,
                        currency: String,
                        price: Double,
                        expires: LocalDate = LocalDate.now().plus(7, ChronoUnit.DAYS))
