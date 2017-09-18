package com.github.thinker0.mesos

import java.lang.{Double => JDouble, Long => JLong}
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.{Timer, TimerTask, Map => JMap}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaModule}
import com.twitter.finagle.Service
import com.twitter.finagle.http.{Request, RequestBuilder, Response}
import com.twitter.heron.api.metric.{MeanReducer, MeanReducerState, MultiCountMetric, MultiReducedMetric}
import com.twitter.io.Buf
import com.twitter.util.Future
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.apache.mesos.v1.master.master.Response.GetTasks
import org.apache.storm.spout.SpoutOutputCollector
import org.apache.storm.task.TopologyContext
import org.apache.storm.topology.OutputFieldsDeclarer
import org.apache.storm.topology.base.BaseRichSpout
import org.apache.storm.tuple.Fields
import org.slf4j.LoggerFactory

import scala.concurrent.duration._


@SerialVersionUID(1)
class MasterMetricsSpout(url: URL) extends BaseRichSpout {
  private final val logger = LoggerFactory getLogger this.getClass.getName
  private var lastUpdated: Long = 0
  private var lastExecuted: Long = 0
  private var lastTasks: String = ""
  private val cacheBuildDuration = 1.minutes
  private var channel: ManagedChannel = _
  private var masterHost: Future[Service[Request, Response]] = _

  private var countMetrics: MultiCountMetric = _
  private var reducedMetrics: MultiReducedMetric[MeanReducerState, Number, JDouble] = _

  private var spoutOutputCollector: SpoutOutputCollector = _

  private var scheduleTimer: Timer = _

  override def open(map: JMap[_, _], context: TopologyContext, spoutOutputCollector: SpoutOutputCollector) = {
    this.spoutOutputCollector = spoutOutputCollector
    this.countMetrics = new MultiCountMetric with Serializable
    this.reducedMetrics = new MultiReducedMetric[MeanReducerState, Number, JDouble](new MeanReducer with Serializable) with Serializable
    this.masterHost = CachedHttpModules.provideHttpService(url.getHost, url.getPort)
    this.scheduleTimer = new Timer(true)
    this.channel = ManagedChannelBuilder.forAddress(url.getHost, url.getPort)
      .usePlaintext(true)
      .build()
    logger.info(s"Host: ${url}")
    
    scheduleTimer.scheduleAtFixedRate(new TimerTask() {
      val requestBody = Buf.Utf8("""{"type":"GET_TASKS","get_metrics":{"timeout":{"nanoseconds":5000000000}}}""".trim)
      val objectMapper: ObjectMapper with ScalaObjectMapper = (new ObjectMapper with ScalaObjectMapper)
        .registerModule(DefaultScalaModule)
        .asInstanceOf[ObjectMapper with ScalaObjectMapper]


      def run() = {
        val startTime = System.currentTimeMillis()
        val request = RequestBuilder().url(url).setHeader("Host", url.getHost).setHeader("Content-Type", "application/json").buildPost(requestBody)
        logger.info(s"Scheduler Updated  $request ${request.contentString} !!!!!!!!!!!!!")
        for {
          masterExecutor <- masterHost
          response <- masterExecutor(request)
        } yield {
          response.statusCode match {
            case 200 =>
              lastUpdated = System.currentTimeMillis()
              logger.info(s"Response: $url ${response.statusCode} ${System.currentTimeMillis() - startTime}ms")
              lastTasks = response.contentString
              val tasks = objectMapper.readValue[GetTasks](response.contentString)
              logger.info(s"$tasks")
            case 307 =>
              val redir = new URL(s"${url.getProtocol}:${response.location.get}")
              val request2 = RequestBuilder().url(redir).setHeader("Host", redir.getHost).setHeader("Content-Type", "application/json").buildPost(requestBody)
              logger.info(s"new Leader: $redir")
              logger.info(s"${request2} ${request2.contentString}")
              for {
                leaderHost <- CachedHttpModules.provideHttpService(redir.getHost, redir.getPort)
                response2 <- leaderHost(request2)
              } yield {
                response2.statusCode match {
                  case 200 =>
                    lastUpdated = System.currentTimeMillis()
                    logger.info(s"Response: ${redir} ${response2.statusCode} ${System.currentTimeMillis() - startTime}ms")
                    lastTasks = response2.contentString
                  case _ =>
                    logger.info(s"Response: ${redir} ${response2.statusCode} ${System.currentTimeMillis() - startTime}ms")
                }
              }
            case _ =>
              logger.info(s"Response: ${url} ${response.statusCode} ${response.contentString} ${System.currentTimeMillis() - startTime}ms")
          }
        }
      }
    }, 1000, cacheBuildDuration.toMillis)
    logger.info("Opened")
  }

  override def nextTuple(): Unit = {
    if (lastExecuted != lastUpdated) {
      logger.info(s"Start $url")

      lastExecuted = lastUpdated
    } else {
      // logger.info(s"Start $lastExecuted = $lastUpdated")
    }
  }

  override def activate(): Unit = {
    logger.info("Activated")
  }

  override def close(): Unit = {
    logger.info("Close")
    scheduleTimer.purge()
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  override def deactivate(): Unit = {
    logger.info("Deactivate")
    scheduleTimer.cancel()
  }

  override def declareOutputFields(declarer: OutputFieldsDeclarer): Unit = {
    declarer.declare(new Fields("word"))
  }

}
