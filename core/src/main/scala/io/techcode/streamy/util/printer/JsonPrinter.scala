/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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
package io.techcode.streamy.util.printer

import akka.util.{ByteString, ByteStringBuilder}
import io.techcode.streamy.util.json.Json

/**
  * Represent a [[Json]] printer that provide an efficient way to print [[Json]].
  *
  * @param pkt input to print.
  */
abstract class JsonPrinter(pkt: Json) {

  // Used to build bytestring directly
  lazy protected val builder: ByteStringBuilder = ByteString.newBuilder

  /**
    * Attempt to print input [[Json]].
    *
    * @return [[ByteString]] object result of parsing or [[None]].
    */
  final def print(): Option[ByteString] = {
    if (process()) {
      Some(builder.result())
    } else {
      None
    }
  }

  /**
    * This method must be override and considered as root rule printing.
    *
    * @return true if printing succeeded, otherwise false.
    */
  def process(): Boolean

  /**
    * Compute error message report.
    *
    * @return error message report.
    */
  def error(): String = "Unexpected printing error occured"

}