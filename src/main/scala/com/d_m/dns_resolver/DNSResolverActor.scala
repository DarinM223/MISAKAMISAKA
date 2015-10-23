package com.d_m.dns_resolver

import java.net.{URL, InetAddress}

import akka.actor.{ActorRef, Actor}
import org.xbill.DNS._
import redis.RedisClient
import scala.concurrent.Await
import scala.concurrent.duration._

/**
 * Created by darin on 10/20/15.
 */
class DNSResolverActor(originalSender: ActorRef, redis: RedisClient) extends Actor {
  def receive = {
    case url: URL =>
      val timeout = 1 second
      val response = redis.get[String](url.getHost)

      Await.result(response, timeout) match {
        case Some(address) =>
          originalSender ! address
        case None =>
          val addr: InetAddress = Address.getByName(url.getHost)
          redis.set(url.getHost, addr.getHostAddress)
          originalSender !  addr.getHostAddress
      }
  }
}
