package com.d_m.dns_resolver.actors

import java.net.{InetAddress, URL}

import akka.actor.{Actor, ActorRef}
import org.xbill.DNS._
import redis.RedisClient

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by darin on 10/20/15.
 */
class DNSResolver(originalSender: ActorRef, redis: RedisClient) extends Actor {
  def receive = {
    case url: URL =>
      val timeout = 1 second
      val response = redis.get[String](url.getHost)

      Await.result(response, timeout) match {
        case Some(address) =>
          originalSender ! address
        case None =>
          val addr: InetAddress = Address.getByName(url.getHost)
          redis.set(url.getHost, addr.getHostAddress) onSuccess { case _ =>
            originalSender !  addr.getHostAddress
          }
      }
  }
}
