Running
=======

In order to run:

1. Run workers in certain machines/ports
2. Edit application.conf in supervisor machine to describe the worker machines/ports
3. Run supervisor

To run worker:

    sbt "run-main com.d_m.worker.Worker <port_name>"

To run supervisor:

    sbt "run-main com.d_m.supervisor.Supervisor"

