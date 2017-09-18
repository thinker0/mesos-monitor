package com.github.thinker0.mesos

import com.twitter.finagle.Dtab

/**
  * Holds different delegation tables
  */
object Dtabs {

  def init(): Unit = {
    Dtab.base = base
  }

  /**
    * base dtab to resolve requests on your local system
    */
  val base = Dtab.read(
    """|/zk##    => /$/com.twitter.serverset;
       |/zk#     => /zk##/127.0.0.1:2181;
       |/s#      => /zk#/service;
       |/env     => /s#/local;
       |/s       => /env;""".stripMargin)
}