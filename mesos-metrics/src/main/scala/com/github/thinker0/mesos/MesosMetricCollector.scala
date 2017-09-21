package com.github.thinker0.mesos

import java.net.URL

import com.twitter.app.{App, Flag}
import com.twitter.heron.api.Config
import com.twitter.heron.common.basics.ByteAmount
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.{LocalCluster, StormSubmitter}
import org.slf4j.LoggerFactory

object MesosMetricCollector extends App {
  final val logger = LoggerFactory getLogger MesosMetricCollector.this.getClass.getName

  val mesosMaster: Flag[String] = flag[String](name = "mesos.master", default = "http://localhost:5050", help = "mesos master")

  def main(): Unit = {
    Dtabs.init()

    val builder = new TopologyBuilder
    val topologyConfig = new Config()

    builder.setSpout("master", new MasterMetricsSpout(new URL(mesosMaster())), 1)

    builder.setBolt("metrics", new MasterMetricsBolt).shuffleGrouping("master")

    topologyConfig.put(Config.TOPOLOGY_WORKER_CHILDOPTS, s"-Dfile.encoding=UTF-8 ")

    if (args.isEmpty) {
      val cluster = new LocalCluster()
      cluster.submitTopology("mesos-metric-collector-topology", topologyConfig, builder.createTopology())
    } else {
      com.twitter.heron.api.Config.setComponentRam(topologyConfig, "stream-kafka", ByteAmount.fromMegabytes(500))
      com.twitter.heron.api.Config.setComponentRam(topologyConfig, "parse-message", ByteAmount.fromMegabytes(500))
      com.twitter.heron.api.Config.setComponentRam(topologyConfig, "legibility-calculator", ByteAmount.fromMegabytes(500))
      com.twitter.heron.api.Config.setContainerCpuRequested(topologyConfig, 1)
      com.twitter.heron.api.Config.setContainerRamRequested(topologyConfig, ByteAmount.fromMegabytes(1500))
      com.twitter.heron.api.Config.setContainerDiskRequested(topologyConfig, ByteAmount.fromGigabytes(10))

      StormSubmitter.submitTopology(args.head, topologyConfig, builder.createTopology())
    }

  }
}
