package com.d_m.dns_resolver.actors

import java.net.{InetAddress, URL}

import akka.actor.{ActorRef, Status, Actor}
import com.d_m.RedisException
import org.xbill.DNS._
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * Actor that resolves the IP addresses of URLs
 */
class DNSResolver(redis: RedisClient) extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case (originalSender: ActorRef, url: URL) =>
      val retrieveFromRedis = redis.get[String]("dnsresolve:" + url.getHost) flatMap {
        case Some(address) =>
          Future { address }
        case None =>
          val address: InetAddress = Address.getByName(url.getHost)
          redis.set("dnsresolve:" + url.getHost, address.getHostAddress) flatMap {
            case success if success => Future { address.getHostAddress }
            case _ => Future { "Error!" }
          } map { _ => address.getHostAddress }
      }

      retrieveFromRedis onComplete {
        case Success(address: String) => originalSender ! address
        case Failure(e) => throw RedisException(e.getMessage)
      }
    case _ => println("Received unexpected type")
  }
}
