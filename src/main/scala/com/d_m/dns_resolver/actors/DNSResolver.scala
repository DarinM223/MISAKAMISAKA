package com.d_m.dns_resolver.actors

import java.net.{InetAddress, URL}

import akka.actor.{Actor, ActorRef}
import org.xbill.DNS._
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Actor that resolves the IP addresses of URLs
 */
class DNSResolver(originalSender: ActorRef, redis: RedisClient) extends Actor {
  def receive = {
    case url: URL =>
      redis.get[String](url.getHost).onSuccess {
        case Some(address) =>
          originalSender ! address
        case None =>
          val addr: InetAddress = Address.getByName(url.getHost)
          redis.set(url.getHost, addr.getHostAddress) onSuccess { case _ =>
            originalSender ! addr.getHostAddress
          }
      }
  }
}
