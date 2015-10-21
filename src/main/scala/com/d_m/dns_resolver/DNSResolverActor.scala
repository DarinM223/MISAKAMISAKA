package com.d_m.dns_resolver

import java.net.InetAddress

import akka.actor.{ActorRef, Actor}
import org.xbill.DNS._

/**
 * Created by darin on 10/20/15.
 */
class DNSResolverActor(originalSender: ActorRef) extends Actor {
  def receive = {
    case url: String =>
      val addr: InetAddress = Address.getByName(url)
      originalSender ! addr.getHostAddress
  }
}
