package com.github.thinker0.mesos

import java.lang.{Double => JDouble, Long => JLong}
import java.util.{Map => JMap}

import com.twitter.heron.api.metric._
import org.apache.storm.topology.BasicOutputCollector
import org.apache.storm.topology.base.BaseBasicBolt
import org.apache.storm.tuple.Tuple
import org.apache.storm.{task, topology}

@SerialVersionUID(1)
class MasterMetricsBolt extends BaseBasicBolt {
  var countMetrics:MultiCountMetric = _
  var reducedMetrics:MultiReducedMetric[MeanReducerState, Number, JDouble] = _

  override def execute(tuple: Tuple, collector: BasicOutputCollector) = {
    tuple getValue 1 match {
      case json: String if json.trim.nonEmpty =>
        reducedMetrics scope "some_avg" update 10
      case _ =>
        countMetrics scope "unexpected_input" incr()
    }
  }

  override def declareOutputFields(declarer: topology.OutputFieldsDeclarer) = {

  }

  // stormConf is now a typed Map
  override def prepare(stormConf: JMap[_, _], context: task.TopologyContext) = {
    super.prepare(stormConf, context)
    countMetrics = new MultiCountMetric
    reducedMetrics = new MultiReducedMetric[MeanReducerState, Number, JDouble](new MeanReducer)
  }

}
