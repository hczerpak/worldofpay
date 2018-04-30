
import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import model.Offer

class OfferDelete extends ApiSpecsDefaults {

  "DELETE /offers/<id>" should "allow a merchant to cancel offers" in {
    val offer = Offer(UUID.randomUUID().toString, "For Sale", "kg of potatoes", 25.4)
    offersCache.set(offer.id, offer)

    Delete(s"/offers/${offer.id}") ~> routes ~> check {
      status shouldBe StatusCodes.OK
    }

    //second delete should fail
    Delete(s"/offers/${offer.id}") ~> routes ~> check {
      status shouldBe StatusCodes.NotFound
    }
  }
}
