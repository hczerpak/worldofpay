
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import model.Offer

class OfferQuery extends ApiSpecsDefaults {

  "GET /offers/<id>" should "allow a merchant to query offers" in {

    val offer = Offer(UUID.randomUUID().toString, "For Sale", "kg of potatoes", 25.4)
    offersCache.set(offer.id, offer)

    Get(s"/offers/${offer.id}") ~> routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[OfferSearchResponse].offer.id shouldBe offer.id
    }

    Get(s"/offers/${UUID.randomUUID().toString}") ~> routes ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }

  "After the period of time defined on the offer it should expire and further requests to query the offer" should "reflect that somehow" in {
    val offer = Offer(
      UUID.randomUUID().toString,
      "For Sale",
      "kg of potatoes",
      25.4,
      LocalDate.now().minus(1, ChronoUnit.DAYS)
    )
    offersCache.set(offer.id, offer)

    Get(s"/offers/${offer.id}") ~> routes ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }
}
