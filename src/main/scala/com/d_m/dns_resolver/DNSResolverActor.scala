package com.d_m.dns_resolver

import akka.actor._

/**
 * Created by darin on 10/20/15.
 */
class DNSResolverActor extends Actor {
  def receive = {
    case domain: String =>
      sender ! "TODO: send IP Address of domain name"
  }
}
