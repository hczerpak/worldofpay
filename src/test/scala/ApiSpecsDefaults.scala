import akka.event.Logging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.testkit.TestActorRef
import cache.{Cache, InMemoryCache}
import com.typesafe.config.Config
import model.{Offer, OfferHandlerActor}
import org.scalatest.{FlatSpec, Matchers}


abstract class ApiSpecsDefaults
  extends FlatSpec
    with ScalatestRouteTest
    with Service
    with Matchers {

  override val logger = Logging(system, getClass)
  val offersCache: Cache[Offer] = new InMemoryCache[Offer]
  val handler: TestActorRef[OfferHandlerActor] = TestActorRef[OfferHandlerActor](new OfferHandlerActor(offersCache))

  override def config: Config = testConfig
}