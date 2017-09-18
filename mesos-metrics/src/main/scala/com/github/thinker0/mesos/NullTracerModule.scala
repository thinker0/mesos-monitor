package com.github.thinker0.mesos

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver}
import com.twitter.finagle.tracing.{NullTracer, Tracer}

object NullTracerModule extends AbstractModule {

  @Singleton
  @Provides
  def provideTracer(): Tracer = {
    NullTracer
  }

  @Singleton
  @Provides
  def provideStatsReceiver(): StatsReceiver = {
    NullStatsReceiver
  }

  override def configure(): Unit = {

  }
}
