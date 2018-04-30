package model

import java.time.LocalDate

import akka.actor.{Actor, ActorLogging, Props}
import cache.Cache

import scala.concurrent.ExecutionContextExecutor

object OfferHandlerActor {

  def actorProps(c: Cache[Offer]): Props = Props(new OfferHandlerActor(c))

  //messages
  case class Create(offer: Offer)

  case class CreatedWith(id: String)

  case class FindById(id: String)

  case class Found(offer: Offer)

  case class DeleteById(id: String)

  case class Deleted()

  case class Missing()

  case class Invalid()

}

class OfferHandlerActor(offers: Cache[Offer]) extends Actor with ActorLogging {

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  import OfferHandlerActor._

  override def receive: Receive = {

    case Create(offer) => if (offer.expires.isAfter(LocalDate.now())) {
      offers.set(offer.id, offer)
      sender ! CreatedWith(offer.id)
    }
    else sender ! Invalid

    case FindById(id) =>
      offers.get(id) match {
        case Some(offer) => if (offer.expires.isAfter(LocalDate.now())) {
          sender ! Found(offer)
        } else sender ! Missing
        case None => sender ! Missing
      }
    case DeleteById(id) =>
      offers.get(id) match {
        case Some(_) => offers.del(id); sender ! Deleted()
        case None => sender ! Missing
      }
    case _ => sender ! Invalid
  }
}