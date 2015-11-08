package com.d_m.supervisor

import akka.actor.{Props, ActorSystem}
import akka.cluster.Cluster
import com.d_m.supervisor.actors.{Producer, Supervisor}
import com.d_m.worker.actors.Worker

/**
 * Main application for the supervisor program
 */
object Main extends App {
  import com.typesafe.config.ConfigFactory

  val config = ConfigFactory.load()
  val system = ActorSystem("Supervisor", config.getConfig("Supervisor"))

  Cluster(system)

  // Start main supervisor actor
  val supervisorActor = system.actorOf(Props[Supervisor], "SupervisorActor")
  val worker = system.actorOf(Props[Worker], "WorkerActor")
  val producer = system.actorOf(Props[Producer], "ProducerActor")

  println("Supervisor started at port: " + config.getConfig("Supervisor").getInt("akka.remote.netty.tcp.port"))
}
