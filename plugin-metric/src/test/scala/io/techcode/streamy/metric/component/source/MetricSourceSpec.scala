/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.techcode.streamy.metric.component.source

import com.typesafe.config.{Config, ConfigFactory}
import io.techcode.streamy.TestSystem
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.concurrent._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Seconds, Span}
import org.slf4j.Logger

/**
  * Metric source spec.
  */
class MetricSourceSpec extends TestSystem with MockitoSugar with OneInstancePerTest with Eventually {

  // Logger
  val loggerMock: Logger = mock[Logger]

  "Metrics" should {
    "logs some informations" in {
      MetricSource.register(system, MetricSourceSpec.Config)
      MetricSource.jvm().runForeach(loggerMock.info(_))
      eventually(timeout = Timeout(Span(120, Seconds)), Interval(Span(1, Seconds))) {
        verify(loggerMock, atLeastOnce()).info(any())
      }
    }
  }

}

object MetricSourceSpec {

  val Config: Config = ConfigFactory.load("config.conf")

}