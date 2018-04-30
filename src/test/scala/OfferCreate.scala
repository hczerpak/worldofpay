
import java.util.UUID

import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.Created
import model.Offer

class OfferCreate extends ApiSpecsDefaults {

  val offer = Offer(UUID.randomUUID().toString, "For Sale", "kg of potatoes", 25.4)

  """POST /offers with json body: {"description": <String>, "currency": <String>, "price": <Float>, "expires": "10/10/2020"}""" should "allow a merchant to create a new simple offer" in {
    Post("/offers", OfferCreateRequest(offer.description, offer.currency, offer.price, offer.expires)) ~> routes ~> check {
      val id = responseAs[String]
      offersCache.get(id).isEmpty shouldBe false
    }
  }

  it should "return 201" in {
    Post("/offers", OfferCreateRequest(offer.description, offer.currency, offer.price, offer.expires)) ~> routes ~> check {
      status shouldBe Created
    }
  }

  it should """return json body: {"workflow_id": <string>}""" in {
    Post("/offers", OfferCreateRequest(offer.description, offer.currency, offer.price, offer.expires)) ~> routes ~> check {
      contentType shouldBe `application/json`
    }
  }
}
