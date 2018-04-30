import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit

import _root_.model.OfferHandlerActor._
import _root_.model.{Offer, OfferHandlerActor}
import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.ContentTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import cache.InMemoryCache
import com.typesafe.config.{Config, ConfigFactory}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.Duration

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val ldtFormat = new RootJsonFormat[LocalDate] {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    val readFormatter = DateTimeFormatter.ofPattern("\"dd/MM/uuuu\"")
    override def read(json: JsValue): LocalDate = LocalDate.parse(json.toString, readFormatter)
    override def write(ldt: LocalDate): JsValue = JsString(ldt.format(formatter))
  }
  implicit val offerConversion = jsonFormat5(Offer)
  implicit val offerCreateConversion = jsonFormat4(OfferCreateRequest)
  implicit val searchResponseConversion = jsonFormat1(OfferSearchResponse)
}

trait Service extends Directives with JsonSupport {

  implicit def timeout = Timeout(Duration(1, TimeUnit.SECONDS))
  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer
  val logger: LoggingAdapter
  val routes =
    pathPrefix("offers") {
      pathEnd {
        post {
          entity(as[OfferCreateRequest]) { r =>
            val offer = Offer(
              UUID.randomUUID().toString,
              r.description,
              r.currency,
              r.price,
              r.expires)
            complete {
              (handler ? OfferHandlerActor.Create(offer)).map {
                case CreatedWith(id) =>
                  HttpResponse(
                    status = Created,
                    entity = HttpEntity(`application/json`, id)
                  )
                case Invalid => HttpResponse(status = BadRequest)
              }
            }
          }
        }
      }
    } ~
      path("offers" / JavaUUID) { (uid) =>
        val id = uid.toString
        delete {
          complete {
            (handler ? OfferHandlerActor.DeleteById(id)).map {
              case Deleted() => HttpResponse(status = OK)
              case Missing => HttpResponse(status = NotFound)
            }
          }
        } ~
          get {
            complete {
              (handler ? OfferHandlerActor.FindById(id)).map {
                case found@OfferHandlerActor.Found(_) => HttpResponse(
                  status = OK,
                  entity = HttpEntity(`application/json`, searchResponseConversion.write(OfferSearchResponse(found.offer)).toString)
                )
                case Missing => HttpResponse(status = NotFound)
              }
            }
          }
      }

  def config: Config

  def handler: ActorRef
}

// domain model
final case class OfferCreateRequest(description: String,
                                    currency: String,
                                    price: Double,
                                    expires: LocalDate)

final case class OfferSearchResponse(offer: Offer)

object WorldOfPayService extends App with Service {
  implicit val system = ActorSystem()
  implicit def executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val handler = system.actorOf(OfferHandlerActor.actorProps(new InMemoryCache[Offer]()))

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}