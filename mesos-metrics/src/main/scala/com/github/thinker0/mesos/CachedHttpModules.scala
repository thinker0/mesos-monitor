package com.github.thinker0.mesos

import java.util.concurrent.ConcurrentHashMap

import com.google.inject.AbstractModule
import com.twitter.cache.{ConcurrentMapCache, EvictingCache}
import com.twitter.conversions.time._
import com.twitter.finagle.transport.Transport
import com.twitter.finagle.{Http, Service, http}
import com.twitter.util.Future
import org.slf4j.LoggerFactory
import com.twitter.conversions.storage._
 import com.twitter.util.StorageUnit

object CachedHttpModules extends AbstractModule {
  final val logger = LoggerFactory getLogger this.getClass.getName

  val map = new ConcurrentHashMap[String, Future[Service[http.Request, http.Response]]]()
  val cache = new ConcurrentMapCache[String, Service[http.Request, http.Response]](map)
  val evictionCache = EvictingCache(cache)

  override protected def configure(): Unit = {}

  def provideHttpService(host: String, port: Int): Future[Service[http.Request, http.Response]] = {
    val hostAndPort = s"$host:$port"
    logger.info(s"HttpClient: $hostAndPort")
    evictionCache.getOrElseUpdate(hostAndPort) {
      logger.info(s"New HttpClient: $hostAndPort")
      val client = Future {
        Http.client
          .withRequestTimeout(30000.millis)
          .configured(Transport.Options(noDelay = true, reuseAddr = true))
          .withSession.acquisitionTimeout(30000.millis)
          .withSessionPool.maxSize(1)
            .withMaxResponseSize(10.megabytes)
          .newService(hostAndPort, "mesos-agents")
      }
      evictionCache.set(hostAndPort, client)
      client
    }
  }

}