package com.d_m.dns_cache

import akka.actor._

/**
 * Created by darin on 10/20/15.
 */
class DNSCacheActor extends Actor {
  def receive = {
    case domain: String =>
      sender ! "TODO: send IP Address of domain name"
  }
}
