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
package io.techcode.streamy.tcp.component

import akka.Done
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.techcode.streamy.TestSystem
import io.techcode.streamy.tcp.event.{TcpConnectionCloseEvent, TcpConnectionCreateEvent}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Tcp sink spec.
  */
class TcpSinkSpec extends TestSystem {

  "Tcp sink" should {
    "send data correctly" in {
      val result = Source.single(TcpFlowSpec.Input)
        .runWith(TcpSink.client(TcpSinkSpec.Sink.Simple))

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "send data correctly with reconnect" in {
      val result = Source.single(TcpFlowSpec.Input)
        .runWith(TcpSink.client(TcpSinkSpec.Sink.SimpleWithReconnect))

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "send event correctly" in {
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCreateEvent])
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCloseEvent])
      Source.single(TcpFlowSpec.Input)
        .runWith(TcpSink.client(TcpSinkSpec.Sink.Simple))

      expectMsgClass(classOf[TcpConnectionCreateEvent])
      expectMsgClass(classOf[TcpConnectionCloseEvent])
    }

    "send event correctly with reconnect" in {
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCreateEvent])
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCloseEvent])
      Source.single(TcpFlowSpec.Input)
        .runWith(TcpSink.client(TcpSinkSpec.Sink.SimpleWithReconnect))

      expectMsgClass(classOf[TcpConnectionCreateEvent])
      expectMsgClass(classOf[TcpConnectionCloseEvent])
    }

  }

}

object TcpSinkSpec {

  val Input = ByteString("Hello world !")

  object Sink {

    val Simple = TcpFlow.Client.Config(
      host = "localhost",
      port = 500
    )

    val SimpleWithReconnect = Simple.copy(
      reconnect = Some(TcpFlow.Client.ReconnectConfig(
        minBackoff = 1 seconds,
        maxBackoff = 1 second,
        randomFactor = 0.2D
      ))
    )

  }

}