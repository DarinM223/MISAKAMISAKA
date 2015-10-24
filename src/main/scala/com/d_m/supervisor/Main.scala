package com.d_m.supervisor

import akka.actor.{Props, ActorSystem}
import com.d_m.supervisor.actors.Supervisor

/**
 * Created by darin on 10/20/15.
 */
object Main extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  val system = ActorSystem("Supervisor", config.getConfig("Supervisor"))

  // Start main supervisor actor
  val supervisorActor = system.actorOf(Props[Supervisor], "SupervisorActor")
  println("Supervisor started at port: " + config.getConfig("Supervisor").getInt("akka.remote.netty.tcp.port"))
}
