Running
=======

In order to run:

0. Start redis server
1. Run DNS Resolver
2. Run Rate Limiter
3. Run workers in certain machines/ports
4. Edit application.conf in supervisor machine to describe the worker machines/ports
5. Run supervisor

To run DNS Resolver:

    sbt "run-main com.d_m.dns_resolver.DNSResolver"
    
To run Rate Limiter:

    sbt "run-main com.d_m.rate_limiter.RateLimiter"

To run worker:

    sbt "run-main com.d_m.worker.Worker <port_name>"

To run supervisor:

    sbt "run-main com.d_m.supervisor.Supervisor"

To run tests:

    sbt test
    
TODO:
=====

* ~~Start DNS Resolver implementation~~
    * ~~For every URL received, get the hostname and return the IP address~~
    * ~~Cache hostname -> IP in Redis so that subsequent requests won't have to be resolved~~
* Start rate limiter implementation (maybe storing a queue in DNSResolver or using another remote service actor)
* Figure out how to start actors in a cluster in a round robin fashion
* Start worker implementation
    * Resolve DNS using DNSResolver
    * If not requested, check domain rate limit
    * If rate limit ok, send request
    * Save some data to db
    * If rate limit not established already, read robots.txt and set the rate limit
    * Parse links and send them back to supervisor to queue
* Start supervisor implementation
    * Start with a list of sample page URLs
    * For each URL, create a new worker
    * Whenever you receive a list of links, add them to some kind of queue (priority queue?)
    * Keep popping off the next URL in the queue and sending them to workers
    * Check if the next URL was already requested before sending to worker (maybe using bloom filter + checking db or just a cache)




