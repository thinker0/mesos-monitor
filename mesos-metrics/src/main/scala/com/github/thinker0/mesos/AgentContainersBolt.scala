package com.github.thinker0.mesos

import java.lang.{Double => JDouble, Long => JLong}
import java.net.URL
import java.util.{Map => JMap}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.twitter.finagle.http.{RequestBuilder, Response}
import com.twitter.heron.api.metric.{MeanReducer, MeanReducerState, MultiCountMetric, MultiReducedMetric}
import com.twitter.io.Buf
import org.apache.mesos.v1.agent.agent.Response.GetContainers
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.topology.base.BaseBasicBolt
import org.apache.storm.tuple.{Fields, Tuple}
import org.apache.storm.{task, topology}
import org.slf4j.LoggerFactory


@SerialVersionUID(1)
class AgentContainersBolt(url: URL) extends BaseBasicBolt {
  private final val logger = LoggerFactory getLogger this.getClass.getName

  private var countMetrics: MultiCountMetric = _
  private var reducedMetrics: MultiReducedMetric[MeanReducerState, Number, JDouble] = _

  private var objectMapper: ObjectMapper with ScalaObjectMapper = _

  val requestBody = Buf.Utf8("""{"type":"GET_CONTAINERS"}""".trim)

  // stormConf is now a typed Map
  override def prepare(stormConf: JMap[_, _], context: task.TopologyContext) = {
    this.countMetrics = new MultiCountMetric with Serializable
    this.reducedMetrics = new MultiReducedMetric[MeanReducerState, Number, JDouble](new MeanReducer with Serializable) with Serializable
    this.objectMapper = (new ObjectMapper with ScalaObjectMapper)
      .registerModule(DefaultScalaModule)
      .asInstanceOf[ObjectMapper with ScalaObjectMapper]

  }

  override def execute(tuple: Tuple, collector: BasicOutputCollector) = {
    val hostName = tuple getStringByField "hostname"
    val hostPort = tuple getLongByField "port"
    val url = new URL(s"http://$hostName:$hostPort/api/v1")
    val agentService = CachedHttpModules.provideHttpService(hostName, hostPort.toInt)
    val request = RequestBuilder().url(url).setHeader("Host", url.getHost).setHeader("Content-Type", "application/json").buildPost(requestBody)
    (for {
      service <- agentService
      response <- service(request)
    } yield {
      response.statusCode match {
        case 200 =>
          Option(objectMapper.readValue[GetContainers](response.getInputStream()))
        case _ =>
          None
      }
    }).onSuccess { getContainers: Option[GetContainers] =>

    }.onFailure { t: Throwable =>
      logger.error(t.getMessage, t)
    }
  }

  override def declareOutputFields(declarer: topology.OutputFieldsDeclarer) = {
    declarer.declare(new Fields("word"))
  }

}
