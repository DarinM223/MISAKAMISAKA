package com.d_m.worker

import java.net.URL
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorRef}
import akka.io.IO
import akka.util.Timeout
import org.jsoup.Jsoup
import spray.can.Http
import spray.http.HttpEntity.{Empty, NonEmpty}
import spray.http.HttpResponse
import spray.httpx.RequestBuilding._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import akka.pattern._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

/**
 * A utility object for Worker with functions for
 * calling the other actors, sending requests,
 * and parsing the http body
 */
object WorkerUtils {
  // Implicit values for Akka IO
  implicit val timeout = Timeout(FiniteDuration(1, TimeUnit.MINUTES))
  implicit val system = ActorSystem()

  /**
   * Queries both the DNS Resolver and the Rate Limiter concurrently
   * @param url the url to resolve and check rate limit for
   * @return a future containing a tuple of the resolved address and the result of the rate limiter query
   */
  def getAddressAndRateLimit(
      url: URL,
      dnsResolver: ActorRef,
      rateLimiter: ActorRef): Future[(String, com.d_m.rate_limiter.Message.Message)] = {

    val getAddressFuture: Future[String] = (dnsResolver ? url).mapTo[String]
    val getRateLimiterResult: Future[com.d_m.rate_limiter.Message.Message] =
      (rateLimiter ? url).mapTo[com.d_m.rate_limiter.Message.Message]

    // Fetch both results concurrently
    for {
      address: String <- getAddressFuture
      result: com.d_m.rate_limiter.Message.Message <- getRateLimiterResult
    } yield (address, result)
  }

  /**
   * Sends a GET http request to an address
   * @param address the address to request
   * @return a future containing the response
   */
  def sendRequest(address: String): Future[HttpResponse] =
    (IO(Http) ? Get(address)).mapTo[HttpResponse]

  /**
   * Given a http response body, returns a list of links in the body
   * @param body the http response body
   * @return list of links in the body
   */
  def retrieveLinksFromBody(body: String): List[String] = {
    val doc = Jsoup.parse(body)
    val links = doc.select("a")
    links.asScala.map(link => link.attr("href")).toList
  }

  /**
   * Parses the http body and returns a list of links contained in the http body
   * @param response
   */
  def parseHttpBody(sender: ActorRef, response: HttpResponse) = response.entity match {
    case body: NonEmpty => retrieveLinksFromBody(body.asString)
    case Empty => List()
  }
}
